package com.rs.utils.data.parsers.misc;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.game.player.Player;
import com.rs.game.player.content.shops.ShopViewer;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.misc.pojos.ShopPojo;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Iterator;

public class ShopsDataParser {
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String SHOPS_FILE_PATH = "items/shops.json";
    private static final Int2ObjectOpenHashMap<ShopPojo> SHOPS = new Int2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<ShopPojo, ShopViewer> SHOP_POJO_TO_VIEWER = new Object2ObjectOpenHashMap<>();
    private static final Int2IntOpenHashMap SHOP_FOR_NPC_ID = new Int2IntOpenHashMap();
    private static final Int2IntOpenHashMap IRONMAN_SHOP_FOR_NPC_ID = new Int2IntOpenHashMap();

    public static void init() {
        loadShops();
    }

    public static void resetShops() {
        SHOPS.clear();
        SHOP_FOR_NPC_ID.clear();
        IRONMAN_SHOP_FOR_NPC_ID.clear();
        SHOP_POJO_TO_VIEWER.clear();
        init();
    }

    private static void loadShops() {
        JsonParser parser = new JsonParser(DataPaths.resolve(SHOPS_FILE_PATH), ShopPojo[].class);
        ShopPojo[] shops = parser.getFileLoaded();
        for (ShopPojo shop : shops) {
            ShopPojo ironManClone = (ShopPojo) shop.clone();
            ironManClone.setShopId(shop.getShopId() + 1000);
            SHOPS.put(shop.getShopId(), shop);
            SHOPS.put(ironManClone.getShopId(), ironManClone);
            SHOP_POJO_TO_VIEWER.put(shop, new ShopViewer(shop));
            SHOP_POJO_TO_VIEWER.put(ironManClone, new ShopViewer(ironManClone));
            for (int npcId : shop.getNpcIds()) {
                SHOP_FOR_NPC_ID.put(npcId, shop.getShopId());
                IRONMAN_SHOP_FOR_NPC_ID.put(npcId, ironManClone.getShopId());
            }
        }
        Logger.getGlobal().info("Loaded " + (SHOPS.size() / 2) + " shops.");
    }

    public static boolean openShopByNpc(Player player, int npcId) {
        int key;
        if (player.isHCIronMan() || player.isIronMan() ||
                player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan()) {
            key = IRONMAN_SHOP_FOR_NPC_ID.get(npcId);
        } else {
            key = SHOP_FOR_NPC_ID.get(npcId);
        }
        return openShop(player, key);
    }

    public static boolean openShop(Player player, int key) {
        player.stopAll(true, true);
        ShopViewer shopViewer = SHOP_POJO_TO_VIEWER.get(
                SHOPS.get((player.isIronMan() || player.isHCIronMan() ||
                        player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan()) ? (key + 1000) : key));

        if (player.isGroupIronman() && (key == 160 || key == 165 || key == 166 || key == 167)) {
            Dialogue.sendSingleNPCDialogue(player, 756, Dialogue.NORMAL, "Group Ironmen cannot access my shop, sorry!");
            return false;
        }
        if (player.isHCIronMan() && key < 40 && key != 27
                && key != 16 && key != 9 && key != 31
                && key != 32 && key != 14 && key != 29 && key != 168 && key != 1) {
            player.sendMessage("Hardcore ironmen cannot use this shop.");
            return false;
        }
        if (shopViewer == null) {
            return false;
        }
        shopViewer.addPlayer(player);
        return true;
    }

    public static void restoreShops() {
        ShopViewer shopViewer;
        for (Iterator<ShopPojo> iterator = SHOPS.values().iterator(); iterator.hasNext(); ) {
            shopViewer = SHOP_POJO_TO_VIEWER.get(iterator.next());
            if (shopViewer != null) {
                shopViewer.restoreItems();
            }
        }
    }

