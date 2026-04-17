package org.devcloud.waypoints.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.devcloud.waypoints.service.ShareService
import org.devcloud.waypoints.service.TeleportService
import org.devcloud.waypoints.service.VisibilityService
import org.devcloud.waypoints.service.WaypointService

class PlayerLifecycleListener(
    private val waypointService: WaypointService,
    private val shareService: ShareService,
    private val visibilityService: VisibilityService,
    private val teleportService: TeleportService,
) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId
        waypointService.warmPlayer(uuid)
        shareService.warmFor(uuid)
        visibilityService.warm(uuid)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        waypointService.forgetPlayer(uuid)
        shareService.forget(uuid)
        visibilityService.forget(uuid)
        teleportService.forget(uuid)
    }
}
