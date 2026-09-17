package com.rs.network.protocol.modern950;

/**
 * Wire definitions for the native WIN64 950-1 client.
 *
 * Every opcode below was identified from its native parser and cross-checked against the client's
 * own descriptor tables; registration order was never accepted as evidence on its own. Sizes are
 * taken per row from those tables rather than carried over as a column: the two CLIENT_SETVARCSTR
 * variants swap frame width between 947 and 950, so a copied column mis-frames the stream.
 *
 * A known packet size does not establish its meaning.
 */
public final class Native950Protocol {
    public static final int BUILD = 950;
    /** The 950 client's update number is not established; it is unused by this server. */
    public static final int UPDATE = 0;
    public static final String ORIGINAL_CLIENT_SHA256 =
            "fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36";
    public static final int UNKNOWN_SIZE = Integer.MIN_VALUE;
    /**
     * Opcode and size of a packet whose 950 encoding is not derived.
     *
     * A wrong opcode is not a visible failure on this client: it parses positionally, so the bytes
     * are consumed as whatever packet does own that opcode and applied silently. Constants carrying
     * this value throw from both accessors rather than let that happen.
     */
    public static final int UNDERIVED_OPCODE = Integer.MIN_VALUE + 1;

    // Indexed by opcode, from the 950 client's own descriptor table. 112 of the opcodes that exist
    // in both revisions have a different length here, so the 947 table cannot be reused.
    private static final int[] CLIENT_SIZES = {
        -1, -1, 4, -1, -1, 0, -1, 4, -2, 6,
        2, 0, 18, 3, -2, 22, 3, -1, 9, 12,
        3, -2, 8, -1, 9, -2, 1, 3, 4, 1,
        18, 9, 2, -1, 9, 4, 3, 3, -1, 3,
        -1, 9, 0, 1, 9, -1, 3, -1, 9, 9,
        -1, 3, 8, -1, 7, -1, 8, -1, 3, 9,
        3, 4, 2, 3, -2, 6, 9, -1, 15, 18,
        -1, 3, -2, 9, -1, 1, -1, -1, 18, 9,
        4, 9, 4, 9, -1, 13, -2, -1, 5, 9,
        18, 0, 3, 0, 3, -1, -1, 9, 4, 2,
        9, 6, 12, 8, 0, -1, 5, 3, 4, -2,
        4, -1, 17, 8, 4, 1, 3, 3, -2, 1,
        8, -2, 9, -2, 11, -1, 9, 8, 5
    };

