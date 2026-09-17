package modern947;

import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950NpcMasks.BitWriter;
import com.rs.network.protocol.modern950.Native950NpcMasks.Update;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Byte-exact fixtures for the 950 NPC update-mask blocks.
 *
 * <p>Every expected string here was computed by hand from the field list derived out of
 * the 950 mask parser {@code 0x1401205E0} and its per-block selector tables in .rdata -
 * not by running the encoder. The 947 string each one replaces is kept beside it as the
 * re-derivation baseline, because the failure mode this file exists to catch is silent:
 * on this client a wrong mask bit or a wrong byte transform does not error, it selects a
 * different reader and shifts every field after it in the packet.
 *
 * <p>The selector alphabet, from the decoders themselves:
 * byte {@code 0x14010D850}/{@code 0x14010D8E0} - 0 plain, 1 {@code value+128},
 * 2 {@code -value}, 3 {@code 128-value}; u16 {@code 0x14010D970} - 0 big endian,
 * 1 little endian, 2 {@code ushort128}, 3 {@code ushortle128}.
 */
public final class Native950NpcMasksTest {

    /**
     * The port plan's own Stage 5 acceptance vector, end to end.
     *
     * <p>{@code 01} retained count 1; {@code 1 00} changed with selector 00 (mask only);
     * sixteen 1 bits = the addition terminator; five pad bits to the byte boundary - which
     * is {@code 01 9F FF E0}. Then the two bytes the client skips at {@code 0x14011F5DB},
     * then mask byte {@code 0x40} (bit 6 = SAY, and 0x40 does not set header marker 0x10 so
     * the header stays one byte), then the NUL-terminated text.
     *
     * <p>This is the one fixture that pins the header, the alignment and the two skipped
     * bytes at once with zero selector risk: SAY's body has no selector-driven field.
     */
    @Test public void planAcceptanceVectorForSayIsReproducedBitForBit() {
        BitWriter out = new BitWriter(16);
        Native950NpcMasks.retainedCount(out, 1);
        Native950NpcMasks.retainedMaskOnly(out);
        Native950NpcMasks.additionsEnd(out);
        out.align();
        out.bytes(Native950NpcMasks.maskBlock(new Update().say("Hi")));
        assertArrayEquals(hex("01 9f ff e0 00 00 40 48 69 00"), out.toByteArray());
    }

    /** SAY moved from 947 bit 0 to 950 bit 6; the NUL-terminated CP1252 body is unchanged. */
    @Test public void sayUsesBitSixAndAnUnchangedBody() {
        // 947 baseline: 00 00 01 48 69 00
        assertArrayEquals(hex("00 00 40 48 69 00"), Native950NpcMasks.maskBlock(new Update().say("Hi")));
    }

    /**
     * TRANSFORM moved from 947 bit 5 to 950 bit 2. The id still goes through
     * {@code smart2or4null}, whose 950 reader {@code 0x1400FEEA0} is instruction-identical
     * to 947's {@code 0x1401003F0} and consumes no selector byte, so 494 stays {@code 01 ee}.
     */
    @Test public void transformUsesBitTwoAndAnUnchangedSmartId() {
        // 947 baseline: 00 00 20 01 ee
        assertArrayEquals(hex("00 00 04 01 ee"), Native950NpcMasks.maskBlock(new Update().transform(494)));
    }

    /**
     * FACE_COORD moved from 947 bit 3 to 950 bit 7, and x changed transform.
     *
     * <p>Both shorts still carry {@code 2*tile+1}: 3200 encodes as 6401 = {@code 0x1901}.
     * Selector table {@code 0x140B60408} = {@code 00 01}, so x is a plain big-endian short
     * ({@code 19 01}) where 947 wrote {@code ushortle128} ({@code 81 19}); y stays little
     * endian ({@code 01 19}).
     */
    @Test public void faceCoordinateUsesBitSevenAndABigEndianX() {
        // 947 baseline: 00 00 08 81 19 01 19
        assertArrayEquals(hex("00 00 80 19 01 01 19"),
                Native950NpcMasks.maskBlock(new Update().faceCoordinate(3200, 3200)));
    }

