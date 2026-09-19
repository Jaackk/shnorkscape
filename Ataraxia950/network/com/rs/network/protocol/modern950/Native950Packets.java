package com.rs.network.protocol.modern950;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;
import java.util.function.IntSupplier;

import static com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;

/**
 * Verified native 947-3 packet bodies. Every method returns an unframed packet;
 * frame it exactly once on the owning connection's outbound event loop.
 * P6 RETIREMENT: the seven single-entity writers below are deprecated and no longer
 * reachable from the world. Every PLAYER_INFO and NPC_INFO byte a session sends now comes
 * from {@code Native950PlayerInfo} / {@code Native950NpcInfo} through
 * {@code Native950EntityFrames}; there is exactly one live encoder. They remain only as the
 * literal wire fixtures the verification tests pin, because those bytes are the evidence
 * the generalised encoders are checked against, and each requires one local player with
 * 2046 unchanged external slots or a local NPC list of exactly one.
 */
public final class Native950Packets {
    /** Native scene constructor0x1401177c9 fixes width256; independent of server interest radius. */
    public static final int SCENE_SIZE = 256;
    /** Native full logout; flush this frame before closing the world transport. */
    public static Packet logoutFull() { return packet(ServerPacket.LOGOUT_FULL, new byte[0]); }

    public static Packet tickEnd() { return packet(ServerPacket.SERVER_TICK_END, new byte[0]); }
    public static Packet keepAlive() { return packet(ServerPacket.NO_TIMEOUT, new byte[0]); }

    /** Completes one native permanent-client-variable upload batch. */
    public static Packet permanentVariablesAcknowledgement() {
        return packet(ServerPacket.PERMANENT_VARIABLES_ACKNOWLEDGEMENT, new byte[0]);
    }

    /**
     * Native 107 (947: 87): replace current streamed music with an index-40 resource, or stop
     * with -1. The resource is the paired cache's enum-1351 value, not its music-track key.
     *
     * 950: b0..b3 the id as a PLAIN big-endian u32, b4 = 128 - volume. 947 permuted the id bytes
     * as 1,0,3,2 and sent the volume unbiased, so neither field survives the revision unchanged.
     */
    public static Packet music(int archiveId, int volume) {
        if (archiveId < -1) throw new IllegalArgumentException("Music archive must be nonnegative or -1 to stop");
        if (volume < 0 || volume > 255) throw new IllegalArgumentException("Music volume must fit unsigned byte");
        return packet(ServerPacket.MUSIC, new byte[] {
                (byte) (archiveId >>> 24), (byte) (archiveId >>> 16),
                (byte) (archiveId >>> 8), (byte) archiveId, (byte) (128 - volume)
        });
    }

    /**
     * Selects one 8x8 zone relative to the current REBUILD_NORMAL scene origin. Deltas are
     * signed zone counts, not absolute tiles. Each following OBJ packet must use this zone.
     * Native 0x140141040 adds deltaX*8/deltaY*8 to the client's scene base and retains plane.
     */
    public static Packet zonePartialFollows(int zoneDeltaX, int zoneDeltaY, int plane) {
        if (zoneDeltaX < -128 || zoneDeltaX > 127 || zoneDeltaY < -128 || zoneDeltaY > 127)
            throw new IllegalArgumentException("Zone deltas must fit signed bytes");
        if (plane < 0 || plane > 3) throw new IllegalArgumentException("Plane must be 0..3");
        return packet(ServerPacket.UPDATE_ZONE_PARTIAL_FOLLOWS, new byte[] {
                (byte) zoneDeltaY, (byte) -zoneDeltaX, (byte) (plane + 128)
        });
    }

    /**
     * Adds one entry to the selected zone's tile pile. The native quantity is u16, not an int;
     * splitting or merging larger server stacks belongs to the ground-item service. This does
     * not merge an existing entry, even if the ID and tile match. Native parser 0x140140b80.
     */
    public static Packet groundItemAdd(int itemId, int amount, int offsetX, int offsetY) {
        groundItemId(itemId); groundItemAmount(amount);
        int offset = groundItemOffset(offsetX, offsetY);
        return packet(ServerPacket.OBJ_ADD, new byte[] {
                (byte) itemId, (byte) (itemId >>> 8), (byte) (itemId >>> 16),
                (byte) (128 - offset), (byte) (amount >>> 8), (byte) amount
        });
    }

    /** Removes the first matching ID at the tile, not every matching entry. */
    public static Packet groundItemRemove(int itemId, int offsetX, int offsetY) {
        groundItemId(itemId);
        int offset = groundItemOffset(offsetX, offsetY);
        return packet(ServerPacket.OBJ_DEL, new byte[] {
                (byte) (128 - offset), (byte) itemId, (byte) (itemId >>> 8), (byte) (itemId >>> 16)
        });
    }

    /**
     * Native 0x140115020 replaces the quantity on ALL entries matching both ID and oldAmount.
     * Use remove for deletion. Callers must avoid ambiguous equal-count duplicate entries.
     */
    public static Packet groundItemCount(int itemId, int oldAmount, int newAmount, int offsetX, int offsetY) {
        groundItemId(itemId); groundItemAmount(oldAmount); groundItemAmount(newAmount);
        int offset = groundItemOffset(offsetX, offsetY);
        return packet(ServerPacket.OBJ_COUNT, new byte[] {
                (byte) offset, (byte) (itemId >>> 16), (byte) (itemId >>> 8), (byte) itemId,
                (byte) (oldAmount >>> 8), (byte) oldAmount, (byte) (newAmount >>> 8), (byte) newAmount
        });
    }

    /**
     * Replaces the selected tile's object slot, or creates a new object there. Native opcode11
     * is variable-byte because bit7 of the shape field can append a map transform. This
     * ordinary writer leaves that bit clear; it does not emit an animation update (opcode75).
     * Parser0x1401130b0 queues operation2 with the ID in the object-definition field +0x4c.
     */
    public static Packet objectAdd(int objectId, int shape, int rotation, int offsetX, int offsetY) {
        return objectAdd(objectId, shape, rotation, offsetX, offsetY, null);
    }

