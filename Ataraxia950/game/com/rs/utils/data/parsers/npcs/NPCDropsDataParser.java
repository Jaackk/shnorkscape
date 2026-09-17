package com.rs.utils.data.parsers.npcs;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class NPCDropsDataParser {
    private static final Int2ObjectOpenHashMap<NPCDrop[]> NPC_DROPS = new Int2ObjectOpenHashMap<>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "npcs/drops.json";

    public static void init() {
        NPC_DROPS.clear();
        loadNPCDrops();
    }

    private static void loadNPCDrops() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), NPCDropsFileTemplate[].class);
        NPCDropsFileTemplate[] drops = parser.getFileLoaded();
        for (NPCDropsFileTemplate drop : drops) {
            NPC_DROPS.put(drop.npcId, drop.drops);
            NPCDefinitions.npcDefinitionsByName.put(NPCDefinitions.getNPCDefinitions(drop.npcId).name,drop.npcId);
           // NPCDefinitions.getNPCDefinitions(drop.npcId);
        }

        Logger.getGlobal().info("Loaded " + NPC_DROPS.size() + " NPC drops.");
    }

    public static NPCDrop[] getDrops(int npcId) {
        return NPC_DROPS.get(npcId);
    }

    private class NPCDropsFileTemplate {
        private int npcId;
        private NPCDrop[] drops;
    }

   /* public static void main(String[] args) {
        NPCDrops.init();
        try (
                Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            NPCDrops.npcDrops.forEach((key, value) -> {
                try {
                    writer.write("\t{\n");
                    writer.write("\t\t\"npcId\": " + key + ",\n");
                    writer.write("\t\t\"drops\": [\n");
                    int i = 0;
                    for (NPCDrop npcDrop : value) {
                        writer.write("\t\t\t{\n");
                        writer.write("\t\t\t\t\"itemId\": " + npcDrop.getItemId() + ",\n");
                        writer.write("\t\t\t\t\"minAmount\": " + npcDrop.getMinAmount() + ",\n");
                        writer.write("\t\t\t\t\"maxAmount\": " + npcDrop.getMaxAmount() + ",\n");
                        writer.write("\t\t\t\t\"rate\": " + npcDrop.getRate() + "\n");
                        i++;
                        if (i == value.length) {
                            writer.write("\t\t\t}\n");
                        } else {
                            writer.write("\t\t\t},\n");
                        }
                    }
                    writer.write("\t\t]\n");
                    writer.write("\t},\n");
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
            writer.write("]");
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }*/
}
