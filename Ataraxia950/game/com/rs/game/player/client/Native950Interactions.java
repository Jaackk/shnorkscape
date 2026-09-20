package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.client.ui.Native950Bindings;
import com.rs.game.player.content.Commands;
import com.rs.game.player.content.MoneyPouch;
import com.rs.game.player.content.jujupotions.harmonypillar.HarmonyPillarManager;
import com.rs.game.player.content.jujupotions.vineherbpatch.VineHerbPatchManager;
import com.rs.game.activites.gim.bank.GIMBankManager;
import com.rs.game.activites.quest.QuestHandler;
import com.rs.game.player.ChargesManagerNew;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.game.route.strategy.EntityStrategy;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.TreeMap;

/**
 * Thin adapter between the native 947 session and the 910 engine.
 *
 * <p>Since P5 every container mutation of the M2b slice is performed by the 910
 * code ({@code ObjectHandler} -> {@code Bank.openBank}, {@code ButtonHandler} ->
 * {@code Bank.depositItem/withdrawItem/depositAllInventory}, {@code ButtonHandler}
 * -> {@code InventoryOptionsHandler} -> {@code sendWear}, {@code Equipment.handleEquipment}
 * -> {@code sendRemove}, {@code Inventory.switchItem}) reached through
 * {@link Native950ActionRouter}. This class keeps what is native by nature:
 * <ul>
 * <li>the approach: object and NPC clicks are routed with the shared pathfinder
 *     and dispatched into the 910 handler only once the player is in reach, so
 *     the 910 {@code RouteEvent} completes on the same tick;</li>
 * <li>the verified 947 open/close recipes and the container refreshes (full
 *     93/94/95 containers, varp 8971, the equipment layout script) that are sent
 *     AFTER the 910 code changed state, because the 910 emitters only reach the
 *     wire for ids the binding table declares;</li>
 * <li>the actor-prediction pre-filters (post-prediction item claims, same-tick
 *     changed-slot barrier, reversed drag pair), applied before the 910 handler
 *     sees a click; nothing inside the handlers is relaxed;</li>
 * <li>the banker's Talk/Collect answers, whose 910 forms are dialogues (M7).</li>
 * </ul>
 * <p>M4 generalised the NPC click. A clicked index is resolved through the list
 * this viewer's client was actually SENT ({@code Native950NpcView.canInteract},
 * which reads the published index list rather than the world roster, so a client
 * cannot name an NPC it was never shown), then through {@code World.getNPCs()},
 * and then dispatched into {@code NPCHandler.dispatch} by
 * {@link Native950ActionRouter#npcOption}. A click on an index this viewer does
 * not hold, or on one whose NPC has left the world, is a counted rejection. The
 * banker keeps its own branch unchanged and remains the regression anchor for the
 * whole interaction path.
 * {@link Native950Containers} remains the read model and the save/restore path
 * only; it validates the containers it shares with the 910 code every tick.
 */
public final class Native950Interactions {
    static final int BACKPACK_INTERFACE = 1473, BACKPACK_ITEMS = 5;
    static final int INVENTORY_CONTAINER = 93, BANK_CONTAINER = 95;
    private static final int COINS = 995;
    private static volatile Native950Bindings bindings;
    private static volatile boolean bindingsLoaded;

    private final Player player;
    private final Channel channel;
    private final Native950Content content;
    private final Native950Containers containers;
    private final Native950NpcView npcView;
    private final CombatActions combatActions;
    private Native950GroundItemsView groundItems;
    private com.rs.game.item.floor.FloorItem pendingGroundItem;
    private int groundTicks;
    void attachGroundItems(Native950GroundItemsView view){groundItems=view;}

    /** Input tests replace this boundary; production uses only the player's world-owned service. */
    interface CombatActions {
        String attack(Player player, NPC npc);
        void stop(Player player);
        default void cancelAttack(Player player){stop(player);}
    }
    private static final CombatActions WORLD_COMBAT = new CombatActions() {
        @Override public String attack(Player player, NPC npc) {
            Native950MeleeCombat combat = player.getNative950Combat();
            return combat == null ? "Melee combat is not available in this world" : combat.attack(player, npc);
        }
        @Override public void cancelAttack(Player player) {
            Native950MeleeCombat combat=player.getNative950Combat();
            if(combat!=null)combat.cancelAttack(player);
        }
        @Override public void stop(Player player) {
            Native950MeleeCombat combat = player.getNative950Combat();
            if (combat != null) combat.stop(player);
        }
    };
    private final Native950ActionRouter router;
    private final Native950StatsUi statsUi;
    private final Native950WorldMap worldMap;
    private final Native950Settings settings;
    private final Native950Lodestones lodestones;
    private final Native950SkillGuide skillGuide;
    private final Native950ExitUi exitUi;
    private final Native950Dialogues dialogues;
    private final Native950ProductionMenu productionMenu;
    private final Native950ItemBrowser itemBrowser;
    private final Native950ToolbeltUi toolbeltUi;
    private final Native950ForgeUi forgeUi;
    private final Native950QuantityInput quantityInput;
    private boolean quantityPromptVisible;
    private long bankEpoch;
    private com.rs.game.player.Bank quantityBank;
    private NPC dialogueNpc;
    private WorldObject pendingBank, activeBank;
    private WorldObject pendingSkillObject;
    private Native950Actions.ItemOnObjectAction pendingItemOnObject;
    private WorldTile pendingSkillEntry;
    private NPC pendingSkillNpc;
    private int npcSkillOption, npcSkillApproachTicks;
    private int pendingSkillOption, skillApproachTicks;
    private int pendingObjectOption;
    private boolean bankOpen;
    private boolean equipmentUiReady;
    private int pendingNpcOption;
    private boolean activeNpcBank;
    private long transactions, rejectedActions, coinPouchRedirects, deferredEquipmentChanges;
    /** Decoded actions no branch of {@link #handle} claims, by action class name. Never silent. */
    private final Map<String, Long> unhandledActions = new TreeMap<String, Long>();
    private long unhandledActionTotal;
    /** Typed commands: attempted, refused by the 910 gates, and thrown out of. */
    private long commandsRun, commandsRefused, commandFailures;
    private int pendingTicks;
    private Native950Containers.Snapshot displayedInventory, displayedBank, observedEquipment;
    private final java.util.Set<Integer> changedInventorySlots = new java.util.HashSet<>();
    private final java.util.Set<Integer> changedBankSlots = new java.util.HashSet<>();

    Native950Interactions(Player player, Channel channel, Native950Content content) {
        this(player, channel, content, null);
    }

    Native950Interactions(Player player, Channel channel, Native950Content content, Native950Save saved) {
        this(player, channel, content, saved, null);
    }

    Native950Interactions(Player player, Channel channel, Native950Content content, Native950Save saved,
                          Native950NpcView npcView) {
        this(player, channel, content, saved, npcView, Native950Dialogues::verify, Native950QuantityInput::verify);
    }

    /** Verification is injectable for isolated tests; production verifies the selected cache. */
    Native950Interactions(Player player, Channel channel, Native950Content content, Native950Save saved,
                          Native950NpcView npcView, Runnable dialogueVerifier, Runnable quantityVerifier) {
        this(player, channel, content, saved, npcView, dialogueVerifier, quantityVerifier, Native950Settings::verify);
    }

    Native950Interactions(Player player, Channel channel, Native950Content content, Native950Save saved,
                          Native950NpcView npcView, Runnable dialogueVerifier, Runnable quantityVerifier,
                          Runnable settingsVerifier) {
        this(player, channel, content, saved, npcView, dialogueVerifier, quantityVerifier,
                settingsVerifier, WORLD_COMBAT);
    }

    Native950Interactions(Player player, Channel channel, Native950Content content, Native950Save saved,
                          Native950NpcView npcView, Runnable dialogueVerifier, Runnable quantityVerifier,
                          Runnable settingsVerifier, CombatActions combatActions) {
        this.player = player; this.channel = channel; this.content = content;
        this.npcView = npcView;
        this.combatActions = java.util.Objects.requireNonNull(combatActions, "combatActions");
        // The 910 handlers dereference the managers LoginManager.init builds; P4's
        // hydration is idempotent and packet-free, so it is safe to request here.
        player.hydrateForNative950();
        ensureLegacyFields(player);
        Native950Bindings table = loadBindings();
        this.router = new Native950ActionRouter(player, content, table);
        // M3 owns the skills tab, the action bar and the minimap orbs; every id it
        // uses comes from the same validated table the router installs.
        this.statsUi = new Native950StatsUi(player, table);
        this.worldMap = new Native950WorldMap(player, channel);
        this.settings = new Native950Settings(player, channel, settingsVerifier);
        this.lodestones = new Native950Lodestones(player, channel);
        skillGuide = new Native950SkillGuide(player, channel);
        this.exitUi = new Native950ExitUi(player, channel);
        final Native950Quests quests = new Native950Quests(player, channel);
        final Native950Beasts beasts = new Native950Beasts(player, channel);
        final Native950Minigames minigames = new Native950Minigames(player, channel);
        skillGuide.navigation().setPageListener(new Native950Navigation.PageListener() {
            public void opened(int menu,int page) { quests.opened(menu,page); beasts.opened(menu,page); minigames.opened(menu,page); player.getNative950ActionBar().enableBooks(channel); }
            public void closed(int menu,int page) { quests.closed(menu,page); beasts.closed(menu,page); minigames.closed(menu,page); }
            public boolean handle(Native950Actions.InterfaceAction action) {
                return quests.handle(action) || beasts.handle(action) || minigames.handle(action);
            }
        });
        this.quantityInput = new Native950QuantityInput(quantityVerifier);
        this.dialogues = new Native950Dialogues(player, channel, this::cancelQuantity, dialogueVerifier);
        player.setNative950Dialogues(dialogues);
        this.productionMenu = new Native950ProductionMenu(player, dialogues);
        this.itemBrowser = new Native950ItemBrowser(player, channel, dialogues);
        this.toolbeltUi = new Native950ToolbeltUi(player, channel);
        player.getInterfaceManager().setNative950ToolbeltUi(toolbeltUi);
        this.forgeUi = new Native950ForgeUi(player,channel);
        player.getInterfaceManager().setNative950ForgeUi(forgeUi);
        this.containers = new Native950Containers(player, content.items);
        if (saved == null) containers.seedStarterItems();
        else containers.restore(saved);
        Native950Skilling.attach(player, containers);
        observedEquipment = containers.equipmentSnapshot();
    }

    /**
     * Loads the verified binding table once per JVM. A legacy cache yields null
     * (identity ids stay in force); a changed 947 cache is fatal by design; a
     * classpath without the resource (a build tree not yet refreshed by Gradle)
     * is logged and the router falls back to the verified content constants.
     */
    static Native950Bindings loadBindings() {
        if (bindingsLoaded) return bindings;
        synchronized (Native950Interactions.class) {
            if (bindingsLoaded) return bindings;
            try {
                bindings = Native950Bindings.tryLoad();
            } catch (IllegalStateException failure) {
                if (failure.getMessage() == null || !failure.getMessage().contains("not on the classpath")) throw failure;
                System.out.println("[Ataraxia950] " + failure.getMessage() + "; routing with the handed-over content bindings only");
                bindings = null;
            }
            bindingsLoaded = true;
            return bindings;
        }
    }

    /** The table the adapter routes with, or null. */
    static Native950Bindings bindings() { return bindings; }

    /**
     * Public Player fields the M2b 910 paths dereference that P4's hydration does
     * not (yet) build: {@code gimBank} (ButtonHandler 517 branches, Bank history,
     * ItemConstants.isBankAble), {@code pouch} (Bank.withdrawItem coins),
     * {@code quests} (ItemConstants.isBankAble), the two juju patch managers
     * (ObjectHandler option-2 runnable), the charges manager (sendWear) and
     * {@code treasureTrails} (unconditional {@code useObject} call in
     * {@code ObjectHandler.handleOption2}, the branch that opens the bank chest).
     * All are packet-free to construct. Reported in notes/P5-router.md so P4 can
     * absorb them into hydrateForNative950.
     */
    static void ensureLegacyFields(Player player) {
        if (player.treasureTrails == null) player.treasureTrails = new com.rs.game.player.TreasureTrails();
        player.treasureTrails.setPlayer(player);
        // ButtonHandler.handleButtons dereferences this one unconditionally, before
        // it reaches any interface branch (ButtonHandler.java "getDungeoneeringBinds").
        if (player.getDungeoneeringBinds() == null) player.setDungeoneeringBinds();
        player.getDungeoneeringBinds().setPlayer(player);
        // InventoryOptionsHandler.handleItemOption2 (Wear/Wield) dereferences the
        // gem bag twice before it reaches ButtonHandler.sendWear.
        if (player.getGemBag() == null) player.setGemBag();
        player.getGemBag().setPlayer(player);
        if (player.gimBank == null) player.gimBank = new GIMBankManager(player);
        if (player.pouch == null) player.pouch = new MoneyPouch();
        player.pouch.setPlayer(player);
        if (player.quests == null) player.quests = new QuestHandler();
        player.quests.init(player);
        if (player.harmonyPillars == null) player.harmonyPillars = new HarmonyPillarManager();
        player.harmonyPillars.setPlayer(player);
        if (player.vineHerbPatches == null) player.vineHerbPatches = new VineHerbPatchManager();
        player.vineHerbPatches.setPlayer(player);
        if (player.getChargesManagerNew() == null) player.setChargesManagerNew(new ChargesManagerNew());
        player.getChargesManagerNew().setPlayer(player);
    }

