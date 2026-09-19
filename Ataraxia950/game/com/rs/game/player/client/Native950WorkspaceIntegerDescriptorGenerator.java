package com.rs.game.player.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rs.cache.Cache;
import com.rs.cache.loaders.VarBitDefinitions;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/** Build-time/read-only generator for the pinned workspace descriptor. Never used by a live session. */
public final class Native950WorkspaceIntegerDescriptorGenerator {
    private static final int[] ROOT_SCRIPTS = {8707, 8708, 8709};
    private static final String BOOTSTRAP = "2852,2853,2854,2855,2856,2857,2858,2859,2860,2862,2863,2864,2865,2866,2867,2868,2869,2912,2913,2914,2915,2916,2917,2918,2919,2920,2921,2922,2923,2924,2925,2926,2927,2928,2929,2930,2931,2932,2933,2934,2935,2936,2937,2938,2939,2940,2941,2942,2943,2944,2945,2946,2947,2948,2949,2950,2951,2952,2953,2954,2955,2956,2957,2959,2960,2961,2962,2963,2964,2965,2966,2967,2968,2969,2970,2971,2972,2973,2974,2975,2976,2977,2978,2979,2980,2981,2982,2983,2984,2985,2986,2987,2988,2989,2990,2991,2992,2993,2994,2995,2996,3721,3722,3723,3769,3770,3825,3826,4120,4154,4155,4254,4255,4322,4323,4324,4325,4496,4510,4511,4512,4513,4514,4515,4516,4646,4647,4684,4685,4705,4706,4723,4724,4764,4765,4794,4798,4813,4814,4939,4954,4955,4997,4998,4999,5000,5001,5002,5003,5004,5005,5006,5007,5008,5009,5010,5014,5015,5016,5017,5018,5019,5020,5021,5079,5081,5082,5139,5140,5154,5155,5160,5161,5192,5193,5840,5841,5937,5947,5948,6005,6006,6046,6047,6102,6103,6104,6105,6106,6107,6108,6109,6110,6111,6112,6113,6114,6115,6116,6117,6118,6119,6277,6278,6296,6302,6304,6305,6323,6324,6417,6418,6439,6457,6458";
    private static final Set<Integer> FOUR = new LinkedHashSet<Integer>(Arrays.asList(0x35e,0x592,0x713,0x647,0x412,0xab,0x454,0x73d,0x3f2,0x25a,0x717,0x895,0x267,0x51a,0x56,0x96,0x639,0x1ca,0x195,0x30,0xa2));

