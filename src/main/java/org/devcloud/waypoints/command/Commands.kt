package org.devcloud.waypoints.command

import io.github.bananapuncher714.cartographer.core.api.command.CommandBase
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.devcloud.waypoints.command.validator.MapCursorTypeInputValidator
import org.devcloud.waypoints.util.PermissionList

class Commands(
    private var commandFactory: CommandFactory,
) {

    fun initCommandBase(): CommandBase {
        val commandBase = CommandBase(CommandName.BASE_COMMAND)
            .setPermission(PermissionList.USER)

        val mainCommand = SubCommand(CommandName.BASE_COMMAND)
            .addSenderValidator(SenderValidatorPlayer())
            .addSenderValidator(SenderValidatorPermission(PermissionList.USER))
            .defaultTo(commandFactory.createHelpCommand()::execute)

        initSubCommands(mainCommand)

        commandBase.setSubCommand(mainCommand)

        return commandBase
    }


    private fun initSubCommands(mainCommand: SubCommand) {
        val listSubCommand = SubCommand(CommandName.LIST_COMMAND)
            .defaultTo(commandFactory.createListCommand()::execute)

        val helpSubCommand = SubCommand(CommandName.HELP_COMMAND)
            .defaultTo(commandFactory.createHelpCommand()::execute)

        val hideSubCommand = SubCommand(CommandName.HIDE_COMMAND)
            .defaultTo(commandFactory.createHideCommand()::execute)

        val createSubCommand = SubCommand(CommandName.CREATE_COMMAND)
            .add(
                SubCommand(commandFactory.createWayPointNameInputValidator())
                    .defaultTo(commandFactory.createCreateCommand()::execute)
                    .add(
                        SubCommand(MapCursorTypeInputValidator(isRequired = false))
                            .defaultTo(commandFactory.createCreateCommand()::execute)
                    )
                    .whenUnknown(commandFactory.createMapCursorTypeBadArgumentExecutor()::execute)
            )

        val deleteSubCommand = SubCommand(CommandName.DELETE_COMMAND)
            .add(
                SubCommand(commandFactory.createPlayerWayPointsListInputValidator())
                    .defaultTo(commandFactory.createDeleteCommand()::execute)
            )
            .whenUnknown(commandFactory.createWayPointNotFoundExecutor()::execute)
//            .defaultTo(commandFactory.createDeleteCommand()::execute)

        mainCommand
            .add(listSubCommand)
            .add(helpSubCommand)
            .add(hideSubCommand)
            .add(createSubCommand)
            .add(deleteSubCommand)

        initAdminSubCommands(mainCommand)
    }

    private fun initAdminSubCommands(mainCommand: SubCommand) {
        val globalMainCommand = SubCommand(CommandName.GLOBAL_COMMAND)
            .addSenderValidator(SenderValidatorPlayer())
            .addSenderValidator(SenderValidatorPermission(PermissionList.ADMIN))
            .defaultTo(commandFactory.createHelpCommand()::execute)

        val globalCreateSubCommand = SubCommand(CommandName.CREATE_COMMAND)
            .add(
                SubCommand(commandFactory.createWayPointNameInputValidator())
                    .add(
                        SubCommand(MapCursorTypeInputValidator())
                            .defaultTo(commandFactory.createGlobalCreateCommand()::execute)
                    )
                    .whenUnknown(commandFactory.createMapCursorTypeBadArgumentExecutor()::execute)
            )


        val globalDeleteSubCommand = SubCommand(CommandName.DELETE_COMMAND)
            .add(
                SubCommand(commandFactory.createGlobalWayPointsListInputValidator())
                    .defaultTo(commandFactory.createGlobalDeleteCommand()::execute)
            )
            .whenUnknown(commandFactory.createWayPointNotFoundExecutor()::execute)

        val globalListSubCommand = SubCommand(CommandName.LIST_COMMAND)
            .defaultTo(commandFactory.createGlobalListCommand()::execute)

        globalMainCommand
            .add(globalCreateSubCommand)
            .add(globalDeleteSubCommand)
            .add(globalListSubCommand)

        mainCommand.add(globalMainCommand)
    }
}
