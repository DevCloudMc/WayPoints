package org.devcloud.waypoints.database.impl

import org.devcloud.waypoints.database.Database
import org.devcloud.waypoints.database.Row
import java.sql.Connection
import java.sql.DriverManager

class MySQLDatabase(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val user: String,
    private val password: String
) : Database {
    private var connection: Connection? = null

    init {
        val url = "jdbc:mysql://$host:$port/$database"
        connection = DriverManager.getConnection(url, user, password)
    }

    override fun executeSQL(sql: String) {
        connection?.createStatement()?.use { stmt ->
            stmt.execute(sql)
        }
    }

    override fun createRow(tableName: String, row: Row) {
        TODO("Not yet implemented")
    }

    override fun updateRow(tableName: String, conditions: Map<String, Any>, row: Row) {
        TODO("Not yet implemented")
    }

    override fun getRows(tableName: String, conditions: Map<String, Any>): List<Row> {
        TODO("Not yet implemented")
    }

    override fun getAllRows(tableName: String): List<Row> {
        TODO("Not yet implemented")
    }

    override fun removeRow(tableName: String, conditions: Map<String, Any>) {
        TODO("Not yet implemented")
    }

    override fun existsRow(tableName: String, conditions: Map<String, Any>): Boolean {
        TODO("Not yet implemented")
    }

    override fun closeConnection() {
        connection?.close()
    }
}
