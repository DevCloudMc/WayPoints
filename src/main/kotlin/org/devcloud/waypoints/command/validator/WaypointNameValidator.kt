package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender

class WaypointNameValidator : InputValidator<String> {
    private val regex = Regex("^[A-Za-z0-9_-]{1,32}$")

    override fun isValid(sender: CommandSender, input: Array<out String>, args: Array<out String>): Boolean {
        val v = input.firstOrNull() ?: return false
        return regex.matches(v)
    }

    override fun get(sender: CommandSender, args: Array<out String>): String = args.first()

    override fun getTabCompletes(sender: CommandSender, args: Array<out String>): Collection<String> =
        listOf("<name>")
}
