package org.devcloud.waypoints

import io.github.bananapuncher714.cartographer.core.map.Minimap
import io.github.bananapuncher714.cartographer.core.module.Module
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit.getServer
import org.bukkit.plugin.RegisteredServiceProvider
import org.devcloud.waypoints.command.CommandFactory
import org.devcloud.waypoints.command.Commands
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.MainConfig
import org.devcloud.waypoints.database.Database
import org.devcloud.waypoints.database.DatabaseFactory
import org.devcloud.waypoints.exception.DependencyException
import org.devcloud.waypoints.listener.Listener
import org.devcloud.waypoints.manager.GlobalPointManager
import org.devcloud.waypoints.manager.PersonalPointManager
import org.devcloud.waypoints.manager.UserManager
import org.devcloud.waypoints.manager.WayPointsManager
import org.devcloud.waypoints.provider.WayPointsProvider


class Main : Module() {
    private lateinit var config: MainConfig
    private lateinit var database: Database
    private lateinit var permissionProvider: Permission
    private lateinit var userManager: UserManager
    private lateinit var wayPointsProvider: WayPointsProvider
    private lateinit var languageConfig: LanguageConfig
    private lateinit var globalPointManager: GlobalPointManager
    private lateinit var personalPointManager: PersonalPointManager

    override fun onEnable() {
        logger.info("Enabling waypoints module...")

        initDependencies()
        initConfigs()
        initManagers()
        initDatabase()
        WayPointsManager.loadGlobalWayPoints()
        registerProviders()
        registerListeners()
        registerCommands()

        WayPointsManager.reloadWayPointsForAll()

        config.autoReloadMiniMap.let {
            cartographer.mapManager.minimaps.forEach { (_: String?, minimap: Minimap?) ->
                require(minimap != null) { "'$minimap' does not exist!" }
                val saveDir = minimap.dataFolder

                cartographer.mapManager.unload(minimap)
                cartographer.mapManager.load(saveDir)
                logger.info("§eReloaded minimap '§f${minimap.id}§e'")
            }
        }

        logger.info("Waypoints module enabled")
    }

    override fun onDisable() {
        database.closeConnection()
    }

    private fun initDependencies() {
        try {
            setupPermissions()
        } catch (e: DependencyException) {
            logger.severe("Failed to initialize ${e.dependencyName}. Error: ${e.message}. Disable module")
            cartographer.moduleManager.disableModule(this)
        }
    }

    private fun setupPermissions() {
        val registeredServiceProvider: RegisteredServiceProvider<Permission> =
            getServer().servicesManager.getRegistration(Permission::class.java)
                ?: throw DependencyException(
                    message = "No permission provider found. Please install a permission plugin",
                    dependencyName = "Vault"
                )
        permissionProvider = registeredServiceProvider.provider
    }

    private fun initConfigs() {
        config = MainConfig(this)
        languageConfig = LanguageConfig(this)
    }

    private fun initManagers() {
        userManager = UserManager(permissionProvider, config)
    }

    private fun initDatabase() {
        try {
            database = DatabaseFactory.createDatabase(config, this)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create database", e)
        }

        personalPointManager = PersonalPointManager()
        globalPointManager = GlobalPointManager()

        WayPointsManager.initialize(database, personalPointManager, globalPointManager, permissionProvider, userManager)

        WayPointsManager.setupDatabase()
    }

    private fun registerProviders() {
        wayPointsProvider = WayPointsProvider(userManager, languageConfig, globalPointManager)
    }

    private fun registerListeners() {
        registerListener(
            Listener(
                userManager,
                config,
                permissionProvider,
                wayPointsProvider
            )
        )
    }

    private fun registerCommands() {
        val commandFactory = CommandFactory(userManager, globalPointManager, languageConfig)
        val commands = Commands(commandFactory).initCommandBase()

        registerCommand(commands.build())
    }
}