package org.devcloud.waypoints.command.admin

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap

class ReloadCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("reload")
            .addSenderValidator(SenderValidatorPermission("waypoints.admin"))
            .defaultTo(this::execute)

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        ctx.reload()
        ctx.messenger.send(sender, ctx.lang.message("admin-reload-ok"))
    }
}
