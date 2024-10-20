package org.devcloud.waypoints.config

import io.github.bananapuncher714.cartographer.core.module.Module
import org.devcloud.waypoints.database.DatabaseType
import org.devcloud.waypoints.database.toDatabaseType

class MainConfig(module: Module) : YamlConfig(module, "config", true) {
    val autoReloadMiniMap: Boolean
    val groups: Map<String, Int>
    val databaseType: DatabaseType

    var sqliteConfig: SQLiteConfig? = null
    var mysqlConfig: MySQLConfig? = null
    var postgresqlConfig: PostgreSQLConfig? = null

    init {
        autoReloadMiniMap = yml.getBoolean("autoReloadMiniMap", true)

        groups = yml.getConfigurationSection("groups")
            ?.getValues(false)
            ?.mapValues {
                it.value.toString().toIntOrNull() ?: 0
            } ?: emptyMap()

        val section = yml.getConfigurationSection("database")
            ?: throw IllegalArgumentException("Database configuration is missing")

        databaseType = yml.getString("database.type", "sqlite")!!.toDatabaseType()


        when (databaseType) {
            DatabaseType.SQLITE -> {
                val databaseFile = yml.getString("database.sqlite.database", "database.db")
                sqliteConfig = SQLiteConfig(databaseFile!!)
            }

            DatabaseType.MYSQL -> {
                module.logger.warning("MySQL is not supported yet. Use SQLite instead.")
                //todo implement MySQL
//                mysqlConfig = MySQLConfig(
//                    host = section.getString("host", "localhost")!!,
//                    port = section.getInt("port", 3306),
//                    database = section.getString("database", "my_database")!!,
//                    user = section.getString("user", "root")!!,
//                    password = section.getString("password", "")!!
//                )
            }

            DatabaseType.POSTGRESQL -> {
                module.logger.warning("PostgreSQ: is not supported yet. Use SQLite instead.")
                //todo implement PostgreSQL
//                postgresqlConfig = PostgreSQLConfig(
//                    host = section.getString("host", "localhost")!!,
//                    port = section.getInt("port", 5432),
//                    database = section.getString("database", "my_database")!!,
//                    user = section.getString("user", "postgres")!!,
//                    password = section.getString("password", "")!!,
//                    schema = section.getString("schema", "public")!!
//                )
            }
        }
    }
}