    /**
     * NAME is still bit 18, but the header grew a different shape because header byte 1's
     * marker moved from 0x40 to 0x10: mask 0x40000 pulls in 0x2000 then 0x10, giving
     * {@code 10 20 04} where 947 gave {@code 40 20 04}.
     */
    @Test public void nameKeepsBitEighteenButGetsTheNewHeaderMarkers() {
        // 947 baseline: 00 00 40 20 04 42 6f 62 00
        assertArrayEquals(hex("00 00 10 20 04 42 6f 62 00"),
                Native950NpcMasks.maskBlock(new Update().name("Bob")));
    }

    /**
     * FORCE_MOVEMENT moved from 947 bit 12 to 950 bit 14, and eight of its nine fields
     * changed transform.
     *
     * <p>Selector table {@code 0x140B60420} = {@code 02 02 00 00 02 00 01 01 01}: dx1 and
     * dy1 are negated, dx2 and dy2 plain, dplane1 negated, dplane2 plain, and all three
     * shorts little endian. For (1,2,3,4,0,0,10,20,8192) that is {@code ff fe 03 04 00 00}
     * then {@code 0a 00}, {@code 14 00}, {@code 00 20}. Header: 0x4000 pulls in marker 0x10,
     * so {@code 10 40}.
     */
    @Test public void forceMovementUsesBitFourteenAndTheNewByteAndShortTransforms() {
        // 947 baseline: 00 00 40 10 01 82 03 fc 00 00 8a 00 94 00 80 20
        assertArrayEquals(hex("00 00 10 40 ff fe 03 04 00 00 0a 00 14 00 00 20"),
                Native950NpcMasks.maskBlock(new Update().forceMovement(1, 2, 3, 4, 0, 0, 10, 20, 8192)));
    }

    /**
     * COLOUR_TINT is still bit 28, but all six wire fields changed transform.
     *
     * <p>Selector table {@code 0x140B60414} = {@code 01 01 00 00 00 00}: hue and saturation
     * are {@code value+128} ({@code 86}, {@code 83}), luminance and opacity are plain
     * ({@code 64}, {@code c8}), and both delays are plain big-endian shorts. Header: 0x10000000
     * pulls in 0x800000, 0x2000 and 0x10, giving {@code 10 20 80 10} - all three of those
     * markers moved from their 947 values, so the header is a different four bytes even
     * though the mask bit did not move.
     */
    @Test public void colourTintKeepsBitTwentyEightAndChangesAllSixFields() {
        // 947 baseline: 00 00 40 20 40 10 7a 7d 9c b8 f4 01 03 e8
        assertArrayEquals(hex("00 00 10 20 80 10 86 83 64 c8 01 f4 03 e8"),
                Native950NpcMasks.maskBlock(new Update().colourTint(6, 3, 100, 200, 500, 1000)));
    }

    /**
     * The emit order is the order of the client's tests in {@code 0x1401205E0}, not bit
     * order and not 947's order. say (0x140120656) comes first, then transform
     * (0x140120AA2), then faceCoordinate (0x140121217) last. 947 read faceCoordinate,
     * then transform, then say - the exact reverse of these three.
     */
    @Test public void blocksAreEmittedInTheClientsConsumptionOrderNotBitOrder() {
        Update update = new Update().faceCoordinate(3200, 3200).transform(494).say("Hi");
        // 947 baseline (mask 0x29, faceCoordinate then transform then say):
        //   00 00 29 81 19 01 19 01 ee 48 69 00
        assertArrayEquals(hex("00 00 c4 48 69 00 01 ee 19 01 01 19"), Native950NpcMasks.maskBlock(update));
    }

