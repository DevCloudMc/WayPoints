package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.service.WaypointService

class OwnedWaypointValidator(private val waypointService: WaypointService) : InputValidator<String> {
    override fun isValid(sender: CommandSender, input: Array<out String>, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        val name = input.firstOrNull() ?: return false
        return waypointService.findOwned(sender.uniqueId, name) != null
    }

    override fun get(sender: CommandSender, args: Array<out String>): String = args.first()

    override fun getTabCompletes(sender: CommandSender, args: Array<out String>): Collection<String> =
        if (sender is Player) waypointService.listOwned(sender.uniqueId).map { it.name } else emptyList()
}
