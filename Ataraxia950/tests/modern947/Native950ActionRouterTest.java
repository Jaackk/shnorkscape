package modern947;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950ActionRouter;
import com.rs.game.player.client.Native950Containers;
import com.rs.game.player.client.Native950Content;
import com.rs.game.player.client.Native950ItemCatalog;
import com.rs.network.packet.PacketRepository;
import com.rs.network.packet.impl.NPCHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Cache-free unit cover for the P5 router: the native option -> 910 packet map,
 * the (947 interface, component) -> (910 interface, component) translation in both
 * directions, the three actor-prediction pre-filters, unmatched-pair counting and
 * the M2b whitelist.
 *
 * <p>Everything here runs without a cache: {@code Player.createNative950} hydrates
 * packet-free, {@code Native950Content} is plain data and the 910 handlers are
 * replaced by {@link Recorder} so no handler body (and therefore no cache
 * definition lookup) is reached. The one place the real handlers are exercised is
 * the real-cache smoke set.
 */
public class Native950ActionRouterTest {

    private static final int COINS = 995, LOGS = 1511, SHRIMPS = 315;
    private static final int BANK = Native950ActionRouter.BANK_INTERFACE;
    private static final int NATIVE_BANK_ITEMS = 201, NATIVE_BANK_INVENTORY = 15,
            NATIVE_BANK_CLOSE = 317, NATIVE_BANK_DEPOSIT_ALL = 39;

    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final Player player = Player.createNative950("router-test", new WorldTile(3222, 3222, 0), channel);
    private final Native950ItemCatalog catalog = new Native950ItemCatalog(Arrays.asList(
            new Native950ItemCatalog.Entry(COINS, "Coins", true, new String[] {"Add to pouch"}),
            new Native950ItemCatalog.Entry(LOGS, "Logs", false, new String[] {"Craft"}),
            new Native950ItemCatalog.Entry(SHRIMPS, "Shrimps", false, new String[] {"Eat"})));
    private final Native950Content content = content(catalog);
    private final Recorder handlers = new Recorder();
    private final Native950ActionRouter router = new Native950ActionRouter(player, content, null, handlers);
    private final Native950Containers containers = new Native950Containers(player, catalog);

    @After public void release() { channel.finishAndReleaseAll(); }

    private static Native950Content content(Native950ItemCatalog catalog) {
        int[] deposits = new int[11], withdrawals = new int[11];
        // Explicit options are 2 = one, 3 = five, 4 = ten, 7 = all; the router resolves 1/5 dynamically.
        deposits[1] = withdrawals[1] = 1;
        deposits[2] = withdrawals[2] = 1;
        deposits[3] = withdrawals[3] = 5;
        deposits[4] = withdrawals[4] = 10;
        deposits[7] = withdrawals[7] = Integer.MAX_VALUE;
        return new Native950Content(catalog, new Native950Content.BankUi(BANK, NATIVE_BANK_ITEMS,
                NATIVE_BANK_INVENTORY, NATIVE_BANK_CLOSE, NATIVE_BANK_DEPOSIT_ALL,
                deposits, withdrawals, Collections.emptyList(), Collections.emptyList()));
    }

    /** Records what the router hands the 910 handlers instead of running them. */
    private static final class Recorder implements Native950ActionRouter.Handlers {
        final List<String> calls = new ArrayList<String>();
        RuntimeException failWith;
        @Override public void object(Player player, WorldObject object, int option, boolean forceRun) {
            if (failWith != null) throw failWith;
            calls.add("object " + object.getId() + "/" + option + "/" + forceRun);
        }
        @Override public void npc(Player player, NPC npc, int option, boolean forceRun) {
            if (failWith != null) throw failWith;
            calls.add("npc " + option);
        }
        @Override public void button(Player player, int interfaceId, int componentId, int slot, int slotId2, int packetId) {
            if (failWith != null) throw failWith;
            calls.add("button " + interfaceId + ":" + componentId + " slot=" + slot + " item=" + slotId2 + " packet=" + packetId);
        }
    }

    private Native950ActionRouter.Binding binding(int nativeInterface, int nativeComponent) {
        return router.binding((nativeInterface << 16) | (nativeComponent & 0xffff));
    }

