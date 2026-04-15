package org.devcloud.waypoints.api.event

import java.util.*
import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.devcloud.waypoints.domain.Waypoint

/**
 * Fired just before a previously shared personal [Waypoint] is revoked from another player.
 *
 * Cancelling the event keeps the share in place.
 *
 * @property target UUID of the player losing access to the waypoint.
 * @see WaypointEvent
 * @see WaypointShareEvent
 * @since 3.0.0
 */
class WaypointUnshareEvent(sender: CommandSender, waypoint: Waypoint, val target: UUID) :
    WaypointEvent(sender, waypoint) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic private val HANDLERS = HandlerList()

        /** Bukkit handler list accessor. */
        @JvmStatic fun getHandlerList(): HandlerList = HANDLERS
    }
}
