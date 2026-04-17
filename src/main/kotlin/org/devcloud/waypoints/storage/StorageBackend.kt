package org.devcloud.waypoints.storage

import java.util.concurrent.CompletableFuture

interface StorageBackend : AutoCloseable {
    val type: StorageType
    val waypoints: WaypointRepository
    val shares: ShareRepository
    val players: PlayerRepository

    fun init(): CompletableFuture<Unit>

    fun exportAll(): CompletableFuture<StorageSnapshot>

    fun importAll(snapshot: StorageSnapshot): CompletableFuture<Unit>

    override fun close()
}
