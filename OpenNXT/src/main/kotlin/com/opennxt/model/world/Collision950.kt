package com.opennxt.model.world

import com.opennxt.OpenNXT
import com.opennxt.ext.readSmartShort
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import mu.KotlinLogging

/**
 * Server-side collision for the native 950 client.
 *
 * The NXT client does no server-authoritative collision of its own, so everything here is the
 * server's own model of the world, built from the same cache the client renders from. It is
 * ported from the 947 project's `Region`/`RegionMap`/`World.checkWalkStep`, whose map data is
 * byte-identical to 950's.
 *
 * Sources, all in JS5 index 5 under `group = regionX or (regionY shl 7)`:
 *   file 3  terrain, `jagx` + version 1, then 4 x 64 x 64 tile records
 *   file 0  loc (object) placements, delta coded
 * Loc definitions come from index 16, `group = id shr 8`, `file = id and 0xff`.
 *
 * Both decoders were validated over the whole cache before this was written: all 5173 populated
 * map squares decode with the loc buffer consumed exactly, and 140220 of 140252 loc definitions
 * decode, the 32 failures being the undecoded opcode-209 records.
 *
 * The clip map is global and sparse rather than per-square. That is deliberate: a wall on a square
 * boundary stamps a bit on the neighbouring square's tile, and a per-square array would drop those
 * writes and turn every region seam into a one-way wall.
 */
object Collision950 {
    private val logger = KotlinLogging.logger { }

    const val WALL_NORTH = 0x2
    const val WALL_EAST = 0x8
    const val WALL_SOUTH = 0x20
    const val WALL_WEST = 0x80
    const val CORNER_NW = 0x1
    const val CORNER_NE = 0x4
    const val CORNER_SE = 0x10
    const val CORNER_SW = 0x40
    const val OBJECT = 0x100
    const val FLOOR_DECO_BLOCKS = 0x40000
    const val FLOOR_BLOCKS = 0x200000

    /** Anything that makes a tile itself impassable. */
    const val BLOCKED = FLOOR_BLOCKS or FLOOR_DECO_BLOCKS or OBJECT

    private const val MAP_INDEX = 5
    private const val LOC_INDEX = 16
    private const val FILE_LOCS = 0
    private const val FILE_TERRAIN = 3
    private const val JAGX = 0x6A616778

    private val clip = Long2IntOpenHashMap()
    private val loadedRegions = IntOpenHashSet()
    private val locTypes = Int2ObjectOpenHashMap<LocType?>()
    private val warnedLocIds = IntOpenHashSet()

    internal class LocType(
        var sizeX: Int = 1,
        var sizeY: Int = 1,
        var clipType: Int = 2,
        var ignoreClipOnAlternativeRoute: Boolean = false
    )

    fun mapGroup(regionX: Int, regionY: Int): Int {
        require(regionX in 0..127) { "regionX $regionX does not fit the 7-bit index 5 key" }
        return regionX or (regionY shl 7)
    }

    private fun key(plane: Int, x: Int, y: Int): Long =
        (plane.toLong() shl 56) or (x.toLong() shl 28) or y.toLong()

    private fun orBits(plane: Int, x: Int, y: Int, bits: Int) {
        if (plane !in 0..3) return
        val k = key(plane, x, y)
        clip.put(k, clip.get(k) or bits)
    }

    /**
     * The clip mask for a tile, or -1 when the map square is not loaded.
     *
     * -1 has every bit set so every walk test fails. Returning 0 for "not loaded" would let the
     * player walk through terrain the server has never read.
     */
    fun mask(plane: Int, x: Int, y: Int): Int {
        if (plane !in 0..3) return -1
        if (((x shr 6) shl 8) or (y shr 6) !in loadedRegions) return -1
        return clip.get(key(plane, x, y))
    }

    /** Loads the map square containing this tile and its eight neighbours. Idempotent. */
    fun ensureLoadedAround(tile: TileLocation) {
        val rx = tile.x shr 6
        val ry = tile.y shr 6
        for (dx in -1..1) for (dy in -1..1) loadRegion(rx + dx, ry + dy)
    }

