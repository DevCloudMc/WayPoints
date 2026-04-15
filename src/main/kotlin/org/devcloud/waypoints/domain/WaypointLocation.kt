package org.devcloud.waypoints.domain

import org.bukkit.Bukkit
import org.bukkit.Location

data class WaypointLocation(
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
) {
    fun resolve(): Location? {
        val world = Bukkit.getWorld(worldName) ?: return null
        return Location(world, x, y, z, yaw, pitch)
    }

    companion object {
        fun of(loc: Location): WaypointLocation =
            WaypointLocation(
                worldName = requireNotNull(loc.world) { "Location has no world" }.name,
                x = loc.x,
                y = loc.y,
                z = loc.z,
                yaw = loc.yaw,
                pitch = loc.pitch,
            )
    }
}
