package com.opennxt.model.world

import com.opennxt.net.buf.GamePacketReader
import io.netty.buffer.Unpooled
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Native947WorldBootstrapTest {
    @Test
    fun `idle and walk frames remain aligned as native previous-skip flags change`() {
        var localWasSkipped = false
        var externalsWereSkipped = true // The initial appearance frame has already skipped external slots.
        for (moving in listOf(false, false, true, true, false, true, false)) {
            val payload = if (moving) Native947WorldBootstrap.walkStep(1, 0) else Native947WorldBootstrap.idle()
            val buffer = Unpooled.wrappedBuffer(payload)
            try {
                val reader = GamePacketReader(buffer)
                // Native pass order: local clear, local set, external set, external clear.
                for (pass in 0..3) {
                    reader.switchToBitAccess()
                    if (pass == (if (localWasSkipped) 1 else 0)) {
                        assertEquals(if (moving) 1 else 0, reader.getBits(1))
                        if (moving) {
                            assertEquals(0, reader.getBits(1))
                            assertEquals(3, reader.getBits(2))
                            assertEquals(0, reader.getBits(1))
                            assertEquals(0x2020, reader.getBits(15))
                        } else {
                            assertEquals(0, reader.getBits(2), "No additional local players to skip")
                        }
                    }
                    if (pass == (if (externalsWereSkipped) 2 else 3)) {
                        assertEquals(0, reader.getBits(1))
                        assertEquals(3, reader.getBits(2))
                        assertEquals(2045, reader.getBits(11))
                    }
                    reader.switchToByteAccess()
                }
                assertEquals(0, buffer.readableBytes())
                localWasSkipped = !moving
                externalsWereSkipped = true
            } finally {
                buffer.release()
            }
        }
    }

    @Test
    fun `walk step decodes every adjacent direction at walk speed without masks`() {
        for (dx in -1..1) for (dy in -1..1) {
            if (dx == 0 && dy == 0) continue
            val buffer = Unpooled.wrappedBuffer(Native947WorldBootstrap.walkStep(dx, dy))
            try {
                val reader = GamePacketReader(buffer)
                reader.switchToBitAccess()
                assertEquals(1, reader.getBits(1))
                assertEquals(0, reader.getBits(1), "Movement carries no unverified masks")
                assertEquals(3, reader.getBits(2))
                assertEquals(0, reader.getBits(1), "Short relative position")
                assertEquals(2, reader.getBits(3), "Native speed-table entry for walking")
                assertEquals(0, reader.getBits(2), "Plane is unchanged")
                fun signedDelta(): Int = reader.getBits(5).let { if (it > 15) it - 32 else it }
                assertEquals(dx, signedDelta())
                assertEquals(dy, signedDelta())
                reader.switchToByteAccess()
                reader.switchToBitAccess()
                assertEquals(0, reader.getBits(1))
                assertEquals(3, reader.getBits(2))
                assertEquals(2046, 1 + reader.getBits(11))
                reader.switchToByteAccess()
                assertEquals(0, buffer.readableBytes())
            } finally {
                buffer.release()
            }
        }
        assertFailsWith<IllegalArgumentException> { Native947WorldBootstrap.walkStep(0, 0) }
        assertFailsWith<IllegalArgumentException> { Native947WorldBootstrap.walkStep(2, -1) }
    }

    @Test
    fun `walk and minimap requests decode the verified shared prefix`() {
        // y=0x1234, x=0x2345, modifier=1, using the independently recovered client writes.
        val prefix = byteArrayOf(0x12, 0xB4.toByte(), 0x23, 0x45, 0x7F)
        val expected = Native947WorldBootstrap.WalkRequest(0x2345, 0x1234, 1)
        assertEquals(expected, Native947WorldBootstrap.readWalkRequest(3, prefix))
        assertEquals(expected, Native947WorldBootstrap.readWalkRequest(28, prefix + ByteArray(13) { 0xFF.toByte() }))
        assertNull(Native947WorldBootstrap.readWalkRequest(3, prefix + byteArrayOf(0)))
        assertNull(Native947WorldBootstrap.readWalkRequest(28, prefix))
        assertNull(Native947WorldBootstrap.readWalkRequest(4, prefix))
        assertNull(Native947WorldBootstrap.readWalkRequest(3, byteArrayOf(0x40, 0x80.toByte(), 0, 0, 0x80.toByte())))
    }

    @Test
    fun `walking stays within the loaded scene and preserves its anchor`() {
        val base = TileLocation(3200, 3200)
        val original = TileLocation(3200, 3200)
        val destination = TileLocation(3203, 3198)
        var current = base
        val visited = mutableListOf<TileLocation>()
        while (true) {
            current = Native947WorldBootstrap.nextWalkTile(current, destination) ?: break
            assertTrue(Native947WorldBootstrap.isWithinScene(current, base, MapSize.SIZE_104))
            visited += current
        }
        assertEquals(listOf(TileLocation(3201, 3199), TileLocation(3202, 3198), destination), visited)
        assertEquals(original, base)
        assertNull(Native947WorldBootstrap.nextWalkTile(base, TileLocation(3201, 3201, 1)))
        // Rebuild base chunk400 puts the scene origin at3152; retain a one-tile margin on each edge.
        assertTrue(Native947WorldBootstrap.isWithinScene(TileLocation(3153, 3254), base, MapSize.SIZE_104))
        assertFalse(Native947WorldBootstrap.isWithinScene(TileLocation(3152, 3200), base, MapSize.SIZE_104))
        assertFalse(Native947WorldBootstrap.isWithinScene(TileLocation(3255, 3200), base, MapSize.SIZE_104))
        assertFalse(Native947WorldBootstrap.isWithinScene(TileLocation(3200, 3200, 1), base, MapSize.SIZE_104))
    }

    @Test
    fun `initial update decodes one appearance and skips every external player`() {
        // Exercise both sides of the native byte-length wrap, including the largest valid body.
        for (length in listOf(1, 127, 128, 255)) {
            val appearance = ByteArray(length) { it.toByte() }
            val buffer = Unpooled.wrappedBuffer(Native947WorldBootstrap.initialAppearance(appearance))
            try {
                val reader = GamePacketReader(buffer)
                reader.switchToBitAccess()
                assertEquals(1, reader.getBits(1), "Local player has an update")
                assertEquals(1, reader.getBits(1), "Local player has a pending mask")
                assertEquals(0, reader.getBits(2), "Local player does not move")
                reader.switchToByteAccess()
                // The next two passes have no eligible slots immediately after initialization.
                reader.switchToBitAccess()
                assertEquals(0, reader.getBits(1), "First external player is unchanged")
                assertEquals(3, reader.getBits(2), "Eleven-bit skip count follows")
                assertEquals(2046, 1 + reader.getBits(11), "Every external slot is covered")
                reader.switchToByteAccess()
                assertEquals(0, buffer.readUnsignedShort())
                assertEquals(0x04, buffer.readUnsignedByte().toInt())
                val decodedLength = (buffer.readUnsignedByte().toInt() + 128) and 255
                assertEquals(length, decodedLength)
                val decodedAppearance = ByteArray(decodedLength)
                buffer.readBytes(decodedAppearance)
                assertContentEquals(appearance, decodedAppearance)
                assertEquals(0, buffer.readableBytes())
            } finally {
                buffer.release()
            }
        }
    }

    @Test
    fun `rejects an appearance that cannot be represented by the native length`() {
        assertFailsWith<IllegalArgumentException> { Native947WorldBootstrap.initialAppearance(ByteArray(0)) }
        assertFailsWith<IllegalArgumentException> { Native947WorldBootstrap.initialAppearance(ByteArray(256)) }
    }
}
