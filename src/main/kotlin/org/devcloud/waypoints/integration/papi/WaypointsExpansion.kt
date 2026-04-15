package org.devcloud.waypoints.integration.papi

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.devcloud.waypoints.service.WaypointDistanceService
import org.devcloud.waypoints.service.WaypointService
import org.devcloud.waypoints.util.PermissionLimit

class WaypointsExpansion(
    private val waypointService: WaypointService,
    private val pluginVersion: String,
) : PlaceholderExpansion() {
    override fun getIdentifier() = "waypoints"

    override fun getAuthor() = "DevCloud"

    override fun getVersion() = pluginVersion

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val online = player?.player ?: return null
        return when (params.lowercase()) {
            "count" -> waypointService.listOwned(online.uniqueId).size.toString()
            "max" -> PermissionLimit.compute(online, "waypoints.limit").toString()
            "nearest_name" -> nearest(online)?.first.orEmpty()
            "nearest_distance" -> nearest(online)?.second?.let { "%.1f".format(it) }.orEmpty()
            else -> null
        }
    }

    private fun nearest(player: Player): Pair<String, Double>? {
        val all = waypointService.listOwned(player.uniqueId) + waypointService.listGlobals()
        var bestName: String? = null
        var bestDist = Double.MAX_VALUE
        for (wp in all) {
            val d = WaypointDistanceService.horizontal(player.location, wp.location) ?: continue
            if (d < bestDist) {
                bestDist = d
                bestName = wp.name
            }
        }
        return bestName?.let { it to bestDist }
    }
}
