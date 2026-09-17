package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cache.loaders.*;import com.rs.cache.loaders.rs3.*;import java.nio.file.*;import java.util.*;import java.security.*;
public final class Native950ProductionThirdPassAssetDump {
 static final Set<Integer> items=new TreeSet<>(),structs=new TreeSet<>();static final StringBuilder pins=new StringBuilder();
 static void pin(String kind,int id,int index,int shift)throws Exception{byte[] bytes=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));if(bytes==null)throw new AssertionError("Missing "+kind+id);StringBuilder hex=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))hex.append(String.format("%02x",b&255));pins.append(kind+"."+id+"="+hex+"\n");}
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get(args[0]));int[] products={1393,1395,1397,1399,48960,48961,10142,10143,10144,10145,45,9187,46,9188,9189,9190,9191,9192,9193,9194,31867,2307,1953,2283,2315,2321,2317,2319,2285,2287,1889,2309,2289,1891};
  for(int id:products){items.add(id);ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);StringBuilder evidence=new StringBuilder();for(int p=2640;p<2700;p++){int value=d.getCSOpcode(p,0);if(value!=0)evidence.append(p+"="+value+" ");}System.out.println(id+" "+d.name+" "+Arrays.toString(d.inventoryOptions)+" "+evidence);
   for(int i=0;i<10;i++){int input=d.getCSOpcode(2655+i,0);if(input>0)items.add(input);int struct=d.getCSOpcode(2675+i,0);if(struct>0){structs.add(struct);RS3GeneralRequirementMap map=RS3GeneralRequirementMap.getMap(struct);for(int j=0;j<10;j++){int alternate=map.getIntValue(2655+j);if(alternate>0&&alternate<65535)items.add(alternate);}}}
   for(int i=0;i<3;i++){int tool=d.getCSOpcode(2650+i,0);if(tool>0)items.add(tool);}
  }
  for(int id:new int[]{413,233,1755,1931,1935,1925,1923,1887,2311,2305,1903,4819,4820,4821,4822,4823,4824})items.add(id);
  for(int id:items){pin("item",id,19,8);System.out.println("item "+id+" "+ItemDefinitions.getItemDefinitions(id).name);}
  for(int id:structs)pin("struct",id,22,5);for(int id:new int[]{363,886,897})pin("sequence",id,20,7);
  Files.write(Paths.get(args[1]),pins.toString().getBytes("UTF-8"));
  for(int id:new int[]{2349,2351,2353,2355,2357,2359,2361,2363,44838,44840,44842,44844}){ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);System.out.println("BAR "+id+" "+d.name+" type="+d.getCSOpcode(2640,0)+" level="+d.getCSOpcode(2645,0)+" xp="+d.getCSOpcode(2697,0));}
 }
}
