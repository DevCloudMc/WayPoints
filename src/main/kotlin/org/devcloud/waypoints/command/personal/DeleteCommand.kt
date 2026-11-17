package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointDeleteEvent
import org.devcloud.waypoints.command.CommandSupport
import org.devcloud.waypoints.command.validator.OwnedWaypointValidator

class DeleteCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("delete")
            .addSenderValidator(SenderValidatorPlayer())
            .add(SubCommand(OwnedWaypointValidator(ctx.waypointService)).defaultTo(this::execute))
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-delete")) }

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val player = sender as Player
        val name = p.getLast(String::class.java)
        val wp = CommandSupport.findOwnedOrReply(ctx, player, name) ?: return
        if (CommandSupport.callCancellable(WaypointDeleteEvent(player, wp))) {
            return
        }
        CommandSupport.reply(
            ctx,
            player,
            ctx.waypointService.deletePersonal(player.uniqueId, name),
        ) {
            ctx.lang.message("waypoint-deleted", "name" to name)
        }
    }
}
