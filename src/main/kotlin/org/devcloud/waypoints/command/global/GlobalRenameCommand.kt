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
import org.devcloud.waypoints.util.Outcome

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
        val wp = ctx.waypointService.findGlobal(old) ?: run {
            ctx.messenger.send(sender, ctx.lang.message("waypoint-not-found", "name" to old))
            return
        }
        if (CommandSupport.callCancellable(WaypointRenameEvent(sender, wp, new))) return
        ctx.waypointService.renameGlobal(old, new).thenAccept { res ->
            ctx.async.runOnMain {
                when (res) {
                    is Outcome.Ok ->
                        ctx.messenger.send(
                            sender,
                            ctx.lang.message("global-renamed", "old" to old, "new" to new),
                        )
                    is Outcome.Err ->
                        ctx.messenger.send(sender, CommandSupport.renderError(ctx, res.error))
                }
            }
        }
    }
}
