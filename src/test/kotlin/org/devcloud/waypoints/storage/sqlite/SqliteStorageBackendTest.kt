package org.devcloud.waypoints.storage.sqlite

import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.ExecutionException
import kotlin.test.assertFailsWith
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.domain.WaypointLocation
import org.devcloud.waypoints.domain.WaypointScope
import org.devcloud.waypoints.storage.RepositoryContractTest
import org.devcloud.waypoints.storage.StorageBackend
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class SqliteStorageBackendTest : RepositoryContractTest() {
    @TempDir lateinit var tmp: Path

    override fun newBackend(name: String): StorageBackend =
        SqliteStorageBackend(tmp.resolve("$name.db"))

    @Test
    fun `two global waypoints with the same name are rejected`() {
        val backend = newBackend().also { it.init().get() }
        fun global() =
            Waypoint(
                id = WaypointId.random(),
                owner = null,
                name = "spawn",
                icon = MapCursor.Type.RED_X,
                location = WaypointLocation("w", 0.0, 64.0, 0.0, 0f, 0f),
                scope = WaypointScope.GLOBAL,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            )
        backend.waypoints.save(global()).get()
        assertFailsWith<ExecutionException> { backend.waypoints.save(global()).get() }
    }
}
