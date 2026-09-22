package com.rs.network.protocol.modern950;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Native WIN64 950-1 client -&gt; server decoding. Gameplay validation belongs on the world thread.
 *
 * <p><b>Every address in this file is a 950 VA</b> (client
 * {@code fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36}). The 947 and 950 code
 * ranges overlap, so a 947 address disassembles cleanly in the 950 binary and lands on unrelated
 * code; do not carry an address across revisions without re-deriving it.
 *
 * <h2>Why a wrong byte here is worse than a crash</h2>
 * This decoder is the inverse of a positional writer. The client never labels a field: it allocates
 * a descriptor-sized frame and stamps bytes at fixed offsets. If we read the wrong offset or the
 * wrong bias we do not fail - we hand the world a plausible-looking coordinate, index or component
 * hash and the player walks somewhere else, or clicks a different button. Every layout below was
 * read out of the client's own sender, instruction by instruction, and the derivation is cited on
 * the branch that uses it.
 *
 * <h2>How the opcodes were established</h2>
 * The client's inbound descriptor table is a 129-entry array of 16-byte descriptors based at 950
 * {@code 0x140e942e0}; {@code descriptor(op) == 0x140e942e0 + 16*op}. Every sender takes
 * {@code lea rax, [descriptor]} / {@code lea r8, [descriptor+4]} and calls the allocator
 * {@code 0x1400af710}. Sweeping the whole {@code .text} for rip-relative references into that array
 * therefore recovers (opcode -&gt; sender) exactly, with no name matching and no scoring. The five
 * numbered families come out of that sweep as uniform-stride runs of thunks, which pins the
 * <em>option order</em> as well as the opcode set:
 * <ul>
 * <li>IF_BUTTON 1..10: a 10-entry pointer array at 950 {@code 0x140b63c00}, read by
 *     {@code mov rax, [rdx + r13*8 - 8]} at 950 {@code 0x1401a9916} with r13 = option.</li>
 * <li>object 1..6 and the object-target variant: thunk chain 950 {@code 0x14010bef7} descending by
 *     0x50, each {@code lea rdx, [descriptor]; jmp 0x1400e5110}.</li>
 * <li>NPC 1..6 and the NPC-target variant: thunk chain 950 {@code 0x14010bcc7}, shared writer
 *     {@code 0x1400e56a0}.</li>
 * <li>player 1..10: thunk chain 950 {@code 0x14010b877}, shared writer {@code 0x1400e65d0}.</li>
 * <li>ground item 1..6: switch jump table at 950 {@code 0x1400e65b0} returning the descriptor for
 *     option-1 (dispatcher {@code 0x1400e6560}), writers {@code 0x1400e60ee} / {@code 0x1400e5ad9}.</li>
 * </ul>
 * Every (opcode, size) pair below also equals {@code Native950Protocol.clientSize(opcode)}, which is
 * the client's own descriptor table. {@link #implementedOpcodes()} exists so a test can assert that.
 */
public final class Native950Actions {

    // ---------------------------------------------------------------------------------------
    // Opcode tables. NOTHING here is a 947 opcode: the two revisions renumbered the whole
    // inbound namespace and 112 of the shared opcodes also changed length, so a mechanical
    // substitution produces frames that still parse and mean something else.
    // ---------------------------------------------------------------------------------------

    /** IF_BUTTON options 1..10, in option order, from the pointer array at 950 0x140b63c00. */
    private static final int[] INTERFACE_OPCODES = {18, 122, 89, 100, 81, 126, 49, 66, 31, 59};
    /** Exact-950 IF_BUTTON option, or zero. Used by bounded diagnostics without retaining payloads. */
    public static int interfaceOption(int opcode) { return option(INTERFACE_OPCODES, opcode); }
    /** Object options 1..6, in option order, from the thunk chain at 950 0x14010bef7 (stride -0x50). */
    private static final int[] OBJECT_OPCODES = {34, 48, 24, 41, 73, 79};
    /** NPC options 1..6, in option order, from the thunk chain at 950 0x14010bcc7 (stride -0x50). */
    private static final int[] NPC_OPCODES = {60, 92, 27, 107, 13, 36};
    /** Player options 1..10, in option order, from the thunk chain at 950 0x14010b877 (stride -0x50). */
    private static final int[] PLAYER_OPCODES = {20, 46, 71, 39, 37, 94, 51, 63, 117, 58};
    /** Ground-item options 1..6, in option order, from the jump table at 950 0x1400e65b0. */
    private static final int[] GROUND_ITEM_OPCODES = {127, 103, 22, 56, 52, 113};

    /** Ground click, size 5. Sender 950 0x1400e46b0, body written from 0x1400e4800. */
    private static final int WALK_OPCODE = 88;
    /** Minimap click, size 18. Same sender, same 5-byte prefix; mode byte [entity+0x48] picks it. */
    private static final int MINIMAP_WALK_OPCODE = 78;
    /** Connection keepalive, size 0. Sender 950 0x1400e9d90; the allocator writes only the opcode. */
    private static final int KEEP_ALIVE_OPCODE = 104;
    /** Item-on-item drag, size 18. Sender 950 0x1401abf30. */
    private static final int DRAG_OPCODE = 12;
    /** Dialogue continue / option click, size 6. Sender 950 0x1401abc40. */
    private static final int DIALOGUE_CLICK_OPCODE = 101;
    /** Typed amount, size 8. Sender 950 0x1402055d1 via the i64 writer 0x1400af2e0. */
    private static final int COUNT_DIALOGUE_OPCODE = 120;
    /** Typed free text, size -1. Sender 950 0x140205e5c. */
    private static final int STRING_DIALOGUE_OPCODE = 17;
    /** Typed player name, size -1. Sender 950 0x1402059fc - byte-identical shape to 17. */
    private static final int NAME_DIALOGUE_OPCODE = 53;
    /** Continue / dismiss, size 0. Sender 950 0x140206083. */
    private static final int PAUSE_BUTTON_OPCODE = 11;
    /** The client closed its own modal, size 0. Sender 950 0x1401a05b1. */
    private static final int CLOSE_MODAL_OPCODE = 5;
    /** Public chat, size -1. Sender 950 0x14009b360. */
    private static final int MESSAGE_PUBLIC_OPCODE = 87;
    /** Private chat, size -2. Sender 950 0x14009b6a9. */
    private static final int MESSAGE_PRIVATE_OPCODE = 72;
    /** Audio resource finished / unmuted, size 4. Sender 950 0x14009540e. */
    private static final int MUSIC_ENDED_OPCODE = 110;
    /** Display-mode and window-size report, size 6. Sender 950 0x14014b084. */
    private static final int WINDOW_REPORT_OPCODE = 9;
    /** Scene-build duration report, size 4. Sender 950 0x1401197ef. */
    private static final int MAP_BUILD_REPORT_OPCODE = 98;
    /** World-list refresh request carrying the client's cached checksum, size 4. Sender 950 0x14016469a. */
    private static final int WORLDLIST_FETCH_OPCODE = 108;
    /** Selected inventory/UI item used on another item. Native sender0x1400e2c8d, size18. */
    private static final int ITEM_ON_ITEM_OPCODE = 69;
    /** Selected inventory/UI item targets an object or NPC; native writers cited below. */
    private static final int ITEM_ON_OBJECT_OPCODE = 90, ITEM_ON_NPC_OPCODE = 19;

    /**
     * Rows whose framing is known but whose <em>meaning</em> is not derived on 950. They are listed
     * here so "we chose not to decode this" is visible and testable, instead of looking like an
     * oversight. {@link #isImplemented(int)} is false for all of them, so the transport frames them,
     * counts them and drops them; it never manufactures an action and never closes the connection.
     *
     * <p>Pairs are {opcode, reason index into {@link #UNDERIVED_REASONS}}.
     */
    private static final int[][] UNDERIVED = {
        {112, 0},                          // remaining selected-ground-item target variant
        {114, 1}, {10, 1}, {32, 1},          // the three count-int rows
        {97, 2}, {128, 2}, {75, 2},          // camera / focus reports
    };

