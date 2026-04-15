# Module WayPoints

Public API for the WayPoints Cartographer2 addon.

Addons and plugins that integrate with WayPoints consume two things:

- [org.devcloud.waypoints.api.WaypointsApi] — a read-only facade registered through
  [org.bukkit.plugin.ServicesManager].
- The cancellable Bukkit events in [org.devcloud.waypoints.api.event] — the hook
  to block, modify or extend every state change WayPoints performs.

Everything under `service`, `storage`, `command`, `listener`, `provider`, `config`,
`messaging`, `integration` and `util` is implementation detail. Consumers should
not depend on those packages; they can and will change between minor releases.

# Package org.devcloud.waypoints.api

Public façade. Start with [org.devcloud.waypoints.api.WaypointsApi].

# Package org.devcloud.waypoints.api.event

Cancellable Bukkit events. See [org.devcloud.waypoints.api.event.WaypointEvent] for
the shared contract.

# Package org.devcloud.waypoints.domain

Value types surfaced through [org.devcloud.waypoints.api.WaypointsApi]. All immutable,
all safe to pass across threads.
