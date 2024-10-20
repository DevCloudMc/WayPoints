package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.manager.GlobalPointManager
import org.devcloud.waypoints.manager.UserManager

class GlobalWayPointsListInputValidator(
    private val globalPointManager: GlobalPointManager,
) : InputValidator<String> {

    override fun getTabCompletes(sender: CommandSender, input: Array<out String>): MutableCollection<String> {
        return globalPointManager.pointMap.map { it.key }.toMutableList()
    }

    override fun isValid(sender: CommandSender, input: Array<out String>, args: Array<out String>): Boolean {
        return globalPointManager.pointMap.keys.contains(input.joinToString())
    }

    override fun get(sender: CommandSender, input: Array<out String>): String {
        return input[0]
    }
} 