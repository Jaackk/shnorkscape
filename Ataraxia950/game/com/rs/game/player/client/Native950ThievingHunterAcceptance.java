package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.thieving.PickPocketAction;
import com.rs.game.player.actions.thieving.def.PickPocketableNPC;
import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTasksManager;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Actual950 cache, originalActionManager and authoritative inventory/XP regression; no socket or save writes. */
public final class Native950ThievingHunterAcceptance {
 public static void main(String[] args)throws Exception{
  if(args.length!=1)throw new IllegalArgumentException("Usage: Native950ThievingHunterAcceptance <cache>");
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950World owner=Native950World.getInstance();
  owner.execute(()->{try(Fixture f=new Fixture()){f.check();}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
  System.out.println("PASS: original pickpocket Action, ordinary tables and exact950 assets; loot/XP, failure/stun, cancellation, full bag/overflow, stall depletion/restore, butterfly net/jar/level/catch/respawn/release and controller veto");
 }
 static final class Fixture implements AutoCloseable {
  final EmbeddedChannel channel=new EmbeddedChannel();
  final Player player=Player.createNative950("thiefhunter",new WorldTile(3217,3258,0),channel);
  final Native950Containers items;NPC npc;WorldObject stall;int hitFrames;
  Fixture(){
   player.setActive(true);Native950World.installVarpSink(player);World.addNative950Player(player,1);World.updateEntityRegion(player);player.loadMapRegions();player.setClientHasLoadedMapRegion();
   List<Native950ItemCatalog.Entry> entries=new ArrayList<>();for(int id:new int[]{10010,11259,10014,10016,10018,10020}){Native950ItemCatalog.Entry e=Native950Hunter.itemEntry(id);require(e!=null,"Missing Hunter item "+id);entries.add(e);}
   items=new Native950Containers(player,new Native950ItemCatalog(entries).withLegacyDrops());Native950Skilling.attach(player,items);
   level(Skills.THIEVING,99);level(Skills.HUNTER,99);player.setHitpoints(1000);npc=spawn(1);
  }
  NPC spawn(int id){NPC n=NPC.createNative950Diagnostic(id,new WorldTile(player.getX()+1,player.getY(),player.getPlane()));if(id>=5082&&id<=5085)Native950World.getInstance().addDiagnosticNpc(n);else {World.addNative950Npc(n);World.updateEntityRegion(n);}return n;}
  void level(int skill,int level){player.getSkills().setLevelWithoutRefresh(skill,level);player.getSkills().setXpWithoutRefresh(skill,level==99?13034431:0);}
  void check(){
   require(Native950Thieving.verifyCacheBindings()&&Native950Hunter.verifyCacheBindings(),"Asset preflight failed");
   int thief=0,stalls=0;
   for(int id:Native950Thieving.pickpocketIds()){NPC n=NPC.createNative950Diagnostic(id,new WorldTile(3200,3200,0));if(Native950Thieving.isPickpocket(n,3))thief++;}
   for(int id:Native950Thieving.stallIds()){WorldObject o=new WorldObject(id,10,0,3218,3258,0);for(int op=1;op<=5;op++)if(Native950Thieving.isStall(o,op)){stalls++;break;}}
   require(thief>=60,"Too few admitted ordinary pickpockets: "+thief);require(stalls>=30,"Too few admitted ordinary stalls: "+stalls);
   System.out.println("PASS: "+thief+" NPC pickpocket IDs; "+stalls+" stall IDs; 4 butterflies; actual menu slots and asset pins");
   require(!Native950Thieving.isPickpocket(npc,1)&&!Native950Thieving.isPickpocket(npc,2),"Talk/Attack misclassified");
   require(!Native950Thieving.isPickpocket(NPC.createNative950Diagnostic(2268,new WorldTile(player)),1),"Repurposed Rogue ID accepted");
   require(Native950Thieving.startPickpocket(player,npc,3),"Original PickPocketAction refused");require(player.getActionManager().getAction() instanceof PickPocketAction,"Original action not reused");player.getActionManager().forceStop();
   double xp=player.getSkills().getXp(Skills.THIEVING);
   require(player.getActionManager().setAction(new JourneyAction(new Native950Thieving.Journey(npc,PickPocketableNPC.MAN,b->b-1))),"Pickpocket success start refused");
   ticks(3);require(player.getInventory().items.getNumberOf(995)==3&&player.getSkills().getXp(Skills.THIEVING)>xp,"First pickpocket did not commit coins/XP");
   require(player.getActionManager().hasSkillWorking(),"Successful pickpocket did not continue automatically");ticks(2);require(player.getInventory().items.getNumberOf(995)==3,"Automatic pickpocket repeated before three ticks");ticks(1);require(player.getInventory().items.getNumberOf(995)==6,"Automatic pickpocket did not repeat after three ticks");
   Native950Thieving.Journey cancelled=new Native950Thieving.Journey(npc,PickPocketableNPC.MAN,b->b-1);player.getActionManager().forceStop();require(player.getActionManager().setAction(new JourneyAction(cancelled)),"Repeated theft cancellation setup");player.getActionManager().forceStop();double cancelledXp=player.getSkills().getXp(Skills.THIEVING);ticks(6);require(cancelled.finish(player)==-1&&player.getSkills().getXp(Skills.THIEVING)==cancelledXp&&player.getInventory().items.getNumberOf(995)==6,"Cancelled retained journey awarded additional loot");
   // Overflow arising between automatic attempts must stop with no partial second reward.
   player.getInventory().items.set(0,new Item(995,Integer.MAX_VALUE-3));require(player.getActionManager().setAction(new JourneyAction(new Native950Thieving.Journey(npc,PickPocketableNPC.MAN,b->b-1))),"Automatic overflow setup");ticks(3);require(player.getInventory().items.getNumberOf(995)==Integer.MAX_VALUE&&!player.getActionManager().hasSkillWorking(),"Auto pickpocket did not stop safely at overflow");
   clear();xp=player.getSkills().getXp(Skills.THIEVING);
   require(player.getActionManager().setAction(new JourneyAction(new Native950Thieving.Journey(npc,PickPocketableNPC.MAN,b->0))),"Failure start refused");ticks(3);
   require(hitFrames>0&&player.isLocked()&&player.getSkills().getXp(Skills.THIEVING)==xp&&!player.getInventory().containsItem(995,1),"Failure must stun without reward/XP");player.unlock();player.setHitpoints(1000);player.resetMasks();
   require(Native950Thieving.startPickpocket(player,npc,3),"Cancellation start refused");player.setNextWorldTile(player.transform(2,0,0));player.getActionManager().process();
   require(!player.getActionManager().hasSkillWorking()&&player.getSkills().getXp(Skills.THIEVING)==xp,"Pending teleport awarded theft");player.setNextWorldTile(null);
   for(int i=0;i<28;i++)player.getInventory().items.set(i,new Item(1511,1));require(!Native950Thieving.startPickpocket(player,npc,3),"Full bag accepted theft");clear();
   player.getInventory().items.set(0,new Item(995,Integer.MAX_VALUE));require(!Native950Thieving.startPickpocket(player,npc,3),"Coin overflow accepted theft");clear();
   npc.setLocation(player.transform(5,0,0));require(!Native950Thieving.startPickpocket(player,npc,3),"Remote pickpocket started");npc.setLocation(player.transform(1,0,0));
   require(Native950Thieving.startPickpocket(player,npc,3),"Stale NPC start refused");World.removeNative950Npc(npc);ticks(5);require(player.getSkills().getXp(Skills.THIEVING)==xp,"Removed NPC awarded XP");npc=null;
   stall=new WorldObject(635,10,0,3218,3258,0);World.spawnObject(stall);require(Native950Thieving.startStall(player,stall,2),"Tea stall start refused");ticks(4);
   require(player.getInventory().containsItem(712,1)&&player.getSkills().getXp(Skills.THIEVING)>xp,"Tea stall reward/XP missing");
   require(!Native950Woodcutting.current(stall)&&!Native950Thieving.startStall(player,stall,2),"Depleted stall can still be stolen from");ticks(25);require(Native950Woodcutting.current(stall),"Stall failed to respawn");
   clear();require(Native950Thieving.startStall(player,stall,2),"Stall restart refused");xp=player.getSkills().getXp(Skills.THIEVING);World.removeObject(stall);ticks(5);
   require(player.getSkills().getXp(Skills.THIEVING)==xp,"Removed stall awarded XP");stall=null;
   npc=spawn(5085);require(Native950Hunter.isCatchable(npc,1)&&!Native950Hunter.isCatchable(npc,2),"Butterfly Catch menu misclassified");
   require(!Native950Hunter.start(player,npc,1),"No-net catch accepted");give(10010,1);require(items.equip(player.getInventory().getItems().getThisItemSlot(10010),10010).moved==1,"Net equip failed");
   require(!Native950Hunter.start(player,npc,1),"No-jar catch accepted");give(10012,1);level(Skills.HUNTER,1);require(!Native950Hunter.start(player,npc,1),"Hunter level gate missing");level(Skills.HUNTER,99);
   for(int i=1;i<28;i++)player.getInventory().items.set(i,new Item(1511,1));xp=player.getSkills().getXp(Skills.HUNTER);
   require(player.getActionManager().setAction(new Native950Hunter.CatchAction(npc,1,FlyingEntities.RUBY_HARVEST,b->0)),"Full-bag1:1 jar exchange incorrectly refused");ticks(4);
   require(player.getInventory().containsItem(10020,1)&&!player.getInventory().containsItem(10012,1)&&player.getSkills().getXp(Skills.HUNTER)>xp,"Butterfly catch reward/XP missing");require(npc.hasFinished(),"Caught butterfly not removed");
   ticks(35);NPC replacement=null;for(NPC n:World.getNPCs())if(n!=null&&n.getId()==5085)replacement=n;require(replacement!=null&&replacement!=npc,"Butterfly failed to respawn as fresh native NPC");npc=replacement;
   int slot=player.getInventory().getItems().getThisItemSlot(10020);require(Native950Hunter.release(player,slot,10020,1),"Butterfly release refused");require(player.getInventory().containsItem(10012,1)&&!player.getInventory().containsItem(10020,1),"Release jar exchange wrong");
   xp=player.getSkills().getXp(Skills.HUNTER);require(player.getActionManager().setAction(new Native950Hunter.CatchAction(npc,1,FlyingEntities.RUBY_HARVEST,b->99)),"Hunter failure start refused");ticks(4);
   require(player.getInventory().containsItem(10012,1)&&player.getSkills().getXp(Skills.HUNTER)==xp&&!npc.hasFinished(),"Failed catch consumed jar/awarded XP");
   require(Native950Hunter.start(player,npc,1),"Hunter cancellation start refused");player.setNextWorldTile(player.transform(1,0,0));player.getActionManager().process();player.setNextWorldTile(null);
   require(!player.getActionManager().hasSkillWorking()&&player.getSkills().getXp(Skills.HUNTER)==xp,"Teleport awarded Hunter XP");
   player.getControlerManager().startControler(new Controller(){public void start(){}@Override public boolean canDeleteInventoryItem(int id,int amount){return false;}});
   require(player.getActionManager().setAction(new Native950Hunter.CatchAction(npc,1,FlyingEntities.RUBY_HARVEST,b->0)),"Veto action failed before commit test");ticks(4);
   require(player.getInventory().containsItem(10012,1)&&player.getSkills().getXp(Skills.HUNTER)==xp&&!npc.hasFinished(),"Controller veto consumed jar/awarded XP");player.getControlerManager().forceStop();
   Native950Save save=Native950PlayerBinder.capture(player,items.saveSnapshot("thiefhunter",player.getX(),player.getY(),player.getPlane()),System.currentTimeMillis());
   require(save.skills().xp(Skills.THIEVING)==player.getSkills().getXp(Skills.THIEVING)&&save.skills().xp(Skills.HUNTER)==player.getSkills().getXp(Skills.HUNTER),"Saved skill XP differs from owner");
  }
  void give(int id,int amount){require(Native950Skilling.giveItem(player,id,amount),"Cannot give "+id);}
  void clear(){player.getActionManager().forceStop();player.unlock();player.resetMasks();for(int i=0;i<28;i++)player.getInventory().items.set(i,null);}
  void ticks(int n){for(int i=0;i<n;i++){player.processEntity();player.processEntityUpdate();if(!player.getNextHits().isEmpty()){require(Native950Hits.fromRunningCache(player).refusals()==0,"Unverified native stun hit display");hitFrames++;}WorldTasksManager.processTasks();player.resetMasks();channel.flush();channel.runPendingTasks();while(channel.readOutbound()!=null){}channel.checkException();}}
  public void close(){player.getActionManager().forceStop();if(stall!=null)World.removeObject(stall);if(npc!=null&&!npc.hasFinished()){Native950World.getInstance().nativeNpcs().remove(npc);World.removeNative950Npc(npc);}World.removeNative950Player(player);Native950Skilling.detach(player);channel.finishAndReleaseAll();}
 }
 static final class JourneyAction extends Action {final Native950Thieving.Journey j;JourneyAction(Native950Thieving.Journey j){this.j=j;}public boolean start(Player p){return j.start(p);}public boolean process(Player p){return j.process(p);}public int processWithDelay(Player p){return j.finish(p);}public void stop(Player p){j.stop(p);}}
 static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
