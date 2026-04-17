package org.devcloud.waypoints.domain

import java.util.*

/**
 * Per-player settings persisted across sessions.
 *
 * Currently holds only [VisibilityState]; future versions may extend this with per-player teleport
 * cooldowns, locale overrides, etc. When a player has no saved profile,
 * [VisibilityState.ALL_VISIBLE] is used as the default.
 *
 * @property uuid UUID of the owning player.
 * @property visibility which waypoint scopes this player wants to see.
 * @since 3.0.0
 */
data class PlayerProfile(
    val uuid: UUID,
    val visibility: VisibilityState = VisibilityState.ALL_VISIBLE,
)
