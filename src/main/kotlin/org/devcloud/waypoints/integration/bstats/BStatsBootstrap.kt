package org.devcloud.waypoints.integration.bstats

import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bukkit.plugin.java.JavaPlugin
import org.devcloud.waypoints.config.ConfigSchema

object BStatsBootstrap {
    private const val PLUGIN_ID = 30756

    fun start(plugin: JavaPlugin, cfg: ConfigSchema) {
        if (!cfg.metricsEnabled) return
        val metrics = Metrics(plugin, PLUGIN_ID)
        metrics.addCustomChart(SimplePie("storage_backend") { cfg.storageType.name.lowercase() })
    }
}
