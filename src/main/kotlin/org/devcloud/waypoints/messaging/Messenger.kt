package org.devcloud.waypoints.messaging

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

class Messenger(private val prefix: Component) {
    fun send(target: CommandSender, message: Component) {
        target.sendMessage(Component.empty().append(prefix).append(Component.space()).append(message))
    }
}
