package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.command.HideType

class HideTypeInputValidator : InputValidator<String> {
    override fun getTabCompletes(sender: CommandSender, input: Array<out String>): MutableCollection<String> {
        return HideType.entries.map {
            it.name
        }.toMutableList()
    }

    override fun isValid(sender: CommandSender, input: Array<out String>?, args: Array<out String>): Boolean {
        if (input == null || input.isEmpty()) return false

        return enumValues<HideType>().any { it.name.equals(input.joinToString(), true) }
    }

    override fun get(sender: CommandSender, input: Array<out String>): String {
        return input[0].uppercase()
    }
}