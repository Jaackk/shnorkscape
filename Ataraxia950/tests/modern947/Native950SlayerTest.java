package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.slayer.SlayerMasterData;
import com.rs.game.player.actions.slayer.SlayerTaskData;
import com.rs.game.player.actions.slayer.TaskSet;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public final class Native950SlayerTest {
    @Rule public final TemporaryFolder temporary = new TemporaryFolder();

    @Test public void currentMasterMenusWithSpacesAndHyphensRemainStrictlyVerified() {
        com.rs.cache.loaders.NPCDefinitions d = com.rs.cache.loaders.NPCDefinitions.decodeStrict947(8461,new byte[]{0},null);
        d.name="Turael";d.models=new int[]{130884};d.size=1;
        for(String option:new String[]{"Talk to","Talk-to","Get task","Get-task","Assignment"}) {
            d.menuOptions=new String[]{option,null,null,null,null};
            assertTrue(option,Native950Slayer.verifiedMaster(8461,d));
        }
        d.menuOptions=new String[]{"Talk to",null,"Get task","Trade","Rewards"};
        d.transformTo=new int[]{8461};assertFalse(Native950Slayer.verifiedMaster(8461,d));d.transformTo=null;
        d.name="Fake Turael";assertFalse(Native950Slayer.verifiedMaster(8461,d));d.name="Turael";
        d.menuOptions[1]="Attack";assertFalse(Native950Slayer.verifiedMaster(8461,d));
        d.menuOptions=new String[]{"Trade",null,null,null,null};assertFalse(Native950Slayer.verifiedMaster(8461,d));
        assertFalse(Native950Slayer.isTalkOption("Talk to someone"));assertFalse(Native950Slayer.isAssignmentOption("Get reward"));
    }
    @Test public void noviceAssignmentsUseOriginalWeightsCountsAndOnlyPopulatedFamilies() {
        Set<String> names = names("chicken", "cow", "goblin", "giant rat");
        List<SlayerTaskData> eligible = Native950Slayer.eligible(SlayerMasterData.TURAEL, 1, 3, names);
        assertEquals(Arrays.asList(SlayerTaskData.BIRDS, SlayerTaskData.COWS, SlayerTaskData.GOBLINS), eligible);
        for (int position = 0; position < 30; position++) {
            Native950Slayer.State state = new Native950Slayer.State();
            final int value = position;
            final int[] calls = {0};
            assertTrue(Native950Slayer.assign(state, SlayerMasterData.TURAEL, 1, 3, names,
                    bound -> calls[0]++ == 0 ? value : bound - 1));
            assertEquals(50, state.remaining());
            assertEquals(Integer.valueOf(Native950Slayer.code(eligible.get(position / 10))), snapshot(state).get(Native950Slayer.TASK));
        }
    }
    @Test public void noUnloadedTasksNoBypassedRequirementsAndNoZeroWeightAssignments() {
        assertTrue(Native950Slayer.eligible(SlayerMasterData.TURAEL, 120, 152, names()).isEmpty());
        assertTrue(Native950Slayer.eligible(SlayerMasterData.MAZCHNA, 120, 19, names("bat")).isEmpty());
        assertTrue(Native950Slayer.eligible(SlayerMasterData.SUMONA, 34, 152, names("grotworm")).isEmpty());
        assertTrue(Native950Slayer.eligible(SlayerMasterData.TURAEL, 6, 152, names("cave bug")).isEmpty());
        assertEquals(Collections.singletonList(SlayerTaskData.CAVE_BUGS),
                Native950Slayer.eligible(SlayerMasterData.TURAEL, 7, 152, names("cave bug")));
        assertTrue(Native950Slayer.eligible(SlayerMasterData.KURADAL, 120, 152, names("volcanic creature")).isEmpty());
        Native950Slayer.State state = new Native950Slayer.State();
        assertFalse(Native950Slayer.assign(state, SlayerMasterData.TURAEL, 1, 3, names(), bound -> {fail("No random draw without candidates");return 0;}));
        assertFalse(state.hasTask());
    }
    @Test public void activeAssignmentsCannotBeRerolledOrReplacedByAnotherMaster() {
        Native950Slayer.State state = task(SlayerMasterData.TURAEL, SlayerTaskData.GOBLINS, 12, 4, 0, 0);
        Map<String,Integer> before = snapshot(state);
        assertFalse(Native950Slayer.assign(state, SlayerMasterData.MAZCHNA, 120, 152, names("bat"), bound -> 0));
        assertEquals(before, snapshot(state));
    }
    @Test public void taskMatchingUsesExactOriginalAliasesAndIntegerEngineHpXp() {
        Native950Slayer.State state = task(SlayerMasterData.TURAEL, SlayerTaskData.BIRDS, 3, 0, 0, 0);
        assertFalse(state.killed("Chicken keeper", 30).matched);
        assertFalse(state.killed("Goblin", 50).matched);
        assertFalse(state.killed(null, 30).matched);
        assertFalse(state.killed("Chicken", 0).matched);
        assertFalse(state.killed("Chicken", Integer.MAX_VALUE).matched);
        assertEquals(3, state.remaining());
        Native950Slayer.Kill kill = state.killed("cHiCkEn", 39);
        assertTrue(kill.matched); assertFalse(kill.completed); assertEquals(3, kill.baseXp);
        assertEquals(2, state.remaining());
        assertTrue(state.killed("Seagull", 30).matched);
        kill = state.killed("Duck", 30);
        assertTrue(kill.completed); assertEquals(0, kill.points); assertEquals(1, state.completed());
        assertEquals(0, state.streak()); assertFalse(state.hasTask());
        assertFalse(state.killed("Chicken", 30).matched);
        assertEquals(1, state.completed());
    }
    @Test public void pointWarmupAndTenthFiftiethMilestonesUseNativePolicy() {
        for (int prior : new int[] {0, 4, 9, 49}) {
            Native950Slayer.State state = task(SlayerMasterData.MAZCHNA, SlayerTaskData.BATS, 1, prior, prior, 100);
            Native950Slayer.Kill kill = state.killed("Bat", 100);
            int expected = prior == 49 ? 30 : prior == 9 ? 10 : prior < 4 ? 0 : 2;
            assertTrue(kill.completed); assertEquals(expected, kill.points);
            assertEquals(100 + expected, state.points()); assertEquals(prior + 1, state.streak());
            assertEquals(prior + 1, state.completed());
        }
        Native950Slayer.State turael = task(SlayerMasterData.TURAEL, SlayerTaskData.GOBLINS, 1, 9, 9, 100);
        assertEquals(0, turael.killed("Goblin", 50).points);
        assertEquals(9, turael.streak()); assertEquals(10, turael.completed()); assertEquals(100, turael.points());
    }
    @Test public void completionCountersCannotOverflowIntoNegativeSavedValues() {
        Native950Slayer.State state = task(SlayerMasterData.MAZCHNA, SlayerTaskData.BATS, 1,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
        assertEquals(1, state.killed("Bat", 100).points);
        assertEquals(Integer.MAX_VALUE, state.completed()); assertEquals(Integer.MAX_VALUE, state.streak());
        assertEquals(Integer.MAX_VALUE, state.points());
    }
    @Test public void unsupportedUnlocksAndFinishingMechanicsAreNeverAssigned() {
        for (SlayerTaskData task : new SlayerTaskData[] {SlayerTaskData.BANSHEES, SlayerTaskData.ROCK_SLUGS,
                SlayerTaskData.TUROTH, SlayerTaskData.ICE_STRYKEWYRMS, SlayerTaskData.EDIMMUS,
                SlayerTaskData.NIHIL, SlayerTaskData.MUSPAH, SlayerTaskData.TORMENTED_DEMONS}) {
            assertFalse(Native950Slayer.baselineTask(task));
            Set<String> populated = new HashSet<>();
            for (String name : task.getMonsters()) populated.add(name.toLowerCase(java.util.Locale.ROOT));
            for (SlayerMasterData master : SlayerMasterData.values())
                assertFalse(Native950Slayer.eligible(master, 120, 152, populated).contains(task));
        }
    }
    @Test public void malformedActiveTupleClearsTogetherWhileCountersAreBounded() {
        Native950Slayer.State state = task(SlayerMasterData.TURAEL, SlayerTaskData.GOBLINS, 10, 12, 8, 25);
        Map<String,Integer> settings = snapshot(state);
        settings.put(Native950Slayer.REMAINING, 51);
        state.restore(settings); assertFalse(state.hasTask()); assertEquals(0, state.remaining());
        assertEquals(12, state.completed()); assertEquals(25, state.points());
        settings.put(Native950Slayer.REMAINING, 1);
        settings.put(Native950Slayer.MASTER, SlayerMasterData.MAZCHNA.getNpcId());
        state.restore(settings); assertFalse(state.hasTask());
        settings.put(Native950Slayer.TASK, -1); settings.put(Native950Slayer.POINTS, -1);
        settings.put(Native950Slayer.COMPLETED, 3); settings.put(Native950Slayer.STREAK, 99);
        state.restore(settings); assertFalse(state.hasTask()); assertEquals(0, state.points()); assertEquals(3, state.streak());
        state.restore(null); assertFalse(state.hasTask()); assertEquals(0, state.completed());
    }
    @Test public void nativeAttackRequirementsRetainPerNpcAndFamilySpecificLevels() {
        assertEquals(1,Native950Slayer.requiredLevel(12353,"Goblin"));
        assertEquals(7,Native950Slayer.requiredLevel(999999,"Cave bug"));
        assertEquals(83,Native950Slayer.requiredLevel(999999,"Spiritual mage"));
        assertEquals(101,Native950Slayer.requiredLevel(24172,"Hydrix dragon"));
        assertEquals(85,Native950Slayer.requiredLevel(1615,"Abyssal demon"));
        assertEquals(1,Native950Slayer.requiredLevel(999999,"Goblin trainer"));
    }
    @Test public void taskCodesAreStableNameBasedDistinctAndNotEnumOrdinals() {
        Set<Integer> codes = new HashSet<>();
        for (SlayerTaskData task : SlayerTaskData.values()) {
            assertEquals(task.name().hashCode() & Integer.MAX_VALUE, Native950Slayer.code(task));
            assertTrue(Native950Slayer.code(task) > 0); assertTrue(codes.add(Native950Slayer.code(task)));
            for (TaskSet set : task.getTaskSet()) assertTrue(set.getMaximumAmount() <= 10000);
        }
    }
    @Test public void persistentRemainingAndRewardsRoundTripThroughPlayerBinderAndSaveStore() throws Exception {
        EmbeddedChannel channel1 = new EmbeddedChannel(), channel2 = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("slayerprobe", new WorldTile(3217,3258,0), channel1);
            int[] ids = new int[28]; Arrays.fill(ids, -1);
            Native950Save base = new Native950Save("slayerprobe",3217,3258,0,ids,new int[28],new int[0],new int[0]);
            Native950Save before = Native950PlayerBinder.capture(player,base,1234L);
            player.getNative950Slayer().restore(snapshot(task(SlayerMasterData.TURAEL,SlayerTaskData.GOBLINS,12,9,4,37)));
            player.getNative950Slayer().killed("Goblin",50);
            Native950Save after = Native950PlayerBinder.capture(player,base,1234L);
            assertEquals(java.util.EnumSet.of(Native950Save.Section.SETTINGS),after.changedSections(before));
            assertArrayEquals(before.inventoryIds(),after.inventoryIds()); assertArrayEquals(before.bankIds(),after.bankIds());
            Native950SaveStore store = new Native950SaveStore(temporary.newFolder("profiles").toPath());
            store.save(after);
            Player restored = Player.createNative950("slayerprobe",new WorldTile(3217,3258,0),channel2);
            Native950PlayerBinder.restore(restored,store.load("slayerprobe"));
            assertEquals(11,restored.getNative950Slayer().remaining()); assertEquals(37,restored.getNative950Slayer().points());
            assertEquals(snapshot(player.getNative950Slayer()),snapshot(restored.getNative950Slayer()));
            restored.applyNativeSettings(Collections.<String,Integer>emptyMap());
            assertFalse(restored.getNative950Slayer().hasTask()); assertEquals(0,restored.getNative950Slayer().points());
        } finally {channel1.finishAndReleaseAll();channel2.finishAndReleaseAll();}
    }
    static Native950Slayer.State task(SlayerMasterData master,SlayerTaskData task,int remaining,int completed,int streak,int points) {
        Map<String,Integer> settings = new LinkedHashMap<>();
        settings.put(Native950Slayer.TASK, Native950Slayer.code(task));settings.put(Native950Slayer.MASTER,master.getNpcId());
        settings.put(Native950Slayer.REMAINING,remaining);settings.put(Native950Slayer.COMPLETED,completed);
        settings.put(Native950Slayer.STREAK,streak);settings.put(Native950Slayer.POINTS,points);
        Native950Slayer.State state = new Native950Slayer.State();state.restore(settings);assertTrue(state.hasTask());return state;
    }
    static Map<String,Integer> snapshot(Native950Slayer.State state) {
        Map<String,Integer> settings = new LinkedHashMap<>();state.writeSettings(settings);return settings;
    }
    private static Set<String> names(String... names) {return new HashSet<>(Arrays.asList(names));}
}
