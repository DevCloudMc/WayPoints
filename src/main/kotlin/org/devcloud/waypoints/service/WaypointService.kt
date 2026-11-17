package org.devcloud.waypoints.service

import java.time.Clock
import java.time.Instant
import java.util.*
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

    // shortcut: entries of owners who logged out stay here so their shared waypoints keep
    // rendering; it grows up to the number of stored waypoints, evict when that gets large.
    private val byId = ConcurrentHashMap<WaypointId, Waypoint>()

    fun warmGlobals(): CompletableFuture<Unit> =
        storage.waypoints
            .listGlobal()
            .thenAccept { list ->
                globalCache.clear()
                list.forEach {
                    globalCache[it.name] = it
                    byId[it.id] = it
                }
            }
            .thenApply {}

    fun warmPlayer(owner: UUID): CompletableFuture<Unit> =
        storage.waypoints
            .listByOwner(owner)
            .thenAccept { list ->
                val map = ConcurrentHashMap<String, Waypoint>()
                list.forEach {
                    map[it.name] = it
                    byId[it.id] = it
                }
                personalCache[owner] = map
            }
            .thenApply {}

    fun forgetPlayer(owner: UUID) {
        personalCache.remove(owner)
    }

    fun listOwned(owner: UUID): List<Waypoint> = personalCache[owner]?.values?.toList().orEmpty()

    fun listGlobals(): List<Waypoint> = globalCache.values.toList()

    fun findOwned(owner: UUID, name: String): Waypoint? = personalCache[owner]?.get(name)

    fun findGlobal(name: String): Waypoint? = globalCache[name]

    fun findById(id: WaypointId): Waypoint? = byId[id]

    // Loads waypoints shared with a player whose owners have not been online since startup.
    fun warmShared(ids: Collection<WaypointId>): CompletableFuture<Unit> {
        val loads =
            ids.filterNot(byId::containsKey).map { id ->
                storage.waypoints.findById(id).thenAccept { wp ->
                    if (wp != null) byId.putIfAbsent(wp.id, wp)
                }
            }
        return CompletableFuture.allOf(*loads.toTypedArray()).thenApply {}
    }

    // An online owner keeps an empty list; removing it would make their next create fail as not
    // loaded.
    fun wipeOwner(owner: UUID) {
        personalCache[owner]?.clear()
        byId.values.removeIf { it.owner == owner }
    }

    fun createPersonal(
        owner: UUID,
        name: String,
        icon: MapCursor.Type,
        location: WaypointLocation,
        limit: Int,
        approve: (Waypoint) -> Boolean = { true },
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        if (!nameRegex.matches(name)) {
            return done(Outcome.Err(WaypointError.InvalidName(name)))
        }
        // No entry means the join warm-up has not finished; creating now would bypass the limit.
        val owned = personalCache[owner] ?: return done(Outcome.Err(WaypointError.NotLoaded))
        if (owned.containsKey(name)) {
            return done(Outcome.Err(WaypointError.NameTaken(name)))
        }
        if (owned.size >= limit) {
            return done(Outcome.Err(WaypointError.LimitReached(owned.size, limit)))
        }
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
        if (!approve(wp)) return done(Outcome.Err(WaypointError.Cancelled))
        owned[name] = wp
        return persisted(storage.waypoints.save(wp), null, wp) {
            owned.remove(name, wp)
        }
    }

    fun createGlobal(
        name: String,
        icon: MapCursor.Type,
        location: WaypointLocation,
        approve: (Waypoint) -> Boolean = { true },
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        if (!nameRegex.matches(name)) {
            return done(Outcome.Err(WaypointError.InvalidName(name)))
        }
        if (globalCache.containsKey(name)) {
            return done(Outcome.Err(WaypointError.NameTaken(name)))
        }
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
        if (!approve(wp)) return done(Outcome.Err(WaypointError.Cancelled))
        globalCache[name] = wp
        return persisted(storage.waypoints.save(wp), null, wp) {
            globalCache.remove(name, wp)
        }
    }

    fun deletePersonal(
        owner: UUID,
        name: String,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        val wp =
            personalCache[owner]?.remove(name)
                ?: return done(Outcome.Err(WaypointError.NotFound(name)))
        return persisted(storage.waypoints.delete(wp.id), wp, null) {
            personalCache[owner]?.putIfAbsent(name, wp)
        }
    }

    fun deleteGlobal(name: String): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        val wp = globalCache.remove(name) ?: return done(Outcome.Err(WaypointError.NotFound(name)))
        return persisted(storage.waypoints.delete(wp.id), wp, null) {
            globalCache.putIfAbsent(name, wp)
        }
    }

    fun renamePersonal(
        owner: UUID,
        old: String,
        new: String,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        if (!nameRegex.matches(new)) {
            return done(Outcome.Err(WaypointError.InvalidName(new)))
        }
        val owned = personalCache[owner] ?: return done(Outcome.Err(WaypointError.NotFound(old)))
        val wp = owned[old] ?: return done(Outcome.Err(WaypointError.NotFound(old)))
        if (owned.containsKey(new)) {
            return done(Outcome.Err(WaypointError.NameTaken(new)))
        }
        val renamed = wp.copy(name = new)
        owned.remove(old)
        owned[new] = renamed
        return persisted(storage.waypoints.save(renamed), wp, renamed) {
            owned.remove(new, renamed)
            owned.putIfAbsent(old, wp)
        }
    }

    fun renameGlobal(
        old: String,
        new: String,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        if (!nameRegex.matches(new)) {
            return done(Outcome.Err(WaypointError.InvalidName(new)))
        }
        val wp = globalCache[old] ?: return done(Outcome.Err(WaypointError.NotFound(old)))
        if (globalCache.containsKey(new)) {
            return done(Outcome.Err(WaypointError.NameTaken(new)))
        }
        val renamed = wp.copy(name = new)
        globalCache.remove(old)
        globalCache[new] = renamed
        return persisted(storage.waypoints.save(renamed), wp, renamed) {
            globalCache.remove(new, renamed)
            globalCache.putIfAbsent(old, wp)
        }
    }

    // The cache is written first so the minimap updates at once; a failed write undoes it.
    private fun persisted(
        write: CompletableFuture<*>,
        before: Waypoint?,
        now: Waypoint?,
        rollback: () -> Unit,
    ): CompletableFuture<Outcome<Waypoint, WaypointError>> {
        val result = requireNotNull(now ?: before)
        index(result.id, now)
        return write.handle { _, e ->
            if (e == null) {
                Outcome.Ok(result)
            } else {
                rollback()
                index(result.id, before)
                Outcome.Err(WaypointError.Storage(e))
            }
        }
    }

    private fun index(id: WaypointId, wp: Waypoint?) {
        if (wp == null) {
            byId.remove(id)
        } else {
            byId[id] = wp
        }
    }

    private fun <T> done(value: T): CompletableFuture<T> = CompletableFuture.completedFuture(value)
}
