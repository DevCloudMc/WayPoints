package org.devcloud.waypoints.service

import be.seeseemelk.mockbukkit.MockBukkit
import java.nio.file.Path
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.domain.WaypointLocation
import org.devcloud.waypoints.domain.error.WaypointError
import org.devcloud.waypoints.storage.yaml.YamlStorageBackend
import org.devcloud.waypoints.util.Outcome
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WaypointServiceTest {
    @TempDir lateinit var tmp: Path

    private val owner = UUID.randomUUID()
    private val loc = WaypointLocation("w", 0.0, 64.0, 0.0, 0f, 0f)

    @BeforeAll
    fun setup() {
        if (!MockBukkit.isMocked()) MockBukkit.mock()
    }

    @AfterAll
    fun tearDown() {
        if (MockBukkit.isMocked()) MockBukkit.unmock()
    }

    private fun newService(): WaypointService {
        val backend = YamlStorageBackend(tmp.resolve("svc-${System.nanoTime()}.yml"))
        backend.init().get()
        val svc =
            WaypointService(
                backend,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            )
        svc.warmGlobals().get()
        svc.warmPlayer(owner).get()
        return svc
    }

    @Test
    fun `creates personal waypoint`() {
        val svc = newService()
        val r = svc.createPersonal(owner, "home", MapCursor.Type.PLAYER_OFF_MAP, loc, 5).get()
        assertTrue(r is Outcome.Ok)
        assertEquals(1, svc.listOwned(owner).size)
    }

    @Test
    fun `rejects duplicate name`() {
        val svc = newService()
        svc.createPersonal(owner, "home", MapCursor.Type.PLAYER_OFF_MAP, loc, 5).get()
        val r = svc.createPersonal(owner, "home", MapCursor.Type.PLAYER_OFF_MAP, loc, 5).get()
        assertEquals(WaypointError.NameTaken("home"), (r as Outcome.Err).error)
    }

    @Test
    fun `rejects invalid name`() {
        val svc = newService()
        val r = svc.createPersonal(owner, "bad name!", MapCursor.Type.PLAYER_OFF_MAP, loc, 5).get()
        assertEquals(WaypointError.InvalidName("bad name!"), (r as Outcome.Err).error)
    }

    @Test
    fun `enforces limit`() {
        val svc = newService()
        repeat(3) {
            svc.createPersonal(owner, "wp$it", MapCursor.Type.PLAYER_OFF_MAP, loc, 3).get()
        }
        val r = svc.createPersonal(owner, "extra", MapCursor.Type.PLAYER_OFF_MAP, loc, 3).get()
        assertEquals(WaypointError.LimitReached(3, 3), (r as Outcome.Err).error)
    }

    @Test
    fun `delete returns the waypoint`() {
        val svc = newService()
        svc.createPersonal(owner, "x", MapCursor.Type.PLAYER_OFF_MAP, loc, 5).get()
        val r = svc.deletePersonal(owner, "x").get()
        assertTrue(r is Outcome.Ok)
        assertNull(svc.findOwned(owner, "x"))
    }

    @Test
    fun `rename moves waypoint to new key`() {
        val svc = newService()
        svc.createPersonal(owner, "old", MapCursor.Type.PLAYER_OFF_MAP, loc, 5).get()
        val r = svc.renamePersonal(owner, "old", "new").get() as Outcome.Ok
        assertEquals("new", r.value.name)
        assertNull(svc.findOwned(owner, "old"))
        assertNotNull(svc.findOwned(owner, "new"))
    }

    @Test
    fun `global waypoint lifecycle`() {
        val svc = newService()
        val r = svc.createGlobal("spawn", MapCursor.Type.RED_X, loc).get() as Outcome.Ok
        assertEquals("spawn", r.value.name)
        assertNotNull(svc.findGlobal("spawn"))
        svc.deleteGlobal("spawn").get()
        assertNull(svc.findGlobal("spawn"))
    }
}
