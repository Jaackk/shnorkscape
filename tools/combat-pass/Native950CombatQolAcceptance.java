package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.game.item.Item;import com.rs.game.player.*;import java.nio.file.*;import java.util.*;import java.util.concurrent.TimeUnit;
import static com.rs.game.player.client.Native950CombatPassAcceptance.*;
/** Real paired950 cache and ephemeral players; never accesses live profiles. */
public final class Native950CombatQolAcceptance {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950AbilityAssets.verify();
  Native950IdMap.install(new Native950ActionRouter.IdMapAdapter(com.rs.game.player.client.ui.Native950Bindings.tryLoad().allowListResolver()));
  Native950World.getInstance().execute(()->{run();return null;}).get(120,TimeUnit.SECONDS);
  System.out.println("QOL PASS checks="+checks+"; real cache, ephemeral players; Vulkan pending");System.exit(0);
 }
 static void run()throws Exception{
  Native950CombatPolish2Acceptance.run();
  try(Fixture f=new Fixture()){
   f.first.setDisplayName("qolone");equip(f.first,3,false,false);f.first.getAppearence().generateAppearenceData();byte[] before=f.first.getAppearence().getAppeareanceData();
   check(Native950LivingDeathAppearance.verified(),"Living Death cache identity missing");
   f.cast(f.first,48324);check(f.first.getAppearence().isNative950LivingDeath(),"Living Death did not activate morph");
   byte[] during=f.first.getAppearence().getAppeareanceData();check(!Arrays.equals(before,during),"Morph appearance unchanged");
   check(during[2]==1&&(during[3]&255)==118&&(during[4]&255)==60&&during[5]==0,"Wrong950 morph sentinel/NPC/render encoding");
   check(!f.second.getAppearence().isNative950LivingDeath(),"Morph leaked to other player");
   f.combat.detach(f.first);check(!f.first.getAppearence().isNative950LivingDeath(),"Detach retained skeleton");
   check(Arrays.equals(before,f.first.getAppearence().getAppeareanceData()),"Original appearance not restored");
  }
  for(int style=0;style<4;style++){
   Native950DeveloperLoadouts.Loadout l=Native950DeveloperLoadouts.builtins().get(style);Item[] quick=Native950ContentCommands.quickItems(l);
   check(quick.length<=28,"Best quick kit exceeds backpack");
   for(Item i:quick)check(i.getId()!=42251&&Native950DeveloperSearch.verifiedItem(i.getId()),"Food or invalid item in quick kit");
   for(Item i:l.equipment)if(i!=null)check(Arrays.stream(quick).anyMatch(q->q.getId()==i.getId()&&q.getAmount()==i.getAmount()),"Best equipment omitted");
  }
 }
}
