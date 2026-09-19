package modern947;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.Native950IdMap;
import com.rs.game.player.client.Native950PacketDispatcher;
import com.rs.game.player.client.Native950StatsUi;
import com.rs.game.player.client.ui.Native950Bindings;
import com.rs.game.player.client.ui.Native950CacheReader;
import com.rs.network.modern.Native950GameTransport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * The M3 login HUD bootstrap: the skills tab and the action bar are attached to
 * their root slots, the minimap wrapper is shown, and only then does the stat and
 * vital state go out. Order is the contract, because the skills panel has no
 * onLoad hook and is only built when a stat transmit reaches it
 * (verified/ui/SKILLS_TAB.md section 2).
 *
 * <p>Every id comes from a binding table validated against a scripted cache, and
 * the allow-list resolver of that same table is installed into the facade, so a
 * frame reaching the channel proves the id was declared. Nothing here touches the
 * real 22 GB cache.
 *
 * <p><b>Wire layer: 950.</b> The opcodes and the three body readers below
 * ({@link #assertOpenSub}, {@link #assertSetHide}, {@link #statSkill}) were re-derived from the
 * VERIFIED outbound table in {@code protocol-analysis/ataraxia-950-port-plan.md} PART 1 and
 * re-checked against the 950 parsers cited on each one. Two of the bootstrap behaviours here
 * were never at fault in the port - the emitter contract and the stat burst both still hold; it
 * was this file's 947 opcodes and byte offsets that had gone stale. See the port notes on
 * {@link #theDefaultEmitterLeavesTheStateToTheSession} and
 * {@link #theEngineStateBurstCoversEveryStatTheCacheDefines}.
 *
 * <p>The interface, component and script ids are a separate question and are NOT settled by this
 * file: the port plan's largest open risk is that 1465 renumbered +1, 1430 +2 and 1475
 * non-uniformly between the revisions, and that none of the pinned clientscripts survive. This
 * test runs against a scripted binding table, so it proves the recipe and its ordering, never
 * that a given id still names the same component on a real 950 cache.
 */
public final class Native950StatsUiTest {

    private static final int ROOT = 1477;
    private static final int SKILLS_PANEL = 1466, ACTION_BAR = 1430, MINIMAP = 1465;
    /** The struct-declared alternative panel (SKILLS_TAB.md section 2), CANDIDATE. */
    private static final int SKILLS_PANEL_ALT = 320;
    private static final int SKILLS_ATTACH = 300, SKILLS_WRAPPER = 298;
    private static final int BAR_ATTACH = 70, BAR_WRAPPER = 67;
    private static final int MINIMAP_WRAPPER = 92;
    private static final int SIZE = 11145, POSITION = 13268, SHOW = 2330;
    private static final int SAVE_WORKING = 8707, SAVE_PRESET = 8708;

    /** The packed hash the layout scripts take as their component argument. */
    private static final int SKILLS_WRAPPER_HASH = (ROOT << 16) | SKILLS_WRAPPER;

    /** Enum 7716 key of the skills slot; scripts 8707/8708 take the key, not the hash. */
    private static final int SKILLS_SLOT_KEY = 0;

    /** Active layout preset, client variable 4108 = 8 (EQUIPMENT_CONTENT.md). */
    private static final int PRESET = 8;

    // The geometry the opt-in layout recipe would send. NONE of it is evidence:
    // 224x360 and the 232,80 anchor are the EQUIPMENT slot's live-verified numbers,
    // 456 is 232+224 arithmetic, and the cache geometry of 1477:298 is 224x288 at
    // 0,0. The test pins the shipped defaults so they cannot drift silently while
    // the live check that would replace them is still outstanding.
    private static final int WIDTH = 224, HEIGHT = 360, X = 456, Y = 80, X_MODE = 2, Y_MODE = 2;

    // 950 opcodes, from the VERIFIED outbound table in
    // protocol-analysis/ataraxia-950-port-plan.md PART 1. Every one of these moved from 947, and
    // an opcode is not a cosmetic constant on this client: it selects the parser. Leaving 947's
    // 8 / 103 / 121 / 105 / 66 here would have made these assertions vacuously true (two of the
    // tests below did exactly that until this file was ported), because the frames that actually
    // arrive carry 100 / 67 / 35 / 33 / 92 and no assertion would ever match them.
    // 947: IF_OPENSUB 8, IF_SETHIDE 103, RUNCLIENTSCRIPT 121, MESSAGE_GAME 105.
    private static final int IF_OPENSUB = 100, IF_SETHIDE = 67, RUNCLIENTSCRIPT = 35, MESSAGE_GAME = 33;
    /** UPDATE_STAT, 947 opcode 66 -> 950 opcode 92, fixed 6-byte body (Native950Protocol.ServerPacket). */
    private static final int UPDATE_STAT = 92;

    private static final String SHA = "1111111111111111111111111111111111111111111111111111111111111111";

    private EmbeddedChannel channel;
    private Player player;
    private Native950PacketDispatcher packets;

    @Before
    public void attach() {
        Native950PacketDispatcher.setStrict(true);
        Native950IdMap.reset();
        Native950StatsUi.resetSkillsPanelBinding();
        Native950StatsUi.resetSkillsLayout();
        channel = new EmbeddedChannel(new Native950GameTransport(() -> 0, () -> 0, Thread.currentThread()));
        player = Player.createNative950("stats-ui-test", new WorldTile(3222, 3222, 0), channel);
        packets = (Native950PacketDispatcher) player.getPackets();
    }

    @After
    public void detach() {
        Native950IdMap.reset();
        Native950PacketDispatcher.setStrict(true);
        Native950StatsUi.resetSkillsPanelBinding();
        Native950StatsUi.resetSkillsLayout();
        channel.finishAndReleaseAll();
    }

    // ------------------------------------------------------------------ tests

    @Test
    public void loginBootstrapEmitsThePanelsInOrderAndThenTheState() {
        Native950Bindings bindings = load(fullTable());
        installAllowList(bindings);
        // Geometry is a recovery-only seed. Even when its separate switch is set,
        // ordinary login must leave native workspace position and visibility alone.
        Native950StatsUi.setSkillsLayoutEnabled(true);
        Native950StatsUi ui = new Native950StatsUi(player, bindings, marker());

        ui.bootstrap();

        List<Frame> frames = drain();
        assertEquals("skills open, action bar open and reveal, state", 4, frames.size());

        // The content mounts, but wrapper state is owned by the native workspace.
        assertOpenSub(frames.get(0), SKILLS_PANEL, ROOT, SKILLS_ATTACH);
        assertOpenSub(frames.get(1), ACTION_BAR, ROOT, BAR_ATTACH);
        assertSetHide(frames.get(2), ROOT, BAR_WRAPPER, false);
        assertEquals(MESSAGE_GAME, frames.get(3).opcode);

        assertEquals(2, ui.panelsOpened());
        assertEquals(1, ui.wrappersShown());
        assertEquals(0, ui.layoutSteps());
        assertEquals(0, ui.skippedBindings());
        assertEquals(0, ui.stateFailures());
        assertEquals(0, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalStrictHits());
        assertEquals(4, packets.counters().totalSent());
    }

    /**
     * The default emitter is {@link Native950StatsUi#NONE}: the panels go out and
     * the stat burst is left to {@code Native950Session.sendNative950LoginState()},
     * which runs immediately after this bootstrap. Nothing may be emitted twice.
     *
     * <p>Port note: this test's "only panel packets" failure was a stale fixture, not a
     * behavioural break. The emitter contract never changed - the frame count is still 10 and
     * {@link Native950StatsUi#NONE} still emits nothing - but the whitelist compared against
     * 947's opcodes 8 / 103 / 121 while the writers had already moved to 100 / 67 / 35, so every
     * legitimate panel packet looked foreign. The constants at the top of this class are the fix.
     */
    @Test
    public void theDefaultEmitterLeavesTheStateToTheSession() {
        Native950Bindings bindings = load(fullTable());
        installAllowList(bindings);
        Native950StatsUi ui = new Native950StatsUi(player, bindings);

        ui.bootstrap();

        List<Frame> frames = drain();
        assertEquals("panels only", 3, frames.size());
        for (Frame frame : frames)
            assertTrue("only panel packets", frame.opcode == IF_OPENSUB || frame.opcode == IF_SETHIDE
                    || frame.opcode == RUNCLIENTSCRIPT);
        assertEquals(0, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalStrictHits());
    }

    /**
     * What an ordinary login sends: only the two required content attachments.
     * Visibility and geometry are native workspace state and must not be replaced
     * with a server-side default at login.
     */
    @Test
    public void theLayoutRecipeIsOffByDefault() {
        assertFalse(Native950StatsUi.skillsLayoutEnabled());
        Native950Bindings bindings = load(fullTable());
        installAllowList(bindings);
        Native950StatsUi ui = new Native950StatsUi(player, bindings, marker());

        ui.bootstrap();

        List<Frame> frames = drain();
        assertEquals("skills open, bar open and reveal, state", 4, frames.size());
        assertOpenSub(frames.get(0), SKILLS_PANEL, ROOT, SKILLS_ATTACH);
        assertOpenSub(frames.get(1), ACTION_BAR, ROOT, BAR_ATTACH);
        assertSetHide(frames.get(2), ROOT, BAR_WRAPPER, false);
        assertEquals(MESSAGE_GAME, frames.get(3).opcode);
        for (Frame frame : frames)
            assertNotEquals("no layout script may be sent by default", RUNCLIENTSCRIPT, frame.opcode);
        assertEquals(0, ui.layoutSteps());
        assertEquals(2, ui.panelsOpened());
        assertEquals(0, ui.skippedBindings());
        assertEquals(0, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalStrictHits());
    }

    /** The bootstrap runs once; a second call must not reopen the panels. */
    @Test
    public void bootstrapIsIdempotent() {
        Native950Bindings bindings = load(fullTable());
        installAllowList(bindings);
        Native950StatsUi.setSkillsLayoutEnabled(true);
        Native950StatsUi ui = new Native950StatsUi(player, bindings, marker());
        ui.bootstrap();
        int first = drain().size();
        ui.bootstrap();
        assertEquals(0, drain().size());
        assertEquals(4, first);
    }

    /**
     * Fail closed: a slot or interface the table does not declare is skipped with
     * a counted, logged reason. Nothing is invented and no other step is lost.
     */
    @Test
    public void undeclaredBindingsAreSkippedNotGuessed() {
        Native950Bindings bindings = load(minimapOnlyTable());
        installAllowList(bindings);
        Native950StatsUi ui = new Native950StatsUi(player, bindings, marker());

        ui.openPanels();

        List<Frame> frames = drain();
        assertEquals(0, frames.size());
        assertEquals("the skills slot and the action bar slot", 2, ui.skippedBindings());
        assertEquals(0, ui.panelsOpened());
        assertEquals(0, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalStrictHits());
    }

    /** With no validated table at all (legacy cache) nothing is emitted. */
    @Test
    public void aMissingTableEmitsNothing() {
        Native950StatsUi ui = new Native950StatsUi(player, null, marker());
        ui.openPanels();
        assertEquals(0, drain().size());
        assertEquals(1, ui.skippedBindings());
        assertEquals(0, packets.counters().totalSent());
    }

    /**
     * An id the resolver rejects never reaches the wire: the facade turns it into
     * a counted drop, and the rest of the recipe still goes out.
     */
    @Test
    public void aRejectedIdBecomesACountedDropInsteadOfBytes() {
        Native950Bindings bindings = load(fullTable());
        final Native950Bindings.Resolver allow = bindings.allowListResolver();
        Native950IdMap.install(new Native950IdMap.Resolver() {
            @Override public int interfaceId(int id) { return id == SKILLS_PANEL ? -1 : allow.interfaceId(id); }
            @Override public int componentId(int iface, int comp) { return allow.componentId(iface, comp); }
            @Override public int varp(int id) { return allow.varp(id); }
            @Override public int varbit(int id) { return allow.varbit(id); }
            @Override public int varc(int id) { return -1; }
            @Override public int script(int id) { return allow.script(id); }
            @Override public int container(int id) { return allow.container(id); }
        });
        Native950StatsUi ui = new Native950StatsUi(player, bindings, marker());

        ui.openPanels();

        List<Frame> frames = drain();
        // The id lives at b16..b17 with a +128 bias on 950, not little-endian at b4 as on 947.
        // With the 947 offset this loop passed against every possible frame, including one that
        // DID open the rejected panel.
        for (Frame frame : frames)
            assertFalse("the rejected panel must not be opened",
                    frame.opcode == IF_OPENSUB && openSubInterfaceId(frame.body) == SKILLS_PANEL);
        assertEquals(1, packets.counters().dropped("sendInterface"));
        assertEquals(0, packets.counters().totalStrictHits());
        assertTrue(channel.isActive());

        // The server-side bookkeeping must not claim the panel is open either:
        // InterfaceManager.registerNativeOpen backs containsInterface and
        // getInterfaceParentId, which the 910 handlers gate on.
        assertEquals("a dropped IF_OPENSUB may not register an open interface",
                -1, player.getInterfaceManager().getInterfaceParentId(SKILLS_PANEL));
        assertFalse(player.getInterfaceManager().containsInterface(SKILLS_PANEL));
        assertEquals("the drop is counted as a skip", 1, ui.skippedBindings());
        assertEquals("only the action bar counts as opened", 1, ui.panelsOpened());
        // The action bar, whose id the resolver accepts, still opens and registers.
        assertTrue(player.getInterfaceManager().containsInterface(ACTION_BAR));
    }

    /**
     * The 1466-versus-320 switch really switches: a DECLARED alternative name opens
     * that entry's interface id, not the default one.
     */
    @Test
    public void aDeclaredAlternativeSkillsPanelNameOpensThatInterface() {
        assertEquals("skills", Native950StatsUi.skillsPanelBinding());
        Native950Bindings bindings = load(twoPanelTable());
        installAllowList(bindings);
        Native950StatsUi.setSkillsPanelBinding("skills_alt");
        Native950StatsUi ui = new Native950StatsUi(player, bindings, marker());

        ui.openPanels();

        List<Frame> frames = drain();
        assertOpenSub(frames.get(0), SKILLS_PANEL_ALT, ROOT, SKILLS_ATTACH);
        assertEquals(0, ui.skippedBindings());
        assertEquals("the alternative skills panel and the action bar", 2, ui.panelsOpened());
        assertTrue(player.getInterfaceManager().containsInterface(SKILLS_PANEL_ALT));
        assertFalse("the default must not be opened as well",
                player.getInterfaceManager().containsInterface(SKILLS_PANEL));
    }

    /**
     * An explicitly configured name the table does not declare is a SKIP, not a
     * fallback to the default. The table used here DOES declare the default
     * "skills" interface, so the only possible cause of the skip is the switch:
     * a fallback would silently re-run the very comparison the switch exists to
     * make, and an operator would read the default's behaviour as the
     * alternative's result.
     */
    @Test
    public void anUndeclaredConfiguredSkillsPanelNameIsSkippedNotFallenBackOn() {
        Native950Bindings bindings = load(fullTable());
        installAllowList(bindings);
        Native950StatsUi.setSkillsPanelBinding("no_such_panel");
        Native950StatsUi ui = new Native950StatsUi(player, bindings, marker());

        ui.openPanels();

        List<Frame> frames = drain();
        // Same 950 offset correction as in aRejectedIdBecomesACountedDropInsteadOfBytes: b16..b17
        // biased, not b4 little-endian.
        for (Frame frame : frames)
            assertFalse("the default panel must not be opened behind the operator's back",
                    frame.opcode == IF_OPENSUB && openSubInterfaceId(frame.body) == SKILLS_PANEL);
        assertEquals("only the action bar was opened", 1, ui.panelsOpened());
        assertEquals("the configured-but-undeclared panel", 1, ui.skippedBindings());
        assertEquals(-1, player.getInterfaceManager().getInterfaceParentId(SKILLS_PANEL));
        assertEquals(0, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalStrictHits());
    }

    /**
     * {@link Native950StatsUi#ENGINE_STATE}, the burst this class carries for a
     * caller that owns the ordering, must walk every row the 947 skills panel
     * draws - all 29 stats the cache stat definitions declare (enum 680 names 29,
     * script 4708 sums 29 base levels for the tab's total), not the 27 the 910
     * model had. Archaeology (27) and Necromancy (28) are ordinary rows in it.
     *
     * <p>Identity id resolution is in force here (no allow-list is installed), so
     * the vitals that follow the stats are not dropped and the loop is the only
     * thing under test. Only {@code sendInitialState} runs, so no panel packet is
     * mixed into the count.
     *
     * <p>Port note: "expects 29 and sees none" was a stale fixture, not a broken burst.
     * {@code ENGINE_STATE} still calls {@code sendSkillLevel} once per modelled stat and the
     * dispatcher still builds one UPDATE_STAT for each; the filter was matching 947's opcode 66
     * against frames carrying 950's 92, so the loop counted nothing and both the "sent twice"
     * guard and the set comparison had no input. Had it matched by accident, {@link #statSkill}
     * would still have read the 947 offset - the last byte, which on 950 is the low byte of the
     * experience - and produced a set of plausible-looking wrong indices. Both were corrected.
     *
     * <p>{@code Skills.SKILL_COUNT == 29} on 950 is itself unsettled (port plan PART 1, GUESS 8:
     * the 950 stat-defaults table is the same 785 bytes but a different digest). This test
     * deliberately asserts against the model constant rather than a literal 29, so it pins
     * "one packet per modelled stat" and not the count itself.
     */
    @Test
    public void theEngineStateBurstCoversEveryStatTheCacheDefines() {
        Native950StatsUi ui = new Native950StatsUi(player, load(fullTable()), Native950StatsUi.ENGINE_STATE);

        ui.sendInitialState();

        java.util.Set<Integer> statsSeen = new java.util.TreeSet<Integer>();
        for (Frame frame : drain())
            if (frame.opcode == UPDATE_STAT)
                assertTrue("UPDATE_STAT sent twice for stat " + statSkill(frame), statsSeen.add(statSkill(frame)));
        java.util.Set<Integer> expected = new java.util.TreeSet<Integer>();
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) expected.add(skill);
        assertEquals("one UPDATE_STAT per modelled stat", expected, statsSeen);
        assertEquals(Skills.SKILL_COUNT, statsSeen.size());
        assertTrue("27 Archaeology is a row in the burst", statsSeen.contains(Skills.ARCHAEOLOGY));
        assertTrue("28 Necromancy is a row in the burst", statsSeen.contains(Skills.NECROMANCY));
        assertEquals("the burst must not fail", 0, ui.stateFailures());
    }

    // ------------------------------------------------------------------ helpers

    /**
     * The skill index out of a 950 UPDATE_STAT body (plan A4). The whole packet reversed: 947
     * sent {@code experience, level, stat} and the index was the LAST byte; 950 sends
     * {@code -skill, -level, experience} and the index is the FIRST.
     *
     * <p>950 parser 0x140141290: b0 is negated at 0x1401412c6 and the result scales the 24-byte
     * stat entry ({@code lea rcx,[rbp*2]; add rcx,rbp; lea rbx,[rcx*8]} at 0x140141306), so b0 is
     * the skill; b1 is negated at 0x1401412d4 and stored at {@code entry+0x14} (0x14014132e), so
     * b1 is the level; b2..b5 are a dword with {@code bswap edx} (0x1401412e4..0x1401412f2), so
     * the experience is big-endian and last.
     *
     * <p>Reading the 947 offset here does not fail loudly - it returns a plausible small integer
     * taken from the top byte of the experience - which is exactly why this helper exists instead
     * of an inline index.
     */
    private static int statSkill(Frame frame) { return (-(frame.body[0] & 0xff)) & 0xff; }

    /** A state emitter that writes one recognisable verified packet. */
    private static Native950StatsUi.StateEmitter marker() {
        return new Native950StatsUi.StateEmitter() {
            @Override public void emit(Player player) { player.getPackets().sendGameMessage("state"); }
        };
    }

    private void installAllowList(Native950Bindings bindings) {
        final Native950Bindings.Resolver allow = bindings.allowListResolver();
        Native950IdMap.install(new Native950IdMap.Resolver() {
            @Override public int interfaceId(int id) { return allow.interfaceId(id); }
            @Override public int componentId(int iface, int comp) { return allow.componentId(iface, comp); }
            @Override public int varp(int id) { return allow.varp(id); }
            @Override public int varbit(int id) { return allow.varbit(id); }
            @Override public int varc(int id) { return -1; }
            @Override public int script(int id) { return allow.script(id); }
            @Override public int container(int id) { return allow.container(id); }
        });
    }

    private static Native950Bindings load(String json) {
        return Native950Bindings.load(new StringReader(json), new FakeCache());
    }

    // The four scripted tables below take their "revision" from Native950Bindings.REVISION rather
    // than a literal. Native950Bindings.load rejects a table whose revision does not match that
    // constant, and the constant is still 947 while the binding layer waits its turn in the port
    // (plan Stage 3 moves it to 950 together with the ui-bindings-950.json resource). Hard-coding
    // either number would make these tests fail for a reason that has nothing to do with the HUD
    // bootstrap the moment the other half of that change lands.
    private static String fullTable() {
        return "{\"revision\": " + Native950Bindings.REVISION + ", \"rootInterface\": \"root\", \"slotEnum\": 7716,"
                + " \"attachParam\": 3505, \"wrapperParam\": 3503,"
                + " \"slots\": {"
                + "  \"skills\": {\"enumKey\": 0},"
                + "  \"action_bar\": {\"enumKey\": 1003},"
                + "  \"minimap\": {\"enumKey\": 1004}},"
                + " \"interfaces\": {"
                + "  \"root\": {\"id\": 1477, \"minComponents\": 923, \"components\": {\"root_layer\": 0}},"
                + "  \"skills\": {\"id\": 1466, \"minComponents\": 15, \"components\": {\"cells\": 7}},"
                + "  \"action_bar\": {\"id\": 1430, \"minComponents\": 269, \"components\": {\"hitpoints\": 7}},"
                + "  \"minimap\": {\"id\": 1465, \"minComponents\": 43, \"components\": {\"run_orb\": 14}}},"
                + " \"scripts\": {" + scripts() + "}}";
    }

    /** Adds a second, differently named skills interface so the switch has a target. */
    private static String twoPanelTable() {
        return "{\"revision\": " + Native950Bindings.REVISION + ", \"rootInterface\": \"root\", \"slotEnum\": 7716,"
                + " \"attachParam\": 3505, \"wrapperParam\": 3503,"
                + " \"slots\": {"
                + "  \"skills\": {\"enumKey\": 0},"
                + "  \"action_bar\": {\"enumKey\": 1003},"
                + "  \"minimap\": {\"enumKey\": 1004}},"
                + " \"interfaces\": {"
                + "  \"root\": {\"id\": 1477, \"minComponents\": 923, \"components\": {\"root_layer\": 0}},"
                + "  \"skills\": {\"id\": 1466, \"minComponents\": 15, \"components\": {\"cells\": 7}},"
                + "  \"skills_alt\": {\"id\": 320, \"minComponents\": 18, \"components\": {\"cells\": 2}},"
                + "  \"action_bar\": {\"id\": 1430, \"minComponents\": 269, \"components\": {\"hitpoints\": 7}},"
                + "  \"minimap\": {\"id\": 1465, \"minComponents\": 43, \"components\": {\"run_orb\": 14}}},"
                + " \"scripts\": {" + scripts() + "}}";
    }

    /** Declares the minimap slot only: the M3 panels are absent from the table. */
    private static String minimapOnlyTable() {
        return "{\"revision\": " + Native950Bindings.REVISION + ", \"rootInterface\": \"root\", \"slotEnum\": 7716,"
                + " \"slots\": {\"minimap\": {\"enumKey\": 1004}},"
                + " \"interfaces\": {"
                + "  \"root\": {\"id\": 1477, \"minComponents\": 923, \"components\": {\"root_layer\": 0}},"
                + "  \"minimap\": {\"id\": 1465, \"minComponents\": 43, \"components\": {\"run_orb\": 14}}},"
                + " \"scripts\": {" + scripts() + "}}";
    }

    /** Declares the skills slot but no skills interface under any accepted name. */
    private static String skillsSlotOnlyTable() {
        return "{\"revision\": " + Native950Bindings.REVISION + ", \"rootInterface\": \"root\", \"slotEnum\": 7716,"
                + " \"slots\": {\"skills\": {\"enumKey\": 0}},"
                + " \"interfaces\": {"
                + "  \"root\": {\"id\": 1477, \"minComponents\": 923, \"components\": {\"root_layer\": 0}}},"
                + " \"scripts\": {" + scripts() + "}}";
    }

    private static String scripts() {
        int[] ids = { SIZE, POSITION, SHOW, SAVE_WORKING, SAVE_PRESET };
        String[] names = { "component_size", "component_position", "component_show",
                "layout_save_working", "layout_save_preset" };
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) out.append(',');
            out.append('"').append(names[i]).append("\": {\"id\": ").append(ids[i])
                    .append(", \"sha256\": \"").append(SHA).append("\"}");
        }
        return out.toString();
    }

    /** The 947 evidence for exactly the groups and slots this test declares. */
    private static final class FakeCache implements Native950CacheReader {
        private final Map<Integer, Integer> interfaceCounts = new HashMap<Integer, Integer>();
        private final Map<Integer, Integer> slotStructs = new HashMap<Integer, Integer>();
        private final Map<String, Integer> structParams = new HashMap<String, Integer>();

        FakeCache() {
            interfaceCounts.put(ROOT, 923);
            interfaceCounts.put(SKILLS_PANEL, 15);
            interfaceCounts.put(SKILLS_PANEL_ALT, 18);
            interfaceCounts.put(ACTION_BAR, 269);
            interfaceCounts.put(MINIMAP, 43);
            slot(0, 21293, SKILLS_ATTACH, SKILLS_WRAPPER);
            slot(1003, 21277, BAR_ATTACH, BAR_WRAPPER);
            slot(1004, 21278, 94, MINIMAP_WRAPPER);
        }

        private void slot(int key, int struct, int attach, int wrapper) {
            slotStructs.put(key, struct);
            structParams.put(struct + ":3505", ROOT << 16 | attach);
            structParams.put(struct + ":3503", ROOT << 16 | wrapper);
        }

        private static boolean isScript(int group) {
            return group == SIZE || group == POSITION || group == SHOW
                    || group == SAVE_WORKING || group == SAVE_PRESET;
        }

        @Override public boolean groupExists(int index, int group) {
            if (index == 3) return interfaceCounts.containsKey(group);
            if (index == 12) return isScript(group);
            return index == 2 && (group == 5 || group == 60 || group == 69);
        }

        @Override public int fileCount(int index, int group) {
            if (index == 3) { Integer c = interfaceCounts.get(group); return c == null ? -1 : c; }
            if (index == 2 && group == 60) return 12798;
            if (index == 2 && group == 69) return 60682;
            return groupExists(index, group) ? 1 : -1;
        }

        @Override public boolean fileExists(int index, int group, int file) {
            if (file < 0) return false;
            if (index == 3) return groupExists(3, group) && file < interfaceCounts.get(group);
            if (index == 12) return isScript(group) && file == 0;
            if (index == 2 && group == 60) return file < 12798;
            return false;
        }

        @Override public String sha256(int index, int group, int file) {
            return fileExists(index, group, file) ? SHA : null;
        }

        @Override public int enumInt(int enumId, int key) {
            if (enumId != 7716) return -1;
            Integer struct = slotStructs.get(key);
            return struct == null ? -1 : struct;
        }

        @Override public int structInt(int structId, int param) {
            Integer value = structParams.get(structId + ":" + param);
            return value == null ? -1 : value;
        }

        @Override public int[] varbit(int varbitId) { return null; }
    }

    // ------------------------------------------------------------------ frames

    private static final class Frame {
        final int opcode;
        final byte[] body;
        Frame(int opcode, byte[] body) { this.opcode = opcode; this.body = body; }
    }

    /** Reads every framed packet the session queued; the ISAAC stream is zeroed. */
    private List<Frame> drain() {
        channel.flush();
        List<Frame> frames = new ArrayList<Frame>();
        for (ByteBuf buffer = channel.readOutbound(); buffer != null; buffer = channel.readOutbound()) {
            try {
                byte[] bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
                frames.add(parse(bytes));
            } finally {
                buffer.release();
            }
        }
        return frames;
    }

    /**
     * Splits one framed packet into opcode and body.
     *
     * <p>The frame widths are the 950 descriptor sizes, not 947's: {@code 950-server-sizes.toml}
     * gives 35 = -2 (u16 length) for RUNCLIENTSCRIPT and 33 = -1 (u8 length) for MESSAGE_GAME,
     * while every other opcode this test sees (100, 67, 92) is fixed-width and carries no prefix.
     * Sizes do NOT port as a column - the two CLIENT_SETVARCSTR rows swap frame width between the
     * revisions (plan A3) - so each one has to be read off the 950 table at its own opcode.
     */
    private static Frame parse(byte[] framed) {
        int opcode = framed[0] & 0xff;
        int offset, length;
        if (opcode == RUNCLIENTSCRIPT) { length = ((framed[1] & 0xff) << 8) | (framed[2] & 0xff); offset = 3; }
        else if (opcode == MESSAGE_GAME) { length = framed[1] & 0xff; offset = 2; }
        else { length = framed.length - 1; offset = 1; }
        byte[] body = new byte[length];
        System.arraycopy(framed, offset, body, 0, length);
        return new Frame(opcode, body);
    }

    /**
     * IF_OPENSUB, 950 opcode 100, fixed 23 bytes. No field kept both its 947 offset and its 947
     * transform, so this reader is a full rewrite, not a re-index.
     *
     * <p>950 parser 0x1400fb790 (a 950 VA; the 947 parser lived elsewhere and its address
     * disassembles into unrelated code in this image):
     * <ul>
     * <li>b0..b3 the parent hash as a PLAIN big-endian u32 - a dword load at 0x1400fb7c5 followed
     *     by the {@code _byteswap_ulong} idiom at 0x1400fb7cb..0x1400fb7e6. 947 put the parent at
     *     the END of the packet under an intv1 permutation.</li>
     * <li>b4..b15 skipped: the cursor jumps {@code lea rdx,[rcx+0xe]} at 0x1400fb7e9 and only
     *     b16/b17 are read from the far end of that jump.</li>
     * <li>b16 = id + 128 ({@code sub eax,ecx} with ecx=0x80 at 0x1400fb7fc), b17 = id &gt;&gt;&gt; 8
     *     ({@code shl eax,8} at 0x1400fb807) - a little-endian id whose LOW byte is biased. 947
     *     carried the id at b4..b5 unbiased, which is why reading offset 4 here finds zeros.</li>
     * <li>b18 = 128 - walkable ({@code sub cl, byte [rdx+r9]} at 0x1400fb81c).</li>
     * <li>b19..b22 skipped ({@code add rax,4} at 0x1400fb814) to reach the fixed width of 23.</li>
     * </ul>
     * The two skipped runs are asserted to be zero as well. They are the only slack in a
     * positional packet: if a future writer starts putting something there, the client will read
     * it as part of no field at all and the mistake is invisible without this check.
     */
    private static void assertOpenSub(Frame frame, int interfaceId, int parentInterface, int component) {
        assertEquals("IF_OPENSUB", IF_OPENSUB, frame.opcode);
        assertEquals(23, frame.body.length);
        assertEquals("IF_OPENSUB interface id", interfaceId, openSubInterfaceId(frame.body));
        assertEquals("IF_OPENSUB parent hash", (parentInterface << 16) | component, intAt(frame.body, 0));
        // Every HUD slot is opened walkable (Native950StatsUi.attach passes clickThrough=true),
        // so the client must read 128 - 1 out of b18.
        assertEquals("IF_OPENSUB walkable flag", 128 - 1, frame.body[18] & 0xff);
        for (int at = 4; at < 16; at++)
            assertEquals("IF_OPENSUB b" + at + " is unread padding", 0, frame.body[at] & 0xff);
        for (int at = 19; at < 23; at++)
            assertEquals("IF_OPENSUB b" + at + " is unread padding", 0, frame.body[at] & 0xff);
    }

    /** The 950 IF_OPENSUB interface id: little-endian at b16..b17 with +128 on the low byte. */
    private static int openSubInterfaceId(byte[] body) {
        return (((body[16] & 0xff) - 128) & 0xff) | ((body[17] & 0xff) << 8);
    }

    /**
     * IF_SETHIDE, 950 opcode 67, fixed 5 bytes. The two halves of the packet swapped ends.
     *
     * <p>950 parser 0x140107590: the hash comes FIRST as intv1 - 0x1401075b5..0x1401075db
     * assembles {@code (b2<<24)|(b3<<16)|(b0<<8)|b1} - and the flag is the LAST byte, read plain
     * at 0x1401075ea with no bias. 947 led with the flag under a +128 bias and followed with a
     * plain little-endian hash.
     *
     * <p>(Native950Protocol's {@code IF_SETHIDE} javadoc still calls this permutation "intv2".
     * The bytes the writer emits and the parser above are intv1; the javadoc wording is stale,
     * the encoding is not.)
     */
    private static void assertSetHide(Frame frame, int parentInterface, int component, boolean hidden) {
        assertEquals("IF_SETHIDE", IF_SETHIDE, frame.opcode);
        assertEquals(5, frame.body.length);
        int hash = ((frame.body[2] & 0xff) << 24) | ((frame.body[3] & 0xff) << 16)
                | ((frame.body[0] & 0xff) << 8) | (frame.body[1] & 0xff);
        assertEquals("IF_SETHIDE component hash", (parentInterface << 16) | component, hash);
        assertEquals("IF_SETHIDE flag is plain, not +128", hidden ? 1 : 0, frame.body[4] & 0xff);
    }

    /**
     * RUNCLIENTSCRIPT: the type string in natural order, a zero terminator, the
     * argument VALUES in reverse order, then the script id.
     *
     * <p>The values are asserted here, not just the envelope: a size call sent as
     * (360, 224, ...), the attach hash 1477:300 passed where the wrapper hash
     * 1477:298 belongs, a preset save sent as (8, 0), or a future edit routing the
     * call through the argument-reversing {@code sendRunScript} would all leave
     * the type string and the script id untouched.
     *
     * <p>The body itself did not change between the revisions - only the opcode (121 -&gt; 35)
     * and the frame width. The port plan lists "RUNCLIENTSCRIPT unchanged" as a GUESS because
     * the 950 argument loop had not been walked; it has been now. 950 parser 0x1400f73c0 scans
     * the descriptor to its NUL at 0x1400f7413..0x1400f741a, then iterates the type characters
     * from {@code len-1} down to 0 ({@code sub edi,1; js} at 0x1400f7521) - hence the reversed
     * values - dispatching on 's' at 0x1400f754a and 'l' at 0x1400f760d with integers as the
     * fall-through, and finally reads the script id as a byte-swapped dword at
     * 0x1400f7724..0x1400f775a. Long argument lists are fine; the shape above is exact.
     *
     * @param naturalOrderArgs the arguments as the cs2 script receives them
     */
    private static void assertScript(Frame frame, int script, String types, Object... naturalOrderArgs) {
        assertEquals("RUNCLIENTSCRIPT", RUNCLIENTSCRIPT, frame.opcode);
        StringBuilder actual = new StringBuilder();
        int at = 0;
        while (at < frame.body.length && frame.body[at] != 0) actual.append((char) (frame.body[at++] & 0xff));
        assertEquals("script " + script + " argument types", types, actual.toString());
        assertEquals("the fixture must name one expected value per declared type",
                types.length(), naturalOrderArgs.length);
        at++; // the type terminator

        for (int i = naturalOrderArgs.length - 1; i >= 0; i--) {
            if (types.charAt(i) == 's') {
                StringBuilder text = new StringBuilder();
                while (frame.body[at] != 0) text.append((char) (frame.body[at++] & 0xff));
                at++;
                assertEquals("script " + script + " argument " + i, naturalOrderArgs[i], text.toString());
            } else {
                assertEquals("script " + script + " argument " + i,
                        ((Integer) naturalOrderArgs[i]).intValue(), intAt(frame.body, at));
                at += 4;
            }
        }
        assertEquals("script " + script + " id", script, intAt(frame.body, at));
        assertEquals("script " + script + " must carry no trailing bytes", frame.body.length, at + 4);
    }

    /**
     * The flag that makes every later retirement reversible.
     *
     * <p>There is no way to recover a half-wired interface from inside the client, so the plan
     * retires the login force-open by flipping this default rather than deleting the code. The
     * default must therefore be exactly today's behaviour, and the property must be read late
     * enough that a restart is sufficient to change it.
     */
    @Test
    public void workspaceVisibilityDefaultsToClientOwnershipAndIsReadLate() {
        String previous = System.getProperty(Native950StatsUi.FORCE_OPEN_PROPERTY);
        try {
            System.clearProperty(Native950StatsUi.FORCE_OPEN_PROPERTY);
            assertFalse("ordinary login must not force a saved panel visible",
                    Native950StatsUi.forceOpenPanels());
            System.setProperty(Native950StatsUi.FORCE_OPEN_PROPERTY, "false");
            assertFalse(Native950StatsUi.forceOpenPanels());
            System.setProperty(Native950StatsUi.FORCE_OPEN_PROPERTY, "true");
            assertTrue("the explicit recovery switch is read late", Native950StatsUi.forceOpenPanels());
            System.setProperty(Native950StatsUi.FORCE_OPEN_PROPERTY, "nonsense");
            assertFalse("an unparseable value must fail closed, not open panels",
                    Native950StatsUi.forceOpenPanels());
        } finally {
            if (previous == null) System.clearProperty(Native950StatsUi.FORCE_OPEN_PROPERTY);
            else System.setProperty(Native950StatsUi.FORCE_OPEN_PROPERTY, previous);
        }
    }

    private static int intAt(byte[] body, int offset) {
        return ((body[offset] & 0xff) << 24) | ((body[offset + 1] & 0xff) << 16)
                | ((body[offset + 2] & 0xff) << 8) | (body[offset + 3] & 0xff);
    }

}
