package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.route.Flags;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Real-cache persistence probe using only newly created, isolated temporary profiles. */
public final class Native950PersistenceSmoke {
    private static final int BANK_ID = 79036, BANK_X = 3215, BANK_Y = 3257;
    private static final String PROFILE = "Save_Probe";
    private static final int BACKPACK_HASH = (1473 << 16) | 5;

    public static void main(String[] args) throws Exception {
        if (args.length == 6 && "--restart-check".equals(args[0])) {
            restartCheck(Paths.get(args[1]), Paths.get(args[2]), Integer.parseInt(args[3]),
                    Integer.parseInt(args[4]), Integer.parseInt(args[5]));
            return;
        }
        if (args.length == 4 && "--skills-check".equals(args[0])) {
            skillsCheck(Paths.get(args[1]), Paths.get(args[2]), Integer.parseInt(args[3]));
            return;
        }
        if (args.length != 1)
            throw new IllegalArgumentException("Usage: Native950PersistenceSmoke <flat-cache-directory>");
        Path cache = Paths.get(args[0]).toAbsolutePath().normalize();
        Cache.initFlatReadOnly(cache);
        WorldTile start = findApproachStart();
        Native950Content content = content();
        Native950World world = Native950World.getInstance();
        Path build = Files.createDirectories(Paths.get("build").toAbsolutePath().normalize());
        Path directory = Files.createTempDirectory(build, "persistence947-smoke-");
        Native950SaveStore store = new Native950SaveStore(directory.resolve("profiles"));
        Native950Session.Snapshot prepared;
        try (Connection connection = new Connection(world)) {
            connection.attach(PROFILE, store, scene(start), content);
            connection.assertInitialScene(start.getX(), start.getY(), start.getPlane());
            Native950Session.Snapshot state = connection.snapshot();
            assertStarter(state);
            require(World.getPlayers().size() == 1 && World.getPlayers().get(1).isNative950(),
                    "Persistence must belong to the real Ataraxia Player");
            object(connection, BANK_ID, BANK_X, BANK_Y);
            state = connection.await(value -> value.interactions.bankOpen, 20000, "route to real bank chest");
            require(state.steps > 0, "Saving must follow real collision-checked Player movement");

            long previous = state.interactions.transactions;
            button(connection, 4, 517, 15, 0, 995); // Deposit ten coins from the stack.
            state = connection.awaitTransaction(previous);
            previous = state.interactions.transactions;
            button(connection, 3, 517, 15, 1, -1); // Depleted actor after Deposit-5 logs.
            state = connection.awaitTransaction(previous);
            require(total(state.interactions.bank, 995) == 10 && total(state.interactions.bank, 1511) == 5,
                    "Expected actual deposited coins and separate logs before saving");

            button(connection, 1, 517, 317, -1, -1);
            state = connection.await(value -> !value.interactions.bankOpen, 5000, "explicit bank close");
            previous = state.interactions.transactions;
            dragCoinsToLastSlot(connection);
            prepared = connection.awaitTransaction(previous);
            assertPrepared(prepared);
            object(connection, BANK_ID, BANK_X, BANK_Y);
            prepared = connection.await(value -> value.interactions.bankOpen, 5000, "reopen bank before disconnect");
            assertPrepared(prepared);
            require(prepared.x != start.getX() || prepared.y != start.getY(),
                    "Saved position must differ from the default login scene");
            require(prepared.unhandledFrames == 0, "Persistence inputs must use verified encrypted decoders");
        }
        Native950Save saved = new Native950SaveStore(directory.resolve("profiles")).load(PROFILE);
        assertSavedState(saved, prepared);
        System.out.println("PASS: disconnect saves real position, backpack slot order and separate bank quantities");

        // A fresh JVM proves the result is on disk rather than retained in a
        // Player, World singleton, store object, or static process cache.
        runRestartProcess(cache, directory.resolve("profiles"), prepared, directory.resolve("restart-check.log"));

        try (Connection connection = new Connection(world)) {
            connection.attach("  SAVE PROBE  ", new Native950SaveStore(directory.resolve("profiles")), scene(start), content);
            connection.assertInitialScene(prepared.x, prepared.y, prepared.plane);
            Native950Session.Snapshot restored = connection.snapshot();
            assertSavedState(saved, restored);
            assertPrepared(restored);
            require(restored.steps == 0 && restored.interactions.transactions == 0,
                    "New sessions must not restore movement queues or old transaction counters");
            require(!restored.interactions.bankOpen, "A saved bank must reopen closed until the chest is used again");
            System.out.println("PASS: canonical profile reconnect uses saved initial REBUILD and adds no starter items");
        }

        try (Connection connection = new Connection(world)) {
            connection.attach("other", new Native950SaveStore(directory.resolve("profiles")), scene(start), content);
            connection.assertInitialScene(start.getX(), start.getY(), start.getPlane());
            assertStarter(connection.snapshot());
        }
        assertSavedState(new Native950SaveStore(directory.resolve("profiles")).load(PROFILE), prepared);
        System.out.println("PASS: a different local profile starts independently without changing the saved character");

        verifyCorruptAdmission(world, content, start, directory.resolve("corrupt-probe"));
        verifyActiveSaveFailure(world, content, start, directory.resolve("failed-write-probe"));
        verifySchemaUpgradeAndSkills(world, content, start, directory.resolve("upgrade-probe"), cache, directory.resolve("skills-check.log"));
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty(), "Persistence smoke must release the single world slot");
        System.out.println("PASS: persistence smoke complete; isolated evidence retained at " + directory);
    }

    // ------------------------------------------------------------------ P4: schema 2 -> 3, skills

    private static final String UPGRADE_PROFILE = "upgrade";

    /**
     * A schema-2 profile (written with an independent encoder, never the current
     * one) is admitted, its quantities survive unchanged, its skills are the fresh
     * table, the first checkpoint rewrites it as schema 3 in place, a level set on
     * the world thread reaches the SKILLS section, and a fresh JVM restores it.
     *
     * <p>The levels set here include 27 Archaeology and 28 Necromancy, the two
     * stats the 947 cache adds, so the run proves the widened SKILLS section is
     * written, read back across a process boundary and restored onto a real
     * {@code Player} - not just that the arrays got longer.
     */
    private static void verifySchemaUpgradeAndSkills(Native950World world, Native950Content content, WorldTile start,
                                                     Path directory, Path cache, Path log) throws Exception {
        Native950SaveStore store = new Native950SaveStore(directory);
        try (Connection connection = new Connection(world)) {
            connection.attach(UPGRADE_PROFILE, store, scene(start), content);
            connection.assertInitialScene(start.getX(), start.getY(), start.getPlane());
            assertStarter(connection.snapshot());
        }
        Path file;
        try (Stream<Path> entries = Files.list(directory)) {
            List<Path> files = entries.filter(Files::isRegularFile).collect(Collectors.toList());
            require(files.size() == 1, "Expected one upgrade-probe save file");
            file = files.get(0);
        }
        int[] ids = new int[Native950Save.INVENTORY_SIZE], amounts = new int[Native950Save.INVENTORY_SIZE];
        Arrays.fill(ids, -1);
        ids[2] = 995; amounts[2] = 777;
        ids[5] = 1511; amounts[5] = 1;
        Native950Save legacy = new Native950Save(UPGRADE_PROFILE, start.getX() + 1, start.getY(), start.getPlane(),
                ids, amounts, new int[] {315, 1511}, new int[] {5, 4});
        byte[] schemaTwo = schemaTwoBytes(legacy);
        Files.write(file, schemaTwo);
        require(ByteBuffer.wrap(schemaTwo).getInt(8) == 2, "Fixture must be a schema-2 file");
        Native950Save loaded = store.load(UPGRADE_PROFILE);
        require(loaded.skills().equals(Native950Save.Skills.fresh()) && loaded.vitals().equals(Native950Save.Vitals.fresh()),
                "A schema-2 file must load with the default SKILLS and VITALS sections");
        require(Arrays.equals(schemaTwo, Files.readAllBytes(file)), "Loading must not rewrite the schema-2 file");

        Native950Session.Snapshot state;
        try (Connection connection = new Connection(world)) {
            connection.attach(UPGRADE_PROFILE, store, scene(start), content);
            connection.assertInitialScene(start.getX() + 1, start.getY(), start.getPlane());
            state = connection.await(value -> value.checkpoints >= 1 && value.ticks >= 1, 5000, "first schema-3 checkpoint");
            require(total(state.interactions.inventory, 995) == 777 && total(state.interactions.inventory, 1511) == 1
                            && total(state.interactions.bank, 315) == 5 && total(state.interactions.bank, 1511) == 4,
                    "Schema-2 quantities must survive the in-place upgrade: " + state);
            require(state.skillLevels[Skills.ATTACK] == 1 && state.skillLevels[Skills.HITPOINTS] == 10 && state.hitpoints == 100,
                    "A schema-2 profile starts with the fresh skill table: " + Arrays.toString(state.skillLevels));
            require(ByteBuffer.wrap(Files.readAllBytes(file)).getInt(8) == Native950Save.SCHEMA_VERSION,
                    "The first checkpoint must rewrite the profile as schema 3");
            Native950Save upgraded = new Native950SaveStore(directory).load(UPGRADE_PROFILE);
            require(Arrays.equals(upgraded.inventoryIds(), legacy.inventoryIds()) && Arrays.equals(upgraded.inventoryAmounts(), legacy.inventoryAmounts())
                            && Arrays.equals(upgraded.bankIds(), legacy.bankIds()) && Arrays.equals(upgraded.bankAmounts(), legacy.bankAmounts())
                            && upgraded.skills().equals(Native950Save.Skills.fresh()),
                    "The schema-3 rewrite must carry the schema-2 items and default skills");
            long checkpoints = state.checkpoints;
            // M3: level, xp and the three VITALS values change on the world thread
            // through the ordinary 910 setters. Each one now emits a verified packet
            // (UPDATE_STAT 66, VARBIT_SMALL/LARGE, UPDATE_RUNENERGY 116) through the
            // facade, so the run must still show facadeStrictHits == 0.
            world.execute(() -> {
                Player player = World.getPlayers().get(1);
                player.getSkills().set(Skills.ATTACK, 50);
                player.getSkills().setXp(Skills.ATTACK, 101333);
                // Prayer.restorePrayer caps at getLevelForXp(PRAYER) * 10, so raise the
                // level first; 50 gives a 500-point bar and a varbit value of 5000,
                // which only VARBIT_LARGE can carry.
                player.getSkills().setXp(Skills.PRAYER, 101333);
                player.getSkills().set(Skills.PRAYER, 50);
                player.getPrayer().setPrayerpoints(0);
                player.getPrayer().restorePrayer(500);
                player.setRunEnergyWithoutRefresh(100);
                player.setRunEnergy(64);
                player.setRun(true);
                // The full addXp path (multipliers, pet roll, level-up) needs the cache,
                // so it is exercised here rather than in the hermetic unit test.
                player.getSkills().addXp(Skills.COOKING, 100);
                // The two stats the 947 cache adds must persist like any other. The
                // experience values are the cache default curve's own level 50 and
                // level 99 rows (verified/ui/STAT_DEFINITIONS.md), and both stats are
                // capped at 120 there, so neither level is clamped away.
                player.getSkills().setXp(Skills.ARCHAEOLOGY, 101333);
                player.getSkills().set(Skills.ARCHAEOLOGY, 50);
                player.getSkills().setXp(Skills.NECROMANCY, 13034431);
                player.getSkills().set(Skills.NECROMANCY, 99);
                return null;
            }).get(5, TimeUnit.SECONDS);
            state = connection.await(value -> value.checkpoints > checkpoints, 5000, "SKILLS section checkpoint");
            require(state.skillLevels[Skills.ATTACK] == 50 && state.facadeStrictHits == 0 && state.tickFailures == 0,
                    "Skills.set must persist through the sectioned checkpoint without STRICT hits: " + state);
            require(state.skillXp[Skills.ATTACK] == 101333, "The snapshot must carry the changed experience: " + state.skillXp[Skills.ATTACK]);
            require(state.skillXp[Skills.COOKING] > 0, "Skills.addXp must award and store experience: " + state.skillXp[Skills.COOKING]);
            require(state.prayerPoints == 500 && state.runEnergy == 64 && state.running,
                    "VITALS must follow the 910 setters: prayer=" + state.prayerPoints
                            + " energy=" + state.runEnergy + " running=" + state.running);
            Long skillWrites = state.sectionWrites.get(Native950Save.Section.SKILLS);
            require(skillWrites != null && skillWrites >= 1, "The SKILLS section must be marked dirty by the level change: " + state.sectionWrites);
            Long vitalWrites = state.sectionWrites.get(Native950Save.Section.VITALS);
            require(vitalWrites != null && vitalWrites >= 1, "The VITALS section must be marked dirty by the vitals change: " + state.sectionWrites);
            Native950Save persisted = new Native950SaveStore(directory).load(UPGRADE_PROFILE);
            require(persisted.skills().level(Skills.ATTACK) == 50 && persisted.skills().xp(Skills.ATTACK) == 101333,
                    "The level and xp must be on disk before disconnect");
            require(persisted.skills().xp(Skills.COOKING) > 0, "The addXp award must be on disk before disconnect");
            require(persisted.skills().level(Skills.ARCHAEOLOGY) == 50 && persisted.skills().xp(Skills.ARCHAEOLOGY) == 101333,
                    "Archaeology must be on disk before disconnect: level " + persisted.skills().level(Skills.ARCHAEOLOGY)
                            + " xp " + persisted.skills().xp(Skills.ARCHAEOLOGY));
            require(persisted.skills().level(Skills.NECROMANCY) == 99 && persisted.skills().xp(Skills.NECROMANCY) == 13034431,
                    "Necromancy must be on disk before disconnect: level " + persisted.skills().level(Skills.NECROMANCY)
                            + " xp " + persisted.skills().xp(Skills.NECROMANCY));
            require(persisted.vitals().prayerPoints == 500 && persisted.vitals().runEnergy == 64 && persisted.vitals().running,
                    "The vitals must be on disk before disconnect: " + persisted.vitals().prayerPoints
                            + "/" + persisted.vitals().runEnergy + "/" + persisted.vitals().running);
        }
        require(state.checkpoints >= 2, "Expected at least the upgrade and the skills checkpoints");
        System.out.println("PASS: schema-2 profile upgraded in place to schema 3 with quantities preserved, fresh skills, then a persisted level");

        String executable = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win") ? "java.exe" : "java";
        Path java = Paths.get(System.getProperty("java.home"), "bin", executable);
        Process child = new ProcessBuilder(java.toString(), "-Xmx2g", "-cp", System.getProperty("java.class.path"),
                Native950PersistenceSmoke.class.getName(), "--skills-check", cache.toString(), directory.toString(), "50")
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        boolean finished = false;
        try {
            finished = child.waitFor(60, TimeUnit.SECONDS);
            require(finished, "Fresh-JVM skills probe exceeded 60 seconds; inspect " + log);
            require(child.exitValue() == 0, "Fresh-JVM skills probe failed; inspect " + log);
            String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            require(output.contains("PASS: fresh JVM restores skills"), "Fresh-JVM skills marker missing; inspect " + log);
            System.out.println("PASS: an independent JVM restored the persisted level (" + log + ")");
        } finally {
            if (!finished) { child.destroyForcibly(); child.waitFor(5, TimeUnit.SECONDS); }
        }
    }

    private static void skillsCheck(Path cache, Path directory, int expectedAttack) throws Exception {
        Cache.initFlatReadOnly(cache);
        WorldTile defaultStart = findApproachStart();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save saved = store.load(UPGRADE_PROFILE);
        require(saved != null && saved.skills().level(Skills.ATTACK) == expectedAttack, "Fresh process must read the persisted level");
        require(saved.skills().xp(Skills.ATTACK) == 101333, "Fresh process must read the persisted experience: " + saved.skills().xp(Skills.ATTACK));
        require(saved.skills().xp(Skills.COOKING) > 0, "Fresh process must read the addXp award");
        // The two stats the 947 cache adds survive a JVM boundary like any other.
        require(saved.skills().level(Skills.ARCHAEOLOGY) == 50 && saved.skills().xp(Skills.ARCHAEOLOGY) == 101333,
                "Fresh process must read the persisted Archaeology: level " + saved.skills().level(Skills.ARCHAEOLOGY)
                        + " xp " + saved.skills().xp(Skills.ARCHAEOLOGY));
        require(saved.skills().level(Skills.NECROMANCY) == 99 && saved.skills().xp(Skills.NECROMANCY) == 13034431,
                "Fresh process must read the persisted Necromancy: level " + saved.skills().level(Skills.NECROMANCY)
                        + " xp " + saved.skills().xp(Skills.NECROMANCY));
        require(saved.vitals().prayerPoints == 500 && saved.vitals().runEnergy == 64 && saved.vitals().running,
                "Fresh process must read the persisted vitals");
        Native950World world = Native950World.getInstance();
        try (Connection connection = new Connection(world)) {
            connection.attach(UPGRADE_PROFILE, store, scene(defaultStart), content());
            Native950Session.Snapshot state = connection.await(value -> value.ticks >= 1, 5000, "restored session tick");
            require(state.skillLevels[Skills.ATTACK] == expectedAttack && state.skillLevels[Skills.HITPOINTS] == 10,
                    "The restored Player must carry the persisted level: " + Arrays.toString(state.skillLevels));
            require(state.skillXp[Skills.ATTACK] == 101333,
                    "The restored Player must carry the persisted experience: " + state.skillXp[Skills.ATTACK]);
            require(state.skillXp[Skills.COOKING] > 0,
                    "The restored Player must carry the addXp award: " + state.skillXp[Skills.COOKING]);
            require(state.skillLevels.length == Native950Save.SKILL_COUNT,
                    "The restored Player must model every stat the cache defines, saw " + state.skillLevels.length);
            require(state.skillLevels[Skills.ARCHAEOLOGY] == 50 && state.skillXp[Skills.ARCHAEOLOGY] == 101333,
                    "The restored Player must carry Archaeology: level " + state.skillLevels[Skills.ARCHAEOLOGY]
                            + " xp " + state.skillXp[Skills.ARCHAEOLOGY]);
            require(state.skillLevels[Skills.NECROMANCY] == 99 && state.skillXp[Skills.NECROMANCY] == 13034431,
                    "The restored Player must carry Necromancy: level " + state.skillLevels[Skills.NECROMANCY]
                            + " xp " + state.skillXp[Skills.NECROMANCY]);
            require(state.prayerPoints == 500 && state.runEnergy == 64 && state.running,
                    "The restored Player must carry the persisted vitals: prayer=" + state.prayerPoints
                            + " energy=" + state.runEnergy + " running=" + state.running);
            // M3: the same restored state must have been pushed to the client on login.
            Long statFrames = state.sentByMethod.get("sendSkillLevel");
            require(statFrames != null && statFrames >= Native950Save.SKILL_COUNT,
                    "The login burst must emit one UPDATE_STAT per modelled skill, saw " + statFrames);
            require(state.tickFailures == 0 && state.facadeStrictHits == 0, "Restore must not produce tick failures or STRICT hits: " + state);
            System.out.println("[Ataraxia950] restored login burst: UPDATE_STAT frames=" + statFrames
                    + " strictHits=" + state.facadeStrictHits + " tickFailures=" + state.tickFailures);
        }
        System.out.println("PASS: fresh JVM restores skills from the schema-3 SKILLS section");
    }

    /** Independent schema-2 encoder (the pre-P4 layout); never the store's current encoder. */
    private static byte[] schemaTwoBytes(Native950Save save) throws Exception {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (java.io.DataOutputStream data = new java.io.DataOutputStream(bytes)) {
            data.write(new byte[] {'A', 'N', 'X', 'T', '9', '4', '7', '\n'});
            data.writeInt(2); data.writeInt(947);
            byte[] name = save.username().getBytes(StandardCharsets.US_ASCII);
            data.writeInt(name.length); data.write(name);
            data.writeInt(save.x()); data.writeInt(save.y()); data.writeInt(save.plane());
            for (int[][] items : new int[][][] {{save.inventoryIds(), save.inventoryAmounts()}, {save.bankIds(), save.bankAmounts()},
                    {save.equipmentIds(), save.equipmentAmounts()}}) {
                data.writeInt(items[0].length);
                for (int i = 0; i < items[0].length; i++) { data.writeInt(items[0][i]); data.writeInt(items[1][i]); }
            }
            data.writeByte(save.equipmentKitClaimed() ? 1 : 0);
        }
        byte[] body = bytes.toByteArray();
        bytes.write(java.security.MessageDigest.getInstance("SHA-256").digest(body));
        return bytes.toByteArray();
    }

    private static void restartCheck(Path cache, Path directory, int x, int y, int plane) throws Exception {
        Cache.initFlatReadOnly(cache);
        WorldTile defaultStart = findApproachStart();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save saved = store.load(PROFILE);
        require(saved != null && saved.x() == x && saved.y() == y && saved.plane() == plane,
                "Fresh process must read the previous process's persisted position");
        Native950World world = Native950World.getInstance();
        try (Connection connection = new Connection(world)) {
            connection.attach(PROFILE, store, scene(defaultStart), content());
            connection.assertInitialScene(x, y, plane);
            Native950Session.Snapshot state = connection.snapshot();
            assertSavedState(saved, state);
            assertPrepared(state);
            require(!state.interactions.bankOpen && state.steps == 0, "Fresh JVM cannot resume an open UI or movement queue");
        }
        System.out.println("PASS: fresh JVM restores saved character and initial encrypted scene without duplication");
    }

    private static void runRestartProcess(Path cache, Path directory, Native950Session.Snapshot state, Path log) throws Exception {
        String executable = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win") ? "java.exe" : "java";
        Path java = Paths.get(System.getProperty("java.home"), "bin", executable);
        Process child = new ProcessBuilder(java.toString(), "-Xmx2g", "-cp", System.getProperty("java.class.path"),
                Native950PersistenceSmoke.class.getName(), "--restart-check", cache.toString(), directory.toString(),
                Integer.toString(state.x), Integer.toString(state.y), Integer.toString(state.plane))
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        boolean finished = false;
        try {
            finished = child.waitFor(45, TimeUnit.SECONDS);
            require(finished, "Fresh-JVM persistence probe exceeded 45 seconds; inspect " + log);
            require(child.exitValue() == 0, "Fresh-JVM persistence probe failed; inspect " + log);
            String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            require(output.contains("PASS: fresh JVM restores saved character"), "Fresh-JVM success marker missing; inspect " + log);
            System.out.println("PASS: an independent JVM restored the same profile (" + log + ")");
        } finally {
            if (!finished) { child.destroyForcibly(); child.waitFor(5, TimeUnit.SECONDS); }
        }
    }

    private static void verifyCorruptAdmission(Native950World world, Native950Content content, WorldTile start, Path directory) throws Exception {
        try (Connection connection = new Connection(world)) {
            connection.attach("corrupt", new Native950SaveStore(directory), scene(start), content);
            connection.assertInitialScene(start.getX(), start.getY(), start.getPlane());
            assertStarter(connection.snapshot());
        }
        List<Path> files;
        try (Stream<Path> entries = Files.list(directory)) {
            files = entries.filter(Files::isRegularFile).collect(Collectors.toList());
        }
        require(files.size() == 1, "Expected exactly one isolated corrupt-probe save file");
        Path file = files.get(0);
        byte[] invalid = "intentionally invalid isolated 947 smoke save".getBytes(StandardCharsets.UTF_8);
        Files.write(file, invalid);
        try (Connection connection = new Connection(world)) {
            CompletableFuture<Native950Session> admitted = world.attach(connection.channel, "corrupt",
                    connection.incoming, connection.outgoing, new byte[] {0}, scene(start), Collections.emptyList(),
                    content, new Native950SaveStore(directory));
            try {
                connection.awaitFuture(admitted);
                throw new AssertionError("A corrupt save must refuse world admission");
            } catch (ExecutionException expected) {
                Throwable cause = expected.getCause();
                while (cause != null && !(cause instanceof IOException)) cause = cause.getCause();
                require(cause instanceof IOException, "Corrupt-save admission must report the storage failure");
            }
            require(Arrays.equals(invalid, Files.readAllBytes(file)), "Rejected corrupt save must never be overwritten by fresh supplies");
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty(), "Failed save load must release admission and Player state");
        try (Connection connection = new Connection(world)) {
            connection.attach("recovery", new Native950SaveStore(directory), scene(start), content);
            connection.assertInitialScene(start.getX(), start.getY(), start.getPlane());
            assertStarter(connection.snapshot());
        }
        require(Arrays.equals(invalid, Files.readAllBytes(file)), "A later valid profile must preserve the corrupt file for recovery");
        System.out.println("PASS: corrupt saves fail closed without overwrite; subsequent profile admission succeeds");
    }

    private static void verifyActiveSaveFailure(Native950World world, Native950Content content, WorldTile start, Path directory) throws Exception {
        Path file;
        byte[] invalid = "intentionally damaged active smoke save".getBytes(StandardCharsets.UTF_8);
        try (Connection connection = new Connection(world)) {
            connection.attach("writefail", new Native950SaveStore(directory), scene(start), content);
            connection.assertInitialScene(start.getX(), start.getY(), start.getPlane());
            assertStarter(connection.await(value -> value.ticks >= 1, 5000, "first completed tick before write failure"));
            connection.pump();
            connection.observedOpcodes.clear();
            List<Path> files;
            try (Stream<Path> entries = Files.list(directory)) {
                files = entries.filter(Files::isRegularFile).collect(Collectors.toList());
            }
            require(files.size() == 1, "Expected one isolated active-session save file");
            file = files.get(0);
            Files.write(file, invalid);
            // The item move is valid; only the refused durable checkpoint may
            // close this session. Existing durable bytes must remain untouched.
            dragCoinsToLastSlot(connection);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (connection.channel.isActive() && System.nanoTime() < deadline) {
                connection.pump(); Thread.sleep(20);
            }
            require(!connection.channel.isActive(), "A failed mutation checkpoint must disconnect instead of continuing unsaved play");
            connection.pump();
            boolean mutationOutput = false;
            for (int opcode : connection.observedOpcodes) {
                if (opcode == ServerPacket.UPDATE_INV_FULL.opcode()) mutationOutput = true;
                require(!mutationOutput || opcode != ServerPacket.SERVER_TICK_END.opcode(),
                        "A failed checkpoint must not finish a tick that publishes the inventory mutation");
            }
            require(Arrays.equals(invalid, Files.readAllBytes(file)), "Failed checkpoint must preserve the existing damaged file");
        }
        require(Arrays.equals(invalid, Files.readAllBytes(file)), "Session cleanup must not retry overwriting a refused save");
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty(), "A failed active-session save must release the world slot");
        System.out.println("PASS: active-session checkpoint failure disconnects before mutation tick completion and preserves the file");
    }

    private static Native950World.SceneConfig scene(WorldTile tile) {
        return new Native950World.SceneConfig(tile.getX(), tile.getY(), tile.getPlane(), 1, 7, 0, 0, 0);
    }

    private static void assertSavedState(Native950Save save, Native950Session.Snapshot state) {
        require(save != null, "Expected a durable local save");
        require(save.username().equals(Native950Save.canonicalUsername(PROFILE)), "Save must use the canonical local profile identity");
        require(save.x() == state.x && save.y() == state.y && save.plane() == state.plane, "Save must preserve the exact authoritative position");
        require(Arrays.equals(save.inventoryIds(), state.interactions.inventory.ids)
                        && Arrays.equals(save.inventoryAmounts(), state.interactions.inventory.amounts),
                "Save must preserve all 28 backpack slots and amounts exactly");
        int[] ids = save.bankIds(), amounts = save.bankAmounts();
        require(ids.length == amounts.length && ids.length <= state.interactions.bank.ids.length, "Saved compact bank shape differs");
        for (int slot = 0; slot < state.interactions.bank.ids.length; slot++) {
            int id = slot < ids.length ? ids[slot] : -1, amount = slot < amounts.length ? amounts[slot] : 0;
            require(id == state.interactions.bank.ids[slot] && amount == state.interactions.bank.amounts[slot],
                    "Save must preserve bank slot " + slot + " and trailing empties");
        }
    }

    private static void assertStarter(Native950Session.Snapshot state) {
        assertTotals(state);
        require(!state.interactions.bankOpen && state.interactions.inventory.ids[0] == 995
                        && state.interactions.inventory.amounts[0] == 1000,
                "New profiles must receive supplies in the initial backpack layout");
        for (int slot = 0; slot < state.interactions.bank.ids.length; slot++)
            require(state.interactions.bank.ids[slot] == -1 && state.interactions.bank.amounts[slot] == 0,
                    "A new profile must not inherit another character's bank");
    }

    private static void assertPrepared(Native950Session.Snapshot state) {
        assertTotals(state);
        require(state.interactions.inventory.ids[0] == -1 && state.interactions.inventory.ids[27] == 995
                        && state.interactions.inventory.amounts[27] == 990,
                "Saved backpack layout must keep the remaining coin stack in slot 27");
        require(total(state.interactions.inventory, 1511) == 0 && total(state.interactions.inventory, 315) == 5
                        && total(state.interactions.bank, 995) == 10 && total(state.interactions.bank, 1511) == 5
                        && total(state.interactions.bank, 315) == 0,
                "Saved bank/backpack split must preserve every deposited item without reseeding");
    }

    private static void assertTotals(Native950Session.Snapshot state) {
        int[] ids = {995, 1511, 315}; long[] amounts = {1000, 5, 5};
        for (int i = 0; i < ids.length; i++)
            require(total(state.interactions.inventory, ids[i]) + total(state.interactions.bank, ids[i]) == amounts[i],
                    "Persistence must conserve total quantity for item " + ids[i]);
    }

    private static long total(Native950Containers.Snapshot state, int id) {
        long result = 0;
        for (int slot = 0; slot < state.ids.length; slot++) if (state.ids[slot] == id) result += state.amounts[slot];
        return result;
    }

    private static WorldTile findApproachStart() {
        WorldObject bank = null;
        for (WorldObject object : World.getRegion(new WorldTile(BANK_X, BANK_Y, 0).getRegionId(), true).getObjects().values())
            if (object.getId() == BANK_ID && object.getX() == BANK_X && object.getY() == BANK_Y && object.getPlane() == 0) bank = object;
        require(bank != null && bank.getDefinitions().options != null && bank.getDefinitions().options.length > 1
                        && "Use".equals(bank.getDefinitions().options[1]), "Expected the pinned cache bank chest and Use option");
        for (int radius = 4; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) for (int dy = -radius; dy <= radius; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
                int x = BANK_X + dx, y = BANK_Y + dy;
                World.getRegion(new WorldTile(x, y, 0).getRegionId(), true);
                if ((World.getMask(0, x, y) & (Flags.OBJ | Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK)) != 0) continue;
                if (RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, x, y, 0, 1, new ObjectStrategy(bank), false) > 0
                        && !RouteFinder.lastIsAlternative()) return new WorldTile(x, y, 0);
            }
        }
        throw new AssertionError("No real collision-safe approach to the verified bank chest");
    }

    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] deposits = new int[11], withdrawals = new int[11];
        deposits[1] = withdrawals[1] = 1; deposits[2] = withdrawals[2] = 1;
        deposits[3] = withdrawals[3] = 5; deposits[4] = withdrawals[4] = 10;
        deposits[7] = withdrawals[7] = Integer.MAX_VALUE;
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                deposits, withdrawals, Collections.emptyList(), Collections.emptyList()));
    }

    private static void object(Connection connection, int id, int x, int y) {
        input(connection, 11, new byte[] {(byte) (x >>> 8), (byte) (x + 128), 0, (byte) y, (byte) (y >>> 8),
                (byte) (id >>> 16), (byte) (id >>> 24), (byte) id, (byte) (id >>> 8)});
    }

    private static void button(Connection connection, int option, int iface, int component, int slot, int item) {
        int[] opcodes = {96, 77, 4, 95, 29, 51, 5, 21, 18, 36};
        int hash = (iface << 16) | component;
        input(connection, opcodes[option - 1], new byte[] {(byte) (item >>> 8), (byte) item,
                (byte) (hash >>> 8), (byte) hash, (byte) (hash >>> 24), (byte) (hash >>> 16),
                (byte) (slot >>> 8), (byte) slot});
    }

    private static void dragCoinsToLastSlot(Connection connection) {
        int hash = BACKPACK_HASH;
        // Native onDrag has already swapped actor IDs when it sends: the source
        // claims empty and target claims coins. The server resolves prior state.
        input(connection, 40, new byte[] {(byte) 255, (byte) 127,
                (byte) hash, (byte) (hash >>> 8), (byte) (hash >>> 16), (byte) (hash >>> 24),
                (byte) (hash >>> 24), (byte) (hash >>> 16), (byte) (hash >>> 8), (byte) hash,
                (byte) (995 + 128), (byte) (995 >>> 8), 27, 0, 0, 0});
    }

    private static void input(Connection connection, int opcode, byte[] payload) {
        require(connection.channel.isActive(), "Encrypted input requires an active local session");
        connection.channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) (opcode + connection.client.getAsInt())}));
        connection.channel.writeInbound(Unpooled.wrappedBuffer(payload));
    }

    private static final class Connection implements AutoCloseable {
        private final Native950World world;
        private final EmbeddedChannel channel = new EmbeddedChannel();
        private final Native950Isaac client = new Native950Isaac(new int[] {947, 3, 2026, 947});
        private final Native950Isaac incoming = new Native950Isaac(new int[] {947, 3, 2026, 947});
        private final Native950Isaac outgoing = new Native950Isaac(new int[] {997, 53, 2076, 997});
        private final Native950Isaac expectedOutput = new Native950Isaac(new int[] {997, 53, 2076, 997});
        private final List<Integer> observedOpcodes = new ArrayList<Integer>();

        Connection(Native950World world) {
            this.world = world;
            for (int i = 0; i < 3; i++) { client.getAsInt(); incoming.getAsInt(); }
            for (int i = 0; i < 2; i++) { outgoing.getAsInt(); expectedOutput.getAsInt(); }
        }

        void attach(String name, Native950SaveStore store, Native950World.SceneConfig scene, Native950Content content) throws Exception {
            awaitFuture(world.attach(channel, name, incoming, outgoing, new byte[] {0}, scene,
                    Collections.emptyList(), content, store));
        }

        void awaitFuture(CompletableFuture<?> future) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!future.isDone() && System.nanoTime() < deadline) { channel.runPendingTasks(); Thread.sleep(20); }
            future.get(1, TimeUnit.SECONDS);
        }

        void assertInitialScene(int x, int y, int plane) {
            Object frame = channel.readOutbound();
            require(frame instanceof ByteBuf, "Expected initial encrypted REBUILD packet before other frames");
            try {
                ByteBuf buffer = (ByteBuf) frame;
                byte[] actual = new byte[buffer.readableBytes()]; buffer.readBytes(actual);
                require(Arrays.equals(actual, Native950Packets.initialSinglePlayerScene(1, x, y, plane, 7, 0, 0, 0).frame(expectedOutput)),
                        "Initial encrypted REBUILD must use authoritative saved position and the live cipher stream");
            } finally { ReferenceCountUtil.release(frame); }
        }

        Native950Session.Snapshot snapshot() throws Exception {
            pump();
            Native950Session.Snapshot state = world.snapshot().get(2, TimeUnit.SECONDS);
            require(state != null && state.active && state.interactions != null, "Expected live persistence-enabled interaction session");
            return state;
        }

        Native950Session.Snapshot await(Predicate<Native950Session.Snapshot> condition, long timeoutMs, String description) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
            Native950Session.Snapshot state;
            do {
                state = snapshot();
                if (condition.test(state)) return state;
                Thread.sleep(20);
            } while (System.nanoTime() < deadline);
            throw new AssertionError("Timed out: " + description + "; last state=" + state);
        }

        Native950Session.Snapshot awaitTransaction(long previous) throws Exception {
            Native950Session.Snapshot state = await(value -> value.interactions.transactions > previous, 5000, "container transaction");
            require(state.interactions.transactions == previous + 1, "One encrypted input must produce one container transaction");
            assertTotals(state);
            return state;
        }

        private void pump() {
            channel.runPendingTasks();
            Object frame;
            while ((frame = channel.readOutbound()) != null) {
                try {
                    require(frame instanceof ByteBuf, "Expected an encrypted native output frame");
                    ByteBuf bytes = (ByteBuf) frame;
                    int opcode = (bytes.readUnsignedByte() - expectedOutput.getAsInt()) & 255;
                    if (opcode >= 128) opcode = ((opcode - 128) << 8) | ((bytes.readUnsignedByte() - expectedOutput.getAsInt()) & 255);
                    observedOpcodes.add(opcode);
                } finally { ReferenceCountUtil.release(frame); }
            }
            channel.checkException();
        }

        @Override public void close() throws Exception {
            try {
                channel.close();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (world.reservedSlots() > 0 && System.nanoTime() < deadline) { pump(); Thread.sleep(20); }
                require(world.reservedSlots() == 0 && World.getPlayers().isEmpty(), "Disconnect must save and release the real Player within five seconds");
                require(world.snapshot().get(2, TimeUnit.SECONDS) == null, "Disconnected session must not remain in the world");
            } finally { channel.finishAndReleaseAll(); }
        }
    }

    private static void require(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }

    private Native950PersistenceSmoke() { }
}
