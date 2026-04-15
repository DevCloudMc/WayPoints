package org.devcloud.waypoints.service

import io.mockk.every
import io.mockk.mockk
import org.bukkit.Location
import org.bukkit.World
import org.devcloud.waypoints.domain.WaypointLocation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WaypointDistanceServiceTest {
    @Test
    fun `horizontal returns null for different worlds`() {
        val world = mockk<World>()
        every { world.name } returns "a"
        val loc = Location(world, 0.0, 64.0, 0.0)
        val wp = WaypointLocation("b", 3.0, 64.0, 4.0, 0f, 0f)
        assertNull(WaypointDistanceService.horizontal(loc, wp))
    }

    @Test
    fun `horizontal computes 3-4-5 triangle`() {
        val world = mockk<World>()
        every { world.name } returns "w"
        val loc = Location(world, 0.0, 64.0, 0.0)
        val wp = WaypointLocation("w", 3.0, 64.0, 4.0, 0f, 0f)
        assertEquals(5.0, WaypointDistanceService.horizontal(loc, wp))
    }

    @Test
    fun `formatLabel rounds to integer when decimals is 0`() {
        assertEquals("home 5 m", WaypointDistanceService.formatLabel("home", 4.7, 0))
    }
}
