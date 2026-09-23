package com.rs.game.player.client;
import com.rs.cache.Cache;
import java.nio.file.Paths;
public final class Native950LootProbe {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get(args[0]));
  for(int id:new int[]{532,995,12163,12160,12159,12158,1618,1620,1622,1624}){
   Native950NpcDrops.ItemMetadata m=Native950NpcDrops.metadata(id);
   System.out.println(id+" actual="+Native950CacheItems.definition(id).name+" metadata="+(m==null?"REFUSED":m.name+" noted="+m.noted+" stack="+m.stackable));
  }
  System.out.println("Bork validated rewards="+Native950NpcDrops.borkDrops(Native950NpcDrops::metadata,0).size());
 }
}