    /** Preserves a cached map placement's flagged BE-short quaternion/translation/scale data. */
    public static Packet objectAdd(int objectId, int shape, int rotation, int offsetX, int offsetY, byte[] transform) {
        if (objectId < 0) throw new IllegalArgumentException("Object ID must be nonnegative");
        int info = objectInfo(shape, rotation);
        int offset = groundItemOffset(offsetX, offsetY);
        int extra = 0;
        if (transform != null) {
            if (transform.length == 0) throw new IllegalArgumentException("Empty object transform");
            int flags = transform[0] & 255;
            extra = 1 + ((flags & 1) != 0 ? 8 : 0) + 2 * Integer.bitCount(flags & 14)
                    + ((flags & 16) != 0 ? 2 : 2 * Integer.bitCount(flags & 224));
            if (transform.length != extra) throw new IllegalArgumentException("Truncated or trailing object transform");
            info |= 128;
        }
        byte[] body = new byte[6 + extra];
        body[0] = (byte) offset; body[1] = (byte) (objectId >>> 16); body[2] = (byte) (objectId >>> 24);
        body[3] = (byte) objectId; body[4] = (byte) (objectId >>> 8); body[5] = (byte) (info + 128);
        if (extra != 0) System.arraycopy(transform, 0, body, 6, extra);
        return packet(ServerPacket.LOC_ADD_CHANGE, body);
    }

    /** Removes the object occupying this shape's scene slot; no definition ID is on the wire. */
    public static Packet objectRemove(int shape, int rotation, int offsetX, int offsetY) {
        int info = objectInfo(shape, rotation);
        int offset = groundItemOffset(offsetX, offsetY);
        return packet(ServerPacket.LOC_DEL, new byte[] {(byte) (128 - offset), (byte) (info + 128)});
    }

    private static int objectInfo(int shape, int rotation) {
        // These are the four ordinary Region slots. Native shapes23/24 are internal secondary
        // wall parts generated by the parser for shapes2/8, never sent by this server.
        if (shape < 0 || shape > 22 || rotation < 0 || rotation > 3)
            throw new IllegalArgumentException("Object shape/rotation must be 0..22/0..3");
        return (shape << 2) | rotation;
    }

    private static void groundItemId(int itemId) {
        if (itemId < 0 || itemId >= 0xffffff)
            throw new IllegalArgumentException("Ground item ID must be a non-sentinel u24");
    }

    private static void groundItemAmount(int amount) {
        if (amount < 1 || amount > 65535)
            throw new IllegalArgumentException("Ground item amount must be 1..65535");
    }

    private static int groundItemOffset(int offsetX, int offsetY) {
        if (offsetX < 0 || offsetX > 7 || offsetY < 0 || offsetY > 7)
            throw new IllegalArgumentException("Ground item offsets must be inside the selected 8x8 zone");
        return (offsetX << 4) | offsetY;
    }

    public static Packet openTop(int interfaceId) {
        unsignedShort(interfaceId, "interfaceId");
        // 950: b0=id, b1=id>>>8 (plain little-endian), b2..b18 unread. 947 wrote the id one
        // byte later and biased the low byte by +128; both of those are gone.
        byte[] body = new byte[19];
        body[0] = (byte) interfaceId;
        body[1] = (byte) (interfaceId >>> 8);
        return packet(ServerPacket.IF_OPENTOP, body);
    }

    public static Packet openSub(int parentInterface, int component, int interfaceId, boolean walkable) {
        unsignedShort(parentInterface, "parentInterface");
        unsignedShort(component, "component");
        unsignedShort(interfaceId, "interfaceId");
        // 950: b0..3 parent as a PLAIN big-endian u32, b4..15 unread, b16=id+128, b17=id>>>8,
        // b18=128-walkable, b19..22 unread. 947 put the parent last under an intv1 permutation and
        // the id at b4..5 unbiased, so no field in this packet kept both its offset and transform.
        byte[] body = new byte[23];
        int parent = (parentInterface << 16) | component;
        body[0] = (byte) (parent >>> 24);
        body[1] = (byte) (parent >>> 16);
        body[2] = (byte) (parent >>> 8);
        body[3] = (byte) parent;
        body[16] = (byte) (interfaceId + 128);
        body[17] = (byte) (interfaceId >>> 8);
        body[18] = (byte) (128 - (walkable ? 1 : 0));
        return packet(ServerPacket.IF_OPENSUB, body);
    }

    public static Packet hideInterface(int parentInterface, int component, boolean hidden) {
        unsignedShort(parentInterface, "parentInterface");
        unsignedShort(component, "component");
        int parent = (parentInterface << 16) | component;
        // 950: b0..3 hash intv1, b4 flag plain. 947 led with the flag under a +128 bias and
        // followed with a plain little-endian int.
        return packet(ServerPacket.IF_SETHIDE, new byte[] {
            (byte) (parent >>> 8), (byte) parent, (byte) (parent >>> 24), (byte) (parent >>> 16),
            (byte) (hidden ? 1 : 0)
        });
    }

    /** Removes the subinterface attached at this parent/component hash. */
    public static Packet closeSub(int parentInterface, int component) {
        unsignedShort(parentInterface, "parentInterface"); unsignedShort(component, "component");
        int hash = (parentInterface << 16) | component;
        // 950: hash intv2 (b0=h>>>16, b1=h>>>24, b2=h, b3=h>>>8), not 947's plain little-endian.
        return packet(ServerPacket.IF_CLOSESUB, new byte[] {
                (byte) (hash >>> 16), (byte) (hash >>> 24), (byte) hash, (byte) (hash >>> 8)
        });
    }

    /** Native opcode 2: big-endian component hash followed by a terminated CP1252 string. */
    public static Packet interfaceText(int interfaceId, int component, String text) {
        unsignedShort(interfaceId, "interfaceId"); unsignedShort(component, "component");
        Objects.requireNonNull(text, "text");
        Writer out = new Writer();
        out.i32((interfaceId << 16) | component); out.string(text);
        return packet(ServerPacket.IF_SETTEXT, out.bytes());
    }

