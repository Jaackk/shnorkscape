package com.rs.game.player.client;
import com.google.gson.Gson;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.item.Item;
import java.io.*;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.util.*;
/** Raw paired-cache pins and the reviewed current anvil recipe roster. */
final class Native950SmithingAssets {
    static final class Ingredient {int id,amount;}
    static final class Row {int id,bar,tier,level,progress,upgrade,quantity;String name;double xp;Ingredient[] inputs;}
    static final class Roster {Row[] rows;}
    private static final Properties PINS=new Properties();private static final Row[] ROWS;
    private static Object store;private static final Map<String,Boolean> checked=new HashMap<>();private static boolean allVerified;
    static {
        try(InputStream in=Native950SmithingAssets.class.getResourceAsStream("/native950/smithing-assets-950.properties");
            InputStream rows=Native950SmithingAssets.class.getResourceAsStream("/native950/smithing-recipes-950.json")){
            if(in==null||rows==null)throw new IllegalStateException("Missing950 Smithing assets");
            PINS.load(in);ROWS=new Gson().fromJson(new InputStreamReader(rows,StandardCharsets.UTF_8),Roster.class).rows;
        }catch(IOException e){throw new ExceptionInInitializerError(e);}
    }
    static synchronized boolean pin(String key){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return false;
        if(store!=Cache.STORE){store=Cache.STORE;checked.clear();allVerified=false;}
        if(!checked.containsKey(key)){
            String expected=PINS.getProperty(key),actual="missing";String[] parts=key.split("\\.");int id=Integer.parseInt(parts[1]);
            int index=parts[0].equals("item")?19:parts[0].equals("struct")?22:parts[0].equals("object")?16:parts[0].equals("sequence")?20:parts[0].equals("hitbar")?2:8;
            int shift=index==22?5:index==20?7:8;int group=index==2?72:index==8?id:id>>>shift,file=index==2?id:index==8?0:id&((1<<shift)-1);
            try{byte[] raw=Cache.STORE.getIndexes()[index].getFile(group,file);if(raw!=null){StringBuilder h=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))h.append(String.format("%02x",b&255));actual=h.toString();}}
            catch(Exception invalid){actual="unreadable";}
            checked.put(key,expected!=null&&expected.equals(actual));
        }return checked.get(key);
    }
    static synchronized void verifyAll(){
        if(!pin("sequence.22143"))throw new IllegalStateException("Changed950 Smithing animation");
        if(allVerified)return;
        for(String key:PINS.stringPropertyNames())if(!pin(key))throw new IllegalStateException("Changed950 Smithing asset "+key);
        if(ROWS==null||ROWS.length!=702)throw new IllegalStateException("Wrong950 Smithing recipe roster");
        for(Row row:ROWS)if(!valid(row))throw new IllegalStateException("Invalid950 Smithing recipe "+row.id);
        allVerified=true;
    }
    static boolean item(int id){
        if(id<1||id>65534||!pin("item."+id))return false;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
        return d.loaded&&d.name!=null&&!"null".equalsIgnoreCase(d.name)&&d.certTemplateId<0&&d.lendTemplateId<0&&d.bindTemplateId<0&&d.shardTemplateId<0;
    }
    static Row[] rows(){verifyAll();return ROWS.clone();}
    static boolean valid(Row r){
        if(!item(r.id)||!item(r.bar)||r.quantity<1||r.quantity>1000||r.progress<1||r.progress>100000)return false;
        ItemDefinitions d=ItemDefinitions.getItemDefinitions(r.id);
        if(d.getCSOpcode(2640,0)!=14||d.getCSOpcode(2641,0)!=62||d.getCSOpcode(2646,0)!=187||d.getCSOpcode(2642,0)>0||d.getCSOpcode(2698,0)>0
            ||d.getCSOpcode(2650,0)!=2347||d.getCSOpcode(2645,0)!=r.level||d.getCSOpcode(7801,0)!=r.progress
            ||d.getCSOpcode(2653,1)!=r.quantity||d.getCSOpcode(2697,-1)/10.0!=r.xp||!d.name.equals(r.name))return false;
        Map<Integer,Integer> actual=new TreeMap<>(),expected=new TreeMap<>();
        for(int i=0;i<10;i++){int id=d.getCSOpcode(2655+i,0),n=d.getCSOpcode(2665+i,0);if(id>0){if(n<1)return false;actual.merge(id,n,Integer::sum);}}
        for(int i=0;i<10;i++){int id=d.getCSOpcode(2675+i,0);if(id<=0)continue;if(!pin("struct."+id))return false;
            RS3GeneralRequirementMap m=RS3GeneralRequirementMap.getMap(id);int n=m.getIntValue(2666);
            if(m.getIntValue(2656)!=r.bar||m.getIntValue(7763)!=r.bar||n<1)return false;actual.merge(r.bar,n,Integer::sum);}
        int total=0;for(Ingredient i:r.inputs){if(!item(i.id)||i.amount<1)return false;expected.merge(i.id,i.amount,Integer::sum);total+=i.amount;}
        return total<=27&&actual.equals(expected);
    }
}
