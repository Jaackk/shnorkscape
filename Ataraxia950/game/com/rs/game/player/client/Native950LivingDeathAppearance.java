package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import java.util.Arrays;
/** Paired950 Living Death actor, not a generic skeleton or hostile conjure. */
public final class Native950LivingDeathAppearance {
    private Native950LivingDeathAppearance(){}
    public static boolean verified(){
        if(!Cache.isFlatReadOnly()||Cache.STORE==null)return false;
        byte[] raw=Cache.STORE.getIndexes()[18].getFile(236,60);if(raw==null)return false;
        NPCDefinitions d=NPCDefinitions.decodeStrict947(30268,raw,null);
        return "Living Death".equals(d.name)&&Arrays.equals(d.models,new int[]{127935,128810})
                &&d.size==1&&d.clientScriptData!=null&&Integer.valueOf(1).equals(d.clientScriptData.get(2805));
    }
}
