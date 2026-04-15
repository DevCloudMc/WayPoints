package org.devcloud.waypoints

import io.github.bananapuncher714.cartographer.core.Cartographer
import io.github.bananapuncher714.cartographer.core.module.Module
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.devcloud.waypoints.api.WaypointsApi
import org.devcloud.waypoints.command.CommandTreeBuilder
import org.devcloud.waypoints.config.ConfigSchema
import org.devcloud.waypoints.config.LangSchema
import org.devcloud.waypoints.config.YamlConfigLoader
import org.devcloud.waypoints.integration.bstats.BStatsBootstrap
import org.devcloud.waypoints.integration.papi.WaypointsExpansion
import org.devcloud.waypoints.listener.MinimapLifecycleListener
import org.devcloud.waypoints.listener.PlayerLifecycleListener
import org.devcloud.waypoints.messaging.Messenger
import org.devcloud.waypoints.provider.WaypointWorldCursorProvider
import org.devcloud.waypoints.service.ShareService
import org.devcloud.waypoints.service.TeleportService
import org.devcloud.waypoints.service.VisibilityService
import org.devcloud.waypoints.service.WaypointService
import org.devcloud.waypoints.storage.StorageBackend
import org.devcloud.waypoints.storage.StorageType
import org.devcloud.waypoints.storage.sqlite.SqliteStorageBackend
import org.devcloud.waypoints.storage.yaml.YamlStorageBackend
import org.devcloud.waypoints.util.Async

class WayPointsBootstrap(val module: Module) {
    lateinit var config: ConfigSchema
        private set
    lateinit var lang: LangSchema
        private set
    lateinit var messenger: Messenger
        private set
    lateinit var storage: StorageBackend
        private set
    lateinit var waypointService: WaypointService
        private set
    lateinit var shareService: ShareService
        private set
    lateinit var visibilityService: VisibilityService
        private set
    lateinit var teleportService: TeleportService
        private set
    lateinit var provider: WaypointWorldCursorProvider
        private set
    lateinit var async: Async
        private set

    private val hostPlugin: JavaPlugin by lazy { Cartographer.getInstance() }

    fun start() {
        loadConfigs()
        async = Async(hostPlugin)
        storage = openBackend(config)
        storage.init().get()

        waypointService = WaypointService(storage)
        shareService = ShareService(storage)
        visibilityService = VisibilityService(storage)
        teleportService = TeleportService(config.teleportCooldown)

        waypointService.warmGlobals().get()

        provider =
            WaypointWorldCursorProvider(
                waypointService,
                shareService,
                visibilityService,
                config.hideLabelWithinBlocks,
                config.labelDistanceDecimals,
            )

        module.registerListener(
            PlayerLifecycleListener(waypointService, shareService, visibilityService, teleportService)
        )
        module.registerListener(MinimapLifecycleListener(provider))

        // Already-loaded minimaps won't fire MinimapLoadEvent again — register on them now.
        Cartographer.getInstance().mapManager.minimaps.values.forEach { it.registerProvider(provider) }

        // Warm profiles + personals for players that are already online (e.g. after /reload).
        for (player in Bukkit.getOnlinePlayers()) {
            val uuid = player.uniqueId
            waypointService.warmPlayer(uuid)
            shareService.warmFor(uuid)
            visibilityService.warm(uuid)
        }

        val api: WaypointsApi = WaypointsApiImpl(waypointService, shareService)
        Bukkit.getServicesManager().register(WaypointsApi::class.java, api, hostPlugin, ServicePriority.Normal)

        val tree = CommandTreeBuilder(this).build()
        module.registerCommandViaReflection(tree)

        BStatsBootstrap.start(hostPlugin, config)

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            WaypointsExpansion(waypointService, VERSION).register()
        }
    }

    fun stop() {
        runCatching { storage.close() }
    }

    fun reload() {
        loadConfigs()
        // Live-updating provider constants requires replacing the provider; caches remain valid.
        // For 2.0 we only reload lang + config values that are read on each call (teleport cooldown from TeleportService
        // is captured at construction, so a full reload requires a restart). Document this in help.
    }

    private fun loadConfigs() {
        val loader = YamlConfigLoader(module.dataFolder)
        val cfgYaml =
            loader.loadOrCopyDefault("config.yml") {
                requireNotNull(javaClass.getResourceAsStream("/config.yml")) { "config.yml missing from JAR" }
            }
        val langYaml =
            loader.loadOrCopyDefault("lang.yml") {
                requireNotNull(javaClass.getResourceAsStream("/lang.yml")) { "lang.yml missing from JAR" }
            }
        config = ConfigSchema.load(cfgYaml)
        lang = LangSchema.of(langYaml)
        messenger = Messenger(lang.message("prefix"))
    }

    private fun openBackend(cfg: ConfigSchema): StorageBackend {
        val dataFolder = module.dataFolder.toPath()
        return when (cfg.storageType) {
            StorageType.SQLITE -> SqliteStorageBackend(dataFolder.resolve(cfg.sqliteFile))
            StorageType.YAML -> YamlStorageBackend(dataFolder.resolve(cfg.yamlFile))
        }
    }

    companion object {
        const val VERSION = "2.0.0"
    }
}

/**
 * Module#registerCommand takes a PluginCommand. CommandBase.build() returns PluginCommand via reflection/protected
 * wiring in Cartographer2. This helper isolates that integration point.
 */
private fun Module.registerCommandViaReflection(cmd: io.github.bananapuncher714.cartographer.core.api.command.CommandBase) {
    val method = Module::class.java.getDeclaredMethod("registerCommand", org.bukkit.command.PluginCommand::class.java)
    method.isAccessible = true
    method.invoke(this, cmd.build())
}
