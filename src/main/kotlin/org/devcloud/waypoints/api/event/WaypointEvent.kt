package org.devcloud.waypoints.api.event

import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.devcloud.waypoints.domain.Waypoint

abstract class WaypointEvent(
    val sender: CommandSender,
    val waypoint: Waypoint,
) : Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}
