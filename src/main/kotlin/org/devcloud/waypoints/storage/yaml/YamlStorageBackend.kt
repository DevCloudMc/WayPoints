package org.devcloud.waypoints.storage.yaml

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import org.bukkit.configuration.file.YamlConfiguration
import org.devcloud.waypoints.domain.IconRegistry
import org.devcloud.waypoints.domain.PlayerProfile
import org.devcloud.waypoints.domain.VisibilityState
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.domain.WaypointId
import org.devcloud.waypoints.domain.WaypointLocation
import org.devcloud.waypoints.domain.WaypointScope
import org.devcloud.waypoints.domain.WaypointShare
import org.devcloud.waypoints.storage.PlayerRepository
import org.devcloud.waypoints.storage.ShareRepository
import org.devcloud.waypoints.storage.StorageBackend
import org.devcloud.waypoints.storage.StorageSnapshot
import org.devcloud.waypoints.storage.StorageType
import org.devcloud.waypoints.storage.WaypointRepository

class YamlStorageBackend(private val file: Path) : StorageBackend {
    private val executor =
        Executors.newSingleThreadExecutor { r ->
            Thread(r, "WayPoints-YAML").apply { isDaemon = true }
        }

    private var cache: StorageSnapshot = StorageSnapshot(emptyList(), emptyList(), emptyList())

    override val type = StorageType.YAML

    override val waypoints: WaypointRepository =
        object : WaypointRepository {
            override fun findById(id: WaypointId) = submit {
                cache.waypoints.firstOrNull { it.id == id }
            }

            override fun findByOwnerAndName(owner: UUID, name: String) = submit {
                cache.waypoints.firstOrNull {
                    it.owner == owner && it.scope == WaypointScope.PERSONAL && it.name == name
                }
            }

            override fun findGlobalByName(name: String) = submit {
                cache.waypoints.firstOrNull { it.scope == WaypointScope.GLOBAL && it.name == name }
            }

            override fun listByOwner(owner: UUID) = submit {
                cache.waypoints.filter { it.owner == owner }
            }

            override fun listGlobal() = submit {
                cache.waypoints.filter { it.scope == WaypointScope.GLOBAL }
            }

            override fun save(wp: Waypoint) = submit {
                cache = cache.copy(waypoints = cache.waypoints.filterNot { it.id == wp.id } + wp)
                persist()
            }

            override fun delete(id: WaypointId) = submit {
                val before = cache.waypoints.size
                cache =
                    cache.copy(
                        waypoints = cache.waypoints.filterNot { it.id == id },
                        shares = cache.shares.filterNot { it.waypointId == id },
                    )
                val removed = cache.waypoints.size != before
                if (removed) {
                    persist()
                }
                removed
            }

            override fun deleteAllByOwner(owner: UUID) = submit {
                val toRemove = cache.waypoints.filter { it.owner == owner }
                val removedIds = toRemove.map { it.id }.toSet()
                cache =
                    cache.copy(
                        waypoints = cache.waypoints - toRemove.toSet(),
                        shares = cache.shares.filterNot { it.waypointId in removedIds },
                    )
                if (toRemove.isNotEmpty()) {
                    persist()
                }
                toRemove.size
            }
        }

    override val shares: ShareRepository =
        object : ShareRepository {
            override fun add(share: WaypointShare) = submit {
                if (
                    cache.shares.any {
                        it.waypointId == share.waypointId && it.sharedWith == share.sharedWith
                    }
                ) {
                    false
                } else {
                    cache = cache.copy(shares = cache.shares + share)
                    persist()
                    true
                }
            }

            override fun remove(id: WaypointId, target: UUID) = submit {
                val before = cache.shares.size
                cache =
                    cache.copy(
                        shares =
                            cache.shares.filterNot {
                                it.waypointId == id && it.sharedWith == target
                            }
                    )
                val removed = cache.shares.size != before
                if (removed) {
                    persist()
                }
                removed
            }

            override fun listSharedWith(target: UUID) = submit {
                cache.shares.filter { it.sharedWith == target }
            }

            override fun listSharesFor(id: WaypointId) = submit {
                cache.shares.filter { it.waypointId == id }
            }

            override fun removeAllByOwner(owner: UUID) = submit {
                val ownedIds = cache.waypoints.filter { it.owner == owner }.map { it.id }.toSet()
                val toRemove = cache.shares.filter { it.waypointId in ownedIds }
                cache = cache.copy(shares = cache.shares - toRemove.toSet())
                if (toRemove.isNotEmpty()) {
                    persist()
                }
                toRemove.size
            }
        }

    override val players: PlayerRepository =
        object : PlayerRepository {
            override fun loadProfile(uuid: UUID) = submit {
                cache.profiles.firstOrNull { it.uuid == uuid } ?: PlayerProfile(uuid)
            }

