package org.devcloud.waypoints.command

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.devcloud.waypoints.config.LanguageConfig
import org.devcloud.waypoints.config.LanguagePath
import org.devcloud.waypoints.manager.UserManager
import org.devcloud.waypoints.util.PermissionList

class HelpCommand(
    private val userManager: UserManager,
    private val languageConfig: LanguageConfig
) : CommandExecutable {

    override fun execute(sender: CommandSender, args: Array<out String>, parameters: CommandParameters) {
        val user = userManager.getOrCreate((sender as Player).uniqueId)
        var helpList = languageConfig.ofList(LanguagePath.HELP)

        if (user.player.hasPermission(PermissionList.ADMIN)) {
            helpList = helpList
                .appendNewline()
                .append(languageConfig.ofList(LanguagePath.ADMIN_HELP)) as TextComponent
        }

        user.player.sendMessage(helpList)
    }
}