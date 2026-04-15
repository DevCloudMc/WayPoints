package org.devcloud.waypoints.domain

import java.time.Instant
import java.util.UUID

data class WaypointShare(val waypointId: WaypointId, val sharedWith: UUID, val sharedAt: Instant)
