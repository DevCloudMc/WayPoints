package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender

class ShowHideValidator : InputValidator<Boolean> {
    override fun isValid(sender: CommandSender, input: Array<out String>, args: Array<out String>): Boolean {
        val v = input.firstOrNull()?.lowercase() ?: return false
        return v == "show" || v == "hide"
    }

    /** Returns true when the user wants to HIDE the given scope. */
    override fun get(sender: CommandSender, args: Array<out String>): Boolean = args.first().equals("hide", true)

    override fun getTabCompletes(sender: CommandSender, args: Array<out String>): Collection<String> =
        listOf("show", "hide")
}
