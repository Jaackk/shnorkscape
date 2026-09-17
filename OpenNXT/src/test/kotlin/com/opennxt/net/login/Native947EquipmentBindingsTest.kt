package com.opennxt.net.login

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Native947EquipmentBindingsTest {
    private fun inspectedFiles(): Map<String, ByteArray> {
        val text = requireNotNull(javaClass.getResource("/native947/inspected-equipment-bindings.json")).readText()
        return JsonParser.parseString(text).asJsonObject.entrySet().associate { (path, value) ->
            path to value.asString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        }
    }

    @Test
    fun `every inspected equipment script and appearance prerequisite is checked`() {
        val files = inspectedFiles()
        val reads = mutableSetOf<String>()
        val reader = { index: Int, group: Int, file: Int ->
            val path = "$index/$group/$file"
            reads += path
            files.getValue(path)
        }
        Native947CacheContent.verifyEquipmentBindings(reader)
        Native947CacheContent.appearance(reader)
        assertEquals(files.keys, reads)
    }

    @Test
    fun `changed equipment scripts or appearance defaults fail before enabling equipment`() {
        for (changed in inspectedFiles().keys) {
            val files = inspectedFiles()
            files.getValue(changed)[0] = (files.getValue(changed)[0].toInt() xor 1).toByte()
            assertFailsWith<IllegalStateException>(changed) {
                val reader = { index: Int, group: Int, file: Int -> files.getValue("$index/$group/$file") }
                Native947CacheContent.verifyEquipmentBindings(reader)
                Native947CacheContent.appearance(reader)
            }
        }
    }
}