    fun loadRegion(regionX: Int, regionY: Int) {
        if (regionX !in 0..127 || regionY < 0) return
        val id = (regionX shl 8) or regionY
        if (!loadedRegions.add(id)) return
        try {
            buildRegion(regionX, regionY)
        } catch (e: Exception) {
            // Leave the square marked loaded but empty of clip data would be worse than useless:
            // it would read as fully walkable. Drop it back out so mask() keeps returning -1.
            loadedRegions.remove(id)
            logger.warn(e) { "Collision load failed for region $regionX,$regionY; it stays impassable" }
        }
    }

    private fun archiveFile(index: Int, group: Int, file: Int): ByteArray? {
        val table = OpenNXT.filesystem.getReferenceTable(index) ?: return null
        val archive = table.loadArchive(group) ?: return null
        return archive.files[file]?.data
    }

    private fun buildRegion(regionX: Int, regionY: Int) {
        val group = mapGroup(regionX, regionY)
        val terrainRaw = archiveFile(MAP_INDEX, group, FILE_TERRAIN) ?: return
        val settings = decodeTerrain(Unpooled.wrappedBuffer(terrainRaw))
        val baseX = regionX * 64
        val baseY = regionY * 64

        // Pass 1 - blocked terrain. The bridge bit is always read from plane 1, whatever plane the
        // tile itself is on; a tile that would drop below plane 0 is discarded, not clamped,
        // because clamping re-blocks the water underneath every bridge.
        for (plane in 0..3) {
            for (x in 0..63) {
                for (y in 0..63) {
                    if (settings[plane][x][y] and 1 == 0) continue
                    val real = plane - if (settings[1][x][y] and 2 != 0) 1 else 0
                    if (real >= 0) orBits(real, baseX + x, baseY + y, FLOOR_DECO_BLOCKS)
                }
            }
        }

        // Pass 2 - locs.
        val locsRaw = archiveFile(MAP_INDEX, group, FILE_LOCS) ?: return
        decodeLocs(Unpooled.wrappedBuffer(locsRaw)) { id, lx, ly, plane, shape, rotation ->
            val real = plane - if (settings[1][lx][ly] and 2 != 0) 1 else 0
            if (real >= 0) stampLoc(id, baseX + lx, baseY + ly, real, shape, rotation)
        }
    }

    private fun stampLoc(id: Int, x: Int, y: Int, plane: Int, shape: Int, rotation: Int) {
        val type = locType(id)
        if (type == null) {
            if (warnedLocIds.add(id)) logger.warn { "No usable loc definition for $id; placement skipped" }
            return
        }
        // Shape 22 is inverted relative to every other shape: a ground decoration clips only when
        // opcode 27 set clipType 1, everything else clips unless opcode 17 set clipType 0.
        if (if (shape == 22) type.clipType != 1 else type.clipType == 0) return

        when {
            shape <= 3 -> {
                // Opcode 74 suppresses the wall entirely; the 947 reference wraps the whole
                // addWall in that check.
                if (type.ignoreClipOnAlternativeRoute) return
                stampWall(x, y, plane, shape, rotation)
            }
            shape <= 8 -> return                       // wall decorations never clip
            shape <= 21 -> {
                val w = if (rotation % 2 == 0) type.sizeX else type.sizeY
                val l = if (rotation % 2 == 0) type.sizeY else type.sizeX
                for (ox in 0 until w) for (oy in 0 until l) orBits(plane, x + ox, y + oy, OBJECT)
            }
            shape == 22 -> orBits(plane, x, y, FLOOR_DECO_BLOCKS)
        }
    }

