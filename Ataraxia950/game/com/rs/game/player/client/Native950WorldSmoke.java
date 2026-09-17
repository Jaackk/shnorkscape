package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.route.Flags;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import com.rs.utils.Utils;

/** Opt-in real-cache, real-Player probe; never reads or writes account saves. */
public final class Native950WorldSmoke {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: Native950WorldSmoke <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        verifyCacheObstacle();
        Native950World world = Native950World.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        EmbeddedChannel second = new EmbeddedChannel();
        int[] seeds = {19, 47, 3, 2026};
        Native950Isaac incoming = new Native950Isaac(seeds);
        Native950Isaac client = new Native950Isaac(seeds);
        Native950Isaac outgoing = new Native950Isaac(new int[] {69, 97, 53, 2076});
        Native950Isaac expectedOutput = new Native950Isaac(new int[] {69, 97, 53, 2076});
        // Model cipher words consumed by login before the bridge is installed.
        for (int i = 0; i < 3; i++) { incoming.getAsInt(); client.getAsInt(); }
        for (int i = 0; i < 2; i++) { outgoing.getAsInt(); expectedOutput.getAsInt(); }
        Native950World.SceneConfig scene = new Native950World.SceneConfig(3222, 3222, 0, 1, 7, 0, 0, 0);
        try {
            CompletableFuture<Native950Session> attached = world.attach(channel, "native947-smoke", incoming,
                    outgoing, new byte[] {0}, scene, Collections.singletonList(Native950Packets.openTop(1477)));
            await(channel, attached::isDone, 30000);
            attached.get(1, TimeUnit.SECONDS);
            require(world.reservedSlots() == 1, "Active connection must own exactly one world slot");
            require(World.getPlayers().size() == 1 && World.getPlayers().get(1).isNative950(),
                    "A real native-profile Ataraxia Player must be registered");
            byte[] expected = Native950Packets.initialSinglePlayerScene(1, 3222, 3222, 0, 7, 0, 0, 0)
                    .frame(expectedOutput);
            require(Arrays.equals(expected, readBytes(channel)), "Live outgoing cipher position was not preserved");
            require(world.attach(second, "second", () -> 0, () -> 0, new byte[] {0}, scene,
                    Collections.emptyList()).isCompletedExceptionally(), "An occupied player slot must refuse a second admission");

            byte[] walk = {(byte) (3 + client.getAsInt()), (byte) (3222 >>> 8), (byte) (3222 + 128),
                    (byte) (3223 >>> 8), (byte) 3223, (byte) 128};
            channel.writeInbound(Unpooled.wrappedBuffer(walk));
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            Native950Session.Snapshot snapshot;
            do {
                channel.runPendingTasks();
                snapshot = world.snapshot().get(2, TimeUnit.SECONDS);
                if (snapshot.steps > 0) break;
                Thread.sleep(20);
            } while (System.nanoTime() < deadline);
            require(snapshot.steps > 0 && snapshot.x == 3223 && snapshot.y == 3222,
                    "Encrypted walk must move the real Player through Ataraxia routing: " + snapshot);
            System.out.println("PASS: authenticated channel -> Ataraxia Player -> clipped route -> native 947 update: " + snapshot);

            // An arbitrary client modifier cannot turn a remote click into a teleport.
            byte[] farWalk = {(byte) (3 + client.getAsInt()), (byte) (4000 >>> 8), (byte) (4000 + 128),
                    (byte) (4000 >>> 8), (byte) 4000, (byte) (128 - 2)};
            channel.writeInbound(Unpooled.wrappedBuffer(farWalk));
            Thread.sleep(650);
            snapshot = world.snapshot().get(2, TimeUnit.SECONDS);
            require(snapshot.x == 3223 && snapshot.y == 3222 && snapshot.rejectedWalks > 0,
                    "Out-of-range movement must be rejected");

            // Follow an ordinary clipped route far enough north to leave the
            // initial scene's safe area. This runs at the actual 600ms cadence.
            byte[] northWalk = {(byte) (3 + client.getAsInt()), (byte) (3262 >>> 8), (byte) (3262 + 128),
                    (byte) (3223 >>> 8), (byte) 3223, (byte) 128};
            channel.writeInbound(Unpooled.wrappedBuffer(northWalk));
            final long rebuildDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(45);
            do {
                channel.runPendingTasks();
                snapshot = world.snapshot().get(2, TimeUnit.SECONDS);
                require(snapshot != null, "Scene crossing must not disconnect the player");
                if (snapshot.sceneRebuilds > 0) break;
                Thread.sleep(50);
            } while (System.nanoTime() < rebuildDeadline);
            require(snapshot.sceneRebuilds > 0 && snapshot.steps >= 30,
                    "Clipped movement must cross and rebuild the scene: " + snapshot);
            System.out.println("PASS: authoritative movement crossed the initial scene and sent a rebuild: " + snapshot);
            // P4: the P8 bootstrap ran inside admission, Player.processEntity ticked in strict
            // mode with the tick wheel and WorldTasksManager pumping, and nothing STRICT was reached.
            require(Boolean.TRUE.equals(snapshot.bootstrapOk), "The 947 bootstrap must have run and passed during admission: " + snapshot);
            require(snapshot.tickFailures == 0, "processEntity must tick without exceptions: " + snapshot);
            require(snapshot.facadeStrictHits == 0, "No STRICT-tier facade method may be reached: " + snapshot.strictHitsByMethod);
            require(snapshot.schedulerFailed == 0, "No scheduled task may fail on the tick wheel: " + snapshot);
            require(snapshot.ticks >= 30 && snapshot.actionsDrained >= 2, "Ticks and drained actions must be counted: " + snapshot);
            System.out.println("PASS: bootstrap-in-admission, strict processEntity ticks and facade/scheduler counters: sent="
                    + snapshot.sentByMethod + " noops=" + snapshot.noopsByMethod + " deferred=" + snapshot.deferredRefreshes);

            // M2b acceptance (e): the backlog asks for 100 native ticks in strict mode
            // with the per-tick exception counter at 0. Idle the session up to 100 ticks
            // and compare Player.processEntity's own entry counter against the session's
            // tick counter, so "processEntity ran on every native tick" is measured, not
            // inferred from the call site.
            final long idleDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(120);
            while (snapshot.ticks < 100 && System.nanoTime() < idleDeadline) {
                channel.runPendingTasks();
                snapshot = world.snapshot().get(2, TimeUnit.SECONDS);
                require(snapshot != null, "Idle ticking must not disconnect the player");
                Thread.sleep(50);
            }
            require(snapshot.ticks >= 100, "The native world must reach 100 ticks: " + snapshot);
            require(snapshot.processEntityRuns == snapshot.ticks,
                    "Player.processEntity must run on every native tick: runs=" + snapshot.processEntityRuns
                            + " ticks=" + snapshot.ticks);
            require(snapshot.tickFailures == 0, "100 strict native ticks must raise no tick exception: " + snapshot);
            require(snapshot.facadeStrictHits == 0, "100 strict native ticks must reach no STRICT facade method: "
                    + snapshot.strictHitsByMethod);
            require(snapshot.schedulerFailed == 0, "100 strict native ticks must fail no scheduled task: " + snapshot);
            System.out.println("PASS: 100 strict native ticks, processEntity ran on every one: processEntityRuns="
                    + snapshot.processEntityRuns + " ticks=" + snapshot.ticks
                    + " tickFailures=" + snapshot.tickFailures + " strictHits=" + snapshot.facadeStrictHits);

            channel.close();
            await(channel, () -> world.reservedSlots() == 0, 5000);
            require(World.getPlayers().isEmpty(), "Disconnect must remove the authoritative Player");
            require(world.snapshot().get(2, TimeUnit.SECONDS) == null, "Disconnect must clear session state");
            CompletableFuture<Native950Session> readmitted = world.attach(second, "native947-reconnect",
                    () -> 0, () -> 0, new byte[] {0}, scene, Collections.emptyList());
            await(second, readmitted::isDone, 10000);
            readmitted.get(1, TimeUnit.SECONDS);
            require(World.getPlayers().get(1).isNative950(), "Reconnect must reuse player slot 1");
            second.close();
            await(second, () -> world.reservedSlots() == 0, 5000);
            require(World.getPlayers().isEmpty(), "Reconnect cleanup must leave no orphan player");
            System.out.println("PASS: single-player admission, live cipher preservation, range rejection, disconnect and reconnect");
        } finally {
            channel.finishAndReleaseAll();
            second.finishAndReleaseAll();
        }
    }

    /** Run before the world starts so even the diagnostic uses no concurrent world mutation. */
    private static void verifyCacheObstacle() {
        for (int x = 3214; x <= 3230; x++) {
            for (int y = 3214; y <= 3230; y++) {
                World.getRegion(new WorldTile(x, y, 0).getRegionId(), true);
                if ((World.getMask(0, x, y) & (Flags.OBJ | Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK)) != 0)
                    continue;
                for (int dir = 0; dir < 8; dir++) {
                    if (World.checkWalkStep(0, x, y, dir, 1)) continue;
                    int destX = x + Utils.DIRECTION_DELTA_X[dir];
                    int destY = y + Utils.DIRECTION_DELTA_Y[dir];
                    EmbeddedChannel channel = new EmbeddedChannel();
                    try {
                        Player probe = Player.createNative950("collision-probe", new WorldTile(x, y, 0), channel);
                        require(!probe.addWalkSteps(destX, destY, 1, true), "A cache obstacle must reject a direct player step");
                        require(probe.getWalkSteps().isEmpty() && probe.getX() == x && probe.getY() == y,
                                "Rejected direct step must leave the player and walk queue unchanged");
                        System.out.println("PASS: cache obstacle blocks direct Player step " + x + "," + y
                                + " -> " + destX + "," + destY);
                        return;
                    } finally { channel.finishAndReleaseAll(); }
                }
            }
        }
        throw new AssertionError("Expected an actual cache obstacle in Lumbridge");
    }

    private static void await(EmbeddedChannel channel, BooleanSupplier condition, long timeoutMs) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            channel.runPendingTasks();
            Thread.sleep(10);
        }
        channel.runPendingTasks();
        require(condition.getAsBoolean(), "Timed out waiting for world/channel operation");
    }

    private static byte[] readBytes(EmbeddedChannel channel) {
        Object message = channel.readOutbound();
        require(message instanceof ByteBuf, "Expected a native byte frame");
        ByteBuf buffer = (ByteBuf) message;
        try {
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally { ReferenceCountUtil.release(buffer); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
