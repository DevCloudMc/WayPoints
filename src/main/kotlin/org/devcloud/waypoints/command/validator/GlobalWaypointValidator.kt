package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.service.WaypointService

class GlobalWaypointValidator(private val waypointService: WaypointService) :
    InputValidator<String> {
    override fun isValid(
        sender: CommandSender,
        input: Array<out String>,
        args: Array<out String>,
    ): Boolean {
        val name = input.firstOrNull() ?: return false
        return waypointService.findGlobal(name) != null
    }

    override fun get(sender: CommandSender, args: Array<out String>): String = args.first()

    override fun getTabCompletes(
        sender: CommandSender,
        args: Array<out String>,
    ): Collection<String> = waypointService.listGlobals().map { it.name }
}
