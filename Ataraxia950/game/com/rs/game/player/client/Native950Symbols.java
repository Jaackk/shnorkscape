package com.rs.game.player.client;

import java.util.*;

/** Small fail-closed execution layer over provenance-bearing exact-cache symbols.
 * Cache replacement requires a restart; verified bindings live only for this JVM.
 * Project aliases remain explicitly distinguished from recovered Jagex gamevals. */
final class Native950Symbols {
    private static final Map<String,Integer> VERIFIED=new HashMap<>();
    private static Object store;
    private Native950Symbols(){}
    static synchronized int require(String type,String name){
        if(store!=com.rs.cache.Cache.STORE){VERIFIED.clear();store=com.rs.cache.Cache.STORE;}
        String key=type+":"+name;Integer cached=VERIFIED.get(key);if(cached!=null)return cached;
        Native950GamevalLookup.Entry match=null;
        for(Native950GamevalLookup.Entry e:Native950GamevalLookup.search(type+" "+name))
            if(e.type.equals(type)&&e.name.equals(name)){if(match!=null)throw new IllegalStateException("Ambiguous symbol "+key);match=e;}
        if(match==null||!match.verify().equals("950 payload SHA-256 MATCH"))throw new IllegalStateException("Unverified 950 symbol "+key);
        int id=Integer.parseInt(match.packed());VERIFIED.put(key,id);return id;
    }
}