    Native950Save saveSnapshot() {
        return containers.saveSnapshot(player.getUsername(), player.getX(), player.getY(), player.getPlane());
    }

    private final Native950InventionUi inventionUi = new Native950InventionUi();

    void bootstrap() {
        // The frontend flushed the initial scene before the session became ready;
        // the 910 object gate (ObjectHandler.dispatch) requires this flag and the
        // native map loader never clears it again.
        player.setClientHasLoadedMapRegion();
        sendInventory();
        if (content.equipment != null) {
            sendEquipment();
            for (Native950Packets.Packet packet : content.equipment.bootstrap) channel.write(packet);
            equipmentUiReady = true;
        }
        // M3: attach the skills tab and the action bar and show the minimap wrapper.
        // The skills panel has no onLoad hook (SKILLS_TAB.md section 2), so its
        // cells only appear once UPDATE_STAT arrives; the caller
        // (Native950Session.ready) sends that burst immediately after this method
        // returns, which is why the panels are attached here and not later.
        statsUi.bootstrap();
        player.getNative950ActionBar().bootstrap(player,channel);
        lodestones.bootstrap();
        skillGuide.bootstrap();
        exitUi.bootstrap();
        toolbeltUi.bootstrap();
        compass();
        inventionUi.sync(player,channel);
    }

    /**
     * Mount the compass into the minimap's top-left orb layer.
     *
     * <p>Interface 1919 is the compass: a 36x36 root over a static 33x33 ring and a 33x33 sprite
     * whose contentType is 1339. Only nine of the cache's 104,285 components carry a contentType
     * at all, and the neighbours identify the family - 1337 is the 3D scene (1482:0) and 1338 the
     * minimap viewport (1465:0) - so 1339 is the needle the client rotates from the camera.
     *
     * <p>Nothing in the cache says where it mounts: it is not one of the root's 101 panel slots,
     * no enum maps it, and the scripts that name 1919 are unusable as evidence because 1919 is
     * also an item id (a jug, in the glassblowing product list). The mount was found by trying
     * the minimap's hidden layers that are large enough for a 36x36 child. 1465:21 renders it
     * over the lodestone button in the bottom left; 1465:12, the 41x41 layer, puts it in the
     * top-left quadrant where it belongs, and it tracks the camera from there.
     */
    private void compass() {
        // The compass is a permanent HUD child, just like the minimap itself. Type 0
        // (false) is modal: the client's generic close-modal pass can remove it when
        // walking or closing Settings/map, although the server never closes this slot.
        channel.write(Native950Packets.openSub(Native950ActionRouter.MINIMAP_INTERFACE, COMPASS_MOUNT, COMPASS_INTERFACE, true));
    }

    /** The compass widget, and the minimap layer it mounts into. */
    private static final int COMPASS_INTERFACE = 1919, COMPASS_MOUNT = 12;

    Native950Containers.Snapshot equipmentSnapshot() { return containers.equipmentSnapshot(); }

    Native950ActionRouter router() { return router; }
    boolean blocksWorldInput(){return exitUi.isOpen()||exitUi.isSigningOut();}

    void beginTick() {
        inventionUi.sync(player,channel);
        changedInventorySlots.clear(); changedBankSlots.clear();
        if (!router.playerMayAct() || player.isLocked()) {
            cancelConversations();
            settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        }
        // A wear scheduled by InventoryOptionsHandler.handleItemOption2 ran in this
        // tick's WorldTasksManager pass (before the session drained input).
        observeEquipment("Equipped");
        observeInventory();
    }

    /**
     * Dispatches one decoded action, and <b>counts the ones nothing handles</b>.
     *
     * <p>{@code Native950Actions.decode} produces more action types than this method has
     * branches for. Before 2026-09-07 the surplus fell off the end of the if-chain with no
     * counter, no log and no reject: a decoded, queued, drained action simply evaporated. That
     * is the one thing this port is not allowed to do - the whole design is that an input the
     * server cannot honour is a <i>counted</i> drop with a reason, never a silent one, so that a
     * login-and-walk session is a measurement rather than an impression.
     *
     * <p>Nothing is sent to the player here. An unhandled action is not the player's mistake and
     * a red chat line for it is noise; the counter is for us. {@code unhandledActions} is keyed
     * by the action's simple class name so the report names what was dropped, not merely how
     * many.
     *
     * <p>The {@code playerMayAct} gate keeps its own counter. It is deliberately NOT folded in
     * with the unhandled ones: "you may not act right now" and "we never built this" are
     * different facts, and conflating them attributes the drop to the wrong cause.
     */
    void handle(Native950Actions.Action action) {
        Native950BugTest.action(player,action);
        // CLOSE_MODAL reports a change the client already made. It must reconcile
        // server state even if the player died or became inactive before this tick.
        if (action instanceof Native950Actions.CloseModalAction) {
            if(exitUi.consumeOpeningCloseAcknowledgement())return;
            if(exitUi.consumeLayoutEditorClose())return;
            closeModal();return;
        }
        if(exitUi.isOpen()&&(action instanceof Native950Actions.ObjectAction
                ||action instanceof Native950Actions.NpcAction||action instanceof Native950Actions.GroundItemAction
                ||action instanceof Native950Actions.ItemOnObjectAction||action instanceof Native950Actions.ItemOnNpcAction)){
            rejectedActions++;return;
        }
        if (!router.playerMayAct()) { cancelConversations(); settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close(); rejectedActions++; return; }
        // Loadout is a second view of the same inventory. Normalize only while that
        // verified page is mounted; stale off-screen actors retain their rejected hashes.
        if (skillGuide.navigation().isPage(0,3))
            action = Native950Actions.aliasInventorySurface(action,(1474 << 16) | 8,
                    (BACKPACK_INTERFACE << 16) | BACKPACK_ITEMS);
        if ((action instanceof Native950Actions.ObjectAction || action instanceof Native950Actions.NpcAction
                || action instanceof Native950Actions.GroundItemAction || action instanceof Native950Actions.ItemOnItemAction
                || action instanceof Native950Actions.ItemOnObjectAction || action instanceof Native950Actions.ItemOnNpcAction)
                && (player.isLocked() || player.isNative950ForceMovementActive())) {
            reject("You cannot move to that interaction right now"); return;
        }
        if (action instanceof Native950Actions.StringDialogueAction) {
            if (!itemBrowser.handle((Native950Actions.StringDialogueAction) action)) unhandled(action);
            return;
        }
        if (action instanceof Native950Actions.CountDialogueAction) {
            if (itemBrowser.handle((Native950Actions.CountDialogueAction) action)) return;
            quantity((Native950Actions.CountDialogueAction) action); return;
        }
        if (action instanceof Native950Actions.DialogueClickAction) {
            dialogue((Native950Actions.DialogueClickAction) action); return;
        }
        if (action instanceof Native950Actions.PauseButtonAction && quantityInput.active()) {
            // Numeric mode 17 Escape sends950 opcode11; unlike CLOSE_MODAL it leaves the bank open.
            cancelQuantity(); return;
        }
        if (action instanceof Native950Actions.PauseButtonAction && itemBrowser.cancelInput()) return;
        if (!(action instanceof Native950Actions.GroundItemAction)) pendingGroundItem=null;
        if (action instanceof Native950Actions.GroundItemAction) groundItem((Native950Actions.GroundItemAction)action);
        else if (action instanceof Native950Actions.ObjectAction) object((Native950Actions.ObjectAction) action);
        else if (action instanceof Native950Actions.InterfaceAction) button((Native950Actions.InterfaceAction) action);
        else if (action instanceof Native950Actions.ItemOnItemAction) itemOnItem((Native950Actions.ItemOnItemAction) action);
        else if (action instanceof Native950Actions.ItemOnObjectAction) itemOnObject((Native950Actions.ItemOnObjectAction) action);
        else if (action instanceof Native950Actions.ItemOnNpcAction) itemOnNpc((Native950Actions.ItemOnNpcAction) action);
        else if (action instanceof Native950Actions.DragAction) drag((Native950Actions.DragAction) action);
        else if (action instanceof Native950Actions.NpcAction) npc((Native950Actions.NpcAction) action);
        else if (action instanceof Native950Actions.PublicChatAction) typed((Native950Actions.PublicChatAction) action);
        else if (action instanceof Native950Actions.WindowReportAction)
            Native950Workspace.windowReport(player,(Native950Actions.WindowReportAction) action);
        else if (action instanceof Native950Actions.MapBuildReportAction)
            mapBuildReport((Native950Actions.MapBuildReportAction) action);
        else unhandled(action);
    }

    /**
     * The in-client escape hatch: a line the player types that begins with {@code ::} or
     * {@code ;;} runs a 910 command. Ordinary chat is not handled here and stays counted.
     *
     * <p><b>Why this exists.</b> Until now nothing a player typed could reach the server, so a
     * mistake in the interface could only be undone by stopping the server, rebuilding and
     * restarting - which disconnects everyone and takes two toolchains. That made every UI change
     * expensive to try and dangerous to ship. One typed command turns a failed experiment into a
     * relog. UI-PLAN.md calls this the blocker that gates all the rest of the interface work.
     *
     * <p><b>Why it does not go through the chat handler.</b> The 910 chat path's dispatcher
     * methods are strict-tier stubs on this port: {@code sendPublicMessage} and its siblings
     * throw, and a throw that escapes a tick closes the session. Routing commands through chat
     * would therefore have built an escape hatch that kicks you out. This calls
     * {@code Commands.processCommand} directly, which strips the prefix itself and applies its
     * own per-command rights gates.
     *
     * <p><b>Nothing a command does may close the session.</b> Many 910 commands touch subsystems
     * this port has not hydrated, so throwing is expected rather than exceptional. The failure is
     * caught, counted and reported to the player as a message. An escape hatch that can itself
     * disconnect you is not an escape hatch.
     */
    private void typed(Native950Actions.PublicChatAction action) {
        String text = action.text() == null ? "" : action.text().trim();
        if (!text.startsWith("::") && !text.startsWith(";;")) {
            // Real public chat. M5 owns it; until then it is a counted drop like any other.
            unhandled(action);
            return;
        }
        commandsRun++;
        boolean passiveDiagnostic=Native950DevelopmentCommands.preservesGameplay(text);
        if(!passiveDiagnostic){
            cancelSkill();
            combatActions.cancelAttack(player);
            cancelConversations();
            settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        }
        try {
            if (Native950DevelopmentCommands.isCommand(text)) {
                if(!passiveDiagnostic)closeModal();
                Native950DevelopmentCommands.handle(player, channel, text, skillGuide);
            } else if (!Commands.processCommand(player, text, false, false)) commandsRefused++;
        } catch (Throwable failure) {
            commandFailures++;
            System.out.println("[Ataraxia950] command failed: " + text + " -> " + failure);
            channel.write(Native950Packets.gameMessage(0,
                    "That command failed: " + failure.getClass().getSimpleName() + "."));
        }
    }

    /**
     * The client sends this after it finishes building a scene, carrying how long the build took.
     * It is the only signal the client gives about scene construction, and the port had been
     * discarding it, which is why an arrival that renders as empty sky could not be told apart
     * from one that never built at all. One line per rebuild, so it stays on permanently.
     */
    private void mapBuildReport(Native950Actions.MapBuildReportAction action) {
        mapBuilds++;
        if(mapBuilds==1)workspaceRestore=Native950DisposableWorkspaceRestore.sceneReady(player,channel);
        if(mapBuilds==1)Native950WorkspaceCapture.sceneReady(player,channel);
        System.out.println("[Ataraxia950] Client finished a scene build in " + action.elapsed()
                + " client timer units; player " + player.getIndex() + " is at "
                + player.getX() + "," + player.getY() + "," + player.getPlane()
                + " map square " + (player.getX() >> 6) + "," + (player.getY() >> 6)
                + " (build " + mapBuilds + " this session)");
    }

    /** Scene builds the client has reported completing this session. */
    private long mapBuilds;
    private Native950DisposableWorkspaceRestore workspaceRestore;