    @Test public void animationTailUsesBitThreeFourSmartIdsAndPlainDelay() {
        // 947 baseline: 00 00 80 03 57 7f ff 7f ff 7f ff 80.
        assertArrayEquals(hex("00 00 08 03 57 7f ff 7f ff 7f ff 00"),
                Native950NpcMasks.maskBlock(new Update().animation(855, -1, -1, -1, 0)));
        assertArrayEquals(hex("00 00 08 7f fe 80 00 7f ff 80 01 00 00 ff ff ff ff ff"),
                Native950NpcMasks.maskBlock(new Update().animation(32766, 32767, 65536, Integer.MAX_VALUE, 255)));
    }

    @Test public void faceEntitySelectsKindsAndPermutesTheMedium() {
        assertArrayEquals(hex("00 00 02 12 01 34"), Native950NpcMasks.maskBlock(new Update().faceNpc(0x1234)));
        assertArrayEquals(hex("00 00 02 ab 02 cd"), Native950NpcMasks.maskBlock(new Update().facePlayer(0xabcd)));
        assertArrayEquals(hex("00 00 02 ff ff ff"), Native950NpcMasks.maskBlock(new Update().clearFaceEntity()));
    }

    @Test public void spotanimListsUseIndependent950Selectors() {
        // 950 0x140B603C8: addition count+128, slot plain, id ushort128,
        // packed [16,24,0,8], rotation 128-v, offsets [8,16,0].
        assertArrayEquals(hex("00 00 10 20 80 01 01 00 03 81 02 12 b4 34 12 56 34 7b 0c 20 00"),
                Native950NpcMasks.maskBlock(new Update().spotanims(new int[]{3},
                        Native950NpcMasks.Spotanim.of(2, 0x1234, 0x3456, 0x1234, 5, 1, 2))));
        assertArrayEquals(hex("00 00 10 20 80 01 01 ff ff 80"),
                Native950NpcMasks.maskBlock(new Update().spotanims(new int[]{-1})));
    }

    @Test public void spotanimLoopRestartsItsSelectorAndAcceptsTheNullId() {
        assertArrayEquals(hex("00 00 10 20 80 01 00 82"
                + " 00 ff 7f 00 00 00 00 80 00 00 00"
                + " ff 00 81 00 00 01 00 79 ff 3f ff"),
                Native950NpcMasks.maskBlock(new Update().spotanims(new int[0],
                        Native950NpcMasks.Spotanim.of(0, -1, 0, 0, 0, -1023, -1023),
                        Native950NpcMasks.Spotanim.of(255, 1, 1, 0, 7, 1024, 1024))));
    }

    @Test public void ordinaryHitsSupportSimpleDualAndUntypedRecords() {
        assertArrayEquals(hex("00 00 20 ff 01 81 2c 02 00"),
                Native950NpcMasks.maskBlock(new Update().hits(new Native950NpcMasks.Hit[]{
                        Native950NpcMasks.Hit.of(1, 300, 2)}, new Native950NpcMasks.Hitbar[0])));
        assertArrayEquals(hex("00 00 20 ff ff ff 02 81 90 03 81 f4 80 80 00"),
                Native950NpcMasks.maskBlock(new Update().hits(new Native950NpcMasks.Hit[]{
                        Native950NpcMasks.Hit.dual(2, 400, 3, 500, 128)}, new Native950NpcMasks.Hitbar[0])));
        // Untyped amount is NEGATED under NPC selector 02, not player's selector.
        assertArrayEquals(hex("00 00 20 ff ff fe 38 01 00"),
                Native950NpcMasks.maskBlock(new Update().hits(new Native950NpcMasks.Hit[]{
                        Native950NpcMasks.Hit.untyped(200, 1)}, new Native950NpcMasks.Hitbar[0])));
    }