    /** Native 81: local player's chat head; parser reads hash bytes in order 1,0,3,2. */
    public static Packet interfacePlayerHead(int interfaceId, int component) {
        unsignedShort(interfaceId, "interfaceId"); unsignedShort(component, "component");
        int hash = (interfaceId << 16) | component;
        // 950: a plain big-endian u32. 947 applied the intv2 permutation.
        return packet(ServerPacket.IF_SETPLAYERHEAD, new byte[] {
                (byte) (hash >>> 24), (byte) (hash >>> 16), (byte) (hash >>> 8), (byte) hash
        });
    }

    /** Native opcode 105, plain server message with no sender names and integer parameter zero. */
    public static Packet gameMessage(int type, String message) {
        if (type < 0 || type > 32767) throw new IllegalArgumentException("Message type must fit unsigned smart");
        Objects.requireNonNull(message, "message");
        Writer out = new Writer();
        if (type < 128) out.u8(type); else out.u16(type + 32768);
        out.i32(0); out.u8(0); out.string(message);
        return packet(ServerPacket.MESSAGE_GAME, out.bytes());
    }

    /**
     * Native opcode 105 with the verified sender-name flag bits. The handler reads flag
     * bit 0 as "a sender string follows" and, only when bit 0 is set, bit 1 as "a second
     * sender name follows" (INVENTORY_INTERFACES.md, opcode 105). A null sender emits the
     * same bytes as {@link #gameMessage(int, String)}; a second name without a first is
     * rejected because the client would never read it.
     */
    public static Packet gameMessage(int type, String sender, String senderSecondName, String message) {
        if (type < 0 || type > 32767) throw new IllegalArgumentException("Message type must fit unsigned smart");
        Objects.requireNonNull(message, "message");
        if (sender == null && senderSecondName != null)
            throw new IllegalArgumentException("A second sender name requires a first sender name");
        int flags = (sender != null ? 1 : 0) | (senderSecondName != null ? 2 : 0);
        Writer out = new Writer();
        if (type < 128) out.u8(type); else out.u16(type + 32768);
        out.i32(0); out.u8(flags);
        if (sender != null) out.string(sender);
        if (senderSecondName != null) out.string(senderSecondName);
        out.string(message);
        return packet(ServerPacket.MESSAGE_GAME, out.bytes());
    }

    /** Updates just a varbit's defined bit range, preserving the parent variable's other bits. */
    public static Packet varbitSmall(int id, int value) {
        unsignedShort(id, "varbitId");
        if (value < 0 || value > 255) throw new IllegalArgumentException("Small varbit value must fit unsigned byte");
        // 950: value FIRST then the id little-endian. The value byte is unchanged - the plan
        // writes it as value-128 and 947 as value+128, which are the same byte modulo 256 - so
        // this row is a pure field reorder.
        return packet(ServerPacket.VARBIT_SMALL, new byte[] {
                (byte) (value + 128), (byte) id, (byte) (id >>> 8)
        });
    }

    /** Replaces a domain-zero player variable with its full signed 32-bit value. */
    public static Packet varp(int id, int value) {
        unsignedShort(id, "varpId");
        // 950: id ushortle128 then value intv1. 947 led with the value and ended with a plain
        // little-endian id.
        return packet(ServerPacket.VARP_LARGE, new byte[] {
                (byte) (id + 128), (byte) (id >>> 8),
                (byte) (value >>> 8), (byte) value, (byte) (value >>> 24), (byte) (value >>> 16)
        });
    }

    // ---------------------------------------------------------------------------------------
    // Native 947-3 variable, stat and scalar writers.
    //
    // Every method below is derived from an independently CONFIRMED evidence note under
    // OpenNXT/data/prot/947/generated/native947-3/verified (see PROTOCOL-S-SUMMARY.md).
    // Deliberately not implemented, because their layout or sink is only CANDIDATE, or they
    // were REFUTED: 175 TELEMETRY_GRID_ADD_ROW, 215 TELEMETRY_GRID_ADD_COLUMN, 170 VARP_LONG,
    // 161 CLIENT_SETVARC_LONG, 99 VARCLAN, 120 VARCLAN_ENABLE, 25 VARCLAN_DISABLE,
    // 154 / 213 CANDIDATE scalars, and the refuted 185 / 163 / 191 group-vector edits.
    // ---------------------------------------------------------------------------------------

