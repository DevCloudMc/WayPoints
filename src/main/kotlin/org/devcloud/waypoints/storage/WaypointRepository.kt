package org.devcloud.waypoints.storage

import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointId
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface WaypointRepository {
    fun findById(id: WaypointId): CompletableFuture<Waypoint?>

    fun findByOwnerAndName(owner: UUID, name: String): CompletableFuture<Waypoint?>

    fun findGlobalByName(name: String): CompletableFuture<Waypoint?>

    fun listByOwner(owner: UUID): CompletableFuture<List<Waypoint>>

    fun listGlobal(): CompletableFuture<List<Waypoint>>

    fun save(wp: Waypoint): CompletableFuture<Unit>

    fun delete(id: WaypointId): CompletableFuture<Boolean>

    fun deleteAllByOwner(owner: UUID): CompletableFuture<Int>
}
