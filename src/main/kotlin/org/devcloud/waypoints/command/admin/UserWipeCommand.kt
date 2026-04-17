package org.devcloud.waypoints.command.admin

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap

class UserWipeCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("wipe")
            .addSenderValidator(SenderValidatorPermission("waypoints.admin"))
            .add(SubCommand(InputValidatorPlayer()).defaultTo(this::execute))

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val target = p.getLast(OfflinePlayer::class.java)
        ctx.waypointService.forgetPlayer(target.uniqueId)
        ctx.storage.shares
            .removeAllByOwner(target.uniqueId)
            .thenCompose { ctx.storage.waypoints.deleteAllByOwner(target.uniqueId) }
            .thenAccept { count ->
                ctx.async.runOnMain {
                    ctx.messenger.send(
                        sender,
                        ctx.lang.message(
                            "admin-user-wiped",
                            "count" to count.toString(),
                            "player" to (target.name ?: "?"),
                        ),
                    )
                }
            }
    }
}
