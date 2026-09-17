package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.*;
import com.rs.game.player.controllers.Controller;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Disposable current-cache tool transactions and UI packets; no character save or rendered-client claims. */
public final class Native950ToolbeltAcceptance {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static void level(Player p,int skill,int level){p.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,level));p.getSkills().setLevelWithoutRefresh(skill,level);}
 static void clear(Player p){for(int i=0;i<28;i++)p.getInventory().items.set(i,null);}
 static Native950Actions.InterfaceAction click(int inter,int comp,int slot,int option){int h=inter<<16|comp;byte[] b={(byte)255,(byte)255,(byte)255,(byte)(h>>16),(byte)(h>>24),(byte)h,(byte)(h>>8),(byte)(slot>>8),(byte)slot};return (Native950Actions.InterfaceAction)Native950Actions.decode(option==1?18:122,b);}
 static boolean packet(EmbeddedChannel c,Native950Packets.Packet expected){Object o;boolean found=false;c.flush();while((o=c.readOutbound())!=null)if(o instanceof Native950Packets.Packet){Native950Packets.Packet p=(Native950Packets.Packet)o;found|=p.type()==expected.type()&&Arrays.equals(p.payload(),expected.payload());}return found;}
 public static void main(String[]a)throws Exception{
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(a[0]));
  Native950World.getInstance().execute(()->{run();return null;}).get(120,TimeUnit.SECONDS);
  System.out.println("PASS toolbelt: "+checks+" actual-cache transaction, skill-lookup and native-UI packet checks; rendered client not tested.");
 }
 static void run(){
  Native950Toolbelt.verifyCacheBindings();EmbeddedChannel ch=new EmbeddedChannel();Player p=Player.createNative950("belt-probe",new WorldTile(3217,3258,0),ch);p.setActive(true);p.setIndex(1);p.resetMasks();
  Native950Containers containers=new Native950Containers(p,new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops());Native950Skilling.attach(p,containers);
  try{
   check(Native950Toolbelt.snapshot(p).length==35,"Incorrect free basic tool count");
   for(int id:new int[]{946,590,1265,1351,303,307,1733,2347,5341,5325,952})check(Native950Toolbelt.has(p,id),"Missing free basic tool "+id);
   check(Native950Toolbelt.describe(p,0).contains("free basic")&&!Native950Toolbelt.describe(p,0).contains("Right-click"),"Free bronze description offers removal");
   check(!Native950Toolbelt.has(p,4162)&&!Native950Toolbelt.has(p,18337)&&!Native950Toolbelt.has(p,49539),"Granted an optional or gated tool");
   check(Native950Mining.bestPickaxe(p).itemId==1265,"Default bronze belt pick not used");check(Native950Woodcutting.bestAxe(p).itemId==1351,"Default bronze belt hatchet not used");check(Native950Farming.hasTool(p,952),"Farming ignores free spade");
   for(int[] bad:new int[][]{{1267,946},{946,946},{1265,1267},{-1},{18337},{36367},{Integer.MAX_VALUE},new int[78]}){boolean refused=false;try{Native950Toolbelt.validateSnapshot(bad);}catch(IllegalArgumentException expected){refused=true;}check(refused,"Invalid belt save admitted: "+Arrays.toString(bad));}
   int[] restore={946,1267};Native950Toolbelt.restore(p,restore);restore[1]=1265;check(Native950Toolbelt.has(p,1267)&&Native950Toolbelt.snapshot(p).length==35,"Restore shares caller data or drops defaults");Native950Toolbelt.restore(p,new int[0]);
   p.getInventory().items.set(0,new Item(1267,1));level(p,Skills.MINING,1);Native950Toolbelt.add(p,0,1267,"Add to tool belt");check(p.getInventory().getItem(0)!=null&&!Native950Toolbelt.has(p,1267),"Underlevelled add consumed item");
   level(p,Skills.MINING,99);Native950Toolbelt.add(p,0,45467,"Add to tool belt");check(p.getInventory().containsItem(1267,1)&&!Native950Toolbelt.has(p,45467),"Mismatched source slot consumed tool");
   Item charged=new Item(1267,1);charged.setCharges(1);p.getInventory().items.set(0,charged);Native950Toolbelt.add(p,0,1267,"Add to tool belt");check(p.getInventory().getItem(0)==charged&&!Native950Toolbelt.has(p,1267),"Personalised item consumed by ID");p.getInventory().items.set(0,new Item(1267,1));
   level(p,Skills.MINING,99);check(Native950Toolbelt.add(p,0,1267,"Add-to-toolbelt"),"Add option unhandled");check(p.getInventory().getItem(0)==null&&Native950Toolbelt.has(p,1267)&&Native950Toolbelt.has(p,1265),"Iron upgrade missing or free bronze became an item");check(Native950Mining.bestPickaxe(p).itemId==1267,"Mining ignores upgraded belt pick");check(Native950Toolbelt.describe(p,0).contains("Right-click to remove")&&Native950Toolbelt.describe(p,0).contains("returns this one"),"Stored upgrade lacks accurate removal/exchange description");
   clear(p);p.getInventory().items.set(0,new Item(45467,1));for(int i=1;i<28;i++)p.getInventory().items.set(i,new Item(1511,1));Native950Toolbelt.add(p,0,45467,"Add to tool belt");check(Native950Toolbelt.has(p,45467)&&p.getInventory().getItem(0).getId()==1267,"Full-backpack upgrade did not atomically return old pick into freed slot");
   int[] before=Native950Toolbelt.snapshot(p);Native950Toolbelt.remove(p,0);check(Arrays.equals(before,Native950Toolbelt.snapshot(p))&&p.getInventory().getItem(0).getId()==1267,"Full-backpack removal consumed or duplicated tool");
   clear(p);Native950Toolbelt.remove(p,0);check(p.getInventory().containsItem(45467,1)&&Native950Toolbelt.currentItem(p,0)==1265,"Remove did not return exact tool and restore free bronze");Native950Toolbelt.remove(p,0);check(!p.getInventory().containsItem(1265,1),"Free bronze could be removed into backpack");
   clear(p);p.getInventory().items.set(0,new Item(1267,1));p.getControlerManager().startControler(new Controller(){public void start(){}public boolean canDeleteInventoryItem(int id,int amount){return false;}});Native950Toolbelt.add(p,0,1267,"Add to tool belt");check(p.getInventory().containsItem(1267,1)&&!Native950Toolbelt.has(p,1267),"Controller delete veto ignored");p.getControlerManager().forceStop();Native950Toolbelt.add(p,0,1267,"Add to tool belt");
   p.getControlerManager().startControler(new Controller(){public void start(){}public boolean canAddInventoryItem(int id,int amount){return false;}});Native950Toolbelt.remove(p,0);check(Native950Toolbelt.has(p,1267)&&!p.getInventory().containsItem(1267,1),"Controller receive veto ignored");p.getControlerManager().forceStop();
   clear(p);p.getInventory().items.set(0,new Item(45467,1));p.getControlerManager().startControler(new Controller(){public void start(){}public boolean canDeleteInventoryItem(int id,int amount){Native950Toolbelt.remove(p,0);return true;}});Native950Toolbelt.add(p,0,45467,"Add to tool belt");check(Native950Toolbelt.has(p,45467)&&p.getInventory().getAmountOf(1267)==1&&!p.getInventory().containsItem(45467,1),"Reentrant controller duplicated tool");p.getControlerManager().forceStop();
   clear(p);p.getInventory().items.set(0,new Item(57391,1));level(p,Skills.MINING,120);Native950Toolbelt.add(p,0,57391,"Add to tool belt");check(p.getInventory().containsItem(57391,1)&&!Native950Toolbelt.has(p,57391),"Unported Primal action consumed tool");
   clear(p);p.getInventory().items.set(0,new Item(49539,1));Native950Toolbelt.add(p,0,49539,"Add to tool belt");check(Native950Toolbelt.has(p,49539)&&Native950Archaeology.mattock(p)==49539,"Archaeology ignores belt mattock");
   p.getInventory().items.set(0,new Item(4162,1));Native950Toolbelt.add(p,0,4162,"Add to tool belt");check(Native950Toolbelt.has(p,4162)&&p.getInventory().getItem(0)==null,"Ordinary optional tool cannot be stored");
   int[] saved=Native950Toolbelt.snapshot(p);Native950Toolbelt.restore(p,saved);check(Arrays.equals(saved,Native950Toolbelt.snapshot(p)),"Toolbelt restore changed exact upgraded state");
   Native950ToolbeltUi ui=new Native950ToolbeltUi(p,ch);ui.bootstrap();check(packet(ch,Native950Packets.interfaceEvents(1462,35,4353,4353,2)),"Equipment toolbelt entry not enabled");
   check(!Native950ToolbeltUi.isOpenRequest(click(1462,35,1,1)),"Other equipment button opens belt");check(ui.handle(click(1462,35,4353,1))&&ui.isOpen(),"Native belt not open");check(packet(ch,Native950Packets.openSub(1477,735,1944,false)),"Wrong toolbelt mount");
   ui.close();ui.handle(click(1462,35,4353,1));check(packet(ch,Native950Packets.hideInterface(1944,12,true)),"Settings artwork parent remains visible");
   ui.handle(click(1944,7,0,1));check(packet(ch,Native950Packets.interfaceText(1944,27,Native950Toolbelt.describe(p,0))),"Tool detail remains Loading");
   clear(p);ui.handle(click(1944,7,0,2));check(p.getInventory().containsItem(45467,1)&&Native950Toolbelt.currentItem(p,0)==1265,"Native remove action fails");
   int amount=p.getInventory().getAmountOf(45467);ui.handle(click(1944,7,77,2));check(p.getInventory().getAmountOf(45467)==amount,"Invalid UI slot mutates inventory");
   ui.handle(click(1944,102,-1,1));check(!ui.isOpen()&&packet(ch,Native950Packets.closeSub(1477,735)),"Native close not balanced");
   check(!ui.handle(click(1944,7,0,2)),"Closed toolbelt handles stale removal");
   clear(p);Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
   Native950Interactions input=new Native950Interactions(p,ch,new Native950Content(new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops(),bank),null,null);
   try{
    input.handle(click(1462,35,4353,1));check(p.getInterfaceManager().containsNative950ToolbeltUi()&&p.getInterfaceManager().containsInterface(1944),"Interactions did not own opened toolbelt");
    input.handle(click(1466,7,0,1));check(!p.getInterfaceManager().containsNative950ToolbeltUi()&&!p.getInterfaceManager().containsInterface(1944),"Opening Skills retained toolbelt ownership");
    check(p.getInterfaceManager().containsNative950SkillGuide()&&p.getInterfaceManager().containsInterface(1448),"Replacing toolbelt closed the newly opened Hero guide");
    p.getInterfaceManager().closeNative950ToolbeltUi();check(p.getInterfaceManager().containsNative950SkillGuide()&&p.getInterfaceManager().containsInterface(1448),"Stale toolbelt close retired another modal");
   }finally{input.close();}

  }finally{p.getControlerManager().forceStop();p.getActionManager().forceStop();Native950Skilling.detach(p);ch.finishAndReleaseAll();}
 }
}
