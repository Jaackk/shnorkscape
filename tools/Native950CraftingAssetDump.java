package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cache.loaders.*;import com.rs.cache.loaders.rs3.*;import com.rs.game.item.Item;import com.rs.game.player.actions.crafting.LeatherCrafting.LeatherData;import com.rs.game.player.actions.crafting.Spinning.SpinningItem;import com.rs.game.player.actions.summoning.defs.*;import com.rs.game.player.actions.summoning.defs.SummoningScrolls.SummoningScroll;import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.util.*;
/** Deliberate build-time binding generator; runtime never trusts newly generated ids automatically. */
public final class Native950CraftingAssetDump{
 static final Map<String,String> pins=new TreeMap<>();static final Set<Integer> items=new TreeSet<>(),structs=new TreeSet<>();
 static void add(int id){if(id>0&&id<65535)items.add(id);}
 static void pin(String kind,int id,int index,int shift)throws Exception{byte[] data=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));if(data==null)throw new IllegalStateException(kind+id);StringBuilder h=new StringBuilder();for(byte v:MessageDigest.getInstance("SHA-256").digest(data))h.append(String.format("%02x",v&255));pins.put(kind+"."+id,h.toString());}
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get(args[0]));
  for(LeatherData r:LeatherData.values()){add(r.getFinalProduct());add(r.getLeatherId());}
  for(SpinningItem r:SpinningItem.values()){add(r.getAfterId());for(int id:r.getBeforeId())add(id);}
  for(int[] ids:new int[][]{Native950Crafting.GOLD,Native950Crafting.GLASS,Native950Crafting.POTTERY,Native950Crafting.FIRED,{1714,1720,5525,1775,1733,1734,1783,1785,1692,1694,1696,1698,1700,1702,6581,1716}})for(int id:ids)add(id);
  for(SummoningPouches r:SummoningPouches.values()){int id=Native950Summoning.canonicalPouchId(r);if(id>=17000&&id<19000||id>65534)continue;add(id);for(Item i:r.getItems())add(i.getId());}
  for(SummoningScroll r:SummoningScroll.values()){if(r.getPouch().getId()>=17000&&r.getPouch().getId()<19000||r.getPouch().getId()<1)continue;add(r.getItemId());add(r.getPouch().getId());}
  for(int id=25590;id<=25600;id++)add(id);
  // One level of direct recipe dependencies suffices: ingredients are never recursively produced here.
  for(int id:new ArrayList<>(items)){
   ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);add(d.getCSOpcode(2989,0));
   for(int i=0;i<3;i++)add(d.getCSOpcode(2650+i,0));
   for(int i=0;i<10;i++)add(d.getCSOpcode(2655+i,0));
   for(int i=0;i<10;i++){int struct=d.getCSOpcode(2675+i,0);if(struct>0){structs.add(struct);RS3GeneralRequirementMap m=RS3GeneralRequirementMap.getMap(struct);for(int n=0;n<10;n++)add(m.getIntValue(2655+n));}}
  }
  List<String> names=new ArrayList<>();for(int id:items){ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);if(!d.loaded||d.name==null||d.name.equals("null"))continue;pin("item",id,19,8);names.add(id+"\t"+d.name+"\t"+Arrays.toString(d.inventoryOptions));}
  for(int id:structs)pin("struct",id,22,5);
  for(int id:new int[]{25594,896,32626,884,725})pin("sequence",id,20,7);
  List<String> objects=new ArrayList<>();for(String line:Files.readAllLines(Paths.get("dumps/objects.txt"),StandardCharsets.UTF_8)){
   String[] cols=line.split("\t");if(cols.length<2||!cols[0].matches("[0-9]+"))continue;
   String name=cols[1].toLowerCase(Locale.ROOT);if(!Arrays.asList("obelisk","spinning wheel","pottery wheel","pottery oven","furnace").contains(name))continue;
   int id=Integer.parseInt(cols[0]);ObjectDefinitions d=ObjectDefinitions.getObjectDefinitions(id);
   if(!d.loaded||d.transforms!=null||d.options==null)continue;
   boolean op=false;for(String s:d.options)op|="Smelt".equalsIgnoreCase(s)||"Use".equalsIgnoreCase(s)||"Form".equalsIgnoreCase(s)||"Craft".equalsIgnoreCase(s)||"Spin".equalsIgnoreCase(s)||"Fire".equalsIgnoreCase(s)||"Infuse-pouch".equalsIgnoreCase(s);
   if(!op)continue;pin("object",id,16,8);objects.add(id+"\t"+d.name+"\t"+Arrays.toString(d.options));
  }
  List<String> lines=new ArrayList<>();lines.add("# Paired950 definitions, generated from original product families and current recipe dependencies.");for(Map.Entry<String,String> p:pins.entrySet())lines.add(p.getKey()+"="+p.getValue());
  Files.write(Paths.get("Ataraxia950/resources/native950/crafting-summoning-assets-950.properties"),lines,StandardCharsets.UTF_8);
  Files.write(Paths.get("protocol-analysis/crafting-summoning-items-950.tsv"),names,StandardCharsets.UTF_8);Files.write(Paths.get("protocol-analysis/crafting-summoning-objects-950.tsv"),objects,StandardCharsets.UTF_8);
  System.out.println("Wrote "+pins.size()+" pins, "+names.size()+" named items, "+objects.size()+" stations");
 }
}
