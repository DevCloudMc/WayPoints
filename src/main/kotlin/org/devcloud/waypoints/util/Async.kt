package org.devcloud.waypoints.util

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class Async(private val plugin: Plugin) {
    fun runOnMain(block: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            block()
        } else {
            Bukkit.getScheduler().runTask(plugin, Runnable(block))
        }
    }
}