    @Test public void runOrbTargetsThe910RunButtonAndRefusesOtherOptions() {
        assertNull("950 component14 is decoration, not Run", binding(1465, 14));
        Native950ActionRouter.Binding orb = binding(1465, 15);
        assertEquals(Native950ActionRouter.Target.RUN_ORB, orb.target);
        assertEquals(1465, orb.legacyInterface);
        assertEquals(11, orb.legacyComponent);
        player.setActive(true);
        assertTrue(router.runOrb(orb, 1, -1, -1).accepted);
        assertEquals("button 1465:11 slot=-1 item=-1 packet=" + PacketRepository.ACTION_BUTTON1_PACKET,
                handlers.calls.get(0));
        for (int option : new int[] {0, 2, 3, 10})
            assertFalse(router.runOrb(orb, option, -1, -1).accepted);
        assertEquals(1, handlers.calls.size());
        assertFalse(router.runOrb(binding(BANK, NATIVE_BANK_ITEMS), 1, -1, -1).accepted);
    }

    @Test public void runOrbCannotBypassInactiveDeadOrLockedPlayerGates() {
        assertNull("950 component14 is decoration, not Run", binding(1465, 14));
        Native950ActionRouter.Binding orb = binding(1465, 15);
        player.setActive(false);
        assertFalse(router.runOrb(orb, 1, -1, -1).accepted);
        player.setActive(true);
        player.lock(10);
        assertFalse(router.runOrb(orb, 1, -1, -1).accepted);
        player.unlock();
        player.setHitpoints(0);
        assertFalse(router.runOrb(orb, 1, -1, -1).accepted);
        assertEquals(0, handlers.calls.size());
    }

    // ------------------------------------------------------------------ option -> packet id

    @Test public void backpackAndEquipmentOptionsMapOneToOneAndExamineMapsToPacketEight() {
        assertEquals(PacketRepository.ACTION_BUTTON1_PACKET, Native950ActionRouter.packetIdFor(1));
        assertEquals(PacketRepository.ACTION_BUTTON2_PACKET, Native950ActionRouter.packetIdFor(2));
        assertEquals(PacketRepository.ACTION_BUTTON3_PACKET, Native950ActionRouter.packetIdFor(3));
        assertEquals(PacketRepository.ACTION_BUTTON4_PACKET, Native950ActionRouter.packetIdFor(4));
        assertEquals(PacketRepository.ACTION_BUTTON5_PACKET, Native950ActionRouter.packetIdFor(5));
        assertEquals(PacketRepository.ACTION_BUTTON6_PACKET, Native950ActionRouter.packetIdFor(6));
        assertEquals(PacketRepository.ACTION_BUTTON7_PACKET, Native950ActionRouter.packetIdFor(7));
        assertEquals(PacketRepository.ACTION_BUTTON8_PACKET,
                Native950ActionRouter.packetIdFor(Native950ActionRouter.NATIVE_EXAMINE_OPTION));
        // Nothing else may be guessed into a packet id.
        for (int option : new int[] {-1, 0, 8, 9, 11, 255})
            assertEquals("option " + option, -1, Native950ActionRouter.packetIdFor(option));
    }

    @Test public void bankAmountsUseTheExplicitAmountPacketsAndNeverTheDefaultAmountPacket() {
        assertEquals(PacketRepository.ACTION_BUTTON2_PACKET, Native950ActionRouter.bankPacketIdFor(1));
        assertEquals(PacketRepository.ACTION_BUTTON3_PACKET, Native950ActionRouter.bankPacketIdFor(5));
        assertEquals(PacketRepository.ACTION_BUTTON4_PACKET, Native950ActionRouter.bankPacketIdFor(10));
        assertEquals(PacketRepository.ACTION_BUTTON6_PACKET, Native950ActionRouter.bankPacketIdFor(Integer.MAX_VALUE));
        for (int amount : new int[] {0, -1, 2, 11, 1000})
            assertEquals("amount " + amount, -1, Native950ActionRouter.bankPacketIdFor(amount));
    }

