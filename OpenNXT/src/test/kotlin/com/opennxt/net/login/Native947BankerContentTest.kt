package com.opennxt.net.login

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Native947BankerContentTest {
    private fun inspectedFiles(): Map<String, ByteArray> {
        val text = requireNotNull(javaClass.getResource("/native947/inspected-banker-definitions.json")).readText()
        return JsonParser.parseString(text).asJsonObject.entrySet().associate { (path, value) ->
            path to value.asString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        }
    }

    @Test
    fun `banker bindings retain the inspected modern operation gap`() {
        val files = inspectedFiles()
        val reads = mutableSetOf<String>()
        val banker = Native947CacheContent.banker { index, group, file ->
            val path = "$index/$group/$file"
            reads += path
            files.getValue(path)
        }
        assertEquals(files.keys, reads)
        assertEquals(494, banker.definitionId)
        assertEquals("Banker", banker.name)
        assertEquals(1, banker.size)
        assertEquals(1, banker.bankOption)
        assertEquals(3, banker.talkOption, "The modern definition leaves option 2 empty")
        assertEquals(4, banker.collectOption)
    }

    @Test
    fun `changed npc or animation metadata cannot enable banker content`() {
        for (changedFile in inspectedFiles().keys) {
            val files = inspectedFiles()
            files.getValue(changedFile)[0] = 0
            assertFailsWith<IllegalStateException>(changedFile) {
                Native947CacheContent.banker { index, group, file -> files.getValue("$index/$group/$file") }
            }
        }
    }
}
