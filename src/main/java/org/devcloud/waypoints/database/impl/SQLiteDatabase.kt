package org.devcloud.waypoints.database.impl

import org.devcloud.waypoints.database.Database
import org.devcloud.waypoints.database.Row
import java.sql.*

class SQLiteDatabase(
    dbPath: String,
) : Database {
    private var connection: Connection? = null

    init {
        val url = "jdbc:sqlite:$dbPath.db"
        connection = DriverManager.getConnection(url)
    }

    @Throws(SQLException::class)
    override fun executeSQL(sql: String) {
        connection?.createStatement()?.use { stmt ->
            stmt.execute(sql)
        }
    }

    override fun createRow(tableName: String, row: Row) {
        val columns = row.fields.keys.joinToString(", ")
        val placeholders = row.fields.keys.joinToString(", ") { "?" }
        val sql = "INSERT INTO `$tableName` ($columns) VALUES ($placeholders)"
        connection?.prepareStatement(sql)?.use { stmt ->
            setStatementParameters(stmt, row.fields.values.toList())
            stmt.executeUpdate()
        }
    }

    override fun updateRow(tableName: String, conditions: Map<String, Any>, row: Row) {
        val setClause = row.fields.keys.joinToString(", ") { "$it = ?" }
        val whereClause = conditions.keys.joinToString(" AND ") { "$it = ?" }
        val sql = "UPDATE `$tableName` SET $setClause WHERE $whereClause"
        connection?.prepareStatement(sql)?.use { stmt ->
            val params = row.fields.values + conditions.values
            setStatementParameters(stmt, params.toList())
            stmt.executeUpdate()
        }
    }

    override fun getRows(tableName: String, conditions: Map<String, Any>): List<Row> {
        val whereClause = conditions.keys.joinToString(" AND ") { "$it = ?" }
        val sql = "SELECT * FROM `$tableName` WHERE $whereClause"
        connection?.prepareStatement(sql)?.use { stmt ->
            setStatementParameters(stmt, conditions.values.toList())
            val resultSet = stmt.executeQuery()
            return resultSetToRows(resultSet)
        }
        return emptyList()
    }

    override fun getAllRows(tableName: String): List<Row> {
        val sql = "SELECT * FROM `$tableName`"
        connection?.createStatement()?.use { stmt ->
            val resultSet = stmt.executeQuery(sql)
            return resultSetToRows(resultSet)
        }
        return emptyList()
    }

    override fun removeRow(tableName: String, conditions: Map<String, Any>) {
        val whereClause = conditions.keys.joinToString(" AND ") { "$it = ?" }
        val sql = "DELETE FROM `$tableName` WHERE $whereClause"
        connection?.prepareStatement(sql)?.use { stmt ->
            setStatementParameters(stmt, conditions.values.toList())
            stmt.executeUpdate()
        }
    }

    override fun existsRow(tableName: String, conditions: Map<String, Any>): Boolean {
        val whereClause = conditions.keys.joinToString(" AND ") { "$it = ?" }
        val sql = "SELECT 1 FROM `$tableName` WHERE $whereClause LIMIT 1"
        connection?.prepareStatement(sql)?.use { stmt ->
            setStatementParameters(stmt, conditions.values.toList())
            val resultSet = stmt.executeQuery()
            return resultSet.next()
        }
        return false
    }

    override fun closeConnection() {
        connection?.close()
    }

    private fun setStatementParameters(stmt: PreparedStatement, params: List<Any?>) {
        params.forEachIndexed { index, param ->
            stmt.setObject(index + 1, param)
        }
    }

    private fun resultSetToRows(resultSet: ResultSet): List<Row> {
        val rows = mutableListOf<Row>()
        val metaData = resultSet.metaData
        while (resultSet.next()) {
            val row = Row()
            for (i in 1..metaData.columnCount) {
                val columnName = metaData.getColumnName(i)
                val value = resultSet.getObject(i)
                row.addField(columnName, value)
            }
            rows.add(row)
        }
        return rows
    }
}
