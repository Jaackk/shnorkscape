package com.rs.network.protocol.modern950;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;

/**
 * Retained-entry movement forms and update-mask blocks of native 950 NPC_INFO
 * (server opcode 80, size -2).
 *
 * <p><b>All addresses below are 950 addresses</b> unless a comment says "947:".
 * The two revisions' code ranges overlap, so a 947 VA disassembles cleanly in the
 * 950 binary and lands on unrelated code; every VA here was re-read out of
 * {@code OpenNXT/data/clients/950/win64/original/rs2client.exe}. The 950 chain is:
 * NPC_INFO outer reader -&gt; addition records {@code 0x14011FD20} -&gt; per-pending-NPC
 * dispatch at {@code 0x14011F5DB} (which skips two bytes, {@code add qword
 * [rsi+0x18], 2}) -&gt; mask parser {@code 0x1401205E0}, whose first act is the
 * variable-length header reader {@code 0x140120540}.
 *
 * <p><b>How the byte transforms were derived.</b> The 950 client does not inline its
 * field decoders; it drives them from a per-block <i>selector table</i> in .rdata.
 * The reader object holds the payload base at {@code +0x10}, the byte cursor at
 * {@code +0x18} and a <i>selector cursor</i> at {@code +0x28}. Before each mask block
 * the parser stores that block's table into {@code +0x28}; every read then consumes
 * one selector byte and advances. The decoders and their selector meanings, read out
 * of the binary:
 * <ul>
 *   <li>byte {@code 0x14010D850} / {@code 0x14010D8E0} - 0: plain; 1: wire is
 *       {@code value+128}; 2: wire is {@code -value}; 3: wire is {@code 128-value}.</li>
 *   <li>u16 {@code 0x14010D970} - 0: big endian; 1: little endian;
 *       2: {@code ushort128} = [v&gt;&gt;&gt;8, v+128]; 3: {@code ushortle128} = [v+128, v&gt;&gt;&gt;8].</li>
 *   <li>i32 {@code 0x14010DD10} - 0: {@code intbe}; 1: {@code intle}; 2: {@code intv1};
 *       3: {@code intv2}.</li>
 *   <li>{@code smart2or4null} {@code 0x1400FEEA0} and the NUL-terminated string reader
 *       {@code 0x140100A90} consume <i>no</i> selector, so their encodings cannot move.
 *       {@code 0x1400FEEA0} is instruction-identical to 947's {@code 0x1401003F0}.</li>
 * </ul>
 * A selector of 4 or more returns zero <i>without consuming a payload byte</i>, which is
 * why a mis-numbered mask bit is not a loud failure: it silently changes how many bytes
 * the client eats.
 *
 * <p><b>Bits moved between revisions and are NOT a relabelling.</b> Derived here:
 * SAY 947 bit 0 -&gt; <b>6</b>, TRANSFORM 947 bit 5 -&gt; <b>2</b>, FACE_COORD 947 bit 3 -&gt;
 * <b>7</b>, FORCE_MOVEMENT 947 bit 12 -&gt; <b>14</b>, NAME <b>18</b> and COLOUR_TINT
 * <b>28</b> unmoved. Every one of those 947 bits means something else on 950, so no
 * block's 950 bit may be inferred from its 947 value.
 *
 * <p><b>Consumption order changed completely.</b> The client parses positionally, in the
 * order its tests appear in {@code 0x1401205E0}, which is neither bit order nor 947's
 * order. For the derived blocks that order is: say ({@code 0x140120656}), colourTint
 * ({@code 0x140120A01}), transform ({@code 0x140120AA2}), forceMovement
 * ({@code 0x140120AEA}), name ({@code 0x140121067}), faceCoordinate
 * ({@code 0x140121217}), followed by spotanims, animation and hits in the tail.
 * FaceEntity precedes faceCoordinate. 947 emitted animation, forceMovement, colourTint,
 * faceCoordinate, transform, say, name.
 *
 * <p>A caller composes one NPC_INFO body in exactly this order:
 *
 * <ol>
 *   <li><b>Retained list.</b> {@link #retainedCount(BitWriter, int)} writes the
 *       unsigned 8-bit count (950 {@code 0x14011F8C4}/{@code 0x14011F8C9}), then one
 *       entry per retained NPC in the client's existing list order:
 *       {@link #retainedEntry(BitWriter)}, {@link #retainedMaskOnly(BitWriter)},
 *       {@link #retainedWalk}, {@link #retainedStep}, {@link #retainedRun} or
 *       {@link #retainedRemove(BitWriter)}. A count larger than the client's previous
 *       count is invalid; a smaller count removes the trailing NPCs.</li>
 *   <li><b>Additions.</b> Each added NPC uses the {@code Native950NpcInfo.addRecord}
 *       layout, which is <b>reordered</b> in 950 (plan A7). This class does not
 *       duplicate that writer.</li>
 *   <li><b>Addition sentinel.</b> {@link #additionsEnd(BitWriter)} writes the 16-bit
 *       65535 terminator, tested at 950 {@code 0x14011FE14} ({@code cmp eax, 0xffff})
 *       on the index read at {@code 0x14011FE08}.</li>
 *   <li><b>Byte alignment.</b> {@link BitWriter#align()} rounds the bit cursor up to
 *       the next byte.</li>
 *   <li><b>Mask section.</b> One {@link #maskBlock(Update)} per NPC that asked for an
 *       update, in pending-list order: retained-list order first, then addition order.
 *       Each block starts with the two bytes the client skips at 950
 *       {@code 0x14011F5DB} and is self-delimiting; a wrong block length
 *       desynchronises the rest of the packet, not just that NPC.</li>
 * </ol>
 *
 * <p>Unimplemented blocks remain fenced. The complete tail of the parser must be
 * inspected with linear ranges: the bounded CFG helper originally missed the register
 * change from r12 to rsi and incorrectly reported animation bit 3 and hits bit 5 absent.
 * Animation is tested at 0x1401232C1; ordinary hits at 0x140123836.
 */
