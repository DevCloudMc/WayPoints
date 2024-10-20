package org.devcloud.waypoints.command.global

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.UserManager
import org.devcloud.waypoints.manager.WayPointsManager

class GlobalCreateCommand(
    private val userManager: UserManager,
    private val languageConfig: LanguageConfig
) : CommandExecutable {

    override fun execute(sender: CommandSender, args: Array<out String>, parameters: CommandParameters) {
        val user = userManager.getOrCreate((sender as Player).uniqueId)
        val player = user.player

        val wayPointName = parameters.get(String::class.java, 3) ?: run {
            player.sendMessage(languageConfig.of(LanguagePath.ENTER_NAME))
            return
        }

        val waypointType = parameters.get(String::class.java, 4)?.lowercase() ?: run {
            player.sendMessage(languageConfig.of(LanguagePath.CHOOSE_TYPE))
            return
        }

        try {
            val namespacedKey = NamespacedKey.minecraft(waypointType)
            Registry.MAP_DECORATION_TYPE[namespacedKey]
        } catch (e: IllegalArgumentException) {
            val cursorTypeConfig = languageConfig.getReplaceCursorTypeConfig(waypointType)
            player.sendMessage(
                languageConfig.of(LanguagePath.DIFFERENT_TYPE)
                    .replaceText(cursorTypeConfig)
            )
            return
        }

        val wayPointNameComponent = Component.text(wayPointName)
        val legacyName = LegacyComponentSerializer.legacySection().serialize(wayPointNameComponent)

        WayPointsManager.createGlobalWayPoint(user, legacyName, waypointType)

        val pointNameConfig = languageConfig.getReplacePointNameConfig(wayPointName)

        val resultMessage = languageConfig.of(LanguagePath.POINT_CREATED)
            .replaceText(pointNameConfig) as TextComponent

        player.sendMessage(resultMessage)
    }
}