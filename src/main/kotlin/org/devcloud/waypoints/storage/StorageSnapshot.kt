package org.devcloud.waypoints.storage

import org.devcloud.waypoints.domain.PlayerProfile
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointShare

data class StorageSnapshot(
    val waypoints: List<Waypoint>,
    val shares: List<WaypointShare>,
    val profiles: List<PlayerProfile>,
)
