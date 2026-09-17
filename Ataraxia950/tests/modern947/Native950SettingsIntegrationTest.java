package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/** Decoded settings events across the real interaction and shared UI owners. */
public class Native950SettingsIntegrationTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Interactions interactions;
    private String previousMapProperty;

    @Before public void setup() throws Exception {
        previousMapProperty = System.getProperty(Native950WorldMap.PROPERTY);
        System.setProperty(Native950WorldMap.PROPERTY, "true");
        channel = new EmbeddedChannel();
        player = Player.createNative950("settings-integration", new WorldTile(3217, 3258, 0), channel);
        player.setActive(true);
        // These unrelated legacy slot predicates require a cache. Keep the shared
        // cleanup itself real while the isolated fixture has no other panels.
        InterfaceManager manager = new InterfaceManager(player) {
            @Override public boolean containsScreenInter() { return false; }
            @Override public boolean containsInventoryInter() { return false; }
            @Override public boolean containsBankInterface() { return false; }
            @Override public boolean containsCentralInterfaceLargeInterface() { return false; }
            @Override public boolean containsPlayerInspectInterface() { return false; }
        };
        set(player, "interfaceManager", manager);
        interactions = interactions(player, channel, () -> {});
        set(interactions, "worldMap", new Native950WorldMap(player, channel, () -> {}));
        manager.registerNativeOpen(1482, 1477, 30);
    }

    @After public void cleanup() {
        if (previousMapProperty == null) System.clearProperty(Native950WorldMap.PROPERTY);
        else System.setProperty(Native950WorldMap.PROPERTY, previousMapProperty);
        channel.finishAndReleaseAll();
    }

    @Test public void acceptedEntryRetiresDialogueAndLateContinueCannotAdvanceIt() {
        ProbeDialogue dialogue = startDialogue();
        pendingRoute();
        interactions.handle(entry());
        assertOpen();
        assertFalse(player.getDialogueManager().hasDialogue());
        assertFalse(player.getNative950Dialogues().isOpen());
        assertEquals(1, dialogue.finishes);
        assertNull(player.getRouteEvent());
        assertFalse(player.hasWalkSteps());
        interactions.afterMovement();
        assertOpen();
        interactions.handle(reply());
        assertEquals(0, dialogue.runs);
        assertOpen();
        assertSceneAndNoClosePacket();
    }

    /**
     * The ordering contract of {@code Native950World.bootstrapOpensScene}: the server may only
     * register 1482 as owning 1477:30 if the login burst it actually flushed still leaves that
     * sub-interface open at the end - a later IF_OPENTOP resets the whole stack, a matching
     * IF_CLOSESUB closes it, and a second IF_OPENSUB on the same parent replaces the occupant.
     *
     * <p>The ORDERING is revision independent and this test does not change for 950. What is
     * revision dependent is how the helper reads an IF_OPENSUB body, and as of the 950 writer
     * port it reads it at 947 offsets - see the failure note on the first assertTrue below.
     * Do not relax these assertions to make the suite green: the fix belongs in
     * {@code Native950World.bootstrapOpensScene}, not here.
     */
    @Test public void initialSceneOwnershipFollowsTheActualOrderedBootstrap() {
        Native950Packets.Packet scene = Native950Packets.openSub(1477, 30, 1482, true);
        Native950Packets.Packet top = Native950Packets.openTop(1477);
        assertFalse("an empty bootstrap opens nothing",
                Native950World.bootstrapOpensScene(Collections.<Native950Packets.Packet>emptyList()));
        assertFalse("IF_OPENTOP alone establishes no sub-interface",
                Native950World.bootstrapOpensScene(Collections.singletonList(top)));
        // KNOWN FAILURE on 950, and it is the production code that is wrong, not this line.
        // Native950World.bootstrapOpensScene still decodes the IF_OPENSUB body at 947 offsets:
        //   parent from bytes 19..22 under the intv1 inverse ((b21<<24)|(b22<<16)|(b19<<8)|b20)
        //   interface id from bytes 4..5, little-endian and unbiased.
        // The 950 body (plan PART 1, IF_OPENSUB 8 -> 100, size 23) is
        //   b0..3 parent PLAIN big-endian u32, b4..15 = 0, b16 = id+128, b17 = id>>>8,
        //   b18 = 128-walkable, b19..22 = 0
        // - which is exactly what Native950Packets.openSub now writes. So the helper reads four
        // zero bytes as the parent, never matches (1477<<16)|30, and reports "the bootstrap never
        // opened the scene" for every login. The 950 reader is:
        //   int parent = ((b[0]&255)<<24)|((b[1]&255)<<16)|((b[2]&255)<<8)|(b[3]&255);
        //   if (parent == ((1477<<16)|30)) open = (((b[16]&255)-128) & 255 | ((b[17]&255)<<8)) == 1482;
        // Consequence if left unfixed: the client HAS 1482 open at 1477:30 after login but
        // InterfaceManager.registerNativeOpen is never called, so every later owner check
        // ("is the scene open?") disagrees with the client. This client does not error on a
        // redundant open or a refused close - it just ends up with a stale or duplicated
        // sub-interface, which is the silent-divergence failure mode, not a crash.
        assertTrue("a flushed IF_OPENSUB 1477:30 -> 1482 must be seen as opening the scene,"
                + " even with unrelated opens after it",
                Native950World.bootstrapOpensScene(Arrays.asList(top, scene,
                        Native950Packets.openSub(1477, 64, 1431, true))));
        assertFalse("a later IF_OPENTOP resets the interface stack and drops the scene",
                Native950World.bootstrapOpensScene(Arrays.asList(scene, top)));
        assertFalse("a matching IF_CLOSESUB on 1477:30 closes the scene again",
                Native950World.bootstrapOpensScene(Arrays.asList(scene, Native950Packets.closeSub(1477, 30))));
        assertFalse("a second IF_OPENSUB on 1477:30 replaces 1482 with 1421",
                Native950World.bootstrapOpensScene(Arrays.asList(scene, Native950Packets.openSub(1477, 30, 1421, true))));
        assertTrue("an IF_CLOSESUB on a DIFFERENT component leaves the scene open",
                Native950World.bootstrapOpensScene(Arrays.asList(scene, Native950Packets.closeSub(1477, 31))));
    }

    @Test public void malformedEntryDoesNotCancelTheCurrentDialogue() {
        ProbeDialogue dialogue = startDialogue();
        RouteEvent route = pendingRoute();
        interactions.handle(button(1431, 0, 99, -1)); // RuneMetrics actor6 is now valid.
        interactions.handle(button(1433, 15, -1, -1)); // Unowned Quick Options remains invalid.
        interactions.handle(button(1431, 0, 7, 137));
        interactions.handle(button(1477, 8, 0, -1));
        assertClosed();
        assertSame(dialogue, player.getDialogueManager().getDialogue());
        assertTrue(player.getNative950Dialogues().isOpen());
        assertEquals(0, dialogue.finishes);
        assertSame(route, player.getRouteEvent());
        assertTrue(player.hasWalkSteps());
        assertSceneAndNoClosePacket();
    }

    @Test public void lockedEntryPreservesExistingDialogueUntilTheNormalTickCleanup() {
        ProbeDialogue dialogue = startDialogue();
        player.lock(10);
        interactions.handle(entry());
        assertClosed();
        assertSame(dialogue, player.getDialogueManager().getDialogue());
        assertEquals(0, dialogue.finishes);
        player.unlock();
        player.closeInterfaceLocked = true;
        interactions.handle(entry());
        assertClosed();
        assertSame(dialogue, player.getDialogueManager().getDialogue());
        assertSceneAndNoClosePacket();
    }

    @Test public void verificationFailurePreservesTheDialogueAndPendingRoute() throws Exception {
        // Replace only the verifier-bearing owner; reconstructing the whole
        // adapter would incorrectly seed an already initialized character twice.
        set(interactions, "settings", new Native950Settings(player, channel,
                () -> { throw new IllegalStateException("changed settings cache"); }));
        ProbeDialogue dialogue = startDialogue();
        RouteEvent route = pendingRoute();
        try {
            interactions.handle(entry());
            fail("Unverified settings opened");
        } catch (IllegalStateException expected) {
            assertEquals("changed settings cache", expected.getMessage());
        }
        assertClosed();
        assertSame(dialogue, player.getDialogueManager().getDialogue());
        assertEquals(0, dialogue.finishes);
        assertSame(route, player.getRouteEvent());
        assertTrue(player.hasWalkSteps());
        assertSceneAndNoClosePacket();
    }

    @Test public void entryCancelsAnOwnedAmountAndLateCountCannotTransferItems() throws Exception {
        Native950QuantityInput quantity = (Native950QuantityInput) field(interactions, "quantityInput");
        Constructor<Native950Containers.Snapshot> ctor = Native950Containers.Snapshot.class
                .getDeclaredConstructor(Item[].class, int.class);
        ctor.setAccessible(true);
        Native950Containers.Snapshot bank = ctor.newInstance(new Item[] {new Item(995, 20)}, 1);
        assertTrue(quantity.beginWithdraw(1L, 0, 995, bank));
        set(interactions, "quantityPromptVisible", true);
        set(interactions, "bankOpen", true);
        player.getInterfaceManager().registerNativeOpen(1418, 1477, 749);
        player.getInterfaceManager().registerNativeOpen(1469, 1418, 2);
        interactions.handle(entry());
        assertOpen();
        assertFalse(quantity.active());
        assertFalse(interactions.snapshot().bankOpen);
        assertFalse(player.getInterfaceManager().containsInterface(1418));
        assertFalse(player.getInterfaceManager().containsInterface(1469));
        interactions.handle(Native950Actions.decode(120, ByteBuffer.allocate(8).putLong(7L).array()));
        assertEquals(0, interactions.snapshot().transactions);
        assertEquals(1000, total(interactions.snapshot().inventory, 995));
        assertSceneAndNoClosePacket();
    }

    @Test public void modalCloseRetiresSettingsEvenWhenInactiveAndBothCloseGuardsAreLocked() {
        interactions.handle(entry());
        interactions.handle(button(1477, 714, 15, -1));
        assertEquals((1448 << 16) | 5, player.getInterfaceManager().getInterfaceParentId(429));
        assertEquals((1448 << 16) | 3, player.getInterfaceManager().getInterfaceParentId(1426));
        assertFalse(player.getInterfaceManager().containsInterface(742));
        player.lock(10);
        player.closeInterfaceLocked = true;
        player.setActive(false);
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        assertClosed();
        assertFalse(player.getInterfaceManager().containsInterface(429));
        assertFalse(player.getInterfaceManager().containsInterface(1426));
        assertFalse(player.getInterfaceManager().containsInterface(1448));
        player.unlock();
        player.closeInterfaceLocked = false;
        player.setActive(true);
        interactions.handle(button(1477, 714, 15, -1)); // Old Audio row cannot reopen the owner.
        interactions.handle(button(1477, 717, 1, -1));
        assertClosed();
        assertSceneAndNoClosePacket();
    }

    @Test public void walkingTickInvalidationAndDisconnectEachRetireSettings() {
        interactions.handle(entry());
        assertOpen();
        interactions.walking();
        assertClosed();
        interactions.handle(entry());
        player.lock(10);
        interactions.beginTick();
        assertClosed();
        player.unlock();
        interactions.handle(entry());
        player.setActive(false);
        interactions.handle(button(1477, 714, 15, -1));
        assertClosed();
        player.setActive(true);
        interactions.handle(entry());
        interactions.close();
        assertClosed();
        interactions.close();
        assertSceneAndNoClosePacket();
    }

    @Test public void mapAndSettingsTransitionsPreserveTheSceneAndRetireEachOthersOwner() {
        interactions.handle(entry());
        assertOpen();
        assertSceneAndNoClosePacket();
        interactions.handle(button(1465, 11, -1, -1));
        assertClosed();
        assertTrue(player.getInterfaceManager().containsWorldMapInterface());
        discardPackets(); // Opening the map intentionally closes the scene once.
        interactions.handle(entry());
        assertOpen();
        assertFalse(player.getInterfaceManager().containsWorldMapInterface());
        assertSceneAndNoClosePacket();
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        assertClosed();
        interactions.handle(button(1477, 714, 15, -1));
        assertClosed();
        assertSceneAndNoClosePacket();
    }

    @Test public void sharedPlayerCleanupUsesTheSettingsOwnerWithoutTouchingTheScene() {
        interactions.handle(entry());
        assertOpen();
        player.closeInterfaces();
        player.closeInterfaces();
        assertClosed();
        assertSceneAndNoClosePacket();
    }

    @Test public void settingsOwnershipDoesNotLeakBetweenPlayers() {
        EmbeddedChannel otherChannel = new EmbeddedChannel();
        try {
            Player other = Player.createNative950("other-settings", new WorldTile(3217, 3258, 0), otherChannel);
            other.setActive(true);
            Native950Interactions otherInteractions = interactions(other, otherChannel, () -> {});
            interactions.handle(entry());
            assertFalse(other.getInterfaceManager().containsNative950Settings());
            otherInteractions.handle(button(1477, 714, 15, -1));
            assertFalse(other.getInterfaceManager().containsNative950Settings());
            otherInteractions.handle(entry());
            assertTrue(other.getInterfaceManager().containsNative950Settings());
            interactions.close();
            assertClosed();
            assertTrue(other.getInterfaceManager().containsNative950Settings());
            otherInteractions.close();
            assertFalse(other.getInterfaceManager().containsNative950Settings());
            assertSceneAndNoClosePacket();
        } finally { otherChannel.finishAndReleaseAll(); }
    }

    private ProbeDialogue startDialogue() {
        ProbeDialogue dialogue = new ProbeDialogue();
        player.getDialogueManager().startDialogue(dialogue);
        return dialogue;
    }

    private RouteEvent pendingRoute() {
        RouteEvent route = new RouteEvent(new WorldTile(3218, 3258, 0), () -> {
            throw new AssertionError("Retired approach must never dispatch after settings entry");
        });
        player.setRouteEvent(route);
        player.getWalkSteps().add(new Object[] {4, 3218, 3258, false});
        return route;
    }

    private static final class ProbeDialogue extends Dialogue {
        int runs, finishes;
        @Override public void start() { sendDialogue("Settings must cancel this pending reply."); }
        @Override public void run(int panel, int component) { runs++; }
        @Override public void finish() { finishes++; }
    }

    private void assertOpen() { assertTrue(player.getInterfaceManager().containsNative950Settings()); }
    private void assertClosed() { assertFalse(player.getInterfaceManager().containsNative950Settings()); }

    private void assertSceneAndNoClosePacket() {
        assertEquals((1477 << 16) | 30, player.getInterfaceManager().getInterfaceParentId(1482));
        channel.flush();
        Object value;
        Native950Packets.Packet forbidden = Native950Packets.closeSub(1477, 30);
        while ((value = channel.readOutbound()) != null) {
            assertTrue(value instanceof Native950Packets.Packet);
            Native950Packets.Packet packet = (Native950Packets.Packet) value;
            assertFalse("Settings cleanup must never close the scene", packet.type() == forbidden.type()
                    && Arrays.equals(packet.payload(), forbidden.payload()));
        }
    }

    private void discardPackets() { channel.flush(); while (channel.readOutbound() != null) { } }

    private static Native950Interactions interactions(Player player, EmbeddedChannel channel, Runnable verifier) {
        return new Native950Interactions(player, channel, content(), null, null, () -> {}, () -> {}, verifier);
    }

    // 950 inbound frames. These were 947 until Native950Actions was ported, and both helpers
    // changed with it exactly as the note here specified.
    private static Native950Actions.Action entry() { return button(1431, 0, 7, -1); }

    /**
     * IF_BUTTON option 1: 950 opcode 18, nine bytes. b0..b2 item BE u24 (0xFFFFFF absent),
     * b3..b6 hash intv2 [h>>>16, h>>>24, h, h>>>8], b7..b8 slot BE u16. 947 sent opcode 96 with
     * eight bytes in a different order, so nothing about this frame carried over.
     */
    private static Native950Actions.Action button(int panel, int component, int slot, int item) {
        int hash = (panel << 16) | component;
        return Native950Actions.decode(18, new byte[] {
                (byte) (item >>> 16), (byte) (item >>> 8), (byte) item,
                (byte) (hash >>> 16), (byte) (hash >>> 24), (byte) hash, (byte) (hash >>> 8),
                (byte) (slot >>> 8), (byte) slot});
    }

    /**
     * Dialogue click: 950 opcode 101, still six bytes, but the two fields swapped ends. 950 reads
     * the component hash first as a plain big-endian i32, then the slot as b4 = slot>>>8 and
     * b5 = (slot & 255) + 128. 947 opcode 15 put the slot first. Both orders are legal six-byte
     * frames, which is why the wrong one resolves to a real but different component rather than
     * failing.
     */
    private static Native950Actions.Action reply() {
        int hash = (1186 << 16) | 8;
        int slot = -1;
        return Native950Actions.decode(101, new byte[] {
                (byte) (hash >>> 24), (byte) (hash >>> 16), (byte) (hash >>> 8), (byte) hash,
                (byte) (slot >>> 8), (byte) (slot + 128)});
    }
    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
    private static int total(Native950Containers.Snapshot values, int item) {
        int amount = 0;
        for (int i = 0; i < values.ids.length; i++) if (values.ids[i] == item) amount += values.amounts[i];
        return amount;
    }
    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] amounts = new int[11]; amounts[1] = amounts[2] = 1;
        amounts[3] = 5; amounts[4] = 10; amounts[7] = Integer.MAX_VALUE;
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                amounts, amounts, Collections.emptyList(), Collections.emptyList(), 6));
    }
}
