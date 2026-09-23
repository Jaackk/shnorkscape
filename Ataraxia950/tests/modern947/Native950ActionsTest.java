package modern947;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.GroundItemAction;
import com.rs.network.protocol.modern950.Native950Actions.InterfaceAction;
import com.rs.network.protocol.modern950.Native950Actions.ObjectAction;
import com.rs.network.protocol.modern950.Native950Actions.PlayerAction;
import com.rs.network.protocol.modern950.Native950Actions.DragAction;
import com.rs.network.protocol.modern950.Native950Protocol;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Every byte string below was computed from the 950 field list (recorded on the matching branch of
 * {@code Native950Actions}, each citing the 950 sender it came from), not by running the decoder
 * and copying what it produced. The 947 fixture it replaces is kept beside it so the two can be
 * diffed: in almost every family the 947 bytes are still a well-formed 950 frame of the right
 * length, which is exactly why a wrong port here is invisible.
 */
public final class Native950ActionsTest {

    @Test
    public void everyImplementedOpcodeAgreesWithTheClientsOwnDescriptorTable() {
        // Two independently derived tables: the opcode set here comes from sweeping .text for
        // references into the descriptor array at 950 0x140e942e0, the sizes come from the
        // registrar walk that produced Native950Protocol.CLIENT_SIZES. A disagreement means one
        // of the two derivations is wrong, and the decoder would then mis-frame or mis-read.
        for (int opcode : Native950Actions.implementedOpcodes()) {
            assertTrue("opcode " + opcode + " is outside the 950 client table",
                    Native950Protocol.clientSize(opcode) != Native950Protocol.UNKNOWN_SIZE);
            assertTrue("opcode " + opcode + " is implemented but not reported as such",
                    Native950Actions.isImplemented(opcode));
        }
        // 55 rows: 10 IF_BUTTON + 6 object + 6 NPC + 10 player + 6 ground item + 17 singles.
        assertEquals(58, Native950Actions.implementedOpcodes().length);
        int previous = -1;
        for (int opcode : Native950Actions.implementedOpcodes()) {
            assertTrue("duplicate or unsorted opcode " + opcode, opcode > previous);
            previous = opcode;
        }
    }
    @Test public void tileTargetPacketMatchesExact950SenderAndRejectsMalformedCoordinates(){
        byte[] body={0,7,(byte)255,(byte)255,(byte)255,5,(byte)150,0,64,12,(byte)0xfd,(byte)0xca,12};
        Native950Actions.InterfaceOnTileAction action=(Native950Actions.InterfaceOnTileAction)Native950Actions.decode(85,body);
        assertNotNull(action);assertEquals(1430,action.sourceInterfaceId());assertEquals(64,action.sourceComponentId());
        assertEquals(7,action.sourceSlot());assertEquals(-1,action.sourceItemId());
        assertEquals(3197,action.x());assertEquals(3274,action.y());assertEquals(13,Native950Protocol.clientSize(85));
        assertNull(Native950Actions.decode(85,java.util.Arrays.copyOf(body,12)));
        body[9]=64;assertNull(Native950Actions.decode(85,body));
    }

    @Test
    public void musicCompletionUsesResourceIdInPlainBigEndianBytes() {
        // 950 opcode 110 (947 opcode 59). Body unchanged: 0x14009543f bswap edx then one store.
        Native950Actions.MusicEndedAction action = (Native950Actions.MusicEndedAction)
                Native950Actions.decode(110, new byte[] {0x12, 0x34, 0x56, 0x78});
        assertEquals(0x12345678, action.archiveId());
        assertEquals(36067, ((Native950Actions.MusicEndedAction) Native950Actions.decode(110,
                new byte[] {0, 0, (byte) 0x8c, (byte) 0xe3})).archiveId());
        assertEquals(-1, ((Native950Actions.MusicEndedAction) Native950Actions.decode(110,
                new byte[] {-1, -1, -1, -1})).archiveId());
        assertEquals(Integer.MIN_VALUE, ((Native950Actions.MusicEndedAction) Native950Actions.decode(110,
                new byte[] {(byte) 0x80, 0, 0, 0})).archiveId());
        assertTrue(Native950Actions.isImplemented(110));
        assertNull(Native950Actions.decode(110, null));
        assertNull(Native950Actions.decode(110, new byte[3]));
        assertNull(Native950Actions.decode(110, new byte[5]));
        // 947's opcode 59 is now IF_BUTTON option 10, a 9-byte frame. Four bytes of resource id
        // would have been read as a truncated button click rather than rejected.
        assertNull(Native950Actions.decode(59, new byte[] {0x12, 0x34, 0x56, 0x78}));
    }

