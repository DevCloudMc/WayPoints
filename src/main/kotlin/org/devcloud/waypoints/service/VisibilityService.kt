package org.devcloud.waypoints.service

import org.devcloud.waypoints.domain.PlayerProfile
import org.devcloud.waypoints.domain.VisibilityState
import org.devcloud.waypoints.storage.StorageBackend
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class VisibilityService(private val storage: StorageBackend) {
    private val cache = ConcurrentHashMap<UUID, VisibilityState>()

    fun warm(uuid: UUID): CompletableFuture<Unit> =
        storage.players.loadProfile(uuid).thenApply { p ->
            cache[uuid] = p.visibility
            Unit
        }

    fun forget(uuid: UUID) {
        cache.remove(uuid)
    }

    fun get(uuid: UUID): VisibilityState = cache[uuid] ?: VisibilityState.ALL_VISIBLE

    fun set(uuid: UUID, state: VisibilityState): CompletableFuture<Unit> {
        cache[uuid] = state
        return storage.players.saveProfile(PlayerProfile(uuid, state))
    }
}
