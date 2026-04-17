package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import java.time.Duration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointTeleportEvent
import org.devcloud.waypoints.command.CommandSupport
import org.devcloud.waypoints.command.validator.OwnedWaypointValidator

class TeleportCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("tp")
            .addSenderValidator(SenderValidatorPlayer())
            .addSenderValidator(SenderValidatorPermission("waypoints.tp"))
            .add(SubCommand(OwnedWaypointValidator(ctx.waypointService)).defaultTo(this::execute))
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-tp")) }

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val player = sender as Player
        if (!ctx.config.teleportEnabled) {
            ctx.messenger.send(player, ctx.lang.message("teleport-disabled"))
            return
        }
        val name = p.getLast(String::class.java)
        val wp =
            ctx.waypointService.findOwned(player.uniqueId, name)
                ?: run {
                    ctx.messenger.send(
                        player,
                        ctx.lang.message("waypoint-not-found", "name" to name),
                    )
                    return
                }
        val bypass = player.hasPermission("waypoints.tp.cooldown.bypass")
        val remaining = ctx.teleportService.remainingCooldown(player.uniqueId, bypass)
        if (remaining > Duration.ZERO) {
            ctx.messenger.send(
                player,
                ctx.lang.message("teleport-cooldown", "seconds" to remaining.seconds.toString()),
            )
            return
        }
        val resolved =
            wp.location.resolve()
                ?: run {
                    ctx.messenger.send(
                        player,
                        ctx.lang.message(
                            "world-missing",
                            "world" to wp.location.worldName,
                            "name" to wp.name,
                        ),
                    )
                    return
                }
        if (CommandSupport.callCancellable(WaypointTeleportEvent(player, wp))) {
            return
        }
        ctx.teleportService.markUsed(player.uniqueId)
        ctx.messenger.send(player, ctx.lang.message("teleport-ok", "name" to wp.name))
        player.teleport(resolved)
    }
}
