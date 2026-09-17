package com.opennxt.model.entity.player.appearance

import com.opennxt.OpenNXT
import com.opennxt.model.world.Native950WorldBootstrap
import com.opennxt.model.entity.PlayerEntity
import com.opennxt.net.buf.DataType
import com.opennxt.net.buf.GamePacketBuilder
import com.opennxt.resources.FilesystemResources
import com.opennxt.resources.defaults.wearpos.WearposDefaults
import com.opennxt.util.MD5
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

class PlayerModel(val player: PlayerEntity) {
    /** The conversion map of appearance index -> equipment index */
    private val CONVERSION_MAP = intArrayOf(0, 1, 2, 3, 4, 5, -1, 7, -1, 9, 10, -1, -1, -1, -1, -1, -1, -1, 18)

    var gender = Gender.MALE
    var renderType = RenderType.PLAYER

    var npcId = -1
    var showSkillLevel = false // if the player has weapons sheated, this is true

    val idkit = intArrayOf(3, 14, 18, 26, 34, 38, 42)
    val colours = intArrayOf(3, 16, 16, 0, 0, 0, 0, 0, 0, 0)

    /** Both the 947 and 950 clients use 0x800 as the wearpos item-id base. */
    private val ITEM_BASE = 0x800

    var data = ByteArray(0)
    var hash = ByteArray(0)

    var dirty = true

    private fun ByteBuf.toByteArray(): ByteArray {
        val readable = readableBytes()
        val out = ByteArray(readable)
        readBytes(out)
        return out
    }

    fun refresh() {
        dirty = false

        val buf = Unpooled.buffer()
        val builder = GamePacketBuilder(buf)

        var flags = 0x0
        if (gender == Gender.FEMALE) flags = flags or 0x1
        if (showSkillLevel) flags = flags or 0x4
        builder.put(DataType.BYTE, flags)

        builder.put(DataType.BYTE, 0) // RenderType. TODO: Figure values out
        appendAppearance(builder)
        builder.putString(player.controllingPlayer?.name ?: "null")
        builder.put(DataType.BYTE, 3) // Combat level
        if (showSkillLevel) {
            builder.put(DataType.SHORT, 1000) // Total skill level
        } else {
            builder.put(DataType.BYTE, 0) // Combat level + summoning OR 0
            builder.put(DataType.BYTE, -1)
        }
        builder.put(DataType.BYTE, 0)

        this.data = buf.toByteArray()
        this.hash = MD5.hash(data)

        buf.release()
    }

    /**
     * Identity-kit base for a wearpos slot value.
     *
     * The 950 client subtracts 2 (0x140131A C6 sets [obj+8]=2, applied at 0x140131D78);
     * the 947 client subtracts 0x100 (0x140130E8A sets [obj+0]=0x100, applied at 0x140131130).
     */
    private fun kitBase(): Int = if (OpenNXT.config.build == 950) 2 else 0x100

    /**
     * Writes one wearpos slot value.
     *
     * 950 reads each slot as an unsigned LEB128 varint - 7 bits per byte, least significant
     * group first, continuing while the byte is above 0x7F (0x140131C60-0x140131C8B).
     * 947 reads a single 0 byte for "empty" or a big-endian unsigned short (0x140131027-0x140131064).
     * Both clients treat values at or above ITEM_BASE as an item id.
     */
    private fun putWearposSlot(builder: GamePacketBuilder, value: Int) {
        if (OpenNXT.config.build != 950) {
            if (value == 0) builder.put(DataType.BYTE, 0) else builder.put(DataType.SHORT, value)
            return
        }

        for (byte in Native950WorldBootstrap.encodeWearposSlot(value))
            builder.put(DataType.BYTE, byte.toInt() and 0xFF)
    }

    private fun getLookIndex(slot: Int): Int = when (slot) {
        4 -> 2
        6 -> 3
        7 -> 5
        8 -> 0
        9 -> 4
        10 -> 6
        11 -> 1
        else -> -1
    }

    private fun appendAppearance(builder: GamePacketBuilder) {
        val wearpos = FilesystemResources.instance.defaults.get<WearposDefaults>().slots

        for (index in 0 until wearpos.size) {
            if (wearpos[index] != 0) continue

            // TODO Items (Equipment)
            /*
            val item: Item? = when(CONVERSION_MAP[index]) {
                0 -> Equipment.SLOT_HEAD
                1 -> Equipment.SLOT_CAPE
                ....
            }
            if (item != null) {
                putWearposSlot(builder, ITEM_BASE + item.id)
                continue
            }
             */

            val lookIndex = getLookIndex(index)
            if (lookIndex != -1 && idkit[lookIndex] > 0) {
                putWearposSlot(builder, kitBase() + idkit[lookIndex])
                continue
            }

            putWearposSlot(builder, 0)
        }

        builder.put(DataType.SHORT, 0)

        for (i in colours)
            builder.put(DataType.BYTE, i)
        for (i in 0 until 10)
            builder.put(DataType.BYTE, 0)

        builder.put(DataType.SHORT, 2699)
    }
}