    /**
     * Native opcode 92 (947: 66). The 950 parser is 0x140141290 and the wire order is
     * <b>-skill, -level, then experience big-endian</b> - the reverse of 947, which led with the
     * experience and ended with (128 - level) and (skill + 128). Plan appendix A4 derives it:
     * 0x1401412c1 negates byte 0 into the register that scales the 24-byte stat entry, 0x1401412cf
     * negates byte 1 into the value stored at entry+0x14, and 0x1401412e4..0x1401412f2 byte-swaps
     * bytes 2..5 as the experience.
     *
     * <p>This javadoc used to describe 947's order while the body below emitted 950's. Restoring
     * experience-first to match it would put the experience's top byte in the stat index, so the
     * client would overwrite an unrelated skill's entry and display a plausible wrong level -
     * which is the failure this paragraph now exists to prevent.
     *
     * <p>The experience field carries WHOLE experience points, not tenths. The parser stores
     * the wire int straight into the live stat entry (setter 0x140369C10, {@code entry+0xC}),
     * and the entry's flag byte at {@code +8} decides both the clamp and whether the level
     * scan divides by ten. The live table is the vector at {@code [[owner+0x198E0]+0x7618]};
     * exactly two instructions in the whole image write that field, 0x1400CCD60 (the null at
     * construction) and 0x1400CDF8E, and the entries the latter builds set {@code entry+8}
     * from the function's zero register at 0x1400CDF25, i.e. flag 0. A flag-zero entry is
     * clamped at 0x0BEBC200 = 200,000,000 and its base level is scanned from the RAW value
     * with no division (0x140369C49..0x140369C7E), so the wire units are whole points and the
     * ceiling enforced here is the client's own.
     *
     * <p>The "tenths, send xp * 10" conclusion in UPDATE_STAT.md is a mis-attribution: the
     * flag-byte-1 construction it cites (0x1402FE438) belongs to function 0x1402FE290, a
     * different stat table that is never stored at +0x7618. STAT_DEFINITIONS.md section 3b.1
     * corrects it, and the cache agrees - script 8489 compares the raw stored experience
     * (op_0517, no division) against the literal 200000000, and script 11849 resolves the
     * level through enum 716 / enum 10699, whose rows are whole experience.
     *
     * <p>The client performs no bounds check on the skill index.
     */
    public static Packet updateStat(int skill, int level, int experience) {
        if (skill < 0 || skill > 255) throw new IllegalArgumentException("Skill index must fit an unsigned byte");
        if (level < 0 || level > 255) throw new IllegalArgumentException("Stat level must fit an unsigned byte");
        if (experience < 0 || experience > 200000000)
            throw new IllegalArgumentException("Experience must fit the client clamp of 0..200000000 points");
        Writer out = new Writer();
        // 950 (parser 0x140141290): b0=-skill, b1=-level, b2..5 experience big-endian. 947 led
        // with the experience and ended with the level and skill; the whole packet is reversed.
        out.u8(-skill); out.u8(-level); out.i32(experience);
        return packet(ServerPacket.UPDATE_STAT, out.bytes());
    }

    /**
     * Native opcode 10 (vars/VARP_SMALL.md, parser 0x1401414A0). Little-endian id and a value
     * the client decodes as the negation of a signed wire byte, so only -128..127 is reachable.
     * Use {@link #varp(int, int)} (opcode 111) for the full 32-bit form.
     */
    public static Packet varpSmall(int id, int value) {
        unsignedShort(id, "varpId");
        if (value < -128 || value > 127)
            throw new IllegalArgumentException("Small varp value must fit a signed byte");
        // 950: b0 = value as a plain signed byte, b1 = id>>>8, b2 = id+128. 947 led with a
        // plain little-endian id and negated the value.
        return packet(ServerPacket.VARP_SMALL, new byte[] {
                (byte) value, (byte) (id >>> 8), (byte) (id + 128)
        });
    }