    @Test
    public void dragDecodesDistinctSourceAndTargetItemsHashesAndSlots() {
        // 950 opcode 12, 18 bytes, writer 0x1401abf30. Fields, in order:
        //   srcSlot LE u16, srcHash intv1, srcItem LE u24,
        //   tgtSlot LE u16, tgtHash intv2, tgtItem [i>>>16, i, i>>>8].
        // src 0x2345 / 0x12345678 / 0x1234, tgt 0x3456 / 0x01234567 / 0x4567.
        // 947 fixture was 16 bytes: 12 b4 78 56 34 12 01 23 45 67 e7 45 56 34 23 45.
        DragAction action = (DragAction) Native950Actions.decode(12, new byte[] {
                69, 35, 86, 120, 18, 52, 52, 18, 0,
                86, 52, 35, 1, 103, 69, 0, 103, 69});
        assertEquals(0x2345, action.sourceSlot());
        assertEquals(0x12345678, action.sourceComponentHash());
        assertEquals(0x1234, action.sourceInterfaceId());
        assertEquals(0x5678, action.sourceComponentId());
        assertEquals(0x1234, action.sourceItemId());
        assertEquals(0x3456, action.targetSlot());
        assertEquals(0x01234567, action.targetComponentHash());
        assertEquals(0x0123, action.targetInterfaceId());
        assertEquals(0x4567, action.targetComponentId());
        assertEquals(0x4567, action.targetItemId());
        assertNull(Native950Actions.decode(12, new byte[17]));
        assertNull(Native950Actions.decode(12, new byte[19]));
        assertNull(Native950Actions.decode(12, null));
        // 947's drag opcode 40 is size 9 in the 950 table and belongs to no implemented family.
        assertFalse(Native950Actions.isImplemented(40));
    }

    @Test
    public void numberedInterfaceFamilyUsesTheNativeTableAndTheNewHashOrder() {
        // Option order is the pointer array at 950 0x140b63c00, read at 0x1401a9916 by option index.
        int[] opcodes = {18, 122, 89, 100, 81, 126, 49, 66, 31, 59};
        // item 1511 BE u24, hash 1473:5 as intv2, slot 2 BE u16.
        // 947 fixture for the same click: 05 e7 00 05 05 c1 00 02 (8 bytes, u16 item).
        byte[] payload = {0, 5, (byte) 0xe7, (byte) 0xc1, 5, 5, 0, 0, 2};
        for (int index = 0; index < opcodes.length; index++) {
            assertEquals(index+1,Native950Actions.interfaceOption(opcodes[index]));
            InterfaceAction action = (InterfaceAction) Native950Actions.decode(opcodes[index], payload);
            assertEquals(index + 1, action.option());
            assertEquals(1473, action.interfaceId());
            assertEquals(5, action.componentId());
            assertEquals((1473 << 16) | 5, action.componentHash());
            assertEquals(1511, action.itemId());
            assertEquals(2, action.slot());
        }
        assertEquals(0,Native950Actions.interfaceOption(12));
        assertEquals(0,Native950Actions.interfaceOption(87));
        // All nine bytes distinct, so a swapped hash byte cannot pass by coincidence.
        InterfaceAction action = (InterfaceAction) Native950Actions.decode(18,
                new byte[] {0, 18, 52, 52, 18, 120, 86, 69, 103});
        assertEquals(0x12345678, action.componentHash());
        assertEquals(0x1234, action.itemId());
        assertEquals(0x4567, action.slot());
    }

    @Test
    public void interfaceSentinelsAreAbsentTargetsAndOldRevisionOpcodesAreNotReused() {
        InterfaceAction action = (InterfaceAction) Native950Actions.decode(18,
                new byte[] {-1, -1, -1, (byte) 0xc5, 5, 1, 0, -1, -1});
        assertEquals(-1, action.itemId());
        assertEquals(-1, action.slot());
        assertEquals(1477, action.interfaceId());
        assertEquals(1, action.componentId());
        // The 24-bit item field is not folded by the client, so a value that was already narrowed
        // to a ushort upstream arrives as 0x00FFFF; both spellings mean "no item".
        assertEquals(-1, ((InterfaceAction) Native950Actions.decode(18,
                new byte[] {0, -1, -1, (byte) 0xc5, 5, 1, 0, 0, 1})).itemId());
        // 947's IF_BUTTON1 was opcode 96, which does not exist in the 950 client table at all.
        assertFalse(Native950Actions.isImplemented(96));
        assertNull(Native950Actions.decode(96, new byte[9]));
        assertNull(Native950Actions.decode(18, new byte[8]));
        assertNull(Native950Actions.decode(18, new byte[10]));
        assertNull(Native950Actions.decode(18, null));
    }