    /*public static void main(String[] args) throws IOException {
        int[][] auraShops = {{22889, 200}, {22895, 200},
                // Tier 1
                {22897, 200}, {20966, 200}, {22905, 200}, {22891, 200}, {22294, 200}, {23848, 200},
                {22296, 200}, {22280, 200}, {22300, 200}, {20958, 200}, {22284, 200}, {22893, 200},
                {22292, 200}, {20965, 200}, {20962, 200}, {22899, 200}, {20967, 200}, {20964, 200},
                {22927, 200}, {22298, 200}, {30784, 200},
                // Tier 2
                {22268, 400}, {22270, 400}, {22274, 400}, {22276, 400}, {22278, 400}, {22282, 400},
                {22286, 400}, {22290, 400}, {22885, 400}, {22901, 400}, {22907, 400}, {22929, 400},
                {23842, 400}, {23850, 400}, {22302, 400}, {22272, 400}, {30786, 400},
                // Tier 3
                {22887, 800}, {22903, 800}, {22909, 800}, {22911, 800}, {22917, 800}, {22919, 800},
                {22921, 800}, {22923, 800}, {22925, 800}, {22931, 800}, {22933, 800}, {23844, 800},
                {23852, 800}, {22913, 800}, {22915, 800}, {30788, 800},
                // Tier 4
                {23854, 1300}, {23856, 1300}, {23858, 1300}, {23860, 1300}, {23862, 1300}, {23864, 1300},
                {23866, 1300}, {23868, 1300}, {23870, 1300}, {23872, 1300}, {23874, 1300}, {23876, 1300},
                {23878, 1300}, {30790, 1300},
                // Tier 5
                {30792, 2600}, {30794, 2600}, {30796, 2600}, {30798, 2600}, {30800, 2600}, {30802, 2600},
                {30804, 2600},};

        Cache.init();
        init();
        try (Writer writer = new FileWriter("Output.json")) {

            for (int i = 53; i <= 57; i++) {
                writer.write("[\n");
                for (ShopItem item : SHOPS.get(i).getItems()) {
                    writer.write("\t\t\t{\n");
                    writer.write("\t\t\t\t\"id\": " + item.getId() + ",\n");
                    writer.write("\t\t\t\t\"amount\": " + item.getAmount() + ",\n");
                    for (int[] anItem : auraShops) {
                        if (anItem[0] == item.getId()) {
                            writer.write("\t\t\t\t\"price\": " + anItem[1] + "\n");
                            break;
                        }
                    }
                    writer.write("\t\t\t},\n");
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }*/

    /*public static void main(String[] args) throws IOException {
        Cache.init();
        ShopsHandler.init();
        try (Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            ShopsHandler.handledShops.forEach((key, value) -> {
                try {
                    if (value.getMoney() != -1) {
                        writer.write("\t{\n");
                        writer.write("\t\t\"shopId\": " + key + ",\n");
                        writer.write("\t\t\"name\": \"" + value.getName() + "\",\n");
                        writer.write("\t\t\"items\": [\n");
                        for (int i = 0; i < value.getMainStock().length; i++) {
                            writer.write("\t\t\t{\n");
                            writer.write("\t\t\t\t\"id\": " + value.getMainStock()[i].getId() + ",\n");
                            writer.write("\t\t\t\t\"amount\": " + value.getMainStock()[i].getAmount() + "\n");
                            if (i == value.getMainStock().length - 1) {
                                writer.write("\t\t\t}\n");
                            } else {
                                writer.write("\t\t\t},\n");
                            }
                        }
                        if (value.isGeneralStore()) {
                            writer.write("\t\t],\n");
                        } else {
                            writer.write("\t\t]\n");
                        }
                        if (value.isGeneralStore()) {
                            writer.write("\t\t\"isGeneralStore\": " + value.isGeneralStore() + "\n");
                        }
                        writer.write("\t},\n");

                    }
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
            writer.write("]");
        }
    }*/
}
