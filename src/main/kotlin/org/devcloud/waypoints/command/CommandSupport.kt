package org.devcloud.waypoints.command

import java.util.logging.Level
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointEvent
import org.devcloud.waypoints.domain.error.WaypointError

internal object CommandSupport {
    fun renderError(ctx: WayPointsBootstrap, err: WaypointError): Component =
        when (err) {
            is WaypointError.InvalidName -> ctx.lang.message("invalid-name")
            is WaypointError.NameTaken -> ctx.lang.message("name-taken", "name" to err.name)
            is WaypointError.LimitReached ->
                ctx.lang.message("limit-reached", "max" to err.max.toString())

            is WaypointError.NotFound -> ctx.lang.message("waypoint-not-found", "name" to err.name)
            is WaypointError.InvalidIcon ->
                ctx.lang.message(
                    "invalid-icon",
                    "icon" to err.icon,
                    "hint" to err.allowed.take(8).joinToString(", "),
                )

            is WaypointError.WorldMissing ->
                ctx.lang.message("world-missing", "world" to err.worldName, "name" to "")

            WaypointError.NotLoaded -> ctx.lang.message("still-loading")
            WaypointError.NotOwner,
            WaypointError.InsufficientPermission -> ctx.lang.message("internal-error")

            is WaypointError.Storage ->
                ctx.lang.message("internal-error").also {
                    ctx.module.logger.log(Level.SEVERE, "Storage write failed", err.cause)
                }
        }

    // Tail for command futures that talk to storage directly: without it a failure vanishes.
    fun reportFailure(ctx: WayPointsBootstrap, sender: CommandSender, e: Throwable): Void? {
        ctx.module.logger.log(Level.SEVERE, "Storage call failed", e)
        ctx.async.runOnMain { ctx.messenger.send(sender, ctx.lang.message("internal-error")) }
        return null
    }

    /** Call a cancellable Bukkit event on the main thread; returns true when cancelled. */
    fun callCancellable(event: WaypointEvent): Boolean {
        Bukkit.getPluginManager().callEvent(event)
        return event.isCancelled
    }
}
