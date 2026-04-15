package org.devcloud.waypoints.api

import org.devcloud.waypoints.domain.Waypoint
import java.util.UUID

interface WaypointsApi {
    fun listOwned(owner: UUID): List<Waypoint>

    fun listGlobal(): List<Waypoint>

    fun findOwned(owner: UUID, name: String): Waypoint?

    fun findGlobal(name: String): Waypoint?

    /** Owned + globals + shared (ids resolved to waypoints). */
    fun listAccessible(player: UUID): List<Waypoint>
}
