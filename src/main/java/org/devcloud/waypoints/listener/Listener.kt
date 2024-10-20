package org.devcloud.waypoints.listener

import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent
import net.milkbowl.vault.permission.Permission
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.devcloud.waypoints.config.MainConfig
import org.devcloud.waypoints.manager.UserManager
import org.devcloud.waypoints.manager.WayPointsManager
import org.devcloud.waypoints.provider.WayPointsProvider
import org.bukkit.event.Listener as BukkitEventListener

class Listener(
    private val userManager: UserManager,
    private val mainConfig: MainConfig,
    private val permissionProvider: Permission,
    private val wayPointsProvider: WayPointsProvider
) : BukkitEventListener {

    @EventHandler(ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = userManager.getOrCreate(player.uniqueId)

        WayPointsManager.loadWayPoints(user)

        val group = permissionProvider.getPrimaryGroup(player)
        user.maxPoint = mainConfig.groups[group] ?: 0
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        userManager.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onMinimapLoad(event: MinimapLoadEvent) {
        event.minimap.registerProvider(wayPointsProvider)
    }
}
