package org.devcloud.waypoints.domain

import io.mockk.mockk
import java.time.Instant
import java.util.UUID
import org.bukkit.map.MapCursor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WaypointTest {
    private val loc = WaypointLocation("w", 0.0, 0.0, 0.0, 0f, 0f)
    private val icon = mockk<MapCursor.Type>()

    @Test
    fun `global waypoint cannot have an owner`() {
        assertThrows<IllegalArgumentException> {
            Waypoint(
                id = WaypointId.random(),
                owner = UUID.randomUUID(),
                name = "x",
                icon = icon,
                location = loc,
                scope = WaypointScope.GLOBAL,
                createdAt = Instant.EPOCH,
            )
        }
    }

    @Test
    fun `personal waypoint requires an owner`() {
        assertThrows<IllegalArgumentException> {
            Waypoint(
                id = WaypointId.random(),
                owner = null,
                name = "x",
                icon = icon,
                location = loc,
                scope = WaypointScope.PERSONAL,
                createdAt = Instant.EPOCH,
            )
        }
    }
}
