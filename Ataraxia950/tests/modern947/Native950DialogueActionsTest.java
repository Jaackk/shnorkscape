package modern947;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.Action;
import com.rs.network.protocol.modern950.Native950Actions.CloseModalAction;
import com.rs.network.protocol.modern950.Native950Actions.CountDialogueAction;
import com.rs.network.protocol.modern950.Native950Actions.DialogueClickAction;
import com.rs.network.protocol.modern950.Native950Actions.KeepAliveAction;
import com.rs.network.protocol.modern950.Native950Actions.PauseButtonAction;
import com.rs.network.protocol.modern950.Native950Actions.StringDialogueAction;
import com.rs.network.modern.Native950GameTransport;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Actual 950 chatbox/dialogue replies. String/name packet counts belong to framing,
 * so their body is just the NUL-terminated text. Dialogue click field transforms
 * and typed signed i64 amounts are independently derived from their native senders.
 */
public final class Native950DialogueActionsTest {

    @Test
    public void countDialogueIsASigned64BitBigEndianCount() {
        // 950 opcode 120 (947 opcode 16), size 8. Sender 0x1402055d1 hands the value to the
        // big-endian i64 writer 0x1400af2e0, so the body is unchanged - only the opcode moved.
        CountDialogueAction typed = (CountDialogueAction) Native950Actions.decode(120,
                new byte[] {0, 0, 0, 0, 0, 0, 0x30, 0x39});
        assertEquals(12345L, typed.count());
        CountDialogueAction negative = (CountDialogueAction) Native950Actions.decode(120,
                new byte[] {-1, -1, -1, -1, -1, -1, -1, -1});
        assertEquals(-1L, negative.count());
        CountDialogueAction wide = (CountDialogueAction) Native950Actions.decode(120,
                new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef});
        assertEquals(0x0123456789abcdefL, wide.count());
        assertTrue(Native950Actions.isImplemented(120));
        assertNull(Native950Actions.decode(120, new byte[7]));
        assertNull(Native950Actions.decode(120, new byte[9]));
        assertNull(Native950Actions.decode(120, null));
        // 947's opcode 16 is a three-byte packet in the 950 table and is not implemented here.
        assertFalse(Native950Actions.isImplemented(16));
    }

    @Test
    public void stringAndNameBodiesStartWithTheirTerminatedText() {
        StringDialogueAction string = (StringDialogueAction) Native950Actions.decode(17,
                new byte[] {0x42, 0x6f, 0x62, 0x00});
        assertEquals("Bob", string.text());
        assertFalse(string.isNameDialogue());
        assertEquals("", ((StringDialogueAction) Native950Actions.decode(17, new byte[] {0x00})).text());
        StringDialogueAction name = (StringDialogueAction) Native950Actions.decode(53,
                new byte[] {0x5a, 0x65, 0x7a, 0x69, 0x6d, 0x61, 0x00});
        assertEquals("Zezima", name.text());
        assertTrue(name.isNameDialogue());
        assertTrue(Native950Actions.isImplemented(17));
        assertTrue(Native950Actions.isImplemented(53));
    }

    @Test
    public void missingOrInteriorTerminatorsAreRefused() {
        assertNull(Native950Actions.decode(17, new byte[] {0x42, 0x6f, 0x62}));
        assertNull(Native950Actions.decode(17, new byte[] {0x42, 0x00, 0x62, 0x00}));
        assertNull(Native950Actions.decode(17, new byte[] {0x01}));
        assertNull(Native950Actions.decode(17, new byte[0]));
        assertNull(Native950Actions.decode(17, null));
        assertNull(Native950Actions.decode(53, new byte[] {0x5a, 0x65}));
        assertNull(Native950Actions.decode(53, null));
    }

    @Test
    public void literalStringAndNameFramesHaveOnlyOneTransportLength() {
        // Actual native allocator emits only the opcode; 04 and 07 below are the
        // counts written at 0x140205e98 / 0x140205a38, each followed by plain text.
        byte[] wire = new byte[] {0x11, 0x04, 0x42, 0x6f, 0x62, 0x00,
                0x35, 0x07, 0x5a, 0x65, 0x7a, 0x69, 0x6d, 0x61, 0x00};
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 4, 4);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            for (byte value : wire) channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {value}));
            assertTrue(channel.isActive());
            assertNull(transport.terminalFailure());
            List<Action> applied = new ArrayList<Action>();
            assertEquals(2, transport.drainActions(applied::add));
            assertEquals("Bob", ((StringDialogueAction) applied.get(0)).text());
            assertFalse(((StringDialogueAction) applied.get(0)).isNameDialogue());
            assertEquals("Zezima", ((StringDialogueAction) applied.get(1)).text());
            assertTrue(((StringDialogueAction) applied.get(1)).isNameDialogue());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    public void zeroLengthDialogueSignalsAreDistinctAndRejectAnyBody() {
        // 950: pause 11 (947 41), close modal 5 (947 55), keepalive 104 (947 27).
        assertTrue(Native950Actions.decode(11, new byte[0]) instanceof PauseButtonAction);
        assertTrue(Native950Actions.decode(5, new byte[0]) instanceof CloseModalAction);
        assertTrue(Native950Actions.decode(104, new byte[0]) instanceof KeepAliveAction);
        assertTrue(Native950Actions.isImplemented(11));
        assertTrue(Native950Actions.isImplemented(5));
        assertTrue(Native950Actions.isImplemented(104));
        assertTrue(Native950Actions.isKeepAliveOpcode(104));
        assertFalse(Native950Actions.isKeepAliveOpcode(11));
        assertNull(Native950Actions.decode(11, new byte[1]));
        assertNull(Native950Actions.decode(5, new byte[1]));
        assertNull(Native950Actions.decode(104, new byte[1]));
        assertNull(Native950Actions.decode(11, null));
        // The 947 zero-length opcodes now mean something else entirely: 950 opcode 41 is object
        // option 4 (nine bytes), 27 is NPC option 3 (three bytes) and 55 is a variable-length row
        // with no derived meaning. Treating any of them as an empty signal would have consumed no
        // body and mis-framed the rest of the stream.
        assertTrue(Native950Actions.isImplemented(41));
        assertTrue(Native950Actions.isImplemented(27));
        assertFalse(Native950Actions.isImplemented(55));
        assertNull(Native950Actions.decode(41, new byte[0]));
        assertNull(Native950Actions.decode(27, new byte[0]));
        assertNull(Native950Actions.decode(55, new byte[0]));
        // Zero-length 950 rows with no derived meaning stay unhandled.
        for (int opcode : new int[] {42, 91, 93}) {
            assertFalse(Native950Actions.isImplemented(opcode));
            assertNull(Native950Actions.decode(opcode, new byte[0]));
        }
    }

    @Test
    public void dialogueClickLeadsWithTheHashAndEndsWithTheBiasedSlot() {
        // 950 opcode 101, size 6. Sender 0x1401abc40 writes the hash first (0x1401abcac bswap),
        // then slot>>>8 (0x1401abcf0) and (slot & 255)+128 (0x1401abced). 947 opcode 15 wrote the
        // slot FIRST: continue button 1184:11 with no slot was ff 7f 04 a0 00 0b there and is
        // 04 a0 00 0b ff 7f here. Both are legal six-byte frames, so only the field order tells
        // them apart - reading the 947 order yields component hash 0xFF7F04A0, a real component.
        DialogueClickAction absent = (DialogueClickAction) Native950Actions.decode(101,
                new byte[] {0x04, (byte) 0xa0, 0x00, 0x0b, (byte) 0xff, 0x7f});
        assertEquals(-1, absent.slot());
        assertEquals(1184, absent.interfaceId());
        assertEquals(11, absent.componentId());
        assertEquals((1184 << 16) | 11, absent.componentHash());
        // An option row of 1188 with a real slot exercises both halves of the transform.
        DialogueClickAction row = (DialogueClickAction) Native950Actions.decode(101,
                new byte[] {0x04, (byte) 0xa4, 0x00, 0x05, 0x12, (byte) 0xb4});
        assertEquals(0x1234, row.slot());
        assertEquals(1188, row.interfaceId());
        assertEquals(5, row.componentId());
        assertTrue(Native950Actions.isImplemented(101));
        assertNull(Native950Actions.decode(101, new byte[5]));
        assertNull(Native950Actions.decode(101, new byte[7]));
        assertNull(Native950Actions.decode(101, null));
    }

    @Test
    public void encryptedDialogueSequenceReachesTheQueueAndTheKeepaliveDoesNot() {
        // Real ISAAC pair; the helpers below apply the stream, so only the opcodes and bodies
        // changed from the 947 version of this test.
        int[] seeds = {1, 2, 3, 4};
        Native950GameTransport transport = new Native950GameTransport(
                new com.rs.network.modern.Native950Isaac(seeds), () -> 0, Thread.currentThread(), 8, 8);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            com.rs.network.modern.Native950Isaac cipher = new com.rs.network.modern.Native950Isaac(seeds);
            byte[] wire = concat(
                    fixed(cipher, 101, new byte[] {0x04, (byte) 0xa0, 0x00, 0x0b, (byte) 0xff, 0x7f}),
                    fixed(cipher, 104, new byte[0]),
                    fixed(cipher, 120, new byte[] {0, 0, 0, 0, 0, 0, 0x30, 0x39}),
                    byteFramed(cipher, 17, new byte[] {0x42, 0x6f, 0x62, 0x00}),
                    fixed(cipher, 11, new byte[0]),
                    fixed(cipher, 5, new byte[0]));
            for (byte value : wire) channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {value}));
            assertTrue(channel.isActive());
            assertEquals(1, transport.keepAliveFrameCount());
            assertEquals(0, transport.unhandledFrameCount());
            assertEquals(5, transport.pendingActions());
            List<Action> applied = new ArrayList<Action>();
            assertEquals(5, transport.drainActions(applied::add));
            assertEquals(1184, ((DialogueClickAction) applied.get(0)).interfaceId());
            assertEquals(12345L, ((CountDialogueAction) applied.get(1)).count());
            assertEquals("Bob", ((StringDialogueAction) applied.get(2)).text());
            assertTrue(applied.get(3) instanceof PauseButtonAction);
            assertTrue(applied.get(4) instanceof CloseModalAction);
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    public void aKeepaliveBurstNeverFillsTheQueueOrClosesTheConnection() {
        Native950GameTransport transport = new Native950GameTransport(() -> 0, () -> 0,
                Thread.currentThread(), 1, 1);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            byte[] burst = new byte[256];
            for (int index = 0; index < burst.length; index++) burst[index] = 104;
            channel.writeInbound(Unpooled.wrappedBuffer(burst));
            assertTrue(channel.isActive());
            assertNull(transport.terminalFailure());
            assertEquals(0, transport.pendingActions());
            assertEquals(0, transport.unhandledFrameCount());
            assertEquals(256, transport.keepAliveFrameCount());
        } finally { channel.finishAndReleaseAll(); }
    }

    private static byte[] fixed(java.util.function.IntSupplier cipher, int opcode, byte[] payload) {
        byte[] framed = new byte[payload.length + 1];
        framed[0] = (byte) (opcode + cipher.getAsInt());
        System.arraycopy(payload, 0, framed, 1, payload.length);
        return framed;
    }

    private static byte[] byteFramed(java.util.function.IntSupplier cipher, int opcode, byte[] payload) {
        byte[] framed = new byte[payload.length + 2];
        framed[0] = (byte) (opcode + cipher.getAsInt());
        framed[1] = (byte) payload.length;
        System.arraycopy(payload, 0, framed, 2, payload.length);
        return framed;
    }

    private static byte[] concat(byte[]... packets) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (byte[] packet : packets) result.write(packet, 0, packet.length);
        return result.toByteArray();
    }
}
