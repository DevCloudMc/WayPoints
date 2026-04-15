package org.devcloud.waypoints.domain

/**
 * Per-player toggle for which waypoint scopes to render on the minimap.
 *
 * Controlled by `/wp visibility <show|hide> [scope]`; persisted per-player in [PlayerProfile] and
 * consulted by the [org.devcloud.waypoints.provider.WaypointWorldCursorProvider] on every render.
 *
 * All flags default to `false` (everything visible).
 *
 * @property hidePersonal hide the player's own personal waypoints.
 * @property hideGlobal hide every global waypoint.
 * @property hideShared hide waypoints other players shared with them.
 * @since 3.0.0
 */
data class VisibilityState(
    val hidePersonal: Boolean = false,
    val hideGlobal: Boolean = false,
    val hideShared: Boolean = false,
) {
    companion object {
        /** Everything visible. Default for a new player. */
        val ALL_VISIBLE = VisibilityState()

        /** Nothing visible; WayPoints contributes no cursors to the minimap. */
        val ALL_HIDDEN = VisibilityState(true, true, true)
    }
}
