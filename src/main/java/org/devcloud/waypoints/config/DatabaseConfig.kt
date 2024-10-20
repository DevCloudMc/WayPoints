package org.devcloud.waypoints.config

data class DatabaseConfig(
    val type: String,
    val sqlite: SQLiteConfig? = null,
    val mysql: MySQLConfig? = null,
    val postgresql: PostgreSQLConfig? = null
)

data class SQLiteConfig(val databaseFile: String)

data class MySQLConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String
)

data class PostgreSQLConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String,
    val schema: String
)
