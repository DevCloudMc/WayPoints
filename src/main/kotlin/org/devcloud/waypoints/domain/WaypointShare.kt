package org.devcloud.waypoints.domain

import java.time.Instant
import java.util.*

/**
 * A grant of read-only access to a personal [Waypoint] for another player.
 *
 * Shares are unique per `(waypointId, sharedWith)` — the same waypoint can be shared with many
 * players, but adding the same share twice is a no-op.
 *
 * When the underlying waypoint is deleted, every share that references it is deleted too (cascaded
 * by the storage backend). When the owning player is wiped via `/wp admin user <player> wipe`, all
 * shares they issued are removed first to satisfy the foreign-key constraint.
 *
 * @property waypointId the [WaypointId] of the shared waypoint.
 * @property sharedWith UUID of the receiving player.
 * @property sharedAt wall-clock time the share was granted.
 * @see org.devcloud.waypoints.api.event.WaypointShareEvent
 * @since 3.0.0
 */
data class WaypointShare(val waypointId: WaypointId, val sharedWith: UUID, val sharedAt: Instant)
