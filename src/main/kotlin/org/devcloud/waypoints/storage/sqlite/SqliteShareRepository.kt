package org.devcloud.waypoints.storage.sqlite

import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.domain.WaypointShare
import org.devcloud.waypoints.storage.ShareRepository
import java.sql.SQLException
import java.util.UUID
import java.util.concurrent.CompletableFuture

class SqliteShareRepository(private val pool: ConnectionPool) : ShareRepository {
    override fun add(share: WaypointShare): CompletableFuture<Boolean> =
        pool.submit { c ->
            try {
                c.prepareStatement(
                    "INSERT INTO waypoint_shares (waypoint_id, shared_with, shared_at) VALUES (?, ?, ?)"
                ).use { ps ->
                    ps.setString(1, share.waypointId.toString())
                    ps.setString(2, share.sharedWith.toString())
                    ps.setLong(3, share.sharedAt.toEpochMilli())
                    ps.executeUpdate()
                }
                true
            } catch (e: SQLException) {
                val msg = e.message ?: throw e
                if (msg.contains("UNIQUE", true) || msg.contains("PRIMARY KEY", true)) false else throw e
            }
        }

    override fun remove(id: WaypointId, target: UUID): CompletableFuture<Boolean> =
        pool.submit { c ->
            c.prepareStatement("DELETE FROM waypoint_shares WHERE waypoint_id = ? AND shared_with = ?").use { ps ->
                ps.setString(1, id.toString())
                ps.setString(2, target.toString())
                ps.executeUpdate() > 0
            }
        }

    override fun listSharedWith(target: UUID): CompletableFuture<List<WaypointShare>> =
        pool.submit { c ->
            c.prepareStatement("SELECT * FROM waypoint_shares WHERE shared_with = ?").use { ps ->
                ps.setString(1, target.toString())
                ps.executeQuery().use { rs ->
                    val out = mutableListOf<WaypointShare>()
                    while (rs.next()) out += SqliteRowMappers.share(rs)
                    out
                }
            }
        }

    override fun listSharesFor(id: WaypointId): CompletableFuture<List<WaypointShare>> =
        pool.submit { c ->
            c.prepareStatement("SELECT * FROM waypoint_shares WHERE waypoint_id = ?").use { ps ->
                ps.setString(1, id.toString())
                ps.executeQuery().use { rs ->
                    val out = mutableListOf<WaypointShare>()
                    while (rs.next()) out += SqliteRowMappers.share(rs)
                    out
                }
            }
        }

    override fun removeAllByOwner(owner: UUID): CompletableFuture<Int> =
        pool.submit { c ->
            c.prepareStatement(
                """
                DELETE FROM waypoint_shares
                WHERE waypoint_id IN (SELECT id FROM waypoints WHERE owner = ?)
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, owner.toString())
                ps.executeUpdate()
            }
        }
}