    /**
     * Native opcode 71 (vars/VARBIT_LARGE.md, parser 0x140140FF0). Big-endian id whose low byte
     * carries the +128 transform, then a little-endian 32-bit value. The varbit setter keeps the
     * old parent value when the number does not fit the definition's bit width.
     */
    public static Packet varbitLarge(int id, int value) {
        unsignedShort(id, "varbitId");
        // 950: b0..3 value big-endian, b4=id, b5=id>>>8. 947 led with a +128-biased big-endian
        // id and wrote the value little-endian, so both fields changed order AND endianness.
        return packet(ServerPacket.VARBIT_LARGE, new byte[] {
                (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value,
                (byte) id, (byte) (id >>> 8)
        });
    }

    /**
     * Native opcode 1 (vars/CLIENT_SETVARC_SMALL.md, parser 0x140140EE0). The value byte carries
     * the +128 transform and the id is little-endian, so only -128..127 is reachable.
     */
    public static Packet varcSmall(int id, int value) {
        unsignedShort(id, "varcId");
        if (value < -128 || value > 127)
            throw new IllegalArgumentException("Small varc value must fit a signed byte");
        // 950: b0=id, b1=id>>>8, b2=128-value. Note 128-value is NOT the same byte as 947's
        // value-128 except at value 0, so this row changes the transform as well as the order.
        return packet(ServerPacket.CLIENT_SETVARC_SMALL, new byte[] {
                (byte) id, (byte) (id >>> 8), (byte) (128 - value)
        });
    }

    /**
     * Native opcode 112 (vars/CLIENT_SETVARC_LARGE.md, parser 0x140140DC0). Big-endian 32-bit
     * value, then a little-endian id whose low byte carries the +128 transform.
     */
    public static Packet varcLarge(int id, int value) {
        unsignedShort(id, "varcId");
        // 950 (parser 0x140141DB0): id ushort128 then the value as (b2,b3,b4,b5) =
        // (v>>>16, v>>>24, v, v>>>8). 947 wrote a plain big-endian value first and the id last.
        return packet(ServerPacket.CLIENT_SETVARC_LARGE, new byte[] {
                (byte) (id >>> 8), (byte) (id + 128),
                (byte) (value >>> 16), (byte) (value >>> 24), (byte) value, (byte) (value >>> 8)
        });
    }

    /** Picks the shortest client-variable form the value fits; both produce the same stored int. */
    public static Packet varc(int id, int value) {
        return value >= -128 && value <= 127 ? varcSmall(id, value) : varcLarge(id, value);
    }

    /**
     * Native opcode 115 (vars/CLIENT_SETVARCBIT_SMALL.md, parser 0x140140B60). The value byte is
     * negated on the wire and the id is big-endian with the +128 low byte. The shared client
     * varbit setter 0x1400CC490 discards values below zero or wider than the definition, so this
     * writer accepts only 0..127; the caller must also respect the target bit width.
     * The symbolic name follows the standard RS3 convention; it is absent from the 946 list.
     */
    public static Packet varcBitSmall(int id, int value) {
        unsignedShort(id, "varcBitId");
        if (value < 0 || value > 127)
            throw new IllegalArgumentException("Small client varbit value must fit 0..127");
        // 950: b0=128-value, b1=id>>>8, b2=id. 947 negated the value and biased the id's low
        // byte by +128; the bias is gone and the value transform changed.
        return packet(ServerPacket.CLIENT_SETVARCBIT_SMALL, new byte[] {
                (byte) (128 - value), (byte) (id >>> 8), (byte) id
        });
    }

    /**
     * Native opcode 55 (vars/CLIENT_SETVARCBIT_LARGE.md, parser 0x140140A60). The value uses the
     * client's mixed intv1 order (b2 b3 b0 b1 of the logical big-endian int) and the id is
     * little-endian. Negative values are discarded by setter 0x1400CC490 and are rejected here.
     * The symbolic name follows the standard RS3 convention.
     */
    public static Packet varcBitLarge(int id, int value) {
        unsignedShort(id, "varcBitId");
        if (value < 0) throw new IllegalArgumentException("Client varbit value must be nonnegative");
        // 950: b0..3 value little-endian, b4=id>>>8, b5=id+128. 947 wrote the value under the
        // intv1 permutation and the id plain little-endian.
        return packet(ServerPacket.CLIENT_SETVARCBIT_LARGE, new byte[] {
                (byte) value, (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24),
                (byte) (id >>> 8), (byte) (id + 128)
        });
    }

    /**
     * Native opcodes 67 and 15 (vars/CLIENT_SETVARCSTR_SMALL.md parser 0x140140940,
     * vars/CLIENT_SETVARCSTR_LARGE.md parser 0x140140810). The two forms differ in field order
     * as well as in their length prefix, and 950 swapped both: the byte-length form now sends the
     * id first (big-endian, +128 on the low byte) and the short-length form sends the string first
     * followed by a PLAIN little-endian id. The shortest form that fits the encoded text is used.
     */
    public static Packet varcString(int id, String text) {
        unsignedShort(id, "varcStringId");
        Objects.requireNonNull(text, "text");
        byte[] value = terminatedString(text);
        if (value.length > 65533)
            throw new IllegalArgumentException("Client string variable is too long for an unsigned-short frame");
        Writer out = new Writer();
        if (value.length <= 253) {
            // 950 byte frame (opcode 30): id first, big-endian with +128 on the low byte, then
            // the terminated string. This is the exact reverse of 947's byte frame.
            out.u8(id >>> 8); out.u8(id + 128); out.raw(value);
            return packet(ServerPacket.CLIENT_SETVARCSTR_SMALL, out.bytes());
        }
        // 950 short frame (opcode 81): string first, then the id LITTLE-endian with NO +128 bias.
        // The two frames swapped field order between revisions and the short frame also dropped
        // the bias, which is why the size table cannot be carried over as a column here.
        out.raw(value); out.u8(id); out.u8(id >>> 8);
        return packet(ServerPacket.CLIENT_SETVARCSTR_LARGE, out.bytes());
    }

    /**
     * Native opcode 48 (vars/RESET_CLIENT_VARCACHE.md, parser 0x1401415C0). Empty body; clears
     * the cached server-variable maps at owner+0x19F98 and the changed-id set at owner+0x520E8.
     */
    public static Packet resetClientVarCache() {
        return packet(ServerPacket.RESET_CLIENT_VARCACHE, new byte[0]);
    }

    /**
     * Native opcode 116 (UPDATE_RUNENERGY.md, parser 0x1401064A0). One untransformed byte stored
     * at stats+0x18 and read back as a percentage by cs2 op 731, whose consumers compare it with
     * 100, 75, 40 and 25. The client itself range-checks nothing; this writer enforces 0..100.
     * The symbolic name is the convention derived from that consumer, not a recovered symbol.
     */
    public static Packet runEnergy(int percent) {
        if (percent < 0 || percent > 100)
            throw new IllegalArgumentException("Run energy must be a 0..100 percentage");
        return packet(ServerPacket.UPDATE_RUNENERGY, new byte[] {(byte) percent});
    }

    /**
     * Native opcode 108 (UPDATE_RUNWEIGHT.md, parser 0x140106400). Big-endian signed short in
     * kilograms, sign-extended by the client and formatted with one decimal by its cs2 readers.
     */
    public static Packet runWeight(int kilograms) {
        if (kilograms < -32768 || kilograms > 32767)
            throw new IllegalArgumentException("Run weight must fit a signed short");
        return packet(ServerPacket.UPDATE_RUNWEIGHT, new byte[] {(byte) (kilograms >> 8), (byte) kilograms});
    }

    /**
     * Native opcode 52 (UPDATE_REBOOT_TIMER.md, parser 0x14010A780). Big-endian unsigned short of
     * remaining server ticks; the client rescales it to frames and counts it down once per frame.
     * The symbolic name is a meaning-derived convention (system update countdown): no native
     * string or name table in this image ties the field to a reboot, only its per-frame decrement
     * at 0x1400CE912 and its cs2 getter, op 371.
     */
    public static Packet rebootTimer(int ticks) {
        unsignedShort(ticks, "rebootTicks");
        return packet(ServerPacket.UPDATE_REBOOT_TIMER, new byte[] {(byte) (ticks >>> 8), (byte) ticks});
    }

    private static byte[] terminatedString(String text) {
        Writer out = new Writer();
        out.string(text);
        return out.bytes();
    }

    /** Replaces the event settings on an inclusive slot range; -1 addresses the component itself. */
    public static Packet interfaceEvents(int interfaceId, int component, int firstSlot, int lastSlot, int settings) {
        unsignedShort(interfaceId, "interfaceId"); unsignedShort(component, "component");
        if (firstSlot < -1 || lastSlot > 65534 || firstSlot > lastSlot)
            throw new IllegalArgumentException("Interface slot range must be ordered within -1..65534");
        int hash = (interfaceId << 16) | component;
        Writer out = new Writer();
        // 950 order (parser 0x140107970): mask intv1, toSlot big-endian u16, fromSlot
        // ushort128, parent intle. Every one of the four fields moved, and the mask and the
        // parent swapped ends of the packet.
        out.u8(settings >>> 8); out.u8(settings); out.u8(settings >>> 24); out.u8(settings >>> 16);
        out.u16(lastSlot);
        out.u8(firstSlot >>> 8); out.u8(firstSlot + 128);
        out.u8(hash); out.u8(hash >>> 8); out.u8(hash >>> 16); out.u8(hash >>> 24);
        return packet(ServerPacket.IF_SETEVENTS, out.bytes());
    }

    /**
     * Native opcode 69: sequential container slots. Item IDs are logical IDs, with -1/0 for empty.
     * Only ordinary item ID/amount entries are supported; per-item parameter blocks are disabled.
     */
    public static Packet inventoryFull(int containerId, boolean secondary, int[] itemIds, int[] amounts) {
        unsignedShort(containerId, "containerId"); inventoryEntries(itemIds, amounts);
        if (itemIds.length > 65535) throw new IllegalArgumentException("Too many inventory slots");
        Writer out = new Writer();
        out.u16(containerId); out.u8(secondary ? 1 : 0); out.u16(itemIds.length);
        for (int i = 0; i < itemIds.length; i++) {
            // 950 widened the item field to a big-endian u24 (0x1400fd131). One extra byte per
            // slot, so a 947-width body desynchronises everything after the first item.
            out.u24(itemIds[i] + 1); inventoryAmount(out, amounts[i]);
        }
        return packet(ServerPacket.UPDATE_INV_FULL, out.bytes());
    }

    /** Native opcode 5: sparse slot updates. Empty slots carry only their smart index and zero item ID. */
    public static Packet inventorySlots(int containerId, boolean secondary, int[] slots, int[] itemIds, int[] amounts) {
        unsignedShort(containerId, "containerId"); inventoryEntries(itemIds, amounts);
        Objects.requireNonNull(slots, "slots");
        if (slots.length != itemIds.length) throw new IllegalArgumentException("Inventory arrays must have equal lengths");
        Writer out = new Writer();
        out.u16(containerId); out.u8(secondary ? 1 : 0);
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] < 0 || slots[i] > 32767) throw new IllegalArgumentException("Inventory slot must fit unsigned smart");
            if (slots[i] < 128) out.u8(slots[i]); else out.u16(slots[i] + 32768);
            out.u24(itemIds[i] + 1);
            if (itemIds[i] != -1) inventoryAmount(out, amounts[i]);
        }
        return packet(ServerPacket.UPDATE_INV_PARTIAL, out.bytes());
    }

    private static void inventoryEntries(int[] itemIds, int[] amounts) {
        Objects.requireNonNull(itemIds, "itemIds"); Objects.requireNonNull(amounts, "amounts");
        if (itemIds.length != amounts.length) throw new IllegalArgumentException("Inventory arrays must have equal lengths");
        for (int i = 0; i < itemIds.length; i++) {
            if (itemIds[i] < -1 || itemIds[i] > com.rs.game.player.client.Native950ItemCatalog.MAX_ITEM_ID || amounts[i] < 0
                    || (itemIds[i] == -1 ? amounts[i] != 0 : amounts[i] == 0))
                throw new IllegalArgumentException("Inventory entries require ID 0..16777214 and positive amount, or -1/0 for empty");
        }
    }

    private static void inventoryAmount(Writer out, int amount) {
        if (amount < 255) out.u8(amount); else { out.u8(255); out.i32(amount); }
    }

    /** Supports only the verified integer and CP1252 string argument forms. */
    public static Packet runClientScript(int script, Object... args) {
        if (script < 0) throw new IllegalArgumentException("script must be nonnegative");
        Objects.requireNonNull(args, "args");
        Writer out = new Writer();
        for (Object arg : args) {
            if (!(arg instanceof String) && !(arg instanceof Integer))
                throw new IllegalArgumentException("Client script arguments must be String or Integer");
            out.u8(arg instanceof String ? 's' : 'i');
        }
        out.u8(0);
        for (int i = args.length - 1; i >= 0; i--) {
            if (args[i] instanceof String) out.string((String) args[i]);
            else out.i32((Integer) args[i]);
        }
        out.i32(script);
        return packet(ServerPacket.RUNCLIENTSCRIPT, out.bytes());
    }

    /**
     * First scene only: initialize one local player and no visible external players.
     * Area/hash values must come from the selected cache's verified scene configuration.
     * The native header requires wire mapSize=5; it is not the scene's width in tiles.
     *
     * @deprecated P6: replaced by {@code Native950PlayerInfo.initialScene}; kept as a wire fixture.
     */
    @Deprecated
    public static Packet initialSinglePlayerScene(int playerIndex, int x, int y, int plane,
                                                 int npcBits, int areaType, int hash1, int hash2) {
        if (playerIndex < 1 || playerIndex > 2047) throw new IllegalArgumentException("playerIndex 1..2047 required");
        coordinate(x, "x"); coordinate(y, "y");
        if (plane < 0 || plane > 3) throw new IllegalArgumentException("plane 0..3 required");
        // 30 local bits + 2046 * 20 external bits, padded to a byte boundary.
        byte[] initial = new byte[5119];
        int tileHash = (plane << 28) | (x << 14) | y;
        putBits(initial, 0, 30, tileHash);
        // Viewport.init encodes absent slots as regionHash=0 and STATIONARY.id=-1.
        // The speed occupies the top two bits of each 20-bit external record.
        for (int slot = 0; slot < 2046; slot++) putBits(initial, 30 + slot * 20, 20, 0xC0000);
        Writer out = new Writer();
        out.raw(initial);
        out.raw(sceneHeader(x >>> 3, y >>> 3, npcBits, areaType, hash1, hash2));
        return packet(ServerPacket.REBUILD_NORMAL, out.bytes());
    }

    /** Subsequent rebuilds omit the initial player bit stream; caller owns visibility state. */
    public static Packet rebuildScene(int chunkX, int chunkY, int npcBits, int areaType, int hash1, int hash2) {
        return packet(ServerPacket.REBUILD_NORMAL, sceneHeader(chunkX, chunkY, npcBits, areaType, hash1, hash2));
    }

    private static byte[] sceneHeader(int chunkX, int chunkY, int npcBits, int areaType, int hash1, int hash2) {
        if (chunkX < 0 || chunkX > 2047 || chunkY < 0 || chunkY > 2047)
            throw new IllegalArgumentException("Scene chunk coordinates must fit the 14-bit world");
        if (npcBits < 0 || npcBits > 255) throw new IllegalArgumentException("npcBits must fit a byte");
        unsignedShort(areaType, "areaType");
        // The 950 parser at 0x1400f6e50 reads a different order from 947's, not a permutation of
        // the same fields:
        //   b0..b1   chunkY, big-endian with +128 on the low byte   (0x1400f6e60)
        //   b2       mapSize, plain; anything but 5 aborts the packet (0x1400f6f8e) AND skips the
        //            GPI gate-byte clear at 0x1400f7189, so the NEXT rebuild is mis-parsed too
        //   b3       npcBits, +128                                   (0x1400f6e97)
        //   b4..b5   skipped, never read                             (0x1400f6e93)
        //   b6..b7   chunkX, plain big-endian                        (0x1400f6ea2)
        // Bytes 6..7 land in scene slot 0, the axis bounds-checked against +0x14034 and fed by
        // hash1's high 14 bits, so slot 0 is X. The axes are not interchangeable.
        Writer out = new Writer();
        out.u8(chunkY >>> 8); out.u8(chunkY + 128);
        out.u8(5);
        out.u8(npcBits + 128);
        out.u8(0); out.u8(0);
        out.u16(chunkX);
        out.u16(areaType); out.i32(hash1); out.i32(hash2);
        return out.bytes();
    }

    /**
     * @deprecated P6: replaced by the appearance block {@code Native950PlayerInfo} emits; kept as a wire fixture.
     */
    @Deprecated
    public static Packet singlePlayerAppearance(byte[] appearance) {
        Objects.requireNonNull(appearance, "appearance");
        if (appearance.length < 1 || appearance.length > 255)
            throw new IllegalArgumentException("Appearance requires 1..255 bytes");
        Writer out = new Writer();
        // Mask 0x20 on 950 (0x14012DC31), and the body is biased b ^ 0x80 because the client's
        // copy adds 0x80 to every byte (transform script 0x02 at .rdata 0x140B5FEC9).
        out.raw(new byte[] {(byte) 0xC0, 0x7F, (byte) 0xF4, 0, 0, 0x20, (byte) (appearance.length + 128)});
        byte[] biased = new byte[appearance.length];
        for (int index = 0; index < biased.length; index++)
            biased[index] = (byte) (appearance[index] ^ 0x80);
        out.raw(biased);
        return packet(ServerPacket.PLAYER_INFO, out.bytes());
    }

    /**
     * @deprecated P6: replaced by {@code Native950PlayerInfo.frame}; kept as a wire fixture.
     */
    @Deprecated
    public static Packet singlePlayerIdle() {
        return packet(ServerPacket.PLAYER_INFO, new byte[] {0, 0x7F, (byte) 0xF4});
    }

    /**
     * Replaces the local NPC list with one stationary NPC, without update masks.
     *
     * <p>Native 950 opcode 80 reads the addition record as index16, dy[npcBits],
     * immediate1, facing3, type16, plane2, mask1, dx[npcBits] (950 0x14011fd20,
     * bit reads at 950 0x14011fe08 and 0x1401200aa..0x14012017d; plan A7).
     * <b>947 opcode 12 read index16, dy, plane2, dx, type16, immediate1, facing3,
     * mask1</b> - same total width, so the 947 order still frames but silently
     * places every NPC wrong. {@code Native950NpcInfo.addRecord} carries the full
     * derivation and the failure analysis; this writer only mirrors it.
     *
     * <p>npcBits must match the most recent scene header. Offsets are from the
     * local player's position after this tick's movement. A zero retained count
     * safely replaces the old list on scene rebuilds too.
     *
     * @deprecated P6: replaced by the addition record {@code Native950NpcInfo} writes; kept as a wire fixture.
     */
    @Deprecated
    public static Packet staticNpcAdd(int index, int typeId, int dx, int dy, int plane,
                                      int npcBits, int facing) {
        if (index < 0 || index >= 65535) throw new IllegalArgumentException("NPC index must fit 0..65534");
        unsignedShort(typeId, "NPC typeId");
        if (npcBits < 1 || npcBits > 15) throw new IllegalArgumentException("NPC offset width must fit 1..15 bits");
        int limit = 1 << (npcBits - 1);
        if (dx < -limit || dx >= limit || dy < -limit || dy >= limit)
            throw new IllegalArgumentException("NPC offsets exceed the scene's signed bit width");
        if (plane < 0 || plane > 3) throw new IllegalArgumentException("NPC plane must fit 0..3");
        if (facing < 0 || facing > 7) throw new IllegalArgumentException("NPC facing must fit 0..7");
        // 8 count + 16 index + two offsets + 2 plane + 16 type + 5 flags + 16 sentinel.
        // The 950 reorder did not change the total, so the length formula is untouched.
        byte[] body = new byte[(63 + npcBits * 2 + 7) / 8];
        int offset = 8;
        putBits(body, offset, 16, index); offset += 16;
        putBits(body, offset, npcBits, dy); offset += npcBits;
        putBits(body, offset, 1, 1); offset++; // Position immediately, including re-adds.
        putBits(body, offset, 3, facing); offset += 3;
        putBits(body, offset, 16, typeId); offset += 16;
        putBits(body, offset, 2, plane); offset += 2;
        offset++; // No update mask, therefore no byte-aligned mask block.
        putBits(body, offset, npcBits, dx); offset += npcBits;
        putBits(body, offset, 16, 65535);
        return packet(ServerPacket.NPC_INFO, body);
    }

    /**
     * Requires exactly one NPC in the client's local list; keeps its existing state.
     * Retained count 1 then a clear changed bit (950 0x14011f8c9 and 0x14011f9d6).
     * The retained section did not change at 950 - only NPC_INFO's opcode moved,
     * 12 to 80 - so this body is byte identical to the 947 one.
     *
     * @deprecated P6: replaced by the retained list {@code Native950NpcInfo} writes; kept as a wire fixture.
     */
    @Deprecated
    public static Packet singleNpcRetain() {
        return packet(ServerPacket.NPC_INFO, new byte[] {1, 0});
    }

    /**
     * Zero retained NPCs and no additions removes the previous local NPC list: the
     * client resets its count at 950 0x14011f90d and re-appends nothing. No addition
     * terminator is needed because fewer than sixteen bits remain (950 0x14011fd4e).
     * Byte identical to the 947 body; only the opcode moved, 12 to 80.
     *
     * @deprecated P6: replaced by the retained list {@code Native950NpcInfo} writes; kept as a wire fixture.
     */
    @Deprecated
    public static Packet singleNpcRemove() {
        return packet(ServerPacket.NPC_INFO, new byte[] {0});
    }

    /**
     * Emits an already accepted adjacent step; this method does not route or bypass collision.
     *
     * @deprecated P6: replaced by the local movement {@code Native950PlayerInfo} writes; kept as a wire fixture.
     */
    @Deprecated
    public static Packet singlePlayerWalkStep(int dx, int dy) {
        if (dx < -1 || dx > 1 || dy < -1 || dy > 1 || (dx == 0 && dy == 0))
            throw new IllegalArgumentException("One adjacent walk step required");
        int relative = (2 << 12) | ((dx & 31) << 5) | (dy & 31);
        int bits = (0b10110 << 15) | relative;
        return packet(ServerPacket.PLAYER_INFO, new byte[] {
            (byte) (bits >>> 12), (byte) (bits >>> 4), (byte) (bits << 4), 0x7F, (byte) 0xF4
        });
    }

    /**
     * One accepted adjacent step and its appearance mask in the same PLAYER_INFO frame.
     *
     * @deprecated P6: replaced by movement plus the appearance block in one {@code Native950PlayerInfo} frame.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static Packet singlePlayerWalkStep(int dx, int dy, byte[] appearance) {
        Objects.requireNonNull(appearance, "appearance");
        if (appearance.length < 1 || appearance.length > 255)
            throw new IllegalArgumentException("Appearance requires 1..255 bytes");
        byte[] movement = singlePlayerWalkStep(dx, dy).payload();
        // The bit after local-update marks the pending mask (native 0x1401258A7..0x1401258C0).
        movement[0] |= 0x40;
        Writer out = new Writer();
        out.raw(movement);
        // Delegated rather than open-coded. This used to write {0, 0, 4, len+128} and the raw
        // appearance - 947's mask bit and an unbiased body - while singlePlayerAppearance next
        // door had already moved to 0x20 and b ^ 0x80. Two hand-written copies of one block is
        // how that drift happened, so there is now one: the mask encoder's own.
        out.raw(Native950PlayerMasks.encodeWithSkippedPrefix(
                Native950PlayerMasks.builder().appearance(appearance).build()));
        return packet(ServerPacket.PLAYER_INFO, out.bytes());
    }

    /** The caller may provide a separately verified body for a known server packet. */
    public static Packet packet(ServerPacket type, byte[] payload) {
        Objects.requireNonNull(type, "type"); Objects.requireNonNull(payload, "payload");
        if (type.size() >= 0 && payload.length != type.size())
            throw new IllegalArgumentException(type + " requires " + type.size() + " bytes");
        int maximum = type.size() == -1 ? 255 : 65535;
        if (payload.length > maximum) throw new IllegalArgumentException(type + " payload too long");
        return new Packet(type, payload.clone());
    }

    public static final class Packet {
        private final ServerPacket type;
        private final byte[] payload;
        private Packet(ServerPacket type, byte[] payload) { this.type = type; this.payload = payload; }
        public ServerPacket type() { return type; }
        public byte[] payload() { return payload.clone(); }
        /** Caller serializes use of its outgoing cipher, preserving order with the network write. */
        public byte[] frame(IntSupplier outgoingCipher) {
            Objects.requireNonNull(outgoingCipher, "outgoing ISAAC supplier");
            Writer out = new Writer();
            int opcode = type.opcode();
            if (opcode >= 128) out.u8((opcode >>> 8) + 128 + outgoingCipher.getAsInt());
            out.u8(opcode + outgoingCipher.getAsInt());
            if (type.size() == -1) out.u8(payload.length);
            else if (type.size() == -2) out.u16(payload.length);
            out.raw(payload);
            return out.bytes();
        }
    }

    private static void coordinate(int n, String field) {
        if (n < 0 || n > 16383) throw new IllegalArgumentException(field + " must fit 14 bits");
    }
    private static void unsignedShort(int n, String field) {
        if (n < 0 || n > 65535) throw new IllegalArgumentException(field + " must fit an unsigned short");
    }
    private static void putBits(byte[] out, int offset, int count, int value) {
        for (int bit = count - 1; bit >= 0; bit--, offset++)
            if (((value >>> bit) & 1) != 0) out[offset >>> 3] |= 1 << (7 - (offset & 7));
    }

    private static final class Writer {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        void u8(int n) { out.write(n); }
        void u16(int n) { u8(n >>> 8); u8(n); }
        void u24(int n) { u8(n >>> 16); u8(n >>> 8); u8(n); }
        void i32(int n) { u8(n >>> 24); u8(n >>> 16); u8(n >>> 8); u8(n); }
        void raw(byte[] bytes) { out.write(bytes, 0, bytes.length); }
        void string(String value) {
            if (value.indexOf('\0') >= 0) throw new IllegalArgumentException("Embedded NUL in client string");
            try {
                ByteBuffer encoded = Charset.forName("windows-1252").newEncoder()
                        .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                        .encode(CharBuffer.wrap(value));
                while (encoded.hasRemaining()) u8(encoded.get());
                u8(0);
            } catch (CharacterCodingException ex) {
                throw new IllegalArgumentException("Client string is not representable in CP1252", ex);
            }
        }
        byte[] bytes() { return out.toByteArray(); }
    }

    private Native950Packets() { }
}
