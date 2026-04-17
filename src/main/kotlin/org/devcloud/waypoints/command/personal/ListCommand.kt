package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap

class ListCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("list").addSenderValidator(SenderValidatorPlayer()).defaultTo(this::execute)

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val player = sender as Player
        val wps = ctx.waypointService.listOwned(player.uniqueId)
        if (wps.isEmpty()) {
            ctx.messenger.send(player, ctx.lang.message("list-empty"))
            return
        }
        ctx.messenger.send(player, ctx.lang.message("list-header", "count" to wps.size.toString()))
        for (wp in wps) {
            ctx.messenger.send(
                player,
                ctx.lang.message(
                    "list-entry",
                    "name" to wp.name,
                    "scope" to wp.scope.name.lowercase(),
                    "world" to wp.location.worldName,
                ),
            )
        }
    }
}
