package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/** Exercises decoded CLOSE_MODAL through the actual interaction state machine. */
public class Native950ModalCloseTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Interactions interactions;

    @Before public void createSession() {
        channel = new EmbeddedChannel();
        player = Player.createNative950("modal-close-test", new WorldTile(3217, 3258, 0), channel);
        player.setActive(true);
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] amounts = new int[11];
        amounts[1] = 1;
        Native950Content content = new Native950Content(items, new Native950Content.BankUi(
                517, 201, 15, 317, 39, amounts, amounts, Collections.emptyList(),
                Collections.singletonList(Native950Packets.closeSub(1477, 369))));
        interactions = new Native950Interactions(player, channel, content);
    }

    @After public void closeChannel() { channel.finishAndReleaseAll(); }

    @Test public void clientCloseClearsBothBankStatesAndCannotReopenOnNextTick() throws Exception {
        openBankState();
        // A server-side close lock cannot preserve a modal the client already closed.
        player.closeInterfaceLocked = true;
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        assertFalse(interactions.snapshot().bankOpen);
        assertFalse(player.getInterfaceManager().containsInterface(517));
        assertEquals(0, interactions.snapshot().unhandledActions);
        channel.flush();
        assertNotNull(channel.readOutbound());
        interactions.afterMovement();
        channel.flush();
        assertFalse(interactions.snapshot().bankOpen);
        assertNull(channel.readOutbound());
    }

    @Test public void staleWithdrawalAfterClientCloseCannotMoveItems() throws Exception {
        openBankState();
        player.getBank().bankTabs[0] = new Item[] {new Item(995, 50)};
        Native950Interactions.State before = interactions.snapshot();
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        interactions.handle(button(517, 201, 0, 995));
        Native950Interactions.State after = interactions.snapshot();
        assertEquals(before.transactions, after.transactions);
        assertEquals(before.rejectedActions + 1, after.rejectedActions);
        assertArrayEquals(before.inventory.ids, after.inventory.ids);
        assertArrayEquals(before.inventory.amounts, after.inventory.amounts);
        assertArrayEquals(before.bank.ids, after.bank.ids);
        assertArrayEquals(before.bank.amounts, after.bank.amounts);
        assertEquals(0, after.routerDispatches);
    }

    @Test public void repeatedCloseWithoutModalIsHarmlessAndDoesNotCancelOrdinaryWalking() {
        player.getWalkSteps().add(new Object[] {4, 3218, 3258, false});
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        assertEquals(1, player.getWalkSteps().size());
        assertFalse(interactions.snapshot().bankOpen);
        assertEquals(0, interactions.snapshot().unhandledActions);
        assertEquals(0, interactions.snapshot().rejectedActions);
        assertNull(channel.readOutbound());
    }

    @Test public void unboundInterfaceClickIsCountedWithoutAPlayerErrorMessage() {
        interactions.handle(button(1431, 1, -1, -1));
        channel.flush();
        assertEquals(1, interactions.snapshot().unmatchedPairs);
        assertEquals(1, interactions.snapshot().rejectedActions);
        assertEquals(0, interactions.snapshot().routerDispatches);
        assertNull(channel.readOutbound());
    }

    @Test public void closeCancelsPendingBankerApproachEvenWhenPlayerCannotAct() throws Exception {
        set("pendingNpcOption", 2);
        player.getWalkSteps().add(new Object[] {4, 3218, 3258, false});
        player.setActive(false);
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        assertTrue(player.getWalkSteps().isEmpty());
        assertEquals(0, field("pendingNpcOption").getInt(interactions));
        assertEquals(0, interactions.snapshot().rejectedActions);
        assertEquals(0, interactions.snapshot().unhandledActions);
    }

    private void openBankState() throws Exception {
        set("bankOpen", true);
        player.getInterfaceManager().registerNativeOpen(517, 1477, 369);
    }

    private void set(String name, Object value) throws Exception { field(name).set(interactions, value); }
    private Field field(String name) throws Exception {
        Field field = Native950Interactions.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /**
     * IF_BUTTON option 1 is 950 opcode 18 (947: 96) and its body grew a byte. Layout from the 950
     * writer 0x1401a9980: item as a big-endian u24, then the component hash as intv2
     * [h&gt;&gt;&gt;16, h&gt;&gt;&gt;24, h, h&gt;&gt;&gt;8], then the slot as a big-endian u16.
     * The 947 helper wrote eight bytes: item ushort, component ushort, panel ushort, slot ushort.
     */
    private static Native950Actions.Action button(int panel, int component, int slot, int item) {
        int hash = (panel << 16) | (component & 0xffff);
        return Native950Actions.decode(18, new byte[] {
                (byte) (item >>> 16), (byte) (item >>> 8), (byte) item,
                (byte) (hash >>> 16), (byte) (hash >>> 24), (byte) hash, (byte) (hash >>> 8),
                (byte) (slot >>> 8), (byte) slot});
    }
}