    private static final String[] UNDERIVED_REASONS = {
        "target variant for a ground item: selection identity is derived, but its additional"
            + " ground-target flags and gameplay relationship are not admitted; no menu enables it.",
        "count-int row: opcode and size are derived (114 is the 947 opcode-32 sender, 950 0x1402052ea"
            + " writes a plain BE i32; 950 opcodes 10 and 32 write a plain BE u16 at 0x1402057d7 and"
            + " 0x140205c37) but no sink was traced for any of them, so the number has no meaning"
            + " yet. 947 left its opcode 32 unsupported for the same reason.",
        "camera / focus report: framing is known from the descriptor table, the sender is known"
            + " (950 0x1400fde7a for 97, 0x1401c3dca for 128, 0x140156fa1 for 75) and the payload"
            + " has no derived field list.",
    };

    /**
     * Colour prefix codes 0..11 and effect prefix codes 0..5, re-derived on the 950 binary rather
     * than carried over: the localized prefix strings live in one flat table with a 0x38 stride
     * (7 language slots per entry). Colours run yellow/red/green/cyan/purple/white/flash1..3/
     * glow1..3 from 950 0x140c6d220 to 0x140c6d488 = 12 entries, so the code is 0..11. Effects run
     * wave/wave2/shake/scroll/slide from 950 0x140c6d4c0 to 0x140c6d5a0 = 5 entries, and code 0 is
     * "no prefix", so the code is 0..5.
     *
     * <p>These bounds are load-bearing in an unusual direction: the transport treats a null decode
     * of an implemented opcode as malformed and <em>closes the connection</em>, so a bound that is
     * too tight disconnects a player for typing a legal prefix. Widen before narrowing.
     */
    private static final int PUBLIC_CHAT_MAX_COLOUR = 11;
    private static final int PUBLIC_CHAT_MAX_EFFECT = 5;

    /** Coordinates are 14 bits on the wire; anything wider is a forged frame. */
    private static final int MAX_COORDINATE = 16383;

    private static final AtomicLong HUFFMAN_UNAVAILABLE = new AtomicLong();
    private static volatile Huffman950 huffman;
    private static volatile boolean cacheProbed;

    /** Immutable request only; the world still validates the target and ownership. */
    public interface Action { }

    public static Action decode(Native950InboundDecoder.Frame frame) {
        return decode(frame.opcode(), frame.payload());
    }

    /** Null means unrelated or malformed; {@link #isImplemented(int)} distinguishes those cases. */
    public static Action decode(int opcode, byte[] payload) {
        if (opcode == WALK_OPCODE || opcode == MINIMAP_WALK_OPCODE) return decodeWalk(opcode, payload);
        if (opcode == DRAG_OPCODE) return decodeDrag(payload);
        if (opcode == ITEM_ON_ITEM_OPCODE) return decodeItemOnItem(payload);
        if (opcode == ITEM_ON_OBJECT_OPCODE) return decodeItemOnObject(payload);
        if (opcode == ITEM_ON_NPC_OPCODE) return decodeItemOnNpc(payload);
        int option = option(INTERFACE_OPCODES, opcode);
        if (option != 0) return decodeInterface(option, payload);
        option = option(OBJECT_OPCODES, opcode);
        if (option != 0) return decodeObject(option, payload);
        option = option(NPC_OPCODES, opcode);
        if (option != 0) return decodeNpc(option, payload);
        option = option(PLAYER_OPCODES, opcode);
        if (option != 0) return decodePlayer(option, payload);
        option = option(GROUND_ITEM_OPCODES, opcode);
        if (option != 0) return decodeGroundItem(option, payload);
        if (opcode == KEEP_ALIVE_OPCODE || opcode == PAUSE_BUTTON_OPCODE || opcode == CLOSE_MODAL_OPCODE) {
            // 950 senders 0x1400e9d90 (104), 0x140206083 (11) and 0x1401a05b1 (5) call the
            // allocator and immediately hand the frame to the queue: no byte is ever stamped, so
            // a compliant frame carries no body at all.
            if (payload == null || payload.length != 0) return null;
            return opcode == KEEP_ALIVE_OPCODE ? KeepAliveAction.INSTANCE
                    : opcode == PAUSE_BUTTON_OPCODE ? (Action) PauseButtonAction.INSTANCE
                    : CloseModalAction.INSTANCE;
        }
        if (opcode == COUNT_DIALOGUE_OPCODE) return decodeCountDialogue(payload);
        if (opcode == STRING_DIALOGUE_OPCODE || opcode == NAME_DIALOGUE_OPCODE)
            return decodeStringDialogue(opcode, payload);
        if (opcode == DIALOGUE_CLICK_OPCODE) return decodeDialogueClick(payload);
        if (opcode == MESSAGE_PUBLIC_OPCODE) return decodePublicChat(payload);
        if (opcode == MESSAGE_PRIVATE_OPCODE) return decodePrivateChat(payload);
        if (opcode == MUSIC_ENDED_OPCODE) {
            // 950 0x14009540e: `bswap ebx` then one 4-byte store - a plain big-endian signed int,
            // with no bias and no fold. Unchanged in shape from the 947 row, only renumbered.
            if (payload == null || payload.length != 4) return null;
            return new MusicEndedAction(intBE(payload, 0));
        }
        if (opcode == WINDOW_REPORT_OPCODE) return decodeWindowReport(payload);
        if (opcode == MAP_BUILD_REPORT_OPCODE) {
            // 950 0x1401197ef: `mov edx,[globalTimer]; sub edx,[scene+0x6a8]; bswap edx` - the
            // elapsed build time, big-endian, as one signed int.
            if (payload == null || payload.length != 4) return null;
            return new MapBuildReportAction(intBE(payload, 0));
        }
        if (opcode == WORLDLIST_FETCH_OPCODE) {
            // 950 0x14016469a: the cached world-list checksum (0 when the client holds no list),
            // `bswap edx` then one 4-byte store.
            if (payload == null || payload.length != 4) return null;
            return new WorldListFetchAction(intBE(payload, 0));
        }
        return null;
    }

    public static boolean isImplemented(int opcode) {
        return opcode == WALK_OPCODE || opcode == MINIMAP_WALK_OPCODE || opcode == DRAG_OPCODE || opcode == ITEM_ON_ITEM_OPCODE
                || opcode == ITEM_ON_OBJECT_OPCODE || opcode == ITEM_ON_NPC_OPCODE
                || option(INTERFACE_OPCODES, opcode) != 0 || option(OBJECT_OPCODES, opcode) != 0
                || option(NPC_OPCODES, opcode) != 0 || option(PLAYER_OPCODES, opcode) != 0
                || option(GROUND_ITEM_OPCODES, opcode) != 0
                || opcode == COUNT_DIALOGUE_OPCODE || opcode == STRING_DIALOGUE_OPCODE
                || opcode == NAME_DIALOGUE_OPCODE || opcode == PAUSE_BUTTON_OPCODE
                || opcode == CLOSE_MODAL_OPCODE || opcode == KEEP_ALIVE_OPCODE
                || opcode == DIALOGUE_CLICK_OPCODE || opcode == MUSIC_ENDED_OPCODE
                || opcode == WINDOW_REPORT_OPCODE || opcode == MAP_BUILD_REPORT_OPCODE
                || opcode == WORLDLIST_FETCH_OPCODE || isChatOpcode(opcode);
    }

    /** The two huffman-compressed client rows; the transport gives them their own drop lane. */
    public static boolean isChatOpcode(int opcode) {
        return opcode == MESSAGE_PUBLIC_OPCODE || opcode == MESSAGE_PRIVATE_OPCODE;
    }

    /** Opcode 104 is a timer-driven keepalive, never a game request; the transport must not queue it. */
    public static boolean isKeepAliveOpcode(int opcode) { return opcode == KEEP_ALIVE_OPCODE; }