public final class Native950NpcMasks {

    /**
     * Mask bit 6 (947: bit 0). Forced overhead speech: one NUL-terminated CP1252 string,
     * read at 950 {@code 0x140120679} through the selector-free string reader
     * {@code 0x140100A90}, then handed to the NPC's virtual slot {@code +0x150} with
     * {@code r8d=0, r9d=0} - the identical sink and identical argument set as 947's
     * {@code 0x1401226B1}. This is the first block in 950 consumption order.
     */
    public static final long MASK_SAY = 0x40L; // 947: 0x1

    /**
     * Mask bit 2 (947: bit 5). NPC type change: one {@code smart2or4null} id read at 950
     * {@code 0x140120AD3}, handed to the definition setter at virtual slot {@code 0x218}
     * ({@code 0x140120ACC}..{@code 0x140120ADD}) - the same setter the addition record
     * uses. The reader consumes no selector and is instruction-identical to 947's, so the
     * block's bytes are unchanged; only the bit moved.
     */
    public static final long MASK_TRANSFORM = 0x4L; // 947: 0x20

    /**
     * Mask bit 7 (947: bit 3). Face coordinate. 950 tests it as {@code test r12b, r12b;
     * jns} at {@code 0x140121217} - a sign-bit test, not an immediate - which is why a
     * naive scan for {@code test r12b, 0x80} finds nothing. The sink is unchanged: both
     * shorts decode as {@code (v-1)>>1} into actor {@code +0x22C} / {@code +0x234}
     * ({@code 0x14012123B}..{@code 0x140121263}).
     */
    public static final long MASK_FACE_COORD = 0x80L; // 947: 0x8

    /**
     * Mask bit 14 (947: bit 12). Force movement: six signed bytes then three shorts, the
     * last converted to an angle by {@code 0x1407474B0} (still a 14-bit field - it does
     * {@code and eax, 0x3fff}), into the eleven-argument sink {@code 0x14031DAE0} at
     * {@code 0x140120BA7}. The read-to-argument mapping is identical to 947's
     * {@code 0x140319250} call, so the field <i>order</i> did not move - only the
     * transforms did.
     */
    public static final long MASK_FORCE_MOVEMENT = 0x4000L; // 947: 0x1000

    /**
     * Mask bit 18, unmoved. Name override: a NUL-terminated CP1252 string scanned inline
     * at 950 {@code 0x140121087}..{@code 0x14012108E} and assigned to actor {@code +0x90}
     * via {@code 0x140121128}. An empty string falls back to the definition name at
     * {@code 0x1401210FD} ({@code [npcDef+0x1B8]}), exactly as in 947.
     */
    public static final long MASK_NAME = 0x40000L;

    /**
     * Mask bit 28, unmoved. Colour tint. Sink confirmed: {@code 0x1403235D0} writes actor
     * {@code +0x190}, {@code +0x194}, {@code +0x198}, {@code +0x19C}, {@code +0x1A0} and
     * {@code +0x1A4} - the same six fields 947 wrote inline. All six wire fields changed
     * transform even though the bit did not move.
     */
    public static final long MASK_COLOUR_TINT = 0x10000000L;

    /** Bit 3: four inline smart2or4null ids, then plain delay (950 0x1401232C1..0x14012351B). */
    public static final long MASK_ANIMATION = 0x8L;
    /** Bit 1: target kind/index medium, wire middle/high/low (950 0x14012118F). */
    public static final long MASK_FACE_ENTITY = 0x2L;
    /** Bit 24: removal and addition spotanim lists (950 0x1401227B3..0x140122D42). */
    public static final long MASK_SPOTANIMS = 0x1000000L;
    /** Bit 5: ordinary hits and hitbars (950 0x140123836..0x140123DA7). */
    public static final long MASK_HITS = 0x20L;

    /**
     * Header extension marker for header byte 1, tested as {@code test r8b, 0x10} at
     * {@code 0x140120558}. 947 used 0x40, which on 950 is {@link #MASK_SAY}: emitting a
     * 947-style header on a 950 client announces a forced-chat block that is not there.
     */
    static final long MARKER_BYTE1 = 0x10L; // 947: 0x40
    /** Header byte 2 marker, {@code bt r8, 0xd} at {@code 0x140120575}. Unmoved. */
    static final long MARKER_BYTE2 = 0x2000L;
    /** Header byte 3 marker, {@code bt r8, 0x17} at {@code 0x140120593}. */
    static final long MARKER_BYTE3 = 0x800000L; // 947: 0x400000
    /** Header byte 4 marker, {@code bt r8, 0x1b} at {@code 0x1401205B1}. */
    static final long MARKER_BYTE4 = 0x8000000L; // 947: 0x1000000

    /**
     * The blocks whose 950 bit AND byte transforms AND sink are all derived. Anything else
     * reaching {@link #maskBlock(Update)} throws: a block with a right bit and a wrong
     * transform is the worst case on this client - it selects the correct reader, feeds it
     * wrong bytes, and every field after it in the frame shifts.
     */
    private static final long DERIVED_BLOCKS = MASK_SAY | MASK_FACE_COORD | MASK_TRANSFORM
            | MASK_FORCE_MOVEMENT | MASK_NAME | MASK_COLOUR_TINT
            | MASK_ANIMATION | MASK_FACE_ENTITY | MASK_SPOTANIMS | MASK_HITS;

