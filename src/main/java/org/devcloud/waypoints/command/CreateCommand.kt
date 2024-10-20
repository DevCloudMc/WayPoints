package org.devcloud.waypoints.command

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.UserManager
import org.devcloud.waypoints.manager.WayPointsManager
import org.devcloud.waypoints.util.PermissionList

class CreateCommand(
    private val userManager: UserManager,
    private val languageConfig: LanguageConfig
) : CommandExecutable {

    override fun execute(sender: CommandSender, args: Array<out String>, parameters: CommandParameters) {
        val user = userManager.getOrCreate((sender as Player).uniqueId)
        val player = user.player

        if (user.wayPoints.size >= user.maxPoint) {
            player.sendMessage(languageConfig.of(LanguagePath.MAX_POINTS))
            return
        }

        val wayPointName = parameters.get(String::class.java, 2) ?: run {
            player.sendMessage(languageConfig.of(LanguagePath.ENTER_NAME))
            return
        }

        val waypointType = parameters.get(String::class.java, 3)
            ?: MapCursor.Type.PLAYER_OFF_MAP.toString()


        val type: String = if (!player.hasPermission(PermissionList.EXTENDED)) {
            MapCursor.Type.PLAYER_OFF_MAP.toString()
        } else {
            try {
                val namespacedKey = NamespacedKey.minecraft(waypointType.lowercase())
                val type = Registry.MAP_DECORATION_TYPE[namespacedKey]
                    ?: throw IllegalArgumentException()
                type.key.key
            } catch (e: Exception) {
                val cursorTypeConfig = languageConfig.getReplaceCursorTypeConfig(waypointType)
                player.sendMessage(
                    languageConfig.of(LanguagePath.DIFFERENT_TYPE)
                        .replaceText(cursorTypeConfig)
                )
                return
            }
        }.lowercase()

        val isRecreateAction = user.wayPoints.keys.contains(wayPointName)
        WayPointsManager.createWayPoint(user, wayPointName, type)

        val pointNameConfig = languageConfig.getReplacePointNameConfig(wayPointName)
        val resultMessage =
            languageConfig.of(if (isRecreateAction) LanguagePath.POINT_RECREATED else LanguagePath.POINT_CREATED)
                .replaceText(pointNameConfig)

        player.sendMessage(resultMessage)

    }
}