    @Test public void bankOptionsResolveThroughTheVerifiedAmountTable() {
        Native950ActionRouter.Binding items = binding(BANK, NATIVE_BANK_ITEMS);
        Native950ActionRouter.Binding inventory = binding(BANK, NATIVE_BANK_INVENTORY);
        assertEquals(1, router.bankAmount(items, 1));
        assertEquals(5, router.bankAmount(items, 3));
        assertEquals(10, router.bankAmount(items, 4));
        assertEquals(Integer.MAX_VALUE, router.bankAmount(items, 7));
        assertEquals(1, router.bankAmount(items, 5));
        assertEquals(PacketRepository.ACTION_BUTTON1_PACKET, router.bankPacketId(inventory, 1));
        assertEquals(PacketRepository.ACTION_BUTTON3_PACKET, router.bankPacketId(inventory, 3));
        assertEquals(PacketRepository.ACTION_BUTTON6_PACKET, router.bankPacketId(items, 7));
        assertEquals(PacketRepository.ACTION_BUTTON5_PACKET, router.bankPacketId(items, 5));
        // Examine is packet 8 on both item layers and unavailable on deposit-all.
        assertEquals(PacketRepository.ACTION_BUTTON8_PACKET,
                router.bankPacketId(items, Native950ActionRouter.NATIVE_EXAMINE_OPTION));
        assertEquals(PacketRepository.ACTION_BUTTON8_PACKET,
                router.bankPacketId(inventory, Native950ActionRouter.NATIVE_EXAMINE_OPTION));
        Native950ActionRouter.Binding depositAll = binding(BANK, NATIVE_BANK_DEPOSIT_ALL);
        assertEquals(PacketRepository.ACTION_BUTTON1_PACKET, router.bankPacketId(depositAll, 1));
        assertEquals(-1, router.bankPacketId(depositAll, 2));
        assertEquals(-1, router.bankPacketId(depositAll, Native950ActionRouter.NATIVE_EXAMINE_OPTION));
    }

    @Test public void bothBankGridsUseSelectedQuantityAndLastXWhileExplicitOptionsStayFixed() {
        Native950ActionRouter.Binding[] grids = {
                binding(BANK, NATIVE_BANK_ITEMS), binding(BANK, NATIVE_BANK_INVENTORY)};
        int[] explicitOptions = {2, 3, 4, 7};
        int[] explicitAmounts = {1, 5, 10, Integer.MAX_VALUE};
        int[] explicitPackets = {PacketRepository.ACTION_BUTTON2_PACKET, PacketRepository.ACTION_BUTTON3_PACKET,
                PacketRepository.ACTION_BUTTON4_PACKET, PacketRepository.ACTION_BUTTON6_PACKET};
        for (int mode : new int[] {5, 10, Integer.MAX_VALUE, 11}) {
            player.getBank().restoreNativePreferences(true, 37, mode);
            for (Native950ActionRouter.Binding grid : grids) {
                assertEquals("Default amount for " + grid + " mode " + mode,
                        mode == 11 ? 37 : mode, router.bankAmount(grid, 1));
                assertEquals(37, router.bankAmount(grid, 5));
                // Packet identity must retain default/last-X semantics, even when their amounts coincide.
                assertEquals(PacketRepository.ACTION_BUTTON1_PACKET, router.bankPacketId(grid, 1));
                assertEquals(PacketRepository.ACTION_BUTTON5_PACKET, router.bankPacketId(grid, 5));
                for (int i = 0; i < explicitOptions.length; i++) {
                    assertEquals(explicitAmounts[i], router.bankAmount(grid, explicitOptions[i]));
                    assertEquals(explicitPackets[i], router.bankPacketId(grid, explicitOptions[i]));
                }
                assertEquals(0, router.bankAmount(grid, 6)); // New-X prompt is not a transfer yet.
            }
            assertEquals(mode, player.getBank().getNativeDefaultInteractionAmount());
        }
        player.getBank().restoreNativePreferences(false, 58, 11);
        for (Native950ActionRouter.Binding grid : grids) {
            assertEquals(58, router.bankAmount(grid, 1));
            assertEquals(58, router.bankAmount(grid, 5));
            assertEquals(1, router.bankAmount(grid, 2));
            assertEquals(Integer.MAX_VALUE, router.bankAmount(grid, 7));
        }
        assertTrue("Quantity resolution itself must not dispatch an item transfer", handlers.calls.isEmpty());
    }
    // ------------------------------------------------------------------ component translation

