package org.devcloud.waypoints.manager

import org.bukkit.Location
import org.bukkit.map.MapCursor

class GlobalPointManager {

    val pointMap: MutableMap<String, WayPoint> = HashMap()

    fun create(name: String, type: MapCursor.Type, location: Location): WayPoint? {
        return pointMap.put(name, WayPoint(name, type, location))
    }

    fun remove(name: String): WayPoint? {
        return pointMap.remove(name)
    }
}
