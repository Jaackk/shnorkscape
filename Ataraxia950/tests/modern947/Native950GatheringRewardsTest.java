package com.rs.game.player.client;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
public class Native950GatheringRewardsTest {
 Player p;EmbeddedChannel c;Native950Containers items;
 @Before public void setup(){c=new EmbeddedChannel();p=Player.createNative950("gather",new WorldTile(3217,3258,0),c);p.setActive(true);
  items=new Native950Containers(p,new Native950ItemCatalog(Arrays.asList(new Native950ItemCatalog.Entry(995,"coins",true,new String[5]),new Native950ItemCatalog.Entry(1511,"logs",false,new String[5]))));Native950Skilling.attach(p,items);}
 @After public void close(){Native950Skilling.detach(p);c.finishAndReleaseAll();}
 @Test public void lastFreeSlotCannotGrantOnlyFirstOutput(){for(int i=0;i<27;i++)p.getInventory().items.set(i,new Item(1511,1));Item[] rewards={new Item(995,5),new Item(1511,1)};
  assertFalse(Native950Skilling.canGiveItems(p,rewards));assertFalse(Native950Skilling.giveItems(p,rewards));assertNull(p.getInventory().items.get(27));}
 @Test public void duplicateStacksCannotOverflowTogether(){p.getInventory().items.set(0,new Item(995,Integer.MAX_VALUE-5));Item[] rewards={new Item(995,3),new Item(995,3)};
  assertFalse(Native950Skilling.giveItems(p,rewards));assertEquals(Integer.MAX_VALUE-5,p.getInventory().items.get(0).getAmount());}
 @Test public void previewAndCommitUseSameCapacity(){Item[] rewards={new Item(995,5),new Item(1511,2)};
  assertTrue(Native950Skilling.canGiveItems(p,rewards));assertTrue(p.getInventory().items.isEmpty());assertTrue(Native950Skilling.giveItems(p,rewards));assertEquals(5,p.getInventory().items.getNumberOf(995));assertEquals(2,p.getInventory().items.getNumberOf(1511));}
 @Test public void emptyUnknownAndNullRewardsAreRejected(){assertFalse(Native950Skilling.giveItems(p,new Item[0]));assertFalse(Native950Skilling.giveItems(p,null));assertFalse(Native950Skilling.giveItems(p,new Item[]{new Item(9999,1)}));assertTrue(p.getInventory().items.isEmpty());}
}
