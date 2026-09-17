package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.ControlerManager;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real container, floor storage and native pickup transactions; no old cache definitions. */
public final class Native950InventoryDropTest {
    private final WorldTile tile = new WorldTile(3212,3215,0);
    private final List<EmbeddedChannel> channels = new ArrayList<EmbeddedChannel>();
    private final List<FloorItem> scheduled = new ArrayList<FloorItem>();
    private final Native950ItemCatalog catalog = new Native950ItemCatalog(Arrays.asList(
            entry(995,true,"Drop"), entry(526,false,"Drop"), entry(527,true,"Drop"),
            entry(100,false,"Destroy"), entry(101,false,"Discard"), entry(102,false,null)));
    private Player player;
    private Native950Containers containers;
    private List<FloorItem> pile;

    @Before public void setup() {
        player = player("drop-owner"); containers = new Native950Containers(player,catalog);
        pile = new ArrayList<FloorItem>();
    }
    @After public void cleanup() {
        for (EmbeddedChannel channel : channels) channel.finishAndReleaseAll();
    }

    @Test public void selectedDuplicateNonStackableSlotIsTheOnlySlotRemoved() {
        Item first = new Item(526,1), selected = new Item(526,1);
        player.getInventory().items.set(2,first); player.getInventory().items.set(17,selected);
        Native950InventoryDrop.Result result = drop(17,526);
        assertEquals(1,result.moved); assertNull(result.reason); assertSame(first,player.getInventory().items.get(2));
        assertNull(player.getInventory().items.get(17)); assertEquals(1,pile.size());
        assertSame(result.groundItem,pile.get(0)); assertEquals(1,result.groundItem.getAmount());
        assertSame(result.groundItem,scheduled.get(0)); assertTrue(result.groundItem.isNative950());
        assertTrue(result.groundItem.isInvisible()); assertEquals(player.getUsername(),result.groundItem.getOwner());
        assertTrue(result.groundItem.getTile().matches(tile));
    }

    @Test public void entireStackIncludingNotedItemsTransfersAndDuplicateRequestDoesNothing() {
        for (int id : new int[]{995,527}) {
            player.getInventory().items.set(9,new Item(id,314159));
            Native950InventoryDrop.Result result = drop(9,id);
            assertEquals(314159,result.moved); assertEquals(314159,result.groundItem.getAmount());
            assertNull(player.getInventory().items.get(9));
            assertEquals(0,drop(9,id).moved);
        }
        assertEquals(2,pile.size()); assertEquals(2,scheduled.size());
    }

    @Test public void staleAndOutOfRangeRequestsLeaveAllStorageUntouched() {
        Item held = new Item(526,1); player.getInventory().items.set(2,held);
        for (int[] request : new int[][]{{-1,526},{28,526},{2,995},{1,526}}) {
            Native950InventoryDrop.Result result = drop(request[0],request[1]);
            assertEquals(0,result.moved); assertNotNull(result.reason);
        }
        assertSame(held,player.getInventory().items.get(2)); assertTrue(pile.isEmpty()); assertTrue(scheduled.isEmpty());
    }

    @Test public void destroyDiscardAndAbsentDropLabelsNeverDeleteTheItem() {
        for (int id : new int[]{100,101,102}) {
            Item held = new Item(id,1); player.getInventory().items.set(2,held);
            assertEquals(0,drop(2,id).moved); assertSame(held,player.getInventory().items.get(2));
        }
        assertTrue(pile.isEmpty());
    }

    @Test public void chargedAndAttributeBearingItemsAreExplicitlyRefused() {
        Item charged = new Item(526,1,10); player.getInventory().items.set(2,charged);
        assertEquals(0,drop(2,526).moved); assertSame(charged,player.getInventory().items.get(2));
        Item attributed = new Item(526,1);
        ConcurrentHashMap<TemporaryAttributes.Key,Object> attributes = new ConcurrentHashMap<>();
        attributes.put(TemporaryAttributes.Key.ITEM_INVENTION_DATA,"fixture");
        attributed.setAttributes(attributes); player.getInventory().items.set(2,attributed);
        assertEquals(0,drop(2,526).moved); assertSame(attributed,player.getInventory().items.get(2));
        assertTrue(pile.isEmpty());
    }

    @Test public void inventionExperienceAndControllerAddedInventionDataAreNeverErased() throws Exception {
        com.rs.game.player.actions.invention.InventionData data =
                new com.rs.game.player.actions.invention.InventionData(4321.5);
        Item augmented = new Item(526,1); augmented.setInventionData(data);
        player.getInventory().items.set(2,augmented);
        Native950InventoryDrop.Result refused = drop(2,526);
        assertEquals(0,refused.moved); assertTrue(refused.reason.contains("special state"));
        assertSame(augmented,player.getInventory().items.get(2)); assertSame(data,augmented.getInventionData());
        assertTrue(pile.isEmpty()); assertTrue(scheduled.isEmpty());

        Item plain = new Item(526,1); player.getInventory().items.set(2,plain);
        install(new Controller() { public void start(){} @Override public boolean canDropItem(Item item) {
            item.setInventionData(data); return true;
        } });
        assertEquals(0,drop(2,526).moved); assertSame(plain,player.getInventory().items.get(2));
        assertSame(data,plain.getInventionData()); assertTrue(pile.isEmpty()); assertTrue(scheduled.isEmpty());
    }

