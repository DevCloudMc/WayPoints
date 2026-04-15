package org.devcloud.waypoints.domain

import org.bukkit.Bukkit
import org.bukkit.Location

/**
 * A [org.bukkit.Location] stored by world **name** rather than by reference.
 *
 * Bukkit [Location] holds a live [org.bukkit.World] reference, which becomes invalid when the world
 * is unloaded or recreated. Persisting that reference would either leak the old world or force the
 * persistence layer to serialise it. Keeping the world as a string lets a waypoint survive a world
 * unload / reload / rename cycle and re-resolve itself when the world is available again.
 *
 * ### Resolving back to a live [Location]
 *
 * ```kotlin
 * val wp: Waypoint = api.findGlobal("spawn") ?: return
 * val loc = wp.location.resolve()
 *     ?: run { sender.sendMessage("World ${wp.location.worldName} is not loaded"); return }
 * player.teleport(loc)
 * ```
 *
 * [resolve] returns `null` — it does **not** throw — when the world is missing, so callers can
 * render a friendly error.
 *
 * @property worldName exact name of the [org.bukkit.World] this waypoint lives in.
 * @property x world X coordinate (block-space, fractional).
 * @property y world Y coordinate.
 * @property z world Z coordinate.
 * @property yaw facing yaw (0°–360°, unchecked).
 * @property pitch facing pitch (-90°–90°, unchecked).
 * @since 3.0.0
 */
data class WaypointLocation(
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
) {
    /**
     * Resolves this stored position into a live Bukkit [Location].
     *
     * @return a fresh [Location] pointing at the stored coordinates in the current
     *   [org.bukkit.World] instance of [worldName], or `null` when that world is not loaded.
     * @since 3.0.0
     */
    fun resolve(): Location? {
        val world = Bukkit.getWorld(worldName) ?: return null
        return Location(world, x, y, z, yaw, pitch)
    }

    companion object {
        /**
         * Convenience factory that reads world name and coordinates off a live [Location].
         *
         * @param loc a non-null Bukkit location with a non-null [Location.world].
         * @return a fresh [WaypointLocation] mirroring [loc].
         * @throws IllegalArgumentException if [Location.world] is `null`.
         * @since 3.0.0
         */
        fun of(loc: Location): WaypointLocation =
            WaypointLocation(
                worldName = requireNotNull(loc.world) { "Location has no world" }.name,
                x = loc.x,
                y = loc.y,
                z = loc.z,
                yaw = loc.yaw,
                pitch = loc.pitch,
            )
    }
}
