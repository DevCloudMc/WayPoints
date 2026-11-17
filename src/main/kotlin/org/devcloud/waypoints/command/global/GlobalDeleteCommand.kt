package org.devcloud.waypoints.command.global

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointDeleteEvent
import org.devcloud.waypoints.command.CommandSupport
import org.devcloud.waypoints.command.validator.GlobalWaypointValidator

class GlobalDeleteCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("delete")
            .addSenderValidator(SenderValidatorPermission("waypoints.global"))
            .add(SubCommand(GlobalWaypointValidator(ctx.waypointService)).defaultTo(this::execute))

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val name = p.getLast(String::class.java)
        val wp = CommandSupport.findGlobalOrReply(ctx, sender, name) ?: return
        if (CommandSupport.callCancellable(WaypointDeleteEvent(sender, wp))) {
            return
        }
        CommandSupport.reply(ctx, sender, ctx.waypointService.deleteGlobal(name)) {
            ctx.lang.message("global-deleted", "name" to name)
        }
    }
}
