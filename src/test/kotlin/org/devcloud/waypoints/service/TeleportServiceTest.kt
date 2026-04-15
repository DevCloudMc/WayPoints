package org.devcloud.waypoints.service

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class TeleportServiceTest {
    private val now = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun `no cooldown when never used`() {
        val svc = TeleportService(Duration.ofSeconds(10), Clock.fixed(now, ZoneOffset.UTC))
        assertEquals(Duration.ZERO, svc.remainingCooldown(UUID.randomUUID(), bypass = false))
    }

    @Test
    fun `cooldown remaining after use`() {
        val svc = TeleportService(Duration.ofSeconds(10), Clock.fixed(now, ZoneOffset.UTC))
        val uuid = UUID.randomUUID()
        svc.markUsed(uuid)
        val remaining = svc.remainingCooldown(uuid, bypass = false)
        assertTrue(remaining > Duration.ZERO)
    }

    @Test
    fun `bypass returns zero`() {
        val svc = TeleportService(Duration.ofSeconds(10), Clock.fixed(now, ZoneOffset.UTC))
        val uuid = UUID.randomUUID()
        svc.markUsed(uuid)
        assertEquals(Duration.ZERO, svc.remainingCooldown(uuid, bypass = true))
    }
}
