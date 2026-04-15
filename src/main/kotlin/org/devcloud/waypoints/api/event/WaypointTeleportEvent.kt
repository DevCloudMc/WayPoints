package org.devcloud.waypoints.api.event

import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.devcloud.waypoints.domain.Waypoint

/**
 * Fired just before a player is teleported to one of their own waypoints via `/wp tp`.
 *
 * Fires **after** the cooldown check has passed and the destination world has been verified as
 * loaded, but **before** [org.bukkit.entity.Player.teleport] is called. Cancelling the event denies
 * the teleport; no cooldown is applied, so the player can try again immediately.
 *
 * This is the hook to use for companion addons that want to charge a Vault economy balance, play a
 * sound, or gate teleport on a per-world flag.
 *
 * ### Example — charge a flat 100 coins via Vault before each teleport
 *
 * ```java
 * @EventHandler
 * public void on(WaypointTeleportEvent event) {
 *     Player player = (Player) event.getSender();
 *     if (!economy.has(player, 100)) {
 *         event.setCancelled(true);
 *         player.sendMessage("§cYou need 100 coins to teleport.");
 *         return;
 *     }
 *     economy.withdrawPlayer(player, 100);
 * }
 * ```
 *
 * @see WaypointEvent
 * @since 3.0.0
 */
class WaypointTeleportEvent(sender: CommandSender, waypoint: Waypoint) :
    WaypointEvent(sender, waypoint) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic private val HANDLERS = HandlerList()

        /** Bukkit handler list accessor. */
        @JvmStatic fun getHandlerList(): HandlerList = HANDLERS
    }
}
