package org.devcloud.waypoints.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.bukkit.Bukkit
import org.bukkit.World
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class WaypointLocationTest {
    @BeforeEach
    fun setup() {
        mockkStatic(Bukkit::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Bukkit::class)
    }

    @Test
    fun `resolve returns null when world is missing`() {
        every { Bukkit.getWorld("nope") } returns null
        val loc = WaypointLocation("nope", 1.0, 64.0, 1.0, 0f, 0f)
        assertNull(loc.resolve())
    }

    @Test
    fun `resolve returns Bukkit Location when world is loaded`() {
        val world = mockk<World>()
        every { Bukkit.getWorld("w") } returns world
        val loc = WaypointLocation("w", 1.5, 64.0, -3.25, 90f, -45f).resolve()
        assertEquals(1.5, loc?.x)
        assertEquals(world, loc?.world)
    }
}
