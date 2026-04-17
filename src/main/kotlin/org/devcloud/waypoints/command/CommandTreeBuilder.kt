package org.devcloud.waypoints.command

import io.github.bananapuncher714.cartographer.core.api.command.CommandBase
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.command.admin.MigrateCommand
import org.devcloud.waypoints.command.admin.ReloadCommand
import org.devcloud.waypoints.command.admin.UserListCommand
import org.devcloud.waypoints.command.admin.UserWipeCommand
import org.devcloud.waypoints.command.global.GlobalCreateCommand
import org.devcloud.waypoints.command.global.GlobalDeleteCommand
import org.devcloud.waypoints.command.global.GlobalListCommand
import org.devcloud.waypoints.command.global.GlobalRenameCommand
import org.devcloud.waypoints.command.personal.CreateCommand
import org.devcloud.waypoints.command.personal.DeleteCommand
import org.devcloud.waypoints.command.personal.InfoCommand
import org.devcloud.waypoints.command.personal.ListCommand
import org.devcloud.waypoints.command.personal.RenameCommand
import org.devcloud.waypoints.command.personal.ShareCommand
import org.devcloud.waypoints.command.personal.TeleportCommand
import org.devcloud.waypoints.command.personal.UnshareCommand
import org.devcloud.waypoints.command.personal.VisibilityCommand

class CommandTreeBuilder(private val ctx: WayPointsBootstrap) {
    fun build(): CommandBase {
        val help = HelpCommand(ctx)
        val root =
            SubCommand("wp")
                .add(CreateCommand(ctx).build())
                .add(DeleteCommand(ctx).build())
                .add(RenameCommand(ctx).build())
                .add(ListCommand(ctx).build())
                .add(InfoCommand(ctx).build())
                .add(TeleportCommand(ctx).build())
                .add(ShareCommand(ctx).build())
                .add(UnshareCommand(ctx).build())
                .add(VisibilityCommand(ctx).build())
                .add(
                    SubCommand("global")
                        .add(GlobalCreateCommand(ctx).build())
                        .add(GlobalDeleteCommand(ctx).build())
                        .add(GlobalRenameCommand(ctx).build())
                        .add(GlobalListCommand(ctx).build())
                )
                .add(
                    SubCommand("admin")
                        .add(ReloadCommand(ctx).build())
                        .add(MigrateCommand(ctx).build())
                        .add(
                            SubCommand("user")
                                .add(UserListCommand(ctx).build())
                                .add(UserWipeCommand(ctx).build())
                        )
                )
                .add(help.build())
                .defaultTo(help::execute)

        return CommandBase("wp")
            .setPermission("waypoints.use")
            .setDescription("WayPoints commands")
            .addAliases("waypoint")
            .setSubCommand(root)
    }
}
