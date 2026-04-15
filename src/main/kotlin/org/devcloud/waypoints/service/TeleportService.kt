package org.devcloud.waypoints.service

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TeleportService(
    private val cooldown: Duration,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val lastUsed = ConcurrentHashMap<UUID, Instant>()

    /** Returns remaining cooldown, or [Duration.ZERO] if the player may teleport now. */
    fun remainingCooldown(uuid: UUID, bypass: Boolean): Duration {
        if (bypass || cooldown.isZero || cooldown.isNegative) return Duration.ZERO
        val last = lastUsed[uuid] ?: return Duration.ZERO
        val elapsed = Duration.between(last, Instant.now(clock))
        return if (elapsed >= cooldown) Duration.ZERO else cooldown.minus(elapsed)
    }

    fun markUsed(uuid: UUID) {
        lastUsed[uuid] = Instant.now(clock)
    }

    fun forget(uuid: UUID) {
        lastUsed.remove(uuid)
    }
}
