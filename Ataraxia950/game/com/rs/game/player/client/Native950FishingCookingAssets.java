package com.rs.game.player.client;
import com.rs.cache.Cache;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
/** Exact-cache assets used by the original fishing/cooking tables. */
final class Native950FishingCookingAssets {
    private static final Properties PINS=load();
    private static Object store;
    private static final Map<String,Boolean> VERIFIED=new HashMap<>();
    static synchronized boolean verified(String kind,int id) {
        if(Cache.STORE==null || !Cache.isFlatReadOnly() || id<1)return false;
        if(store!=Cache.STORE){store=Cache.STORE;VERIFIED.clear();}
        String key=kind+"."+id;Boolean before=VERIFIED.get(key);if(before!=null)return before;
        boolean valid=false;String expected=PINS.getProperty(key);
        if(expected!=null)try {
            int index=kind.equals("item")?19:kind.equals("npc")?18:20,shift=kind.equals("item")?8:7;
            byte[] raw=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
            if(raw!=null){StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));valid=expected.equals(hash.toString());}
        }catch(Exception unavailable){valid=false;}
        VERIFIED.put(key,valid);return valid;
    }
    static boolean item(int id){return verified("item",id)&&Native950CacheItems.entry(id)!=null;}
    private static Properties load(){Properties p=new Properties();try(InputStream in=Native950FishingCookingAssets.class.getResourceAsStream("/native950/fishing-cooking-assets-950.properties")){
        if(in==null)throw new IllegalStateException("Missing950 fishing/cooking asset pins");p.load(in);return p;
    }catch(java.io.IOException e){throw new IllegalStateException(e);}}
}
