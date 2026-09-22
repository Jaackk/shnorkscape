package com.rs.game.player.client;

import com.rs.cache.Cache;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Properties;

/** Verify the current cache before admitting sessions to the new hub/bar behavior. */
final class Native950AbilityAssets {
    private static Object verified;
    static synchronized void verify(){
        if(Cache.STORE!=null&&verified==Cache.STORE)return;
        try(InputStream input=Native950AbilityAssets.class.getResourceAsStream("/native950/ability-hub-assets.properties")){
            if(input==null||Cache.STORE==null)throw new IllegalStateException("Missing ability cache assets");
            Properties pins=new Properties();pins.load(input);
            if(pins.isEmpty())throw new IllegalStateException("Empty ability asset pins");
            for(String key:pins.stringPropertyNames()){
                String[] id=key.split("/");byte[] raw=Cache.STORE.getIndexes()[Integer.parseInt(id[0])].getFile(Integer.parseInt(id[1]),Integer.parseInt(id[2]));
                if(raw==null)throw new IllegalStateException("Missing ability asset "+key);
                StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));
                if(!pins.getProperty(key).equals(hash.toString()))throw new IllegalStateException("Changed ability asset "+key);
            }
            Native950AbilityCatalog.verify();
            verifyBookBit(36453,3705,24,27);verifyBookBit(36454,3705,28,31);
            verifyBookBit(44637,3706,8,8);verifyBookBit(27344,3706,5,5);
            verified=Cache.STORE;
        }catch(java.io.IOException|java.security.NoSuchAlgorithmException e){throw new IllegalStateException("Cannot verify ability assets",e);}
    }
    private static void verifyBookBit(int id,int parent,int low,int high){
        com.rs.cache.loaders.VarBitDefinitions v=com.rs.cache.loaders.VarBitDefinitions.getClientVarpBitDefinitions(id);
        if(v.varDomain!=0||v.baseVar!=parent||v.startBit!=low||v.endBit!=high)
            throw new IllegalStateException("Changed950 ability-book varbit "+id);
    }
}
