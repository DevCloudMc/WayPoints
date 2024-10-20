package org.devcloud.waypoints.command

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.UserManager
import org.devcloud.waypoints.manager.WayPointsManager
import org.devcloud.waypoints.util.TextComponentUtil

class DeleteCommand(
    private val userManager: UserManager,
    private val languageConfig: LanguageConfig
) : CommandExecutable {

    override fun execute(sender: CommandSender, args: Array<out String>, parameters: CommandParameters) {
        val user = userManager.getOrCreate((sender as Player).uniqueId)

        val wayPointName = parameters.get(String::class.java, 2) ?: run {
            user.player.sendMessage(languageConfig.of(LanguagePath.CHOOSE_WAYPOINT))
            return
        }
        val pointNameConfig = languageConfig.getReplacePointNameConfig(wayPointName)

        WayPointsManager.deleteWayPoint(user, wayPointName)?.let {
            val resultMessage = languageConfig.of(LanguagePath.POINT_DELETED)
                .replaceText(pointNameConfig)
            user.player.sendMessage(resultMessage)
        } ?: run {
            val resultMessage = languageConfig.of(LanguagePath.POINT_NOT_FOUND)
                .replaceText(pointNameConfig)
            user.player.sendMessage(resultMessage)
        }
    }
}