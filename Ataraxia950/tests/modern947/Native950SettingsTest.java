package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/** Ownership and native frame lifecycle; visual preference changes require the real client. */
public class Native950SettingsTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Settings settings;
    private int verifications;

    @Before public void setup() {
        channel = new EmbeddedChannel();
        player = Player.createNative950("settings-state-test", new WorldTile(3208, 3215, 0), channel);
        player.setActive(true);
        player.getInterfaceManager().registerNativeOpen(1482, 1477, 30);
        settings = new Native950Settings(player, channel, () -> verifications++);
    }

    @After public void cleanup() { channel.finishAndReleaseAll(); }

    @Test public void entryRequiresTheExactNativeTargetAndSentinels() {
        assertTrue(Native950Settings.isOpenRequest(button(1477, 8, -1, -1, 1)));
        assertTrue(Native950Settings.isOpenRequest(button(1431, 0, 7, -1, 1)));
        assertTrue(Native950Settings.isOpenRequest(button(1430, 256, -1, -1, 1)));
        for (Native950Actions.InterfaceAction invalid : Arrays.asList(
                button(1477, 8, 0, -1, 1), button(1477, 8, -1, 0, 1),
                button(1477, 8, -1, -1, 2), button(1431, 0, 137, -1, 1),
                button(1431, 0, 7, 0, 1), button(1433, 14, -1, -1, 1))) {
            assertFalse(Native950Settings.isOpenRequest(invalid));
            assertFalse(settings.handle(invalid));
        }
        assertFalse(settings.isOpen());
        assertEquals(0, verifications);
        assertTrue(packets().isEmpty());
    }

    @Test public void quickOptionsEntriesChooseTheirNativeSettingsPage() {
        settings.handle(button(1433,35,-1,-1,1));
        assertTrue(player.getInterfaceManager().containsInterface(567));
        settings.handle(button(1433,36,-1,-1,1));
        assertFalse(player.getInterfaceManager().containsInterface(567));
        assertTrue(player.getInterfaceManager().containsInterface(1444));
        settings.handle(button(1433,15,-1,-1,1));
        assertFalse(player.getInterfaceManager().containsInterface(1444));
        assertTrue(player.getInterfaceManager().containsInterface(742));
        settings.close();
        assertFalse(player.getInterfaceManager().containsInterface(1448));
        assertTrue(player.getInterfaceManager().containsInterface(1482));
    }
    @Test public void actionBarCogUsesTheNativeCombatSettingsPage() {
        assertTrue(settings.handle(button(1430,256,-1,-1,1)));
        assertTrue(player.getInterfaceManager().containsInterface(365));
    }
    @Test public void gameplayRevolutionRowPersistsTheRealClientConfiguration(){
        settings.handle(button(1477,8,-1,-1,1));
        assertTrue(settings.handle(button(1477,714,3,-1,1)));packets();
        assertTrue(settings.handle(button(365,19,10241,-1,1)));
        assertTrue(player.getNative950ActionBar().isRevolutionEnabled());
        List<Native950Packets.Packet> revolution=packets();
        assertTrue(contains(revolution,Native950Packets.varbitSmall(21682,1)));
        assertTrue(contains(revolution,Native950Packets.runClientScript(2929)));
        assertTrue(settings.handle(button(365,19,10240,-1,1)));
        assertFalse(player.getNative950ActionBar().isRevolutionEnabled());
    }

    @Test public void aCacheMismatchCannotMutateOrWriteAnOpen() {
        settings = new Native950Settings(player, channel, () -> { throw new IllegalStateException("cache mismatch"); });
        try {
            settings.handle(button(1477, 8, -1, -1, 1));
            fail("Must reject the changed cache");
        } catch (IllegalStateException expected) { assertEquals("cache mismatch", expected.getMessage()); }
        assertFalse(settings.isOpen());
        assertFalse(player.getInterfaceManager().containsInterface(1448));
        assertScene();
        assertTrue(packets().isEmpty());
    }

    @Test public void nativeTabsReplaceOnlyTheirOwnedPageAndClosePreservesScene() {
        settings.verifyBeforeOpen();
        assertTrue(settings.handle(button(1477, 8, -1, -1, 1)));
        assertEquals(1, verifications);
        assertTrue(player.getInterfaceManager().containsNative950Settings());
        assertEquals((1477 << 16) | 715, player.getInterfaceManager().getInterfaceParentId(1448));
        assertEquals((1448 << 16) | 3, player.getInterfaceManager().getInterfaceParentId(1426));
        assertEquals(1426 << 16, player.getInterfaceManager().getInterfaceParentId(742));
        List<Native950Packets.Packet> opening = packets();
        assertTrue(contains(opening, Native950Packets.runClientScript(8179)));
        assertQuickCleanupRestoresOptionsKey(opening);
        assertTrue(contains(opening, Native950Packets.interfaceEvents(1477, 8, -1, -1, 252)));
        assertTrue(contains(opening, Native950Packets.runClientScript(8283, 21182, 0)));
        for (int bit : new int[] {19029, 19031, 47565, 60056})
            assertTrue(contains(opening, Native950Packets.varbitSmall(bit, 0)));
        for (int actor : new int[] {3, 7, 11, 15, 19, 23})
            assertTrue(contains(opening, Native950Packets.interfaceEvents(1477, 714, actor, actor, 2)));

        assertTrue(settings.handle(button(1477, 714, 15, -1, 1)));
        assertFalse(player.getInterfaceManager().containsInterface(742));
        assertEquals((1448 << 16) | 3, player.getInterfaceManager().getInterfaceParentId(1426));
        assertEquals((1448 << 16) | 5, player.getInterfaceManager().getInterfaceParentId(429));
        assertFalse(player.getInterfaceManager().containsInterface(187));
        List<Native950Packets.Packet> audio = packets();
        assertTrue(contains(audio, Native950Packets.runClientScript(8283, 21183, 1)));
        assertTrue(contains(audio, Native950Packets.hideInterface(1448, 3, true)));
        assertTrue(contains(audio, Native950Packets.hideInterface(1448, 5, false)));
        assertFalse(contains(audio, Native950Packets.runClientScript(8283, 21182, 0)));
        assertNoSceneClose(audio);
        assertTrue(settings.handle(button(1477, 714, 7, -1, 1)));
        assertFalse(player.getInterfaceManager().containsInterface(429));
        assertTrue(player.getInterfaceManager().containsInterface(742));
        assertNoSceneClose(packets());

        assertTrue(settings.handle(button(1477, 717, 1, -1, 1)));
        assertFalse(settings.isOpen());
        assertFalse(player.getInterfaceManager().containsNative950Settings());
        for (int id : new int[] {1448, 1426, 742, 429, 365, 1444, 567})
            assertFalse(player.getInterfaceManager().containsInterface(id));
        assertScene();
        List<Native950Packets.Packet> closing = packets();
        assertTrue(contains(closing, Native950Packets.runClientScript(8179)));
        assertQuickCleanupRestoresOptionsKey(closing);
        assertTrue(contains(closing, Native950Packets.interfaceEvents(1477, 8, -1, -1, 254)));
        assertTrue(contains(closing, Native950Packets.varcLarge(2911, -1)));
        assertTrue(contains(closing, Native950Packets.runClientScript(8290, 1)));
        assertNoSceneClose(closing);
        settings.close();
        assertTrue(packets().isEmpty());
    }

    @Test public void repeatedTabClickAcknowledgesTheClientLoadingStateWithoutResettingControls() {
        settings.handle(button(1477, 8, -1, -1, 1));
        packets();
        for (int page : new int[] {1, 2, 3, 4, 5, 6}) {
            assertTrue(settings.handle(button(1477, 714, page * 4 - 1, -1, 1)));
            packets();
            assertTrue(settings.handle(button(1477, 714, page * 4 - 1, -1, 1)));
            List<Native950Packets.Packet> refreshed = packets();
            assertTrue(contains(refreshed, Native950Packets.varbitSmall(19001, page)));
            assertTrue(contains(refreshed, Native950Packets.hideInterface(1448, 3, page == 4)));
            if (page == 4) assertTrue(contains(refreshed, Native950Packets.hideInterface(1448, 5, false)));
            assertTrue(contains(refreshed, Native950Packets.hideInterface(1448, 1, true)));
            for (Native950Packets.Packet packet : refreshed) {
                assertNotEquals("A repeated tab must not recreate its controls", Native950Packets.openSub(0, 0, 0, true).type(), packet.type());
                assertNotEquals("A repeated tab must not retire its controls", Native950Packets.closeSub(0, 0).type(), packet.type());
            }
            assertScene();
        }
    }

    @Test public void invalidOrStaleTabsCannotOpenAHiddenPanel() {
        settings.handle(button(1477, 8, -1, -1, 1));
        packets();
        for (int slot : new int[] {-1, 0, 1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 27})
            assertFalse(settings.handle(button(1477, 714, slot, -1, 1)));
        for (int actor : new int[] {3, 7, 11, 15, 19, 23}) {
            assertFalse(settings.handle(button(1477, 714, actor, 0, 1)));
            assertFalse(settings.handle(button(1477, 714, actor, -1, 2)));
            assertFalse(settings.handle(button(1477, 713, actor, -1, 1)));
        }
        assertFalse(settings.handle(button(1477, 717, -1, -1, 1)));
        assertTrue(packets().isEmpty());
        settings.close();
        packets();
        for (int actor : new int[] {3, 7, 11, 15, 19, 23})
            assertFalse(settings.handle(button(1477, 714, actor, -1, 1)));
        assertFalse(settings.handle(button(1477, 717, 1, -1, 1)));
        assertFalse(settings.isOpen());
        assertScene();
        assertTrue(packets().isEmpty());
    }

    @Test public void newlyAvailablePagesReplaceTheirOwnerAndLeaveNoContentAfterClose() {
        for (int[] tab : new int[][] {{1, 365}, {3, 1444}, {5, 567}, {6, 365}}) {
            settings.handle(button(1431, 0, 7, -1, 1));
            packets();
            assertTrue(settings.handle(button(1477, 714, tab[0] * 4 - 1, -1, 1)));
            assertSimplePage(tab[1]);
            List<Native950Packets.Packet> opened = packets();
            assertTrue(contains(opened, Native950Packets.openSub(1448, 3, tab[1], true)));
            assertTrue(indexOf(opened, Native950Packets.varbitSmall(19001, tab[0]))
                    < indexOf(opened, Native950Packets.openSub(1448, 3, tab[1], true)));
            assertTrue(contains(opened, Native950Packets.hideInterface(1448, 3, false)));
            assertNoSceneClose(opened);
            settings.close();
            for (int id : new int[] {1448, 1426, 742, 429, 365, 1444, 567})
                assertFalse("Page must not survive its settings owner: " + id,
                        player.getInterfaceManager().containsInterface(id));
            assertScene();
            List<Native950Packets.Packet> closed = packets();
            assertTrue(contains(closed, Native950Packets.closeSub(1448, 3)));
            assertNoSceneClose(closed);
        }
    }

    @Test public void gameplayAndAccessibilityReinitializeTheirSharedInterfaceForTheNewPage() {
        settings.handle(button(1477, 8, -1, -1, 1));
        settings.handle(button(1477, 714, 3, -1, 1));
        packets();
        for (int page : new int[] {6, 1}) {
            assertTrue(settings.handle(button(1477, 714, page * 4 - 1, -1, 1)));
            assertSimplePage(365);
            List<Native950Packets.Packet> transition = packets();
            int close = indexOf(transition, Native950Packets.closeSub(1448, 3));
            int select = indexOf(transition, Native950Packets.varbitSmall(19001, page));
            int open = indexOf(transition, Native950Packets.openSub(1448, 3, 365, true));
            assertTrue("Shared interface must be retired, then select the new page before onLoad", close < select && select < open);
            if (page == 6) {
                int initialize = indexOf(transition, Native950Packets.runClientScript(20387));
                assertTrue("Initialize Accessibility selectors before onLoad captures them", select < initialize && initialize < open);
                assertTrue(indexOf(transition, Native950Packets.hideInterface(1477, 708, false)) < initialize);
            }
            assertNoSceneClose(transition);
        }
    }

    @Test public void audioToEachOtherPageRetiresBothAudioHostsAndRejectsLateMute() {
        settings.handle(button(1477, 8, -1, -1, 1));
        for (int page : new int[] {1, 2, 3, 5, 6}) {
            settings.handle(button(1477, 714, 15, -1, 1));
            packets();
            assertTrue(settings.handle(button(1477, 714, page * 4 - 1, -1, 1)));
            List<Native950Packets.Packet> transition = packets();
            assertTrue(contains(transition, Native950Packets.closeSub(1448, 5)));
            assertTrue(contains(transition, Native950Packets.closeSub(1448, 3)));
            assertFalse(player.getInterfaceManager().containsInterface(429));
            if (page != 2) assertFalse(player.getInterfaceManager().containsInterface(1426));
            assertFalse(settings.handle(button(429, 7, -1, -1, 1)));
            assertFalse(settings.handle(button(429, 15, -1, -1, 1)));
            assertTrue(packets().isEmpty());
            assertScene();
            assertNoSceneClose(transition);
        }
    }

    @Test public void escapeTogglesTheSameOwnerAndGearOpensAtGraphics() {
        settings.close();
        assertTrue(packets().isEmpty());
        settings.handle(button(1431, 0, 7, -1, 1));
        settings.handle(button(1477, 714, 15, -1, 1));
        settings.handle(button(1477, 8, -1, -1, 1));
        assertFalse(settings.isOpen());
        settings.handle(button(1477, 8, -1, -1, 1));
        assertTrue(settings.isOpen());
        assertTrue(player.getInterfaceManager().containsInterface(742));
        assertFalse(player.getInterfaceManager().containsInterface(429));
        assertEquals(1, verifications);
        assertScene();
        assertNoSceneClose(packets());
    }

    @Test public void muteCheckboxesReuseNativeSettersOnlyForTheVisibleAudioPage() {
        settings.handle(button(1477, 8, -1, -1, 1));
        assertFalse(settings.handle(button(429, 7, -1, -1, 1)));
        settings.handle(button(1477, 714, 15, -1, 1));
        packets();
        assertTrue(settings.handle(button(429, 7, -1, -1, 1)));
        List<Native950Packets.Packet> global = packets();
        assertEquals(1, global.size());
        assertTrue(contains(global, Native950Packets.runClientScript(9287)));
        for (int channelIndex = 0; channelIndex < 4; channelIndex++) {
            assertTrue(settings.handle(button(429, 15 + 17 * channelIndex, -1, -1, 1)));
            List<Native950Packets.Packet> toggle = packets();
            assertEquals(2, toggle.size());
            assertTrue(contains(toggle, Native950Packets.runClientScript(5523, channelIndex)));
            assertTrue(contains(toggle, Native950Packets.runClientScript(13818)));
        }
        // The global text already runs9287 locally; do not toggle it a second time.
        assertFalse(settings.handle(button(429, 6, -1, -1, 1)));
        assertFalse(settings.handle(button(429, 7, 0, -1, 1)));
        assertFalse(settings.handle(button(429, 7, -1, 0, 1)));
        assertFalse(settings.handle(button(429, 7, -1, -1, 2)));
        assertFalse(settings.handle(button(429, 81, -1, -1, 1)));
        assertTrue(packets().isEmpty());
        settings.close();
        packets();
        assertFalse(settings.handle(button(429, 7, -1, -1, 1)));
        assertTrue(packets().isEmpty());
    }

    @Test public void audioCloseRemovesTheHiddenPlaceholderAndBothHosts() {
        settings.handle(button(1477, 8, -1, -1, 1));
        settings.handle(button(1477, 714, 15, -1, 1));
        packets();
        settings.close();
        List<Native950Packets.Packet> closing = packets();
        assertTrue(contains(closing, Native950Packets.closeSub(1448, 5)));
        assertTrue(contains(closing, Native950Packets.closeSub(1448, 3)));
        for (int id : new int[] {1448, 1426, 742, 429})
            assertFalse(player.getInterfaceManager().containsInterface(id));
        assertScene();
        assertNoSceneClose(closing);
    }

    @Test public void repeatedGearFocusesGraphicsRegardlessOfPriorNativeCloseTiming() {
        settings.handle(button(1431, 0, 7, -1, 1));
        settings.handle(button(1477, 714, 15, -1, 1));
        packets();
        settings.handle(button(1431, 0, 7, -1, 1));
        assertTrue(settings.isOpen());
        assertTrue(player.getInterfaceManager().containsInterface(742));
        assertFalse(player.getInterfaceManager().containsInterface(429));
        List<Native950Packets.Packet> focused = packets();
        assertTrue(contains(focused, Native950Packets.runClientScript(8179)));
        assertQuickCleanupRestoresOptionsKey(focused);
        assertTrue(contains(focused, Native950Packets.interfaceEvents(1477, 8, -1, -1, 252)));
        // The native hook may already have sent CLOSE_MODAL before the gear event.
        settings.close();
        packets();
        settings.handle(button(1431, 0, 7, -1, 1));
        assertTrue(settings.isOpen());
        assertTrue(player.getInterfaceManager().containsInterface(742));
        assertScene();
        assertNoSceneClose(packets());
    }

    @Test public void escapeKeepsItsNativeHookButSuppressesOnlyTheDuplicateGenericOpenWhileVisible() {
        settings.handle(button(1477, 8, -1, -1, 1));
        List<Native950Packets.Packet> open = packets();
        assertTrue(contains(open, Native950Packets.runClientScript(8180, 1, 1)));
        assertFalse(contains(open, Native950Packets.runClientScript(8180, 0, 1)));
        assertTrue(contains(open, Native950Packets.interfaceEvents(1477, 8, -1, -1, 252)));
        assertFalse(contains(open, Native950Packets.interfaceEvents(1477, 8, -1, -1, 254)));
        // 8181's existing key hook emits this close actor. Its original root8
        // action must be suppressed at the client, not swallowed later by time.
        settings.handle(button(1477, 717, 1, -1, 1));
        assertFalse(settings.isOpen());
        List<Native950Packets.Packet> close = packets();
        assertTrue(contains(close, Native950Packets.interfaceEvents(1477, 8, -1, -1, 254)));
        assertFalse(contains(close, Native950Packets.interfaceEvents(1477, 8, -1, -1, 252)));
        // Those complete masks retain native operations2..7 in both states;
        // only bit1 (operation1) changes. The next real key can reopen normally.
        settings.handle(button(1477, 8, -1, -1, 1));
        assertTrue(settings.isOpen());
        assertScene();
        assertNoSceneClose(packets());
    }

    @Test public void unportedCombatModeRecoversOnlyItsVisiblePendingCheckbox() {
        settings.handle(button(1477, 8, -1, -1, 1));
        assertFalse(settings.handle(button(365, 19, 10240, -1, 1)));
        settings.handle(button(1477, 714, 3, -1, 1));
        List<Native950Packets.Packet> opened = packets();
        assertTrue(contains(opened, Native950Packets.interfaceEvents(365, 19, 10240, 10242, 2)));
        assertTrue(contains(opened, Native950Packets.interfaceEvents(365, 19, 15872, 15872, 2)));
        for (int slot : new int[] {10240, 10241}) {
            assertTrue(settings.handle(button(365, 19, slot, -1, 1)));
            List<Native950Packets.Packet> response = packets();
            assertEquals(3, response.size());
            assertTrue(contains(response, Native950Packets.varbitSmall(21682,slot == 10241 ? 1 : 0)));
            assertTrue(contains(response, Native950Packets.runClientScript(2929)));
        }
        for (int slot : new int[] {10242, 15872}) {
            assertTrue(settings.handle(button(365, 19, slot, -1, 1)));
            List<Native950Packets.Packet> response = packets();
            assertEquals(2, response.size());
            assertTrue(contains(response, Native950Packets.runClientScript(2929)));
            assertTrue(contains(response, Native950Packets.gameMessage(0,
                    "This combat mode is not available yet on the local server.")));
        }
        // Nearby native preferences must not receive a false unsupported reply.
        for (int slot : new int[] {-1, 0, 10239, 10243, 15871, 15873, 65535})
            assertFalse(settings.handle(button(365, 19, slot, -1, 1)));
        assertFalse(settings.handle(button(365, 19, 10240, 0, 1)));
        assertFalse(settings.handle(button(365, 19, 10240, -1, 2)));
        assertFalse(settings.handle(button(365, 18, 10240, -1, 1)));
        assertTrue(packets().isEmpty());
        settings.handle(button(1477, 714, 23, -1, 1));
        List<Native950Packets.Packet> leaving = packets();
        assertTrue(contains(leaving, Native950Packets.interfaceEvents(365, 19, 10240, 10242, 0)));
        assertTrue(contains(leaving, Native950Packets.interfaceEvents(365, 19, 15872, 15872, 0)));
        assertFalse(settings.handle(button(365, 19, 10240, -1, 1)));
        assertTrue(packets().isEmpty());
        settings.handle(button(1477, 714, 3, -1, 1));
        packets();
        settings.close();
        List<Native950Packets.Packet> closing = packets();
        assertTrue(contains(closing, Native950Packets.interfaceEvents(365, 19, 10240, 10242, 0)));
        assertTrue(contains(closing, Native950Packets.interfaceEvents(365, 19, 15872, 15872, 0)));
        assertFalse(settings.handle(button(365, 19, 10240, -1, 1)));
        assertTrue(packets().isEmpty());
        assertScene();
    }

    private void assertScene() {
        assertEquals((1477 << 16) | 30, player.getInterfaceManager().getInterfaceParentId(1482));
    }

    private void assertSimplePage(int interfaceId) {
        assertTrue(settings.isOpen());
        assertEquals((1477 << 16) | 715, player.getInterfaceManager().getInterfaceParentId(1448));
        assertEquals((1448 << 16) | 3, player.getInterfaceManager().getInterfaceParentId(interfaceId));
        for (int other : new int[] {365, 1444, 567, 1426, 742, 429})
            if (other != interfaceId) assertFalse("Stale settings interface " + other,
                    player.getInterfaceManager().containsInterface(other));
        assertScene();
    }

    private static int indexOf(List<Native950Packets.Packet> packets, Native950Packets.Packet expected) {
        for (int i = 0; i < packets.size(); i++) {
            Native950Packets.Packet packet = packets.get(i);
            if (packet.type() == expected.type() && Arrays.equals(packet.payload(), expected.payload())) return i;
        }
        fail("Missing expected packet " + expected.type());
        return -1;
    }

    private static void assertNoSceneClose(List<Native950Packets.Packet> packets) {
        assertFalse(contains(packets, Native950Packets.closeSub(1477, 30)));
    }

    private static void assertQuickCleanupRestoresOptionsKey(List<Native950Packets.Packet> packets) {
        Native950Packets.Packet cleanup = Native950Packets.runClientScript(8179);
        Native950Packets.Packet restore = Native950Packets.runClientScript(8180, 1, 1);
        int found = 0;
        for (int i = 0; i < packets.size(); i++) {
            Native950Packets.Packet packet = packets.get(i);
            if (packet.type() != cleanup.type() || !Arrays.equals(packet.payload(), cleanup.payload())) continue;
            assertTrue("Options key must be restored after quick cleanup", i + 1 < packets.size());
            Native950Packets.Packet following = packets.get(i + 1);
            assertEquals(restore.type(), following.type());
            assertArrayEquals(restore.payload(), following.payload());
            found++;
        }
        assertEquals(1, found);
    }

    private List<Native950Packets.Packet> packets() {
        channel.flush();
        List<Native950Packets.Packet> result = new ArrayList<>();
        Object packet;
        while ((packet = channel.readOutbound()) != null) result.add((Native950Packets.Packet) packet);
        return result;
    }

    private static boolean contains(List<Native950Packets.Packet> packets, Native950Packets.Packet expected) {
        for (Native950Packets.Packet packet : packets)
            if (packet.type() == expected.type() && Arrays.equals(packet.payload(), expected.payload())) return true;
        return false;
    }

    /**
     * One IF_BUTTON frame as a 950 client sends it.
     *
     * Options 1..10 are opcodes {18, 122, 89, 100, 81, 126, 49, 66, 31, 59} and the body is nine
     * bytes: b0..b2 the item id as a big-endian u24 (0xFFFFFF absent), b3..b6 the component hash
     * under the intv2 permutation [h>>>16, h>>>24, h, h>>>8], b7..b8 the dynamic slot big-endian.
     *
     * 947 sent opcode 96 with eight bytes - item BE u16, component BE u16, panel BE u16, slot
     * BE u16 - so neither the opcode, the width nor the field order carries over. The descriptor
     * tables corroborate the remap: 950 opcode 96 is a variable-length row and 950 opcode 18 is
     * the nine-byte one.
     */
    private static Native950Actions.InterfaceAction button(int panel, int component, int slot, int item, int option) {
        int hash = (panel << 16) | component;
        return (Native950Actions.InterfaceAction) Native950Actions.decode(option == 1 ? 18 : 122,
                new byte[] {(byte) (item >>> 16), (byte) (item >>> 8), (byte) item,
                        (byte) (hash >>> 16), (byte) (hash >>> 24), (byte) hash, (byte) (hash >>> 8),
                        (byte) (slot >>> 8), (byte) slot});
    }
}
