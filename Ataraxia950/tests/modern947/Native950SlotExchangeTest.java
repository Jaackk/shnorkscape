package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.invention.InventionData;
import com.rs.game.player.controllers.Controller;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Selected inventory resources cannot be replaced by another duplicate during an action. */
public final class Native950SlotExchangeTest {
    private Player player; private EmbeddedChannel channel; private Native950Containers containers;
    @Before public void setup() {
        channel = new EmbeddedChannel();
        player = Player.createNative950("slot-exchange", new WorldTile(3217,3258,0), channel);
        player.setActive(true);
        List<Native950ItemCatalog.Entry> types = new ArrayList<>();
        for (int id : new int[]{11238,11260,946,1511,995})
            types.add(new Native950ItemCatalog.Entry(id,"Item " + id,id == 995,new String[5]));
        containers = new Native950Containers(player,new Native950ItemCatalog(types));
        Native950Skilling.attach(player,containers);
    }
    @After public void cleanup() {
        player.getActionManager().forceStop(); Native950Skilling.detach(player); channel.finishAndReleaseAll();
    }
    private Item put(int slot,int id,int amount) {
        Item item = new Item(id,amount); player.getInventory().items.set(slot,item); return item;
    }
    private Item[] output() { return new Item[]{new Item(11260,1),new Item(946,1)}; }
    private boolean loot(int slot) { return Native950Skilling.exchangeSlot(player,slot,11238,1,output()); }
    private int amount(int id) { return player.getInventory().items.getNumberOf(id); }

