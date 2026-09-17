package com.rs.utils.data.parsers.items;

import java.util.List;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData.Component;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ItemDisassembleDataParser {
    private static final Int2ObjectOpenHashMap<ItemDisassembleData> ITEMS_DISASSEMBLE_DATA = new Int2ObjectOpenHashMap<>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "items/itemsDisassembleData.json";

    public static void init() {
        loadItemsDisassembleData();
    }

    private static void loadItemsDisassembleData() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), ItemsDisassembleDataFileTemplate[].class);
        ItemsDisassembleDataFileTemplate[] itemsData = parser.getFileLoaded();
        for (ItemsDisassembleDataFileTemplate data : itemsData) {
            ITEMS_DISASSEMBLE_DATA.put(data.itemId, data.data);
        }
        Logger.getGlobal().info("Loaded " + ITEMS_DISASSEMBLE_DATA.size() + " items disassemble data.");
    }

    public static ItemDisassembleData getItemDisassembleData(Player player, int itemId) {
        if(player == null)
            return ITEMS_DISASSEMBLE_DATA.get(itemId);
        boolean ironman = player.isGroupIronman() || player.isIronMan() || player.isHCIronMan() || player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan();
        ItemDisassembleData data = ITEMS_DISASSEMBLE_DATA.get(itemId);
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        String name = defs.getName().toLowerCase();
        if (data != null && name.contains("(") && name.contains(")")) {
            String number = name.substring(name.indexOf("(")+1, name.indexOf(")"));
            int amount = number.length() > 1 || !Character.isDigit(number.charAt(0)) ? -1 : Integer.parseInt(number);
            if (amount != -1) {
                data = new ItemDisassembleData(data.getMaterialCount(), data.getRequiredQuantity(), data.getXp(), data.getJunkChance(), data.getComponentsCopy());
                data.setMaterialCount(data.getMaterialCount() * amount);
            }
        }
        if (data == null || !ironman)
            return data;
        data = new ItemDisassembleData(data.getMaterialCount(), data.getRequiredQuantity(), data.getXp(), data.getJunkChance(), data.getComponentsCopy());
        List<Component> comps = data.getComponentsCopyAsList();
        int extraMaterials = 0;
        if (name.contains("mind rune") || name.contains("body rune") || name.contains("air rune") || name.contains("fire rune") || name.contains("water rune") || name.contains("earth rune") || name.contains("chaos rune") || name.contains("law rune") || name.contains("death rune") || name.contains("blood rune")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.025));
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.03));
        }
        if (name.contains("mud rune") || name.contains("soul rune") || name.contains("cosmic rune") || name.contains("astral rune") || name.contains("nature rune")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.03));
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.04));
        }
        if ((name.contains("guam") || name.contains("tarromin") || name.contains("marrentill") || name.contains("harralander") || name.contains("fellstalk") || name.contains("ardrigal") || name.contains("rogue's purse") || name.contains("sito foil") || name.contains("snake weed") || name.contains("volencia moss")) && (name.contains("clean") || name.contains("grimy"))) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.02));
            if (name.contains("fellstalk"))
                extraMaterials = 3;
        }
        if ((name.contains("ranarr") || name.contains("toadflax") || name.contains("spirit weed") || name.contains("irit") || name.contains("wergali") || name.contains("avantoe") || name.contains("kwuarm") || name.contains("bloodweed") || name.contains("snapdragon")) && (name.contains("clean") || name.contains("grimy")))
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.03));
        if ((name.contains("cadantine") || name.contains("lantadyme") || name.contains("dwarf weed") || name.contains("torstol") || name.contains("arbuck")) && (name.contains("clean") || name.contains("grimy")))
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
        if (name.contains("gold ring") || name.contains("gold necklace") || name.contains("gold bracelet") || name.contains("gold amulet") || isGem(name, "sapphire") || isGem(name, "emerald") || isGem(name, "ruby"))
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.025));
        if (isGem(name, "opal") || isGem(name, "jade") || isGem(name, "red topaz"))
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.01));
        if (name.contains("sapphire ring") || name.contains("sapphire necklace") || name.contains("sapphire bracelet") || name.contains("sapphire amulet") || name.contains("emerald ring") || name.contains("emerald necklace") || name.contains("emerald bracelet") || name.contains("emerald amulet") || name.contains("ruby ring") || name.contains("ruby necklace") || name.contains("ruby bracelet") || name.contains("ruby amulet"))
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.03));
        if (name.contains("diamond ring") || name.contains("diamond necklace") || name.contains("diamond bracelet") || name.contains("diamond amulet") || isGem(name, "diamond")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.03));
            if (!isGem(name, "diamond"))
                extraMaterials = 2;
        }
        if (name.contains("dragonstone ring") || name.contains("dragonstone necklace") || name.contains("dragonstone bracelet") || name.contains("dragonstone amulet") || isGem(name, "dragonstone")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.03));
            if (!isGem(name, "dragonstone"))
                extraMaterials = 3;
        }
        if (name.contains("onyx ring") || name.contains("onyx necklace") || name.contains("onyx bracelet") || name.contains("onyx amulet") || isGem(name, "onyx")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            extraMaterials = isGem(name, "onyx") ? 3 : 4;
        }
        if (name.contains("hydrix ring") || name.contains("hydrix necklace") || name.contains("hydrix bracelet") || name.contains("hydrix amulet") || isGem(name, "hydrix")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.07));
            extraMaterials = isGem(name, "hydrix") ? 3 : 4;
        }
        if (name.contains("weapon poison") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("super restore") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("super hunter") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("sanfew serum") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("camouflage") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            if (name.contains("flask"))
                extraMaterials = 2;
        }

        if (name.contains("super defence") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("antipoison+") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("antifire") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.04));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("super divination") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("super ranging") && name.contains("("))
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
        if (name.contains("weapon poison+") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("super runecrafting") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("super magic") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("invention") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("stamina") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("zamorak brew") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("antipoison++") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("super cooking") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("extreme hunter") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("saradomin brew") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("weapon poison++") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("aggression") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("adrenaline") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            if (name.contains("flask"))
                extraMaterials = 2;
        }
        if (name.contains("super antifire") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("super adrenaline") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("super invention") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("extreme attack") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("extreme strength") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("extreme divination") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("extreme defence") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("extended super antifire") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("extreme magic") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("extreme runecrafting") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("extreme ranging") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 3 : 1;
        }
        if (name.contains("super saradomin brew") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("super zamorak brew") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("super guthix") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("super prayer") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("prayer renewal") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("extreme invention") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("harvest") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("overload") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("weapon poison+++") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("charming") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("charming") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("extreme cooking") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("extreme prayer") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = name.contains("flask") ? 4 : 2;
        }
        if (name.contains("grand strength") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 3;
        }
        if (name.contains("grand ranging") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 3;
        }
        if (name.contains("grand magic") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 3;
        }
        if (name.contains("grand attack") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 3;
        }
        if (name.contains("grand defence") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 3;
        }
        if (name.contains("super melee") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 3;
        }
        if (name.contains("super warmaster's") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 4;
        }
        if (name.contains("replenishment") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 4;
        }
        if (name.contains("wyrmfire") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 4;
        }
        if (name.contains("enhanced replenishment") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 4;
        }
        if (name.contains("extreme brawler's") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 4;
        }
        if (name.contains("extreme battlemage's") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 4;
        }
        if (name.contains("extreme sharpshooter's") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("extreme warmaster's") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("supreme strength") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("supreme attack") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("supreme defence") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("supreme magic") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("brightfire") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("holy overload") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("searing overload") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("overload salve") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("aggroverload") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("supreme overload") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("supreme overload salve") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("perfect plus") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("elder overload") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("elder overload salve") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }
        if (name.contains("spiritual prayer") && name.contains("(")) {
            addOrEdit(comps, new Component("Enhancing components", 28, 1, 0.05));
            extraMaterials = 5;
        }

        if (name.contains("wooden stock") || name.contains("bronze limbs") || name.contains("bronze crossbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
        }
        if (name.contains("willow stock") || name.contains("iron limbs") || name.contains("iron crossbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.04));
        }
        if (name.contains("teak stock") || name.contains("steel limbs") || name.contains("steel crossbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.04));
        }
        if (name.contains("maple stock") || name.contains("mithril limbs") || name.contains("mithril crossbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
            extraMaterials = 1;
        }
        if (name.contains("mahogany stock") || name.contains("adamant limbs") || name.contains("adamant crossbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.04));
            extraMaterials = 1;
        }
        if (name.contains("yew stock") || name.contains("runite limbs") || name.contains("rune crossbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.05));
            extraMaterials = 2;
        }
        if (name.equalsIgnoreCase("shortbow (u)") || name.equalsIgnoreCase("shortbow") || name.contains("oak shortbow"))
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
        if (name.contains("oak shortbow"))
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.04));
        if (name.contains("maple shortbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
            extraMaterials = 1;
        }
        if (name.contains("yew shortbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
            extraMaterials = 2;
        }
        if (name.contains("magic shortbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
            extraMaterials = 3;
        }
        if (name.contains("elder shortbow")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
            extraMaterials = 4;
        }
        if (name.contains("bronze mace") || name.contains("bronze longsword") || name.contains("bronze sword"))
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
        if (name.contains("iron mace") || name.contains("iron longsword") || name.contains("iron sword")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.03));
            extraMaterials = 1;
        }
        if (name.contains("steel mace") || name.contains("steel longsword") || name.contains("steel sword")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.04));
            extraMaterials = 1;
        }
        if (name.contains("mithril mace") || name.contains("mithril longsword") || name.contains("mithril sword")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.04));
            extraMaterials = 2;
        }
        if (name.contains("adamant mace") || name.contains("adamant longsword") || name.contains("adamant sword")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.04));
            extraMaterials = 3;
        }
        if (name.contains("rune mace") || name.contains("rune longsword") || name.contains("rune sword")) {
            addOrEdit(comps, new Component("Dextrous components", 37, 1, 0.05));
            extraMaterials = 3;
        }
        if (itemId == 1379)
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.04));
        if (name.contains("staff of air") || name.contains("staff of water") || name.contains("staff of earth") || name.contains("staff of fire")) {
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.05));
            extraMaterials = 1;
        }
        if (itemId == 1391)
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.05));
        if (name.contains("air battlestaff") || name.contains("water battlestaff") || name.contains("earth battlestaff") || name.contains("fire battlestaff")) {
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.06));
            extraMaterials = 1;
        }
        if (name.contains("mystic air staff") || name.contains("mystic water staff") || name.contains("mystic earth staff") || name.contains("mystic fire staff")) {
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.08));
            extraMaterials = 2;
        }
        if (name.contains("mind talismen") || name.contains("body talismen") || name.contains("air talismen") || name.contains("fire talismen") || name.contains("water talismen") || name.contains("earth talismen")

        ) {
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.03));
            extraMaterials = 1;
        }
        if (name.contains("chaos talismen") || name.contains("law talismen")) {
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.03));
            extraMaterials = 2;
        }
        if (name.contains("nature talismen") || name.contains("astral talismen") || name.contains("cosmic talismen") || name.contains("soul talismen") || name.contains("mud talismen") || name.contains("blood talismen")) {
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.04));
            extraMaterials = 2;
        }
        if (name.contains("death talismen")) {
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.04));
            extraMaterials = 3;
        }
        if (name.contains("blood talismen")) {
            addOrEdit(comps, new Component("Powerful components", 24, 1, 0.03));
            extraMaterials = 5;
        }
        if (name.contains("death rune"))
            extraMaterials = 2;
        if (name.contains("blood rune"))
            extraMaterials = 3;
        if (itemId == 1511)
            extraMaterials = 1;
        if (name.equalsIgnoreCase("oak logs") || name.equalsIgnoreCase("willow logs"))
            extraMaterials = 2;
        if (name.equalsIgnoreCase("maple logs") || name.equalsIgnoreCase("mahogany logs") || name.equalsIgnoreCase("yew logs"))
            extraMaterials = 3;
        if (name.equalsIgnoreCase("magic logs"))
            extraMaterials = 4;
        if (name.equalsIgnoreCase("elder logs"))
            extraMaterials = 5;
        if (name.equalsIgnoreCase("cow hide") || name.equalsIgnoreCase("soft leather") || name.equalsIgnoreCase("hard leather"))
            extraMaterials = 1;
        if (name.equalsIgnoreCase("green dragonhide") || name.equalsIgnoreCase("green dragon leather"))
            extraMaterials = 2;
        if (name.equalsIgnoreCase("blue dragonhide") || name.equalsIgnoreCase("green dragon leather"))
            extraMaterials = 3;
        if (name.equalsIgnoreCase("red dragonhide") || name.equalsIgnoreCase("green dragon leather"))
            extraMaterials = 4;
        if (name.equalsIgnoreCase("black dragonhide") || name.equalsIgnoreCase("green dragon leather"))
            extraMaterials = 5;
        if (name.equalsIgnoreCase("royal dragonhide") || name.equalsIgnoreCase("green dragon leather"))
            extraMaterials = 8;
        if (name.equalsIgnoreCase("bronze knife") || name.equalsIgnoreCase("bronze throwing axe") || name.contains("bronze dart") || name.equalsIgnoreCase("iron knife") || name.equalsIgnoreCase("iron throwing axe") || name.contains("iron dart")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.4));
            extraMaterials = 2;
        }
        if (name.equalsIgnoreCase("steel knife") || name.equalsIgnoreCase("steel throwing axe") || name.contains("steel dart")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.45));
            extraMaterials = 2;
        }
        if (name.equalsIgnoreCase("mithril knife") || name.equalsIgnoreCase("mithril throwing axe") || name.contains("mithril dart")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.45));
            extraMaterials = 4;
        }
        if (name.equalsIgnoreCase("adamant knife") || name.equalsIgnoreCase("adamant throwing axe") || name.contains("adamant dart")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.5));
            extraMaterials = 4;
        }
        if (name.equalsIgnoreCase("rune knife") || name.equalsIgnoreCase("rune throwing axe") || name.contains("rune dart")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.6));
            extraMaterials = 4;
        }
        if (name.equalsIgnoreCase("plank")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
            extraMaterials = 1;
        }
        if (name.equalsIgnoreCase("oak plank")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
            extraMaterials = 2;
        }
        if (name.equalsIgnoreCase("teak plank")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
            extraMaterials = 3;
        }
        if (name.equalsIgnoreCase("mahogany plank")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
            extraMaterials = 3;
        }
        if (name.equalsIgnoreCase("copper ore"))
            extraMaterials = 1;
        if (name.equalsIgnoreCase("tin ore"))
            extraMaterials = 1;
        if (name.equalsIgnoreCase("bronze bar"))
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
        if (name.equalsIgnoreCase("iron ore"))
            extraMaterials = 1;
        if (name.equalsIgnoreCase("iron bar")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
            extraMaterials = 2;
        }
        if (name.equalsIgnoreCase("coal"))
            extraMaterials = 2;
        if (name.equalsIgnoreCase("silver bar")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
            extraMaterials = 2;
        }
        if (name.equalsIgnoreCase("steel bar")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
            extraMaterials = 3;
        }
        if (name.equalsIgnoreCase("gold bar")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.8));
            extraMaterials = 3;
        }
        if (name.equalsIgnoreCase("mithril ore"))
            extraMaterials = 2;
        if (name.equalsIgnoreCase("mithril bar")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.85));
            extraMaterials = 2;
        }
        if (name.equalsIgnoreCase("adamantite ore")) {
            extraMaterials = 3;
        }
        if (name.equalsIgnoreCase("adamante bar")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.85));
            extraMaterials = 5;
        }
        if (name.equalsIgnoreCase("runite ore")) {
            extraMaterials = 4;
        }
        if (name.equalsIgnoreCase("rune bar")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.9));
            extraMaterials = 5;
        }
        if (name.equalsIgnoreCase("cannonball")) {
            addOrEdit(comps, new Component("Simple parts", 0, 1, 0.65));
            extraMaterials = 2;
        }
        data.setMaterialCount(data.getMaterialCount() + extraMaterials);
        data.setComponents(comps.stream().toArray(Component[]::new));
        return data;
    }

    public static boolean isGem(String name, String gemName) {
        return name.equalsIgnoreCase(gemName) || (name.contains(gemName) && name.contains("uncut"));
    }

    public static void addOrEdit(List<Component> comps, Component comp) {
        for (int i = 0; i < comps.size(); i++) {
            if (comps.get(i).getId() == comp.getId()) {
                comps.set(i, comp);
                return;
            }
        }
        comps.add(comp);
    }



    @AllArgsConstructor
    public static class ItemsDisassembleDataFileTemplate {
        @Getter
        private final int itemId;
        private final ItemDisassembleData data;
    }

}
