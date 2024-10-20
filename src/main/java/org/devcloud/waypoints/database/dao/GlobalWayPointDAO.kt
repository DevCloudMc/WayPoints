package org.devcloud.waypoints.database.dao

import org.devcloud.waypoints.database.Database
import org.devcloud.waypoints.database.Row

interface GlobalWayPointDAO {
    val database: Database
    fun setupDatabase()

    fun createWayPoint(row: Row)

    fun updateWayPoint(name: String, row: Row)

    fun existsWayPoint(name: String): Boolean

    fun removeWayPoint(name: String)

    fun getAllWayPoints(): List<Row>
}