    @Test public void nativeComponentsTranslateToTheComponentsTheHandlersBranchOn() {
        Native950ActionRouter.Binding backpack = binding(Native950ActionRouter.BACKPACK_INTERFACE, 5);
        assertEquals(Native950ActionRouter.Target.BACKPACK_ITEMS, backpack.target);
        assertEquals(Native950ActionRouter.BACKPACK_INTERFACE, backpack.legacyInterface);
        assertEquals(Native950ActionRouter.LEGACY_BACKPACK_COMPONENT, backpack.legacyComponent);

        assertEquals(Native950ActionRouter.LEGACY_BANK_ITEMS, binding(BANK, NATIVE_BANK_ITEMS).legacyComponent);
        assertEquals(Native950ActionRouter.LEGACY_BANK_INVENTORY, binding(BANK, NATIVE_BANK_INVENTORY).legacyComponent);
        assertEquals(Native950ActionRouter.LEGACY_BANK_DEPOSIT_ALL, binding(BANK, NATIVE_BANK_DEPOSIT_ALL).legacyComponent);
        assertEquals(BANK, binding(BANK, NATIVE_BANK_ITEMS).legacyInterface);

        Native950ActionRouter.Binding equipment = binding(Native950ActionRouter.EQUIPMENT_INTERFACE, 31);
        assertEquals(Native950ActionRouter.EQUIPMENT_INTERFACE, equipment.legacyInterface);
        assertEquals(Native950ActionRouter.LEGACY_EQUIPMENT_ITEMS, equipment.legacyComponent);

        // The bank close component has no 910 button branch: it is routed to
        // Player.closeInterfaces() by the adapter, never to ButtonHandler.
        assertEquals(-1, binding(BANK, NATIVE_BANK_CLOSE).legacyComponent);
    }

    @Test public void everyTranslationIsInvertibleSoNoTwoNativeComponentsShareA910Branch() {
        Set<String> legacy = new HashSet<String>();
        Set<Integer> native947 = new HashSet<Integer>();
        for (Native950ActionRouter.Binding b : router.bindings()) {
            assertTrue("duplicate 947 hash " + b, native947.add(b.nativeHash));
            if (b.legacyComponent < 0) continue;
            assertTrue("duplicate 910 pair " + b, legacy.add(b.legacyInterface + ":" + b.legacyComponent));
            // The reverse direction: the 910 pair identifies exactly one native pair.
            assertEquals(b.nativeHash, reverse(b.legacyInterface, b.legacyComponent));
        }
        assertEquals(7, router.bindings().size());
    }

    private int reverse(int legacyInterface, int legacyComponent) {
        int found = -1;
        for (Native950ActionRouter.Binding b : router.bindings())
            if (b.legacyInterface == legacyInterface && b.legacyComponent == legacyComponent) {
                assertEquals("ambiguous reverse translation", -1, found);
                found = b.nativeHash;
            }
        return found;
    }

    // ------------------------------------------------------------------ unmatched pairs and whitelist

    @Test public void unknownComponentPairsAreCountedAndNeverTranslated() {
        assertEquals(0, router.unmatchedPairs());
        int stranger = (BANK << 16) | 202;
        assertNull(router.binding(stranger));
        assertNull(router.binding(stranger));
        assertNull(router.binding((Native950ActionRouter.BACKPACK_INTERFACE << 16) | 7));
        assertEquals(3, router.unmatchedPairs());
        assertEquals(2, router.unmatchedPairs(stranger));
        assertTrue(router.report().contains("unmatched"));
    }

    @Test public void aNullBindingCanNeverReachTheHandlerAndIsCountedAsARejection() {
        Native950ActionRouter.Outcome outcome = router.button(null, PacketRepository.ACTION_BUTTON1_PACKET, 0, COINS);
        assertFalse(outcome.accepted);
        assertEquals(0, handlers.calls.size());
        assertEquals(1, router.rejections());
        assertEquals(0, router.dispatches());
    }

