package com.rs.utils.data.parsers.items;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.game.item.Item;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.items.pojos.ItemWeight;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class ItemWeightsDataParser {
    private static final Object2DoubleOpenHashMap<String> ITEM_WEIGHTS = new Object2DoubleOpenHashMap<>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "items/itemWeights.json";
    private static final int[] NEGATIVE_WEIGHT_ITEMS = { 88, 10553, 10069, 10071, 24210, 24208, 24206, 14936, 14938,
            24560, 24561, 24562, 24563, 24564, 32342, 32343, 32344, 32345, 32346, 32347, 32348, 32349, 32350, 32351,
            32352, 32353, 32354, 32355, 32356, 32357, 32358, 32359, 32360, 32361 };

    public static void init() {
        loadItemWeights();
    }

    private static void loadItemWeights() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), ItemWeight[].class);
        ItemWeight[] weights = parser.getFileLoaded();
        for (ItemWeight weight : weights) {
            ITEM_WEIGHTS.put(weight.getItemName(), weight.getWeight());
        }
        Logger.getGlobal().info("Loaded " + ITEM_WEIGHTS.size() + " item weights.");
    }

    public static double getWeight(Item item, boolean equipped) {
        if (item.getDefinitions().isNoted()) {
            return 0;
        }
        double weight = ITEM_WEIGHTS.getOrDefault(item.getName(), 0);
        if (equipped) {
            for (int itemId : NEGATIVE_WEIGHT_ITEMS) {
                if (itemId == item.getId()) {
                    return -weight;
                }
            }
        }
        return weight;
    }

    // converter:
    /*
    ItemWeights.init();
        try (Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            ItemWeights.WEIGHTS.forEach((key, value) -> {
                try {
                    writer.write("\t{\n");
                    writer.write("\t\t\"itemName\": \"" + key + "\",\n");
                    writer.write("\t\t\"weight\": " + value + "\n");
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
