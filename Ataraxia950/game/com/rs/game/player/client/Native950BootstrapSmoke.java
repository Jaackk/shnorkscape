package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DataPaths;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import com.rs.utils.data.parsers.npcs.NPCSpawnsDataParser;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Opt-in real-cache probe for {@link Native950Bootstrap}. Initialises the flat
 * read-only cache exactly like the other Native950 smokes, runs the bootstrap
 * against the staged {@code Ataraxia950/data} tree and exits non-zero when any
 * initialiser failed or a headline table (NPC spawns, shops, item examines)
 * came back empty. It never starts the native world thread, opens no socket
 * and writes nothing.
 *
 * <p>Usage: {@code Native950BootstrapSmoke <flat-cache-directory> [data-root]}.
 * The data root argument (default: {@code DataPaths.root()}, i.e.
 * {@code -Dataraxia950.data} or {@code <cwd>/data}) is installed into
 * {@code DataPaths} by the bootstrap, so the JVM may start in any working
 * directory; the smoke prints which root was used and how it was resolved.
 */
public final class Native950BootstrapSmoke {

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2)
            throw new IllegalArgumentException("Usage: Native950BootstrapSmoke <flat-cache-directory> [data-root]");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Path dataRoot;
        try {
            dataRoot = args.length == 2 ? Paths.get(args[1]).toAbsolutePath().normalize() : Native950Bootstrap.resolveDataRoot();
        } catch (IllegalStateException unresolved) {
            // Fail closed with the actionable DataPaths message instead of a stack trace.
            System.out.println("[Ataraxia950] bootstrap smoke FAILED: " + unresolved.getMessage());
            System.exit(1);
            return;
        }
        System.out.println("[Ataraxia950] bootstrap smoke: cache=" + args[0] + " dataRoot=" + dataRoot
                + " workingDirectory=" + Paths.get("").toAbsolutePath());
        int exit = 0;
        try {
            Native950Bootstrap.Report report = Native950Bootstrap.run(dataRoot);
            System.out.println(report.format());
            System.out.println("[Ataraxia950] data root: " + report.dataRoot + " [" + report.dataRootSource + "] DataPaths.root()=" + DataPaths.root());
            require(report.dataRoot.equals(DataPaths.root()), "Bootstrap root and DataPaths.root() disagree");
            int spawns = countNpcSpawns();
            int shops = Native950Bootstrap.tableSize(ShopsDataParser.class, "SHOPS") / 2;
            int examines = Native950Bootstrap.tableSize(ItemExaminesDataParser.class, "ITEM_EXAMINES");
            System.out.println("[Ataraxia950] tables: npcSpawns=" + spawns + " shops=" + shops + " itemExamines=" + examines);
            for (Native950Bootstrap.Entry entry : report.skipped())
                System.out.println("[Ataraxia950] skipped: " + entry.name + " - " + entry.reason);
            require(report.dataRootUsable, "Data root is not usable: " + report.dataRoot + " must hold " + DataPaths.MARKER_FILE
                    + " (stage Ataraxia950/data or pass -D" + DataPaths.PROPERTY + ")");
            require(report.allOk(), "Bootstrap entries failed: " + names(report.failures()));
            // Every skip must name the milestone that unblocks it (P0 wheel, M2b task tab, M3 prayer).
            for (Native950Bootstrap.Entry entry : report.skipped())
                require(entry.reason != null && entry.reason.startsWith("requires-"), "Unexpected skip: " + entry);
            // M3: the two prayer-touching tasks are queued again (Prayer is hydrated by
            // Player.hydrateForNative950 and both packets they need are verified). The task
            // tab still writes unverified interfaces 635/930 every 600 ms, so it stays skipped.
            for (String task : new String[] {"World.addRefreshTargetBuffsTask"}) {
                Native950Bootstrap.Entry entry = report.entry(task);
                require(entry != null && entry.status == Native950Bootstrap.Status.SKIPPED, task + " must be skipped in this phase: " + entry);
            }
            for (String task : new String[] {"World.addDrainPrayerTask", "World.addRestoreSkillsTask"}) {
                Native950Bootstrap.Entry entry = report.entry(task);
                require(entry != null && entry.status == Native950Bootstrap.Status.OK, task + " must be queued from M3 on: " + entry);
            }
            require(WorldTasksManager.getTasksCount() == 6, "Expected exactly 6 queued world tasks, saw " + WorldTasksManager.getTasksCount());
            require(spawns > 0, "No NPC spawns loaded");
            require(shops > 0, "No shops loaded");
            require(examines > 0, "No item examines loaded");
            Native950Bootstrap.Report again = Native950Bootstrap.run(dataRoot);
            require(again == report, "Bootstrap must be idempotent");
            require(countNpcSpawns() == spawns, "Second run must not reload NPC spawns");
            System.out.println("[Ataraxia950] bootstrap smoke passed: ok=" + report.count(Native950Bootstrap.Status.OK)
                    + " skipped(requires-*)=" + report.count(Native950Bootstrap.Status.SKIPPED));
        } catch (Throwable failure) {
            failure.printStackTrace(System.out);
            System.out.println("[Ataraxia950] bootstrap smoke FAILED: " + failure.getMessage());
            exit = 1;
        }
        System.exit(exit);
    }

    /** Total NPC templates across the per-region lists kept by the spawns parser. */
    private static int countNpcSpawns() throws ReflectiveOperationException {
        Field field = NPCSpawnsDataParser.class.getDeclaredField("NPC_SPAWNS");
        field.setAccessible(true);
        Map<?, ?> byRegion = (Map<?, ?>) field.get(null);
        int total = 0;
        for (Object value : byRegion.values()) {
            @SuppressWarnings("unchecked")
            List<NPC> npcs = (List<NPC>) value;
            total += npcs.size();
        }
        return total;
    }

    private static String names(List<Native950Bootstrap.Entry> entries) {
        StringBuilder out = new StringBuilder();
        for (Native950Bootstrap.Entry entry : entries) {
            if (out.length() > 0) out.append(", ");
            out.append(entry.name);
        }
        return out.toString();
    }

    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    private Native950BootstrapSmoke() { }
}
