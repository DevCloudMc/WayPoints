package org.devcloud.waypoints.command.global

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.command.CommandName
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.GlobalPointManager
import org.devcloud.waypoints.manager.UserManager

class GlobalListCommand(
    private val globalPointManager: GlobalPointManager,
    private val languageConfig: LanguageConfig
) : CommandExecutable {

    override fun execute(sender: CommandSender, args: Array<out String>, parameters: CommandParameters) {
        if (globalPointManager.pointMap.isEmpty()) {
            sender.sendMessage(languageConfig.of(LanguagePath.POINT_LIST_EMPTY))
            return
        }

        var resultMessage = Component.text()
        val pointsList = globalPointManager.pointMap.keys
        pointsList.forEachIndexed{ index, markName ->
            if (index != 0 && index < pointsList.size) {
                resultMessage.append(Component.newline())
            }
            val deleteMessage = languageConfig.of(LanguagePath.POINT_LIST_DELETE)

            val deleteButton = deleteMessage
                .hoverEvent(languageConfig.of(LanguagePath.HOVER_MESSAGE))
                .clickEvent(
                    ClickEvent.runCommand(
                        "/${CommandName.BASE_COMMAND} ${CommandName.GLOBAL_COMMAND} ${CommandName.DELETE_COMMAND} $markName"
                    )
                )

            val pointMessage = Component.text("-")
                .appendSpace()
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(markName))
                .appendSpace()
                .append(deleteButton)
            resultMessage = resultMessage
                .append(pointMessage)
        }
        sender.sendMessage(resultMessage)
    }
}