    /** Counts one decoded action that no branch above claims, and names it once in the log. */
    private void unhandled(Native950Actions.Action action) {
        String kind = action == null ? "null" : action.getClass().getSimpleName();
        // Bug Test is opt-in; this records a real decoded action only when it has no
        // gameplay owner, which is the evidence needed for unwired native controls.
        Native950BugTest.event(player, "input", "unhandled-action", "type", kind);
        Long seen = unhandledActions.get(kind);
        unhandledActions.put(kind, Long.valueOf(seen == null ? 1L : seen.longValue() + 1L));
        unhandledActionTotal++;
        if (seen == null)
            System.out.println("[Ataraxia950] decoded input has no handler: " + kind
                    + " (counted; the player is told nothing)");
    }

    /** Unhandled decoded actions by type, most frequent first, for a session report. */
    public String unhandledActionReport() {
        if (unhandledActions.isEmpty()) return "none";
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, Long> entry : unhandledActions.entrySet()) {
            if (text.length() > 0) text.append(", ");
            text.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return text.toString();
    }

    void walking() { cancelSkill(); pendingGroundItem=null; combatActions.cancelAttack(player); cancelConversations(); pendingBank = null; pendingNpcOption = 0; closeBank(); worldMap.close(); settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close(); }

    /** Retire all pending responses before the session leaves the world. */
    void close() { Native950Familiars.onLogout(player); cancelSkill(); itemBrowser.dispose(); Native950Skilling.detach(player); pendingGroundItem=null; combatActions.stop(player); cancelConversations(); settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close(); }

    private void cancelQuantity() {
        quantityInput.cancel();
        quantityBank = null;
        if (!quantityPromptVisible) return;
        quantityPromptVisible = false;
        for (Native950Packets.Packet packet : quantityInput.cancelPackets()) channel.write(packet);
        player.getInterfaceManager().unregisterNativeOpen(1469);
        player.getInterfaceManager().unregisterNativeOpen(1418);
        if (bankOpen) syncBankPreferences(); // Revert Change-X's local selection when cancelled.
    }

    private void cancelDialogue() {
        itemBrowser.close();
        productionMenu.close(); forgeUi.close();
        dialogueNpc = null;
        try {
            player.getDialogueManager().finishDialogue();
        } catch (RuntimeException failure) {
            router.dialogueCloseFailed(failure);
        } finally {
            dialogues.close();
        }
    }

    private void cancelConversations() { cancelQuantity(); cancelDialogue(); }

