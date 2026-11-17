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
                "help-unshare",
                "help-visibility",
            ) +
                (if (sender.hasPermission("waypoints.global")) GLOBAL_KEYS else emptyList()) +
                (if (sender.hasPermission("waypoints.admin")) ADMIN_KEYS else emptyList())
        for (key in keys) ctx.messenger.send(sender, ctx.lang.message(key))
    }

    private companion object {
        val GLOBAL_KEYS =
            listOf(
                "help-global-create",
                "help-global-delete",
                "help-global-rename",
                "help-global-list",
            )
        val ADMIN_KEYS =
            listOf(
                "help-admin-reload",
                "help-admin-migrate",
                "help-admin-user-list",
                "help-admin-user-wipe",
            )
    }
}
