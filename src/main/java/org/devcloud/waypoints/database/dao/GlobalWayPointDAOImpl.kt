package org.devcloud.waypoints.database.dao

import org.devcloud.waypoints.database.Database
import org.devcloud.waypoints.database.Row

class GlobalWayPointDAOImpl(
    override val database: Database
) : GlobalWayPointDAO {
    override fun setupDatabase() {
        //language=SQLite
        val sql = """
            CREATE TABLE IF NOT EXISTS `global`
            (
                `name`  VARCHAR(16) NOT NULL,
                `type`  VARCHAR(16) NOT NULL,
                `world` VARCHAR(32) NOT NULL,
                `x`     DOUBLE      NOT NULL,
                `y`     DOUBLE      NOT NULL,
                `z`     DOUBLE      NOT NULL,
                `yaw`   FLOAT       NOT NULL,
                `pitch` FLOAT       NOT NULL
            )
        """.trimIndent()
        database.executeSQL(sql)
    }

    override fun createWayPoint(row: Row) {
        database.createRow("global", row)
    }

    override fun updateWayPoint(name: String, row: Row) {
        val conditions = mapOf("name" to name)
        database.updateRow("global", conditions, row)
    }

    override fun existsWayPoint(name: String): Boolean {
        val conditions = mapOf("name" to name)
        return database.existsRow("global", conditions)
    }

    override fun removeWayPoint(name: String) {
        val conditions = mapOf("name" to name)
        database.removeRow("global", conditions)
    }

    override fun getAllWayPoints(): List<Row> {
        return database.getAllRows("global")
    }
}