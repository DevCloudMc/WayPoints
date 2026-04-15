package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.map.MapCursor
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointCreateEvent
import org.devcloud.waypoints.command.CommandSupport
import org.devcloud.waypoints.command.validator.IconValidator
import org.devcloud.waypoints.command.validator.WaypointNameValidator
import org.devcloud.waypoints.domain.WaypointLocation
import org.devcloud.waypoints.util.Outcome
import org.devcloud.waypoints.util.PermissionLimit

class CreateCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("create")
            .addSenderValidator(SenderValidatorPlayer())
            .add(
                SubCommand(WaypointNameValidator())
                    .add(
                        SubCommand(IconValidator())
                            .addSenderValidator(
                                SenderValidatorPermission("waypoints.icon.extended")
                            )
                            .defaultTo(this::executeWithIcon)
                    )
                    .defaultTo(this::executeDefaultIcon)
            )
            .defaultTo(this::executeMissing)

    private fun executeMissing(
        sender: CommandSender,
        args: Array<out String>,
        p: CommandParameters,
    ) {
        ctx.messenger.send(sender, ctx.lang.message("usage-create"))
    }

    private fun executeDefaultIcon(
        sender: CommandSender,
        args: Array<out String>,
        p: CommandParameters,
    ) {
        val player = sender as Player
        val name = p.getLast(String::class.java)
        runCreate(player, name, ctx.config.defaultIcon)
    }

    private fun executeWithIcon(
        sender: CommandSender,
        args: Array<out String>,
        p: CommandParameters,
    ) {
        val player = sender as Player
        val icon = p.getLast(MapCursor.Type::class.java)
        val name = p.get(String::class.java, p.size() - 2)
        runCreate(player, name, icon)
    }

    private fun runCreate(player: Player, name: String, icon: MapCursor.Type) {
        val limit = PermissionLimit.compute(player, "waypoints.limit")
        if (limit <= 0) {
            ctx.messenger.send(player, ctx.lang.message("limit-reached", "max" to "0"))
            return
        }
        val loc = WaypointLocation.of(player.location)
        ctx.waypointService.createPersonal(player.uniqueId, name, icon, loc, limit).thenAccept { res
            ->
            ctx.async.runOnMain {
                when (res) {
                    is Outcome.Ok -> {
                        if (
                            !CommandSupport.callCancellable(WaypointCreateEvent(player, res.value))
                        ) {
                            ctx.messenger.send(
                                player,
                                ctx.lang.message("waypoint-created", "name" to res.value.name),
                            )
                        } else {
                            // cancelled — roll back
                            ctx.waypointService.deletePersonal(player.uniqueId, res.value.name)
                        }
                    }
                    is Outcome.Err -> {
                        ctx.messenger.send(player, CommandSupport.renderError(ctx, res.error))
                    }
                }
            }
        }
    }
}
