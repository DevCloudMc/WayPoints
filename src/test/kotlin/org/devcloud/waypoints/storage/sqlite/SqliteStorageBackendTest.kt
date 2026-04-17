package org.devcloud.waypoints.storage.sqlite

import java.nio.file.Path
import org.devcloud.waypoints.storage.RepositoryContractTest
import org.devcloud.waypoints.storage.StorageBackend
import org.junit.jupiter.api.io.TempDir

class SqliteStorageBackendTest : RepositoryContractTest() {
    @TempDir lateinit var tmp: Path

    override fun newBackend(): StorageBackend =
        SqliteStorageBackend(tmp.resolve("test-${System.nanoTime()}.db"))
}