    /**
     * Every opcode this decoder turns into an {@link Action}, ascending. A test uses it to assert
     * that each one's payload length agrees with the client's own descriptor table
     * ({@code Native950Protocol.clientSize}); the two tables were derived independently, so a
     * disagreement means one of them is wrong.
     */
    public static int[] implementedOpcodes() {
        int[] all = new int[INTERFACE_OPCODES.length + OBJECT_OPCODES.length + NPC_OPCODES.length
                + PLAYER_OPCODES.length + GROUND_ITEM_OPCODES.length + 19];
        int at = 0;
        for (int opcode : INTERFACE_OPCODES) all[at++] = opcode;
        for (int opcode : OBJECT_OPCODES) all[at++] = opcode;
        for (int opcode : NPC_OPCODES) all[at++] = opcode;
        for (int opcode : PLAYER_OPCODES) all[at++] = opcode;
        for (int opcode : GROUND_ITEM_OPCODES) all[at++] = opcode;
        int[] singles = {WALK_OPCODE, MINIMAP_WALK_OPCODE, KEEP_ALIVE_OPCODE, DRAG_OPCODE,
                DIALOGUE_CLICK_OPCODE, COUNT_DIALOGUE_OPCODE, STRING_DIALOGUE_OPCODE,
                NAME_DIALOGUE_OPCODE, PAUSE_BUTTON_OPCODE, CLOSE_MODAL_OPCODE,
                MESSAGE_PUBLIC_OPCODE, MESSAGE_PRIVATE_OPCODE, MUSIC_ENDED_OPCODE,
                WINDOW_REPORT_OPCODE, MAP_BUILD_REPORT_OPCODE, WORLDLIST_FETCH_OPCODE, ITEM_ON_ITEM_OPCODE,
                ITEM_ON_OBJECT_OPCODE, ITEM_ON_NPC_OPCODE};
        for (int opcode : singles) all[at++] = opcode;
        Arrays.sort(all);
        return all;
    }

    /**
     * Why this opcode is deliberately left unhandled, or null when it is not one of the named
     * rows. See {@link #UNDERIVED}: these are framed and counted, never decoded.
     */
    public static String underivedReason(int opcode) {
        for (int[] row : UNDERIVED) if (row[0] == opcode) return UNDERIVED_REASONS[row[1]];
        return null;
    }

    /**
     * Fail-closed guard for callers that want an action for one of the named-but-underived rows.
     * Inbound frames themselves must never throw - dropping the connection over a packet the client
     * is entitled to send is worse than ignoring it - so {@link #decode(int, byte[])} returns null
     * and the transport counts it. This method exists for code that would otherwise <em>invent</em>
     * a meaning.
     */
    public static void requireDerived(int opcode) {
        String reason = underivedReason(opcode);
        if (reason != null)
            throw new UnsupportedOperationException("Native 950 client opcode " + opcode
                    + " has no derived meaning: " + reason);
        if (!isImplemented(opcode))
            throw new UnsupportedOperationException("Native 950 client opcode " + opcode
                    + " has no derived meaning; its size is known from the client descriptor table"
                    + " but no sender was traced to a field list.");
    }

    // ---------------------------------------------------------------------------------------
    // Movement
    // ---------------------------------------------------------------------------------------

    public static WalkRequest decodeWalk(Native950InboundDecoder.Frame frame) {
        return decodeWalk(frame.opcode(), frame.payload());
    }

    /**
     * Ground click (88, 5) and minimap click (78, 18).
     *
     * <p>950 sender 0x1400e46b0. Field {@code [entity+0x48]} selects the descriptor - 0 takes
     * {@code lea rax,[0x140e94860]} = opcode 88 at 0x1400e47a4, 1 takes
     * {@code lea rax,[0x140e947c0]} = opcode 78 at 0x1400e47c1 - and then <b>both</b> modes fall
     * into the same five stores at 0x1400e4800..0x1400e4869, so the prefix is identical and the
     * minimap row's extra 13 bytes are written later (from 0x1400e48d8) and carry no destination.
     *
     * <pre>
     *   b0..b1  [entity+0x50] = y, plain big-endian     0x1400e4804 movzx + ror dx,8 + one word store
     *   b2      modifier + 0x80                          0x1400e4812 add bpl,0x80
     *   b3      (x &amp; 255) + 0x80                     0x1400e484b lea ecx,[r8+0x80]
     *   b4      x &gt;&gt;&gt; 8                        0x1400e4852 sar r8d,8
     * </pre>
     *
     * <p>Three things moved versus 947, and none of them fails loudly if missed: y lost its +128
     * bias and x gained one, x moved from b2..b3 (plain BE) to b3..b4 (low byte first, biased), and
     * <b>the modifier sign flipped</b> - 947 sent {@code 128 - m} and 950 sends {@code m + 128}
     * (0x1400e4812 {@code add}, where 947's writer subtracted). Reading a 947 modifier here turns
     * every ordinary click into a modified one and vice versa.
     */
    public static WalkRequest decodeWalk(int opcode, byte[] payload) {
        int expected = opcode == WALK_OPCODE ? 5 : opcode == MINIMAP_WALK_OPCODE ? 18 : -1;
        if (expected < 0 || payload == null || payload.length != expected) return null;
        int y = shortBE(payload, 0);
        int x = ((payload[4] & 255) << 8) | ((payload[3] - 128) & 255);
        int modifier = (payload[2] - 128) & 255;
        if (x > MAX_COORDINATE || y > MAX_COORDINATE || modifier > 1) return null;
        return new WalkRequest(x, y, modifier, opcode == MINIMAP_WALK_OPCODE);
    }

    // ---------------------------------------------------------------------------------------
    // Numbered option families
    // ---------------------------------------------------------------------------------------

    /**
     * IF_BUTTON 1..10, size 9 each. 950 writer 0x1401a9980, called from 0x1401a9929 with the
     * descriptor picked out of the 10-entry array at 0x140b63c00 by option index.
     *
     * <pre>
     *   b0..b2  item, big-endian u24        0x1401a99db sar 0x10 / 0x1401a99f8 sar 8 / 0x1401a9a1a r8b
     *   b3..b6  component hash, intv2       0x1401a9a24 sar 0x10, 0x1401a9a3c sar 0x18, dil, sar 8
     *   b7..b8  slot, big-endian u16        0x1401a9a94 shl/or byte swap then one word store
     * </pre>
     *
     * <p>Both fields changed shape versus 947: the item was a u16 and is now a <b>big-endian u24</b>
     * (a stale u16 reader consumes the hash's first byte as the item's low byte and shifts every
     * field after it), and the hash byte order changed. 947 read [h, h&gt;&gt;&gt;8, h&gt;&gt;&gt;24,
     * h&gt;&gt;&gt;16]; 950 writes intv2 = [h&gt;&gt;&gt;16, h&gt;&gt;&gt;24, h, h&gt;&gt;&gt;8].
     */
    private static InterfaceAction decodeInterface(int option, byte[] payload) {
        if (payload == null || payload.length != 9) return null;
        int item = ((payload[0] & 255) << 16) | ((payload[1] & 255) << 8) | (payload[2] & 255);
        int hash = ((payload[3] & 255) << 16) | ((payload[4] & 255) << 24)
                | (payload[5] & 255) | ((payload[6] & 255) << 8);
        return new InterfaceAction(option, hash, sentinel16(shortBE(payload, 7)), sentinel24(item));
    }

