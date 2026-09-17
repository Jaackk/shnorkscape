package com.rs.game.player.client;
import com.rs.cache.Cache;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
/** Exact paired-cache admission shared only by these two original skill ports. */
final class Native950RunecraftingDivinationAssets {
 private static final Properties PINS=load(); private static Object store;
 private static final Map<String,Boolean> checked=new HashMap<>();
 static synchronized boolean verified(String kind,int id) {
  if(Cache.STORE==null||!Cache.isFlatReadOnly()||id<0)return false;
  if(store!=Cache.STORE){store=Cache.STORE;checked.clear();}
  String key=kind+"."+id;Boolean known=checked.get(key);if(known!=null)return known;
  String expected=PINS.getProperty(key);boolean valid=false;
  if(expected!=null)try{
   int index=kind.equals("object")?16:kind.equals("npc")?18:kind.equals("item")?19:kind.equals("graphic")?21:kind.equals("struct")?22:20;
   int shift=index==22?5:index==18||index==20?7:8;
   byte[] raw=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
   if(raw!=null){StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));valid=expected.equals(hash.toString());}
  }catch(Exception unavailable){valid=false;}
  checked.put(key,valid);return valid;
 }
 static void verifyAll(){for(String key:PINS.stringPropertyNames()){int dot=key.lastIndexOf('.');if(!verified(key.substring(0,dot),Integer.parseInt(key.substring(dot+1))))throw new IllegalStateException("Unverified 950 Runecrafting/Divination asset: "+key);}}
 static Native950ItemCatalog.Entry item(int id){return verified("item",id)?Native950CacheItems.entry(id):null;}
 static boolean effects(int sequence,int graphic){return verified("sequence",sequence)&&verified("graphic",graphic);}
 private static Properties load(){Properties p=new Properties();try(InputStream in=Native950RunecraftingDivinationAssets.class.getResourceAsStream("/native950/runecrafting-divination-assets-950.properties")){
  if(in==null)throw new IllegalStateException("Missing paired950 Runecrafting/Divination pins");p.load(in);return p;
 }catch(java.io.IOException e){throw new IllegalStateException(e);}}
}
