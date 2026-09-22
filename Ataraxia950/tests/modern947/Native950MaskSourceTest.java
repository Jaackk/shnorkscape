package modern947;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.hitbar.HitBar;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950EntityFrames;
import com.rs.game.player.client.Native950EntityMasks;
import com.rs.game.player.client.Native950Frames;
import com.rs.game.player.client.Native950World;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * M4: the CONFIRMED entity masks are fed from real 910 state.
 *
 * <p>P6 shipped mask-capable encoders and no mask source at all, so a player could only ever
 * carry the appearance block. These tests drive the real objects - an Ataraxia {@code Player}
 * and an Ataraxia {@code NPC} - through {@code Native950EntityFrames} and
 * {@code Native950EntityMasks} and compare the bytes that come out against the literals in
 * {@code protocol-analysis/player-masks-950-derived.md} and the corresponding NPC derivation.
 *
 * <p>Cache free: {@code Player.createNative950} hydrates packet-free, {@code NPC.createNative950}
 * builds an NPC without reading definitions, no appearance body is ever generated (so the
 * appearance block never appears in these frames), and every assertion is about the wire.
 */
public final class Native950MaskSourceTest {

    private static final int VIEWER_INDEX = 1;
    private static final int VIEWER_X = 3200, VIEWER_Y = 3200, VIEWER_PLANE = 0;
    private static final int NPC_BITS = 7;

    /** PLAYER_INFO.md: the local player alone, nothing pending. */
    private static final String IDLE_FRAME = "00 7f f4";
    /** The same prefix with the pending-mask bit set. */
    private static final String MASK_ONLY_PREFIX = "c0 7f f4";

    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final Player viewer = Player.createNative950("mask-source-test",
            new WorldTile(VIEWER_X, VIEWER_Y, VIEWER_PLANE), channel);
    private final Native950EntityFrames frames = new Native950EntityFrames();

    /** A hit bar with no cache behind it; the point is only that one is queued. */
    private static final HitBar BAR = new HitBar() {
        @Override public int getType() { return 0; }
        @Override public int getPercentage() { return 100; }
    };

    @Before public void admit() {
        viewer.setIndex(VIEWER_INDEX);
        // The world's phase 1b runs Entity.processMovement for every character before any frame
        // is built; without it nextRunDirection is still the constructor's 0 and the 910
        // face-angle condition (no walk, no run, no force movement, no face entity) could not
        // be evaluated the way the live tick evaluates it.
        viewer.processMovement();
        frames.admit(viewer, new Native950World.SceneConfig(VIEWER_X, VIEWER_Y, VIEWER_PLANE,
                VIEWER_INDEX, NPC_BITS, 0, 0, 0), Collections.singletonList(viewer));
    }

    @After public void release() { channel.finishAndReleaseAll(); }

    // ---------------------------------------------------------------- player masks

    @Test public void anAnimationOnThePlayerProducesTheDocumentedAnimationFrame() {
        // Sequence 855 in slot 0, the other three empty, speed 0.
        // Animation(id1,id2,id3,id4,speed) fills the engine slots individually.
        // 950 bit 0x8: BE smart sequence slots, then plain delay.
        viewer.setNextAnimation(new Animation(855, -1, -1, -1, 0));
        assertArrayEquals(hex("c0 7f f4 00 00 08 03 57 7f ff 7f ff 7f ff 00"), tick());
    }

    @Test public void theAnimationBlockCarriesTheFourSlotsAndTheSpeedTheEntityHolds() {
        // 910 LocalPlayerUpdate.applyAnimationMask writes the first four ids and the speed
        // byte; this port does the same, so a four-slot animation survives intact.
        viewer.setNextAnimation(new Animation(855, 856, -1, -1, 3));
        assertArrayEquals(hex("c0 7f f4 00 00 08 03 57 03 58 7f ff 7f ff 03"), tick());
    }

