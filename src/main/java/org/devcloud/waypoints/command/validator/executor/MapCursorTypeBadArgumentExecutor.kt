package org.devcloud.waypoints.command.validator.executor

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.util.TextComponentUtil

class MapCursorTypeBadArgumentExecutor(
    private val languageConfig: LanguageConfig
): CommandExecutable {
    override fun execute(sender: CommandSender, args: Array<out String>, params: CommandParameters?) {
        val cursorTypeConfig =
            languageConfig.getReplaceCursorTypeConfig(args.joinToString())

        languageConfig.of(LanguagePath.UNKNOWN_CURSOR_TYPE)
            .replaceText(cursorTypeConfig)
            .let(sender::sendMessage)
    }
}