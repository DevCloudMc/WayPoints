package org.devcloud.waypoints.storage

import org.devcloud.waypoints.domain.PlayerProfile
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PlayerRepository {
    fun loadProfile(uuid: UUID): CompletableFuture<PlayerProfile>

    fun saveProfile(profile: PlayerProfile): CompletableFuture<Unit>
}
