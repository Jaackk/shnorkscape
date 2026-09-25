package com.rs.game.player.client;
import com.rs.cache.Cache;
import java.nio.file.Paths;
import java.util.*;

/** Read-only paired-cache test of shared item data and canonical NPC choices. */
public final class Native950DeveloperPhaseBAcceptance {
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get(args[0]));
        List<Native950EquipmentCatalogue.Entry> torva=Native950DeveloperItems.search("tOrVa");
        if(torva.size()<3)throw new AssertionError("Missing Torva family");
        for(Native950EquipmentCatalogue.Entry e:torva){
            if(!Native950DeveloperSearch.verifiedItem(e.id))throw new AssertionError("Unsafe item "+e.id);
            if(Native950DeveloperItems.search(String.valueOf(e.id)).get(0).id!=e.id)throw new AssertionError("Exact item search mismatch");
        }
        System.out.println("Shared Torva search: "+torva.size()+" items; "+Native950DeveloperItems.details(torva.get(0).id));
        for(String name:new String[]{"Banker","Kalphite King","Nex","Vorago"}){
            List<Native950DeveloperCatalogue.Entry> grouped=Native950DeveloperCatalogue.groupedNpcs(name);
            Native950DeveloperCatalogue.Entry selected=grouped.stream().filter(e->e.name.equalsIgnoreCase(name)).findFirst().get();
            System.out.println(name+": representative="+selected.id+", variants="+Native950DeveloperCatalogue.variants(selected).size());
        }
        java.lang.reflect.Method verify=Native950DeveloperConsole.class.getDeclaredMethod("verify");verify.setAccessible(true);verify.invoke(null);
        System.out.println("Phase B cache data and native script pins PASS. Vulkan acceptance pending.");
    }
}
