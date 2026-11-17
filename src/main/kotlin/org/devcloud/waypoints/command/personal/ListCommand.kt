package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.command.CommandSupport

class ListCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("list").addSenderValidator(SenderValidatorPlayer()).defaultTo(this::execute)

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val player = sender as Player
        CommandSupport.sendList(
            ctx,
            player,
            ctx.waypointService.listOwned(player.uniqueId),
            "list-empty",
            "list-header",
        )
    }
}
