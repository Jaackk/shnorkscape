package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import java.nio.file.Paths;
import java.util.*;
/** Read-only admission diagnosis; no asset substitutions or policy changes. */
public final class Native950EquipmentAudit {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get("cache"));
  for(String arg:args){int id=Integer.parseInt(arg);ItemDefinitions d=Native950CacheItems.definition(id);
   System.out.println(id+" "+d.name+" slot="+d.equipSlot+" hide="+d.getEquipType()+","+d.getEquipType2()
    +" noted="+d.noted+" templates="+d.certTemplateId+","+d.shardTemplateId+" stack="+d.stackable
    +" loaded="+d.loaded+" failure="+d.decodeFailure+" options="+Arrays.toString(d.inventoryOptions)
    +" requirements="+Native950EquipmentTypes.requirements(d)+" BAS="+Native950EquipmentAnimations.resolveBas(d,false)+","+Native950EquipmentAnimations.resolveBas(d,true)
    +" admitted="+(Native950EquipmentTypes.resolve(id)!=null));
   for(int model:new int[]{d.maleEquip1,d.maleEquip2,d.maleEquipModelId3,d.femaleEquip1,d.femaleEquip2,d.femaleEquipModelId3})
    System.out.println(" model="+model+" present="+(model<0||Cache.STORE.getIndexes()[47].getFile(model,0)!=null));
  }
 }
}
