package modern947;

import com.rs.game.player.client.Native950InventoryMenu;
import com.rs.network.protocol.modern950.Native950Packets;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950InventoryMenuTest {
    @Test public void useHasBothNativeSourceAndTargetGatesWithNoUnsupportedTargetFamily() {
        int mask = Native950InventoryMenu.EVENT_MASK;
        // Native950 14019f4e8 shifts11 then masks127; 1401699ba tests destination bit22.
        assertEquals(38, (mask >>> 11) & 127);
        assertEquals(1, (mask >>> 22) & 1);
        assertEquals(3, (mask >>> 18) & 7); // dynamic item -> grid -> panel -> backpack root
        assertEquals(1, (mask >>> 21) & 1);
        assertEquals(0, mask & (1 << 23)); // no alternate drag mode
        for (int operation = 1; operation <= 10; operation++) assertTrue((mask & (1 << operation)) != 0);
        assertEquals(0, ((mask >>> 11) & 127) & (1 | 8 | 16 | 64));
    }
    @Test public void menuMaskIsEncodedForEveryBackpackSlotInNative950ByteOrder() {
        byte[] expected = {(byte)0x37, (byte)0xfe, 0, 0x6d, 0, 0x1b, 0, (byte)0x80, 5, 0, (byte)0xc1, 5};
        assertArrayEquals(expected, Native950Packets.interfaceEvents(Native950InventoryMenu.INTERFACE,
                Native950InventoryMenu.ITEMS_COMPONENT, 0, 27, Native950InventoryMenu.EVENT_MASK).payload());
    }
    @Test public void ordinaryAuthoredOptionsDoNotUseTheLegacyConsecutiveIndices() {
        int[] expected = {0,1,2,3,0,0,0,4,5,0,0,0};
        for (int operation = 0; operation < expected.length; operation++)
            assertEquals("UI operation " + operation, expected[operation], Native950InventoryMenu.ordinaryCacheOption(operation));
        assertEquals(0, Native950InventoryMenu.ordinaryCacheOption(-1));
    }
    @Test public void specializedMenuBranchesCannotBecomeOrdinaryDropOperations() {
        for (int id : new int[]{35,5509,5510,5511,5512,5513,5514,5515,6099,6100,6101,6102,13561,13562,14632,19040,19042,19760,19865,19866,19867,19868,20709,21581,24199,24200,24202,24203,24205,27616,27618,27620,27622,27624,27996,28575,28686,28688,28690,28692,28694,29970,31089,31091,31093,31095,31097,31099,31101,35277,35279,35281,35283,35285,35287,35289,36619,36620,39784,39786,39788,39790,39792,41808,42679,42682,44155,51275,51276,51309,58451})
            assertFalse("special item " + id, Native950InventoryMenu.usesOrdinaryOperations(id,0,0,0));
        for (int category : new int[]{1825,3464,4040,4355,4568,5096,5248,5363})
            assertFalse("special category " + category, Native950InventoryMenu.usesOrdinaryOperations(1511,category,0,0));
        assertFalse(Native950InventoryMenu.usesOrdinaryOperations(1511,0,1,0));
        assertFalse(Native950InventoryMenu.usesOrdinaryOperations(1511,0,0,1));
        assertFalse(Native950InventoryMenu.usesOrdinaryOperations(-1,0,0,0));
        for (int id : new int[]{995,1511,526,527,590,1733,1741,317,315,12047})
            assertTrue(Native950InventoryMenu.usesOrdinaryOperations(id,0,0,0));
    }

}