    @Test public void aFaceEntityRequestProducesTheConfirmedKindTwoBlock() {
        // Entity.getClientIndex() offsets a player index by 32768; the 950 block names
        // the kind explicitly, so the translation has to strip that offset again.
        EmbeddedChannel other = new EmbeddedChannel();
        try {
            Player target = Player.createNative950("face-target",
                    new WorldTile(VIEWER_X + 1, VIEWER_Y, VIEWER_PLANE), other);
            target.setIndex(5);
            assertEquals(5 + Native950EntityMasks.PLAYER_CLIENT_INDEX_BASE, target.getClientIndex());
            // 950 bit 0x80, little-endian target medium: index 5, kind 2.
            viewer.setNextFaceEntity(target);
            assertArrayEquals(hex("c0 7f f4 00 00 80 05 00 02"), tick());
        } finally {
            other.finishAndReleaseAll();
        }
    }

    @Test public void clearingTheFaceTargetProducesTheConfirmedFfBlock() {
        viewer.setNextFaceEntity(null);
        assertEquals(Native950EntityMasks.CLEAR_FACE_ENTITY, viewer.getNextFaceEntity());
        assertArrayEquals(hex("c0 7f f4 00 00 80 ff ff ff"), tick());
    }

    @Test public void aForceTalkProducesTheConfirmedOverheadBlockWithFlagsZero() {
        // 950 bit 0x400000 (947: 0x10000), byte 3, so the header carries both extension
        // markers - 0x400000 | 0x8000 | 0x10 emits 10 80 40 - then the NUL terminated CP1252 text
        // and the flags byte. The body itself is one of the three the plan states did not change.
        // Flags 0 is overhead only; bit 0 would echo into the chatbox, which is M5's public-chat path.
        viewer.setNextForceTalk(new ForceTalk("Hi"));
        assertArrayEquals(hex(MASK_ONLY_PREFIX + " 00 00 10 80 40 48 69 00 00"), tick());
    }

    @Test public void aTextTheClientCouldNotReadIsDroppedRatherThanTruncated() {
        long refusedBefore = Native950EntityMasks.refusals();
        viewer.setNextForceTalk(new ForceTalk("bad\0text"));
        assertNull("an embedded NUL would truncate the client's strlen reader",
                Native950EntityMasks.playerMasks(viewer));
        assertArrayEquals(hex(IDLE_FRAME), tick());
        assertTrue(Native950EntityMasks.refusals() > refusedBefore);
    }

    @Test public void publicChatEchoesToBothOverheadAndChatbox() {
        assertTrue(com.rs.game.player.client.Native950Social.speak(viewer, "Hi", System.currentTimeMillis()));
        assertArrayEquals(hex(MASK_ONLY_PREFIX + " 00 00 10 80 40 48 69 00 01"), tick());
    }

    @Test public void aFaceRectangleProducesTheFaceAngleBlockFromTheEntitysDirection() {
        // 950 bit 0x2 (947: 0x80): a plain big-endian ushort in 1/16384 turn units. The body is
        // unchanged, so 947's worked example "80 20 00" for angle 8192 becomes "02 20 00" - the
        // mask byte moved, the two angle bytes did not. The expected bytes are literals derived
        // from the evidence, not from the encoder: Entity.updateAngle (Entity.java:1128-1136)
        // is atan2(srcX-dstX, srcY-dstY) * 2607.5945876176133 & 0x3FFF, and for a viewer at
        // (3200,3200) facing a 1x1 tile ten squares due EAST that is atan2(-5120, 0) * k
        // = -4096, i.e. 12288 = 0x3000. Ten squares due NORTH is atan2(0,-5120) * k = 8192,
        // which is the document's own example block byte for byte.
        viewer.setNextFaceWorldTile(new WorldTile(VIEWER_X + 10, VIEWER_Y, VIEWER_PLANE));
        assertEquals("the 910 encoder must agree with the evidence's own angle formula",
                12288, viewer.getDirection() & 0x3FFF);
        assertArrayEquals(hex(MASK_ONLY_PREFIX + " 00 00 02 30 00"), tick());

        viewer.setNextFaceWorldTile(new WorldTile(VIEWER_X, VIEWER_Y + 10, VIEWER_PLANE));
        assertEquals(8192, viewer.getDirection() & 0x3FFF);
        assertArrayEquals(hex(MASK_ONLY_PREFIX + " 00 00 02 20 00"), tick());
    }

