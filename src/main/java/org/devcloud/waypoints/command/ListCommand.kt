package org.devcloud.waypoints.command

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.UserManager

class ListCommand(
    private val userManager: UserManager,
    private val languageConfig: LanguageConfig
) : CommandExecutable {

    override fun execute(sender: CommandSender, args: Array<out String>, parameters: CommandParameters) {
        val user = userManager.getOrCreate((sender as Player).uniqueId)
        if (user.wayPoints.isEmpty()) {
            sender.sendMessage(languageConfig.of(LanguagePath.POINT_LIST_EMPTY))
            return
        }

        user.wayPoints.keys.forEach { markName ->
            val deleteMessage = languageConfig.of(LanguagePath.POINT_LIST_DELETE)

            val deleteButton = deleteMessage
                .hoverEvent(languageConfig.of(LanguagePath.HOVER_MESSAGE))
                .clickEvent(ClickEvent.runCommand("/${CommandName.BASE_COMMAND} ${CommandName.DELETE_COMMAND} $markName"))

            val name = Component.text("-")
                .append(Component.space())
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(markName))
                .append(Component.space())
                .append(deleteButton)

            user.player.sendMessage(name)
        }
    }
}