package org.devcloud.waypoints.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

private const val HEX_FIRST_REGEX = "<#[A-Fa-f0-9]{6}>"
private const val HEX_SECOND_REGEX = "&#[A-Fa-f0-9]{6}"
private val WHITE_RGB = TextColor.color(255, 255, 255)

object TextComponentUtil {

    fun getTextColorByHex(hex: String): TextColor {
        val rgb = hex.toInt(16)
        return TextColor.color((rgb shr 16) and 0xFF, (rgb shr 8) and 0xFF, rgb and 0xFF)
    }

    fun removeHexColor(input: String): String {
        return input.replace(Regex("$HEX_FIRST_REGEX|$HEX_SECOND_REGEX"), "")
    }

    fun splitString(input: String): Array<String> {
        val regex = "(?=<#[A-Fa-f0-9]{6}>|&#[A-Fa-f0-9]{6}|&[0-9A-Fa-f])"
        return input.split(Regex(regex)).toTypedArray()
    }

    fun parseHexColor(input: String): TextColor {
        val hexFirstPattern = Regex(HEX_FIRST_REGEX)
        val hexSecondPattern = Regex(HEX_SECOND_REGEX)

        return when {
            hexFirstPattern.containsMatchIn(input) -> {
                val hexColor = hexFirstPattern.find(input)?.value?.substring(2, 8)
                getTextColorByHex(hexColor ?: "FFFFFF")
            }

            hexSecondPattern.containsMatchIn(input) -> {
                val hexColor = hexSecondPattern.find(input)?.value?.substring(2, 8)
                getTextColorByHex(hexColor ?: "FFFFFF")
            }

            else -> WHITE_RGB
        }
    }

    fun parseColors(input: String): TextColor? {
        return if (input.startsWith("&") && input.length > 1 && input[1] != '#') {
            LegacyComponentSerializer.legacyAmpersand().deserialize(input).color()
        } else {
            parseHexColor(input)
        }
    }

    fun parseDecoration(input: String): TextDecoration {
        return TextDecoration.valueOf(input.uppercase())
    }

    fun buildMessageWithColors(originalMessage: String): TextComponent {
        return buildMessageWithColors(Component.text(originalMessage))
    }

    fun buildMessageWithColors(originalMessage: TextComponent): TextComponent {
        val strings = splitString(originalMessage.content())

        val builder = Component.text()
        strings.forEach { builder.append(buildTextWithColors(it)) }

        return builder.build()
    }

    private fun buildTextWithColors(input: String): TextComponent {
        return if (input.startsWith("&") && input.length > 1 && input[1] != '#') {
            LegacyComponentSerializer.legacyAmpersand().deserialize(input)
        } else {
            Component.text(removeHexColor(input)).color(parseHexColor(input))
        }
    }
}
