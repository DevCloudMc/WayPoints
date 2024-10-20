package org.devcloud.waypoints.command.validator.executor

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath

class WayPointNotFoundExecutor(
    private val languageConfig: LanguageConfig
): CommandExecutable {
    override fun execute(sender: CommandSender, args: Array<out String>, params: CommandParameters?) {
        languageConfig.of(LanguagePath.POINT_NOT_FOUND)
            .let(sender::sendMessage)
    }
}