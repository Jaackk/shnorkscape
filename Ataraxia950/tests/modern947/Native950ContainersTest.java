package modern947;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950Containers;
import com.rs.game.player.client.Native950ItemCatalog;
import com.rs.game.player.client.Native950Save;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Test;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class Native950ContainersTest {
    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final Player player = Player.createNative950("containers-test", new WorldTile(3222,3222,0), channel);
    private final Native950ItemCatalog catalog = new Native950ItemCatalog(Arrays.asList(
            new Native950ItemCatalog.Entry(995,"Coins",true,new String[]{"Add to pouch"}),
            new Native950ItemCatalog.Entry(1511,"Logs",false,new String[]{"Craft"}),
            new Native950ItemCatalog.Entry(315,"Shrimps",false,new String[]{"Eat"})));
    private final Native950Containers containers = new Native950Containers(player, catalog);
    @After public void release() { channel.finishAndReleaseAll(); }

    @Test public void transfersMutateActualAtaraxiaStorageAndConserveItems() {
        containers.seedStarterItems();
        assertEquals(10,containers.deposit(0,995,10).moved);
        assertEquals(990,player.getInventory().items.get(0).getAmount());
        assertEquals(10,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(10,containers.withdraw(0,995,Integer.MAX_VALUE).moved);
        assertEquals(1000,player.getInventory().items.get(0).getAmount());
        assertEquals(0,player.getBank().bankTabs[0].length);
        assertEquals(1000,total(995));
    }

    @Test public void staleAndInvalidClicksCannotMoveDifferentItems() {
        containers.seedStarterItems();
        assertEquals(0,containers.deposit(0,1511,1).moved);
        assertEquals(0,containers.deposit(-1,995,1).moved);
        assertEquals(0,containers.deposit(0,995,-1).moved);
        assertEquals(1,containers.deposit(1,1511,1).moved);
        assertEquals(1,containers.deposit(6,315,1).moved);
        assertEquals(1,containers.withdraw(0,1511,1).moved);
        // Removing the first bank entry shifts the shrimp into slot zero.
        assertEquals(0,containers.withdraw(0,1511,1).moved);
        assertEquals(315,player.getBank().bankTabs[0][0].getId());
        assertEquals(5,total(1511)); assertEquals(5,total(315)); assertEquals(1000,total(995));
    }

    @Test public void nonStackableWithdrawalMovesOnlyWhatFits() {
        for(int i=0;i<26;i++) player.getInventory().items.set(i,new Item(315,1));
        player.getBank().bankTabs = new Item[][]{{new Item(1511,5)}};
        assertEquals(2,containers.withdraw(0,1511,10).moved);
        assertEquals(3,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(0,containers.withdraw(0,1511,Integer.MAX_VALUE).moved);
        assertEquals(5,total(1511));
        assertEquals(1,player.getInventory().items.get(26).getAmount());
        assertEquals(1,player.getInventory().items.get(27).getAmount());
    }

    @Test public void withdrawalPreviewPreservesStateAndMatchesPartialCapacityCommit() {
        for(int i=0;i<26;i++) player.getInventory().items.set(i,new Item(315,1));
        player.getBank().bankTabs = new Item[][]{{new Item(1511,5),new Item(995,1000)}};
        Native950Containers.Snapshot beforeInventory=containers.inventorySnapshot(), beforeBank=containers.bankSnapshot();
        assertEquals(2,containers.withdrawableAmount(0,1511,Integer.MAX_VALUE));
        assertEquals(1,containers.withdrawableAmount(0,1511,1));
        assertEquals(0,containers.withdrawableAmount(0,995,5));
        assertEquals(0,containers.withdrawableAmount(-1,1511,5));
        assertEquals(0,containers.withdrawableAmount(2,1511,5));
        assertEquals(0,containers.withdrawableAmount(0,1511,0));
        assertEquals(0,containers.withdrawableAmount(0,1511,-1));
        assertEquals(0,containers.withdrawableAmount(0,999999,5));
        assertArrayEquals(beforeInventory.ids,containers.inventorySnapshot().ids);
        assertArrayEquals(beforeInventory.amounts,containers.inventorySnapshot().amounts);
        assertArrayEquals(beforeBank.ids,containers.bankSnapshot().ids);
        assertArrayEquals(beforeBank.amounts,containers.bankSnapshot().amounts);
        assertEquals(2,containers.withdraw(0,1511,Integer.MAX_VALUE).moved);
        assertEquals(3,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(0,containers.withdrawableAmount(0,1511,Integer.MAX_VALUE));
        assertEquals(5,total(1511));
    }

    @Test public void withdrawalPreviewHandlesStackHeadroomAndFullBackpack() {
        player.getInventory().items.set(0,new Item(995,Integer.MAX_VALUE-2));
        for(int i=1;i<28;i++) player.getInventory().items.set(i,new Item(315,1));
        player.getBank().bankTabs = new Item[][]{{new Item(995,1000)}};
        long before=total(995);
        assertEquals(1,containers.withdrawableAmount(0,995,1));
        assertEquals(2,containers.withdrawableAmount(0,995,Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE-2,player.getInventory().items.get(0).getAmount());
        assertEquals(2,containers.withdraw(0,995,Integer.MAX_VALUE).moved);
        assertEquals(0,containers.withdrawableAmount(0,995,1));
        assertEquals(before,total(995));
        player.getInventory().items.set(0,new Item(315,1));
        assertEquals(0,containers.withdrawableAmount(0,995,Integer.MAX_VALUE));
        player.getInventory().items.set(0,null);
        assertEquals(998,containers.withdrawableAmount(0,995,Integer.MAX_VALUE));
        assertEquals(998,containers.withdraw(0,995,Integer.MAX_VALUE).moved);
        assertEquals(0,containers.withdrawableAmount(0,995,1)); // source was exhausted
        assertEquals(998,player.getInventory().items.get(0).getAmount());
    }

    @Test public void nonStackableDepositQuantityUsesClickedSlotThenOtherCopies() {
        containers.seedStarterItems();
        assertEquals(3, containers.deposit(5,1511,3).moved);
        assertNull(player.getInventory().items.get(5)); // selected copy is consumed first
        assertNull(player.getInventory().items.get(1));
        assertNull(player.getInventory().items.get(2));
        assertEquals(1511,player.getInventory().items.get(3).getId());
        assertEquals(1511,player.getInventory().items.get(4).getId());
        assertEquals(3,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(5,total(1511)); assertEquals(5,total(315)); assertEquals(1000,total(995));
        assertEquals(2,containers.deposit(4,1511,Integer.MAX_VALUE).moved);
        assertEquals(5,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(5,containers.withdraw(0,1511,5).moved);
        assertEquals(0,player.getBank().bankTabs[0].length);
        assertEquals(5,total(1511));
    }

    @Test public void nonStackableDepositFiveAndTenAreLimitedByAvailableCopies() {
        containers.seedStarterItems();
        assertEquals(5,containers.deposit(3,1511,5).moved);
        for(int slot=1;slot<=5;slot++) assertNull(player.getInventory().items.get(slot));
        assertEquals(5,containers.deposit(9,315,10).moved);
        for(int slot=6;slot<=10;slot++) assertNull(player.getInventory().items.get(slot));
        assertEquals(5,total(1511)); assertEquals(5,total(315)); assertEquals(1000,total(995));
    }

    @Test public void nonStackableDepositRespectsBankIntegerLimitWithoutRemovingExtraCopies() {
        containers.seedStarterItems();
        player.getBank().bankTabs = new Item[][]{{new Item(1511,Integer.MAX_VALUE-2)}};
        long before = total(1511);
        assertEquals(2,containers.deposit(5,1511,10).moved);
        assertNull(player.getInventory().items.get(5));
        assertNull(player.getInventory().items.get(1));
        assertEquals(1511,player.getInventory().items.get(2).getId());
        assertEquals(Integer.MAX_VALUE,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(0,containers.deposit(2,1511,Integer.MAX_VALUE).moved);
        assertEquals(before,total(1511));
    }

    @Test public void depositAtIntegerLimitNeverWrapsOrLosesQuantity() {
        player.getInventory().items.set(0,new Item(995,1000));
        player.getBank().bankTabs = new Item[][]{{new Item(995,Integer.MAX_VALUE-2)}};
        long before=total(995);
        assertEquals(2,containers.deposit(0,995,Integer.MAX_VALUE).moved);
        assertEquals(Integer.MAX_VALUE,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(998,player.getInventory().items.get(0).getAmount());
        assertEquals(0,containers.deposit(0,995,1).moved);
        assertEquals(before,total(995));
    }

    @Test public void existingStackAcceptsWithdrawalEvenWithNoFreeSlots() {
        player.getInventory().items.set(0,new Item(995,Integer.MAX_VALUE-3));
        for(int i=1;i<28;i++) player.getInventory().items.set(i,new Item(315,1));
        player.getBank().bankTabs = new Item[][]{{new Item(995,1000)}};
        long before=total(995);
        assertEquals(3,containers.withdraw(0,995,Integer.MAX_VALUE).moved);
        assertEquals(997,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(0,containers.withdraw(0,995,1).moved);
        assertEquals(before,total(995));
    }

    @Test public void depositAllCoalescesBankStacksAndPreservesTotals() {
        containers.seedStarterItems();
        assertEquals(11,containers.depositAll());
        assertTrue(player.getInventory().items.isEmpty());
        assertEquals(3,player.getBank().bankTabs[0].length);
        assertEquals(1000,total(995)); assertEquals(5,total(1511)); assertEquals(5,total(315));
        assertEquals(0,containers.depositAll());
    }

    @Test public void snapshotsAreDetachedAndMutationRequiresWorldThread() throws Exception {
        containers.seedStarterItems();
        containers.inventorySnapshot().ids[0]=315;
        assertEquals(995,player.getInventory().items.get(0).getId());
        try {
            CompletableFuture.supplyAsync(() -> containers.deposit(0,995,1)).get(3, TimeUnit.SECONDS);
            fail("Expected thread ownership rejection");
        } catch(ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
        try {
            CompletableFuture.supplyAsync(() -> containers.withdrawableAmount(0,995,1)).get(3, TimeUnit.SECONDS);
            fail("Expected preview thread ownership rejection");
        } catch(ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
        assertEquals(1000,total(995));
    }

    @Test public void inventoryDragChecksBothEndsAndPreservesWholeStacks() {
        containers.seedStarterItems();
        assertEquals(0,containers.swap(0,995,1,315).moved);
        assertEquals(1,containers.swap(0,995,1,1511).moved);
        assertEquals(1511,player.getInventory().items.get(0).getId());
        assertEquals(1000,player.getInventory().items.get(1).getAmount());
        assertEquals(1,containers.swap(1,995,27,-1).moved);
        assertNull(player.getInventory().items.get(1));
        assertEquals(1000,player.getInventory().items.get(27).getAmount());
        assertEquals(0,containers.swap(1,995,27,-1).moved);
        assertEquals(1000,total(995)); assertEquals(5,total(1511));
    }

    @Test public void saveRoundTripRestoresActualBackpackHolesAndCompactBankGroups() {
        containers.seedStarterItems();
        assertEquals(1,containers.swap(0,995,27,-1).moved);
        assertEquals(3,containers.deposit(3,1511,3).moved);
        assertEquals(2,containers.deposit(8,315,2).moved);
        Native950Save saved=containers.saveSnapshot("SAVE_TEST",3215,3256,1);
        assertEquals("save test",saved.username());
        assertEquals(3215,saved.x()); assertEquals(3256,saved.y()); assertEquals(1,saved.plane());
        assertArrayEquals(new int[]{1511,315},saved.bankIds());
        assertArrayEquals(new int[]{3,2},saved.bankAmounts());
        // Later live mutations must not rewrite the state captured for a save.
        assertEquals(6,containers.depositAll());
        EmbeddedChannel restoredChannel=new EmbeddedChannel();
        try {
            Player restoredPlayer=Player.createNative950("save test",new WorldTile(3215,3256,1),restoredChannel);
            Native950Containers restored=new Native950Containers(restoredPlayer,catalog);
            restored.restore(saved);
            assertArrayEquals(saved.inventoryIds(),restored.inventorySnapshot().ids);
            assertArrayEquals(saved.inventoryAmounts(),restored.inventorySnapshot().amounts);
            for(int slot:new int[]{0,1,2,3,6,8,11,26}) assertNull(restoredPlayer.getInventory().items.get(slot));
            assertEquals(995,restoredPlayer.getInventory().items.get(27).getId());
            assertEquals(1000,restoredPlayer.getInventory().items.get(27).getAmount());
            assertEquals(1511,restoredPlayer.getInventory().items.get(4).getId());
            assertEquals(1,restoredPlayer.getInventory().items.get(4).getAmount());
            assertEquals(315,restoredPlayer.getInventory().items.get(7).getId());
            assertEquals(1,restoredPlayer.getBank().bankTabs.length);
            assertEquals(2,restoredPlayer.getBank().bankTabs[0].length);
            assertEquals(1511,restoredPlayer.getBank().bankTabs[0][0].getId());
            assertEquals(3,restoredPlayer.getBank().bankTabs[0][0].getAmount());
            assertEquals(315,restoredPlayer.getBank().bankTabs[0][1].getId());
            assertEquals(2,restoredPlayer.getBank().bankTabs[0][1].getAmount());
            Native950Save again=restored.saveSnapshot("save test",3215,3256,1);
            assertArrayEquals(saved.inventoryIds(),again.inventoryIds());
            assertArrayEquals(saved.inventoryAmounts(),again.inventoryAmounts());
            assertArrayEquals(saved.bankIds(),again.bankIds());
            assertArrayEquals(saved.bankAmounts(),again.bankAmounts());
        } finally { restoredChannel.finishAndReleaseAll(); }
    }

    @Test public void restoreRejectsUnknownCatalogItemsBeforeEitherContainerChanges() {
        int[] ids=emptyInventoryIds(), amounts=new int[28];
        ids[0]=995; amounts[0]=1000;
        ids[27]=9999; amounts[27]=1;
        assertInvalidRestoreLeavesEmpty(new Native950Save("restore",3222,3222,0,
                ids,amounts,new int[]{1511},new int[]{5}));
        ids[27]=-1; amounts[27]=0;
        assertInvalidRestoreLeavesEmpty(new Native950Save("restore",3222,3222,0,
                ids,amounts,new int[]{1511,9999},new int[]{5,1}));
    }

    @Test public void restoreRejectsMultipleNonStackableItemsInOneBackpackSlotBeforeCommit() {
        int[] ids=emptyInventoryIds(), amounts=new int[28];
        ids[0]=995; amounts[0]=1000;
        ids[27]=1511; amounts[27]=2;
        assertInvalidRestoreLeavesEmpty(new Native950Save("restore",3222,3222,0,
                ids,amounts,new int[]{315},new int[]{5}));
    }

    @Test public void restoreRejectsDuplicateBackpackCoinStacksBeforeCommit() {
        int[] ids=emptyInventoryIds(), amounts=new int[28];
        ids[0]=995; amounts[0]=1000;
        ids[27]=995; amounts[27]=1;
        assertInvalidRestoreLeavesEmpty(new Native950Save("restore",3222,3222,0,
                ids,amounts,new int[]{1511},new int[]{5}));
    }

    @Test public void secondRestoreCannotReplaceOccupiedContainers() {
        int[] ids=emptyInventoryIds(), amounts=new int[28];
        ids[27]=995; amounts[27]=1000;
        Native950Save initial=new Native950Save("restore",3222,3222,0,
                ids,amounts,new int[]{1511,315},new int[]{5,5});
        containers.restore(initial);
        Native950Save replacement=new Native950Save("restore",3222,3222,0,
                emptyInventoryIds(),new int[28],new int[0],new int[0]);
        try { containers.restore(replacement); fail("Expected occupied character rejection"); }
        catch(IllegalStateException expected) { }
        Native950Save after=containers.saveSnapshot("restore",3222,3222,0);
        assertArrayEquals(initial.inventoryIds(),after.inventoryIds());
        assertArrayEquals(initial.inventoryAmounts(),after.inventoryAmounts());
        assertArrayEquals(initial.bankIds(),after.bankIds());
        assertArrayEquals(initial.bankAmounts(),after.bankAmounts());
    }

    @Test public void restoreAndSaveSnapshotRequireTheOwningWorldThread() throws Exception {
        int[] ids=emptyInventoryIds(), amounts=new int[28];
        ids[0]=995; amounts[0]=1000;
        Native950Save saved=new Native950Save("restore",3222,3222,0,
                ids,amounts,new int[]{1511},new int[]{5});
        try {
            CompletableFuture.runAsync(() -> containers.restore(saved)).get(3,TimeUnit.SECONDS);
            fail("Expected restore thread ownership rejection");
        } catch(ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
        assertTrue(player.getInventory().items.isEmpty());
        assertEquals(0,player.getBank().bankTabs[0].length);
        containers.restore(saved);
        try {
            CompletableFuture.supplyAsync(() -> containers.saveSnapshot("restore",3222,3222,0)).get(3,TimeUnit.SECONDS);
            fail("Expected save snapshot thread ownership rejection");
        } catch(ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
        assertEquals(1000,total(995)); assertEquals(5,total(1511));
    }

    @Test public void unsupportedItemDiagnosticsIdentifyContainerSlotAndRule() {
        Item charged=new Item(995,1);
        charged.setCharges(1);
        player.getInventory().items.set(7,charged);
        try {
            new Native950Containers(player,catalog);
            fail("Expected charged native item rejection");
        } catch(IllegalStateException expected) {
            assertTrue(expected.getMessage(),expected.getMessage().contains("container=inventory slot=7"));
            assertTrue(expected.getMessage(),expected.getMessage().contains("item=995 (Coins)"));
            assertTrue(expected.getMessage(),expected.getMessage().contains("charges=1"));
            assertTrue(expected.getMessage(),expected.getMessage().contains("rule=charges are unsupported"));
        }
    }

    private void assertInvalidRestoreLeavesEmpty(Native950Save saved) {
        try { containers.restore(saved); fail("Expected invalid saved item rejection"); }
        catch(IllegalArgumentException expected) { }
        for(Item item:player.getInventory().items.getItems()) assertNull(item);
        assertEquals(1,player.getBank().bankTabs.length);
        assertEquals(0,player.getBank().bankTabs[0].length);
    }

    private static int[] emptyInventoryIds() {
        int[] ids=new int[28]; Arrays.fill(ids,-1); return ids;
    }

    private long total(int id) {
        long count=0;
        for(Item item:player.getInventory().items.getItems()) if(item!=null && item.getId()==id)count+=item.getAmount();
        for(Item[] tab:player.getBank().bankTabs)for(Item item:tab)if(item!=null&&item.getId()==id)count+=item.getAmount();
        return count;
    }
}
