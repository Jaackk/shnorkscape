package com.rs.game.player.client;
import com.rs.game.WorldTile;import com.rs.game.item.Item;import com.rs.game.player.Player;import com.rs.game.player.Skills;import io.netty.channel.embedded.EmbeddedChannel;import org.junit.*;import java.util.*;import static org.junit.Assert.*;
public class Native950CraftingActionTest {
 private Player player;private EmbeddedChannel channel;
 @Before public void setup(){channel=new EmbeddedChannel();player=Player.createNative950("sewing-test",new WorldTile(3217,3258,0),channel);player.setActive(true);List<Native950ItemCatalog.Entry> rows=new ArrayList<>();for(int id:new int[]{1733,1734,1741,1059})rows.add(new Native950ItemCatalog.Entry(id,"Item "+id,id==1733||id==1734,new String[5]));Native950Skilling.attach(player,new Native950Containers(player,new Native950ItemCatalog(rows)));}
 @After public void close(){player.getActionManager().forceStop();Native950Skilling.detach(player);channel.finishAndReleaseAll();}
 private void give(int id,int amount){assertTrue(Native950Skilling.giveItem(player,id,amount));}
 private int n(int id){return player.getInventory().items.getNumberOf(id);}
 private Native950Production.Recipe recipe(){return new Native950Production.Recipe("Sew: Leather gloves",Skills.CRAFTING,1,0,-1,1,new Item[]{new Item(1741,1)},new Item[]{new Item(1059,1)},1733,1734);}
 private void run(int ticks){for(int i=0;i<ticks;i++)player.getActionManager().process();}
 @Test public void firstReelSupportsExactlyFiveItemsAndNeedleIsRetained(){give(1733,1);give(1734,1);give(1741,6);assertTrue(player.getActionManager().setAction(new Native950CraftingAction(recipe(),10)));run(60);assertEquals(5,n(1059));assertEquals(1,n(1741));assertEquals(0,n(1734));assertEquals(1,n(1733));assertFalse(player.getActionManager().hasSkillWorking());}
 @Test public void fourItemsDoNotConsumeFirstReelAndChosenAmountStops(){give(1733,1);give(1734,1);give(1741,6);assertTrue(player.getActionManager().setAction(new Native950CraftingAction(recipe(),4)));run(40);assertEquals(4,n(1059));assertEquals(2,n(1741));assertEquals(1,n(1734));}
 @Test public void lostThreadOrCancelledActionCannotProduce(){give(1733,1);give(1734,1);give(1741,2);assertTrue(player.getActionManager().setAction(new Native950CraftingAction(recipe(),2)));player.getInventory().items.set(player.getInventory().items.lookupSlot(1734),null);run(10);assertEquals(2,n(1741));assertEquals(0,n(1059));give(1734,1);assertTrue(player.getActionManager().setAction(new Native950CraftingAction(recipe(),2)));player.getActionManager().forceStop();run(10);assertEquals(2,n(1741));assertEquals(0,n(1059));}
 @Test public void fullBackpackReplacesLeatherWithoutRequiringSpareSlot(){give(1733,1);give(1734,1);give(1741,26);assertEquals(0,player.getInventory().getFreeSlots());assertTrue(player.getActionManager().setAction(new Native950CraftingAction(recipe(),5)));run(40);assertEquals(5,n(1059));assertEquals(21,n(1741));assertEquals(0,n(1734));}
}
