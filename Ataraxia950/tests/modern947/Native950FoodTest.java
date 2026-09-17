package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Foods.Food;
import com.rs.game.player.controllers.Controller;
import com.rs.utils.Utils;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950FoodTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Containers containers;
    private final Native950ItemCatalog.Entry shrimp = food(315,"Shrimps");
    private final Native950ItemCatalog.Entry pie = food(2325,"Redberry pie");
    private final Native950ItemCatalog.Entry half = food(2333,"Half a redberry pie");
    private final Native950ItemCatalog.Entry dish = new Native950ItemCatalog.Entry(2313,"Pie dish",false,new String[5]);
    private final Native950ItemCatalog.Entry karam = food(3144,"Cooked karambwan");
    @Before public void setup() {
        channel = new EmbeddedChannel();
        player = Player.createNative950("food-test",new WorldTile(3217,3258,0),channel);
        player.setActive(true); player.setHitpoints(10);
        containers = new Native950Containers(player,new Native950ItemCatalog(Arrays.asList(shrimp,pie,half,dish,karam)));
        player.getInventory().items.set(0,new Item(315,1));
        player.getInventory().items.set(1,new Item(315,1));
    }
    @After public void cleanup() { channel.finishAndReleaseAll(); }
    private Native950Food.Result eat(int slot,Native950ItemCatalog.Entry type) {
        return Native950Food.eat(player,containers,slot,type.id,type);
    }
    @Test public void ordinaryFoodUsesOriginalHealingAndExactClickedSlotWithCooldown() {
        assertEquals(100,player.getMaxHitpoints());
        assertTrue(eat(1,shrimp).accepted);
        assertNotNull(player.getInventory().getItem(0)); assertNull(player.getInventory().getItem(1));
        assertEquals(10 + Food.SHRIMP.getHeal(player)*10,player.getHitpoints());
        assertEquals(18001,player.getNextAnimation().getIds()[0]);
        assertEquals(4,player.getActionManager().getActionDelay());
        Native950Food.Result repeat=eat(0,shrimp);
        assertFalse(repeat.accepted); assertTrue(repeat.reason.contains("wait"));
        assertNotNull(player.getInventory().getItem(0)); assertEquals(40,player.getHitpoints());
    }
    @Test public void healingCapsAtMaxAndStillConsumesAtFullHealthLikeOriginal() {
        player.setHitpoints(95); assertTrue(eat(0,shrimp).accepted); assertEquals(100,player.getHitpoints());
        player.addFoodDelay(-10000); assertTrue(eat(1,shrimp).accepted);
        assertEquals(100,player.getHitpoints()); assertNull(player.getInventory().getItem(1));
    }
    @Test public void fullBagKeepsTheClickedPiePortionAndDishInTheSameSlot() {
        for(int i=0;i<28;i++)player.getInventory().items.set(i,new Item(315,1));
        player.getInventory().items.set(5,new Item(2325,1));
        player.getInventory().items.set(9,new Item(2325,1));
        assertTrue(eat(9,pie).accepted);
        assertEquals(2325,player.getInventory().getItem(5).getId());
        assertEquals(2333,player.getInventory().getItem(9).getId());
        assertEquals(0,player.getInventory().getFreeSlots());
        player.addFoodDelay(-10000);
        long before=Utils.currentTimeMillis(); assertTrue(eat(9,half).accepted);
        assertEquals(2313,player.getInventory().getItem(9).getId());
        assertEquals(3,player.getActionManager().getActionDelay());
        assertTrue(player.getFoodDelay()>=before+800);
        assertTrue(player.getFoodDelay()<=Utils.currentTimeMillis()+800);
    }
    @Test public void allOriginalControllerGatesRefuseBeforeItemHealthOrDelayChanges() {
        for(int veto=0;veto<4;veto++) {
            final int selected=veto;
            player.getControlerManager().startControler(new Controller() {
                public void start() { }
                @Override public boolean handleItemOption1(Item item,int id,int slot) { return selected!=0; }
                @Override public boolean canEat(Food food) { return selected!=1; }
                @Override public boolean canDeleteInventoryItem(int id,int amount) { return selected!=2; }
                @Override public boolean canAddInventoryItem(int id,int amount) { return selected!=3; }
            });
            player.getInventory().items.set(5,new Item(2325,1));
            assertFalse(eat(5,pie).accepted);
            assertEquals(2325,player.getInventory().getItem(5).getId());
            assertEquals(10,player.getHitpoints()); assertEquals(0,player.getFoodDelay());
            assertNull(player.getNextAnimation());
        }
    }
    @Test public void missingLeftoverMetadataNeverConsumesFood() {
        containers = new Native950Containers(player,new Native950ItemCatalog(Arrays.asList(shrimp,pie)));
        player.getInventory().items.set(5,new Item(2325,1));
        assertFalse(eat(5,pie).accepted);
        assertEquals(2325,player.getInventory().getItem(5).getId());
        assertEquals(10,player.getHitpoints()); assertEquals(0,player.getFoodDelay());
    }
    @Test public void staleSlotControllerMutationDeadLockedAndLogoutDoNotConsume() {
        assertFalse(eat(5,shrimp).accepted);
        assertFalse(Native950Food.eat(player,containers,0,315,null).accepted);
        player.lock(2); assertFalse(eat(0,shrimp).accepted); player.unlock();
        player.setActive(false); assertFalse(eat(0,shrimp).accepted); player.setActive(true);
        player.setHitpoints(0); assertFalse(eat(0,shrimp).accepted); player.setHitpoints(10);
        player.getControlerManager().startControler(new Controller() {
            public void start() { }
            @Override public boolean handleItemOption1(Item item,int id,int slot) {
                player.getInventory().items.set(slot,new Item(2325,1)); return true;
            }
        });
        assertFalse(eat(0,shrimp).accepted);
        assertEquals(2325,player.getInventory().getItem(0).getId());
        assertEquals(315,player.getInventory().getItem(1).getId());
        assertEquals(10,player.getHitpoints()); assertEquals(0,player.getFoodDelay());
    }
    @Test public void specialFoodsAndAnotherPlayersContainerAreNotAdmitted() {
        for(int id:new int[]{7218,9475,19949,19948,10476,19467,14162,527})assertFalse(Native950Food.supports(id));
        assertFalse(eat(0,food(7218,"Summer pie")).accepted);
        assertFalse(Native950Food.eat(player,null,0,315,shrimp).accepted);
        Player other=Player.createNative950("other-food-test",new WorldTile(3217,3258,0),channel);other.setActive(true);
        Native950Containers foreign=new Native950Containers(other,new Native950ItemCatalog(Arrays.asList(shrimp)));
        assertFalse(Native950Food.eat(player,foreign,0,315,shrimp).accepted);
        assertEquals(2,player.getInventory().getAmountOf(315)); assertEquals(10,player.getHitpoints());
    }
    @Test public void karambwanUsesSeparateOriginalCooldownAndCombatEatingAnimation() {
        player.getInventory().items.set(3,new Item(3144,1));
        player.getInventory().items.set(4,new Item(3144,1));
        player.addFoodDelay(10000); player.setAttackedByDelay(Utils.currentTimeMillis()+1000);
        assertTrue(eat(3,karam).accepted); assertFalse(eat(4,karam).accepted);
        assertEquals(18002,player.getNextAnimation().getIds()[0]);
        assertNotNull(player.getInventory().getItem(4));
    }
    private static Native950ItemCatalog.Entry food(int id,String name) {
        return new Native950ItemCatalog.Entry(id,name,false,new String[]{"Eat",null,null,null,"drop"});
    }
}