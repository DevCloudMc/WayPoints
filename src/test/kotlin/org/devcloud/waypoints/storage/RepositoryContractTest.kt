package org.devcloud.waypoints.storage

import be.seeseemelk.mockbukkit.MockBukkit
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class RepositoryContractTest {
    protected abstract fun newBackend(): StorageBackend

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

    private fun global(name: String) =
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
        val backend = newBackend()
        backend.init().get()
        val owner = UUID.randomUUID()
        val wp = personal(owner, "home")
        backend.waypoints.save(wp).get()
        assertEquals(wp, backend.waypoints.findById(wp.id).get())
        backend.close()
    }

    @Test
    fun `findByOwnerAndName returns matching personal`() {
        val backend = newBackend()
        backend.init().get()
        val owner = UUID.randomUUID()
        val wp = personal(owner, "mine")
        backend.waypoints.save(wp).get()
        assertEquals(wp, backend.waypoints.findByOwnerAndName(owner, "mine").get())
        assertNull(backend.waypoints.findByOwnerAndName(owner, "other").get())
        backend.close()
    }

    @Test
    fun `findGlobalByName returns matching global`() {
        val backend = newBackend()
        backend.init().get()
        val wp = global("spawn")
        backend.waypoints.save(wp).get()
        assertEquals(wp, backend.waypoints.findGlobalByName("spawn").get())
        assertNull(backend.waypoints.findGlobalByName("nope").get())
        backend.close()
    }

    @Test
    fun `delete returns false when missing`() {
        val backend = newBackend()
        backend.init().get()
        assertFalse(backend.waypoints.delete(WaypointId.random()).get())
        backend.close()
    }

    @Test
    fun `deleteAllByOwner removes only owner waypoints`() {
        val backend = newBackend()
        backend.init().get()
        val a = UUID.randomUUID()
        val b = UUID.randomUUID()
        backend.waypoints.save(personal(a, "x")).get()
        backend.waypoints.save(personal(a, "y")).get()
        backend.waypoints.save(personal(b, "z")).get()
        assertEquals(2, backend.waypoints.deleteAllByOwner(a).get())
        assertEquals(0, backend.waypoints.listByOwner(a).get().size)
        assertEquals(1, backend.waypoints.listByOwner(b).get().size)
        backend.close()
    }

    @Test
    fun `share lifecycle`() {
        val backend = newBackend()
        backend.init().get()
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
        backend.close()
    }

    @Test
    fun `profile defaults for missing uuid`() {
        val backend = newBackend()
        backend.init().get()
        val uuid = UUID.randomUUID()
        val p = backend.players.loadProfile(uuid).get()
        assertEquals(VisibilityState.ALL_VISIBLE, p.visibility)
        assertEquals(uuid, p.uuid)
        backend.close()
    }

    @Test
    fun `profile saves persist`() {
        val backend = newBackend()
        backend.init().get()
        val uuid = UUID.randomUUID()
        val p = PlayerProfile(uuid, VisibilityState(hideGlobal = true))
        backend.players.saveProfile(p).get()
        assertEquals(p, backend.players.loadProfile(uuid).get())
        backend.close()
    }

    @Test
    fun `export and import round-trip`() {
        val backend = newBackend()
        backend.init().get()
        val owner = UUID.randomUUID()
        backend.waypoints.save(personal(owner, "a")).get()
        backend.waypoints.save(global("g")).get()
        val snap = backend.exportAll().get()

        val backend2 = newBackend()
        backend2.init().get()
        backend2.importAll(snap).get()
        assertEquals(snap.waypoints.toSet(), backend2.exportAll().get().waypoints.toSet())
        backend.close()
        backend2.close()
    }
}
