package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.manager.UserManager

class PlayerWayPointsListInputValidator(
    private val userManager: UserManager,
) : InputValidator<String> {

    override fun getTabCompletes(sender: CommandSender, input: Array<out String>): MutableCollection<String> {
        val user = userManager.getOrCreate((sender as Player).uniqueId)
        return user.wayPoints.map { it.key }.toMutableList()
    }

    override fun isValid(sender: CommandSender, input: Array<out String>, args: Array<out String>): Boolean {
        val user = userManager.getOrCreate((sender as Player).uniqueId)
        return user.wayPoints.keys.contains(input.joinToString())
    }

    override fun get(sender: CommandSender, input: Array<out String>): String {
        return input[0]
    }
} 