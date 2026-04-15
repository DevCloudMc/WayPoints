package org.devcloud.waypoints.command.global

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointDeleteEvent
import org.devcloud.waypoints.command.CommandSupport
import org.devcloud.waypoints.command.validator.GlobalWaypointValidator
import org.devcloud.waypoints.util.Outcome

class GlobalDeleteCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("delete")
            .addSenderValidator(SenderValidatorPermission("waypoints.global"))
            .add(SubCommand(GlobalWaypointValidator(ctx.waypointService)).defaultTo(this::execute))

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val name = p.getLast(String::class.java)
        val wp =
            ctx.waypointService.findGlobal(name)
                ?: run {
                    ctx.messenger.send(
                        sender,
                        ctx.lang.message("waypoint-not-found", "name" to name),
                    )
                    return
                }
        if (CommandSupport.callCancellable(WaypointDeleteEvent(sender, wp))) {
            return
        }
        ctx.waypointService.deleteGlobal(name).thenAccept { res ->
            ctx.async.runOnMain {
                when (res) {
                    is Outcome.Ok ->
                        ctx.messenger.send(
                            sender,
                            ctx.lang.message("global-deleted", "name" to name),
                        )
                    is Outcome.Err ->
                        ctx.messenger.send(sender, CommandSupport.renderError(ctx, res.error))
                }
            }
        }
    }
}
