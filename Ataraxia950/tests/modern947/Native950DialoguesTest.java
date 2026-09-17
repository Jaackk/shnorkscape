package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/** Exact rendered-response ownership and independent packet fixtures for the shared dialogue bridge. */
public class Native950DialoguesTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Dialogues dialogues;
    private int verifications;
    private int cancellations;

    @Before public void setup() {
        channel = new EmbeddedChannel();
        player = Player.createNative950("dialogue-test", new WorldTile(3217, 3258, 0), channel);
        dialogues = new Native950Dialogues(player, channel, () -> cancellations++, () -> verifications++);
        player.setNative950Dialogues(dialogues);
    }

    @After public void cleanup() { channel.finishAndReleaseAll(); }

    @Test public void npcSpeechUsesVerifiedHostPortraitAndTheActualContinueTarget() {
        Dialogue.sendEntityDialogue(player, Dialogue.IS_NPC, "Banker", 494, 9827, "Good day.", "How may I help?");
        assertEquals(1, verifications);
        assertEquals(1, cancellations);
        assertTrue(player.getInterfaceManager().containsChatBoxInter());
        assertTrue(player.getInterfaceManager().containsChatBoxInterface(1184));
        assertEquals((1477 << 16) | 750, player.getInterfaceManager().getInterfaceParentId(1184));
        List<Native950Packets.Packet> packets = packets();
        assertEquals(9, packets.size());
        packet(Native950Packets.openSub(1477, 750, 1184, false), packets.get(0));
        packet(Native950Packets.hideInterface(1477, 747, false), packets.get(1));
        packet(Native950Packets.runClientScript(1364), packets.get(2));
        packet(Native950Packets.interfaceText(1184, 4, "Banker"), packets.get(3));
        packet(Native950Packets.interfaceText(1184, 10, "Good day. How may I help?"), packets.get(4));
        packet(Native950Packets.runClientScript(2374, (1184 << 16) | 8, 494), packets.get(5));
        packet(Native950Packets.runClientScript(16429, 9827, (1184 << 16) | 8), packets.get(6));
        assertFalse(dialogues.acceptsResponse(1184, 11, -1)); // Graphic is not the pause target.
        assertFalse(dialogues.acceptsResponse(1184, 15, 0));
        assertFalse(dialogues.acceptsResponse(1191, 15, -1));
        assertTrue(dialogues.consumeResponse(1184, 15, -1));
        assertFalse(dialogues.consumeResponse(1184, 15, -1));
    }

    @Test public void playerSpeechUsesNativeLocalHeadAndClearsTheAnimationWhenRequested() {
        Dialogue.sendEntityDialogue(player, 0, "My player", -1, -1, "Hello.");
        List<Native950Packets.Packet> packets = packets();
        assertEquals(9, packets.size());
        assertEquals(ServerPacket.IF_SETPLAYERHEAD, packets.get(5).type());
        // Native parser 0x14010857A..0x1401085A1, independently assembled for1191:8.
        assertArrayEquals(new byte[] {4, (byte) 0xa7, 0, 8}, packets.get(5).payload());
        packet(Native950Packets.runClientScript(16429, -1, (1191 << 16) | 8), packets.get(6));
        assertTrue(dialogues.acceptsResponse(1191, 15, -1));
    }

    @Test public void playerHeadWriterPreservesEveryHashByteAndRejectsOverflow() {
        Native950Packets.Packet packet = Native950Packets.interfacePlayerHead(0x1234, 0x5678);
        assertEquals(38, packet.type().opcode());
        assertEquals(4, packet.type().size());
        assertArrayEquals(new byte[] {0x12, 0x34, 0x56, 0x78}, packet.payload());
        try { Native950Packets.interfacePlayerHead(65536, 0); fail("overflow accepted"); }
        catch (IllegalArgumentException expected) { }
        try { Native950Packets.interfacePlayerHead(1, -1); fail("negative component accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    @Test public void optionsWriteNaturalArgumentOrderAndAcceptOnlyVisibleRowsOnce() {
        Dialogue.sendOptionsDialogueStatic(player, "Choose", "Bank", "Never mind");
        List<Native950Packets.Packet> packets = packets();
        assertEquals(4, packets.size());
        packet(Native950Packets.runClientScript(5589, 2, "Choose", "Bank", "Never mind", "", "", ""), packets.get(3));
        assertTrue(dialogues.acceptsResponse(1188, 8, -1));
        assertTrue(dialogues.acceptsResponse(1188, 13, -1));
        assertFalse(dialogues.acceptsResponse(1188, 18, -1));
        assertFalse(dialogues.acceptsResponse(1188, 9, -1));
        assertFalse(dialogues.acceptsResponse(1188, 4, -1));
        assertFalse(dialogues.acceptsResponse(1188, 8, 1));
        assertTrue(dialogues.consumeResponse(1188, 13, -1));
        assertFalse(dialogues.consumeResponse(1188, 8, -1));
        dialogues.options("Again", "One", "Two", "Three", "Four", "Five");
        assertTrue(dialogues.acceptsResponse(1188, 28, -1));
        assertFalse(dialogues.acceptsResponse(1188, 33, -1));
    }

    @Test public void noContinueHidesBothTheGraphicAndItsClickableSibling() {
        dialogues.speech(false, 494, 9827, "Banker", false, "Please wait.");
        List<Native950Packets.Packet> packets = packets();
        packet(Native950Packets.hideInterface(1184, 11, true), packets.get(7));
        packet(Native950Packets.hideInterface(1184, 15, true), packets.get(8));
        assertFalse(dialogues.consumeResponse(1184, 15, -1));
        dialogues.message(false, "Waiting");
        packets = packets();
        packet(Native950Packets.hideInterface(1186, 4, true), packets.get(4));
        packet(Native950Packets.hideInterface(1186, 8, true), packets.get(5));
        assertFalse(dialogues.consumeResponse(1186, 8, -1));
        Dialogue.sendDialogue(player, "Ready");
        assertTrue(dialogues.consumeResponse(1186, 8, -1));
    }

    @Test public void replacementAndCloseKeepExistingManagerBookkeepingAccurateWhenLocked() {
        dialogues.speech(false, 494, 9827, "Banker", true, "Hello");
        dialogues.options("Choose", "Continue");
        assertFalse(player.getInterfaceManager().containsInterface(1184));
        assertTrue(player.getInterfaceManager().containsInterface(1188));
        assertFalse(dialogues.acceptsResponse(1184, 15, -1));
        packets();
        player.closeInterfaceLocked = true;
        player.getInterfaceManager().closeChatBoxInterface();
        assertFalse(dialogues.isOpen());
        assertEquals(-1, dialogues.interfaceId());
        assertFalse(player.getInterfaceManager().containsChatBoxInterface());
        assertFalse(player.getInterfaceManager().containsInterface(1188));
        List<Native950Packets.Packet> packets = packets();
        assertEquals(2, packets.size());
        packet(Native950Packets.closeSub(1477, 750), packets.get(0));
        packet(Native950Packets.runClientScript(1364), packets.get(1));
        dialogues.close();
        assertTrue(packets().isEmpty());
        assertFalse(dialogues.consumeResponse(1188, 8, -1));
    }

    @Test public void cacheMismatchDoesNotCancelInputsOrRenderAnything() {
        dialogues = new Native950Dialogues(player, channel, () -> cancellations++,
                () -> { throw new IllegalStateException("cache changed"); });
        player.setNative950Dialogues(dialogues);
        try { dialogues.options("Choose", "Continue"); fail("mismatched cache accepted"); }
        catch (IllegalStateException expected) { assertEquals("cache changed", expected.getMessage()); }
        assertEquals(0, cancellations);
        assertFalse(dialogues.isOpen());
        assertFalse(player.getInterfaceManager().containsInterface(1188));
        assertTrue(packets().isEmpty());
    }

    @Test public void malformedOptionsCannotReplaceAnExistingPage() {
        dialogues.options("Choose", "One");
        packets();
        try { dialogues.options("Choose"); fail("empty options accepted"); }
        catch (IllegalArgumentException expected) { }
        try { dialogues.options("Choose", ""); fail("empty row accepted"); }
        catch (IllegalArgumentException expected) { }
        try { dialogues.open(1185); fail("unverified interface accepted"); }
        catch (IllegalArgumentException expected) { }
        assertEquals(1, cancellations);
        assertEquals(1, verifications);
        assertTrue(dialogues.acceptsResponse(1188, 8, -1));
        assertTrue(packets().isEmpty());
    }

    @Test public void numericCancellationUsesTheOwnedCallbackAndCorrectSiblingBookkeeping() {
        assertFalse(player.getInterfaceManager().containsInputTextInterface());
        player.getInterfaceManager().registerNativeOpen(1418, 1477, 749);
        player.getInterfaceManager().registerNativeOpen(1469, 1418, 2);
        assertTrue(player.getInterfaceManager().containsInputTextInterface());
        player.getInterfaceManager().removeInputTextInterface();
        assertEquals(1, cancellations);
        assertTrue(packets().isEmpty());
        try { player.getInterfaceManager().sendInputTextInterface(); fail("legacy input opener accepted"); }
        catch (UnsupportedOperationException expected) { }
        player.getInterfaceManager().unregisterNativeOpen(1469);
        assertFalse(player.getInterfaceManager().containsInputTextInterface());
    }

    @Test public void dialogueRestoresTheWrapperAfterAnEarlierQuantityEscape() {
        Native950QuantityInput input = quantityPrompt();
        assertFalse(wrapperHidden(packets(), true));
        dialogues.cancelInput();
        assertFalse(input.active());
        assertTrue(wrapperHidden(packets(), false));

        // The user's failing sequence: Escape quantity, close bank, then Talk.
        // Cancellation is already complete, so opening must restore visibility itself.
        dialogues.speech(false, 494, 9827, "Banker", true, "Hello again.");
        assertFalse(wrapperHidden(packets(), true));
        assertTrue(dialogues.consumeResponse(1184, 15, -1));
    }

    @Test public void openingDialogueRestoresTheWrapperAfterItsCancellationCallback() {
        Native950QuantityInput input = quantityPrompt();
        packets();
        dialogues.options("Choose", "Continue");
        assertFalse(input.active());
        List<Native950Packets.Packet> transition = packets();
        // Actual quantity teardown hides747 before the new page can show it.
        packet(Native950Packets.hideInterface(1477, 747, true), transition.get(3));
        packet(Native950Packets.openSub(1477, 750, 1188, false), transition.get(5));
        packet(Native950Packets.hideInterface(1477, 747, false), transition.get(6));
        packet(Native950Packets.runClientScript(1364), transition.get(7));
        assertFalse(wrapperHidden(transition, false));
        assertTrue(dialogues.consumeResponse(1188, 8, -1));
    }

    private Native950QuantityInput quantityPrompt() {
        Native950QuantityInput input = new Native950QuantityInput(() -> { });
        Native950ItemCatalog catalog = new Native950ItemCatalog(Collections.singletonList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[]{"Add to pouch"})));
        Native950Containers containers = new Native950Containers(player, catalog);
        player.getBank().bankTabs = new Item[][]{new Item[]{new Item(995, 100)}};
        assertTrue(input.beginWithdraw(1, 0, 995, containers.bankSnapshot()));
        for (Native950Packets.Packet packet : input.promptPackets()) channel.write(packet);
        dialogues = new Native950Dialogues(player, channel, () -> {
            if (!input.active()) return;
            input.cancel();
            for (Native950Packets.Packet packet : input.cancelPackets()) channel.write(packet);
        }, () -> verifications++);
        player.setNative950Dialogues(dialogues);
        return input;
    }

    /** Decode the actual shared-wrapper hide writes; this does not emulate client scripts. */
    private static boolean wrapperHidden(List<Native950Packets.Packet> packets, boolean hidden) {
        for (Native950Packets.Packet packet : packets) {
            if (packet.type() != ServerPacket.IF_SETHIDE) continue;
            // 950 IF_SETHIDE: b0..b3 hash under intv1 (b0=h>>>8, b1=h, b2=h>>>24, b3=h>>>16),
            // then the flag as a plain byte. 947 led with a +128-biased flag and a little-endian
            // hash, so both the field order and the flag transform moved.
            byte[] body = packet.payload();
            int hash = ((body[2] & 255) << 24) | ((body[3] & 255) << 16)
                    | ((body[0] & 255) << 8) | (body[1] & 255);
            if (hash == ((1477 << 16) | 747)) hidden = body[4] != 0;
        }
        return hidden;
    }

    private List<Native950Packets.Packet> packets() {
        channel.flush();
        List<Native950Packets.Packet> result = new ArrayList<Native950Packets.Packet>();
        Object packet;
        while ((packet = channel.readOutbound()) != null) {
            assertTrue("Unexpected outbound " + packet.getClass(), packet instanceof Native950Packets.Packet);
            result.add((Native950Packets.Packet) packet);
        }
        return result;
    }

    private static void packet(Native950Packets.Packet expected, Native950Packets.Packet actual) {
        assertEquals(expected.type(), actual.type());
        assertArrayEquals(expected.payload(), actual.payload());
    }
}