    @Test public void duplicateJarsConsumeOnlySelectedSlotAndStaleRepeatCannotSpendAnotherJar() {
        Item first = put(0,11238,1), clicked = put(5,11238,1);
        assertTrue(Native950Skilling.canExchangeSlot(player,5,11238,1,output()));
        assertSame(clicked,player.getInventory().getItem(5)); assertTrue(loot(5));
        assertSame(first,player.getInventory().getItem(0));
        assertEquals(1,amount(11238)); assertEquals(1,amount(11260)); assertEquals(1,amount(946));
        assertFalse(loot(5)); assertSame(first,player.getInventory().getItem(0));
        assertEquals(1,amount(11260)); assertEquals(1,amount(946));
    }
    @Test public void fullBagFailureAndStackOverflowLeaveSourceAndAllOutputsUnchanged() {
        for(int i=0;i<28;i++)put(i,1511,1); Item clicked=put(5,11238,1);
        assertFalse(Native950Skilling.canExchangeSlot(player,5,11238,1,output())); assertFalse(loot(5));
        assertSame(clicked,player.getInventory().getItem(5)); assertEquals(0,amount(11260));assertEquals(0,amount(946));
        put(1,995,Integer.MAX_VALUE);
        assertFalse(Native950Skilling.exchangeSlot(player,5,11238,1,new Item[]{new Item(11260,1),new Item(995,1)}));
        assertSame(clicked,player.getInventory().getItem(5));assertEquals(Integer.MAX_VALUE,amount(995));assertEquals(0,amount(11260));
        assertTrue(Native950Skilling.exchangeSlot(player,5,11238,1,new Item[]{new Item(11260,1)}));
        assertEquals(11260,player.getInventory().getItem(5).getId());
    }
    @Test public void partialStackExchangePreservesUnconsumedQuantityAndPreviewHasNoSideEffects() {
        Item stack=put(5,995,20);
        assertTrue(Native950Skilling.canExchangeSlot(player,5,995,7,new Item[]{new Item(11260,1)}));
        assertSame(stack,player.getInventory().getItem(5));assertEquals(20,stack.getAmount());
        assertTrue(Native950Skilling.exchangeSlot(player,5,995,7,new Item[]{new Item(11260,1)}));
        assertEquals(13,amount(995));assertEquals(1,amount(11260));
    }
    @Test public void wrongSlotIdAndAmountNeverConsume() {
        Item clicked=put(5,11238,1);
        assertFalse(loot(-1));assertFalse(loot(28));assertFalse(loot(4));
        assertFalse(Native950Skilling.exchangeSlot(player,5,11260,1,output()));
        assertFalse(Native950Skilling.exchangeSlot(player,5,11238,2,output()));
        assertFalse(Native950Skilling.exchangeSlot(player,5,11238,0,output()));
        assertSame(clicked,player.getInventory().getItem(5));assertEquals(0,amount(946));
    }
    @Test public void deleteAndSecondOutputControllerVetoNeverPartiallyCommit() {
        Item clicked=put(5,11238,1);
        player.getControlerManager().startControler(new Controller(){public void start(){}public boolean canDeleteInventoryItem(int id,int n){return false;}});
        assertFalse(loot(5));assertSame(clicked,player.getInventory().getItem(5));
        player.getControlerManager().forceStop();
        player.getControlerManager().startControler(new Controller(){public void start(){}public boolean canAddInventoryItem(int id,int n){return id!=946;}});
        assertFalse(loot(5));assertSame(clicked,player.getInventory().getItem(5));assertEquals(0,amount(11260));assertEquals(0,amount(946));
    }
    @Test public void sameIdReplacementDuringControllerCannotBeConsumed() {
        Item first=put(0,11238,1);put(5,11238,1);Item replacement=new Item(11238,1);
        player.getControlerManager().startControler(new Controller(){public void start(){}public boolean canAddInventoryItem(int id,int n){player.getInventory().items.set(5,replacement);return true;}});
        assertFalse(loot(5));assertSame(first,player.getInventory().getItem(0));assertSame(replacement,player.getInventory().getItem(5));assertEquals(0,amount(946));
    }
    @Test public void controllerQuantityOrIdChangesArePreservedAndCancelExchange() {
        Item stack=put(5,995,20);
        player.getControlerManager().startControler(new Controller(){public void start(){}public boolean canDeleteInventoryItem(int id,int n){stack.setAmount(21);return true;}});
        assertFalse(Native950Skilling.exchangeSlot(player,5,995,7,new Item[]{new Item(11260,1)}));
        assertSame(stack,player.getInventory().getItem(5));assertEquals(21,amount(995));assertEquals(0,amount(11260));
        player.getControlerManager().forceStop();Item clicked=put(5,11238,1);
        player.getControlerManager().startControler(new Controller(){public void start(){}public boolean canDeleteInventoryItem(int id,int n){clicked.setId(1511);return true;}});
        assertFalse(loot(5));assertSame(clicked,player.getInventory().getItem(5));assertEquals(1511,clicked.getId());assertEquals(0,amount(946));
    }
    @Test public void specialSourceAndOutputStateAreNeverErased() {
        Item selected=put(5,11238,1);selected.setCharges(7);
        assertFalse(loot(5));assertSame(selected,player.getInventory().getItem(5));assertEquals(7,selected.getCharges());
        selected.setCharges(0);InventionData data=new InventionData(4321.5);selected.setInventionData(data);
        assertFalse(loot(5));assertSame(data,selected.getInventionData());
        selected=put(5,11238,1);Item product=new Item(11260,1);product.setInventionData(data);
        assertFalse(Native950Skilling.exchangeSlot(player,5,11238,1,new Item[]{product}));assertSame(selected,player.getInventory().getItem(5));
    }
    @Test public void controllerAddedSpecialStateCancelsWithoutDeletingIt() {
        Item selected=put(5,11238,1);InventionData data=new InventionData(4321.5);
        player.getControlerManager().startControler(new Controller(){public void start(){}public boolean canAddInventoryItem(int id,int n){selected.setInventionData(data);return true;}});
        assertFalse(loot(5));assertSame(selected,player.getInventory().getItem(5));assertSame(data,selected.getInventionData());assertEquals(0,amount(946));
    }
}
