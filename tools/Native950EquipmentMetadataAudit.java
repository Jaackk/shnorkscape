import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.client.Native950CacheItems;
import com.rs.game.player.client.Native950EquipmentTypes;
import java.nio.file.Paths;
import java.util.*;

/** Read-only exhaustive current-cache equipment metadata audit; no character/account/server. */
public final class Native950EquipmentMetadataAudit {
 public static void main(String[] args)throws Exception {
  Cache.initFlatReadOnly(Paths.get(args[0]));Index index=Cache.STORE.getIndexes()[19];
  int records=0,wearable=0,admitted=0,unresolved=0;Map<Integer,Integer>slots=new TreeMap<>();Map<String,Integer>transforms=new TreeMap<>();List<String>rejected=new ArrayList<>();
  for(int group:index.getTable().getValidArchiveIds())for(int file:index.getTable().getArchives()[group].getValidFileIds()){
   int id=(group<<8)|file;records++;ItemDefinitions d=Native950CacheItems.definition(id);if(d==null){unresolved++;continue;}
   boolean wear=false;for(String option:d.inventoryOptions)if(Native950EquipmentTypes.isWearLabel(option))wear=true;
   if(!wear||d.equipSlot<0||d.certTemplateId>=0||d.shardTemplateId>=0)continue;wearable++;
   Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(id);
   if(type==null){if(rejected.size()<100)rejected.add(id+" "+d.name+" slot="+d.equipSlot+" models="+d.maleEquip1+","+d.maleEquip2+","+d.maleEquipModelId3+"/"+d.femaleEquip1+","+d.femaleEquip2+","+d.femaleEquipModelId3+" params="+d.clientScriptData);continue;}
   admitted++;slots.merge(type.slot,1,Integer::sum);transforms.merge(type.transform.name(),1,Integer::sum);
  }
  System.out.println("RECORDS="+records+" UNRESOLVED_TEMPLATE_DEFS="+unresolved+" RESOLVED_WEARABLE="+wearable+" ADMITTED="+admitted+" REJECTED="+(wearable-admitted));
  System.out.println("SLOTS="+slots+" TRANSFORMS="+transforms);
  for(String row:rejected)System.out.println("REJECTED "+row);
  for(int id:new int[]{58486,1044,1053,1061,884,1635,13534,20981}){
   Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(id);if(type==null)throw new AssertionError("Actual equipment witness rejected "+id);
   System.out.println("WITNESS "+id+" "+type.name+" slot="+type.slot+" conflicts="+Arrays.toString(type.conflictSlots())+" stack="+type.stackable+" normalBAS="+type.bas+" combatBAS="+type.combatBas+" requirements="+type.requirements+" transform="+type.transform);
  }
  Native950EquipmentTypes.Type staff=Native950EquipmentTypes.resolve(58486);
  if(staff.slot!=3||!staff.twoHanded||!Integer.valueOf(99).equals(staff.requirements.get(6))||staff.bas!=2697||staff.combatBas!=2689)
   throw new AssertionError("Masterwork staff's current-cache contract differs from its raw definition/profile");
  if(Native950EquipmentTypes.resolve(1045)!=null||Native950EquipmentTypes.resolve(30054)!=null)
   throw new AssertionError("A note or shard was incorrectly admitted as worn equipment");
 }
}