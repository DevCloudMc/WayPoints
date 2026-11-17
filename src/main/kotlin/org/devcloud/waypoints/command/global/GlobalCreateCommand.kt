package org.devcloud.waypoints.command.global

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

class GlobalCreateCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("create")
            .addSenderValidator(SenderValidatorPlayer())
            .addSenderValidator(SenderValidatorPermission("waypoints.global"))
            .add(
                SubCommand(WaypointNameValidator())
                    .add(SubCommand(IconValidator()).defaultTo(this::execute))
            )
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-global-create")) }

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val player = sender as Player
        val icon = p.getLast(MapCursor.Type::class.java)
        val name = p.get(String::class.java, p.size() - 2)
        val loc = WaypointLocation.of(player.location)
        val result =
            ctx.waypointService.createGlobal(name, icon, loc) {
                !CommandSupport.callCancellable(WaypointCreateEvent(player, it))
            }
        CommandSupport.reply(ctx, player, result) {
            ctx.lang.message("global-created", "name" to name)
        }
    }
}
