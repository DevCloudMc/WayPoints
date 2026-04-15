package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.command.validator.OwnedWaypointValidator
import kotlin.math.roundToInt

class InfoCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("info")
            .addSenderValidator(SenderValidatorPlayer())
            .add(SubCommand(OwnedWaypointValidator(ctx.waypointService)).defaultTo(this::execute))
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-info")) }

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val player = sender as Player
        val name = p.getLast(String::class.java)
        val wp = ctx.waypointService.findOwned(player.uniqueId, name) ?: run {
            ctx.messenger.send(player, ctx.lang.message("waypoint-not-found", "name" to name))
            return
        }
        ctx.messenger.send(
            player,
            ctx.lang.message(
                "waypoint-info",
                "name" to wp.name,
                "world" to wp.location.worldName,
                "x" to wp.location.x.roundToInt().toString(),
                "y" to wp.location.y.roundToInt().toString(),
                "z" to wp.location.z.roundToInt().toString(),
            ),
        )
    }
}