    // NPC direction selector, consumed by 950 0x14011FB90 (called from the retained walk
    // path at 0x14011FA97 and both run paths at 0x14011FB16/0x14011FB2E). That function is
    // a cmp-chain on the 3-bit selector: 0 adds the positive constant to Y only, 2 adds it
    // to X only, 4 adds the negative constant to Y only, 6 adds it to X only, and the four
    // odd selectors add one constant to each axis - clockwise from north, matching the 947
    // table. It is NOT the player table; do not share one table.
    private static final int[] DIRECTION_DX = {0, 1, 1, 1, 0, -1, -1, -1};
    private static final int[] DIRECTION_DY = {1, 1, 0, -1, -1, -1, 0, 1};

    /** East-west step of an NPC direction selector, per 950 0x14011FB90. */
    public static int directionDeltaX(int direction) {
        return DIRECTION_DX[checkDirection(direction)];
    }

    /** North-south step of an NPC direction selector, per 950 0x14011FB90. */
    public static int directionDeltaY(int direction) {
        return DIRECTION_DY[checkDirection(direction)];
    }

    /** The 3-bit selector for one adjacent NPC step, or IllegalArgumentException. */
    public static int direction(int dx, int dy) {
        for (int direction = 0; direction < 8; direction++)
            if (DIRECTION_DX[direction] == dx && DIRECTION_DY[direction] == dy) return direction;
        throw new IllegalArgumentException("NPC steps must be one adjacent tile, not " + dx + "," + dy);
    }

    private static int checkDirection(int direction) {
        if (direction < 0 || direction > 7) throw new IllegalArgumentException("NPC direction must fit 0..7");
        return direction;
    }

    /**
     * MSB-first bit writer matching the client's bit reader {@code 0x1400FEF30}. The same
     * cursor carries the retained list, the addition records and the sentinel, so one
     * instance builds the whole bit-addressed part of an NPC_INFO body.
     */
    public static final class BitWriter {
        private byte[] data;
        private int position;

        public BitWriter() { this(32); }

        public BitWriter(int expectedBytes) {
            if (expectedBytes < 1) throw new IllegalArgumentException("Expected size must be positive");
            data = new byte[expectedBytes];
        }

        /** Appends the low {@code count} bits of {@code value}, most significant first. */
        public BitWriter bits(int count, int value) {
            if (count < 1 || count > 32) throw new IllegalArgumentException("Bit count must fit 1..32");
            if (count < 32) {
                int unsignedLimit = (1 << count) - 1;
                int signedFloor = -(1 << (count - 1));
                if (value > unsignedLimit || value < signedFloor)
                    throw new IllegalArgumentException("Value " + value + " does not fit " + count + " bits");
            }
            ensure(position + count);
            for (int bit = count - 1; bit >= 0; bit--, position++)
                if (((value >>> bit) & 1) != 0) data[position >>> 3] |= 1 << (7 - (position & 7));
            return this;
        }

        /** Rounds the cursor up to the next byte, before the byte-addressed mask section. */
        public BitWriter align() {
            position = (position + 7) & ~7;
            ensure(position);
            return this;
        }

        /** Appends already encoded bytes; the cursor must be byte aligned first. */
        public BitWriter bytes(byte[] raw) {
            Objects.requireNonNull(raw, "raw");
            if ((position & 7) != 0) throw new IllegalArgumentException("Byte append requires an aligned cursor");
            ensure(position + raw.length * 8);
            System.arraycopy(raw, 0, data, position >>> 3, raw.length);
            position += raw.length * 8;
            return this;
        }

        /** Bits written so far, before any padding. */
        public int bitPosition() { return position; }

        /** The written bits, zero padded up to a whole number of bytes. */
        public byte[] toByteArray() {
            byte[] out = new byte[(position + 7) >>> 3];
            System.arraycopy(data, 0, out, 0, out.length);
            return out;
        }

        private void ensure(int bits) {
            int needed = (bits + 7) >>> 3;
            if (needed <= data.length) return;
            byte[] grown = new byte[Math.max(needed, data.length * 2)];
            System.arraycopy(data, 0, grown, 0, data.length);
            data = grown;
        }
    }

    /**
     * Unsigned 8-bit retained count, read with width 8 at 950 {@code 0x14011F8C4}. It may
     * not grow the client's list.
     */
    public static void retainedCount(BitWriter out, int count) {
        Objects.requireNonNull(out, "out");
        if (count < 0 || count > 255) throw new IllegalArgumentException("Retained count must fit 0..255");
        out.bits(8, count);
    }

    /** changed=0: keeps the NPC untouched and stamps its update cycle (950 0x14011F9DF). */
    public static void retainedEntry(BitWriter out) {
        Objects.requireNonNull(out, "out");
        out.bits(1, 0);
    }

    /**
     * changed=1 selector 00: no movement, but the NPC joins the pending mask list at
     * {@code npcMgr+0xC0F0} (950 0x14011FA45).
     */
    public static void retainedMaskOnly(BitWriter out) {
        Objects.requireNonNull(out, "out");
        out.bits(1, 1);
        out.bits(2, 0);
    }

    /**
     * changed=1 selector 01: one step, 3-bit direction read at 950 {@code 0x14011FA86}
     * with the walk move-speed constant loaded at {@code 0x14011FA8B}, then the
     * pending-mask bit read at {@code 0x14011FAA4}.
     */
    public static void retainedWalk(BitWriter out, int direction, boolean pendingMask) {
        Objects.requireNonNull(out, "out");
        checkDirection(direction);
        out.bits(1, 1);
        out.bits(2, 1);
        out.bits(3, direction);
        out.bits(1, pendingMask ? 1 : 0);
    }

