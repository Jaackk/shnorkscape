package modern947;

import com.rs.network.protocol.modern950.Native950PlayerMasks;
import com.rs.network.protocol.modern950.Native950PlayerMasks.Update;
import org.junit.Test;
import static org.junit.Assert.*;

/** Independent native layouts: publication queues final XY, then uses force offsets from there. */
public final class Native950ForceMaskRebaseTest {
    @Test public void rebasingPreservesAnimationPlaneAndTimingWithoutMutatingTheSource() {
        Update original=Native950PlayerMasks.builder().animation(855,0)
                .forceMovement(Native950PlayerMasks.ForceMovement.of(0,0,0,2,0,1,0,60,0)).build();
        byte[] originalBytes=hex("09 0357 7fff 7fff 7fff 00 80 00 00 82 00 7f 0000 003c 0000");
        byte[] fromFinal=hex("09 0357 7fff 7fff 7fff 00 80 fe 00 80 00 7f 0000 003c 0000");
        assertArrayEquals(originalBytes,Native950PlayerMasks.encode(original));
        assertArrayEquals(fromFinal,Native950PlayerMasks.encode(original.rebaseForceMovement(0,-2)));
        assertArrayEquals("Another viewer still receives an independent rebased copy",fromFinal,
                Native950PlayerMasks.encode(original.rebaseForceMovement(0,-2)));
        assertArrayEquals(originalBytes,Native950PlayerMasks.encode(original));
    }
    @Test public void rebasingRejectsAnUnencodableSpanAndMissingForceBlock() {
        Update wide=Native950PlayerMasks.builder().forceMovement(
                Native950PlayerMasks.ForceMovement.of(-127,0,127,0,0,0,30,60,0)).build();
        try { wide.rebaseForceMovement(-127,0);fail("A 254-tile first offset cannot be truncated"); }
        catch(IllegalArgumentException expected) { }
        try { Native950PlayerMasks.builder().animation(855,0).build().rebaseForceMovement(0,0);fail(); }
        catch(IllegalStateException expected) { }
    }
    private static byte[] hex(String value) {
        String s=value.replace(" ","");byte[] b=new byte[s.length()/2];
        for(int i=0;i<b.length;i++) b[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);return b;
    }
}
