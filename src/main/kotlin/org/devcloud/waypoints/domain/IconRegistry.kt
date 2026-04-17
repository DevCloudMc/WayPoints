package org.devcloud.waypoints.domain

import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.domain.IconRegistry.parse
import org.devcloud.waypoints.domain.IconRegistry.serialize

/**
 * Lookup and serialisation helpers for [MapCursor.Type] that avoid the deprecated, enum-era surface
 * of that interface.
 *
 * Since Paper 1.21 [MapCursor.Type] is registry-backed. Its legacy enum-style helpers — `values()`,
 * `valueOf(String)`, `name()` — are all marked `@Deprecated(forRemoval = true, inVersion = "1.22")`
 * in favour of going through [Registry.MAP_DECORATION_TYPE] and [org.bukkit.Keyed.getKey]. This
 * object is the single place in WayPoints that reads that registry; everywhere else parses and
 * serialises through [parse] and [serialize].
 *
 * ### Example — parse user input from a command argument
 *
 * ```kotlin
 * val iconName = args[0]
 * val icon = IconRegistry.parse(iconName)
 *     ?: run {
 *         sender.sendMessage("Unknown icon '$iconName'. Try: ${IconRegistry.allNames().take(8)}")
 *         return
 *     }
 * ```
 *
 * @since 3.0.0
 */
object IconRegistry {
    /**
     * Default cursor used when no explicit icon is configured and the player lacks the
     * `waypoints.icon.extended` permission. Resolved via [Registry.MAP_DECORATION_TYPE] so it works
     * identically on every Minecraft version that keeps `minecraft:player_off_map`.
     *
     * @since 3.0.0
     */
    val SAFE_DEFAULT: MapCursor.Type =
        requireNotNull(Registry.MAP_DECORATION_TYPE[NamespacedKey.minecraft("player_off_map")]) {
            "Expected minecraft:player_off_map cursor type in the registry"
        }

    private val byKey: Map<String, MapCursor.Type> =
        Registry.MAP_DECORATION_TYPE.associateBy { it.key.key.lowercase() }

    /**
     * Parses an icon name into a [MapCursor.Type], case-insensitively.
     *
     * Accepts both plain keys (`"red_x"`) and namespaced forms (`"minecraft:red_x"`). The legacy
     * enum-style upper-snake form (`"RED_X"`) also works — it is simply lowercased and matched
     * against the registry's key component.
     *
     * @param input the name to parse.
     * @return the matching [MapCursor.Type], or `null` if no such cursor exists on this server.
     * @since 3.0.0
     */
    fun parse(input: String): MapCursor.Type? {
        val trimmed = input.lowercase().removePrefix("minecraft:")
        return byKey[trimmed]
    }

    /**
     * Canonical string form used for persistence: the key component of the cursor's [NamespacedKey]
     * (e.g. `"red_x"`).
     *
     * @param type the cursor type to serialise.
     * @return the serialised name; round-trips through [parse].
     * @since 3.0.0
     */
    fun serialize(type: MapCursor.Type): String = type.key.key

    /**
     * Returns every valid icon name in lowercase, sorted alphabetically. Handy for tab-completion
     * and help text.
     *
     * @return an immutable, sorted list of icon names.
     * @since 3.0.0
     */
    fun allNames(): List<String> = byKey.keys.sorted()
}
