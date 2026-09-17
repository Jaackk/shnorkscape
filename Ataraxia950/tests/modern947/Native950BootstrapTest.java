package modern947;

import com.rs.game.player.client.Native950Bootstrap;
import com.rs.game.player.client.Native950Bootstrap.Entry;
import com.rs.game.player.client.Native950Bootstrap.Initialiser;
import com.rs.game.player.client.Native950Bootstrap.Report;
import com.rs.game.player.client.Native950Bootstrap.Requirement;
import com.rs.game.player.client.Native950Bootstrap.Session;
import com.rs.game.player.client.Native950Bootstrap.Status;
import com.rs.game.player.client.Native950Bootstrap.Step;
import com.rs.utils.DataPaths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Cache-free coverage of the 947 bootstrap: how {@link DataPaths} picks the data
 * root (installed root, {@code -Dataraxia950.data}, {@code <cwd>/data} with the
 * marker file, else an actionable failure) and how a {@link Session} accounts
 * for ok / failed / skipped entries over an injected fake list. The production
 * entry list is only inspected, never run: running it needs the flat cache.
 */
public final class Native950BootstrapTest {

    private Path temp;
    private String previousProperty;

    @Before
    public void setUp() throws IOException {
        temp = Files.createTempDirectory("native947-bootstrap");
        previousProperty = System.getProperty(DataPaths.PROPERTY);
        System.clearProperty(DataPaths.PROPERTY);
        DataPaths.reset();
    }

    @After
    public void tearDown() throws IOException {
        DataPaths.reset();
        if (previousProperty == null) System.clearProperty(DataPaths.PROPERTY);
        else System.setProperty(DataPaths.PROPERTY, previousProperty);
        deleteRecursively(temp);
    }

    // ---------------------------------------------------------------- DataPaths

    @Test
    public void propertyWinsOverTheWorkingDirectory() throws IOException {
        Path staged = stagedRoot(temp.resolve("staged"));
        Path cwd = Files.createDirectories(temp.resolve("cwd"));
        stagedRoot(cwd.resolve("data"));
        assertEquals(staged, DataPaths.resolveRoot(staged.toString(), cwd));
    }

    @Test
    public void propertyIsMadeAbsoluteAndNormalised() throws IOException {
        Path staged = stagedRoot(temp.resolve("staged"));
        String dotted = staged.resolve("..").resolve("staged").toString();
        assertEquals(staged, DataPaths.resolveRoot(dotted, temp));
        assertTrue(DataPaths.resolveRoot(dotted, temp).isAbsolute());
    }

    @Test
    public void workingDirectoryFallbackNeedsTheMarkerFile() throws IOException {
        Path cwd = Files.createDirectories(temp.resolve("cwd"));
        Path legacy = stagedRoot(cwd.resolve("data"));
        assertEquals(legacy, DataPaths.resolveRoot(null, cwd));
        assertEquals(legacy, DataPaths.resolveRoot("   ", cwd));
    }

    @Test
    public void failsClosedNamingBothCandidatesWhenTheMarkerIsMissing() throws IOException {
        Path cwd = Files.createDirectories(temp.resolve("cwd"));
        Path legacy = Files.createDirectories(cwd.resolve("data"));
        try {
            DataPaths.resolveRoot(null, cwd);
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            String message = expected.getMessage();
            assertTrue(message, message.contains("-D" + DataPaths.PROPERTY));
            assertTrue(message, message.contains(legacy.toString()));
            assertTrue(message, message.contains("does not contain " + DataPaths.MARKER_FILE));
        }
    }

    @Test
    public void failsClosedWhenTheWorkingDirectoryHasNoDataDirectory() throws IOException {
        Path cwd = Files.createDirectories(temp.resolve("cwd"));
        try {
            DataPaths.resolveRoot("", cwd);
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            String message = expected.getMessage();
            assertTrue(message, message.contains("-D" + DataPaths.PROPERTY));
            assertTrue(message, message.contains(cwd.resolve("data").toString()));
            assertTrue(message, message.contains("does not exist"));
        }
    }

    @Test
    public void badPropertyFailsClosedInsteadOfFallingBack() throws IOException {
        Path cwd = Files.createDirectories(temp.resolve("cwd"));
        stagedRoot(cwd.resolve("data")); // a perfectly good fallback that must NOT be used
        Path missing = temp.resolve("nowhere");
        try {
            DataPaths.resolveRoot(missing.toString(), cwd);
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            String message = expected.getMessage();
            assertTrue(message, message.contains(missing.toString()));
            assertTrue(message, message.contains("-D" + DataPaths.PROPERTY));
            assertTrue(message, message.contains("not consulted"));
        }
    }

