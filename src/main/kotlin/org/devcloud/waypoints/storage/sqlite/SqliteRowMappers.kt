package org.devcloud.waypoints.storage.sqlite

import java.sql.ResultSet
import java.time.Instant
import java.util.UUID
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.domain.PlayerProfile
import org.devcloud.waypoints.domain.VisibilityState
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.domain.WaypointLocation
import org.devcloud.waypoints.domain.WaypointScope
import org.devcloud.waypoints.domain.WaypointShare

internal object SqliteRowMappers {
    fun waypoint(rs: ResultSet): Waypoint {
        val ownerStr = rs.getString("owner")
        return Waypoint(
            id = WaypointId.parse(rs.getString("id")),
            owner = ownerStr?.let(UUID::fromString),
            name = rs.getString("name"),
            icon = MapCursor.Type.valueOf(rs.getString("icon")),
            location =
                WaypointLocation(
                    worldName = rs.getString("world"),
                    x = rs.getDouble("x"),
                    y = rs.getDouble("y"),
                    z = rs.getDouble("z"),
                    yaw = rs.getFloat("yaw"),
                    pitch = rs.getFloat("pitch"),
                ),
            scope = WaypointScope.valueOf(rs.getString("scope")),
            createdAt = Instant.ofEpochMilli(rs.getLong("created_at")),
        )
    }

    fun share(rs: ResultSet) =
        WaypointShare(
            WaypointId.parse(rs.getString("waypoint_id")),
            UUID.fromString(rs.getString("shared_with")),
            Instant.ofEpochMilli(rs.getLong("shared_at")),
        )

    fun profile(rs: ResultSet) =
        PlayerProfile(
            uuid = UUID.fromString(rs.getString("uuid")),
            visibility =
                VisibilityState(
                    hidePersonal = rs.getInt("hide_personal") == 1,
                    hideGlobal = rs.getInt("hide_global") == 1,
                    hideShared = rs.getInt("hide_shared") == 1,
                ),
        )
}