    @Test public void aFaceEntityRequestSuppressesTheFaceAngleExactlyAsThe910EncoderDoes() {
        // The 910 condition for the angle block excludes a tick that already carries a
        // face-entity request, because the two would fight over the same model rotation.
        // Assert both source selection and the independently derived 950 wire.
        EmbeddedChannel other = new EmbeddedChannel();
        try {
            Player target = Player.createNative950("face-target-2",
                    new WorldTile(VIEWER_X + 1, VIEWER_Y, VIEWER_PLANE), other);
            target.setIndex(5);

            // (a1) Control, on this same viewer and tick: the face rectangle ALONE does queue the
            // angle block. Without this the suppression assertion below would also pass if the
            // source had simply stopped producing angles altogether.
            viewer.setNextFaceWorldTile(new WorldTile(VIEWER_X + 10, VIEWER_Y, VIEWER_PLANE));
            Native950PlayerMasks.Builder angleOnly = Native950EntityMasks.playerMasks(viewer);
            assertNotNull("a face rectangle alone must open a mask block", angleOnly);
            assertEquals("the rectangle alone queues exactly the face-angle block",
                    Native950PlayerMasks.FACE_ANGLE, angleOnly.build().maskBits());

            // (a2) Add the face-entity request in the same tick. The angle must disappear from
            // the source - not merely be overwritten later - leaving face entity alone.
            viewer.setNextFaceEntity(target);
            Native950PlayerMasks.Builder both = Native950EntityMasks.playerMasks(viewer);
            assertNotNull("the face-entity request must still open a mask block", both);
            int queued = both.build().maskBits();
            assertEquals("face entity must be queued",
                    Native950PlayerMasks.FACE_ENTITY, queued & Native950PlayerMasks.FACE_ENTITY);
            assertEquals("the face angle must be suppressed in the same tick as a face entity",
                    0, queued & Native950PlayerMasks.FACE_ANGLE);
            assertEquals("face entity must be the ONLY block queued",
                    Native950PlayerMasks.FACE_ENTITY, queued);

            // 950 consumes the target as LE u24, with no angle block after it.
            assertArrayEquals(hex("c0 7f f4 00 00 80 05 00 02"), tick());
        } finally {
            other.finishAndReleaseAll();
        }
    }

    @Test public void unverifiedNonMeleeHitsAreRefusedWithoutAnUntypedScaleFallback() {
        long before=Native950EntityMasks.refusals();
        viewer.getNextHits().add(new Hit(50,HitLook.REGULAR_DAMAGE));
        assertArrayEquals(hex(IDLE_FRAME),tick());
        assertTrue(Native950EntityMasks.refusals()>before);
    }

    @Test public void unsupportedLargeDamageIsDroppedNotTruncated() {
        long refusedBefore = Native950EntityMasks.refusals();
        viewer.getNextHits().add(new Hit(2560 * 10, HitLook.REGULAR_DAMAGE));
        assertNull(Native950EntityMasks.playerMasks(viewer));
        assertArrayEquals(hex(IDLE_FRAME), tick());
        assertTrue(Native950EntityMasks.refusals() > refusedBefore);
    }

    @Test public void unverifiedCustomHitBarsAreCountedAndDropped() {
        long refusedBefore = Native950EntityMasks.refusals();
        viewer.getNextHitBars().add(BAR);
        assertNull("a bar alone must not open a mask block", Native950EntityMasks.playerMasks(viewer));
        assertArrayEquals(hex(IDLE_FRAME), tick());
        assertTrue(Native950EntityMasks.refusals() > refusedBefore);
    }

