package org.devcloud.waypoints.config

import java.time.Duration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.domain.IconRegistry
import org.devcloud.waypoints.storage.StorageType

data class ConfigSchema(
    val storageType: StorageType,
    val sqliteFile: String,
    val yamlFile: String,
    val hideLabelWithinBlocks: Double,
    val labelDistanceDecimals: Int,
    val teleportEnabled: Boolean,
    val teleportCooldown: Duration,
    val teleportWarmup: Duration,
    val teleportCancelOnMove: Boolean,
    val defaultIcon: MapCursor.Type,
    val metricsEnabled: Boolean,
) {
    companion object {
        fun load(cfg: YamlConfiguration): ConfigSchema =
            ConfigSchema(
                storageType =
                    StorageType.valueOf(cfg.getString("storage.type", "sqlite")!!.uppercase()),
                sqliteFile = cfg.getString("storage.sqlite.file", "database.db")!!,
                yamlFile = cfg.getString("storage.yaml.file", "waypoints.yml")!!,
                hideLabelWithinBlocks = cfg.getDouble("display.hide-label-within-blocks", 5.0),
                labelDistanceDecimals = cfg.getInt("display.label-distance-decimals", 0),
                teleportEnabled = cfg.getBoolean("teleport.enabled", true),
                teleportCooldown =
                    Duration.ofSeconds(cfg.getInt("teleport.cooldown-seconds", 10).toLong()),
                teleportWarmup =
                    Duration.ofSeconds(cfg.getInt("teleport.warmup-seconds", 0).toLong()),
                teleportCancelOnMove = cfg.getBoolean("teleport.cancel-on-move", true),
                defaultIcon =
                    IconRegistry.parse(cfg.getString("defaults.icon", "PLAYER_OFF_MAP")!!)
                        ?: IconRegistry.SAFE_DEFAULT,
                metricsEnabled = cfg.getBoolean("advanced.metrics", true),
            )
    }
}
