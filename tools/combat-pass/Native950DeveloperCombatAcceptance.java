package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.data.parsers.npcs.*;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.*;import java.util.*;
/** Isolated real-cache tooling/ownership acceptance; never opens a socket or loads player saves. */
public class Native950DeveloperCombatAcceptance {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 static void position(Player p,WorldTile tile){p.setNextWorldTile(null);p.setLocation(tile);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();}
 public static void main(String[] args)throws Exception{
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
  Cache.initFlatReadOnly(Paths.get("cache"));Native950World world=Native950World.getInstance();
  world.execute(()->{
   NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();NPCDropsDataParser.init();
   EmbeddedChannel c=new EmbeddedChannel(),d=new EmbeddedChannel();
   Player p=Player.createNative950("developer-probe-a",new WorldTile(3200,3200,0),c),other=Player.createNative950("developer-probe-b",new WorldTile(3200,3210,0),d);
   p.setActive(true);other.setActive(true);World.addNative950Player(p,1);World.addNative950Player(other,2);
   try{
    check(Native950CombatInspector.search("50").stream().anyMatch(s->s.contains("special attack style")),"blocked boss reason");
    check(Native950CombatInspector.search("shnorkscape_general_graardor_6260").stream().anyMatch(s->s.contains("Attackable by native combat: true")),"symbol lookup");
    check(Native950BossCatalogue.details(2882).stream().anyMatch(s->s.contains("950 payload SHA-256 MATCH")),"related binding verification");
    for(int id:new int[]{2881,6260}){position(p,new WorldTile(3200,3200,0));Native950BossCatalogue.travel(p,id);check(p.getNextWorldTile()!=null,"boss travel "+id);}
    WorldTile arena=null;
    for(int x=3200;x<3250&&arena==null;x+=10)for(int y=3200;y<3250;y+=10){
     position(p,new WorldTile(x,y,0));String result=Native950BossCatalogue.spawn(p,6260);
     if(result.startsWith("Created")){arena=new WorldTile(p);break;}
     check(world.nativeNpcs().isEmpty(),"failed spawn left partial actors: "+result);
    }
    check(arena!=null,"no test arena floor found");check(world.nativeNpcs().size()==4,"Graardor encounter actor count");
    List<NPC> first=new ArrayList<>(world.nativeNpcs());
    check(Native950BossCatalogue.spawn(p,6260).startsWith("Clear"),"duplicate encounter guard");
    boolean made=false;
    for(int x=3200;x<3260&&!made;x+=10)for(int y=3260;y<3310;y+=10){position(other,new WorldTile(x,y,0));if(Native950BossCatalogue.spawn(other,2883).startsWith("Created")){made=true;break;}}
    check(made,"second owner encounter");check(world.nativeNpcs().size()==5,"second owner actor");
    Native950BossCatalogue.clear(p);check(world.nativeNpcs().size()==1,"clear must preserve second owner");
    for(NPC actor:first)check(!World.containsNPC(actor),"cleared actor still visible");
    java.lang.reflect.Field combatField=Native950World.class.getDeclaredField("combat");combatField.setAccessible(true);
    Native950MeleeCombat combat=(Native950MeleeCombat)combatField.get(world);combat.attach(other);
    combat.detach(other);check(world.nativeNpcs().isEmpty(),"actual combat detach/logout cleanup");
    combat.attach(p);Native950AbilityCatalog.Definition surge=Native950AbilityCatalog.get(14726);
    p.getNative950ActionBar().restore(Collections.singletonMap("actionBar.0",Native950ActionBar.pack(surge.book,surge.key)));
    check(combat.abilityDiagnostic(p,1).stream().anyMatch(line->line.contains("Presentation sequence=18358")),"real-cache diagnostic uses verified Surge binding");
    // Force failure after the boss has been created by occupying the second actor's cell.
    position(p,arena);NPC blocker=Native950DiagnosticSpawns.placeNpc(p,16027,new WorldTile(arena.getX()+7,arena.getY()+3,0),false);
    String failure=Native950BossCatalogue.spawn(p,6260);check(failure.contains("rolled back"),"blocked group must fail atomically");
    check(world.nativeNpcs().size()==1&&World.containsNPC(blocker),"rollback must preserve preexisting actor");world.removeDiagnosticNpc(blocker);
    System.out.println("PASS: NPC refusal/symbol lookup; related pins; boss travel; four-actor encounter; duplicate guard; two-owner clear/logout isolation; late-placement rollback");
   }finally{Native950BossCatalogue.cleanup(p);Native950BossCatalogue.cleanup(other);c.finishAndReleaseAll();d.finishAndReleaseAll();}
   return null;
  }).get(120,java.util.concurrent.TimeUnit.SECONDS);
 }
}