    /**
     * Object options 1..6, size 9 each. 950 writer 0x1400e5110; the plain path starts at
     * 0x1400e5377 (the branch at 0x1400e5165 diverts the size-18 target variant, opcode 90).
     *
     * <pre>
     *   b0      (y &amp; 255) + 0x80        0x1400e538f lea ecx,[r8+0x80] on [entity+0x50]
     *   b1      y &gt;&gt;&gt; 8           0x1400e5396 sar r8d,8
     *   b2..b5  object id, big-endian i32   0x1400e53c4 bswap edx on [entity+0x48], one dword store
     *   b6      x &gt;&gt;&gt; 8           0x1400e53e9 sar ecx,8 on [entity+0x4c]
     *   b7      (x &amp; 255) + 0x80        0x1400e53f0 add r8b,0x80
     *   b8      -modifier                   0x1400e5432 neg dl, so 0x00 or 0xFF
     * </pre>
     *
     * <p>x is {@code [entity+0x4c]} and y is {@code [entity+0x50]} throughout the client; the pair
     * is passed as (x, y) to the pathfinder at 0x1400e48cc. Getting them the wrong way round sends
     * the player to the transposed tile, which is a legal tile, so nothing complains.
     */
    private static ObjectAction decodeObject(int option, byte[] payload) {
        if (payload == null || payload.length != 9) return null;
        int y = ((payload[1] & 255) << 8) | ((payload[0] - 128) & 255);
        int id = intBE(payload, 2);
        int x = ((payload[6] & 255) << 8) | ((payload[7] - 128) & 255);
        int modifier = (-payload[8]) & 255;
        if (x > MAX_COORDINATE || y > MAX_COORDINATE || id < 0 || modifier > 1) return null;
        return new ObjectAction(option, id, x, y, modifier);
    }

    /**
     * NPC options 1..6, size 3 each. 950 writer 0x1400e56a0; the plain path starts at 0x1400e5802
     * (the branch at 0x1400e56f5 diverts the size-12 target variant, opcode 19).
     *
     * <pre>
     *   b0      modifier, PLAIN 0 or 1      0x1400e5817 shr edx,2 / and dl,1 - then stored as-is
     *   b1      index &gt;&gt;&gt; 8       0x1400e5833 sar ecx,8 on [npc+0x48]
     *   b2      (index &amp; 255) + 0x80    0x1400e583e add r8b,0x80
     * </pre>
     *
     * <p>The modifier is the one field that is <em>not</em> transformed. The object family negates
     * the same bit (0x1400e5432) and the player family biases it by 128 (0x1400e6670); this family
     * stores it raw. A decoder that applies the object family's {@code -m} here reads 0 as 0 and 1
     * as 255, so the range check below is the only thing that would notice.
     */
    private static NpcAction decodeNpc(int option, byte[] payload) {
        if (payload == null || payload.length != 3) return null;
        int modifier = payload[0] & 255;
        int index = ((payload[1] & 255) << 8) | ((payload[2] - 128) & 255);
        if (index == 65535 || modifier > 1) return null;
        return new NpcAction(option, index, modifier);
    }

    /**
     * Player options 1..10, size 3 each. 950 writer 0x1400e65d0.
     *
     * <pre>
     *   b0      index &amp; 255             0x1400e662f dl of [player+0x48]
     *   b1      index &gt;&gt;&gt; 8       0x1400e6636 sar edx,8
     *   b2      modifier + 0x80             0x1400e6670 add dl,0x80
     * </pre>
     *
     * <p>Note the index is little-endian here and big-endian-with-bias in the NPC family; the two
     * are not interchangeable even though both are 3-byte frames.
     */
    private static PlayerAction decodePlayer(int option, byte[] payload) {
        if (payload == null || payload.length != 3) return null;
        int index = ((payload[1] & 255) << 8) | (payload[0] & 255);
        int modifier = (payload[2] - 128) & 255;
        if (index == 65535 || modifier > 1) return null;
        return new PlayerAction(option, index, modifier);
    }

    /**
     * Ground-item options 1..6, size 8 each. Two 950 writers share the layout - 0x1400e60ee (the
     * direct click) and 0x1400e5ad9 (the walk-then-take path) - and only the first byte differs
     * between them.
     *
     * <pre>
     *   b0      0x80 - (modifier | flag2)   0x1400e615c sub cl,r8b; 0x1400e5b6a with flag2 or'd in
     *   b1..b2  x, big-endian u16           0x1400e6174 movzx + ror r8w,8, one word store
     *   b3..b4  y, big-endian u16           0x1400e619e same shape
     *   b5..b7  item, big-endian u24        0x1400e61e3 sar 0x10, 0x1400e61fd sar 8, then r8b
     * </pre>
     *
     * <p>flag2 is bit 1 of that first field: 0x1400e5b4c reads a byte at {@code [client+0x19928]+0x68},
     * {@code neg cl; sbb al,al; and al,2} turns "non-zero" into 2, and it is or'd with the modifier
     * bit <em>before</em> the subtraction. So the wire byte is one of 0x80, 0x7F, 0x7E, 0x7D and a
     * decoder that only accepts 0x80/0x7F drops half the legal frames - which, because the transport
     * treats a null decode of an implemented opcode as malformed, would disconnect the player.
     */
    private static GroundItemAction decodeGroundItem(int option, byte[] payload) {
        if (payload == null || payload.length != 8) return null;
        int flags = (128 - payload[0]) & 255;
        if (flags > 3) return null;
        int x = shortBE(payload, 1), y = shortBE(payload, 3);
        int item = ((payload[5] & 255) << 16) | ((payload[6] & 255) << 8) | (payload[7] & 255);
        if (x > MAX_COORDINATE || y > MAX_COORDINATE) return null;
        return new GroundItemAction(option, sentinel24(item), x, y, flags & 1, (flags >>> 1) & 1);
    }

    /**
     * Native sender0x1400e2cc6..0x1400e2ebe. Selected source state is independently assigned
     * by0x14019f4c7/+0x25c (component hash),0x14019f4df/+0x260 (child slot), and
     *0x14019f50b/+0x264 (item getter). This is a targeted use, never a drag/swap.
     */
    private static ItemOnItemAction decodeItemOnItem(byte[] payload) {
        if (payload == null || payload.length != 18) return null;
        int sourceSlot = ((payload[0] & 255) << 8) | ((payload[1] - 128) & 255);
        int sourceItem = ((payload[2] & 255) << 16) | (payload[3] & 255) | ((payload[4] & 255) << 8);
        int targetHash = ((payload[5] & 255) << 8) | (payload[6] & 255)
                | ((payload[7] & 255) << 24) | ((payload[8] & 255) << 16);
        int targetSlot = ((payload[9] & 255) << 8) | (payload[10] & 255);
        int sourceHash = ((payload[11] & 255) << 8) | (payload[12] & 255)
                | ((payload[13] & 255) << 24) | ((payload[14] & 255) << 16);
        int targetItem = (payload[15] & 255) | ((payload[16] & 255) << 8) | ((payload[17] & 255) << 16);
        return new ItemOnItemAction(sourceHash, sentinel16(sourceSlot), sentinel24(sourceItem),
                targetHash, sentinel16(targetSlot), sentinel24(targetItem));
    }

    /**
     * Selected object writer950 0x1400e516b..0x1400e5372, descriptor0x140e94880 (90).
     * b0 modifierNeg; b1..2 x BE128; b3..4 selected slot BE; b5..6 y LE128;
     * b7..9 selected item LE24; b10..13 object LE32; b14..17 selected hash intv1.
     * The shared selection setter0x14019f4c7/4df/50b establishes hash/slot/item identity.
     */
    private static ItemOnObjectAction decodeItemOnObject(byte[] p) {
        if (p == null || p.length != 18) return null;
        int modifier = (-p[0]) & 255;
        int x = ((p[1] & 255) << 8) | ((p[2] - 128) & 255);
        int slot = shortBE(p, 3);
        int y = ((p[6] & 255) << 8) | ((p[5] - 128) & 255);
        int item = (p[7] & 255) | ((p[8] & 255) << 8) | ((p[9] & 255) << 16);
        int object = (p[10] & 255) | ((p[11] & 255) << 8) | ((p[12] & 255) << 16) | ((p[13] & 255) << 24);
        int hash = ((p[14] & 255) << 8) | (p[15] & 255) | ((p[16] & 255) << 24) | ((p[17] & 255) << 16);
        if (modifier > 1 || x > MAX_COORDINATE || y > MAX_COORDINATE || object < 0) return null;
        return new ItemOnObjectAction(hash, sentinel16(slot), sentinel24(item), object, x, y, modifier);
    }

