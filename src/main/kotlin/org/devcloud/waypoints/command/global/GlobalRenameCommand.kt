package org.devcloud.waypoints.command.global

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointRenameEvent
import org.devcloud.waypoints.command.CommandSupport
import org.devcloud.waypoints.command.validator.GlobalWaypointValidator
import org.devcloud.waypoints.command.validator.WaypointNameValidator

class GlobalRenameCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("rename")
            .addSenderValidator(SenderValidatorPermission("waypoints.global"))
            .add(
                SubCommand(GlobalWaypointValidator(ctx.waypointService))
                    .add(SubCommand(WaypointNameValidator()).defaultTo(this::execute))
            )

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val new = p.getLast(String::class.java)
        val old = p.get(String::class.java, p.size() - 2)
        val wp = CommandSupport.findGlobalOrReply(ctx, sender, old) ?: return
        if (CommandSupport.callCancellable(WaypointRenameEvent(sender, wp, new))) {
            return
        }
        CommandSupport.reply(ctx, sender, ctx.waypointService.renameGlobal(old, new)) {
            ctx.lang.message("global-renamed", "old" to old, "new" to new)
        }
    }
}
