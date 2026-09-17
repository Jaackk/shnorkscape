package com.rs.game.player.client;

import com.google.gson.*;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Exact native950 map membership, derived by tools/generate_950_map_areas.py.
 * Index23/group3 supplies one RLE array per map square; its24-bit labels identify
 * opcode2 of config2/83. Entries are8-tile chunks in x-major order, not rectangles.
 * The client compares these labels at0x1403162dc before building terrain.
 */
public final class Native950MapAreas {
    private static final String RESOURCE="/native950/map-areas-950.json";
    private static Object verifiedStore;
    private Native950MapAreas() { }

    /** Returns -1 for an absent/unassigned chunk; never aliases out-of-world X into Y. */
    public static int resolvedAreaType(int x,int y) {
        if(x<0 || x>=8192 || y<0 || y>=16384) return -1;
        int[] square=Data.SQUARES.get((x>>>6)|((y>>>6)<<7));
        return square==null ? -1 : square[((x>>>3)&7)*8+((y>>>3)&7)];
    }
    public static int areaTypeFor(int x,int y,int configured) {
        int resolved=resolvedAreaType(x,y);
        return resolved<0 ? configured : resolved;
    }
    public static int defaultAreaType() { return Data.JSON.get("default").getAsInt(); }
    public static int mappedSquares() { return Data.SQUARES.size(); }

    /** Match the client RLE reader: the final label fills the remainder without a count byte. */
    static int[] decodeLabels(byte[] raw) {
        if(raw==null) throw new IllegalArgumentException("Missing950 map labels");
        int[] labels=new int[64]; int p=0,n=0;
        while(p<raw.length) {
            if(raw.length-p<3) throw new IllegalArgumentException("Truncated950 map label");
            int label=((raw[p]&255)<<16)|((raw[p+1]&255)<<8)|(raw[p+2]&255);p+=3;
            int count=p<raw.length ? raw[p++]&255 : 64-n;
            if(count<1 || count>64-n) throw new IllegalArgumentException("Invalid950 map label run");
            Arrays.fill(labels,n,n+count,label);n+=count;
        }
        if(n!=64)throw new IllegalArgumentException("950 map labels must fill64 chunks");
        return labels;
    }

    /** Enforce both complete source groups against the paired cache, once per opened store. */
    public static synchronized void verify() {
        if(Cache.STORE!=null && verifiedStore==Cache.STORE)return;
        if(Cache.STORE==null || !Cache.isFlatReadOnly())throw new IllegalStateException("Map areas require the paired950 cache");
        try {
            for(JsonElement e:Data.JSON.getAsJsonArray("bindings")) {
                JsonObject pin=e.getAsJsonObject();int indexId=pin.get("index").getAsInt(),group=pin.get("group").getAsInt();
                Index index=Cache.STORE.getIndexes()[indexId];
                if(index==null || !index.archiveExists(group))throw new IllegalStateException("Missing950 map-area group");
                int[] ids=index.getTable().getArchives()[group].getValidFileIds().clone();Arrays.sort(ids);
                if(ids.length!=pin.get("files").getAsInt())throw new IllegalStateException("Changed950 map-area membership");
                MessageDigest digest=MessageDigest.getInstance("SHA-256");
                for(int id:ids) {
                    byte[] raw=index.getFile(group,id);
                    if(raw==null)throw new IllegalStateException("Missing950 map-area file "+indexId+"/"+group+"/"+id);
                    digest.update(ByteBuffer.allocate(8).putInt(id).putInt(raw.length).array());digest.update(raw);
                    if(indexId==23) {
                        int[] actual=decodeLabels(raw),derived=Data.SQUARES.get(id);
                        if(derived==null)throw new IllegalStateException("Missing generated map square "+id);
                        for(int n=0;n<64;n++) {
                            Integer area=Data.LABELS.get(actual[n]);
                            if(derived[n]!=(area==null?-1:area))throw new IllegalStateException("Changed generated map chunk "+id+":"+n);
                        }
                    } else {
                        if(raw.length<5 || raw[0]!=2)throw new IllegalStateException("Changed950 area definition "+id);
                        int label=((raw[1]&255)<<16)|((raw[2]&255)<<8)|(raw[3]&255);
                        if(!Integer.valueOf(id).equals(Data.LABELS.get(label)))throw new IllegalStateException("Changed generated area label "+id);
                    }
                }
                StringBuilder actual=new StringBuilder();for(byte b:digest.digest())actual.append(String.format("%02x",b&255));
                if(!pin.get("sha256").getAsString().equals(actual.toString()))throw new IllegalStateException("Changed950 map-area cache group "+indexId+"/"+group);
            }
        } catch(java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
        verifiedStore=Cache.STORE;
    }

    private static final class Data {
        static final JsonObject JSON=load();
        static final Map<Integer,Integer> LABELS=labels();
        static final Map<Integer,int[]> SQUARES=squares();
        static JsonObject load() {
            java.io.InputStream stream=Native950MapAreas.class.getResourceAsStream(RESOURCE);
            if(stream==null)throw new IllegalStateException("Missing950 map-area resource");
            try(InputStreamReader reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) {
                return new JsonParser().parse(reader).getAsJsonObject();
            } catch(java.io.IOException failure) { throw new IllegalStateException("Cannot read950 map-area resource",failure); }
        }
        static Map<Integer,Integer> labels() {
            Map<Integer,Integer> labels=new HashMap<Integer,Integer>();
            for(JsonElement e:JSON.getAsJsonArray("areas")) {
                JsonArray row=e.getAsJsonArray();int id=row.get(0).getAsInt(),label=row.get(1).getAsInt();
                if(id<0 || id>65535 || label<=0 || label>0xffffff || labels.put(label,id)!=null)
                    throw new IllegalStateException("Invalid950 area identity");
            }
            return Collections.unmodifiableMap(labels);
        }
        static Map<Integer,int[]> squares() {
            Map<Integer,int[]> squares=new HashMap<Integer,int[]>();Set<Integer> areas=new HashSet<Integer>(LABELS.values());
            for(JsonElement e:JSON.getAsJsonArray("squares")) {
                JsonArray row=e.getAsJsonArray();int id=row.get(0).getAsInt(),n=0;int[] chunks=new int[64];
                if(id<0 || id>=32768 || row.size()<3 || (row.size()&1)==0)throw new IllegalStateException("Invalid950 map square");
                for(int p=1;p<row.size();p+=2) {
                    int area=row.get(p).getAsInt(),count=row.get(p+1).getAsInt();
                    if((area!=-1 && !areas.contains(area)) || count<1 || count>64-n)throw new IllegalStateException("Invalid950 area run");
                    Arrays.fill(chunks,n,n+count,area);n+=count;
                }
                if(n!=64 || squares.put(id,chunks)!=null)throw new IllegalStateException("Duplicate/incomplete950 map square");
            }
            return Collections.unmodifiableMap(squares);
        }
    }
}