    /** Each wall stamps its own tile and the mirrored bit on the neighbour, so both sides block. */
    private fun stampWall(x: Int, y: Int, plane: Int, shape: Int, rotation: Int) {
        when (shape) {
            0 -> when (rotation) {
                0 -> { orBits(plane, x, y, WALL_WEST); orBits(plane, x - 1, y, WALL_EAST) }
                1 -> { orBits(plane, x, y, WALL_NORTH); orBits(plane, x, y + 1, WALL_SOUTH) }
                2 -> { orBits(plane, x, y, WALL_EAST); orBits(plane, x + 1, y, WALL_WEST) }
                else -> { orBits(plane, x, y, WALL_SOUTH); orBits(plane, x, y - 1, WALL_NORTH) }
            }
            1, 3 -> when (rotation) {
                0 -> { orBits(plane, x, y, CORNER_NW); orBits(plane, x - 1, y + 1, CORNER_SE) }
                1 -> { orBits(plane, x, y, CORNER_NE); orBits(plane, x + 1, y + 1, CORNER_SW) }
                2 -> { orBits(plane, x, y, CORNER_SE); orBits(plane, x + 1, y - 1, CORNER_NW) }
                else -> { orBits(plane, x, y, CORNER_SW); orBits(plane, x - 1, y - 1, CORNER_NE) }
            }
            2 -> when (rotation) {
                0 -> {
                    orBits(plane, x, y, WALL_WEST or WALL_NORTH)
                    orBits(plane, x - 1, y, WALL_EAST); orBits(plane, x, y + 1, WALL_SOUTH)
                }
                1 -> {
                    orBits(plane, x, y, WALL_NORTH or WALL_EAST)
                    orBits(plane, x, y + 1, WALL_SOUTH); orBits(plane, x + 1, y, WALL_WEST)
                }
                2 -> {
                    orBits(plane, x, y, WALL_EAST or WALL_SOUTH)
                    orBits(plane, x + 1, y, WALL_WEST); orBits(plane, x, y - 1, WALL_NORTH)
                }
                else -> {
                    orBits(plane, x, y, WALL_SOUTH or WALL_WEST)
                    orBits(plane, x, y - 1, WALL_NORTH); orBits(plane, x - 1, y, WALL_EAST)
                }
            }
        }
    }

    /**
     * Whether a size-1 entity may step from [from] to the adjacent tile [to].
     *
     * Diagonals need three tiles clear: the destination and both orthogonal neighbours, so an
     * entity cannot slip through the corner where two walls meet.
     */
    fun checkWalkStep(from: TileLocation, to: TileLocation): Boolean {
        if (from.plane != to.plane) return false
        val dx = to.x - from.x
        val dy = to.y - from.y
        if (dx !in -1..1 || dy !in -1..1 || (dx == 0 && dy == 0)) return false
        val p = from.plane
        val x = from.x
        val y = from.y
        val m = mask(p, x + dx, y + dy)
        return when {
            dx == -1 && dy == 0 -> m and (BLOCKED or WALL_EAST) == 0
            dx == 1 && dy == 0 -> m and (BLOCKED or WALL_WEST) == 0
            dx == 0 && dy == -1 -> m and (BLOCKED or WALL_NORTH) == 0
            dx == 0 && dy == 1 -> m and (BLOCKED or WALL_SOUTH) == 0
            dx == -1 && dy == -1 ->
                m and (BLOCKED or WALL_NORTH or WALL_EAST or CORNER_NE) == 0 &&
                    mask(p, x - 1, y) and (BLOCKED or WALL_EAST) == 0 &&
                    mask(p, x, y - 1) and (BLOCKED or WALL_NORTH) == 0
            dx == 1 && dy == -1 ->
                m and (BLOCKED or WALL_NORTH or WALL_WEST or CORNER_NW) == 0 &&
                    mask(p, x + 1, y) and (BLOCKED or WALL_WEST) == 0 &&
                    mask(p, x, y - 1) and (BLOCKED or WALL_NORTH) == 0
            dx == -1 && dy == 1 ->
                m and (BLOCKED or WALL_EAST or WALL_SOUTH or CORNER_SE) == 0 &&
                    mask(p, x - 1, y) and (BLOCKED or WALL_EAST) == 0 &&
                    mask(p, x, y + 1) and (BLOCKED or WALL_SOUTH) == 0
            else ->
                m and (BLOCKED or WALL_SOUTH or WALL_WEST or CORNER_SW) == 0 &&
                    mask(p, x + 1, y) and (BLOCKED or WALL_WEST) == 0 &&
                    mask(p, x, y + 1) and (BLOCKED or WALL_SOUTH) == 0
        }
    }

