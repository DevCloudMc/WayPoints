package org.devcloud.waypoints.domain

import java.time.Instant
import java.util.UUID
import org.bukkit.map.MapCursor

/**
 * A waypoint — a named, iconified location rendered on the Cartographer2 minimap.
 *
 * Waypoints come in two flavours distinguished by [scope]:
 * - **Personal** — owned by a specific player; only the owner (and players the owner has shared it
 *   with) can see it on their minimap. Every personal waypoint has a non-null [owner].
 * - **Global** — staff-managed points of interest visible to every player; [owner] is always
 *   `null`.
 *
 * Waypoints are value objects. Operations that "change" a waypoint return a new instance
 * (`copy(...)`) rather than mutating in place.
 *
 * ### Identity vs. naming
 *
 * Two waypoints with the same name are not the same waypoint — names are unique per `(owner,
 * scope)`, so player A's `home` and player B's `home` are independent entities with distinct [id]s.
 * When you need to compare or persist a waypoint, use [id].
 *
 * ### Invariants
 *
 * Enforced in the constructor:
 * - `scope == GLOBAL` **requires** `owner == null`
 * - `scope == PERSONAL` **requires** `owner != null`
 *
 * Violating these throws [IllegalArgumentException] at construction time.
 *
 * ### Name format
 *
 * Names are restricted to `^[A-Za-z0-9_-]{1,32}$` at the service layer; any waypoint you receive
 * through [org.devcloud.waypoints.api.WaypointsApi] already satisfies this.
 *
 * @property id stable identifier, preserved across renames.
 * @property owner UUID of the owning player, or `null` for global waypoints.
 * @property name the player-chosen name; unique per `(owner, scope)`.
 * @property icon the [MapCursor.Type] used when rendering the cursor on the minimap.
 * @property location the target location, stored by world name so it survives world unloads.
 * @property scope [WaypointScope.PERSONAL] or [WaypointScope.GLOBAL].
 * @property createdAt wall-clock time when this waypoint was first created. Not updated on rename.
 * @see WaypointScope
 * @see WaypointLocation
 * @see org.devcloud.waypoints.api.WaypointsApi
 * @since 2.0.0
 */
data class Waypoint(
    val id: WaypointId,
    val owner: UUID?,
    val name: String,
    val icon: MapCursor.Type,
    val location: WaypointLocation,
    val scope: WaypointScope,
    val createdAt: Instant,
) {
    init {
        when (scope) {
            WaypointScope.GLOBAL ->
                require(owner == null) { "Global waypoint must not have an owner" }
            WaypointScope.PERSONAL ->
                require(owner != null) { "Personal waypoint must have an owner" }
        }
    }
}
