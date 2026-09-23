package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import java.util.*;

/** Reorders only existing objects in one tab. Never merges or recreates charged/augmented property. */
final class Native950BankSort {
    static void sort(Item[][] tabs,int selected){
        if(tabs==null||selected<0||selected>=tabs.length||tabs[selected]==null)return;
        Item[] sorted=tabs[selected].clone();
        Map<Integer,Native950EquipmentCatalogue.Entry> metadata=new HashMap<>();
        for(Item item:sorted)if(item!=null&&!metadata.containsKey(item.getId())){
            ItemDefinitions d=Native950CacheItems.definition(item.getId());
            if(d!=null&&d.name!=null)metadata.put(item.getId(),Native950EquipmentCatalogue.entry(d,
                    Math.max(0,Native950EquipmentCatalogue.category(d))));
        }
        Arrays.sort(sorted,Comparator.nullsLast((a,b)->{
            Native950EquipmentCatalogue.Entry x=metadata.get(a.getId()),y=metadata.get(b.getId());
            if(x==null||y==null)return x==y?Integer.compare(a.getId(),b.getId()):x==null?1:-1;
            int c=Integer.compare(section(x),section(y));if(c!=0)return c;
            c=Integer.compare(y.tier,x.tier);if(c!=0)return c;
            c=x.group.replace("augmented ","").compareTo(y.group.replace("augmented ",""));if(c!=0)return c;
            c=Integer.compare(Native950EquipmentCatalogue.slotOrder(x.slot),Native950EquipmentCatalogue.slotOrder(y.slot));
            if(c!=0)return c;c=x.name.compareToIgnoreCase(y.name);return c!=0?c:Integer.compare(a.getId(),b.getId());
        }));
        tabs[selected]=sorted;
    }
    private static int section(Native950EquipmentCatalogue.Entry e){
        if(e.category==7)return 8;
        if(e.slot>=0&&e.slot!=13)return e.category>=1&&e.category<=5?e.category:5;
        if(e.slot==13||e.name.toLowerCase(Locale.ROOT).endsWith(" rune"))return 6;
        if(e.category==6)return 7;if(e.category==7)return 8;return 9;
    }
}
