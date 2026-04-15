package org.devcloud.waypoints.command.admin

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.storage.StorageBackend
import org.devcloud.waypoints.storage.StorageType
import org.devcloud.waypoints.storage.sqlite.SqliteStorageBackend
import org.devcloud.waypoints.storage.yaml.YamlStorageBackend

private class StorageTypeValidator : InputValidator<StorageType> {
    override fun isValid(
        sender: CommandSender,
        input: Array<out String>,
        args: Array<out String>,
    ): Boolean {
        val v = input.firstOrNull()?.uppercase() ?: return false
        return runCatching { StorageType.valueOf(v) }.isSuccess
    }

    override fun get(sender: CommandSender, args: Array<out String>): StorageType =
        StorageType.valueOf(args.first().uppercase())

    override fun getTabCompletes(
        sender: CommandSender,
        args: Array<out String>,
    ): Collection<String> = StorageType.values().map { it.name.lowercase() }
}

class MigrateCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("migrate")
            .addSenderValidator(SenderValidatorPermission("waypoints.admin"))
            .add(
                SubCommand(StorageTypeValidator())
                    .add(SubCommand(StorageTypeValidator()).defaultTo(this::execute))
            )
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-admin-migrate")) }

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val to = p.getLast(StorageType::class.java)
        val from = p.get(StorageType::class.java, p.size() - 2)
        if (from == to) {
            ctx.messenger.send(sender, ctx.lang.message("admin-migrate-same"))
            return
        }

        val dataFolder = ctx.module.dataFolder.toPath()
        val source: StorageBackend =
            when (from) {
                StorageType.SQLITE ->
                    SqliteStorageBackend(dataFolder.resolve(ctx.config.sqliteFile))

                StorageType.YAML -> YamlStorageBackend(dataFolder.resolve(ctx.config.yamlFile))
            }
        val target: StorageBackend =
            when (to) {
                StorageType.SQLITE ->
                    SqliteStorageBackend(dataFolder.resolve(ctx.config.sqliteFile))

                StorageType.YAML -> YamlStorageBackend(dataFolder.resolve(ctx.config.yamlFile))
            }

        source
            .init()
            .thenCompose { source.exportAll() }
            .thenCompose { snapshot ->
                target.init().thenCompose { target.importAll(snapshot) }.thenApply { snapshot }
            }
            .whenComplete { snapshot, err ->
                ctx.async.runOnMain {
                    runCatching { source.close() }
                    runCatching { target.close() }
                    if (err != null) {
                        ctx.messenger.send(
                            sender,
                            ctx.lang.message(
                                "admin-migrate-failed",
                                "reason" to (err.message ?: err.toString()),
                            ),
                        )
                    } else {
                        ctx.messenger.send(
                            sender,
                            ctx.lang.message(
                                "admin-migrate-ok",
                                "count" to snapshot.waypoints.size.toString(),
                                "from" to from.name.lowercase(),
                                "to" to to.name.lowercase(),
                            ),
                        )
                    }
                }
            }
    }
}