    @Test public void unsupportedOptionsAndComponentsWithoutA910BranchAreRejected() {
        Native950ActionRouter.Binding items = binding(BANK, NATIVE_BANK_ITEMS);
        assertFalse(router.button(items, -1, 0, COINS).accepted);
        assertFalse(router.button(binding(BANK, NATIVE_BANK_CLOSE),
                PacketRepository.ACTION_BUTTON1_PACKET, -1, -1).accepted);
        assertEquals(0, handlers.calls.size());
        assertEquals(2, router.rejections());
    }

    @Test public void anAcceptedButtonReachesTheHandlerWithThe910PairAndTheRealItemId() {
        Native950ActionRouter.Binding backpack = binding(Native950ActionRouter.BACKPACK_INTERFACE, 5);
        Native950ActionRouter.Outcome outcome = router.button(backpack, PacketRepository.ACTION_BUTTON2_PACKET, 3, LOGS);
        assertTrue(outcome.accepted);
        assertEquals(PacketRepository.ACTION_BUTTON2_PACKET, outcome.packetId);
        assertEquals(Collections.singletonList("button " + Native950ActionRouter.BACKPACK_INTERFACE + ":"
                        + Native950ActionRouter.LEGACY_BACKPACK_COMPONENT + " slot=3 item=" + LOGS
                        + " packet=" + PacketRepository.ACTION_BUTTON2_PACKET),
                handlers.calls);
        assertEquals(1, router.dispatches());
        assertEquals(1, router.dispatches("button:backpack.items"));
    }

    @Test public void aThrowingHandlerIsCountedAndDowngradedToARejection() {
        handlers.failWith = new IllegalStateException("boom");
        Native950ActionRouter.Outcome outcome = router.button(binding(BANK, NATIVE_BANK_ITEMS),
                PacketRepository.ACTION_BUTTON2_PACKET, 0, COINS);
        assertFalse(outcome.accepted);
        assertEquals(1, router.handlerFailures());
        assertEquals(1, router.failureDetails().size());
        assertEquals(0, router.dispatches());
    }

    @Test public void objectsAndNpcsOutsideTheM2bWhitelistNeverReachTheHandlers() {
        assertTrue(router.whitelistedObject(Native950ActionRouter.BANK_CHEST_ID));
        assertFalse(router.whitelistedObject(Native950ActionRouter.BANK_CHEST_ID + 1));
        assertTrue(router.whitelistedNpc(Native950ActionRouter.BANKER_ID));
        assertFalse(router.whitelistedNpc(1));
        assertTrue(router.whitelistedInterface(BANK));
        assertTrue(router.whitelistedInterface(Native950ActionRouter.BACKPACK_INTERFACE));
        assertTrue(router.whitelistedInterface(Native950ActionRouter.EQUIPMENT_INTERFACE));
        assertTrue(router.whitelistedInterface(Native950ActionRouter.MINIMAP_INTERFACE));
        assertFalse(router.whitelistedInterface(1477));
        assertFalse(router.whitelistedInterface(517 + 1));

        assertFalse(router.object(null, 2, false).accepted);
        WorldObject stranger = new WorldObject(Native950ActionRouter.BANK_CHEST_ID + 1, 10, 0, 3222, 3222, 0);
        assertFalse(router.object(stranger, 2, false).accepted);
        WorldObject chest = new WorldObject(Native950ActionRouter.BANK_CHEST_ID, 10, 0, 3222, 3222, 0);
        assertFalse("option 0 is not a client option", router.object(chest, 0, false).accepted);
        assertFalse("option 6 is not a client option", router.object(chest, 6, false).accepted);
        assertEquals(0, handlers.calls.size());
        assertEquals(4, router.rejections());
    }

    // ------------------------------------------------------------------ actor prediction

