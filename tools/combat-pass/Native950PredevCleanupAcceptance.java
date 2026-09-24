package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.item.Item;
import com.rs.game.player.*;
import com.rs.network.protocol.modern950.Native950Actions;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static com.rs.game.player.client.Native950CombatPassAcceptance.*;

/** Paired-cache validation only; no live saves, socket listener or deployment. */
public final class Native950PredevCleanupAcceptance {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950AbilityAssets.verify();
  Native950IdMap.install(new Native950ActionRouter.IdMapAdapter(com.rs.game.player.client.ui.Native950Bindings.tryLoad().allowListResolver()));
  Native950World.getInstance().execute(()->{run();return null;}).get(120,TimeUnit.SECONDS);
  System.out.println("PREDEV PASS checks="+checks+"; paired cache; physical Vulkan pending");System.exit(0);
 }
 static void run()throws Exception{
  Native950CombatQolAcceptance.run();
  try(Fixture f=new Fixture()){
   Player p=f.first;Native950ActionBar bar=p.getNative950ActionBar();
   Native950Settings settings=new Native950Settings(p,f.firstChannel,()->{});
   settings.handle(button(1430,256,-1));
   check(!settings.handle(button(365,20,4)),"Unarmed dropdown value accepted");
   settings.handle(button(365,19,10754));
   settings.handle(button(365,19,10756));settings.handle(button(365,20,1));
   settings.handle(button(365,19,10758));settings.handle(button(365,20,2));
   check(bar.preferences.boundBar(0,false,true)==1,"Native row/value did not save melee bar2");
   settings.handle(button(365,19,10760));settings.handle(button(365,20,4));
   settings.handle(button(365,19,10762));settings.handle(button(365,20,4));
   check(bar.preferences.boundBar(1,true,false)==3,"Native row/value did not save ranged bar4");
   Native950ActionBar restored=new Native950ActionBar();restored.restore(p.nativeSettingsSnapshot());
   check(restored.preferences.boundBar(0,false,true)==1,"Binding lost on restore");
   check(f.second.getNative950ActionBar().preferences.boundBar(0,false,true)==-1,"Binding leaked");
   settings.close();check(!settings.handle(button(365,20,1)),"Closed setting accepted stale value");
   equip(p,0,false,false);bar.preferences.equipmentChanged(p,false);check(bar.activeBar()==1,"Equipped melee did not select bar2");
   equip(p,1,false,false);bar.preferences.equipmentChanged(p,false);check(bar.activeBar()==3,"Equipped ranged did not select bar4");
   bar.preferences.presetSwitch=false;equip(p,0,false,false);bar.preferences.equipmentChanged(p,true);check(bar.activeBar()==3,"Disabled bank-preset switch was ignored");
   bar.preferences.presetSwitch=true;bar.preferences.equipmentChanged(p,true);check(bar.activeBar()==1,"Enabled bank-preset switch failed");
  }
  String[] names={"melee","range","mage","necro"};
  for(int style=0;style<4;style++)try(Fixture f=new Fixture()){
   Player p=f.first;equip(p,style,false,false);
   for(int slot=0;slot<28;slot++)p.getInventory().items.set(slot,new Item(20135,1));
   int oldWeapon=p.getEquipment().getWeaponId();
   Native950ContentCommands.handle(p,f.firstChannel,new String[]{names[style]});
   check(p.getEquipment().getWeaponId()==Native950DeveloperLoadouts.builtins().get(style).equipment[3].getId(),"Quick gear did not equip Best weapon");
   int helmets=0,weapons=0;for(Item[] tab:p.getBank().bankTabs)for(Item i:tab)if(i!=null){if(i.getId()==20135)helmets+=i.getAmount();if(i.getId()==oldWeapon)weapons+=i.getAmount();}
   check(helmets==28&&weapons>=1,"Quick gear lost displaced property");
   for(Item i:p.getInventory().items.getItems())if(i!=null)check(i.getId()!=42251,"Quick gear supplied food");
   check(f.second.getBank().bankTabs[0].length==0,"Quick gear changed other bank");
  }
  check((Native950InventoryMenu.eventsForItem(962)&(8<<11))!=0,"Cracker cannot target players");
  check((Native950InventoryMenu.eventsForItem(995)&(8<<11))==0,"Ordinary items unexpectedly target players");
  check(Native950InventoryMenu.eventsForItem(-1)==Native950InventoryMenu.EVENT_MASK,"Empty former cracker slot retains player targeting");
 }
 private static Native950Actions.InterfaceAction button(int iface,int component,int slot){
  int hash=(iface<<16)|component;byte[] b={(byte)255,(byte)255,(byte)255,(byte)(hash>>>16),(byte)(hash>>>24),(byte)hash,(byte)(hash>>>8),(byte)(slot>>>8),(byte)slot};
  return (Native950Actions.InterfaceAction)Native950Actions.decode(18,b);
 }
}
