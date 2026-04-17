package org.devcloud.waypoints.domain

import be.seeseemelk.mockbukkit.MockBukkit
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.bukkit.map.MapCursor
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IconRegistryTest {
    @BeforeAll
    fun setup() {
        if (!MockBukkit.isMocked()) MockBukkit.mock()
    }

    @AfterAll
    fun tearDown() {
        if (MockBukkit.isMocked()) MockBukkit.unmock()
    }

    @Test
    fun `parse is case-insensitive`() {
        assertEquals(MapCursor.Type.RED_X, IconRegistry.parse("red_x"))
        assertEquals(MapCursor.Type.RED_X, IconRegistry.parse("RED_X"))
    }

    @Test
    fun `parse returns null for unknown name`() {
        assertNull(IconRegistry.parse("nope"))
    }

    @Test
    fun `default icon is PLAYER_OFF_MAP`() {
        assertEquals(MapCursor.Type.PLAYER_OFF_MAP, IconRegistry.SAFE_DEFAULT)
    }
}
