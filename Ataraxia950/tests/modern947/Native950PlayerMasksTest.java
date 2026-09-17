package modern947;

import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import com.rs.network.protocol.modern950.Native950PlayerMasks.Animation;
import com.rs.network.protocol.modern950.Native950PlayerMasks.ColourOverlay;
import com.rs.network.protocol.modern950.Native950PlayerMasks.FaceEntity;
import com.rs.network.protocol.modern950.Native950PlayerMasks.ForceMovement;
import com.rs.network.protocol.modern950.Native950PlayerMasks.Hit;
import com.rs.network.protocol.modern950.Native950PlayerMasks.Hitbar;
import com.rs.network.protocol.modern950.Native950PlayerMasks.Spotanim;
import com.rs.network.protocol.modern950.Native950PlayerMasks.SpotanimList;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Literal 950 wire fixtures independently derived from the native reader. See
 * protocol-analysis/player-masks-950-derived.md for instruction addresses and byte selectors.
 * Unknown raw blocks still fail closed. These tests do not treat the encoder's output as truth.
 */
public final class Native950PlayerMasksTest {

    // ---------------------------------------------------------------- single masks

    @Test public void forceMovementUsesSixDistinctByteTransformsAndLeBeLeShorts() {
        assertArrayEquals(hex("01 7f 00 fe 82 00 80 00 00 00 0a 00 20"), encode(
                Native950PlayerMasks.builder().forceMovement(
                        ForceMovement.of(1, 0, 2, 2, 0, 0, 0, 10, 8192))));
        assertArrayEquals(hex("01 8c 17 22 ad 01 7e 34 12 56 78 45 23"), encode(
                Native950PlayerMasks.builder().forceMovement(
                        ForceMovement.of(-12, 23, -34, 45, -1, 2, 0x1234, 0x5678, 0x2345))));
    }

    @Test public void animationUsesFourBigSmartsAndPlainDelay() {
        assertArrayEquals(hex("08 03 57 7f ff 7f ff 7f ff 00"),
                encode(Native950PlayerMasks.builder().animation(855, 0)));
    }

    @Test public void animationCoversSmartBoundarySentinelAndFullIntRange() {
        assertArrayEquals(hex("08 80 00 7f ff 7f ff 7f ff 7f ff 01"),
                encode(Native950PlayerMasks.builder().animation(0x7FFF, 1)));
        assertArrayEquals(hex("08 7f fe 80 00 ff ff 7f ff ff ff ff ff ff"),
                encode(Native950PlayerMasks.builder().animation(
                        Animation.of(new int[] {0x7FFE, 0xFFFF, -1, 0x7FFFFFFF}, 255))));
    }

    @Test public void faceAngleIsAPlainBigEndianShort() {
        // Bit 0x2 on 950 (947: 0x80); the body is one of the three the plan states did not change.
        assertArrayEquals(hex("02 20 00"), encode(Native950PlayerMasks.builder().faceAngle(8192)));
        assertArrayEquals(hex("02 3f ff"), encode(Native950PlayerMasks.builder().faceAngle(16383)));
    }

    @Test public void forceTalkCarriesTheNulTerminatedStringAndFlagsByte() {
        // Bit 0x400000 on 950 (947: 0x10000). Byte 3 of the mask, so the header pulls in both
        // lower extension markers: 0x400000 | 0x8000 | 0x10 emits 10 80 40.
        assertArrayEquals(hex("10 80 40 48 69 00 01"),
                encode(Native950PlayerMasks.builder().forceTalk("Hi", 1)));
    }

    @Test public void forceTalkLocalCarriesTheStringOnly() {
        // Bit 0x400 on 950 (947: 0x8000): byte 2, so only the first extension marker is pulled in.
        assertArrayEquals(hex("10 04 48 69 00"),
                encode(Native950PlayerMasks.builder().forceTalkLocal("Hi")));
    }

    @Test public void hitsUseBiasedCountAndUnchangedUnsignedSmarts() {
        assertArrayEquals(hex("40 81 80 84 19 00 00"),
                encode(Native950PlayerMasks.builder().hit(Hit.of(132, 25, 0))));
        assertArrayEquals(hex("40 81 7f 80 80 ff ff 00"),
                encode(Native950PlayerMasks.builder().hit(Hit.of(127, 128, 32767))));
    }

