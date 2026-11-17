package org.devcloud.waypoints.command.admin

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import org.bukkit.command.CommandSender
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.command.validator.EnumValidator
import org.devcloud.waypoints.storage.StorageType

class MigrateCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("migrate")
            .addSenderValidator(SenderValidatorPermission("waypoints.admin"))
            .add(
                SubCommand(EnumValidator(StorageType.entries))
                    .add(SubCommand(EnumValidator(StorageType.entries)).defaultTo(this::execute))
            )
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-admin-migrate")) }

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val to = p.getLast(StorageType::class.java)
        val from = p.get(StorageType::class.java, p.size() - 2)
        if (from == to) {
            ctx.messenger.send(sender, ctx.lang.message("admin-migrate-same"))
            return
        }
        // Reading or writing the live files through a second backend loses data, so the source
        // is always the running backend and the target is never the active type.
        val source = ctx.storage
        if (from != source.type) {
            ctx.messenger.send(sender, ctx.lang.message("admin-migrate-not-active"))
            return
        }
        val target = ctx.openBackend(to)

        source
            .exportAll()
            .thenCompose { snapshot ->
                target.init().thenCompose { target.importAll(snapshot) }.thenApply { snapshot }
            }
            .whenComplete { snapshot, err ->
                ctx.async.runOnMain {
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
