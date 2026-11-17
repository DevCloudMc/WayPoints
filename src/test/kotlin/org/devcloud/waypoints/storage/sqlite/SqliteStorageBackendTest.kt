package org.devcloud.waypoints.storage.sqlite

import java.nio.file.Path
import java.util.concurrent.ExecutionException
import kotlin.test.assertFailsWith
import org.devcloud.waypoints.storage.RepositoryContractTest
import org.devcloud.waypoints.storage.StorageBackend
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class SqliteStorageBackendTest : RepositoryContractTest() {
    @TempDir lateinit var tmp: Path

    override fun newBackend(name: String): StorageBackend =
        SqliteStorageBackend(tmp.resolve("$name.db"))

    @Test
    fun `two global waypoints with the same name are rejected`() {
        backend.waypoints.save(global("spawn")).get()
        assertFailsWith<ExecutionException> { backend.waypoints.save(global("spawn")).get() }
    }
}
