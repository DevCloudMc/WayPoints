package org.devcloud.waypoints.api.event

import java.util.UUID
import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.devcloud.waypoints.domain.Waypoint

/**
 * Fired just before a personal [Waypoint] is shared with another player.
 *
 * [target] is the UUID of the receiving player. Cancelling the event prevents the share from being
 * recorded; the target sees nothing.
 *
 * ### Example — require that the target has a given permission
 *
 * ```java
 * @EventHandler
 * public void on(WaypointShareEvent event) {
 *     OfflinePlayer target = Bukkit.getOfflinePlayer(event.getTarget());
 *     if (!target.isOp() &&
 *         !Bukkit.getPlayer(event.getTarget()).hasPermission("waypoints.accept-shares")) {
 *         event.setCancelled(true);
 *     }
 * }
 * ```
 *
 * @property target UUID of the player the waypoint is being shared with.
 * @see WaypointEvent
 * @see WaypointUnshareEvent
 * @since 3.0.0
 */
class WaypointShareEvent(sender: CommandSender, waypoint: Waypoint, val target: UUID) :
    WaypointEvent(sender, waypoint) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic private val HANDLERS = HandlerList()

        /** Bukkit handler list accessor. */
        @JvmStatic fun getHandlerList(): HandlerList = HANDLERS
    }
}