    /**
     * Selected NPC writer950 0x1400e56fb..0x1400e5800, descriptor0x140e94410 (19).
     * b0..1 NPC index BE; b2..4 selected item LE24; b5 modifierNeg;
     * b6..7 selected slot LE; b8..11 selected hash BE32.
     */
    private static ItemOnNpcAction decodeItemOnNpc(byte[] p) {
        if (p == null || p.length != 12) return null;
        int index = shortBE(p, 0);
        int item = (p[2] & 255) | ((p[3] & 255) << 8) | ((p[4] & 255) << 16);
        int modifier = (-p[5]) & 255;
        int slot = (p[6] & 255) | ((p[7] & 255) << 8);
        int hash = ((p[8] & 255) << 24) | ((p[9] & 255) << 16) | ((p[10] & 255) << 8) | (p[11] & 255);
        if (modifier > 1) return null;
        return new ItemOnNpcAction(hash, sentinel16(slot), sentinel24(item), index, modifier);
    }

    /**
     * Item-on-item drag, opcode 12, size 18. 950 writer 0x1401abf30. Nothing in this frame is
     * biased by 128; the client folds a missing slot or component from 0xFFFF to -1 before writing.
     *
     * <pre>
     *   b0..b1   source slot, little-endian u16     0x1401abf91 dl then 0x1401abf98 sar edx,8
     *   b2..b5   source hash, intv1                 0x1401abfd5 sar 8, r9b, sar 0x18, sar 0x10
     *   b6..b8   source item, little-endian u24     0x1401ac048 r9b, sar 8, sar 0x10
     *   b9..b10  target slot, little-endian u16     0x1401ac09e / 0x1401ac0a5
     *   b11..b14 target hash, intv2                 0x1401ac0e2 sar 0x10, sar 0x18, r9b, sar 8
     *   b15..b17 target item as [i&gt;&gt;&gt;16, i, i&gt;&gt;&gt;8]  0x1401ac151 / 0x1401ac174 / 0x1401ac17c
     * </pre>
     *
     * <p>The two hashes use <em>different</em> byte orders (intv1 for the source, intv2 for the
     * target) and the two item fields use different byte orders as well. That is not a transcription
     * slip: it is what the writer does, and symmetrising it silently swaps the drag's endpoints.
     */
    private static DragAction decodeDrag(byte[] payload) {
        if (payload == null || payload.length != 18) return null;
        int sourceSlot = ((payload[1] & 255) << 8) | (payload[0] & 255);
        int sourceHash = ((payload[2] & 255) << 8) | (payload[3] & 255)
                | ((payload[4] & 255) << 24) | ((payload[5] & 255) << 16);
        int sourceItem = (payload[6] & 255) | ((payload[7] & 255) << 8) | ((payload[8] & 255) << 16);
        int targetSlot = ((payload[10] & 255) << 8) | (payload[9] & 255);
        int targetHash = ((payload[11] & 255) << 16) | ((payload[12] & 255) << 24)
                | (payload[13] & 255) | ((payload[14] & 255) << 8);
        int targetItem = ((payload[15] & 255) << 16) | (payload[16] & 255) | ((payload[17] & 255) << 8);
        return new DragAction(sourceHash, sentinel16(sourceSlot), sentinel24(sourceItem),
                targetHash, sentinel16(targetSlot), sentinel24(targetItem));
    }

    // ---------------------------------------------------------------------------------------
    // Chatbox and dialogue
    // ---------------------------------------------------------------------------------------

    /**
     * Typed amount, opcode 120, size 8. 950 sender 0x1402055d1 hands the parsed value straight to
     * the i64 writer 0x1400af2e0, which byte-swaps it on a little-endian host (the constant
     * 0x3020100 compare at 0x1400af2e0 is the host-order probe) and stores all eight bytes. So the
     * count arrives signed and big-endian, and {@code -1} arrives as ff*8.
     */
    private static CountDialogueAction decodeCountDialogue(byte[] payload) {
        if (payload == null || payload.length != 8) return null;
        long count = 0;
        for (int index = 0; index < 8; index++) count = (count << 8) | (payload[index] & 255L);
        return new CountDialogueAction(count);
    }

    /**
     * Typed free text (17) and typed name (53), size -1. Their payload is the
     * CP1252 text followed by one NUL, after transport removes the sole length byte.
     *
     * <p>Senders 0x140205e5c and 0x1402059fc use the same opcode-only allocator
     * 0x1400af710 as chat. They write the encoded byte count including NUL at
     * 0x140205e98 / 0x140205a38, then the terminated string through 0x1400ae0c0
     * at 0x140205eca / 0x140205a6a. That count is framing, not a second body field.
     * Which identical string row belongs to the name prompt remains provisional.
     */
    private static StringDialogueAction decodeStringDialogue(int opcode, byte[] payload) {
        if (payload == null || payload.length < 1) return null;
        if (payload[payload.length - 1] != 0) return null;
        for (int index = 0; index < payload.length - 1; index++) if (payload[index] == 0) return null;
        String text = cp1252(payload, 0, payload.length - 1);
        return text == null ? null : new StringDialogueAction(opcode == NAME_DIALOGUE_OPCODE, text);
    }

    /**
     * Dialogue continue / option click, opcode 101, size 6. 950 sender 0x1401abc40.
     *
     * <pre>
     *   b0..b3  component hash, plain big-endian i32   0x1401abcac bswap edx, one dword store
     *   b4      slot &gt;&gt;&gt; 8                  0x1401abcf0 sar ecx,8
     *   b5      (slot &amp; 255) + 0x80                0x1401abced add dl,0x80
     * </pre>
     *
     * <p>The two fields are <b>swapped relative to 947</b>, which sent the slot first. Both orders
     * produce a well-formed 6-byte frame, so reading the 947 order gives a component hash built out
     * of the slot's two bytes and half the hash - a real but different component - and the click
     * lands somewhere plausible.
     */
    private static DialogueClickAction decodeDialogueClick(byte[] payload) {
        if (payload == null || payload.length != 6) return null;
        int hash = intBE(payload, 0);
        int slot = ((payload[4] & 255) << 8) | ((payload[5] - 128) & 255);
        return new DialogueClickAction(hash, sentinel16(slot));
    }

    /**
     * Public chat, opcode 87, size -1. Body after Native950InboundDecoder removes framing:
     * colour, effect, unsigned-smart character count, Huffman bits.
     *
     * <p>The allocator 0x1400af710 writes only the encrypted opcode (0x1400af856).
     * The zero at 0x14009b3a4, back-filled at 0x14009b3ff, is the packet's sole
     * variable-byte transport length, not an additional body field. Colour and effect
     * follow at 0x14009b3bc/0x14009b3cf; 0x14009b3e1 calls the Huffman writer.
     * The sender queues the entire existing buffer at 0x14009b484..0x14009b49c.
     */
    private static PublicChatAction decodePublicChat(byte[] payload) {
        if (payload == null || payload.length < 3) return null;
        int colour = payload[0] & 255, effect = payload[1] & 255;
        if (colour > PUBLIC_CHAT_MAX_COLOUR || effect > PUBLIC_CHAT_MAX_EFFECT) return null;
        String text = huffmanText(payload, 2);
        return text == null ? null : new PublicChatAction(colour, effect, text);
    }

    /**
     * Private chat, opcode 72, size -2. Body after transport framing: recipient
     * (CP1252, NUL terminated), unsigned-smart character count, Huffman bits.
     *
     * <p>The same allocator writes only the opcode. The word placeholder at
     * 0x14009b6e4, back-filled big-endian at 0x14009b7c8, is the sole transport
     * length. The recipient writer is called at 0x14009b71c and Huffman at
     * 0x14009b772. Neither of those length bytes is part of this method's payload.
     */
    private static PrivateChatAction decodePrivateChat(byte[] payload) {
        if (payload == null || payload.length < 2) return null;
        int terminator = -1;
        for (int index = 0; index < payload.length; index++)
            if (payload[index] == 0) { terminator = index; break; }
        if (terminator < 0) return null;
        String recipient = cp1252(payload, 0, terminator);
        if (recipient == null) return null;
        String text = huffmanText(payload, terminator + 1);
        return text == null ? null : new PrivateChatAction(recipient, text);
    }

