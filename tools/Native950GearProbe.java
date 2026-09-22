package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.item.Item;
import java.nio.file.Paths;
import java.util.Collections;

/** Read-only real-cache check for the developer kits; no player or live world mutations. */
public final class Native950GearProbe {
    public static void main(String[] args) throws Exception {
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950ItemCatalog catalog=new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops();
        for(String kit:new String[]{"melee","mage","range","necro","weapons"}) {
            for(Item item:Native950ContentCommands.kit(kit)) {
                Native950ItemCatalog.Entry entry=catalog.get(item.getId());
                if(entry==null||entry.equipSlot<0)throw new IllegalStateException("Unavailable kit equipment: "+item.getId());
                System.out.println(kit+": "+entry.id+" "+entry.name+" slot="+entry.equipSlot);
            }
        }
        System.out.println("PASS: all kit items resolve as equipment in the selected950 cache.");
    }
}
