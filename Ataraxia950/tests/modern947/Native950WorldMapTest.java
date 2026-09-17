package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.InterfaceManager;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/** State/packet lifecycle for the map, independently of client rendering. */
public class Native950WorldMapTest {
    private String previousProperty;
    private EmbeddedChannel channel;
    private Player player;
    private Native950WorldMap map;
    private int verifications;

    @Before public void setup() {
        previousProperty = System.getProperty(Native950WorldMap.PROPERTY);
        System.setProperty(Native950WorldMap.PROPERTY, "true");
        channel = new EmbeddedChannel();
        player = Player.createNative950("map-state-test", new WorldTile(3217, 3258, 0), channel);
        player.setActive(true);
        player.getInterfaceManager().registerNativeOpen(1482, 1477, 30);
        map = new Native950WorldMap(player, channel, () -> verifications++);
    }

    @After public void cleanup() {
        if (previousProperty == null) System.clearProperty(Native950WorldMap.PROPERTY);
        else System.setProperty(Native950WorldMap.PROPERTY, previousProperty);
        channel.finishAndReleaseAll();
    }

    @Test public void old947MapDecorationDoesNotOpenAMap() {
        assertFalse(map.handle(button(1465, 10, 1)));
        assertFalse(map.isOpen());
        assertEquals(0, verifications);
        assertTrue(packets().isEmpty());
    }

    @Test public void openIsIdempotentAndCloseRestoresTheGameViewBeforeReopening() {
        assertTrue(map.handle(button(1465, 11, 1)));
        assertTrue(player.getInterfaceManager().containsInterface(1421));
        assertTrue(player.getInterfaceManager().containsInterface(1422));
        assertFalse(player.getInterfaceManager().containsInterface(1482));
        List<Native950Packets.Packet> open = packets();
        assertEquals(5, open.size());
        // Controls must be a later sibling of the viewport, not a descendant
        // hidden behind it, and must not introduce a modal mouse-input barrier.
        assertPacket(Native950Packets.closeSub(1477, 30), open.get(2));
        assertPacket(Native950Packets.openSub(1477, 31, 1421, true), open.get(3));
        assertPacket(Native950Packets.openSub(1477, 37, 1422, true), open.get(4));
        assertEquals((1477 << 16) | 31, player.getInterfaceManager().getInterfaceParentId(1421));
        assertEquals((1477 << 16) | 37, player.getInterfaceManager().getInterfaceParentId(1422));
        assertTrue(map.handle(button(1465, 11, 1)));
        assertEquals(0, packets().size());
        assertEquals(1, verifications);

        assertTrue(map.handle(button(1422, 111, 1)));
        List<Native950Packets.Packet> close = packets();
        assertEquals(4, close.size());
        assertPacket(Native950Packets.runClientScript(1898), close.get(0));
        assertPacket(Native950Packets.closeSub(1477, 37), close.get(1));
        assertPacket(Native950Packets.closeSub(1477, 31), close.get(2));
        assertPacket(Native950Packets.openSub(1477, 30, 1482, true), close.get(3));
        assertFalse(player.getInterfaceManager().containsInterface(1421));
        assertFalse(player.getInterfaceManager().containsInterface(1422));
        assertEquals((1477 << 16) | 30, player.getInterfaceManager().getInterfaceParentId(1482));
        map.close();
        assertEquals(0, packets().size());
        assertTrue(map.handle(button(1465, 11, 1)));
        assertEquals(5, packets().size());
        assertEquals(2, verifications);
    }

    @Test public void clientMapControlsAndWrongCloseOptionDoNotCloseTheMap() {
        map.handle(button(1465, 11, 1));
        packets();
        assertFalse(map.handle(button(1422, 112, 1)));
        assertFalse(map.handle(button(1422, 111, 2)));
        assertTrue(player.getInterfaceManager().containsInterface(1422));
        assertEquals(0, packets().size());
    }

