package modern947;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.client.ClientProfile;
import com.rs.game.player.client.Native950Bootstrap;
import com.rs.game.player.client.Native950PacketDispatcher;
import com.rs.game.player.client.Native950Session;
import com.rs.game.player.client.Native950World;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public final class Native950WorldLifecycleTest {
    @Test
    public void nativeInitializerCreatesRealMovementStateWithoutLegacyLoginOrCache() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("local-test", new WorldTile(3222, 3222, 0), channel);
            assertEquals(Player.class, player.getClass());
            assertEquals(ClientProfile.NATIVE_950, player.getClientProfile());
            assertEquals("local-test", player.getUsername());
            assertEquals(3222, player.getX());
            assertEquals(1, player.getSize());
            assertEquals(0, player.getMapSize()); // 104-tile scene; native wire ID remains 5.
            assertNotNull(player.getInventory());
            assertNotNull(player.getEquipment());
            assertNotNull(player.getSkills());
            assertNotNull(player.getWalkSteps());
            assertFalse(player.isRunning());
            assertFalse(player.isActive());
            // P1: a 947 player gets the Native950PacketDispatcher facade instead of the 910 serializer.
            assertTrue(player.getPackets() instanceof com.rs.game.player.client.Native950PacketDispatcher);
            assertSame(player.getPackets(), player.getPackets());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    public void disconnectBeforeAdmissionReleasesSlotWithoutConsumingLiveCiphers() throws Exception {
        Native950World world = Native950World.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        AtomicInteger inputWords = new AtomicInteger(), outputWords = new AtomicInteger();
        channel.close();
        CompletableFuture<Native950Session> future = world.attach(channel, "disconnected",
                inputWords::incrementAndGet, outputWords::incrementAndGet, new byte[] {0},
                new Native950World.SceneConfig(3222, 3222, 0, 1, 7, 0, 0, 0), Collections.emptyList());
        try {
            future.get(3, TimeUnit.SECONDS);
            fail("A disconnected channel must not enter the world");
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause().getMessage().contains("disconnected"));
        } finally { channel.finishAndReleaseAll(); }
        assertEquals(0, inputWords.get());
        assertEquals(0, outputWords.get());
        // P6: with N slots, canReserve() is true almost always, so it can no longer be the
        // leak check on its own. The failed attachment must leave no reservation standing.
        assertEquals("a failed attachment must not leak its slot", 0, world.reservedSlots());
        assertTrue(world.canReserve());
        assertNull(world.snapshot().get(3, TimeUnit.SECONDS));
    }

    /**
     * P4 admission order: channel -> flat cache -> (P8 bootstrap, once) -> player.
     * Without a cache the attach fails closed before the bootstrap can run, so the
     * JVM-wide report stays null and the slot is released.
     */
    @Test
    public void admissionWithoutTheFlatCacheFailsClosedBeforeTheBootstrap() throws Exception {
        Native950World world = Native950World.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        CompletableFuture<Native950Session> future = world.attach(channel, "nocache",
                () -> 0, () -> 0, new byte[] {0},
                new Native950World.SceneConfig(3222, 3222, 0, 1, 7, 0, 0, 0), Collections.emptyList());
        try {
            future.get(3, TimeUnit.SECONDS);
            fail("No cache must not enter the world");
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause().getMessage().contains("read-only cache"));
        } finally { channel.finishAndReleaseAll(); }
        assertNull("the bootstrap must not run without a cache", Native950Bootstrap.lastReport());
        assertEquals("a failed attachment must not leak its slot", 0, world.reservedSlots());
        assertTrue(world.canReserve());
        assertNull(world.snapshot().get(3, TimeUnit.SECONDS));
    }

    /** P4: every native varp write funnels through the facade's sendConfig (counted, never a throw). */
    @Test
    public void varpSinkFunnelsVarsManagerWritesThroughTheFacade() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("varps", new WorldTile(3222, 3222, 0), channel);
            player.getVarsManager().sendVar(463, 1);
            assertEquals("cached and counted until the sink exists", 1L, player.getVarsManager().nativeUnsentVarps());
            Native950World.installVarpSink(player);
            Native950PacketDispatcher.Counters counters = ((Native950PacketDispatcher) player.getPackets()).counters();
            player.getVarsManager().forceSendVar(463, 1);
            player.getVarBitManager().forceSendVar(1056, 0);
            assertEquals(1L, player.getVarsManager().nativeUnsentVarps());
            // No transport is attached to the EmbeddedChannel: the facade counts drops, not sends.
            assertEquals(2L, counters.dropped("sendConfig"));
            assertEquals(0L, counters.totalSent());
            assertEquals(0L, counters.totalStrictHits());
        } finally { channel.finishAndReleaseAll(); }
    }

    /**
     * P6: the scene carries whichever slot the world reserved, so SceneConfig accepts every
     * index PLAYER_INFO can address and refuses everything outside 1..2047. The world's own
     * capacity is the installed frame encoder's, not a constant here.
     */
    @Test
    public void sceneConfigAcceptsEveryPlayerInfoSlotAndRefusesTheRest() {
        for (int index : new int[] {1, 2, 47, Native950World.MAX_PLAYER_INDEX})
            assertEquals(index, new Native950World.SceneConfig(3222, 3222, 0, index, 7, 0, 0, 0).playerIndex);
        for (int index : new int[] {0, -1, Native950World.MAX_PLAYER_INDEX + 1}) {
            try {
                new Native950World.SceneConfig(3222, 3222, 0, index, 7, 0, 0, 0);
                fail("Player slot " + index + " is outside the verified PLAYER_INFO range");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("1.." + Native950World.MAX_PLAYER_INDEX));
            }
        }
    }

    /**
     * P6 admission: {@code reserve} hands out the index {@code GameLoginResponse} must carry
     * before any Player exists, never hands the same one out twice, and {@code release} returns
     * it. A leaked reservation is permanent capacity loss, so this pins both directions.
     */
    @Test
    public void reservationsAreUniqueAndReleasedSlotsAreHandedOutAgain() {
        Native950World world = Native950World.getInstance();
        int before = world.reservedSlots();
        int first = world.reserve("reserve-a");
        int second = world.reserve("reserve-b");
        try {
            assertTrue("the world must have room for this test", first > 0 && second > 0);
            assertNotEquals(first, second);
            assertEquals(before + 2, world.reservedSlots());
            // Nothing is in the world yet: a reservation is not a character.
            assertNull(world.snapshot(first).get(3, TimeUnit.SECONDS));
            assertNull(world.snapshot(second).get(3, TimeUnit.SECONDS));
        } catch (Exception failure) {
            throw new AssertionError(failure);
        } finally {
            world.release(first);
            world.release(second);
        }
        assertEquals(before, world.reservedSlots());
        int reused = world.reserve("reserve-c");
        try {
            assertEquals("a released slot must be handed out again, lowest index first", first, reused);
        } finally {
            world.release(reused);
        }
        assertEquals(before, world.reservedSlots());
    }

    /**
     * P6: one account holds exactly one slot. Two sessions for one username would each load the
     * same profile by name and checkpoint it independently, so two whole-profile writers would
     * interleave over one save file, and {@code World.playerMap} holds one entry per username
     * which the first departure would remove out from under the survivor. Before P6 the single
     * 'occupied' flag made a second login impossible; nothing else replaced it.
     */
    @Test
    public void oneAccountCannotReserveTwoSlots() {
        Native950World world = Native950World.getInstance();
        int before = world.reservedSlots();
        assertFalse(world.isOnline("dupe-login"));
        int first = world.reserve("dupe-login");
        assertTrue("the world must have room for this test", first > 0);
        try {
            assertTrue(world.isOnline("dupe-login"));
            assertEquals("a second reservation for the same account must be refused",
                    0, world.reserve("dupe-login"));
            // The save store folds case and separators, so these are the same profile file.
            assertTrue(world.isOnline("DUPE-LOGIN"));
            assertEquals(0, world.reserve("DUPE-LOGIN"));
            assertEquals("the refusal must leave the first slot untouched",
                    before + 1, world.reservedSlots());
            // A different account is still admitted.
            int other = world.reserve("dupe-other");
            assertTrue(other > 0);
            assertNotEquals(first, other);
            world.release(other, "dupe-other");
        } finally {
            world.release(first, "dupe-login");
        }
        assertFalse(world.isOnline("dupe-login"));
        assertEquals(before, world.reservedSlots());
        // Once released, the same account logs in again.
        int again = world.reserve("dupe-login");
        assertTrue(again > 0);
        world.release(again, "dupe-login");
        assertEquals(before, world.reservedSlots());
    }

    /**
     * P6: a login failure branch can fire long after the world already freed the slot, and by
     * then a different connection may hold a fresh reservation at that same index. The
     * username-checked release is what {@code Ataraxia947Handoff} calls, so a stale branch
     * cannot hand a concurrent login's reservation away.
     */
    @Test
    public void aStaleReleaseCannotTakeAnotherLoginsReservation() {
        Native950World world = Native950World.getInstance();
        int before = world.reservedSlots();
        int index = world.reserve("stale-owner");
        assertTrue("the world must have room for this test", index > 0);
        // The world frees the slot itself (a failed attachment), and a second login takes it.
        world.release(index, "stale-owner");
        assertEquals(before, world.reservedSlots());
        int reused = world.reserve("next-owner");
        try {
            assertEquals("the freed index is handed out again", index, reused);
            // The first login's late failure branch now runs. It must not touch the slot.
            world.release(reused, "stale-owner");
            assertEquals("a stale release must not free another login's slot",
                    before + 1, world.reservedSlots());
            // The rightful owner still releases it.
            world.release(reused, "next-owner");
            assertEquals(before, world.reservedSlots());
        } finally {
            world.release(reused, "next-owner");
        }
        assertEquals(before, world.reservedSlots());
    }

    /** The world admits at most what the installed encoder can describe in one frame. */
    @Test
    public void worldCapacityFollowsTheInstalledFrameEncoder() {
        Native950World world = Native950World.getInstance();
        assertEquals(world.capacity(), Math.min(world.frames().playerCapacity(), Native950World.MAX_PLAYER_INDEX));
        assertTrue("the generalised encoder must admit more than one character", world.capacity() > 1);
        assertTrue(world.canReserve());
    }
}