    @Test
    public void rootHonoursTheSystemProperty() throws IOException {
        Path staged = stagedRoot(temp.resolve("staged"));
        System.setProperty(DataPaths.PROPERTY, staged.toString());
        assertEquals(staged, DataPaths.root());
        assertTrue(DataPaths.describeResolution(), DataPaths.describeResolution().startsWith("-D" + DataPaths.PROPERTY));
        assertEquals(staged.resolve("items").resolve("shops.json"), DataPaths.path("items/shops.json"));
        assertEquals(staged.resolve("items").resolve("shops.json").toString(), DataPaths.resolve("items/shops.json"));
        assertEquals(staged.resolve("items").resolve("shops.json").toFile(), DataPaths.file("items/shops.json"));
    }

    @Test
    public void installedRootWinsUntilReset() throws IOException {
        Path staged = stagedRoot(temp.resolve("staged"));
        Path installed = Files.createDirectories(temp.resolve("installed"));
        System.setProperty(DataPaths.PROPERTY, staged.toString());
        DataPaths.install(installed);
        assertEquals(installed, DataPaths.root());
        assertEquals(installed, DataPaths.installed());
        assertTrue(DataPaths.describeResolution().startsWith("installed: "));
        DataPaths.reset();
        assertNull(DataPaths.installed());
        assertEquals(staged, DataPaths.root());
    }

