package com.opennxt.net.login

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.rs.network.protocol.modern947.Native947Packets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Native947InterfaceBootstrapTest {
    private fun inspectedSlots(): Map<Int, Native947InterfaceBootstrap.Slot> {
        // This committed evidence is compact metadata; no cache or server is initialized.
        val bytes = Files.readAllBytes(Paths.get(
            "data/prot/947/generated/native947-3/verified/ui/slot-inventory-7716.json"
        ))
        assertEquals("b123b135a6df569d12f4821a21b1db1a34eefe6d14b84e9dc949741ed9bc4539",
            hex(MessageDigest.getInstance("SHA-256").digest(bytes)), "Revalidate a changed slot inventory")
        val inventory = JsonParser.parseString(bytes.toString(Charsets.UTF_8)).asJsonObject
        assertEquals(947, inventory["revision"].asInt)
        assertEquals(7716, inventory["enum"].asInt)
        return inventory["slots"].asJsonArray.associate { element ->
            val slot = element.asJsonObject
            slot["key"].asInt to Native947InterfaceBootstrap.Slot(
                componentHash(slot["attach"]), componentHash(slot["wrapper"])
            )
        }.also { assertEquals(101, it.size) }
    }

    @Test
    fun `pure extraction reproduces every pre-policy opcode and payload in order`() {
        val packets = withRecoverySeed {
            Native947InterfaceBootstrap.packets(
                inspectedSlots(), Native947InterfaceBootstrap.baselineHiddenSlots, assertModernMode = false
            )
        }
        assertEquals(85, packets.size)
        assertEquals(golden("baseline"), wire(packets))
    }

    @Test
    fun `modern policy adds mode and onboarding assertions inert wrappers and the supported run action`() {
        val packets = withRecoverySeed { Native947InterfaceBootstrap.packets(inspectedSlots()) }
        assertEquals(100, packets.size)
        assertEquals(golden("modern"), wire(packets))
        assertEquals(listOf(94, 50, 50, 50, 50, 50, 50, 8), packets.take(8).map { it.type().opcode() },
            "Mode and onboarding bits must precede the panel onLoad hooks")
        assertEquals(listOf(45, 46, 1049, 1050, 1051, 1052, 1053),
            Native947InterfaceBootstrap.inertHiddenSlots)
    }

    @Test
    fun `normal world startup completes native tutorial gates without choosing a league`() {
        val packets = Native947InterfaceBootstrap.packets(inspectedSlots())
        val state = packets.drop(1).takeWhile { it.type().opcode() == 50 }.associate { packet ->
            val bytes = packet.payload()
            assertEquals(3, bytes.size)
            val id = (bytes[0].toInt() and 255) or ((bytes[1].toInt() and 255) shl 8)
            id to ((bytes[2].toInt() - 128) and 255)
        }
        assertEquals(mapOf(27168 to 0, 27169 to 0, 22875 to 0,
            39917 to 98, 49044 to 100, 60098 to 1), state)
        // 15532 also accepts an active League, but startup must not manufacture
        // a League selection by sending a full parent varp such as 12314.
        assertTrue(packets.take(7).all { it.type().opcode() in listOf(94, 50) })
        val baseline = Native947InterfaceBootstrap.packets(inspectedSlots(), assertModernMode = false)
        assertEquals(8, baseline[1].type().opcode(), "Historical baseline has no mode/onboarding writes")
    }

    @Test
    fun `run orb enables only Toggle Run on the static component`() {
        val events = Native947InterfaceBootstrap.packets(inspectedSlots())
            .filter { it.type().opcode() == 35 }
        assertEquals(2, events.size)
        assertEquals("ffffffff0e00b90500000200", hex(events.last().payload()),
            "Component1465:14, static slot -1, mask2 enables operation1 only")
        val baselineEvents = Native947InterfaceBootstrap.packets(
            inspectedSlots(), Native947InterfaceBootstrap.baselineHiddenSlots, assertModernMode = false
        ).count { it.type().opcode() == 35 }
        assertEquals(1, baselineEvents, "The pre-policy golden baseline remains unchanged")
    }

    @Test
    fun `discovering another addressable slot cannot change visibility`() {
        val slots = inspectedSlots()
        val before = wire(Native947InterfaceBootstrap.packets(slots))
        val after = wire(Native947InterfaceBootstrap.packets(
            slots + (9999 to Native947InterfaceBootstrap.Slot(hash(900), hash(901)))
        ))
        assertEquals(before, after)
    }

    @Test
    fun `an explicitly hidden alias cannot hide a retained ancestor or panel`() {
        val slots = inspectedSlots() + mapOf(
            9000 to Native947InterfaceBootstrap.Slot(hash(30), hash(27)),
            9001 to Native947InterfaceBootstrap.Slot(hash(103), hash(101)),
            9002 to Native947InterfaceBootstrap.Slot(hash(300), hash(298))
        )
        assertEquals(
            wire(Native947InterfaceBootstrap.packets(slots)),
            wire(Native947InterfaceBootstrap.packets(slots,
                Native947InterfaceBootstrap.hiddenSlots + listOf(9000, 9001, 9002)))
        )
    }

    @Test
    fun `missing and off-root selected addresses cannot produce malformed interface frames`() {
        val slots = inspectedSlots()
        for (missing in listOf(2, 1049)) {
            assertFailsWith<IllegalArgumentException> {
                Native947InterfaceBootstrap.packets(slots - missing)
            }
        }
        for (bad in listOf(-1, (1475 shl 16) or 17, hash(65535))) {
            assertFailsWith<IllegalArgumentException> {
                Native947InterfaceBootstrap.packets(
                    slots + (2 to Native947InterfaceBootstrap.Slot(bad, hash(101)))
                )
            }
            assertFailsWith<IllegalArgumentException> {
                Native947InterfaceBootstrap.packets(
                    slots + (1049 to Native947InterfaceBootstrap.Slot(hash(576), bad))
                )
            }
        }
    }

    @Test
    fun `optional ribbon attaches to its cache slot and retains its wrapper`() {
        val slots = inspectedSlots()
        val disabled = wire(Native947InterfaceBootstrap.packets(slots))
        val enabled = wire(Native947InterfaceBootstrap.packets(slots, ribbonEnabled = true))
        val hide = wire(listOf(Native947Packets.hideInterface(1477, 61, true))).single()
        val show = wire(listOf(Native947Packets.hideInterface(1477, 61, false))).single()
        val attach = wire(listOf(Native947Packets.openSub(1477, 64, 1431, true))).single()
        assertTrue(hide in disabled)
        assertFalse(hide in enabled)
        assertFalse(show in enabled, "ordinary login attaches content without forcing workspace visibility")
        assertTrue(attach in enabled)
        assertEquals(disabled.filterNot { it == hide }, enabled.filterNot { it == attach })
        val recovery = wire(withRecoverySeed { Native947InterfaceBootstrap.packets(slots, ribbonEnabled = true) })
        assertTrue(show in recovery, "the explicit recovery seed remains available")
    }

    @Test
    fun `ordinary bootstrap never sends a panel-visible override`() {
        val slots = inspectedSlots()
        val packets = Native947InterfaceBootstrap.packets(slots, ribbonEnabled = true)
        val forcedVisible = listOf(2, 3, 4, 5, 6, 7, 18, 33, 34, 35, 36, 39, 1002).map { key ->
            val wrapper = requireNotNull(slots[key]).wrapper and 65535
            wire(listOf(Native947Packets.hideInterface(1477, wrapper, false))).single()
        }
        assertTrue(forcedVisible.none(wire(packets)::contains),
            "visibility is restored by the native workspace, not overwritten at login")
    }

    private fun <T> withRecoverySeed(block: () -> T): T {
        val key = "ataraxia950.workspace.forceOpenPanels"
        val previous = System.getProperty(key)
        return try {
            System.setProperty(key, "true")
            block()
        } finally {
            if (previous == null) System.clearProperty(key) else System.setProperty(key, previous)
        }
    }

    private fun golden(name: String): List<String> = requireNotNull(javaClass.getResource(
        "/native947/interface-bootstrap-$name.txt"
    )).readText().lineSequence().filter { it.isNotBlank() && !it.startsWith("#") }.toList()

    private fun wire(packets: List<Native947Packets.Packet>): List<String> = packets.map {
        "${it.type().opcode()} ${hex(it.payload())}"
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it.toInt() and 255) }
    private fun hash(component: Int): Int = (1477 shl 16) or component
    private fun componentHash(value: JsonElement): Int = if (value.isJsonNull) -1 else
        (value.asJsonObject["interface"].asInt shl 16) or value.asJsonObject["component"].asInt
}
