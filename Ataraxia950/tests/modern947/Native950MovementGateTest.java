package com.rs.game.player.client;

import com.rs.cores.CoresManager;
import com.rs.cores.Native950TickScheduler;
import com.rs.game.ForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.RouteEvent;
import com.rs.network.modern.Native950GameTransport;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real 950 walk bytes reach the session gate before they can replace an owned route. */
public final class Native950MovementGateTest {
    @Test public void lockedGroundClickIsConsumedWithoutResettingTheExistingRoute() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Player player = fixture.player;
            RouteEvent route = pendingRoute(player);
            Object[] step = {4, 3201, 3200, false};
            player.getWalkSteps().add(step);
            player.lock(100);

            fixture.groundClick(3202, 3200);

            assertEquals(1L, fixture.rejectedWalks());
            assertSame(route, player.getRouteEvent());
            assertEquals(1, player.getWalkSteps().size());
            assertSame(step, player.getWalkSteps().peek());
            assertEquals(3200, player.getX());
            assertEquals(3200, player.getY());
            assertNull(player.getNextWorldTile());
        }
    }

    @Test public void activeForceRejectsClicksAfterItsMaskWasResetAndStillReachesItsEndpoint() throws Exception {
        Field schedulerField = CoresManager.class.getDeclaredField("native947Scheduler");
        schedulerField.setAccessible(true);
        Object previousScheduler = schedulerField.get(null);
        Native950TickScheduler wheel = new Native950TickScheduler();
        schedulerField.set(null, wheel);
        try (Fixture fixture = new Fixture()) {
            Player player = fixture.player;
            RouteEvent route = pendingRoute(player);
            player.setNextForceMovement(new ForceMovement(new WorldTile(3202, 3200, 0), 2, ForceMovement.EAST));
            assertFalse("The force gate must work independently of wall-clock locks", player.isLocked());
            assertTrue(player.isNative950ForceMovementActive());
            assertNotNull(player.getNextNative950ForceMovement());
            player.resetMasks();
            assertNull("The update block is sent only once", player.getNextNative950ForceMovement());
            assertTrue("Resetting a sent mask must not cancel pending movement", player.isNative950ForceMovementActive());

            fixture.groundClick(3200, 3202);

            assertEquals(1L, fixture.rejectedWalks());
            assertSame(route, player.getRouteEvent());
            assertTrue(player.getWalkSteps().isEmpty());
            assertTrue(player.isNative950ForceMovementActive());
            wheel.tick();
            assertNull("The second-tick endpoint must not arrive early", player.getNextWorldTile());
            assertTrue(player.isNative950ForceMovementActive());
            wheel.tick();
            WorldTile endpoint = player.getNextWorldTile();
            assertNotNull("A refused click must not cancel scheduled arrival", endpoint);
            assertEquals(3202, endpoint.getX());
            assertEquals(3200, endpoint.getY());
            assertEquals(0, endpoint.getPlane());
            assertEquals(0, wheel.pendingCount());
        } finally {
            schedulerField.set(null, previousScheduler);
        }
    }

    @Test public void asynchronousLogoutFenceRejectsLaterActionsInTheSameInputBatch() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.player.lock(100);
            // Model logout becoming inactive between two queued actions while the socket
            // remains open until its pending output completes on another thread.
            java.util.concurrent.ArrayBlockingQueue<com.rs.network.protocol.modern950.Native950Actions.Action> queue =
                new java.util.concurrent.ArrayBlockingQueue<com.rs.network.protocol.modern950.Native950Actions.Action>(8) {
                    private int polls;
                    @Override public com.rs.network.protocol.modern950.Native950Actions.Action poll() {
                        if (++polls == 2) fixture.player.setActive(false);
                        return super.poll();
                    }
                };
            Field actions = Native950GameTransport.class.getDeclaredField("actions");
            actions.setAccessible(true); actions.set(fixture.transport,queue);
            for (int i=0;i<2;i++) fixture.channel.writeInbound(Unpooled.wrappedBuffer(
                    new byte[]{88,12,(byte)128,(byte)128,2,12}));
            assertEquals(2,fixture.transport.pendingActions());
            fixture.session.tickInput();
            assertTrue("Socket may still be waiting to flush logout",fixture.channel.isActive());
            assertFalse(fixture.player.isActive());
            assertEquals("Only the first action reaches the route handler",1L,fixture.rejectedWalks());
            assertEquals(0,fixture.transport.pendingActions());
        }
    }

    private static RouteEvent pendingRoute(Player player) {
        RouteEvent route = new RouteEvent(new WorldTile(3201, 3200, 0),
                () -> { throw new AssertionError("Input gating must not dispatch the existing route"); });
        player.setRouteEvent(route);
        return route;
    }

    private static final class Fixture implements AutoCloseable {
        final Native950GameTransport transport = new Native950GameTransport(
                () -> 0, () -> 0, Thread.currentThread(), 8, 8);
        final EmbeddedChannel channel = new EmbeddedChannel(transport);
        final Player player = Player.createNative950("movement-gate", new WorldTile(3200, 3200, 0), channel);
        final Native950Session session;

        Fixture() throws Exception {
            player.setActive(true);
            session = new Native950Session(player, channel, transport,
                    new Native950World.SceneConfig(3200, 3200, 0, 1, 5, 0, 0, 0),
                    null, null, null, null);
            // Avoid the unrelated login bootstrap/cache; exercise the production input phase.
            Field ready = Native950Session.class.getDeclaredField("ready");
            ready.setAccessible(true);
            ready.setBoolean(session, true);
        }
        void groundClick(int x, int y) {
            // 950 ground-click opcode 88: y BE, modifier+128, x LE low byte+128.
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {88, (byte)(y >>> 8), (byte)y,
                    (byte)128, (byte)(x + 128), (byte)(x >>> 8)}));
            assertEquals(1, transport.pendingWalkRequests());
            session.tickInput();
            assertEquals("The refused input must still be consumed", 0, transport.pendingActions());
        }
        long rejectedWalks() throws Exception {
            Field rejected = Native950Session.class.getDeclaredField("rejectedWalks");
            rejected.setAccessible(true);
            return rejected.getLong(session);
        }
        @Override public void close() {
            player.setNextForceMovement(null);
            channel.finishAndReleaseAll();
        }
    }
}
