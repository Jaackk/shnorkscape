package modern947;

import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Packets.Packet;
import org.junit.Test;
import static org.junit.Assert.*;

/** Literal fixtures transcribed from the paired 950 native parser, not an encoder round trip. */
public final class Native950GroundPacketsTest {
    @Test public void zonePrefixSelectsSignedRelativeChunkCoordinatesAndPlane() {
        assertPacket(96, "07 fb 82", Native950Packets.zonePartialFollows(5, 7, 2));
        assertPacket(96, "f9 03 80", Native950Packets.zonePartialFollows(-3, -7, 0));
        assertPacket(96, "7f 80 83", Native950Packets.zonePartialFollows(-128, 127, 3));
    }

    @Test public void addUsesLittleEndian24BitIdentityAndBigEndianQuantity() {
        assertPacket(51, "56 34 12 5b 78 9a", Native950Packets.groundItemAdd(0x123456, 0x789a, 2, 5));
        assertPacket(51, "e3 03 00 80 00 01", Native950Packets.groundItemAdd(995, 1, 0, 0));
        assertPacket(51, "fe ff ff 09 ff ff", Native950Packets.groundItemAdd(0xfffffe, 65535, 7, 7));
    }

    @Test public void removeLeadsWithTransformedOffsetBeforeFull24BitIdentity() {
        assertPacket(109, "5b 56 34 12", Native950Packets.groundItemRemove(0x123456, 2, 5));
        assertPacket(109, "80 e3 03 00", Native950Packets.groundItemRemove(995, 0, 0));
    }

    @Test public void quantityUsesUnbiasedOffsetAndBigEndianFields() {
        assertPacket(70, "25 12 34 56 78 9a bc de", Native950Packets.groundItemCount(0x123456, 0x789a, 0xbcde, 2, 5));
        assertPacket(70, "77 00 03 e3 00 01 ff ff", Native950Packets.groundItemCount(995, 1, 65535, 7, 7));
    }

    @Test public void outOfRangeZonesAndPlanesAreRefusedBeforeFraming() {
        reject(() -> Native950Packets.zonePartialFollows(-129, 0, 0));
        reject(() -> Native950Packets.zonePartialFollows(128, 0, 0));
        reject(() -> Native950Packets.zonePartialFollows(0, -129, 0));
        reject(() -> Native950Packets.zonePartialFollows(0, 128, 0));
        reject(() -> Native950Packets.zonePartialFollows(0, 0, -1));
        reject(() -> Native950Packets.zonePartialFollows(0, 0, 4));
    }

    @Test public void sentinelIdentitiesAndOverflowingOrEmptyPilesAreRefused() {
        for (int id : new int[] {-1, 0xffffff, 0x1000000}) {
            reject(() -> Native950Packets.groundItemAdd(id, 1, 0, 0));
            reject(() -> Native950Packets.groundItemRemove(id, 0, 0));
            reject(() -> Native950Packets.groundItemCount(id, 1, 2, 0, 0));
        }
        for (int amount : new int[] {-1, 0, 65536, Integer.MAX_VALUE}) {
            reject(() -> Native950Packets.groundItemAdd(995, amount, 0, 0));
            reject(() -> Native950Packets.groundItemCount(995, amount, 1, 0, 0));
            reject(() -> Native950Packets.groundItemCount(995, 1, amount, 0, 0));
        }
    }

    @Test public void offsetsOutsideZoneAreNeverSilentlyWrapped() {
        for (int coordinate : new int[] {-1, 8, 255}) {
            reject(() -> Native950Packets.groundItemAdd(995, 1, coordinate, 0));
            reject(() -> Native950Packets.groundItemRemove(995, 0, coordinate));
            reject(() -> Native950Packets.groundItemCount(995, 1, 2, coordinate, 0));
        }
    }

    private static void assertPacket(int opcode, String expected, Packet packet) {
        String[] values = expected.split(" ");
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) bytes[i] = (byte) Integer.parseInt(values[i], 16);
        assertEquals(opcode, packet.type().opcode());
        assertEquals(bytes.length, packet.type().size());
        assertArrayEquals(bytes, packet.payload());
        byte[] frame = packet.frame(() -> 0);
        assertEquals(opcode, frame[0] & 255);
        assertEquals(bytes.length + 1, frame.length);
    }
    private static void reject(Runnable action) {
        try { action.run(); fail("Expected invalid ground packet input to be refused"); }
        catch (IllegalArgumentException expected) { }
    }
}
