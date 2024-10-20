package org.devcloud.waypoints.database.dao

import org.devcloud.waypoints.database.Database
import org.devcloud.waypoints.database.Row

interface LocalWayPointDAO {
    val database: Database
    fun setupDatabase()

    fun createWayPoint(row: Row)

    fun updateWayPoint(player: String, name: String, row: Row)

    fun existsWayPoint(player: String, name: String): Boolean

    fun removeWayPoint(player: String, name: String)

    fun getWayPoints(player: String): List<Row>
}
