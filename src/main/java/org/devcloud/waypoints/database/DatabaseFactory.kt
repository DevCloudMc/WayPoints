package org.devcloud.waypoints.database

import org.devcloud.waypoints.Main
import org.devcloud.waypoints.config.MainConfig
import org.devcloud.waypoints.database.impl.SQLiteDatabase
import java.io.File

object DatabaseFactory {
    fun createDatabase(config: MainConfig, module: Main): Database {
        return when (config.databaseType) {
            DatabaseType.SQLITE -> {
                val sqliteConfig = config.sqliteConfig
                    ?: throw IllegalArgumentException("SQLite configuration is missing")
                SQLiteDatabase(module.dataFolder.path + File.separator + sqliteConfig.databaseFile)
            }

            else -> throw IllegalArgumentException("Unsupported database type")
        }
    }
}