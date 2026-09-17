package com.opennxt.model.world

/** Minimal local-player updates checked against the 950-1 WIN64 receive code. */
internal object Native950WorldBootstrap {
    // Same four byte-aligned GPI passes as 947: local unchanged, 2046 external slots skipped.
    fun idle(): ByteArray = byteArrayOf(0, 0x7F, 0xF4.toByte())

    /**
     * Applies the 950 wire transform for the appearance body.
     *
     * The appearance block copy at 0x14012DC81 dispatches through 0x14010DDF0, whose variant is
     * taken from the transform-script byte the caller installs at 0x14012DC46. That cursor points
     * at .rdata 0x140B5FEC9, which holds 0x02: the forward copy at 0x14010DE7C, which adds 0x80 to
     * every byte (0x14010DEA4). The server therefore has to send each byte pre-biased by -0x80,
     * which for bytes is the same as xor 0x80.
     *
     * The 947 client installs 0x00 at 0x140B91BAD instead, selecting the plain memcpy at
     * 0x14010DF09, which is why Native947WorldBootstrap sends the body verbatim.
     */
    private fun transformBody(appearance: ByteArray): ByteArray =
        ByteArray(appearance.size) { (appearance[it].toInt() xor 0x80).toByte() }

    /**
     * Encodes one wearpos slot value the way the 950 client reads it.
     *
     * 0x140131C60-0x140131C8B: unsigned LEB128 - 7 bits per byte, least significant group first,
     * continuing while the byte read is above 0x7F. The client then classifies the value using the
     * constants installed by the constructor at 0x140131A70: 0 is an empty slot, 1 at wearpos index
     * 0 is the npc-morph escape, values below 0x800 are identity-kit ids biased by 2, and values at
     * or above 0x800 are item ids biased by 0x800.
     */
    fun encodeWearposSlot(value: Int): ByteArray {
        require(value >= 0) { "Wearpos slot value must not be negative: $value" }
        val out = ArrayList<Byte>(5)
        var remaining = value
        while (remaining >= 0x80) {
            out.add(((remaining and 0x7F) or 0x80).toByte())
            remaining = remaining ushr 7
        }
        out.add(remaining.toByte())
        return out.toByteArray()
    }

    /**
     * One local-player movement step, as a complete PLAYER_INFO (opcode 36) payload.
     *
     * Decoder 0x140126030 reads, MSB first: 1 bit update = 1, 1 bit mask = 0, 2 bits type = 3,
     * 1 bit long = 0, then 15 bits of style(3) | planeDelta(2) | dx(5 signed) | dy(5 signed)
     * (0x1401264f2: `sar ecx,0xc; and ecx,7`, `sar ecx,0xa`, `sar ecx,5; and ecx,0x1f`).
     * The style index selects from the table at 0x140C6EC88 = {-1, 0, 1, 2, 3}; index 2 is WALK
     * and 3 is RUN. Indices 5..7 read past the end of that table and must never be sent.
     *
     * Types 1 and 2 are deliberately unused: type 2 never writes the style pointer, so the step
     * renders with no animation. Type 3 carries the style in the same 15 bits, which is why the
     * equivalent 947 frame animates.
     *
     * The trailing 0x7F 0xF4 is the same skip used by [idle]: update 0, selector 3, 11-bit count
     * 2045, covering all 2046 external slots.
     */
    fun walkStep(dx: Int, dy: Int, styleIndex: Int = STYLE_WALK, planeDelta: Int = 0): ByteArray {
        require(dx in -16..15 && dy in -16..15) { "Relocate delta out of 5-bit signed range: $dx,$dy" }
        require(styleIndex == STYLE_WALK || styleIndex == STYLE_RUN) {
            "Only WALK($STYLE_WALK) and RUN($STYLE_RUN) are safe style indices, got $styleIndex"
        }
        require(planeDelta in 0..3) { "Plane delta must fit two bits: $planeDelta" }
        val bits = (0b10110 shl 15) or (styleIndex shl 12) or (planeDelta shl 10) or
            ((dx and 31) shl 5) or (dy and 31)
        return byteArrayOf(
            (bits ushr 12).toByte(), (bits ushr 4).toByte(), (bits shl 4).toByte(),
            0x7F, 0xF4.toByte()
        )
    }

