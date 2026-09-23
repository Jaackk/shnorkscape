package com.rs.game.player.client;

import com.rs.cache.Cache;
import java.io.InputStream;
import java.util.*;

/** Fail closed if the exact native bank/tab/search contract changes. */
final class Native950EquipmentLibraryAssets {
    private static Object verified;
    static synchronized void verify(){
        Native950BankUi.verify();
        if(verified==Cache.STORE)return;
        Native950LibraryBridge.verify();
        try(InputStream in=Native950EquipmentLibraryAssets.class.getResourceAsStream("/native950/equipment-library-ui-950.properties")){
            if(in==null)throw new IllegalStateException("Missing equipment-library UI bindings");
            Properties pins=new Properties();pins.load(in);
            try(InputStream presets=Native950EquipmentLibraryAssets.class.getResourceAsStream("/native950/developer-presets-ui-950.properties")){
                if(presets==null)throw new IllegalStateException("Missing native developer preset bindings");pins.load(presets);
            }
            if(pins.isEmpty())throw new IllegalStateException("Empty equipment-library UI bindings");
            for(String key:pins.stringPropertyNames()){
                String[] ids=key.split("/");byte[] raw=Cache.STORE.getIndexes()[Integer.parseInt(ids[0])].getFile(Integer.parseInt(ids[1]),Integer.parseInt(ids[2]));
                if(!Native950LibraryBridge.matches(key,pins.getProperty(key),Native950EquipmentCatalogue.hash(raw)))throw new IllegalStateException("Changed library UI asset "+key);
            }
            verified=Cache.STORE;
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot verify equipment-library UI",e);}
    }
    static List<String> generate(){
        List<String> pins=new ArrayList<>();pins.add("# Exact950 bank library contract; generated from read-only cache.");
        for(int file:Cache.STORE.getIndexes()[3].getTable().getArchives()[517].getValidFileIds())pin(pins,3,517,file);
        for(int script:new int[]{8420,5792,5797,5799,5787,5788,5869,5883,8905,8906,9511,9587,9642,9670,14370,14372,
                9324,9325,9329,9330,13890,13898,13903,13909,14337,10906,13798,14362,6794})pin(pins,12,script,0);
        for(int id=45141;id<=45220;id++)pin(pins,2,69,id);
        for(int id:new int[]{8657,15585})pin(pins,17,id>>>8,id&255);
        for(int id:new int[]{13197,13193,13196,13205,30936,13195,13212,8123})pin(pins,8,id,0);
        return pins;
    }
    private static void pin(List<String> pins,int index,int group,int file){pins.add(index+"/"+group+"/"+file+"="+Native950EquipmentCatalogue.hash(Cache.STORE.getIndexes()[index].getFile(group,file)));}
}
