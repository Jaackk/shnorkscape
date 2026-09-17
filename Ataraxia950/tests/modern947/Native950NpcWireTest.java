package modern947;

import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Literal fixtures for the single-NPC wire writers, against the native 950 reader.
 *
 * <p>Every expected string below was recomputed bit by bit from the 950 addition
 * record's field list - index16, dy[npcBits], immediate1, facing3, type16, plane2,
 * mask1, dx[npcBits] (950 0x14011fd20, bit reads at 950 0x14011fe08 and
 * 0x1401200aa..0x14012017d; plan A7) - not by running the writer. The 947 string
 * each one replaces is kept beside it as the re-derivation baseline: the 947 record
 * was index16, dy, plane2, dx, type16, immediate1, facing3, mask1, which is the same
 * total width, so if a 947-ordered body were still being produced nothing would
 * desync and only these fixtures would catch it.
 */
public final class Native950NpcWireTest {

    /** NPC_INFO moved from 947 opcode 12 to 950 opcode 80 (0x50); the size stayed -2. */
    @Test public void addsOneNpcUsingSceneWidthAndNativeFieldOrder() {
        Native950Packets.Packet packet = Native950Packets.staticNpcAdd(1, 494, 2, -3, 0, 7, 0);
        assertEquals(ServerPacket.NPC_INFO, packet.type());
        // index 1, dy -3 (1111101), immediate 1, facing 000, type 494 (0000000111101110),
        // plane 00, mask 0, dx 2 (0000010), then the 16-bit addition terminator.
        // 947 baseline: 0c 000a 00 0001 fa02 01ee 87fff8
        assertArrayEquals(hex("50 000a 00 0001 fb00 3dc0 17fff8"), packet.frame(() -> 0));
    }

    /**
     * Width 5 at both signed edges, with every non-offset field at its maximum. The
     * record is unaligned everywhere, so a single misplaced field shifts the rest:
     * index 65534, dy 15 (01111), immediate 1, facing 111, type 65535, plane 11,
     * mask 0, dx -16 (10000), terminator.
     */
    @Test public void signedFiveBitBoundaryPlaneAndFacingUseExactWidths() {
        // 947 baseline: 00 fffe 7f0f ffff 7fff80
        assertArrayEquals(hex("00 fffe 7fff ffe8 7fff80"),
                Native950Packets.staticNpcAdd(65534, 65535, -16, 15, 3, 5, 7).payload());
    }

    /**
     * Width 7 at both signed edges with index and type zero: dy 63 (0111111),
     * immediate 1, facing 100, type 0, plane 01, mask 0, dx -64 (1000000).
     */
    @Test public void signedSevenBitBoundaryAllowsIndexAndTypeZero() {
        // 947 baseline: 00 0000 7ec0 0000 c7fff8
        assertArrayEquals(hex("00 0000 7f80 000a 07fff8"),
                Native950Packets.staticNpcAdd(0, 0, -64, 63, 1, 7, 4).payload());
    }

    /** The retained section did not change at 950; only the opcode moved. */
    @Test public void retainsOneNpcOrRemovesAllWithoutAnyMaskOrSentinel() {
        assertArrayEquals(hex("50 0002 0100"), Native950Packets.singleNpcRetain().frame(() -> 0));
        assertArrayEquals(hex("50 0001 00"), Native950Packets.singleNpcRemove().frame(() -> 0));
    }

    @Test public void rejectsSentinelIndicesAndTruncatedCoordinatesBeforeFraming() {
        rejects(() -> Native950Packets.staticNpcAdd(-1, 494, 0, 0, 0, 7, 0));
        rejects(() -> Native950Packets.staticNpcAdd(65535, 494, 0, 0, 0, 7, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, -1, 0, 0, 0, 7, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 65536, 0, 0, 0, 7, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 64, 0, 0, 7, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, -65, 0, 0, 7, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 0, 16, 0, 5, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 0, -17, 0, 5, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 0, 0, -1, 7, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 0, 0, 4, 7, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 0, 0, 0, 0, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 0, 0, 0, 16, 0));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 0, 0, 0, 7, -1));
        rejects(() -> Native950Packets.staticNpcAdd(1, 494, 0, 0, 0, 7, 8));
    }

    private static void rejects(Runnable action) {
        try { action.run(); fail("Invalid NPC packet accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}
