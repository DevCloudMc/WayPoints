package org.devcloud.waypoints.command

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.UserManager

class HideCommand(
    private val userManager: UserManager,
    private val languageConfig: LanguageConfig
) : CommandExecutable {

    override fun execute(sender: CommandSender, args: Array<out String>, parameters: CommandParameters) {
        val user = userManager.getOrCreate((sender as Player).uniqueId)

        userManager.switchHide(user)
        if (user.hide) {
            user.player.sendMessage(languageConfig.of(LanguagePath.HIDE))
        } else {
            user.player.sendMessage(languageConfig.of(LanguagePath.UN_HIDE))
        }
    }
}