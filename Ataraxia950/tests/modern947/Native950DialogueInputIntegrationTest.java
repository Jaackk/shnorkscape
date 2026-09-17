package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.network.protocol.modern950.Native950Actions;
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

/** Exercises decoded replies through the adapter and the real 910 dialogue manager. */
public class Native950DialogueInputIntegrationTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Interactions interactions;
    private Native950Dialogues dialogues;
    private Native950QuantityInput quantity;

    @Before public void setup() throws Exception {
        channel = new EmbeddedChannel();
        player = Player.createNative950("dialogue-input-test", new WorldTile(3217, 3258, 0), channel);
        player.setActive(true);
        interactions = new Native950Interactions(player, channel, content(), null, null, () -> {}, () -> {});
        dialogues = player.getNative950Dialogues();
        quantity = (Native950QuantityInput) field(interactions, "quantityInput");
    }

    @After public void cleanup() { channel.finishAndReleaseAll(); }

    @Test public void visibleReplyAdvancesTheExistingManagerAndStalePageCannotChoose() {
        ProbeDialogue dialogue = start();
        assertSame(dialogue, player.getDialogueManager().getDialogue());
        assertTrue(dialogues.isOpen(1186));
        interactions.handle(reply(1186, 4, -1)); // Graphic, not the verified continue target.
        interactions.handle(reply(1186, 8, 0)); // Static dialogue has no item slot.
        assertEquals(0, dialogue.runs);
        interactions.handle(reply(1186, 8, -1));
        assertEquals(1, dialogue.runs);
        assertTrue(dialogues.isOpen(1188));
        interactions.handle(reply(1186, 8, -1)); // Duplicate old continue after page changed.
        interactions.handle(reply(1188, 18, -1)); // Hidden third row.
        assertEquals(1, dialogue.runs);
        interactions.handle(reply(1188, 13, -1));
        assertEquals(2, dialogue.runs);
        assertEquals(13, dialogue.chosen);
        assertFalse(dialogues.isOpen());
        assertFalse(player.getDialogueManager().hasDialogue());
        interactions.handle(reply(1188, 13, -1));
        assertEquals(2, dialogue.runs);
        assertEquals(0, interactions.snapshot().unhandledActions);
    }

    @Test public void aConsumedPageCannotRunAgainWithoutBeingRenderedAgain() {
        final int[] runs = {0};
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override public void start() { sendDialogue("One response only."); }
            @Override public void run(int panel, int component) { runs[0]++; }
            @Override public void finish() {}
        });
        interactions.handle(reply(1186, 8, -1));
        interactions.handle(reply(1186, 8, -1));
        assertEquals(1, runs[0]);
        assertTrue(player.getDialogueManager().hasDialogue());
    }

    @Test public void closeModalRetiresDialogueEvenWhilePlayerAndLegacyCloseAreLocked() {
        ProbeDialogue dialogue = start();
        player.lock(10);
        player.closeInterfaceLocked = true;
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        assertClosed();
        player.unlock();
        interactions.handle(reply(1186, 8, -1));
        assertEquals(0, dialogue.runs);
        assertEquals(1, dialogue.finishes);
    }

    @Test public void walkingRetiresDialogueAndLateReplyCannotReopenIt() {
        ProbeDialogue dialogue = start();
        interactions.walking();
        assertClosed();
        interactions.handle(reply(1186, 8, -1));
        assertEquals(0, dialogue.runs);
        assertFalse(interactions.snapshot().bankOpen);
    }

    @Test public void disconnectCleanupRetiresBothManagerAndPendingQuantity() throws Exception {
        ProbeDialogue dialogue = start();
        interactions.close();
        assertClosed();
        assertEquals(1, dialogue.finishes);
        beginQuantity();
        interactions.close();
        assertFalse(quantity.active());
        interactions.handle(count(7));
        assertEquals(0, interactions.snapshot().transactions);
    }

    @Test public void throwingLegacyFinishCannotAbortOrRepeatNativeTeardown() {
        final int[] finishes = {0};
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override public void start() { sendDialogue("This legacy dialogue has a broken finish callback."); }
            @Override public void run(int panel, int component) {}
            @Override public void finish() {
                finishes[0]++;
                throw new IllegalStateException("deliberate legacy teardown failure");
            }
        });
        interactions.close();
        assertClosed();
        assertEquals(1, finishes[0]);
        assertEquals(1, interactions.router().handlerFailures());
        interactions.close();
        assertEquals(1, finishes[0]);
        assertEquals(1, interactions.router().handlerFailures());
    }

    @Test public void openingDialogueCancelsQuantityOwnershipBeforeAReplyCanArrive() throws Exception {
        beginQuantity();
        start();
        assertFalse(quantity.active());
        interactions.handle(count(7));
        assertEquals(0, interactions.snapshot().transactions);
        assertEquals(1000, total(interactions.snapshot().inventory, 995));
        assertEquals(0, interactions.snapshot().unhandledActions);
    }

    @Test public void walkingAndCloseModalEachCancelPendingQuantity() throws Exception {
        beginQuantity();
        interactions.walking();
        assertFalse(quantity.active());
        beginQuantity();
        player.closeInterfaceLocked = true;
        player.setActive(false);
        interactions.handle(Native950Actions.decode(5, new byte[0]));
        assertFalse(quantity.active());
        player.setActive(true);
        interactions.handle(count(7));
        assertEquals(0, interactions.snapshot().transactions);
    }

    @Test public void inputEscapeRetiresOnlyItsFrameAndPreservesBankBookkeeping() throws Exception {
        beginQuantity();
        set(interactions, "bankOpen", true);
        player.getInterfaceManager().registerNativeOpen(517, 1477, 369);
        interactions.handle(Native950Actions.decode(11, new byte[0]));
        assertFalse(quantity.active());
        assertFalse(player.getInterfaceManager().containsInterface(1418));
        assertFalse(player.getInterfaceManager().containsInterface(1469));
        assertTrue(interactions.snapshot().bankOpen);
        assertTrue(player.getInterfaceManager().containsInterface(517));
        interactions.handle(count(7));
        assertEquals(0, interactions.snapshot().transactions);
        assertEquals(0, interactions.snapshot().unhandledActions);
    }

    @Test public void countWithoutAnOwnedPromptNeverUsesAnOldLegacyCallback() {
        Object staleCallback = new Object();
        player.getTemporaryAttributtes().put("pluginInteger", staleCallback);
        for (long value : new long[] {7, 0, -1, 2147483648L, Long.MAX_VALUE})
            interactions.handle(count(value));
        assertEquals(0, interactions.snapshot().transactions);
        assertEquals(1000, total(interactions.snapshot().inventory, 995));
        assertEquals(0, interactions.snapshot().unhandledActions);
    }

    private ProbeDialogue start() {
        ProbeDialogue result = new ProbeDialogue();
        player.getDialogueManager().startDialogue(result);
        return result;
    }

    private void assertClosed() {
        assertFalse(dialogues.isOpen());
        assertFalse(player.getDialogueManager().hasDialogue());
        assertFalse(player.getInterfaceManager().containsInterface(1186));
        assertFalse(player.getInterfaceManager().containsInterface(1188));
    }

    private void beginQuantity() throws Exception {
        // This fixture acquires ownership only. Real reach, stock and Bank.withdrawItem
        // are exercised by Native950DialogueInputSmoke against the paired cache.
        Constructor<Native950Containers.Snapshot> ctor = Native950Containers.Snapshot.class
                .getDeclaredConstructor(Item[].class, int.class);
        ctor.setAccessible(true);
        Native950Containers.Snapshot bank = ctor.newInstance(new Item[] {new Item(995, 20)}, 1);
        assertTrue(quantity.beginWithdraw(1L, 0, 995, bank));
        set(interactions, "quantityPromptVisible", true);
        player.getInterfaceManager().registerNativeOpen(1418, 1477, 749);
        player.getInterfaceManager().registerNativeOpen(1469, 1418, 2);
    }

    private static final class ProbeDialogue extends Dialogue {
        int runs, finishes, chosen = -1;
        @Override public void start() { sendDialogue("A message from the existing dialogue manager."); }
        @Override public void run(int panel, int component) {
            runs++;
            if (runs == 1) sendOptionsDialogue("Choose", "Proceed", "Never mind");
            else { chosen = component; end(); }
        }
        @Override public void finish() { finishes++; }
    }

    /**
     * The dialogue click is 950 opcode 101 (947: 15) and its two fields swapped places: the 950
     * writer 0x1401abc40 stamps the component hash big-endian FIRST (0x1401abcac) and the slot
     * last as [slot&gt;&gt;&gt;8, (slot &amp; 255)+128] (0x1401abcf0 / 0x1401abced). The 947 helper
     * emitted the slot first. Both orders are six well-formed bytes, so nothing but the field
     * order distinguishes a correct reply from one aimed at a different component.
     */
    private static Native950Actions.Action reply(int panel, int component, int slot) {
        int hash = (panel << 16) | component;
        return Native950Actions.decode(101, new byte[] {
                (byte) (hash >>> 24), (byte) (hash >>> 16), (byte) (hash >>> 8), (byte) hash,
                (byte) (slot >>> 8), (byte) (slot + 128)});
    }

    /** The typed amount is 950 opcode 120 (947: 16); the signed big-endian i64 body is unchanged. */
    private static Native950Actions.Action count(long value) {
        return Native950Actions.decode(120, ByteBuffer.allocate(8).putLong(value).array());
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
                amounts, amounts, Collections.emptyList(), Collections.emptyList()));
    }
}
