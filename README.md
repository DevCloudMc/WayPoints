# WayPoints

A waypoint addon for [Cartographer2](https://github.com/BananaPuncher714/Cartographer2). Lets players pin named locations on the minimap with a live distance readout, and lets staff publish server-wide points of interest.
---

## Why this exists

Cartographer2 ships an excellent minimap engine but no way for players to mark locations of their own. WayPoints fills that gap:

- **Players** mark homes, mines, builds, friends' bases — anywhere worth coming back to.
- **Staff** publish global points: spawn, shops, events, dungeon entrances. Visible to everyone, no chat noise.
- **Other addons** subscribe to waypoint events to extend behaviour (Discord notifications, leaderboards, claim integrations).

It is a thin addon. It does one thing — manage map pins — and gets out of the way.

---

## Features

### For players

- Create/delete/rename personal waypoints anywhere in any world.
- Pick from any vanilla minimap cursor type (`MapCursor.Type`) when you have the right permission, or use a safe default.
- Inspect a waypoint with `/wp info <name>`: world, coordinates, distance, owner.
- Teleport to your own waypoints with `/wp tp <name>` (cooldown configurable, no economy dependency).
- Share a personal waypoint with another player (read-only on their side).
- Per-player visibility scopes — hide all, hide only globals, hide only shared, etc.
- Hard limit on waypoint count, granted via permission nodes (`waypoints.limit.<N>`).

### For staff

- Global waypoints visible to every player on the server.
- Per-player admin tools: list, wipe, audit.
- Live config reload without restarting Cartographer2 or the server.
- Storage migration command: move data between SQLite and YAML without manual SQL.

### For developers

- Custom Bukkit events: `WaypointCreateEvent`, `WaypointDeleteEvent`, `WaypointTeleportEvent`, `WaypointShareEvent`. Cancellable where it makes sense.
- PlaceholderAPI placeholders: `%waypoints_count%`, `%waypoints_max%`, `%waypoints_nearest_name%`, `%waypoints_nearest_distance%`.
- bStats metrics opt-out via standard `bStats/config.yml`.

---

## Requirements

| | |
|---|---|
| Server software | Paper 1.21.1+ |
| Java | 21 |
| Cartographer2 | 2.15.16+ |
| Optional | PlaceholderAPI |

WayPoints does **not** require Vault. Limits are driven by permission nodes, and teleport cost is intentionally omitted to keep the dependency surface minimal — economies belong in dedicated economy plugins, not minimap addons.

---

## Installation

1. Install [Cartographer2](https://github.com/BananaPuncher714/Cartographer2) on your Paper server.
2. Drop `WayPoints-2.x.jar` into `plugins/Cartographer2/modules/`.
3. Restart, or run `/cartographer reload modules`.
4. Default config and language files are generated under `plugins/Cartographer2/modules/WayPoints/`.

---

## Commands

Root command: `/wp` (alias `/waypoint`).

### Personal

| Command | Description |
|---|---|
| `/wp create <name> [icon]` | Create a waypoint at your current location. |
| `/wp delete <name>` | Delete one of your waypoints. |
| `/wp rename <old> <new>` | Rename one of your waypoints. |
| `/wp list [page]` | List your waypoints (clickable). |
| `/wp info <name>` | Show details: world, coords, distance, owner. |
| `/wp tp <name>` | Teleport to your waypoint (subject to cooldown). |
| `/wp share <player> <name>` | Share read-only access with another player. |
| `/wp unshare <player> <name>` | Revoke a previously shared waypoint. |
| `/wp visibility <show\|hide> [scope]` | Scope: `personal`, `global`, `shared`, `all`. |
| `/wp help [page]` | Built-in help. |

### Global (staff)

| Command | Permission |
|---|---|
| `/wp global create <name> <icon>` | `waypoints.global` |
| `/wp global delete <name>` | `waypoints.global` |
| `/wp global rename <old> <new>` | `waypoints.global` |
| `/wp global list` | `waypoints.global` |

### Admin

| Command | Permission |
|---|---|
| `/wp admin reload` | `waypoints.admin` |
| `/wp admin migrate <from> <to>` | `waypoints.admin` |
| `/wp admin user <player> list` | `waypoints.admin` |
| `/wp admin user <player> wipe` | `waypoints.admin` |

---

## Permissions

| Node | Default | Purpose |
|---|---|---|
| `waypoints.use` | `true` | Base permission to use personal commands. |
| `waypoints.limit.<N>` | — | Sets the cap on personal waypoints (highest matched node wins). |
| `waypoints.icon.extended` | `op` | Allow choosing any `MapCursor.Type` instead of the default. |
| `waypoints.tp` | `true` | Allow teleporting to own waypoints. |
| `waypoints.tp.cooldown.bypass` | `op` | Bypass the teleport cooldown. |
| `waypoints.share` | `true` | Allow sharing waypoints with other players. |
| `waypoints.global` | `op` | Manage global waypoints. |
| `waypoints.admin` | `op` | Admin commands (`reload`, `migrate`, user tools). |

---

## Configuration

`config.yml`:

```yaml
storage:
  # sqlite | yaml
  type: sqlite

  sqlite:
    file: database.db

  yaml:
    file: waypoints.yml

display:
  # Hide the label when the player is closer than this many blocks.
  hide-label-within-blocks: 5
  # Round coordinates in the label to this many decimals.
  label-distance-decimals: 0

teleport:
  enabled: true
  cooldown-seconds: 10
  warmup-seconds: 0
  cancel-on-move: true

defaults:
  # Cursor type used when the player has no `waypoints.icon.extended` permission.
  icon: PLAYER_OFF_MAP
```

Limits are **not** in `config.yml` — they are permission nodes. Example bundle for LuckPerms:

```
group.default permission set waypoints.limit.3 true
group.vip     permission set waypoints.limit.10 true
group.staff   permission set waypoints.limit.50 true
```

Language strings live in `lang.yml` and use Adventure MiniMessage formatting.

---

## Storage backends

Two interchangeable backends, both async:

- **SQLite** — single file, indexed, suitable for any server size. Default.
- **YAML** — flat file, human-readable, good for small servers and easy backup.

Switch backends in config and run `/wp admin migrate <from> <to>` to copy data over.

---

## Developer API

Add the dependency (artifact will be published to the project's Maven repository):

```kotlin
dependencies {
    compileOnly("org.devcloud:waypoints:3.0.0")
}
```

Subscribe to events like any other Bukkit listener:

```java
@EventHandler
public void onWaypointCreate(WaypointCreateEvent event) {
    Waypoint wp = event.getWaypoint();
    Player owner = event.getPlayer();
    // event.setCancelled(true) to block creation
}
```

Available events:

- `WaypointCreateEvent` (cancellable)
- `WaypointDeleteEvent` (cancellable)
- `WaypointRenameEvent` (cancellable)
- `WaypointShareEvent` (cancellable)
- `WaypointTeleportEvent` (cancellable, fires at teleport request, before warmup)

---

## Build

```bash
./gradlew shadowJar
```

Output: `build/libs/WayPoints-<version>.jar`.

CI builds are produced by `.github/workflows/build.yml` on every push and PR. Releases are attached to GitHub releases by `.github/workflows/release.yml` after a tag matches the project version.
