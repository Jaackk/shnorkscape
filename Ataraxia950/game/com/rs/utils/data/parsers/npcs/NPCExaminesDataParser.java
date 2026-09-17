package com.rs.utils.data.parsers.npcs;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.game.npc.NPC;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCExamine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class NPCExaminesDataParser {
    private static final Object2ObjectOpenHashMap<String, String> NPC_EXAMINES = new Object2ObjectOpenHashMap<>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "npcs/examines.json";

    public static void init() {
        loadNPCExamines();
    }

    private static void loadNPCExamines() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), NPCExamine[].class);
        NPCExamine[] examines = parser.getFileLoaded();
        for (NPCExamine examine : examines) {
            NPC_EXAMINES.put(examine.getName(), examine.getDescription());
        }
        Logger.getGlobal().info("Loaded " + NPC_EXAMINES.size() + " NPC examines.");
    }

    public static String getExamine(NPC npc) {
        String examine = NPC_EXAMINES.get(npc.getName());
        if (examine != null)
            return examine;
        return "It's " + Utils.getAorAn(npc.getDefinitions().name) + " " + npc.getDefinitions().name + ".";
    }

    // converter:
    /*
    NPCExamines.init();
        try (Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            npcExamines.forEach((key, value) -> {
                try {
                    writer.write("\t{\n");
                    writer.write("\t\t\"name\": \"" + key + "\",\n");
                    writer.write("\t\t\"description\": \"" + value + "\"\n");
                    writer.write("\t},\n");
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
            writer.write("]");
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
     */
}
