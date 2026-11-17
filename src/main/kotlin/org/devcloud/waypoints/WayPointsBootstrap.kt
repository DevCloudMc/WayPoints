package org.devcloud.waypoints

import io.github.bananapuncher714.cartographer.core.Cartographer
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
import org.devcloud.waypoints.listener.TeleportWarmupListener
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

class WayPointsBootstrap(val module: WayPointsModule) {
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

    lateinit var teleportWarmups: TeleportWarmupListener
        private set

    private lateinit var api: WaypointsApi
    private var expansion: WaypointsExpansion? = null

    private val hostPlugin: JavaPlugin by lazy { Cartographer.getInstance() }

    fun start() {
        reload()
        async = Async(hostPlugin)
        storage = openBackend(config.storageType)
        storage.init().get()

        waypointService = WaypointService(storage)
        shareService = ShareService(storage)
        visibilityService = VisibilityService(storage)
        teleportService = TeleportService({ config.teleportCooldown })

        waypointService.warmGlobals().get()

        provider =
            WaypointWorldCursorProvider(
                waypointService,
                shareService,
                visibilityService,
            ) {
                config
            }

        val playerListener =
            PlayerLifecycleListener(
                waypointService,
                shareService,
                visibilityService,
                module.logger,
            )
        module.registerListener(playerListener)
        module.registerListener(MinimapLifecycleListener(provider))
        teleportWarmups =
            TeleportWarmupListener(module::later, { config.teleportCancelOnMove }) {
                messenger.send(it, lang.message("teleport-cancelled"))
            }
        module.registerListener(teleportWarmups)

        // Already-loaded minimaps won't fire MinimapLoadEvent again — register on them now.
        Cartographer.getInstance().mapManager.minimaps.values.forEach {
            it.registerProvider(provider)
        }

        // Warm profiles + personals for players that are already online (e.g. after /reload).
        for (player in Bukkit.getOnlinePlayers()) {
            playerListener.warm(player.uniqueId)
        }

        api = WaypointsApiImpl(waypointService, shareService)
        Bukkit.getServicesManager()
            .register(WaypointsApi::class.java, api, hostPlugin, ServicePriority.Normal)

        val tree = CommandTreeBuilder(this).build()
        module.register(tree.build())

        BStatsBootstrap.start(hostPlugin, config)

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            expansion =
                WaypointsExpansion(waypointService, module.version) { config.defaultLimit }
                    .also { it.register() }
        }
    }

    fun stop() {
        // Cartographer2 drops listeners and commands itself; these three it does not know about.
        runCatching {
            Cartographer.getInstance().mapManager.minimaps.values.forEach {
                it.unregisterProvider(provider)
            }
        }
        runCatching { Bukkit.getServicesManager().unregister(WaypointsApi::class.java, api) }
        runCatching { expansion?.unregister() }
        runCatching { storage.close() }
    }

    // storage.* is read once at startup; everything else is read on use.
    fun reload() {
        val loader = YamlConfigLoader(module.dataFolder)
        val cfgYaml =
            loader.loadOrCopyDefault("config.yml") {
                requireNotNull(javaClass.getResourceAsStream("/config.yml")) {
                    "config.yml missing from JAR"
                }
            }
        val langYaml =
            loader.loadOrCopyDefault("lang.yml") {
                requireNotNull(javaClass.getResourceAsStream("/lang.yml")) {
                    "lang.yml missing from JAR"
                }
            }
        config = ConfigSchema.load(cfgYaml)
        lang = LangSchema.of(langYaml)
        messenger = Messenger(lang.message("prefix"))
    }

    fun openBackend(type: StorageType): StorageBackend {
        val dataFolder = module.dataFolder.toPath()
        return when (type) {
            StorageType.SQLITE -> SqliteStorageBackend(dataFolder.resolve(config.sqliteFile))
            StorageType.YAML -> YamlStorageBackend(dataFolder.resolve(config.yamlFile))
        }
    }
}
