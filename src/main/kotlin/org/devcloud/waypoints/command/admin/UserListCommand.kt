package org.devcloud.waypoints.command.admin

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap

class UserListCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("list")
            .addSenderValidator(SenderValidatorPermission("waypoints.admin"))
            .add(SubCommand(InputValidatorPlayer()).defaultTo(this::execute))

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val target = p.getLast(OfflinePlayer::class.java)
        ctx.storage.waypoints.listByOwner(target.uniqueId).thenAccept { list ->
            ctx.async.runOnMain {
                if (list.isEmpty()) {
                    ctx.messenger.send(sender, ctx.lang.message("list-empty"))
                    return@runOnMain
                }
                ctx.messenger.send(
                    sender,
                    ctx.lang.message("list-header", "count" to list.size.toString()),
                )
                for (wp in list) {
                    ctx.messenger.send(
                        sender,
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
    }
}
