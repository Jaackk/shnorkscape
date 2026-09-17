package modern947;

import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.Action;
import com.rs.network.protocol.modern950.Native950Actions.PrivateChatAction;
import com.rs.network.protocol.modern950.Native950Actions.PublicChatAction;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

import static org.junit.Assert.*;

/**
 * Actual 950 chat wire fixtures. The sender allocator already writes the opcode; the
 * sender's back-filled byte/word is the sole transport length. The inbound decoder
 * consumes that framing before the colour/effect or recipient reaches Native950Actions.
 *
 * <p>HUFFMAN_TABLE is cache index 10, group 1 (huffman), file 0. It is unchanged in
 * the actual 950 cache. Literal "hi" and "Hello world" bits are independent examples.
 */
public final class Native950ChatActionsTest {

    private static final String HUFFMAN_TABLE_HEX =
            "1616161616161516161416161615161616161616161616161616161616161616"
            + "03081610161011070d0d0d10070a06100a0b0c0c0c0c0d0d0e0e0b0e130f1108"
            + "0b090a0a0a0a0b0a09070c0b0a0a090a0a0c0a09080c0c090e080c111011160d"
            + "150407060503060605040a0705060404060a0504040507060a060a1613160e16"
            + "1616161616161616161616161616161616161616161616161616161616161616"
            + "1616161616161616161616161616161616161616161616161616161616161616"
            + "1616161616161616161616161616161616161616161616161616161616161616"
            + "1616161616161616161616161616161616161616161616151615161616151616";

    @Before
    public void installVerifiedTable() {
        Native950Actions.resetHuffman();
        Native950Actions.installHuffman(table());
    }

    @After
    public void forgetTable() {
        Native950Actions.resetHuffman();
    }

    @Test
    public void publicChatBodyStartsWithColourAndEffectAfterFraming() {
        PublicChatAction plain = (PublicChatAction) Native950Actions.decode(87,
                new byte[] {0x00, 0x00, 0x02, (byte) 0x84, (byte) 0x80});
        assertEquals(0, plain.colour());
        assertEquals(0, plain.effect());
        assertEquals("hi", plain.text());
        PublicChatAction longer = (PublicChatAction) Native950Actions.decode(87,
                new byte[] {0x00, 0x00, 0x0b, 0x0d, (byte) 0xb8, (byte) 0xc7, 0x0f,
                        (byte) 0xd9, 0x58, (byte) 0xa8});
        assertEquals("Hello world", longer.text());
        // red:wave:hi arrives as colour 1, effect 1 and compressed "hi".
        PublicChatAction prefixed = (PublicChatAction) Native950Actions.decode(87,
                new byte[] {0x01, 0x01, 0x02, (byte) 0x84, (byte) 0x80});
        assertEquals(1, prefixed.colour());
        assertEquals(1, prefixed.effect());
        assertEquals("hi", prefixed.text());
        assertTrue(Native950Actions.isImplemented(87));
        assertTrue(Native950Actions.isChatOpcode(87));
        assertFalse(Native950Actions.isChatOpcode(69));
    }

    @Test
    public void privateChatBodyStartsWithRecipientAfterFraming() {
        PrivateChatAction message = (PrivateChatAction) Native950Actions.decode(72,
                new byte[] {0x42, 0x6f, 0x62, 0x00, 0x02, (byte) 0x84, (byte) 0x80});
        assertEquals("Bob", message.recipient());
        assertEquals("hi", message.text());
        assertTrue(Native950Actions.isImplemented(72));
        assertTrue(Native950Actions.isChatOpcode(72));
        assertFalse(Native950Actions.isChatOpcode(68));
    }

