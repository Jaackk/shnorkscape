package com.rs.game.player.client;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.ControlerManager;
import com.rs.game.player.controllers.Controller;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.*;
import java.lang.reflect.Field;
import org.junit.*;
import static org.junit.Assert.*;
public class Native950ProductionTest {
    private Player player;private EmbeddedChannel channel;private Native950Containers containers;
    @Before public void setup(){
        channel=new EmbeddedChannel();player=Player.createNative950("production-test",new WorldTile(3217,3258,0),channel);player.setActive(true);
        List<Native950ItemCatalog.Entry> types=new ArrayList<>();
        for(int id:new int[]{1511,50,946,4162,199,249,227,91,221,121,52,53,314,995})
            types.add(new Native950ItemCatalog.Entry(id,"Item "+id,id==52||id==53||id==314||id==995,new String[5]));
        containers=new Native950Containers(player,new Native950ItemCatalog(types));Native950Skilling.attach(player,containers);
    }
    @After public void cleanup(){player.getActionManager().forceStop();Native950Skilling.detach(player);channel.finishAndReleaseAll();}
    private Item[] items(int id,int n){return new Item[]{new Item(id,n)};}
    private void put(int slot,int id,int n){player.getInventory().items.set(slot,new Item(id,n));}
    private int amount(int id){return player.getInventory().items.getNumberOf(id);}
    @Test public void fullBagCanReplaceIngredientWithoutAnExtraEmptySlot(){
        for(int i=0;i<28;i++)put(i,199,1);
        assertTrue(Native950Skilling.canExchange(player,items(199,1),items(249,1)));
        assertEquals(28,amount(199));assertEquals(0,amount(249));
        assertTrue(Native950Skilling.exchange(player,items(199,1),items(249,1)));
        assertEquals(27,amount(199));assertEquals(1,amount(249));
    }
    @Test public void failedMultiOutputRollsBackConsumedAndEarlierOutput(){
        for(int i=0;i<28;i++)put(i,199,1);
        assertFalse(Native950Skilling.exchange(player,items(199,1),new Item[]{new Item(249,1),new Item(227,1)}));
        assertEquals(28,amount(199));assertEquals(0,amount(249));assertEquals(0,amount(227));
    }
    @Test public void duplicateInputsAreCountedTogetherAndCannotDoubleSpend(){
        put(0,199,1);Item[] input={new Item(199,1),new Item(199,1)};
        assertFalse(Native950Skilling.exchange(player,input,items(249,1)));assertEquals(1,amount(199));
        put(1,199,1);assertTrue(Native950Skilling.exchange(player,input,items(249,1)));
        assertEquals(0,amount(199));assertEquals(1,amount(249));
    }
    @Test public void previewDoesNotMutateStacksAndOverflowCannotDestroyMaterials(){
        put(0,52,Integer.MAX_VALUE);put(1,1511,1);
        assertFalse(Native950Skilling.canExchange(player,items(1511,1),items(52,15)));
        assertFalse(Native950Skilling.exchange(player,items(1511,1),items(52,15)));
        assertEquals(Integer.MAX_VALUE,amount(52));assertEquals(1,amount(1511));
        assertTrue(Native950Skilling.canExchange(player,items(52,15),items(53,15)));
        assertEquals(Integer.MAX_VALUE,amount(52));assertEquals(0,amount(53));
    }
    @Test public void prayerStyleEmptyOutputIsValidAndEmptyInputIsNot(){
        put(0,199,1);assertTrue(Native950Skilling.exchange(player,items(199,1),new Item[0]));
        assertEquals(0,amount(199));assertFalse(Native950Skilling.exchange(player,new Item[0],items(249,1)));
    }
    @Test public void controllerCanRefuseProductBeforeInputsAreCommitted()throws Exception{
        put(0,199,1);Controller deny=new Controller(){public void start(){}public boolean canAddInventoryItem(int id,int n){return false;}};
        Field f=ControlerManager.class.getDeclaredField("controler");f.setAccessible(true);f.set(player.getControlerManager(),deny);
        f=ControlerManager.class.getDeclaredField("inited");f.setAccessible(true);f.setBoolean(player.getControlerManager(),true);
        assertFalse(Native950Skilling.exchange(player,items(199,1),items(249,1)));
        assertEquals(1,amount(199));assertEquals(0,amount(249));
    }
    private Native950Production.Recipe recipe(){return new Native950Production.Recipe("Shafts",Skills.FLETCHING,1,0,-1,1,items(1511,1),items(52,15),946);}
    @Test public void originalActionManagerRepeatsThenStopsAtChosenAmountAndKeepsTool(){
        put(0,946,1);put(1,1511,1);put(2,1511,1);put(3,1511,1);
        assertTrue(player.getActionManager().setAction(new Native950ProductionAction(recipe(),2)));
        for(int i=0;i<20;i++)player.getActionManager().process();
        assertEquals(30,amount(52));assertEquals(1,amount(1511));assertEquals(1,amount(946));
        assertFalse(player.getActionManager().hasSkillWorking());
    }
    @Test public void cancellationAndLostToolPreventFutureCompletions(){
        put(0,946,1);put(1,1511,1);
        assertTrue(player.getActionManager().setAction(new Native950ProductionAction(recipe(),2)));
        player.getActionManager().forceStop();for(int i=0;i<10;i++)player.getActionManager().process();
        assertEquals(1,amount(1511));assertEquals(0,amount(52));
        // Basic knives are now permanently available. An optional tool still has to remain present.
        put(0,4162,1);assertFalse(Native950Toolbelt.has(player,4162));
        Native950Production.Recipe optional=new Native950Production.Recipe("Optional tool",Skills.FLETCHING,1,0,-1,1,items(1511,1),items(52,15),4162);
        assertTrue(player.getActionManager().setAction(new Native950ProductionAction(optional,2)));
        player.getInventory().items.set(0,null);for(int i=0;i<10;i++)player.getActionManager().process();
        assertEquals(1,amount(1511));assertEquals(0,amount(52));
    }
    @Test public void originalFletchingBatchAndRecipeLevelsAreRetained(){
        Native950Production.Recipe logs=Native950Production.recipes().stream().filter(r->r.label.equals("Fletch REGULAR_BOW 0")).findFirst().get();
        assertEquals(1,logs.consumed()[0].getAmount());assertEquals(15,logs.produced()[0].getAmount());assertEquals(12,logs.xp,0.0001);
        Native950Production.Recipe arrows=Native950Production.recipes().stream().filter(r->r.label.equals("Fletch BRONZE_ARROWS 0")).findFirst().get();
        assertEquals(15,arrows.consumed()[0].getAmount());assertEquals(15,arrows.consumed()[1].getAmount());assertEquals(15,arrows.produced()[0].getAmount());
        assertFalse(Native950Production.recipes().stream().anyMatch(r->r.label.startsWith("Mix TORSTOL ")&&r.consumed()[1].getId()!=227));
    }
}
