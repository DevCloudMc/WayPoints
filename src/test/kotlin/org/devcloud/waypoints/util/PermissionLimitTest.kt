package org.devcloud.waypoints.util

import io.mockk.every
import io.mockk.mockk
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.permissions.Permissible
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PermissionLimitTest {
    private fun permInfo(node: String, value: Boolean): PermissionAttachmentInfo {
        val info = mockk<PermissionAttachmentInfo>()
        every { info.permission } returns node
        every { info.value } returns value
        return info
    }

    @Test
    fun `returns 0 when no matching permission`() {
        val p = mockk<Permissible>()
        every { p.effectivePermissions } returns mutableSetOf(permInfo("foo.bar", true))
        assertEquals(0, PermissionLimit.compute(p, "waypoints.limit"))
    }

    @Test
    fun `returns highest matching permission`() {
        val p = mockk<Permissible>()
        every { p.effectivePermissions } returns mutableSetOf(
            permInfo("waypoints.limit.3", true),
            permInfo("waypoints.limit.10", true),
            permInfo("waypoints.limit.7", true),
        )
        assertEquals(10, PermissionLimit.compute(p, "waypoints.limit"))
    }

    @Test
    fun `ignores negated permissions`() {
        val p = mockk<Permissible>()
        every { p.effectivePermissions } returns mutableSetOf(
            permInfo("waypoints.limit.10", false),
            permInfo("waypoints.limit.5", true),
        )
        assertEquals(5, PermissionLimit.compute(p, "waypoints.limit"))
    }

    @Test
    fun `ignores non-numeric suffixes`() {
        val p = mockk<Permissible>()
        every { p.effectivePermissions } returns mutableSetOf(
            permInfo("waypoints.limit.foo", true),
            permInfo("waypoints.limit.4", true),
        )
        assertEquals(4, PermissionLimit.compute(p, "waypoints.limit"))
    }
}