    @Test public void hitbarsRespectRemovalCycleZeroAndSizedBranches() {
        assertArrayEquals(hex("00 00 20 00 01 0a ff ff"),
                Native950NpcMasks.maskBlock(new Update().hits(new Native950NpcMasks.Hit[0],
                        new Native950NpcMasks.Hitbar[]{Native950NpcMasks.Hitbar.remove(10)})));
        assertArrayEquals(hex("00 00 20 00 01 00 00 02 da 00"),
                Native950NpcMasks.maskBlock(new Update().hits(new Native950NpcMasks.Hit[0],
                        new Native950NpcMasks.Hitbar[]{Native950NpcMasks.Hitbar.update(0, 0, 2, 90, 90)})));
        assertArrayEquals(hex("00 00 20 00 01 04 03 05 e4 ce 80 80 07 77"),
                Native950NpcMasks.maskBlock(new Update().hits(new Native950NpcMasks.Hit[0],
                        new Native950NpcMasks.Hitbar[]{Native950NpcMasks.Hitbar.sized(4, 3, 5, 100, 50, 127, 7, 9)})));
    }

    @Test public void tailBlocksFollowFaceAndSpeechRegardlessOfSetterOrder() {
        assertArrayEquals(hex("00 00 6a 48 69 00 12 01 34 03 57 7f ff 7f ff 7f ff 00 ff ff fe 38 01 00"),
                Native950NpcMasks.maskBlock(new Update()
                        .hits(new Native950NpcMasks.Hit[]{Native950NpcMasks.Hit.untyped(200, 1)}, new Native950NpcMasks.Hitbar[0])
                        .animation(855, -1, -1, -1, 0).faceNpc(0x1234).say("Hi")));
    }

    @Test public void onlyTenDerivedBitsAreSupported() {
        int[] derived = {1, 2, 3, 5, 6, 7, 14, 18, 24, 28};
        for (int bit = 0; bit < 64; bit++) {
            boolean expected = false;
            for (int one : derived) if (one == bit) expected = true;
            assertEquals("bit " + bit, expected, Native950NpcMasks.isSupportedMaskBit(bit));
        }
        unsupported(() -> Native950NpcMasks.candidateMask(20), "not derived");
        unsupported(() -> Native950NpcMasks.candidateMask(33), "not derived");
    }

    @Test public void newBlocksRejectUnrepresentableInputsBeforeFraming() {
        invalid(() -> new Update().animation(-2, -1, -1, -1, 0));
        invalid(() -> new Update().animation(1, -1, -1, -1, 256));
        invalid(() -> new Update().faceNpc(65536)); invalid(() -> new Update().facePlayer(-1));
        invalid(() -> Native950NpcMasks.Spotanim.of(0, 65535, 0, 0, 0, 0, 0));
        invalid(() -> Native950NpcMasks.Spotanim.of(0, 1, 32768, 0, 0, 0, 0));
        invalid(() -> Native950NpcMasks.Spotanim.of(0, 1, 0, 0, 8, 0, 0));
        invalid(() -> Native950NpcMasks.Spotanim.of(0, 1, 0, 0, 0, -1024, 0));
        invalid(() -> new Update().spotanims(new int[0]));
        invalid(() -> new Update().spotanims(new int[]{32768}));
        invalid(() -> new Update().spotanims(new int[]{-1, 3}));
        invalid(() -> new Update().spotanims(new int[]{3, -1}));
        invalid(() -> Native950NpcMasks.Hit.of(32766, 1, 0));
        invalid(() -> Native950NpcMasks.Hit.untyped(256, 0));
        invalid(() -> Native950NpcMasks.Hitbar.sized(0, 0, 0, 1, 2, -1, 0, 0));
        invalid(() -> Native950NpcMasks.Hitbar.sized(0, 1, 0, 1, 2, -1, 1, 0));
        invalid(() -> new Update().hits(new Native950NpcMasks.Hit[256], new Native950NpcMasks.Hitbar[0]));
    }

    private static void invalid(Runnable body) {
        try { body.run(); fail("Expected IllegalArgumentException"); }
        catch (IllegalArgumentException expected) { }
    }

    /** An update with nothing set is a caller bug, not an empty block. */
    @Test public void anEmptyUpdateIsRejected() {
        try {
            Native950NpcMasks.maskBlock(new Update());
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("derived block"));
        }
    }

    private static void unsupported(Runnable body, String fragment) {
        try {
            body.run();
            fail("expected UnsupportedOperationException mentioning " + fragment);
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains(fragment));
        }
    }

    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}
