package org.devcloud.waypoints.database

import java.sql.SQLException

interface Database {

    @Throws(SQLException::class)
    fun executeSQL(sql: String)

    fun createRow(tableName: String, row: Row)

    fun updateRow(tableName: String, conditions: Map<String, Any>, row: Row)

    fun getRows(tableName: String, conditions: Map<String, Any>): List<Row>

    fun getAllRows(tableName: String): List<Row>

    fun removeRow(tableName: String, conditions: Map<String, Any>)

    fun existsRow(tableName: String, conditions: Map<String, Any>): Boolean

    fun closeConnection()
}
