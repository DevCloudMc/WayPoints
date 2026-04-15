package org.devcloud.waypoints.command.personal

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.WayPointsBootstrap
import org.devcloud.waypoints.command.validator.ScopeValidator
import org.devcloud.waypoints.command.validator.ShowHideValidator
import org.devcloud.waypoints.command.validator.VisibilityScope
import org.devcloud.waypoints.domain.VisibilityState

class VisibilityCommand(private val ctx: WayPointsBootstrap) {
    fun build(): SubCommand =
        SubCommand("visibility")
            .addSenderValidator(SenderValidatorPlayer())
            .add(
                SubCommand(ShowHideValidator())
                    .add(SubCommand(ScopeValidator()).defaultTo(this::executeWithScope))
                    .defaultTo(this::executeAllScope)
            )
            .defaultTo { s, _, _ -> ctx.messenger.send(s, ctx.lang.message("usage-visibility")) }

    private fun executeAllScope(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        apply(sender as Player, p.getLast(Boolean::class.java), VisibilityScope.ALL)
    }

    private fun executeWithScope(sender: CommandSender, args: Array<out String>, p: CommandParameters) {
        val scope = p.getLast(VisibilityScope::class.java)
        val hide = p.get(Boolean::class.java, p.size() - 2)
        apply(sender as Player, hide, scope)
    }

    private fun apply(player: Player, hide: Boolean, scope: VisibilityScope) {
        val cur = ctx.visibilityService.get(player.uniqueId)
        val next =
            when (scope) {
                VisibilityScope.PERSONAL -> cur.copy(hidePersonal = hide)
                VisibilityScope.GLOBAL -> cur.copy(hideGlobal = hide)
                VisibilityScope.SHARED -> cur.copy(hideShared = hide)
                VisibilityScope.ALL -> VisibilityState(hide, hide, hide)
            }
        ctx.visibilityService.set(player.uniqueId, next).thenAccept {
            ctx.async.runOnMain {
                ctx.messenger.send(
                    player,
                    ctx.lang.message(
                        "visibility-set",
                        "scope" to scope.name.lowercase(),
                        "state" to if (hide) "hidden" else "visible",
                    ),
                )
            }
        }
    }
}
