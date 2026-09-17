package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cache.loaders.*;import com.rs.game.item.Item;import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.util.*;
public final class Native950ImplingAssetDump {
 static final Map<String,String> pins=new TreeMap<>();
 static void pin(String kind,int id,int index,int shift)throws Exception {byte[] raw=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));if(raw==null)throw new IllegalStateException(kind+id);StringBuilder h=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))h.append(String.format("%02x",b&255));pins.put(kind+"."+id,h.toString());}
 public static void main(String[] args)throws Exception {
  Cache.initFlatReadOnly(Paths.get(args[0]));Set<Integer> items=new TreeSet<>(Arrays.asList(11260,10010,11259,12158,12159,12160,12163));List<String> rows=new ArrayList<>();
  for(FlyingEntities f:FlyingEntities.values())if(f.isImpling()){
   NPCDefinitions n=NPCDefinitions.getNPCDefinitions(f.getNpcId());if(n.decodeFailure!=null||!n.hasOption("Catch")||!Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,f.getNpcId()))throw new IllegalStateException("Unverified impling "+f+" "+n.name);
   pin("npc",f.getNpcId(),18,7);pins.put("npcName."+f.getNpcId(),n.name);items.add(f.getReward());rows.add(f.name()+"\t"+f.getNpcId()+"\t"+f.getReward()+"\t"+f.getLevel()+"\t"+f.getRsExperience());
   for(Item[] bucket:new Item[][]{f.getRarleyCommon(),f.getCommon(),f.getRare(),f.getExtremelyRare()})if(bucket!=null)for(Item item:bucket){items.add(item.getId());if(Native950NpcDrops.metadata(item.getId())==null)System.out.println("UNVERIFIED_LOOT "+item.getId()+" "+ItemDefinitions.getItemDefinitions(item.getId()).name);}
  }
  for(int id:items){pin("item",id,19,8);ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);if(Native950CacheItems.entry(id)==null)throw new IllegalStateException("Unavailable current item "+id);rows.add("ITEM\t"+id+"\t"+d.name+"\t"+Arrays.toString(d.inventoryOptions));}
  pin("sequence",6606,20,7);List<String> out=new ArrayList<>();out.add("# Original910 FlyingEntities with paired950 cache identity pins.");out.add("format=1");out.add("revision=950");for(Map.Entry<String,String> e:pins.entrySet())out.add(e.getKey()+"="+e.getValue());Files.write(Paths.get("Ataraxia950/resources/native950/impling-assets-950.properties"),out,StandardCharsets.UTF_8);Files.write(Paths.get("protocol-analysis/impling-assets-950.tsv"),rows,StandardCharsets.UTF_8);System.out.println("Wrote "+pins.size()+" bindings for "+items.size()+" items");
 }
}
