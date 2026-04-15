package org.devcloud.waypoints.storage.sqlite

import java.util.*
import java.util.concurrent.CompletableFuture
import org.devcloud.waypoints.domain.PlayerProfile
import org.devcloud.waypoints.domain.VisibilityState
import org.devcloud.waypoints.storage.PlayerRepository

class SqlitePlayerRepository(private val pool: ConnectionPool) : PlayerRepository {
    override fun loadProfile(uuid: UUID): CompletableFuture<PlayerProfile> =
        pool.submit { c ->
            c.prepareStatement("SELECT * FROM player_profiles WHERE uuid = ?").use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        PlayerProfile(
                            uuid = uuid,
                            visibility =
                                VisibilityState(
                                    hidePersonal = rs.getInt("hide_personal") == 1,
                                    hideGlobal = rs.getInt("hide_global") == 1,
                                    hideShared = rs.getInt("hide_shared") == 1,
                                ),
                        )
                    } else {
                        PlayerProfile(uuid)
                    }
                }
            }
        }

    override fun saveProfile(profile: PlayerProfile): CompletableFuture<Unit> =
        pool
            .submit { c ->
                c.prepareStatement(
                        """
                INSERT INTO player_profiles (uuid, hide_personal, hide_global, hide_shared)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    hide_personal = excluded.hide_personal,
                    hide_global   = excluded.hide_global,
                    hide_shared   = excluded.hide_shared
                """
                            .trimIndent()
                    )
                    .use { ps ->
                        ps.setString(1, profile.uuid.toString())
                        ps.setInt(
                            2,
                            if (profile.visibility.hidePersonal) {
                                1
                            } else {
                                0
                            },
                        )
                        ps.setInt(
                            3,
                            if (profile.visibility.hideGlobal) {
                                1
                            } else {
                                0
                            },
                        )
                        ps.setInt(
                            4,
                            if (profile.visibility.hideShared) {
                                1
                            } else {
                                0
                            },
                        )
                        ps.executeUpdate()
                    }
            }
            .thenApply {}
}
