package com.rs.utils.data.parsers.npcs;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.utils.Logger;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class NPCCombatDefinitionsDataParser {
    private static final Int2ObjectOpenHashMap<NPCCombatDefinition> NPC_COMBAT_DEFINITIONS = new Int2ObjectOpenHashMap<>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "npcs/combatDefs.json";
    private static final NPCCombatDefinition DEFAULT_DEFINITION = new NPCCombatDefinition(1, -1,
            -1, -1, 5, 1, 33, 0,
            "MELEE", -1, -1, "PASSIVE");

    public static void init() {
        loadNPCCombatDefinitions();
    }

    public static Int2ObjectOpenHashMap<NPCCombatDefinition> getDefinitions() {
        return NPC_COMBAT_DEFINITIONS;
    }

    public static NPCCombatDefinition getNPCCombatDefinitions(int npcId) {
        NPCCombatDefinition def = NPC_COMBAT_DEFINITIONS.get(npcId);
        if (def == null)
            return DEFAULT_DEFINITION;
        return def;
    }

    private static void loadNPCCombatDefinitions() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), NPCCombatDefinitionsFileTemplate[].class);
        NPCCombatDefinitionsFileTemplate[] definitions = parser.getFileLoaded();
        for (NPCCombatDefinitionsFileTemplate definition : definitions) {
            NPC_COMBAT_DEFINITIONS.put(definition.npcId, definition.combatDefinition);
        }
        Logger.getGlobal().info("Loaded " + NPC_COMBAT_DEFINITIONS.size() + " NPC combat definitions.");
    }

    public static void resetNpcCombatDefinitions() {
        NPC_COMBAT_DEFINITIONS.clear();
        init();
    }

    private class NPCCombatDefinitionsFileTemplate {
        int npcId;
        NPCCombatDefinition combatDefinition;
    }

   /* public static void main(String[] args) {
        NPCCombatDefinitionsL.init();
        try (
                Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            NPCCombatDefinitionsL.npcCombatDefinitions.forEach((key, value) -> {
                try {
                    String attackStyle = "MELEE";
                    String aggressivenessType = "PASSIVE";
                    switch (value.getAttackStyle()) {
                        case NPCCombatDefinitionConstants.RANGE:
                            attackStyle = "RANGE";
                            break;
                        case NPCCombatDefinitionConstants.MAGE:
                            attackStyle = "MAGE";
                            break;
                        case NPCCombatDefinitionConstants.SPECIAL:
                            attackStyle = "SPECIAL";
                            break;
                        case NPCCombatDefinitionConstants.SPECIAL2:
                            attackStyle = "SPECIAL2";
                            break;
                    }
                    switch (value.getAgressivenessType()) {
                        case NPCCombatDefinitionConstants.AGRESSIVE:
                            aggressivenessType = "AGGRESSIVE";
                            break;
                    }
                    writer.write("\t{\n");
                    writer.write("\t\t\"npcId\": " + key + ",\n");
                    writer.write("\t\t\"combatDefinition\": {\n");
                    writer.write("\t\t\t\"hitpoints\": " + value.getHitpoints() + ",\n" +
                            "\t\t\t\"attackAnim\": " + value.getAttackEmote() + ",\n" +
                            "\t\t\t\"defenceAnim\": " + value.getDefenceEmote() + ",\n" +
                            "\t\t\t\"attackDelay\": " + value.getAttackDelay() + ",\n" +
                            "\t\t\t\"deathDelay\": " + value.getDeathDelay() + ",\n" +
                            "\t\t\t\"respawnDelay\": " + value.getRespawnDelay() + ",\n" +
                            "\t\t\t\"maxHit\": " + value.getMaxHit() + ",\n" +
                            "\t\t\t\"attackStyle\": \"" + attackStyle + "\",\n" +
                            "\t\t\t\"attackGfx\": " + value.getAttackGfx() + ",\n" +
                            "\t\t\t\"attackProjectile\": " + value.getAttackProjectile() + ",\n" +
                            "\t\t\t\"aggressivenessType\": \"" + aggressivenessType + "\"\n");
                    writer.write("\t\t}\n");
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