    @Test public void anInitiallyClosedMapNeverTreatsTheSceneAsAnOpenMap() {
        assertFalse(map.isOpen());
        assertFalse(player.getInterfaceManager().containsWorldMapInterface());
        assertTrue(player.getInterfaceManager().containsGameMapInterface());
        player.getInterfaceManager().removeWorldMapInterface();
        player.getInterfaceManager().removeWorldMapInterface();
        map.close();
        assertEquals((1477 << 16) | 30, player.getInterfaceManager().getInterfaceParentId(1482));
        assertEquals(0, packets().size());
    }

    @Test public void sharedCleanupAfterMapClosePreservesTheSceneOnRepeatedCalls() throws Exception {
        // The isolated test has no cache for unrelated panel-slot lookups.
        // Keep Player.closeInterfaces and the map bridge real, with those other panels absent.
        InterfaceManager manager = new InterfaceManager(player) {
            @Override public boolean containsScreenInter() { return false; }
            @Override public boolean containsInventoryInter() { return false; }
            @Override public boolean containsBankInterface() { return false; }
            @Override public boolean containsCentralInterfaceLargeInterface() { return false; }
            @Override public boolean containsPlayerInspectInterface() { return false; }
        };
        set(player, "interfaceManager", manager);
        manager.registerNativeOpen(1482, 1477, 30);
        map = new Native950WorldMap(player, channel, () -> verifications++);
        map.handle(button(1465, 11, 1));
        packets();
        map.close();
        packets();

        player.closeInterfaces();
        player.closeInterfaces();

        assertFalse(map.isOpen());
        assertFalse(manager.containsWorldMapInterface());
        assertTrue(manager.containsGameMapInterface());
        assertEquals((1477 << 16) | 30, manager.getInterfaceParentId(1482));
        // In particular, generic cleanup must never send the old map close for 1477:30.
        assertEquals(0, packets().size());
    }

    @Test public void sharedMapRemovalUsesTheOwnerAndRestoresTheSceneOnlyOnce() {
        map.handle(button(1465, 11, 1));
        packets();
        assertTrue(map.isOpen());
        assertTrue(player.getInterfaceManager().containsWorldMapInterface());
        assertFalse(player.getInterfaceManager().containsGameMapInterface());

        player.getInterfaceManager().removeWorldMapInterface();

        List<Native950Packets.Packet> close = packets();
        assertEquals(4, close.size());
        assertPacket(Native950Packets.runClientScript(1898), close.get(0));
        assertPacket(Native950Packets.closeSub(1477, 37), close.get(1));
        assertPacket(Native950Packets.closeSub(1477, 31), close.get(2));
        assertPacket(Native950Packets.openSub(1477, 30, 1482, true), close.get(3));
        assertFalse(map.isOpen());
        assertFalse(player.getInterfaceManager().containsWorldMapInterface());
        assertFalse(player.getInterfaceManager().containsInterface(1421));
        assertFalse(player.getInterfaceManager().containsInterface(1422));
        assertTrue(player.getInterfaceManager().containsGameMapInterface());
        assertEquals((1477 << 16) | 30, player.getInterfaceManager().getInterfaceParentId(1482));
        player.getInterfaceManager().removeWorldMapInterface();
        map.close();
        assertEquals(0, packets().size());
    }

    @Test public void disabledFlagIgnoresOpenAndUnopenedMapButtons() {
        System.setProperty(Native950WorldMap.PROPERTY, "false");
        assertFalse(map.handle(button(1465, 11, 1)));
        assertFalse(map.handle(button(1422, 111, 1)));
        assertEquals(0, verifications);
        assertTrue(player.getInterfaceManager().containsInterface(1482));
        assertEquals(0, packets().size());
    }