    @Test public void exactClaimsOnlyAcceptTheItemThatIsActuallyInTheSlot() {
        containers.seedStarterItems();
        Native950Containers.Snapshot inventory = containers.inventorySnapshot();
        assertTrue(Native950ActionRouter.exactClaim(COINS, 0, inventory));
        assertFalse(Native950ActionRouter.exactClaim(LOGS, 0, inventory));
        assertFalse(Native950ActionRouter.exactClaim(COINS, -1, inventory));
        assertFalse(Native950ActionRouter.exactClaim(COINS, 99, inventory));
        assertFalse("an empty slot claim is never exact", Native950ActionRouter.exactClaim(-1, 27, inventory));
    }

    @Test public void aClearedBackpackActorIsAcceptedOnlyWhenTheRequestWouldExhaustTheSlot() {
        containers.seedStarterItems();
        Native950Containers.Snapshot inventory = containers.inventorySnapshot();
        Set<Integer> unchanged = Collections.emptySet();
        // Coins: slot 0 holds 1000. A deposit-all clears the actor, so -1 resolves.
        assertEquals(COINS, Native950ActionRouter.transferItem(-1, 0, inventory, Integer.MAX_VALUE, -1, unchanged, catalog));
        assertEquals(COINS, Native950ActionRouter.transferItem(-1, 0, inventory, 1000, -1, unchanged, catalog));
        // A partial deposit leaves the actor in place, so the sentinel is not the
        // predicted claim and is handed on unchanged for the exact-id check to fail.
        assertEquals(-1, Native950ActionRouter.transferItem(-1, 0, inventory, 999, -1, unchanged, catalog));
        // A slot the server already changed this tick can never be predicted.
        Set<Integer> changed = new HashSet<Integer>(Arrays.asList(0));
        assertEquals(-1, Native950ActionRouter.transferItem(-1, 0, inventory, Integer.MAX_VALUE, -1, changed, catalog));
        // Out-of-range, empty and unverified slots resolve to nothing.
        assertEquals(-1, Native950ActionRouter.transferItem(-1, -1, inventory, 1, -1, unchanged, catalog));
        assertEquals(-1, Native950ActionRouter.transferItem(-1, 27, inventory, 1, -1, unchanged, catalog));
        // A concrete claim is always passed through untouched.
        assertEquals(LOGS, Native950ActionRouter.transferItem(LOGS, 0, inventory, 1, -1, unchanged, catalog));
    }

    @Test public void aWithdrawalClaimMustMatchTheClickedActorBeforeDeferredBankLayout() {
        player.getBank().bankTabs = new Item[][] {{new Item(LOGS, 5), new Item(SHRIMPS, 5)}};
        Native950Containers.Snapshot bank = containers.bankSnapshot();
        Set<Integer> unchanged = Collections.emptySet();
        // Partial withdrawal: the actor keeps its own id.
        assertEquals(LOGS, Native950ActionRouter.withdrawalItem(LOGS, 0, bank, 2, unchanged, catalog));
        assertEquals(-1, Native950ActionRouter.withdrawalItem(SHRIMPS, 0, bank, 2, unchanged, catalog));
        // Full withdrawal of a non-final slot clears only that actor before the sender.
        assertEquals(LOGS, Native950ActionRouter.withdrawalItem(Native950ActionRouter.EMPTY_BANK_ACTOR, 0, bank, 5, unchanged, catalog));
        assertEquals(-1, Native950ActionRouter.withdrawalItem(SHRIMPS, 0, bank, 5, unchanged, catalog));
        assertEquals(-1, Native950ActionRouter.withdrawalItem(Native950ActionRouter.EMPTY_BANK_ACTOR, 0, bank, 2, unchanged, catalog));
        assertEquals(LOGS, Native950ActionRouter.withdrawalItem(LOGS, 0, bank, 0, unchanged, catalog));
        assertEquals(-1, Native950ActionRouter.withdrawalItem(LOGS, 0, bank, -1, unchanged, catalog));
        assertEquals(-1, Native950ActionRouter.withdrawalItem(LOGS, 0, bank, 6, unchanged, catalog));
        assertEquals("the pre-click id is not the predicted post-action actor",
                -1, Native950ActionRouter.withdrawalItem(LOGS, 0, bank, 5, unchanged, catalog));
        // Full withdrawal of the final slot: the empty-group sentinel.
        assertEquals(SHRIMPS, Native950ActionRouter.withdrawalItem(Native950ActionRouter.EMPTY_BANK_ACTOR, 1, bank, 5,
                unchanged, catalog));
        assertEquals(-1, Native950ActionRouter.withdrawalItem(SHRIMPS, 1, bank, 5, unchanged, catalog));
        // Same-tick changed slot, out-of-range slot and empty slot are all refused.
        assertEquals(-1, Native950ActionRouter.withdrawalItem(LOGS, 0, bank, 2,
                new HashSet<Integer>(Arrays.asList(0)), catalog));
        assertEquals(-1, Native950ActionRouter.withdrawalItem(LOGS, -1, bank, 2, unchanged, catalog));
        assertEquals(-1, Native950ActionRouter.withdrawalItem(LOGS, 5, bank, 2, unchanged, catalog));
    }

