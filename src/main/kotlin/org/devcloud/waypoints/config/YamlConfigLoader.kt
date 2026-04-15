package org.devcloud.waypoints.config

import java.io.File
import java.io.InputStream
import org.bukkit.configuration.file.YamlConfiguration

class YamlConfigLoader(private val dataFolder: File) {
    fun loadOrCopyDefault(name: String, defaults: () -> InputStream): YamlConfiguration {
        val file = File(dataFolder, name)
        if (!file.exists()) {
            dataFolder.mkdirs()
            defaults().use { input -> file.outputStream().use { out -> input.copyTo(out) } }
        }
        return YamlConfiguration.loadConfiguration(file)
    }
}