    // ------------------------------------------------------------------ decoders

    /** Index 5 file 3: `jagx` + version 1, then plane-major 64 x 64 tile records. */
    internal fun decodeTerrain(buf: ByteBuf): Array<Array<IntArray>> {
        val magic = buf.readInt()
        val version = buf.readUnsignedByte().toInt()
        check(magic == JAGX && version == 1) { "Unsupported map settings header %08x/%d".format(magic, version) }
        val settings = Array(4) { Array(64) { IntArray(64) } }
        for (plane in 0..3) {
            for (x in 0..63) {
                for (y in 0..63) {
                    val flags = buf.readUnsignedByte().toInt()
                    // The tripwire: only the low four bits are ever set, so a desync shows up here
                    // instead of silently producing plausible-looking collision.
                    check(flags and 0xF.inv() == 0) { "Unsupported tile flags $flags" }
                    if (flags and 0x1 != 0) { buf.readUnsignedByte(); buf.readSmartShort() }
                    if (flags and 0x2 != 0) settings[plane][x][y] = buf.readUnsignedByte().toInt()
                    if (flags and 0x4 != 0) buf.readSmartShort()
                    if (flags and 0x8 != 0) buf.readUnsignedShort()
                }
            }
        }
        // A variable environment/lighting tail follows; it is render data, never fully consumed.
        return settings
    }

    /** Index 5 file 0. Consumes the buffer exactly; anything left over means the stream desynced. */
    internal fun decodeLocs(buf: ByteBuf, emit: (Int, Int, Int, Int, Int, Int) -> Unit) {
        var id = -1
        while (true) {
            val delta = readSmart2(buf)
            if (delta == 0) break
            id += delta
            var pos = 0
            while (true) {
                val step = buf.readSmartShort()
                if (step == 0) break
                pos += step - 1
                val plane = pos shr 12
                val lx = (pos shr 6) and 0x3F
                val ly = pos and 0x3F
                val attr = buf.readUnsignedByte().toInt()
                val shape = (attr shr 2) and 0x1F
                val rotation = attr and 3
                // The 0x80 transform block is mandatory. Skipping it desynchronises everything
                // after the first loc in most 950 map squares.
                if (attr and 0x80 != 0) skipTransform(buf)
                if (plane in 0..3 && lx in 0..63 && ly in 0..63) emit(id, lx, ly, plane, shape, rotation)
            }
        }
        check(!buf.isReadable) { "Trailing loc bytes: ${buf.readableBytes()}" }
    }

    private fun skipTransform(buf: ByteBuf) {
        val f = buf.readUnsignedByte().toInt()
        if (f and 0x01 != 0) buf.skipBytes(8)          // quaternion
        if (f and 0x02 != 0) buf.skipBytes(2)
        if (f and 0x04 != 0) buf.skipBytes(2)
        if (f and 0x08 != 0) buf.skipBytes(2)
        if (f and 0x10 != 0) {
            buf.skipBytes(2)                            // uniform scale, ends the block
        } else {
            if (f and 0x20 != 0) buf.skipBytes(2)
            if (f and 0x40 != 0) buf.skipBytes(2)
            if (f and 0x80 != 0) buf.skipBytes(2)
        }
    }

    private fun readSmart2(buf: ByteBuf): Int {
        var total = 0
        var v = buf.readSmartShort()
        while (v == 32767) {
            total += 32767
            v = buf.readSmartShort()
        }
        return total + v
    }

