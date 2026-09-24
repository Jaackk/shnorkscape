package com.rs.game.player.client;
import com.rs.game.WorldTile;import com.rs.game.player.Player;import com.rs.game.item.Item;import java.util.*;import io.netty.channel.embedded.EmbeddedChannel;import org.junit.Test;import static org.junit.Assert.*;
public class Native950ChristmasCrackerTest {
 @Test public void twoPlayerPullChecksCapacityAndConsumesOnlyTheExactCracker(){
  EmbeddedChannel ca=new EmbeddedChannel(),cb=new EmbeddedChannel();Player a=Player.createNative950("owner",new WorldTile(3200,3200,0),ca),b=Player.createNative950("partner",new WorldTile(3201,3200,0),cb);a.setActive(true);b.setActive(true);
  List<Native950ItemCatalog.Entry> entries=new ArrayList<>();for(int id:new int[]{962,1038,1040,1042,1044,1046,1048,1969,2355,1217,1635,1973,1718,950,563,1987})entries.add(new Native950ItemCatalog.Entry(id,"item",false,new String[5]));
  Native950Skilling.attach(a,new Native950Containers(a,new Native950ItemCatalog(entries)));Native950Skilling.attach(b,new Native950Containers(b,new Native950ItemCatalog(entries)));
  try{
   for(int i=0;i<28;i++){a.getInventory().items.set(i,new Item(962));b.getInventory().items.set(i,new Item(1969));}
   assertNotNull(Native950ChristmasCracker.pull(a,b,0,new Random(0)));assertEquals(962,a.getInventory().items.get(0).getId());
   b.getInventory().items.set(4,null);assertNull(Native950ChristmasCracker.pull(a,b,0,new Random(0)));
   assertTrue(a.getInventory().items.get(0).getId()>=1038&&a.getInventory().items.get(0).getId()<=1048);assertNotNull(b.getInventory().items.get(4));
   assertEquals(27,a.getInventory().getAmountOf(962));assertNotNull(Native950ChristmasCracker.pull(a,b,0,new Random(0)));
   assertNotNull(Native950ChristmasCracker.pull(a,a,1));b.setNextWorldTile(new WorldTile(3300,3300,0));assertNotNull(Native950ChristmasCracker.pull(a,b,1));assertEquals(27,a.getInventory().getAmountOf(962));
  }finally{Native950Skilling.detach(a);Native950Skilling.detach(b);ca.finishAndReleaseAll();cb.finishAndReleaseAll();}
 }
 @Test public void selectedPlayerPacketUsesExact950ByteOrders(){
  byte[] p={(byte)255,0,(byte)130,0,(byte)133,0,3,(byte)194,0,5,5,(byte)193};
  com.rs.network.protocol.modern950.Native950Actions.ItemOnPlayerAction a=(com.rs.network.protocol.modern950.Native950Actions.ItemOnPlayerAction)com.rs.network.protocol.modern950.Native950Actions.decode(102,p);
  assertEquals(2,a.index());assertEquals(5,a.sourceSlot());assertEquals(962,a.sourceItemId());assertEquals((1473<<16)|5,a.sourceHash());assertEquals(1,a.modifier());
  assertEquals(12,com.rs.network.protocol.modern950.Native950Protocol.clientSize(102));assertNull(com.rs.network.protocol.modern950.Native950Actions.decode(102,new byte[11]));p[0]=2;assertNull(com.rs.network.protocol.modern950.Native950Actions.decode(102,p));
 }
}