    @Test public void hitFormMarkersSelectDualAndPlainByteUntypedDamage() {
        assertArrayEquals(hex("40 81 ff ff 01 02 03 04 00 00"),
                encode(Native950PlayerMasks.builder().hit(Hit.dual(1, 2, 3, 4, 0))));
        assertArrayEquals(hex("40 81 ff fe ff 00 00"),
                encode(Native950PlayerMasks.builder().hit(Hit.untyped(255, 0))));
    }

    @Test public void hitbarsUseNegatedCountAndPercentThen128MinusSecondPercent() {
        assertArrayEquals(hex("40 80 ff 00 01 00 9c 4e 00"), encode(
                Native950PlayerMasks.builder().hits(Collections.<Hit>emptyList(),
                        Arrays.asList(Hitbar.update(0, 1, 0, 100, 50)))));
        assertArrayEquals(hex("40 80 ff 00 00 00 9c 01 e4"), encode(
                Native950PlayerMasks.builder().hits(Collections.<Hit>emptyList(),
                        Arrays.asList(Hitbar.sized(0, 0, 0, 100, 100, 0, 100, 100)))));
        assertArrayEquals(hex("40 80 ff 00 ff ff"), encode(
                Native950PlayerMasks.builder().hits(Collections.<Hit>emptyList(),
                        Arrays.asList(Hitbar.remove(0)))));
    }

    @Test public void hitbarSizeBoundaryAndBothQuantityBytesAreDerived() {
        assertArrayEquals(hex("40 80 ff 00 01 00 00 80 80 80 81 ff"), encode(
                Native950PlayerMasks.builder().hits(Collections.<Hit>emptyList(),
                        Arrays.asList(Hitbar.sized(0, 1, 0, 0, 0, 127, 1, 127)))));
        assertArrayEquals(hex("40 80 fe 00 01 00 00 80 7f 80 80 01 01 00 00 80 ff ff 7f 00"),
                encode(Native950PlayerMasks.builder().hits(Collections.<Hit>emptyList(), Arrays.asList(
                        Hitbar.sized(0, 1, 0, 0, 0, 126, 0, 0),
                        Hitbar.sized(1, 1, 0, 0, 0, 32766, 255, 128)))));
    }

    @Test public void faceEntityUsesLittleEndianMediumWithKindInLastByte() {
        assertArrayEquals(hex("80 05 00 02"), encode(
                Native950PlayerMasks.builder().faceEntity(FaceEntity.player(5))));
        assertArrayEquals(hex("80 d2 04 01"), encode(
                Native950PlayerMasks.builder().faceEntity(FaceEntity.npc(1234))));
        assertArrayEquals(hex("80 ff ff ff"), encode(
                Native950PlayerMasks.builder().faceEntity(FaceEntity.none())));
    }

    @Test public void colourOverlayUses128MinusSaturationAndStrengthAndMixedShorts() {
        assertArrayEquals(hex("10 80 20 00 80 40 81 80 00 00 0a"), encode(
                Native950PlayerMasks.builder().colourOverlay(ColourOverlay.of(0, 0, 64, 255, 0, 10))));
        assertArrayEquals(hex("10 80 20 11 7b 63 b8 b4 12 45 67"), encode(
                Native950PlayerMasks.builder().colourOverlay(
                        ColourOverlay.of(17, 5, 99, 200, 0x1234, 0x4567))));
    }

    @Test public void spotanimAdditionsUseThe950TransformScript() {
        assertArrayEquals(hex("10 80 04 04 00 01 80 52 04 00 00 64 00 00 ff fb 1f"), encode(
                Native950PlayerMasks.builder().spotanims(SpotanimList.of(new int[0],
                        Arrays.asList(Spotanim.of(0, 1234, 0, 100, 0, 0, 0))))));
        assertArrayEquals(hex("10 80 04 04 02 12 34 56 78 02 5d b4 12 45 a3 34 12 85 dc 9a 71"
                + " 80 7f ff 00 00 00 00 00 00 f8 3f"), encode(
                Native950PlayerMasks.builder().spotanims(SpotanimList.of(new int[] {0x1234, 0x5678},
                        Arrays.asList(Spotanim.of(0x23, 0x1234, 0x2345, 0x1234, true, 5, true,
                                -0x123, 0x234, true), Spotanim.of(0, -1, 0, 0, 0, -1023, 1024))))));
    }

