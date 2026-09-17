package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.data.parsers.npcs.NPCSpawnsDataParser;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.EntityStrategy;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/** Opt-in real-cache NPC/interaction probe; saves only to a new isolated build directory. */
public final class Native950NpcSmoke {
    private static final int NPC_X = 3217, NPC_Y = 3257, NPC_ID = 494;

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2)
            throw new IllegalArgumentException("Usage: Native950NpcSmoke <flat-cache-directory> [--cold|--spawns]");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950InteractionsSmoke.captureLegacyDispatchBaseline();
        if (args.length == 2 && args[1].equals("--cold")) { coldStart(); return; }
        // M4: the spawn phase on its own, over regions it loads itself. The full run below
        // reaches it too; this mode exists so the spawner can be exercised without paying
        // for the banking, routing and reconnect phases in front of it.
        if (args.length == 2 && args[1].equals("--spawns")) { spawnsOnly(); return; }
        require(args.length == 1, "Unknown smoke option");
        Process cold = new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-Xmx2g", "-cp", System.getProperty("java.class.path"), Native950NpcSmoke.class.getName(),
                args[0], "--cold").inheritIO().start();
        if (!cold.waitFor(35, TimeUnit.SECONDS)) { cold.destroyForcibly(); throw new AssertionError("Cold NPC admission timed out"); }
        require(cold.exitValue() == 0, "Fresh-process remote NPC admission failed");
        // RouteFinder is shared mutable state: choose test tiles before the world thread starts.
        World.getRegion(new WorldTile(NPC_X, NPC_Y, 0).getRegionId(), true);
        NPC target = NPC.createNative950(NPC_ID, new WorldTile(NPC_X, NPC_Y, 0), 1);
        WorldTile start = approachTile(target), loginStart = new WorldTile(3222, 3222, 0);
        int approach = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, start.getX(), start.getY(), 0, 1,
                new EntityStrategy(target), false);
        require(approach > 0 && !RouteFinder.lastIsAlternative(), "Expected exact route to banker");
        WorldTile bankStand = new WorldTile(RouteFinder.getLastPathBufferX()[0], RouteFinder.getLastPathBufferY()[0], 0);
        WorldTile far = new WorldTile(3217, 3217, 0);
        int rebuildChunks = ((Settings.MAP_SIZES[0] >> 3) / 2) - 1;
        require(bankStand.getChunkY() - loginStart.getChunkY() >= rebuildChunks
                        && bankStand.getChunkY() - far.getChunkY() >= rebuildChunks,
                "Test route must cross actual chunk-based scene thresholds in both directions");
        verifyRoute(loginStart, start);
        verifyRoute(bankStand, far);
        verifyRoute(far, start);
        Files.createDirectories(Paths.get("build"));
        Path directory = Files.createTempDirectory(Paths.get("build"), "npc947-smoke-");
        Native950SaveStore saves = new Native950SaveStore(directory.resolve("profiles"));
        Native950Content content = content();
        Native950World world = Native950World.getInstance();
        int savedX, savedY;
        try (Connection c = new Connection(world, content, saves, loginStart)) {
            Native950Session.Snapshot state = c.await(s -> s.ticks >= 1, 5000, "new profile at normal spawn");
            require(!state.npc.visible && c.adds == 0, "Banker must begin out of view at the normal spawn");
            c.walk(start.getX(), start.getY());
            state = c.await(s -> s.x == start.getX() && s.y == start.getY() && s.npc.visible,
                    60000, "walk from normal spawn into banker visibility");
            int index = state.npc.index;
            require(World.getNPCs().size() == 1 && World.getNPCs().get(index).isNative950(), "One isolated real NPC must be registered");
            require(c.adds == 1 && c.lastAddedIndex == index, "Encrypted NPC_INFO must contain the registered NPC index");
            // A banker nobody has asked for anything yet still has direction 0, which is
            // Ataraxia's SOUTH and reaches the client as facing 4. Before 2026-09-07 every NPC
            // was added with 0 instead, which is not an "unspecified" value - it means north.
            require(c.lastAddedFacing == 4,
                    "An untouched banker faces south, wire facing 4; saw " + c.lastAddedFacing);
            long initialTicks = state.ticks;
            state = c.await(s -> s.ticks >= initialTicks + 2, 4000, "NPC retention");
            require(c.retains >= 2, "Native output must retain the same static NPC across ticks");

            long rejected = state.interactions.rejectedActions;
            c.npc(1, index + 1);
            state = c.rejected(rejected);
            require(!state.interactions.bankOpen, "A forged NPC index must not open banking");
            long messageCount = c.examines;
            c.npc(6, index);
            state = c.await(s -> c.examines > messageCount, 5000, "NPC Examine output");
            require(state.x == start.getX() && state.y == start.getY(), "Examine must not move the character");

            // Both packets arrive together: a later walk must cancel the earlier NPC approach.
            long cancelTicks = state.ticks;
            c.npcThenWalk(index, start.getX(), start.getY());
            state = c.await(s -> s.ticks >= cancelTicks + 3, 5000, "walk cancels queued NPC interaction");
            require(!state.interactions.bankOpen && state.x == start.getX() && state.y == start.getY(),
                    "A cancelled approach must not open banking on a later tick");

            long beforeBankRebuilds = state.sceneRebuilds, beforeBankAdditions = state.npc.additions;
            c.npc(1, index);
            state = c.await(s -> s.interactions.bankOpen, 20000, "collision-safe route to banker");
            require(state.steps > 0 && Math.abs(state.x - NPC_X) <= 1 && Math.abs(state.y - NPC_Y) <= 1,
                    "Bank action must approach the actual banker before opening");
            // Serving turns the banker: Entity.faceEntity sets the face rectangle and
            // Entity.updateAngle sets direction with it. Remember the tile it was served from,
            // because nothing turns it back and the re-add below must still point there.
            int servedX = state.x, servedY = state.y;
            require(state.sceneRebuilds > beforeBankRebuilds && state.npc.additions > beforeBankAdditions,
                    "Approaching from normal spawn must re-add the already visible banker across a scene rebuild");
            long transactions = state.interactions.transactions;
            c.button(1, 39, -1, -1); // Deposit all.
            state = c.transactions(transactions);
            require(total(state.interactions.inventory, 995) == 0 && total(state.interactions.bank, 995) == 1000,
                    "NPC banking must deposit into the real containers");
            transactions = state.interactions.transactions;
            c.button(1, 201, slotOf(state.interactions.bank, 995), 995);
            state = c.transactions(transactions);
            require(total(state.interactions.inventory, 995) == 1 && total(state.interactions.bank, 995) == 999,
                    "NPC banking must withdraw exactly one actual coin");
            long talks = c.talks;
            c.npc(3, index);
            state = c.await(s -> c.talks > talks, 5000, "NPC Talk-to output");
            require(!state.interactions.bankOpen, "Talk-to must close the bank and deliver its response");
            // M4: the NPC mask source is installed, so serving a player makes the banker turn
            // to face them. That reaches the client as the retained mask-only form plus the
            // CONFIRMED face-coordinate block, both decoded above.
            require(c.maskOnly > 0 && c.faceCoordBlocks > 0,
                    "Banking must publish the banker's face-coordinate mask");
            System.out.println("PASS: encrypted Bank/Talk/Examine, exact NPC index, collision approach, walk cancellation and item conservation"
                    + " (maskOnly=" + c.maskOnly + " faceCoord=" + c.faceCoordBlocks + ")");

            long rebuilds = state.sceneRebuilds, additions = state.npc.additions;
            c.walk(far.getX(), far.getY());
            state = c.await(s -> s.x == far.getX() && s.y == far.getY(), 60000, "walk outside NPC view across scene boundary");
            require(!state.npc.visible && state.npc.removals > 0 && c.removes > 0,
                    "Leaving range must remove the NPC from native visibility");
            require(state.sceneRebuilds > rebuilds, "Range test must include an actual scene rebuild");
            rejected = state.interactions.rejectedActions;
            c.npc(1, index);
            state = c.rejected(rejected);
            require(!state.interactions.bankOpen && state.x == far.getX() && state.y == far.getY(),
                    "Remembering a valid NPC index must not authorize out-of-view banking or movement");
            c.walk(start.getX(), start.getY());
            state = c.await(s -> s.x == start.getX() && s.y == start.getY() && s.npc.visible,
                    60000, "return to NPC view");
            require(state.npc.additions > additions && c.adds >= 2 && c.lastAddedIndex == index,
                    "Returning must re-add the same registered NPC with native NPC_INFO");
            // The facing survives the removal and comes back on the addition record. This is the
            // assertion that would have caught a facing hard-wired to any constant: the banker
            // turned once, and both the value and the fact that it is no longer 4 are checked.
            int servedFacing = facingToward(NPC_X, NPC_Y, servedX, servedY);
            require(c.lastAddedFacing == servedFacing,
                    "A re-added banker must still face the tile it served from (" + servedX + "," + servedY
                            + "), which is facing " + servedFacing + "; saw " + c.lastAddedFacing);
            require(servedFacing != 4, "This route must turn the banker away from its default facing");
            c.npc(1, index);
            state = c.await(s -> s.interactions.bankOpen, 20000, "bank again after remove/re-add");
            require(state.unhandledFrames == 0, "Every smoke input must use a verified native decoder");
            require(state.interactions.unhandledActions == 0,
                    "Every decoded action must reach a handler: " + state.interactions.unhandledActionReport);
            assertTotals(state);
            savedX = state.x; savedY = state.y;
            Native950InteractionsSmoke.reportRouting("npc", state, 0, 0, 0, 2);
            System.out.println("PASS: native removal/re-add, real scene rebuild, out-of-view rejection and banker remains usable");
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),
                "Disconnect must release the character, banker and world reservation");
        try (Connection c = new Connection(world, content, saves, start)) {
            Native950Session.Snapshot state = c.await(s -> s.npc.visible, 5000, "fresh connection NPC addition");
            require(state.x == savedX && state.y == savedY && !state.interactions.bankOpen,
                    "Saved position must restore while NPC bank interface starts closed");
            require(c.adds == 1 && c.firstNpcWasAdd, "Reconnect must begin with an add, not retained stale NPC state");
            require(total(state.interactions.inventory, 995) == 1 && total(state.interactions.bank, 995) == 999,
                    "NPC-bank transfers must survive isolated profile reconnect without starter duplication");
            c.npc(1, state.npc.index);
            state = c.await(s -> s.interactions.bankOpen, 5000, "bank on restored profile");
            assertTotals(state);
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty() && World.getNPCs().isEmpty(), "Reconnected NPC must also clean up");
        System.out.println("PASS: NPC profile reconnect, closed initial bank, no starter duplication, fresh visibility and disconnect cleanup");
        System.out.println("Isolated NPC smoke profile: " + directory.toAbsolutePath());
        spawnPhase(world);
    }

    // ------------------------------------------------------------------ M4: spawns.json

    /**
     * Lumbridge and the three map squares around it, which is the scene a character standing
     * at the smoke's banker actually loads. Region 12850 is the interesting one: its 123
     * spawn rows contain stable ids, ids with no 947 definition and ids the 947 cache
     * repurposed, so all four verdict classes are exercised by real data.
     */
    private static final int[] SPAWN_PROBE_REGIONS = { 12850, 12851, 12594, 13106 };

    /** {@code --spawns}: load the Lumbridge squares and run only the spawn phase. */
    private static void spawnsOnly() throws Exception {
        for (int regionId : SPAWN_PROBE_REGIONS) World.getRegion(regionId, true);
        spawnPhase(Native950World.getInstance());
    }

    /**
     * The spawn tables the live server gets from {@code Native950Bootstrap}. This smoke does
     * not run the bootstrap, so the four npc parsers the spawn rows depend on are loaded
     * here, in the bootstrap's own order: the legacy NPC prototype the parser builds reads
     * weaknesses, combat definitions and stats while it is constructed.
     */
    /**
     * Which way an NPC at {@code (fromX,fromY)} must be seen to look when it is facing
     * {@code (toX,toY)}, as the addition record's three-bit field.
     *
     * <p>Derived from tile geometry and the CONFIRMED field mapping, NOT from the server's
     * angle arithmetic, so this is an independent expectation rather than a restatement of
     * {@code Entity.updateAngle}. The field is
     * {@code ((direction >> 11) - 4) & 7} and Ataraxia's direction is
     * {@code atan2(dx, dy) * 16384/(2*pi)} measured from the target back to the entity, which
     * composes to {@code floor(theta * 4/pi) mod 8} with {@code theta} the bearing from the
     * entity to what it is looking at, zero at north and increasing clockwise
     * (`verified/MOVEMENT_TABLES.md` section 4). Sector floors, not nearest compass point:
     * field 0 covers north up to but excluding north-east.
     */
    private static int facingToward(int fromX, int fromY, int toX, int toY) {
        int dx = toX - fromX, dy = toY - fromY;
        // Nothing to look at leaves direction 0, which is Ataraxia's SOUTH and wire facing 4.
        if (dx == 0 && dy == 0) return 4;
        int sector = (int) Math.floor(Math.atan2(dx, dy) * 4.0 / Math.PI);
        return ((sector % 8) + 8) % 8;
    }

    private static void loadSpawnData() {
        if (NPCSpawnsDataParser.native947SpawnRegions() > 0) return;
        com.rs.utils.data.parsers.npcs.NPCWeaknessesDataParser.init();
        com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser.init();
        com.rs.utils.data.parsers.npcs.NPCStatsDataParser.init();
        NPCSpawnsDataParser.init();
        require(NPCSpawnsDataParser.native947SpawnRegions() > 0,
                "npcs/spawns.json must load before the spawn phase can mean anything");
    }

    /** What the data file and the validity table say should happen, computed independently. */
    private static final class Expectation {
        final java.util.TreeMap<String, Integer> refusedByVerdict = new java.util.TreeMap<String, Integer>();
        int rows, safe, undecodable, wanderers, regionsWithRows;
        /**
         * The regions this expectation was computed over. Spawning grows {@code World.getRegions()}
         * with unloaded neighbours (an NPC's own {@code loadMapRegions} creates Region objects
         * without loading their maps), so the re-announce phase must touch exactly these and not
         * whatever the map has accumulated since.
         */
        final java.util.List<Integer> regions = new java.util.ArrayList<Integer>();
        void refuse(String verdict) {
            Integer current = refusedByVerdict.get(verdict);
            refusedByVerdict.put(verdict, Integer.valueOf(current == null ? 1 : current.intValue() + 1));
        }
        int refused() {
            int total = 0;
            for (Integer count : refusedByVerdict.values()) total += count.intValue();
            return total;
        }
    }

    /**
     * M4: {@code npcs/spawns.json} becomes world entities, behind the 910 -&gt; 947 id validity
     * table. Runs last and with no session attached, because the assertions above pin the
     * exact contents of an otherwise empty world, and because a populated Lumbridge would
     * make every one of those counts meaningless. Data spawning is off by default for the
     * same reason; this phase turns it on explicitly.
     *
     * <p>Three things are checked, and the expectation for all three is derived from the
     * data file plus the checked-in table rather than from the spawner's own tallies:
     * the right number of NPCs appear, every row that did not appear is accounted for by a
     * refusal with a reason, and a wanderer actually changes tile from one tick to the next
     * without leaving its spawn radius.
     */
    private static void spawnPhase(Native950World world) throws Exception {
        require(World.getNPCs().isEmpty(), "The spawn phase must begin with an empty NPC roster");
        require(!world.isDataSpawnsEnabled(), "Data spawning must be off until this phase asks for it");
        loadSpawnData();
        final Native950IdValidity validity = Native950IdValidity.get();
        System.out.println("Validity table: " + validity);

        final Expectation expected = world.execute(() -> {
            Expectation e = new Expectation();
            for (Integer regionId : new java.util.TreeSet<Integer>(World.getRegions().keySet())) {
                e.regions.add(regionId);
                java.util.List<NPCSpawnsDataParser.Native950Spawn> rows =
                        NPCSpawnsDataParser.native947Spawns(regionId.intValue());
                if (!rows.isEmpty()) e.regionsWithRows++;
                for (NPCSpawnsDataParser.Native950Spawn row : rows) {
                    e.rows++;
                    Native950IdValidity.Verdict verdict = validity.verdict(Native950IdValidity.Kind.NPC, row.npcId);
                    if (!verdict.isSafe()) { e.refuse(verdict.name().toLowerCase()); continue; }
                    NPCDefinitions definitions = NPCDefinitions.getNPCDefinitions(row.npcId);
                    if (definitions == null || definitions.decodeFailure != null
                            || definitions.size < 1 || definitions.size > 255) { e.undecodable++; continue; }
                    e.safe++;
                    if (Native950World.wanderRadius(row, definitions) > 0) e.wanderers++;
                }
            }
            // Enabled only after the expectation is fixed, so the two cannot influence
            // each other; regions already loaded are re-announced by this call.
            world.setDataSpawnsEnabled(true);
            return e;
        }).get(30, TimeUnit.SECONDS);
        require(expected.rows > 0 && expected.safe > 0,
                "The smoke's own route work must have loaded at least one region with usable spawn rows");

        Native950World.SpawnCounters counters = null;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (System.nanoTime() < deadline) {
            counters = world.spawnCounters();
            if (counters.rows >= expected.rows) break;
            Thread.sleep(50);
        }
        require(counters != null && counters.rows == expected.rows,
                "Every spawn row of every loaded region must be read exactly once: expected " + expected.rows
                        + ", read " + (counters == null ? -1 : counters.rows));
        require(counters.regions == expected.regionsWithRows,
                "Each loaded region with rows must be spawned exactly once: expected " + expected.regionsWithRows
                        + ", got " + counters.regions);
        require(counters.spawned == expected.safe,
                "Only ids the table calls 'same' may spawn: expected " + expected.safe + ", got " + counters.spawned);
        require(counters.undecodable == expected.undecodable,
                "Undecodable 947 definitions must be counted, not spawned");
        require(counters.failures == 0, "No spawn row may fail with an exception: " + counters);
        require(counters.refusedByVerdict.equals(expected.refusedByVerdict),
                "Skip counts must match the validity table verdict for verdict: expected "
                        + expected.refusedByVerdict + ", got " + counters.refusedByVerdict);
        require(counters.spawned + counters.refused() + counters.undecodable + counters.failures == counters.rows,
                "Every row is either spawned or accounted for by a counted reason: " + counters);

        int inWorld = world.execute(() -> Integer.valueOf(World.getNPCs().size())).get(10, TimeUnit.SECONDS).intValue();
        require(inWorld == expected.safe, "The world must hold exactly the spawned NPCs: expected "
                + expected.safe + ", found " + inWorld);
        System.out.println("Spawned " + counters);

        // Anchors: the classification is not just arithmetic. Lumbridge 12850 carries both a
        // stable id (1 "Man") and a repurposed one (5146, "Li'l lamb" in 910 and something
        // else in 947), and only one of them may be standing in the world.
        String anchors = world.execute(() -> {
            boolean stable = false, repurposed = false;
            for (NPC npc : World.getNPCs()) {
                if (npc == null) continue;
                if (npc.getId() == 1) stable = true;
                if (!validity.isSafe(Native950IdValidity.Kind.NPC, npc.getId())) repurposed = true;
            }
            return stable + "/" + repurposed;
        }).get(10, TimeUnit.SECONDS);
        require(anchors.endsWith("/false"), "No NPC the validity table refuses may be in the world");
        require(anchors.startsWith("true"), "The stable Lumbridge ids must actually be spawned");

        // Region registration is what Native950NpcViewport.nearbyNpcs reads, so a spawned
        // NPC that is not in its region index would be invisible however correct the encoder.
        int unregistered = world.execute(() -> {
            int missing = 0;
            for (NPC npc : World.getNPCs()) {
                if (npc == null) continue;
                java.util.List<Integer> indexes = World.getRegion(npc.getRegionId()).getNPCsIndexes();
                if (indexes == null || !indexes.contains(Integer.valueOf(npc.getIndex()))) missing++;
            }
            return Integer.valueOf(missing);
        }).get(10, TimeUnit.SECONDS).intValue();
        require(unregistered == 0, unregistered + " spawned NPCs are not in their region index and could never be published");

        require(expected.wanderers > 0, "The loaded regions must contain at least one NPC the 947 cache lets walk");
        java.util.Map<Integer, String> before = tiles(world);
        int moved = 0;
        long walkDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (System.nanoTime() < walkDeadline && moved == 0) {
            Thread.sleep(100);
            java.util.Map<Integer, String> now = tiles(world);
            moved = 0;
            for (java.util.Map.Entry<Integer, String> entry : now.entrySet())
                if (!entry.getValue().equals(before.get(entry.getKey()))) moved++;
        }
        require(moved > 0, "At least one spawned NPC must actually change tile across ticks");

        int strayed = world.execute(() -> {
            int outside = 0;
            for (NPC npc : World.getNPCs()) {
                if (npc == null) continue;
                WorldTile home = npc.getRespawnTile();
                if (Math.max(Math.abs(npc.getX() - home.getX()), Math.abs(npc.getY() - home.getY()))
                        > Native950World.DATA_WANDER_RADIUS) outside++;
            }
            return Integer.valueOf(outside);
        }).get(10, TimeUnit.SECONDS).intValue();
        require(strayed == 0, strayed + " wandering NPCs left their spawn radius");

        // The world empties its NPC roster when the last character logs out. Everything that
        // decides whether a region may be spawned again lives outside this class - the world's
        // own spawnedRegions set AND the per-Region one-shot announcement flag - so this phase
        // drives that teardown directly and requires the very same population to come back.
        // Without Region.rearmNative950Spawns() the announcement is once per JVM and the world
        // would stay empty for every login after the first.
        long spawnedBefore = counters.spawned;
        int emptied = world.execute(() -> {
            world.clearNativeNpcs();
            return Integer.valueOf(World.getNPCs().size());
        }).get(10, TimeUnit.SECONDS).intValue();
        require(emptied == 0, "Clearing the roster must remove every native NPC, found " + emptied);
        world.execute(() -> {
            for (Integer regionId : expected.regions) World.getRegion(regionId.intValue(), true);
            return null;
        }).get(30, TimeUnit.SECONDS);
        Native950World.SpawnCounters after = null;
        long respawnDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (System.nanoTime() < respawnDeadline) {
            after = world.spawnCounters();
            if (after.spawned >= spawnedBefore * 2) break;
            Thread.sleep(50);
        }
        require(after != null && after.spawned == spawnedBefore * 2,
                "A re-announced region must spawn exactly the same population again: expected "
                        + (spawnedBefore * 2) + " cumulative, got " + (after == null ? -1 : after.spawned));
        require(after.rows == counters.rows * 2, "Every row must be read again: expected "
                + (counters.rows * 2) + ", read " + after.rows);
        require(after.failures == 0, "No spawn row may fail on the second pass: " + after);
        int repopulated = world.execute(() -> Integer.valueOf(World.getNPCs().size())).get(10, TimeUnit.SECONDS).intValue();
        require(repopulated == expected.safe, "The world must hold the same NPCs after a re-login: expected "
                + expected.safe + ", found " + repopulated);
        System.out.println("Re-announced after an emptied roster: " + after);

        System.out.println("PASS: " + counters.spawned + " NPCs spawned from spawns.json across "
                + counters.regions + " regions, " + counters.refused() + " rows refused by the validity table "
                + counters.refusedByVerdict + ", " + moved + " of " + expected.wanderers + " wanderers moved within "
                + Native950World.DATA_WANDER_RADIUS + " tiles of home");
    }

    /** Every registered NPC's index and tile, read on the world thread. */
    private static java.util.Map<Integer, String> tiles(Native950World world) throws Exception {
        return world.execute(() -> {
            java.util.Map<Integer, String> out = new java.util.TreeMap<Integer, String>();
            for (NPC npc : World.getNPCs())
                if (npc != null) out.put(Integer.valueOf(npc.getIndex()),
                        npc.getX() + "," + npc.getY() + "," + npc.getPlane());
            return out;
        }).get(10, TimeUnit.SECONDS);
    }

    private static void coldStart() throws Exception {
        WorldTile remote = null;
        for (int x = 3360; x < 3370 && remote == null; x++) for (int y = 3360; y < 3370; y++) {
            World.getRegion(new WorldTile(x, y, 0).getRegionId(), true);
            if (World.isFloorFree(0, x, y)) { remote = new WorldTile(x, y, 0); break; }
        }
        require(remote != null, "Expected a walkable remote test tile");
        int bankRegion = new WorldTile(NPC_X, NPC_Y, 0).getRegionId();
        require(!World.getRegions().containsKey(bankRegion), "Cold test must begin without the bank map square loaded");
        Native950World world = Native950World.getInstance();
        try (Connection c = new Connection(world, content(), null, remote)) {
            Native950Session.Snapshot state = c.await(s -> s.ticks >= 1, 5000, "cold remote character admission");
            require(!state.npc.visible && c.adds == 0 && World.getNPCs().size() == 1,
                    "A distant first login must register its banker without exposing it remotely");
            require(World.getRegions().containsKey(bankRegion), "NPC admission must load its own collision square");
        }
        require(World.getNPCs().isEmpty() && World.getPlayers().isEmpty(), "Cold remote NPC must clean up");
        System.out.println("PASS: fresh JVM login outside Lumbridge loads banker's own map and keeps it out of view");
    }

    private static WorldTile approachTile(NPC target) {
        for (int radius = 6; radius <= 9; radius++) {
            for (int dx = -radius; dx <= radius; dx++) for (int dy = -radius; dy <= radius; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
                int x = NPC_X + dx, y = NPC_Y + dy;
                World.getRegion(new WorldTile(x, y, 0).getRegionId(), true);
                if (World.isFloorFree(0, x, y) && RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,
                        x, y, 0, 1, new EntityStrategy(target), false) > 0 && !RouteFinder.lastIsAlternative())
                    return new WorldTile(x, y, 0);
            }
        }
        throw new AssertionError("No real-cache banker approach tile");
    }

    private static void verifyRoute(WorldTile from, WorldTile to) {
        require(Math.abs(to.getX() - from.getX()) <= 48 && Math.abs(to.getY() - from.getY()) <= 48,
                "Smoke walking request must fit the server's route bound");
        World.getRegion(from.getRegionId(), true); World.getRegion(to.getRegionId(), true);
        require(World.isFloorFree(0, from.getX(), from.getY()) && World.isFloorFree(0, to.getX(), to.getY()),
                "Smoke route endpoints must be real walkable floor");
        int count = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, from.getX(), from.getY(), 0, 1,
                new FixedTileStrategy(to.getX(), to.getY()), false);
        require(count > 0 && !RouteFinder.lastIsAlternative(), "Smoke route must reach its exact target");
        int steps = 0, previousX = from.getX(), previousY = from.getY();
        int[] routeX = RouteFinder.getLastPathBufferX(), routeY = RouteFinder.getLastPathBufferY();
        for (int i = count - 1; i >= 0; i--) {
            steps += Math.max(Math.abs(routeX[i] - previousX), Math.abs(routeY[i] - previousY));
            previousX = routeX[i]; previousY = routeY[i];
        }
        require(steps <= 80, "Smoke route detour exceeds its bounded wait");
        System.out.println("Verified NPC route: " + from.getX() + "," + from.getY() + " -> " + to.getX() + "," + to.getY()
                + " (" + steps + " steps)");
    }

    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] amounts = new int[11];
        amounts[1] = amounts[2] = 1; amounts[3] = 5; amounts[4] = 10; amounts[7] = Integer.MAX_VALUE;
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                amounts, amounts, Collections.emptyList(), Collections.emptyList()),
                new Native950Content.BankerNpc(NPC_ID, "Banker", NPC_X, NPC_Y, 0, 1, 1, 3));
    }

    private static final class Connection implements AutoCloseable {
        final Native950World world;
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Native950Isaac client = new Native950Isaac(new int[] {947, 3, 2026, 494});
        final Native950Isaac output = new Native950Isaac(new int[] {997, 53, 2076, 544});
        long adds, retains, removes, examines, talks;
        /** M4: retained mask-only entries and the face-coordinate blocks they carried. */
        long maskOnly, faceCoordBlocks;
        boolean firstNpcWasAdd;
        int lastAddedIndex = -1;
        /** The three-bit facing of the most recent addition record. -1 until one arrives. */
        int lastAddedFacing = -1;

        Connection(Native950World world, Native950Content content, Native950SaveStore saves, WorldTile start) throws Exception {
            this.world = world;
            Native950Isaac incoming = new Native950Isaac(new int[] {947, 3, 2026, 494});
            Native950Isaac outgoing = new Native950Isaac(new int[] {997, 53, 2076, 544});
            for (int i = 0; i < 3; i++) { client.getAsInt(); incoming.getAsInt(); }
            for (int i = 0; i < 2; i++) { output.getAsInt(); outgoing.getAsInt(); }
            CompletableFuture<Native950Session> future = world.attach(channel, "npc_probe", incoming, outgoing,
                    new byte[] {0}, new Native950World.SceneConfig(start.getX(), start.getY(), 0, 1, 7, 0, 0, 0),
                    Collections.emptyList(), content, saves);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!future.isDone() && System.nanoTime() < deadline) { channel.runPendingTasks(); Thread.sleep(20); }
            future.get(1, TimeUnit.SECONDS);
        }

        void npc(int option, int index) { input(npcOpcode(option), npcBody(index)); }
        private static int npcOpcode(int option) { return new int[] {88, 115, 33, 60, 123, 104}[option - 1]; }
        private static byte[] npcBody(int index) { return new byte[] {(byte) (index >>> 8), (byte) index, (byte) 128}; }
        void walk(int x, int y) { input(3, walkBody(x, y)); }
        private static byte[] walkBody(int x, int y) {
            return new byte[] {(byte) (y >>> 8), (byte) (y + 128), (byte) (x >>> 8), (byte) x, (byte) 128};
        }
        void npcThenWalk(int index, int x, int y) {
            ByteBuf bytes = Unpooled.buffer(10);
            bytes.writeByte(88 + client.getAsInt()).writeBytes(npcBody(index));
            bytes.writeByte(3 + client.getAsInt()).writeBytes(walkBody(x, y));
            channel.writeInbound(bytes);
        }
        void button(int option, int component, int slot, int item) {
            int opcode = new int[] {96, 77, 4, 95, 29, 51, 5, 21, 18, 36}[option - 1], hash = (517 << 16) | component;
            input(opcode, new byte[] {(byte) (item >>> 8), (byte) item, (byte) (hash >>> 8), (byte) hash,
                    (byte) (hash >>> 24), (byte) (hash >>> 16), (byte) (slot >>> 8), (byte) slot});
        }
        void input(int opcode, byte[] payload) {
            require(channel.isActive(), "Input requires a live smoke connection");
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) (opcode + client.getAsInt())}));
            channel.writeInbound(Unpooled.wrappedBuffer(payload)); // Deliberately fragmented native input.
        }

        Native950Session.Snapshot rejected(long previous) throws Exception {
            Native950Session.Snapshot state = await(s -> s.interactions.rejectedActions > previous, 5000, "rejected NPC action");
            assertTotals(state); return state;
        }
        Native950Session.Snapshot transactions(long previous) throws Exception {
            Native950Session.Snapshot state = await(s -> s.interactions.transactions > previous, 5000, "NPC bank transaction");
            require(state.interactions.transactions == previous + 1, "Exactly one requested transfer must occur");
            assertTotals(state); return state;
        }
        Native950Session.Snapshot await(Predicate<Native950Session.Snapshot> test, long timeoutMs, String description) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
            Native950Session.Snapshot state;
            do {
                pump(); state = world.snapshot().get(2, TimeUnit.SECONDS); pump();
                require(state != null && state.active && state.npc != null, "A live NPC-enabled character is required");
                if (test.test(state)) return state;
                Thread.sleep(20);
            } while (System.nanoTime() < deadline);
            throw new AssertionError("Timed out: " + description + "; last=" + state);
        }
        void pump() {
            channel.runPendingTasks();
            Object item;
            while ((item = channel.readOutbound()) != null) {
                try {
                    require(item instanceof ByteBuf, "Native output must be framed bytes");
                    ByteBuf bytes = (ByteBuf) item;
                    while (bytes.isReadable()) {
                        int opcode = (bytes.readUnsignedByte() - output.getAsInt()) & 255;
                        if (opcode >= 128) opcode = ((opcode - 128) << 8) | ((bytes.readUnsignedByte() - output.getAsInt()) & 255);
                        ServerPacket packet = null;
                        for (ServerPacket candidate : ServerPacket.values()) if (candidate.opcode() == opcode) packet = candidate;
                        require(packet != null, "Unknown/cipher-misaligned native output opcode " + opcode);
                        int size = packet.size();
                        if (size == -1) size = bytes.readUnsignedByte(); else if (size == -2) size = bytes.readUnsignedShort();
                        require(size <= bytes.readableBytes(), "Truncated native output frame");
                        byte[] body = new byte[size]; bytes.readBytes(body);
                        if (packet == ServerPacket.NPC_INFO) npcOutput(body);
                        if (packet == ServerPacket.MESSAGE_GAME && body.length >= 7 && body[0] == 0) {
                            String message = new String(body, 6, body.length - 7, Charset.forName("windows-1252"));
                            if (message.equals("Banker.")) examines++;
                            if (message.startsWith("Banker: Welcome!")) talks++;
                        }
                    }
                } finally { ReferenceCountUtil.release(item); }
            }
            channel.checkException();
        }
        /**
         * P6 parses the generalised NPC_INFO body instead of matching the three literal
         * single-NPC payloads: an 8-bit retained count, one changed bit (plus a 2-bit
         * selector when set) per retained entry, then additions while at least 16 bits
         * remain (verified/NPC_INFO.md, "Retained NPC list" and "Adding one stationary NPC").
         * The unchanged and empty bodies are still 01 00 and 00; a removal now normally
         * arrives as the retained list's removal selector rather than a zero count.
         */
        private void npcOutput(byte[] body) {
            boolean firstFrame = adds + retains + removes == 0;
            Bits bits = new Bits(body);
            int retained = bits.read(8);
            int addedHere = 0;
            int pendingMasks = 0;
            for (int entry = 0; entry < retained; entry++) {
                if (bits.read(1) == 0) { retains++; continue; }
                int selector = bits.read(2);
                if (selector == 3) { removes++; continue; }
                // M4 installed the entity mask source, so the static banker now also uses the
                // mask-only retained form: banking makes it face the player it is serving, and
                // Entity.faceEntity sets the face RECTANGLE, which reaches the client as the
                // CONFIRMED face-coordinate block. It still never walks or runs.
                require(selector == 0, "The static banker only ever uses the removal or mask-only retained form");
                maskOnly++;
                pendingMasks++;
            }
            while (body.length * 8 - bits.position >= 16) {
                int index = bits.read(16);
                if (index == 65535) break; // explicit terminator, only written before a mask section
                lastAddedIndex = index;
                bits.read(7);
                require(bits.read(2) == 0, "Banker must be on the correct plane");
                bits.read(7);
                require(bits.read(16) == NPC_ID && bits.read(1) == 1,
                        "NPC type and immediate-position flag must match");
                // The addition record's three-bit facing field, wired in on 2026-09-07. The
                // banker is created by NPC.createNative950, which leaves direction at 0, and 0
                // in Ataraxia's 1/16384-turn compass is SOUTH (Utils.getAngleToForceMovement:
                // angle>>11 == 0 is SOUTH). The verified transform is (direction >> 11) - 4
                // truncated to three bits, so 0 goes on the wire as 4, and the client's decode
                // adds the 180 degrees back and faces the banker south again. This number is
                // written independently of the encoder on purpose: it is the whole point of a
                // wire smoke that it does not ask the code under test what to expect.
                // The addition record's three-bit facing field, wired in on 2026-09-07. What it
                // should say depends on what the banker has been asked to look at, so the parser
                // only records it and the phases below decide.
                lastAddedFacing = bits.read(3);
                if (bits.read(1) == 1) pendingMasks++;
                adds++;
                addedHere++;
            }
            if (pendingMasks == 0) {
                require(body.length * 8 - bits.position < 8, "NPC_INFO must end within one byte of padding");
            } else {
                // Byte alignment (0x14011F0D7), then one mask block per pending NPC, each
                // preceded by the two bytes the client skips at 0x14011F11B. The only mask this
                // banker can produce is the face coordinate (bit 3), whose block is four bytes.
                int offset = (bits.position + 7) / 8;
                for (int block = 0; block < pendingMasks; block++) {
                    require(offset + 7 <= body.length, "Truncated NPC mask section");
                    require(body[offset] == 0 && body[offset + 1] == 0,
                            "Each NPC mask block starts with the two bytes the client skips");
                    require((body[offset + 2] & 255) == 0x08,
                            "The banker may only send the confirmed face-coordinate mask");
                    faceCoordBlocks++;
                    offset += 7;
                }
                require(offset == body.length, "NPC_INFO must end exactly after its mask section");
            }
            if (firstFrame && addedHere > 0) firstNpcWasAdd = true;
        }
        @Override public void close() throws Exception {
            channel.close();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (world.reservedSlots() > 0 && System.nanoTime() < deadline) { pump(); Thread.sleep(20); }
            pump(); channel.finishAndReleaseAll();
            require(world.reservedSlots() == 0, "World reservation must be released after NPC disconnect");
        }
    }

    private static final class Bits {
        final byte[] bytes; int position;
        Bits(byte[] bytes) { this.bytes = bytes; }
        int read(int count) {
            int result = 0;
            for (int i = 0; i < count; i++, position++) result = (result << 1) | ((bytes[position >>> 3] >>> (7 - (position & 7))) & 1);
            return result;
        }
    }
    private static void assertTotals(Native950Session.Snapshot state) {
        int[] ids = {995, 1511, 315}, quantities = {1000, 5, 5};
        for (int i = 0; i < ids.length; i++) require(total(state.interactions.inventory, ids[i]) + total(state.interactions.bank, ids[i]) == quantities[i],
                "NPC banking changed the total amount of item " + ids[i]);
    }
    private static long total(Native950Containers.Snapshot container, int id) {
        long total = 0;
        for (int i = 0; i < container.ids.length; i++) if (container.ids[i] == id) total += container.amounts[i];
        return total;
    }
    private static int slotOf(Native950Containers.Snapshot container, int id) {
        for (int i = 0; i < container.ids.length; i++) if (container.ids[i] == id) return i;
        throw new AssertionError("Missing item " + id);
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private Native950NpcSmoke() { }
}
