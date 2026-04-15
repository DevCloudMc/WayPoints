package org.devcloud.waypoints.storage

import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.domain.WaypointShare

interface ShareRepository {
    fun add(share: WaypointShare): CompletableFuture<Boolean>

    fun remove(id: WaypointId, target: UUID): CompletableFuture<Boolean>

    fun listSharedWith(target: UUID): CompletableFuture<List<WaypointShare>>

    fun listSharesFor(id: WaypointId): CompletableFuture<List<WaypointShare>>

    fun removeAllByOwner(owner: UUID): CompletableFuture<Int>
}
