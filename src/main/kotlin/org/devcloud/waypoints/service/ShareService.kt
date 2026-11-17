package org.devcloud.waypoints.service

import java.time.Clock
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.domain.WaypointShare
import org.devcloud.waypoints.storage.StorageBackend

class ShareService(
    private val storage: StorageBackend,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val cache = ConcurrentHashMap<UUID, MutableSet<WaypointId>>()

    fun warmFor(target: UUID): CompletableFuture<Unit> =
        storage.shares
            .listSharedWith(target)
            .thenAccept { list ->
                val set = ConcurrentHashMap.newKeySet<WaypointId>()
                list.forEach { set += it.waypointId }
                cache[target] = set
            }
            .thenApply {}

    fun forget(target: UUID) {
        cache.remove(target)
    }

    fun listFor(target: UUID): Set<WaypointId> = cache[target]?.toSet().orEmpty()

    // A missing cache entry means the target is offline: storage is then the only source of truth.
    fun share(id: WaypointId, target: UUID): CompletableFuture<Boolean> {
        val set = cache[target]
        if (set != null && !set.add(id)) return CompletableFuture.completedFuture(false)
        return storage.shares.add(WaypointShare(id, target, Instant.now(clock))).whenComplete { _, e
            ->
            if (e != null) set?.remove(id)
        }
    }

    fun unshare(id: WaypointId, target: UUID): CompletableFuture<Boolean> {
        val set = cache[target]
        if (set != null && !set.remove(id)) return CompletableFuture.completedFuture(false)
        return storage.shares.remove(id, target).whenComplete { _, e ->
            if (e != null) set?.add(id)
        }
    }
}
