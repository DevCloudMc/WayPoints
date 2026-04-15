package org.devcloud.waypoints.command

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap

class HelpCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand = SubCommand("help").defaultTo(this::execute)

    fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val keys =
            listOf(
                "help-header",
                "help-create",
                "help-delete",
                "help-rename",
                "help-list",
                "help-info",
                "help-tp",
                "help-share",
                "help-visibility",
            )
        for (key in keys) ctx.messenger.send(sender, ctx.lang.message(key))
    }
}