    @Test
    public void numberedObjectFamilyUsesNativeIdCoordinateAndModifierTransforms() {
        // Thunk chain 950 0x14010bef7, stride -0x50; plain writer path 0x1400e5377.
        int[] opcodes = {34, 48, 24, 41, 73, 79};
        // y ushort128 low-first, id BE i32, x high-then-biased-low, -modifier.
        // 947 fixture for the same click: 0c 01 ff 80 0c 23 01 67 45.
        byte[] payload = {0, 12, 1, 35, 69, 103, 12, 1, (byte) 0xff};
        for (int index = 0; index < opcodes.length; index++) {
            ObjectAction action = (ObjectAction) Native950Actions.decode(opcodes[index], payload);
            assertEquals(index + 1, action.option());
            assertEquals(3201, action.x());
            assertEquals(3200, action.y());
            assertEquals(0x01234567, action.objectId());
            assertEquals(1, action.modifier());
        }
    }

    @Test
    public void impossibleObjectFieldsAndTruncatedPayloadsAreRejected() {
        byte[] valid = {0, 12, 1, 35, 69, 103, 12, 1, 0};
        assertNotNull(Native950Actions.decode(34, valid));
        assertEquals(0, ((ObjectAction) Native950Actions.decode(34, valid)).modifier());
        byte[] invalid = valid.clone(); invalid[1] = 0x40;       // y past 14 bits
        assertNull(Native950Actions.decode(34, invalid));
        invalid = valid.clone(); invalid[6] = 0x40;              // x past 14 bits
        assertNull(Native950Actions.decode(34, invalid));
        invalid = valid.clone(); invalid[2] = (byte) 0x80;       // negative object id
        assertNull(Native950Actions.decode(34, invalid));
        invalid = valid.clone(); invalid[8] = 1;                 // modifier is negated, never 1
        assertNull(Native950Actions.decode(34, invalid));
        assertNull(Native950Actions.decode(34, new byte[8]));
        assertNull(Native950Actions.decode(34, new byte[10]));
        assertNull(Native950Actions.decode(34, null));
    }

    @Test
    public void tenPlayerOptionsDecodeALittleEndianIndexAndABiasedModifier() {
        // Thunk chain 950 0x14010b877, writer 0x1400e65d0. The player family had no decoder at
        // all before the 950 port; it is the one family whose absence was silent rather than wrong.
        int[] opcodes = {20, 46, 71, 39, 37, 94, 51, 63, 117, 58};
        for (int index = 0; index < opcodes.length; index++) {
            PlayerAction action = (PlayerAction) Native950Actions.decode(opcodes[index],
                    new byte[] {52, 18, (byte) 0x81});
            assertEquals(index + 1, action.option());
            assertEquals(4660, action.index());
            assertEquals(1, action.modifier());
            assertTrue(Native950Actions.isImplemented(opcodes[index]));
        }
        PlayerAction plain = (PlayerAction) Native950Actions.decode(20, new byte[] {1, 0, (byte) 0x80});
        assertEquals(1, plain.index());
        assertEquals(0, plain.modifier());
        // The NPC family's byte order would read index 4660 as 13330 here; reject the shapes that
        // cannot come from 0x1400e65d0 instead.
        assertNull(Native950Actions.decode(20, new byte[] {1, 0, 0}));
        assertNull(Native950Actions.decode(20, new byte[] {1, 0, (byte) 0x82}));
        assertNull(Native950Actions.decode(20, new byte[] {-1, -1, (byte) 0x80}));
        assertNull(Native950Actions.decode(20, new byte[2]));
        assertNull(Native950Actions.decode(20, new byte[4]));
        assertNull(Native950Actions.decode(20, null));
    }

