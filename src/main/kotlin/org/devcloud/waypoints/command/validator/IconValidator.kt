package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.domain.IconRegistry

class IconValidator : InputValidator<MapCursor.Type> {
    override fun isValid(
        sender: CommandSender,
        input: Array<out String>,
        args: Array<out String>,
    ): Boolean {
        val v = input.firstOrNull() ?: return false
        return IconRegistry.parse(v) != null
    }

    override fun get(sender: CommandSender, args: Array<out String>): MapCursor.Type =
        IconRegistry.parse(args.first()) ?: IconRegistry.SAFE_DEFAULT

    override fun getTabCompletes(
        sender: CommandSender,
        args: Array<out String>,
    ): Collection<String> = IconRegistry.allNames()
}
