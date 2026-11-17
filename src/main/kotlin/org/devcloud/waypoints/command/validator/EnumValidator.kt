package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender

class EnumValidator<T : Enum<T>>(private val entries: List<T>) : InputValidator<T> {
    private fun find(name: String?): T? = entries.firstOrNull { it.name.equals(name, true) }

    override fun isValid(
        sender: CommandSender,
        input: Array<out String>,
        args: Array<out String>,
    ): Boolean = find(input.firstOrNull()) != null

    override fun get(sender: CommandSender, args: Array<out String>): T = find(args.first())!!

    override fun getTabCompletes(
        sender: CommandSender,
        args: Array<out String>,
    ): Collection<String> = entries.map { it.name.lowercase() }
}