    /**
     * changed=1 selector 10 with the two-step bit clear (950 {@code 0x14011FAF7} reads it,
     * {@code 0x14011FB04} tests it): one step with the slow move-speed constant loaded at
     * {@code 0x14011FB33} and the direction read at {@code 0x14011FB3A}, then the shared
     * pending-mask bit.
     */
    public static void retainedStep(BitWriter out, int direction, boolean pendingMask) {
        Objects.requireNonNull(out, "out");
        checkDirection(direction);
        out.bits(1, 1);
        out.bits(2, 2);
        out.bits(1, 0);
        out.bits(3, direction);
        out.bits(1, pendingMask ? 1 : 0);
    }

    /**
     * changed=1 selector 10 with the two-step bit set: two queued steps, directions read
     * at 950 {@code 0x14011FB09} and {@code 0x14011FB26}, then the shared pending-mask bit.
     */
    public static void retainedRun(BitWriter out, int direction1, int direction2, boolean pendingMask) {
        Objects.requireNonNull(out, "out");
        checkDirection(direction1);
        checkDirection(direction2);
        out.bits(1, 1);
        out.bits(2, 2);
        out.bits(1, 1);
        out.bits(3, direction1);
        out.bits(3, direction2);
        out.bits(1, pendingMask ? 1 : 0);
    }

    /**
     * changed=1 selector 11: queues the NPC for removal (950 {@code 0x14011FB4C}, list at
     * {@code npcMgr+0xB0A8}). There is no retained teleport form; repositioning is removal
     * plus a fresh addition record.
     */
    public static void retainedRemove(BitWriter out) {
        Objects.requireNonNull(out, "out");
        out.bits(1, 1);
        out.bits(2, 3);
    }

    /**
     * The 16-bit 65535 index that ends the addition records (950 index read
     * {@code 0x14011FE08}, terminator test {@code 0x14011FE14}). Required whenever a mask
     * section follows, and whenever 16 or more bits remain in the buffer.
     */
    public static void additionsEnd(BitWriter out) {
        Objects.requireNonNull(out, "out");
        out.bits(16, 65535);
    }

    /** True only for the six mask bits whose 950 layout and sink are both derived. */
    public static boolean isSupportedMaskBit(int bit) {
        if (bit < 0 || bit > 63) return false;
        return (DERIVED_BLOCKS & (1L << bit)) != 0;
    }

    /**
     * One NPC's update-mask block: the two bytes the client skips at 950
     * {@code 0x14011F5DB}, the variable-length header from {@code 0x140120540}, then the
     * requested blocks in the client's fixed consumption order ({@code 0x1401205E0}).
     */
    public static byte[] maskBlock(Update update) {
        Objects.requireNonNull(update, "update");
        requireDerivedBlocks(update);
        long mask = update.mask;
        if (mask == 0) throw new IllegalArgumentException("An NPC mask block needs at least one derived block");
        // Extension markers cascade downwards: the marker for header byte 4 (bit 27) lives
        // in byte 3, byte 3's marker (bit 23) in byte 2, byte 2's marker (bit 13) in byte 1
        // and byte 1's marker (bit 4) in byte 0. Evaluate high to low so each marker added
        // pulls in the one below it.
        if ((mask >>> 32) != 0) mask |= MARKER_BYTE4;
        if ((mask >>> 24) != 0) mask |= MARKER_BYTE3;
        if ((mask >>> 16) != 0) mask |= MARKER_BYTE2;
        if ((mask >>> 8) != 0) mask |= MARKER_BYTE1;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0);
        out.write(0);
        out.write((int) (mask & 0xFF));
        if ((mask & MARKER_BYTE1) != 0) out.write((int) ((mask >>> 8) & 0xFF));
        if ((mask & MARKER_BYTE2) != 0) out.write((int) ((mask >>> 16) & 0xFF));
        if ((mask & MARKER_BYTE3) != 0) out.write((int) ((mask >>> 24) & 0xFF));
        if ((mask & MARKER_BYTE4) != 0) out.write((int) ((mask >>> 32) & 0xFF));

