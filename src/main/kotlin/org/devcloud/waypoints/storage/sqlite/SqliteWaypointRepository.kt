package org.devcloud.waypoints.storage.sqlite

import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.devcloud.waypoints.domain.IconRegistry
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.storage.WaypointRepository

class SqliteWaypointRepository(private val pool: ConnectionPool) : WaypointRepository {
    override fun findById(id: WaypointId): CompletableFuture<Waypoint?> =
        pool.submit { c ->
            c.prepareStatement("SELECT * FROM waypoints WHERE id = ?").use { ps ->
                ps.setString(1, id.toString())
                ps.executeQuery().use { rs ->
                    if (rs.next()) SqliteRowMappers.waypoint(rs) else null
                }
            }
        }

    override fun findByOwnerAndName(owner: UUID, name: String): CompletableFuture<Waypoint?> =
        pool.submit { c ->
            c.prepareStatement(
                    "SELECT * FROM waypoints WHERE owner = ? AND scope = 'PERSONAL' AND name = ?"
                )
                .use { ps ->
                    ps.setString(1, owner.toString())
                    ps.setString(2, name)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) SqliteRowMappers.waypoint(rs) else null
                    }
                }
        }

    override fun findGlobalByName(name: String): CompletableFuture<Waypoint?> =
        pool.submit { c ->
            c.prepareStatement("SELECT * FROM waypoints WHERE scope = 'GLOBAL' AND name = ?").use {
                ps ->
                ps.setString(1, name)
                ps.executeQuery().use { rs ->
                    if (rs.next()) SqliteRowMappers.waypoint(rs) else null
                }
            }
        }

    override fun listByOwner(owner: UUID): CompletableFuture<List<Waypoint>> =
        pool.submit { c ->
            c.prepareStatement("SELECT * FROM waypoints WHERE owner = ?").use { ps ->
                ps.setString(1, owner.toString())
                ps.executeQuery().use { rs ->
                    val out = mutableListOf<Waypoint>()
                    while (rs.next()) out += SqliteRowMappers.waypoint(rs)
                    out
                }
            }
        }

    override fun listGlobal(): CompletableFuture<List<Waypoint>> =
        pool.submit { c ->
            c.prepareStatement("SELECT * FROM waypoints WHERE scope = 'GLOBAL'").use { ps ->
                ps.executeQuery().use { rs ->
                    val out = mutableListOf<Waypoint>()
                    while (rs.next()) out += SqliteRowMappers.waypoint(rs)
                    out
                }
            }
        }

    override fun save(wp: Waypoint): CompletableFuture<Unit> =
        pool.submit { c ->
            c.prepareStatement(
                    """
                INSERT INTO waypoints
                    (id, owner, name, icon, world, x, y, z, yaw, pitch, scope, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    name=excluded.name, icon=excluded.icon,
                    world=excluded.world, x=excluded.x, y=excluded.y, z=excluded.z,
                    yaw=excluded.yaw, pitch=excluded.pitch
                """
                        .trimIndent()
                )
                .use { ps ->
                    ps.setString(1, wp.id.toString())
                    ps.setString(2, wp.owner?.toString())
                    ps.setString(3, wp.name)
                    ps.setString(4, IconRegistry.serialize(wp.icon))
                    ps.setString(5, wp.location.worldName)
                    ps.setDouble(6, wp.location.x)
                    ps.setDouble(7, wp.location.y)
                    ps.setDouble(8, wp.location.z)
                    ps.setFloat(9, wp.location.yaw)
                    ps.setFloat(10, wp.location.pitch)
                    ps.setString(11, wp.scope.name)
                    ps.setLong(12, wp.createdAt.toEpochMilli())
                    ps.executeUpdate()
                    Unit
                }
        }

    override fun delete(id: WaypointId): CompletableFuture<Boolean> =
        pool.submit { c ->
            c.prepareStatement("DELETE FROM waypoints WHERE id = ?").use { ps ->
                ps.setString(1, id.toString())
                ps.executeUpdate() > 0
            }
        }

    override fun deleteAllByOwner(owner: UUID): CompletableFuture<Int> =
        pool.submit { c ->
            c.prepareStatement("DELETE FROM waypoints WHERE owner = ?").use { ps ->
                ps.setString(1, owner.toString())
                ps.executeUpdate()
            }
        }
}
