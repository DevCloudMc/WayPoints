package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.apache.commons.lang3.StringUtils
import org.bukkit.Registry
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath

class WayPointNameInputValidator(
    private val languageConfig: LanguageConfig
) : InputValidator<String> {

    override fun getTabCompletes(sender: CommandSender, input: Array<out String>): MutableCollection<String> {
        val hint = languageConfig.of(LanguagePath.WAYPOINT_NAME_HINT)
        val hintMessage = LegacyComponentSerializer.legacySection().serialize(hint)
        return mutableListOf(hintMessage)
    }

    override fun isValid(sender: CommandSender, input: Array<out String>, args: Array<out String>): Boolean {
        return true
    }

    override fun get(sender: CommandSender, input: Array<out String>): String {
        return input[0]
    }
}