package org.devcloud.waypoints.domain

import java.util.UUID

data class PlayerProfile(
    val uuid: UUID,
    val visibility: VisibilityState = VisibilityState.ALL_VISIBLE,
)
