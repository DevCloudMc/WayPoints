package org.devcloud.waypoints.api.event

import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.devcloud.waypoints.domain.Waypoint

/**
 * Base class for every cancellable Bukkit event emitted by WayPoints.
 *
 * Events fire **synchronously on the main thread**, immediately before the action takes effect, and
 * are [Cancellable]. Listeners that cancel an event prevent the corresponding state change:
 *
 * | Subclass                | Effect of cancelling                              |
 * |-------------------------|---------------------------------------------------|
 * | [WaypointCreateEvent]   | Waypoint not stored and not shown on the minimap. |
 * | [WaypointDeleteEvent]   | Waypoint preserved.                               |
 * | [WaypointRenameEvent]   | Waypoint keeps its previous name.                 |
 * | [WaypointShareEvent]    | Share not granted.                                |
 * | [WaypointUnshareEvent]  | Share retained.                                   |
 * | [WaypointTeleportEvent] | Teleport request denied; no cooldown applied.     |
 *
 * ### Example — deny creation of waypoints named `spawn`
 *
 * ```kotlin
 * class NoSpawnListener : Listener {
 *     @EventHandler
 *     fun on(event: WaypointCreateEvent) {
 *         if (event.waypoint.name.equals("spawn", ignoreCase = true)) {
 *             event.isCancelled = true
 *             event.sender.sendMessage("Reserved name")
 *         }
 *     }
 * }
 * ```
 *
 * @property sender the command sender that triggered the action. For personal commands this is
 *   always the player themselves; for global/admin commands it can be a player, the console, or
 *   another plugin.
 * @property waypoint the waypoint the event is about. Treat it as immutable — mutating it after an
 *   event fires does not update the cache.
 * @since 2.0.0
 */
abstract class WaypointEvent(val sender: CommandSender, val waypoint: Waypoint) :
    Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}
