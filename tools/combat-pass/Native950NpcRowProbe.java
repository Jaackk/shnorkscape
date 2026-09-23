package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.utils.data.parsers.npcs.*;
import java.nio.file.Paths;
/** Read-only admission diagnosis, never repairs authored rows by guessing. */
public final class Native950NpcRowProbe {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get("cache"));NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();
  com.google.gson.Gson gson=new com.google.gson.Gson();
  for(String arg:args){int id=Integer.parseInt(arg);
   NPCDefinitions d=NPCDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127),null);
   System.out.println(id+" "+d.name+" params="+d.clientScriptData+" render="+d.renderEmote);
   System.out.println("combat="+gson.toJson(NPCCombatDefinitionsDataParser.getDefinitions().get(id)));
   System.out.println("stats="+gson.toJson(NPCStatsDataParser.getDefinitions().get(id)));
  }
 }
}
