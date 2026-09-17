package modern947;

import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Packets.Packet;
import org.junit.Test;
import static org.junit.Assert.*;

/** Independent literal fixtures decoded directly from the paired950 native parser. */
public final class Native950ObjectPacketsTest {
    @Test public void addUsesVariableByteFrameAndMixed32BitObjectId() {
        assertPacket(11, new byte[]{0x25,0x34,0x12,0x78,0x56,(byte)0xab},
                Native950Packets.objectAdd(0x12345678,10,3,2,5), true);
        assertPacket(11, new byte[]{0x12,1,0,0x66,0x14,(byte)0xa8},
                Native950Packets.objectAdd(70758,10,0,1,2), true);
    }
    @Test public void deleteUsesDifferentOffsetTransformAndNoIdentity() {
        assertPacket(26,new byte[]{0x5b,(byte)0xab},Native950Packets.objectRemove(10,3,2,5),false);
        assertPacket(26,new byte[]{9,(byte)0xd8},Native950Packets.objectRemove(22,0,7,7),false);
    }
    @Test public void ordinaryShapeNeverSetsTheExtendedTransformBit() {
        for (int shape=0;shape<=22;shape++) for(int rotation=0;rotation<4;rotation++) {
            byte[] body=Native950Packets.objectAdd(1,shape,rotation,0,0).payload();
            int packed=(body[5]+128)&255;
            assertEquals(0,packed&128);
            assertEquals(shape,packed>>>2);
            assertEquals(rotation,packed&3);
        }
    }
    @Test public void extendedMapTransformUsesFlagAndExactValidatedPayload() {
        byte[] transform={0x12,0x00,0x40,0x00,(byte)0x80}; // X translation64; uniform scale1.0.
        assertPacket(11,new byte[]{0x25,0x34,0x12,0x78,0x56,0x2b,0x12,0,0x40,0,(byte)0x80},
                Native950Packets.objectAdd(0x12345678,10,3,2,5,transform),true);
        for(byte[] malformed:new byte[][]{new byte[0],{1},{16,0},{0,0},{0x12,0,1,0}})
            reject(()->Native950Packets.objectAdd(1,10,0,0,0,malformed));
    }
    @Test public void refusesSentinelsUnsupportedShapesRotationsAndOffsets() {
        reject(()->Native950Packets.objectAdd(-1,10,0,0,0));
        for(int shape:new int[]{-1,23,31,32}) {
            reject(()->Native950Packets.objectAdd(1,shape,0,0,0));
            reject(()->Native950Packets.objectRemove(shape,0,0,0));
        }
        for(int rotation:new int[]{-1,4,255}) reject(()->Native950Packets.objectRemove(10,rotation,0,0));
        for(int coordinate:new int[]{-1,8,255}) {
            reject(()->Native950Packets.objectAdd(1,10,0,coordinate,0));
            reject(()->Native950Packets.objectRemove(10,0,0,coordinate));
        }
    }
    private static void assertPacket(int opcode,byte[] expected,Packet packet,boolean variable) {
        assertEquals(opcode,packet.type().opcode());
        assertEquals(variable?-1:expected.length,packet.type().size());
        assertArrayEquals(expected,packet.payload());
        byte[] frame=packet.frame(()->0);
        assertEquals(opcode,frame[0]&255);
        assertEquals(expected.length+(variable?2:1),frame.length);
        if(variable)assertEquals(expected.length,frame[1]&255);
    }
    private static void reject(Runnable action) {
        try { action.run(); fail("Invalid object packet accepted"); } catch(IllegalArgumentException expected) { }
    }
}
