package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender

enum class VisibilityScope {
    PERSONAL,
    GLOBAL,
    SHARED,
    ALL,
}

class ScopeValidator : InputValidator<VisibilityScope> {
    override fun isValid(
        sender: CommandSender,
        input: Array<out String>,
        args: Array<out String>,
    ): Boolean {
        val v = input.firstOrNull()?.uppercase() ?: return false
        return runCatching { VisibilityScope.valueOf(v) }.isSuccess
    }

    override fun get(sender: CommandSender, args: Array<out String>): VisibilityScope =
        VisibilityScope.valueOf(args.first().uppercase())

    override fun getTabCompletes(
        sender: CommandSender,
        args: Array<out String>,
    ): Collection<String> = VisibilityScope.values().map { it.name.lowercase() }
}
