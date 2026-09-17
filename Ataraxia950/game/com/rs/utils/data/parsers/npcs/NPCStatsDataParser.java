package com.rs.utils.data.parsers.npcs;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.npcs.pojos.NPCStats;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;

public class NPCStatsDataParser {
    private static final Int2ObjectOpenHashMap<NPCStats> NPC_STATS = new Int2ObjectOpenHashMap<>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    public static final String path = "npcs/npcstats.json";

    public static void init() {
        loadNPCStats();
    }

    private static void loadNPCStats() {
        JsonParser parser = new JsonParser(DataPaths.resolve(path), NPCStatsFileTemplate[].class);
        NPCStatsFileTemplate[] stats = parser.getFileLoaded();
        for (NPCStatsFileTemplate stat : stats) {
            stat.stats.fixStats();
            NPC_STATS.put(stat.npcId, stat.stats);
        }
        Logger.getGlobal().info("Loaded " + NPC_STATS.size() + " NPC combat stats.");
    }

    public static void resetNpcStats() {
        NPC_STATS.clear();
        init();
    }

    public static NPCStats getStats(int npcId) {
        NPCStats stats = NPC_STATS.get(npcId);
        if (stats == null) {
            stats = new NPCStats(40, 40, 40, 40, 90, 55, 55, 55);
        }
        return stats;
    }

    public static Int2ObjectOpenHashMap<NPCStats> getDefinitions() {
        return NPC_STATS;
    }

    @Data
    public static class NPCStatsFileTemplate {
        private int npcId;
        private NPCStats stats;

        public NPCStatsFileTemplate(int npcId, NPCStats stats) {
            this.npcId = npcId;
            this.stats = stats;
        }
    }
}
