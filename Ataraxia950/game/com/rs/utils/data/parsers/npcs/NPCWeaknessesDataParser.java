package com.rs.utils.data.parsers.npcs;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.npcs.pojos.NPCWeakness;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class NPCWeaknessesDataParser {
    public static final int STAB = 0;
    public static final int SLASH = 1;
    public static final int CRUSH = 2;
    public static final int ARROW = 3;
    public static final int BOLT = 4;
    public static final int THROWN = 5;
    public static final int AIR = 6;
    public static final int WATER = 7;
    public static final int EARTH = 8;
    public static final int FIRE = 9;

    private static final Int2IntOpenHashMap NPC_WEAKNESSES = new Int2IntOpenHashMap();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "npcs/weaknesses.json";

    public static void init() {
        loadNPCWeaknesses();
    }

    private static void loadNPCWeaknesses() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), NPCWeakness[].class);
        NPCWeakness[] weaknesses = parser.getFileLoaded();
        for (NPCWeakness weakness : weaknesses) {
            String weaknessAsString = weakness.getWeakness();
            int weaknessAsInt = STAB;
            if (weaknessAsString.equalsIgnoreCase("slash")) {
                weaknessAsInt = SLASH;
            } else if (weaknessAsString.equalsIgnoreCase("crush")) {
                weaknessAsInt = CRUSH;
            } else if (weaknessAsString.equalsIgnoreCase("arrow")) {
                weaknessAsInt = ARROW;
            } else if (weaknessAsString.equalsIgnoreCase("bolt")) {
                weaknessAsInt = BOLT;
            } else if (weaknessAsString.equalsIgnoreCase("thrown")) {
                weaknessAsInt = THROWN;
            } else if (weaknessAsString.equalsIgnoreCase("air")) {
                weaknessAsInt = AIR;
            } else if (weaknessAsString.equalsIgnoreCase("water")) {
                weaknessAsInt = WATER;
            } else if (weaknessAsString.equalsIgnoreCase("earth")) {
                weaknessAsInt = EARTH;
            } else if (weaknessAsString.equalsIgnoreCase("fire")) {
                weaknessAsInt = FIRE;
            }
            NPC_WEAKNESSES.put(weakness.getNpcId(), weaknessAsInt);
        }
        Logger.getGlobal().info("Loaded " + NPC_WEAKNESSES.size() + " NPC combat weaknesses.");
    }

    public static int getWeakness(int npcId) {
        return NPC_WEAKNESSES.get(npcId);
    }

    /*
    NPCWeaknesses.init();
        try (Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            NPCWeaknesses.WEAKNESSES.forEach((key, value) -> {
                try {
                    writer.write("\t{\n");
                    writer.write("\t\t\"npcId\": " + key + ",\n");
                    int weaknessAsInt = value;
                    String weaknessAsString = "STAB";
                    switch (weaknessAsInt) {
                        case SLASH: weaknessAsString = "SLASH";break;
                        case CRUSH: weaknessAsString = "CRUSH";break;
                        case ARROW: weaknessAsString = "ARROW";break;
                        case BOLT: weaknessAsString = "BOLT";break;
                        case THROWN: weaknessAsString = "THROWN";break;
                        case AIR: weaknessAsString = "AIR";break;
                        case WATER: weaknessAsString = "WATER";break;
                        case EARTH: weaknessAsString = "EARTH";break;
                        case FIRE: weaknessAsString = "FIRE";break;
                    }
                    writer.write("\t\t\"weakness\": \"" + weaknessAsString + "\"\n");
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
