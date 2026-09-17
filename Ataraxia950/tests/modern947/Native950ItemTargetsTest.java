package modern947;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.ItemOnObjectAction;
import com.rs.network.protocol.modern950.Native950Actions.ItemOnNpcAction;
import com.rs.network.protocol.modern950.Native950Protocol;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950ItemTargetsTest {
    @Test public void asymmetricObjectFixtureMatchesTheNativeSelectedWriter() {
        // modifier1; x1234; slot2345; y3456; item56789a; object12345678; hash89abcdef.
        ItemOnObjectAction a=(ItemOnObjectAction)Native950Actions.decode(90,hex("ff 12 b4 23 45 d6 34 9a 78 56 78 56 34 12 cd ef 89 ab"));
        assertNotNull(a);assertEquals(1,a.modifier());assertEquals(0x1234,a.x());assertEquals(0x3456,a.y());
        assertEquals(0x2345,a.sourceSlot());assertEquals(0x56789a,a.sourceItemId());assertEquals(0x12345678,a.objectId());
        assertEquals(0x89abcdef,a.sourceHash());assertEquals(0x89ab,a.sourceInterfaceId());assertEquals(0xcdef,a.sourceComponentId());
    }
    @Test public void asymmetricNpcFixtureKeepsItsDifferentSlotAndHashEndianness() {
        ItemOnNpcAction a=(ItemOnNpcAction)Native950Actions.decode(19,hex("34 56 9a 78 56 ff 45 23 89 ab cd ef"));
        assertNotNull(a);assertEquals(0x3456,a.index());assertEquals(0x56789a,a.sourceItemId());assertEquals(1,a.modifier());
        assertEquals(0x2345,a.sourceSlot());assertEquals(0x89abcdef,a.sourceHash());
        assertEquals(0x89ab,a.sourceInterfaceId());assertEquals(0xcdef,a.sourceComponentId());
    }
    @Test public void backpackRawFishOnRangeKeepsSourceIdentityAndWorldCoordinates() {
        ItemOnObjectAction a=(ItemOnObjectAction)Native950Actions.decode(90,hex("00 0c 12 00 02 0d 0c 3d 01 00 72 00 00 00 00 05 05 c1"));
        assertEquals(1473,a.sourceInterfaceId());assertEquals(5,a.sourceComponentId());assertEquals(2,a.sourceSlot());
        assertEquals(317,a.sourceItemId());assertEquals(114,a.objectId());assertEquals(3218,a.x());assertEquals(3213,a.y());assertEquals(0,a.modifier());
    }
    @Test public void absentSourceValuesReachWorldValidationWithoutChangingTheirMeaning() {
        ItemOnObjectAction object=(ItemOnObjectAction)Native950Actions.decode(90,hex("00 00 80 ff ff 80 00 ff ff ff 00 00 00 00 ff ff ff ff"));
        assertEquals(-1,object.sourceSlot());assertEquals(-1,object.sourceItemId());assertEquals(-1,object.sourceHash());
        ItemOnNpcAction npc=(ItemOnNpcAction)Native950Actions.decode(19,hex("ff ff ff ff ff 00 ff ff ff ff ff ff"));
        assertEquals(-1,npc.sourceSlot());assertEquals(-1,npc.sourceItemId());assertEquals(-1,npc.sourceHash());assertEquals(65535,npc.index());
    }
    @Test public void malformedBoundsAndFramingNeverMakeActions() {
        for(int opcode:new int[]{90,19}) {
            assertTrue(Native950Actions.isImplemented(opcode));assertNull(Native950Actions.underivedReason(opcode));Native950Actions.requireDerived(opcode);
            int size=opcode==90?18:12;assertEquals(size,Native950Protocol.clientSize(opcode));
            assertNull(Native950Actions.decode(opcode,null));assertNull(Native950Actions.decode(opcode,new byte[size-1]));assertNull(Native950Actions.decode(opcode,new byte[size+1]));
        }
        byte[] object=hex("00 00 80 00 00 80 00 00 00 00 00 00 00 00 00 00 00 00");
        object[0]=1;assertNull(Native950Actions.decode(90,object));object[0]=0;
        object[1]=0x40;assertNull(Native950Actions.decode(90,object));object[1]=0;
        object[6]=0x40;assertNull(Native950Actions.decode(90,object));object[6]=0;
        object[13]=(byte)0x80;assertNull(Native950Actions.decode(90,object));
        byte[] npc=new byte[12];npc[5]=1;assertNull(Native950Actions.decode(19,npc));
    }
    private static byte[] hex(String s) {String[] words=s.split(" ");byte[] result=new byte[words.length];for(int i=0;i<words.length;i++)result[i]=(byte)Integer.parseInt(words[i],16);return result;}
}
