package org.devcloud.waypoints.database

enum class DatabaseType {
    SQLITE, MYSQL, POSTGRESQL;

    companion object {
        fun fromString(type: String): DatabaseType {
            return when (type.lowercase()) {
                "sqlite" -> SQLITE
                "mysql" -> MYSQL
                "postgresql" -> POSTGRESQL
                else -> throw IllegalArgumentException("Unknown database type: $type")
            }
        }
    }
}


fun String.toDatabaseType(): DatabaseType {
    return DatabaseType.fromString(this)
}