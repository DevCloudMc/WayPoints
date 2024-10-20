package org.devcloud.waypoints.database.dao

import org.devcloud.waypoints.database.Database
import org.devcloud.waypoints.database.Row

class LocalWayPointDAOImpl(
    override val database: Database
) : LocalWayPointDAO {
    override fun setupDatabase() {
        //language=SQLite
        val sql = """
            CREATE TABLE IF NOT EXISTS `local`
            (
                `player` VARCHAR(16) NOT NULL,
                `name`   VARCHAR(16) NOT NULL,
                `type`   VARCHAR(16) NOT NULL,
                `world`  VARCHAR(32) NOT NULL,
                `x`      DOUBLE      NOT NULL,
                `y`      DOUBLE      NOT NULL,
                `z`      DOUBLE      NOT NULL,
                `yaw`    FLOAT       NOT NULL,
                `pitch`  FLOAT       NOT NULL
            )
        """.trimIndent()
        database.executeSQL(sql)
    }

    override fun createWayPoint(row: Row) {
        database.createRow("local", row)
    }

    override fun updateWayPoint(player: String, name: String, row: Row) {
        val conditions = mapOf("player" to player, "name" to name)
        database.updateRow("local", conditions, row)
    }

    override fun existsWayPoint(player: String, name: String): Boolean {
        val conditions = mapOf("player" to player, "name" to name)
        return database.existsRow("local", conditions)
    }

    override fun removeWayPoint(player: String, name: String) {
        val conditions = mapOf("player" to player, "name" to name)
        database.removeRow("local", conditions)
    }

    override fun getWayPoints(player: String): List<Row> {
        val conditions = mapOf("player" to player)
        return database.getRows("local", conditions)
    }
}
