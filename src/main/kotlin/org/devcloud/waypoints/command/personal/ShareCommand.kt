package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointShareEvent
import org.devcloud.waypoints.command.CommandSupport
import org.devcloud.waypoints.command.validator.OwnedWaypointValidator

class ShareCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("share")
            .addSenderValidator(SenderValidatorPlayer())
            .addSenderValidator(SenderValidatorPermission("waypoints.share"))
            .add(
                SubCommand(InputValidatorPlayer())
                    .add(SubCommand(OwnedWaypointValidator(ctx.waypointService)).defaultTo(this::execute))
            )
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-share")) }

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val player = sender as Player
        val name = p.getLast(String::class.java)
        val target = p.get(OfflinePlayer::class.java, p.size() - 2)
        val wp = ctx.waypointService.findOwned(player.uniqueId, name) ?: run {
            ctx.messenger.send(player, ctx.lang.message("waypoint-not-found", "name" to name))
            return
        }
        if (CommandSupport.callCancellable(WaypointShareEvent(player, wp, target.uniqueId))) return
        ctx.shareService.share(wp.id, target.uniqueId).thenAccept { added ->
            ctx.async.runOnMain {
                if (added) {
                    ctx.messenger.send(
                        player,
                        ctx.lang.message("share-added", "name" to wp.name, "target" to (target.name ?: "?")),
                    )
                } else {
                    ctx.messenger.send(player, ctx.lang.message("share-duplicate"))
                }
            }
        }
    }
}
