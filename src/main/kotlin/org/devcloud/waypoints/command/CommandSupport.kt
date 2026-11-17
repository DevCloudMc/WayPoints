package org.devcloud.waypoints.command

import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointEvent
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.error.WaypointError
import org.devcloud.waypoints.util.Outcome

internal object CommandSupport {
    fun renderError(ctx: WayPointsBootstrap, err: WaypointError): Component =
        when (err) {
            is WaypointError.InvalidName -> ctx.lang.message("invalid-name")
            is WaypointError.NameTaken -> ctx.lang.message("name-taken", "name" to err.name)
            is WaypointError.LimitReached ->
                ctx.lang.message("limit-reached", "max" to err.max.toString())

            is WaypointError.NotFound -> ctx.lang.message("waypoint-not-found", "name" to err.name)
            WaypointError.NotLoaded -> ctx.lang.message("still-loading")
            WaypointError.Cancelled -> ctx.lang.message("internal-error")

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

    fun findOwnedOrReply(ctx: WayPointsBootstrap, player: Player, name: String): Waypoint? =
        ctx.waypointService.findOwned(player.uniqueId, name) ?: notFound(ctx, player, name)

    fun findGlobalOrReply(ctx: WayPointsBootstrap, sender: CommandSender, name: String): Waypoint? =
        ctx.waypointService.findGlobal(name) ?: notFound(ctx, sender, name)

    private fun notFound(ctx: WayPointsBootstrap, sender: CommandSender, name: String): Waypoint? {
        ctx.messenger.send(sender, ctx.lang.message("waypoint-not-found", "name" to name))
        return null
    }

    // A Cancelled error stays silent: the cancelling listener already told the player.
    fun <T> reply(
        ctx: WayPointsBootstrap,
        sender: CommandSender,
        result: CompletableFuture<Outcome<T, WaypointError>>,
        ok: (T) -> Component,
    ) {
        result.thenAccept { res ->
            ctx.async.runOnMain {
                when (res) {
                    is Outcome.Ok -> ctx.messenger.send(sender, ok(res.value))
                    is Outcome.Err ->
                        if (res.error != WaypointError.Cancelled) {
                            ctx.messenger.send(sender, renderError(ctx, res.error))
                        }
                }
            }
        }
    }

    fun sendList(
        ctx: WayPointsBootstrap,
        sender: CommandSender,
        wps: List<Waypoint>,
        emptyKey: String,
        headerKey: String,
    ) {
        if (wps.isEmpty()) {
            ctx.messenger.send(sender, ctx.lang.message(emptyKey))
            return
        }
        ctx.messenger.send(sender, ctx.lang.message(headerKey, "count" to wps.size.toString()))
        for (wp in wps) {
            ctx.messenger.send(
                sender,
                ctx.lang.message(
                    "list-entry",
                    "name" to wp.name,
                    "scope" to wp.scope.name.lowercase(),
                    "world" to wp.location.worldName,
                ),
            )
        }
    }

    /** Call a cancellable Bukkit event on the main thread; returns true when cancelled. */
    fun callCancellable(event: WaypointEvent): Boolean {
        Bukkit.getPluginManager().callEvent(event)
        return event.isCancelled
    }
}