    /** Only these server packet meanings and sizes have native verification on 950. */
    public enum ServerPacket {
        /** 947 opcode 94. parser 0x140107dd0; LE interface id then 17 unread bytes */
        IF_OPENTOP(1, 19),
        /** 947 opcode 111. id ushortle128 then value intv1 */
        VARP_LARGE(4, 6),
        /** 947 opcode 108. exact parser match to 947 0x140106400 */
        UPDATE_RUNWEIGHT(7, 2),
        /** 947 opcode 69. item field widened to BE u24 at 0x1400fd131 */
        UPDATE_INV_FULL(9, -2),
        /** 947 opcode 116. exact parser match to 947 0x1401064A0 */
        UPDATE_RUNENERGY(21, 1),
        /** 947 opcode 48. exact parser match to 947 0x1401415C0; empty body */
        RESET_CLIENT_VARCACHE(23, 0),
        /** 947 opcode 35. 950 parser 0x140107970, read field by field: mask intv1, toSlot plain BE u16, fromSlot ushort128, parent intle; both u16 fields fold 0xFFFF to -1 */
        IF_SETEVENTS(24, 12),
        /** 947 opcode 50. parser 0x1401421E0; value+0x80 FIRST, then id LE u16 */
        VARBIT_SMALL(28, 3),
        /** 947 opcode 67. byte frame; id BE u16 low+128 first, then NUL string */
        CLIENT_SETVARCSTR_SMALL(30, -1),
        /** 947 opcode 52. exact parser match to 947 0x14010A780 */
        UPDATE_REBOOT_TIMER(31, 2),
        /** 947 opcode 105. unchanged body */
        MESSAGE_GAME(33, -1),
        /** 947 opcode 121. parser 0x1400f73c0; NUL descriptor, reverse args, BE ints */
        RUNCLIENTSCRIPT(35, -2),
        /** 947 opcode 27. dispatch 0x140143660; GPI bit layout unchanged, masks moved */
        PLAYER_INFO(36, -2),
        /** 947 opcode 81. plain BE u32 hash, not intv1 */
        IF_SETPLAYERHEAD(38, 4),
        /** 947 opcode 115. registration table base 0x140E94EF0 + 48*80 + 0x10 */
        CLIENT_SETVARCBIT_SMALL(48, 3),
        /** 947 opcode 5. item field widened to BE u24, as UPDATE_INV_FULL */
        UPDATE_INV_PARTIAL(50, -2),
        /** 947 opcode 90. parser 0x1400f6e10; chunkY b0..b1, chunkX b6..b7 */
        REBUILD_NORMAL(63, -2),
        /** 947 opcode 103. 950 parser 0x140107590 reassembles (b2<<24)|(b3<<16)|(b0<<8)|b1 at 0x1401075b5..0x1401075db, i.e. hash intv1 - NOT intv2 - then a plain boolean */
        IF_SETHIDE(67, 5),
        /** 947 opcode 33. parent intv2 */
        IF_CLOSESUB(69, 4),
        /** 947 opcode 10. value sbyte then id ushort128 */
        VARP_SMALL(79, 3),
        /** 947 opcode 15. short frame; NUL string first, then id LE u16 with NO bias */
        CLIENT_SETVARCSTR_LARGE(81, -2),
        /** 947 opcode 71. registration table base + 82*80 + 0x10 */
        VARBIT_LARGE(82, 6),
        /** 947 opcode 55. registration table base + 87*80 + 0x10 */
        CLIENT_SETVARCBIT_LARGE(87, 6),
        /** 947 opcode 66. parser 0x140141290; skill, level, then BE experience */
        UPDATE_STAT(92, 6),
        /** 947 opcode 8. parent hash, 12 unused, interface id LE low+128, 128-minus flag */
        IF_OPENSUB(100, 23),
        /** 947 opcode 2. parent int then NUL string */
        IF_SETTEXT(115, -2),
        /** 947 opcode 112. parser 0x140141DB0; id ushort128, value (b3,b2,b5,b4) */
        CLIENT_SETVARC_LARGE(119, 6),
        /** 947 opcode 1. registration table base + 126*80 + 0x10 */
        CLIENT_SETVARC_SMALL(126, 3),
        /** 947 opcode 159. BE checksum; live-verified in the lobby */
        WORLDLIST_FETCH_REPLY(129, -2),
        /** 947 opcode 195. empty body; writes the loading-gate byte at 0x14010ae5d */
        SERVER_TICK_END(160, 0),
        /** 950 descriptor140e98e10, dispatch1401052f0 ->1400fe2b0: no payload, full session reset. */
        LOGOUT_FULL(202, 0),
        /** 947 opcode 216. empty body */
        NO_TIMEOUT(183, 0),
        /** 947 opcode 87. id plain BE u32 then 128-volume; size 5 confirmed in the 950 server table */
        MUSIC(107, 5),
        /** 950 parser 0x1401130b0: plain offset, ID bytes16/24/0/8, shape+128. */
        LOC_ADD_CHANGE(11, -1),
        /** 950 parser 0x140113f30: 128-offset, shape+128; native removal operation3. */
        LOC_DEL(26, 2),
        /** 950 parser 0x140141040: signed zoneY, negated signed zoneX, plane+128. */
        UPDATE_ZONE_PARTIAL_FOLLOWS(96, 3),
        /** 950 parser 0x140140b80: item LE u24, 128-offset, amount BE u16. */
        OBJ_ADD(51, 6),
        /** 950 parser 0x140140ab0: 128-offset then item LE u24. */
        OBJ_DEL(109, 4),
        /** 950 parser 0x140115020: offset, item BE u24, old/new amounts BE u16. */
        OBJ_COUNT(70, 8),
        /** 947 opcode 12. retained/mask sections unchanged; addition record reordered (plan A7) */
        NPC_INFO(80, -2);

        // Deliberately absent until verified. Adding a writer for one of these would send bytes a
        // live client parses positionally and silently mis-applies:
        //   IF_SETCOLOUR           83     structurally degenerate with IF_SETSCROLLPOS; likely, not verified
        //   IF_SETSCROLLPOS        16     GUESS - same shape as IF_SETCOLOUR, only the +128 bias differs

        private final int opcode;
        private final int size;
        ServerPacket(int opcode, int size) { this.opcode = opcode; this.size = size; }

        /** True when this packet's 950 encoding has not been derived and it must not be sent. */
        public boolean isDerived() { return opcode != UNDERIVED_OPCODE; }

        public int opcode() { return checked(opcode); }

        /** Fixed bytes, -1 for an unsigned-byte length, -2 for unsigned-short. */
        public int size() { return checked(size); }

        private int checked(int value) {
            if (opcode == UNDERIVED_OPCODE)
                throw new UnsupportedOperationException(name() + " has no derived 950 encoding."
                        + " Its opcode was carried over from 947 and the 950 client parses opcodes"
                        + " positionally, so sending it would be silently mis-applied rather than"
                        + " rejected. Derive it against the native parser before enabling it.");
            return value;
        }
    }

    public static int clientSize(int opcode) {
        return opcode >= 0 && opcode < CLIENT_SIZES.length ? CLIENT_SIZES[opcode] : UNKNOWN_SIZE;
    }

    private Native950Protocol() { }
}
