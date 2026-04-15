package org.devcloud.waypoints.service

import java.time.Clock
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.domain.WaypointLocation
import org.devcloud.waypoints.domain.WaypointScope
import org.devcloud.waypoints.domain.error.WaypointError
import org.devcloud.waypoints.storage.StorageBackend
import org.devcloud.waypoints.util.Outcome

class WaypointService(
    private val storage: StorageBackend,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val nameRegex = Regex("^[A-Za-z0-9_-]{1,32}$")

    private val personalCache = ConcurrentHashMap<UUID, ConcurrentHashMap<String, Waypoint>>()
    private val globalCache = ConcurrentHashMap<String, Waypoint>()

    fun warmGlobals(): CompletableFuture<Unit> =
        storage.waypoints.listGlobal().thenApply { list ->
            globalCache.clear()
            list.forEach { globalCache[it.name] = it }
            Unit
        }

    fun warmPlayer(owner: UUID): CompletableFuture<Unit> =
        storage.waypoints.listByOwner(owner).thenApply { list ->
            val map = ConcurrentHashMap<String, Waypoint>()
            list.forEach { map[it.name] = it }
            personalCache[owner] = map
            Unit
        }

    fun forgetPlayer(owner: UUID) {
        personalCache.remove(owner)
    }

    fun listOwned(owner: UUID): List<Waypoint> = personalCache[owner]?.values?.toList().orEmpty()

    fun listGlobals(): List<Waypoint> = globalCache.values.toList()

    fun findOwned(owner: UUID, name: String): Waypoint? = personalCache[owner]?.get(name)

    fun findGlobal(name: String): Waypoint? = globalCache[name]

    fun findById(id: WaypointId): Waypoint? {
        for (map in personalCache.values) {
            for (wp in map.values) {
                if (wp.id == id) return wp
            }
        }
        for (wp in globalCache.values) {
            if (wp.id == id) return wp
        }
        return null
    }

    fun createPersonal(
        owner: UUID,
        name: String,
        icon: MapCursor.Type,
        location: WaypointLocation,
        limit: Int,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        if (!nameRegex.matches(name)) return done(Outcome.Err(WaypointError.InvalidName(name)))
        val owned = personalCache.getOrPut(owner) { ConcurrentHashMap() }
        if (owned.containsKey(name)) return done(Outcome.Err(WaypointError.NameTaken(name)))
        if (owned.size >= limit)
            return done(Outcome.Err(WaypointError.LimitReached(owned.size, limit)))
        val wp =
            Waypoint(
                WaypointId.random(),
                owner,
                name,
                icon,
                location,
                WaypointScope.PERSONAL,
                Instant.now(clock),
            )
        owned[name] = wp
        return storage.waypoints.save(wp).thenApply {
            Outcome.Ok(wp) as Outcome<Waypoint, WaypointError>
        }
    }

    fun createGlobal(
        name: String,
        icon: MapCursor.Type,
        location: WaypointLocation,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        if (!nameRegex.matches(name)) return done(Outcome.Err(WaypointError.InvalidName(name)))
        if (globalCache.containsKey(name)) return done(Outcome.Err(WaypointError.NameTaken(name)))
        val wp =
            Waypoint(
                WaypointId.random(),
                null,
                name,
                icon,
                location,
                WaypointScope.GLOBAL,
                Instant.now(clock),
            )
        globalCache[name] = wp
        return storage.waypoints.save(wp).thenApply {
            Outcome.Ok(wp) as Outcome<Waypoint, WaypointError>
        }
    }

    fun deletePersonal(
        owner: UUID,
        name: String,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        val wp =
            personalCache[owner]?.remove(name)
                ?: return done(Outcome.Err(WaypointError.NotFound(name)))
        return storage.waypoints.delete(wp.id).thenApply {
            Outcome.Ok(wp) as Outcome<Waypoint, WaypointError>
        }
    }

    fun deleteGlobal(name: String): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        val wp = globalCache.remove(name) ?: return done(Outcome.Err(WaypointError.NotFound(name)))
        return storage.waypoints.delete(wp.id).thenApply {
            Outcome.Ok(wp) as Outcome<Waypoint, WaypointError>
        }
    }

    fun renamePersonal(
        owner: UUID,
        old: String,
        new: String,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        if (!nameRegex.matches(new)) return done(Outcome.Err(WaypointError.InvalidName(new)))
        val owned = personalCache[owner] ?: return done(Outcome.Err(WaypointError.NotFound(old)))
        val wp = owned[old] ?: return done(Outcome.Err(WaypointError.NotFound(old)))
        if (owned.containsKey(new)) return done(Outcome.Err(WaypointError.NameTaken(new)))
        val renamed = wp.copy(name = new)
        owned.remove(old)
        owned[new] = renamed
        return storage.waypoints.save(renamed).thenApply {
            Outcome.Ok(renamed) as Outcome<Waypoint, WaypointError>
        }
    }

    fun renameGlobal(
        old: String,
        new: String,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        if (!nameRegex.matches(new)) return done(Outcome.Err(WaypointError.InvalidName(new)))
        val wp = globalCache[old] ?: return done(Outcome.Err(WaypointError.NotFound(old)))
        if (globalCache.containsKey(new)) return done(Outcome.Err(WaypointError.NameTaken(new)))
        val renamed = wp.copy(name = new)
        globalCache.remove(old)
        globalCache[new] = renamed
        return storage.waypoints.save(renamed).thenApply {
            Outcome.Ok(renamed) as Outcome<Waypoint, WaypointError>
        }
    }

    private fun <T> done(value: T): CompletableFuture<T> = CompletableFuture.completedFuture(value)
}
