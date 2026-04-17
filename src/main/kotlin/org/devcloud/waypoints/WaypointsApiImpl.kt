package org.devcloud.waypoints

import java.util.*
import org.devcloud.waypoints.api.WaypointsApi
import org.devcloud.waypoints.domain.Waypoint
import org.devcloud.waypoints.service.ShareService
import org.devcloud.waypoints.service.WaypointService

internal class WaypointsApiImpl(
    private val waypointService: WaypointService,
    private val shareService: ShareService,
) : WaypointsApi {
    override fun listOwned(owner: UUID): List<Waypoint> = waypointService.listOwned(owner)

    override fun listGlobal(): List<Waypoint> = waypointService.listGlobals()

    override fun findOwned(owner: UUID, name: String): Waypoint? =
        waypointService.findOwned(owner, name)

    override fun findGlobal(name: String): Waypoint? = waypointService.findGlobal(name)

    override fun listAccessible(player: UUID): List<Waypoint> {
        val out = mutableListOf<Waypoint>()
        out += waypointService.listOwned(player)
        out += waypointService.listGlobals()
        for (id in shareService.listFor(player)) {
            waypointService.findById(id)?.let(out::add)
        }
        return out
    }
}