            override fun saveProfile(profile: PlayerProfile) = submit {
                cache =
                    cache.copy(
                        profiles = cache.profiles.filterNot { it.uuid == profile.uuid } + profile
                    )
                persist()
            }
        }

    override fun init(): CompletableFuture<Unit> = submit {
        Files.createDirectories(file.toAbsolutePath().parent)
        if (Files.exists(file)) {
            cache = load()
        } else {
            persist()
        }
    }

    override fun exportAll(): CompletableFuture<StorageSnapshot> = submit { cache }

    override fun importAll(snapshot: StorageSnapshot): CompletableFuture<Unit> = submit {
        cache = snapshot
        persist()
    }

    override fun close() {
        executor.shutdown()
    }

    private fun <T> submit(block: () -> T): CompletableFuture<T> {
        val cf = CompletableFuture<T>()
        executor.submit {
            try {
                cf.complete(block())
            } catch (e: Throwable) {
                cf.completeExceptionally(e)
            }
        }
        return cf
    }

    private fun load(): StorageSnapshot {
        val cfg = YamlConfiguration.loadConfiguration(file.toFile())
        val wps =
            cfg.getMapList("waypoints").map { row ->
                @Suppress("UNCHECKED_CAST") val m = row as Map<String, Any?>
                Waypoint(
                    id = WaypointId.parse(m["id"] as String),
                    owner = (m["owner"] as String?)?.let(UUID::fromString),
                    name = m["name"] as String,
                    icon = IconRegistry.parse(m["icon"] as String) ?: IconRegistry.SAFE_DEFAULT,
                    location =
                        WaypointLocation(
                            worldName = m["world"] as String,
                            x = (m["x"] as Number).toDouble(),
                            y = (m["y"] as Number).toDouble(),
                            z = (m["z"] as Number).toDouble(),
                            yaw = (m["yaw"] as Number).toFloat(),
                            pitch = (m["pitch"] as Number).toFloat(),
                        ),
                    scope = WaypointScope.valueOf(m["scope"] as String),
                    createdAt = Instant.ofEpochMilli((m["createdAt"] as Number).toLong()),
                )
            }
        val sh =
            cfg.getMapList("shares").map { row ->
                @Suppress("UNCHECKED_CAST") val m = row as Map<String, Any?>
                WaypointShare(
                    WaypointId.parse(m["waypointId"] as String),
                    UUID.fromString(m["sharedWith"] as String),
                    Instant.ofEpochMilli((m["sharedAt"] as Number).toLong()),
                )
            }
        val pr =
            cfg.getMapList("profiles").map { row ->
                @Suppress("UNCHECKED_CAST") val m = row as Map<String, Any?>
                PlayerProfile(
                    uuid = UUID.fromString(m["uuid"] as String),
                    visibility =
                        VisibilityState(
                            hidePersonal = (m["hidePersonal"] as Boolean?) ?: false,
                            hideGlobal = (m["hideGlobal"] as Boolean?) ?: false,
                            hideShared = (m["hideShared"] as Boolean?) ?: false,
                        ),
                )
            }
        return StorageSnapshot(wps, sh, pr)
    }

    private fun persist() {
        val cfg = YamlConfiguration()
        cfg.set(
            "waypoints",
            cache.waypoints.map { wp ->
                mapOf(
                    "id" to wp.id.toString(),
                    "owner" to wp.owner?.toString(),
                    "name" to wp.name,
                    "icon" to IconRegistry.serialize(wp.icon),
                    "world" to wp.location.worldName,
                    "x" to wp.location.x,
                    "y" to wp.location.y,
                    "z" to wp.location.z,
                    "yaw" to wp.location.yaw,
                    "pitch" to wp.location.pitch,
                    "scope" to wp.scope.name,
                    "createdAt" to wp.createdAt.toEpochMilli(),
                )
            },
        )
        cfg.set(
            "shares",
            cache.shares.map { s ->
                mapOf(
                    "waypointId" to s.waypointId.toString(),
                    "sharedWith" to s.sharedWith.toString(),
                    "sharedAt" to s.sharedAt.toEpochMilli(),
                )
            },
        )
        cfg.set(
            "profiles",
            cache.profiles.map { p ->
                mapOf(
                    "uuid" to p.uuid.toString(),
                    "hidePersonal" to p.visibility.hidePersonal,
                    "hideGlobal" to p.visibility.hideGlobal,
                    "hideShared" to p.visibility.hideShared,
                )
            },
        )
        Files.createDirectories(file.toAbsolutePath().parent)
        val tmp = file.resolveSibling("${file.fileName}.tmp")
        cfg.save(tmp.toFile())
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
}
