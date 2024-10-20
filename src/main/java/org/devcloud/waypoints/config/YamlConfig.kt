package org.devcloud.waypoints.config

import io.github.bananapuncher714.cartographer.core.module.Module
import io.github.bananapuncher714.cartographer.core.util.FileUtil
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

open class YamlConfig(module: Module, name: String, isResource: Boolean) {
    val yml: YamlConfiguration

    init {
        val file = File(module.dataFolder, "/$name.yml")
        file.parentFile.mkdirs()
        if (!file.exists()) {
            if (isResource) {
                FileUtil.saveToFile(module.getResource("$name.yml"), file, false)
            } else {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        yml = YamlConfiguration.loadConfiguration(file)
    }

    val fileConfiguration: FileConfiguration
        get() = yml
}
