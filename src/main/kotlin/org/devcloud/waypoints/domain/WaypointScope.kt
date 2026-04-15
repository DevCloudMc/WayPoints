package org.devcloud.waypoints.domain

/**
 * Visibility class of a [Waypoint].
 *
 * @since 2.0.0
 */
enum class WaypointScope {
    /** Owned by a single player and only shown to the owner (or players they have shared with). */
    PERSONAL,

    /** Server-wide; visible to every player. Managed by staff via `/wp global`. */
    GLOBAL,
}