    const val STYLE_WALK = 2
    const val STYLE_RUN = 3

    data class WalkRequest(val x: Int, val y: Int, val modifier: Int, val minimap: Boolean)

    /**
     * Decodes a 950 map or minimap walk click.
     *
     * Sender 0x1400e46b0 writes the payload at 0x1400e47f6-0x1400e4869:
     *   b0..b1  destination y, plain big-endian u16
     *   b2      (modifier + 0x80) and 0xFF
     *   b3      (x + 0x80) and 0xFF
     *   b4      (x ushr 8) and 0xFF
     * Opcode 88 (size 5) is a ground click; opcode 78 (size 18) is a minimap click and appends a
     * fixed 13-byte telemetry trailer this server ignores. Registration is at 0x14000b160
     * (`mov edx,0x58` / `lea r8d,[rdx-0x53]`) and 0x14000b020 (`mov edx,0x4e` / `[rdx-0x3c]`).
     *
     * x and y are absolute world tiles; the packet carries no plane.
     *
     * The 947 layout is NOT compatible and must not be reused: there the modifier is at b4 with
     * the inverse transform `(128 - b4)`, and the `+128` bias sits on y rather than x. Applying
     * the 947 formula to a 950 payload turns modifier 1 into 255 and corrupts both coordinates.
     */
    fun readWalkRequest950(opcode: Int, payload: ByteArray): WalkRequest? {
        val minimap = when (opcode) {
            88 -> false
            78 -> true
            else -> return null
        }
        if (payload.size != (if (minimap) 18 else 5)) return null
        val y = ((payload[0].toInt() and 0xFF) shl 8) or (payload[1].toInt() and 0xFF)
        val modifier = (payload[2].toInt() - 128) and 0xFF
        val x = ((payload[4].toInt() and 0xFF) shl 8) or ((payload[3].toInt() - 128) and 0xFF)
        if (x !in 0..16383 || y !in 0..16383) return null
        if (modifier !in 0..1) return null
        return WalkRequest(x, y, modifier, minimap)
    }

    /** Option 1 of the interface-action family. The ten options are a fixed table at 0x140b63c00. */
    const val CLIENT_IF_BUTTON1 = 18

    data class InterfaceButton(val interfaceId: Int, val component: Int, val slot: Int, val item: Int)

    /**
     * Decodes an interface click (client opcodes 18, 122, 89, 100, 81, 126, 49, 66, 31, 59 for
     * options 1..10, all fixed size 9).
     *
     *   b0..b2  big-endian u24 item id, 0xFFFFFF meaning -1
     *   b3..b6  interface hash in wire order b2,b3,b0,b1
     *   b7..b8  big-endian u16 dynamic slot, 0xFFFF meaning -1
     *
     * The hash order is the one that makes every live capture land on a component that exists in
     * the 950 cache; plain big-endian, little-endian and the 947 order all produce absent ids.
     */
    fun readInterfaceButton(payload: ByteArray): InterfaceButton? {
        if (payload.size != 9) return null
        val b = IntArray(9) { payload[it].toInt() and 0xFF }
        val rawItem = (b[0] shl 16) or (b[1] shl 8) or b[2]
        val hash = (b[4] shl 24) or (b[3] shl 16) or (b[6] shl 8) or b[5]
        val rawSlot = (b[7] shl 8) or b[8]
        return InterfaceButton(
            interfaceId = hash ushr 16,
            component = hash and 0xFFFF,
            slot = if (rawSlot == 0xFFFF) -1 else rawSlot,
            item = if (rawItem == 0xFFFFFF) -1 else rawItem
        )
    }

    /**
     * CLIENT_SETVARC_LARGE payload, opcode 119, fixed 6 bytes.
     *
     * Parser 0x140141DB0: the id is a big-endian u16 whose low byte carries +0x80
     * (lea eax,[r9-0x80] at 0x140141e1c); the value is reassembled as
     * (b3 shl 24) or (b2 shl 16) or (b5 shl 8) or b4 (0x140141de5-0x140141e0f).
     * Registration entry base + 119*80 + 0x10 = 0x140E97430, table base 0x140E94EF0.
     */
    fun encodeVarc(id: Int, value: Int): ByteArray {
        require(id in 0..65535) { "Client var id out of range: $id" }
        return byteArrayOf(
            ((id ushr 8) and 0xFF).toByte(), ((id + 128) and 0xFF).toByte(),
            ((value ushr 16) and 0xFF).toByte(), ((value ushr 24) and 0xFF).toByte(),
            (value and 0xFF).toByte(), ((value ushr 8) and 0xFF).toByte()
        )
    }

