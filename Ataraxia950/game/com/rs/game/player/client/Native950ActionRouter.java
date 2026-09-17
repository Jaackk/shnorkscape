package com.rs.game.player.client;

import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.client.ui.Native950Bindings;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.content.homearea.HomeAreaHandler;
import com.rs.network.packet.PacketRepository;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.network.packet.impl.NPCHandler;
import com.rs.network.packet.impl.ObjectHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * World-thread router from decoded native 947 actions into the 910 handlers.
 *
 * <p>Before P5 the native slice was a hand-built bypass: {@code Native950Interactions}
 * mutated its own view of the containers and wrote packets directly. This class
 * makes the 910 code the authority again. For every action it
 * <ul>
 * <li>applies the same gates the legacy stream handlers apply (active, dead,
 *     locked, map region loaded, NPC visibility) before anything runs;</li>
 * <li>keeps a per-interaction whitelist -- object 79036 (Lumbridge bank chest),
 *     NPC 494 (banker), interfaces 517 (bank), 1473 (backpack), 1462 (worn
 *     equipment) -- because only those ids have verified 947 bindings; anything
 *     else is a counted rejection, never a guess;</li>
 * <li>translates the (947 interface, 947 component) pair of a click into the
 *     (910 interface, 910 component) pair {@link ButtonHandler} branches on. The
 *     947 side of every pair is taken from the verified binding table when one
 *     is loaded (names {@code backpack.items}, {@code bank.items}, ...), the 910
 *     side is the constant the handler hard-codes (backpack 1473:7, bank
 *     items 517:184, bank inventory 517:14, deposit-all 517:37, equipment
 *     1462:31); unmatched pairs are counted with the first offenders kept;</li>
 * <li>maps the native option number to the {@code ACTION_BUTTONn_PACKET} id the
 *     handler expects: backpack and equipment options 1..7 map one-to-one and
 *     the native examine (10) is the 910 examine packet (8); the bank uses the
 *     verified option-amount tables instead, because native bank option 1 is
 *     "the selected quantity" (pinned to 1 by the open recipe) whereas 910
 *     packet 1 means "the default amount", and the 910 packet for an explicit
 *     amount is unambiguous (1 -> 2, 5 -> 3, 10 -> 4, all -> 6);</li>
 * <li>converts the client's post-prediction item claims to the real item id
 *     BEFORE the handler runs (see {@link #transferItem} and
 *     {@link #withdrawalItem}); the handler's own checks are not relaxed;</li>
 * <li>calls the same {@code ControlerManager} hooks the legacy handlers call
 *     at the same points (through the handlers themselves, or explicitly before
 *     starting the native banker's shared dialogue);</li>
 * <li>installs the verified binding table's allow-list resolver into
 *     {@link Native950IdMap} once, so every id a 910 emitter passes to the
 *     facade is either declared in the table or a counted drop.</li>
 * </ul>
 *
 * <p>The router never writes packets and never mutates a container itself; the
 * adapter ({@code Native950Interactions}) owns approach routing, the verified
 * open/close recipes and the container refreshes that follow a 910 mutation.
 * The 910 handlers are reached through {@link Handlers} so unit tests can run
 * the translation, pre-filter and counting logic without the cache.
 */
public final class Native950ActionRouter {

    /** Object whitelist for M2b: the cache-verified Lumbridge bank chest. */
    public static final int BANK_CHEST_ID = 79036;
    /** NPC whitelist for M2b: the verified Lumbridge banker definition. */
    public static final int BANKER_ID = 494;
    /** Interface whitelist for M2b. */
    public static final int BANK_INTERFACE = 517, BACKPACK_INTERFACE = 1473, EQUIPMENT_INTERFACE = 1462,
            MINIMAP_INTERFACE = 1465;
    /** 947 component ids used when no binding table is loaded (the verified Kotlin literals). */
    static final int BACKPACK_ITEMS_COMPONENT = 5, EQUIPMENT_ITEMS_COMPONENT = 31, RUN_ORB_COMPONENT = 15;
    /** 910 components the handlers branch on (ButtonHandler.java backpack/517/1462 branches). */
    public static final int LEGACY_BACKPACK_COMPONENT = 7, LEGACY_BANK_ITEMS = 184, LEGACY_BANK_INVENTORY = 14,
            LEGACY_BANK_DEPOSIT_ALL = 37, LEGACY_EQUIPMENT_ITEMS = 31, LEGACY_RUN_ORB_COMPONENT = 11;
    /** Native bank script sentinel for any exhausted ordinary actor in the mounted bank. */
    public static final int EMPTY_BANK_ACTOR = 48447;
    /** Native examine option (interface opcode table index 9). */
    public static final int NATIVE_EXAMINE_OPTION = 10;
    /** Native NPC examine option (NPC opcode table index 5). */
    public static final int NATIVE_NPC_EXAMINE_OPTION = 6;
    /**
     * {@link #legacyNpcOption(int)} for a native option the 910 {@code NPCHandler} has no
     * branch for. Zero is never a valid decoded option, and {@code NPCHandler.EXAMINE_OPTION}
     * is -1, so it cannot collide with either.
     */
    public static final int UNSUPPORTED_NPC_OPTION = 0;

    /** Which 910 branch a translated component addresses. */
    public enum Target { BACKPACK_ITEMS, BANK_ITEMS, BANK_INVENTORY, BANK_DEPOSIT_ALL, BANK_CLOSE, EQUIPMENT_ITEMS, RUN_ORB }

    /** One verified (947 component) -> (910 component) translation. */
    public static final class Binding {
        public final Target target;
        public final String name;
        public final int nativeHash, legacyInterface, legacyComponent;
        Binding(Target target, String name, int nativeHash, int legacyInterface, int legacyComponent) {
            this.target = target; this.name = name; this.nativeHash = nativeHash;
            this.legacyInterface = legacyInterface; this.legacyComponent = legacyComponent;
        }
        public int nativeInterface() { return nativeHash >>> 16; }
        public int nativeComponent() { return nativeHash & 0xffff; }
        @Override public String toString() {
            return name + " " + Native950Bindings.hashText(nativeHash) + " -> " + legacyInterface + ":" + legacyComponent;
        }
    }

    /** The 910 entry points; replaced by fakes in unit tests. */
    public interface Handlers {
        void object(Player player, WorldObject object, int option, boolean forceRun);
        void npc(Player player, NPC npc, int option, boolean forceRun);
        void button(Player player, int interfaceId, int componentId, int slot, int slotId2, int packetId) throws Exception;
    }

    /** The real decoded overloads added to the 910 handlers in P5. */
    public static final Handlers LEGACY = new Handlers() {
        @Override public void object(Player player, WorldObject object, int option, boolean forceRun) {
            ObjectHandler.dispatch(player, object, option, forceRun);
        }
        @Override public void npc(Player player, NPC npc, int option, boolean forceRun) {
            NPCHandler.dispatch(player, npc, option, forceRun);
        }
        @Override public void button(Player player, int interfaceId, int componentId, int slot, int slotId2, int packetId) throws Exception {
            ButtonHandler.handleButtons(player, interfaceId, componentId, slot, slotId2, packetId);
        }
    };

    /** Result of one routed action; {@code reason} is the counted rejection text when not accepted. */
    public static final class Outcome {
        public final boolean accepted;
        public final String reason;
        public final int packetId;
        private Outcome(boolean accepted, String reason, int packetId) { this.accepted = accepted; this.reason = reason; this.packetId = packetId; }
        static Outcome accepted(int packetId) { return new Outcome(true, null, packetId); }
        static Outcome rejected(String reason) { return new Outcome(false, reason, -1); }
        @Override public String toString() { return accepted ? "accepted(packet " + packetId + ")" : "rejected(" + reason + ")"; }
    }

    // ---------------------------------------------------------------- binding-table installation

    private static volatile Native950Bindings installedBindings;

    /**
     * Installs the allow-list resolver of the verified table into
     * {@link Native950IdMap} and hands the table to {@link InterfaceManager}
     * (full-hash slot lookups). Idempotent per table instance; a null table (legacy
     * cache, or a probe without one) leaves the identity resolver in place.
     */
    public static synchronized void installBindings(Native950Bindings bindings) {
        if (bindings == null || installedBindings == bindings) return;
        Native950IdMap.install(new IdMapAdapter(bindings.allowListResolver()));
        InterfaceManager.setNative950Bindings(bindings);
        installedBindings = bindings;
        System.out.println("[Ataraxia950] Installed the allow-list id resolver from the 950 binding table");
    }

    /** The table currently installed into the id map, or null. */
    public static Native950Bindings installedBindings() { return installedBindings; }

    /** The installed resolver's report, or a note that identity is still in force. */
    public static String bindingsReport() {
        Native950Bindings bindings = installedBindings;
        return bindings == null ? "947 id resolver: none installed (identity)" : bindings.allowListResolver().report();
    }

    /**
     * Bridges the table resolver into the facade's id map. Every kind returns -1
     * on rejection, which every verified writer refuses (a counted drop), except
     * containers: the facade masks container keys to 16 bits after resolution,
     * so -1 would become a live key 65535; throwing inside the writer body is
     * caught by the facade and counted as a drop instead. Varcs have no verified
     * binding at all (the facade keeps them STRICT), so they are rejected too.
     */
    static final class IdMapAdapter implements Native950IdMap.Resolver {
        final Native950Bindings.Resolver resolver;
        final AtomicLong varcRejections = new AtomicLong();
        IdMapAdapter(Native950Bindings.Resolver resolver) { this.resolver = Objects.requireNonNull(resolver, "resolver"); }
        @Override public int interfaceId(int interfaceId) { return resolver.interfaceId(interfaceId); }
        @Override public int componentId(int interfaceId, int componentId) { return resolver.componentId(interfaceId, componentId); }
        @Override public int varp(int id) { return resolver.varp(id); }
        @Override public int varbit(int id) { return resolver.varbit(id); }
        @Override public int varc(int id) { varcRejections.incrementAndGet(); return -1; }
        @Override public int script(int id) { return resolver.script(id); }
        @Override public int container(int id) {
            int resolved = resolver.container(id);
            if (resolved < 0) throw new IllegalArgumentException("container " + id + " has no 947 binding");
            return resolved;
        }
    }

    // ---------------------------------------------------------------- option translation

    /**
     * Native option n -> 910 packet id for the backpack and equipment layers:
     * 1..7 one-to-one (the 947 actor menus carry the item's five cache options
     * plus the interface operations exactly as 910 did), 10 (native examine) ->
     * packet 8 (910 examine). Everything else is -1.
     */
    public static int packetIdFor(int nativeOption) {
        switch (nativeOption) {
            case 1: return PacketRepository.ACTION_BUTTON1_PACKET;
            case 2: return PacketRepository.ACTION_BUTTON2_PACKET;
            case 3: return PacketRepository.ACTION_BUTTON3_PACKET;
            case 4: return PacketRepository.ACTION_BUTTON4_PACKET;
            case 5: return PacketRepository.ACTION_BUTTON5_PACKET;
            case 6: return PacketRepository.ACTION_BUTTON6_PACKET;
            case 7: return PacketRepository.ACTION_BUTTON7_PACKET;
            case NATIVE_EXAMINE_OPTION: return PacketRepository.ACTION_BUTTON8_PACKET;
            default: return -1;
        }
    }

    /**
     * Verified bank amount -> the 910 packet whose 517 branch performs exactly
     * that amount (ButtonHandler 517 components 14/184): 1 -> packet 2, 5 -> 3,
     * 10 -> 4, all -> 6. Packet 1 ("default amount") is deliberately never used
     * so a stale server-side default can never change a quantity the client
     * displayed. Unknown amounts are -1.
     */
    public static int bankPacketIdFor(int amount) {
        if (amount == 1) return PacketRepository.ACTION_BUTTON2_PACKET;
        if (amount == 5) return PacketRepository.ACTION_BUTTON3_PACKET;
        if (amount == 10) return PacketRepository.ACTION_BUTTON4_PACKET;
        if (amount == Integer.MAX_VALUE) return PacketRepository.ACTION_BUTTON6_PACKET;
        return -1;
    }

    // ---------------------------------------------------------------- actor prediction pre-filters

    /**
     * Native onOp scripts run before the button sender reads its actor item, so
     * exhausting a stack clears the clicked backpack actor to -1 (bank deposit
     * chain 8911/14367). Admit that sentinel only when the requested quantity
     * would exhaust the authoritative clicked slot and the slot did not already
     * change this tick; then return the real id for the 910 handler. Any other
     * claim is returned unchanged so the handler's exact-id check decides.
     */
    public static int transferItem(int claimedItemId, int slot, Native950Containers.Snapshot state, int quantity,
                                   int emptyActor, Set<Integer> changedSlots, Native950ItemCatalog catalog) {
        if (claimedItemId != emptyActor || slot < 0 || slot >= state.ids.length
                || state.ids[slot] < 0 || catalog.get(state.ids[slot]) == null
                || quantity < state.amounts[slot] || changedSlots.contains(slot)) return claimedItemId;
        return state.ids[slot];
    }

    /**
     * In the supported950 bank, script14362 ->13796 ->13798 clears an
     * exhausted clicked actor to48447 before IF_BUTTON reads its held reference.
     * Actual950 captures show this sentinel for non-final as well as final slots;
     * layout compaction is not a license to accept a different bank item ID.
     * Partial/capacity-limited withdrawals retain the clicked actor's own ID.
     * The refreshed authoritative bank may compact afterwards, so slots changed
     * this tick remain ineligible and neither pre-action nor next-item guesses pass.
     *
     * @param movable capacity-limited amount the original bank can actually move
     * @return the authoritative selected item, or -1 for a stale/invalid claim
     */
    public static int withdrawalItem(int claimedItemId, int slot, Native950Containers.Snapshot bank, int movable,
                                     Set<Integer> changedBankSlots, Native950ItemCatalog catalog) {
        if (slot < 0 || slot >= bank.ids.length || bank.ids[slot] < 0
                || catalog.get(bank.ids[slot]) == null || changedBankSlots.contains(slot)
                || movable < 0 || movable > bank.amounts[slot]) return -1;
        int currentId = bank.ids[slot];
        int expectedActor = movable > 0 && movable == bank.amounts[slot] ? EMPTY_BANK_ACTOR : currentId;
        return claimedItemId == expectedActor ? currentId : -1;
    }

    /** Backpack onOp 1620 and equipment script 8471 keep the original actor id: exact match only. */
    public static boolean exactClaim(int claimedItemId, int slot, Native950Containers.Snapshot state) {
        return slot >= 0 && slot < state.ids.length && claimedItemId >= 0 && state.ids[slot] == claimedItemId;
    }

    /** True when both slots are distinct backpack slots; a malformed drag is rejected before the claim check. */
    public static boolean dragSlotsValid(Native950Containers.Snapshot inventory, int from, int to) {
        return from >= 0 && from < inventory.ids.length && to >= 0 && to < inventory.ids.length && from != to;
    }

    /**
     * Cache script 1616 -> 3902 swaps the two backpack actors before the native
     * drag sender reads them, so the pair the client claims is the POST-swap one:
     * the source claim is the item that is authoritatively still in {@code to} and
     * the target claim the item still in {@code from}. Accept exactly that reversed
     * pair; accepting both orders as well would let a repeated packet silently undo
     * the first authoritative swap.
     */
    public static boolean reversedDragPair(Native950Containers.Snapshot inventory, int from, int to,
                                           int claimedSourceItemId, int claimedTargetItemId) {
        if (!dragSlotsValid(inventory, from, to)) return false;
        return inventory.ids[from] == claimedTargetItemId && inventory.ids[to] == claimedSourceItemId;
    }

    // ---------------------------------------------------------------- instance

    private final Player player;
    private final Native950Content content;
    private final Native950Bindings bindings;
    private final Handlers handlers;
    private final Map<Integer, Binding> bindingsByHash = new LinkedHashMap<Integer, Binding>();
    private final ConcurrentHashMap<Integer, AtomicLong> unmatchedPairs = new ConcurrentHashMap<Integer, AtomicLong>();
    private final ConcurrentHashMap<String, AtomicLong> rejections = new ConcurrentHashMap<String, AtomicLong>();
    private final ConcurrentHashMap<String, AtomicLong> dispatches = new ConcurrentHashMap<String, AtomicLong>();
    private final ConcurrentHashMap<String, AtomicLong> handlerFailures = new ConcurrentHashMap<String, AtomicLong>();
    private final List<String> failureDetails = Collections.synchronizedList(new ArrayList<String>());

    public Native950ActionRouter(Player player, Native950Content content, Native950Bindings bindings) {
        this(player, content, bindings, LEGACY);
    }

    public Native950ActionRouter(Player player, Native950Content content, Native950Bindings bindings, Handlers handlers) {
        this.player = Objects.requireNonNull(player, "player");
        this.content = Objects.requireNonNull(content, "content");
        this.bindings = bindings;
        this.handlers = Objects.requireNonNull(handlers, "handlers");
        if (!player.isNative950()) throw new IllegalArgumentException("Native character required");
        installBindings(bindings);
        buildBindings();
    }

    /**
     * The whitelisted translations. When the binding table is loaded the 947 side
     * comes from it (and must agree with the content the frontend handed over);
     * otherwise the verified content constants are used.
     */
    private void buildBindings() {
        Native950Content.BankUi bank = content.bank;
        int backpackItems = hash(BACKPACK_INTERFACE, BACKPACK_ITEMS_COMPONENT);
        int bankItems = hash(bank.interfaceId, bank.itemComponent);
        int bankInventory = hash(bank.interfaceId, bank.inventoryComponent);
        int bankDepositAll = hash(bank.interfaceId, bank.depositAllComponent);
        int bankClose = hash(bank.interfaceId, bank.closeComponent);
        int equipmentItems = content.equipment == null ? hash(EQUIPMENT_INTERFACE, EQUIPMENT_ITEMS_COMPONENT)
                : hash(content.equipment.interfaceId, content.equipment.itemComponent);
        if (bindings != null) {
            backpackItems = verified("backpack", "items", backpackItems);
            bankItems = verified("bank", "items", bankItems);
            bankInventory = verified("bank", "inventory", bankInventory);
            bankDepositAll = verified("bank", "deposit_all", bankDepositAll);
            bankClose = verified("bank", "close", bankClose);
            equipmentItems = verified("worn_equipment", "items", equipmentItems);
        }
        add(new Binding(Target.BACKPACK_ITEMS, "backpack.items", backpackItems, BACKPACK_INTERFACE, LEGACY_BACKPACK_COMPONENT));
        add(new Binding(Target.BANK_ITEMS, "bank.items", bankItems, BANK_INTERFACE, LEGACY_BANK_ITEMS));
        add(new Binding(Target.BANK_INVENTORY, "bank.inventory", bankInventory, BANK_INTERFACE, LEGACY_BANK_INVENTORY));
        add(new Binding(Target.BANK_DEPOSIT_ALL, "bank.deposit_all", bankDepositAll, BANK_INTERFACE, LEGACY_BANK_DEPOSIT_ALL));
        // The 910 client closes the bank with its CLOSE_INTERFACE packet, not a button; the
        // adapter routes this component to Player.closeInterfaces() (the same 910 path).
        add(new Binding(Target.BANK_CLOSE, "bank.close", bankClose, BANK_INTERFACE, -1));
        add(new Binding(Target.EQUIPMENT_ITEMS, "worn_equipment.items", equipmentItems, EQUIPMENT_INTERFACE, LEGACY_EQUIPMENT_ITEMS));
        int runOrb = hash(MINIMAP_INTERFACE, RUN_ORB_COMPONENT);
        if (bindings != null) runOrb = verified("minimap", "run_orb", runOrb);
        // ORBS_AND_VARS.md: 950 cache component 1465:15 is Toggle Run. The same 910
        // component number controls XP settings, so the translation is essential.
        add(new Binding(Target.RUN_ORB, "minimap.run_orb", runOrb, MINIMAP_INTERFACE, LEGACY_RUN_ORB_COMPONENT));
    }

    private int verified(String interfaceName, String componentName, int fromContent) {
        int fromTable = bindings.componentHash(interfaceName, componentName);
        if (fromTable != fromContent)
            throw new IllegalStateException("947 binding " + interfaceName + "." + componentName + " is "
                    + Native950Bindings.hashText(fromTable) + " in the table but " + Native950Bindings.hashText(fromContent)
                    + " in the handed-over content; refusing to route with disagreeing bindings");
        return fromTable;
    }

    private void add(Binding binding) {
        if (bindingsByHash.put(binding.nativeHash, binding) != null)
            throw new IllegalStateException("Duplicate 947 component binding " + binding);
    }

    static int hash(int interfaceId, int componentId) { return (interfaceId << 16) | (componentId & 0xffff); }

    /** The whitelisted translations in declaration order. */
    public List<Binding> bindings() { return new ArrayList<Binding>(bindingsByHash.values()); }

    /**
     * Looks a native component hash up; an unknown pair is counted (first
     * offenders kept in {@link #report()}) and returns null. An interface outside
     * the whitelist is a counted rejection even when a table declares it.
     */
    public Binding binding(int nativeHash) {
        Binding binding = bindingsByHash.get(nativeHash);
        if (binding == null) bump(unmatchedPairs, nativeHash);
        return binding;
    }

    public boolean whitelistedInterface(int interfaceId) {
        return interfaceId == BANK_INTERFACE || interfaceId == BACKPACK_INTERFACE || interfaceId == EQUIPMENT_INTERFACE
                || interfaceId == MINIMAP_INTERFACE;
    }

    public boolean whitelistedObject(int objectId) { return objectId == BANK_CHEST_ID; }

    public boolean whitelistedNpc(int definitionId) { return definitionId == BANKER_ID; }

    /**
     * The packet id for a bank click: the verified option-amount table decides
     * the amount, {@link #bankPacketIdFor} the packet. Examine is packet 8 and
     * deposit-all accepts only its primary option.
     */
    public int bankPacketId(Binding binding, int nativeOption) {
        if (nativeOption == NATIVE_EXAMINE_OPTION && (binding.target == Target.BANK_ITEMS || binding.target == Target.BANK_INVENTORY))
            return PacketRepository.ACTION_BUTTON8_PACKET;
        if (binding.target == Target.BANK_INVENTORY || binding.target == Target.BANK_ITEMS) {
            if (nativeOption == 1) return PacketRepository.ACTION_BUTTON1_PACKET;
            if (nativeOption == 5) return PacketRepository.ACTION_BUTTON5_PACKET;
        }
        switch (binding.target) {
            case BANK_INVENTORY: return bankPacketIdFor(content.bank.depositAmount(nativeOption));
            case BANK_ITEMS: return bankPacketIdFor(content.bank.withdrawAmount(nativeOption));
            case BANK_DEPOSIT_ALL: return nativeOption == 1 ? PacketRepository.ACTION_BUTTON1_PACKET : -1;
            default: return -1;
        }
    }

    /** The verified bank amount a native option requests on a bank component, 0 when the option is disabled. */
    public int bankAmount(Binding binding, int nativeOption) {
        if (binding.target == Target.BANK_INVENTORY || binding.target == Target.BANK_ITEMS) {
            if (nativeOption == 1) return player.getBank().getDefaultInteractionAmount();
            if (nativeOption == 5) return player.getBank().getLastX();
        }
        switch (binding.target) {
            case BANK_INVENTORY: return content.bank.depositAmount(nativeOption);
            case BANK_ITEMS: return content.bank.withdrawAmount(nativeOption);
            default: return 0;
        }
    }

    // ---------------------------------------------------------------- 910 gates

    /** The adapter-level gate every legacy handler starts with (Player.processLogicPackets order). */
    public boolean playerMayAct() {
        return player.isActive() && !player.hasFinished() && !player.isDead();
    }

    /**
     * The NPC gates {@code NPCHandler.handleOptionN} applies before anything else,
     * then {@code stopAll(false)} exactly as the handler does. The home-area check
     * needs the NPC name, which a native 947 NPC cannot provide before P6, so it
     * is skipped for native NPCs (the banker is not in the home area anyway).
     */
    public boolean npcGates(NPC npc) {
        if (player.isLocked()) return false;
        if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished()
                || !player.getMapRegionsIds().contains(npc.getRegionId())) return false;
        player.stopAll(false);
        if (!npc.isNative950() && !HomeAreaHandler.playerCanInteractWithNpc(player, npc)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, npc.getName(), HomeAreaHandler.Type.NPC);
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------------- dispatch

    /** The verified run orb's primary option, through the ordinary 910 button gates. */
    public Outcome runOrb(Binding binding, int nativeOption, int slot, int itemId) {
        if (binding == null || binding.target != Target.RUN_ORB)
            return reject("That component is not the run orb");
        if (!playerMayAct() || player.isLocked()) return reject("You cannot do that right now");
        // Rest starts an animation/action chain that has not been validated yet.
        if (nativeOption != 1) return reject("That run orb option is not available yet");
        return button(binding, PacketRepository.ACTION_BUTTON1_PACKET, slot, itemId);
    }

    /**
     * Runs a whitelisted object option through {@code ObjectHandler.dispatch}. The
     * 910 handler installs a {@link RouteEvent} for almost every option; since the
     * adapter already walked the player into reach, the event is processed at once
     * so its runnable (controller hook, face, {@code Bank.openBank()}) runs on this
     * tick. When the player is somehow not in reach the event stays installed and
     * {@link #processRouteEvent()} finishes it on a later tick.
     */
    public Outcome object(WorldObject object, int option, boolean forceRun) {
        if (object == null) return reject("The requested object is not present");
        if (!whitelistedObject(object.getId())) return reject("Object " + object.getId() + " is outside the M2b whitelist");
        if (option < 1 || option > 5) return reject("Unsupported object option " + option);
        try {
            handlers.object(player, object, option, forceRun);
            processRouteEvent();
        } catch (RuntimeException failure) {
            return handlerFailed("object", object.getId() + "/" + option, failure);
        }
        bump(dispatches, "object");
        return Outcome.accepted(-1);
    }

    /**
     * Processes the player's pending {@link RouteEvent} once; returns true when it
     * completed (its runnable ran or it gave up). The event nulls itself through
     * {@code stopAll()} when it runs, so a completed event is never run twice even
     * if the native tick also processes it.
     */
    public boolean processRouteEvent() {
        RouteEvent event = player.getRouteEvent();
        if (event == null) return false;
        if (!event.processEvent(player)) return false;
        if (player.getRouteEvent() == event) player.setRouteEvent(null);
        return true;
    }

    /**
     * Native banker "Bank": the 910 NPC path for a banker starts the BankList /
     * Banker dialogues (M7), so after the NPC gates the router performs what the
     * dialogue's Bank option does -- the option-2 controller hook (910 Bank is
     * option 2), face each other, {@code Bank.openBank()} -- and reports whether
     * the 910 InterfaceManager now holds interface 517.
     */
    public Outcome npcBank(NPC npc) {
        if (!npcGates(npc)) return reject("That NPC is not available");
        if (!whitelistedNpc(npc.getId())) return reject("NPC " + npc.getId() + " is outside the M2b whitelist");
        try {
            if (!player.getControlerManager().processNPCClick2(npc)) return reject("The controller refused the banker");
            npc.faceEntity(player);
            player.faceEntity(npc);
            player.getBank().openBank();
        } catch (RuntimeException failure) {
            return handlerFailed("npcBank", String.valueOf(npc.getId()), failure);
        }
        bump(dispatches, "npcBank");
        return bankInterfaceOpen() ? Outcome.accepted(-1) : reject("The 910 bank did not open (see the facade drop log)");
    }

    /**
     * Native banker examine: the 910 examine text needs {@code NPC.getDefinitions()},
     * which a native NPC cannot answer before P6, so the verified content name is
     * sent through the same facade call the 910 handler uses. The examine
     * controller hook runs first, exactly as in {@code NPCHandler.handleExamine}.
     */
    public Outcome npcExamine(NPC npc, String name) {
        if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished()
                || !player.getMapRegionsIds().contains(npc.getRegionId())) return reject("That NPC is not available");
        if (!whitelistedNpc(npc.getId())) return reject("NPC " + npc.getId() + " is outside the M2b whitelist");
        try {
            if (!player.getControlerManager().processNPCExamine(npc)) return reject("The controller refused the examine");
            if (npc.isNative950()) player.getPackets().sendNPCMessage(0, 15263739, npc, name + ".");
            else handlers.npc(player, npc, NPCHandler.EXAMINE_OPTION, false);
        } catch (RuntimeException failure) {
            return handlerFailed("npcExamine", String.valueOf(npc.getId()), failure);
        }
        bump(dispatches, "npcExamine");
        return Outcome.accepted(-1);
    }

    /** Native banker Talk-to reuses the original dialogue manager after the NPC gates. */
    public Outcome npcTalk(NPC npc) {
        if (!npcGates(npc)) return reject("That NPC is not available");
        if (!whitelistedNpc(npc.getId())) return reject("NPC " + npc.getId() + " is outside the M2b whitelist");
        if (!player.getControlerManager().processNPCClick1(npc)) return reject("The controller refused the banker");
        npc.faceEntity(player);
        player.faceEntity(npc);
        try {
            player.getDialogueManager().startDialogue(new com.rs.game.player.dialogue.impl.Banker(), npc.getId());
        } catch (RuntimeException failure) {
            return handlerFailed("npcTalk", String.valueOf(npc.getId()), failure);
        }
        bump(dispatches, "npcTalk");
        return Outcome.accepted(-1);
    }

    /** The caller has consumed the current rendered response and validated its NPC. */
    public Outcome dialogue(int interfaceId, int componentId) {
        try {
            player.getDialogueManager().continueDialogue(interfaceId, componentId);
        } catch (RuntimeException failure) {
            return handlerFailed("dialogue", interfaceId + ":" + componentId, failure);
        }
        bump(dispatches, "dialogue");
        return Outcome.accepted(-1);
    }

    public void dialogueCloseFailed(RuntimeException failure) {
        handlerFailed("dialogueClose", "cleanup", failure);
    }

    /** An owned native quantity request has already revalidated slot, item and capacity. */
    public Outcome withdrawQuantity(int slot, int itemId, int amount) {
        if (!playerMayAct() || player.isLocked() || !bankInterfaceOpen() || amount <= 0)
            return reject("That bank quantity request is no longer active");
        int[] realSlot = player.getBank().getRealSlot(slot);
        Item claim = realSlot == null ? null : player.getBank().getItem(realSlot);
        if (claim == null || claim.getId() != itemId || claim.getAmount() < amount)
            return reject("The bank item changed before the quantity was applied");
        try {
            player.getBank().withdrawItem(slot, amount);
        } catch (RuntimeException failure) {
            return handlerFailed("withdrawQuantity", slot + "/" + itemId, failure);
        }
        bump(dispatches, "withdrawQuantity");
        return Outcome.accepted(-1);
    }

    /** Owned Deposit-X uses the shared Bank path after the adapter verifies its snapshot. */
    public Outcome depositQuantity(int slot, int itemId, int amount) {
        if (!playerMayAct() || player.isLocked() || !bankInterfaceOpen() || amount <= 0)
            return reject("That bank quantity request is no longer active");
        Item claim = slot < 0 || slot >= 28 ? null : player.getInventory().getItem(slot);
        if (claim == null || claim.getId() != itemId)
            return reject("The inventory item changed before the quantity was applied");
        try {
            player.getBank().depositItem(slot, amount, true);
        } catch (RuntimeException failure) {
            return handlerFailed("depositQuantity", slot + "/" + itemId, failure);
        }
        bump(dispatches, "depositQuantity");
        return Outcome.accepted(-1);
    }

    /** Native banker Collect (development kit): the option-3 hook runs; the adapter grants the kit. */
    public Outcome npcCollect(NPC npc) {
        if (!npcGates(npc)) return reject("That NPC is not available");
        if (!whitelistedNpc(npc.getId())) return reject("NPC " + npc.getId() + " is outside the M2b whitelist");
        if (!player.getControlerManager().processNPCClick3(npc)) return reject("The controller refused the banker");
        npc.faceEntity(player);
        player.faceEntity(npc);
        bump(dispatches, "npcCollect");
        return Outcome.accepted(-1);
    }

    /**
     * M4: one click on an ordinary world NPC, dispatched into the 910 decoded overload
     * {@code NPCHandler.dispatch}. The caller ({@code Native950Interactions}) has already
     * resolved the index through the list this viewer's client was actually sent, so this
     * method only maps the option and applies the gates before handing over.
     *
     * <p>The gates repeated here are exactly the ones every {@code NPCHandler.handleOptionN}
     * starts with - locked player, absent or uninteractable NPC, an NPC outside the regions
     * the viewer holds. They are applied as a pre-filter so a refusal is a counted rejection
     * with a reason instead of a handler that silently returns. {@code stopAll(false)} is NOT
     * repeated: the decoded overload performs it itself, and running it twice would drop the
     * player's state before the handler had looked at it.
     *
     * <p>After the dispatch the pending {@code RouteEvent} is processed once, exactly as
     * {@link #object(WorldObject, int, boolean)} does: the 910 handler installs the walk, and a
     * player already in reach then completes on this tick. One that is not stays installed and
     * is finished by {@code Native950Interactions.afterMovement}.
     *
     * <p>Shared name and menu checks use verified 947 metadata. Content branches can
     * still require unported definitions or presentation bindings; a handler exception
     * is counted and contained here rather than escaping into the world tick.
     */
    public Outcome npcOption(NPC npc, int nativeOption, boolean forceRun) {
        if (npc == null) return reject("That NPC is not present");
        int legacyOption = legacyNpcOption(nativeOption);
        if (legacyOption == UNSUPPORTED_NPC_OPTION)
            return reject("NPC option " + nativeOption + " has no 910 handler");
        if (player.isLocked()) return reject("You cannot do that right now");
        if (npc.isCantInteract() || npc.isDead() || npc.hasFinished()
                || !player.getMapRegionsIds().contains(npc.getRegionId()))
            return reject("That NPC is not available");
        try {
            handlers.npc(player, npc, legacyOption, forceRun);
            processRouteEvent();
        } catch (RuntimeException failure) {
            return handlerFailed("npcOption", npc.getIndex() + "/" + nativeOption, failure);
        }
        bump(dispatches, "npcOption:" + legacyOption);
        return Outcome.accepted(-1);
    }

    /**
     * The 947 NPC opcode table has six entries ({@code Native950Actions.NPC_OPCODES}), so a
     * click carries option 1..6 with 6 being examine. The 910 {@code NPCHandler} implements
     * options 1..4 and examine; native option 5 has no 910 branch at all, so it is refused
     * rather than guessed onto one of the others.
     *
     * @return the {@code NPCHandler.dispatch} option, or {@link #UNSUPPORTED_NPC_OPTION}
     */
    public static int legacyNpcOption(int nativeOption) {
        if (nativeOption >= 1 && nativeOption <= 4) return nativeOption;
        if (nativeOption == NATIVE_NPC_EXAMINE_OPTION) return NPCHandler.EXAMINE_OPTION;
        return UNSUPPORTED_NPC_OPTION;
    }

    /**
     * Runs one translated click through {@code ButtonHandler.handleButtons} with
     * the 910 pair, the mapped packet id and the REAL item id (the caller applied
     * the prediction pre-filter). Handler exceptions are counted and reported as
     * rejections; they never reach the world tick.
     */
    public Outcome button(Binding binding, int packetId, int slot, int realItemId) {
        if (binding == null) return reject("That interface component has no verified 947 binding");
        if (!whitelistedInterface(binding.nativeInterface())) return reject("Interface " + binding.nativeInterface() + " is outside the M2b whitelist");
        if (packetId < 0) return reject("Unsupported option on " + binding.name);
        if (binding.legacyComponent < 0) return reject(binding.name + " has no 910 button branch");
        try {
            handlers.button(player, binding.legacyInterface, binding.legacyComponent, slot, realItemId, packetId);
        } catch (Exception failure) {
            return handlerFailed("button", binding.name + "/" + packetId, failure);
        }
        bump(dispatches, "button:" + binding.name);
        return Outcome.accepted(packetId);
    }

    /** True when the 910 InterfaceManager holds the bank interface (the state {@code Bank.openBank()} leaves). */
    public boolean bankInterfaceOpen() {
        InterfaceManager manager = player.getInterfaceManager();
        return manager != null && manager.containsInterface(BANK_INTERFACE);
    }

    // ---------------------------------------------------------------- counters

    /** Counts a rejection under its reason and returns the outcome. */
    public Outcome reject(String reason) {
        bump(rejections, reason);
        return Outcome.rejected(reason);
    }

    private Outcome handlerFailed(String kind, String detail, Exception failure) {
        bump(handlerFailures, kind);
        String text = kind + " " + detail + ": " + failure;
        if (failureDetails.size() < 20) failureDetails.add(text);
        System.out.println("[Ataraxia950] 910 handler failed for native " + text);
        failure.printStackTrace(System.out);
        return reject("The 910 " + kind + " handler failed: " + failure.getClass().getSimpleName());
    }

    private static <K> void bump(ConcurrentHashMap<K, AtomicLong> map, K key) {
        AtomicLong counter = map.get(key);
        if (counter == null) {
            AtomicLong fresh = new AtomicLong();
            counter = map.putIfAbsent(key, fresh);
            if (counter == null) counter = fresh;
        }
        counter.incrementAndGet();
    }

    public long unmatchedPairs() { long total = 0; for (AtomicLong c : unmatchedPairs.values()) total += c.get(); return total; }
    public long unmatchedPairs(int nativeHash) { AtomicLong c = unmatchedPairs.get(nativeHash); return c == null ? 0 : c.get(); }
    public long rejections() { long total = 0; for (AtomicLong c : rejections.values()) total += c.get(); return total; }
    public long rejections(String reason) { AtomicLong c = rejections.get(reason); return c == null ? 0 : c.get(); }
    public long dispatches() { long total = 0; for (AtomicLong c : dispatches.values()) total += c.get(); return total; }
    public long dispatches(String kind) { AtomicLong c = dispatches.get(kind); return c == null ? 0 : c.get(); }
    public long handlerFailures() { long total = 0; for (AtomicLong c : handlerFailures.values()) total += c.get(); return total; }
    public List<String> failureDetails() { synchronized (failureDetails) { return new ArrayList<String>(failureDetails); } }

    /**
     * The decoded-dispatch counters the three 910 handlers increment. Both the
     * legacy stream entry points and this router pass through them, so in a native
     * smoke a non-zero count is direct evidence that the 910 handler body ran
     * instead of a bypass.
     */
    public static String legacyDispatchCounters() {
        return "910 decoded dispatches: ObjectHandler.dispatch=" + ObjectHandler.DISPATCHES.get()
                + " NPCHandler=" + NPCHandler.DISPATCHES.get()
                + " ButtonHandler.handleButtons=" + ButtonHandler.DISPATCHES.get();
    }

    /** Sorted, human-readable counters plus the installed resolver's report. */
    public String report() {
        StringBuilder out = new StringBuilder("947 action router: dispatched=" + dispatches() + " rejected=" + rejections()
                + " handlerFailures=" + handlerFailures() + " unmatchedPairs=" + unmatchedPairs());
        for (Map.Entry<String, AtomicLong> e : new TreeMap<String, AtomicLong>(dispatches).entrySet())
            out.append("\n  dispatched ").append(e.getKey()).append('=').append(e.getValue().get());
        for (Map.Entry<String, AtomicLong> e : new TreeMap<String, AtomicLong>(rejections).entrySet())
            out.append("\n  rejected ").append(e.getValue().get()).append("x ").append(e.getKey());
        int shown = 0;
        for (Map.Entry<Integer, AtomicLong> e : new TreeMap<Integer, AtomicLong>(unmatchedPairs).entrySet()) {
            if (shown++ >= 20) { out.append("\n  ..."); break; }
            out.append("\n  unmatched ").append(Native950Bindings.hashText(e.getKey())).append(" x").append(e.getValue().get());
        }
        for (String detail : failureDetails()) out.append("\n  failure ").append(detail);
        out.append("\n  ").append(bindingsReport());
        return out.toString();
    }
}
