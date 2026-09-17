package com.rs.game.player.client;

import com.rs.Scanner;
import com.rs.cache.Cache;
import com.rs.cache.loaders.WorldAreaTypeDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.npc.combat.CombatScriptsHandler;
import com.rs.game.player.ActionBar;
import com.rs.game.player.commands.CommandHandler;
import com.rs.game.player.content.FishingSpotsHandler;
import com.rs.game.player.controllers.ControllerHandler;
import com.rs.game.player.cutscenes.CutscenesHandler;
import com.rs.game.player.tt.TTAllRewards;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DataPaths;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.items.ItemDisassembleDataParser;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.items.ItemWeightsDataParser;
import com.rs.utils.data.parsers.items.TreasureHunterRewardParser;
import com.rs.utils.data.parsers.maps.CustomObjectSpawnsDataParser;
import com.rs.utils.data.parsers.misc.MusicHintsDataParser;
import com.rs.utils.data.parsers.misc.PerkGenerationDataParser;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.NPCExaminesDataParser;
import com.rs.utils.data.parsers.npcs.NPCSpawnsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import com.rs.utils.data.parsers.npcs.NPCWeaknessesDataParser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Explicit, ordered, idempotent replacement for the parts of
 * {@code ServerLauncher.init()} that the native 947 JVM needs. The legacy
 * launcher (and the {@code WorldThread} it starts) must never run next to
 * {@link Native950World}, so nothing here is discovered or implicit: every
 * initialiser is one named entry that runs inside its own try/catch and is
 * recorded in the returned {@link Report} with its status, elapsed time and
 * reason. A failed entry never aborts the run; the caller decides what a
 * failure means (the smoke exits non-zero, the P4 handoff refuses admission).
 *
 * <p>Ordering follows {@code ServerLauncher.init()} except that the NPC
 * definition tables (weaknesses, combat definitions, stats, drops, examines)
 * are loaded BEFORE {@code NPCSpawnsDataParser.init()}. The launcher loads the
 * spawns first, so its NPC templates are built with weakness 0 and the default
 * combat definition; live spawns re-read both at spawn time, so the visible
 * behaviour is unchanged, but the 947 path does not repeat the quirk.
 *
 * <p>Data files: the parsers resolve every file through
 * {@link DataPaths#root()}, so the 947 JVM (OpenNXT, working directory
 * {@code OpenNXT/}) points at the staged {@code Ataraxia950/data} tree with
 * {@code -Dataraxia950.data=...}; no working-directory constraint remains.
 * {@link #run(Path)} installs the root it is given into {@link DataPaths} so
 * the bootstrap and the parsers cannot disagree. A data-backed entry only runs
 * when the root holds {@value DataPaths#MARKER_FILE} and the file it reads
 * exists; otherwise it fails closed with a reason instead of letting a parser
 * NPE on a null JSON array.
 *
 * <p>Two layers: the static facade ({@link #run()}, {@link #run(Path)},
 * {@link #lastReport()}) is the JVM-wide single-shot entry point that P4 wires
 * into the world thread and that requires the flat cache; the {@link Session}
 * underneath owns one ordered {@link Initialiser} list and its single report,
 * so the unit tests drive a session over fake entries with no cache at all.
 *
 * <p>Excluded from the 947 bootstrap on purpose (see notes/P8-bootstrap.md):
 * {@code AccountPin.DISABLED}, {@code AutoBackup}, {@code SQLThread},
 * {@code Cache.init} (replaced by {@code Cache.initFlatReadOnly}),
 * {@code ItemsEquipIds}, {@code Huffman}, {@code NXTClientsManager},
 * {@code ChargesDatabase}, {@code DisplayNames}, {@code BodyDefinitions},
 * {@code CosmeticsManager}, {@code IPBanL}, {@code IPMute},
 * {@code TelosEnrageRanks}, {@code LoggingSqlManager}, {@code GrandExchange},
 * {@code WorldRepository}, {@code ClansManager}, {@code TriviaBot},
 * {@code AgilityManager}, {@code FriendChatsManager}, {@code CoresManager.init},
 * every activity initialiser inside {@code World.init}, {@code EliteDungeon},
 * {@code WellOfGoodWill}, {@code ActivitiesScheduler}, {@code MapBuilder},
 * {@code NetworkBootstrapper}, {@code LendingManager}, the accounts saving /
 * clean memory / recalculate prices tasks, {@code BossInstanceHandler},
 * {@code AraxxorManager}, {@code StarterMap}, {@code GetRichestBanksSql},
 * {@code SiphonActionNodes}, {@code ObjectSpawns.addCustomSpawns},
 * {@code FactionManager}, {@code Lottery}, {@code GIM}, {@code EvilTreeHandler},
 * skilling contracts, {@code FlowerGirlD}, {@code SeasonalEventManager},
 * {@code JadinkoManager} and {@code populateStartupBots}.
 */
public final class Native950Bootstrap {

    /** System property naming the data root; same key as {@link DataPaths#PROPERTY}. */
    public static final String DATA_ROOT_PROPERTY = DataPaths.PROPERTY;

    public enum Status { OK, FAILED, SKIPPED }

    /** What an initialiser needs beyond the flat cache and the data files. */
    public enum Requirement {
        NONE(null),
        /** Needs CoresManager's ServiceProvider; P0's tick wheel provides it once Native950World exists. */
        WORLD_SCHEDULER("requires-P0: CoresManager.getServiceProvider() is null in the 947 JVM"),
        /**
         * Historic gate for {@code World.addDrainPrayerTask} and
         * {@code World.addRestoreSkillsTask}: both dereference
         * {@code player.getPrayer()} for every world player, and
         * {@code Player.createNative950} used to construct none.
         *
         * <p><b>Lifted in M3.</b> {@code Player.hydrateForNative950} now builds and
         * wires a {@link com.rs.game.player.Prayer} and
         * {@code requireNative950TickManagers} refuses admission without one, and
         * both packets the tasks need are verified (UPDATE_STAT 66 and
         * VARBIT_LARGE 71). The two initialisers therefore carry
         * {@link #NONE} again. The constant is kept because it is still the right
         * gate for any future initialiser that needs a per-player manager the
         * native admission does not own, and because it is the fixture the
         * bootstrap unit tests use for a never-satisfied requirement.
         */
        NATIVE_PRAYER("requires-M3: an initialiser that needs a per-player manager native admission does not build"),
        /**
         * {@code World.addRefreshTargetBuffsTask} calls {@code TaskTab.sendTab} for every
         * active player every 600 ms; on a native player that is three interface-text
         * writes per tick through the facade to interfaces (635/930) no M2b binding has
         * verified. Skipped until the M2b interface set includes the task tab.
         */
        NATIVE_TASK_TAB("requires-M2b: TaskTab.sendTab would write unverified interfaces 635/930 through the facade every tick");

        /** Skip reason recorded in the report; null for NONE. Always starts with "requires-". */
        public final String skipReason;

        Requirement(String skipReason) { this.skipReason = skipReason; }
    }

    /** Body of one initialiser; may return a short note (loaded counts) for the report. */
    public interface Step {
        String run() throws Exception;
    }

    /**
     * One named initialiser: what it needs ({@link Requirement}), which data
     * file it reads (relative to the root, or null) and what it does. Public so
     * tests can build fake lists; the production list is {@link #initialisers()}.
     */
    public static final class Initialiser {
        public final String name;
        public final String dataFile;
        public final Requirement requirement;
        public final Step step;

        public Initialiser(String name, String dataFile, Requirement requirement, Step step) {
            this.name = Objects.requireNonNull(name, "name");
            this.dataFile = dataFile;
            this.requirement = Objects.requireNonNull(requirement, "requirement");
            this.step = Objects.requireNonNull(step, "step");
        }
    }

    /** One recorded initialiser outcome. Immutable. */
    public static final class Entry {
        public final int order;
        public final String name;
        public final String dataFile;
        public final Status status;
        public final long elapsedMillis;
        public final String reason;

        Entry(int order, String name, String dataFile, Status status, long elapsedMillis, String reason) {
            this.order = order;
            this.name = name;
            this.dataFile = dataFile;
            this.status = status;
            this.elapsedMillis = elapsedMillis;
            this.reason = reason;
        }

        @Override
        public String toString() {
            StringBuilder line = new StringBuilder();
            line.append('[').append(status.name().toLowerCase()).append("] ");
            line.append(String.format("%5d ms  ", elapsedMillis));
            line.append(name);
            if (dataFile != null) line.append(" (").append(dataFile).append(')');
            if (reason != null && !reason.isEmpty()) line.append(" - ").append(reason);
            return line.toString();
        }
    }

    /** Ordered outcome of one bootstrap run. Immutable once returned. */
    public static final class Report {
        /** The resolved, absolute data root every data-backed entry read from. */
        public final Path dataRoot;
        /** How the root was chosen (installed / -Dataraxia950.data / working directory); informational. */
        public final String dataRootSource;
        /** {@code <working directory>/data}, kept for the log only: nothing reads relative to it any more. */
        public final Path workingDirectoryData;
        /** True when {@link #dataRoot} is a directory holding {@value DataPaths#MARKER_FILE}. */
        public final boolean dataRootUsable;
        public final List<Entry> entries;
        public final long totalMillis;

        Report(Path dataRoot, String dataRootSource, Path workingDirectoryData, boolean dataRootUsable,
               List<Entry> entries, long totalMillis) {
            this.dataRoot = dataRoot;
            this.dataRootSource = dataRootSource;
            this.workingDirectoryData = workingDirectoryData;
            this.dataRootUsable = dataRootUsable;
            this.entries = Collections.unmodifiableList(new ArrayList<Entry>(entries));
            this.totalMillis = totalMillis;
        }

        public int count(Status status) {
            int n = 0;
            for (Entry entry : entries) if (entry.status == status) n++;
            return n;
        }

        public List<Entry> failures() { return withStatus(Status.FAILED); }

        public List<Entry> skipped() { return withStatus(Status.SKIPPED); }

        /** True when no initialiser failed; skipped entries (requires-P0/M2b/M3) are allowed. */
        public boolean allOk() { return count(Status.FAILED) == 0; }

        public Entry entry(String name) {
            for (Entry entry : entries) if (entry.name.equals(name)) return entry;
            return null;
        }

        private List<Entry> withStatus(Status status) {
            List<Entry> result = new ArrayList<Entry>();
            for (Entry entry : entries) if (entry.status == status) result.add(entry);
            return result;
        }

        /** Multi-line human-readable form used by the smoke and the handoff log. */
        public String format() {
            StringBuilder out = new StringBuilder();
            out.append("Native950Bootstrap report").append(System.lineSeparator());
            out.append("  data root: ").append(dataRoot).append(dataRootUsable ? "" : " (NOT USABLE)")
                    .append(" [").append(dataRootSource).append(']').append(System.lineSeparator());
            out.append("  working directory data: ").append(workingDirectoryData).append(System.lineSeparator());
            for (Entry entry : entries) out.append("  ").append(entry).append(System.lineSeparator());
            out.append("  summary: ok=").append(count(Status.OK)).append(" failed=").append(count(Status.FAILED))
                    .append(" skipped=").append(count(Status.SKIPPED)).append(" total=").append(entries.size())
                    .append(" in ").append(totalMillis).append(" ms");
            return out.toString();
        }

        @Override
        public String toString() { return format(); }
    }

    /**
     * One ordered initialiser list and its single-shot report. The static
     * facade owns the production session; tests construct their own over fake
     * entries. A session never touches {@link DataPaths} or the cache: the
     * root it is handed is the root it validates and reports.
     */
    public static final class Session {
        private final List<Initialiser> initialisers;
        private Report completed;

        public Session(List<Initialiser> initialisers) {
            this.initialisers = Collections.unmodifiableList(new ArrayList<Initialiser>(
                    Objects.requireNonNull(initialisers, "initialisers")));
        }

        /** Report of the completed run, or null when this session has not run yet. */
        public synchronized Report lastReport() { return completed; }

        /** The entries this session runs, in order. */
        public List<Initialiser> initialisers() { return initialisers; }

        /**
         * Runs every initialiser once. A second call returns the first report
         * unchanged: the parsers append to static tables and the world tasks would
         * be scheduled twice, so a session is single-shot by design.
         *
         * @param dataRoot         root the data-backed entries read from (made absolute)
         * @param dataRootSource   how the root was chosen; recorded verbatim in the report
         * @param schedulerPresent whether {@code CoresManager.getServiceProvider()} is
         *                         available (P0's tick wheel); gates {@link Requirement#WORLD_SCHEDULER}
         */
        public synchronized Report run(Path dataRoot, String dataRootSource, boolean schedulerPresent) {
            Objects.requireNonNull(dataRoot, "dataRoot");
            if (completed != null) {
                Logger.getGlobal().warn("Native950Bootstrap.run called again; returning the completed report");
                return completed;
            }
            long started = System.nanoTime();
            Path root = dataRoot.toAbsolutePath().normalize();
            Path workingData = Paths.get("").toAbsolutePath().resolve("data").normalize();
            String rootProblem = dataRootProblem(root);
            boolean rootUsable = rootProblem == null;
            if (!rootUsable) Logger.getGlobal().warn("Native950Bootstrap data root unusable: " + rootProblem);
            List<Entry> entries = new ArrayList<Entry>();
            int order = 0;
            for (Initialiser initialiser : initialisers) {
                order++;
                entries.add(execute(order, initialiser, root, rootProblem, schedulerPresent));
            }
            Report report = new Report(root, dataRootSource == null ? "unspecified" : dataRootSource, workingData,
                    rootUsable, entries, (System.nanoTime() - started) / 1_000_000L);
            completed = report;
            Logger.getGlobal().info(report.format());
            return report;
        }
    }

    private static Session production;

    private Native950Bootstrap() { }

    /**
     * Resolves the data root through {@link DataPaths#root()} (installed root,
     * then {@code -Dataraxia950.data}, then {@code <cwd>/data} holding
     * {@value DataPaths#MARKER_FILE}).
     *
     * @throws IllegalStateException when no candidate is usable
     */
    public static Path resolveDataRoot() { return DataPaths.root(); }

    /** Report of the completed production run, or null when the bootstrap has not run yet. */
    public static synchronized Report lastReport() {
        return production == null ? null : production.lastReport();
    }

    /**
     * Runs with the data root from {@link DataPaths#root()}. When no root
     * resolves the returned report has every data-backed entry FAILED with the
     * resolution message and {@code dataRootUsable == false}; the cache-only
     * entries still run so the report is complete rather than an exception.
     */
    public static Report run() {
        Path root;
        String source;
        try {
            root = DataPaths.root();
            source = DataPaths.describeResolution();
        } catch (IllegalStateException unresolved) {
            Logger.getGlobal().warn("Native950Bootstrap: " + unresolved.getMessage());
            root = Paths.get("data").toAbsolutePath().normalize();
            source = "unresolved: " + unresolved.getMessage();
        }
        return runProduction(root, source);
    }

    /**
     * Runs with an explicit data root, which is installed into
     * {@link DataPaths} first so the parsers read the same tree. A second call
     * (any root) returns the first report unchanged.
     */
    public static Report run(Path dataRoot) {
        Objects.requireNonNull(dataRoot, "dataRoot");
        Path root = dataRoot.toAbsolutePath().normalize();
        if (Files.isDirectory(root)) DataPaths.install(root);
        return runProduction(root, "argument: " + root);
    }

    private static synchronized Report runProduction(Path root, String source) {
        if (production != null && production.lastReport() != null)
            return production.run(root, source, false); // logs the warning and returns the first report
        if (!Cache.isFlatReadOnly())
            throw new IllegalStateException("Initialize the modern read-only cache (Cache.initFlatReadOnly) before the 947 bootstrap");
        if (production == null) production = new Session(initialisers());
        return production.run(root, source, CoresManager.getServiceProvider() != null);
    }

    static Entry execute(int order, Initialiser initialiser, Path root, String rootProblem, boolean scheduler) {
        if (!satisfied(initialiser.requirement, scheduler)) {
            return new Entry(order, initialiser.name, initialiser.dataFile, Status.SKIPPED, 0, initialiser.requirement.skipReason);
        }
        if (initialiser.dataFile != null) {
            if (rootProblem != null)
                return new Entry(order, initialiser.name, initialiser.dataFile, Status.FAILED, 0, rootProblem);
            Path file = root.resolve(initialiser.dataFile);
            if (!Files.isRegularFile(file))
                return new Entry(order, initialiser.name, initialiser.dataFile, Status.FAILED, 0, "missing data file " + file);
        }
        long started = System.nanoTime();
        try {
            String note = initialiser.step.run();
            return new Entry(order, initialiser.name, initialiser.dataFile, Status.OK,
                    (System.nanoTime() - started) / 1_000_000L, note);
        } catch (Throwable failure) {
            Throwable cause = failure instanceof InvocationTargetException && failure.getCause() != null
                    ? failure.getCause() : failure;
            Logger.getGlobal().warn("Native950Bootstrap entry failed: " + initialiser.name, cause);
            return new Entry(order, initialiser.name, initialiser.dataFile, Status.FAILED,
                    (System.nanoTime() - started) / 1_000_000L, cause.getClass().getSimpleName()
                            + (cause.getMessage() == null ? "" : ": " + cause.getMessage()));
        }
    }

    /**
     * Whether an initialiser may run in this JVM. WORLD_SCHEDULER is satisfied by P0's tick
     * wheel; NATIVE_PRAYER and NATIVE_TASK_TAB are never satisfied in this phase: the
     * native player has no Prayer and no verified task-tab interface, and because
     * Native950World.pumpSchedulers() drains both WorldTasksManager and the wheel every tick,
     * queuing those tasks would make them fail every 600 ms instead of lying dormant.
     */
    static boolean satisfied(Requirement requirement, boolean scheduler) {
        switch (requirement) {
            case NONE: return true;
            case WORLD_SCHEDULER: return scheduler;
            default: return false;
        }
    }

    /**
     * A usable root is a directory holding {@value DataPaths#MARKER_FILE}; the
     * working directory is irrelevant now that the parsers resolve through
     * {@link DataPaths}.
     */
    static String dataRootProblem(Path root) {
        if (!Files.isDirectory(root)) return "data root " + root + " is not a directory";
        if (!Files.isRegularFile(root.resolve(DataPaths.MARKER_FILE)))
            return "data root " + root + " does not contain " + DataPaths.MARKER_FILE
                    + " (stage Ataraxia950/data or point -D" + DataPaths.PROPERTY + " at it)";
        return null;
    }

    /** The production entry list, in order; 29 entries. */
    public static List<Initialiser> initialisers() {
        List<Initialiser> list = new ArrayList<Initialiser>();
        list.add(cache("WorldAreaTypeDefinitions.init", new Step() {
            public String run() {
                WorldAreaTypeDefinitions.init();
                return "mapped=" + WorldAreaTypeDefinitions.Mapped.size() + " lookUp=" + WorldAreaTypeDefinitions.LOOK_UP.size();
            }
        }));
        list.add(cache("TTAllRewards.init", new Step() {
            public String run() {
                TTAllRewards.init();
                requireLoaded(TTAllRewards.ALL_TOTAL > 0 ? 1 : 0, "treasure trail reward weights");
                return "allTotal=" + TTAllRewards.ALL_TOTAL;
            }
        }));
        list.add(data("CustomObjectSpawnsDataParser.init", "map/customObjectSpawns.json", new Step() {
            public String run() {
                CustomObjectSpawnsDataParser.init();
                return "regions=" + tableSize(CustomObjectSpawnsDataParser.class, "OBJECT_SPAWNS");
            }
        }));
        list.add(data("NPCWeaknessesDataParser.init", "npcs/weaknesses.json", new Step() {
            public String run() {
                NPCWeaknessesDataParser.init();
                return "loaded=" + requireLoaded(tableSize(NPCWeaknessesDataParser.class, "NPC_WEAKNESSES"), "NPC weaknesses");
            }
        }));
        list.add(data("NPCCombatDefinitionsDataParser.init", "npcs/combatDefs.json", new Step() {
            public String run() {
                NPCCombatDefinitionsDataParser.init();
                return "loaded=" + requireLoaded(NPCCombatDefinitionsDataParser.getDefinitions().size(), "NPC combat definitions");
            }
        }));
        list.add(data("NPCStatsDataParser.init", "npcs/npcstats.json", new Step() {
            public String run() {
                NPCStatsDataParser.init();
                return "loaded=" + requireLoaded(NPCStatsDataParser.getDefinitions().size(), "NPC stats");
            }
        }));
        list.add(data("NPCDropsDataParser.init", "npcs/drops.json", new Step() {
            public String run() {
                NPCDropsDataParser.init();
                return "loaded=" + requireLoaded(tableSize(NPCDropsDataParser.class, "NPC_DROPS"), "NPC drops");
            }
        }));
        list.add(data("NPCExaminesDataParser.init", "npcs/examines.json", new Step() {
            public String run() {
                NPCExaminesDataParser.init();
                return "loaded=" + requireLoaded(tableSize(NPCExaminesDataParser.class, "NPC_EXAMINES"), "NPC examines");
            }
        }));
        list.add(data("NPCSpawnsDataParser.init", "npcs/spawns.json", new Step() {
            public String run() {
                NPCSpawnsDataParser.init();
                return "regions=" + requireLoaded(tableSize(NPCSpawnsDataParser.class, "NPC_SPAWNS"), "NPC spawn regions");
            }
        }));
        list.add(data("ItemExaminesDataParser.init", "items/itemExamines.json", new Step() {
            public String run() {
                ItemExaminesDataParser.init();
                return "loaded=" + requireLoaded(tableSize(ItemExaminesDataParser.class, "ITEM_EXAMINES"), "item examines");
            }
        }));
        list.add(data("ItemWeightsDataParser.init", "items/itemWeights.json", new Step() {
            public String run() {
                ItemWeightsDataParser.init();
                return "loaded=" + requireLoaded(tableSize(ItemWeightsDataParser.class, "ITEM_WEIGHTS"), "item weights");
            }
        }));
        list.add(data("ItemDisassembleDataParser.init", "items/itemsDisassembleData.json", new Step() {
            public String run() {
                ItemDisassembleDataParser.init();
                return "loaded=" + requireLoaded(tableSize(ItemDisassembleDataParser.class, "ITEMS_DISASSEMBLE_DATA"), "item disassemble data");
            }
        }));
        list.add(data("PerkGenerationDataParser.init", "perkGenerationData.json", new Step() {
            public String run() {
                PerkGenerationDataParser.init();
                requireLoaded(PerkGenerationDataParser.DATA == null ? 0 : 1, "perk generation data");
                return null;
            }
        }));
        list.add(data("TreasureHunterRewardParser.init", "items/treasurehunterrewards.json", new Step() {
            public String run() {
                TreasureHunterRewardParser.init();
                return "loaded=" + requireLoaded(TreasureHunterRewardParser.getTreasureHunterRewards().size(), "treasure hunter rewards");
            }
        }));
        list.add(data("MusicHintsDataParser.init", "musics/hints.json", new Step() {
            public String run() {
                MusicHintsDataParser.init();
                return "loaded=" + requireLoaded(tableSize(MusicHintsDataParser.class, "MUSIC_HINTS"), "music hints");
            }
        }));
        // CombatScriptsHandler.init now throws itself when it registers nothing (Utils.getClasses scans jars
        // as well as directories); the count here is belt and braces for the report.
        list.add(cache("CombatScriptsHandler.init", new Step() {
            public String run() {
                CombatScriptsHandler.init();
                return "scripts=" + requireLoaded(CombatScriptsHandler.getScriptKeyCount(), "NPC combat scripts");
            }
        }));
        list.add(cache("ControllerHandler.init", new Step() {
            public String run() {
                ControllerHandler.init();
                return "controllers=" + requireLoaded(tableSize(ControllerHandler.class, "handledControlers"), "controllers");
            }
        }));
        list.add(cache("CutscenesHandler.init", new Step() {
            public String run() {
                CutscenesHandler.init();
                return "cutscenes=" + requireLoaded(tableSize(CutscenesHandler.class, "handledCutscenes"), "cutscenes");
            }
        }));
        list.add(data("ShopsDataParser.init", "items/shops.json", new Step() {
            public String run() {
                ShopsDataParser.init();
                // The parser stores a regular and an ironman clone per shop.
                return "shops=" + requireLoaded(tableSize(ShopsDataParser.class, "SHOPS") / 2, "shops");
            }
        }));
        list.add(cache("FishingSpotsHandler.init", new Step() {
            public String run() {
                FishingSpotsHandler.init();
                return "spots=" + requireLoaded(FishingSpotsHandler.moveSpots.size(), "fishing spots");
            }
        }));
        // FastClasspathScanner 3.0.3 registration of dialogues, commands, lunar spells and ED rooms.
        list.add(cache("Scanner.scan", new Step() {
            public String run() {
                Scanner.scan();
                return "dialogues=" + requireLoaded(tableSize(com.rs.game.player.dialogue.DialogueHandler.class, "handledDialogues"), "dialogues")
                        + " commands=" + requireLoaded(CommandHandler.possibleCommands.size(), "commands");
            }
        }));
        // World.init core tasks through the public World.queue*Task wrappers (World.initCoreTasks queues all six
        // at once; the bootstrap needs them one by one because two are gated). Native950World.pumpSchedulers()
        // ALREADY drains WorldTasksManager (and the P0 wheel) every 600 ms, so each task queued here starts
        // running the moment run() is wired into the 947 JVM; only tasks that survive a native player are queued.
        // M3: both prayer-touching tasks are queued again. Prayer is hydrated by
        // Player.hydrateForNative950 and enforced by requireNative950TickManagers,
        // processPrayerDrain returns immediately while no prayer is active, and
        // addRestoreSkillsTask's Skills.set now emits a verified UPDATE_STAT.
        list.add(worldTask("addDrainPrayerTask", Requirement.NONE, new Runnable() {
            public void run() { World.queueDrainPrayerTask(); }
        }));
        list.add(worldTask("addRestoreHitPointsTask", Requirement.NONE, new Runnable() {
            public void run() { World.queueRestoreHitPointsTask(); }
        }));
        list.add(worldTask("addRestoreSkillsTask", Requirement.NONE, new Runnable() {
            public void run() { World.queueRestoreSkillsTask(); }
        }));
        list.add(worldTask("addRestoreSpecialAttackTask", Requirement.NONE, new Runnable() {
            public void run() { World.queueRestoreSpecialAttackTask(); }
        }));
        list.add(worldTask("addOwnedObjectsTask", Requirement.NONE, new Runnable() {
            public void run() { World.queueOwnedObjectsTask(); }
        }));
        list.add(worldTask("addRestoreShopItemsTask", Requirement.NONE, new Runnable() {
            public void run() { World.queueRestoreShopItemsTask(); }
        }));
        list.add(new Initialiser("World.addRefreshTargetBuffsTask", null, Requirement.NATIVE_TASK_TAB, new Step() {
            public String run() {
                World.addRefreshTargetBuffsTask();
                return null;
            }
        }));
        list.add(new Initialiser("ActionBar.addActionBarTask", null, Requirement.WORLD_SCHEDULER, new Step() {
            public String run() {
                ActionBar.addActionBarTask();
                return null;
            }
        }));
        return list;
    }

    private static Initialiser cache(String name, Step step) {
        return new Initialiser(name, null, Requirement.NONE, step);
    }

    private static Initialiser data(String name, String dataFile, Step step) {
        return new Initialiser(name, dataFile, Requirement.NONE, step);
    }

    /** Names keep the legacy {@code World.add*Task} form so reports and the smoke stay comparable. */
    private static Initialiser worldTask(String method, Requirement requirement, final Runnable queue) {
        return new Initialiser("World." + method, null, requirement, new Step() {
            public String run() {
                queue.run();
                return "worldTasks=" + WorldTasksManager.getTasksCount();
            }
        });
    }

    private static int requireLoaded(int count, String what) {
        if (count <= 0) throw new IllegalStateException("Loaded 0 " + what);
        return count;
    }

    /**
     * Size of a private static table without changing the owning class. The
     * parsers keep their maps private and log counts instead of exposing them;
     * this is read-only and fails loudly when a field is renamed.
     */
    static int tableSize(Class<?> owner, String fieldName) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object table = field.get(null);
            if (table instanceof Map) return ((Map<?, ?>) table).size();
            if (table instanceof java.util.Collection) return ((java.util.Collection<?>) table).size();
            throw new IllegalStateException(owner.getSimpleName() + "." + fieldName + " is not a table");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read " + owner.getSimpleName() + "." + fieldName, e);
        }
    }
}
