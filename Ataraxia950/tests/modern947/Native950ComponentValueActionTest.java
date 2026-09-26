package modern947;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.ComponentValueAction;
import com.rs.network.protocol.modern950.Native950Protocol;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Stage B Phase 3: client opcode 124, "component value/selection" (see the field note on
 * {@code Native950Actions.COMPONENT_VALUE_OPCODE}). The byte layout was derived independently by
 * disassembling the sender at 950 {@code 0x1401a9e80} (not copied from Artaven's decoder, though
 * it agrees with it byte-for-byte, which is expected: it is the same client). See
 * docs/STAGE-B-PHASE3-COMPONENT-VALUE-ACTIONS-20260926.md for the full derivation and for why
 * this opcode is NOT the transport used by Action Bar Equipment Binding.
 */
public final class Native950ComponentValueActionTest {

    private static byte[] bytes(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        return out;
    }

    @Test public void opcodeAgreesWithTheClientsOwnDescriptorTable() {
        assertEquals(11, Native950Protocol.clientSize(124));
        assertTrue(Native950Actions.isImplemented(124));
    }

    @Test public void decodesInterfaceHashSlotValueAndSelectedFlag() {
        // Field values matched to Artaven's own house-arrival fixture: interface 365 component 19,
        // dynamic slot 12545, value 1, selected true. Independently rederived here from the 950
        // sender rather than trusted from their decoder.
        ComponentValueAction a = (ComponentValueAction) Native950Actions.decode(124, bytes("00010000310113006d017f"));
        assertEquals(365, a.interfaceId());
        assertEquals(19, a.componentId());
        assertEquals(12545, a.slot());
        assertEquals(1, a.value());
        assertTrue(a.selected());
    }

    @Test public void decodesSignedValueAndAnExplicitDynamicSlot() {
        ComponentValueAction a = (ComponentValueAction) Native950Actions.decode(124, bytes("ffffffffffff13006d0180"));
        assertEquals(-1, a.value());
        assertEquals(-1, a.slot()); // all-ones slot bytes -> the 65535 sentinel
        assertFalse(a.selected());
    }

    @Test public void endianAndFieldOrderFixture() {
        // Deliberately distinct nibbles per field so a transposed byte or wrong shift direction
        // fails loudly instead of by coincidence.
        ComponentValueAction a = (ComponentValueAction) Native950Actions.decode(124, bytes("567812343456efcdab897f"));
        assertEquals(0x12345678, a.value());
        assertEquals(0x3456, a.slot());
        assertEquals(0x89abcdef, a.componentHash());
        assertEquals(0x89ab, a.interfaceId());
        assertEquals(0xcdef, a.componentId());
        assertTrue(a.selected());
    }

    @Test public void malformedFramesAreRejectedWithoutDisconnecting() {
        assertNull(Native950Actions.decode(124, bytes("00010000310113006d01"))); // 10 bytes, short
        assertNull(Native950Actions.decode(124, bytes("00010000310113006d017e"))); // flag byte 0x7e: neither sentinel
        assertNull(Native950Actions.decode(124, null));
    }
}
