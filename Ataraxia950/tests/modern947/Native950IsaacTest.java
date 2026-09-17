package modern947;

import com.rs.network.modern.Native950Isaac;
import org.junit.Test;

import static org.junit.Assert.*;

public final class Native950IsaacTest {
    @Test
    public void matchesOpenNxtReferenceAcrossBlockRefills() {
        // Generated independently using OpenNXT/src/main/java/com/opennxt/util/ISAACCipher.java.
        int[] positions = {0, 1, 2, 3, 4, 5, 6, 7, 252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 1020, 1021, 1022, 1023};
        int[] expected = {0xdaf8863e, 0x74a5cb37, 0xafd4ed73, 0x877c7c44, 0x8fc83d9b, 0x606024ad, 0xff7a07aa, 0x4fb9c0c7,
                0xd04be89f, 0x347b9f8a, 0xd96568bd, 0x289edf7a, 0x3c3d3009, 0xd89ded00, 0xaa7699b4, 0xc747b45f,
                0x188b498e, 0xb46839b8, 0x745414ae, 0xf8b10f76, 0xab53e21d, 0x693d5443, 0xc90febda, 0x17699525};
        int[] seeds = {1, 2, 3, 4};
        Native950Isaac cipher = new Native950Isaac(seeds);
        seeds[0] = 99; // Constructed state must not retain the caller's mutable seed array.
        int fixture = 0;
        for (int position = 0; position < 1024; position++) {
            int actual = cipher.getAsInt();
            if (position == positions[fixture]) {
                assertEquals("ISAAC word " + position, expected[fixture], actual);
                fixture++;
                if (fixture == positions.length) break;
            }
        }
        assertEquals(positions.length, fixture);
    }
}
