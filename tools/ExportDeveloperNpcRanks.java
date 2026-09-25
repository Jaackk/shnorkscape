package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Offline paired-cache ranking. No world or save is created. */
public final class ExportDeveloperNpcRanks {
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get(args[0]));List<String> lines=new ArrayList<>();
        com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser.init();
        com.rs.utils.data.parsers.npcs.NPCStatsDataParser.init();
        lines.add("# Exact950 concrete NPC options; score prioritises useful interactions, curated boss identity then attack.");
        for(Native950DeveloperCatalogue.Entry e:Native950DeveloperCatalogue.search("NPC","")){
            NPCDefinitions d=NPCDefinitions.getNPCDefinitions(e.id);int score=0;
            if(d.menuOptions!=null)for(String op:d.menuOptions)if(op!=null){
                String name=op.toLowerCase(Locale.ROOT);
                if(name.contains("bank"))score+=1000;
                else if(name.equals("attack"))score+=100;
                else if(name.equals("talk-to")||name.equals("trade"))score+=200;
                else if(!name.equals("examine"))score+=10;
            }
            if(Native950BossCatalogue.find(e.id)!=null)score+=2000;
            if(Native950NpcCombatCatalog.inspectRunningCache(e.id).profile!=null)score+=5000;
            lines.add(e.id+"\t"+score);
        }
        Files.write(Paths.get(args[1]),lines,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);
        System.out.println("Exported "+(lines.size()-1)+" NPC ranks.");
    }
}
