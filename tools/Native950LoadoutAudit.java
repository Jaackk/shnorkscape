package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import java.nio.file.Paths;
import java.util.*;
/** Read-only item metadata report for selecting coherent native loadouts. */
public final class Native950LoadoutAudit {
 public static void main(String[] args)throws Exception {
  Cache.initFlatReadOnly(Paths.get("cache"));
  if(args.length==1&&args[0].equals("top")){
   for(int style=0;style<4;style++){
    List<String> rows=new ArrayList<>();
    for(Native950DeveloperSearch.Row row:Native950DeveloperSearch.current()){
     if(row.item.slot!=3||row.lower.contains("augmented")||row.lower.contains("(")||row.lower.contains("lucky")||row.lower.contains("golden"))continue;
     Native950CombatStyles.Profile p;try{p=Native950CombatStyles.profile(row.item.id);}catch(RuntimeException invalid){continue;}
     if(p!=null&&p.style==style&&p.tier>=95)rows.add(String.format("%03d %d %s",p.tier,row.item.id,row.item.name));
    }
    Collections.sort(rows,Collections.reverseOrder());System.out.println("STYLE "+style+" "+rows.subList(0,Math.min(20,rows.size())));
   }return;
  }
  for(String value:args){int id=Integer.parseInt(value);ItemDefinitions d=Native950CacheItems.definition(id);
   Native950EquipmentTypes.Type t=Native950EquipmentTypes.resolve(id);
   System.out.println(id+" "+d.name+" slot="+d.equipSlot+" tier="+d.getCSOpcode(23,0)+" stack="+d.stackable
    +" style="+d.getCSOpcode(2821,0)+","+d.getCSOpcode(2822,0)+","+d.getCSOpcode(2823,0)+","+d.getCSOpcode(8898,0)
    +" weapon="+d.getCSOpcode(2825,0)+","+d.getCSOpcode(2826,0)+","+d.getCSOpcode(2827,0)
    +" twoHanded="+(t!=null&&t.twoHanded)+" requirements="+(t==null?"INVALID":t.requirements));
  }
 }
}
