package org.devcloud.waypoints.manager

import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.devcloud.waypoints.database.Database
import org.devcloud.waypoints.database.Row
import org.devcloud.waypoints.database.dao.GlobalWayPointDAO
import org.devcloud.waypoints.database.dao.GlobalWayPointDAOImpl
import org.devcloud.waypoints.database.dao.LocalWayPointDAO
import org.devcloud.waypoints.database.dao.LocalWayPointDAOImpl
import org.devcloud.waypoints.util.LocationUtil


object WayPointsManager {
    private lateinit var database: Database
    private lateinit var localWayPointDAO: LocalWayPointDAO
    private lateinit var globalWayPointDAO: GlobalWayPointDAO
    private lateinit var personalPointManager: PersonalPointManager
    private lateinit var globalPointManager: GlobalPointManager
    private lateinit var permissionProvider: Permission
    private lateinit var userManager: UserManager

    fun initialize(
        database: Database,
        personalPointManager: PersonalPointManager,
        globalPointManager: GlobalPointManager,
        permissionProvider: Permission,
        userManager: UserManager
    ) {
        WayPointsManager.database = database
        WayPointsManager.personalPointManager = personalPointManager
        WayPointsManager.globalPointManager = globalPointManager
        localWayPointDAO = LocalWayPointDAOImpl(database)
        globalWayPointDAO = GlobalWayPointDAOImpl(database)
        WayPointsManager.permissionProvider = permissionProvider
        WayPointsManager.userManager = userManager
    }

    fun setupDatabase() {
        localWayPointDAO.setupDatabase()
        globalWayPointDAO.setupDatabase()
    }

    fun createWayPoint(user: User, name: String, type: String) {
        val location = user.player.location
        val row = Row()
        row.addField("player", user.player.name)
        row.addField("name", name)
        row.addField("type", type)
        row.addField("world", location.world.name)
        row.addField("x", LocationUtil.round(location.x))
        row.addField("y", LocationUtil.round(location.y))
        row.addField("z", LocationUtil.round(location.z))
        row.addField("yaw", LocationUtil.round(location.yaw))
        row.addField("pitch", LocationUtil.round(location.pitch))

        val namespacedKey = NamespacedKey.minecraft(type)
        val cursorType = Registry.MAP_DECORATION_TYPE[namespacedKey]

        if (!localWayPointDAO.existsWayPoint(user.player.name, name)) {
            localWayPointDAO.createWayPoint(row)
            personalPointManager.create(user, name, cursorType!!, location)
            user.wayPoints[name] = WayPoint(name, cursorType!!, location)
        } else {
            localWayPointDAO.updateWayPoint(user.player.name, name, row)
            user.wayPoints.remove(name)
            user.wayPoints[name] = WayPoint(name, cursorType!!, location)
        }
    }

    fun createGlobalWayPoint(user: User, name: String, type: String) {
        val location = user.player.location
        val row = Row()
        row.addField("name", name)
        row.addField("type", type)
        row.addField("world", location.world.name)
        row.addField("x", LocationUtil.round(location.x))
        row.addField("y", LocationUtil.round(location.y))
        row.addField("z", LocationUtil.round(location.z))
        row.addField("yaw", LocationUtil.round(location.yaw))
        row.addField("pitch", LocationUtil.round(location.pitch))

        val namespacedKey = NamespacedKey.minecraft(type)
        val cursorType = Registry.MAP_DECORATION_TYPE[namespacedKey]

        if (!globalWayPointDAO.existsWayPoint(name)) {
            globalWayPointDAO.createWayPoint(row)
            globalPointManager.create(name, cursorType!!, location)
        } else {
            globalWayPointDAO.updateWayPoint(name, row)
            globalPointManager.remove(name)
            globalPointManager.create(name, cursorType!!, location)
        }
    }

    fun deleteWayPoint(user: User, name: String): WayPoint? {
        localWayPointDAO.removeWayPoint(user.player.name, name)
        return user.wayPoints.remove(name)
    }

    fun deleteGlobalWayPoint(name: String) {
        globalWayPointDAO.removeWayPoint(name)
        globalPointManager.remove(name)
    }

    fun reloadWayPointsForAll() {
        for (player in Bukkit.getOnlinePlayers()) {
            val user = userManager.getOrCreate(player.uniqueId)
            user.wayPoints.clear()
            loadWayPoints(user)
            user.maxPoint = userManager.getMaxWayPoints(player.uniqueId)
        }
    }

    fun loadWayPoints(user: User) {
        val wayPoints = localWayPointDAO.getWayPoints(user.player.name)
        for (row in wayPoints) {
            val name = row.getField("name") as String
            val type = row.getField("type") as String
            val worldName = row.getField("world") as String
            val world = Bukkit.getWorld(worldName)
            val x = (row.getField("x") as Number).toDouble()
            val y = (row.getField("y") as Number).toDouble()
            val z = (row.getField("z") as Number).toDouble()
            val yaw = (row.getField("yaw") as Number).toFloat()
            val pitch = (row.getField("pitch") as Number).toFloat()
            val location = Location(world, x, y, z, yaw, pitch)
            try {
                val namespacedKey = NamespacedKey.minecraft(type.lowercase())
                val cursorType = Registry.MAP_DECORATION_TYPE[namespacedKey]
                user.wayPoints[name] = WayPoint(name, cursorType!!, location)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadGlobalWayPoints() {
        val wayPoints = globalWayPointDAO.getAllWayPoints()
        for (row in wayPoints) {
            val name = row.getField("name") as String
            val type = row.getField("type") as String
            val worldName = row.getField("world") as String
            val world = Bukkit.getWorld(worldName)
            val x = (row.getField("x") as Number).toDouble()
            val y = (row.getField("y") as Number).toDouble()
            val z = (row.getField("z") as Number).toDouble()
            val yaw = (row.getField("yaw") as Number).toFloat()
            val pitch = (row.getField("pitch") as Number).toFloat()
            val location = Location(world, x, y, z, yaw, pitch)

            val namespacedKey = NamespacedKey.minecraft(type.lowercase())
            val cursorType = Registry.MAP_DECORATION_TYPE[namespacedKey]

            globalPointManager.create(name, cursorType!!, location)
        }
    }
}
