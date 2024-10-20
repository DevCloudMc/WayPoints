package org.devcloud.waypoints.provider

import io.github.bananapuncher714.cartographer.core.api.WorldCursor
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider
import io.github.bananapuncher714.cartographer.core.map.Minimap
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Location
import org.bukkit.entity.Player
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.GlobalPointManager
import org.devcloud.waypoints.manager.UserManager
import org.devcloud.waypoints.manager.WayPoint
import kotlin.math.sqrt

class WayPointsProvider(
    private val userManager: UserManager,
    private val languageConfig: LanguageConfig,
    private val globalPointManager: GlobalPointManager
) : WorldCursorProvider {

    private var cursors: MutableSet<WorldCursor> = HashSet()

    override fun getCursors(player: Player, minimap: Minimap, playerSetting: PlayerSetting): Collection<WorldCursor> {
        cursors = HashSet()
        if (userManager.getOrCreate(player.uniqueId).hide) {
            return cursors
        }

        globalPointManager.pointMap.forEach { (markName, globalPoint) ->
            drawPoint(player, globalPoint, markName)
        }

        userManager.getOrCreate(player.uniqueId).wayPoints.forEach { (markName, personalPoint) ->
            drawPoint(player, personalPoint, markName)
        }

        return cursors
    }

    private fun drawPoint(
        player: Player,
        personalPoint: WayPoint,
        markName: String
    ) {
        if (player.location.world != personalPoint.location.world) {
            return
        }

        val distance = getHorizontalDistance(player.location, personalPoint.location)
        if (Math.round(distance) > 5) {
            val pointLocation = personalPoint.location.clone().apply {
                yaw = player.location.yaw
            }

            val nameReplacement = languageConfig.getReplaceConfig("%name%", markName)
            val distanceReplacement = languageConfig.getReplaceConfig("%distance%", Math.round(distance).toString())
            val nameComponent = languageConfig.of(LanguagePath.POINT_NAME)
                .replaceText(nameReplacement)
                .replaceText(distanceReplacement)

            val nameString = LegacyComponentSerializer.legacySection().serialize(nameComponent)
            //todo bad way. need to fix
            val finalNameString = nameString.replace('&', '§')

            cursors.add(WorldCursor(finalNameString, pointLocation, personalPoint.type, true))
        }
    }

    private fun getHorizontalDistanceSquare(loc1: Location, loc2: Location): Double {
        return square(loc1.x - loc2.x) + square(loc1.z - loc2.z)
    }

    private fun square(num: Double): Double {
        return num * num
    }

    private fun getHorizontalDistance(loc1: Location, loc2: Location): Double {
        return sqrt(getHorizontalDistanceSquare(loc1, loc2))
    }
}
