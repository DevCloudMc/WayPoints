package org.devcloud.waypoints.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.test.assertTrue
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.storage.StorageBackend
import org.junit.jupiter.api.Test

class ShareServiceTest {
    @Test
    fun `unshare reaches storage when the target is offline`() {
        val id = WaypointId.random()
        val target = UUID.randomUUID()
        val backend = mockk<StorageBackend>()
        every { backend.shares.remove(id, target) } returns CompletableFuture.completedFuture(true)

        assertTrue(ShareService(backend).unshare(id, target).get())
        verify { backend.shares.remove(id, target) }
    }
}
