package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.*;
import com.rs.game.*;
import com.rs.utils.data.parsers.npcs.*;
import java.nio.file.*;import java.util.*;
/** Offline map, combat and loot evidence; no session or listening socket. */
public class Native950CombatWorldAudit {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get("cache"));NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();NPCDropsDataParser.init();
  Map<String,Object> out=new LinkedHashMap<>();List<Object> npcs=new ArrayList<>(),objects=new ArrayList<>();
  for(int id:new int[]{2881,2882,2883,6260,6261,6263,6265,18932,50,1158,1160,1610,1615,7133}){
   Map<String,Object> r=new LinkedHashMap<>();NPCDefinitions d=NPCDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127),null);
   r.put("id",id);r.put("name",d.name);r.put("size",d.size);r.put("params",d.clientScriptData);r.put("combat",NPCCombatDefinitionsDataParser.getDefinitions().get(id));r.put("stats",NPCStatsDataParser.getDefinitions().get(id));
   Native950NpcCombatCatalog.Resolution result=Native950NpcCombatCatalog.inspectRunningCache(id);r.put("admission",result.profile==null?result.reason:"ACCEPTED");
   r.put("drops",NPCDropsDataParser.getDrops(id));npcs.add(r);
  }
  for(int[] xy:new int[][]{{3294,10129},{2910,4448},{2544,3740},{2870,5369},{2880,5310},{3428,3537},{2884,9798},{3210,9616}}){
   int region=new WorldTile(xy[0],xy[1],0).getRegionId();Region r=World.getRegion(region,true);
   if(r.getAllObjects()==null)continue;
   for(WorldObject o:r.getAllObjects()){
    ObjectDefinitions d=o.getDefinitions();String n=d.name==null?"":d.name.toLowerCase(Locale.ROOT);
    if(!n.matches(".*(portal|ladder|stair|door|entrance|exit|gate|chain|cave|rope|hole|trapdoor|barrier|crevice).*"))continue;
    Map<String,Object> v=new LinkedHashMap<>();v.put("id",o.getId());v.put("name",d.name);v.put("x",o.getX());v.put("y",o.getY());v.put("plane",o.getPlane());v.put("type",o.getType());v.put("rotation",o.getRotation());v.put("options",d.options);objects.add(v);
   }
  }
  if(args.length>1){
   List<String> symbols=new ArrayList<>();symbols.add("# Project-authored aliases, not recovered Jagex names. Reproduce with Native950CombatWorldAudit.");
   for(int id:new int[]{2881,2882,2883,6260,6261,6263,6265,18932,50,1158,1160,1610,1615,7133}){
    NPCDefinitions d=NPCDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127),null);
    symbols.add(symbol("npc",id,d.name,18,id>>>7,id&127));
   }
   for(int id:new int[]{82481,82483,82485,82487,82666,82667,82668,82669,82488,82489,82491,114746,114761,10229,8930,8929,26425,74864}){
    symbols.add(symbol("object",id,ObjectDefinitions.getObjectDefinitions(id).name,16,id>>>8,id&255));
   }
   Files.write(Paths.get(args[1]),symbols,java.nio.charset.StandardCharsets.UTF_8);
  }
  out.put("npcs",npcs);out.put("objects",objects);
  Files.write(Paths.get(args[0]),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(out).getBytes("UTF-8"));
  System.out.println("Audited "+npcs.size()+" NPCs and "+objects.size()+" travel objects");
 }
 static String symbol(String type,int id,String name,int index,int group,int file)throws Exception{
  return type+"\t"+id+"\tshnorkscape_"+name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","_")+"_"+id+"\t"+index+"\t"+group+"\t"+file+"\t"+sha(Cache.STORE.getIndexes()[index].getFile(group,file))+"\tProject alias: exact950 decoded definition and map/combat audit; not an original gameval\t"+sha(Cache.STORE.getIndex255().getArchiveData(index));
 }
 static String sha(byte[] raw)throws Exception{if(raw==null)throw new IllegalStateException("Missing definition");StringBuilder b=new StringBuilder();for(byte v:java.security.MessageDigest.getInstance("SHA-256").digest(raw))b.append(String.format("%02x",v&255));return b.toString();}

}
