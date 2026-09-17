package modern947;

import com.rs.game.Colour;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950PlayerEffects;
import com.rs.game.player.client.Native950PlayerEffects.DefinitionSource;
import com.rs.game.player.client.Native950PlayerEffects.Result;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

/** Engine adaptation fixtures, independently derived from the two caches and the 950 parser. */
public final class Native950PlayerEffectsTest {
    // Full raw definitions, not fixture hashes supplied by the implementation.
    private static final DefinitionSource DEFINITIONS = new DefinitionSource() {
        @Override public byte[] definition(int id) {
            switch (id) {
                case 53: return hex("01 0c 3d 00");
                case 94: return hex("01 80 00 e3 c4 02 37 d6 00");
                case 184: return hex("01 80 00 82 5c 02 01 48 04 00 a8 05 00 a8 28 01 00 3d 43 c0 00");
                case 436: return hex("01 24 21 02 0a 24 08 0f 07 14 00");
                case 1576: return hex("01 7f ce 02 22 ec 2e 00");
                // Present in 950, but changed since 910: existence alone must not admit it.
                case 93: return hex("01 80 00 d8 ac 02 37 d3 0a 00");
                default: return null;
            }
        }
    };

    @Test public void verifiesIdentitiesAgainstActualRunningDefinitionBytes() {
        assertTrue(Native950PlayerEffects.verifiedDefinitionCount() > 6000);
        for (int id : new int[] {53, 94, 184, 436, 1576})
            assertTrue("independent matching definition " + id, Native950PlayerEffects.isVerifiedGraphic(id, DEFINITIONS));
        assertFalse(Native950PlayerEffects.isVerifiedGraphic(93, DEFINITIONS));
        assertFalse(Native950PlayerEffects.isVerifiedGraphic(12, DEFINITIONS));
        assertFalse(Native950PlayerEffects.isVerifiedGraphic(65535, DEFINITIONS));
        assertFalse(Native950PlayerEffects.isVerifiedGraphic(-2, DEFINITIONS));
        assertFalse(Native950PlayerEffects.isVerifiedGraphic(94, id -> hex("00")));
        assertFalse(Native950PlayerEffects.isVerifiedGraphic(94, id -> null));
        assertFalse(Native950PlayerEffects.isVerifiedGraphic(94, id -> { throw new IllegalArgumentException("bad cache"); }));
        assertTrue(Native950PlayerEffects.isVerifiedGraphic(-1, id -> { throw new AssertionError("clear must not read definitions"); }));
    }