    private fun locType(id: Int): LocType? {
        if (locTypes.containsKey(id)) return locTypes.get(id)
        val decoded = try {
            val data = archiveFile(LOC_INDEX, id shr 8, id and 0xFF)
            if (data == null) null else decodeLocType(Unpooled.wrappedBuffer(data))
        } catch (e: Exception) {
            // 32 definitions carry an opcode-209 record nobody has decoded. Cache the failure so
            // the map square still loads; only those placements are dropped.
            null
        }
        locTypes.put(id, decoded)
        return decoded
    }

    /**
     * Index 16 loc definition. Only sizeX/sizeY/clipType/opcode-74 are kept, but every opcode's
     * payload must still be consumed exactly or the fields that follow are read from the wrong
     * offset. Validated over all 140252 definitions in the 950 cache.
     */
    internal fun decodeLocType(buf: ByteBuf): LocType {
        val t = LocType()
        while (true) {
            val op = buf.readUnsignedByte().toInt()
            if (op == 0) break
            when {
                op == 1 -> repeat(buf.readUnsignedByte().toInt()) {
                    buf.readByte(); repeat(buf.readUnsignedByte().toInt()) { readBigSmart(buf) }
                }
                op == 2 -> readString(buf)
                op == 14 -> t.sizeX = buf.readUnsignedByte().toInt()
                op == 15 -> t.sizeY = buf.readUnsignedByte().toInt()
                op == 17 -> t.clipType = 0
                op == 18 -> Unit
                op == 19 -> buf.readUnsignedByte()
                op == 21 || op == 22 || op == 23 -> Unit
                op == 24 -> readBigSmart(buf)
                op == 27 -> t.clipType = 1
                op == 28 -> buf.readUnsignedByte()
                op == 29 || op == 39 -> buf.readByte()
                op in 30..34 -> readString(buf)
                op == 40 || op == 41 -> repeat(buf.readUnsignedByte().toInt()) {
                    buf.readUnsignedShort(); buf.readUnsignedShort()
                }
                op == 42 -> buf.skipBytes(buf.readUnsignedByte().toInt())
                op == 44 || op == 45 -> buf.readUnsignedShort()
                op == 62 || op == 64 -> Unit
                op == 65 || op == 66 || op == 67 -> buf.readUnsignedShort()
                op == 69 -> buf.readUnsignedByte()
                op == 70 || op == 71 || op == 72 -> buf.readShort()
                op == 73 -> Unit
                op == 74 -> t.ignoreClipOnAlternativeRoute = true
                op == 75 -> buf.readUnsignedByte()
                op == 77 || op == 92 -> {
                    buf.readUnsignedShort(); buf.readUnsignedShort()
                    if (op == 92) readBigSmart(buf)
                    repeat(buf.readSmartShort() + 1) { readBigSmart(buf) }
                }
                op == 78 -> { buf.readUnsignedShort(); buf.readUnsignedByte() }
                op == 79 -> {
                    buf.readUnsignedShort(); buf.readUnsignedShort(); buf.readUnsignedByte()
                    repeat(buf.readSmartShort()) { buf.readUnsignedShort() }
                }
                op == 81 -> buf.readUnsignedByte()
                op == 82 || op == 88 || op == 89 || op == 91 -> Unit
                op == 93 -> buf.readUnsignedShort()
                op == 94 -> Unit
                op == 95 -> buf.readShort()
                op == 97 || op == 98 || op == 99 || op == 100 -> Unit
                op == 101 -> buf.readUnsignedByte()
                op == 102 -> buf.readUnsignedShort()
                op == 103 -> Unit
                op == 104 -> buf.readUnsignedByte()
                op == 105 -> Unit
                op == 106 -> repeat(buf.readUnsignedByte().toInt()) {
                    readBigSmart(buf); buf.readUnsignedByte()
                }
                op == 107 -> buf.readUnsignedShort()
                op in 108..111 -> Unit                  // 950 additions, no payload
                op in 150..154 -> readString(buf)
                op == 160 -> repeat(buf.readUnsignedByte().toInt()) { buf.readUnsignedShort() }
                op == 162 -> buf.readInt()
                op == 163 -> buf.skipBytes(4)
                op == 164 || op == 165 || op == 166 -> buf.readShort()
                op == 167 -> buf.readUnsignedShort()
                op == 168 || op == 169 -> Unit
                op == 170 || op == 171 -> readSmart(buf)
                op == 173 -> { buf.readUnsignedShort(); buf.readUnsignedShort() }
                op == 177 -> Unit
                op == 178 -> buf.readUnsignedByte()
                op == 186 -> buf.readUnsignedByte()
                op == 188 || op == 189 -> Unit
                op in 190..195 -> buf.readUnsignedShort()
                op == 196 || op == 197 -> buf.readUnsignedByte()
                op == 198 || op == 199 -> Unit
                op == 200 -> Unit
                op == 201 -> repeat(6) { buf.readSmartShort() }
                op == 202 -> buf.readUnsignedByte()
                op == 203 -> Unit
                op == 204 -> buf.skipBytes(buf.readUnsignedByte().toInt() * 27)
                op == 205 -> readModelMorphs(buf)
                op == 206 -> {
                    buf.readUnsignedByte(); buf.readUnsignedByte()
                    buf.skipBytes(buf.readUnsignedByte().toInt() * 37)
                }
                op == 207 || op == 208 -> {
                    buf.readMedium(); buf.readUnsignedShort()
                    if (op == 208) readBigSmart(buf)
                    repeat(buf.readSmartShort() + 1) { readBigSmart(buf) }
                }
                op == 249 -> repeat(buf.readUnsignedByte().toInt()) {
                    val isString = buf.readUnsignedByte().toInt() == 1
                    buf.readMedium()
                    if (isString) readString(buf) else buf.readInt()
                }
                op == 255 -> { buf.readUnsignedShort(); buf.readUnsignedShort(); buf.readUnsignedShort() }
                else -> throw IllegalArgumentException("Unsupported loc opcode $op")
            }
        }
        check(!buf.isReadable) { "Trailing loc definition bytes: ${buf.readableBytes()}" }
        return t
    }

