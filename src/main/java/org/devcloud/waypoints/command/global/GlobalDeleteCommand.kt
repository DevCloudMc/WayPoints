package org.devcloud.waypoints.command.global

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.UserManager
import org.devcloud.waypoints.manager.WayPointsManager

class GlobalDeleteCommand(
    private val userManager: UserManager,
    private val languageConfig: LanguageConfig
) : CommandExecutable {

    override fun execute(sender: CommandSender, args: Array<out String>, parameters: CommandParameters) {
        val user = userManager.getOrCreate((sender as Player).uniqueId)

        val wayPointName = parameters.get(String::class.java, 3) ?: run {
            user.player.sendMessage(languageConfig.of(LanguagePath.CHOOSE_WAYPOINT))
            return
        }

        WayPointsManager.deleteGlobalWayPoint(wayPointName)
        val pointNameConfig = languageConfig.getReplacePointNameConfig(wayPointName)
        val resultMessage = languageConfig.of(LanguagePath.POINT_DELETED)
            .replaceText(pointNameConfig)

        user.player.sendMessage(resultMessage)
    }
}