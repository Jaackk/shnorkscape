package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cache.loaders.*;import com.rs.game.item.Item;import com.rs.game.player.*;
import java.nio.file.*;import java.util.*;import java.util.concurrent.TimeUnit;
import static com.rs.game.player.client.Native950CombatPassAcceptance.*;
/** Real patched-cache gate. Synthetic players only; no client/live profile access. */
public final class Native950CombatPolish2Acceptance {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950AbilityAssets.verify();
  Native950IdMap.install(new Native950ActionRouter.IdMapAdapter(com.rs.game.player.client.ui.Native950Bindings.tryLoad().allowListResolver()));
  Native950World.getInstance().execute(()->{run();return null;}).get(120,TimeUnit.SECONDS);
  System.out.println("POLISH2 PASS checks="+checks+"; paired cache, ephemeral players; Vulkan pending");System.exit(0);
 }
 static void run()throws Exception{
  Native950CombatPolishAcceptance.run();conjurePresentation();
  Native950EquipmentCatalogue.current();
  check(Native950DeveloperSearch.verifiedItem(61355),"Entropic no longer admitted by global developer search");
  check(!Native950DeveloperSearch.find(Native950DeveloperSearch.current(),"entropic lantern").isEmpty(),"Entropic no longer searchable");
  for(int id:new int[]{30265,30266,30267,31142}){
   NPCDefinitions d=NPCDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[18].getFile(id>>7,id&127),null);
   check(d.aBool5315,"Client interaction still disabled for conjure "+id);
   for(String op:d.menuOptions)check(op==null||!op.equalsIgnoreCase("Attack"),"Hostile menu introduced");
   check(Native950Conjures.verifiedName(id)!=null,"Patched companion failed identity verification");
  }
  check(ItemDefinitions.decodeStrict947(61355,Cache.STORE.getIndexes()[19].getFile(239,171),null).getCSOpcode(8928)==48397,"Entropic native passive missing");
  try(Fixture f=new Fixture()){
   equip(f.first,3,false,false);
   f.first.getEquipment().getItems().set(5,new Item(61355));
   check(Native950NecromancyResources.soulCap(f.first)==5,"Entropic server cap differs from native CS17459");
   Native950NecromancyResources r=new Native950NecromancyResources();for(int i=0;i<7;i++)r.gainSoul(f.first,0);
   check(r.souls(f.first)==5&&f.first.getVarsManager().getValue(11035)==5,"Entropic count/publication not five");
   f.first.getEquipment().getItems().set(5,null);r.pulse(1,p->p==f.first);check(r.souls(f.first)==5,"Equipment swap erased earned souls");
   check(Native950AbilityCatalog.animation(f.first,19254)==19866,"Sunshine native pose missing");
   check(Native950AbilityCatalog.animation(f.first,19251)==19879,"Swiftness native pose missing");
   check(Native950AbilityCatalog.animation(f.first,14726)==18358,"Surge native pose missing");
   check(AnimationDefinitions.getAnimationDefinitions(35475).getNative950EmoteTime()==1200,"Living Death guard is not sixty native cycles");
   check(AnimationDefinitions.getAnimationDefinitions(37099).getNative950EmoteTime()==1200,"Skeletal Surge timing missing");
   check(GraphicDefinitions.getGraphicDefinitions(3856).emoteId==19865,"Sunshine world graphic lost exact sequence");
   Native950CombatBuffs buffs=new Native950CombatBuffs();buffs.apply(f.first,Native950CombatBuffs.Type.SUNSHINE,0);
   check(buffs.outgoing(f.first,com.rs.game.Hit.HitLook.MAGIC_DAMAGE,100,1)==150,"Sunshine in-area bonus missing");
   check(buffs.outgoing(f.second,com.rs.game.Hit.HitLook.MAGIC_DAMAGE,100,1)==100,"Sunshine bonus leaked to other player");
   f.first.setLocation(f.first.getX()+4,f.first.getY(),f.first.getPlane());
   check(buffs.outgoing(f.first,com.rs.game.Hit.HitLook.MAGIC_DAMAGE,100,1)==100,"Sunshine followed caster outside area");
   check(!buffs.active(f.first,Native950CombatBuffs.Type.SUNSHINE,50),"Sunshine did not expire");
  }
 }
 static void conjurePresentation()throws Exception{
  try(Fixture f=new Fixture()){
   equip(f.first,3,false,false);equip(f.second,3,false,false);
   f.first.setInfiniteCombatRunes(true);f.second.setInfiniteCombatRunes(true);
   final Map<com.rs.game.npc.NPC,Player> actors=new IdentityHashMap<>();
   final Map<Player,Integer> hits=new IdentityHashMap<>();
   com.rs.game.npc.NPC target=com.rs.game.npc.NPC.createNative950(12353,new com.rs.game.WorldTile(3220,3258,0),1);
   Native950Conjures spirits=new Native950Conjures(new Native950Conjures.Host(){
    public com.rs.game.npc.NPC spawn(Player p,Native950Conjures.Kind k){com.rs.game.npc.NPC n=com.rs.game.npc.NPC.createNative950Conjure(k.npc,p);actors.put(n,p);return n;}
    public void remove(com.rs.game.npc.NPC n){actors.remove(n);}
    public void follow(com.rs.game.npc.NPC n,com.rs.game.WorldTile t){}
    public boolean valid(Player p){return Native950Conjures.ownerLifecycleValid(p);}
    public boolean conduit(Player p){return true;}
    public com.rs.game.npc.NPC target(Player p){return target;}
    public boolean reach(com.rs.game.npc.NPC n,com.rs.game.npc.NPC t,int r){return true;}
    public int strike(Player p,com.rs.game.npc.NPC t,int min,int max){hits.put(p,hits.getOrDefault(p,0)+1);return min;}
    public void area(Player p,com.rs.game.WorldTile t,int r,int min,int max){}
    public int abilityDamage(Player p){return 100;}
   });
   try{
    spirits.cast(f.first,33965,0);spirits.cast(f.second,33965,0);
    check(actors.size()==8,"Two armies share actors");
    for(com.rs.game.npc.NPC n:actors.keySet()){
     Native950Conjures.Kind k=null;for(Native950Conjures.Kind candidate:Native950Conjures.Kind.values())if(candidate.npc==n.getId())k=candidate;
     check(n.getNextAnimation()!=null&&n.getNextAnimation().getIds()[0]==Native950PresentationBindings.conjureSpawn(k),"Spawn pose absent for "+n.getId());
     n.resetMasks();
    }
    spirits.pulse(7);
    for(com.rs.game.npc.NPC n:actors.keySet())if(n.getId()==30265||n.getId()==30266)
     check(n.getNextAnimation()!=null,"Conjure attack pose absent");
    com.rs.game.npc.NPC dying=null;for(com.rs.game.npc.NPC n:actors.keySet())if(actors.get(n)==f.first&&n.getId()==30265)dying=n;
    dying.setHitpoints(0);
    spirits.clear(f.first);
    check(dying.isNative950DeathVisible(),"Dying companion hidden before exit pose");
    check(spirits.count(f.first)==0&&spirits.count(f.second)==4,"Exiting owner affected other army");
    check(actors.size()==8,"Actors removed before exit presentation");
    int before=hits.get(f.first);
    for(com.rs.game.npc.NPC n:actors.keySet())if(actors.get(n)==f.first)check(n.getNextAnimation()!=null,"Exit pose absent");
    spirits.pulse(8);check(hits.get(f.first)==before,"Retiring actor still attacked");
    spirits.pulse(40);check(actors.size()==4,"Retiring actors did not leave after presentation");
    check(spirits.count(f.second)==4,"Other owner expired prematurely");
   }finally{spirits.clear();check(actors.isEmpty(),"Shutdown leaked retired actors");}
  }
 }

}
