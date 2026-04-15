package org.devcloud.waypoints.api.event

import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.devcloud.waypoints.domain.Waypoint

/**
 * Fired just before a new [Waypoint] is persisted and shown on the minimap.
 *
 * Cancelling the event rolls back the service-level cache write so the waypoint never becomes
 * visible, as if the player had never run the command.
 *
 * ### Example — enforce a per-world block-list
 *
 * ```java
 * @EventHandler
 * public void on(WaypointCreateEvent event) {
 *     if ("spawn".equals(event.getWaypoint().getLocation().getWorldName())) {
 *         event.setCancelled(true);
 *     }
 * }
 * ```
 *
 * Fires for both personal (`/wp create`) and global (`/wp global create`) waypoints.
 *
 * @see WaypointEvent
 * @since 3.0.0
 */
class WaypointCreateEvent(sender: CommandSender, waypoint: Waypoint) :
    WaypointEvent(sender, waypoint) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic private val HANDLERS = HandlerList()

        /** Bukkit handler list accessor. */
        @JvmStatic fun getHandlerList(): HandlerList = HANDLERS
    }
}
