package com.rs.game.player.client;

import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Inspects real authored rows against the paired950 cache; no world, saves or listening socket. */
public final class Native950NpcCombatCoverage {
    private Native950NpcCombatCoverage() { }
    public static void main(String[] args) throws Exception {
        if(args.length<1 || args.length>2)
            throw new IllegalArgumentException("Usage: Native950NpcCombatCoverage <950-cache> [report.json]");
        if(!NativeCacheVerification.isEnforced()) throw new IllegalStateException("Cache verification must remain enabled");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();
        Map<Integer,NPCCombatDefinition> authored=new TreeMap<>();
        for(int id:NPCCombatDefinitionsDataParser.getDefinitions().keySet())
            authored.put(id,NPCCombatDefinitionsDataParser.getDefinitions().get(id));
        List<Map<String,Object>> accepted=new ArrayList<>(),refused=new ArrayList<>();
        Map<String,Integer> reasons=new TreeMap<>();
        Map<Integer,Map<String,Object>> samples=new TreeMap<>();
        int cacheSpeed=0,cacheAttack=0,cacheDefence=0,multiTile=0,missingAttack=0,missingBlock=0,missingDeath=0;
        for(int id:authored.keySet()) {
            Native950NpcCombatCatalog.Resolution resolution=Native950NpcCombatCatalog.inspectRunningCache(id);
            Map<String,Object> row=new LinkedHashMap<>();row.put("npcId",id);
            String currentName=name(id);row.put("name950",currentName);
            row.put("legacyIdentity",Native950IdValidity.get().verdict(Native950IdValidity.Kind.NPC,id).name());
            Native950NpcCombatProfile p=resolution.profile;
            if(p==null) {
                if(resolution.reason==null) throw new AssertionError("Missing refusal reason for "+id);
                row.put("reason",resolution.reason);refused.add(row);
                reasons.put(resolution.reason,reasons.containsKey(resolution.reason)?reasons.get(resolution.reason)+1:1);
            } else {
                require(p.npcId==id&&p.name.equals(currentName),"Resolved identity mismatch "+id);
                require(p.attackAnim==-1||Native950NpcCombatAnimations.resolve(p.attackAnim)!=null,"Unverified emitted attack "+id);
                require(p.blockAnim==-1||Native950NpcCombatAnimations.resolve(p.blockAnim)!=null,"Unverified emitted block "+id);
                require(p.deathAnim==-1||Native950NpcCombatAnimations.resolve(p.deathAnim)!=null,"Unverified emitted death "+id);
                row.put("size",p.size);row.put("combatLevel",p.combatLevel);
                row.put("hpEngine",p.hp);row.put("attackLevel",p.attackLevel);row.put("defenceLevel",p.defenceLevel);
                row.put("maxHitEngine",p.maxHit);row.put("attackTicks",p.attackSpeed);
                row.put("meleeAttackBonus",p.meleeAttackBonus);row.put("meleeDefenceBonus",p.meleeDefenceBonus);
                row.put("attackStyle",p.attackStyle==0?"MELEE":p.attackStyle==1?"RANGE":"MAGIC");
                row.put("attackProjectile",p.attackProjectile);row.put("attackGraphic",p.attackGraphic);
                row.put("attackAnimation",p.attackAnim);row.put("blockAnimation",p.blockAnim);row.put("deathAnimation",p.deathAnim);
                row.put("deathAnimationTicks",p.deathAnimationTicks);row.put("deathTicks",p.deathTicks);row.put("respawnTicks",p.respawnTicks);
                Map<String,String> provenance=new LinkedHashMap<>();
                provenance.put("nameSizeLevelAttackMenu","950 NPC definition");
                provenance.put("hpMaxHitLevelsDeathRespawn","authored server tables");
                provenance.put("attackSpeed",p.cacheAttackSpeed?"950 param14":"authored server table");
                provenance.put("attackBonus",p.cacheAttackBonus?"950 param"+(p.attackStyle==0?29:p.attackStyle==1?4:3)+" converted through RS2 rating adapter":"neutral zero fallback");
                provenance.put("defenceBonus",p.cacheDefenceBonus?"950 param2865 converted through RS2 rating adapter":"neutral zero fallback");
                provenance.put("animations","authored role bindings, verified against paired cache sequence definitions");
                row.put("provenance",provenance);accepted.add(row);
                if(p.cacheAttackSpeed)cacheSpeed++;if(p.cacheAttackBonus)cacheAttack++;if(p.cacheDefenceBonus)cacheDefence++;
                if(p.size>1)multiTile++;if(p.attackAnim==-1)missingAttack++;if(p.blockAnim==-1)missingBlock++;if(p.deathAnim==-1)missingDeath++;
            }
            if(id==41||id==12353||id==7873||id==81||id==86||id==6113||id==12362||id==12365)samples.put(id,row);
        }
        require(accepted.size()>6,"Generic resolver did not expand beyond six NPCs");
        for(int id:new int[]{41,12353,7873}) {
            Native950NpcCombatProfile p=Native950NpcCombatCatalog.inspectRunningCache(id).profile;
            require(p!=null,"Expected generic baseline refused: "+id);
        }
        // These authored melee rows exercise multi-tile metadata when their combat bindings
        // remain compatible. A refused binding is an explicit coverage gap, not a fake pass.
        for(int id:new int[]{81,86}) {
            Native950NpcCombatCatalog.Resolution result=Native950NpcCombatCatalog.inspectRunningCache(id);
            if(result.profile!=null)require(result.profile.size==2,"Expected actual950 two-tile NPC "+id);
            else require(result.reason!=null,"Missing large-NPC refusal evidence "+id);
        }
        Map<String,Object> report=new LinkedHashMap<>();
        report.put("revision",950);report.put("cache",Paths.get(args[0]).toAbsolutePath().toString());
        report.put("scope","All authored combat rows resolved through production950 metadata admission; no world simulation");
        report.put("authoredRows",authored.size());report.put("accepted",accepted.size());report.put("refused",refused.size());
        report.put("refusalReasons",reasons);
        Map<String,Integer> coverage=new LinkedHashMap<>();
        coverage.put("cacheAttackSpeed",cacheSpeed);coverage.put("authoredAttackSpeed",accepted.size()-cacheSpeed);
        coverage.put("cacheAttackBonus",cacheAttack);coverage.put("neutralAttackBonus",accepted.size()-cacheAttack);
        coverage.put("cacheDefenceBonus",cacheDefence);coverage.put("neutralDefenceBonus",accepted.size()-cacheDefence);
        coverage.put("multiTile",multiTile);coverage.put("omittedAttackAnimation",missingAttack);coverage.put("omittedBlockAnimation",missingBlock);coverage.put("omittedDeathAnimation",missingDeath);
        report.put("acceptedCoverage",coverage);report.put("samples",samples);
        report.put("acceptedRows",accepted);report.put("refusedRows",refused);
        report.put("limits",Arrays.asList("Combat metadata and cache binding validation does not prove rendered model animation compatibility",
                "An authored -1 attack animation allows combat without an NPC swing animation; no animation is substituted",
                "Authored health, levels and maximum damage are server content, not newly decoded cache stats",
                "Special attacks, ranged, magic, aggression, loot and quest behavior are outside this basic melee resolver"));
        String json=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report);
        if(args.length==2)Files.write(Paths.get(args[1]),json.getBytes(StandardCharsets.UTF_8));
        System.out.println("PASS: "+accepted.size()+" / "+authored.size()+" authored NPC combat profiles accepted; "+multiTile+" multi-tile; "+refused.size()+" explicit refusals");
        System.out.println("PROVENANCE: "+coverage);System.out.println("REFUSALS: "+reasons);
        for(Map.Entry<Integer,Map<String,Object>> sample:samples.entrySet())System.out.println("SAMPLE: "+sample.getValue());
        if(args.length==1)System.out.println(json);else System.out.println("REPORT: "+Paths.get(args[1]).toAbsolutePath());
    }
    private static String name(int id) {
        try {
            byte[] raw=Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127);
            return raw==null?null:NPCDefinitions.decodeStrict947(id,raw,null).name;
        }catch(RuntimeException invalid){return null;}
    }
    private static void require(boolean condition,String message) {if(!condition)throw new AssertionError(message);}
}
