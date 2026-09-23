package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.rs3.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Read-only exact950 metadata audit; outputs source artifacts, never changes the running cache. */
public final class Native950PresentationAudit {
    static final Map<Integer,String> effects=new TreeMap<>();
    static final Map<String,String> pins=new TreeMap<>();
    static int pin(int index,int group,int file)throws Exception{
        byte[] raw=Cache.STORE.getIndexes()[index].getFile(group,file);
        if(raw==null)throw new IllegalStateException("Missing asset "+index+"/"+group+"/"+file);
        StringBuilder out=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))out.append(String.format("%02x",b&255));
        pins.put(index+"/"+group+"/"+file,out.toString());return file;
    }
    static Integer integer(Map<?,Object> map,Object key){Object v=map==null?null:map.get(key);return v instanceof Integer&&((Integer)v)>=0?(Integer)v:null;}
    static void graphic(Integer id)throws Exception{if(id==null)return;pin(21,id>>>8,id&255);effects.put(id,pins.get("21/"+(id>>>8)+"/"+(id&255)));}
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get(args[0]));List<Map<String,Object>> rows=new ArrayList<>();
        for(Native950AbilityCatalog.Definition d:Native950AbilityCatalog.DEFINITIONS){
            Map<Long,Object> params=RS3GeneralRequirementMap.getMap(d.struct).getValues();pin(22,d.struct>>>5,d.struct&31);
            Set<Integer> sequences=new TreeSet<>();Integer direct=integer(params,2914L),table=integer(params,2915L);
            if(direct!=null)sequences.add(direct);
            if(d.struct==48301)sequences.add(35469); // Named Undercut Volley, matching exact950 impact7879.
            if(d.struct==48324){Integer alternative=integer(params,2535L);if(alternative!=null)sequences.add(alternative);}
            if(table!=null){pin(17,table>>>8,table&255);RS3ClientScriptMap e=RS3ClientScriptMap.getMap(table);if(e.getDefaultIntValue()>=0)sequences.add(e.getDefaultIntValue());
                for(Object value:e.getValues().values())if(value instanceof Integer&&((Integer)value)>=0)sequences.add((Integer)value);}
            Map<String,Object> row=new LinkedHashMap<>();row.put("struct",d.struct);row.put("name",d.name);row.put("animationSequences",sequences);
            List<Map<String,Object>> variants=new ArrayList<>();
            if(sequences.isEmpty())sequences.add(-1);
            for(int sequence:sequences){
                Map<Integer,Object> seq=null;
                if(sequence>=0){pin(20,sequence>>>7,sequence&127);AnimationDefinitions a=AnimationDefinitions.getAnimationDefinitions(sequence);if(a.decodeFailure!=null)throw new IllegalStateException(a.decodeFailure);seq=a.clientScriptData;}
                Map<String,Object> v=new LinkedHashMap<>();v.put("sequence",sequence);String[] names={"casterGraphic","impactGraphic","projectile"};int[] keys={2920,2933,2940};
                for(int i=0;i<keys.length;i++){Integer id=integer(params,(long)keys[i]);if(id==null)id=integer(seq,keys[i]);v.put(names[i],id);graphic(id);}
                variants.add(v);
            }
            row.put("variants",variants);row.put("liveAcceptance","pending; metadata and transport admission cannot prove rendered suitability");rows.add(row);
        }
        // Ordinary Necromancy attacks use the weapon's sequence rather than an ability struct.
        AnimationDefinitions auto=AnimationDefinitions.getAnimationDefinitions(35449);pin(20,35449>>>7,35449&127);
        for(int key:new int[]{2920,2933,2940})graphic(integer(auto.clientScriptData,key));
        // Residual soul count models: 130426/428/430/432/434, sequence35465.
        // Named Pumpkin Residual Souls struct49980 selects the corresponding three-soul
        // model134807/sequence35465. Native originals contain progressively 1..5 copies.
        for(int graphicId=7866;graphicId<=7870;graphicId++)graphic(graphicId);
        for(int model:new int[]{130426,130428,130430,130432,130434})pin(47,model,0);
        pin(20,35465>>>7,35465&127);pin(22,49980>>>5,49980&31);
        Properties legacy=new Properties();
        try(java.io.InputStream in=Native950PlayerEffects.class.getResourceAsStream(Native950PlayerEffects.RESOURCE)){legacy.load(in);}
        List<Integer> previouslyRefused=new ArrayList<>();for(int id:effects.keySet())if(!legacy.containsKey("id."+id))previouslyRefused.add(id);
        Map<String,Object> report=new LinkedHashMap<>();report.put("revision",950);report.put("abilities",rows);report.put("previouslyRefusedByLegacyEffectGate",previouslyRefused);report.put("assetPins",pins);
        Files.write(Paths.get(args[1]),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report).getBytes("UTF-8"));
        StringBuilder properties=new StringBuilder("# Exact950 ability/sequence params2920,2933,2940; not legacy-ID equivalence.\nformat=1\nrevision=950\nindex=21\n");
        for(Map.Entry<Integer,String> e:effects.entrySet())properties.append("id.").append(e.getKey()).append('=').append(e.getValue()).append('\n');
        Files.write(Paths.get(args[2]),properties.toString().getBytes("UTF-8"));
        System.out.println("Presentation audit: "+rows.size()+" admitted definitions, "+effects.size()+" graphics, "+previouslyRefused.size()+" previously refused.");
    }
}
