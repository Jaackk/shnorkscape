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
            row.put("adrenalineGain",d.adrenalineGain());row.put("cooldown",d.cooldown);row.put("gcd",d.effect==Native950AbilityCatalog.Effect.MOVEMENT?0:3);
            row.put("damagePercent",Arrays.asList(d.minPercent,d.maxPercent));row.put("effect",d.effect);
            row.put("hits",d.hits);List<Integer> delays=new ArrayList<>();
            for(int i=0;i<d.hits;i++)delays.add(d.hitDelay(i));row.put("hitTicks",delays);
            row.put("channelTicks",d.channelTicks());
            row.put("weaponRequirement",d.effect==Native950AbilityCatalog.Effect.MOVEMENT?"none":"matching attack style and admitted 950 equipment");
            row.put("damageModel","classic tier-derived baseline x explicit Alpha coefficient; not retail EOC accuracy/balance");
            row.put("revolutionEligible",d.revolutionEligible());
            row.put("manualQueue",d.effect==Native950AbilityCatalog.Effect.MOVEMENT?"immediate utility path":"one replacement request; waits for GCD/animation/channel; own cooldown window <=3 ticks; state revalidated");
            row.put("cancellation",d.effect==Native950AbilityCatalog.Effect.BUFF?"death/logout/world clear; persists through combat stop until expiry":"queue and pending hits cleared on combat stop/death/logout/teleport; channels revalidate range/style/hands/weapon identity");
            row.put("stunBind",d.effect==Native950AbilityCatalog.Effect.STUN?"5 ticks NPC attack/follow suppression; immunity and charge systems incomplete":"none implemented");
            row.put("dot",d.effect==Native950AbilityCatalog.Effect.BLEED?"Alpha opening hit plus 3 follow-ups every 2 ticks; retail bleed model incomplete":"none");
            row.put("buff",d.struct==14707?"33 ticks; melee +75%, incoming combat +25%":d.struct==19251?"50 ticks; ranged +50% within 3 tiles of cast origin; area DOT not implemented":"none implemented");
            row.put("implementationStatus","partial Combat Alpha; see combat-offline-audit.md for per-ability gaps");
            RS3GeneralRequirementMap data=RS3GeneralRequirementMap.getMap(d.struct);
            row.put("cacheParams",new TreeMap<Long,Object>(data.getValues()));
            row.put("structSha256",Native950Bindings.sha256(Cache.STORE.getIndexes()[22].getFile(d.struct>>>5,d.struct&31)));
            int enumId=data.getIntValue(2915);row.put("animationEnum",enumId);
            row.put("animationConfidence",enumId>0?"paired-cache weapon-family mapping; rendered visibility unverified":"missing; no invented fallback");
            row.put("targetGraphic",data.getIntValue(2933));
            row.put("presentationPolicy","sequence param 2920 self graphic; struct 2933 target graphic; ability projectile emission incomplete; raw sequence params below");
            Map<Integer,Object> animations=new TreeMap<>();
            if(enumId>0){
                RS3ClientScriptMap families=RS3ClientScriptMap.getMap(enumId);
                Map<Long,Object> mappings=new TreeMap<>(families.getValues());
                mappings.put(-1L,families.getDefaultIntValue());
                row.put("animationFamilies",mappings);
                for(Object value:mappings.values())if(value instanceof Integer&&((Integer)value)>=0){
                    int id=(Integer)value;if(animations.containsKey(id))continue;
                    byte[] raw=Cache.STORE.getIndexes()[20].getFile(id>>>7,id&127);
                    AnimationDefinitions sequence=AnimationDefinitions.getAnimationDefinitions(id);
                    Map<String,Object> details=new LinkedHashMap<>();
                    details.put("rawPresent",raw!=null&&raw.length>0);
                    details.put("sha256",raw==null?null:Native950Bindings.sha256(raw));
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