    @Test public void severalMasksInOneTickAreEmittedInTheClientsConsumptionOrder() {
        // The caller never orders blocks: Native950PlayerMasks owns the consumption order, which
        // is not generally the numeric bit order. Animation precedes face entity.
        EmbeddedChannel other = new EmbeddedChannel();
        try {
            Player target = Player.createNative950("face-target-3",
                    new WorldTile(VIEWER_X + 1, VIEWER_Y, VIEWER_PLANE), other);
            target.setIndex(5);
            // 950 combined bit 0x88: animation precedes the LE face target.
            viewer.setNextAnimation(new Animation(855, -1, -1, -1, 0));
            viewer.setNextFaceEntity(target);
            assertArrayEquals(hex("c0 7f f4 00 00 88 03 57 7f ff 7f ff 7f ff 00 05 00 02"), tick());
        } finally {
            other.finishAndReleaseAll();
        }
    }

    // ---------------------------------------------------------------- silence

    @Test public void nothingChangedEmitsNoMaskAtAll() {
        assertNull(Native950EntityMasks.playerMasks(viewer));
        assertNull(Native950EntityMasks.playerSource(viewer));
        assertArrayEquals(hex(IDLE_FRAME), tick());
        // And it stays silent tick after tick.
        assertArrayEquals(hex(IDLE_FRAME), tick());
    }

    @Test public void contentWithNoConfirmedSourceStaysOffTheWire() {
        // Graphics (947 spotanim list 0x4000000) is CONFIRMED as a block but the 910 settings
        // hashes are a different record layout, so nothing is derived from them. The 910
        // encoder would have set its own graphics mask here; this one is silent.
        viewer.setNextGraphics(new com.rs.game.Graphics(2000));
        assertTrue("the 910 encoder would have had a mask to send", viewer.needMasksUpdate());
        assertNull(Native950EntityMasks.playerMasks(viewer));
        assertArrayEquals(hex(IDLE_FRAME), tick());
    }

    // ---------------------------------------------------------------- candidate masks

