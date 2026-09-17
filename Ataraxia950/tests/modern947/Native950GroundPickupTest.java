package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.ControlerManager;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real Inventory/ItemsContainer transactions and authoritative pile checks, without a cache. */
public final class Native950GroundPickupTest {
    private final List<EmbeddedChannel> channels=new ArrayList<EmbeddedChannel>();
    private final WorldTile tile=new WorldTile(3212,3215,0);
    private final Native950ItemCatalog catalog=new Native950ItemCatalog(Arrays.asList(
            new Native950ItemCatalog.Entry(995,"Coins",true,new String[5]),
            new Native950ItemCatalog.Entry(526,"Bones",false,new String[5]),
            new Native950ItemCatalog.Entry(527,"Bones",true,new String[5])));
    private Player player;
    private Native950Containers containers;
    private List<FloorItem> pile;
    @Before public void setup() {
        player=player("loot-picker");containers=new Native950Containers(player,catalog);
        pile=new ArrayList<FloorItem>();
    }
    @After public void cleanup(){for(EmbeddedChannel channel:channels)channel.finishAndReleaseAll();}

    @Test public void fullInventoryCannotConsumeOrMutateFloorLoot() {
        fillBones(28);FloorItem floor=floor(995,10,player,true);
        unchanged(floor,Native950Containers.Result.FULL);assertEquals(10,floor.getAmount());
    }
    @Test public void stackOverflowCannotConsumeOrMutateAnyItem() {
        player.getInventory().items.set(0,new Item(995,Integer.MAX_VALUE-5));
        for(int i=1;i<28;i++)player.getInventory().items.set(i,new Item(526,1));
        FloorItem floor=floor(995,6,player,true);unchanged(floor,Native950Containers.Result.FULL);
        assertEquals(Integer.MAX_VALUE-5,player.getInventory().items.get(0).getAmount());assertEquals(6,floor.getAmount());
    }
    @Test public void nonStackablePileRequiresRoomForTheWholeQuantity() {
        fillBones(27);FloorItem floor=floor(526,2,player,true);
        unchanged(floor,Native950Containers.Result.FULL);assertNull(player.getInventory().items.get(27));
    }
    @Test public void existingStackAcceptsLootInAFullBagWithoutMutatingTheFloorObject() {
        player.getInventory().items.set(0,new Item(995,10));
        for(int i=1;i<28;i++)player.getInventory().items.set(i,new Item(526,1));
        FloorItem floor=floor(995,5,player,true);
        assertEquals(5,take(floor).moved);assertEquals(15,player.getInventory().items.get(0).getAmount());
        assertEquals(5,floor.getAmount());assertTrue(pile.isEmpty());
    }
    @Test public void nonStackableSuccessUsesOneBackpackSlotPerItem() {
        fillBones(26);FloorItem floor=floor(526,2,player,true);
        assertEquals(2,take(floor).moved);assertEquals(1,player.getInventory().items.get(26).getAmount());
        assertEquals(1,player.getInventory().items.get(27).getAmount());assertTrue(pile.isEmpty());
    }
    @Test public void staleTakeCannotConsumeAnEqualReplacementPile() {
        FloorItem stale=floor(995,10,player,true);pile.clear();
        FloorItem replacement=floor(995,10,player,true);assertEquals(stale,replacement);
        unchanged(stale,Native950Containers.Result.STALE);assertSame(replacement,pile.get(0));
        assertEquals(10,take(replacement).moved);assertTrue(pile.isEmpty());
    }
    @Test public void pickupRemovesOnlyTheSelectedEqualPileAndCannotRepeat() {
        FloorItem first=floor(995,10,player,true),second=floor(995,10,player,true);
        assertEquals(10,take(second).moved);assertEquals(1,pile.size());assertSame(first,pile.get(0));
        unchanged(second,Native950Containers.Result.STALE);assertEquals(10,take(first).moved);
        assertEquals(20,player.getInventory().items.get(0).getAmount());
    }
    @Test public void anotherOwnersPrivateLootIsUntouchableUntilPublic() {
        Player other=player("ground-other");FloorItem floor=floor(995,7,other,true);
        unchanged(floor,Native950Containers.Result.STALE);floor.setInvisible(false);
        assertEquals(7,take(floor).moved);assertTrue(pile.isEmpty());
    }
    @Test public void untradeablePublicLootRemainsVisibleAndCollectibleOnlyByItsOwner() {
        Player other=player("ground-other");FloorItem foreign=floor(995,7,other,false);
        foreign.setPublicTransferAllowed(false);
        assertFalse(Native950GroundItemsView.visibleTo(player,foreign));
        assertTrue(Native950GroundItemsView.visibleTo(other,foreign));
        unchanged(foreign,Native950Containers.Result.STALE);
        FloorItem own=floor(995,3,player,false);own.setPublicTransferAllowed(false);
        assertTrue(Native950GroundItemsView.visibleTo(player,own));assertEquals(3,take(own).moved);
    }
    @Test public void ironmanOwnershipRestrictionStillAppliesAfterLootBecomesPublic() {
        Player other=player("ground-other");FloorItem floor=floor(995,7,other,false);
        player.setIronMan(true);unchanged(floor,Native950Containers.Result.STALE);
        FloorItem own=floor(995,3,player,false);assertEquals(3,take(own).moved);
        assertEquals(1,pile.size());assertSame(floor,pile.get(0));
    }
    @Test public void deadLockedTeleportingInactiveAndDistantPlayersCannotTake() {
        FloorItem floor=floor(995,10,player,true);
        player.setHitpoints(0);unchanged(floor,Native950Containers.Result.STALE);player.setHitpoints(100);
        player.lock(20);unchanged(floor,Native950Containers.Result.STALE);player.unlock();
        player.setNextWorldTile(new WorldTile(3213,3215,0));unchanged(floor,Native950Containers.Result.STALE);
        player.setNextWorldTile(null);player.setActive(false);unchanged(floor,Native950Containers.Result.STALE);
        player.setActive(true);player.setLocation(3213,3215,0);unchanged(floor,Native950Containers.Result.STALE);
        player.setLocation(3212,3215,1);unchanged(floor,Native950Containers.Result.STALE);
    }
    @Test public void teleportAnimationFlagBlocksTakeBeforeDestinationIsAssigned() {
        FloorItem floor=floor(995,10,player,true);
        player.getTemporaryAttributtes().put("teleporting",true);unchanged(floor,Native950Containers.Result.STALE);
    }
    @Test public void legacyControllerCanVetoTakeOrAddingTheItem() throws Exception {
        FloorItem floor=floor(995,10,player,true);
        install(new Controller(){public void start(){} @Override public boolean canTakeItem(FloorItem item){return false;}});
        assertEquals(0,take(floor).moved);assertTrue(player.getInventory().items.isEmpty());assertSame(floor,pile.get(0));
        install(new Controller(){public void start(){} @Override public boolean canAddInventoryItem(int id,int amount){return false;}});
        assertEquals(0,take(floor).moved);assertTrue(player.getInventory().items.isEmpty());assertSame(floor,pile.get(0));
    }
    @Test public void receivingUnknownChargedZeroAndNegativeItemsCannotChangeInventory() {
        for(Item item:new Item[]{null,new Item(9999,1),new Item(995,0),new Item(995,-1),new Item(995,1,10)}) {
            assertSame(Native950Containers.Result.INVALID,containers.receiveGroundItem(item));
            assertTrue(player.getInventory().items.isEmpty());
        }
    }
    @Test public void currentCatalogStackMetadataCarriesNotedLootThroughBankAndSave() {
        assertEquals(10,containers.receiveGroundItem(new Item(527,10)).moved);
        assertEquals(10,player.getInventory().items.get(0).getAmount());assertNull(player.getInventory().items.get(1));
        assertEquals(4,containers.deposit(0,527,4).moved);
        Native950Save saved=containers.saveSnapshot(player.getUsername(),tile.getX(),tile.getY(),tile.getPlane());
        Player restored=player("loot-restore");Native950Containers next=new Native950Containers(restored,catalog);next.restore(saved);
        assertEquals(6,restored.getInventory().items.get(0).getAmount());assertEquals(4,restored.getBank().bankTabs[0][0].getAmount());
        assertEquals(4,next.withdraw(0,527,4).moved);assertEquals(10,restored.getInventory().items.get(0).getAmount());
    }
    @Test public void groundInsertionRequiresItsWorldOwnerThread() throws Exception {
        AtomicReference<Throwable> failure=new AtomicReference<Throwable>();
        Thread other=new Thread(()->{try{containers.receiveGroundItem(new Item(995,1));}catch(Throwable e){failure.set(e);}});
        other.start();other.join(5000);assertFalse(other.isAlive());assertTrue(failure.get() instanceof IllegalStateException);
        assertTrue(player.getInventory().items.isEmpty());
    }
    @Test public void nullTakeIsAStaleRequestRatherThanADereferenceFailure() {
        assertSame(Native950Containers.Result.STALE,Native950GroundPickup.take(player,containers,null));
    }
    private void fillBones(int count){for(int i=0;i<count;i++)player.getInventory().items.set(i,new Item(526,1));}
    private Player player(String name){EmbeddedChannel channel=new EmbeddedChannel();channels.add(channel);
        Player value=Player.createNative950(name,tile,channel);value.setActive(true);return value;}
    private FloorItem floor(int id,int amount,Player owner,boolean invisible){
        FloorItem value=new FloorItem(new Item(id,amount),new WorldTile(tile),owner,false,invisible);pile.add(value);return value;}
    private Native950Containers.Result take(FloorItem item){return Native950GroundPickup.take(player,containers,item,pile);}
    private void unchanged(FloorItem floor,Native950Containers.Result expected){
        Native950Containers.Snapshot before=containers.inventorySnapshot();List<FloorItem> old=new ArrayList<FloorItem>(pile);
        assertSame(expected,take(floor));assertArrayEquals(before.ids,containers.inventorySnapshot().ids);
        assertArrayEquals(before.amounts,containers.inventorySnapshot().amounts);assertEquals(old.size(),pile.size());
        for(int i=0;i<old.size();i++)assertSame(old.get(i),pile.get(i));
    }
    private void install(Controller controller)throws Exception {
        controller.setPlayer(player);ControlerManager manager=player.getControlerManager();
        Field field=ControlerManager.class.getDeclaredField("controler");field.setAccessible(true);field.set(manager,controller);
        field=ControlerManager.class.getDeclaredField("inited");field.setAccessible(true);field.setBoolean(manager,true);
    }
}
