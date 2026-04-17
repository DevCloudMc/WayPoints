package org.devcloud.waypoints.domain

import java.util.*

/**
 * Opaque, stable identifier for a [Waypoint].
 *
 * Backed by a [UUID] but kept distinct from it at the type level to keep API call sites unambiguous
 * — a [WaypointId] is never a player UUID by accident.
 *
 * The id is assigned at creation time and is preserved across every operation that mutates the
 * waypoint (rename, re-save, etc.). Use it, not the name, whenever you need to refer to a specific
 * waypoint across time.
 *
 * @property value underlying [UUID].
 * @since 3.0.0
 */
@JvmInline
value class WaypointId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        /**
         * Creates a new [WaypointId] backed by a [UUID.randomUUID]. Used by the service layer when
         * a player creates a new waypoint.
         *
         * @since 3.0.0
         */
        fun random(): WaypointId = WaypointId(UUID.randomUUID())

        /**
         * Parses the output of [toString] back into a [WaypointId].
         *
         * @param s a UUID in the standard `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` form.
         * @return the parsed id.
         * @throws IllegalArgumentException if [s] is not a valid UUID string.
         * @since 3.0.0
         */
        fun parse(s: String): WaypointId = WaypointId(UUID.fromString(s))
    }
}
