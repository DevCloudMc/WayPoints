package org.devcloud.waypoints.domain

import java.util.UUID

@JvmInline
value class WaypointId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun random(): WaypointId = WaypointId(UUID.randomUUID())
        fun parse(s: String): WaypointId = WaypointId(UUID.fromString(s))
    }
}
