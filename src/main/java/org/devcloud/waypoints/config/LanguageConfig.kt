package org.devcloud.waypoints.config

import io.github.bananapuncher714.cartographer.core.module.Module
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.devcloud.waypoints.util.TextComponentUtil


class LanguageConfig(private val module: Module) : YamlConfig(module, "lang", true) {
    private var messageMap: Map<String, Component> = emptyMap()

    init {
        this.update()
    }

    private fun update() {
        messageMap = yml.getKeys(false)
            .filter { yml.isString(it) }
            .associateWith { key ->
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                    yml.getString(key) ?: ""
                )
            }
    }

    fun of(key: String): TextComponent {
        val localeString: String? = yml.getString(key)
        if (localeString == null) {
            module.logger.warning("Message not found: $key")
            return Component.text(key)
        }
        return TextComponentUtil.buildMessageWithColors(Component.text(localeString))
    }

    fun of(key: String, vararg args: Any): TextComponent {
        val localeString = yml.getString(key)?.let { String.format(it, args) }
        if (localeString == null) {
            module.logger.warning("Message not found: $key")
            return Component.text(key)
        }
        return TextComponentUtil.buildMessageWithColors(Component.text(localeString))
    }


    fun ofList(key: String): TextComponent {
        val arrayTextComponent = Component.text()
        val stringList = yml.getStringList(key)
        stringList.forEachIndexed { index, item ->
            if (index != 0 && index < stringList.size) {
                arrayTextComponent.append(Component.newline())
            }
            arrayTextComponent.append(
                TextComponentUtil.buildMessageWithColors(Component.text(item))
            )
        }
        return arrayTextComponent.build()
    }

    fun getReplaceConfig(placeholder: String, value: String): TextReplacementConfig {
        return getReplaceConfig(placeholder, Component.text(value))
    }

    fun getReplacePointNameConfig(pointName: String): TextReplacementConfig {
        return getReplacePointNameConfig(TextComponentUtil.buildMessageWithColors(pointName))
    }

    fun getReplacePointNameConfig(pointNameComponent: Component): TextReplacementConfig {
        return getReplaceConfig("{pointName}", pointNameComponent)
    }

    fun getReplaceCursorTypeConfig(cursorType: String): TextReplacementConfig {
        return getReplaceCursorTypeConfig(TextComponentUtil.buildMessageWithColors(cursorType))
    }

    fun getReplaceCursorTypeConfig(cursorTypeComponent: Component): TextReplacementConfig {
        return getReplaceConfig("{cursorType}", cursorTypeComponent)
    }

    fun getReplaceConfig(placeholder: String, value: Component): TextReplacementConfig {
        return TextReplacementConfig.builder()
            .matchLiteral(placeholder)
            .replacement(value)
            .build()
    }


}
