package modern947;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.ItemOnItemAction;
import com.rs.network.protocol.modern950.Native950Protocol;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950ItemOnItemTest {
    @Test public void literalNativeSenderFixtureKeepsDistinctEndpointTransforms() {
        byte[] bytes=hex("12 b4 34 78 56 de f0 9a bc 23 45 56 78 12 34 89 67 45");
        ItemOnItemAction use=(ItemOnItemAction)Native950Actions.decode(69,bytes);
        assertNotNull(use);
        assertEquals(0x12345678,use.sourceHash()); assertEquals(0x1234,use.sourceSlot()); assertEquals(0x345678,use.sourceItemId());
        assertEquals(0x9abcdef0,use.targetHash()); assertEquals(0x2345,use.targetSlot()); assertEquals(0x456789,use.targetItemId());
        assertEquals(0x1234,use.sourceInterfaceId()); assertEquals(0x5678,use.sourceComponentId());
        assertEquals(0x9abc,use.targetInterfaceId()); assertEquals(0xdef0,use.targetComponentId());
    }
    @Test public void actualTinderboxAndLogKeepTheirSelectedAndClickedOrder() {
        ItemOnItemAction use=(ItemOnItemAction)Native950Actions.decode(69,hex("00 80 00 4e 02 00 05 05 cd 00 01 00 05 05 cd e7 05 00"));
        assertEquals(590,use.sourceItemId()); assertEquals(0,use.sourceSlot());
        assertEquals(1511,use.targetItemId()); assertEquals(1,use.targetSlot());
        assertEquals(1485,use.sourceInterfaceId()); assertEquals(5,use.sourceComponentId());
        assertEquals(use.sourceHash(),use.targetHash());
    }
    @Test public void nativeAbsentSentinelsArePreservedForWorldRejection() {
        ItemOnItemAction use=(ItemOnItemAction)Native950Actions.decode(69,hex("ff 7f ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff"));
        assertEquals(-1,use.sourceSlot()); assertEquals(-1,use.sourceItemId());
        assertEquals(-1,use.targetSlot()); assertEquals(-1,use.targetItemId());
        assertEquals(-1,use.sourceHash()); assertEquals(-1,use.targetHash());
    }
    @Test public void framingIsNative18AndMalformedBodiesNeverCreateAnAction() {
        assertEquals(18,Native950Protocol.clientSize(69)); assertTrue(Native950Actions.isImplemented(69));
        assertNull(Native950Actions.decode(69,null));
        assertNull(Native950Actions.decode(69,new byte[17])); assertNull(Native950Actions.decode(69,new byte[19]));
        int occurrences=0;for(int opcode:Native950Actions.implementedOpcodes())if(opcode==69)occurrences++;
        assertEquals(1,occurrences);
        assertTrue(Native950Actions.decode(12,new byte[18]) instanceof Native950Actions.DragAction);
        assertTrue(Native950Actions.decode(69,new byte[18]) instanceof ItemOnItemAction);
    }
    private static byte[] hex(String text) {
        String[] words=text.split(" ");byte[] result=new byte[words.length];
        for(int i=0;i<words.length;i++)result[i]=(byte)Integer.parseInt(words[i],16);return result;
    }
}
