package org.devcloud.waypoints.domain.error

sealed class WaypointError {
    data class NotFound(val name: String) : WaypointError()

    data class NameTaken(val name: String) : WaypointError()

    data class LimitReached(val current: Int, val max: Int) : WaypointError()

    data class InvalidName(val name: String) : WaypointError()

    data object Cancelled : WaypointError()

    data object NotLoaded : WaypointError()

    data class Storage(val cause: Throwable) : WaypointError()
}
