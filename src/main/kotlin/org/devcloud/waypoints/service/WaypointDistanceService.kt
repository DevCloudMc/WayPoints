package org.devcloud.waypoints.service

import org.bukkit.Location
import org.devcloud.waypoints.domain.WaypointLocation
import kotlin.math.hypot
import kotlin.math.roundToLong

object WaypointDistanceService {
    /** Horizontal distance between two locations in the same world; returns null if worlds differ. */
    fun horizontal(playerLoc: Location, wp: WaypointLocation): Double? {
        if (playerLoc.world?.name != wp.worldName) return null
        return hypot(playerLoc.x - wp.x, playerLoc.z - wp.z)
    }

    fun formatLabel(name: String, distanceBlocks: Double, decimals: Int): String {
        val rounded =
            if (decimals == 0) distanceBlocks.roundToLong().toString()
            else "%.${decimals}f".format(distanceBlocks)
        return "$name $rounded m"
    }
}
