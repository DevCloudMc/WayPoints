package org.devcloud.waypoints.api.event

import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.devcloud.waypoints.domain.Waypoint

/**
 * Fired just before a [Waypoint] is renamed.
 *
 * [waypoint] holds the state **before** the rename (so `waypoint.name` is the old name). [newName]
 * is the validated target name; it will already have passed the `^[A-Za-z0-9_-]{1,32}$` regex by
 * the time your listener runs.
 *
 * Cancelling the event leaves the waypoint untouched.
 *
 * @property newName the rename target. Never blank, always matches the name regex.
 * @see WaypointEvent
 * @since 2.0.0
 */
class WaypointRenameEvent(sender: CommandSender, waypoint: Waypoint, val newName: String) :
    WaypointEvent(sender, waypoint) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic private val HANDLERS = HandlerList()

        /** Bukkit handler list accessor. */
        @JvmStatic fun getHandlerList(): HandlerList = HANDLERS
    }
}