    @Test public void bothOriginalControllerGatesCanVetoWithoutMutation() throws Exception {
        Item held = new Item(995,19); player.getInventory().items.set(2,held);
        install(new Controller() { public void start(){} @Override public boolean canDropItem(Item item){return false;} });
        assertEquals(0,drop(2,995).moved); assertSame(held,player.getInventory().items.get(2));
        install(new Controller() { public void start(){} @Override public boolean canDeleteInventoryItem(int id,int amount){
            assertEquals(995,id); assertEquals(19,amount); return false;
        } });
        assertEquals(0,drop(2,995).moved); assertSame(held,player.getInventory().items.get(2));
        assertTrue(pile.isEmpty()); assertTrue(scheduled.isEmpty());
    }

    @Test public void controllerReplacedEqualItemCannotBeConsumedByTheOriginalRequest() throws Exception {
        Item held = new Item(526,1), replacement = new Item(526,1);
        player.getInventory().items.set(2,held);
        install(new Controller() { public void start(){} @Override public boolean canDropItem(Item item) {
            player.getInventory().items.set(2,replacement); return true;
        } });
        assertEquals(0,drop(2,526).moved); assertSame(replacement,player.getInventory().items.get(2)); assertTrue(pile.isEmpty());
    }

    @Test public void controllerChangedSameIdentityIdOrAmountCannotDropStaleData() throws Exception {
        Item held = new Item(995,10); player.getInventory().items.set(2,held);
        install(new Controller() { public void start(){} @Override public boolean canDropItem(Item item) {
            item.setId(527); return true;
        } });
        assertEquals(0,drop(2,995).moved); assertSame(held,player.getInventory().items.get(2)); assertTrue(pile.isEmpty());
        held.setId(995);
        install(new Controller() { public void start(){} @Override public boolean canDropItem(Item item) {
            item.setAmount(7); return true;
        } });
        assertEquals(0,drop(2,995).moved); assertEquals(7,held.getAmount()); assertTrue(pile.isEmpty());
    }

    @Test public void deadLockedInactiveAndTeleportingPlayersCannotDrop() {
        player.getInventory().items.set(2,new Item(526,1));
        player.setHitpoints(0); assertEquals(0,drop(2,526).moved); player.setHitpoints(100);
        player.lock(20); assertEquals(0,drop(2,526).moved); player.unlock();
        player.setActive(false); assertEquals(0,drop(2,526).moved); player.setActive(true);
        player.setNextWorldTile(new WorldTile(3213,3215,0)); assertEquals(0,drop(2,526).moved);
        player.setNextWorldTile(null); player.getTemporaryAttributtes().put("teleporting",true);
        assertEquals(0,drop(2,526).moved); assertEquals(526,player.getInventory().items.get(2).getId()); assertTrue(pile.isEmpty());
    }

    @Test public void anotherPlayersContainerCannotDeleteEitherInventory() {
        player.getInventory().items.set(2,new Item(526,1));
        Player other = player("other"); Native950Containers otherContainers = new Native950Containers(other,catalog);
        assertEquals(0,Native950InventoryDrop.drop(player,otherContainers,2,526,t -> pile,item -> true,scheduled::add).moved);
        assertEquals(526,player.getInventory().items.get(2).getId()); assertTrue(other.getInventory().items.isEmpty());
        assertTrue(pile.isEmpty());
    }

    @Test public void failedGroundStorageRestoresExactSlotAndItemIdentity() {
        Item held = new Item(526,1); player.getInventory().items.set(17,held);
        List<FloorItem> immutable = Collections.emptyList();
        assertEquals(0,Native950InventoryDrop.drop(player,containers,17,526,t -> immutable,item -> true,scheduled::add).moved);
        assertSame(held,player.getInventory().items.get(17)); assertTrue(scheduled.isEmpty());
    }

    @Test public void publisherFailureAfterInsertionRollsBackOnlyItsOwnEqualPile() {
        Item held = new Item(526,1); player.getInventory().items.set(17,held);
        FloorItem existing = new FloorItem(new Item(526,1),tile,player,false,true); pile.add(existing);
        Native950InventoryDrop.Result result = Native950InventoryDrop.drop(player,containers,17,526,t -> pile,item -> true,
                floor -> { throw new IllegalStateException("fixture publisher failed"); });
        assertEquals(0,result.moved); assertSame(held,player.getInventory().items.get(17));
        assertEquals(1,pile.size()); assertSame(existing,pile.get(0));
    }

