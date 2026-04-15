package org.devcloud.waypoints.listener

import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent
import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapUnloadEvent
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MinimapLifecycleListener(private val provider: WorldCursorProvider) : Listener {
    @EventHandler
    fun onLoad(event: MinimapLoadEvent) {
        event.minimap.registerProvider(provider)
    }

    @EventHandler
    fun onUnload(event: MinimapUnloadEvent) {
        event.minimap.unregisterProvider(provider)
    }
}
