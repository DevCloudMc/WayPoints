package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.command.CommandSender

class MapCursorTypeInputValidator(
    private val isRequired: Boolean = true
) : InputValidator<String> {
    override fun getTabCompletes(sender: CommandSender, input: Array<out String>): MutableCollection<String> {
        return Registry.MAP_DECORATION_TYPE.iterator().asSequence().map {
            it.key.key
        }.toMutableList()
    }

    override fun isValid(sender: CommandSender, input: Array<out String>?, args: Array<out String>): Boolean {
        if (!isRequired && (input == null || input.isEmpty())) return true

        if (input == null || input.isEmpty()) return false

        try {
            val namespacedKey = NamespacedKey.minecraft(input.joinToString())
            return Registry.MAP_DECORATION_TYPE[namespacedKey] != null
        } catch (e: IllegalArgumentException) {
            return false
        }
    }

    override fun get(sender: CommandSender, input: Array<out String>): String {
        return input[0].uppercase()
    }
}