package org.devcloud.waypoints.domain.error

sealed class WaypointError {
    data class NotFound(val name: String) : WaypointError()

    data class NameTaken(val name: String) : WaypointError()

    data class LimitReached(val current: Int, val max: Int) : WaypointError()

    data class InvalidName(val name: String) : WaypointError()

    data class InvalidIcon(val icon: String, val allowed: List<String>) : WaypointError()

    data class WorldMissing(val worldName: String) : WaypointError()

    data object NotOwner : WaypointError()

    data object InsufficientPermission : WaypointError()

    data class Storage(val cause: StorageError) : WaypointError()
}
