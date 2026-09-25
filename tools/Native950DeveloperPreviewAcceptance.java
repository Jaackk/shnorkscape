package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import java.nio.file.Paths;

/** Read-only real-cache preview identity probe. No world, socket or character. */
public final class Native950DeveloperPreviewAcceptance {
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get(args[0]));
        java.lang.reflect.Method verify=Native950DeveloperConsole.class.getDeclaredMethod("verify");
        verify.setAccessible(true);verify.invoke(null);
        NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();
        int ordinary=Native950DeveloperCatalogue.search("NPC","Man").get(0).id;
        for(int id:new int[]{ordinary,6260,17161}){
            Native950DeveloperPreview p=Native950DeveloperPreview.resolve(id);
            if(p.npc!=id||p.idle<0||p.zoom<=0)throw new AssertionError("Missing native preview "+id);
            if(id==6260&&p.attack<0)throw new AssertionError("Graardor verified attack missing");
            System.out.println("NPC "+id+": idle="+p.idle+", attack="+p.attack+", zoom="+p.zoom+", height="+p.height+", Beasts framing="+p.nativeFraming);
        }
        System.out.println("Actual-cache preview metadata PASS; Vulkan rendering remains unverified.");
    }
}
