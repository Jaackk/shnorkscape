package com.rs.game.player.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.*;

/** Owned count replies and current capacity; native rendering is covered separately. */
public class Native950QuantityInputTest {
    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final Player player = Player.createNative950("quantity-test", new WorldTile(3222, 3222, 0), channel);
    private final Native950ItemCatalog catalog = new Native950ItemCatalog(Arrays.asList(
            new Native950ItemCatalog.Entry(995, "Coins", true, new String[]{"Add to pouch"}),
            new Native950ItemCatalog.Entry(1511, "Logs", false, new String[]{"Craft"}),
            new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[]{"Eat"})));
    private final Native950Containers containers = new Native950Containers(player, catalog);
    private int verifications, capacityCalls;
    private final Native950QuantityInput input = new Native950QuantityInput(() -> verifications++);

    @After public void release() { channel.finishAndReleaseAll(); }

    @Test public void unsolicitedAndDuplicateRepliesCannotAcquireOrReuseOwnership() {
        bank(995, 100);
        assertNull(consume(5, 1));
        assertEquals(0, capacityCalls);
        assertTrue(begin(1, 0, 995));
        Native950QuantityInput.Accepted result = consume(5, 1);
        assertEquals(0, result.slot); assertEquals(995, result.itemId); assertEquals(5, result.amount);
        assertFalse(input.active()); assertNull(consume(5, 1)); assertEquals(1, capacityCalls);
        assertEquals(100, player.getBank().bankTabs[0][0].getAmount()); // Helper never commits a transfer.
    }

    @Test public void negativeZeroAndOverflowingSignedWireCountsRetireTheRequestBeforeNarrowing() {
        bank(995, 100);
        for (long bad : new long[]{Long.MIN_VALUE, -1, 0, (long) Integer.MAX_VALUE + 1, Long.MAX_VALUE}) {
            assertTrue(begin(1, 0, 995));
            Native950Actions.CountDialogueAction action = (Native950Actions.CountDialogueAction)
                    Native950Actions.decode(120, ByteBuffer.allocate(8).putLong(bad).array());
            assertEquals(bad, action.count()); assertNull(consume(action.count(), 1));
            assertFalse(input.active());
        }
        assertEquals(0, capacityCalls);
    }

    @Test public void maximumIntRemainsValidAndIsCappedToCurrentStock() {
        bank(995, 100);
        assertTrue(begin(1, 0, 995));
        assertEquals(100, consume(Integer.MAX_VALUE, 1).amount);
    }

    @Test public void targetSlotAndClaimMustMatchBeforeOpeningAndCannotReplacePending() {
        bank(995, 100, 1511, 10);
        assertFalse(begin(1, -1, 995)); assertFalse(begin(1, 600, 995));
        assertFalse(begin(1, 2, -1)); assertFalse(begin(1, 0, 1511));
        assertEquals(0, verifications);
        assertTrue(begin(1, 0, 995));
        assertFalse(begin(1, 1, 1511));
        assertEquals(995, consume(1, 1).itemId); assertEquals(1, verifications);
    }

    @Test public void placeholdersAndAbsentSlotsDoNotOpenARequest() {
        bank(995, 0);
        assertFalse(begin(1, 0, 995)); assertFalse(begin(1, 1, 995));
        assertFalse(input.active()); assertEquals(0, verifications);
    }

    @Test public void aDifferentBankSessionConsumesAndRejectsTheOldReply() {
        bank(995, 100);
        assertTrue(begin(1, 0, 995)); assertNull(consume(1, 2));
        assertFalse(input.active()); assertEquals(0, capacityCalls);
    }

    @Test public void targetReplacementQuantityChangeAndCompactionInvalidateTheSavedBank() {
        for (int change = 0; change < 3; change++) {
            bank(995, 100, 1511, 10);
            assertTrue(begin(1, 0, 995));
            if (change == 0) bank(315, 100, 1511, 10);
            else if (change == 1) bank(995, 99, 1511, 10);
            else bank(1511, 10);
            assertNull(consume(5, 1)); assertFalse(input.active());
        }
        assertEquals(0, capacityCalls);
    }

    @Test public void unrelatedBankChangesAlsoInvalidateTheRequest() {
        bank(995, 100, 1511, 10); assertTrue(begin(1, 0, 995));
        bank(995, 100, 1511, 9); assertNull(consume(5, 1)); assertEquals(0, capacityCalls);
    }

    @Test public void capturedArraysAreDefensiveCopies() {
        bank(995, 100);
        Native950Containers.Snapshot before = containers.bankSnapshot();
        assertTrue(input.beginWithdraw(1, 0, 995, before));
        before.ids[0] = 315; before.amounts[0] = 1;
        assertEquals(5, consume(5, 1).amount);
    }

    @Test public void capacityIsRecomputedWhenReplyArrives() {
        bank(1511, 10); assertTrue(begin(1, 0, 1511));
        for (int i = 0; i < 26; i++) player.getInventory().items.set(i, new Item(315, 1));
        assertEquals(2, consume(10, 1).amount);
        assertEquals(10, player.getBank().bankTabs[0][0].getAmount());
        assertNull(player.getInventory().items.get(26));
    }

    @Test public void fullInventoryRejectsWhileStackRoomStillCapsWithoutOverflow() {
        bank(1511, 10, 995, 100);
        for (int i = 0; i < 28; i++) player.getInventory().items.set(i, new Item(315, 1));
        assertTrue(begin(1, 0, 1511)); assertNull(consume(1, 1));
        player.getInventory().items.set(0, new Item(995, Integer.MAX_VALUE - 3));
        assertTrue(begin(1, 1, 995)); assertEquals(3, consume(100, 1).amount);
    }

    @Test public void callbackCannotExpandRequestedQuantityAndFailureCannotLeaveOwnership() {
        bank(995, 100); assertTrue(begin(1, 0, 995));
        assertEquals(5, input.consume(5, 1, containers.bankSnapshot(), (slot, amount) -> Integer.MAX_VALUE).amount);
        assertTrue(begin(1, 0, 995));
        try { input.consume(5, 1, containers.bankSnapshot(), (slot, amount) -> { throw new IllegalStateException("capacity"); }); fail(); }
        catch (IllegalStateException expected) { assertEquals("capacity", expected.getMessage()); }
        assertFalse(input.active());
    }

    @Test public void cancellationRetiresOwnershipAndAllowsAnExplicitLaterRequest() {
        bank(995, 100); assertTrue(begin(1, 0, 995));
        input.cancel(); input.cancel(); assertNull(consume(5, 1));
        assertTrue(begin(2, 0, 995)); assertEquals(1, consume(1, 2).amount);
    }

    @Test public void verifierFailureCannotAcquireOwnership() {
        bank(995, 100);
        Native950QuantityInput failed = new Native950QuantityInput(() -> { throw new IllegalStateException("changed cache"); });
        try { failed.beginWithdraw(1, 0, 995, containers.bankSnapshot()); fail(); }
        catch (IllegalStateException expected) { assertEquals("changed cache", expected.getMessage()); }
        assertFalse(failed.active());
    }

    @Test public void framePacketsPreserveTheBankAndUseMode17BeforeOrderedTeardown() {
        try { input.promptPackets(); fail(); } catch (IllegalStateException expected) { /* No unowned prompt. */ }
        bank(995, 100); assertTrue(begin(1, 0, 995));
        List<Native950Packets.Packet> open = input.promptPackets(); assertEquals(4, open.size());
        packet(Native950Packets.openSub(1477, 749, 1418, true), open.get(0));
        packet(Native950Packets.openSub(1418, 2, 1469, true), open.get(1));
        packet(Native950Packets.hideInterface(1477, 747, false), open.get(2));
        packet(Native950Packets.runClientScript(17396, "How many would you like to withdraw?"), open.get(3));
        input.cancel();
        List<Native950Packets.Packet> close = input.cancelPackets(); assertEquals(5, close.size());
        packet(Native950Packets.runClientScript(1548, 17), close.get(0));
        packet(Native950Packets.closeSub(1418, 2), close.get(1));
        packet(Native950Packets.closeSub(1477, 749), close.get(2));
        packet(Native950Packets.hideInterface(1477, 747, true), close.get(3));
        packet(Native950Packets.runClientScript(1364), close.get(4));
        assertEquals(1, verifications);
    }

    @Test public void wrongThreadCannotReadOrCancelOwnership() throws Exception {
        bank(995, 100); assertTrue(begin(1, 0, 995));
        try { CompletableFuture.runAsync(input::cancel).get(); fail(); }
        catch (ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
        assertTrue(input.active());
    }

    /**
     * The 28 pinned quantity-input cache files, checked against an independently produced
     * evidence file rather than against whatever cache the test host happens to have.
     *
     * <p><b>Skipped until the 950 pin set is produced.</b> The path this used to read,
     * {@code ../OpenNXT/data/prot/947/generated/native947-3/verified/ui/quantity-input-files.json},
     * is 947 evidence and does not exist in this project at all (there is no
     * {@code data/prot/947} tree here); it only exists in the read-only AstraNXT workspace.
     * Re-pointing it there would be worse than the current skip, not better: the 950 cache
     * recompiled every clientscript, and per the port plan 0 of 147 pinned clientscripts survive,
     * with 15 of 17 high-value scripts keeping their exact byte LENGTH while their content
     * changed - the signature of a recompile with renumbered operands. Feeding 947 bodies to
     * {@link Native950QuantityInput#verifyFiles} would report green for files the 950 client
     * never loads, which is the one failure mode this test exists to prevent.
     *
     * <p>To un-skip: extract these 28 logical files (index 3 interfaces 1477/1418/1469 and the
     * index 12 clientscripts listed in {@code Native950QuantityInput.PINS}) from the 950 cache
     * into {@code OpenNXT/data/prot/950/verified/ui/quantity-input-files.json}, keeping the same
     * shape as the 947 file - a top-level "files" object keyed by "index/group/file", each value
     * an object with a "base64" string - and re-derive
     * {@code Native950QuantityInput.PINS} from them. PINS is still the 947 digest table today;
     * it is the port plan's STAGE 8 work and is gated at runtime by the
     * {@code ataraxia.native.verifyCache} kill switch, not by this test.
     */
    @Test public void independentlyInspectedFilesPassPinsAndEverySingleFileDriftIsRejected() throws Exception {
        Path pinned = evidence("verified/ui/quantity-input-files.json");
        Assume.assumeTrue("the 950 quantity-input cache pins have not been produced yet - no"
                + " verified/ui/quantity-input-files.json under OpenNXT/data/prot/950."
                + " Deliberately NOT falling back to the 947 evidence file: the 950 cache"
                + " recompiled every clientscript, so those pins are known stale and a fallback"
                + " would manufacture false green pins rather than a visible gap.", pinned != null);
        JsonObject evidence = new Gson().fromJson(
                new String(Files.readAllBytes(pinned), StandardCharsets.UTF_8), JsonObject.class);
        JsonObject files = evidence.getAsJsonObject("files");
        Native950QuantityInput.verifyFiles((i, g, f) -> bytes(files, i + "/" + g + "/" + f));
        assertEquals(28, files.size());
        for (String changed : files.keySet()) {
            try {
                Native950QuantityInput.verifyFiles((i, g, f) -> {
                    String key = i + "/" + g + "/" + f; byte[] data = bytes(files, key);
                    if (key.equals(changed)) data[0] ^= 1;
                    return data;
                }); fail("Accepted changed " + changed);
            } catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains(changed)); }
        }
    }

    /**
     * Resolves a file in the <b>950</b> evidence tree, or null when it has not been produced.
     *
     * There is deliberately no 947 candidate in this list. Same resolution order as
     * {@code Native950VarWireTest.evidence}: an explicit override first, then the path that works
     * when the suite runs from the Gradle project root, then the absolute workspace path.
     */
    private static Path evidence(String relative) {
        String override = System.getProperty("modern950.evidenceRoot");
        Path[] candidates = {
            override == null ? null : Paths.get(override, relative),
            Paths.get("../OpenNXT/data/prot/950", relative),
            Paths.get("C:\\Users\\developer\\Desktop\\950RevTest\\OpenNXT\\data\\prot\\950", relative)
        };
        for (Path candidate : candidates)
            if (candidate != null && Files.isRegularFile(candidate)) return candidate;
        return null;
    }

    @Test public void depositQuantityCountsAllCopiesAndCapsToFreshCapacity() {
        for(int slot:new int[]{1,9,27})player.getInventory().items.set(slot,new Item(1511,1));
        assertTrue(input.beginDeposit(4,9,1511,containers.inventorySnapshot()));
        assertEquals(Native950QuantityInput.Kind.DEPOSIT,input.kind());
        Native950QuantityInput.Accepted result=input.consume(10,4,containers.inventorySnapshot(),(slot,amount)->{
            assertEquals(9,slot);assertEquals(3,amount);return 2;
        });
        assertEquals(Native950QuantityInput.Kind.DEPOSIT,result.kind);assertEquals(2,result.amount);
        assertEquals(1511,result.itemId);assertEquals(9,result.slot);assertNull(input.kind());
        assertEquals(3,player.getInventory().items.getNumberOf(1511));
        assertNull(input.consume(10,4,containers.inventorySnapshot(),(slot,amount)->amount));
    }
    @Test public void depositStockSumCannotWrapAtTheIntegerBoundary() {
        player.getInventory().items.set(1,new Item(995,Integer.MAX_VALUE));
        player.getInventory().items.set(9,new Item(995,Integer.MAX_VALUE));
        assertTrue(input.beginDeposit(1,9,995,containers.inventorySnapshot()));
        assertEquals(Integer.MAX_VALUE,input.consume(Integer.MAX_VALUE,1,containers.inventorySnapshot(),(slot,amount)->amount).amount);
    }
    @Test public void depositUnrelatedSlotChangesWrongSnapshotAndSessionInvalidateOwnership() {
        player.getInventory().items.set(1,new Item(1511,1));player.getInventory().items.set(9,new Item(315,1));
        assertTrue(input.beginDeposit(1,1,1511,containers.inventorySnapshot()));
        player.getInventory().items.set(9,null);
        assertNull(input.consume(1,1,containers.inventorySnapshot(),(slot,amount)->{fail("Stale deposit reached capacity");return 0;}));
        assertTrue(input.beginDeposit(1,1,1511,containers.inventorySnapshot()));
        assertNull(input.consume(1,1,containers.bankSnapshot(),(slot,amount)->amount));
        assertTrue(input.beginDeposit(1,1,1511,containers.inventorySnapshot()));
        assertNull(input.consume(1,2,containers.inventorySnapshot(),(slot,amount)->amount));
        assertNull(input.kind());
    }
    @Test public void defaultQuantityHasNoItemOrStockCapAndNeverCallsCapacity() {
        assertTrue(input.beginDefault(1,containers.bankSnapshot()));
        assertEquals(Native950QuantityInput.Kind.DEFAULT,input.kind());
        Native950QuantityInput.Accepted result=input.consume(Integer.MAX_VALUE,1,containers.bankSnapshot(),(slot,amount)->{
            fail("Default quantity must not inspect item capacity");return 0;
        });
        assertEquals(Native950QuantityInput.Kind.DEFAULT,result.kind);assertEquals(-1,result.slot);assertEquals(-1,result.itemId);
        assertEquals(Integer.MAX_VALUE,result.amount);assertNull(input.kind());
        assertNull(input.consume(1,1,containers.bankSnapshot(),null));
        assertTrue(input.beginDefault(2,containers.bankSnapshot()));
        assertEquals(37,input.consume(37,2,containers.bankSnapshot(),null).amount);
    }
    @Test public void allNewKindsRejectInvalidCountsAndCannotReplaceAnActiveRequest() {
        player.getInventory().items.set(1,new Item(1511,1));bank(995,100);
        assertFalse(input.beginDeposit(1,-1,1511,containers.inventorySnapshot()));
        assertFalse(input.beginDeposit(1,1,315,containers.inventorySnapshot()));assertFalse(input.beginDefault(1,null));
        assertNull(input.kind());
        assertTrue(input.beginDefault(1,containers.bankSnapshot()));
        assertFalse(input.beginDeposit(1,1,1511,containers.inventorySnapshot()));assertFalse(begin(1,0,995));
        assertEquals(Native950QuantityInput.Kind.DEFAULT,input.kind());input.cancel();
        for(long count:new long[]{Long.MIN_VALUE,-1,0,(long)Integer.MAX_VALUE+1,Long.MAX_VALUE}) {
            assertTrue(input.beginDefault(1,containers.bankSnapshot()));assertNull(input.consume(count,1,containers.bankSnapshot(),null));
            assertTrue(input.beginDeposit(1,1,1511,containers.inventorySnapshot()));
            assertNull(input.consume(count,1,containers.inventorySnapshot(),(slot,amount)->{fail("Invalid count reached capacity");return 0;}));
            assertNull(input.kind());
        }
    }
    @Test public void defaultSnapshotAndEpochCannotBeReusedAfterBankChanges() {
        bank(995,100);assertTrue(input.beginDefault(1,containers.bankSnapshot()));
        bank(995,99);assertNull(input.consume(37,1,containers.bankSnapshot(),null));
        assertTrue(input.beginDefault(1,containers.bankSnapshot()));assertNull(input.consume(37,2,containers.bankSnapshot(),null));
        assertFalse(input.active());
    }
    @Test public void newKindsUseTheSameVerifiedPromptWithExplicitPurpose() {
        player.getInventory().items.set(1,new Item(1511,1));
        assertTrue(input.beginDeposit(1,1,1511,containers.inventorySnapshot()));
        packet(Native950Packets.runClientScript(17396,"How many would you like to deposit?"),input.promptPackets().get(3));
        input.cancel();assertTrue(input.beginDefault(1,containers.bankSnapshot()));
        packet(Native950Packets.runClientScript(17396,"Set the default bank quantity:"),input.promptPackets().get(3));
        assertEquals(2,verifications);
    }
    private static byte[] bytes(JsonObject files, String key) { return Base64.getDecoder().decode(files.getAsJsonObject(key).get("base64").getAsString()); }
    private void bank(int... idAmount) {
        Item[] items = new Item[idAmount.length / 2];
        for (int i = 0; i < items.length; i++) items[i] = new Item(idAmount[i * 2], idAmount[i * 2 + 1]);
        player.getBank().bankTabs = new Item[][]{items};
    }
    private boolean begin(long epoch, int slot, int id) { return input.beginWithdraw(epoch, slot, id, containers.bankSnapshot()); }
    private Native950QuantityInput.Accepted consume(long count, long epoch) {
        Native950Containers.Snapshot current = containers.bankSnapshot();
        return input.consume(count, epoch, current, (slot, amount) -> {
            capacityCalls++; return containers.withdrawableAmount(slot, current.ids[slot], amount);
        });
    }
    private static void packet(Native950Packets.Packet expected, Native950Packets.Packet actual) {
        assertEquals(expected.type(), actual.type()); assertArrayEquals(expected.payload(), actual.payload());
    }
}