    /**
     * VARBIT_SMALL payload, opcode 28, fixed 3 bytes.
     *
     * Parser 0x1401421E0: b0 = (value + 0x80) and 0xFF (add al,0x80 at 0x140142202), then the
     * definition id as a plain little-endian u16 with no bias (0x14014220f-0x14014223f).
     */
    fun encodeVarbit(id: Int, value: Int): ByteArray {
        require(id in 0..65535) { "Varbit id out of range: $id" }
        require(value in 0..255) { "Small varbit value must fit an unsigned byte: $value" }
        return byteArrayOf(
            ((value + 128) and 0xFF).toByte(),
            (id and 0xFF).toByte(), ((id ushr 8) and 0xFF).toByte()
        )
    }

    /**
     * The 18-byte REBUILD_NORMAL (opcode 63) body, in the order the 950 parser reads it at
     * 0x1400f6e50.
     *
     *   b0..b1   chunkY, big-endian u16 with +128 on the low byte  (0x1400f6e60)
     *   b2       mapSize, plain; anything but 5 aborts the packet  (0x1400f6f8e)
     *   b3       npcBits, +128                                     (0x1400f6e97)
     *   b4..b5   skipped, never read                               (0x1400f6e93)
     *   b6..b7   chunkX, plain big-endian u16                      (0x1400f6ea2)
     *   b8..b9   areaType
     *   b10..b13 hash1, b14..b17 hash2
     *
     * The axes are not interchangeable. Bytes 6..7 land in scene slot 0 and bytes 0..1 in slot 1
     * (0x1400f6fe5 / 0x1400f6fed); slot 0 is the axis bounds-checked against +0x14034, which is fed
     * by hash1's high 14 bits, and TileLocation.tileHash packs x in the high bits. So slot 0 is X.
     *
     * A rejected rebuild is worse than a dropped one: the mapSize bail at 0x1400f6f98 jumps past
     * the gate-byte clear at 0x1400f7189, leaving the GPI-bitstream flag armed so the *next*
     * rebuild is mis-parsed too.
     */
    fun rebuildHeader950(
        chunkX: Int,
        chunkY: Int,
        npcBits: Int,
        areaType: Int,
        hash1: Int,
        hash2: Int
    ): ByteArray {
        require(chunkX in 0..0x1FFF) { "chunkX out of range: $chunkX" }
        require(chunkY in 0..0x1FFF) { "chunkY out of range: $chunkY" }
        require(npcBits in 0..255) { "npcBits must fit a byte: $npcBits" }
        return byteArrayOf(
            ((chunkY ushr 8) and 0xFF).toByte(), ((chunkY + 128) and 0xFF).toByte(),
            5,
            ((npcBits + 128) and 0xFF).toByte(),
            0, 0,
            ((chunkX ushr 8) and 0xFF).toByte(), (chunkX and 0xFF).toByte(),
            ((areaType ushr 8) and 0xFF).toByte(), (areaType and 0xFF).toByte(),
            (hash1 ushr 24).toByte(), (hash1 ushr 16).toByte(),
            (hash1 ushr 8).toByte(), hash1.toByte(),
            (hash2 ushr 24).toByte(), (hash2 ushr 16).toByte(),
            (hash2 ushr 8).toByte(), hash2.toByte()
        )
    }

    fun initialAppearance(appearance: ByteArray): ByteArray {
        require(appearance.size in 1..255) { "Native 950 appearance must fit an unsigned byte length" }
        // 0x1401436C4 skips two bytes; 0x14012DC31 tests mask 0x20.
        // 0x14012DC65 reads the length with +128, then 0x14012DC81 copies the transformed body.
        return byteArrayOf(0xC0.toByte(), 0x7F, 0xF4.toByte(), 0, 0, 0x20,
            (appearance.size + 128).toByte()) + transformBody(appearance)
    }
}
