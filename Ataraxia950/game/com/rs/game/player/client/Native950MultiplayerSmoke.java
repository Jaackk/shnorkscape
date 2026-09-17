package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.game.player.Player;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * P6 acceptance probe: two native 947 sessions in one world, on separate channels, against the
 * real cache.
 *
 * <p>It pins the four things P6 changed and the one thing it must not have broken.
 *
 * <ol>
 * <li><b>N-slot admission.</b> Each connection reserves its player index BEFORE any world state
 *     exists - the order {@code Ataraxia947Handoff} needs, because {@code GameLoginResponse}
 *     carries the index while the Player has not been created. The indices are distinct, the
 *     world reports both as used, and the third connection is admitted only after a slot is
 *     freed and gets exactly that freed index back.</li>
 * <li><b>Mutual visibility.</b> Each viewer's PLAYER_INFO viewport grows to hold the other
 *     character, keeps holding it while it walks, and drops it when it disconnects.</li>
 * <li><b>World-phase ordering.</b> A recording encoder captures what every viewer was shown in
 *     each tick. Every viewer in a tick must have been shown the same position for every
 *     character. Under the old per-session tick (move one session, encode it, then move the
 *     next) the first viewer would see the second at its pre-move tile while the second sees
 *     itself post-move, so this assertion is what actually distinguishes the two designs; the
 *     probe also requires that characters really did move during the observation, otherwise
 *     the agreement would be vacuous.</li>
 * <li><b>Several moving NPCs.</b> Two extra world-owned native NPCs are spawned with a wander
 *     radius; both viewers must publish more than one NPC and the NPCs must actually change
 *     tiles, which only happens if the world-phase move runs {@code processNative950Movement}
 *     for the whole roster.</li>
 * <li><b>Nothing regressed.</b> Every outgoing byte decodes with the real ISAAC stream (a
 *     desynchronised frame would break the cipher immediately), no unhandled inbound frame,
 *     conserved container totals, {@code strictHits == 0} and {@code tickFailures == 0} for
 *     every session.</li>
 * </ol>
 */
public final class Native950MultiplayerSmoke {
    private static final int BANKER_X = 3217, BANKER_Y = 3257, BANKER_ID = 494;
    private static final int[] STARTER_IDS = {995, 1511, 315};
    private static final int[] STARTER_AMOUNTS = {1000, 5, 5};

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: Native950MultiplayerSmoke <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950InteractionsSmoke.captureLegacyDispatchBaseline();

        Native950World world = Native950World.getInstance();
        Recorder recorder = new Recorder();
        world.installFrameEncoder(recorder);
        require(world.capacity() > 2, "The generalised encoder must admit more than two characters");

        WorldTile first = walkable(3222, 3222), second = walkable(3225, 3222);
        WorldTile target = walkable(3222, 3226);
        require(Math.abs(first.getX() - second.getX()) <= 24 && Math.abs(first.getY() - second.getY()) <= 24,
                "Both characters must start inside one another's PLAYER_INFO view distance");

        Files.createDirectories(Paths.get("build"));
        Path directory = Files.createTempDirectory(Paths.get("build"), "mp947-smoke-");
        Native950SaveStore saves = new Native950SaveStore(directory.resolve("profiles"));
        Native950Content content = content();

