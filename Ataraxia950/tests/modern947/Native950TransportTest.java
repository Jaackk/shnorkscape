package modern947;

import com.rs.network.protocol.modern950.Native950Protocol;

import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Actions.WalkRequest;
import com.rs.network.protocol.modern950.Native950Actions.Action;
import com.rs.network.protocol.modern950.Native950Actions.InterfaceAction;
import com.rs.network.protocol.modern950.Native950Actions.ObjectAction;
import com.rs.network.protocol.modern950.Native950Actions.PublicChatAction;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

import static org.junit.Assert.*;

/**
 * Transport behaviour over the 950 inbound namespace. Everything the transport does - queueing,
 * eviction, the keepalive lane, the unhandled lane - is revision independent, so the only thing
 * that changed here is the wire fixtures. They are computed from the 950 field lists recorded in
 * {@code Native950Actions}; the 947 bytes each one replaces are kept in the comments.
 */
public final class Native950TransportTest {

    /** Ground click, 950 opcode 88. Prefix layout is shared with the minimap row (opcode 78). */
    private static final int WALK = 88;
    /** IF_BUTTON option 1, 950 opcode 18 (947: 96). */
    private static final int BUTTON1 = 18;
    /** Object option 2, 950 opcode 48 (947: 11). */
    private static final int OBJECT2 = 48;
    /** Public chat, 950 opcode 87 (947: 69). */
    private static final int PUBLIC_CHAT = 87;
    /** Keepalive, 950 opcode 104 (947: 27). */
    private static final int KEEPALIVE = 104;

    /** IF_BUTTON option 1 on 1473:5, item 1511, slot 2; see Native950ActionsTest for the derivation. */
    private static final byte[] BUTTON_BODY = {0, 5, (byte) 0xe7, (byte) 0xc1, 5, 5, 0, 0, 2};
    /** Object option 2 at (3201, 3200), id 0x01234567, unmodified. */
    private static final byte[] OBJECT_BODY = {0, 12, 1, 35, 69, 103, 12, 1, 0};

