package org.devcloud.waypoints.storage.sqlite

import org.devcloud.waypoints.storage.RepositoryContractTest
import org.devcloud.waypoints.storage.StorageBackend
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SqliteStorageBackendTest : RepositoryContractTest() {
    @TempDir
    lateinit var tmp: Path

    override fun newBackend(): StorageBackend = SqliteStorageBackend(tmp.resolve("test-${System.nanoTime()}.db"))
}
