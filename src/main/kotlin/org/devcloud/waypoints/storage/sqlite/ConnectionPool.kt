package org.devcloud.waypoints.storage.sqlite

import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class ConnectionPool(private val dbPath: Path) : AutoCloseable {
    private val executor =
        Executors.newSingleThreadExecutor { r ->
            Thread(r, "WayPoints-SQLite").apply { isDaemon = true }
        }

    private val connection: Connection by lazy {
        Class.forName("org.sqlite.JDBC")
        val url = "jdbc:sqlite:${dbPath.toAbsolutePath()}"
        DriverManager.getConnection(url).apply {
            createStatement().use { st ->
                st.execute("PRAGMA foreign_keys = ON")
                st.execute("PRAGMA journal_mode = WAL")
            }
            autoCommit = true
        }
    }

    fun <T> submit(block: (Connection) -> T): CompletableFuture<T> {
        val cf = CompletableFuture<T>()
        executor.submit {
            try {
                cf.complete(block(connection))
            } catch (e: Throwable) {
                cf.completeExceptionally(e)
            }
        }
        return cf
    }

    override fun close() {
        executor.submit { runCatching { connection.close() } }
        executor.shutdown()
    }
}
