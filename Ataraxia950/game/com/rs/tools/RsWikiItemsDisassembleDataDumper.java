package com.rs.tools;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.external.api.json.JsonParser;
import com.rs.game.player.Equipment;
import com.rs.game.player.content.items.Defenders;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemDisassembleDataParser.ItemsDisassembleDataFileTemplate;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData.Component;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class RsWikiItemsDisassembleDataDumper {
    private static final Int2ObjectOpenHashMap<ItemsDisassembleDataFileTemplate> ITEMS_DISASSEMBLE_DATA = new Int2ObjectOpenHashMap<>();
    private static final String DEFINITIONS_FILE_PATH = "data/items/itemsDisassembleData.json";
    public static List<ItemsDisassembleDataFileTemplate> itemsDisassemblyData;

    static List<String> allParts;

    public static final void main(String[] args) throws IOException {
        System.out.println("Starting..");
        Cache.init();
        allParts = new ArrayList<String>();
        ClientScriptMap map = ClientScriptMap.getMap(10742);
        for (int i = 0; i < map.getSize(); i++) {
            InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
            String partName = (String) def.getDataInIndex(1);
            if (partName != null && !partName.equalsIgnoreCase("junk"))
                allParts.add(partName);
        }
        if (new File(DEFINITIONS_FILE_PATH).exists()) {
            JsonParser parser = new JsonParser(DEFINITIONS_FILE_PATH, ItemsDisassembleDataFileTemplate[].class);
            ItemsDisassembleDataFileTemplate[] definitions = parser.getFileLoaded();
            itemsDisassemblyData = new ArrayList<ItemsDisassembleDataFileTemplate>(Arrays.asList(definitions));
            for (ItemsDisassembleDataFileTemplate data : itemsDisassemblyData)
                ITEMS_DISASSEMBLE_DATA.put(data.getItemId(), data);
        } else {
            new File(DEFINITIONS_FILE_PATH).createNewFile();
            itemsDisassemblyData = new ArrayList<ItemsDisassembleDataFileTemplate>();
        }
        if (!itemsDisassemblyData.isEmpty()) {
            Collections.sort(itemsDisassemblyData, new Comparator<ItemsDisassembleDataFileTemplate>() {

                @Override
                public int compare(ItemsDisassembleDataFileTemplate o1, ItemsDisassembleDataFileTemplate o2) {
                    if (o1.getItemId() < o2.getItemId()) {
                        return -1;
                    } else if (o1.getItemId() > o2.getItemId()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }

            });
        }
        for (int itemId = 0; itemId <= Utils.getItemDefinitionsSize(); itemId++) {
            if (ITEMS_DISASSEMBLE_DATA.containsKey(itemId))
                continue;
            ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
            if (!defs.isNoted())
                if (!dumpDisassembleData(itemId))
                    System.out.println("FAILED ITEM: " + itemId + ", " + ItemDefinitions.getItemDefinitions(itemId).getName());
        }
        // dumpDisassembleData(15301);
    }

    public static final Gson GSON;

    static {
        GSON = new GsonBuilder().enableComplexMapKeySerialization().setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().setVersion(1.0).create();
    }

    public static void saveJsonFile(Object src, String dir, Type type) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(dir))) {
            writer.write(GSON.toJson(src, type));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean dumpDisassembleData(int itemId) {
        String pageName = ItemDefinitions.getItemDefinitions(itemId).getName();
        if (pageName == null || pageName.equals("null"))
            return false;

        pageName = pageName.replace("(p)", "");
        pageName = pageName.replace("(p+)", "");
        pageName = pageName.replace("(p++)", "");
        if (!pageName.contains("bones") && (pageName.contains("(1") || pageName.contains("(2") || pageName.contains("(3") || pageName.contains("(4") || pageName.contains("(5") || pageName.contains("(6")))
            pageName = pageName.replace(" (", "#(");
        pageName = pageName.replaceAll(" ", "_");
        try {
            WebPage page = new WebPage("https://runescape.wiki/w/" + pageName);
            try {
                page.load();
            } catch (Exception e) {
                System.out.println("Invalid page: " + itemId + ", " + pageName);
                return false;
            }
            double disassembleXp = -1;
            int reqQuantity = 0;
            int materialCount = 0;
            double junkChance = 0;

            List<Component> materials = new ArrayList<Component>();
            for (String line : page.getLines()) {
                if (line.contains("rsw-infobox-disassembly")) {
                    String newLine = line;
                    disassembleXp = Double.parseDouble(newLine.substring(newLine.indexOf("\">", newLine.indexOf("<td data-discalc-xp=")) + 2, newLine.indexOf("</td>", newLine.indexOf("<td data-discalc-xp="))));
                    reqQuantity = Integer.parseInt(newLine.substring(newLine.indexOf("\">", newLine.indexOf("data-discalc-iqty=")) + 2, newLine.indexOf("</td>", newLine.indexOf("data-discalc-iqty="))));
                    materialCount = Integer.parseInt(newLine.substring(newLine.indexOf("\">", newLine.indexOf("<td data-discalc-cqty=")) + 2, newLine.indexOf("</td>", newLine.indexOf("<td data-discalc-cqty="))));
                    junkChance = Double.parseDouble(newLine.substring(newLine.indexOf("\">", newLine.indexOf("<span class=\"rsw-discalc-junknum\"")) + 2, newLine.indexOf("</span>", newLine.indexOf("<span class=\"rsw-discalc-junknum\""))));
                }
                if (line.contains("Possible materials</th>")) {
                    line = line.substring(line.indexOf("Possible materials</th>"));
                    if (line.contains("Possible materials</th>")) {
                        String regularComponents = line.substring(line.indexOf("Possible materials</th>"));
                        String specialComponents = line.substring(line.indexOf("Possible materials</th>"));
                        while (specialComponents.contains("data-discalc-special-name=\"")) {
                            String currentComponent = specialComponents.substring(specialComponents.indexOf("data-discalc-special-name=\"") + 27, specialComponents.indexOf("\"", specialComponents.indexOf("data-discalc-special-name=\"") + 27));
                            int amount = Integer.parseInt(specialComponents.substring(specialComponents.indexOf("data-discalc-special-qty=\"") + 26, specialComponents.indexOf("\"", specialComponents.indexOf("data-discalc-special-qty=\"") + 26)));
                            specialComponents = specialComponents.substring(specialComponents.indexOf("data-discalc-special-name=\"") + 28);
                            materials.add(new Component(currentComponent, getPartIndex(currentComponent), amount, 1));
//                          System.out.println("special component:" + currentComponent + ", amount:" + amount);
                        }
                        while (regularComponents.contains("data-discalc-mat=\"")) {
                            String currentComponent = regularComponents.substring(regularComponents.indexOf("data-discalc-mat=\"") + 18, regularComponents.indexOf("\"", regularComponents.indexOf("data-discalc-mat=\"") + 18));
                            double chance = 0;
                            try {
                                chance = Double.parseDouble(regularComponents.substring(regularComponents.indexOf("data-discalc-chance-percent=\"", regularComponents.indexOf("data-discalc-mat=\"")) + 29, regularComponents.indexOf("\"", regularComponents.indexOf("data-discalc-chance-percent=\"", regularComponents.indexOf("data-discalc-mat=\"")) + 29))) / 100.00;
                            } catch (Exception e) {
                                int materialId = getPartIndex(currentComponent);
                                InventionDefinitions def = InventionDefinitions.getData(ClientScriptMap.getMap(10742).getIntValue(materialId));
                                int rarity = (int) def.getDataInIndex(7);
                                chance = rarity >= 3 ? 0.05 : rarity == 2 ? 0.15 : 0.35;
                            }
                            regularComponents = regularComponents.substring(regularComponents.indexOf("data-discalc-mat=\"") + 19);
//                            System.out.println("regular component:" + currentComponent + ", chance:" + chance);
                            materials.add(new Component(currentComponent, getPartIndex(currentComponent), 1, chance));
                        }
                    }
                }
            }
            if (disassembleXp == -1 || materials.isEmpty()) {
                System.out.println("not disassembleable ITEM: " + itemId + ", " + ItemDefinitions.getItemDefinitions(itemId).getName());
                return true;
            }
            itemsDisassemblyData.add(new ItemsDisassembleDataFileTemplate(itemId, new ItemDisassembleData(materialCount, reqQuantity, disassembleXp, junkChance, materials.toArray(new Component[materials.size()]))));
            Collections.sort(itemsDisassemblyData, new Comparator<ItemsDisassembleDataFileTemplate>() {

                @Override
                public int compare(ItemsDisassembleDataFileTemplate o1, ItemsDisassembleDataFileTemplate o2) {
                    if (o1.getItemId() < o2.getItemId()) {
                        return -1;
                    } else if (o1.getItemId() > o2.getItemId()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }

            });
            saveJsonFile(itemsDisassemblyData, DEFINITIONS_FILE_PATH, ArrayList.class);
            System.out.println("DUMPED ITEM : " + itemId + " , " + pageName + ", disassembleXp=" + disassembleXp);
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getPartIndex(String partName) {
        for (int i = 0; i < allParts.size(); i++) {
            if (allParts.get(i).equalsIgnoreCase(partName))
                return i;
        }
        return -1;
    }

}