        int alphaIndex, betaIndex;
        try (Connection alpha = new Connection(world, "mpalpha", content, saves, first);
             Connection beta = new Connection(world, "mpbeta", content, saves, second)) {
            alphaIndex = alpha.index;
            betaIndex = beta.index;
            require(alphaIndex != betaIndex, "Two concurrent characters must own distinct player indices");
            require(world.reservedSlots() == 2, "Both slots must be reserved while both characters are attached");
            require(World.getPlayers().size() == 2, "Both characters must be registered in the world");
            require(World.getPlayers().get(alphaIndex).isNative950()
                    && World.getPlayers().get(betaIndex).isNative950(),
                    "Each world slot must hold the native character that reserved it");
            System.out.println("PASS: reserve-before-response admitted two characters at indices "
                    + alphaIndex + " and " + betaIndex);

            Native950Session.Snapshot alphaState = alpha.await(s -> s.viewport != null && s.viewport.trackedPlayers == 2,
                    10000, "alpha sees beta appear");
            Native950Session.Snapshot betaState = beta.await(s -> s.viewport != null && s.viewport.trackedPlayers == 2,
                    10000, "beta sees alpha appear");
            require(alphaState.viewport.adds >= 1 && betaState.viewport.adds >= 1,
                    "Each viewer must have added the other through an external-add record");
            System.out.println("PASS: each viewer added the other: " + alphaState.viewport + " / " + betaState.viewport);

            // Several moving NPCs, world-owned: both viewers see them, and they wander.
            NPC wandererA = world.spawnNativeNpc(BANKER_ID, walkable(BANKER_X + 3, BANKER_Y - 2), 1, 4)
                    .get(5, TimeUnit.SECONDS);
            NPC wandererB = world.spawnNativeNpc(BANKER_ID, walkable(BANKER_X - 3, BANKER_Y - 2), 1, 4)
                    .get(5, TimeUnit.SECONDS);
            require(wandererA.getIndex() != wandererB.getIndex() && wandererA.isNative950Movable()
                    && wandererB.isNative950Movable(), "Both extra NPCs must be movable and distinctly indexed");
            require(World.getNPCs().size() == 3, "The banker and both wanderers must share one NPC registry");

            // Walk beta to a tile inside both scenes and inside the NPCs' region.
            long betaTicks = betaState.ticks;
            beta.walk(target.getX(), target.getY());
            betaState = beta.await(s -> s.x == target.getX() && s.y == target.getY(), 60000, "beta walks");
            require(betaState.steps > 0, "Beta must have taken real clipped steps");
            require(betaState.ticks > betaTicks, "The walk must have spanned real world ticks");
            alphaState = alpha.await(s -> s.viewport.trackedPlayers == 2, 5000, "alpha still holds beta after the walk");
            System.out.println("PASS: beta walked " + betaState.steps + " steps and stayed in alpha's viewport");

            // Walk both characters north to the bankers so several NPCs enter both viewports.
            WorldTile nearNpcs = walkable(BANKER_X + 1, BANKER_Y - 4);
            alpha.walkFar(nearNpcs.getX(), nearNpcs.getY());
            beta.walkFar(nearNpcs.getX() + 1, nearNpcs.getY() - 1);
            alphaState = alpha.await(s -> s.npc != null && s.npc.publishedCount >= 2, 120000,
                    "alpha publishes several NPCs");
            betaState = beta.await(s -> s.npc != null && s.npc.publishedCount >= 2, 120000,
                    "beta publishes several NPCs");
            require(alphaState.viewport.trackedPlayers == 2 && betaState.viewport.trackedPlayers == 2,
                    "Both characters must still see each other beside the NPCs");
            int wanderStartX = wandererA.getX(), wanderStartY = wandererA.getY();
            int wanderStartX2 = wandererB.getX(), wanderStartY2 = wandererB.getY();
            alpha.await(s -> wandererA.getX() != wanderStartX || wandererA.getY() != wanderStartY
                            || wandererB.getX() != wanderStartX2 || wandererB.getY() != wanderStartY2,
                    60000, "a world-owned NPC wanders during the world-phase move");
            System.out.println("PASS: several moving NPCs published to both viewers: alpha=" + alphaState.npc
                    + " beta=" + betaState.npc);

            // Publishing N NPCs must not widen the interaction gate. Everything the NPC path
            // does below the visibility check is the banker's - its examine text, its route
            // target, its pending option - so a click on any other published NPC is refused
            // until clicked-NPC resolution exists, while the banker itself still works.
            require(alphaState.npc.visible && alphaState.npc.index != wandererA.getIndex(),
                    "Alpha must hold both the banker and at least one other NPC for this check");
            long rejectsBefore = alphaState.interactions.rejectedActions;
            alpha.examineNpc(wandererA.getIndex());
            alphaState = alpha.await(s -> s.interactions.rejectedActions > rejectsBefore, 10000,
                    "a click on a published non-banker NPC is refused");
            long rejectsAfterWanderer = alphaState.interactions.rejectedActions;
            long transactionsBefore = alphaState.interactions.transactions;
            alpha.examineNpc(alphaState.npc.index);
            alphaState = alpha.afterTicks(3);
            require(alphaState.interactions.rejectedActions == rejectsAfterWanderer,
                    "The banker's own Examine must still be accepted: " + alphaState.interactions.rejectedActions
                            + " rejections, expected " + rejectsAfterWanderer);
            require(alphaState.interactions.transactions == transactionsBefore,
                    "An Examine must not move items");
            System.out.println("PASS: a click on a published non-banker NPC is refused while the banker's"
                    + " own Examine is still accepted");

            assertHealthy(alphaState); assertHealthy(betaState);
            recorder.assertConsistentTicks();
            System.out.println("PASS: every viewer in every tick was shown the same post-movement world ("
                    + recorder.ticksWithTwoViewers + " two-viewer ticks, " + recorder.ticksWithMovement
                    + " of them after somebody moved)");

            // Beta leaves: alpha must see it go, and the slot must come back.
            beta.close();
            alphaState = alpha.await(s -> s.viewport.trackedPlayers == 1, 10000, "alpha sees beta leave");
            require(alphaState.viewport.removes >= 1, "Alpha must have removed beta from its local list");
            require(World.getPlayers().size() == 1 && World.getPlayers().get(betaIndex) == null,
                    "A disconnect must remove the character from the world registry");
            require(world.reservedSlots() == 1, "A disconnect must release exactly one world slot");
            System.out.println("PASS: disconnect removed the character, freed slot " + betaIndex
                    + " and alpha saw the removal: " + alphaState.viewport);

            // Gamma logs in beside where alpha now stands, so mutual visibility is testable.
            WorldTile beside = walkable(alphaState.x + 2, alphaState.y + 2);
            long alphaAddsBefore = alphaState.viewport.adds;
            try (Connection gamma = new Connection(world, "mpgamma", content, saves, beside)) {
                require(gamma.index == betaIndex, "A third connection must take the freed slot, not a new one");
                require(world.reservedSlots() == 2, "The freed slot must be occupied again");
                Native950Session.Snapshot gammaState = gamma.await(s -> s.viewport != null && s.viewport.trackedPlayers == 2,
                        10000, "gamma sees alpha");
                alphaState = alpha.await(s -> s.viewport.trackedPlayers == 2, 10000, "alpha sees gamma");
                require(alphaState.viewport.adds > alphaAddsBefore,
                        "Alpha must have added the reused index again, as a fresh external player");
                assertHealthy(gammaState); assertHealthy(alphaState);
                System.out.println("PASS: freed slot " + betaIndex + " re-admitted a different character: "
                        + gammaState.viewport);
            }
            alphaState = alpha.await(s -> s.viewport.trackedPlayers == 1, 10000, "alpha alone again");
            assertHealthy(alphaState);
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),
                "Every disconnect must release its slot, character and the world NPC roster");
        System.out.println("PASS: two-session admission, mutual visibility, world-phase ordering, moving NPCs,"
                + " slot release and reuse. Isolated profile directory: " + directory.toAbsolutePath());
    }

    /**
     * Delegates every byte to the live {@link Native950EntityFrames} and records what each
     * viewer was shown, so the world-phase order is checked against observed frames rather
     * than against the shape of the code that produced them.
     */
    private static final class Recorder implements Native950Frames {
        private final Native950EntityFrames delegate = new Native950EntityFrames();
        private final List<Map<Integer, Map<Integer, int[]>>> ticks =
                new ArrayList<Map<Integer, Map<Integer, int[]>>>();
        private Map<Integer, Map<Integer, int[]>> current;
        int ticksWithTwoViewers, ticksWithMovement;

        @Override public int playerCapacity() { return delegate.playerCapacity(); }
        @Override public int npcCapacity() { return delegate.npcCapacity(); }
        @Override public List<Native950Packets.Packet> admit(Player viewer, Native950World.SceneConfig scene,
                                                             List<Player> characters) {
            return delegate.admit(viewer, scene, characters);
        }
        @Override public void release(Player viewer) { delegate.release(viewer); }
        @Override public Native950Viewport.State viewport(Player viewer) { return delegate.viewport(viewer); }

        @Override public void beginFrames(List<Player> characters) {
            current = new LinkedHashMap<Integer, Map<Integer, int[]>>();
            ticks.add(current);
            delegate.beginFrames(characters);
        }

        @Override public void encode(Frame frame) {
            Map<Integer, int[]> seen = new LinkedHashMap<Integer, int[]>();
            for (Player character : frame.players)
                seen.put(Integer.valueOf(character.getIndex()),
                        new int[] {character.getX(), character.getY(), character.getPlane()});
            current.put(Integer.valueOf(frame.viewer.getIndex()), seen);
            delegate.encode(frame);
        }

        /**
         * Every viewer encoded in the same tick must have been shown identical coordinates for
         * every character. A per-session tick that moved and encoded one connection at a time
         * would show the earlier viewer a pre-move position for the later one.
         */
        void assertConsistentTicks() {
            Map<Integer, int[]> previous = new HashMap<Integer, int[]>();
            for (Map<Integer, Map<Integer, int[]>> tick : ticks) {
                if (tick.size() < 2) continue;
                ticksWithTwoViewers++;
                Map<Integer, int[]> agreed = null;
                for (Map<Integer, int[]> seen : tick.values()) {
                    if (agreed == null) { agreed = seen; continue; }
                    require(agreed.keySet().equals(seen.keySet()),
                            "Two viewers in one tick were shown different character sets: " + agreed.keySet()
                                    + " vs " + seen.keySet());
                    for (Map.Entry<Integer, int[]> entry : agreed.entrySet())
                        require(Arrays.equals(entry.getValue(), seen.get(entry.getKey())),
                                "Viewers in one tick disagreed about character " + entry.getKey() + ": "
                                        + Arrays.toString(entry.getValue()) + " vs "
                                        + Arrays.toString(seen.get(entry.getKey())));
                }
                boolean moved = false;
                for (Map.Entry<Integer, int[]> entry : agreed.entrySet()) {
                    int[] before = previous.get(entry.getKey());
                    if (before != null && !Arrays.equals(before, entry.getValue())) moved = true;
                    previous.put(entry.getKey(), entry.getValue());
                }
                if (moved) ticksWithMovement++;
            }
            require(ticksWithTwoViewers > 10, "The probe must observe many two-viewer ticks");
            require(ticksWithMovement > 5,
                    "The agreement is only meaningful across ticks in which characters actually moved");
        }
    }

    /** One authenticated connection: its own channel, its own live ISAAC pair, its own slot. */
    private static final class Connection implements AutoCloseable {
        final Native950World world;
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Native950Isaac client, output;
        final int index;
        long playerFrames, npcFrames;

        Connection(Native950World world, String username, Native950Content content,
                   Native950SaveStore saves, WorldTile start) throws Exception {
            this.world = world;
            int[] inSeed = {947, 3, 2026, username.hashCode()};
            int[] outSeed = {997, 53, 2076, username.hashCode() + 7};
            this.client = new Native950Isaac(inSeed);
            this.output = new Native950Isaac(outSeed);
            Native950Isaac incoming = new Native950Isaac(inSeed);
            Native950Isaac outgoing = new Native950Isaac(outSeed);
            // Model the cipher words login consumed before the bridge is installed.
            for (int i = 0; i < 3; i++) { client.getAsInt(); incoming.getAsInt(); }
            for (int i = 0; i < 2; i++) { output.getAsInt(); outgoing.getAsInt(); }
            // The handoff order: reserve, then build the scene from the reserved index.
            this.index = world.reserve(username);
            require(index >= 1, "The world must reserve a player index before the login response");
            boolean admitted = false;
            try {
                CompletableFuture<Native950Session> future = world.attach(channel, username, incoming, outgoing,
                        new byte[] {0}, new Native950World.SceneConfig(start.getX(), start.getY(), 0, index, 7, 0, 0, 0),
                        Collections.<Native950Packets.Packet>emptyList(), content, saves);
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
                while (!future.isDone() && System.nanoTime() < deadline) { channel.runPendingTasks(); Thread.sleep(20); }
                future.get(1, TimeUnit.SECONDS);
                admitted = true;
            } finally {
                // Username-checked, exactly as the login handoff releases: by the time this
                // branch runs the world may already have freed the slot and another login may
                // own it.
                if (!admitted) world.release(index, username);
            }
        }

        void walk(int x, int y) {
            input(3, new byte[] {(byte) (y >>> 8), (byte) (y + 128), (byte) (x >>> 8), (byte) x, (byte) 128});
        }

        /** Walks in bounded hops, because the server rejects a click more than 48 tiles away. */
        void walkFar(int x, int y) throws Exception {
            for (;;) {
                Native950Session.Snapshot state = snapshot();
                int dx = x - state.x, dy = y - state.y;
                if (dx == 0 && dy == 0) return;
                int hopX = state.x + Math.max(-40, Math.min(40, dx));
                int hopY = state.y + Math.max(-40, Math.min(40, dy));
                walk(hopX, hopY);
                final int wantX = hopX, wantY = hopY;
                await(s -> (s.x == wantX && s.y == wantY) || !s.active, 120000, "walk hop to " + wantX + "," + wantY);
                if (hopX == x && hopY == y) return;
            }
        }

        /** Native NPC option 6 (Examine); opcode 104, index16 then the 0x80 modifier byte. */
        void examineNpc(int npcIndex) {
            input(104, new byte[] {(byte) (npcIndex >>> 8), (byte) npcIndex, (byte) 0x80});
        }

        /** The session's state once {@code ticks} more world ticks have completed. */
        Native950Session.Snapshot afterTicks(final int ticks) throws Exception {
            final long target = snapshot().ticks + ticks;
            return await(s -> s.ticks >= target, 15000, "waiting for " + ticks + " more world ticks");
        }

        void input(int opcode, byte[] payload) {
            require(channel.isActive(), "Input requires a live smoke connection");
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) (opcode + client.getAsInt())}));
            channel.writeInbound(Unpooled.wrappedBuffer(payload));
        }

        Native950Session.Snapshot snapshot() throws Exception {
            pump();
            Native950Session.Snapshot state = world.snapshot(index).get(2, TimeUnit.SECONDS);
            pump();
            require(state != null && state.active, "Connection " + index + " must stay in the world");
            return state;
        }

        Native950Session.Snapshot await(Predicate<Native950Session.Snapshot> test, long timeoutMs, String what)
                throws Exception {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
            Native950Session.Snapshot state;
            do {
                state = snapshot();
                if (test.test(state)) return state;
                Thread.sleep(20);
            } while (System.nanoTime() < deadline);
            throw new AssertionError("Timed out: " + what + "; last=" + state);
        }

        /**
         * Decodes every outgoing frame with the real ISAAC stream. A PLAYER_INFO or NPC_INFO
         * body of the wrong length would desynchronise the opcode cipher on the next frame, so
         * simply draining the connection is itself a wire check.
         */
        void pump() {
            channel.runPendingTasks();
            Object item;
            while ((item = channel.readOutbound()) != null) {
                try {
                    require(item instanceof ByteBuf, "Native output must be framed bytes");
                    ByteBuf bytes = (ByteBuf) item;
                    while (bytes.isReadable()) {
                        int opcode = (bytes.readUnsignedByte() - output.getAsInt()) & 255;
                        if (opcode >= 128)
                            opcode = ((opcode - 128) << 8) | ((bytes.readUnsignedByte() - output.getAsInt()) & 255);
                        ServerPacket packet = null;
                        for (ServerPacket candidate : ServerPacket.values())
                            if (candidate.opcode() == opcode) packet = candidate;
                        require(packet != null, "Unknown or cipher-misaligned native output opcode " + opcode);
                        int size = packet.size();
                        if (size == -1) size = bytes.readUnsignedByte();
                        else if (size == -2) size = bytes.readUnsignedShort();
                        require(size <= bytes.readableBytes(), "Truncated native output frame " + packet);
                        bytes.skipBytes(size);
                        if (packet == ServerPacket.PLAYER_INFO) playerFrames++;
                        if (packet == ServerPacket.NPC_INFO) npcFrames++;
                    }
                } finally { ReferenceCountUtil.release(item); }
            }
            channel.checkException();
        }

        @Override public void close() throws Exception {
            channel.close();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (world.snapshot(index).get(2, TimeUnit.SECONDS) != null && System.nanoTime() < deadline) {
                pump(); Thread.sleep(20);
            }
            pump();
            channel.finishAndReleaseAll();
            require(world.snapshot(index).get(2, TimeUnit.SECONDS) == null,
                    "Disconnect must clear session state for slot " + index);
            require(playerFrames > 0 && npcFrames > 0,
                    "Every session must have received both entity frames: player=" + playerFrames
                            + " npc=" + npcFrames);
        }
    }

    private static void assertHealthy(Native950Session.Snapshot state) {
        require(state.tickFailures == 0, "processEntity must tick without exceptions: " + state);
        require(state.facadeStrictHits == 0, "No STRICT-tier facade method may be reached: " + state.strictHitsByMethod);
        require(state.schedulerFailed == 0, "No scheduled task may fail on the tick wheel: " + state);
        require(state.unhandledFrames == 0, "Every probe input must use a verified native decoder: " + state);
        require(state.processEntityRuns == state.ticks,
                "Player.processEntity must run on every native tick: " + state);
        for (int i = 0; i < STARTER_IDS.length; i++)
            require(total(state.interactions.inventory, STARTER_IDS[i])
                            + total(state.interactions.bank, STARTER_IDS[i]) == STARTER_AMOUNTS[i],
                    "Multiplayer ticking changed the total amount of item " + STARTER_IDS[i]);
    }

    private static long total(Native950Containers.Snapshot container, int id) {
        long amount = 0;
        for (int slot = 0; slot < container.ids.length; slot++)
            if (container.ids[slot] == id) amount += container.amounts[slot];
        return amount;
    }

    private static WorldTile walkable(int x, int y) {
        for (int radius = 0; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) for (int dy = -radius; dy <= radius; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
                WorldTile tile = new WorldTile(x + dx, y + dy, 0);
                World.getRegion(tile.getRegionId(), true);
                if (World.isFloorFree(0, tile.getX(), tile.getY())) return tile;
            }
        }
        throw new AssertionError("No walkable real-cache tile near " + x + "," + y);
    }

    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] amounts = new int[11];
        amounts[1] = amounts[2] = 1; amounts[3] = 5; amounts[4] = 10; amounts[7] = Integer.MAX_VALUE;
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                amounts, amounts, Collections.<Native950Packets.Packet>emptyList(),
                Collections.<Native950Packets.Packet>emptyList()),
                new Native950Content.BankerNpc(BANKER_ID, "Banker", BANKER_X, BANKER_Y, 0, 1, 1, 3));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private Native950MultiplayerSmoke() { }
}