    // ------------------------------------------------------------------ M4 clicked NPCs

    /** A world NPC that needs no cache: no definitions, no combat data, no spawn task. */
    private NPC worldNpc(int definitionId, int x, int y) {
        NPC npc = NPC.createNative950(definitionId, new WorldTile(x, y, 0), 1);
        // The 910 handlers all require the NPC to sit in a region the viewer has loaded; the
        // native world normally fills this list from the scene.
        player.getMapRegionsIds().add(Integer.valueOf(npc.getRegionId()));
        return npc;
    }

    @Test public void nativeNpcOptionsMapOntoTheDecoded910HandlerAndNothingElse() {
        // The 947 NPC opcode table has six entries; the 910 NPCHandler implements 1..4 and
        // examine. Option 5 has no 910 branch, so it is refused rather than folded onto one.
        for (int option = 1; option <= 4; option++)
            assertEquals("option " + option, option, Native950ActionRouter.legacyNpcOption(option));
        assertEquals(NPCHandler.EXAMINE_OPTION,
                Native950ActionRouter.legacyNpcOption(Native950ActionRouter.NATIVE_NPC_EXAMINE_OPTION));
        for (int option : new int[] {-1, 0, 5, 7, 255})
            assertEquals("option " + option, Native950ActionRouter.UNSUPPORTED_NPC_OPTION,
                    Native950ActionRouter.legacyNpcOption(option));
        // The sentinel can never collide with a real decoded option.
        assertTrue(NPCHandler.EXAMINE_OPTION != Native950ActionRouter.UNSUPPORTED_NPC_OPTION);
    }

    @Test public void aClickOnAResolvedNpcReachesTheDecodedNpcHandler() {
        NPC goblin = worldNpc(101, 3222, 3223);
        Native950ActionRouter.Outcome outcome = router.npcOption(goblin, 1, false);
        assertTrue(outcome.reason, outcome.accepted);
        assertEquals(Collections.singletonList("npc 1"), handlers.calls);
        assertEquals(1, router.dispatches());
        assertEquals(1, router.dispatches("npcOption:1"));
        // The banker's own dispatch names are untouched: this is not the banker path.
        assertEquals(0, router.dispatches("npcBank"));
        assertEquals(0, router.dispatches("npcTalk"));
        assertEquals(0, router.dispatches("npcCollect"));
        assertEquals(0, router.dispatches("npcExamine"));
    }

    @Test public void examineReachesTheDecodedExamineOverload() {
        NPC goblin = worldNpc(101, 3222, 3223);
        assertTrue(router.npcOption(goblin, Native950ActionRouter.NATIVE_NPC_EXAMINE_OPTION, false).accepted);
        assertEquals(Collections.singletonList("npc " + NPCHandler.EXAMINE_OPTION), handlers.calls);
        assertEquals(1, router.dispatches("npcOption:" + NPCHandler.EXAMINE_OPTION));
    }