    /**
     * Display-mode and window-size report, opcode 9, size 6. 950 sender 0x14014b084.
     *
     * <pre>
     *   b0      display mode      0x14014b0d2 setne dl; add edx,2 - so 2 (fixed) or 3 (resizable)
     *   b1..b2  width, BE u16     0x14014b103 call, ror ax,8 at 0x14014b114, one word store
     *   b3..b4  height, BE u16    0x14014b13b call, ror ax,8 at 0x14014b14c
     *   b5      a client flag     0x14014b174 movzx byte [ ... +0x200]
     * </pre>
     *
     * <p>The last byte's meaning is not derived; it is exposed verbatim and named as a flag rather
     * than guessed at. Nothing on the server acts on this packet today - it is a report - so it is
     * decoded to keep it out of the unhandled-opcode lane, not to drive anything.
     */
    private static WindowReportAction decodeWindowReport(byte[] payload) {
        if (payload == null || payload.length != 6) return null;
        return new WindowReportAction(payload[0] & 255, shortBE(payload, 1), shortBE(payload, 3),
                payload[5] & 255);
    }

    /**
     * Reads the smart length and the huffman block that follows it, which must consume exactly the
     * rest of the payload. Returns null when the frame is malformed and also when no huffman table
     * is installed, in which case the failure is counted rather than guessed at; see
     * {@link #installHuffman(byte[])}.
     *
     * <p>The smart encoding is the client's own: 0x1403eda55 compares the character count against
     * 0x80 and writes one byte below it, otherwise a big-endian ushort with 0x8000 set.
     */
    private static String huffmanText(byte[] payload, int offset) {
        if (offset >= payload.length) return null;
        int first = payload[offset] & 255;
        int characters, headerWidth;
        if (first < 128) { characters = first; headerWidth = 1; }
        else {
            if (offset + 1 >= payload.length) return null;
            characters = (((first << 8) | (payload[offset + 1] & 255)) & 0x7fff);
            headerWidth = 2;
        }
        int start = offset + headerWidth, available = payload.length - start;
        if (available < 0) return null;
        if (characters == 0) return available == 0 ? "" : null;
        Huffman950 codec = codec();
        if (codec == null) { HUFFMAN_UNAVAILABLE.incrementAndGet(); return null; }
        byte[] decoded = new byte[characters];
        if (codec.decode(payload, start, available, decoded) != available) return null;
        return cp1252(decoded, 0, characters);
    }

    /**
     * Installs the 256-byte cache bit-length table used to rebuild the client's huffman codes
     * (mask = 1 &lt;&lt; (32 - size) with the keys[33] carry loop). The verified source is 950 cache
     * index 10, the group named "huffman" (group 1, file 0), 256 bytes,
     * sha {@code 77946046...}; the plan confirms it exists in the 950 cache and that ' '=3, 'h'=5,
     * 'i'=4 bits, i.e. the same code lengths the 947 worked example uses. Chat decoding fails closed
     * until a table is here.
     */
    public static void installHuffman(byte[] table) {
        if (table == null || table.length != 256)
            throw new IllegalArgumentException("Native 950 huffman table must be 256 bit lengths");
        huffman = Huffman950.build(table);
        cacheProbed = true;
    }

    /** False means every public and private chat frame is dropped, never guessed. */
    public static boolean isHuffmanInstalled() { return codec() != null; }

    /** Chat frames dropped because no huffman table was available. */
    public static long huffmanUnavailableCount() { return HUFFMAN_UNAVAILABLE.get(); }

    /** Forgets the installed table so the next chat frame re-probes the cache. */
    public static void resetHuffman() { huffman = null; cacheProbed = false; }

    private static Huffman950 codec() {
        Huffman950 codec = huffman;
        if (codec != null || cacheProbed) return codec;
        return probeCache();
    }

    /**
     * One attempt to read the verified table straight out of an already-open flat 950 cache.
     * Anything else - a legacy store, a missing group, a wrong length, a missing class - leaves the
     * codec absent so chat fails closed instead of being guessed.
     */
    private static synchronized Huffman950 probeCache() {
        if (huffman != null || cacheProbed) return huffman;
        cacheProbed = true;
        try {
            if (!com.rs.cache.Cache.isFlatReadOnly()) return null;
            com.rs.cache.filestore.store.Index index = com.rs.cache.Cache.STORE.getIndexes()[10];
            int group = index.getArchiveId("huffman");
            if (group < 0) return null;
            byte[] table = index.getFile(group);
            if (table == null || table.length != 256) return null;
            huffman = Huffman950.build(table);
        } catch (Throwable unavailable) {
            huffman = null;
        }
        return huffman;
    }

