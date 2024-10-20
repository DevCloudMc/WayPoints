package org.devcloud.waypoints.manager

import org.bukkit.Location
import org.bukkit.map.MapCursor

class PersonalPointManager {

    val pointMap: MutableMap<String, WayPoint> = HashMap()

    fun create(user: User, name: String, type: MapCursor.Type, location: Location) {
        user.wayPoints[name] = WayPoint(name, type, location)
    }

    fun remove(name: String): WayPoint? {
        return pointMap.remove(name)
    }
}