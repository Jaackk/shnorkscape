package com.rs.game.player.client;

import com.rs.game.WorldObject;

/** Cache-name plus menu semantics, independent of revision-specific object IDs. */
final class Native950PhysicalBanks {
    private Native950PhysicalBanks() { }
    static boolean bankName(String name){
        return "Bank booth".equalsIgnoreCase(name)||"Bank chest".equalsIgnoreCase(name)
                ||"Bank counter".equalsIgnoreCase(name);
    }
    static boolean accepts(String name,String option){
        return bankName(name)&&("Bank".equalsIgnoreCase(option)
                ||("Bank chest".equalsIgnoreCase(name)&&"Use".equalsIgnoreCase(option)));
    }
    static boolean accepts(WorldObject object,int option){
        if(object==null||option<1||option>5)return false;
        com.rs.cache.loaders.ObjectDefinitions d=object.getDefinitions();
        return d.loaded&&d.transforms==null&&d.options!=null&&option<=d.options.length
                &&accepts(d.name,d.options[option-1]);
    }
    static boolean isBank(WorldObject object){
        for(int option=1;option<=5;option++)if(accepts(object,option))return true;
        return false;
    }
}
