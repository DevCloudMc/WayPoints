package org.devcloud.waypoints.api

import java.util.UUID
import org.devcloud.waypoints.domain.Waypoint

/**
 * Read-only facade exposing the WayPoints 2.x state to downstream addons and plugins.
 *
 * The implementation is registered with [org.bukkit.plugin.ServicesManager] during
 * [org.bukkit.event.server.PluginEnableEvent] handling for the Cartographer2 plugin, which means
 * **WayPoints must be loaded before you query it**. Resolve the service lazily (for example inside
 * your own `onEnable`) rather than from a static initialiser.
 *
 * ### Getting an instance
 *
 * Java:
 * ```java
 * RegisteredServiceProvider<WaypointsApi> rsp =
 *     Bukkit.getServicesManager().getRegistration(WaypointsApi.class);
 * if (rsp == null) throw new IllegalStateException("WayPoints not loaded");
 * WaypointsApi api = rsp.getProvider();
 * ```
 *
 * Kotlin:
 * ```kotlin
 * val api: WaypointsApi = Bukkit.getServicesManager()
 *     .load(WaypointsApi::class.java)
 *     ?: error("WayPoints not loaded")
 * ```
 *
 * ### Thread-safety
 *
 * All methods are safe to call from the main server thread, and from any task scheduled through
 * [org.bukkit.scheduler.BukkitScheduler]. The returned lists are defensive copies; iterating them
 * does not block the render pipeline.
 *
 * Do **not** call these methods from a hot loop inside
 * [io.github.bananapuncher714.cartographer.core.api.map.MapProvider] subclasses — they allocate.
 * WayPoints' own provider keeps derived data in its cursor cache to avoid that.
 *
 * ### Write access
 *
 * There is no public write path. Addons that want to react to creation/deletion should listen to
 * the events under [org.devcloud.waypoints.api.event] and cancel or extend behaviour from there.
 * Cross-addon writes would break the invariant that every write goes through the
 * [org.devcloud.waypoints.service.WaypointService] cache; if you need that, open an issue.
 *
 * @see org.devcloud.waypoints.api.event.WaypointEvent
 * @see org.devcloud.waypoints.domain.Waypoint
 * @since 2.0.0
 */
interface WaypointsApi {
    /**
     * Returns a snapshot of all personal waypoints owned by [owner], in no guaranteed order.
     *
     * If the player is offline, the returned list is empty — WayPoints only keeps personal
     * waypoints in the in-memory cache while their owner is online. Admin consumers that need
     * offline data should read the storage backend directly or ask the player to log in. This
     * method is **O(n)** in the number of cached waypoints and does not hit storage.
     *
     * @param owner UUID of the waypoint owner (usually a player).
     * @return an immutable snapshot; safe to iterate concurrently with further writes.
     * @since 2.0.0
     */
    fun listOwned(owner: UUID): List<Waypoint>

    /**
     * Returns a snapshot of every globally visible waypoint, in no guaranteed order.
     *
     * Global waypoints are warmed on module enable and kept in memory for the server's lifetime.
     * This method is **O(n)** and does not hit storage.
     *
     * @return an immutable snapshot.
     * @since 2.0.0
     */
    fun listGlobal(): List<Waypoint>

    /**
     * Looks up a personal waypoint by its (case-sensitive) [name] under [owner], or `null` when no
     * such waypoint exists or the owner is offline.
     *
     * @param owner UUID of the waypoint owner.
     * @param name the literal name the player used when creating the waypoint.
     * @return the matching waypoint, or `null` if missing.
     * @see Waypoint.name
     * @since 2.0.0
     */
    fun findOwned(owner: UUID, name: String): Waypoint?

    /**
     * Looks up a global waypoint by its (case-sensitive) [name], or `null` when no such waypoint
     * exists.
     *
     * @param name the literal name a staff member gave the global waypoint.
     * @return the matching waypoint, or `null` if missing.
     * @since 2.0.0
     */
    fun findGlobal(name: String): Waypoint?

    /**
     * Returns every waypoint [player] can currently see: their own personal waypoints, all global
     * waypoints, and waypoints shared with them by other players. The list is a union of
     * [listOwned], [listGlobal] and the caller's accessible shares — each waypoint appears at most
     * once.
     *
     * This is the same view the Cartographer2 minimap uses when rendering cursors for the player
     * (subject to [org.devcloud.waypoints.domain.VisibilityState]).
     *
     * ### Example — print every waypoint a player can see
     *
     * ```java
     * UUID playerId = player.getUniqueId();
     * for (Waypoint wp : api.listAccessible(playerId)) {
     *     player.sendMessage(wp.getName() + " @ " + wp.getLocation().getWorldName());
     * }
     * ```
     *
     * @param player UUID of the viewing player. Offline players see only global waypoints.
     * @return an immutable snapshot ordered personal → global → shared.
     * @since 2.0.0
     */
    fun listAccessible(player: UUID): List<Waypoint>
}
