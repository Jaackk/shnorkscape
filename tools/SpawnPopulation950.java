package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.game.World;import com.rs.utils.data.parsers.npcs.*;import java.nio.file.Paths;import java.util.concurrent.TimeUnit;
public final class SpawnPopulation950 {
 public static void main(String[] args)throws Exception {
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950SpawnScope.PROPERTY,"");Cache.initFlatReadOnly(Paths.get(args[0]));
  Native950World w=Native950World.getInstance();w.execute(()->{
   NPCSpawnsDataParser.init();NPCWeaknessesDataParser.init();NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();
   int rows=0,safe=0,regions=0;for(int r=0;r<=65535;r++){java.util.List<NPCSpawnsDataParser.Native950Spawn> list=NPCSpawnsDataParser.native947Spawns(r);if(!list.isEmpty())regions++;for(NPCSpawnsDataParser.Native950Spawn n:list){rows++;if(Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,n.npcId))safe++;}}
   System.out.println("ROSTER rows="+rows+" regions="+regions+" identitySafe="+safe+" excluded="+(rows-safe));
   for(int r:new int[]{12850,12853,11829,11062}){World.getRegion(r,true);int spawned=w.spawnRegion(r);if(spawned<1)throw new AssertionError("Empty town region "+r);if(w.spawnRegion(r)!=0)throw new AssertionError("Duplicate region spawn");System.out.println("PASS town region="+r+" NPCs="+spawned);}
   System.out.println("PASS "+w.spawnCounters());w.clearNativeNpcs();return null;
  }).get(90,TimeUnit.SECONDS);
 }
}
