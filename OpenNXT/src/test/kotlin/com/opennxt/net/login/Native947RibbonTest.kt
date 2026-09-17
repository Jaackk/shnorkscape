package com.opennxt.net.login

import com.google.gson.JsonParser
import com.rs.network.protocol.modern947.Native947Packets
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Native947RibbonTest {
    private fun files(name: String): Map<String, ByteArray> = JsonParser.parseString(
        requireNotNull(javaClass.getResource("/native947/inspected-$name-files.json")).readText()
    ).asJsonObject.entrySet().associate { (path, value) ->
        path to value.asString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    @Test
    fun `modern mode pins accept only inspected server variable definitions and tutorial checks`() {
        val inspected = files("modern-mode")
        val seen = mutableSetOf<String>()
        Native947CacheContent.verifyModernModeBindings { index, group, file ->
            val path = "$index/$group/$file"
            seen += path
            requireNotNull(inspected[path])
        }
        assertEquals(inspected.keys, seen)
        assertEquals(Varbit(0, 3680, 20, 20), bitDefinition(inspected.getValue("2/69/27168")))
        assertEquals(Varbit(0, 3680, 21, 21), bitDefinition(inspected.getValue("2/69/27169")))
        assertEquals(Varbit(0, 3814, 5, 11), bitDefinition(inspected.getValue("2/69/22875")))
        assertEquals(Varbit(0, 7825, 0, 6), bitDefinition(inspected.getValue("2/69/39917")))
        assertEquals(Varbit(0, 9775, 0, 6), bitDefinition(inspected.getValue("2/69/49044")))
        assertEquals(Varbit(0, 1264, 21, 21), bitDefinition(inspected.getValue("2/69/60098")))
        for (changed in inspected.keys) {
            assertFailsWith<IllegalStateException>("Changed $changed must block login") {
                Native947CacheContent.verifyModernModeBindings { index, group, file ->
                    val path = "$index/$group/$file"
                    inspected.getValue(path).clone().also {
                        if (path == changed) it[2] = (it[2].toInt() xor 1).toByte()
                    }
                }
            }
        }
    }

    @Test
    fun `ribbon pins accept inspected components variables and scripts and reject drift`() {
        val inspected = files("ribbon")
        val seen = mutableSetOf<String>()
        Native947Ribbon.verify { index, group, file ->
            val path = "$index/$group/$file"
            seen += path
            requireNotNull(inspected[path])
        }
        // The actual icon builder and click dispatcher must be included, not just their callers.
        assertTrue("12/13845/0" in seen)
        assertTrue("12/10405/0" in seen)
        assertTrue("12/5588/0" in seen)
        assertTrue("12/8361/0" in seen)
        assertTrue("12/8362/0" in seen)
        assertTrue("12/8363/0" in seen)
        assertTrue("12/13268/0" in seen, "Native position argument order is part of the binding")
        assertTrue("17/52/7" in seen, "Settings actor comes from enum13319")
        assertTrue("17/52/9" in seen, "Management button identity comes from enum13321")
        assertEquals(inspected.keys, seen)
        for (changed in seen) {
            assertFailsWith<IllegalStateException>("Changed $changed must block the ribbon") {
                Native947Ribbon.verify { index, group, file ->
                    val path = "$index/$group/$file"
                    inspected.getValue(path).clone().also {
                        if (path == changed) it[it.lastIndex] = (it.last().toInt() xor 1).toByte()
                    }
                }
            }
        }
    }

    @Test
    fun `ribbon selection uses client varbit packets and an explicit zero terminator`() {
        val packets = Native947Ribbon.initialization()
        assertEquals(listOf(
            "115 ff559c", "115 fd559d", "115 fc559e", "115 ed559f",
            "55 008a00002055", "115 0055a1", "115 ff55b8", "115 ffa401"
        ), packets.take(8).map { "${it.type().opcode()} ${hex(it.payload())}" })
        assertEquals(listOf(21788 to 1, 21789 to 3, 21790 to 4, 21791 to 19, 21792 to 138,
            21793 to 0, 21816 to 1, 42113 to 1), packets.take(8).map(::clientVarbit))
        assertTrue(packets.drop(8).all { it.type().opcode() in listOf(121, 103, 35) })
    }

    @Test
    fun `a previous longer ribbon cannot leak buttons beyond the four panels and settings`() {
        val definitions = files("ribbon")
        // Simulate persistent words left by a previously configured client.
        val words = mutableMapOf(4109 to 0x7f7f7f7f, 4110 to 0x7f7f7f7f,
            4116 to 0x7f7f7f7e, 6504 to 0x7f7f7f7e)
        for (packet in Native947Ribbon.initialization().take(8)) {
            val (id, value) = clientVarbit(packet)
            val definition = bitDefinition(definitions.getValue("2/69/$id"))
            assertEquals(2, definition.domain, "Ribbon settings must not write server variables")
            val mask = (1L shl (definition.high - definition.low + 1)) - 1
            val previous = words[definition.parent] ?: 0
            words[definition.parent] = ((previous.toLong() and (mask shl definition.low).inv()) or
                (value.toLong() shl definition.low)).toInt()
        }
        val selected = (21788..21793).map { id ->
            val definition = bitDefinition(definitions.getValue("2/69/$id"))
            (words.getValue(definition.parent) ushr definition.low) and 255
        }.takeWhile { it != 0 }.map { it - 1 }
        assertEquals(listOf(0, 2, 3, 18, 137), selected)
        assertEquals(0x7f7f008a, words[4110], "Only Settings and its terminator replace the first two bytes")
        assertEquals(Varbit(2, 4116, 0, 0), bitDefinition(definitions.getValue("2/69/21816")))
        assertEquals(0x7f7f7f7f, words[4116], "The native checkbox preference matches the displayed custom mode")
        assertEquals(0x7f7f7f7f, words[6504], "The custom-list bit preserves the other client bits")
    }

    @Test
    fun `bottom right ribbon is sized positioned and saved before its dynamic icon tree is rebuilt`() {
        val packets = Native947Ribbon.initialization()
        assertEquals(67, packets.size)
        val wrapper = (1477 shl 16) or 61
        assertEquals(listOf(
            11145 to listOf(224, 48, 0, 0, wrapper),
            13268 to listOf(8, 8, 2, 2, wrapper),
            2330 to listOf(wrapper),
            8707 to listOf(1002),
            8708 to listOf(1002, 8)
        ), afterSelection(packets).take(5).map(::script))
        assertEquals(13833 to listOf(1431 shl 16, (1431 shl 16) or 12), script(packets[packets.lastIndex - 1]))
    }

    @Test
    fun `login clears live tab chains and saves standalone links before creating ribbon buttons`() {
        // Eight selection writes, five ribbon layout steps, then sixteen inactive-panel preparations.
        val scripts = afterSelection(Native947Ribbon.initialization()).drop(5 + 16).dropLast(2).map(::script)
        // Clear every live bar first: hiding a wrapper alone leaves actors in the other bar.
        // Inactive chat slots must also lose their reciprocal links, while remaining hidden.
        val slots = listOf(0, 2, 3, 18, 9, 19, 20, 21, 22, 23, 25, 46)
        assertEquals(slots.map { 8361 to listOf(it) }, scripts.take(12))
        assertEquals(slots.flatMap { listOf(8707 to listOf(it), 8708 to listOf(it, 8)) }, scripts.drop(12))
        assertEquals(36, scripts.size)
        // The recorded cache scripts, rather than hard-coded zero-sized rectangles or a
        // server toggle, own this operation. Their argument counts must remain compatible.
        for ((id, expectedArguments) in listOf(8361 to 1, 8362 to 3, 8363 to 1)) {
            val bytes = files("ribbon").getValue("12/$id/0")
            val switchBytes = ((bytes[bytes.lastIndex - 1].toInt() and 255) shl 8) or
                (bytes.last().toInt() and 255)
            val footer = bytes.size - switchBytes - 18
            assertEquals(expectedArguments, ByteBuffer.wrap(bytes).getShort(footer + 10).toInt(),
                "Inspected script $id integer arguments")
        }
    }

    @Test
    fun `inactive panel saves cannot match the cache uninitialized layout sentinel`() {
        val preparation = afterSelection(Native947Ribbon.initialization()).drop(5).take(16)
        val wrappers = listOf(407, 429, 439, 449, 459, 469, 479, 489)
        assertEquals(wrappers, preparation.take(8).map { packet ->
            assertEquals(103, packet.type().opcode())
            val bytes = packet.payload()
            assertEquals(5, bytes.size)
            assertEquals(129, bytes[0].toInt() and 255, "Inactive wrappers stay hidden before sizing")
            assertEquals(1477, ((bytes[4].toInt() and 255) shl 8) or (bytes[3].toInt() and 255))
            ((bytes[2].toInt() and 255) shl 8) or (bytes[1].toInt() and 255)
        })
        val sizes = preparation.drop(8).map(::script)
        assertEquals(wrappers.map { 11145 to listOf(100, 135, 0, 0, (1477 shl 16) or it) }, sizes)
        // 8701 instructions10610..10649: an empty hidden rectangle with both
        // neighbors -1 is "unset", so it recursively restores preset1's tab links.
        fun usesDefaultLayout(x: Int, y: Int, width: Int, height: Int, visible: Int,
                              previous: Int, next: Int): Boolean =
            x + y + width + height + visible == 0 && previous == -1 && next == -1
        assertTrue(usesDefaultLayout(0, 0, 0, 0, 0, -1, -1), "The old zero-size save restores default links")
        for ((_, arguments) in sizes) {
            assertFalse(usesDefaultLayout(0, 0, arguments[0], arguments[1], 0, -1, -1),
                "The saved hidden rectangle must remain distinct from an uninitialized slot")
        }
        // These are the inspected shared minimum dimensions, not a new visible HUD layout.
        assertEquals("f90300000dd50000006400000dd60000008700000dd70000500000",
            hex(files("ribbon").getValue("22/666/8")))
    }

    @Test
    fun `ribbon can be disabled without changing other UI policy`() {
        val key = "ataraxia947.ribbon"
        val previous = System.getProperty(key)
        try {
            System.clearProperty(key)
            assertTrue(Native947Ribbon.enabled())
            System.setProperty(key, "false")
            assertFalse(Native947Ribbon.enabled())
            System.setProperty(key, "true")
            assertTrue(Native947Ribbon.enabled())
        } finally {
            if (previous == null) System.clearProperty(key) else System.setProperty(key, previous)
        }
    }

    @Test
    fun `settings shortcut uses its catalog actor and native management identity`() {
        val inspected = files("ribbon")
        val actors = enumInts(inspected.getValue("17/52/7"))
        val management = enumInts(inspected.getValue("17/52/9"))
        assertEquals(137, actors[7])
        assertEquals(9, management[137])
        assertEquals(listOf(0, 2, 3, 18), listOf(8, 9, 10, 33).map(actors::getValue))
        // Native IF_SETEVENTS: little-endian first slot, big-endian last slot,
        // little-endian component hash, then mask bytes [16,24,0,8]. Only actor 7/op 1 is enabled.
        val events = Native947Ribbon.initialization().filter { it.type().opcode() == 35 }
        assertEquals(listOf("070000070000970500000200"), events.map { hex(it.payload()) })
        assertEquals(35, Native947Ribbon.initialization().last().type().opcode(),
            "The dynamic actor event override follows the native icon rebuild")
        // Only the ribbon gets a position change; implemented panels retain their accepted geometry.
        val positionChanges = Native947Ribbon.initialization().filter { it.type().opcode() == 121 }
            .map(::script).filter { it.first == 13268 }
        assertEquals(listOf(13268 to listOf(8, 8, 2, 2, (1477 shl 16) or 61)), positionChanges)
    }

    private fun afterSelection(packets: List<Native947Packets.Packet>) =
        packets.dropWhile { it.type().opcode() in listOf(115, 55) }

    /** The inspected enums use compact integer types and either dense-short or full-int keys. */
    private fun enumInts(bytes: ByteArray): Map<Int, Int> {
        val input = ByteBuffer.wrap(bytes)
        val values = mutableMapOf<Int, Int>()
        while (true) {
            when (val tag = input.get().toInt() and 255) {
                0 -> { assertFalse(input.hasRemaining()); return values }
                101, 102 -> assertEquals(0, input.get().toInt(), "Integer enum type")
                4 -> input.int
                6 -> repeat(input.short.toInt() and 65535) { values[input.int] = input.int }
                8 -> {
                    input.short // Allocated range; the following count owns the actual entries.
                    repeat(input.short.toInt() and 65535) { values[input.short.toInt() and 65535] = input.int }
                }
                else -> error("Unexpected inspected enum tag $tag")
            }
        }
    }

    private fun clientVarbit(packet: Native947Packets.Packet): Pair<Int, Int> {
        val bytes = packet.payload()
        fun byte(index: Int) = bytes[index].toInt() and 255
        return when (packet.type().opcode()) {
            115 -> {
                assertEquals(3, bytes.size)
                val id = (byte(1) shl 8) or ((byte(2) - 128) and 255)
                id to ((-byte(0)) and 255)
            }
            55 -> {
                assertEquals(6, bytes.size)
                val id = (byte(5) shl 8) or byte(4)
                val value = (byte(2) shl 24) or (byte(3) shl 16) or (byte(0) shl 8) or byte(1)
                id to value
            }
            else -> error("Not a client varbit packet: ${packet.type().opcode()}")
        }
    }

    private fun script(packet: Native947Packets.Packet): Pair<Int, List<Int>> {
        assertEquals(121, packet.type().opcode())
        val input = ByteBuffer.wrap(packet.payload())
        var count = 0
        while (true) {
            val type = input.get().toInt()
            if (type == 0) break
            assertEquals('i'.code, type)
            count++
        }
        val arguments = List(count) { input.int }.reversed()
        val id = input.int
        assertFalse(input.hasRemaining())
        return id to arguments
    }

    private data class Varbit(val domain: Int, val parent: Int, val low: Int, val high: Int)
    private fun bitDefinition(bytes: ByteArray): Varbit {
        // The inspected logical definitions use exactly tags1(parent) and2(bit range).
        assertEquals(8, bytes.size)
        assertEquals(1, bytes[0].toInt())
        assertEquals(2, bytes[4].toInt())
        assertEquals(0, bytes[7].toInt())
        return Varbit(bytes[1].toInt(), ((bytes[2].toInt() and 255) shl 8) or
            (bytes[3].toInt() and 255), bytes[5].toInt(), bytes[6].toInt())
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it.toInt() and 255) }
}
