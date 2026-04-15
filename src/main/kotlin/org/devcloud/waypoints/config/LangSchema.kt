package org.devcloud.waypoints.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.file.YamlConfiguration

class LangSchema
private constructor(private val cfg: YamlConfiguration, private val mm: MiniMessage) {
    fun message(key: String, vararg args: Pair<String, String>): Component {
        val raw = cfg.getString(key) ?: return Component.text("missing-lang-key:$key")
        val resolvers = args.map { (k, v) -> Placeholder.parsed(k, v) }.toTypedArray<TagResolver>()
        return mm.deserialize(raw, *resolvers)
    }

    companion object {
        fun of(cfg: YamlConfiguration): LangSchema = LangSchema(cfg, MiniMessage.miniMessage())
    }
}
