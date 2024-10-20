package org.devcloud.waypoints.manager

import net.milkbowl.vault.permission.Permission
import org.devcloud.waypoints.config.MainConfig
import java.util.*

class UserManager(
    private val permissionProvider: Permission,
    private val mainConfig: MainConfig
) {

    private val userMap: MutableMap<UUID, User> = WeakHashMap()

    fun getOrCreate(uuid: UUID): User {
        return userMap.computeIfAbsent(uuid) { User.fromUUID(it) }
    }

    fun remove(uuid: UUID): User? {
        return userMap.remove(uuid)
    }

    fun switchHide(user: User) {
        user.hide = !user.hide
    }

    fun getMaxWayPoints(uuid: UUID): Int {
        val user = getOrCreate(uuid)
        val group = permissionProvider.getPrimaryGroup(user.player)
        return mainConfig.groups[group] ?: 0
    }
}
