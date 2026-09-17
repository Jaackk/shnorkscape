package modern947;

import com.rs.game.player.client.Native950Appearance;
import com.rs.network.protocol.modern950.Native950Packets;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Literal bodies for the native 950-1 appearance, against the selected cache's wear positions.
 *
 * Only the wearpos slot section differs from 947: an unsigned LEB128 varint per slot with the
 * identity-kit base moved from 0x100 to 2, where 947 wrote a single 0 byte for an empty slot and
 * a big-endian unsigned short otherwise. The item base 0x800 did not move. Nothing else in the
 * body changed, so the two encodings differ in length by exactly what the varints save.
 *
 * This is the highest-risk encoding in the port: a 947-width body does not degrade, it
 * desynchronises the client's slot loop and the resulting out-of-range value reaches an unguarded
 * definition lookup that faults the process at 0x140131d53.
 */
public final class Native950AppearanceTest {
    private static final int[] WEAR_POSITIONS =
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0};

    @Test public void emptyEquipmentUsesTheVarintKitBaseOfTwo() {
        // Slots: four empty, kits 18/26/38/3/34/42/14 biased by 2, four empty. Each fits seven
        // bits, so every varint is one byte and the section is 16 bytes against 947's 23.
        // 947 asserted: 00 00 00 00 0112 00 011a 0126 0103 0122 012a 010e 00 00 00 00
        assertArrayEquals(hex("0000 00000000 14 00 1c 28 05 24 2c 10 00000000 "
                        + "0000 03101000000000000000 00000000000000000000 0a8b 7800 0300ff00"),
                new Native950Appearance(WEAR_POSITIONS).encode("x", emptyEquipment()));
    }

    @Test public void bronzeEquipmentKeepsTheItemBaseAndHidesOnlyHelmetHair() {
        // The three equipped items cross 0x7F, so each takes two varint bytes: 0x800+1139 = 3187
        // -> f3 18, 0x800+1277 = 3325 -> fd 19, 0x800+1173 = 3221 -> 95 19. The med helm still
        // hides the hair slot and leaves the beard visible.
        // 947 asserted: 0c73 00 00 0cfd 0112 0c95 011a 0126 00 0122 012a 010e 00 00 00 00
        int[] items = emptyEquipment();
        items[0] = 1139; items[3] = 1277; items[5] = 1173;
        assertArrayEquals(hex("0000 f318 00 00 fd19 14 9519 1c 28 00 24 2c 10 00000000 "
                        + "0000 03101000000000000000 00000000000000000000 0a18 7800 0300ff00"),
                new Native950Appearance(WEAR_POSITIONS).encode("x", items));
    }

    @Test public void removingEquipmentRestoresBodyAndDoesNotChangePreviousSnapshots() {
        int[] wear = WEAR_POSITIONS.clone();
        Native950Appearance appearance = new Native950Appearance(wear);
        wear[3] = 1;
        int[] items = emptyEquipment();
        byte[] bare = appearance.encode("x", items);
        items[0] = 1139; items[3] = 1277; items[5] = 1173;
        byte[] equipped = appearance.encode("x", items);
        byte[] detached = equipped.clone();
        Arrays.fill(items, -1);
        assertArrayEquals(bare, appearance.encode("x", items));
        assertArrayEquals(detached, equipped);
        assertFalse(Arrays.equals(bare, equipped));
    }

    @Test public void unverifiedCacheSlotsItemsAndNamesAreRefused() {
        int[] changedWear = WEAR_POSITIONS.clone(); changedWear[17] = 0;
        rejects(() -> new Native950Appearance(changedWear));
        Native950Appearance appearance = new Native950Appearance(WEAR_POSITIONS);
        rejects(() -> appearance.encode("x", new int[18]));
        int[] wrongSlot = emptyEquipment(); wrongSlot[5] = 1277;
        rejects(() -> appearance.encode("x", wrongSlot));
        int[] wrongItem = emptyEquipment(); wrongItem[3] = 1311;
        rejects(() -> appearance.encode("x", wrongItem));
        int[] invalidEmpty = emptyEquipment(); invalidEmpty[12] = -2;
        rejects(() -> appearance.encode("x", invalidEmpty));
        rejects(() -> appearance.encode("x\0y", emptyEquipment()));
        rejects(() -> appearance.encode("abcdefghijklz", emptyEquipment()));
        rejects(() -> appearance.encode("\uD83D\uDE00", emptyEquipment()));
    }

    @Test public void movingAppearanceSetsPendingMaskAndPreservesTheNativeRelativeStep() {
        // Opcode 36, mask 0x20, and the appearance body pre-biased b ^ 0x80.
        assertArrayEquals(hex("24 000c f20010 7ff4 00002083 2a803b"),
                Native950Packets.singlePlayerWalkStep(0, 1, hex("aa00bb")).frame(() -> 0));
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dy == 0) continue;
            byte[] moving = Native950Packets.singlePlayerWalkStep(dx, dy).payload();
            byte[] combined = Native950Packets.singlePlayerWalkStep(dx, dy, hex("55")).payload();
            assertEquals(1, (combined[0] >>> 6) & 1);
            combined[0] &= ~0x40;
            assertArrayEquals(moving, Arrays.copyOf(combined, 5));
            assertArrayEquals(hex("00002081d5"), Arrays.copyOfRange(combined, 5, combined.length));
        }
    }

    @Test public void movingAppearanceBoundsLengthAndOwnsItsPayload() {
        byte[] appearance = new byte[255];
        Arrays.fill(appearance, (byte) 0x91);
        Native950Packets.Packet packet = Native950Packets.singlePlayerWalkStep(-1, -1, appearance);
        Arrays.fill(appearance, (byte) 0);
        byte[] body = packet.payload();
        assertArrayEquals(hex("f23ff0 7ff4 0000207f"), Arrays.copyOf(body, 9));
        assertEquals(264, body.length);
        // The body is pre-biased on the wire, so 0x91 leaves as 0x91 ^ 0x80 = 0x11.
        assertEquals(0x11, body[263] & 255);
        rejects(() -> Native950Packets.singlePlayerWalkStep(0, 1, new byte[0]));
        rejects(() -> Native950Packets.singlePlayerWalkStep(0, 1, new byte[256]));
        rejects(() -> Native950Packets.singlePlayerWalkStep(0, 0, new byte[1]));
        rejects(() -> Native950Packets.singlePlayerWalkStep(2, 0, new byte[1]));
    }

    /**
     * P4 parity: the GlobalPlayerUpdater native branch (fed by the real Equipment
     * container and the strict 947 item decoder) emits exactly the bytes this
     * template produced for the empty set and the three verified bronze items.
     * Uses the same cache-free fixtures as Native950PlayerLifecycleTest.
     */
    @Test public void globalPlayerUpdaterNativeBodyIsByteIdenticalToTheTemplate() throws Exception {
        modern947.Native950PlayerLifecycleTest.seedCacheFreeDefinitions();
        io.netty.channel.embedded.EmbeddedChannel channel = new io.netty.channel.embedded.EmbeddedChannel();
        try {
            // the fixtures are JVM globals; restoreCacheFreeDefinitions() below puts them back
            com.rs.game.player.Player player = com.rs.game.player.Player.createNative950("x",
                    new com.rs.game.WorldTile(3222, 3222, 0), channel);
            Native950Appearance template = new Native950Appearance(WEAR_POSITIONS);
            int[] items = emptyEquipment();
            player.getAppearence().generateAppearenceData();
            assertArrayEquals(template.encode("x", items), player.getAppearence().getAppeareanceData());
            // Same 950 body as emptyEquipmentUsesTheVarintKitBaseOfTwo: the engine's own
            // GlobalPlayerUpdater must agree with the standalone encoder byte for byte, since
            // both reach the client through the same appearance block.
            assertArrayEquals(hex("0000 00000000 14 00 1c 28 05 24 2c 10 00000000 "
                    + "0000 03101000000000000000 00000000000000000000 0a8b 7800 0300ff00"),
                    player.getAppearence().getAppeareanceData());
            player.getEquipment().getItems().set(0, new com.rs.game.item.Item(1139, 1));
            player.getEquipment().getItems().set(3, new com.rs.game.item.Item(1277, 1));
            player.getEquipment().getItems().set(5, new com.rs.game.item.Item(1173, 1));
            items[0] = 1139; items[3] = 1277; items[5] = 1173;
            player.getAppearence().generateAppearenceData();
            assertArrayEquals(template.encode("x", items), player.getAppearence().getAppeareanceData());
            // Same 950 body as bronzeEquipmentKeepsTheItemBaseAndHidesOnlyHelmetHair.
            assertArrayEquals(hex("0000 f318 00 00 fd19 14 9519 1c 28 00 24 2c 10 00000000 "
                    + "0000 03101000000000000000 00000000000000000000 0a18 7800 0300ff00"),
                    player.getAppearence().getAppeareanceData());
            assertEquals(0, player.getAppearence().getNative950WithheldBodies());
        } finally {
            channel.finishAndReleaseAll();
            modern947.Native950PlayerLifecycleTest.restoreCacheFreeDefinitions();
        }
    }

    private static int[] emptyEquipment() {
        int[] items = new int[Native950Appearance.SLOT_COUNT];
        Arrays.fill(items, -1);
        return items;
    }

    private static void rejects(Runnable action) {
        try { action.run(); fail("Unverified appearance accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}
