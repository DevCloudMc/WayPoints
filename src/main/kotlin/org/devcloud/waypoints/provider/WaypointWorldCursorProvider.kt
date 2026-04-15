package org.devcloud.waypoints.provider

import io.github.bananapuncher714.cartographer.core.api.WorldCursor
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider
import io.github.bananapuncher714.cartographer.core.map.Minimap
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting
import org.bukkit.Location
import org.bukkit.entity.Player
import org.devcloud.waypoints.domain.VisibilityState
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.service.ShareService
import org.devcloud.waypoints.service.VisibilityService
import org.devcloud.waypoints.service.WaypointDistanceService
import org.devcloud.waypoints.service.WaypointService

class WaypointWorldCursorProvider(
    private val waypointService: WaypointService,
    private val shareService: ShareService,
    private val visibilityService: VisibilityService,
    private val hideLabelWithinBlocks: Double,
    private val labelDecimals: Int,
) : WorldCursorProvider {
    override fun getCursors(player: Player, map: Minimap, setting: PlayerSetting): Collection<WorldCursor> {
        val visibility = visibilityService.get(player.uniqueId)
        val out = ArrayList<WorldCursor>(8)
        val playerLoc = player.location

        if (!visibility.hidePersonal) {
            for (wp in waypointService.listOwned(player.uniqueId)) addCursor(out, wp, playerLoc, visibility)
        }
        if (!visibility.hideGlobal) {
            for (wp in waypointService.listGlobals()) addCursor(out, wp, playerLoc, visibility)
        }
        if (!visibility.hideShared) {
            for (id in shareService.listFor(player.uniqueId)) {
                val wp = waypointService.findById(id) ?: continue
                addCursor(out, wp, playerLoc, visibility)
            }
        }
        return out
    }

    private fun addCursor(
        out: MutableList<WorldCursor>,
        wp: Waypoint,
        playerLoc: Location,
        @Suppress("UNUSED_PARAMETER") visibility: VisibilityState,
    ) {
        val resolved = wp.location.resolve() ?: return
        val distance = WaypointDistanceService.horizontal(playerLoc, wp.location)
        val label =
            if (distance != null && distance > hideLabelWithinBlocks) {
                WaypointDistanceService.formatLabel(wp.name, distance, labelDecimals)
            } else {
                wp.name
            }
        out += WorldCursor(label, resolved, wp.icon, true)
    }
}