    @Test public void ownerCanPickUpDroppedItemAndNoDuplicateCanBeTaken() {
        player.getInventory().items.set(17,new Item(995,100));
        FloorItem floor = drop(17,995).groundItem;
        Player other = player("other"); Native950Containers otherContainers = new Native950Containers(other,catalog);
        assertEquals(0,Native950GroundPickup.take(other,otherContainers,floor,pile).moved);
        assertEquals(100,Native950GroundPickup.take(player,containers,floor,pile).moved);
        assertTrue(pile.isEmpty()); assertEquals(0,Native950GroundPickup.take(player,containers,floor,pile).moved);
        assertEquals(100,player.getInventory().items.get(0).getAmount());
    }

    @Test public void publicUntradeableRemainsOwnerOnly() {
        player.getInventory().items.set(17,new Item(526,1));
        FloorItem floor = Native950InventoryDrop.drop(player,containers,17,526,t -> pile,item -> false,scheduled::add).groundItem;
        assertFalse(floor.isPublicTransferAllowed()); floor.setInvisible(false);
        Player other = player("other"); Native950Containers otherContainers = new Native950Containers(other,catalog);
        assertEquals(0,Native950GroundPickup.take(other,otherContainers,floor,pile).moved);
        assertEquals(1,Native950GroundPickup.take(player,containers,floor,pile).moved);
    }

    @Test public void originalWorldTasksPublishThenExpireDroppedPileAfterOwnerLeaves() {
        List<FloorItem> realPile = World.getRegion(tile.getRegionId()).getGroundItemsSafe();
        player.getInventory().items.set(17,new Item(526,1));
        FloorItem floor = Native950InventoryDrop.drop(player,containers,17,526,t -> realPile,item -> false,
                Native950InventoryDrop::scheduleLifetime).groundItem;
        try {
            assertTrue(floor.isInvisible()); assertTrue(realPile.stream().anyMatch(item -> item == floor));
            player.setActive(false);
            ticks(Utils.secondsToTicks(Native950Loot.PRIVATE_SECONDS));
            assertTrue(floor.isInvisible());
            ticks(1);
            assertFalse(floor.isInvisible()); assertTrue(realPile.stream().anyMatch(item -> item == floor));
            assertFalse(floor.isPublicTransferAllowed());
            ticks(Utils.secondsToTicks(Native950Loot.PUBLIC_SECONDS) + 1);
            assertFalse(realPile.stream().anyMatch(item -> item == floor));
        } finally { realPile.removeIf(item -> item == floor); }
    }

    @Test public void pickedUpPilesExpiryCannotRemoveAnEqualReplacement() {
        List<FloorItem> realPile = World.getRegion(tile.getRegionId()).getGroundItemsSafe();
        player.getInventory().items.set(17,new Item(526,1));
        FloorItem dropped = Native950InventoryDrop.drop(player,containers,17,526,t -> realPile,item -> true,
                Native950InventoryDrop::scheduleLifetime).groundItem;
        assertEquals(1,Native950GroundPickup.take(player,containers,dropped,realPile).moved);
        FloorItem replacement = new FloorItem(new Item(526,1),tile,player,false,true);
        realPile.add(replacement);
        try {
            ticks(Utils.secondsToTicks(Native950Loot.PRIVATE_SECONDS + Native950Loot.PUBLIC_SECONDS) + 4);
            assertTrue(replacement.isInvisible()); assertTrue(realPile.stream().anyMatch(item -> item == replacement));
        } finally { realPile.removeIf(item -> item == dropped || item == replacement); }
    }

    @Test public void dropRequiresTheContainersOwnerThread() throws Exception {
        player.getInventory().items.set(2,new Item(526,1));
        AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        Thread thread = new Thread(() -> { try { drop(2,526); } catch(Throwable problem){ failure.set(problem); } });
        thread.start(); thread.join(5000);
        assertFalse(thread.isAlive()); assertTrue(failure.get() instanceof IllegalStateException);
        assertEquals(526,player.getInventory().items.get(2).getId()); assertTrue(pile.isEmpty());
    }

    private Native950InventoryDrop.Result drop(int slot,int id) {
        return Native950InventoryDrop.drop(player,containers,slot,id,t -> pile,item -> true,scheduled::add);
    }
    private static Native950ItemCatalog.Entry entry(int id,boolean stackable,String last) {
        return new Native950ItemCatalog.Entry(id,"Fixture "+id,stackable,new String[]{null,null,null,null,last});
    }
    private Player player(String name) {
        EmbeddedChannel channel = new EmbeddedChannel(); channels.add(channel);
        Player result = Player.createNative950(name,tile,channel); result.setActive(true); return result;
    }
    private static void ticks(int count) { for(int tick=0;tick<count;tick++) WorldTasksManager.processTasks(); }
    private void install(Controller controller) throws Exception {
        controller.setPlayer(player);
        Field field = ControlerManager.class.getDeclaredField("controler"); field.setAccessible(true);
        field.set(player.getControlerManager(),controller);
        field = ControlerManager.class.getDeclaredField("inited"); field.setAccessible(true);
        field.setBoolean(player.getControlerManager(),true);
    }
}
