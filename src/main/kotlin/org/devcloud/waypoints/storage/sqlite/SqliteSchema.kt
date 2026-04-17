package org.devcloud.waypoints.storage.sqlite

import java.sql.Connection

object SqliteSchema {
    fun apply(conn: Connection) {
        conn.createStatement().use { st ->
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS waypoints (
                    id TEXT PRIMARY KEY,
                    owner TEXT,
                    name TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x REAL NOT NULL, y REAL NOT NULL, z REAL NOT NULL,
                    yaw REAL NOT NULL, pitch REAL NOT NULL,
                    scope TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    UNIQUE(owner, scope, name)
                )
                """
                    .trimIndent()
            )
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_waypoints_owner ON waypoints(owner)")
            st.executeUpdate(
                "CREATE INDEX IF NOT EXISTS idx_waypoints_scope_name ON waypoints(scope, name)"
            )
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS waypoint_shares (
                    waypoint_id TEXT NOT NULL,
                    shared_with TEXT NOT NULL,
                    shared_at INTEGER NOT NULL,
                    PRIMARY KEY (waypoint_id, shared_with),
                    FOREIGN KEY (waypoint_id) REFERENCES waypoints(id) ON DELETE CASCADE
                )
                """
                    .trimIndent()
            )
            st.executeUpdate(
                "CREATE INDEX IF NOT EXISTS idx_shares_target ON waypoint_shares(shared_with)"
            )
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS player_profiles (
                    uuid TEXT PRIMARY KEY,
                    hide_personal INTEGER NOT NULL DEFAULT 0,
                    hide_global INTEGER NOT NULL DEFAULT 0,
                    hide_shared INTEGER NOT NULL DEFAULT 0
                )
                """
                    .trimIndent()
            )
        }
    }
}
