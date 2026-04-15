package org.devcloud.waypoints.domain

import java.time.Instant
import java.util.UUID
import org.bukkit.map.MapCursor

data class Waypoint(
    val id: WaypointId,
    val owner: UUID?,
    val name: String,
    val icon: MapCursor.Type,
    val location: WaypointLocation,
    val scope: WaypointScope,
    val createdAt: Instant,
) {
    init {
        when (scope) {
            WaypointScope.GLOBAL ->
                require(owner == null) { "Global waypoint must not have an owner" }
            WaypointScope.PERSONAL ->
                require(owner != null) { "Personal waypoint must have an owner" }
        }
    }
}
