package org.devcloud.waypoints.manager

import org.bukkit.Location
import org.bukkit.map.MapCursor

data class WayPoint(
    val name: String,
    val type: MapCursor.Type,
    val location: Location
)