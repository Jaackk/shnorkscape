package modern947;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.NpcAction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * The NPC option family is the one place in the inbound port where the modifier is stored raw.
 * Fixtures are computed from the 950 field list on {@code Native950Actions.decodeNpc}, which reads
 * the plain writer path at 950 0x1400e5802.
 */
public final class Native950NpcActionsTest {

    @Test
    public void sixNativeOptionsDecodeExactSenderBytes() {
        // Option order is the thunk chain at 950 0x14010bcc7 descending by 0x50; every thunk is
        // `lea rdx,[descriptor]; jmp 0x1400e56a0`, so the order is the option index.
        // 947 was {88, 115, 33, 60, 123, 104} and 947's body was index BE u16 then modifier+128.
        int[] opcodes = {60, 92, 27, 107, 13, 36};
        for (int i = 0; i < opcodes.length; i++) {
            // b0 modifier plain, b1 index>>>8, b2 (index & 255)+128. 947 fixture: 12 34 81.
            NpcAction action = (NpcAction) Native950Actions.decode(opcodes[i],
                    new byte[] {1, 0x12, (byte) 0xb4});
            assertEquals(i + 1, action.option());
            assertEquals(4660, action.index());
            assertEquals(1, action.modifier());
            assertTrue(Native950Actions.isImplemented(opcodes[i]));
        }
        NpcAction bank = (NpcAction) Native950Actions.decode(60, new byte[] {0, 0, (byte) 0x81});
        assertEquals(1, bank.index());
        assertEquals(0, bank.modifier());
    }

    @Test
    public void theModifierIsPlainHereAndBiasedOrNegatedInEveryOtherFamily() {
        // 0x1400e581d stores the bit with no transform at all. The object family negates the same
        // bit (0x1400e5432) and the player family adds 128 (0x1400e6670). Reading a biased byte
        // here would turn every index-0x80-ish frame into a "modified" click, so the values that
        // only the other families can produce must be refused.
        assertNull(Native950Actions.decode(60, new byte[] {(byte) 0x80, 0, (byte) 0x81}));
        assertNull(Native950Actions.decode(60, new byte[] {(byte) 0xff, 0, (byte) 0x81}));
        assertNull(Native950Actions.decode(60, new byte[] {2, 0, (byte) 0x81}));
    }

    @Test
    public void invalidNpcClaimsAreMalformedAndTargetedNpcUsesItsSeparateAction() {
        assertNull(Native950Actions.decode(60, null));
        assertNull(Native950Actions.decode(60, new byte[2]));
        assertNull(Native950Actions.decode(60, new byte[4]));
        // index 65535 is the client's "no target" fold, never a real slot.
        assertNull(Native950Actions.decode(60, new byte[] {0, (byte) 0xff, 0x7f}));
        // The separately derived selected writer19 carries source ownership fields, unlike
        // numbered NPC options. It must not accidentally become a normal interaction.
        assertTrue(Native950Actions.isImplemented(19));
        Native950Actions.Action selected = Native950Actions.decode(19, new byte[12]);
        assertTrue(selected instanceof Native950Actions.ItemOnNpcAction);
        assertFalse(selected instanceof NpcAction);
        assertNull(Native950Actions.underivedReason(19));
        assertNull(Native950Actions.decode(19, new byte[3]));
    }
}