    @Test public void spotanimRemovalsUsePlainCountAndSignedBigEndianShorts() {
        assertArrayEquals(hex("10 80 04 04 01 ff ff 00"), encode(
                Native950PlayerMasks.builder().spotanims(
                        SpotanimList.of(new int[] {-1}, Collections.<Spotanim>emptyList()))));
    }

    @Test public void clearAllMustBeTheOnlySpotanimRemoval() {
        // At 0x14012C9FE the client exits the removal loop on -1, even if count says more.
        rejects(() -> SpotanimList.of(new int[] {-1, 1234}, Collections.<Spotanim>emptyList()));
        rejects(() -> SpotanimList.of(new int[] {1234, -1}, Collections.<Spotanim>emptyList()));
        assertArrayEquals(hex("10 80 04 04 01 ff ff 01 80 52 04 00 00 64 00 00 ff fb 1f"), encode(
                Native950PlayerMasks.builder().spotanims(SpotanimList.of(new int[] {-1},
                        Arrays.asList(Spotanim.of(0, 1234, 0, 100, 0, 0, 0))))));
    }

    @Test public void appearanceUsesTheLengthPlus128AndBytesBiasedByEighty() {
        // Bit 0x20 on 950 (947: 0x4), and the body is pre-biased b ^ 0x80 because the client's
        // copy adds 0x80 on receive (transform script 0x02 at .rdata 0x140B5FEC9; 947 had 0x00).
        // The length byte keeps its +128. 947 asserted "04 83 01 02 03" - raw bytes under bit 4.
        assertArrayEquals(hex("20 83 81 82 83"),
                encode(Native950PlayerMasks.builder().appearance(new byte[] {1, 2, 3})));
    }

    // ---------------------------------------------------------------- header markers

    @Test public void headerMarkersAreSelectedByTheHighestSetBit() {
        // 950 markers: 0x10 pulls byte 2, 0x8000 byte 3, 0x40000 byte 4.
        // A bit in byte 1 alone emits one byte and no marker.
        assertEquals(0x02, encode(Native950PlayerMasks.builder().faceAngle(0))[0] & 0xFF);
        // forceTalkLocal is 0x400 - byte 2 - so the first marker appears and nothing above it.
        assertArrayEquals(hex("10 04"), Arrays.copyOf(
                encode(Native950PlayerMasks.builder().forceTalkLocal("")), 2));
        // forceTalk is 0x400000 - byte 3 - which drags the byte-2 marker in with it.
        assertArrayEquals(hex("10 80 40"), Arrays.copyOf(
                encode(Native950PlayerMasks.builder().forceTalk("", 0)), 3));
        assertArrayEquals(hex("10 80 04 04"), Arrays.copyOf(encode(
                Native950PlayerMasks.builder().spotanims(SpotanimList.of(new int[] {-1},
                        Collections.<Spotanim>emptyList()))), 4));
    }

    // ---------------------------------------------------------------- ordering

    @Test public void combinedBlocksFollowTheClientConsumptionOrderNotBitOrder() {
        // 950 order puts forceTalk before appearance and faceAngle last, which is neither bit
        // order nor 947's order. Mask 0x2 | 0x20 | 0x400000, so the header is 32 80 40.
        byte[] block = encode(Native950PlayerMasks.builder()
                .faceAngle(8192)
                .forceTalk("Hi", 1)
                .appearance(new byte[] {1, 2, 3}));
        assertArrayEquals(hex("32 80 40 48 69 00 01 83 81 82 83 20 00"), block);
    }

    @Test public void forceTalkPrecedesAppearanceAndForceTalkLocalPrecedesFaceAngle() {
        // The four previously available blocks retain their consumption order:
        //   forceTalk, appearance, forceTalkLocal, faceAngle.
        // Mask 0x2 | 0x20 | 0x400 | 0x400000 -> header 32 84 40.
        assertArrayEquals(hex("32 84 40 48 69 00 00 81 87 59 6f 00 20 00"),
                encode(Native950PlayerMasks.builder()
                        .faceAngle(8192)
                        .forceTalk("Hi", 0)
                        .appearance(new byte[] {7})
                        .forceTalkLocal("Yo")));
    }