    private fun readModelMorphs(buf: ByteBuf) {
        buf.readUnsignedShort(); buf.readUnsignedShort(); buf.readUnsignedShort()
        val flags = buf.readUnsignedByte().toInt()
        check(flags and 31.inv() == 0) { "Unsupported model morph flags $flags" }
        var bit = 1
        while (bit <= 16) {
            if (flags and bit != 0) {
                repeat(buf.readUnsignedByte().toInt()) {
                    buf.readUnsignedByte()
                    repeat(buf.readUnsignedByte().toInt()) {
                        buf.readUnsignedShort(); buf.readUnsignedShort()
                        if (bit <= 2) {
                            readBigSmart(buf)
                            if (bit == 1) {
                                val extras = buf.readUnsignedByte().toInt()
                                check(extras <= 3) { "Unsupported model morph extra count $extras" }
                                buf.skipBytes(extras)
                            }
                        } else {
                            buf.skipBytes(4)
                        }
                    }
                }
            }
            bit = bit shl 1
        }
    }

    private fun readBigSmart(buf: ByteBuf): Int {
        if (buf.getUnsignedByte(buf.readerIndex()).toInt() < 128) {
            val v = buf.readUnsignedShort()
            return if (v == 32767) -1 else v
        }
        return buf.readInt() and 0x7FFFFFFF
    }

    private fun readSmart(buf: ByteBuf): Int =
        if (buf.getUnsignedByte(buf.readerIndex()).toInt() < 128) buf.readUnsignedByte().toInt() - 64
        else buf.readUnsignedShort() - 49152

    private fun readString(buf: ByteBuf): String {
        val sb = StringBuilder()
        while (true) {
            val c = buf.readByte().toInt()
            if (c == 0) break
            sb.append(c.toChar())
        }
        return sb.toString()
    }
}
