package org.devcloud.waypoints.command.admin

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.command.CommandSupport

class UserListCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("list")
            .addSenderValidator(SenderValidatorPermission("waypoints.admin"))
            .add(SubCommand(InputValidatorPlayer()).defaultTo(this::execute))

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val target = p.getLast(OfflinePlayer::class.java)
        ctx.storage.waypoints
            .listByOwner(target.uniqueId)
            .thenAccept { list ->
                ctx.async.runOnMain {
                    CommandSupport.sendList(ctx, sender, list, "list-empty", "list-header")
                }
            }
            .exceptionally { CommandSupport.reportFailure(ctx, sender, it) }
    }
}
