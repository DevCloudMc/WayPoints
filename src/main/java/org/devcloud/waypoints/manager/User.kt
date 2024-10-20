package org.devcloud.waypoints.manager

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.devcloud.waypoints.exception.PlayerNotFoundException
import java.util.*

data class User(
    val uuid: UUID,
    val player: Player,
    var hide: Boolean = false,
    var maxPoint: Int = 0,
    var wayPoints: MutableMap<String, WayPoint> = mutableMapOf(),
) {
    companion object {
        fun fromUUID(uuid: UUID): User {
            val player = Bukkit.getPlayer(uuid) ?: throw PlayerNotFoundException("Player is null")
            return User(uuid = uuid, player = player)
        }
    }
}
