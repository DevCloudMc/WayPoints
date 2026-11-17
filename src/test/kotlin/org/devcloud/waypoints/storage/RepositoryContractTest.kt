package org.devcloud.waypoints.storage

import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.domain.PlayerProfile
import org.devcloud.waypoints.domain.VisibilityState
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.domain.WaypointLocation
import org.devcloud.waypoints.domain.WaypointScope
import org.devcloud.waypoints.domain.WaypointShare
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockbukkit.mockbukkit.MockBukkit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class RepositoryContractTest {
    protected abstract fun newBackend(name: String = "test-${System.nanoTime()}"): StorageBackend

    protected lateinit var backend: StorageBackend

    @BeforeEach
    fun openBackend() {
        backend = newBackend()
        backend.init().get()
    }

    @AfterEach
    fun closeBackend() {
        backend.close()
    }

    @BeforeAll
    fun bukkitSetup() {
        if (!MockBukkit.isMocked()) MockBukkit.mock()
    }

    @AfterAll
    fun bukkitTearDown() {
        if (MockBukkit.isMocked()) MockBukkit.unmock()
    }

    private fun personal(owner: UUID, name: String) =
        Waypoint(
            id = WaypointId.random(),
            owner = owner,
            name = name,
            icon = MapCursor.Type.PLAYER_OFF_MAP,
            location = WaypointLocation("w", 1.0, 64.0, 1.0, 0f, 0f),
            scope = WaypointScope.PERSONAL,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

    protected fun global(name: String) =
        Waypoint(
            id = WaypointId.random(),
            owner = null,
            name = name,
            icon = MapCursor.Type.RED_X,
            location = WaypointLocation("w", 0.0, 64.0, 0.0, 0f, 0f),
            scope = WaypointScope.GLOBAL,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

    @Test
    fun `save and findById round-trip`() {
        val owner = UUID.randomUUID()
        val wp = personal(owner, "home")
        backend.waypoints.save(wp).get()
        assertEquals(wp, backend.waypoints.findById(wp.id).get())
    }

    @Test
    fun `delete returns false when missing`() {
        assertFalse(backend.waypoints.delete(WaypointId.random()).get())
    }

    @Test
    fun `deleteAllByOwner removes only owner waypoints`() {
        val a = UUID.randomUUID()
        val b = UUID.randomUUID()
        backend.waypoints.save(personal(a, "x")).get()
        backend.waypoints.save(personal(a, "y")).get()
        backend.waypoints.save(personal(b, "z")).get()
        assertEquals(2, backend.waypoints.deleteAllByOwner(a).get())
        assertEquals(0, backend.waypoints.listByOwner(a).get().size)
        assertEquals(1, backend.waypoints.listByOwner(b).get().size)
    }

    @Test
    fun `share lifecycle`() {
        val owner = UUID.randomUUID()
        val target = UUID.randomUUID()
        val wp = personal(owner, "x")
        backend.waypoints.save(wp).get()
        val share = WaypointShare(wp.id, target, Instant.parse("2026-01-01T00:00:00Z"))
        assertTrue(backend.shares.add(share).get())
        assertFalse(backend.shares.add(share).get(), "duplicate share returns false")
        assertEquals(listOf(share), backend.shares.listSharedWith(target).get())
        assertTrue(backend.shares.remove(wp.id, target).get())
        assertTrue(backend.shares.listSharedWith(target).get().isEmpty())
    }

    @Test
    fun `profile defaults for missing uuid`() {
        val uuid = UUID.randomUUID()
        val p = backend.players.loadProfile(uuid).get()
        assertEquals(VisibilityState.ALL_VISIBLE, p.visibility)
        assertEquals(uuid, p.uuid)
    }

    @Test
    fun `profile saves persist`() {
        val uuid = UUID.randomUUID()
        val p = PlayerProfile(uuid, VisibilityState(hideGlobal = true))
        backend.players.saveProfile(p).get()
        assertEquals(p, backend.players.loadProfile(uuid).get())
    }

    @Test
    fun `export and import round-trip`() {
        val owner = UUID.randomUUID()
        backend.waypoints.save(personal(owner, "a")).get()
        backend.waypoints.save(global("g")).get()
        val snap = backend.exportAll().get()

        val backend2 = newBackend()
        backend2.init().get()
        backend2.importAll(snap).get()
        assertEquals(snap.waypoints.toSet(), backend2.exportAll().get().waypoints.toSet())
        backend2.close()
    }

    @Test
    fun `import replaces existing data`() {
        val owner = UUID.randomUUID()
        val old = personal(owner, "old")
        val new = personal(owner, "new")
        backend.waypoints.save(old).get()
        backend.importAll(StorageSnapshot(listOf(new), emptyList(), emptyList())).get()
        assertEquals(listOf(new), backend.exportAll().get().waypoints)
    }

    @Test
    fun `writes issued before close are on disk`() {
        val name = "reopen-${System.nanoTime()}"
        val writer = newBackend(name)
        writer.init().get()
        val owner = UUID.randomUUID()
        repeat(50) { writer.waypoints.save(personal(owner, "wp$it")) }
        writer.close()

        val reopened = newBackend(name)
        reopened.init().get()
        assertEquals(50, reopened.waypoints.listByOwner(owner).get().size)
        reopened.close()
    }
}
