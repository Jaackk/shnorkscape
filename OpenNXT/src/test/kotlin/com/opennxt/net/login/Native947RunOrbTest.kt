package com.opennxt.net.login

import com.google.gson.JsonParser
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Native947RunOrbTest {
    private fun files(): Map<String, ByteArray> = JsonParser.parseString(
        requireNotNull(javaClass.getResource("/native947/inspected-run-orb-menu-files.json")).readText()
    ).asJsonObject.entrySet().associate { (path, data) ->
        path to data.asString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    @Test
    fun `menu helper pins the inspected orb and every direct script prerequisite`() {
        val inspected = files()
        val seen = mutableSetOf<String>()
        Native947RunOrb.verify { index, group, file ->
            val key = "$index/$group/$file"
            seen += key
            inspected.getValue(key)
        }
        assertEquals(inspected.keys, seen)
        for (changed in inspected.keys) {
            assertFailsWith<IllegalStateException>("Changed $changed must block login") {
                Native947RunOrb.verify { index, group, file ->
                    val key = "$index/$group/$file"
                    inspected.getValue(key).clone().also {
                        if (key == changed) it[1] = (it[1].toInt() xor 1).toByte()
                    }
                }
            }
        }
    }

    @Test
    fun `inspected helper clears only operations two through four without changing hooks`() {
        // This walks the real cache bytecode, including its switch and footer.
        // A generic blank-label helper (10370) is unsuitable: it removes onOp.
        val bytes = files().getValue("12/3916/0")
        val input = ByteBuffer.wrap(bytes)
        assertEquals(0, input.get().toInt()) // unnamed script
        instruction(input, 0x035e, 0) // local int 0: selector
        instruction(input, 0x051a, 0) // table 0
        instruction(input, 0x0713, 12) // all other selectors skip to return
        for (option in 2..4) {
            assertEquals(0x0511, input.short.toInt())
            assertEquals(0, input.get().toInt())
            assertEquals(option, input.int)
            assertEquals(0x0511, input.short.toInt())
            assertEquals(2, input.get().toInt()) // string literal
            assertEquals(0, input.get().toInt()) // empty string
            instruction(input, 0x035e, 1) // target component, unchanged
            assertEquals(0x0113, input.short.toInt()) // IF_SETOP
            assertEquals(0, input.get().toInt())
        }
        assertEquals(0x0495, input.short.toInt()) // return; no hook setters or calls
        assertEquals(0, input.get().toInt())
        assertEquals(16, input.int) // instruction count
        assertEquals(listOf(2, 0, 0, 2, 0, 0), List(6) { input.short.toInt() })
        assertEquals(1, input.get().toInt()) // switch table count
        assertEquals(3, input.short.toInt())
        assertEquals(mapOf(0 to 1, 4 to 1, 1 to 1), List(3) { input.int to input.int }.toMap())
        assertEquals(27, input.short.toInt())
        assertEquals(0, input.remaining())
    }

    @Test
    fun `wire selects the clear branch and only the static run orb`() {
        val packet = Native947RunOrb.initialization()
        assertEquals(121, packet.type().opcode())
        assertEquals("69690005b9000e0000000000000f4c", packet.payload().joinToString("") {
            "%02x".format(it.toInt() and 255)
        })
    }

    private fun instruction(input: ByteBuffer, opcode: Int, operand: Int) {
        assertEquals(opcode, input.short.toInt())
        assertEquals(operand, input.int)
    }
}
