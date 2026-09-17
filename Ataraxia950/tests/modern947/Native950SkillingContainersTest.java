package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.ControlerManager;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Skill rewards consume the existing player inventory, with no partial grants or lost inputs. */
public class Native950SkillingContainersTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Containers containers;
    @Before public void setup() {
        channel=new EmbeddedChannel();
        player=Player.createNative950("skilling-containers",new WorldTile(3222,3222,0),channel);
        Native950ItemCatalog catalog=new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995,"Coins",true,new String[]{"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511,"Logs",false,new String[]{null,"Light"}),
                new Native950ItemCatalog.Entry(590,"Tinderbox",false,new String[5])));
        containers=new Native950Containers(player,catalog);
        Native950Skilling.attach(player,containers);
    }
    @After public void cleanup() { Native950Skilling.detach(player);channel.finishAndReleaseAll(); }

    @Test public void ordinaryCostsRefuseModifiedSameIdWithoutThrowingOrConsuming(){
        Item protectedLog=new Item(1511,1,12),plain=new Item(1511,1);
        player.getInventory().items.set(0,protectedLog);player.getInventory().items.set(1,plain);
        assertFalse(containers.canExchangeItems(new Item[]{new Item(1511,1)},new Item[0]));
        assertFalse(containers.exchangeItems(new Item[]{new Item(1511,1)},new Item[0]));
        assertSame(protectedLog,player.getInventory().items.get(0));assertSame(plain,player.getInventory().items.get(1));
        assertEquals(12,protectedLog.getCharges());
    }
    @Test public void nonStackableCapacityPreviewUsesWholeItemSlotsAndNeverChangesInventory() {
        fill(26,590);
        Native950Containers.Snapshot before=containers.inventorySnapshot();
        assertTrue(containers.canReceiveItem(new Item(1511,2)));
        assertFalse(containers.canReceiveItem(new Item(1511,3)));
        assertUnchanged(before);
        assertTrue(Native950Skilling.hasSpace(player,1511,2));
        assertUnchanged(before);
        assertTrue(Native950Skilling.giveItem(player,1511,2));
        assertEquals(1511,player.getInventory().items.get(26).getId());
        assertEquals(1511,player.getInventory().items.get(27).getId());
        assertEquals(1,player.getInventory().items.get(26).getAmount());
        assertEquals(1,player.getInventory().items.get(27).getAmount());
    }
    @Test public void fullOrInsufficientSpaceRefusesTheEntireRewardWithoutPartialInsertion() {
        fill(27,590);
        Native950Containers.Snapshot before=containers.inventorySnapshot();
        assertFalse(Native950Skilling.giveItem(player,1511,2));
        assertUnchanged(before);
        assertTrue(Native950Skilling.giveItem(player,1511,1));
        before=containers.inventorySnapshot();
        assertFalse(Native950Skilling.hasSpace(player,1511,1));
        assertFalse(Native950Skilling.giveItem(player,1511,1));
        assertUnchanged(before);
    }
    @Test public void consumptionNeedsTheExactRequestedCountAndPreservesUnrelatedItems() {
        player.getInventory().items.set(0,new Item(590,1));
        player.getInventory().items.set(1,new Item(1511,1));
        player.getInventory().items.set(7,new Item(1511,1));
        player.getInventory().items.set(22,new Item(1511,1));
        Native950Containers.Snapshot before=containers.inventorySnapshot();
        assertFalse(Native950Skilling.consumeItem(player,1511,4));
        assertUnchanged(before);
        assertTrue(Native950Skilling.consumeItem(player,1511,2));
        assertEquals(1,player.getInventory().items.getNumberOf(1511));
        assertEquals(590,player.getInventory().items.get(0).getId());
        assertTrue(containers.consumeItem(1511,1));
        assertEquals(0,player.getInventory().items.getNumberOf(1511));
        assertFalse(containers.consumeItem(1511,1));
    }
    @Test public void stackHeadroomAllowsFullBagsButOverflowAndPreviewCannotChangeTheExistingStack() {
        fill(28,590);
        player.getInventory().items.set(0,new Item(995,Integer.MAX_VALUE-2));
        Item original=player.getInventory().items.get(0);
        Native950Containers.Snapshot before=containers.inventorySnapshot();
        assertTrue(containers.canReceiveItem(new Item(995,2)));
        assertFalse(containers.canReceiveItem(new Item(995,3)));
        assertFalse(Native950Skilling.giveItem(player,995,3));
        assertSame(original,player.getInventory().items.get(0));
        assertUnchanged(before);
        assertTrue(Native950Skilling.giveItem(player,995,2));
        assertEquals(Integer.MAX_VALUE,player.getInventory().items.get(0).getAmount());
        assertFalse(Native950Skilling.giveItem(player,995,1));
        assertTrue(Native950Skilling.consumeItem(player,995,Integer.MAX_VALUE));
        assertNull(player.getInventory().items.get(0));
        assertEquals(27,player.getInventory().items.getNumberOf(590));
    }
    @Test public void originalControllerCanVetoRewardAndResourceConsumptionBeforeAnyCommit() throws Exception {
        player.getInventory().items.set(0,new Item(1511,1));
        DenyingController controller=new DenyingController();
        installController(controller);
        Native950Containers.Snapshot before=containers.inventorySnapshot();
        assertTrue("space does not itself grant permission",Native950Skilling.hasSpace(player,1511,1));
        assertFalse(Native950Skilling.giveItem(player,1511,1));
        assertFalse(Native950Skilling.consumeItem(player,1511,1));
        assertEquals(1,controller.addCalls);assertEquals(1,controller.deleteCalls);
        assertUnchanged(before);
    }
    @Test public void detachedOrMissingSessionCannotChangeAPlayersContainers() {
        player.getInventory().items.set(0,new Item(1511,1));
        Native950Containers.Snapshot before=containers.inventorySnapshot();
        Native950Skilling.detach(player);
        assertFalse(Native950Skilling.hasSpace(player,1511,1));
        assertFalse(Native950Skilling.giveItem(player,1511,1));
        assertFalse(Native950Skilling.consumeItem(player,1511,1));
        assertFalse(Native950Skilling.giveItem(null,1511,1));
        assertFalse(Native950Skilling.consumeItem(null,1511,1));
        assertUnchanged(before);
    }
    @Test public void invalidQuantitiesAndUncataloguedItemsAreNeverSuccessfulSkillCommits() {
        player.getInventory().items.set(0,new Item(1511,1));
        Native950Containers.Snapshot before=containers.inventorySnapshot();
        for(int amount:new int[]{0,-1}) {
            assertFalse(Native950Skilling.hasSpace(player,1511,amount));
            assertFalse(Native950Skilling.giveItem(player,1511,amount));
            assertFalse(Native950Skilling.consumeItem(player,1511,amount));
        }
        assertFalse(Native950Skilling.hasSpace(player,9999,1));
        assertFalse(Native950Skilling.giveItem(player,9999,1));
        assertFalse(Native950Skilling.consumeItem(player,9999,1));
        assertUnchanged(before);
    }
    @Test public void skillInventoryChecksAndCommitsAreConfinedToTheCreatingWorldThread() throws Exception {
        player.getInventory().items.set(0,new Item(1511,1));
        Native950Containers.Snapshot before=containers.inventorySnapshot();
        offOwner(()->containers.canReceiveItem(new Item(1511,1)));
        offOwner(()->containers.consumeItem(1511,1));
        offOwner(()->Native950Skilling.giveItem(player,1511,1));
        assertUnchanged(before);
    }
    private void offOwner(Runnable operation) throws Exception {
        Throwable failure=CompletableFuture.supplyAsync(()->{
            try {operation.run();return null;}catch(Throwable rejected){return rejected;}
        }).get(3,TimeUnit.SECONDS);
        assertTrue("inventory operation must reject the wrong world thread",failure instanceof IllegalStateException);
    }
    private void fill(int count,int id) { for(int i=0;i<count;i++)player.getInventory().items.set(i,new Item(id,1)); }
    private void assertUnchanged(Native950Containers.Snapshot before) {
        Native950Containers.Snapshot after=containers.inventorySnapshot();
        assertArrayEquals(before.ids,after.ids);assertArrayEquals(before.amounts,after.amounts);
    }
    private void installController(Controller controller) throws Exception {
        ControlerManager manager=player.getControlerManager();
        Field current=ControlerManager.class.getDeclaredField("controler");current.setAccessible(true);current.set(manager,controller);
        Field inited=ControlerManager.class.getDeclaredField("inited");inited.setAccessible(true);inited.setBoolean(manager,true);
    }
    private static final class DenyingController extends Controller {
        int addCalls,deleteCalls;
        @Override public void start() { }
        @Override public boolean canAddInventoryItem(int id,int amount) {addCalls++;return false;}
        @Override public boolean canDeleteInventoryItem(int id,int amount) {deleteCalls++;return false;}
    }
}
