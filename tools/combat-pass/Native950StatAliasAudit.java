package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cache.loaders.NPCDefinitions;import com.rs.utils.data.parsers.npcs.*;import java.util.*;import java.nio.file.*;
/** Read-only candidate discovery. Does not grant admission. */
public final class Native950StatAliasAudit {
 static String key(int id){
  if(!Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,id))return null;
  try{NPCDefinitions d=NPCDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127),null);
   if(d.transformTo!=null||d.name==null||!d.hasOption("Attack"))return null;
   return d.name.toLowerCase(Locale.ROOT)+"|"+d.size+"|"+d.combatLevel+"|"+(d.clientScriptData==null?"":new TreeMap<>(d.clientScriptData));
  }catch(RuntimeException bad){return null;}
 }
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get("cache"));NPCStatsDataParser.init();NPCCombatDefinitionsDataParser.init();
  Map<String,List<Integer>> sources=new HashMap<>();
  for(int id:NPCStatsDataParser.getDefinitions().keySet()){String key=key(id);if(key!=null)sources.computeIfAbsent(key,k->new ArrayList<>()).add(id);}
  List<Object> rows=new ArrayList<>();
  for(int id:NPCCombatDefinitionsDataParser.getDefinitions().keySet())if(!NPCStatsDataParser.getDefinitions().containsKey(id)){
   String key=key(id);List<Integer> matches=key==null?null:sources.get(key);if(matches==null)continue;
   Map<String,Object> r=new LinkedHashMap<>();r.put("target",id);r.put("metadataKey",key);r.put("sources",matches);rows.add(r);
  }
  Files.write(Paths.get(args[0]),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows).getBytes("UTF-8"));System.out.println("Exact metadata candidate groups: "+rows.size());
 }
}
