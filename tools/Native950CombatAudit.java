package com.rs.game.player.client;

import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.player.client.ui.Native950Bindings;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Read-only cache/source inventory. No player, world, transport or save is opened. */
public final class Native950CombatAudit {
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get("cache"));
        List<Map<String,Object>> rows=new ArrayList<>();
        for(Native950AbilityCatalog.Definition d:Native950AbilityCatalog.DEFINITIONS){
            Map<String,Object> row=new LinkedHashMap<>();rows.add(row);
            row.put("name",d.name);row.put("structure",d.struct);row.put("style",d.style());
            row.put("type",d.tier);row.put("level",d.level);row.put("offhand",d.offhandRequired);
            row.put("twoHanded",d.twoHandedRequired);row.put("target",d.targetRequired());
            row.put("adrenalineRequired",d.adrenalineRequired());row.put("adrenalineCost",d.adrenalineCost());
            row.put("adrenalineGain",d.adrenalineGain());row.put("cooldown",d.cooldown);row.put("gcd",3);
            row.put("damagePercent",Arrays.asList(d.minPercent,d.maxPercent));row.put("effect",d.effect);
            row.put("hits",d.hits);List<Integer> delays=new ArrayList<>();
            for(int i=0;i<d.hits;i++)delays.add(d.hitDelay(i));row.put("hitTicks",delays);
            row.put("channelTicks",d.channelTicks());
            RS3GeneralRequirementMap data=RS3GeneralRequirementMap.getMap(d.struct);
            row.put("cacheParams",new TreeMap<Long,Object>(data.getValues()));
            row.put("structSha256",Native950Bindings.sha256(Cache.STORE.getIndexes()[22].getFile(d.struct>>>5,d.struct&31)));
            int enumId=data.getIntValue(2915);row.put("animationEnum",enumId);
            Map<Integer,Object> animations=new TreeMap<>();
            if(enumId>0){
                RS3ClientScriptMap families=RS3ClientScriptMap.getMap(enumId);
                Map<Long,Object> mappings=new TreeMap<>(families.getValues());
                mappings.put(-1L,families.getDefaultIntValue());
                row.put("animationFamilies",mappings);
                for(Object value:mappings.values())if(value instanceof Integer&&((Integer)value)>=0){
                    int id=(Integer)value;if(animations.containsKey(id))continue;
                    AnimationDefinitions sequence=AnimationDefinitions.getAnimationDefinitions(id);
                    Map<String,Object> details=new LinkedHashMap<>();
                    details.put("decodeFailure",sequence.decodeFailure);details.put("legacyFrameMillis",sequence.getEmoteTime());
                    details.put("params",sequence.clientScriptData==null?Collections.emptyMap():new TreeMap<>(sequence.clientScriptData));
                    animations.put(id,details);
                }
            }
            row.put("animations",animations);
        }
        Files.write(Paths.get(args[0]),new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(rows).getBytes(StandardCharsets.UTF_8));
        System.out.println("Audited "+rows.size()+" supported abilities into "+args[0]);
    }
}
