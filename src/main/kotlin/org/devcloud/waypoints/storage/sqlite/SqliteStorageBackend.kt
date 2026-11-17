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

    override fun init(): CompletableFuture<Unit> = pool.submit { c -> SqliteSchema.apply(c) }

    override fun exportAll(): CompletableFuture<StorageSnapshot> = pool.submit { c ->
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

    // Every pool task runs on one thread in submission order, so the writes between the first
    // and the last task below belong to this transaction.
    override fun importAll(snapshot: StorageSnapshot): CompletableFuture<Unit> {
        val steps = mutableListOf<CompletableFuture<*>>()
        steps += pool.submit { c ->
            c.autoCommit = false
            c.createStatement().use { st ->
                st.executeUpdate("DELETE FROM waypoint_shares")
                st.executeUpdate("DELETE FROM waypoints")
                st.executeUpdate("DELETE FROM player_profiles")
            }
        }
        snapshot.waypoints.forEach { steps += waypoints.save(it) }
        snapshot.shares.forEach { steps += shares.add(it) }
        snapshot.profiles.forEach { steps += players.saveProfile(it) }
        return pool.submit { c ->
            val failed = steps.firstOrNull { it.isCompletedExceptionally }
            if (failed == null) c.commit() else c.rollback()
            c.autoCommit = true
            if (failed != null) failed.join()
        }
    }

    override fun close() = pool.close()
}
