package modern947;

import com.rs.cache.loaders.BodyDefinitions;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.Native950Appearance;
import com.rs.game.player.client.Native950IdMap;
import com.rs.game.player.client.Native950PacketDispatcher;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * P4 player lifecycle without a cache: hydration creates every manager the
 * native tick dereferences, 100 strict processEntity ticks produce no failure
 * and no STRICT-tier hit, and the GlobalPlayerUpdater native body is byte-equal
 * to the verified Native950Appearance output.
 *
 * <p>Two managers (AuraManager, InventionManager) size arrays from cache enums
 * in their constructors, so the test registers empty enums 13430/10742/10743 in
 * ClientScriptMap's static cache; the three bronze item definitions are decoded
 * from minimal 947-format byte fixtures through the real strict decoder.
 */
public final class Native950PlayerLifecycleTest {
    /** Wear-position defaults of 28/6/0 in the selected 947 cache (verified, see APPEARANCE_EQUIPMENT.md). */
    private static final int[] WEAR_POSITIONS = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0};

    /** Item ids the fixtures below define; restored to their previous state afterwards. */
    private static final int[] FIXTURE_ITEMS = {1139, 1277, 1173, 4151};
    /** Cache enums the fixtures below seed; restored to their previous state afterwards. */
    private static final int[] FIXTURE_ENUMS = {13430, 10742, 10743, 7716};

    private EmbeddedChannel channel;
    private boolean previousStrict;

    /**
     * Statics saved by {@link #seedCacheFreeDefinitions()} and put back by
     * {@link #restoreCacheFreeDefinitions()}. Every one of these caches is a JVM
     * global that any other 947 test class may have populated first (creating a
     * native player alone caches empty enums 13430/10742/10743 through
     * {@code ClientScriptMap.getMap}), so the fixtures overwrite unconditionally
     * instead of skipping an existing entry: that is what made this class
     * order-dependent under Gradle's class order.
     */
    private static final Map<Integer, ClientScriptMap> SAVED_ENUMS = new HashMap<Integer, ClientScriptMap>();
    private static final Map<Integer, GeneralRequirementMap> SAVED_STRUCTS = new HashMap<Integer, GeneralRequirementMap>();
    private static final Map<Integer, ItemDefinitions> SAVED_ITEMS = new HashMap<Integer, ItemDefinitions>();
    private static int[] savedDisabledSlots;
    private static boolean seeded;

    @BeforeClass
    public static void seedCacheFreeDefinitions() throws Exception {
        if (seeded)
            return;
        savedDisabledSlots = BodyDefinitions.disabledSlots;
        for (int id : FIXTURE_ENUMS) {
            ClientScriptMap previous = enumCache().get(id);
            if (previous != null) SAVED_ENUMS.put(id, previous);
        }
        GeneralRequirementMap previousStruct = structCache().get(0);
        if (previousStruct != null) SAVED_STRUCTS.put(0, previousStruct);
        for (int id : FIXTURE_ITEMS) {
            ItemDefinitions previous = ItemDefinitions.getItemsDefinitions().get(id);
            if (previous != null) SAVED_ITEMS.put(id, previous);
        }
        seeded = true;

        seedEnum(13430, 4);  // AuraManager.AURAS_ENUM_ID
        seedEnum(10742, 16); // InventionManager materials (indices 0 and 10 are seeded by its constructor)
        seedEnum(10743, 4);  // InventionManager blueprints
        seedEnum(7716, 0);   // InterfaceManager slot enum: every key falls back to struct 0
        seedStruct(0);       // ... whose params (3494/3503/3505) read as 0
        BodyDefinitions.disabledSlots = WEAR_POSITIONS.clone();
        // opcode 13 = wear slot, opcode 14 = hidden body slot, opcode 249 = params (644 = BAS)
        defineItem(1139, new byte[] {13, 0, 14, 8, 0});
        defineItem(1277, new byte[] {13, 3, (byte) 249, 1, 0, 0, 0x02, (byte) 0x84, 0, 0, 0x0a, 0x18, 0});
        defineItem(1173, new byte[] {13, 5, 0});
    }

    /**
     * Puts every JVM-global cache this class writes back exactly as it was found,
     * so no later test class inherits the fixtures (and so a class that ran first
     * keeps whatever it installed).
     */
    @AfterClass
    public static void restoreCacheFreeDefinitions() throws Exception {
        if (!seeded)
            return;
        BodyDefinitions.disabledSlots = savedDisabledSlots;
        for (int id : FIXTURE_ENUMS) {
            ClientScriptMap previous = SAVED_ENUMS.get(id);
            if (previous == null) enumCache().remove(id); else enumCache().put(id, previous);
        }
        GeneralRequirementMap previousStruct = SAVED_STRUCTS.get(0);
        if (previousStruct == null) structCache().remove(0); else structCache().put(0, previousStruct);
        for (int id : FIXTURE_ITEMS) {
            ItemDefinitions previous = SAVED_ITEMS.get(id);
            if (previous == null) ItemDefinitions.getItemsDefinitions().remove(id);
            else ItemDefinitions.getItemsDefinitions().put(id, previous);
        }
        SAVED_ENUMS.clear();
        SAVED_STRUCTS.clear();
        SAVED_ITEMS.clear();
        seeded = false;
    }

    @Before
    public void strictFacade() {
        previousStrict = Native950PacketDispatcher.isStrict();
        Native950PacketDispatcher.setStrict(true);
        Native950IdMap.reset(); // the resolver hook is a JVM global; no test may inherit another's
        channel = new EmbeddedChannel();
    }

    @After
    public void restore() {
        Native950PacketDispatcher.setStrict(previousStrict);
        Native950IdMap.reset();
        channel.finishAndReleaseAll();
    }

    /**
     * A native character must default to the MODERN interface, not the 910 default of legacy.
     *
     * <p>{@code setDefaultVariables} is not on the native login path today - it sits inside
     * {@code InterfaceManager.sendInterfaces()}, which no native login reaches. It is pinned here
     * because the ribbon work makes that method reachable, and it runs AFTER the same method has
     * already emitted modern interface ids. Left alone it would emit modern and then flip the
     * model to legacy on a first login, where client script 7198 hides the minimap, action bar
     * and ribbon wrappers, and where the life-points bar is divided by ten against a server that
     * feeds hitpoints on the x100 scale. See UI-DECISIONS.md, decision 1.
     */
    @Test
    public void aNativeCharacterDefaultsToTheModernInterface() {
        Player player = Player.createNative950("modern-default", new WorldTile(3222, 3222, 0), channel);
        try {
            player.setDefaultVariables();
        } catch (RuntimeException noInterfaceManager) {
            // Expected on a bare fixture: the method ends by refreshing interface vars and
            // sending abilities, neither of which a character built by createNative950 alone can
            // do. The three mode flags are assigned before that point, which is the whole of what
            // this test pins - and the fact that the tail throws here is itself a reminder that
            // nothing calls this method on the native path yet.
        }
        assertFalse("a native character must never default into the legacy interface",
                player.isInLegacyInterfaceMode());
        assertFalse("legacy combat mode halves the life-points bar on this port",
                player.isInLegacyCombatMode());
    }

    @Test
    public void hydrationCreatesEveryManagerTheNativeTickDereferences() {
        Player player = Player.createNative950("hydrate", new WorldTile(3222, 3222, 0), channel);
        assertNotNull(player.getPrayer());
        assertNotNull(player.getAuraManager());
        assertNotNull(player.getInventionManager());
        assertNotNull(player.getDayOfWeekManager());
        assertNotNull(player.getBuffDebuffTimersManager());
        assertNotNull(player.getFarmingManager());
        assertNotNull(player.getPlayerExamineManager());
        assertNotNull(player.getQuestManager());
        assertNotNull(player.getToolBeltNew());
        assertNotNull(player.getPerkManager());
        assertNotNull(player.getDeathManager());
        assertNotNull(player.getAccountPin());
        assertTrue("pin marked entered like LoginManager.initBot", player.getAccountPin().hasEnteredPin());
        assertNotNull(player.getControlerManager());
        assertNotNull(player.getCutscenesManager());
        assertNotNull(player.getActionManager());
        assertNotNull(player.getInterfaceManager());
        assertNotNull(player.getDialogueManager());
        assertNotNull(player.getHintIconsManager());
        assertNotNull(player.getPriceCheckManager());
        assertNotNull(player.getVarBitManager());
        assertSame(player.getVarBitManager(), player.getVarBitManager());
        assertNotNull(player.getCombatDefinitions());
        assertNotNull(player.getActivityTimersManager());
        assertNotNull(player.getVisWaxManager());
        assertNotNull(player.getActivePotions());
        // The belt is ALWAYS constructed: Inventory.containsItem / containsOneItem
        // dereference getToolBelt() with no guard and Player.stopAll() reaches them
        // on the first routed object click. Without the enum 13730 -> 6979/6980 ->
        // 2433 chain (M8, and absent with no cache) it is EMPTY, not absent - and an
        // empty belt refuses every item rather than deleting it from the inventory.
        assertNotNull("Toolbelt must never be a silently-null manager", player.getToolBelt());
        assertFalse("Without the 947 enum chain the belt must be empty",
                player.getToolBelt().contains(com.rs.game.player.content.Toolbelt.TOOL_BELT_ITEMS[0][0][0]));
        assertFalse("An unrefreshable belt must refuse an item instead of consuming it",
                player.getToolBelt().addItem(new com.rs.game.item.Item(
                        com.rs.game.player.content.Toolbelt.TOOL_BELT_ITEMS[0][0][0], 1), true));
        assertEquals(player.getMaxHitpoints(), player.getHitpoints());
        assertEquals(100, player.getHitpoints());
        assertFalse(player.isAFK());
        player.hydrateForNative950(); // idempotent
        assertEquals(0, player.getNativeTickFailures());
        assertEquals(0L, ((Native950PacketDispatcher) player.getPackets()).counters().totalSent());
    }

    @Test
    public void nativeMusicNeverQueuesLegacyReplayOrResetsAudioPreferences() {
        Player player = Player.createNative950("music-owner", new WorldTile(3222, 3222, 0), channel);
        int pending = com.rs.game.tasks.WorldTasksManager.getTasksCount();
        Native950PacketDispatcher dispatcher = (Native950PacketDispatcher) player.getPackets();
        long sent = dispatcher.counters().totalSent();
        long noops = dispatcher.counters().totalNoops();
        long dropped = dispatcher.counters().totalDropped();
        assertFalse(player.getMusicsManager().musicEnded());
        player.getMusicsManager().init();
        player.getMusicsManager().replayMusic();
        player.getMusicsManager().playMusic(58);
        player.getMusicsManager().forcePlayMusic(17);
        assertFalse(player.getMusicsManager().musicEnded());
        assertEquals("legacy delayed playback must not survive a native session",
                pending, com.rs.game.tasks.WorldTasksManager.getTasksCount());
        assertEquals(sent, dispatcher.counters().totalSent());
        assertEquals(noops, dispatcher.counters().totalNoops());
        assertEquals(dropped, dispatcher.counters().totalDropped());
    }

    @Test
    public void oneHundredStrictTicksProduceNoFailureAndNoStrictHit() {
        Player player = Player.createNative950("ticker", new WorldTile(3222, 3222, 0), channel);
        player.setActive(true);
        player.setRunning(true);
        for (int tick = 0; tick < 100; tick++) {
            player.processEntity();
            player.processEntityUpdate();
            player.resetMasks();
        }
        Native950PacketDispatcher.Counters counters = ((Native950PacketDispatcher) player.getPackets()).counters();
        assertEquals("processEntity exceptions: " + player.getNativeTickFailures(), 0, player.getNativeTickFailures());
        assertEquals("STRICT tier hits: " + counters.strictHitsByMethod(), 0L, counters.totalStrictHits());
        assertEquals("nothing may reach the wire without a transport", 0L, counters.totalSent());
        assertFalse(player.hasFinished());
        assertEquals(3222, player.getX());
    }

    @Test
    public void processEntityFailureIsCountedAndRethrownInStrictMode() {
        Player player = Player.createNative950("failing", new WorldTile(3222, 3222, 0), channel);
        player.setActive(true);
        // Removing a hydrated manager is the simplest deterministic tick failure.
        player.cutscenesManager = null;
        try {
            player.processEntity();
            fail("strict mode must rethrow the counted tick failure");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("tick failure #1"));
        }
        assertEquals(1, player.getNativeTickFailures());
        Native950PacketDispatcher.setStrict(false);
        player.processEntity(); // lenient: counted, swallowed
        assertEquals(2, player.getNativeTickFailures());
    }

    /**
     * The hydration contract is fail closed: a manager the native tick
     * dereferences may never be null. Hydration verifies the whole list before
     * it returns, so a manager that could not be constructed refuses native
     * admission (Native950World.attachOnWorld propagates this out of
     * Player.createNative950) instead of leaving an NPE landmine in the tick.
     */
    @Test
    public void hydrationRefusesAdmissionInsteadOfLeavingANullManager() {
        Player player = Player.createNative950("closed", new WorldTile(3222, 3222, 0), channel);
        player.cutscenesManager = null;
        try {
            player.hydrateForNative950();
            fail("a null tick manager must refuse native admission");
        } catch (IllegalStateException refused) {
            assertTrue(refused.getMessage(), refused.getMessage().contains("Native 947 admission refused for closed"));
            assertTrue(refused.getMessage(), refused.getMessage().contains("'cutscenesManager'"));
            assertTrue(refused.getMessage(), refused.getMessage().contains("Cache.initFlatReadOnly"));
        }
        assertEquals(0L, ((Native950PacketDispatcher) player.getPackets()).counters().totalSent());
    }

    @Test
    public void legacyLogicPacketQueueIsSkippedNatively() {
        Player player = Player.createNative950("packets", new WorldTile(3222, 3222, 0), channel);
        assertNull(player.logicPackets);
        player.processLogicPackets(); // no requireLegacyGameplay throw
    }

    @Test
    public void skillsAndVitalsSettersAreSilent() {
        Player player = Player.createNative950("skills", new WorldTile(3222, 3222, 0), channel);
        Native950PacketDispatcher.Counters counters = ((Native950PacketDispatcher) player.getPackets()).counters();
        player.getSkills().setLevelWithoutRefresh(Skills.ATTACK, 40);
        player.getSkills().setXpWithoutRefresh(Skills.ATTACK, 37224);
        assertEquals(40, player.getSkills().getLevel(Skills.ATTACK));
        assertEquals(37224, player.getSkills().getXp(Skills.ATTACK), 0.0);
        assertEquals(0L, counters.totalNoops() + counters.totalSent() + counters.totalStrictHits() + counters.totalDropped());
        // M3 behaviour change: the refreshing setters now build REAL packets
        // (UPDATE_STAT 66 and VARBIT_SMALL/LARGE are verified). This channel has no
        // Native950GameTransport, so each one is a counted drop with the reason
        // "transport not attached" - still never a STRICT hit, and still nothing on
        // the wire. Native950StatsTest asserts the bytes over a real transport.
        player.getSkills().set(Skills.STRENGTH, 20);
        assertEquals(1L, counters.dropped("sendSkillLevel"));
        assertEquals(0L, counters.totalStrictHits());
        long deferredBefore = player.getNativeDeferredRefreshes();
        player.refreshHitPoints();
        assertEquals("refreshHitPoints emits 950 varp 13537 instead of deferring",
                deferredBefore, player.getNativeDeferredRefreshes());
        assertEquals(1L, counters.dropped("sendConfig"));
        assertEquals(0L, counters.totalStrictHits());
    }

    @Test
    public void nativeAppearanceBodyMatchesTheVerifiedTemplateForBareAndBronzeBodies() {
        Player player = Player.createNative950("x", new WorldTile(3222, 3222, 0), channel);
        Native950Appearance template = new Native950Appearance(WEAR_POSITIONS);
        int[] ids = emptyEquipment();
        player.getAppearence().generateAppearenceData();
        byte[] bare = player.getAppearence().getAppeareanceData();
        assertArrayEquals(template.encode("x", ids), bare);
        assertNotNull(player.getAppearence().getMD5AppeareanceDataHash());

        player.getEquipment().getItems().set(0, new Item(1139, 1));
        player.getEquipment().getItems().set(3, new Item(1277, 1));
        player.getEquipment().getItems().set(5, new Item(1173, 1));
        ids[0] = 1139; ids[3] = 1277; ids[5] = 1173;
        player.getAppearence().generateAppearenceData();
        byte[] equipped = player.getAppearence().getAppeareanceData();
        assertArrayEquals(template.encode("x", ids), equipped);
        assertFalse(Arrays.equals(bare, equipped));
        assertEquals(0, player.getAppearence().getNative950WithheldBodies());

        // Unverified gear: an item with no wear slot keeps the previous body and counts.
        defineItemQuietly(4151, new byte[] {0});
        player.getEquipment().getItems().set(3, new Item(4151, 1));
        player.getAppearence().generateAppearenceData();
        assertArrayEquals(equipped, player.getAppearence().getAppeareanceData());
        assertEquals(1, player.getAppearence().getNative950WithheldBodies());
        Native950PacketDispatcher.Counters counters = ((Native950PacketDispatcher) player.getPackets()).counters();
        assertEquals("appearance must not emit packets", 0L, counters.totalSent() + counters.totalStrictHits() + counters.totalNoops());
    }

    private static int[] emptyEquipment() {
        int[] items = new int[Native950Appearance.SLOT_COUNT];
        Arrays.fill(items, -1);
        return items;
    }

    private static void defineItem(int id, byte[] file) {
        ItemDefinitions defs = ItemDefinitions.decodeStrict947(id, file, null);
        assertNull(defs.decodeFailure);
        ItemDefinitions.getItemsDefinitions().put(id, defs);
    }

    private static void defineItemQuietly(int id, byte[] file) {
        ItemDefinitions.getItemsDefinitions().put(id, ItemDefinitions.decodeStrict947(id, file, null));
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, ClientScriptMap> enumCache() throws Exception {
        Field cache = ClientScriptMap.class.getDeclaredField("interfaceScripts");
        cache.setAccessible(true);
        return (Map<Integer, ClientScriptMap>) cache.get(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, GeneralRequirementMap> structCache() throws Exception {
        Field cache = GeneralRequirementMap.class.getDeclaredField("maps");
        cache.setAccessible(true);
        return (Map<Integer, GeneralRequirementMap>) cache.get(null);
    }

    private static void seedStruct(int id) throws Exception {
        Map<Integer, GeneralRequirementMap> structs = structCache();
        Constructor<GeneralRequirementMap> constructor = GeneralRequirementMap.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        GeneralRequirementMap struct = constructor.newInstance();
        Field values = GeneralRequirementMap.class.getDeclaredField("values");
        values.setAccessible(true);
        values.set(struct, new HashMap<Long, Object>());
        structs.put(id, struct);
    }

    private static void seedEnum(int id, int size) throws Exception {
        Map<Integer, ClientScriptMap> scripts = enumCache();
        Constructor<ClientScriptMap> constructor = ClientScriptMap.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ClientScriptMap map = constructor.newInstance();
        Field values = ClientScriptMap.class.getDeclaredField("values");
        values.setAccessible(true);
        HashMap<Long, Object> entries = new HashMap<Long, Object>();
        for (long key = 0; key < size; key++) entries.put(key, 0);
        values.set(map, entries);
        scripts.put(id, map);
    }
}
