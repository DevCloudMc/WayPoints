package org.devcloud.waypoints.api.event

import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.devcloud.waypoints.domain.Waypoint

/**
 * Fired just before a [Waypoint] is removed from the cache and storage.
 *
 * Cancelling the event keeps the waypoint intact. Fires for both personal (`/wp delete`) and global
 * (`/wp global delete`) deletions, and for admin wipes (`/wp admin user <player> wipe` — fires once
 * per wiped waypoint).
 *
 * @see WaypointEvent
 * @since 3.0.0
 */
class WaypointDeleteEvent(sender: CommandSender, waypoint: Waypoint) :
    WaypointEvent(sender, waypoint) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic private val HANDLERS = HandlerList()

        /** Bukkit handler list accessor. */
        @JvmStatic fun getHandlerList(): HandlerList = HANDLERS
    }
}
