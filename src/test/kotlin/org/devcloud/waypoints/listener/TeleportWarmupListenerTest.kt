package org.devcloud.waypoints.listener

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.assertEquals
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.Test

class TeleportWarmupListenerTest {
    private val player = mockk<Player> { every { uniqueId } returns UUID.randomUUID() }
    private val task = mockk<BukkitTask>(relaxed = true)
    private val scheduled = mutableListOf<Runnable>()
    private val cancelled = mutableListOf<Player>()
    private var teleports = 0

    private fun listener(cancelOnMove: Boolean) =
        TeleportWarmupListener(
            { _, r ->
                scheduled += r
                task
            },
            { cancelOnMove },
            { cancelled += it },
        )

    private fun move(changedBlock: Boolean) =
        mockk<PlayerMoveEvent> {
            every { player } returns this@TeleportWarmupListenerTest.player
            every { hasChangedBlock() } returns changedBlock
        }

    @Test
    fun `moving to another block cancels the pending teleport once`() {
        val l = listener(cancelOnMove = true)
        l.start(player, 60) { teleports++ }
        l.onMove(move(changedBlock = false))
        verify(exactly = 0) { task.cancel() }
        l.onMove(move(changedBlock = true))
        l.onMove(move(changedBlock = true))
        verify(exactly = 1) { task.cancel() }
        assertEquals(listOf(player), cancelled)
    }

    @Test
    fun `teleport runs after the delay when cancel on move is off`() {
        val l = listener(cancelOnMove = false)
        l.start(player, 60) { teleports++ }
        l.onMove(move(changedBlock = true))
        scheduled.single().run()
        assertEquals(1, teleports)
        assertEquals(emptyList(), cancelled)
    }
}
