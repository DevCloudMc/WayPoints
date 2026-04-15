package org.devcloud.waypoints.util

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class Async(private val plugin: Plugin) {
    val mainExecutor: Executor = Executor { task ->
        if (Bukkit.isPrimaryThread()) task.run() else Bukkit.getScheduler().runTask(plugin, task)
    }

    val asyncExecutor: Executor = Executor { task ->
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task)
    }

    fun <T> async(block: () -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync(block, asyncExecutor)

    fun runOnMain(block: () -> Unit) {
        mainExecutor.execute(block)
    }

    fun <T> CompletableFuture<T>.thenOnMain(block: (T) -> Unit): CompletableFuture<Void> =
        thenAcceptAsync(block, mainExecutor)
}
