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
import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.external.api.json.JsonParser;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.pojos.TreasureHunterReward;

public class RsWikiTreasureHunterRewards {
    private static List<TreasureHunterReward> treasureHunterRewards;
    private static final String DEFINITIONS_FILE_PATH = "data/items/treasurehunterrewards.json";

    public static final void main(String[] args) throws IOException {
        System.out.println("Starting..");
        Cache.init();
        if (new File(DEFINITIONS_FILE_PATH).exists()) {
            JsonParser parser = new JsonParser(DEFINITIONS_FILE_PATH, TreasureHunterReward[].class);
            TreasureHunterReward[] itemsData = parser.getFileLoaded();
            treasureHunterRewards = new ArrayList<TreasureHunterReward>();
            for (TreasureHunterReward d : itemsData)
                treasureHunterRewards.add(d);
        } else {
            new File(DEFINITIONS_FILE_PATH).createNewFile();
            treasureHunterRewards = new ArrayList<TreasureHunterReward>();
        }
        dumpTreasureHunterRewards();
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

    public static boolean dumpTreasureHunterRewards() {
        try {
            WebPage page = new WebPage("https://runescape.wiki/w/Treasure_Hunter/Rewards?action=edit");
            try {
                page.load();
            } catch (Exception e) {
                System.out.println("Couldn't dump treasure hunter rewards");
                return false;
            }
            for (String line : page.getLines()) {
                line = line.replace("{{(m)}}", "");
                if (line.contains("{{DropsLineTH")) {
                    int bracketsIndex = line.indexOf("{{");
                    String dataLine = line.substring(bracketsIndex, line.indexOf("}}") + 2);
                    String dataLine2 = line.substring(line.indexOf("}}") + 2);
                    TreasureHunterReward reward = getTreasureHunterReward(dataLine.replace("|", ","));
                    if (reward != null)
                        treasureHunterRewards.add(reward);
                    else {
                        System.out.println("couldnt find data for :" + dataLine);
                    }
                    if (!dataLine2.equals("")) {
                        reward = getTreasureHunterReward(dataLine2.replace("|", ","));
                        if (reward != null)
                            treasureHunterRewards.add(reward);
                        else {
                            System.out.println("couldnt find data for :" + dataLine);
                        }
                    }
                }
            }
            saveJsonFile(treasureHunterRewards.stream().toArray(TreasureHunterReward[]::new), DEFINITIONS_FILE_PATH, TreasureHunterReward[].class);
            System.out.println("finished Dumping treasure hunter rewards.");
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static TreasureHunterReward getTreasureHunterReward(String dataLine) {
        String[] data = dataLine.split(",");
        int itemId = -1;
        int minAmount = -1;
        int maxAmount = -1;
        int rarity = -1;
        int convert = -1;
        String category = null;
        for (String d : data) {
            if (d.split("=")[0].equalsIgnoreCase("Name"))
                itemId = getItemId(d.split("=")[1]);
            if (d.split("=")[0].equalsIgnoreCase("Quantity")) {
                if (d.split("=")[1].contains("-")) {
                    minAmount = Integer.parseInt(d.split("=")[1].split("-")[0]);
                    maxAmount = Integer.parseInt(d.split("=")[1].split("-")[1]);
                } else
                    minAmount = maxAmount = Integer.parseInt(d.split("=")[1]);
            }
            if (d.split("=")[0].equalsIgnoreCase("Rarity")) {
                String rarityString = d.split("=")[1];
                rarity = rarityString.equalsIgnoreCase("Common") ? 0 : rarityString.equalsIgnoreCase("Fairly Common") ? 1 : rarityString.equalsIgnoreCase("Uncommon") ? 2 : rarityString.equalsIgnoreCase("Rare") ? 3 : rarityString.equalsIgnoreCase("Very Rare") ? 4 : -1;
            }
            if (d.split("=")[0].equalsIgnoreCase("convert"))
                convert = d.split("=").length == 1 ? 0 : d.split("=")[1].contains("-") ? Integer.parseInt(d.split("=")[1].split("-")[1]) : Integer.parseInt(d.split("=")[1]);
            if (d.split("=")[0].equalsIgnoreCase("category")) {
                category = d.split("=")[1].replace("}}", "");
                if (category.equalsIgnoreCase("Cosmetic"))
                    category = "Cosmetic items";
            }
        }
        if (itemId == -1 || minAmount == -1 || maxAmount == -1 || rarity == -1 || convert == -1 || category == null)
            return null;
        return new TreasureHunterReward(itemId, minAmount, maxAmount, rarity, convert, category);
    }

    public static boolean isIncorrectCategory(String category) {
        for (Object v : RS3ClientScriptMap.getMap(8519).getValues().values()) {
            if (((String) v).equalsIgnoreCase(category))
                return false;
        }
        return true;
    }

    public static int getItemId(String name) {
        if (name.equalsIgnoreCase("Ferocious ring"))
            return 15398;
        if (name.equalsIgnoreCase("Telescope (Treasure Hunter)"))
            return 40521;
        if (name.equalsIgnoreCase("Mask of the Automatons "))
            return 35283;
        if (name.equalsIgnoreCase("Coins"))
            return 995;
        name = name.replace(" (General Graardor)", "");
        name = name.replace(" (blue)", "");
        name = name.replace(" (black)", "");
        name = name.replace("Chefs hat", "Chef's hat");
        name = name.replace("(top)", "top");
        name = name.replace("(bottom)", "bottom");
        name = name.replace("Clue scroll{{!", "");
        name = name.replace(" (light)", "");
        name = name.replace(" (dark)", "");
        name = name.replace(" (empty)", "");
        name = name.replace("&amp;", "&");
        name = name.replace("Rune bolts", "Runite bolts");
        name = name.endsWith(" ") ? name : name;
        for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
            ItemDefinitions item = ItemDefinitions.getItemDefinitions(itemId);
            if (item.isNoted() || item.isLended())
                continue;
            if (item.getName().equalsIgnoreCase(name))
                return itemId;
        }
        return -1;
    }

}