    @Test
    public void installRejectsANonDirectory() throws IOException {
        Path file = Files.createFile(temp.resolve("not-a-directory"));
        try {
            DataPaths.install(file);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains(file.toString()));
        }
        assertNull(DataPaths.installed());
    }

    @Test
    public void absoluteArgumentsAreNotReRooted() throws IOException {
        Path staged = stagedRoot(temp.resolve("staged"));
        DataPaths.install(staged);
        Path elsewhere = temp.resolve("elsewhere.json").toAbsolutePath();
        assertEquals(elsewhere.normalize(), DataPaths.path(elsewhere.toString()));
    }

    // ------------------------------------------------------------------ Session

    @Test
    public void reportAccountsForOkFailedAndSkippedEntries() throws IOException {
        Path root = stagedRoot(temp.resolve("root"));
        Files.createDirectories(root.resolve("items"));
        Files.write(root.resolve("items").resolve("present.json"), "[]".getBytes("US-ASCII"));
        final AtomicInteger afterFailure = new AtomicInteger();
        List<Initialiser> entries = new ArrayList<Initialiser>();
        entries.add(new Initialiser("ok", null, Requirement.NONE, note("loaded=3")));
        entries.add(new Initialiser("boom", null, Requirement.NONE, new Step() {
            public String run() { throw new IllegalStateException("Loaded 0 widgets"); }
        }));
        entries.add(new Initialiser("present", "items/present.json", Requirement.NONE, note("ok")));
        entries.add(new Initialiser("absent", "items/absent.json", Requirement.NONE, new Step() {
            public String run() { fail("a missing data file must not reach the step"); return null; }
        }));
        entries.add(new Initialiser("prayer", null, Requirement.NATIVE_PRAYER, new Step() {
            public String run() { fail("NATIVE_PRAYER is never satisfied in this phase"); return null; }
        }));
        entries.add(new Initialiser("taskTab", null, Requirement.NATIVE_TASK_TAB, new Step() {
            public String run() { fail("NATIVE_TASK_TAB is never satisfied in this phase"); return null; }
        }));
        entries.add(new Initialiser("wheel", null, Requirement.WORLD_SCHEDULER, new Step() {
            public String run() { fail("no scheduler in this run"); return null; }
        }));
        entries.add(new Initialiser("last", null, Requirement.NONE, new Step() {
            public String run() { afterFailure.incrementAndGet(); return null; }
        }));

        Session session = new Session(entries);
        assertNull(session.lastReport());
        Report report = session.run(root, "test", false);

        assertSame(report, session.lastReport());
        assertEquals(root, report.dataRoot);
        assertEquals("test", report.dataRootSource);
        assertTrue(report.dataRootUsable);
        assertEquals(8, report.entries.size());
        assertEquals(3, report.count(Status.OK));
        assertEquals(2, report.count(Status.FAILED));
        assertEquals(3, report.count(Status.SKIPPED));
        assertFalse(report.allOk());
        assertEquals(1, afterFailure.get()); // a failure never aborts the run

        Entry ok = report.entry("ok");
        assertEquals(Status.OK, ok.status);
        assertEquals(1, ok.order);
        assertEquals("loaded=3", ok.reason);
        assertNull(ok.dataFile);

        Entry boom = report.entry("boom");
        assertEquals(Status.FAILED, boom.status);
        assertEquals(2, boom.order);
        assertEquals("IllegalStateException: Loaded 0 widgets", boom.reason);

        Entry present = report.entry("present");
        assertEquals(Status.OK, present.status);
        assertEquals("items/present.json", present.dataFile);

        Entry absent = report.entry("absent");
        assertEquals(Status.FAILED, absent.status);
        assertTrue(absent.reason, absent.reason.startsWith("missing data file "));
        assertTrue(absent.reason, absent.reason.contains(root.resolve("items").resolve("absent.json").toString()));

        for (String skipped : new String[] {"prayer", "taskTab", "wheel"}) {
            Entry entry = report.entry(skipped);
            assertEquals(skipped, Status.SKIPPED, entry.status);
            assertTrue(entry.reason, entry.reason.startsWith("requires-"));
        }
        assertEquals("requires-M3", report.entry("prayer").reason.substring(0, 11));
        assertEquals("requires-M2b", report.entry("taskTab").reason.substring(0, 12));
        assertEquals("requires-P0", report.entry("wheel").reason.substring(0, 11));

        assertEquals(Arrays.asList("boom", "absent"), names(report.failures()));
        assertEquals(Arrays.asList("prayer", "taskTab", "wheel"), names(report.skipped()));
        assertNull(report.entry("nope"));

        String formatted = report.format();
        assertTrue(formatted, formatted.contains("data root: " + root + " [test]"));
        assertTrue(formatted, formatted.contains("summary: ok=3 failed=2 skipped=3 total=8"));
        assertTrue(formatted, formatted.contains("[failed]"));
        assertTrue(formatted, formatted.contains("(items/absent.json) - missing data file"));
    }

    @Test
    public void schedulerPresenceUnlocksOnlyTheWorldSchedulerRequirement() throws IOException {
        Path root = stagedRoot(temp.resolve("root"));
        final AtomicInteger wheelRuns = new AtomicInteger();
        List<Initialiser> entries = new ArrayList<Initialiser>();
        entries.add(new Initialiser("wheel", null, Requirement.WORLD_SCHEDULER, new Step() {
            public String run() { wheelRuns.incrementAndGet(); return null; }
        }));
        entries.add(new Initialiser("prayer", null, Requirement.NATIVE_PRAYER, new Step() {
            public String run() { fail("NATIVE_PRAYER must stay gated with a scheduler too"); return null; }
        }));
        entries.add(new Initialiser("taskTab", null, Requirement.NATIVE_TASK_TAB, new Step() {
            public String run() { fail("NATIVE_TASK_TAB must stay gated with a scheduler too"); return null; }
        }));
        Report report = new Session(entries).run(root, "test", true);
        assertEquals(1, wheelRuns.get());
        assertEquals(Status.OK, report.entry("wheel").status);
        assertEquals(Status.SKIPPED, report.entry("prayer").status);
        assertEquals(Status.SKIPPED, report.entry("taskTab").status);
        assertTrue(report.allOk());
    }

    @Test
    public void sessionIsSingleShot() throws IOException {
        Path root = stagedRoot(temp.resolve("root"));
        final AtomicInteger runs = new AtomicInteger();
        List<Initialiser> entries = new ArrayList<Initialiser>();
        entries.add(new Initialiser("counted", null, Requirement.NONE, new Step() {
            public String run() { return "run=" + runs.incrementAndGet(); }
        }));
        Session session = new Session(entries);
        Report first = session.run(root, "first", false);
        Report second = session.run(temp.resolve("other"), "second", true);
        assertSame(first, second);
        assertEquals(1, runs.get());
        assertEquals("first", second.dataRootSource);
        assertEquals("run=1", first.entry("counted").reason);
    }

    @Test
    public void unusableRootFailsEveryDataEntryButStillRunsCacheEntries() throws IOException {
        Path root = Files.createDirectories(temp.resolve("empty")); // a directory without the marker
        final AtomicInteger cacheRuns = new AtomicInteger();
        List<Initialiser> entries = new ArrayList<Initialiser>();
        entries.add(new Initialiser("cacheOnly", null, Requirement.NONE, new Step() {
            public String run() { cacheRuns.incrementAndGet(); return null; }
        }));
        entries.add(new Initialiser("data", "npcs/spawns.json", Requirement.NONE, new Step() {
            public String run() { fail("data entries must not run on an unusable root"); return null; }
        }));
        Report report = new Session(entries).run(root, "test", false);
        assertFalse(report.dataRootUsable);
        assertEquals(1, cacheRuns.get());
        assertEquals(Status.OK, report.entry("cacheOnly").status);
        Entry data = report.entry("data");
        assertEquals(Status.FAILED, data.status);
        assertTrue(data.reason, data.reason.contains("does not contain " + DataPaths.MARKER_FILE));
        assertTrue(data.reason, data.reason.contains("-D" + DataPaths.PROPERTY));
        assertTrue(report.format(), report.format().contains("(NOT USABLE)"));

        Report missing = new Session(entries).run(temp.resolve("does-not-exist"), "test", false);
        assertFalse(missing.dataRootUsable);
        assertTrue(missing.entry("data").reason, missing.entry("data").reason.contains("is not a directory"));
    }

    @Test
    public void reportEntriesAreImmutable() throws IOException {
        Path root = stagedRoot(temp.resolve("root"));
        List<Initialiser> entries = new ArrayList<Initialiser>();
        entries.add(new Initialiser("ok", null, Requirement.NONE, note(null)));
        Report report = new Session(entries).run(root, "test", false);
        try {
            report.entries.add(null);
            fail("entries must be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // ok
        }
        assertEquals(1, report.entries.size());
    }

    // ------------------------------------------------------- production list

    @Test
    public void productionListIsOrderedGatedAndUnique() {
        List<Initialiser> production = Native950Bootstrap.initialisers();
        assertEquals(29, production.size());
        Set<String> names = new HashSet<String>();
        for (Initialiser initialiser : production) {
            assertTrue("duplicate " + initialiser.name, names.add(initialiser.name));
            assertNotNull(initialiser.step);
            if (initialiser.dataFile != null) {
                assertFalse("data files are relative to the root: " + initialiser.dataFile, initialiser.dataFile.startsWith("data/"));
                assertFalse(initialiser.dataFile, Paths.get(initialiser.dataFile).isAbsolute());
            }
        }
        assertEquals("WorldAreaTypeDefinitions.init", production.get(0).name);
        assertEquals("ActionBar.addActionBarTask", production.get(28).name);
        // Definition tables precede the spawns (the deliberate reordering of ServerLauncher.init).
        assertTrue(indexOf(production, "NPCWeaknessesDataParser.init") < indexOf(production, "NPCSpawnsDataParser.init"));
        assertTrue(indexOf(production, "NPCCombatDefinitionsDataParser.init") < indexOf(production, "NPCSpawnsDataParser.init"));
        assertEquals("npcs/spawns.json", production.get(indexOf(production, "NPCSpawnsDataParser.init")).dataFile);
        assertEquals(DataPaths.MARKER_FILE, production.get(indexOf(production, "NPCSpawnsDataParser.init")).dataFile);
        assertEquals(Requirement.NATIVE_TASK_TAB, production.get(indexOf(production, "World.addRefreshTargetBuffsTask")).requirement);
        assertEquals(Requirement.WORLD_SCHEDULER, production.get(indexOf(production, "ActionBar.addActionBarTask")).requirement);
        // M3 behaviour change: the two prayer-touching world tasks are queued again.
        // Player.hydrateForNative950 builds a Prayer (and requireNative950TickManagers
        // refuses admission without one), and the packets they reach for - UPDATE_STAT
        // through Skills.set and the prayer varbit - are verified since Protocol S.
        for (String free : new String[] {"World.addDrainPrayerTask", "World.addRestoreSkillsTask",
                "World.addRestoreHitPointsTask", "World.addRestoreSpecialAttackTask",
                "World.addOwnedObjectsTask", "World.addRestoreShopItemsTask"})
            assertEquals(free, Requirement.NONE, production.get(indexOf(production, free)).requirement);
        for (Requirement requirement : Requirement.values())
            if (requirement != Requirement.NONE)
                assertTrue(requirement.name(), requirement.skipReason.startsWith("requires-"));
    }

    @Test
    public void productionFacadeStartsWithoutAReport() {
        assertNull(Native950Bootstrap.lastReport());
        assertEquals(DataPaths.PROPERTY, Native950Bootstrap.DATA_ROOT_PROPERTY);
    }

    // ------------------------------------------------------------------ helpers

    private static Step note(final String note) {
        return new Step() {
            public String run() { return note; }
        };
    }

    private static Path stagedRoot(Path root) throws IOException {
        Path marker = root.resolve(DataPaths.MARKER_FILE);
        Files.createDirectories(marker.getParent());
        Files.write(marker, "[]".getBytes("US-ASCII"));
        return root.toAbsolutePath().normalize();
    }

    private static List<String> names(List<Entry> entries) {
        List<String> names = new ArrayList<String>();
        for (Entry entry : entries) names.add(entry.name);
        return names;
    }

    private static int indexOf(List<Initialiser> list, String name) {
        for (int i = 0; i < list.size(); i++) if (list.get(i).name.equals(name)) return i;
        fail("missing production entry " + name);
        return -1;
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path)) return;
        if (Files.isDirectory(path)) {
            List<Path> children = new ArrayList<Path>();
            try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path child : stream) children.add(child);
            }
            for (Path child : children) deleteRecursively(child);
        }
        Files.deleteIfExists(path);
    }
}