        // 950 consumption order. The client parses positionally, so this sequence is
        // load-bearing on its own: it is NOT bit order and it is NOT 947's order (947 was
        // animation, forceMovement, colourTint, faceCoordinate, transform, say, name).
        // Address of each test inside 0x1401205E0, ascending:
        //   0x140120656  bit 6   say
        //   0x140120A01  bit 28  colourTint
        //   0x140120AA2  bit 2   transform
        //   0x140120AEA  bit 14  forceMovement
        //   0x140121067  bit 18  name
        //   0x140121217  bit 7   faceCoordinate   (tested as "test r12b, r12b; jns")
        // Tail: faceEntity 0x14012118F; spotanims 0x1401227B3;
        // animation 0x1401232C1; hits 0x140123836.
        writeBlock(out, update.say);
        writeBlock(out, update.colourTint);
        writeBlock(out, update.transform);
        writeBlock(out, update.forceMovement);
        writeBlock(out, update.name);
        writeBlock(out, update.faceEntity);
        writeBlock(out, update.faceCoordinate);
        writeBlock(out, update.spotanims);
        writeBlock(out, update.animation);
        writeBlock(out, update.hits);
        return out.toByteArray();
    }

    /**
     * Mask blocks for several NPCs, concatenated in pending-list order: every
     * retained entry that asked for an update, then every addition that did.
     */
    public static byte[] maskSection(Update... updates) {
        Objects.requireNonNull(updates, "updates");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Update update : updates) {
            byte[] block = maskBlock(update);
            out.write(block, 0, block.length);
        }
        return out.toByteArray();
    }

    /**
     * Refuses any mask bit whose 950 byte transform is not derived. Mirrors
     * {@code Native950PlayerMasks.requireDerivedBlocks}: callers fail loudly rather than
     * inventing an encoding for evidence that does not exist yet.
     */
    private static void requireDerivedBlocks(Update update) {
        long underived = update.mask & ~DERIVED_BLOCKS;
        if (underived != 0)
            throw new UnsupportedOperationException("NPC update block(s) 0x"
                    + Long.toHexString(underived) + " carry a 950 mask bit but a byte transform"
                    + " that has not been derived for 950.");
    }

    /**
     * Refuses one of the remaining mask blocks the 950 parser dispatches but whose record
     * layout and sink are untraced.
     *
     * @throws UnsupportedOperationException always
     */
    public static byte[] candidateMask(int maskBit) {
        throw new UnsupportedOperationException("NPC mask bit " + maskBit + " is dispatched by the"
                + " 950 parser 0x1401205E0 but its record layout and sink are not derived;"
                + " emitting it would shift every field after it in the packet");
    }

    private static void writeBlock(ByteArrayOutputStream out, byte[] block) {
        if (block != null) out.write(block, 0, block.length);
    }

    /**
     * The derived mask blocks for one NPC. Each setter validates and encodes immediately,
     * so an invalid field is rejected before any framing happens.
     */
    public static final class Update {
        private long mask;
        private byte[] forceMovement;
        private byte[] colourTint;
        private byte[] faceCoordinate;
        private byte[] transform;
        private byte[] say;
        private byte[] name;
        private byte[] animation, faceEntity, spotanims, hits;

        /** The 64-bit mask this update will emit, before extension markers are added. */
        public long mask() { return mask; }

        /**
         * Bit 3. Four smart2or4null ids are inlined at 950 0x1401232CB..0x1401234C6;
         * delay is PLAIN at 0x1401234E7, into actor virtual slot +0x1D8.
         * 947 baseline: 00 00 80 03 57 7f ff 7f ff 7f ff 80 for (855,-1,-1,-1,0).
         */
        public Update animation(int id0, int id1, int id2, int id3, int delay) {
            range(delay, 0, 255, "Animation delay");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            smart2or4null(out, id0, "Animation slot 0");
            smart2or4null(out, id1, "Animation slot 1");
            smart2or4null(out, id2, "Animation slot 2");
            smart2or4null(out, id3, "Animation slot 3");
            out.write(delay);
            animation = out.toByteArray();
            mask |= MASK_ANIMATION;
            return this;
        }

        /** Kind 1 is NPC, kind 2 player, 0xFF clears; 950 0x1401211D5..0x1401211FB. */
        public Update faceNpc(int index) { unsignedShort(index, "NPC index"); return faceTarget(0x10000 | index); }
        public Update facePlayer(int index) { unsignedShort(index, "Player index"); return faceTarget(0x20000 | index); }
        public Update clearFaceEntity() { return faceTarget(0xffffff); }
        private Update faceTarget(int target) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mediumMiddleHighLow(out, target);
            faceEntity = out.toByteArray();
            mask |= MASK_FACE_ENTITY;
            return this;
        }

        /**
         * Bit 24. Removal count plain + signed BE shorts; add count +128. Repeated record
         * selectors at 950 0x140B603C9 are 00 02 03 03 03: plain slot, ushort128 id,
         * i32 permutation [16,24,0,8], 128-rotation, medium [8,16,0].
         */
        public Update spotanims(int[] removals, Spotanim... additions) {
            Objects.requireNonNull(removals, "removals");
            Objects.requireNonNull(additions, "additions");
            range(removals.length, 0, 255, "Spotanim removal count");
            range(additions.length, 0, 255, "Spotanim addition count");
            if (removals.length == 0 && additions.length == 0)
                throw new IllegalArgumentException("Spotanim list must change at least one slot");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(removals.length);
            for (int id : removals) {
                range(id, -1, 32767, "Spotanim removal");
                if (id == -1 && removals.length != 1)
                    throw new IllegalArgumentException("Clear-all must be the sole spotanim removal");
                shortBigEndian(out, id);
            }
            out.write((additions.length + 128) & 255);
            for (Spotanim spot : additions) {
                Objects.requireNonNull(spot, "spotanim");
                out.write(spot.slot);
                out.write((spot.id >>> 8) & 255);
                out.write((spot.id + 128) & 255);
                int packed = (spot.height << 16) | spot.delay;
                out.write((packed >>> 16) & 255); out.write((packed >>> 24) & 255);
                out.write(packed & 255); out.write((packed >>> 8) & 255);
                out.write((128 - spot.rotation) & 255);
                int offsets = (spot.xOffset + 1023) | ((spot.yOffset + 1023) << 11);
                mediumMiddleHighLow(out, offsets);
            }
            spotanims = out.toByteArray(); mask |= MASK_SPOTANIMS;
            return this;
        }

        /**
         * Bit 14. Six signed bytes then three shorts, read at 950
         * {@code 0x140120AFA}..{@code 0x140120B67} and passed to {@code 0x14031DAE0} in the
         * same argument slots 947 used, so the field order is unchanged.
         *
         * <p>Selector table {@code 0x140B60420} = {@code 02 02 00 00 02 00 01 01 01}, which
         * decodes to: dx1 negated, dy1 negated, dx2 plain, dy2 plain, dplane1 negated,
         * dplane2 plain, then all three shorts little endian. 947 was dx1 plain, dy1
         * {@code +128}, dx2 plain, dy2 negated, dplane1 plain, dplane2 negated and three
         * {@code ushortle128} shorts - five of the six bytes and all three shorts moved.
         *
         * <p>947 baseline for (1,2,3,4,0,0,10,20,8192):
         * {@code 00 00 40 10 01 82 03 fc 00 00 8a 00 94 00 80 20}.
         *
         * <p>The client scales the four tile offsets and adds both delays to the current
         * cycle; the angle keeps 14 bits ({@code and eax, 0x3fff} inside {@code 0x1407474B0}).
         */
        public Update forceMovement(int dx1, int dy1, int dx2, int dy2, int deltaPlane1, int deltaPlane2,
                                    int delay1, int delay2, int angle) {
            signedByte(dx1, "Force movement dx1");
            signedByte(dy1, "Force movement dy1");
            signedByte(dx2, "Force movement dx2");
            signedByte(dy2, "Force movement dy2");
            signedByte(deltaPlane1, "Force movement plane delta 1");
            signedByte(deltaPlane2, "Force movement plane delta 2");
            unsignedShort(delay1, "Force movement delay 1");
            unsignedShort(delay2, "Force movement delay 2");
            if (angle < 0 || angle > 16383) throw new IllegalArgumentException("Force movement angle must fit 14 bits");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write((-dx1) & 0xFF);          // selector 02
            out.write((-dy1) & 0xFF);          // selector 02
            out.write(dx2 & 0xFF);             // selector 00
            out.write(dy2 & 0xFF);             // selector 00
            out.write((-deltaPlane1) & 0xFF);  // selector 02
            out.write(deltaPlane2 & 0xFF);     // selector 00
            shortLittleEndian(out, delay1);    // selector 01
            shortLittleEndian(out, delay2);    // selector 01
            shortLittleEndian(out, angle);     // selector 01
            forceMovement = out.toByteArray();
            mask |= MASK_FORCE_MOVEMENT;
            return this;
        }

        /**
         * Bit 28. Four bytes then two shorts, read at 950
         * {@code 0x140120A11}..{@code 0x140120A5E}, packed into a 16-bit HSL index
         * ({@code hue<<10 | saturation<<7 | luminance},
         * {@code 0x140120A63}..{@code 0x140120A77}) and written to actor {@code +0x190}
         * ..{@code +0x1A4} by {@code 0x1403235D0}.
         *
         * <p>Selector table {@code 0x140B60414} = {@code 01 01 00 00 00 00}: hue and
         * saturation are {@code value+128}, luminance and opacity are plain, and both delays
         * are plain big-endian shorts. 947 was {@code 128-hue}, {@code 128-saturation},
         * {@code -luminance}, {@code 128-opacity} and a little-endian start delay - so FIVE of the
         * six changed. The end delay was big-endian under both revisions and is the one field that
         * carries over untouched.
         *
         * <p>947 baseline for (6,3,100,200,500,1000):
         * {@code 00 00 40 20 40 10 7a 7d 9c b8 f4 01 03 e8}.
         *
         * <p>The client masks hue to 6 bits, saturation to 3 and luminance to 7 <i>after</i>
         * the transform, and adds both delays to the current cycle.
         */
        public Update colourTint(int hue, int saturation, int luminance, int opacity,
                                 int startDelay, int endDelay) {
            if (hue < 0 || hue > 63) throw new IllegalArgumentException("Tint hue must fit 0..63");
            if (saturation < 0 || saturation > 7) throw new IllegalArgumentException("Tint saturation must fit 0..7");
            if (luminance < 0 || luminance > 127) throw new IllegalArgumentException("Tint luminance must fit 0..127");
            if (opacity < 0 || opacity > 255) throw new IllegalArgumentException("Tint opacity must fit 0..255");
            unsignedShort(startDelay, "Tint start delay");
            unsignedShort(endDelay, "Tint end delay");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write((hue + 128) & 0xFF);        // selector 01
            out.write((saturation + 128) & 0xFF); // selector 01
            out.write(luminance & 0xFF);          // selector 00
            out.write(opacity & 0xFF);            // selector 00
            shortBigEndian(out, startDelay);      // selector 00
            shortBigEndian(out, endDelay);        // selector 00
            colourTint = out.toByteArray();
            mask |= MASK_COLOUR_TINT;
            return this;
        }

        /**
         * Bit 7. Two shorts decoded as {@code (v-1)>>1}, so the wire carries
         * {@code 2*tile+1}, read at 950 {@code 0x140121221} / {@code 0x14012122E} and stored
         * as floats at actor {@code +0x22C} (x) and {@code +0x234} (y).
         *
         * <p>Selector table {@code 0x140B60408} = {@code 00 01}: x is now a plain big-endian
         * short, y stays little endian. 947 wrote x as {@code ushortle128}
         * ({@code [x+128, x>>>8]}), so only x moved.
         *
         * <p>947 baseline for tile (3200,3200): {@code 00 00 08 81 19 01 19}.
         */
        public Update faceCoordinate(int tileX, int tileY) {
            if (tileX < 0 || tileX > 32767) throw new IllegalArgumentException("Face coordinate x must fit 0..32767");
            if (tileY < 0 || tileY > 32767) throw new IllegalArgumentException("Face coordinate y must fit 0..32767");
            int encodedX = tileX * 2 + 1;
            int encodedY = tileY * 2 + 1;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            shortBigEndian(out, encodedX);    // selector 00
            shortLittleEndian(out, encodedY); // selector 01
            faceCoordinate = out.toByteArray();
            mask |= MASK_FACE_COORD;
            return this;
        }

        /**
         * Bit 2. One {@code smart2or4null} NPC definition id read at 950 {@code 0x140120AD3},
         * handed to the definition setter at virtual slot {@code 0x218}. That reader consumes
         * no selector byte and is instruction-identical to 947's, so the bytes are unchanged.
         *
         * <p>947 baseline for type 494: {@code 00 00 20 01 ee} - only the mask byte moves.
         */
        public Update transform(int npcTypeId) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            smart2or4null(out, npcTypeId, "Transform NPC type");
            transform = out.toByteArray();
            mask |= MASK_TRANSFORM;
            return this;
        }

        /**
         * Bit 6. A NUL terminated CP1252 string read at 950 {@code 0x140120679}; forced chat
         * text. The reader takes no selector, so the body is unchanged from 947 and only the
         * mask byte moves.
         *
         * <p>947 baseline for "Hi": {@code 00 00 01 48 69 00}. The 950 form
         * {@code 00 00 40 48 69 00} is the tail of the plan's own Stage 5 acceptance vector
         * {@code 01 9F FF E0 00 00 40 <text> 00}, which decodes as retained count 1,
         * changed=1 selector 00, the 16-bit addition terminator, byte alignment, the two
         * skipped bytes, then mask byte 0x40.
         */
        public Update say(String text) {
            Objects.requireNonNull(text, "text");
            say = cp1252(text, "NPC say text");
            mask |= MASK_SAY;
            return this;
        }

        /**
         * Bit 18. A NUL terminated CP1252 string scanned at 950 {@code 0x140121087} and
         * assigned to actor {@code +0x90}. An empty string restores the name from the NPC
         * definition ({@code 0x1401210FD}). The body is unchanged from 947; only the header
         * differs, because header byte 1's marker moved from 0x40 to 0x10.
         *
         * <p>947 baseline for "Bob": {@code 00 00 40 20 04 42 6f 62 00}.
         */
        public Update name(String value) {
            Objects.requireNonNull(value, "value");
            name = cp1252(value, "NPC name override");
            mask |= MASK_NAME;
            return this;
        }

        /** Ordinary hit form at bit 5; extended-damage bit 33 remains fenced. */
        public Update hits() { return hits(new Hit[0], new Hitbar[0]); }

        /**
         * 950 0x140123836: negated hit count, smart fields, negated untyped damage.
         * Hitbars: plain count, smart id/cycle/delay, percent1 +128, percent2 negated,
         * signed-null smart size, quantity1 plain, quantity2 128-minus.
         */
        public Update hits(Hit[] values, Hitbar[] bars) {
            Objects.requireNonNull(values, "hits"); Objects.requireNonNull(bars, "hitbars");
            range(values.length, 0, 255, "Hit count"); range(bars.length, 0, 255, "Hitbar count");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write((-values.length) & 255);
            for (Hit hit : values) {
                Objects.requireNonNull(hit, "hit");
                if (hit.form == 1) {
                    smart(out, 32767); smart(out, hit.type); smart(out, hit.damage);
                    smart(out, hit.type2); smart(out, hit.damage2);
                } else if (hit.form == 2) {
                    smart(out, 32766); out.write((-hit.damage) & 255);
                } else { smart(out, hit.type); smart(out, hit.damage); }
                smart(out, hit.delay);
            }
            out.write(bars.length);
            for (Hitbar bar : bars) {
                Objects.requireNonNull(bar, "hitbar");
                smart(out, bar.id); smart(out, bar.cycle);
                if (bar.cycle == 32767) continue;
                smart(out, bar.delay);
                out.write((bar.percent1 + 128) & 255);
                if (bar.cycle != 0) out.write((-bar.percent2) & 255);
                if (bar.size <= 126) out.write(bar.size + 1);
                else shortBigEndian(out, bar.size + 32769);
                if (bar.size >= 0) {
                    out.write(bar.quantity1);
                    if (bar.cycle != 0) out.write((128 - bar.quantity2) & 255);
                }
            }
            hits = out.toByteArray(); mask |= MASK_HITS;
            return this;
        }
    }

    /** A spot effect in one actor slot. Unknown flag bits remain zero. */
    public static final class Spotanim {
        private final int slot, id, delay, height, rotation, xOffset, yOffset;
        private Spotanim(int slot, int id, int delay, int height, int rotation, int xOffset, int yOffset) {
            this.slot = slot; this.id = id; this.delay = delay; this.height = height;
            this.rotation = rotation; this.xOffset = xOffset; this.yOffset = yOffset;
        }
        /** Height is the unsigned high word; client expands it by four into scene units. */
        public static Spotanim of(int slot, int id, int delay, int height, int rotation, int xOffset, int yOffset) {
            range(slot, 0, 255, "Spotanim slot"); range(id, -1, 65534, "Spotanim id");
            range(delay, 0, 32767, "Spotanim delay"); range(height, 0, 32767, "Spotanim height");
            range(rotation, 0, 7, "Spotanim rotation");
            range(xOffset, -1023, 1024, "Spotanim X offset"); range(yOffset, -1023, 1024, "Spotanim Y offset");
            return new Spotanim(slot, id, delay, height, rotation, xOffset, yOffset);
        }
    }

    /** Three ordinary hit forms; type 32766/32767 selects special forms. */
    public static final class Hit {
        private final int form, type, damage, type2, damage2, delay;
        private Hit(int form, int type, int damage, int type2, int damage2, int delay) {
            this.form = form; this.type = type; this.damage = damage;
            this.type2 = type2; this.damage2 = damage2; this.delay = delay;
        }
        public static Hit of(int type, int damage, int delay) {
            range(type, 0, 32765, "Hit type"); range(damage, 0, 32767, "Hit damage"); range(delay, 0, 32767, "Hit delay");
            return new Hit(0, type, damage, -1, 0, delay);
        }
        public static Hit dual(int type, int damage, int type2, int damage2, int delay) {
            range(type, 0, 32767, "Hit type"); range(damage, 0, 32767, "Hit damage");
            range(type2, 0, 32767, "Secondary hit type"); range(damage2, 0, 32767, "Secondary hit damage");
            range(delay, 0, 32767, "Hit delay"); return new Hit(1, type, damage, type2, damage2, delay);
        }
        public static Hit untyped(int damage, int delay) {
            range(damage, 0, 255, "Untyped hit damage"); range(delay, 0, 32767, "Hit delay");
            return new Hit(2, -1, damage, -1, 0, delay);
        }
    }

    /** Hitbar values; cache identity must be established by the caller. */
    public static final class Hitbar {
        private final int id, cycle, delay, percent1, percent2, size, quantity1, quantity2;
        private Hitbar(int id, int cycle, int delay, int percent1, int percent2, int size, int quantity1, int quantity2) {
            this.id = id; this.cycle = cycle; this.delay = delay; this.percent1 = percent1;
            this.percent2 = percent2; this.size = size; this.quantity1 = quantity1; this.quantity2 = quantity2;
        }
        public static Hitbar remove(int id) { range(id, 0, 32766, "Hitbar id"); return new Hitbar(id, 32767, 0, 0, 0, -1, 0, 0); }
        public static Hitbar update(int id, int cycle, int delay, int percent1, int percent2) {
            return sized(id, cycle, delay, percent1, percent2, -1, 0, 0);
        }
        public static Hitbar sized(int id, int cycle, int delay, int percent1, int percent2, int size, int quantity1, int quantity2) {
            range(id, 0, 32766, "Hitbar id"); range(cycle, 0, 32766, "Hitbar cycle");
            range(delay, 0, 32767, "Hitbar delay"); range(percent1, 0, 255, "Hitbar percent1"); range(percent2, 0, 255, "Hitbar percent2");
            range(size, -1, 32766, "Hitbar size"); range(quantity1, 0, 255, "Hitbar quantity1"); range(quantity2, 0, 255, "Hitbar quantity2");
            if (cycle == 0 && (percent1 != percent2 || quantity1 != quantity2))
                throw new IllegalArgumentException("Second values are not on the wire at cycle zero");
            if (size == -1 && (quantity1 != 0 || quantity2 != 0))
                throw new IllegalArgumentException("Quantity is not on the wire for size -1");
            return new Hitbar(id, cycle, delay, percent1, percent2, size, quantity1, quantity2);
        }
    }

    private static void smart(ByteArrayOutputStream out, int value) {
        if (value < 128) out.write(value); else shortBigEndian(out, value + 32768);
    }
    private static void mediumMiddleHighLow(ByteArrayOutputStream out, int value) {
        out.write((value >>> 8) & 255); out.write((value >>> 16) & 255); out.write(value & 255);
    }
    private static void range(int value, int min, int max, String field) {
        if (value < min || value > max) throw new IllegalArgumentException(field + " must fit " + min + ".." + max);
    }

    /**
     * {@code smart2or4null} ({@code 0x1400FEEA0}, instruction-identical to 947's
     * {@code 0x1401003F0}): -1 as the two-byte 0x7FFF, values below 0x7FFF as a big-endian
     * short, and anything larger as a big-endian int with bit 31 set so the client's
     * leading-byte test ({@code cmp byte [base+cursor], 0x7f}) picks the four-byte form.
     * This reader takes no selector byte, so it could not have moved with the mask table.
     */
    private static void smart2or4null(ByteArrayOutputStream out, int value, String field) {
        if (value < -1) throw new IllegalArgumentException(field + " must be -1 or a non-negative id");
        if (value == -1 || value < 0x7FFF) {
            int encoded = value == -1 ? 0x7FFF : value;
            out.write((encoded >>> 8) & 0xFF);
            out.write(encoded & 0xFF);
            return;
        }
        int encoded = value | 0x80000000;
        out.write((encoded >>> 24) & 0xFF);
        out.write((encoded >>> 16) & 0xFF);
        out.write((encoded >>> 8) & 0xFF);
        out.write(encoded & 0xFF);
    }

    /** u16 selector 00: the client reads big endian ({@code rol ax, 8} at 0x14010DA4C). */
    private static void shortBigEndian(ByteArrayOutputStream out, int value) {
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    /** u16 selector 01: the client reads little endian ({@code 0x14010DA09}). */
    private static void shortLittleEndian(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >>> 8) & 0xFF);
    }

    private static void signedByte(int value, String field) {
        if (value < -128 || value > 127) throw new IllegalArgumentException(field + " must fit a signed byte");
    }

    private static void unsignedShort(int value, String field) {
        if (value < 0 || value > 65535) throw new IllegalArgumentException(field + " must fit an unsigned short");
    }

    private static byte[] cp1252(String value, String field) {
        if (value.indexOf('\0') >= 0) throw new IllegalArgumentException(field + " contains an embedded NUL");
        try {
            ByteBuffer encoded = Charset.forName("windows-1252").newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(value));
            byte[] out = new byte[encoded.remaining() + 1];
            encoded.get(out, 0, out.length - 1);
            return out;
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException(field + " is not representable in CP1252", ex);
        }
    }

    private Native950NpcMasks() { }
}
