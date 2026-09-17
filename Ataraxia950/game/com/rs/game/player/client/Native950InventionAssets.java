package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import java.io.*;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.util.*;
/** Bindings generated from the paired950 cache and identity-matched910 disassembly rows. */
final class Native950InventionAssets {
 private static Properties pins;private static Object store;private static final Map<String,Boolean> checked=new HashMap<>();
 static synchronized boolean pin(String kind,int id){
  if(Cache.STORE==null||!Cache.isFlatReadOnly()||id<0)return false;
  if(pins==null){pins=new Properties();try(InputStream in=Native950InventionAssets.class.getResourceAsStream("/native950/invention-archaeology-assets-950.properties")){if(in==null)throw new IllegalStateException("Missing Invention/Archaeology bindings");pins.load(in);}catch(IOException e){throw new IllegalStateException(e);}}
  if(store!=Cache.STORE){store=Cache.STORE;checked.clear();}String key=kind+"."+id;
  if(!checked.containsKey(key)){boolean valid=false;String expected=pins.getProperty(key);if(expected!=null)try{
   int idx=kind.equals("item")?19:kind.equals("object")?16:kind.equals("sequence")?20:kind.equals("enum")?17:kind.equals("struct")?22:2;
   int shift=kind.equals("sequence")?7:kind.equals("struct")?5:8;
   byte[] raw=Cache.STORE.getIndexes()[idx].getFile(kind.equals("invention")?41:id>>>shift,kind.equals("invention")?id:id&((1<<shift)-1));
   if(raw!=null){StringBuilder h=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))h.append(String.format("%02x",b&255));valid=expected.equals(h.toString());}
  }catch(Exception changed){valid=false;}checked.put(key,valid);}return checked.get(key);
 }
 static boolean item(int id){if(id<1||id>65534||!pin("item",id))return false;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);return d.loaded&&d.name!=null&&!d.name.equals("null")&&d.certTemplateId<0&&d.lendTemplateId<0&&d.bindTemplateId<0&&d.shardTemplateId<0;}
 static Native950ItemCatalog.Entry itemEntry(int id){if(!item(id))return null;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);return new Native950ItemCatalog.Entry(id,d.name,d.isStackable(),new String[5]);}
 static Reader reader(String resource){InputStream in=Native950InventionAssets.class.getResourceAsStream("/native950/"+resource);if(in==null)throw new IllegalStateException("Missing "+resource);return new InputStreamReader(in,StandardCharsets.UTF_8);}
}
