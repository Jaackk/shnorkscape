package com.opennxt.net.login

import com.google.gson.JsonParser
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Native947CacheContentTest {
    private fun inspectedFiles(): Map<Int, ByteArray> {
        val text = requireNotNull(javaClass.getResource("/native947/inspected-item-definitions.json")).readText()
        return JsonParser.parseString(text).asJsonObject.entrySet().associate { (id, value) ->
            id.toInt() to value.asString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        }
    }

    @Test
    fun `inspected modern definitions admit only supported supplies and bronze equipment`() {
        val files = inspectedFiles()
        val catalog = Native947CacheContent.itemCatalog { index, group, file ->
            assertEquals(19, index)
            requireNotNull(files[(group shl 8) or file])
        }
        assertEquals("Coins", catalog.get(995).name)
        assertTrue(catalog.get(995).stackable)
        assertFalse(catalog.get(1511).stackable)
        assertEquals("Craft", catalog.get(1511).option(1))
        assertEquals("Light", catalog.get(1511).option(2))
        assertEquals("Eat", catalog.get(315).option(1))
        assertEquals("Drop", catalog.get(315).option(5))
        for ((id, slot) in listOf(1277 to 3, 1173 to 5, 1139 to 0)) {
            assertFalse(catalog.get(id).stackable)
            assertEquals(slot, catalog.get(id).equipSlot)
            assertEquals(2, catalog.get(id).equipOption)
            assertNull(catalog.get(id).option(1), "Modern Wear and Wield use operation 2")
        }
        assertEquals("Wield", catalog.get(1277).option(2))
        assertEquals("Wield", catalog.get(1173).option(2))
        assertEquals("Wear", catalog.get(1139).option(2))
        assertEquals(-1, catalog.get(1511).equipSlot)
        assertNull(catalog.get(316), "Uninspected modern definitions cannot enter native container operations")
    }

    @Test
    fun `equipment metadata binds the modern actor layer and rejects stale attachments`() {
        val ordinary = Native947CacheContent.equipmentUi((1477 shl 16) or 114, (1477 shl 16) or 112)
        assertEquals(2, ordinary.bootstrap.size, "ordinary login must not overwrite saved workspace geometry")
        assertEquals(8471, ByteBuffer.wrap(ordinary.bootstrap[0].payload()).let { bytes ->
            bytes.position(bytes.limit() - 4); bytes.int
        })
        val equipment = withRecoverySeed {
            Native947CacheContent.equipmentUi((1477 shl 16) or 114, (1477 shl 16) or 112)
        }
        assertEquals(1462, equipment.interfaceId)
        assertEquals(31, equipment.itemComponent)
        assertEquals(94, equipment.containerId)
        assertEquals(7, equipment.bootstrap.size)
        assertEquals(1, equipment.refresh.size)
        val size = ByteBuffer.wrap(equipment.bootstrap[0].payload())
        assertEquals("iiiii", ByteArray(5).also { size.get(it) }.toString(Charsets.US_ASCII))
        assertEquals(0, size.get().toInt())
        assertEquals((1477 shl 16) or 112, size.int)
        assertEquals(0, size.int)
        assertEquals(0, size.int)
        assertEquals(360, size.int)
        assertEquals(224, size.int)
        assertEquals(11145, size.int)
        assertFalse(size.hasRemaining())
        val position = ByteBuffer.wrap(equipment.bootstrap[1].payload())
        assertEquals("iiiii", ByteArray(5).also { position.get(it) }.toString(Charsets.US_ASCII))
        assertEquals(0, position.get().toInt())
        // Native script arguments travel in reverse order; these are bottom/right anchors.
        assertEquals((1477 shl 16) or 112, position.int)
        assertEquals(2, position.int)
        assertEquals(2, position.int)
        assertEquals(80, position.int)
        assertEquals(232, position.int)
        assertEquals(13268, position.int)
        assertFalse(position.hasRemaining())
        val show = ByteBuffer.wrap(equipment.bootstrap[2].payload())
        assertEquals('i'.code, show.get().toInt())
        assertEquals(0, show.get().toInt())
        assertEquals((1477 shl 16) or 112, show.int)
        assertEquals(2330, show.int)
        assertFalse(show.hasRemaining())
        val render = ByteBuffer.wrap(equipment.bootstrap[3].payload())
        assertEquals("ii", ByteArray(2).also { render.get(it) }.toString(Charsets.US_ASCII))
        assertEquals(0, render.get().toInt())
        assertEquals(94, render.int)
        assertEquals((1462 shl 16) or 3, render.int)
        assertEquals(8471, render.int)
        assertFalse(render.hasRemaining())
        val saveCurrent = ByteBuffer.wrap(equipment.bootstrap[4].payload())
        assertEquals('i'.code, saveCurrent.get().toInt())
        assertEquals(0, saveCurrent.get().toInt())
        assertEquals(3, saveCurrent.int)
        assertEquals(8707, saveCurrent.int)
        assertFalse(saveCurrent.hasRemaining())
        val savePreset = ByteBuffer.wrap(equipment.bootstrap[5].payload())
        assertEquals("ii", ByteArray(2).also { savePreset.get(it) }.toString(Charsets.US_ASCII))
        assertEquals(0, savePreset.get().toInt())
        assertEquals(8, savePreset.int, "Native resizing restores the selected preset 8")
        assertEquals(3, savePreset.int, "Save only the equipment slot")
        assertEquals(8708, savePreset.int)
        assertFalse(savePreset.hasRemaining())
        assertFailsWith<IllegalStateException> {
            Native947CacheContent.equipmentUi((1477 shl 16) or 115, (1477 shl 16) or 112)
        }
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

    @Test
    fun `changed cache metadata fails before a catalog can authorize item transfers`() {
        val files = inspectedFiles().mapValues { (_, data) -> data.clone() }
        files.getValue(995)[0] = 0
        assertFailsWith<IllegalStateException> {
            Native947CacheContent.itemCatalog { _, group, file -> files.getValue((group shl 8) or file) }
        }
    }

    @Test
    fun `bank menu maps All separately from custom quantity and placeholders`() {
        // Selected-cache script 13798 sets 6=Withdraw-X, 7=Withdraw-All, 8=Placeholder.
        // The 910 server handled a different order, so this is a migration regression boundary.
        val bank = Native947CacheContent.bankUi((1477 shl 16) or 695, (1477 shl 16) or 693)
        assertEquals(1, bank.withdrawAmount(1))
        assertEquals(1, bank.depositAmount(1))
        assertEquals(1, bank.withdrawAmount(2))
        assertEquals(5, bank.withdrawAmount(3))
        assertEquals(10, bank.withdrawAmount(4))
        assertEquals(Int.MAX_VALUE, bank.withdrawAmount(7))
        assertEquals(Int.MAX_VALUE, bank.depositAmount(7))
        assertEquals(0, bank.withdrawAmount(6))
        assertEquals(6, bank.withdrawXOption)
        assertEquals(0, bank.withdrawAmount(8))
        assertEquals(0, bank.withdrawAmount(0))
        assertEquals(0, bank.depositAmount(11))
        assertEquals(201, bank.itemComponent)
        assertEquals(15, bank.inventoryComponent)
    }
}
