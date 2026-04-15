package org.devcloud.waypoints.storage.sqlite

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import org.devcloud.waypoints.domain.PlayerProfile
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointShare
import org.devcloud.waypoints.storage.StorageBackend
import org.devcloud.waypoints.storage.StorageSnapshot
import org.devcloud.waypoints.storage.StorageType

class SqliteStorageBackend(dbPath: Path) : StorageBackend {
    init {
        Files.createDirectories(dbPath.parent ?: dbPath.toAbsolutePath().parent)
    }

    private val pool = ConnectionPool(dbPath)

    override val type = StorageType.SQLITE
    override val waypoints = SqliteWaypointRepository(pool)
    override val shares = SqliteShareRepository(pool)
    override val players = SqlitePlayerRepository(pool)

    override fun init(): CompletableFuture<Unit> =
        pool.submit { c ->
            SqliteSchema.apply(c)
            Unit
        }

    override fun exportAll(): CompletableFuture<StorageSnapshot> =
        pool.submit { c ->
            val wps = mutableListOf<Waypoint>()
            val sh = mutableListOf<WaypointShare>()
            val pr = mutableListOf<PlayerProfile>()
            c.prepareStatement("SELECT * FROM waypoints").use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) wps += SqliteRowMappers.waypoint(rs)
                }
            }
            c.prepareStatement("SELECT * FROM waypoint_shares").use { ps ->
                ps.executeQuery().use { rs -> while (rs.next()) sh += SqliteRowMappers.share(rs) }
            }
            c.prepareStatement("SELECT * FROM player_profiles").use { ps ->
                ps.executeQuery().use { rs -> while (rs.next()) pr += SqliteRowMappers.profile(rs) }
            }
            StorageSnapshot(wps, sh, pr)
        }

    override fun importAll(snapshot: StorageSnapshot): CompletableFuture<Unit> {
        val futures = mutableListOf<CompletableFuture<*>>()
        snapshot.waypoints.forEach { futures += waypoints.save(it) }
        snapshot.shares.forEach { futures += shares.add(it) }
        snapshot.profiles.forEach { futures += players.saveProfile(it) }
        return CompletableFuture.allOf(*futures.toTypedArray()).thenApply { Unit }
    }

    override fun close() = pool.close()
}