    @Test public void failedVerificationLeavesSceneAndWalkQueueUntouched() {
        player.getWalkSteps().add(new Object[] {4, 3218, 3258, false});
        map = new Native950WorldMap(player, channel, () -> { throw new IllegalStateException("changed cache"); });
        try { map.handle(button(1465, 11, 1)); fail("changed cache accepted"); }
        catch (IllegalStateException expected) { assertEquals("changed cache", expected.getMessage()); }
        assertEquals(1, player.getWalkSteps().size());
        assertTrue(player.getInterfaceManager().containsInterface(1482));
        assertFalse(player.getInterfaceManager().containsInterface(1422));
        assertEquals(0, packets().size());
    }

    @Test public void closeModalClosesMapAndBankStateWithoutReopeningEither() throws Exception {
        Native950Interactions interactions = new Native950Interactions(player, channel, content());
        set(interactions, "worldMap", map);
        player.getInterfaceManager().setNative950WorldMap(map);
        interactions.handle(button(1465, 11, 1));
        packets();
        // Simulate leftover bank bookkeeping as well: CLOSE_MODAL must retire both.
        set(interactions, "bankOpen", true);
        player.getInterfaceManager().registerNativeOpen(517, 1477, 369);
        player.closeInterfaceLocked = true;
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        assertFalse(interactions.snapshot().bankOpen);
        assertFalse(player.getInterfaceManager().containsInterface(517));
        assertFalse(player.getInterfaceManager().containsInterface(1422));
        assertTrue(player.getInterfaceManager().containsInterface(1482));
        assertEquals(0, interactions.snapshot().unhandledActions);
        interactions.afterMovement();
        assertFalse(interactions.snapshot().bankOpen);
        assertFalse(player.getInterfaceManager().containsInterface(1422));
    }

    @Test public void lockedMapRequestsCannotCloseAnExistingBank() throws Exception {
        Native950Interactions interactions = new Native950Interactions(player, channel, content());
        set(interactions, "worldMap", map);
        player.getInterfaceManager().setNative950WorldMap(map);
        set(interactions, "bankOpen", true);
        player.getInterfaceManager().registerNativeOpen(517, 1477, 369);
        player.lock(10);
        interactions.handle(button(1465, 11, 1));
        assertTrue(interactions.snapshot().bankOpen);
        assertTrue(player.getInterfaceManager().containsInterface(517));
        player.unlock();
        player.closeInterfaceLocked = true;
        interactions.handle(button(1465, 11, 1));
        assertTrue(interactions.snapshot().bankOpen);
        assertTrue(player.getInterfaceManager().containsInterface(517));
        assertFalse(player.getInterfaceManager().containsInterface(1422));
        assertEquals(0, verifications);
        assertEquals(2, interactions.snapshot().rejectedActions);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] amounts = new int[11]; amounts[1] = 1;
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                amounts, amounts, Collections.emptyList(), Collections.emptyList()));
    }

    private List<Native950Packets.Packet> packets() {
        channel.flush();
        List<Native950Packets.Packet> packets = new ArrayList<>();
        Object packet;
        while ((packet = channel.readOutbound()) != null) packets.add((Native950Packets.Packet) packet);
        return packets;
    }

    private static void assertPacket(Native950Packets.Packet expected, Native950Packets.Packet actual) {
        assertEquals(expected.type(), actual.type());
        assertArrayEquals(expected.payload(), actual.payload());
    }

    /**
     * One IF_BUTTON frame as a 950 client sends it: options 1..10 are opcodes
     * {18, 122, 89, 100, 81, 126, 49, 66, 31, 59} and the body is nine bytes - b0..b2 item BE u24
     * (0xFFFFFF absent), b3..b6 hash intv2 [h>>>16, h>>>24, h, h>>>8], b7..b8 slot BE u16.
     * 947 sent opcode 96/77 with eight bytes in a different order.
     */
    static Native950Actions.InterfaceAction button(int panel, int component, int option) {
        int hash = (panel << 16) | component;
        return (Native950Actions.InterfaceAction) Native950Actions.decode(option == 1 ? 18 : 122,
                new byte[] {-1, -1, -1,
                        (byte) (hash >>> 16), (byte) (hash >>> 24), (byte) hash, (byte) (hash >>> 8),
                        -1, -1});
    }
}
