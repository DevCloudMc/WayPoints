package org.devcloud.waypoints.domain

import org.bukkit.map.MapCursor

object IconRegistry {
    val SAFE_DEFAULT: MapCursor.Type = MapCursor.Type.PLAYER_OFF_MAP

    private val byName: Map<String, MapCursor.Type> =
        MapCursor.Type.values().associateBy { it.name().lowercase() }

    fun parse(input: String): MapCursor.Type? = byName[input.lowercase()]

    fun allNames(): List<String> = byName.keys.sorted()
}
