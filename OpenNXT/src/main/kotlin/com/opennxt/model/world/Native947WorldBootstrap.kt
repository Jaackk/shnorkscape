package com.opennxt.model.world

/** Verified native 947-3 updates for one local player and unchanged external slots. */
internal object Native947WorldBootstrap {
    fun idle(): ByteArray = byteArrayOf(0, 0x7F, 0xF4.toByte())

    data class WalkRequest(val x: Int, val y: Int, val modifier: Int)

    fun readWalkRequest(opcode: Int, payload: ByteArray): WalkRequest? {
        val expectedLength = when (opcode) {
            3 -> 5
            28 -> 18
            else -> return null
        }
        if (payload.size != expectedLength) return null
        val y = ((payload[0].toInt() and 255) shl 8) or ((payload[1].toInt() - 128) and 255)
        val x = ((payload[2].toInt() and 255) shl 8) or (payload[3].toInt() and 255)
        if (x !in 0..16383 || y !in 0..16383) return null
        return WalkRequest(x, y, (128 - payload[4].toInt()) and 255)
    }

    fun isWithinScene(tile: TileLocation, base: TileLocation, size: MapSize): Boolean =
        tile.plane == base.plane &&
            tile.getLocalX(base, size) in 1 until (size.size - 1) &&
            tile.getLocalY(base, size) in 1 until (size.size - 1)

    fun nextWalkTile(current: TileLocation, destination: TileLocation): TileLocation? {
        if (current.plane != destination.plane || current == destination) return null
        return TileLocation(
            current.x + destination.x.compareTo(current.x),
            current.y + destination.y.compareTo(current.y),
            current.plane
        )
    }

    fun walkStep(dx: Int, dy: Int): ByteArray {
        require(dx in -1..1 && dy in -1..1 && (dx != 0 || dy != 0)) { "Expected one adjacent walk step" }
        // Native 0x125D57 reads a short relative position; selector 2 selects WALK in its speed table.
        // update=1, mask=0, movement=3, short=0, then speed(3), plane(2), dx(5), dy(5).
        val relativePosition = (2 shl 12) or ((dx and 31) shl 5) or (dy and 31)
        val bits = (0b10110 shl 15) or relativePosition
        // With one local and uniformly skipped external slots, exactly one pass per list has content.
        return byteArrayOf(
            (bits ushr 12).toByte(), (bits ushr 4).toByte(), (bits shl 4).toByte(),
            0x7F, 0xF4.toByte()
        )
    }

    fun initialAppearance(appearance: ByteArray): ByteArray {
        require(appearance.size in 1..255) { "Native 947 appearance must fit an unsigned byte length" }
        // One local mask-only update, then skip all 2046 external players. Each pass is byte aligned.
        // The native mask parser skips two bytes, uses appearance bit 0x4, and copies the body unchanged.
        return byteArrayOf(
            0xC0.toByte(), 0x7F, 0xF4.toByte(),
            0, 0, 0x04, (appearance.size + 128).toByte()
        ) + appearance
    }
}
