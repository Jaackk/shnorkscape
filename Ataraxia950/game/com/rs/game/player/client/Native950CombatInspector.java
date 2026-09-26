package com.rs.game.player.client;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.actions.slayer.SlayerTaskData;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import java.util.*;

/** Read-only developer reports. Admission is always delegated to the production resolver. */
final class Native950CombatInspector {
    static List<String> npc(int id){
        List<String> out=new ArrayList<>();
        Native950NpcCombatCatalog.Resolution result=Native950NpcCombatCatalog.inspectRunningCache(id);
        List<Native950DeveloperCatalogue.Entry> entries=Native950DeveloperCatalogue.search("NPC",Integer.toString(id));
        out.add("NPC "+id+" "+(entries.isEmpty()?"not in world-model catalogue":entries.get(0).name));
        out.add("Attackable by native combat: "+(result.profile!=null)+"; "+(result.reason==null?"profile admitted; runtime target checks still apply":result.reason));
        Native950NpcCombatProfile p=result.profile;
        if(p!=null){
            out.add("Source: authored combatDefs/stats + paired950 parameters and verified presentation bindings.");
            out.add("Style="+p.attackStyle+" (0 melee, 1 range, 2 magic); footprint="+p.size+"; level="+p.combatLevel);
            out.add("HP="+p.hp+" maxHit="+p.maxHit+" engine units (display x10); attackSpeed="+p.attackSpeed+" ticks; cacheSpeed="+p.cacheAttackSpeed);
            out.add("Animation attack/block/death="+p.attackAnim+"/"+p.blockAnim+"/"+p.deathAnim+"; projectile="+p.attackProjectile+" casterGraphic="+p.attackGraphic);
            out.add("Death delay="+p.deathTicks+" respawn="+p.respawnTicks+" ticks (temporary one-life spawns do not respawn).");
            if(p.attackAnim<0||p.deathAnim<0)out.add("PRESENTATION PARTIAL: absent attack/death binding; admission is not visual acceptance.");
        }
        com.rs.utils.data.parsers.npcs.pojos.NPCDrop[] drops=NPCDropsDataParser.getDrops(id);
        out.add("Authored drop rows="+(drops==null?0:drops.length)+"; actual awards retain per-item950 validation and damage-credit ownership.");
        out.add(slayerSummary(id));
        out.add("Encounter policy="+(Native950BossRules.king(id)?"Dagannoth King PARTIAL":id==6260?"Graardor normal PARTIAL":"no curated boss policy; ordinary admission does not certify a boss"));
        for(Native950GamevalLookup.Entry e:Native950GamevalLookup.search(Integer.toString(id)))if(e.type.equals("npc"))out.add("Symbol: "+e.name+"; "+e.verify());
        return out;
    }
    static List<String> search(String query){
        String q=query.trim();if(q.matches("[0-9]{1,7}"))return npc(Integer.parseInt(q));List<Native950DeveloperCatalogue.Entry> rows=Native950DeveloperCatalogue.search("NPC",q);
        if(rows.isEmpty())for(Native950GamevalLookup.Entry e:Native950GamevalLookup.search(q))
            if(e.type.equals("npc")&&e.name.equalsIgnoreCase(q)){rows=Native950DeveloperCatalogue.search("NPC",e.id);break;}
        if(rows.size()==1)return npc(rows.get(0).id);
        List<String> out=new ArrayList<>();out.add(rows.size()+" matches; use ;;npcinfo <exact ID> to inspect. First 12:");
        for(int i=0;i<Math.min(12,rows.size());i++)out.add(rows.get(i).id+" - "+rows.get(i).name);
        return out;
    }
    /**
     * Stage B Phase 4: two independent, disagreement-tolerant Slayer signals, reported side by
     * side rather than merged into one verdict. NPC parameter 50 is a raw cache category id -
     * present on this cache for Abyssal demon (42), Exiled Kalphite Queen (53) and others, but
     * empirically absent on plenty of otherwise-ordinary attackable creatures of the same species,
     * so its absence proves nothing. The name match is the inherited legacy table
     * ({@code SlayerTaskData}) already used for real task assignment; this method changes no
     * gameplay behaviour, it only surfaces both signals for a developer to cross-check.
     */
    private static String slayerSummary(int id){
        NPCDefinitions definition;
        try{definition=NPCDefinitions.getNPCDefinitions(id);}catch(RuntimeException undecodable){definition=null;}
        Object param=definition==null||definition.clientScriptData==null?null:definition.clientScriptData.get(50);
        String cacheCategory=param instanceof Integer?String.valueOf(param):"none";
        String nameMatch="none";
        if(definition!=null&&definition.name!=null){
            String trimmed=definition.name.trim();
            outer: for(SlayerTaskData task:SlayerTaskData.values())
                for(String monster:task.getMonsters())
                    if(monster.equalsIgnoreCase(trimmed)){nameMatch=task.toString();break outer;}
        }
        return "Slayer: cache category param50="+cacheCategory+"; task-name match="+nameMatch
                +" (independent signals; a mismatch or an absent cache param is not itself an error).";
    }
    static void send(Player p,List<String> lines){for(String line:lines)p.sendMessage(line);}
}
