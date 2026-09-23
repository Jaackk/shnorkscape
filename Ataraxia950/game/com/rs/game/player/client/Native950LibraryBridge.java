package com.rs.game.player.client;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** Accept only the byte-pinned, guarded library extensions to the original native scripts. */
public final class Native950LibraryBridge {
    private static final JsonObject PINS=load();
    private static JsonObject load(){
        try(InputStream in=Native950LibraryBridge.class.getResourceAsStream("/native950/library-search-bridge-950.json")){
            if(in==null)throw new IllegalStateException("Missing native library bridge pins");
            return new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
        }catch(IOException e){throw new IllegalStateException(e);}
    }
    public static boolean matches(String key,String original,String actual){
        if(original.equals(actual))return true;
        if(!PINS.has(key))return false;
        JsonObject pin=PINS.getAsJsonObject(key);
        return original.equals(pin.get("original").getAsString())&&actual.equals(pin.get("patched").getAsString());
    }
    static void verify(){
        Boolean installed=null;
        for(java.util.Map.Entry<String,JsonElement> row:PINS.entrySet()){
            String[] key=row.getKey().split("/");JsonObject pin=row.getValue().getAsJsonObject();
            String actual=Native950EquipmentCatalogue.hash(com.rs.cache.Cache.STORE.getIndexes()[Integer.parseInt(key[0])].getFile(Integer.parseInt(key[1]),Integer.parseInt(key[2])));
            boolean patched=actual.equals(pin.get("patched").getAsString());
            if(!patched&&!actual.equals(pin.get("original").getAsString()))throw new IllegalStateException("Changed developer library bridge "+row.getKey());
            if(installed!=null&&installed!=patched)throw new IllegalStateException("Incomplete developer library cache update");
            installed=patched;
        }
    }
}
