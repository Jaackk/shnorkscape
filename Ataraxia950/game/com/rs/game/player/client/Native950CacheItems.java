package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.ItemDefinitions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

/** Current950 identities and template metadata, independent of any910 item-ID allow-list. */
public final class Native950CacheItems {
    private Native950CacheItems() { }
    private static Store definitionStore;
    private static final Map<Integer,ItemDefinitions> definitions=new HashMap<Integer,ItemDefinitions>();

    public static Native950ItemCatalog.Entry entry(int id) {
        return entry(definition(id));
    }

    /** Immutable-by-convention strict current-cache metadata; never runs legacy item customizers. */
    public static synchronized ItemDefinitions definition(int id) {
        if(Cache.STORE==null||!Cache.isFlatReadOnly())
            throw new IllegalStateException("Native950 items require the selected read-only cache");
        if(definitionStore!=Cache.STORE){definitions.clear();definitionStore=Cache.STORE;}
        if(!definitions.containsKey(id))definitions.put(id,resolvedDefinition(id,
                value -> Cache.STORE.getIndexes()[19].getFile(value>>>8,value&255)));
        return definitions.get(id);
    }

    static Native950ItemCatalog.Entry resolveEntry(int id,IntFunction<byte[]> files) {
        return entry(resolvedDefinition(id,files));
    }

    private static Native950ItemCatalog.Entry entry(ItemDefinitions definition) {
        if(definition==null||definition.getName()==null||definition.getName().trim().isEmpty()
                ||"null".equalsIgnoreCase(definition.getName()))return null;
        return new Native950ItemCatalog.Entry(definition.getId(),definition.getName(),
                definition.stackable==1,definition.inventoryOptions);
    }

    /** Shared by equipment metadata: strict bounded template traversal with current-cache links. */
    static ItemDefinitions resolvedDefinition(int id,IntFunction<byte[]> files) {
        try{return resolve(id,files,new HashSet<Integer>(),0);}
        catch(RuntimeException invalid){return null;}
    }

    private static ItemDefinitions resolve(int id,IntFunction<byte[]> files,Set<Integer> path,int depth) {
        if(id<0||id>Native950ItemCatalog.MAX_ITEM_ID||depth>16||!path.add(id))
            throw new IllegalArgumentException("Invalid or cyclic current950 item template");
        try {
            byte[] bytes=files.apply(id);
            if(bytes==null)throw new IllegalArgumentException("Missing current950 item definition");
            ItemDefinitions d=ItemDefinitions.decodeStrict947(id,bytes,null);
            // Match the paired client's template precedence. A template need not have a
            // display name, but every referenced template and base must decode completely.
            if(d.certTemplateId!=-1) {
                resolve(d.certTemplateId,files,path,depth+1);
                ItemDefinitions base=resolve(d.certId,files,path,depth+1);
                d.name=base.name;d.membersOnly=base.membersOnly;d.value=base.value;
                d.stackable=1;d.noted=true;d.clientScriptData=base.clientScriptData;
                // Notes keep their own actions; never inherit Bury/Wear/Eat from the base.
            }
            if(d.lendTemplateId!=-1) {
                resolve(d.lendTemplateId,files,path,depth+1);
                inheritEquipment(d,resolve(d.lendId,files,path,depth+1),"Discard");
                d.lended=true;
            }
            if(d.bindTemplateId!=-1) {
                resolve(d.bindTemplateId,files,path,depth+1);
                inheritEquipment(d,resolve(d.bindId,files,path,depth+1),"Destroy");
            }
            if(d.shardTemplateId!=-1) {
                resolve(d.shardTemplateId,files,path,depth+1);
                ItemDefinitions base=resolve(d.shardId,files,path,depth+1);
                d.name=base.getShardName();d.membersOnly=base.membersOnly;d.stackable=1;
                d.inventoryOptions=new String[]{"Combine",null,null,null,"Drop"};
                d.value=base.shardCombineRequirement>0?base.value/base.shardCombineRequirement:0;
            }
            return d;
        }finally{path.remove(id);}
    }

    private static void inheritEquipment(ItemDefinitions target,ItemDefinitions base,String finalOption) {
        target.name=base.name;target.membersOnly=base.membersOnly;target.value=0;target.teamId=base.teamId;
        target.originalModelColors=base.originalModelColors;
        target.maleEquip1=base.maleEquip1;target.maleEquip2=base.maleEquip2;target.maleEquipModelId3=base.maleEquipModelId3;
        target.femaleEquip1=base.femaleEquip1;target.femaleEquip2=base.femaleEquip2;target.femaleEquipModelId3=base.femaleEquipModelId3;
        target.equipSlot=base.equipSlot;target.equipType=base.equipType;target.equipLookHideSlot2=base.equipLookHideSlot2;
        target.groundOptions=base.groundOptions;target.clientScriptData=base.clientScriptData;
        target.inventoryOptions=new String[5];
        if(base.inventoryOptions!=null)System.arraycopy(base.inventoryOptions,0,target.inventoryOptions,0,Math.min(4,base.inventoryOptions.length));
        target.inventoryOptions[4]=finalOption;
    }
}