    @Test
    public void literalNativeFramesHaveExactlyOneLengthAndSurviveEveryTcpSplit() {
        // Public sender 0x14009b359: opcode 57, length 05, then body.
        // Private sender 0x14009b6a9: opcode 48, length 0007, then body.
        byte[] wire = new byte[] {0x57, 0x05, 0x00, 0x00, 0x02, (byte) 0x84, (byte) 0x80,
                0x48, 0x00, 0x07, 0x42, 0x6f, 0x62, 0x00, 0x02, (byte) 0x84, (byte) 0x80};
        for (int split = 0; split <= wire.length; split++) {
            Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                    Thread.currentThread(), 4, 4);
            EmbeddedChannel channel = new EmbeddedChannel(transport);
            try {
                channel.writeInbound(Unpooled.wrappedBuffer(java.util.Arrays.copyOfRange(wire, 0, split)));
                channel.writeInbound(Unpooled.wrappedBuffer(java.util.Arrays.copyOfRange(wire, split, wire.length)));
                assertTrue("split " + split, channel.isActive());
                assertNull(transport.terminalFailure());
                List<Action> applied = new ArrayList<Action>();
                assertEquals(2, transport.drainActions(applied::add));
                assertEquals("hi", ((PublicChatAction) applied.get(0)).text());
                assertEquals("Bob", ((PrivateChatAction) applied.get(1)).recipient());
                assertEquals("hi", ((PrivateChatAction) applied.get(1)).text());
            } finally { channel.finishAndReleaseAll(); }
        }
    }

    @Test
    public void emptyTextAndTheTwoByteSmartLengthAreBothAccepted() {
        PublicChatAction empty = (PublicChatAction) Native950Actions.decode(87,
                new byte[] {0x00, 0x00, 0x00});
        assertEquals("", empty.text());
        // 130 'a' characters occupy 65 bytes; the smart is 80 82, not another frame length.
        StringBuilder expected = new StringBuilder();
        for (int index = 0; index < 130; index++) expected.append('a');
        byte[] body = concat(new byte[] {0x00, 0x00, (byte) 0x80, (byte) 0x82}, encode(expected.toString()));
        assertEquals(69, body.length);
        assertEquals(expected.toString(), ((PublicChatAction) Native950Actions.decode(87, body)).text());
    }

    @Test
    public void malformedChatBodiesAreRejectedWithoutThrowing() {
        assertNull(Native950Actions.decode(87, new byte[] {0x00, 0x00}));
        assertNull(Native950Actions.decode(87, null));
        // The old duplicated-length fixture must not be mistaken for a real sender body.
        assertNull(Native950Actions.decode(87,
                new byte[] {0x05, 0x00, 0x00, 0x02, (byte) 0x84, (byte) 0x80}));
        // Colour/effect limits come from the actual native prefix tables.
        assertNull(Native950Actions.decode(87, new byte[] {0x0c, 0x00, 0x02, (byte) 0x84, (byte) 0x80}));
        assertNull(Native950Actions.decode(87, new byte[] {0x00, 0x06, 0x02, (byte) 0x84, (byte) 0x80}));
        assertNotNull(Native950Actions.decode(87, new byte[] {0x0b, 0x05, 0x02, (byte) 0x84, (byte) 0x80}));
        assertNull(Native950Actions.decode(87, new byte[] {0x00, 0x00, 0x02, (byte) 0x84}));
        assertNull(Native950Actions.decode(87,
                new byte[] {0x00, 0x00, 0x02, (byte) 0x84, (byte) 0x80, 0x00}));
        assertNull(Native950Actions.decode(87, new byte[] {0x00, 0x00, 0x00, 0x00}));
        assertNull(Native950Actions.decode(72, new byte[] {0x42, 0x6f, 0x62}));
        assertNull(Native950Actions.decode(72, new byte[] {0x42, 0x6f, 0x62, 0x00}));
        assertNull(Native950Actions.decode(72, new byte[] {0x42, 0x6f, 0x62, 0x00, (byte) 0x80}));
        assertNull(Native950Actions.decode(72, null));
    }

    @Test
    public void withoutAHuffmanTableChatFailsClosedAndIsCountedNotGuessed() {
        Native950Actions.resetHuffman();
        assertFalse(Native950Actions.isHuffmanInstalled());
        long before = Native950Actions.huffmanUnavailableCount();
        assertNull(Native950Actions.decode(87, new byte[] {0x00, 0x00, 0x02, (byte) 0x84, (byte) 0x80}));
        assertNull(Native950Actions.decode(72,
                new byte[] {0x42, 0x6f, 0x62, 0x00, 0x02, (byte) 0x84, (byte) 0x80}));
        assertEquals(before + 2, Native950Actions.huffmanUnavailableCount());
        try {
            Native950Actions.installHuffman(new byte[255]);
            fail("A short table must be refused, never padded");
        } catch (IllegalArgumentException expected) { /* validated */ }
        try {
            Native950Actions.installHuffman(null);
            fail("A missing table must be refused");
        } catch (IllegalArgumentException expected) { /* validated */ }
        assertFalse(Native950Actions.isHuffmanInstalled());
    }

    @Test
    public void undecodableChatIsDroppedByTheTransportInsteadOfClosingTheConnection() {
        Native950Actions.resetHuffman();
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 4, 4);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
                    0x57, 0x05, 0x00, 0x00, 0x02, (byte) 0x84, (byte) 0x80}));
            assertTrue(channel.isActive());
            assertNull(transport.terminalFailure());
            assertEquals(0, transport.pendingActions());
            assertEquals(0, transport.unhandledFrameCount());
            assertEquals(1, transport.chatFramesDropped());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    public void encryptedChatFramesReachTheQueueInOrderThroughTheRealCipher() {
        int[] seeds = {1, 2, 3, 4};
        Native950GameTransport transport = new Native950GameTransport(new Native950Isaac(seeds),
                () -> 0, Thread.currentThread(), 8, 8);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            IntSupplier cipher = new Native950Isaac(seeds);
            byte[] wire = concat(
                    byteFramed(cipher, 87, new byte[] {0x01, 0x01, 0x02, (byte) 0x84, (byte) 0x80}),
                    shortFramed(cipher, 72, new byte[] {0x42, 0x6f, 0x62, 0x00,
                            0x02, (byte) 0x84, (byte) 0x80}));
            for (byte value : wire) channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {value}));
            assertTrue(channel.isActive());
            assertEquals(0, transport.unhandledFrameCount());
            assertEquals(0, transport.chatFramesDropped());
            List<Action> applied = new ArrayList<Action>();
            assertEquals(2, transport.drainActions(applied::add));
            PublicChatAction publicChat = (PublicChatAction) applied.get(0);
            assertEquals(1, publicChat.colour());
            assertEquals(1, publicChat.effect());
            assertEquals("hi", publicChat.text());
            PrivateChatAction privateChat = (PrivateChatAction) applied.get(1);
            assertEquals("Bob", privateChat.recipient());
            assertEquals("hi", privateChat.text());
        } finally { channel.finishAndReleaseAll(); }
    }

    private static byte[] table() {
        byte[] bytes = new byte[HUFFMAN_TABLE_HEX.length() / 2];
        for (int index = 0; index < bytes.length; index++)
            bytes[index] = (byte) Integer.parseInt(HUFFMAN_TABLE_HEX.substring(index * 2, index * 2 + 2), 16);
        return bytes;
    }

    /**
     * Reference encoder built from the same construction the client uses, used only to build the
     * two-byte smart-length fixture that the worked examples do not print.
     */
    private static byte[] encode(String text) {
        byte[] bitLengths = table();
        int[] codes = new int[256];
        int[] keys = new int[33];
        for (int sym = 0; sym < 256; sym++) {
            int size = bitLengths[sym] & 255;
            if (size == 0) continue;
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
        int bits = 0;
        for (int index = 0; index < text.length(); index++) bits += bitLengths[text.charAt(index) & 255] & 255;
        byte[] out = new byte[(bits + 7) / 8];
        int offset = 0;
        for (int index = 0; index < text.length(); index++) {
            int symbol = text.charAt(index) & 255;
            int size = bitLengths[symbol] & 255;
            for (int depth = 0; depth < size; depth++, offset++)
                if (((codes[symbol] >>> (31 - depth)) & 1) != 0) out[offset >>> 3] |= 1 << (7 - (offset & 7));
        }
        return out;
    }

    private static byte[] byteFramed(IntSupplier cipher, int opcode, byte[] payload) {
        byte[] framed = new byte[payload.length + 2];
        framed[0] = (byte) (opcode + cipher.getAsInt());
        framed[1] = (byte) payload.length;
        System.arraycopy(payload, 0, framed, 2, payload.length);
        return framed;
    }

    private static byte[] shortFramed(IntSupplier cipher, int opcode, byte[] payload) {
        byte[] framed = new byte[payload.length + 3];
        framed[0] = (byte) (opcode + cipher.getAsInt());
        framed[1] = (byte) (payload.length >>> 8);
        framed[2] = (byte) payload.length;
        System.arraycopy(payload, 0, framed, 3, payload.length);
        return framed;
    }

    private static byte[] concat(byte[]... packets) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (byte[] packet : packets) result.write(packet, 0, packet.length);
        return result.toByteArray();
    }
}
