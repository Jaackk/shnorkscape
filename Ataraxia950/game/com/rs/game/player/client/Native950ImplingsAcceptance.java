package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;
/** Actual cache and original scheduler; every impling and every original loot row, no account/socket writes. */
public final class Native950ImplingsAcceptance {
 static int captured,lootRows;
 public static void main(String[] args)throws Exception {
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950World world=Native950World.getInstance();
  world.execute(()->{try(Native950ThievingHunterAcceptance.Fixture f=new Native950ThievingHunterAcceptance.Fixture()){
   require(Native950Implings.verifyCacheBindings(),"Impling cache bindings unavailable");
   World.removeNative950Npc(f.npc);f.npc=null;f.clear();f.give(10010,1);
   require(f.items.equip(f.player.getInventory().getItems().getThisItemSlot(10010),10010).moved==1,"Net equip failed");
   for(FlyingEntities data:FlyingEntities.values())if(data.isImpling()){
    f.clear();f.npc=NPC.createNative950Diagnostic(data.getNpcId(),f.player.transform(1,0,0));world.addDiagnosticNpc(f.npc);
    require(Native950Hunter.isCatchable(f.npc,1)&&!Native950Hunter.isCatchable(f.npc,2),"Impling menu mapping "+data);
    require(!Native950Hunter.start(f.player,f.npc,1),"Missing jar accepted "+data);f.give(11260,1);
    f.level(Skills.HUNTER,1);require(!Native950Hunter.start(f.player,f.npc,1),"Level gate missing "+data);f.level(Skills.HUNTER,99);
    double xp=f.player.getSkills().getXp(Skills.HUNTER);
    f.npc.setLocation(f.player.transform(5,0,0));require(!Native950Hunter.start(f.player,f.npc,1),"Remote catch accepted");f.npc.setLocation(f.player.transform(1,0,0));
    require(f.player.getActionManager().setAction(new Native950Hunter.CatchAction(f.npc,1,data,b->99)),"Failed catch start");f.ticks(4);
    require(f.player.getInventory().containsItem(11260,1)&&f.player.getSkills().getXp(Skills.HUNTER)==xp,"Miss consumed/rewarded");
    for(int i=1;i<28;i++)f.player.getInventory().items.set(i,new Item(1511,1));
    require(f.player.getActionManager().setAction(new Native950Hunter.CatchAction(f.npc,1,data,b->0)),"Full bag jar exchange refused");f.ticks(4);
    require(f.player.getInventory().containsItem(data.getReward(),1)&&!f.player.getInventory().containsItem(11260,1)&&f.player.getSkills().getXp(Skills.HUNTER)>xp&&f.npc.hasFinished(),"Capture did not commit "+data);captured++;
    world.clearNativeNpcs();f.npc=null;
    for(int tier=0;tier<4;tier++){
     int rarity=new int[]{0,600,900,998}[tier];Item[] pool=Native950Implings.bucket(data,rarity);
     for(int row=0;row<pool.length;row++){
      f.clear();f.give(data.getReward(),1);int source=f.player.getInventory().getItems().getThisItemSlot(data.getReward());double before=f.player.getSkills().getXp(Skills.HUNTER);
      require(Native950Implings.loot(f.player,source,data.getReward(),3,rolls(rarity,row,0,1,1)),"Loot row refused "+data+"/"+tier+"/"+row);
      require(f.player.getInventory().containsItem(pool[row].getId(),1)&&f.player.getInventory().containsItem(11260,1)&&!f.player.getInventory().containsItem(data.getReward(),1),"Wrong loot output");
      require(f.player.getSkills().getXp(Skills.HUNTER)==before,"Jar opening awarded catch XP");lootRows++;
     }
    }
   }
   f.clear();Item firstJar=new Item(11238,1);f.player.getInventory().items.set(0,firstJar);f.player.getInventory().items.set(5,new Item(11238,1));
   require(Native950InventoryMenu.usesOrdinaryOperations(11238),"Impling jar bypasses ordinary inventory route");
   require(Native950Implings.loot(f.player,5,11238,3,rolls(0,0,0,1)),"Selected duplicate impling jar refused");
   require(f.player.getInventory().getItem(0)==firstJar&&f.player.getInventory().getItems().getNumberOf(11238)==1,"Loot consumed another duplicate jar");
   require(!Native950Implings.loot(f.player,5,11238,3,rolls(0,0,0,1)),"Stale selected jar looted another copy");
   f.clear();Item firstButterfly=new Item(10020,1);f.player.getInventory().items.set(0,firstButterfly);f.player.getInventory().items.set(5,new Item(10020,1));
   require(Native950Hunter.release(f.player,5,10020,1),"Selected duplicate butterfly refused");
   require(f.player.getInventory().getItem(0)==firstButterfly&&f.player.getInventory().getItems().getNumberOf(10020)==1,"Release consumed another butterfly");
   require(!Native950Hunter.release(f.player,5,10020,1),"Stale selected butterfly released another copy");
   f.clear();f.give(11238,1);for(int i=1;i<28;i++)f.player.getInventory().items.set(i,new Item(1511,1));
   require(!Native950Implings.loot(f.player,0,11238,3,rolls(0,0,0,1)),"Full backpack consumed jar");require(f.player.getInventory().containsItem(11238,1)&&!f.player.getInventory().containsItem(946,1),"Full backpack lost/duplicated rewards");
   f.player.getInventory().items.set(1,null);require(Native950Implings.loot(f.player,0,11238,3,rolls(0,0,0,1)),"Freed slot failed to resume");
   require(!Native950Implings.loot(f.player,0,11238,3,rolls(0,0,0,1)),"Stale jar action accepted");
   f.clear();f.give(11238,1);f.player.getControlerManager().startControler(new Controller(){public void start(){}public boolean canAddInventoryItem(int id,int amount){return false;}});
   require(!Native950Implings.loot(f.player,0,11238,3,rolls(0,0,0,1)),"Controller veto consumed jar");require(f.player.getInventory().containsItem(11238,1),"Controller lost jar");f.player.getControlerManager().forceStop();
   f.clear();f.give(15513,1);require(Native950Implings.loot(f.player,0,15513,3,rolls(0,0,0,1,0,0)),"Spirit charm opening refused");require(f.player.getInventory().containsItem(12158,1),"Spirit reward must be one charm");
   f.clear();f.give(11238,1);f.player.setHitpoints(1);require(Native950Implings.loot(f.player,0,11238,3,rolls(0,0,0,0)),"Broken jar failed");require(!f.player.getInventory().containsItem(11260,1)&&f.player.getHitpoints()==1,"Broken jar returned/caused death");
   world.clearNativeNpcs();
  }return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
  System.out.println("PASS: "+captured+" impling captures, "+lootRows+" original weighted loot row paths; requirements, miss, reach, full inventory replacement, atomic loot rollback/resume, controller/stale guards, Spirit charm and nonlethal broken jar");
 }
 static IntUnaryOperator rolls(int... values){int[] at={0};return bound->{int value=at[0]<values.length?values[at[0]++]:0;if(value<0||value>=bound)throw new AssertionError("Test roll "+value+" exceeds "+bound);return value;};}
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