    @Test
    public void sixGroundItemOptionsCarryCoordinatesAndAFourValuedFirstByte() {
        // Jump table 950 0x1400e65b0; writers 0x1400e60ee and 0x1400e5ad9.
        int[] opcodes = {127, 103, 22, 56, 52, 113};
        byte[] payload = {127, 12, (byte) 0x81, 12, (byte) 0x80, 0, 5, (byte) 0xe7};
        for (int index = 0; index < opcodes.length; index++) {
            GroundItemAction action = (GroundItemAction) Native950Actions.decode(opcodes[index], payload);
            assertEquals(index + 1, action.option());
            assertEquals(3201, action.x());
            assertEquals(3200, action.y());
            assertEquals(1511, action.itemId());
            assertEquals(1, action.modifier());
            assertEquals(0, action.secondFlag());
        }
        // 0x1400e5ad9 or's a second bit in before the 128-minus, so 0x7D and 0x7E are legal too.
        GroundItemAction both = (GroundItemAction) Native950Actions.decode(127,
                new byte[] {125, 12, (byte) 0x81, 12, (byte) 0x80, 0, 5, (byte) 0xe7});
        assertEquals(1, both.modifier());
        assertEquals(1, both.secondFlag());
        GroundItemAction second = (GroundItemAction) Native950Actions.decode(127,
                new byte[] {126, 12, (byte) 0x81, 12, (byte) 0x80, 0, 5, (byte) 0xe7});
        assertEquals(0, second.modifier());
        assertEquals(1, second.secondFlag());
        assertEquals(-1, ((GroundItemAction) Native950Actions.decode(127,
                new byte[] {(byte) 0x80, 12, (byte) 0x81, 12, (byte) 0x80, -1, -1, -1})).itemId());
        // 0x7C would need a third flag bit, which the writer has no source for.
        assertNull(Native950Actions.decode(127, new byte[] {124, 12, (byte) 0x81, 12, (byte) 0x80, 0, 5, (byte) 0xe7}));
        assertNull(Native950Actions.decode(127, new byte[7]));
        assertNull(Native950Actions.decode(127, new byte[9]));
        assertNull(Native950Actions.decode(127, null));
    }

    @Test
    public void theThreeReportRowsDecodeAndTruncatedOnesDoNot() {
        Native950Actions.WindowReportAction window = (Native950Actions.WindowReportAction)
                Native950Actions.decode(9, new byte[] {3, 3, 32, 2, 88, 1});
        assertEquals(3, window.displayMode());
        assertEquals(800, window.width());
        assertEquals(600, window.height());
        assertEquals(1, window.flag());
        assertEquals(1234, ((Native950Actions.MapBuildReportAction)
                Native950Actions.decode(98, new byte[] {0, 0, 4, (byte) 0xd2})).elapsed());
        assertEquals(-1, ((Native950Actions.WorldListFetchAction)
                Native950Actions.decode(108, new byte[] {-1, -1, -1, -1})).checksum());
        assertNull(Native950Actions.decode(9, new byte[5]));
        assertNull(Native950Actions.decode(98, new byte[3]));
        assertNull(Native950Actions.decode(108, new byte[5]));
    }

    @Test
    public void rowsWithNoDerivedMeaningStayUnhandledAndSayWhy() {
        // These are framed by the client descriptor table and counted by the transport. They must
        // never become an Action: inventing one puts unvalidated fields in front of content code.
        for (int opcode : new int[] {112}) {
            assertFalse(Native950Actions.isImplemented(opcode));
            assertNull(Native950Actions.decode(opcode, new byte[Native950Protocol.clientSize(opcode)]));
            assertNotNull(Native950Actions.underivedReason(opcode));
            assertTrue(Native950Actions.underivedReason(opcode).contains("target variant"));
        }
        for (int opcode : new int[] {114, 10, 32}) {
            assertFalse(Native950Actions.isImplemented(opcode));
            assertNull(Native950Actions.decode(opcode, new byte[Native950Protocol.clientSize(opcode)]));
            assertTrue(Native950Actions.underivedReason(opcode).contains("count-int"));
        }
        for (int opcode : new int[] {97, 128, 75}) {
            assertFalse(Native950Actions.isImplemented(opcode));
            assertTrue(Native950Actions.underivedReason(opcode).contains("camera"));
        }
        // 947 opcode 32 is 950 opcode 114; a mechanical substitution would have kept 32 pointing
        // at a four-byte count while 950 opcode 32 is a two-byte packet, so the frame would have
        // been two bytes short and every following packet mis-framed.
        assertEquals(4, Native950Protocol.clientSize(114));
        assertEquals(2, Native950Protocol.clientSize(32));
        for (int opcode : new int[] {112, 114, 10, 32, 97, 128, 75}) {
            try {
                Native950Actions.requireDerived(opcode);
                fail("opcode " + opcode + " must refuse to be treated as derived");
            } catch (UnsupportedOperationException expected) {
                assertTrue(expected.getMessage().contains(String.valueOf(opcode)));
            }
        }
        assertNull(Native950Actions.underivedReason(18));
        Native950Actions.requireDerived(18);
    }
}