    @Test public void fourRealEntitySlotsReachTheIndependentWireFixture() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("effects-source", new WorldTile(3200, 3200, 0), channel);
            player.setNextGraphics(new Graphics(94, 0, 100));
            player.setNextGraphics(new Graphics(184, 10, 0, 1));
            player.setNextGraphics(new Graphics(436, 30, 64, 7));
            player.setNextGraphics(new Graphics(1576, 0, 10, 3));
            Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
            Result result = Native950PlayerEffects.append(player, builder, DEFINITIONS);
            assertEquals(4, result.graphicsCount());
            assertEquals(0, result.refusedCount());
            assertTrue(result.hasMasks());
            // Header 0x04000000, zero definition removals, four slot additions. Each record
            // resets the native selectors: slot 128-minus, ID LE+128, LE packed, rotation, LE offsets.
            assertArrayEquals(hex("10 80 04 04 00 04"
                    + " 80 de 00 00 00 64 00 00 ff fb 1f"
                    + " 7f 38 00 0a 00 00 00 01 ff fb 1f"
                    + " 7e 34 01 1e 00 40 00 07 ff fb 1f"
                    + " 7d a8 06 00 00 0a 00 03 ff fb 1f"), encode(builder));
            assertNotNull("snapshot must not consume the tick's queued graphics", player.getNextGraphics4());
            player.resetMasks();
            assertFalse(Native950PlayerEffects.append(player, Native950PlayerMasks.builder(), DEFINITIONS).hasMasks());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test public void sparseSlotsAreNotCompactedAndNullDoesNotClear() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[] {null, null, new Graphics(94), null}, null, builder);
        assertEquals(1, result.graphicsCount());
        assertArrayEquals(hex("10 80 04 04 00 01 7e de 00 00 00 00 00 00 ff fb 1f"), encode(builder));
    }

    @Test public void clearIsANullIdAdditionForItsSlotNotADefinitionRemoval() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        // Canonicalize fields that the null-ID sink never uses, including obsolete flags.
        Result result = append(new Graphics[] {null, null, new Graphics(-1, -99, -12, 0, true, 7), null}, null, builder);
        assertEquals(1, result.graphicsCount());
        assertEquals(0, result.refusedCount());
        assertArrayEquals(hex("10 80 04 04 00 01 7e 7f ff 00 00 00 00 00 ff fb 1f"), encode(builder));
    }

    @Test public void queueClearReplacesAllAlreadyPendingEntitySlots() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("effects-clear", new WorldTile(3200, 3200, 0), channel);
            for (int id : new int[] {94, 184, 436, 1576}) player.setNextGraphics(new Graphics(id));
            Native950PlayerEffects.queueClear(player);
            Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder().animation(855, 0);
            Result result = Native950PlayerEffects.append(player, builder, DEFINITIONS);
            assertEquals(4, result.graphicsCount());
            assertEquals(0, result.refusedCount());
            assertArrayEquals(hex("18 80 04 04 00 04"
                    + " 80 7f ff 00 00 00 00 00 ff fb 1f"
                    + " 7f 7f ff 00 00 00 00 00 ff fb 1f"
                    + " 7e 7f ff 00 00 00 00 00 ff fb 1f"
                    + " 7d 7f ff 00 00 00 00 00 ff fb 1f"
                    + " 03 57 7f ff 7f ff 7f ff 00"), encode(builder));
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test public void multipleSlotClearsAndAnAdditionCoexist() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[] {new Graphics(-1), new Graphics(94), null, new Graphics(-1)}, null, builder);
        assertEquals(3, result.graphicsCount());
        assertArrayEquals(hex("10 80 04 04 00 03"
                + " 80 7f ff 00 00 00 00 00 ff fb 1f"
                + " 7f de 00 00 00 00 00 00 ff fb 1f"
                + " 7d 7f ff 00 00 00 00 00 ff fb 1f"), encode(builder));
    }

    @Test public void graphicsAndAnimationUseNativeConsumptionOrder() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder().animation(855, 0);
        Result result = append(new Graphics[] {new Graphics(94), null, null, null}, null, builder);
        assertTrue(result.hasMasks());
        assertArrayEquals(hex("18 80 04 04 00 01 80 de 00 00 00 00 00 00 ff fb 1f"
                + " 03 57 7f ff 7f ff 7f ff 00"), encode(builder));
    }

    @Test public void refusedGraphicDoesNotDiscardTheIndependentAnimation() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder().animation(855, 0);
        Result result = append(new Graphics[] {new Graphics(93), null, null, null}, null, builder);
        assertFalse(result.hasMasks());
        assertEquals(1, result.refusedCount());
        assertArrayEquals(hex("08 03 57 7f ff 7f ff 7f ff 00"), encode(builder));
    }

    @Test public void rejectsUnprovenFlagsAndContinuesOtherSlots() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[] {new Graphics(94, 0, 0, 0, true),
                new Graphics(94, 0, 0, 0, false, 1), new Graphics(94), new Graphics(93)}, null, builder);
        assertEquals(3, result.refusedCount());
        assertEquals(1, result.graphicsCount());
        assertArrayEquals(hex("10 80 04 04 00 01 7e de 00 00 00 00 00 00 ff fb 1f"), encode(builder));
    }

    @Test public void refusesDelayBit15AndUnrepresentableHeightsInsteadOfTruncation() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[] {new Graphics(94, 32768, 0), new Graphics(94, -1, 0),
                new Graphics(94, 0, -1), new Graphics(94, 0, 32768)}, null, builder);
        assertEquals(4, result.refusedCount());
        assertFalse(result.hasMasks());
    }

    @Test public void maximumHeightAndDelayDoNotSetTheReservedPackedFlag() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[] {new Graphics(94, 32767, 32767, 7), null, null, null}, null, builder);
        assertEquals(0, result.refusedCount());
        assertArrayEquals(hex("10 80 04 04 00 01 80 de 00 ff 7f ff 7f 07 ff fb 1f"), encode(builder));
    }

    @Test public void legacyRotationWrapsExactlyAsItsExplicitThreeBitFieldDid() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        append(new Graphics[] {new Graphics(94, 0, 0, 8), null, null, null}, null, builder);
        assertArrayEquals(hex("10 80 04 04 00 01 80 de 00 00 00 00 00 00 ff fb 1f"), encode(builder));
    }

    @Test public void transparentLegacyColourClearsWithoutInventingAnHslConversion() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[4], new Colour(0, 5, 70, 110, 50, 0), builder);
        assertTrue(result.clearedColour());
        assertEquals(0, result.refusedCount());
        // Zero HSL/strength, LE+128 start=0, BE end=5; original invisible colour bytes irrelevant.
        assertArrayEquals(hex("10 80 20 00 80 00 80 80 00 00 05"), encode(builder));
    }

    @Test public void actualLegacyTintRemainsRefusedAndDoesNotLoseGraphics() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[] {new Graphics(94), null, null, null},
                new Colour(0, 50000, 70, 110, 50, 130), builder);
        assertEquals(1, result.refusedCount());
        assertFalse(result.clearedColour());
        assertEquals(Native950PlayerMasks.SPOTANIM_LIST, builder.build().maskBits());
    }

    @Test public void outOfRangeColourWindowIsRefusedWithoutWrapping() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[4], new Colour(-1, 70000, 0, 0, 0, 0), builder);
        assertEquals(1, result.refusedCount());
        assertFalse(result.hasMasks());
    }

    @Test public void noPendingEffectsProduceNoMaskOrRefusal() {
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        Result result = append(new Graphics[4], null, builder);
        assertFalse(result.hasMasks());
        assertEquals(0, result.refusedCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsTheWrongNumberOfEngineSlots() {
        append(new Graphics[3], null, Native950PlayerMasks.builder());
    }

    private static Result append(Graphics[] graphics, Colour colour, Native950PlayerMasks.Builder builder) {
        return Native950PlayerEffects.append(graphics, colour, builder, DEFINITIONS);
    }
    private static byte[] encode(Native950PlayerMasks.Builder builder) {
        return Native950PlayerMasks.encode(builder.build());
    }
    private static byte[] hex(String text) {
        String compact = text.replace(" ", "");
        byte[] out = new byte[compact.length() / 2];
        for (int i = 0; i < out.length; i++) out[i] = (byte) Integer.parseInt(compact.substring(i*2,i*2+2),16);
        return out;
    }
}