    private Native950WorkspaceIntegerDescriptorGenerator() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Usage: <cache> <opcode-map-json> <output-properties>");
        Cache.initFlatReadOnly(java.nio.file.Paths.get(args[0]));
        Map<Integer,Integer> inverse = inverse(java.nio.file.Paths.get(args[1]));
        Evidence evidence = derive(inverse);
        write(evidence, java.nio.file.Paths.get(args[2]));
        System.out.println("workspace descriptor IDs=" + evidence.ids.size() + " scripts=" + evidence.scripts.size() + " domain2-varbits=" + evidence.varbits.size());
    }

    static Evidence derive(Map<Integer,Integer> inverse) throws Exception {
        Set<Integer> ids = bootstrap();
        Map<Integer, byte[]> scripts = new LinkedHashMap<Integer, byte[]>();
        Map<Integer, VarBitDefinitions> varbits = new LinkedHashMap<Integer, VarBitDefinitions>();
        ArrayDeque<Integer> pending = new ArrayDeque<Integer>(); for (int id : ROOT_SCRIPTS) pending.add(id);
        while (!pending.isEmpty()) {
            int scriptId = pending.removeFirst(); if (scripts.containsKey(scriptId)) continue;
            byte[] raw = Cache.STORE.getIndexes()[12].getFile(scriptId, 0);
            if (raw == null) throw new IllegalStateException("Missing workspace script " + scriptId);
            scripts.put(scriptId, raw);
            for (Instruction instruction : decode(raw, inverse)) {
                if (instruction.opcode == 0x895) pending.add((Integer) instruction.argument);
                else if (instruction.opcode == 0x195 && (((Integer) instruction.argument) >>> 24) == 2)
                    ids.add(((Integer) instruction.argument >>> 8) & 0xffff);
                else if (instruction.opcode == 0xa2) {
                    int varbitId = ((Integer) instruction.argument >>> 8) & 0xffff;
                    VarBitDefinitions definition = VarBitDefinitions.getClientVarpBitDefinitions(varbitId);
                    if (definition.varDomain == 2 && !definition.isAbsent()) { ids.add(definition.baseVar); varbits.put(varbitId, definition); }
                }
            }
        }
        if (!ids.contains(3296) || !containsParent(varbits, 3296)) throw new IllegalStateException("Workspace parent 3296 was not derived from domain-2 varbit evidence");
        return new Evidence(ids, scripts, varbits);
    }

    private static boolean containsParent(Map<Integer,VarBitDefinitions> varbits, int parent) { for (VarBitDefinitions value : varbits.values()) if (value.baseVar == parent) return true; return false; }
    private static Set<Integer> bootstrap() { Set<Integer> values=new TreeSet<Integer>(); for(String id:BOOTSTRAP.split(",")) values.add(Integer.parseInt(id)); if(values.size()!=215) throw new IllegalStateException("Expected 215 bootstrap IDs"); return values; }
    private static Map<Integer,Integer> inverse(Path path) throws Exception { JsonObject root=new JsonParser().parse(new String(Files.readAllBytes(path),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("opcodeMap947to950"); Map<Integer,Integer> result=new HashMap<Integer,Integer>(); for(Map.Entry<String,JsonElement> entry:root.entrySet()) result.put(Integer.decode(entry.getValue().getAsJsonObject().get("opcode950").getAsString()),Integer.decode(entry.getKey())); return result; }
    private static Set<Instruction> decode(byte[] raw, Map<Integer,Integer> inverse) { try { ByteBuffer buffer=ByteBuffer.wrap(raw); int end=raw.length-(buffer.getShort(raw.length-2)&65535)-18; while(buffer.get()!=0){} Set<Instruction> result=new LinkedHashSet<Instruction>(); while(buffer.position()<end) { int wire=buffer.getShort()&65535; Integer opcode=inverse.get(wire); if(opcode==null) throw new IllegalStateException("Unmapped workspace script wire opcode "+wire); Object argument; if(opcode==0x511){ int type=buffer.get()&255; if(type==2){ while(buffer.get()!=0){} argument=""; } else argument=type==1?Long.valueOf(buffer.getLong()):Integer.valueOf(buffer.getInt()); } else argument=FOUR.contains(opcode)?Integer.valueOf(buffer.getInt()):Integer.valueOf(buffer.get()&255); result.add(new Instruction(opcode,argument)); } return result; } catch(RuntimeException error) { throw new IllegalStateException("Malformed workspace script evidence",error); } }
    private static void write(Evidence evidence, Path target) throws Exception { Properties p=new Properties(); p.setProperty("version","1");p.setProperty("revision","950");p.setProperty("bootstrap.count","215");p.setProperty("bootstrap.ids",BOOTSTRAP);p.setProperty("script.ids",join(evidence.scripts.keySet()));p.setProperty("varbit.ids",join(evidence.varbits.keySet()));p.setProperty("ids",join(evidence.ids)); for(Map.Entry<Integer,byte[]> e:evidence.scripts.entrySet())p.setProperty("script."+e.getKey()+".sha256",sha(e.getValue())); for(Map.Entry<Integer,VarBitDefinitions> e:evidence.varbits.entrySet()){VarBitDefinitions d=e.getValue(); byte[] raw=Cache.STORE.getIndexes()[2].getFile(69,e.getKey());p.setProperty("varbit."+e.getKey()+".sha256",sha(raw));p.setProperty("varbit."+e.getKey()+".parent",String.valueOf(d.baseVar));p.setProperty("varbit."+e.getKey()+".domain",String.valueOf(d.varDomain));} p.setProperty("fingerprint.sha256",sha(canonical(p).getBytes(StandardCharsets.UTF_8))); Files.createDirectories(target.getParent()); try(java.io.OutputStream out=Files.newOutputStream(target)){p.store(out,"Revision-950 known-safe integer workspace persistence descriptor. Generated; do not hand edit.");} }
    private static String canonical(Properties p){TreeSet<String> keys=new TreeSet<String>(p.stringPropertyNames());keys.remove("fingerprint.sha256");StringBuilder s=new StringBuilder();for(String k:keys)s.append(k).append('=').append(p.getProperty(k)).append('\n');return s.toString();}
    private static String join(Iterable<Integer> ids){StringBuilder s=new StringBuilder();for(Integer id:ids){if(s.length()>0)s.append(',');s.append(id);}return s.toString();}
    private static String sha(byte[] raw)throws Exception{StringBuilder s=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))s.append(String.format("%02x",b&255));return s.toString();}
    static final class Evidence { final Set<Integer> ids;final Map<Integer,byte[]> scripts;final Map<Integer,VarBitDefinitions> varbits; Evidence(Set<Integer> ids,Map<Integer,byte[]>scripts,Map<Integer,VarBitDefinitions>varbits){this.ids=Collections.unmodifiableSet(new TreeSet<Integer>(ids));this.scripts=Collections.unmodifiableMap(new LinkedHashMap<Integer,byte[]>(scripts));this.varbits=Collections.unmodifiableMap(new LinkedHashMap<Integer,VarBitDefinitions>(varbits));} }
    private static final class Instruction { final int opcode; final Object argument; Instruction(int opcode,Object argument){this.opcode=opcode;this.argument=argument;} }
}
