package org.devcloud.waypoints.config

import kotlin.test.assertEquals
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Test

class LangSchemaTest {
    @Test
    fun `placeholder values are literal text, not markup`() {
        val cfg = YamlConfiguration().apply { set("k", "<green>Hi <name></green>") }
        val msg = LangSchema.of(cfg).message("k", "name" to "<red>bob</red>")
        assertEquals("Hi <red>bob</red>", PlainTextComponentSerializer.plainText().serialize(msg))
    }
}
