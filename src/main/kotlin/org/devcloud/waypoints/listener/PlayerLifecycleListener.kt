package org.devcloud.waypoints.listener

import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.devcloud.waypoints.service.ShareService
import org.devcloud.waypoints.service.VisibilityService
import org.devcloud.waypoints.service.WaypointService

class PlayerLifecycleListener(
    private val waypointService: WaypointService,
    private val shareService: ShareService,
    private val visibilityService: VisibilityService,
    private val logger: Logger,
) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        warm(event.player.uniqueId)
    }

    fun warm(uuid: UUID) {
        CompletableFuture.allOf(
                waypointService.warmPlayer(uuid),
                shareService.warmFor(uuid).thenCompose {
                    waypointService.warmShared(shareService.listFor(uuid))
                },
                visibilityService.warm(uuid),
            )
            .exceptionally { e ->
                logger.log(Level.SEVERE, "Failed to load waypoints for $uuid", e)
                null
            }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        waypointService.forgetPlayer(uuid)
        shareService.forget(uuid)
        visibilityService.forget(uuid)
    }
}
