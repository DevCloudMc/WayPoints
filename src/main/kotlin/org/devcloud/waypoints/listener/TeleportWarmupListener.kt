package org.devcloud.waypoints.listener

import java.util.UUID
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask

class TeleportWarmupListener(
    private val schedule: (delayTicks: Long, task: Runnable) -> BukkitTask,
    private val cancelOnMove: () -> Boolean,
    private val onCancelled: (Player) -> Unit,
) : Listener {
    // Touched only from the main thread.
    private val pending = HashMap<UUID, BukkitTask>()

    fun start(player: Player, delayTicks: Long, teleport: () -> Unit) {
        val uuid = player.uniqueId
        val task =
            schedule(delayTicks) {
                pending.remove(uuid)
                teleport()
            }
        pending.put(uuid, task)?.cancel()
    }

    @EventHandler(ignoreCancelled = true)
    fun onMove(event: PlayerMoveEvent) {
        if (pending.isEmpty() || !cancelOnMove() || !event.hasChangedBlock()) return
        val task = pending.remove(event.player.uniqueId) ?: return
        task.cancel()
        onCancelled(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        pending.remove(event.player.uniqueId)?.cancel()
    }
}