    @Test public void allTenBlocksUseNativeConsumptionOrderAndFourByteHeader() {
        assertArrayEquals(hex("fb 84 64 04"
                + " 01 ff ff 00"
                + " 03 57 7f ff 7f ff 7f ff 00"
                + " 05 00 02"
                + " 81 80 84 19 00 00"
                + " 48 69 00 01"
                + " 81 81"
                + " 7f 00 fe 82 00 80 00 00 00 0a 00 20"
                + " 00 80 40 81 80 00 00 0a"
                + " 59 6f 00"
                + " 20 00"), encode(Native950PlayerMasks.builder()
                        .faceAngle(8192).forceTalkLocal("Yo")
                        .colourOverlay(ColourOverlay.of(0, 0, 64, 255, 0, 10))
                        .forceMovement(ForceMovement.of(1, 0, 2, 2, 0, 0, 0, 10, 8192))
                        .appearance(new byte[] {1}).forceTalk("Hi", 1).hit(Hit.of(132, 25, 0))
                        .faceEntity(FaceEntity.player(5)).animation(855, 0)
                        .spotanims(SpotanimList.of(new int[] {-1}, Collections.<Spotanim>emptyList()))));
    }

    // ---------------------------------------------------------------- framing

    @Test public void skippedPrefixReproducesTheExistingAppearanceOnlyTail() {
        byte[] appearance = {1, 2, 3, 4};
        byte[] block = Native950PlayerMasks.encodeWithSkippedPrefix(
                Native950PlayerMasks.builder().appearance(appearance).build());
        assertArrayEquals(hex("00 00 20 84 81 82 83 84"), block);
        byte[] walk = Native950Packets.singlePlayerWalkStep(1, 0, appearance).payload();
        assertArrayEquals(block, Arrays.copyOfRange(walk, walk.length - block.length, walk.length));
    }

    @Test public void appearanceOnlyFrameMatchesThePlayerInfoDocumentedFullFrame() {
        // The existing appearance frame remains byte-identical after the additional blocks:
        // PLAYER_INFO opcode 36, unsigned-short length, the c0 7f f4 movement prefix, the two
        // skipped bytes, mask 0x20, length+128, and the body biased b ^ 0x80.
        assertArrayEquals(hex("24 000a c0 7f f4 00 00 20 83 81 82 83"),
                appearanceFrame().frame(() -> 0));
    }

