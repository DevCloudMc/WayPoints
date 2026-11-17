package org.devcloud.waypoints.command.global

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.command.CommandSupport

class GlobalListCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("list")
            .addSenderValidator(SenderValidatorPermission("waypoints.global"))
            .defaultTo(this::execute)

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        CommandSupport.sendList(
            ctx,
            sender,
            ctx.waypointService.listGlobals(),
            "global-list-empty",
            "global-list-header",
        )
    }
}