    @Test
    public void encryptedMixedActionsPreserveOrderAndShareOneTickBudget() {
        int[] seeds = {1, 2, 3, 4};
        Native950GameTransport transport = new Native950GameTransport(new Native950Isaac(seeds),
                () -> 0, Thread.currentThread(), 4, 2);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            IntSupplier cipher = new Native950Isaac(seeds);
            byte[] bytes = concat(walk(cipher, 3201, 3200),
                    input(cipher, BUTTON1, BUTTON_BODY),
                    input(cipher, OBJECT2, OBJECT_BODY));
            for (byte value : bytes) channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {value}));
            assertEquals(3, transport.pendingActions());
            assertEquals(1, transport.pendingWalkRequests());
            List<Action> applied = new ArrayList<Action>();
            assertEquals(2, transport.drainActions(applied::add));
            assertTrue(applied.get(0) instanceof WalkRequest);
            assertEquals(1511, ((InterfaceAction) applied.get(1)).itemId());
            assertEquals(1, transport.pendingActions());
            assertEquals(1, transport.drainActions(applied::add));
            assertEquals(2, ((ObjectAction) applied.get(2)).option());
            assertEquals(0, transport.unhandledFrameCount());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    public void walkCompatibilityDoesNotConsumeOrReorderInterfaceActions() {
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 4, 4);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeInbound(Unpooled.wrappedBuffer(concat(input(() -> 0, BUTTON1, new byte[9]),
                    walk(() -> 0, 3201, 3200))));
            assertEquals(0, transport.drainWalkRequests(request -> fail("Cannot skip interface action")));
            assertEquals(2, transport.pendingActions());
            List<Action> applied = new ArrayList<Action>();
            assertEquals(2, transport.drainActions(applied::add));
            assertTrue(applied.get(0) instanceof InterfaceAction);
            assertTrue(applied.get(1) instanceof WalkRequest);
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    public void mixedQueueOverflowAndDisconnectDiscardEveryKindOfAction() {
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 2, 2);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeInbound(Unpooled.wrappedBuffer(input(() -> 0, BUTTON1, new byte[9])));
            channel.writeInbound(Unpooled.wrappedBuffer(walk(() -> 0, 3201, 3200)));
            assertThrows(IllegalStateException.class, () -> channel.writeInbound(
                    Unpooled.wrappedBuffer(input(() -> 0, OBJECT2, OBJECT_BODY))));
            assertFalse(channel.isActive());
            assertEquals(0, transport.pendingActions());
            assertEquals(0, transport.drainActions(request -> fail("Disconnected action")));
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    public void fragmentedEncryptedWalkBecomesSemanticActionAndOutputUsesSeparateCipher() {
        int[] seeds = {1, 2, 3, 4};
        Native950GameTransport transport = Native950GameTransport.afterAuthentication(seeds, Thread.currentThread());
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            // 950 opcode 88, y=3200 plain big-endian, modifier 0 as 0+128, x=3201 as
            // (x & 255)+128 then x>>>8. Opcode encrypted with the first OpenNXT ISAAC word
            // daf8863e for seeds 1,2,3,4, so the wire byte is (88 + 0x3e) & 255 = 150 = 0x96.
            // Decimal 88, not 0x88: reading it as hex gives 0xc6, which decrypts to opcode 136 -
            // an opcode the 950 client does not have, since its descriptor table stops at 128.
            // 947 wrote opcode 3 with y short128, x big-endian, modifier 128-m: 41 0c 00 0c 81 80.
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) 0x96, 0x0c}));
            assertEquals(0, transport.pendingWalkRequests());
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) 0x80, (byte) 0x80, 0x01, 0x0c}));
            assertEquals(1, transport.pendingWalkRequests());
            assertNull(channel.readInbound());
            List<WalkRequest> applied = new ArrayList<WalkRequest>();
            assertEquals(1, transport.drainWalkRequests(applied::add));
            assertEquals(3201, applied.get(0).x());
            assertEquals(3200, applied.get(0).y());
            assertEquals(0, applied.get(0).modifier());
            assertFalse(applied.get(0).isMinimap());

            channel.writeOutbound(Native950Packets.openTop(1477), Native950Packets.tickEnd());
            // IF_OPENTOP is 950 opcode 1 (947: 94), size 19, body b0=id, b1=id>>>8 then zeros.
            byte[] openTop = new byte[20];
            openTop[0] = 0x19; // 1 + low byte 18 of outgoing seed+50 word 21fc9d18.
            openTop[1] = (byte) 0xc5;
            openTop[2] = 5;
            assertArrayEquals(openTop, readOutput(channel));
            // SERVER_TICK_END is 950 opcode 160 (947: 195); both are >= 128, so the outbound smart
            // opcode still costs two cipher words 7a803e6f, 6370d89f.
            assertArrayEquals(new byte[] {(byte) 0xef, 0x3f}, readOutput(channel));
            assertArrayEquals(new int[] {1, 2, 3, 4}, seeds);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void coalescedWalksRespectPerTickBudgetAndDisconnectClearsQueue() {
        Native950GameTransport transport = new Native950GameTransport(new Native950Isaac(new int[] {1, 2, 3, 4}),
                new Native950Isaac(new int[] {51, 52, 53, 54}), Thread.currentThread(), 4, 2);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            IntSupplier clientCipher = new Native950Isaac(new int[] {1, 2, 3, 4});
            channel.writeInbound(Unpooled.wrappedBuffer(concat(walk(clientCipher, 3201, 3200),
                    walk(clientCipher, 3202, 3200), walk(clientCipher, 3203, 3200))));
            List<Integer> destinations = new ArrayList<Integer>();
            assertEquals(3, transport.pendingWalkRequests());
            assertEquals(2, transport.drainWalkRequests(request -> destinations.add(request.x())));
            assertEquals(java.util.Arrays.asList(3201, 3202), destinations);
            assertEquals(1, transport.pendingWalkRequests());
            channel.close();
            assertEquals(0, transport.pendingWalkRequests());
            assertEquals(0, transport.drainWalkRequests(request -> fail("Disconnected action applied")));
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void unknownFramingClosesConnectionAndDoesNotReachLegacyHandlers() {
        Native950GameTransport transport = Native950GameTransport.afterAuthentication(new int[] {1, 2, 3, 4}, Thread.currentThread());
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            // The 950 client descriptor table stops at opcode 128, so 200 has no size at all.
            // 200 + 0x3e = 0x106, i.e. wire byte 0x06.
            //
            // The 947 version of this test sent opcode 130 through the two-byte >= 128 escape
            // (be b9). That escape is gone: 128 is an ordinary five-byte 950 packet, so 0xbe would
            // now start a real frame and swallow the next byte instead of failing.
            assertThrows(IllegalArgumentException.class,
                    () -> channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {0x06})));
            assertFalse(channel.isActive());
            assertNotNull(transport.terminalFailure());
            assertNull(channel.readInbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void unimplementedButSizedPacketIsDiagnosedWithoutInventingAnAction() {
        Native950GameTransport transport = Native950GameTransport.afterAuthentication(new int[] {1, 2, 3, 4}, Thread.currentThread());
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            // 950 opcode 42 has size 0 in the client's descriptor table and no derived meaning,
            // so it must stay unhandled: framed, counted, never turned into an action.
            // 42 + 0x3e = 0x68.
            assertFalse(Native950Actions.isImplemented(42));
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {0x68}));
            assertTrue(channel.isActive());
            assertEquals(1, transport.unhandledFrameCount());
            assertEquals(42, transport.lastUnhandledOpcode());
            assertEquals(0, transport.pendingWalkRequests());
            assertNull(channel.readInbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void theRemainingGroundTargetVariantIsFramedAndCountedRatherThanDecoded() {
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 4, 4);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            // Opcode 112 is selected use on a ground item, size 17. Its ground semantics are not admitted, so the
            // right outcome is an unhandled count - not a disconnect, and not an invented target.
            byte[] frame = new byte[1 + Native950Protocol.clientSize(112)];
            frame[0] = 112;
            channel.writeInbound(Unpooled.wrappedBuffer(frame));
            assertTrue(channel.isActive());
            assertNull(transport.terminalFailure());
            assertEquals(1, transport.unhandledFrameCount());
            assertEquals(112, transport.lastUnhandledOpcode());
            assertEquals(0, transport.pendingActions());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    public void queueOverflowClosesAndDiscardsPendingActions() {
        Native950GameTransport transport = new Native950GameTransport(new Native950Isaac(new int[] {1, 2, 3, 4}),
                new Native950Isaac(new int[] {51, 52, 53, 54}), Thread.currentThread(), 1, 1);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            IntSupplier clientCipher = new Native950Isaac(new int[] {1, 2, 3, 4});
            assertThrows(IllegalStateException.class, () -> channel.writeInbound(Unpooled.wrappedBuffer(
                    concat(walk(clientCipher, 3201, 3200), walk(clientCipher, 3202, 3200)))));
            assertFalse(channel.isActive());
            assertEquals(0, transport.pendingWalkRequests());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void worldActionsCannotDrainOnAnotherThreadAndRawOutputIsRefused() {
        Native950GameTransport transport = Native950GameTransport.afterAuthentication(new int[] {1, 2, 3, 4}, new Thread("designated-game-thread"));
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            assertThrows(IllegalStateException.class, () -> transport.drainWalkRequests(request -> fail("Wrong thread")));
            ByteBuf legacy = Unpooled.wrappedBuffer(new byte[] {1, 2, 3});
            assertThrows(IllegalArgumentException.class, () -> channel.writeOutbound(legacy));
            assertEquals(0, legacy.refCnt());
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void chatOverflowEvictsTheOldestChatAndKeepsEveryGameplayAction() {
        Native950Actions.resetHuffman();
        Native950Actions.installHuffman(huffmanTable());
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 3, 3);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeInbound(Unpooled.wrappedBuffer(walk(() -> 0, 3201, 3200)));
            channel.writeInbound(Unpooled.wrappedBuffer(chat(0)));
            channel.writeInbound(Unpooled.wrappedBuffer(chat(1)));
            assertEquals(3, transport.pendingActions());
            assertEquals(0, transport.chatFramesDropped());
            // The queue is full; a fourth frame that is chat evicts the oldest chat.
            channel.writeInbound(Unpooled.wrappedBuffer(chat(2)));
            assertTrue(channel.isActive());
            assertNull(transport.terminalFailure());
            assertEquals(1, transport.chatFramesDropped());
            assertEquals(3, transport.pendingActions());
            List<Action> applied = new ArrayList<Action>();
            assertEquals(3, transport.drainActions(applied::add));
            assertTrue(applied.get(0) instanceof WalkRequest);
            assertEquals(1, ((PublicChatAction) applied.get(1)).colour());
            assertEquals(2, ((PublicChatAction) applied.get(2)).colour());
        } finally {
            channel.finishAndReleaseAll();
            Native950Actions.resetHuffman();
        }
    }

    @Test
    public void chatCannotDisplaceGameplayAndAGameplayFloodStillClosesTheConnection() {
        Native950Actions.resetHuffman();
        Native950Actions.installHuffman(huffmanTable());
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 1, 1);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeInbound(Unpooled.wrappedBuffer(walk(() -> 0, 3201, 3200)));
            // Nothing in the queue may be evicted for chat, so the chat frame itself goes.
            channel.writeInbound(Unpooled.wrappedBuffer(chat(0)));
            assertTrue(channel.isActive());
            assertEquals(1, transport.chatFramesDropped());
            assertEquals(1, transport.pendingActions());
            assertEquals(1, transport.pendingWalkRequests());
            // Gameplay overflow behaviour is unchanged.
            assertThrows(IllegalStateException.class, () -> channel.writeInbound(
                    Unpooled.wrappedBuffer(walk(() -> 0, 3202, 3200))));
            assertFalse(channel.isActive());
        } finally {
            channel.finishAndReleaseAll();
            Native950Actions.resetHuffman();
        }
    }

    @Test
    public void aQueueFullOfChatYieldsToTheNextGameplayActionInsteadOfClosingTheConnection() {
        Native950Actions.resetHuffman();
        Native950Actions.installHuffman(huffmanTable());
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 3, 3);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeInbound(Unpooled.wrappedBuffer(chat(0)));
            channel.writeInbound(Unpooled.wrappedBuffer(chat(1)));
            channel.writeInbound(Unpooled.wrappedBuffer(chat(2)));
            assertEquals(3, transport.pendingActions());
            assertEquals(0, transport.chatFramesDropped());
            // The queue is full of expendable chat; the walk that follows must still be
            // queued, and the player must keep their connection.
            channel.writeInbound(Unpooled.wrappedBuffer(walk(() -> 0, 3201, 3200)));
            assertTrue(channel.isActive());
            assertNull(transport.terminalFailure());
            assertEquals(1, transport.chatFramesDropped());
            assertEquals(3, transport.pendingActions());
            assertEquals(1, transport.pendingWalkRequests());
            List<Action> applied = new ArrayList<Action>();
            assertEquals(3, transport.drainActions(applied::add));
            assertEquals(1, ((PublicChatAction) applied.get(0)).colour());
            assertEquals(2, ((PublicChatAction) applied.get(1)).colour());
            assertEquals(3201, ((WalkRequest) applied.get(2)).x());
        } finally {
            channel.finishAndReleaseAll();
            Native950Actions.resetHuffman();
        }
    }

    @Test
    public void keepaliveFramesAreCountedButNeverQueuedOrTreatedAsUnhandled() {
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 1, 1);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            byte[] burst = new byte[64];
            for (int index = 0; index < burst.length; index++) burst[index] = KEEPALIVE;
            channel.writeInbound(Unpooled.wrappedBuffer(burst));
            channel.writeInbound(Unpooled.wrappedBuffer(walk(() -> 0, 3201, 3200)));
            assertTrue(channel.isActive());
            assertEquals(64, transport.keepAliveFrameCount());
            assertEquals(0, transport.unhandledFrameCount());
            assertEquals(1, transport.pendingActions());
            assertEquals(1, transport.pendingWalkRequests());
        } finally { channel.finishAndReleaseAll(); }
    }

    /**
     * Public chat "hi", with the colour byte varied to tell frames apart. The sole -1 frame
     * length is 5, followed by colour, effect 0, smart text length and Huffman bits.
     * Native allocator writes only the opcode; the chat sender back-fills this length.
     */
    private static byte[] chat(int colour) {
        return new byte[] {PUBLIC_CHAT, 5, (byte) colour, 0, 2, (byte) 0x84, (byte) 0x80};
    }

    /** Cache index 10, group "huffman". Unchanged between the two revisions. */
    private static byte[] huffmanTable() {
        String hex = "1616161616161516161416161615161616161616161616161616161616161616"
                + "03081610161011070d0d0d10070a06100a0b0c0c0c0c0d0d0e0e0b0e130f1108"
                + "0b090a0a0a0a0b0a09070c0b0a0a090a0a0c0a09080c0c090e080c111011160d"
                + "150407060503060605040a0705060404060a0504040507060a060a1613160e16"
                + "1616161616161616161616161616161616161616161616161616161616161616"
                + "1616161616161616161616161616161616161616161616161616161616161616"
                + "1616161616161616161616161616161616161616161616161616161616161616"
                + "1616161616161616161616161616161616161616161616151615161616151616";
        byte[] table = new byte[hex.length() / 2];
        for (int index = 0; index < table.length; index++)
            table[index] = (byte) Integer.parseInt(hex.substring(index * 2, index * 2 + 2), 16);
        return table;
    }

    /**
     * 950 opcode 88, unmodified: y plain big-endian, then modifier+128, then (x &amp; 255)+128 and
     * x&gt;&gt;&gt;8. The 947 helper wrote {y&gt;&gt;&gt;8, y+128, x&gt;&gt;&gt;8, x, 128}.
     */
    private static byte[] walk(IntSupplier cipher, int x, int y) {
        return new byte[] {(byte) (WALK + cipher.getAsInt()), (byte) (y >>> 8), (byte) y,
                (byte) 128, (byte) (x + 128), (byte) (x >>> 8)};
    }

    private static byte[] input(IntSupplier cipher, int opcode, byte[] payload) {
        byte[] framed = new byte[payload.length + 1];
        framed[0] = (byte) (opcode + cipher.getAsInt());
        System.arraycopy(payload, 0, framed, 1, payload.length);
        return framed;
    }

    private static byte[] concat(byte[]... packets) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (byte[] packet : packets) result.write(packet, 0, packet.length);
        return result.toByteArray();
    }

    private static byte[] readOutput(EmbeddedChannel channel) {
        ByteBuf output = channel.readOutbound();
        assertNotNull(output);
        try {
            byte[] bytes = new byte[output.readableBytes()];
            output.readBytes(bytes);
            return bytes;
        } finally {
            output.release();
        }
    }
}
