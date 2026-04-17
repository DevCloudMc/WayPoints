package org.devcloud.waypoints.storage.yaml

import java.nio.file.Path
import org.devcloud.waypoints.storage.RepositoryContractTest
import org.devcloud.waypoints.storage.StorageBackend
import org.junit.jupiter.api.io.TempDir

class YamlStorageBackendTest : RepositoryContractTest() {
    @TempDir lateinit var tmp: Path

    override fun newBackend(): StorageBackend =
        YamlStorageBackend(tmp.resolve("test-${System.nanoTime()}.yml"))
}
