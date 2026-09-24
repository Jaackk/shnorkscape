package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.player.*;
import com.rs.game.player.content.Pots;
import com.rs.game.item.Item;
import com.rs.network.protocol.modern950.Native950Packets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static com.rs.game.player.client.Native950CombatPassAcceptance.*;
/** Real-cache regression for the September24 live polish; never reads character saves. */
public final class Native950CombatPolishAcceptance {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get(args.length==0?"cache":args[0]));Native950AbilityAssets.verify();
  com.rs.game.player.client.ui.Native950Bindings bindings=com.rs.game.player.client.ui.Native950Bindings.tryLoad();
  Native950IdMap.install(new Native950ActionRouter.IdMapAdapter(bindings.allowListResolver()));
  Native950World.getInstance().execute(()->{run();return null;}).get(90,TimeUnit.SECONDS);
  System.out.println("POLISH PASS checks="+checks+"; ephemeral accounts; live visuals pending");System.exit(0);
 }
 static void run()throws Exception{
  conjureExamine();phantomTargets();soulSwapAndExit();
  try(Fixture f=new Fixture()){
   equip(f.first,3,false,false);equip(f.second,3,false,false);
   java.lang.reflect.Field field=Native950MeleeCombat.class.getDeclaredField("necromancy");field.setAccessible(true);
   Native950NecromancyResources resources=(Native950NecromancyResources)field.get(f.combat);
   for(int i=0;i<5;i++)resources.gainSoul(f.first,0);
   int souls=resources.souls(f.first);check(souls>=3,"Expected high-tier conduit soul capacity");
   check(f.combat.attack(f.first,f.npc)==null,"Volley target refused");
   f.cast(f.first,48301);
   check(resources.souls(f.first)==0,"Volley did not consume resources once");
   check(f.combat.pendingHitCount(f.first)==souls,"Volley did not create one flight per soul");
   for(int tick=0;tick<5&&f.combat.pendingHitCount(f.first)>0;tick++){
    f.step(1);
    int landed=f.npc.getNextHits().size();
    check(landed==0||landed==souls,"Volley was artificially split across hit ticks");
   }
   check(f.combat.pendingHitCount(f.first)==0,"Volley never arrived");
   check(resources.souls(f.second)==0,"Volley modified other player's resources");
  }

  try(Fixture f=new Fixture()){
   equip(f.first,3,false,false);equip(f.second,3,false,false);
   int base=f.first.getSkills().getLevelForXp(Skills.NECROMANCY);
   java.lang.reflect.Method damage=Native950MeleeCombat.class.getDeclaredMethod("conjureAbilityDamage",Player.class);damage.setAccessible(true);
   int before=(Integer)damage.invoke(f.combat,f.first);
   drain(f.firstChannel);drain(f.secondChannel);
   f.first.getInventory().items.set(0,new Item(23531));
   check(Native950Potions.drink(f.first,0,23531),"Real overload flask was refused");
   check(f.first.getInventory().getItem(0).getId()==23532,"Overload dose was not consumed");
   Pots.applyOverLoadEffect(f.first);
   check(f.first.getSkills().getLevel(Skills.NECROMANCY)==base+base*15/100+3,"Wrong Necromancy overload boost");
   byte[] boosted=Native950Packets.updateStat(Skills.NECROMANCY,base+base*15/100+3,(int)f.first.getSkills().getXp(Skills.NECROMANCY)).frame(()->0);
   check(drain(f.firstChannel).stream().anyMatch(b->Arrays.equals(b,boosted)),"Boost did not reach native UPDATE_STAT");
   check((Integer)damage.invoke(f.combat,f.first)>before,"Boost did not reach Necromancy damage calculation");
   check(f.second.getSkills().getLevel(Skills.NECROMANCY)==120,"Other player's stat changed");
   Native950Potions.removeOverloadOnDeath(f.first);
   check(f.first.getSkills().getLevel(Skills.NECROMANCY)==base,"Death left boosted Necromancy");
   Pots.applySupremeOverLoadEffect(f.first);
   check(f.first.getSkills().getLevel(Skills.NECROMANCY)==base+base*16/100+4,"Wrong supreme Necromancy boost");
   Pots.resetSupremeOverLoadEffect(f.first);
   check(f.first.getSkills().getLevel(Skills.NECROMANCY)==base,"Expiry left boosted Necromancy");
   drain(f.firstChannel);drain(f.secondChannel);
   Native950Completionist.grant(f.first);
   for(Native950QuestCatalog.Quest q:Native950QuestCatalog.all()){
    for(int[] row:q.varps)check(f.first.getVarsManager().getValue(row[0])>=row[2],"Missing native quest varp "+q.name);
    for(int[] row:q.varbits)check(f.first.getVarsManager().getBitValue(row[0])>=row[2],"Missing native quest varbit "+q.name);
   }
   java.lang.reflect.Field unlockTable=Native950CombatProgression.class.getDeclaredField("UNLOCKS");unlockTable.setAccessible(true);
   for(int[] unlock:(int[][])unlockTable.get(null))check(f.first.getVarsManager().getBitValue(unlock[0])>=unlock[1],"Permanent unlock missing after fresh comp: "+unlock[0]);
   check(f.first.getVarsManager().getBitValue(54631)==1,"Army talent remained locked");
   check(f.first.getVarsManager().getBitValue(53587)==3,"Spirit Pact not completed");
   for(int i=0;i<4;i++)check(f.first.getVarsManager().getValue(11499+i)==i+1,"Empty army selection not initialised");
   check(f.second.getVarsManager().getBitValue(54631)==0,"Other player's talent unlocked");
   check(f.second.getVarsManager().getValue(1297)==0,"Other player's quests changed");
   java.util.List<byte[]> frames=drain(f.firstChannel);
   byte[] expected=Native950Packets.varbitLarge(54631,1).frame(()->0);
   check(frames.stream().anyMatch(b->Arrays.equals(b,expected)),"Talent unlock never reached native transport");
   // Regrant must republish selections even when the model already has them from restore.
   f.first.getVarsManager().setVar(11499,4);
   drain(f.firstChannel);Native950CombatProgression.grant(f.first);
   java.util.List<byte[]> replay=drain(f.firstChannel);
   for(int i=0;i<4;i++){
    final byte[] selection=Native950Packets.varp(11499+i,i==0?4:i+1).frame(()->0);
    check(replay.stream().anyMatch(b->Arrays.equals(b,selection)),"Army selection not republished after restore");
   }
   Native950Quests quests=new Native950Quests(f.first,f.firstChannel);quests.opened(0,1);
   check(quests.summaryCounts()[0]==0&&quests.summaryCounts()[2]>300,"Quest summary erased developer completion");
   check(drain(f.secondChannel).isEmpty(),"Progression wrote to the other player's channel");
  }
 }
 static void soulSwapAndExit(){
  try(Fixture f=new Fixture()){
   equip(f.first,3,false,false);equip(f.second,3,false,false);
   f.first.getEquipment().getItems().set(5,new Item(55482));
   Native950NecromancyResources r=new Native950NecromancyResources();
   for(int i=0;i<5;i++)r.gainSoul(f.first,0);
   r.gainSoul(f.second,0);
   f.first.getEquipment().getItems().set(5,null);
   check(Native950NecromancyResources.soulCap(f.first)==3,"Unequipped gain cap changed");
   for(int t=1;t<=30;t++)r.pulse(t,p->p==f.first);
   check(r.souls(f.first)==5&&f.first.getVarsManager().getValue(11035)==5&&f.first.getNative950SoulVisual()==5,"Style swap discarded earned souls or desynchronised presentation");
   check(r.souls(f.second)==0,"Other player's expiry extended");
   r.gainSoul(f.first,30);check(r.souls(f.first)==5,"Gain at lower cap discarded stored souls");
   r.pulse(31);r.pulse(40);check(r.souls(f.first)==5,"Souls expired before combat-exit grace");
   r.pulse(40,p->p==f.first);r.pulse(41);r.pulse(50);check(r.souls(f.first)==5,"Re-entry did not restart grace");
   r.pulse(51);check(r.souls(f.first)==0&&f.first.getNative950SoulVisual()==0,"Expiry did not clear resources and visuals together");
   check(f.combat.attack(f.first,f.npc)==null,"Engagement fixture refused");
   check(f.combat.hasCombatEngagement(f.first),"Selected combat engagement ignored");
   check(!f.combat.hasCombatEngagement(f.second),"Engagement leaked to other player");
   equip(f.first,3,false,false);f.cast(f.first,48298);f.step(5);f.combat.cancelAttack(f.first);
   check(f.combat.hasCombatEngagement(f.first),"Stopping personal attacks erased incoming NPC engagement");
   f.combat.stop(f.first);check(!f.combat.hasCombatEngagement(f.first),"Genuine combat stop retained engagement forever");
  }
 }
 static void conjureExamine(){
  Native950World world=Native950World.getInstance();
  try(Fixture f=new Fixture()){
   f.first.loadMapRegions();f.first.setClientHasLoadedMapRegion();
   Native950NpcView view=new Native950NpcView(null,null);
   Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
   Native950Content content=new Native950Content(new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops(),bank);
   Native950Interactions input=new Native950Interactions(f.first,f.firstChannel,content,null,view);
   com.rs.game.npc.NPC actor=com.rs.game.npc.NPC.createNative950Conjure(30265,f.first.transform(1,0,0));
   world.addConjure(actor);boolean cantInteractBefore=actor.isCantInteract();
   try{
    byte[] payload={0,(byte)(actor.getIndex()>>>8),(byte)(actor.getIndex()+128)};
    com.rs.network.protocol.modern950.Native950Actions.Action examine=com.rs.network.protocol.modern950.Native950Actions.decode(36,payload);
    check(examine!=null,"Examine packet did not decode");
    drain(f.firstChannel);input.handle(examine);
    byte[] expected=Native950Packets.gameMessage(0,"Skeleton Warrior - a conjured spirit from the Underworld.").frame(()->0);
    check(drain(f.firstChannel).stream().noneMatch(b->Arrays.equals(b,expected)),"Unpublished conjure could be examined");
    view.viewport().synchronize(f.first,f.firstChannel,7,false,Collections.singletonList(actor));
    check(view.canInteract(f.first,actor.getIndex()),"Conjure not published to viewer");
    drain(f.firstChannel);drain(f.secondChannel);input.handle(examine);
    check(drain(f.firstChannel).stream().anyMatch(b->Arrays.equals(b,expected)),"Published conjure Examine did not reply");
    check(drain(f.secondChannel).isEmpty(),"Examine reply reached other player");
    check(actor.isCantInteract()==cantInteractBefore&&actor.getNative950CombatProfile()==null,"Examine enabled hostile interaction");
   }finally{input.close();view.close();world.removeConjure(actor);}
  }
 }
 static void phantomTargets()throws Exception{
  try(Fixture f=new Fixture()){
   equip(f.first,3,false,false);equip(f.second,3,false,false);f.first.setInfiniteCombatRunes(true);f.second.setInfiniteCombatRunes(true);
   java.util.List<com.rs.game.npc.NPC> targets=new ArrayList<>();
   for(int i=0;i<8;i++){
    com.rs.game.npc.NPC n=com.rs.game.npc.NPC.createNative950(12353,new com.rs.game.WorldTile(3218+(i==6?8:0),3258,i==7?1:0),1);n.setIndex(i+1);targets.add(n);
   }
   Native950MeleeCombat c=new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Access(){
    public void activate(com.rs.game.npc.NPC n){}public boolean player(Player p){return p==f.first||p==f.second;}
    public boolean npc(com.rs.game.npc.NPC n){return targets.contains(n);}public boolean clear(com.rs.game.WorldTile t){return true;}
    public boolean reach(com.rs.game.Entity a,com.rs.game.Entity b){return true;}public boolean approach(Player p,com.rs.game.npc.NPC n){return true;}
    public com.rs.game.npc.NPC spawnConjure(Player p,int id){return com.rs.game.npc.NPC.createNative950Conjure(id,p);}
    public void removeConjure(com.rs.game.npc.NPC n){}
   },new Native950MeleeCombat.Rolls(){public boolean accurate(long a,long b){return true;}public int damage(int maximum){return maximum/2;}},Native950CombatStyles::loadout);
   try{
    c.attach(f.first);c.attach(f.second);
    for(com.rs.game.npc.NPC n:targets)c.register(n,new Native950NpcCombatProfile(12353,1,1,1000000,1,1,0,100,1,10,-1,-1,-1,0,0));
    check(c.attack(f.first,targets.get(0))==null,"Phantom command target refused");
    java.lang.reflect.Field field=Native950MeleeCombat.class.getDeclaredField("conjures");field.setAccessible(true);Native950Conjures spirits=(Native950Conjures)field.get(c);
    spirits.cast(f.first,31820,0);spirits.cast(f.second,31820,0);spirits.absorb(f.first,100);drain(f.firstChannel);drain(f.secondChannel);
    spirits.cast(f.first,32342,1);
    for(int i=0;i<8;i++)check(!targets.get(i).getNextHits().isEmpty()==(i<5),"Phantom command target cap/range/plane wrong: "+i);
    byte[] cleared=Native950Packets.varp(11823,0).frame(()->0);
    check(drain(f.firstChannel).stream().anyMatch(b->Arrays.equals(b,cleared)),"Valour clear not published");
    check(f.second.getVarsManager().getValue(11820)==1&&f.second.getVarsManager().getValue(11823)==0,"Command changed other army");
    check(drain(f.secondChannel).isEmpty(),"Command published to other owner");
   }finally{c.clear();}
  }
 }
}
