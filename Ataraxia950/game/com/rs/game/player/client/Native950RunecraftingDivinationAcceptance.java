package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.ControlerManager;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.actions.divination.*;
import com.rs.game.player.actions.divination.DivinationConvert.ConvertMode;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Actual paired-cache original-action acceptance; no listener or player-save writes. */
public final class Native950RunecraftingDivinationAcceptance {
 public static void main(String[] args)throws Exception{
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950Runecrafting.verifyCacheBindings();Native950Divination.verifyCacheBindings();
  final boolean pauseOnly=args.length>1&&"--harvest-pause".equals(args[1]);
  Native950World.getInstance().execute(()->{try(Fixture fixture=new Fixture()){if(pauseOnly)fixture.checkHarvestPause();else fixture.check();}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Native scheduler failed");
  if(pauseOnly){System.out.println("PASS: natural wandering wisp stays stationary through native movement ticks for two harvesters; failed starts do not pause; movement cancellation releases only its owner; final stop restores wander; removed NPC leases retire");return;}
  System.out.println("PASS: 13 ordinary rune altars; six actual-map altar entrances/exits; 12 Divination tiers; all3 conversion modes; original action/XP; fullbag, overflow, controller, approach, stale target and movement cancellation checks");
 }
 private static final class Fixture implements AutoCloseable {
  final com.rs.network.modern.Native950GameTransport transport=new com.rs.network.modern.Native950GameTransport(()->0,()->0,Thread.currentThread());final EmbeddedChannel channel=new EmbeddedChannel(transport);final Player p=Player.createNative950("rcdiv-probe",new WorldTile(3217,3258,0),channel);
  final Native950Containers containers;WorldObject object;NPC npc;
  Fixture(){p.setActive(true);Native950World.installVarpSink(p);World.addNative950Player(p,1);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();p.resetMasks();
   List<Native950ItemCatalog.Entry> catalog=new ArrayList<>(Native950Runecrafting.itemEntries());catalog.addAll(Native950Divination.itemEntries());
   containers=new Native950Containers(p,new Native950ItemCatalog(catalog));Native950Skilling.attach(p,containers);
  }
  void check()throws Exception{
   checkBoons();checkActionLifecycle();
   for(Native950Runecrafting.Altar altar:Native950Runecrafting.Altar.values()){
    clear();object=new WorldObject(altar.objectId,10,0,p.getX()+1,p.getY(),p.getPlane());World.spawnObject(object);level(Skills.RUNECRAFTING,altar.level);
    require(Native950Runecrafting.handles(object,1)&&Native950Runecrafting.handles(object,3)&&!Native950Runecrafting.handles(object,2),"Wrong950altar options "+altar);
    require(!Native950Runecrafting.start(p,object),"Craft without essence "+altar);give(7936,28);
    require(Native950Runecrafting.start(p,object),"Craft refused "+altar);double xp=p.getSkills().getXp(Skills.RUNECRAFTING);
    for(int i=0;i<3;i++)tick();require(p.getInventory().getAmountOf(7936)==0&&p.getInventory().getAmountOf(altar.rune)>=28,"Rune exchange "+altar);
    require(p.getSkills().getXp(Skills.RUNECRAFTING)>xp,"Rune XP "+altar);
    if(altar.pureOnly){clearItems();give(1436,1);require(!Native950Runecrafting.start(p,object),"Normal essence accepted "+altar);}
    if(altar.level>1){clearItems();give(7936,1);level(Skills.RUNECRAFTING,altar.level-1);require(!Native950Runecrafting.start(p,object),"Rune level gate "+altar);}
   }
   clear();object=new WorldObject(2478,10,0,p.getX()+1,p.getY(),p.getPlane());World.spawnObject(object);level(Skills.RUNECRAFTING,1);give(1436,1);
   p.getInventory().items.set(1,new Item(556,Integer.MAX_VALUE));double xp=p.getSkills().getXp(Skills.RUNECRAFTING);
   require(Native950Runecrafting.start(p,object),"Overflow setup");for(int i=0;i<3;i++)tick();
   require(p.getInventory().getAmountOf(1436)==1&&p.getInventory().getAmountOf(556)==Integer.MAX_VALUE&&p.getSkills().getXp(Skills.RUNECRAFTING)==xp,"Rune overflow destroyed essence/awardedXP");
   clearItems();give(1436,1);require(Native950Runecrafting.start(p,object),"Stale setup");World.removeObject(object);object=null;tick();require(p.getInventory().getAmountOf(1436)==1&&p.getSkills().getXp(Skills.RUNECRAFTING)==xp,"Removed altar still crafted");
   for(WispInfo info:WispInfo.values()){
    clear();level(Skills.DIVINATION,info.getLevel());npc=NPC.createNative950Diagnostic(info.getNpcId(),p.transform(1,0,0));World.addNative950Npc(npc);World.updateEntityRegion(npc);
    require(Native950Divination.definition(npc,1)==info&&!Native950Divination.isHarvestable(npc,2),"Wisp identity "+info);
    if(info.getLevel()>1){level(Skills.DIVINATION,info.getLevel()-1);require(!Native950Divination.startHarvest(p,npc,1),"Wisp level gate "+info);level(Skills.DIVINATION,info.getLevel());}
    require(Native950Divination.startHarvest(p,npc,1),"Harvest refused "+info);require(p.getActionManager().getAction() instanceof DivinationHarvest,"OriginalHarvest not reused");xp=p.getSkills().getXp(Skills.DIVINATION);
    for(int i=0;i<10;i++)tick();require(p.getInventory().getAmountOf(info.getEnergyId())>0&&p.getSkills().getXp(Skills.DIVINATION)>xp,"No harvest energy/XP "+info);
    xp=p.getSkills().getXp(Skills.DIVINATION);World.removeNative950Npc(npc);npc=null;for(int i=0;i<3;i++)tick();require(p.getSkills().getXp(Skills.DIVINATION)==xp&&!p.getActionManager().hasSkillWorking(),"Stale wisp reward");
    object=new WorldObject(87306,10,0,p.getX()+1,p.getY(),p.getPlane());World.spawnObject(object);
    for(ConvertMode mode:ConvertMode.values()){
     clearItems();give(info.getMemoryId(),1);give(info.getEnergyId(),10);int before=p.getInventory().getAmountOf(info.getEnergyId());xp=p.getSkills().getXp(Skills.DIVINATION);
     require(Native950Divination.startConvert(p,object,mode),"Convert refused "+info+" "+mode);require(p.getActionManager().getAction() instanceof DivinationConvert,"OriginalConvert not reused");for(int i=0;i<4;i++)tick();
     require(p.getInventory().getAmountOf(info.getMemoryId())==0&&p.getSkills().getXp(Skills.DIVINATION)>xp,"Convert consumed/XP "+info+" "+mode);
     require(mode==ConvertMode.CONVERT_TO_ENERGY?p.getInventory().getAmountOf(info.getEnergyId())>before:p.getInventory().getAmountOf(info.getEnergyId())==before-(mode==ConvertMode.CONVERT_TO_MORE_XP?5:0),"Mode energy policy "+mode);
    }
    if(info!=WispInfo.PALE){clearItems();give(info.getEnrichedMemoryId(),1);give(info.getEnergyId(),10);require(Native950Divination.startConvert(p,object,ConvertMode.CONVERT_TO_MORE_XP),"Enriched setup");for(int i=0;i<4;i++)tick();require(p.getInventory().getAmountOf(info.getEnrichedMemoryId())==0&&p.getInventory().getAmountOf(info.getEnergyId())==0,"Enriched cost must be10");}
    clearItems();give(info.getMemoryId(),1);require(Native950Divination.startConvert(p,object,ConvertMode.CONVERT_TO_MORE_XP),"No-energy fallback refused");for(int i=0;i<4;i++)tick();require(p.getInventory().getAmountOf(info.getMemoryId())==0,"No-energy fallback did not convert");
   }
   clear();level(Skills.DIVINATION,99);npc=NPC.createNative950Diagnostic(18150,p.transform(1,0,0));World.addNative950Npc(npc);World.updateEntityRegion(npc);
   p.addWalkSteps(p.getX(),p.getY()+1,1,false);require(!Native950Divination.startHarvest(p,npc,1),"Started while approaching");p.resetWalkSteps();
   for(int i=0;i<28;i++)p.getInventory().items.set(i,new Item(1436,1));require(!Native950Divination.startHarvest(p,npc,1),"Fullbag harvest admitted");
   clearItems();p.getInventory().items.set(0,new Item(29313,Integer.MAX_VALUE));xp=p.getSkills().getXp(Skills.DIVINATION);require(!Native950Divination.startHarvest(p,npc,1),"Overflow harvest admitted");require(p.getSkills().getXp(Skills.DIVINATION)==xp,"Overflow harvest XP");
   clearItems();require(Native950Divination.startHarvest(p,npc,1),"Movement setup");xp=p.getSkills().getXp(Skills.DIVINATION);p.addWalkSteps(p.getX(),p.getY()+1,1,false);tick();p.resetWalkSteps();require(!p.getActionManager().hasSkillWorking()&&p.getSkills().getXp(Skills.DIVINATION)==xp,"Movement harvest reward");
   clearItems();Item[] outputs={new Item(29313,1),new Item(29384,1)};for(int i=0;i<27;i++)p.getInventory().items.set(i,new Item(1436,1));require(!Native950Skilling.giveItems(p,outputs)&&p.getInventory().getAmountOf(29313)==0,"Partial fullbag grant");
   clearItems();p.getInventory().items.set(0,new Item(29313,Integer.MAX_VALUE));require(!Native950Skilling.giveItems(p,outputs)&&p.getInventory().getAmountOf(29384)==0,"Partial overflow grant");
   clearItems();checkHarvestPause();checkTravel();clearItems();Controller denial=new Controller(){public void start(){}public boolean canAddInventoryItem(int id,int count){return id!=29384;}};
   Field f=ControlerManager.class.getDeclaredField("controler");f.setAccessible(true);f.set(p.getControlerManager(),denial);f=ControlerManager.class.getDeclaredField("inited");f.setAccessible(true);f.setBoolean(p.getControlerManager(),true);
   require(!Native950Skilling.giveItems(p,outputs)&&p.getInventory().getAmountOf(29313)==0&&p.getInventory().getAmountOf(29384)==0,"Controller refused memory after granting energy");
  }
  void checkActionLifecycle()throws Exception{
   clear();level(Skills.RUNECRAFTING,99);object=new WorldObject(2478,10,0,p.getX()+1,p.getY(),p.getPlane());World.spawnObject(object);give(1436,1);
   require(Native950Runecrafting.start(p,object),"Rune action cancellation setup");com.rs.game.player.actions.Action cancelled=p.getActionManager().getAction();p.getActionManager().forceStop();double xp=p.getSkills().getXp(Skills.RUNECRAFTING);
   require(cancelled.processWithDelay(p)==-1&&p.getInventory().getAmountOf(1436)==1&&p.getSkills().getXp(Skills.RUNECRAFTING)==xp,"Cancelled altar callback awarded");
   require(Native950Runecrafting.start(p,object),"Rune controller setup");Field field=ControlerManager.class.getDeclaredField("controler");field.setAccessible(true);field.set(p.getControlerManager(),new Controller(){public void start(){}});for(int i=0;i<3;i++)tick();
   require(p.getInventory().getAmountOf(1436)==1&&p.getSkills().getXp(Skills.RUNECRAFTING)==xp,"Controller changed during altar craft");field.set(p.getControlerManager(),null);
   clear();level(Skills.DIVINATION,99);npc=NPC.createNative950Diagnostic(18150,p.transform(1,0,0));World.addNative950Npc(npc);World.updateEntityRegion(npc);
   Native950Divination.HarvestJourney harvest=new Native950Divination.HarvestJourney(npc,WispInfo.PALE);require(harvest.start(p),"Harvest stop setup");harvest.stop(p);xp=p.getSkills().getXp(Skills.DIVINATION);
   require(harvest.harvest(p)==-1&&p.getInventory().getAmountOf(29313)==0&&p.getSkills().getXp(Skills.DIVINATION)==xp&&Native950Divination.activeHarvestPauses()==0,"Stopped harvest callback awarded or retained pause");
   Native950Divination.HarvestJourney attacked=new Native950Divination.HarvestJourney(npc,WispInfo.PALE);require(attacked.start(p),"Harvest combat setup");p.setAttackedBy(npc);p.setAttackedByDelay(com.rs.utils.Utils.currentTimeMillis()+10000);
   require(attacked.harvest(p)==-1&&p.getInventory().getAmountOf(29313)==0,"Combat harvesting awarded");attacked.stop(p);p.setAttackedBy(null);p.setAttackedByDelay(0);
   clear();object=new WorldObject(87306,10,0,p.getX()+1,p.getY(),p.getPlane());World.spawnObject(object);give(29384,1);Native950Divination.ConvertJourney convert=new Native950Divination.ConvertJourney(object,ConvertMode.CONVERT_TO_XP);
   require(convert.start(p),"Conversion stop setup");convert.stop(p);require(convert.convert(p)==-1&&p.getInventory().getAmountOf(29384)==1,"Stopped conversion callback consumed memory");clear();
   System.out.println("PASS:stopped altar/harvest/conversion callbacks, controller replacement and combat cannot grant rewards or retain wisp pause");
  }
  void checkBoons()throws Exception{
   List<Native950Divination.Boon> recipes=Native950Divination.boonRecipes();require(recipes.size()==11,"Expected11 ordinary boon tiers");int alternatives=0;
   for(Native950Divination.Boon recipe:recipes)for(int energy:recipe.energyIds()){
    clear();Native950Divination.restoreBoons(p,null);level(Skills.DIVINATION,recipe.level);give(energy,recipe.cost);Item selected=p.getInventory().getItem(0);
    require(Native950Divination.isWeavable(energy,1)&&!Native950Divination.isWeavable(energy,2),"Cache Weave option "+energy);
    require(!Native950Divination.weaveChoices(p,0,energy,1).isEmpty(),"Boon missing from Weave choices");double xp=p.getSkills().getXp(Skills.DIVINATION);
    require(p.getActionManager().setAction(new Native950Divination.WeaveBoon(0,selected,recipe.cost,recipe)),"Boon craft refused "+recipe.name);for(int i=0;i<3;i++)tick();
    require(Native950Divination.boons(p)[recipe.index]&&p.getInventory().getAmountOf(energy)==0&&p.getSkills().getXp(Skills.DIVINATION)>xp,"Boon unlock/cost/XP "+recipe.name);
    give(energy,recipe.cost);xp=p.getSkills().getXp(Skills.DIVINATION);require(!p.getActionManager().setAction(new Native950Divination.WeaveBoon(0,p.getInventory().getItem(0),recipe.cost,recipe)),"Duplicate boon consumed again");
    require(p.getInventory().getAmountOf(energy)==recipe.cost&&p.getSkills().getXp(Skills.DIVINATION)==xp,"Duplicate boon changed state");alternatives++;
   }
   Native950Divination.Boon recipe=recipes.get(0);int energy=recipe.energyIds()[0];clear();Native950Divination.restoreBoons(p,null);level(Skills.DIVINATION,recipe.level-1);give(energy,recipe.cost);
   require(!p.getActionManager().setAction(new Native950Divination.WeaveBoon(0,p.getInventory().getItem(0),recipe.cost,recipe)),"Boon level gate");level(Skills.DIVINATION,recipe.level);
   clearItems();give(energy,recipe.cost-1);require(!p.getActionManager().setAction(new Native950Divination.WeaveBoon(0,p.getInventory().getItem(0),recipe.cost-1,recipe)),"Boon cost gate");
   clearItems();give(energy,recipe.cost);Native950Divination.WeaveBoon cancelled=new Native950Divination.WeaveBoon(0,p.getInventory().getItem(0),recipe.cost,recipe);
   require(p.getActionManager().setAction(cancelled),"Cancelled boon setup");p.getActionManager().forceStop();require(cancelled.processWithDelay(p)==-1&&!Native950Divination.boons(p)[1]&&p.getInventory().getAmountOf(energy)==recipe.cost,"Retained cancelled action awarded boon");
   Native950Divination.WeaveBoon stale=new Native950Divination.WeaveBoon(0,p.getInventory().getItem(0),recipe.cost,recipe);require(p.getActionManager().setAction(stale),"Stale boon setup");p.getInventory().items.set(0,new Item(energy,recipe.cost));for(int i=0;i<3;i++)tick();
   require(!Native950Divination.boons(p)[1]&&p.getInventory().getAmountOf(energy)==recipe.cost,"Replaced same-ID energy stack consumed");
   clearItems();give(energy,recipe.cost);p.getInventory().getItem(0).setCharges(1);require(!p.getActionManager().setAction(new Native950Divination.WeaveBoon(0,p.getInventory().getItem(0),recipe.cost,recipe)),"Modified energy accepted");
   clearItems();give(energy,recipe.cost);Controller denial=new Controller(){public void start(){}public boolean canDeleteInventoryItem(int id,int count){return false;}};
   Field field=ControlerManager.class.getDeclaredField("controler");field.setAccessible(true);field.set(p.getControlerManager(),denial);field=ControlerManager.class.getDeclaredField("inited");field.setAccessible(true);field.setBoolean(p.getControlerManager(),true);
   require(p.getActionManager().setAction(new Native950Divination.WeaveBoon(0,p.getInventory().getItem(0),recipe.cost,recipe)),"Controller denial setup");for(int i=0;i<3;i++)tick();require(!Native950Divination.boons(p)[1]&&p.getInventory().getAmountOf(energy)==recipe.cost,"Controller rejection charged/unlocked boon");
   field=ControlerManager.class.getDeclaredField("controler");field.setAccessible(true);field.set(p.getControlerManager(),null);
   clear();Native950Divination.restoreBoons(p,null);level(Skills.DIVINATION,10);object=new WorldObject(87306,10,0,p.getX()+1,p.getY(),p.getPlane());World.spawnObject(object);
   give(29385,1);double before=p.getSkills().getXp(Skills.DIVINATION);require(Native950Divination.startConvert(p,object,ConvertMode.CONVERT_TO_XP),"Before boon conversion");for(int i=0;i<4;i++)tick();double ordinary=p.getSkills().getXp(Skills.DIVINATION)-before;
   boolean[] flags=new boolean[12];flags[1]=true;Native950Divination.restoreBoons(p,flags);give(29385,1);before=p.getSkills().getXp(Skills.DIVINATION);require(Native950Divination.startConvert(p,object,ConvertMode.CONVERT_TO_XP),"After boon conversion");for(int i=0;i<4;i++)tick();double boosted=p.getSkills().getXp(Skills.DIVINATION)-before;
   require(Math.abs(boosted-ordinary*1.1)<0.00001,"Boon conversion boost not exactly10percent");clear();Native950Divination.restoreBoons(p,null);
   System.out.println("PASS:11 paired950 boon recipes, "+alternatives+" cache energy alternatives, exact costs, level gates, duplicate prevention, cancelled/stale/modified stack rejection, controller refusal and10percent conversion boost");
  }
  void checkHarvestPause(){
   clear();level(Skills.DIVINATION,99);npc=NPC.createNative950Diagnostic(18150,p.transform(1,0,0));World.addNative950Npc(npc);World.updateEntityRegion(npc);npc.setNative950Wander(5);
   require((com.rs.cache.loaders.NPCDefinitions.getNPCDefinitions(npc.getId()).movementCapabilities&NPC.NORMAL_WALK)!=0,"Actual Pale wisp is not an ordinary walker");
   npc.addWalkSteps(npc.getX()+1,npc.getY(),1,false);for(int i=0;i<28;i++)p.getInventory().items.set(i,new Item(1436,1));
   require(!Native950Divination.startHarvest(p,npc,1)&&npc.getNative950Wander()==5&&npc.hasWalkSteps()&&Native950Divination.activeHarvestPauses()==0,"Failed start modified wandering");clearItems();
   com.rs.network.modern.Native950GameTransport secondTransport=new com.rs.network.modern.Native950GameTransport(()->0,()->0,Thread.currentThread());
   EmbeddedChannel secondChannel=new EmbeddedChannel(secondTransport);Player other=Player.createNative950("second-harvester",npc.transform(0,-1,0),secondChannel);
   other.setActive(true);Native950World.installVarpSink(other);World.addNative950Player(other,2);World.updateEntityRegion(other);other.loadMapRegions();other.setClientHasLoadedMapRegion();other.resetMasks();
   other.getSkills().setLevelWithoutRefresh(Skills.DIVINATION,99);other.getSkills().setXpWithoutRefresh(Skills.DIVINATION,Skills.getXPForLevel(Skills.DIVINATION,99));
   Native950Skilling.attach(other,new Native950Containers(other,new Native950ItemCatalog(Native950Divination.itemEntries())));
   try{
    require(Native950Divination.startHarvest(p,npc,1)&&Native950Divination.startHarvest(other,npc,1),"Two-harvester setup failed");
    require(npc.getNative950Wander()==0&&!npc.hasWalkSteps()&&Native950Divination.activeHarvestPauses()==1,"Shared pause did not clear old path");WorldTile origin=new WorldTile(npc);
    for(int i=0;i<20;i++){npc.processNative950Movement();tick();other.getActionManager().process();other.resetMasks();secondChannel.flush();secondChannel.runPendingTasks();Object out;while((out=secondChannel.readOutbound())!=null)ReferenceCountUtil.release(out);}
    require(npc.matches(origin)&&p.getActionManager().hasSkillWorking()&&other.getActionManager().hasSkillWorking()&&p.getInventory().getAmountOf(29313)>0&&other.getInventory().getAmountOf(29313)>0,"Native NPC movement interrupted harvesting");
    p.addWalkSteps(p.getX(),p.getY()+1,1,false);tick();p.resetWalkSteps();require(!p.getActionManager().hasSkillWorking()&&npc.getNative950Wander()==0,"First cancellation released other harvester's pause");
    for(int i=0;i<10;i++){npc.processNative950Movement();other.getActionManager().process();other.resetMasks();}
    require(npc.matches(origin)&&other.getActionManager().hasSkillWorking(),"Second harvester lost stationary target");
    other.getActionManager().forceStop();other.getActionManager().forceStop();require(npc.getNative950Wander()==5&&!npc.hasWalkSteps()&&Native950Divination.activeHarvestPauses()==0,"Last/idempotent stop did not restore wander");
    clearItems();for(int i=0;i<28;i++)other.getInventory().items.set(i,null);
    require(Native950Divination.startHarvest(p,npc,1)&&Native950Divination.startHarvest(other,npc,1),"Removed-target setup failed");World.removeNative950Npc(npc);tick();other.getActionManager().process();
    require(!p.getActionManager().hasSkillWorking()&&!other.getActionManager().hasSkillWorking()&&Native950Divination.activeHarvestPauses()==0,"Removed wisp retained harvest owners");
    secondChannel.flush();secondChannel.runPendingTasks();Object out;while((out=secondChannel.readOutbound())!=null)ReferenceCountUtil.release(out);require(secondTransport.terminalFailure()==null&&secondChannel.isActive(),"Second harvester transport failed");
   }finally{other.getActionManager().forceStop();World.removeNative950Player(other);Native950Skilling.detach(other);secondChannel.finishAndReleaseAll();clear();}
  }
  void checkTravel(){
   clear();
   for(Native950RunecraftingTravel.Gateway gateway:Native950RunecraftingTravel.Gateway.values()){
    clearItems();move(gateway.outside());WorldObject ruins=gateway.ruins();World.getRegion(ruins.getRegionId(),true);
    require(Native950Mining.current(ruins)&&Native950Runecrafting.handles(ruins,1),"Missing actual950 ruins "+gateway);
    require(!Native950Runecrafting.start(p,ruins),"Ruins accepted without talisman "+gateway);give(gateway.talisman,1);
    require(Native950Runecrafting.start(p,ruins)&&p.getNextWorldTile().matches(gateway.inside()),"Ruins destination "+gateway+" player="+p+" reach="+Native950Mining.inReach(p,ruins)+" stationary="+Native950Runecrafting.stationary(p)+" walk="+p.getNextWalkDirection()+" teleport="+p.hasTeleported()+" talisman="+p.getInventory().getAmountOf(gateway.talisman));
    move(gateway.inside());require(World.isFloorFree(p.getPlane(),p.getX(),p.getY()),"Blocked altar arrival "+gateway);
    WorldObject portal=gateway.portal();World.getRegion(portal.getRegionId(),true);require(Native950Mining.current(portal),"Missing actual950 exit "+gateway);
    boolean adjacent=false;for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){WorldTile t=portal.transform(d[0],d[1],0);if(!World.isFloorFree(t.getPlane(),t.getX(),t.getY()))continue;move(t);if(Native950Mining.inReach(p,portal)){adjacent=true;break;}}
    require(adjacent,"No portal access "+gateway);clearItems();
    require(Native950Runecrafting.start(p,portal)&&p.getNextWorldTile().matches(gateway.outside()),"Exit destination "+gateway);
    move(gateway.outside());
   }
  }
  void move(WorldTile tile){p.resetWalkSteps();p.setNextWorldTile(new WorldTile(tile));p.processEntityUpdate();p.resetMasks();p.processEntityUpdate();p.resetMasks();p.loadMapRegions();p.setClientHasLoadedMapRegion();drain();}
  void level(int skill,int level){p.getSkills().setLevelWithoutRefresh(skill,level);p.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,level));}
  void give(int id,int count){require(Native950Skilling.giveItem(p,id,count),"Cannot give "+id);}
  void clearItems(){p.getActionManager().forceStop();p.resetWalkSteps();for(int i=0;i<28;i++)p.getInventory().items.set(i,null);p.resetMasks();}
  void clear(){clearItems();if(object!=null){World.removeObject(object);object=null;}if(npc!=null){World.removeNative950Npc(npc);npc=null;}}
  void tick(){p.getActionManager().process();long refusals=Native950EntityMasks.refusals();com.rs.network.protocol.modern950.Native950PlayerMasks.Builder masks=Native950EntityMasks.playerMasks(p);
   if(masks!=null)require(com.rs.network.protocol.modern950.Native950PlayerMasks.encode(masks.build()).length>0,"Empty native masks");
   require(Native950EntityMasks.refusals()==refusals,"Skill animation/effect refused by native mask bridge");p.resetMasks();drain();}
  void drain(){channel.flush();channel.runPendingTasks();Object value;while((value=channel.readOutbound())!=null)ReferenceCountUtil.release(value);channel.checkException();require(transport.terminalFailure()==null&&channel.isActive(),"Native packet transport failed: "+transport.terminalFailure());}
  public void close(){clear();World.removeNative950Player(p);Native950Skilling.detach(p);channel.finishAndReleaseAll();}
 }
 private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
