package org.devcloud.waypoints.command.global

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap

class GlobalListCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("list")
            .addSenderValidator(SenderValidatorPermission("waypoints.global"))
            .defaultTo(this::execute)

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val wps = ctx.waypointService.listGlobals()
        if (wps.isEmpty()) {
            ctx.messenger.send(sender, ctx.lang.message("global-list-empty"))
            return
        }
        ctx.messenger.send(sender, ctx.lang.message("global-list-header", "count" to wps.size.toString()))
        for (wp in wps) {
            ctx.messenger.send(
                sender,
                ctx.lang.message(
                    "list-entry",
                    "name" to wp.name,
                    "scope" to "global",
                    "world" to wp.location.worldName,
                ),
            )
        }
    }
}
