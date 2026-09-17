package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.item.Item;
import com.rs.game.player.Skills;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/** Paired-cache boundary for ordinary Crafting and Summoning production records. */
final class Native950CraftingAssets {
    private static Properties pins;
    private static Object store;
    private static final Map<String,Boolean> verified=new HashMap<>();
    private Native950CraftingAssets(){}
    static synchronized boolean pin(String kind,int id,int index,int shift){
        if(Cache.STORE==null||!Cache.isFlatReadOnly()||id<0)return false;
        if(pins==null){pins=new Properties();try(InputStream in=Native950CraftingAssets.class.getResourceAsStream("/native950/crafting-summoning-assets-950.properties")){
            if(in==null)throw new IllegalStateException("Missing950 Crafting/Summoning bindings");pins.load(in);
        }catch(java.io.IOException e){throw new IllegalStateException(e);}}
        if(store!=Cache.STORE){store=Cache.STORE;verified.clear();}
        String key=kind+"."+id;
        if(!verified.containsKey(key)){
            boolean valid=false;String expected=pins.getProperty(key);
            if(expected!=null)try{byte[] b=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
                if(b!=null){StringBuilder hex=new StringBuilder();for(byte v:MessageDigest.getInstance("SHA-256").digest(b))hex.append(String.format("%02x",v&255));valid=expected.equals(hex.toString());}
            }catch(Exception absent){valid=false;}
            verified.put(key,valid);
        }return verified.get(key);
    }
    static synchronized void verifyAll(){
        if(!pin("sequence",725,20,7))throw new IllegalStateException("Missing Summoning animation binding");
        for(String key:pins.stringPropertyNames()){
            String[] parts=key.split("\\.");int id=Integer.parseInt(parts[1]);String kind=parts[0];
            int index=kind.equals("item")?19:kind.equals("sequence")?20:kind.equals("struct")?22:16;
            int shift=kind.equals("sequence")?7:kind.equals("struct")?5:8;
            if(!pin(kind,id,index,shift))throw new IllegalStateException("Changed950 Crafting/Summoning asset: "+key);
        }
    }
    static boolean item(int id){
        if(id<1||id>65534||!pin("item",id,19,8))return false;
        ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
        return d.loaded&&d.name!=null&&!"null".equalsIgnoreCase(d.name)&&d.certTemplateId<0&&d.lendTemplateId<0&&d.bindTemplateId<0&&d.shardTemplateId<0;
    }
    /** Old content selects the product; current cache supplies its actual recipe and prerequisites. */
    static Native950Production.Recipe product(String group,int id,int skill,int animation,int delay,Item[] preferred,int... requiredTools){
        if(!item(id)||(animation!=-1&&!pin("sequence",animation,20,7)))return null;
        ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
        int type=skill==Skills.CRAFTING?11:skill==Skills.SUMMONING?24:skill==Skills.HERBLORE?9:skill==Skills.FLETCHING?19:skill==Skills.COOKING?16:-1;
        // 950 production-only scroll records name their real backpack output via param2989.
        int output=d.getCSOpcode(2989,id);if(!item(output))return null;
        ItemDefinitions reward=ItemDefinitions.getItemDefinitions(output);
        if(d.getCSOpcode(2640,0)!=type||d.getCSOpcode(2641,0)!=0||reward.getCSOpcode(2696,0)!=type||reward.getCSOpcode(2698,0)!=0)return null;
        int level=d.getCSOpcode(2645,0),quantity=d.getCSOpcode(2653,1),xp=reward.getCSOpcode(2697,-1);
        if(level<1||level>120||quantity<1||quantity>1000||xp<0)return null;
        List<Item> ingredients=new ArrayList<>();
        for(int i=0;i<10;i++){
            int input=d.getCSOpcode(2655+i,0),amount=d.getCSOpcode(2665+i,0);
            if(input==0)continue;
            if(!item(input)||amount<1||amount>100000)return null;
            ingredients.add(new Item(input,amount));
        }
        for(int i=0;i<10;i++){
            int struct=d.getCSOpcode(2675+i,0);if(struct==0)continue;
            if(!pin("struct",struct,22,5))return null;
            RS3GeneralRequirementMap m=RS3GeneralRequirementMap.getMap(struct);Item chosen=null;
            // Metal-bank records are alternatives, not extra physical backpack materials.
            for(Item old:preferred)for(int n=0;n<10;n++){
                int input=m.getIntValue(2655+n),amount=m.getIntValue(2665+n);
                if(input==old.getId()&&amount>0&&amount<=100000&&item(input)){chosen=new Item(input,amount);break;}
                if(chosen!=null)break;
            }
            if(chosen==null)return null;
            ingredients.add(chosen);
        }
        if(ingredients.isEmpty())return null;
        Set<Integer> tools=new LinkedHashSet<>();for(int tool:requiredTools)tools.add(tool);
        for(int i=0;i<3;i++){int tool=d.getCSOpcode(2650+i,0);if(tool>0)tools.add(tool);}
        for(int tool:tools)if(!item(tool))return null;
        int[] toolIds=new int[tools.size()];int i=0;for(int tool:tools)toolIds[i++]=tool;
        return new Native950Production.Recipe(group+": "+reward.name,skill,level,xp/10.0,animation,delay,ingredients.toArray(new Item[0]),new Item[]{new Item(output,quantity)},toolIds);
    }
    static Native950ItemCatalog.Entry entry(int id,String[] options){
        if(!item(id))return null;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
        return new Native950ItemCatalog.Entry(id,d.name,d.isStackable(),options);
    }
    static boolean includes(Native950Production.Recipe r,int id){
        for(Item item:r.consumed())if(item.getId()==id)return true;
        for(Item item:r.produced())if(item.getId()==id)return true;
        for(int tool:r.tools())if(tool==id)return true;
        return false;
    }
    static boolean hasPrimary(com.rs.game.player.Player p,Native950Production.Recipe r){
        for(Item item:r.consumed())if(p.getInventory().containsItem(item.getId(),1))return true;
        return false;
    }
}
