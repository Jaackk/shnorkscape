package com.rs.utils.data.parsers.items;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.game.item.Item;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.pojos.ItemExamine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ItemExaminesDataParser {
    private static final Object2ObjectOpenHashMap<String, String> ITEM_EXAMINES = new Object2ObjectOpenHashMap<>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "items/itemExamines.json";

    public static void init() {
        loadItemExamines();
    }

    private static void loadItemExamines() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), ItemExamine[].class);
        ItemExamine[] examines = parser.getFileLoaded();
        for (ItemExamine examine : examines) {
            ITEM_EXAMINES.put(examine.getItemName(), examine.getDescription());
        }
        Logger.getGlobal().info("Loaded " + ITEM_EXAMINES.size() + " item examines.");
    }

    public static String getExamine(Item item) {
        if (item.getId() == 995) {
            if (item.getAmount() > 99999) {
                return Utils.formatNumber(item.getAmount()) + " x " + item.getDefinitions().getName() + ".";
            }
            return "Lovely money!";
        }
        if (item.getAmount() >= 10000) {
            return Utils.formatNumber(item.getAmount()) + " x " + item.getDefinitions().getName() + ".";
        }
        if (item.getDefinitions().isNoted()) {
            return "Swap this note at any bank for the equivalent item.";
        }
        return getExamine(item.getName());
    }

    public static String getExamine(String itemName) {
        String examine = ITEM_EXAMINES.get(itemName);
        if (examine != null) {
            return examine;
        }
        return "It's an " + itemName + ".";
    }

    public static String getGEExamine(Item item) {
        if (item.getDefinitions().isNoted()) {
            item.setId(item.getDefinitions().getCertId());
        }
        return getExamine(item);
    }

    public static String getGEExamine(String name) {
        return getExamine(name);
    }

    // converter:
    /*
    ItemExamines.init();
        try (Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            ItemExamines.itemExamines.forEach((key, value) -> {
                try {
                    writer.write("\t{\n");
                    writer.write("\t\t\"itemName\": \"" + key + "\",\n");
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
