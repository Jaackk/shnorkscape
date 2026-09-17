package com.rs.game.player.client;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable appearance template for the selected 947 cache and its three verified bronze items.
 * This deliberately does not invoke the legacy player-appearance serializer or item decoder.
 */
public final class Native950Appearance {
    public static final int SLOT_COUNT = 19;
    private static final int[] VERIFIED_WEAR_POSITIONS =
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0};
    private static final int[] BODY_KITS =
            {-1, -1, -1, -1, 18, -1, 26, 38, 3, 34, 42, 14, -1, -1, -1, -1, -1, -1, -1};
    private static final int[] COLOURS = {3, 16, 16, 0, 0, 0, 0, 0, 0, 0};
    private final int[] wearPositions;

    public Native950Appearance(int[] wearPositions) {
        Objects.requireNonNull(wearPositions, "wearPositions");
        if (!Arrays.equals(VERIFIED_WEAR_POSITIONS, wearPositions))
            throw new IllegalArgumentException("The selected cache has unverified 947 wear positions");
        this.wearPositions = wearPositions.clone();
    }

    /** Returns a detached body suitable for the native PLAYER_INFO appearance mask. */
    public byte[] encode(String username, int[] equipmentIds) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(equipmentIds, "equipmentIds");
        if (username.isEmpty() || username.length() > 12)
            throw new IllegalArgumentException("Appearance name requires 1..12 ASCII characters");
        for (int i = 0; i < username.length(); i++) {
            char character = username.charAt(i);
            if (character < 32 || character > 126)
                throw new IllegalArgumentException("Appearance name requires printable ASCII characters");
        }
        if (equipmentIds.length != SLOT_COUNT)
            throw new IllegalArgumentException("Appearance equipment requires 19 slots");
        for (int slot = 0; slot < equipmentIds.length; slot++) {
            int id = equipmentIds[slot];
            if (id != -1 && !(slot == 0 && id == 1139)
                    && !(slot == 3 && id == 1277) && !(slot == 5 && id == 1173))
                throw new IllegalArgumentException("Unverified appearance item " + id + " in slot " + slot);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(80);
        out.write(0); // Male, no titles, combat-level display.
        out.write(0); // Visible player render type.
        for (int slot = 0; slot < wearPositions.length; slot++) {
            if (wearPositions[slot] == 1) continue;
            int item = equipmentIds[slot];
            if (item >= 0) {
                out.write(wearposSlot(ITEM_BASE + item), 0, wearposSlot(ITEM_BASE + item).length);
            } else if (BODY_KITS[slot] >= 0 && !(slot == 8 && equipmentIds[0] == 1139)) {
                // Med helm opcode 14 hides the hair slot (8), leaving beard slot 11 visible.
                out.write(wearposSlot(KIT_BASE + BODY_KITS[slot]), 0, wearposSlot(KIT_BASE + BODY_KITS[slot]).length);
            } else {
                out.write(wearposSlot(0), 0, wearposSlot(0).length);
            }
        }
        unsignedShort(out, 0); // No item model/recolour customization blocks.
        for (int colour : COLOURS) out.write(colour);
        for (int i = 0; i < 10; i++) out.write(0);
        // Sword parameter 644 in the selected modern item definition identifies BAS 2584.
        unsignedShort(out, equipmentIds[3] == 1277 ? 2584 : 2699);
        byte[] name = username.getBytes(StandardCharsets.US_ASCII);
        out.write(name, 0, name.length);
        out.write(0);
        out.write(3); // Combat level.
        out.write(0);
        out.write(255);
        out.write(0); // No optional appearance tail.
        return out.toByteArray();
    }


    /** Identity-kit base. 947 used 0x100; the 950 constructor writes 2 at 0x140131AC6. */
    public static final int KIT_BASE = 2;
    /** Item base. Unchanged between revisions; the 950 constructor writes 0x800 at 0x140131ACD. */
    public static final int ITEM_BASE = 0x800;

    /**
     * Encodes one wearpos slot value the way the 950 client reads it.
     *
     * <p>950 reads each slot as an unsigned LEB128 varint. The loop is 0x140131C60..0x140131C8B and
     * decodes exactly as written here: {@code and eax,0x7f} takes seven bits, {@code shl rax,cl}
     * with {@code add ecx,7} places each group least-significant first, and {@code cmp rdx,0x7f} /
     * {@code ja} continues while the byte read is above 0x7F. 947 read a single 0 byte for an empty
     * slot or a big-endian unsigned short otherwise, so the two encodings are not interchangeable.
     *
     * <p>The four classification constants are not inferred. The 950 constructor at 0x140131A70
     * writes each of them into the reader's state block, and the decode loop compares against those
     * fields:
     * <ul>
     *   <li>{@code [+0] = 0} (0x140131A86), tested at 0x140131C8D - an empty slot;
     *   <li>{@code [+4] = 1} (0x140131ABF), tested at 0x140131CB8 and gated on the slot index being
     *       0 - the npc-morph escape, which replaces 947's 0xFFFF sentinel;
     *   <li>{@code [+8] = 2} (0x140131AC6), subtracted at 0x140131D78 - the identity-kit base;
     *   <li>{@code [+0xc] = 0x800} (0x140131ACD), compared at 0x140131CCD and subtracted at
     *       0x140131CD6 - the item base, and the threshold between the kit and item branches.
     * </ul>
     *
     * <p><b>Why a wrong encoding crashes rather than degrades.</b> A 947-width body desynchronises
     * the slot loop, and a decoded value at or above 0x800 takes the item branch at 0x140131D39.
     * That branch calls the definition lookup at 0x140131D4A and then reads {@code [rax + 0x218]}
     * at 0x140131D53 with no null check on the returned pointer, so an id the cache does not define
     * faults the process outright. That is the crash this port was chasing, and the reason this
     * encoder carries byte-exact unit vectors rather than a round-trip test.
     */
    public static byte[] wearposSlot(int value) {
        if (value < 0) throw new IllegalArgumentException("Wearpos slot value must not be negative: " + value);
        int remaining = value;
        int length = 1;
        while (remaining >= 0x80) { remaining >>>= 7; length++; }
        byte[] out = new byte[length];
        remaining = value;
        for (int index = 0; index < length - 1; index++) {
            out[index] = (byte) ((remaining & 0x7F) | 0x80);
            remaining >>>= 7;
        }
        out[length - 1] = (byte) remaining;
        return out;
    }

    private static void unsignedShort(ByteArrayOutputStream out, int value) {
        out.write(value >>> 8);
        out.write(value);
    }
}
