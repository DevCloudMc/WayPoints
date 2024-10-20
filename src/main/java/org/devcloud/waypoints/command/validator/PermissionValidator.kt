package org.devcloud.waypoints.command.validator

import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

@Deprecated("Use io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission")
private class PermissionValidator(vararg permissions: String) : SenderValidator {

    private val permissions: Set<String> = permissions.toSet()
    private var permissionMessage: Component =
        Component.text("You don't have permission to do that!").color(NamedTextColor.RED)

    override fun isValid(sender: CommandSender): Boolean {
        val isValid = permissions.any { sender.hasPermission(it) }
        if (!isValid) {
            notifyNoPermission(sender)
        }
        return isValid
    }

    private fun notifyNoPermission(sender: CommandSender) {
        sender.sendMessage(permissionMessage)
    }

    fun permissionMessage(permissionMessage: String): SenderValidator? {
        return permissionMessage(Component.text(permissionMessage))
    }

    fun permissionMessage(permissionMessage: Component): SenderValidator? {
        this.permissionMessage = permissionMessage
        return this
    }
}