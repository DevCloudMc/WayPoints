package org.devcloud.waypoints.command

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointEvent
import org.devcloud.waypoints.domain.error.WaypointError

internal object CommandSupport {
    fun renderError(ctx: WayPointsBootstrap, err: WaypointError): Component =
        when (err) {
            is WaypointError.InvalidName -> ctx.lang.message("invalid-name")
            is WaypointError.NameTaken -> ctx.lang.message("name-taken", "name" to err.name)
            is WaypointError.LimitReached -> ctx.lang.message("limit-reached", "max" to err.max.toString())
            is WaypointError.NotFound -> ctx.lang.message("waypoint-not-found", "name" to err.name)
            is WaypointError.InvalidIcon ->
                ctx.lang.message("invalid-icon", "icon" to err.icon, "hint" to err.allowed.take(8).joinToString(", "))
            is WaypointError.WorldMissing ->
                ctx.lang.message("world-missing", "world" to err.worldName, "name" to "")
            WaypointError.NotOwner,
            WaypointError.InsufficientPermission -> ctx.lang.message("internal-error")
            is WaypointError.Storage -> ctx.lang.message("internal-error")
        }

    /** Call a cancellable Bukkit event on the main thread; returns true when cancelled. */
    fun callCancellable(event: WaypointEvent): Boolean {
        Bukkit.getPluginManager().callEvent(event)
        return event.isCancelled
    }
}
