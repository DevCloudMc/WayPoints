package org.devcloud.waypoints.storage

import java.util.*
import java.util.concurrent.CompletableFuture
import org.devcloud.waypoints.domain.PlayerProfile

interface PlayerRepository {
    fun loadProfile(uuid: UUID): CompletableFuture<PlayerProfile>

    fun saveProfile(profile: PlayerProfile): CompletableFuture<Unit>
}
