package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.api.event.WaypointRenameEvent
import org.devcloud.waypoints.command.CommandSupport
import org.devcloud.waypoints.command.validator.OwnedWaypointValidator
import org.devcloud.waypoints.command.validator.WaypointNameValidator
import org.devcloud.waypoints.util.Outcome

class RenameCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("rename")
            .addSenderValidator(SenderValidatorPlayer())
            .add(
                SubCommand(OwnedWaypointValidator(ctx.waypointService))
                    .add(SubCommand(WaypointNameValidator()).defaultTo(this::execute))
            )
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-rename")) }

    private fun execute(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val player = sender as Player
        val new = p.getLast(String::class.java)
        val old = p.get(String::class.java, p.size() - 2)
        val wp =
            ctx.waypointService.findOwned(player.uniqueId, old)
                ?: run {
                    ctx.messenger.send(
                        player,
                        ctx.lang.message("waypoint-not-found", "name" to old),
                    )
                    return
                }
        if (CommandSupport.callCancellable(WaypointRenameEvent(player, wp, new))) return
        ctx.waypointService.renamePersonal(player.uniqueId, old, new).thenAccept { res ->
            ctx.async.runOnMain {
                when (res) {
                    is Outcome.Ok ->
                        ctx.messenger.send(
                            player,
                            ctx.lang.message("waypoint-renamed", "old" to old, "new" to new),
                        )
                    is Outcome.Err ->
                        ctx.messenger.send(player, CommandSupport.renderError(ctx, res.error))
                }
            }
        }
    }
}