    @Test public void framingSurvivesTheRealIsaacPairOnAnEmbeddedChannel() {
        int[] outgoingSeeds = {51, 52, 53, 54};
        Native950GameTransport transport = new Native950GameTransport(
                new Native950Isaac(new int[] {1, 2, 3, 4}), new Native950Isaac(outgoingSeeds),
                Thread.currentThread(), 8, 4);
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeOutbound(appearanceFrame());
            byte[] expected = appearanceFrame().frame(new Native950Isaac(outgoingSeeds));
            assertArrayEquals(expected, readOutput(channel));
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static Native950Packets.Packet appearanceFrame() {
        byte[] movement = hex("c0 7f f4");
        byte[] block = Native950PlayerMasks.encodeWithSkippedPrefix(
                Native950PlayerMasks.builder().appearance(new byte[] {1, 2, 3}).build());
        byte[] body = new byte[movement.length + block.length];
        System.arraycopy(movement, 0, body, 0, movement.length);
        System.arraycopy(block, 0, body, movement.length, block.length);
        return Native950Packets.packet(ServerPacket.PLAYER_INFO, body);
    }

    // ---------------------------------------------------------------- rejects

    @Test public void rejectsOutOfRangeMaskValues() {
        rejects(() -> Native950PlayerMasks.builder().faceAngle(-1));
        rejects(() -> Native950PlayerMasks.builder().faceAngle(16384));
        rejects(() -> Native950PlayerMasks.builder().animation(855, 256));
        rejects(() -> Native950PlayerMasks.builder().animation(855, -1));
        rejects(() -> Animation.of(new int[] {-1, -1, -1}, 0));
        rejects(() -> Animation.of(new int[] {-2, -1, -1, -1}, 0));
        rejects(() -> ForceMovement.of(128, 0, 0, 0, 0, 0, 0, 0, 0));
        rejects(() -> ForceMovement.of(-129, 0, 0, 0, 0, 0, 0, 0, 0));
        rejects(() -> ForceMovement.of(0, 0, 0, 0, 0, 0, 65536, 0, 0));
        rejects(() -> ForceMovement.of(0, 0, 0, 0, 0, 0, 0, 0, 16384));
        rejects(() -> ColourOverlay.of(64, 0, 0, 0, 0, 0));
        rejects(() -> ColourOverlay.of(0, 8, 0, 0, 0, 0));
        rejects(() -> ColourOverlay.of(0, 0, 128, 0, 0, 0));
        rejects(() -> ColourOverlay.of(0, 0, 0, 256, 0, 0));
        rejects(() -> ColourOverlay.of(0, 0, 0, 0, 0, 65536));
        rejects(() -> FaceEntity.npc(65536));
        rejects(() -> FaceEntity.player(-1));
        rejects(() -> Hit.of(0x7FFE, 0, 0));
        rejects(() -> Hit.of(0, 0x8000, 0));
        rejects(() -> Hit.untyped(256, 0));
        rejects(() -> Hitbar.sized(0, 0, 0, 100, 50, -1, 0, 0));
        rejects(() -> Hitbar.sized(0, 1, 0, 0, 0, -1, 1, 1));
        rejects(() -> Hitbar.sized(0, 1, 0, 0, 0, 32767, 0, 0));
        rejects(() -> Hitbar.update(0, 0x7FFF, 0, 0, 0));
        rejects(() -> Spotanim.of(0, 1234, 0, 100, 8, 0, 0));
        rejects(() -> Spotanim.of(0, 1234, 0, 100, 0, -1024, 0));
        rejects(() -> Spotanim.of(0, 1234, 0, 100, 0, 0, 1025));
        rejects(() -> Spotanim.of(256, 1234, 0, 100, 0, 0, 0));
        rejects(() -> SpotanimList.of(new int[0], Collections.<Spotanim>emptyList()));
        rejects(() -> Native950PlayerMasks.builder().appearance(new byte[0]));
        rejects(() -> Native950PlayerMasks.builder().appearance(new byte[256]));
        rejects(() -> Native950PlayerMasks.builder().forceTalk("Hi", 256));
        rejects(() -> Native950PlayerMasks.builder().forceTalk("Hi\0there", 0));
        rejects(() -> Native950PlayerMasks.builder()
                .hits(Collections.<Hit>emptyList(), Collections.<Hitbar>emptyList()));
        rejects(() -> Native950PlayerMasks.builder().build());
    }

    @Test public void refusesEveryCandidateMaskAndNamesTheEvidenceGap() {
        unsupported(() -> Native950PlayerMasks.headIcons(new byte[] {1, 2, 3}), "0x1000");
        unsupported(() -> Native950PlayerMasks.builder().headIcons(new byte[] {1}), "0x1000");
        unsupported(() -> Native950PlayerMasks.candidateMask(0x80000, new byte[] {1}), "0x80000");
        unsupported(() -> Native950PlayerMasks.candidateMask(0x100, new byte[] {1}), "0x100");
        unsupported(() -> Native950PlayerMasks.candidateMask(0x200000, new byte[] {1}), "0x200000");
        unsupported(() -> Native950PlayerMasks.candidateMask(0x400000, new byte[] {1}), "0x400000");
        unsupported(() -> Native950PlayerMasks.candidateMask(0x1000000, new byte[] {1}), "0x1000000");
        // The confirmed no-ops and the header markers are refused for their own reasons.
        unsupported(() -> Native950PlayerMasks.candidateMask(0x2000000, new byte[] {1}), "0x2000000");
        unsupported(() -> Native950PlayerMasks.candidateMask(0x4000, new byte[] {1}), "0x4000");
    }

    // ---------------------------------------------------------------- helpers

    private static byte[] encode(Native950PlayerMasks.Builder builder) {
        return Native950PlayerMasks.encode(builder.build());
    }

    private static byte[] readOutput(EmbeddedChannel channel) {
        ByteBuf output = channel.readOutbound();
        assertNotNull(output);
        try {
            byte[] bytes = new byte[output.readableBytes()];
            output.readBytes(bytes);
            return bytes;
        } finally {
            output.release();
        }
    }

    private static void rejects(Runnable action) {
        try { action.run(); fail("Invalid mask value accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void unsupported(Runnable action, String maskText) {
        try {
            action.run();
            fail("Candidate mask " + maskText + " accepted");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains(maskText));
            assertTrue(expected.getMessage(), expected.getMessage().contains("player-masks-950-derived.md"));
        }
    }

    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}
