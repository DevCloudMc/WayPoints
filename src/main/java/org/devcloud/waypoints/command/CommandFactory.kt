package org.devcloud.waypoints.command

import org.devcloud.waypoints.command.global.GlobalCreateCommand
import org.devcloud.waypoints.command.global.GlobalDeleteCommand
import org.devcloud.waypoints.command.global.GlobalListCommand
import org.devcloud.waypoints.command.validator.GlobalWayPointsListInputValidator
import org.devcloud.waypoints.command.validator.HideTypeInputValidator
import org.devcloud.waypoints.command.validator.executor.MapCursorTypeBadArgumentExecutor
import org.devcloud.waypoints.command.validator.PlayerWayPointsListInputValidator
import org.devcloud.waypoints.command.validator.WayPointNameInputValidator
import org.devcloud.waypoints.command.validator.executor.HideTypeBadArgumentExecutor
import org.devcloud.waypoints.command.validator.executor.WayPointNotFoundExecutor
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.manager.GlobalPointManager
import org.devcloud.waypoints.manager.UserManager

class CommandFactory(
    private val userManager: UserManager,
    private val globalPointManager: GlobalPointManager,
    private val languageConfig: LanguageConfig
) {
    fun createListCommand() = ListCommand(userManager, languageConfig)
    fun createHelpCommand() = HelpCommand(userManager, languageConfig)
    fun createHideCommand() = HideCommand(userManager, languageConfig)
    fun createCreateCommand() = CreateCommand(userManager, languageConfig)
    fun createDeleteCommand() = DeleteCommand(userManager, languageConfig)

    fun createGlobalCreateCommand() = GlobalCreateCommand(userManager, languageConfig)
    fun createGlobalDeleteCommand() = GlobalDeleteCommand(userManager, languageConfig)
    fun createGlobalListCommand() = GlobalListCommand(globalPointManager, languageConfig)

    fun createMapCursorTypeBadArgumentExecutor() = MapCursorTypeBadArgumentExecutor(languageConfig)
    fun createWayPointNotFoundExecutor() = WayPointNotFoundExecutor(languageConfig)
    fun createHideTypeBadArgumentExecutor() = HideTypeBadArgumentExecutor(languageConfig)

    fun createHideTypeInputValidator() = HideTypeInputValidator()
    fun createWayPointNameInputValidator() = WayPointNameInputValidator(languageConfig)
    fun createPlayerWayPointsListInputValidator() = PlayerWayPointsListInputValidator(userManager)
    fun createGlobalWayPointsListInputValidator() = GlobalWayPointsListInputValidator(globalPointManager)
}