    private boolean dialogueNpcAvailable() {
        return dialogueNpc == null || (npcView != null
                && World.getNPCs().get(dialogueNpc.getIndex()) == dialogueNpc
                && npcView.canInteract(player, dialogueNpc.getIndex())
                && !dialogueNpc.isDead() && !dialogueNpc.hasFinished() && !dialogueNpc.isCantInteract()
                && RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(),
                    player.getPlane(), player.getSize(), new EntityStrategy(dialogueNpc), false) == 0);
    }

    private void dialogue(Native950Actions.DialogueClickAction action) {
        if (itemBrowser.handle(action)) return;
        if (productionMenu.handle(action)) return;
        if (player.isLocked() || player.closeInterfaceLocked || !dialogueNpcAvailable()) {
            cancelDialogue(); rejectedActions++; return;
        }
        if (!player.getDialogueManager().hasDialogue()
                || !dialogues.consumeResponse(action.interfaceId(), action.componentId(), action.slot())) {
            rejectedActions++; return;
        }
        NPC source = dialogueNpc;
        Native950ActionRouter.Outcome outcome = router.dialogue(action.interfaceId(), action.componentId());
        if (!outcome.accepted) { cancelDialogue(); reject(outcome.reason); return; }
        if (router.bankInterfaceOpen()) {
            // Banker.run owns the actual bank opening; retain the same proximity gate as Bank-click.
            activeNpcBank = source != null && npcView != null && source == npcView.banker();
            if (activeNpcBank) activeBank = null;
        }
        if (!dialogues.isOpen()) dialogueNpc = null;
        syncBankState();
    }

    private boolean bankAvailable() {
        return bankOpen && router.bankInterfaceOpen() && !player.isLocked() && !player.closeInterfaceLocked
                && (activeNpcBank ? validNpc() && canReachNpc() : validBank(activeBank) && canReachNow(activeBank));
    }

    private void beginBankQuantity(Native950Actions.InterfaceAction action, boolean deposit) {
        Native950Containers.Snapshot source = deposit ? containers.inventorySnapshot() : containers.bankSnapshot();
        // Both op6 chains pass zero to prediction, preserving the original actor.
        if ((deposit ? changedInventorySlots : changedBankSlots).contains(action.slot())
                || !Native950ActionRouter.exactClaim(action.itemId(), action.slot(), source)
                || content.items.get(action.itemId()) == null) {
            rejectedActions++; refreshBank(); return;
        }
        if (quantityInput.active()) { rejectedActions++; return; }
        cancelDialogue();
        boolean begun = deposit ? quantityInput.beginDeposit(bankEpoch, action.slot(), action.itemId(), source)
                : quantityInput.beginWithdraw(bankEpoch, action.slot(), action.itemId(), source);
        if (!begun) { rejectedActions++; return; }
        showQuantityPrompt();
    }

    private void showQuantityPrompt() {
        quantityBank = player.getBank();
        quantityPromptVisible = true;
        for (Native950Packets.Packet packet : quantityInput.promptPackets()) channel.write(packet);
        player.getInterfaceManager().registerNativeOpen(1418, 1477, 749);
        player.getInterfaceManager().registerNativeOpen(1469, 1418, 2);
    }

    private void quantity(Native950Actions.CountDialogueAction action) {
        if (!quantityInput.active()) { rejectedActions++; return; }
        if (!bankAvailable() || quantityBank != player.getBank()) {
            cancelQuantity(); rejectedActions++; return;
        }
        boolean deposit = quantityInput.kind() == Native950QuantityInput.Kind.DEPOSIT;
        Native950Containers.Snapshot source = deposit ? containers.inventorySnapshot() : containers.bankSnapshot();
        Native950QuantityInput.Accepted accepted = quantityInput.consume(action.count(), bankEpoch, source,
                (slot, amount) -> deposit ? amount : Native950Banking.withdrawableAmount(player, containers, slot, source.ids[slot], amount));
        cancelQuantity();
        if (accepted == null) { rejectedActions++; syncBankPreferences(); refreshBank(); return; }
        com.rs.game.player.Bank bank = player.getBank();
        bank.restoreNativePreferences(bank.getWithdrawNotes(), (int) action.count(),
                accepted.kind == Native950QuantityInput.Kind.DEFAULT ? 11 : bank.getNativeDefaultInteractionAmount());
        syncBankPreferences();
        if (accepted.kind == Native950QuantityInput.Kind.DEFAULT) return;
        Native950Containers.Snapshot before = containers.inventorySnapshot();
        int outputId = deposit ? accepted.itemId : Native950Banking.withdrawnItemId(player, accepted.itemId);
        Native950ActionRouter.Outcome outcome = deposit
                ? router.depositQuantity(accepted.slot, accepted.itemId, accepted.amount)
                : router.withdrawQuantity(accepted.slot, accepted.itemId, accepted.amount);
        if (!outcome.accepted) { reject(outcome.reason); refreshBank(); return; }
        long moved = deposit ? total(before, accepted.itemId) - total(containers.inventorySnapshot(), accepted.itemId)
                : total(containers.inventorySnapshot(), outputId) - total(before, outputId);
        if (moved > 0) {
            transactions++;
            channel.write(Native950Packets.gameMessage(0, (deposit ? "Deposited " : "Withdrew ") + moved + " x "
                    + content.items.get(accepted.itemId).name + "."));
        }
        refreshBank();
    }

    /**
     * The client's modal close has no component payload. The bank is the only
     * native modal currently supported, so retire its recipe and its 910
     * bookkeeping together. In particular, a retained 910 bank entry would make
     * syncBankState() reopen the bank on this same tick.
     */
    private void groundItem(Native950Actions.GroundItemAction action) {
        pendingGroundItem=null;
        if(action.option()!=3 || groundItems==null || !groundItems.canTake(player,action.itemId(),action.x(),action.y())) {
            reject("That ground item is not available");return;
        }
        WorldTile tile=new WorldTile(action.x(),action.y(),player.getPlane());
        com.rs.game.item.floor.FloorItem item=null;
        for(com.rs.game.item.floor.FloorItem candidate:World.getRegion(tile.getRegionId()).getGroundItemsSafe()) {
            if(candidate.isNative950() && candidate.getId()==action.itemId() && candidate.getTile().matches(tile)
                    && Native950GroundItemsView.visibleTo(player,candidate)){item=candidate;break;}
        }
        if(item==null || !Native950GroundItemsView.visibleTo(player,item) || content.items.get(item.getId())==null) {
            reject("That ground item is no longer available");return;
        }
        walking();player.setRouteEvent(null);player.getActionManager().forceStop();
        int count=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),player.getSize(),
                new com.rs.game.route.strategy.FixedTileStrategy(tile.getX(),tile.getY()),false);
        if(count<0 || RouteFinder.lastIsAlternative()){reject("You cannot reach that item");return;}
        int[] xs=RouteFinder.getLastPathBufferX(),ys=RouteFinder.getLastPathBufferY();
        for(int step=count-1;step>=0;step--) {
            int remaining=128-player.getWalkSteps().size();
            if(remaining<=0 || !player.addWalkSteps(xs[step],ys[step],remaining,true))break;
        }
        pendingGroundItem=item;groundTicks=0;
    }

    private void processGroundPickup() {
        if(pendingGroundItem==null)return;
        if(!router.playerMayAct() || player.isLocked() || player.getNextWorldTile()!=null || ++groundTicks>100) {
            pendingGroundItem=null;return;
        }
        if(player.hasWalkSteps())return;
        com.rs.game.item.floor.FloorItem item=pendingGroundItem;pendingGroundItem=null;
        Native950Containers.Result result=Native950GroundPickup.take(player,containers,item);
        if(result.moved==0){reject(result.reason);return;}
        transactions++;sendInventory();
        System.out.println("[Ataraxia950] Ground Take player="+player.getIndex()+" item="+item.getId()+" amount="+result.moved);
    }

    private void closeModal() {
        pendingGroundItem=null;
        cancelConversations();
        settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        worldMap.close();
        boolean approachingBank = pendingBank != null || pendingNpcOption != 0
                || (!bankOpen && activeBank != null);
        pendingBank = null;
        pendingObjectOption = 0;
        pendingNpcOption = 0;
        pendingTicks = 0;
        if (approachingBank) {
            player.resetWalkSteps();
            player.setRouteEvent(null);
        }
        closeBank();
        // Player.closeInterfaces() may be guarded by closeInterfaceLocked. That
        // guard cannot undo a close already performed by the native client.
        if (player.getInterfaceManager() != null)
            player.getInterfaceManager().unregisterNativeOpen(Native950ActionRouter.BANK_INTERFACE);
        displayedBank = null;
    }

    void afterMovement() {
        Native950WorkspaceCapture.drain(channel);
        if(workspaceRestore!=null&&workspaceRestore.finishOnWorldThread(player,channel))workspaceRestore=null;
        skillGuide.tick();
        observeInventory();
        processSkillApproach();
        processNpcSkillApproach();
        processGroundPickup();
        if (!router.playerMayAct() || player.isLocked()) cancelConversations();
        if (quantityInput.active() && !bankAvailable()) cancelQuantity();
        if (dialogues.isOpen() && !dialogueNpcAvailable()) cancelDialogue();
        // A 910 RouteEvent the dispatch could not finish inline (player not yet in
        // reach by the object strategy) completes here on a later tick.
        if (player.getRouteEvent() != null && !player.hasWalkSteps()) router.processRouteEvent();
        syncBankState();
        if (pendingNpcOption != 0) {
            if (++pendingTicks > 100) {
                pendingNpcOption = 0; player.resetWalkSteps(); reject("Banker approach timed out"); return;
            }
            if (!player.getWalkSteps().isEmpty()) return;
            int option = pendingNpcOption;
            pendingNpcOption = 0;
            if (!validNpc() || !canReachNpc()) { reject("Cannot reach that banker"); return; }
            NPC npc = npcView.banker();
            if (option == content.banker.talkOption) {
                Native950ActionRouter.Outcome outcome = router.npcTalk(npc);
                if (!outcome.accepted) { cancelDialogue(); reject(outcome.reason); return; }
                dialogueNpc = npc;
            } else if (option == content.banker.collectOption && content.equipment != null) {
                Native950ActionRouter.Outcome outcome = router.npcCollect(npc);
                if (!outcome.accepted) { reject(outcome.reason); return; }
                Native950Containers.Result result = containers.claimEquipmentKit();
                if (result.moved > 0) {
                    transactions++;
                    channel.write(Native950Packets.gameMessage(0,
                            "Banker: Here is your bronze sword, shield and helmet. Wield or wear them from your backpack."));
                    System.out.println("[Ataraxia950] Equipment kit collected for player " + player.getIndex());
                    player.getInventory().refresh(); // the 910 refresh of the container the kit was placed in
                } else reject(result.reason);
                sendInventory();
            } else {
                activeNpcBank = true; activeBank = null;
                Native950ActionRouter.Outcome outcome = router.npcBank(npc);
                if (!outcome.accepted) { activeNpcBank = false; reject(outcome.reason); return; }
                bankOpened("banker " + npc.getIndex());
            }
            return;
        }
        if (pendingBank == null) return;
        if (++pendingTicks > 100) { pendingBank = null; player.resetWalkSteps(); reject("Bank approach timed out"); return; }
        if (!player.getWalkSteps().isEmpty()) return;
        WorldObject target = pendingBank;
        int option = pendingObjectOption;
        pendingBank = null;
        if (!validBank(target) || !canReachNow(target)) { reject("Cannot reach that bank chest"); return; }
        activeBank = target; activeNpcBank = false;
        Native950ActionRouter.Outcome outcome = router.object(target, option, false);
        if (!outcome.accepted) { activeBank = null; reject(outcome.reason); return; }
        if (router.bankInterfaceOpen()) bankOpened("chest " + target.getX() + "," + target.getY());
        else if (player.getRouteEvent() == null) { activeBank = null; reject("The 910 bank did not open (see the facade drop log)"); }
        // else: the 910 RouteEvent is still walking; syncBankState() reports the open later.
    }

    /**
     * Mirrors the 910 InterfaceManager's bank state into the native UI: an open
     * performed by a deferred RouteEvent sends the verified open recipe, a close
     * performed by 910 code (stopAll from another action) sends the close recipe.
     */
    private void syncBankState() {
        boolean open910 = router.bankInterfaceOpen();
        if (!bankOpen && open910) bankOpened(activeNpcBank ? "banker (deferred)" : "chest (deferred)");
        else if (bankOpen && !open910) closeBank();
    }

    private void bankOpened(String source) {
        cancelSkill();
        cancelConversations();
        bankEpoch++;
        worldMap.close();
        settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        bankOpen = true;
        compactBank();
        sendInventory(); sendBank();
        for (Native950Packets.Packet packet : content.bank.open) channel.write(packet);
        syncBankPreferences();
        channel.write(Native950Packets.gameMessage(0, "Bank opened."));
        System.out.println("[Ataraxia950] Bank opened from " + source + " for player " + player.getIndex()
                + " through Bank.openBank()");
    }

    private void cancelSkill() {
        Native950Firemaking.cancelPending(player);
        pendingSkillObject = null; pendingSkillNpc = null; pendingItemOnObject = null;
        player.getActionManager().forceStop();
    }

    private boolean skillOption(WorldObject object, int option) {
        if (object == null || option < 1 || option > 5) return false;
        if(Native950WarsRetreat.handles(object,option))return true;
        if(Native950Farming.isPatch(object))return Native950Farming.accepts(player,object,option);
        String[] options = object.getDefinitions().options;
        if (options == null || option > options.length) return false;
        String name = options[option - 1];
        return ("Chop down".equalsIgnoreCase(name) && Native950Woodcutting.isTree(object))
                || ("Use".equalsIgnoreCase(name) && Native950Firemaking.isFireObject(object))
                || ("Mine".equalsIgnoreCase(name) && Native950Mining.isRock(object))
                || (Native950Cooking.isCookOption(name) && Native950Cooking.isCookingObject(object))
                || ("Smelt".equalsIgnoreCase(name) && Native950Smelting.isFurnace(object))
                || Native950Crafting.accepts(object,option) || Native950Smithing.accepts(object,option)
                || Native950Summoning.accepts(object,option)
                || Native950Runecrafting.handles(object,option)
                || (Native950Divination.isRift(object) && (option==1 || option==2))
                || Native950Thieving.isStall(object,option)
                || Native950Agility.handles(object,option)
                || Native950Construction.handles(object,option) || Native950Invention.accepts(object,option)
                || Native950Archaeology.accepts(object,option) || Native950Prayer.accepts(object,option) || Native950Dungeoneering.handlesObject(object,option);
    }

    private void processSkillApproach() {
        WorldObject object = pendingSkillObject;
        if (object == null) return;
        if (!router.playerMayAct() || player.isLocked() || player.getNextWorldTile() != null
                || player.getPlane() != object.getPlane() || ++skillApproachTicks > 100
                || findObject(object.getId(), object.getX(), object.getY()) != object) {
            pendingSkillObject = null; pendingItemOnObject = null; player.resetWalkSteps(); return;
        }
        // The final movement has left the server queue but has not yet reached the
        // client. Mining and other stationary sequences can block its interpolation.
        // Keep the approach pending until that movement has been published, matching
        // the original 910 RouteEvent (which ran before, rather than after, movement).
        if (player.hasWalkSteps() || player.getNextWalkDirection() != -1 || player.hasTeleported()) return;
        pendingSkillObject = null;
        Native950Actions.ItemOnObjectAction itemUse = pendingItemOnObject;
        pendingItemOnObject = null;
        if (itemUse != null) {
            if (!canReachNow(object)) reject("You cannot reach that object");
            else completeItemOnObject(object,itemUse);
            return;
        }
        if (!skillOption(object, pendingSkillOption) || (pendingSkillEntry!=null ? !pendingSkillEntry.matches(player) : !canReachNow(object))) {
            reject("You cannot reach that resource"); return;
        }
        boolean permitted;
        switch (pendingSkillOption) {
            case 1: permitted=player.getControlerManager().processObjectClick1(object); break;
            case 2: permitted=player.getControlerManager().processObjectClick2(object); break;
            case 3: permitted=player.getControlerManager().processObjectClick3(object); break;
            case 4: permitted=player.getControlerManager().processObjectClick4(object); break;
            case 5: permitted=player.getControlerManager().processObjectClick5(object); break;
            default: permitted=false;
        }
        if (!permitted) return;
        if(Native950WarsRetreat.handles(object,pendingSkillOption))Native950WarsRetreat.use(player,object,pendingSkillOption);
        else if(Native950Farming.isPatch(object))Native950Farming.handle(player,object,pendingSkillOption);
        else if(Native950Construction.handles(object,pendingSkillOption))productionMenu.openChoices("Construct furniture",Native950Construction.choices(player,object));
        else if(Native950Prayer.accepts(object,pendingSkillOption)){
            if("Pray at".equalsIgnoreCase(object.getDefinitions().options[pendingSkillOption-1]))Native950Prayer.recharge(player,object);
            else productionMenu.openChoices("Offer at altar",Native950Prayer.choices(player,object));
        }
        else if(Native950Invention.accepts(object,pendingSkillOption))productionMenu.openChoices("Invention workbench",Native950Invention.choices(player,object));
        else if(Native950Archaeology.accepts(object,pendingSkillOption)){
            if(Native950Archaeology.isExcavation(object))Native950Archaeology.start(player,object);
            else if("Store".equals(object.getDefinitions().options[pendingSkillOption-1]))Native950Archaeology.storeMaterials(player,object);
            else productionMenu.openChoices("Archaeology workbench",Native950Archaeology.choices(player,object));
        }
        else if(Native950Dungeoneering.handlesObject(object,pendingSkillOption))Native950Dungeoneering.object(player,object,pendingSkillOption);
        else if (Native950Agility.handles(object,pendingSkillOption)) Native950Agility.start(player,object,pendingSkillEntry);
        else if (Native950Woodcutting.isTree(object)) Native950Woodcutting.start(player, object);
        else if (Native950Mining.isRock(object)) Native950Mining.start(player, object);
        else if (Native950Smelting.isFurnace(object)) {
            java.util.List<Native950ProductionMenu.Choice> choices = new java.util.ArrayList<>();
            for (Native950Production.Recipe recipe : Native950Smelting.recipes(player,object))
                choices.add(new Native950ProductionMenu.Choice(Native950Production.name(recipe.produced()[0].getId())
                        + " (level " + recipe.level + ")", recipe, (int quantity) -> Native950Smelting.start(player,object,recipe,quantity)));
            choices.addAll(Native950Crafting.choices(player,object));
            forgeUi.open(choices,true,object);
        } else if (Native950Smithing.accepts(object,pendingSkillOption)) {
            forgeUi.open(Native950Smithing.choices(player,object),false,object);
        } else if (Native950Crafting.accepts(object,pendingSkillOption)) {
            productionMenu.openChoices("Choose what to craft",Native950Crafting.choices(player,object));
        } else if (Native950Summoning.accepts(object,pendingSkillOption)) {
            if(Native950Summoning.isRenewOption(object,pendingSkillOption))Native950Summoning.renewPoints(player,object);
            else productionMenu.openChoices("Choose a pouch or scroll",Native950Summoning.choices(player,object));
        } else if (Native950Runecrafting.handles(object,pendingSkillOption)) {
            Native950Runecrafting.start(player,object);
        } else if (Native950Divination.isRift(object)) {
            if(pendingSkillOption==2) productionMenu.openChoices("Convert memories",Native950Divination.conversionChoices(player,object));
            else Native950Divination.startConvert(player,object,pendingSkillOption);
        } else if (Native950Thieving.isStall(object,pendingSkillOption)) {
            Native950Thieving.startStall(player,object,pendingSkillOption);
        } else if (Native950Cooking.isCookingObject(object)) {
            if (Native950Firemaking.isFireObject(object) && !Native950Cooking.available(player,object).isEmpty()) {
                productionMenu.openChoices("Use fire",java.util.Arrays.asList(
                        new Native950ProductionMenu.Choice("Cook food", () -> openCooking(object)),
                        new Native950ProductionMenu.Choice("Add logs", () -> {
                            if(Native950Woodcutting.current(object)&&canReachNow(object))
                                com.rs.game.player.actions.firemaking.Bonfire.addLogs(player,object);
                        })));
            } else if (Native950Firemaking.isFireObject(object)) com.rs.game.player.actions.firemaking.Bonfire.addLogs(player,object);
            else openCooking(object);
        } else com.rs.game.player.actions.firemaking.Bonfire.addLogs(player, object);
    }

    private void openCooking(WorldObject object) {
        productionMenu.openChoices("Choose food to cook",Native950Cooking.choices(player,object));
    }

    private boolean npcSkill(NPC npc,int option) {
        return Native950Fishing.isFishingSpot(npc,option) || Native950Thieving.isPickpocket(npc,option)
                || Native950Familiars.handlesNpc(npc,option) || Native950Divination.isHarvestable(npc,option) || Native950Hunter.isCatchable(npc,option) || Native950Slayer.handles(npc,option) || Native950Dungeoneering.handlesNpc(npc,option);
    }

    /** Follow an ordinary moving target, but never start an animation on the last movement frame. */
    private void processNpcSkillApproach() {
        NPC npc = pendingSkillNpc;
        if(npc==null)return;
        if(!router.playerMayAct() || player.isLocked() || player.getNextWorldTile()!=null || ++npcSkillApproachTicks>100
                || npcView==null || !npcView.canInteract(player,npc.getIndex())
                || World.getNPCs().get(npc.getIndex())!=npc || npc.hasFinished() || npc.isDead() || npc.isCantInteract()
                || !npcSkill(npc,npcSkillOption)) {
            pendingSkillNpc=null;player.resetWalkSteps();return;
        }
        if(player.hasWalkSteps() || player.getNextWalkDirection()!=-1 || player.hasTeleported())return;
        int count=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),
                player.getSize(),new EntityStrategy(npc),false);
        if(count<0 || RouteFinder.lastIsAlternative()) {
            pendingSkillNpc=null;reject("You cannot reach that NPC");return;
        }
        if(count>0) {
            int[] xs=RouteFinder.getLastPathBufferX(),ys=RouteFinder.getLastPathBufferY();
            for(int step=count-1;step>=0;step--){
                int remaining=128-player.getWalkSteps().size();
                if(remaining<=0||!player.addWalkSteps(xs[step],ys[step],remaining,true))break;
            }
            return;
        }
        pendingSkillNpc=null;
        // Fishing and Pickpocket moved to native slot3; their original controller gate remains Click2.
        int gate=(Native950Fishing.isFishingSpot(npc,npcSkillOption)||Native950Thieving.isPickpocket(npc,npcSkillOption))&&npcSkillOption==3?2:npcSkillOption;
        boolean permitted;
        switch(gate){
            case 1:permitted=player.getControlerManager().processNPCClick1(npc);break;
            case 2:permitted=player.getControlerManager().processNPCClick2(npc);break;
            case 3:permitted=player.getControlerManager().processNPCClick3(npc);break;
            case 4:permitted=player.getControlerManager().processNPCClick4(npc);break;
            default:permitted=false;
        }
        if(!permitted)return;
        // Divination explains its own capacity, level and reach refusals. Appending a
        // generic rejection hides that actionable explanation on the last chat line.
        if (Native950Familiars.handlesNpc(npc,npcSkillOption)) {
            dialogueNpc=npc;Native950Familiars.interact(player,npc,npcSkillOption);return;
        }
        if (Native950Divination.isHarvestable(npc,npcSkillOption)) {
            Native950Divination.startHarvest(player,npc,npcSkillOption);
            return;
        }
        if(Native950Dungeoneering.handlesNpc(npc,npcSkillOption)) {
            dialogueNpc=npc;Native950Dungeoneering.interactNpc(player,npc,npcSkillOption);return;
        }
        if(Native950Slayer.handles(npc,npcSkillOption)) {
            dialogueNpc=npc;
            Native950Slayer.interact(player,npc,npcSkillOption);
            if(!dialogues.isOpen())dialogueNpc=null;
            return;
        }
        if(Native950Hunter.isCatchable(npc,npcSkillOption)) {
            Native950Hunter.start(player,npc,npcSkillOption);
            return;
        }
        boolean started=Native950Fishing.isFishingSpot(npc,npcSkillOption)?Native950Fishing.start(player,npc,npcSkillOption)
                :Native950Thieving.isPickpocket(npc,npcSkillOption)?Native950Thieving.startPickpocket(player,npc,npcSkillOption)
                :Native950Hunter.start(player,npc,npcSkillOption);
        if(!started)reject("You cannot start that skill action right now");
    }

    private void object(Native950Actions.ObjectAction action) {
        walking(); player.setRouteEvent(null); player.resetWalkSteps();
        System.out.println("[Ataraxia950] Object action id=" + action.objectId() + " option=" + action.option()
                + " tile=" + action.x() + "," + action.y());
        if (!player.clientHasLoadedMapRegion() || Math.abs(action.x() - player.getX()) > 48
                || Math.abs(action.y() - player.getY()) > 48) {
            reject("Out-of-range object action"); return;
        }
        WorldObject object = findObject(action.objectId(), action.x(), action.y());
        boolean skill = skillOption(object, action.option());
        if (!skill && (action.option() != 2 || !validBank(object))) {
            reject("That object action is not available yet"); return;
        }
        pendingSkillEntry=Native950Agility.handles(object,action.option())?Native950Agility.approach(player,object):null;
        int count = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(),
                player.getPlane(), player.getSize(), pendingSkillEntry==null?new ObjectStrategy(object)
                        :new com.rs.game.route.strategy.FixedTileStrategy(pendingSkillEntry.getX(),pendingSkillEntry.getY()), false);
        if (count < 0 || RouteFinder.lastIsAlternative()) { reject("You cannot reach that object"); return; }
        int[] pathX = RouteFinder.getLastPathBufferX(), pathY = RouteFinder.getLastPathBufferY();
        for (int step = count - 1; step >= 0; step--) {
            int remaining = 128 - player.getWalkSteps().size();
            if (remaining <= 0 || !player.addWalkSteps(pathX[step], pathY[step], remaining, true)) break;
        }
        if (skill) {
            pendingSkillObject = object; pendingSkillOption = action.option(); skillApproachTicks = 0;
        } else {
            pendingBank = object; pendingObjectOption = action.option(); pendingTicks = 0;
        }
    }

    private void npc(Native950Actions.NpcAction action) {
        System.out.println("[Ataraxia950] NPC action index=" + action.index() + " option=" + action.option());
        // Resolution is fail closed and starts from the list this viewer's client was actually
        // SENT (Native950NpcView.canInteract reads the published index list, never the world
        // roster), so a client cannot name an NPC it was never shown. Only then is the index
        // resolved through World.getNPCs(), and the visibility, range and plane gates the
        // published list carries stay exactly as they were.
        if (npcView == null || !npcView.canInteract(player, action.index())) {
            reject("That NPC is not visible or in range"); return;
        }
        NPC clicked = World.getNPCs().get(action.index());
        if (clicked == null) { reject("That NPC is no longer in the world"); return; }
        if (clicked.isCantInteract() || !player.getMapRegionsIds().contains(clicked.getRegionId())) {
            reject("That NPC is not available"); return;
        }
        // Attack is a cache menu operation, not a fixed legacy packet/option number.
        // The verified development banker has its dedicated non-combat menu below.
        boolean banker = npcView.banker() != null && action.index() == npcView.banker().getIndex();
        String option;
        try { option = banker ? null : clicked.getNative950MenuOption(action.option()); }
        catch (RuntimeException failure) {
            reject("That NPC's menu is unavailable: " + failure.getClass().getSimpleName()); return;
        }
        if ("Attack".equalsIgnoreCase(option)) {
            if (!player.clientHasLoadedMapRegion()) { reject("The map is not ready for combat"); return; }
            try {
                if (!player.getControlerManager().processPlayerOption1(clicked) || !clicked.canBeAttacked(player)) {
                    reject("You cannot attack that NPC right now"); return;
                }
                prepareNpcInteraction();
                String refusal = combatActions.attack(player, clicked);
                if (refusal != null) reject(refusal);
            } catch (RuntimeException failure) {
                reject("That attack could not start: " + failure.getClass().getSimpleName());
            }
            return;
        }
        combatActions.cancelAttack(player);
        prepareNpcInteraction();
        if (npcSkill(clicked, action.option())) {
            int count=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),
                    player.getSize(),new EntityStrategy(clicked),false);
            if(count<0||RouteFinder.lastIsAlternative()){reject("You cannot reach that NPC");return;}
            int[] pathX=RouteFinder.getLastPathBufferX(),pathY=RouteFinder.getLastPathBufferY();
            for(int step=count-1;step>=0;step--){
                int remaining=128-player.getWalkSteps().size();
                if(remaining<=0||!player.addWalkSteps(pathX[step],pathY[step],remaining,true))break;
            }
            pendingSkillNpc=clicked;npcSkillOption=action.option();npcSkillApproachTicks=0;return;
        }
        // A diagnostic ID is verified against950 itself, not the old NPCHandler ID table.
        // Examine is cache-only; do not run910 content for a repurposed/new definition.
        if (clicked.isNative950DiagnosticDefinition()) {
            if (action.option() == Native950ActionRouter.NATIVE_NPC_EXAMINE_OPTION) {
                if (clicked.isDead() || clicked.hasFinished()
                        || !player.getControlerManager().processNPCExamine(clicked)) {
                    reject("That NPC is not available to examine"); return;
                }
                channel.write(Native950Packets.gameMessage(0, clicked.getName() + " (NPC " + clicked.getId() + ")."));
                return;
            }
            if (!Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC, clicked.getId())) {
                reject("This diagnostic NPC's interaction has not been ported yet"); return;
            }
        }
        // The banker keeps its own path: its Talk and Collect answers are native content (the
        // 910 forms are dialogues, which are M7), and its Bank option performs what the 910
        // banker dialogue's Bank option does. It is the regression anchor for this whole path,
        // so nothing below the branch changes for it.
        if (npcView.banker() == null || action.index() != npcView.banker().getIndex()) {
            dialogueNpc = clicked;
            // M4: every other published NPC goes into the 910 decoded overload
            // NPCHandler.dispatch through the P5 router, which repeats the handler's own
            // gates as a counted pre-filter and processes the RouteEvent the handler
            // installs. The client's run modifier is deliberately NOT forwarded: the native
            // walk path already refuses to let a modifier flag change movement speed, and an
            // NPC click must not be the way around that.
            Native950ActionRouter.Outcome outcome = router.npcOption(clicked, action.option(), false);
            if (!outcome.accepted) reject(outcome.reason);
            return;
        }
        if (action.option() == Native950ActionRouter.NATIVE_NPC_EXAMINE_OPTION) {
            Native950ActionRouter.Outcome outcome = router.npcExamine(npcView.banker(), content.banker.name);
            if (!outcome.accepted) reject(outcome.reason);
            return;
        }
        if (action.option() != content.banker.bankOption && action.option() != content.banker.talkOption
                && !(content.equipment != null && action.option() == content.banker.collectOption)) {
            reject("That NPC action is not available yet"); return;
        }
        int count = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(),
                player.getPlane(), player.getSize(), new EntityStrategy(npcView.banker()), false);
        if (count < 0 || RouteFinder.lastIsAlternative()) { reject("No route to banker"); return; }
        int[] pathX = RouteFinder.getLastPathBufferX(), pathY = RouteFinder.getLastPathBufferY();
        for (int step = count - 1; step >= 0; step--) {
            int remaining = 128 - player.getWalkSteps().size();
            if (remaining <= 0 || !player.addWalkSteps(pathX[step], pathY[step], remaining, true)) break;
        }
        pendingNpcOption = action.option(); pendingTicks = 0;
    }

    private void prepareNpcInteraction() {
        cancelSkill();
        cancelConversations();
        worldMap.close();
        settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        closeBank(); pendingBank = null; pendingNpcOption = 0;
        player.setRouteEvent(null);
        player.resetWalkSteps();
    }

    private boolean validNpc() { return npcView != null && npcView.canInteract(player, npcView.banker().getIndex()); }
    private boolean canReachNpc() {
        return RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(), player.getPlane(),
                player.getSize(), new EntityStrategy(npcView.banker()), false) == 0;
    }

    private void button(Native950Actions.InterfaceAction action) {
        Native950BugTest.event(player,"interface","button-dispatch","interface",action.interfaceId(),"component",action.componentId(),"slot",action.slot(),"option",action.option());
        if(itemBrowser.handle(action))return;
        if(Native950Prayer.button(player,action))return;
        if(player.getNative950ActionBar().button(player,channel,action))return;
        System.out.println("[Ataraxia950] Interface action " + action.interfaceId() + ":" + action.componentId()
                + " option=" + action.option() + " slot=" + action.slot() + " item=" + action.itemId());
        // A quick-options action is accepted only while its owned window is open.
        if (action.interfaceId() == Native950ExitUi.INTERFACE && !exitUi.isOpen()) {
            rejectedActions++; return;
        }
        if (Native950ExitUi.isOpenRequest(action) && !exitUi.isOpen()) {
            if (player.isLocked() || player.closeInterfaceLocked || player.isDead()
                    || player.isNative950ForceMovementActive() || player.getNextWorldTile() != null) {
                reject("You cannot open the exit menu right now"); return;
            }
            exitUi.verifyBeforeOpen();
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
        }
        // Escape belongs to Quick Options while it is open, before the Settings shortcut.
        if (exitUi.handle(action)) return;
        if (action.interfaceId() == Native950ExitUi.INTERFACE && action.componentId() == 66
                && action.option() == 1 && action.slot() == -1 && action.itemId() == -1) {
            player.sendMessage("You are already in World 1, the only local world."); return;
        }
        if (Native950ToolbeltUi.isOpenRequest(action)) {
            if (!pouchAvailable()) { reject("You cannot open the toolbelt right now"); return; }
            toolbeltUi.verifyBeforeOpen();
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
        }
        if (toolbeltUi.handle(action)) return;
        if (productionMenu.handle(action)) return;
        if (forgeUi.handle(action)) return;
        if (Native950Lodestones.isOpenRequest(action)) {
            if (player.isLocked() || player.closeInterfaceLocked || player.isNative950ForceMovementActive()
                    || player.getNextWorldTile() != null) {
                reject("You cannot open the lodestone network right now"); return;
            }
            lodestones.verifyBeforeOpen();
            cancelSkill();
            combatActions.cancelAttack(player);
            closeBank(); pendingBank = null; pendingNpcOption = 0;
            pendingObjectOption = 0; pendingTicks = 0;
            player.resetWalkSteps(); player.setRouteEvent(null);
            cancelConversations();
            worldMap.close(); settings.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        }
        if (lodestones.handle(action)) return;
        if (Native950SkillGuide.isOpenRequest(action)) {
            if (player.isLocked() || player.closeInterfaceLocked || player.isNative950ForceMovementActive()
                    || player.getNextWorldTile() != null) {
                reject("You cannot open a management menu right now"); return;
            }
            skillGuide.verifyBeforeOpen();
            cancelSkill(); combatActions.cancelAttack(player);
            closeBank(); pendingBank = null; pendingNpcOption = 0;
            pendingObjectOption = 0; pendingTicks = 0;
            player.resetWalkSteps(); player.setRouteEvent(null);
            cancelConversations(); worldMap.close(); settings.close(); lodestones.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        }
        if (skillGuide.handle(action)) return;
        if (Native950Settings.isOpenRequest(action)) {
            if (player.isLocked() || player.closeInterfaceLocked) {
                reject("You cannot open settings right now");
                return;
            }
            // Verify before replacing another owned UI, so cache drift cannot
            // cancel a valid bank, map or quantity request as a side effect.
            settings.verifyBeforeOpen();
            cancelSkill();
            combatActions.cancelAttack(player);
            closeBank(); pendingBank = null; pendingNpcOption = 0;
            pendingObjectOption = 0; pendingTicks = 0;
            player.resetWalkSteps();
            player.setRouteEvent(null);
            cancelConversations();
            worldMap.close();
            lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        }
        if (settings.handle(action)) return;
        if (Native950WorldMap.isOpenRequest(action)) {
            cancelSkill();
            if (player.isLocked() || player.closeInterfaceLocked) {
                reject("You cannot open the world map right now");
                return;
            }
            combatActions.cancelAttack(player);
            closeBank(); pendingBank = null; pendingNpcOption = 0;
            cancelConversations();
            settings.close(); lodestones.close(); skillGuide.close(); toolbeltUi.close(); forgeUi.close(); exitUi.close();
        }
        if (worldMap.handle(action)) return;
        if (Native950InventionUi.target(action.componentHash(),action.slot(),action.itemId()) && action.option()==1) {
            if(!pouchAvailable()){reject("You cannot open the Invention pouch right now");return;}
            walking(); player.resetWalkSteps(); player.setRouteEvent(null);
            productionMenu.openChoices("Invention pouch",Native950Invention.pouchChoices(player)); return;
        }
        if (bankControl(action)) return;
        Native950ActionRouter.Binding binding = router.binding(action.componentHash());
        // An inert or client-owned panel is not a player error. The resolver
        // records the exact pair, and the session keeps its rejection count.
        if (binding == null) { rejectedActions++; return; }
        switch (binding.target) {
            case BACKPACK_ITEMS: backpack(action, binding); return;
            case EQUIPMENT_ITEMS: equipment(action, binding); return;
            case RUN_ORB:
                Native950ActionRouter.Outcome outcome = router.runOrb(binding, action.option(), action.slot(), action.itemId());
                if (!outcome.accepted) reject(outcome.reason);
                return;
            default: bank(action, binding);
        }
    }

    private void syncBankPreferences() {
        com.rs.game.player.Bank bank = player.getBank();
        int raw = bank.getNativeDefaultInteractionAmount();
        int mode = raw == 1 ? 2 : raw == 5 ? 3 : raw == 10 ? 4 : raw == Integer.MAX_VALUE ? 7 : 5;
        for (Native950Packets.Packet packet : Native950BankUi.sync(mode, bank.getLastX(), bank.getWithdrawNotes()))
            channel.write(packet);
    }

    /** Native local control scripts predict state; commit the same choice on the world thread. */
    private boolean bankControl(Native950Actions.InterfaceAction action) {
        if (action.interfaceId() != 517 || action.componentId() == 39 || action.componentId() == 317
                || !Native950BankUi.isControl(action.componentId())) return false;
        if (!bankAvailable()) { closeBank(); reject("Bank action requires an open bank in reach"); return true; }
        int component = action.componentId();
        if (Native950BankUi.isUnsupportedControl(component)) {
            cancelQuantity();
            for (Native950Packets.Packet packet : Native950BankUi.resetUnsupportedModes()) channel.write(packet);
            syncBankPreferences();
            reject("That bank feature has not been ported yet");
            return true;
        }
        if (action.option() != 1) { reject("That bank control option is not available yet"); return true; }
        if (component == 114 && quantityInput.active()) { rejectedActions++; return true; }
        cancelQuantity();
        com.rs.game.player.Bank bank = player.getBank();
        int mode = bank.getNativeDefaultInteractionAmount();
        switch (component) {
            case 93: mode = 1; break;
            case 96: mode = 5; break;
            case 99: mode = 10; break;
            case 103: mode = Integer.MAX_VALUE; break;
            case 106: mode = 11; break;
            case 114:
                cancelDialogue();
                if (quantityInput.beginDefault(bankEpoch, containers.bankSnapshot())) showQuantityPrompt();
                else rejectedActions++;
                return true;
            case 127:
                bank.restoreNativePreferences(!bank.getWithdrawNotes(), bank.getLastX(), mode);
                syncBankPreferences(); return true;
            case 237: case 238: case 239:
                // CS9325 filters/moves original bank child indices locally. A server
                // refresh here would disturb keyboard focus and typed search text.
                return true;
            case 42:
                bank.depositAllEquipment(true);
                observeEquipment("Deposited");
                refreshBank(); return true;
            case 56: case 152: case 165:
                for (Native950Packets.Packet packet : Native950BankUi.resetUnsupportedModes()) channel.write(packet);
                syncBankPreferences(); return true;
            default:
                for (Native950Packets.Packet packet : Native950BankUi.resetUnsupportedModes()) channel.write(packet);
                syncBankPreferences();
                reject("That bank feature has not been ported yet");
                return true;
        }
        bank.restoreNativePreferences(bank.getWithdrawNotes(), bank.getLastX(), mode);
        syncBankPreferences();
        return true;
    }

    private void bank(Native950Actions.InterfaceAction action, Native950ActionRouter.Binding binding) {
        // The native Close hook can emit CLOSE_MODAL before its operation packet.
        // Treat both reports as the same idempotent cleanup, even after the bank closed.
        if (binding.target == Native950ActionRouter.Target.BANK_CLOSE) {
            if (action.option() == 1) closeBank(); else reject("Unsupported bank operation");
            return;
        }
        if (!bankAvailable()) {
            closeBank(); reject("Bank action requires an open bank in reach"); return;
        }
        if ((binding.target == Native950ActionRouter.Target.BANK_ITEMS
                || binding.target == Native950ActionRouter.Target.BANK_INVENTORY) && content.bank.withdrawXOption != 0
                && action.option() == content.bank.withdrawXOption) {
            beginBankQuantity(action, binding.target == Native950ActionRouter.Target.BANK_INVENTORY); return;
        }
        // Another bank action retires any pending amount before it changes the containers.
        cancelQuantity();
        if (action.option() == Native950ActionRouter.NATIVE_EXAMINE_OPTION
                && binding.target != Native950ActionRouter.Target.BANK_DEPOSIT_ALL) {
            Native950Containers.Snapshot state = binding.target == Native950ActionRouter.Target.BANK_INVENTORY
                    ? containers.inventorySnapshot() : containers.bankSnapshot();
            if (!Native950ActionRouter.exactClaim(action.itemId(), action.slot(), state)) { reject("The item in that slot has changed"); return; }
            Native950ActionRouter.Outcome outcome = router.button(binding, router.bankPacketId(binding, action.option()), action.slot(), action.itemId());
            if (!outcome.accepted) reject(outcome.reason);
            return;
        }
        if (binding.target == Native950ActionRouter.Target.BANK_DEPOSIT_ALL) {
            int packetId = router.bankPacketId(binding, action.option());
            if (packetId < 0) { reject("Unsupported bank operation"); return; }
            Native950Containers.Snapshot before = containers.inventorySnapshot();
            Native950ActionRouter.Outcome outcome = router.button(binding, packetId, action.slot(), action.itemId());
            if (!outcome.accepted) { reject(outcome.reason); refreshBank(); return; }
            if (changedSlots(before, containers.inventorySnapshot()) > 0) transactions++;
            refreshBank(); return;
        }
        int amount = router.bankAmount(binding, action.option());
        int packetId = router.bankPacketId(binding, action.option());
        if (amount <= 0 || packetId < 0) { reject("Unsupported bank operation"); refreshBank(); return; }
        int itemId;
        Native950Containers.Snapshot inventoryBefore = containers.inventorySnapshot();
        Native950Containers.Snapshot bankBefore = containers.bankSnapshot();
        if (binding.target == Native950ActionRouter.Target.BANK_INVENTORY) {
            itemId = Native950ActionRouter.transferItem(action.itemId(), action.slot(), inventoryBefore, amount, -1,
                    changedInventorySlots, content.items);
            if (!Native950ActionRouter.exactClaim(itemId, action.slot(), inventoryBefore)) {
                logBankClaimMismatch(action, inventoryBefore, amount, -1, changedInventorySlots);
                reject("The item in that slot has changed"); refreshBank(); return;
            }
        } else {
            int slot = action.slot();
            int currentId = slot >= 0 && slot < bankBefore.ids.length ? bankBefore.ids[slot] : -1;
            int movable = currentId < 0 ? 0 : Native950Banking.withdrawableAmount(player, containers, slot, currentId, amount);
            itemId = Native950ActionRouter.withdrawalItem(action.itemId(), slot, bankBefore, movable, changedBankSlots, content.items);
            if (itemId < 0) {
                logBankClaimMismatch(action, bankBefore, amount, movable, changedBankSlots);
                reject("The item in that slot has changed"); refreshBank(); return;
            }
        }
        boolean deposit = binding.target == Native950ActionRouter.Target.BANK_INVENTORY;
        int outputId = deposit ? itemId : Native950Banking.withdrawnItemId(player, itemId);
        int pouchBefore = player.getMoneyPouchValue();
        Native950ActionRouter.Outcome outcome = router.button(binding, packetId, action.slot(), itemId);
        if (!outcome.accepted) { reject(outcome.reason); refreshBank(); return; }
        redirectPouchCoins(pouchBefore);
        long moved = deposit ? total(inventoryBefore, itemId) - total(containers.inventorySnapshot(), itemId)
                : total(containers.inventorySnapshot(), outputId) - total(inventoryBefore, outputId);
        if (moved > 0) {
            transactions++;
            System.out.println("[Ataraxia950] Bank item transfer id=" + itemId + " amount=" + moved
                    + " component=" + binding.name + " option=" + action.option() + " packet=" + packetId);
            channel.write(Native950Packets.gameMessage(0, (deposit ? "Deposited " : "Withdrew ") + moved + " x "
                    + content.items.get(itemId).name + "."));
        } else reject("The destination has no room for this item");
        refreshBank(); // correct stale client state even when an action was refused
    }

    /**
     * {@code Bank.withdrawItem} routes coins into the 910 money pouch. The verified
     * 947 open recipe promises coins to the backpack (varbit 45158 = 1), so the
     * coins the 910 bank just pouched are moved into the real inventory through the
     * 910 {@code Inventory.addItem}. Counted; see notes/P5-router.md for the Bank
     * change that would make this unnecessary.
     */
    private void redirectPouchCoins(int pouchBefore) {
        int gained = player.getMoneyPouchValue() - pouchBefore;
        if (gained <= 0) return;
        player.getMoneyPouch().setTotal(pouchBefore);
        if (player.getInventory().addItem(new Item(COINS, gained))) coinPouchRedirects++;
        else player.getMoneyPouch().setTotal(pouchBefore + gained);
    }

    private void itemOnItem(Native950Actions.ItemOnItemAction action) {
        if (bankOpen || action.sourceHash() != ((BACKPACK_INTERFACE << 16) | BACKPACK_ITEMS)
                || action.targetHash() != ((BACKPACK_INTERFACE << 16) | BACKPACK_ITEMS)
                || action.sourceSlot() == action.targetSlot()) {
            reject("Use both items from your backpack"); return;
        }
        Native950Containers.Snapshot state = containers.inventorySnapshot();
        if (!Native950ActionRouter.exactClaim(action.sourceItemId(), action.sourceSlot(), state)
                || !Native950ActionRouter.exactClaim(action.targetItemId(), action.targetSlot(), state)
                || changedInventorySlots.contains(action.sourceSlot()) || changedInventorySlots.contains(action.targetSlot())) {
            reject("One of those items has changed"); sendInventory(); return;
        }
        if (!player.getControlerManager().canUseItemOnItem(player.getInventory().getItem(action.sourceSlot()),player.getInventory().getItem(action.targetSlot()))) {
            reject("You cannot combine those items here"); return;
        }
        int log = action.sourceItemId() == 590 ? action.targetItemId()
                : action.targetItemId() == 590 ? action.sourceItemId() : -1;
        java.util.List<Native950Production.Recipe> recipes = Native950Production.pair(action.sourceItemId(), action.targetItemId());
        java.util.List<Native950Production.Recipe> crafting = Native950Crafting.pair(action.sourceItemId(), action.targetItemId());
        if (!Native950Firemaking.isSupportedLog(log) && recipes.isEmpty() && crafting.isEmpty()) {
            reject("That item combination is not available yet"); return;
        }
        walking(); player.setRouteEvent(null); player.resetWalkSteps();
        if (!crafting.isEmpty()) productionMenu.openChoices("Choose what to craft",Native950Crafting.inventoryChoices(player,crafting));
        else if (!recipes.isEmpty()) productionMenu.open("Choose what to make", recipes);
        else com.rs.game.player.actions.firemaking.Firemaking.isFiremaking(player,
                new Item(action.sourceItemId(),1), new Item(action.targetItemId(),1));
        observeInventory();
    }

    private boolean selectedBackpackItem(int hash,int slot,int itemId) {
        if(bankOpen||hash!=((BACKPACK_INTERFACE<<16)|BACKPACK_ITEMS)) {
            reject("Select the item from your backpack"); return false;
        }
        if(!Native950ActionRouter.exactClaim(itemId,slot,containers.inventorySnapshot())||changedInventorySlots.contains(slot)) {
            reject("The selected item has changed");sendInventory();return false;
        }
        return true;
    }

    private int itemOnObjectMode(WorldObject object,int itemId) {
        if(Native950Prayer.acceptsItem(object,itemId))return 9;
        if(Native950Farming.acceptsItem(object,itemId))return 5;
        if(Native950Construction.acceptsItem(object,itemId))return 6;
        if(Native950Invention.isStation(object))return 7;
        if(Native950Archaeology.isStation(object))return 8;
        if(object==null)return 0;
        if(Native950Cooking.isCookingObject(object)&&Native950Cooking.supportsRaw(itemId))return 1;
        if(Native950Firemaking.isFireObject(object)&&Native950Firemaking.isSupportedLog(itemId))return 2;
        if((itemId==1436||itemId==7936)&&Native950Runecrafting.definition(object)!=null)return 3;
        if(Native950Smithing.isStation(object)&&Native950Smithing.isInput(itemId))return 4;
        return 0;
    }

    private void itemOnObject(Native950Actions.ItemOnObjectAction action) {
        if(!selectedBackpackItem(action.sourceHash(),action.sourceSlot(),action.sourceItemId()))return;
        if(!player.clientHasLoadedMapRegion()||Math.abs(action.x()-player.getX())>48||Math.abs(action.y()-player.getY())>48) {
            reject("That object is outside your loaded area");return;
        }
        WorldObject target=findObject(action.objectId(),action.x(),action.y());
        if(target==null){reject("That object is no longer there");return;}
        if(itemOnObjectMode(target,action.sourceItemId())==0){
            reject("Using "+content.items.get(action.sourceItemId()).name+" on "+target.getDefinitions().name+" has not been ported yet");return;
        }
        walking();player.setRouteEvent(null);player.resetWalkSteps();
        int count=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),player.getSize(),new ObjectStrategy(target),false);
        if(count<0||RouteFinder.lastIsAlternative()){reject("You cannot reach that object");return;}
        int[] xs=RouteFinder.getLastPathBufferX(),ys=RouteFinder.getLastPathBufferY();
        for(int step=count-1;step>=0;step--){int remaining=128-player.getWalkSteps().size();if(remaining<=0||!player.addWalkSteps(xs[step],ys[step],remaining,true))break;}
        pendingSkillEntry=null;pendingSkillObject=target;pendingItemOnObject=action;skillApproachTicks=0;
    }

    private void completeItemOnObject(WorldObject object,Native950Actions.ItemOnObjectAction action) {
        if(!selectedBackpackItem(action.sourceHash(),action.sourceSlot(),action.sourceItemId()))return;
        Item selected=player.getInventory().getItem(action.sourceSlot());
        if(!player.getControlerManager().handleItemOnObject(object,selected)) {
            reject("You cannot use that item here");return;
        }
        int mode=itemOnObjectMode(object,action.sourceItemId());
        if(mode==1){
            productionMenu.openChoices("Cook food",java.util.Collections.singletonList(Native950Cooking.choice(
                    player,object,action.sourceItemId())));
        } else if(mode==2){
            com.rs.game.player.actions.firemaking.defs.Log log=com.rs.game.player.actions.firemaking.defs.Log.forId(action.sourceItemId());
            if(Native950Firemaking.checkBonfire(player,log,object))player.getActionManager().setAction(new com.rs.game.player.actions.firemaking.Bonfire(log,object,false));
        } else if(mode==3)Native950Runecrafting.start(player,object);
        else if(mode==4)forgeUi.open(Native950Smithing.choices(player,object),false,object);
        else if(mode==5)Native950Farming.use(player,object,action.sourceItemId());
        else if(mode==6)productionMenu.openChoices("Construct furniture",Native950Construction.choices(player,object));
        else if(mode==7)productionMenu.openChoices("Invention workbench",Native950Invention.choices(player,object));
        else if(mode==9)Native950Prayer.startAltar(player,object,action.sourceItemId(),10000);
        else if(mode==8)productionMenu.openChoices("Archaeology workbench",Native950Archaeology.choices(player,object));
        else reject("That item can no longer be used on this object");
    }

    private void itemOnNpc(Native950Actions.ItemOnNpcAction action) {
        if(Native950ActionBar.barSlot(action.sourceInterfaceId(),action.sourceComponentId())>=0
                ||Native950ActionBar.bookType(action.sourceInterfaceId(),action.sourceComponentId())>0){
            int structure=player.getNative950ActionBar().selectedStructure(player,action.sourceInterfaceId(),action.sourceComponentId(),action.sourceSlot());
            if(structure<0||npcView==null||!npcView.canInteract(player,action.index())||!player.clientHasLoadedMapRegion()){
                reject("Select an available ability and visible target");return;
            }
            NPC target=World.getNPCs().get(action.index());
            if(target==null||!target.canBeAttacked(player)||!player.getControlerManager().processPlayerOption1(target))return;
            if(Native950MeleeCombat.abilityStyle(structure)<0){reject("That ability is not implemented yet");return;}
            Native950MeleeCombat combat=player.getNative950Combat();
            String refusal=combat==null?"Combat is not ready":combat.attack(player,target);
            if(refusal==null)refusal=combat.ability(player,structure);
            if(refusal!=null)reject(refusal);
            return;
        }
        if(!selectedBackpackItem(action.sourceHash(),action.sourceSlot(),action.sourceItemId()))return;
        if(npcView==null||!npcView.canInteract(player,action.index())){reject("That NPC is not visible or in range");return;}
        NPC target=World.getNPCs().get(action.index());
        if(target==null||target.hasFinished()||target.isDead()||target.isCantInteract()){reject("That NPC is no longer available");return;}
        reject("Using "+content.items.get(action.sourceItemId()).name+" on "+target.getName()+" has not been ported yet");
    }


    private void backpack(Native950Actions.InterfaceAction action, Native950ActionRouter.Binding binding) {
        Native950Containers.Snapshot inventory = containers.inventorySnapshot();
        if (!Native950ActionRouter.exactClaim(action.itemId(), action.slot(), inventory)) {
            reject("The item in that slot has changed"); sendInventory(); return;
        }
        // Cache backpack onOp 1620 does not move the actor before the sender:
        // the original item ID was required above, unlike bank deposit prediction.
        Native950ItemCatalog.Entry item = content.items.get(action.itemId());
        int cacheOption = Native950InventoryMenu.ordinaryCacheOption(action.option());
        int packetId = action.option() == Native950ActionRouter.NATIVE_EXAMINE_OPTION ? Native950ActionRouter.packetIdFor(action.option()) : (cacheOption > 0 ? Native950ActionRouter.packetIdFor(cacheOption) : -1);
        if (packetId < 0) { reject("That item action is not available yet"); return; }
        if (action.option() == Native950ActionRouter.NATIVE_EXAMINE_OPTION) {
            Native950ActionRouter.Outcome outcome = router.button(binding, packetId, action.slot(), action.itemId());
            if (!outcome.accepted) reject(outcome.reason);
            return;
        }
        if (bankOpen) { reject("That item action is not available here"); return; }
        if (changedInventorySlots.contains(action.slot())) { reject("That item has changed"); sendInventory(); return; }
        int equipmentOption = Native950InventoryMenu.equipmentCacheOption(player, action.itemId(), action.option());
        if (equipmentOption > 0) {
            Native950EquipmentActions.Result outcome = Native950EquipmentActions.equip(player, containers, action.slot(), action.itemId(), equipmentOption);
            if (!outcome.accepted) { reject(outcome.reason); return; }
            // A gear swap changes the loadout, not the player's route or combat target.
            observeEquipment("Equipped"); return;
        }
        String itemOption=item==null?null:item.option(cacheOption);
        if (Native950Potions.handles(action.itemId(),itemOption)) {
            if (!Native950Potions.drink(player,action.slot(),action.itemId())) { reject("You cannot drink that potion right now"); return; }
            if (changedSlots(inventory,containers.inventorySnapshot())>0) transactions++;
            observeInventory(); return;
        }
        if(!Native950InventoryMenu.usesOrdinaryOperations(action.itemId()) || item==null || item.option(cacheOption)==null){reject("That special item menu action has not been ported yet");return;}
        if ("Light".equalsIgnoreCase(item.option(cacheOption)) && Native950Firemaking.isSupportedLog(action.itemId())) {
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
            com.rs.game.player.actions.firemaking.Firemaking.isFiremaking(player, action.itemId());
            observeInventory();
            return;
        }
        if (Native950Toolbelt.isAddOption(itemOption)) {
            Native950Toolbelt.add(player,action.slot(),action.itemId(),itemOption);
            observeInventory(); return;
        }
        if(Native950Divination.isWeavable(action.itemId(),cacheOption)){
            walking();player.setRouteEvent(null);player.resetWalkSteps();
            productionMenu.openChoices("Divination boons",Native950Divination.weaveChoices(player,action.slot(),action.itemId(),cacheOption));return;
        }
        if(Native950Familiars.handlesItem(action.itemId(),itemOption)){
            walking();player.setRouteEvent(null);player.resetWalkSteps();
            Native950Familiars.item(player,action.slot(),action.itemId(),itemOption);observeInventory();return;
        }

        if(Native950Dungeoneering.handlesItem(action.itemId(),itemOption)){
            walking();player.setRouteEvent(null);player.resetWalkSteps();
            Native950Dungeoneering.item(player,action.itemId(),itemOption);return;
        }
        if ("Eat".equalsIgnoreCase(itemOption)) {
            Native950Food.Result result = Native950Food.eat(player,containers,action.slot(),action.itemId());
            if (!result.accepted) { reject(result.reason); return; }
            transactions++; observeInventory(); return;
        }
        if ("Drop".equalsIgnoreCase(itemOption)) {
            Native950InventoryDrop.Result result = Native950InventoryDrop.drop(player,containers,action.slot(),action.itemId());
            if (result.moved == 0) { reject(result.reason); return; }
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
            transactions++; observeInventory();
            System.out.println("[Ataraxia950] Inventory Drop item="+action.itemId()+" amount="+result.moved+" tile="+player.getX()+","+player.getY());
            return;
        }
        if ("Bury".equalsIgnoreCase(itemOption) || "Scatter".equalsIgnoreCase(itemOption)) {
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
            if (!Native950Prayer.offer(player,action.slot(),action.itemId(),cacheOption)) reject("You cannot offer that item right now");
            observeInventory();return;
        }
        if ("Clean".equalsIgnoreCase(itemOption)) {
            Native950Production.Recipe recipe = Native950Production.cleaning(action.itemId());
            if (recipe != null) {
                walking(); player.setRouteEvent(null); player.resetWalkSteps();
                player.getActionManager().setAction(new Native950ProductionAction(recipe,1));return;
            }
        }
        if ("Loot".equalsIgnoreCase(itemOption) && Native950Implings.isJar(action.itemId())) {
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
            if(Native950Implings.loot(player,action.slot(),action.itemId(),cacheOption)) transactions++;
            observeInventory();return;
        }
        if ("Release".equalsIgnoreCase(itemOption) && Native950Hunter.itemEntry(action.itemId())!=null) {
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
            if(!Native950Hunter.release(player,action.slot(),action.itemId(),cacheOption)) reject("You cannot release that butterfly right now");
            observeInventory();return;
        }
        java.util.List<Native950Production.Recipe> crafting = Native950Crafting.inventoryRecipes(action.itemId(),itemOption);
        if(!crafting.isEmpty()) {
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
            productionMenu.openChoices("Choose what to craft",Native950Crafting.inventoryChoices(player,crafting));return;
        }
        java.util.List<Native950Production.Recipe> itemRecipes = Native950Production.inventoryRecipes(action.itemId(),itemOption);
        if (!itemRecipes.isEmpty()) {
            walking(); player.setRouteEvent(null); player.resetWalkSteps();
            productionMenu.open("Choose what to make",itemRecipes);return;
        }
        reject("The " + itemOption + " action for " + item.name + " has not been ported yet");
    }

    private void equipment(Native950Actions.InterfaceAction action, Native950ActionRouter.Binding binding) {
        Native950Containers.Snapshot worn = containers.equipmentSnapshot();
        if (!Native950ActionRouter.exactClaim(action.itemId(), action.slot(), worn)) {
            reject("The equipment in that slot has changed"); sendEquipment(); return;
        }
        int packetId = Native950ActionRouter.packetIdFor(action.option());
        if (action.option() == Native950ActionRouter.NATIVE_EXAMINE_OPTION) {
            Native950ActionRouter.Outcome outcome = router.button(binding, packetId, action.slot(), action.itemId());
            if (!outcome.accepted) reject(outcome.reason);
            return;
        }
        if (bankOpen || action.option() != 1) { reject("That equipment action is not available here"); return; }
        Native950EquipmentActions.Result outcome = Native950EquipmentActions.remove(player, containers, action.slot(), action.itemId());
        if (!outcome.accepted) { reject(outcome.reason); sendInventory(); sendEquipment(); return; }
        observeEquipment("Removed");
    }

    /** Publish one committed equipment exchange, including stack quantity changes. */

    private boolean observeEquipment(String verb) {
        Native950Containers.Snapshot now = containers.equipmentSnapshot();
        if (java.util.Arrays.equals(observedEquipment.ids, now.ids) && java.util.Arrays.equals(observedEquipment.amounts, now.amounts)) return false;
        int changedId = -1;
        for (int slot = 0; slot < now.ids.length; slot++) {
            if (now.ids[slot] == observedEquipment.ids[slot] && now.amounts[slot] == observedEquipment.amounts[slot]) continue;
            if (changedId < 0) changedId = now.ids[slot] >= 0 ? now.ids[slot] : observedEquipment.ids[slot];
            if ("Equipped".equals(verb) && now.ids[slot] >= 0) { changedId = now.ids[slot]; break; }
        }
        observedEquipment = now;
        transactions++;
        deferredEquipmentChanges++;
        Native950ItemCatalog.Entry item = content.items.get(changedId);
        channel.write(Native950Packets.gameMessage(0, verb + " " + (item == null ? "item " + changedId : item.name) + "."));
        System.out.println("[Ataraxia950] Equipment " + verb.toLowerCase() + " id=" + changedId + " through the cache-driven equipment transaction");
        sendInventory(); sendEquipment();
        return true;
    }

    private boolean pouchAvailable(){
        return !bankOpen&&!player.isLocked()&&!player.closeInterfaceLocked&&!player.isDead()
            && !player.isNative950ForceMovementActive()&&player.getNextForceMovement()==null&&player.getNextWorldTile()==null;
    }

    private void drag(Native950Actions.DragAction action) {
        Native950BugTest.event(player,"drag","dispatch","source",action.sourceInterfaceId()+":"+action.sourceComponentId(),"target",action.targetInterfaceId()+":"+action.targetComponentId());
        if(player.getNative950ActionBar().drag(player,channel,action))return;
        if(Native950InventionUi.target(action.targetComponentHash(),action.targetSlot(),action.targetItemId())){
            if(!pouchAvailable()){reject("You cannot disassemble items right now");return;}
            if(!selectedBackpackItem(action.sourceComponentHash(),action.sourceSlot(),action.sourceItemId()))return;
            // Pouch drops do not execute3902's same-grid client swap. Validate the original slot.
            walking();player.resetWalkSteps();player.setRouteEvent(null);
            productionMenu.openChoices("Disassemble item",Native950Invention.disassemblyChoices(player,action.sourceSlot(),action.sourceItemId()));
            sendInventory();return;
        }
        Native950ActionRouter.Binding source = router.binding(action.sourceComponentHash());
        Native950ActionRouter.Binding target = router.binding(action.targetComponentHash());
        if (bankOpen || source == null || target == null
                || source.target != Native950ActionRouter.Target.BACKPACK_ITEMS
                || target.target != Native950ActionRouter.Target.BACKPACK_ITEMS) {
            reject("Unsupported item drag"); return;
        }
        // Cache script 1616 -> 3902 swaps the two actors before the native drag
        // sender reads them. Accept only that reversed pair; accepting both
        // orders would let a repeated packet undo the first authoritative swap.
        Native950Containers.Snapshot inventory = containers.inventorySnapshot();
        int from = action.sourceSlot(), to = action.targetSlot();
        if (!Native950ActionRouter.dragSlotsValid(inventory, from, to)) {
            reject("Invalid slot or quantity"); sendInventory(); return;
        }
        if (!Native950ActionRouter.reversedDragPair(inventory, from, to, action.sourceItemId(), action.targetItemId())) {
            reject("The item in that slot has changed"); sendInventory(); return;
        }
        player.getInventory().switchItem(from, to); // the 910 drag path (PacketRepository switchComponents)
        transactions++;
        System.out.println("[Ataraxia950] Backpack slots swapped " + from + " -> " + to + " through Inventory.switchItem");
        sendInventory();
    }

    private void logBankClaimMismatch(Native950Actions.InterfaceAction action,
                                      Native950Containers.Snapshot source, int requested, int movable,
                                      java.util.Set<Integer> changed) {
        int slot = action.slot();
        int current = slot >= 0 && slot < source.ids.length ? source.ids[slot] : -1;
        int quantity = current < 0 ? 0 : source.amounts[slot];
        int next = slot >= 0 && slot + 1 < source.ids.length ? source.ids[slot + 1] : -1;
        System.out.println("[Ataraxia950] Bank claim mismatch component=" + action.componentId()
                + " option=" + action.option() + " slot=" + slot + " claimed=" + action.itemId()
                + " current=" + current + " quantity=" + quantity + " next=" + next
                + " requested=" + requested + " movable=" + movable + " changedThisTick=" + changed.contains(slot)
                + " notes=" + player.getBank().getWithdrawNotes() + " bankEpoch=" + bankEpoch
                + " mount=" + player.getInterfaceManager().getInterfaceParentId(517));
    }

    private void refreshBank() { compactBank(); sendInventory(); sendBank(); }

    /**
     * The 910 bank leaves an empty slot behind a full withdrawal until the bank
     * closes ({@code Bank.shiftItems} on {@code removeBankInterface}). We compact
     * before publishing the authoritative full container and occupied span. This
     * server refresh is separate from the client onOp, which clears the selected
     * actor to48447 before the button sender reads its held reference.
     */
    private void compactBank() {
        player.getBank().shiftItems();
    }

    /** Original skill actions mutate shared containers during processEntity/WorldTasksManager. */
    private void observeInventory() {
        if (displayedInventory == null) return;
        Native950Containers.Snapshot now = containers.inventorySnapshot();
        if (!java.util.Arrays.equals(displayedInventory.ids, now.ids)
                || !java.util.Arrays.equals(displayedInventory.amounts, now.amounts)) sendInventory();
    }

    private void sendInventory() {
        Native950Containers.Snapshot state = containers.inventorySnapshot();
        rememberChangedSlots(displayedInventory, state, changedInventorySlots);
        displayedInventory = state;
        channel.write(Native950Packets.inventoryFull(INVENTORY_CONTAINER, false, state.ids, state.amounts));
    }
    private void sendBank() {
        Native950Containers.Snapshot state = containers.bankSnapshot();
        rememberChangedSlots(displayedBank, state, changedBankSlots);
        displayedBank = state;
        int occupied = 0;
        for (int id : state.ids) if (id >= 0) occupied++;
        // The native All Items layout also needs its authoritative occupied span.
        // Reset the client's optimistic count along with the container contents.
        // No holes exist inside this compact occupied span. Retire the client's
        // pending optimistic-compaction marker before replacing its count/items.
        channel.write(Native950Packets.varp(8970, -1));
        channel.write(Native950Packets.varp(8971, occupied));
        channel.write(Native950Packets.inventoryFull(BANK_CONTAINER, false, state.ids, state.amounts));
    }

    private void sendEquipment() {
        if (content.equipment == null) return;
        Native950Containers.Snapshot state = containers.equipmentSnapshot();
        channel.write(Native950Packets.inventoryFull(content.equipment.containerId, false, state.ids, state.amounts));
        if (equipmentUiReady)
            for (Native950Packets.Packet packet : content.equipment.refresh) channel.write(packet);
    }

    private void rememberChangedSlots(Native950Containers.Snapshot before, Native950Containers.Snapshot after,
                                      java.util.Set<Integer> changed) {
        if (before == null) return;
        for (int slot = 0; slot < after.ids.length; slot++)
            if (before.ids[slot] != after.ids[slot]) changed.add(slot);
    }

    private static int changedSlots(Native950Containers.Snapshot before, Native950Containers.Snapshot after) {
        int count = 0;
        for (int slot = 0; slot < after.ids.length; slot++)
            if (before.ids[slot] != after.ids[slot] || before.amounts[slot] != after.amounts[slot]) count++;
        return count;
    }

    private static long total(Native950Containers.Snapshot state, int id) {
        long total = 0;
        for (int slot = 0; slot < state.ids.length; slot++) if (state.ids[slot] == id) total += state.amounts[slot];
        return total;
    }

    /**
     * Closes the bank through the 910 path ({@code Player.closeInterfaces} ->
     * {@code InterfaceManager.removeBankInterface}, which also compacts the bank)
     * and then sends the verified 947 close recipe.
     */
    private void closeBank() {
        cancelQuantity();
        bankEpoch++;
        boolean wasOpen = bankOpen;
        bankOpen = false; activeBank = null; activeNpcBank = false;
        if (router.bankInterfaceOpen()) {
            try {
                player.closeInterfaces();
            } catch (RuntimeException failure) {
                System.out.println("[Ataraxia950] Player.closeInterfaces failed for native player " + player.getIndex() + ": " + failure);
                failure.printStackTrace(System.out);
                if (player.getInterfaceManager() != null) player.getInterfaceManager().unregisterNativeOpen(Native950ActionRouter.BANK_INTERFACE);
            }
        }
        if (wasOpen) {
            for (Native950Packets.Packet packet : content.bank.close) channel.write(packet);
            System.out.println("[Ataraxia950] Bank closed for player " + player.getIndex());
        }
    }

    private boolean validBank(WorldObject object) {
        if (object == null || object.getPlane() != player.getPlane() || !router.whitelistedObject(object.getId())) return false;
        if (Math.abs(object.getX() - player.getX()) > 48 || Math.abs(object.getY() - player.getY()) > 48) return false;
        String[] options = object.getDefinitions().options;
        return options != null && options.length > 1 && "Use".equals(options[1])
                && findObject(object.getId(), object.getX(), object.getY()) == object;
    }

    private WorldObject findObject(int id, int x, int y) {
        int region = new WorldTile(x, y, player.getPlane()).getRegionId();
        if (!player.getMapRegionsIds().contains(region)) return null;
        // Resolve effective slots so a depleted tree cannot be clicked through its stump,
        // and player-made fires are reachable even though they are absent from the cache map.
        com.rs.game.Region map = World.getRegions().get(region);
        if (map == null || map.getObjects() == null) return null;
        for (int slot = 0; slot < 4; slot++) {
            WorldObject object = map.getObjectWithSlot(player.getPlane(), x & 63, y & 63, slot);
            if (object != null && (object.getId() == id || Native950Farming.matches(object,id,player))) return object;
        }
        return null;
    }

    private boolean canReachNow(WorldObject object) {
        return RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(), player.getPlane(),
                player.getSize(), new ObjectStrategy(object), false) == 0;
    }
    private void reject(String reason) {
        rejectedActions++;
        System.out.println("[Ataraxia950] Interaction rejected: " + reason);
        channel.write(Native950Packets.gameMessage(0, reason + "."));
    }

    State snapshot() { return new State(bankOpen, transactions, rejectedActions, containers.inventorySnapshot(),
            containers.bankSnapshot(), containers.equipmentSnapshot(), containers.equipmentKitClaimed(),
            router.dispatches(), router.rejections(), router.handlerFailures(), router.unmatchedPairs(),
            coinPouchRedirects, router.report(), unhandledActionTotal, unhandledActionReport(),
            commandsRun, commandsRefused, commandFailures); }
    public static final class State {
        public final boolean bankOpen;
        public final long transactions, rejectedActions;
        public final Native950Containers.Snapshot inventory, bank, equipment;
        public final boolean equipmentKitClaimed;
        /** Router counters: 910 dispatches, router rejections, handler exceptions, unmatched (interface, component) pairs. */
        public final long routerDispatches, routerRejections, handlerFailures, unmatchedPairs, coinPouchRedirects;
        public final String routerReport;
        /** Decoded actions that reached no handler: the total, and a by-type breakdown. */
        public final long unhandledActions;
        public final String unhandledActionReport;
        /** Typed 910 commands: attempted, refused by their own rights gates, and thrown out of. */
        public final long commandsRun, commandsRefused, commandFailures;
        State(boolean bankOpen, long transactions, long rejectedActions, Native950Containers.Snapshot inventory,
              Native950Containers.Snapshot bank, Native950Containers.Snapshot equipment, boolean equipmentKitClaimed,
              long routerDispatches, long routerRejections, long handlerFailures, long unmatchedPairs,
              long coinPouchRedirects, String routerReport, long unhandledActions, String unhandledActionReport,
              long commandsRun, long commandsRefused, long commandFailures) {
            this.bankOpen = bankOpen; this.transactions = transactions; this.rejectedActions = rejectedActions;
            this.inventory = inventory; this.bank = bank; this.equipment = equipment;
            this.equipmentKitClaimed = equipmentKitClaimed;
            this.routerDispatches = routerDispatches; this.routerRejections = routerRejections;
            this.handlerFailures = handlerFailures; this.unmatchedPairs = unmatchedPairs;
            this.coinPouchRedirects = coinPouchRedirects; this.routerReport = routerReport;
            this.unhandledActions = unhandledActions; this.unhandledActionReport = unhandledActionReport;
            this.commandsRun = commandsRun; this.commandsRefused = commandsRefused;
            this.commandFailures = commandFailures;
        }
    }
}
