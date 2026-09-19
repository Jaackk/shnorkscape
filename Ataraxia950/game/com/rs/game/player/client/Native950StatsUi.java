package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.ui.Native950Bindings;
import com.rs.game.player.content.InterfaceManager;
import com.rs.network.packet.PacketDispatcher;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Java-side owner of the M3 HUD panels: the skills (stats) tab, the main action
 * bar and the minimap orbs.
 *
 * <p>The Kotlin handoff ({@code Ataraxia947Handoff.interfaceBootstrap}) opens the
 * root window 1477 and five slots (game view, minimap, backpack, worn equipment,
 * all chat) and then hides every other root wrapper. The skills wrapper and the
 * action-bar wrapper are among the hidden ones, so M3 has to attach and unhide
 * them here, after the session is ready and the game transport is in the
 * pipeline. Every id comes from the validated binding table
 * ({@link Native950Bindings}); nothing is hard-coded, and a name the table does
 * not declare is skipped with a counted, once-logged line naming it. Every byte
 * leaves through the packet facade ({@link Native950PacketDispatcher}), so the
 * allow-list resolver still gets the last word on each individual id and the
 * session counters show what was sent.
 *
 * <p>Order matters and is fixed by the cache evidence:
 * <ol>
 * <li><b>Skills slot</b> (enum 7716 key 0 -&gt; struct 21293, attach 1477:300,
 *     wrapper 1477:298): IF_OPENSUB and IF_SETHIDE(false). Both ids are CONFIRMED
 *     and both are sent on every login. The equipment-slot layout recipe - size,
 *     position, show, save-to-working-layout, save-to-preset - is NOT sent by
 *     default: {@code SKILLS_TAB.md} section 8 says explicitly that "whether
 *     1477:298 needs the equipment-style size/position/show and 8707/8708 layout
 *     save was not re-tested for slot 0", and the box this class would send
 *     (224x360 at 456,80) is not evidence either - 224x360 and the 232,80 anchor
 *     are the EQUIPMENT slot's live-verified numbers
 *     ({@code EQUIPMENT_CONTENT.md}), 456 is 232+224 arithmetic, and the cache
 *     geometry recorded for 1477:298 itself is 224x288 at 0,0. Script 8708 writes
 *     the slot into the client's persistent layout preset 8, so a guessed box
 *     would be committed to saved client state on the first login. The recipe
 *     therefore sits behind {@value #SKILLS_LAYOUT_PROPERTY} (default off) with
 *     every number overridable from a system property, so one live session can
 *     settle the geometry without a rebuild. See {@code notes/M3-ui.md}.</li>
 * <li><b>Action bar</b> (key 1003 -&gt; struct 21277, attach 1477:70, wrapper
 *     1477:67): IF_OPENSUB and IF_SETHIDE(false) only. Unlike the skills panel,
 *     1430:0 carries an onLoad hook, {@code 8109(1003)}, which runs the slot's
 *     own layout helpers, so no explicit geometry is sent
 *     ({@code ORBS_AND_VARS.md} section 5).</li>
 * <li><b>Minimap wrapper</b> (key 1004, wrapper 1477:92): IF_SETHIDE(false). The
 *     handoff already attached 1465 to 1477:94; only its wrapper needs to be
 *     visible for the run orb and the energy bar.</li>
 * <li><b>Initial state</b>: the skills panel has no onLoad hook at all - it is
 *     built by scripts 8488/8489 from the component's onStatTransmit hook - so
 *     the UPDATE_STAT burst must arrive <em>after</em> the panel is attached,
 *     never before. The vitals vars follow, and the action bar's four bars pick
 *     them up through their own varp/stat transmit lists. That burst is emitted
 *     by {@code Native950Session.sendNative950LoginState()}, which
 *     {@code Native950Session.ready()} calls immediately after
 *     {@code Native950Interactions.bootstrap()} - i.e. after this class has
 *     attached the panels - so the default {@link StateEmitter} here is
 *     {@link #NONE} and nothing is emitted twice. {@link #ENGINE_STATE} carries
 *     the same burst for a caller that wants this class to own the order.</li>
 * </ol>
 *
 * <p>Which interface the skills slot should hold is still CANDIDATE: 1466 (bound
 * to slot key 0 by cache script 2141) and 320 (declared by struct 21293 params
 * 3514..3517) both exist and carry the same builder hook, gated by a client-side
 * mode value. 1466 is the default; {@link #skillsPanelBinding()} names the table
 * entry and can be switched with the {@value #SKILLS_PANEL_PROPERTY} system
 * property so a single live login settles it. See {@code notes/M3-ui.md}.
 */
public final class Native950StatsUi {

    /** System property naming the binding-table entry used for the skills panel. */
    public static final String SKILLS_PANEL_PROPERTY = "ataraxia947.skillsPanel";

    /** Default skills panel binding: interface 1466, the one script 2141 case 0 rebuilds. */
    public static final String SKILLS_PANEL_DEFAULT = "skills";

    private static volatile String skillsPanel =
            System.getProperty(SKILLS_PANEL_PROPERTY, SKILLS_PANEL_DEFAULT);

    /**
     * Enables the skills-wrapper layout recipe (scripts 11145 / 13268 / 2330 and
     * the 8707 / 8708 layout saves). Default OFF, because whether slot 0 needs the
     * recipe at all is a CANDIDATE row and the geometry it would send is invented;
     * {@code -Dataraxia947.skillsLayout=true} turns it on for the live session
     * that settles both.
     */
    public static final String SKILLS_LAYOUT_PROPERTY = "ataraxia947.skillsLayout";

    /**
     * {@code -Dataraxia950.workspace.forceOpenPanels=true} enables the old recovery seed that
     * explicitly reveals native panels after mounting them.
     *
     * <p>Default <b>false</b>. Normal login still mounts the content required by the client, but
     * leaves visibility, position, docking, and tab state to the native workspace manager.
     * The recovery seed is intentionally JVM-wide and is reserved for repairing an empty or
     * corrupt client workspace without changing ordinary player sessions.
     *
     * <p>It is read per call rather than cached, so a restart changes behaviour without a
     * rebuild. It is still JVM-wide: making panel policy per-session is its own piece of work.
     */
    public static final String FORCE_OPEN_PROPERTY = "ataraxia950.workspace.forceOpenPanels";

    /** Whether the recovery seed explicitly reveals mounted panels. See {@link #FORCE_OPEN_PROPERTY}. */
    public static boolean forceOpenPanels() {
        return Boolean.parseBoolean(System.getProperty(FORCE_OPEN_PROPERTY, "false"));
    }

    /** Property prefix for the six geometry numbers, e.g. {@code ataraxia947.skillsLayout.x}. */
    private static final String GEOMETRY_PREFIX = SKILLS_LAYOUT_PROPERTY + ".";

    private static volatile boolean skillsLayout =
            Boolean.parseBoolean(System.getProperty(SKILLS_LAYOUT_PROPERTY, "false"));

    /**
     * Geometry of the skills wrapper, in the argument shape scripts 11145 (size)
     * and 13268 (position) take. NONE of these six numbers is evidence: 224x360
     * and the 232,80 anchor are the EQUIPMENT slot's live-verified box
     * ({@code EQUIPMENT_CONTENT.md}), 456 is 232+224 arithmetic meant to put the
     * two panels side by side, and the cache geometry recorded for 1477:298
     * itself is 224x288 at 0,0 ({@code SKILLS_TAB.md} section 1). They are a
     * starting point for the live check only, they are read only when
     * {@link #SKILLS_LAYOUT_PROPERTY} is on, and each one can be overridden from
     * a system property so a live session can iterate without a rebuild.
     */
    static final int SKILLS_WIDTH = geometry("width", 224), SKILLS_HEIGHT = geometry("height", 360);
    static final int SKILLS_X = geometry("x", 456), SKILLS_Y = geometry("y", 80);
    static final int SKILLS_X_MODE = geometry("xMode", 2), SKILLS_Y_MODE = geometry("yMode", 2);

    private static int geometry(String name, int fallback) {
        try {
            return Integer.parseInt(System.getProperty(GEOMETRY_PREFIX + name, String.valueOf(fallback)));
        } catch (NumberFormatException malformed) {
            return fallback;
        }
    }

    /** Active layout preset (client variable 4108 = 8, EQUIPMENT_CONTENT.md). */
    static final int LAYOUT_PRESET = 8;

    // Accepted binding-table names, in preference order. The first entry is the
    // documented name; the aliases only exist so a differently named but equally
    // validated table entry is still found instead of being silently skipped.
    private static final String[] SKILLS_NAMES = {"skills", "skills_tab", "stats"};
    private static final String[] ACTION_BAR_NAMES = {"action_bar", "actionbar", "main_action_bar"};
    private static final String[] MINIMAP_NAMES = {"minimap"};

    private static final String SCRIPT_SIZE = "component_size", SCRIPT_POSITION = "component_position";
    private static final String SCRIPT_SHOW = "component_show";
    private static final String SCRIPT_SAVE_WORKING = "layout_save_working", SCRIPT_SAVE_PRESET = "layout_save_preset";

    private static final int LOG_ONCE_LIMIT = 128;
    private static final Set<String> LOGGED = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /**
     * The initial stat and vital burst, isolated behind an interface so the
     * ordering can be asserted without the engine's refresh paths.
     */
    public interface StateEmitter {
        void emit(Player player);
    }

    /**
     * The default: this class opens the panels and emits nothing itself, because
     * {@code Native950Session.ready()} calls its own
     * {@code sendNative950LoginState()} immediately after
     * {@code Native950Interactions.bootstrap()}. The panels are therefore already
     * attached when the stat burst goes out, which is the ordering the skills tab
     * needs; emitting it here as well would only double every var.
     */
    public static final StateEmitter NONE = new StateEmitter() {
        @Override public void emit(Player player) { }
    };

    /**
     * The full initial burst, available for a caller that owns the ordering
     * itself (and used to prove the ordering in the unit test): the per-skill
     * UPDATE_STAT burst the skills panel needs to draw, then the vitals the
     * action bar's four bars and the run orb read.
     *
     * <p>Every call here is a single-var facade call. {@code Skills.init()} is
     * deliberately NOT used: besides the stat burst it re-binds the XP tracker
     * and XP popup slots (enum 7716 keys 1015 and 1026), both of which are
     * CANDIDATE in the verified evidence and must not be bound before a live
     * test.
     *
     * <p>The loop covers every stat the engine models, which the 947 cache stat
     * definitions put at 29 - the same 29 rows the skills panel draws (enum 680)
     * and the same 29 the client's own total-level script 4708 sums. It reads
     * {@code Skills.SKILL_NAME.length} rather than a literal so it can only ever
     * disagree with the model by the model changing. Archaeology (27) and
     * Necromancy (28) are ordinary rows in it: they are transmitted like any
     * other stat, at level 1 with no experience for a fresh character, instead of
     * being left at the client's stat-table initialiser defaults. An id the
     * binding table does not declare is still a counted drop inside the facade,
     * so nothing here can put an out-of-range stat on the wire.
     */
    public static final StateEmitter ENGINE_STATE = new StateEmitter() {
        @Override
        public void emit(final Player player) {
            final PacketDispatcher packets = player.getPackets();
            for (int skill = 0; skill < Skills.SKILL_NAME.length; skill++) {
                final int id = skill;
                guard("sendSkillLevel(" + id + ")", new Runnable() {
                    @Override public void run() { packets.sendSkillLevel(id); }
                });
            }
            guard("refreshHitPoints", new Runnable() {
                @Override public void run() { player.refreshHitPoints(); }
            });
            guard("refreshPrayerPoints", new Runnable() {
                @Override public void run() {
                    if (player.getPrayer() != null) player.getPrayer().refreshPrayerPoints();
                }
            });
            guard("refreshSpecialAttackPercentage", new Runnable() {
                @Override public void run() {
                    if (player.getCombatDefinitions() != null) player.getCombatDefinitions().refreshSpecialAttackPercentage();
                }
            });
            guard("refreshAutoRelatie", new Runnable() {
                @Override public void run() {
                    if (player.getCombatDefinitions() != null) player.getCombatDefinitions().refreshAutoRelatie();
                }
            });
            guard("sendRunButtonConfig", new Runnable() {
                @Override public void run() { player.sendRunButtonConfig(); }
            });
            guard("sendRunEnergy", new Runnable() {
                @Override public void run() { packets.sendRunEnergy(); }
            });
        }
    };

    private final Player player;
    private final Native950Bindings bindings;
    private final StateEmitter state;
    private boolean bootstrapped;
    private long panelsOpened, wrappersShown, layoutSteps, skipped, stateFailures;

    public Native950StatsUi(Player player, Native950Bindings bindings) {
        this(player, bindings, NONE);
    }

    public Native950StatsUi(Player player, Native950Bindings bindings, StateEmitter state) {
        if (player == null) throw new IllegalArgumentException("player is required");
        if (!player.isNative950())
            throw new IllegalStateException("Native950StatsUi is only for native 947 players");
        this.player = player;
        this.bindings = bindings;
        this.state = state == null ? NONE : state;
    }

    /** The table entry the skills slot is opened with; 1466 by default. */
    public static String skillsPanelBinding() { return skillsPanel; }

    /** Switches the skills panel binding (the 1466 versus 320 live check). */
    public static void setSkillsPanelBinding(String name) {
        skillsPanel = name == null || name.isEmpty() ? SKILLS_PANEL_DEFAULT : name;
    }

    /** Restores the default binding; tests call this from their teardown. */
    public static void resetSkillsPanelBinding() { skillsPanel = SKILLS_PANEL_DEFAULT; }

    /** True when the CANDIDATE skills-wrapper layout recipe may be sent. */
    public static boolean skillsLayoutEnabled() { return skillsLayout; }

    /** Turns the layout recipe on or off (the live geometry check). */
    public static void setSkillsLayoutEnabled(boolean value) { skillsLayout = value; }

    /** Restores the configured default (off unless the property says otherwise). */
    public static void resetSkillsLayout() {
        skillsLayout = Boolean.parseBoolean(System.getProperty(SKILLS_LAYOUT_PROPERTY, "false"));
    }

    /**
     * Opens the M3 panels and then runs the configured {@link StateEmitter}, in
     * that order. Idempotent: a second call is ignored.
     */
    public void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        openPanels();
        sendInitialState();
    }

    /** Attaches HUD content. The main action bar is a required gameplay surface; optional panels
     * retain client-owned visibility, docking, and tab state. */
    public void openPanels() {
        if (bindings == null) {
            skip("binding table", "no validated 947 table is installed");
            return;
        }
        boolean reveal = forceOpenPanels();
        Native950Bindings.Slot skills = slot(SKILLS_NAMES);
        if (skills != null) {
            int skillsPanelId = skillsPanelId();
            if (skillsPanelId >= 0) {
                // Which of 1466 / 320 was actually opened is the one thing the
                // live login has to report back, so it is logged, not inferred.
                log("[Ataraxia950] M3 skills slot opens interface " + skillsPanelId
                        + " (binding '" + skillsPanel + "')");
                // The skills panel is the only M3 slot with no onLoad hook of its
                // own, so its wrapper geometry may have to be set and saved
                // explicitly - see layout(), which is opt-in until a live check.
                if (attach(skills, skillsPanelId, reveal) && skillsLayout && reveal) layout(skills);
            }
        }
        Native950Bindings.Slot actionBar = slot(ACTION_BAR_NAMES);
        if (actionBar != null) {
            int actionBarId = interfaceId(ACTION_BAR_NAMES);
            // Regression guard: 7d2d567 stopped revealing every wrapper to preserve workspace
            // state. Unlike optional panels, the native main bar has no alternate bootstrap and
            // became inaccessible. Showing its wrapper does not write position, preset, or tabs.
            if (actionBarId >= 0) attach(actionBar, actionBarId, true);
        }
        Native950Bindings.Slot minimap = slot(MINIMAP_NAMES);
        if (minimap != null && reveal) show(minimap);
    }

    /** The stat and vital burst; must follow {@link #openPanels()}. */
    public void sendInitialState() {
        try {
            state.emit(player);
        } catch (RuntimeException failure) {
            stateFailures++;
            log("[Ataraxia950] M3 initial state emission failed: " + failure);
        }
    }

    public long panelsOpened() { return panelsOpened; }
    public long wrappersShown() { return wrappersShown; }
    public long layoutSteps() { return layoutSteps; }
    public long skippedBindings() { return skipped; }
    public long stateFailures() { return stateFailures; }

    // ---------------------------------------------------------------- recipe steps

    /**
     * IF_OPENSUB on the slot's attach hash, then IF_SETHIDE(false) on its wrapper.
     *
     * <p>{@code InterfaceManager.registerNativeOpen} writes the server-side "this
     * interface is open" bookkeeping that {@code containsInterface} and
     * {@code getInterfaceParentId} read, while the facade turns an unbound id into
     * a counted drop without telling its caller. Registering unconditionally would
     * therefore let the server believe a panel is open when nothing reached the
     * wire, so the send is confirmed against the facade's own sent counter first
     * and a drop is counted as a skip, like every other fail-closed outcome here.
     *
     * @return true when IF_OPENSUB actually reached the transport
     */
    private boolean attach(Native950Bindings.Slot slot, int interfaceId, boolean reveal) {
        // The 910 clickThrough flag is the 947 walkable flag; the handoff opens
        // every HUD slot walkable so the scene keeps receiving map clicks.
        long before = sendInterfaceCount();
        player.getPackets().sendInterface(true, slot.attach, interfaceId);
        if (sendInterfaceCount() == before) {
            skip("interface " + interfaceId + " in slot key " + slot.enumKey,
                    "the packet facade did not send IF_OPENSUB for it (unbound id, or no transport)");
            if (reveal) show(slot);
            return false;
        }
        panelsOpened++;
        InterfaceManager manager = player.getInterfaceManager();
        if (manager != null) manager.registerNativeOpen(interfaceId, slot.attach >>> 16, slot.attach & 0xffff);
        if (reveal) show(slot);
        return true;
    }

    /** Sent-frame count for {@code sendInterface}; -1 when the facade cannot report one. */
    private long sendInterfaceCount() {
        PacketDispatcher packets = player.getPackets();
        return packets instanceof Native950PacketDispatcher
                ? ((Native950PacketDispatcher) packets).counters().sent("sendInterface")
                : -1;
    }

    private void show(Native950Bindings.Slot slot) {
        player.getPackets().sendHideIComponent(slot.wrapper >>> 16, slot.wrapper & 0xffff, false);
        wrappersShown++;
    }

    /**
     * The equipment-slot layout recipe applied to this slot: size, position,
     * show, then a save into working layout 9 and into the active preset so a
     * window resize does not restore a zero-size hidden slot.
     *
     * <p>Reached only when {@link #SKILLS_LAYOUT_PROPERTY} is on. The five script
     * ids are CONFIRMED for slot 3 ({@code EQUIPMENT_CONTENT.md}); that slot 0
     * needs the same sequence is CANDIDATE ({@code SKILLS_TAB.md} section 8), and
     * the geometry it sends is invented - which is why this is opt-in, since
     * script 8708 commits the box to the client's persistent preset 8.
     */
    private void layout(Native950Bindings.Slot slot) {
        int size = script(SCRIPT_SIZE), position = script(SCRIPT_POSITION), show = script(SCRIPT_SHOW);
        if (size < 0 || position < 0 || show < 0) return;
        PacketDispatcher packets = player.getPackets();
        packets.sendExecuteScript(size, SKILLS_WIDTH, SKILLS_HEIGHT, 0, 0, slot.wrapper);
        packets.sendExecuteScript(position, SKILLS_X, SKILLS_Y, SKILLS_X_MODE, SKILLS_Y_MODE, slot.wrapper);
        packets.sendExecuteScript(show, slot.wrapper);
        layoutSteps += 3;
        int saveWorking = script(SCRIPT_SAVE_WORKING), savePreset = script(SCRIPT_SAVE_PRESET);
        if (saveWorking < 0 || savePreset < 0) return;
        packets.sendExecuteScript(saveWorking, slot.enumKey);
        packets.sendExecuteScript(savePreset, slot.enumKey, LAYOUT_PRESET);
        layoutSteps += 2;
    }

    // ---------------------------------------------------------------- table lookups

    /**
     * The configured skills panel. An EXPLICITLY configured name the table does
     * not declare is a SKIP, never a fallback: the switch exists for a one-attempt
     * live comparison of 1466 against 320, and quietly reopening the default would
     * make an operator who launched with the alternative read the default's
     * behaviour as the alternative's result. Only the untouched default resolves
     * through the alias list.
     */
    private int skillsPanelId() {
        String preferred = skillsPanel;
        if (bindings.interfaceNames().contains(preferred)) return bindings.interfaceId(preferred);
        if (!SKILLS_PANEL_DEFAULT.equals(preferred)) {
            skip("interface " + preferred,
                    "the configured skills panel is not declared in the 947 table; not falling back to the default");
            return -1;
        }
        return interfaceId(SKILLS_NAMES);
    }

    private Native950Bindings.Slot slot(String[] names) {
        for (String name : names) if (bindings.slotNames().contains(name)) return bindings.slot(name);
        skip("slot " + names[0], "the 950 binding table declares no such HUD slot");
        return null;
    }

    private int interfaceId(String[] names) {
        for (String name : names) if (bindings.interfaceNames().contains(name)) return bindings.interfaceId(name);
        skip("interface " + names[0], "the 950 binding table declares no such interface");
        return -1;
    }

    private int script(String name) {
        if (bindings.scriptNames().contains(name)) return bindings.script(name);
        skip("script " + name, "the 950 binding table declares no such cs2 script");
        return -1;
    }

    private void skip(String what, String reason) {
        skipped++;
        log("[Ataraxia950] M3 HUD bootstrap skipped " + what + ": " + reason);
    }

    // ---------------------------------------------------------------- diagnostics

    /** Runs one initial-state call; a failure is logged once and never aborts the rest. */
    static void guard(String what, Runnable body) {
        try {
            body.run();
        } catch (RuntimeException failure) {
            log("[Ataraxia950] M3 initial state " + what + " failed: " + failure);
        }
    }

    private static void log(String message) {
        if (LOGGED.size() >= LOG_ONCE_LIMIT) return;
        if (LOGGED.add(message)) System.out.println(message);
    }
}