    @Test public void anNpcOutsideTheViewersLoadedRegionsIsACountedRejection() {
        // Nothing is added to getMapRegionsIds(), which is the gate every
        // NPCHandler.handleOptionN opens with. The pre-filter turns it into a counted
        // rejection with a reason instead of a handler that silently returns.
        NPC stranger = NPC.createNative950(101, new WorldTile(3222, 3223, 0), 1);
        Native950ActionRouter.Outcome outcome = router.npcOption(stranger, 1, false);
        assertFalse(outcome.accepted);
        assertEquals(0, handlers.calls.size());
        assertEquals(0, router.dispatches());
        assertEquals(1, router.rejections());
    }

    @Test public void anAbsentNpcAndAnUnsupportedOptionNeverReachTheHandler() {
        assertFalse(router.npcOption(null, 1, false).accepted);
        NPC goblin = worldNpc(101, 3222, 3223);
        assertFalse("native option 5 has no 910 branch", router.npcOption(goblin, 5, false).accepted);
        assertFalse(router.npcOption(goblin, 0, false).accepted);
        assertEquals(0, handlers.calls.size());
        assertEquals(3, router.rejections());
    }

    @Test public void aFinishedNpcIsRefusedBeforeTheHandlerSeesIt() {
        // hasFinished() is one of the gates every NPCHandler.handleOptionN opens with, and it
        // is the one that matters most here: EntityList reuses the lowest free index, so a
        // click that arrives a tick after the NPC left must never reach a handler.
        NPC goblin = worldNpc(101, 3222, 3223);
        goblin.setFinished(true);
        assertFalse(router.npcOption(goblin, 1, false).accepted);
        assertEquals(0, handlers.calls.size());
        assertEquals(1, router.rejections());
    }

    @Test public void aThrowingNpcHandlerIsCountedAndDowngradedToARejection() {
        // A native 947 NPC has no 910 definitions, so the deeper branches of the real handler
        // throw rather than answering. Nothing reaches the wire; the click is a counted
        // rejection and the world tick is unaffected.
        handlers.failWith = new UnsupportedOperationException("Native 947 NPC cannot use legacy NPC definitions");
        NPC goblin = worldNpc(101, 3222, 3223);
        Native950ActionRouter.Outcome outcome = router.npcOption(goblin, 1, false);
        assertFalse(outcome.accepted);
        assertEquals(1, router.handlerFailures());
        assertEquals(0, router.dispatches());
    }

    @Test public void theBankerPathStillRefusesEveryNpcOutsideItsWhitelist() {
        // The banker entry points are unchanged by M4: they still answer only for NPC 494, so
        // generalising the click could not have widened them.
        NPC goblin = worldNpc(101, 3222, 3223);
        assertFalse(router.npcExamine(goblin, "Goblin").accepted);
        assertTrue(router.whitelistedNpc(Native950ActionRouter.BANKER_ID));
        assertFalse(router.whitelistedNpc(101));
        assertEquals(0, handlers.calls.size());
        assertEquals(1, router.rejections());
    }

    @Test public void onlyTheReversedDragPairIsAccepted() {
        containers.seedStarterItems();
        Native950Containers.Snapshot inventory = containers.inventorySnapshot();
        // Slot 0 = coins, slot 1 = logs. The client's onOp already swapped them, so
        // the claim for dragging 0 -> 1 is source=logs, target=coins.
        assertTrue(Native950ActionRouter.reversedDragPair(inventory, 0, 1, LOGS, COINS));
        assertFalse("the pre-swap order must not be accepted twice",
                Native950ActionRouter.reversedDragPair(inventory, 0, 1, COINS, LOGS));
        assertFalse(Native950ActionRouter.reversedDragPair(inventory, 0, 1, SHRIMPS, COINS));
        assertFalse(Native950ActionRouter.reversedDragPair(inventory, 0, 0, COINS, COINS));
        assertFalse(Native950ActionRouter.reversedDragPair(inventory, -1, 1, LOGS, COINS));
        assertFalse(Native950ActionRouter.reversedDragPair(inventory, 0, 28, LOGS, COINS));
        assertTrue(Native950ActionRouter.dragSlotsValid(inventory, 0, 1));
        assertFalse(Native950ActionRouter.dragSlotsValid(inventory, 1, 1));
        assertFalse(Native950ActionRouter.dragSlotsValid(inventory, 0, -1));
    }

}