    @Test public void aCandidateMaskIsStillRefusedByTheEncoder() {
        try {
            Native950PlayerMasks.builder().headIcons(new byte[] {1, 2, 3});
            fail("mask 0x1000 is CANDIDATE and must not be encodable");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("0x1000"));
        }
    }

    @Test public void aCandidateMaskOfferedByASourceNeverReachesTheWire() {
        Native950PlayerInfo.Actor[] world = new Native950PlayerInfo.Actor[Native950PlayerInfo.SLOTS];
        world[VIEWER_INDEX] = Native950PlayerInfo.Actor.builder(VIEWER_INDEX, VIEWER_X, VIEWER_Y, VIEWER_PLANE)
                .masks(new Native950PlayerInfo.MaskSource() {
                    @Override public Native950PlayerMasks.Builder masks() {
                        return Native950PlayerMasks.builder().candidateMask(0x80000, new byte[] {1});
                    }
                }).build();
        com.rs.game.player.client.Native950Viewport viewport =
                new com.rs.game.player.client.Native950Viewport(VIEWER_INDEX);
        viewport.initialScene(world, NPC_BITS, 0, 0, 0);
        try {
            viewport.frame(world);
            fail("a CANDIDATE mask must not be encodable through any path");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("unavailable"));
        }
    }

    // ---------------------------------------------------------------- npc masks

    @Test public void anNpcAnimationReachesTheWireAndCoexistsWithSpeech() {
        long refusedBefore = Native950EntityMasks.refusals();
        NPC npc = NPC.createNative950(494, new WorldTile(VIEWER_X + 2, VIEWER_Y, VIEWER_PLANE), 1);
        npc.setNextAnimation(new Animation(855, -1, -1, -1, 0));
        Native950NpcMasks.Update animation = Native950EntityMasks.npcMasks(npc, false);
        assertNotNull(animation);
        assertTrue(Native950NpcMasks.isSupportedMaskBit(3));
        // 950 0x1401232CB: four BE big-smarts, followed by plain delay at 0x1401234E7.
        assertArrayEquals(hex("00 00 08 03 57 7f ff 7f ff 7f ff 00"),
                Native950NpcMasks.maskBlock(animation));
        assertEquals(refusedBefore, Native950EntityMasks.refusals());

        npc.setNextForceTalk(new ForceTalk("Hi"));
        Native950NpcMasks.Update both = Native950EntityMasks.npcMasks(npc, false);
        assertNotNull(both);
        assertEquals(Native950NpcMasks.MASK_SAY | Native950NpcMasks.MASK_ANIMATION, both.mask());
        // Speech is consumed before animation, independently of setter order or bit order.
        assertArrayEquals(hex("00 00 48 48 69 00 03 57 7f ff 7f ff 7f ff 00"),
                Native950NpcMasks.maskBlock(both));
    }

    @Test public void anNpcFacingAnEntityArrivesAsTheConfirmedFaceCoordinateBlock() {
        // 910 Entity.faceEntity sets a face RECTANGLE. This source uses the derived bit 7
        // coordinate block and sends 2*tile+1 on both axes.
        //
        // 947 asserted: 00 00 08 81 19 01 19. Two things moved. The bit: 0x8 -> 0x80, and 0x8 is
        // animation's bit on 950, so a carried-over constant would select the wrong reader. The x transform: 947 wrote it as ushortle128
        // ([x+128, x>>>8] = 81 19), 950's selector table 0x140B60408 is 00 01, so x is a plain
        // big-endian short (19 01) and only y stays little endian (01 19).
        NPC npc = NPC.createNative950(494, new WorldTile(VIEWER_X + 2, VIEWER_Y, VIEWER_PLANE), 1);
        npc.faceEntity(viewer);
        Native950NpcMasks.Update update = Native950EntityMasks.npcMasks(npc, false);
        assertNotNull(update);
        assertEquals(Native950NpcMasks.MASK_FACE_COORD, update.mask());
        assertArrayEquals(hex("00 00 80 19 01 01 19"), Native950NpcMasks.maskBlock(update));
        // The viewer is at (3200,3200), so both axes carry 2*3200+1 = 0x1901.
        assertEquals(VIEWER_X, (((0x19 << 8) | 0x01) - 1) >> 1);
        assertEquals(VIEWER_Y, ((0x01 | (0x19 << 8)) - 1) >> 1);
    }

    @Test public void anNpcAlreadySteppingSendsNoFaceCoordinateBlock() {
        // The 910 encoder writes the face-tile mask only when no step is being taken
        // (LocalNPCUpdate.java:227 for the bit and :305 for the block), and Entity.resetMasks
        // (Entity.java:1732) clears nextFaceWorldTile only on a tick with no walk step. So a
        // rectangle set before a walk survives the whole walk: without the guard it would be
        // re-published every tick with a stale target and fight the step for the facing.
        NPC npc = NPC.createNative950(494, new WorldTile(VIEWER_X + 2, VIEWER_Y, VIEWER_PLANE), 1);
        npc.setNative950Wander(1);
        World.addNative950Npc(npc);
        try {
            World.updateEntityRegion(npc);
            npc.faceEntity(viewer);
            assertNotNull("a standing NPC does publish its face rectangle",
                    Native950EntityMasks.npcMasks(npc, false));
            assertTrue(npc.addWalkSteps(npc.getX(), npc.getY() + 1, 1, false));
            npc.processMovement();
            assertTrue("the fixture must actually have taken a step", npc.getNextWalkDirection() != -1);
            assertNotNull("910 keeps the rectangle across the walk", npc.getNextFaceWorldTile());
            assertNull("a stepping NPC's facing is decided by the step, not by a stale rectangle",
                    Native950EntityMasks.npcMasks(npc, false));
        } finally {
            World.removeNative950Npc(npc);
        }
    }

    @Test public void anNpcForceTalkProducesTheConfirmedSayBlock() {
        // Bit 6 on 950 (947: bit 0). The reader is the selector-free NUL-terminated string
        // reader 0x140100A90, so the body did not change and only the mask byte moved:
        // 947 asserted 00 00 01 48 69 00. 947's bit 0 is a live but DIFFERENT block on 950, so
        // carrying the old constant would have selected that one instead of forced chat.
        // 0x40 in header byte 0 needs no extension marker, which makes this the tail of the
        // port plan's own Stage 5 acceptance vector 01 9F FF E0 00 00 40 <text> 00.
        NPC npc = NPC.createNative950(494, new WorldTile(VIEWER_X + 2, VIEWER_Y, VIEWER_PLANE), 1);
        npc.setNextForceTalk(new ForceTalk("Hi"));
        Native950NpcMasks.Update update = Native950EntityMasks.npcMasks(npc, false);
        assertNotNull(update);
        assertEquals(Native950NpcMasks.MASK_SAY, update.mask());
        assertArrayEquals(hex("00 00 40 48 69 00"), Native950NpcMasks.maskBlock(update));
    }

    @Test public void anIdleNpcProducesNoMaskAtAll() {
        NPC npc = NPC.createNative950(494, new WorldTile(VIEWER_X + 2, VIEWER_Y, VIEWER_PLANE), 1);
        assertNull(Native950EntityMasks.npcMasks(npc, false));
    }

    @Test public void unverifiedNpcHitsCannotUseTheOldUntypedFallback() {
        long refusedBefore = Native950EntityMasks.refusals();
        NPC npc = NPC.createNative950(494, new WorldTile(VIEWER_X + 2, VIEWER_Y, VIEWER_PLANE), 1);
        npc.getNextHits().add(new Hit(50, HitLook.REGULAR_DAMAGE));
        Native950NpcMasks.Update update = Native950EntityMasks.npcMasks(npc, false);
        assertNull(update);
        assertTrue(Native950EntityMasks.refusals()>refusedBefore);
        assertTrue(Native950NpcMasks.isSupportedMaskBit(5));
        assertFalse(Native950NpcMasks.isSupportedMaskBit(10));
        assertFalse(Native950NpcMasks.isSupportedMaskBit(33));
        try {
            Native950NpcMasks.candidateMask(33);
            fail("The extended hit block still has no complete derivation");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("33"));
            assertTrue(expected.getMessage(), expected.getMessage().contains("not derived"));
        }
    }

    // ---------------------------------------------------------------- helpers

    /** One PLAYER_INFO body, including the engine's mask source and subsequent reset. */
    private byte[] tick() {
        List<Player> characters = Collections.singletonList(viewer);
        frames.beginFrames(characters);
        frames.encode(new Native950Frames.Frame(viewer, channel, characters,
                Collections.<NPC>emptyList(), null, false, NPC_BITS,
                VIEWER_X, VIEWER_Y, VIEWER_PLANE));
        channel.flush();
        Object written = channel.readOutbound();
        assertNotNull("the frame encoder wrote nothing", written);
        assertTrue(written instanceof Native950Packets.Packet);
        Native950Packets.Packet packet = (Native950Packets.Packet) written;
        assertEquals(com.rs.network.protocol.modern950.Native950Protocol.ServerPacket.PLAYER_INFO,
                packet.type());
        // Masks are read during the frame phase and cleared in world phase 3, exactly as the
        // live tick does, so a mask never survives into the next frame.
        viewer.resetMasks();
        return packet.payload();
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] out = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, out, first.length, second.length);
        return out;
    }

    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }

}