    private static String cp1252(byte[] bytes, int offset, int length) {
        try {
            CharsetDecoder decoder = Charset.forName("windows-1252").newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(bytes, offset, length)).toString();
        } catch (CharacterCodingException notRepresentable) {
            return null;
        } catch (RuntimeException notRepresentable) {
            return null;
        }
    }

    /**
     * Canonicalizes one verified inventory surface after the caller has checked its UI ownership.
     * This does not validate possession, slots or targets and must not be enabled globally.
     * Every untrusted field except an exactly matching component hash is preserved verbatim.
     */
    public static Action aliasInventorySurface(Action action, int fromHash, int toHash) {
        if (fromHash == toHash) return action;
        if (action instanceof InterfaceAction) {
            InterfaceAction a = (InterfaceAction) action;
            return a.componentHash != fromHash ? action
                    : new InterfaceAction(a.option, toHash, a.slot, a.itemId);
        }
        if (action instanceof DragAction) {
            DragAction a = (DragAction) action;
            if (a.sourceHash != fromHash && a.targetHash != fromHash) return action;
            return new DragAction(a.sourceHash == fromHash ? toHash : a.sourceHash, a.sourceSlot, a.sourceItemId,
                    a.targetHash == fromHash ? toHash : a.targetHash, a.targetSlot, a.targetItemId);
        }
        if (action instanceof ItemOnItemAction) {
            ItemOnItemAction a = (ItemOnItemAction) action;
            if (a.sourceHash != fromHash && a.targetHash != fromHash) return action;
            return new ItemOnItemAction(a.sourceHash == fromHash ? toHash : a.sourceHash, a.sourceSlot, a.sourceItemId,
                    a.targetHash == fromHash ? toHash : a.targetHash, a.targetSlot, a.targetItemId);
        }
        if (action instanceof ItemOnObjectAction) {
            ItemOnObjectAction a = (ItemOnObjectAction) action;
            return a.sourceHash != fromHash ? action
                    : new ItemOnObjectAction(toHash, a.sourceSlot, a.sourceItemId, a.objectId, a.x, a.y, a.modifier);
        }
        if (action instanceof ItemOnNpcAction) {
            ItemOnNpcAction a = (ItemOnNpcAction) action;
            return a.sourceHash != fromHash ? action
                    : new ItemOnNpcAction(toHash, a.sourceSlot, a.sourceItemId, a.index, a.modifier);
        }
        return action;
    }

    // ---------------------------------------------------------------------------------------
    // Action types
    // ---------------------------------------------------------------------------------------

    public static final class WalkRequest implements Action {
        private final int x, y, modifier;
        private final boolean minimap;
        private WalkRequest(int x, int y, int modifier, boolean minimap) {
            this.x = x; this.y = y; this.modifier = modifier; this.minimap = minimap;
        }
        public int x() { return x; }
        public int y() { return y; }
        /** Native modifier semantics remain separate from server run/teleport policy. */
        public int modifier() { return modifier; }
        public boolean isMinimap() { return minimap; }
    }

    public static final class InterfaceAction implements Action {
        private final int option, componentHash, slot, itemId;
        private InterfaceAction(int option, int componentHash, int slot, int itemId) {
            this.option = option; this.componentHash = componentHash; this.slot = slot; this.itemId = itemId;
        }
        public int option() { return option; }
        public int componentHash() { return componentHash; }
        public int interfaceId() { return componentHash >>> 16; }
        public int componentId() { return componentHash & 65535; }
        /** -1 is the native 65535 sentinel for a component without a slot. */
        public int slot() { return slot; }
        /** -1 is the absent-item sentinel; this is an untrusted client claim. */
        public int itemId() { return itemId; }
    }

    public static final class ObjectAction implements Action {
        private final int option, objectId, x, y, modifier;
        private ObjectAction(int option, int objectId, int x, int y, int modifier) {
            this.option = option; this.objectId = objectId; this.x = x; this.y = y; this.modifier = modifier;
        }
        public int option() { return option; }
        public int objectId() { return objectId; }
        public int x() { return x; }
        public int y() { return y; }
        public int modifier() { return modifier; }
    }

    public static final class NpcAction implements Action {
        private final int option, index, modifier;
        private NpcAction(int option, int index, int modifier) {
            this.option = option; this.index = index; this.modifier = modifier;
        }
        public int option() { return option; }
        public int index() { return index; }
        public int modifier() { return modifier; }
    }

    /** One of the ten player-menu options (950 opcodes 20,46,71,39,37,94,51,63,117,58). */
    public static final class PlayerAction implements Action {
        private final int option, index, modifier;
        private PlayerAction(int option, int index, int modifier) {
            this.option = option; this.index = index; this.modifier = modifier;
        }
        public int option() { return option; }
        /** Untrusted client claim; the world still checks that this slot is actually in view. */
        public int index() { return index; }
        public int modifier() { return modifier; }
    }

    /** One of the six ground-item options (950 opcodes 127,103,22,56,52,113). */
    public static final class GroundItemAction implements Action {
        private final int option, itemId, x, y, modifier, secondFlag;
        private GroundItemAction(int option, int itemId, int x, int y, int modifier, int secondFlag) {
            this.option = option; this.itemId = itemId; this.x = x; this.y = y;
            this.modifier = modifier; this.secondFlag = secondFlag;
        }
        public int option() { return option; }
        /** -1 is the absent-item sentinel; untrusted. */
        public int itemId() { return itemId; }
        public int x() { return x; }
        public int y() { return y; }
        public int modifier() { return modifier; }
        /**
         * Bit 1 of the first wire byte, from a client-side byte at [client+0x19928]+0x68. It is
         * carried through verbatim because its meaning is not derived - do not treat it as a second
         * modifier without deriving that byte first.
         */
        public int secondFlag() { return secondFlag; }
    }

    /** A selected item targets another component item; all ownership remains a world check. */
    public static final class ItemOnItemAction implements Action {
        private final int sourceHash, sourceSlot, sourceItemId, targetHash, targetSlot, targetItemId;
        private ItemOnItemAction(int sourceHash, int sourceSlot, int sourceItemId,
                                 int targetHash, int targetSlot, int targetItemId) {
            this.sourceHash = sourceHash; this.sourceSlot = sourceSlot; this.sourceItemId = sourceItemId;
            this.targetHash = targetHash; this.targetSlot = targetSlot; this.targetItemId = targetItemId;
        }
        public int sourceHash() { return sourceHash; }
        public int sourceInterfaceId() { return sourceHash >>> 16; }
        public int sourceComponentId() { return sourceHash & 65535; }
        public int sourceSlot() { return sourceSlot; }
        public int sourceItemId() { return sourceItemId; }
        public int targetHash() { return targetHash; }
        public int targetInterfaceId() { return targetHash >>> 16; }
        public int targetComponentId() { return targetHash & 65535; }
        public int targetSlot() { return targetSlot; }
        public int targetItemId() { return targetItemId; }
    }

    /** A selected component item targets a world object; possession and proximity are untrusted. */
    public static final class ItemOnObjectAction implements Action {
        private final int sourceHash, sourceSlot, sourceItemId, objectId, x, y, modifier;
        private ItemOnObjectAction(int hash, int slot, int item, int object, int x, int y, int modifier) {
            sourceHash = hash; sourceSlot = slot; sourceItemId = item; objectId = object;
            this.x = x; this.y = y; this.modifier = modifier;
        }
        public int sourceHash() { return sourceHash; }
        public int sourceInterfaceId() { return sourceHash >>> 16; }
        public int sourceComponentId() { return sourceHash & 65535; }
        public int sourceSlot() { return sourceSlot; }
        public int sourceItemId() { return sourceItemId; }
        public int objectId() { return objectId; }
        public int x() { return x; }
        public int y() { return y; }
        public int modifier() { return modifier; }
    }

    /** A selected component item targets a published NPC index; the world resolves that index. */
    public static final class ItemOnNpcAction implements Action {
        private final int sourceHash, sourceSlot, sourceItemId, index, modifier;
        private ItemOnNpcAction(int hash, int slot, int item, int index, int modifier) {
            sourceHash = hash; sourceSlot = slot; sourceItemId = item;
            this.index = index; this.modifier = modifier;
        }
        public int sourceHash() { return sourceHash; }
        public int sourceInterfaceId() { return sourceHash >>> 16; }
        public int sourceComponentId() { return sourceHash & 65535; }
        public int sourceSlot() { return sourceSlot; }
        public int sourceItemId() { return sourceItemId; }
        public int index() { return index; }
        public int modifier() { return modifier; }
    }

    public static final class DragAction implements Action {
        private final int sourceHash, sourceSlot, sourceItemId, targetHash, targetSlot, targetItemId;
        private DragAction(int sourceHash, int sourceSlot, int sourceItemId,
                           int targetHash, int targetSlot, int targetItemId) {
            this.sourceHash = sourceHash; this.sourceSlot = sourceSlot; this.sourceItemId = sourceItemId;
            this.targetHash = targetHash; this.targetSlot = targetSlot; this.targetItemId = targetItemId;
        }
        public int sourceComponentHash() { return sourceHash; }
        public int sourceInterfaceId() { return sourceHash >>> 16; }
        public int sourceComponentId() { return sourceHash & 65535; }
        public int sourceSlot() { return sourceSlot; }
        public int sourceItemId() { return sourceItemId; }
        public int targetComponentHash() { return targetHash; }
        public int targetInterfaceId() { return targetHash >>> 16; }
        public int targetComponentId() { return targetHash & 65535; }
        public int targetSlot() { return targetSlot; }
        public int targetItemId() { return targetItemId; }
    }

    /** Client opcode 110 reports a completed or unmuted resource; the server owns any replay decision. */
    public static final class MusicEndedAction implements Action {
        private final int archiveId;
        private MusicEndedAction(int archiveId) { this.archiveId = archiveId; }
        /** Untrusted signed resource ID; never an instruction to play an arbitrary archive. */
        public int archiveId() { return archiveId; }
    }

    /** Client opcode 120. The count is an untrusted signed client claim. */
    public static final class CountDialogueAction implements Action {
        private final long count;
        private CountDialogueAction(long count) { this.count = count; }
        /** Signed 64-bit; negative and out-of-range values reach the server verbatim. */
        public long count() { return count; }
    }

    /**
     * Client opcodes 17 and 53. Both carry the identical wire shape; only the client's input type
     * distinguishes them, and the 17-vs-53 labelling is a coin flip (see
     * {@link #decodeStringDialogue(int, byte[])}).
     */
    public static final class StringDialogueAction implements Action {
        private final boolean nameDialogue;
        private final String text;
        private StringDialogueAction(boolean nameDialogue, String text) {
            this.nameDialogue = nameDialogue; this.text = text;
        }
        public boolean isNameDialogue() { return nameDialogue; }
        /** Decoded CP1252 without the length byte or the NUL terminator; unfiltered client input. */
        public String text() { return text; }
    }

    /** Client opcode 11: the generic continue / dismiss signal, empty body. */
    public static final class PauseButtonAction implements Action {
        static final PauseButtonAction INSTANCE = new PauseButtonAction();
        private PauseButtonAction() { }
    }

    /** Client opcode 5: the client already closed its own attachments. */
    public static final class CloseModalAction implements Action {
        static final CloseModalAction INSTANCE = new CloseModalAction();
        private CloseModalAction() { }
    }

    /**
     * Client opcode 104: the connection-manager keepalive, sent after a run of idle ticks. It
     * carries no request, so the transport records it and never queues it. Unrelated to server
     * opcode 183 (NO_TIMEOUT); the two namespaces are separate.
     */
    public static final class KeepAliveAction implements Action {
        static final KeepAliveAction INSTANCE = new KeepAliveAction();
        private KeepAliveAction() { }
    }

    /**
     * Client opcode 101, the dialogue continue / option click. Opcode, size, sender (950
     * 0x1401abc40) and body layout are derived; the symbolic NAME is not, so this descriptive name
     * is provisional. Client opcode 101 is unrelated to any server opcode 101; the two namespaces
     * are separate.
     */
    public static final class DialogueClickAction implements Action {
        private final int componentHash, slot;
        private DialogueClickAction(int componentHash, int slot) {
            this.componentHash = componentHash; this.slot = slot;
        }
        public int componentHash() { return componentHash; }
        public int interfaceId() { return componentHash >>> 16; }
        public int componentId() { return componentHash & 65535; }
        /** -1 is the native 65535 sentinel; the dialogue rows send it for every click. */
        public int slot() { return slot; }
    }

    /**
     * Client opcode 87. Colour and effect are the prefix codes the client stripped from the typed
     * text (950 prefix tables at 0x140c6d220 and 0x140c6d4c0); the text is the remainder and is
     * untrusted, unfiltered client input.
     */
    public static final class PublicChatAction implements Action {
        private final int colour, effect;
        private final String text;
        private PublicChatAction(int colour, int effect, String text) {
            this.colour = colour; this.effect = effect; this.text = text;
        }
        /** 0 yellow, 1 red, 2 green, 3 cyan, 4 purple, 5 white, 6..8 flash, 9..11 glow. */
        public int colour() { return colour; }
        /** 0 none, 1 wave, 2 wave2, 3 shake, 4 scroll, 5 slide. */
        public int effect() { return effect; }
        public String text() { return text; }
    }

    /** Client opcode 72. The recipient is an unvalidated client claim. */
    public static final class PrivateChatAction implements Action {
        private final String recipient, text;
        private PrivateChatAction(String recipient, String text) {
            this.recipient = recipient; this.text = text;
        }
        public String recipient() { return recipient; }
        public String text() { return text; }
    }

    /** Client opcode 9: the client's own window geometry. A report, not a request. */
    public static final class WindowReportAction implements Action {
        private final int displayMode, width, height, flag;
        private WindowReportAction(int displayMode, int width, int height, int flag) {
            this.displayMode = displayMode; this.width = width; this.height = height; this.flag = flag;
        }
        /** 2 fixed, 3 resizable (950 0x14014b0d2 `setne dl; add edx,2`). */
        public int displayMode() { return displayMode; }
        public int width() { return width; }
        public int height() { return height; }
        /** Trailing byte whose meaning is not derived; exposed verbatim. */
        public int flag() { return flag; }
    }

    /** Client opcode 98: how long the last scene build took, in the client's own timer units. */
    public static final class MapBuildReportAction implements Action {
        private final int elapsed;
        private MapBuildReportAction(int elapsed) { this.elapsed = elapsed; }
        public int elapsed() { return elapsed; }
    }

    /** Client opcode 108: refresh the world list, quoting the checksum the client already holds. */
    public static final class WorldListFetchAction implements Action {
        private final int checksum;
        private WorldListFetchAction(int checksum) { this.checksum = checksum; }
        public int checksum() { return checksum; }
    }

    /**
     * The client's canonical huffman construction, rebuilt from the 256-byte cache bit-length
     * table, then walked as a prefix tree to decode the MSB-first bit stream the writer produces.
     * The construction is revision-independent; only the table's cache location is revision
     * specific (950 index 10, group "huffman").
     */
    private static final class Huffman950 {
        private final int[] left, right, symbol;

        private Huffman950(int[] left, int[] right, int[] symbol) {
            this.left = left; this.right = right; this.symbol = symbol;
        }

        static Huffman950 build(byte[] bitLengths) {
            int[] codes = new int[bitLengths.length];
            int[] keys = new int[33];
            int capacity = 1;
            for (int sym = 0; sym < bitLengths.length; sym++) {
                int size = bitLengths[sym] & 255;
                if (size == 0) continue;
                if (size > 32) throw new IllegalArgumentException("Huffman code length " + size + " exceeds 32 bits");
                capacity += size;
                int bit = 1 << (32 - size);
                int current = keys[size];
                codes[sym] = current;
                int next;
                if ((current & bit) == 0) {
                    for (int shorter = size - 1; shorter >= 1; shorter--) {
                        int value = keys[shorter];
                        if (value != current) break;
                        int carry = 1 << (32 - shorter);
                        if ((value & carry) != 0) { keys[shorter] = keys[shorter - 1]; break; }
                        keys[shorter] = value | carry;
                    }
                    next = current | bit;
                } else next = keys[size - 1];
                keys[size] = next;
                for (int longer = size + 1; longer <= 32; longer++) if (keys[longer] == current) keys[longer] = next;
            }
            int[] left = new int[capacity], right = new int[capacity], symbol = new int[capacity];
            Arrays.fill(left, -1); Arrays.fill(right, -1); Arrays.fill(symbol, -1);
            int used = 1;
            for (int sym = 0; sym < bitLengths.length; sym++) {
                int size = bitLengths[sym] & 255;
                if (size == 0) continue;
                int node = 0;
                for (int depth = 0; depth < size; depth++) {
                    if (symbol[node] >= 0) throw new IllegalArgumentException("Huffman table is not a prefix code");
                    int[] child = ((codes[sym] >>> (31 - depth)) & 1) == 0 ? left : right;
                    if (child[node] < 0) child[node] = used++;
                    node = child[node];
                }
                if (symbol[node] >= 0 || left[node] >= 0 || right[node] >= 0)
                    throw new IllegalArgumentException("Huffman table is not a prefix code");
                symbol[node] = sym;
            }
            return new Huffman950(left, right, symbol);
        }

        /** Fills out with out.length characters and returns the bytes consumed, or -1. */
        int decode(byte[] data, int offset, int available, byte[] out) {
            int node = 0, produced = 0, bits = 0;
            while (produced < out.length) {
                if ((bits >> 3) >= available) return -1;
                int bit = (data[offset + (bits >> 3)] >>> (7 - (bits & 7))) & 1;
                bits++;
                node = bit == 0 ? left[node] : right[node];
                if (node < 0) return -1;
                if (symbol[node] >= 0) { out[produced++] = (byte) symbol[node]; node = 0; }
            }
            return (bits + 7) >> 3;
        }
    }

    private static int option(int[] opcodes, int opcode) {
        for (int i = 0; i < opcodes.length; i++) if (opcodes[i] == opcode) return i + 1;
        return 0;
    }

    private static int shortBE(byte[] bytes, int offset) {
        return ((bytes[offset] & 255) << 8) | (bytes[offset + 1] & 255);
    }

    private static int intBE(byte[] bytes, int offset) {
        return ((bytes[offset] & 255) << 24) | ((bytes[offset + 1] & 255) << 16)
                | ((bytes[offset + 2] & 255) << 8) | (bytes[offset + 3] & 255);
    }

    private static int sentinel16(int value) { return value == 65535 ? -1 : value; }

    /**
     * The item fields widened to 24 bits in 950 and the client applies no fold to them, unlike every
     * slot and hash field, which are visibly folded from 0xFFFF to -1 before the store. So the
     * absent-item value on the wire is whatever the source accessor returned truncated to 24 bits:
     * 0xFFFFFF if it was -1, 0x00FFFF if it was already folded to a ushort somewhere upstream.
     * Both are accepted as absent, which costs the ability to name item id 65535 - a trade the plan
     * makes deliberately, because a real 65535 reaching a handler as 65535 is indistinguishable from
     * an absent claim anyway.
     */
    private static int sentinel24(int value) { return value == 0xFFFFFF || value == 0x00FFFF ? -1 : value; }

    private Native950Actions() { }
}
