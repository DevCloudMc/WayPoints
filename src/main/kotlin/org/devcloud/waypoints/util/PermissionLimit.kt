package org.devcloud.waypoints.util

import org.bukkit.permissions.Permissible

object PermissionLimit {
    /**
     * Walks the permissible's effective permissions and returns the highest integer suffix found on
     * a node matching `${prefix}.<N>`. Returns 0 if none match. Negated permissions are ignored.
     */
    fun compute(permissible: Permissible, prefix: String): Int {
        val needle = "$prefix."
        var max = 0
        for (info in permissible.effectivePermissions) {
            if (!info.value) {
                continue
            }
            val node = info.permission
            if (!node.startsWith(needle)) {
                continue
            }
            val tail = node.substring(needle.length)
            val n = tail.toIntOrNull() ?: continue
            if (n > max) {
                max = n
            }
        }
        return max
    }
}
