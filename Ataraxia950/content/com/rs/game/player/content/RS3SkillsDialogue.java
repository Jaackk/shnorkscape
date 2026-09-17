package com.rs.game.player.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rs.Settings;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.InterfaceManager.MainInterfaceComponents;

public final class RS3SkillsDialogue {

    private static final Map<Integer, SkillDialogue> skillDialogues = new HashMap<Integer, SkillDialogue>();

    private static final int[] NO_MENU_CATEGORIES = { 6810, 6839, 6940, 6942, 6944, 6982, 7005, 7007, 7080, 7043, 7046, 7052, 7082, 7813, 9882, 10739, 10980,

            /* old ones */9468, 9467, 9056, 7053, 7050, 7048, 7049, 7046, 7047, 7070, 7069, 7065, 7064, 7063, 7077, 7078, 7103, 7102, 7096, 7093, 7092, 7106, 8076, 7104, 7105, 7556, 8002, 6921, 7512, 6972, 6973, 6974, 6975, 6969, 6970, 6971, 6979, 6977, 6976, 6980, 6986, 6990, 6989, 8858, 8856, 8857, 8854, 8855, 8853, 6808, 6840, 6830, 6831, 6829, 6868, 6898, 6674, 7278, 7277, 7276, 7275, 8697, 6770, 6771, 6768, 6769, 6774, 6775, 6772, 6773, 6778, 6779, 6776, 6777, 6762, 6761, 6767, 6766, 6765, 6764, 11357, 11358, 11359, 11360, 11361, 11362, 11363, 11364, 11365, 10741, 8083 };
    // 2222

    private static final int[] SKILL_DIALOGUES = { -1, 6761, 6809, 6838, 6939, 6941, 6943, 6977, 6981, 7004, 7006, 7042, 7051, 7079, 7081, 7812, 8796, 9883, 10738, 11359,

            /* old ones */6691, 966, 5773, 6675, 6780, 6784, 6789, 6794, 6803, 6809, 6821, 6823, 6832, 6838, 6848, 6852, 6853, 6861, 6869, 6877, 6879, 6894, 6896, 6919, 6939, 6932, 6933, 6934, 6935, 6936, 6937, 6938, 6941, 6943, 6945, 6981, 6987, 7004, 7006, 7008, 7010, 7012, 7042, 7043, 7044, 7045, 7051, 7057, 7059, 7061, 7079, 7081, 7094, 7113, 7551, 7800, 7812, 9471, 9473, 9474, 9475, 9476, 9477, 10739, 10740, 10738, 10741, 11385, 11463, 11357, 11358, 11359, 11360, 11361, 11362, 11363, 11364, 11365, 8083 };
    // 1168

    public static class SkillDialogue {

        private final int menuCSMapId;
        private final int menuNamesCSMapId;
        private final SkillCategory[] categories;

        private SkillDialogue(int id) {
            menuCSMapId = id;

            if (id == -1) {
                menuNamesCSMapId = -1;
                categories = new SkillCategory[NO_MENU_CATEGORIES.length];
                for (int i = 0; i < categories.length; i++)
                    categories[i] = new SkillCategory(NO_MENU_CATEGORIES[i]);
            } else {
                menuNamesCSMapId = id + (menuCSMapId == 6852 || menuCSMapId == 6853 || menuCSMapId == 7551 ? 2 : 1);
                RS3ClientScriptMap csmap = RS3ClientScriptMap.getMap(id);
                categories = new SkillCategory[csmap.getSize()];
                for (int i = 0; i < categories.length; i++)
                    categories[i] = new SkillCategory(csmap.getIntValue(i));
            }
        }

        public SkillCategory getCategory(int id) {
            for (SkillCategory c : categories)
                if (c.getItemsCSMapId() == id)
                    return c;
            return null;
        }

        public SkillCategory[] getCategorys() {
            return categories;
        }
    }

    public static final int SKILL_VAR = 1168, CATEGORY_VAR = 1169, PRODUCT_VAR = 1170, MAX_QUANTITY_VAR = 8846, CURRENT_QUANTITY_VAR = 8847;

    public static class SkillCategory {

        private int itemsCSMapId;
        private final int categorySpriteId;
        private int[] items;

        private SkillCategory(int id) {
            setItemsCSMapId(id);
            categorySpriteId = RS3ClientScriptMap.getMap(6817).getIntValue(id);
            RS3ClientScriptMap csmap = RS3ClientScriptMap.getMap(id);
            setItems(new int[csmap.getSize()]);
            for (int i = 0; i < getItems().length; i++)
                getItems()[i] = csmap.getIntValue(i);
        }

        public int[] getItems() {
            return items;
        }

        public void setItems(int[] items) {
            this.items = items;
        }

        public int getItemsCSMapId() {
            return itemsCSMapId;
        }

        public void setItemsCSMapId(int itemsCSMapId) {
            this.itemsCSMapId = itemsCSMapId;
        }
    }

    public static SkillDialogue getSkillDialogue(int id) {
        SkillDialogue d = skillDialogues.get(id);
        if (d == null)
            skillDialogues.put(id, d = new SkillDialogue(id));
        return d;
    }

    public enum CategoryTypes {

        COOKING(14145), CRAFTING(14149), FARMING(14153), FIREMAKING(14157), FLETCHING(14161), HERBLORE(14165), MAGIC(14169), RUNECRAFTING(14173), SMITHING(14177), SUMMOMING(14181), WOODCUTTING(14185), REWARDS(14193), WATER(14197), DIVINATION(20390), INVENTION(26562);

        private final int spriteId;

        CategoryTypes(int spriteId) {
            this.spriteId = spriteId;
        }
    }

    /*
     * not recommended since material repeats so it doesnt always get correctly
     */
    public static SkillDialogue findSkillDialogueByMaterial(int materialId, CategoryTypes type, int index, boolean skipNoMenu) {
        int idx = 0;
        for (int i = skipNoMenu ? 1 : 0; i < SKILL_DIALOGUES.length; i++) {
            SkillDialogue d = getSkillDialogue(SKILL_DIALOGUES[i]);
            SkillCategory c = getCategoryWithMaterial(d, materialId);
            if (c == null || (type != null && c.categorySpriteId != type.spriteId))
                continue;
            if (idx++ == index)
                return d;
        }
        return null;
    }

    /*
     * 100% safe. produce is only one for each dialogue
     */
    public static SkillDialogue findSkillDialogueByProduce(int produceId) {
        return findSkillDialogueByProduce(produceId, -1);
    }

    public static SkillDialogue findSkillDialogueByProduce(int produceId, CategoryTypes type, boolean skipNoMenu) {
        for (int i = skipNoMenu ? 1 : 0; i < SKILL_DIALOGUES.length; i++) {
            SkillDialogue d = getSkillDialogue(SKILL_DIALOGUES[i]);
            for (SkillCategory c : d.categories) {
                if (type != null && c.categorySpriteId != type.spriteId)
                    continue;
                for (int id : c.getItems())
                    if (produceId == id) {
                        if (Settings.DEBUG)
                            System.out.println("here you go " + i + ": " + d.menuCSMapId + ", " + c.itemsCSMapId);
                        return d;
                    }
            }
        }
        return null;
    }

    public static SkillDialogue findSkillDialogueByProduce(int produceId, int skip) {
        for (int i = 0; i < SKILL_DIALOGUES.length; i++) {
            if (skip != -1 && SKILL_DIALOGUES[i] == skip)
                continue;
            SkillDialogue d = getSkillDialogue(SKILL_DIALOGUES[i]);
            for (SkillCategory c : d.categories) {
                for (int id : c.getItems())
                    if (produceId == id) {
                        if (Settings.DEBUG)
                            System.out.println("here you go " + i + ": " + d.menuCSMapId + ", " + c.itemsCSMapId);
                        return d;
                    }
            }
        }
        return null;
    }

    private static SkillCategory getCategoryWithProduce(SkillDialogue sd, int produceId, CategoryTypes type) {
        for (SkillCategory sc : sd.categories) {
            if (type != null && sc.categorySpriteId != type.spriteId)
                continue;
            for (int id : sc.getItems()) {
                if (produceId == id)
                    return sc;
            }
        }
        return null;
    }

    private static SkillCategory getCategoryWithProduce(SkillDialogue sd, int produceId) {
        for (SkillCategory sc : sd.categories) {
            for (int id : sc.getItems()) {
                if (produceId == id)
                    return sc;
            }
        }
        return null;
    }

    public static void sendSkillDialogueByMaterial(Player player, int material, CategoryTypes type, int index, boolean skipNoMenu) {
        SkillDialogue d = findSkillDialogueByMaterial(material, type, index, skipNoMenu);
        if (d == null)
            return;
        sendSkillDialogue(player, d, getCategoryWithMaterial(d, material));
    }

    public static void sendSkillDialogueByProduce(Player player, int produceId) {
        sendSkillDialogueByProduce(player, produceId, -1);
    }

    public static void sendSkillDialogueByProduce(Player player, int produceId, int skip) {
        SkillDialogue d = findSkillDialogueByProduce(produceId, skip);
        if (d == null)
            return;
        SkillCategory sc = getCategoryWithProduce(d, produceId);
        sendSkillDialogue(player, d, sc, sc != null ? produceId : -1);
    }

    public static void sendSkillDialogueByProduce(Player player, int produceId, CategoryTypes type, boolean skipNoMenu) {
        SkillDialogue d = findSkillDialogueByProduce(produceId, type, skipNoMenu);
        if (d == null)
            return;
        SkillCategory sc = getCategoryWithProduce(d, produceId, type);
        sendSkillDialogue(player, d, sc, sc != null ? produceId : -1);
    }

    public static void sendItemProductionShell(Player player, int productId) {
        SkillDialogue d = getSkillDialogue(11385);
        SkillCategory sc = d.getCategory(8925);
        sendSkillDialogue(player, d, sc, productId);
    }

    public static void sendNoMenuSkillDialogue(Player player, int categoryMapId, int productId) {
        sendSkillDialogue(player, getSkillDialogue(-1), new SkillCategory(categoryMapId), productId);
    }

    public static void sendCustomSkillDialogue(Player player, int menuMapId, int menuNamesMapId, int categoryMapId, int productId) {
        sendSkillDialogue(player, menuMapId, menuNamesMapId, new SkillCategory(categoryMapId), productId);
    }

    public static void sendCustomSkillDialogueWithoutProduct(Player player, int menuMapId, int menuNamesMapId, int categoryMapId) {
        sendSkillDialogue(player, menuMapId, menuNamesMapId, new SkillCategory(categoryMapId), -1, false);
    }

    public static void sendGemCuttingDialogueByProduce(Player player, int produceId) {
        SkillDialogue d = getSkillDialogue(6981);
        if (d == null)
            return;
        sendSkillDialogue(player, d, getCategoryWithProduce(d, produceId));
    }

    // recommend using the otherControllers one for skillAction. this one is
    // manual
    public static void sendSkillDialogue(final Player player, SkillDialogue sd, SkillCategory sc) {
        sendSkillDialogue(player, sd, sc, -1);
    }

    public static void sendSkillDialogue(final Player player, SkillDialogue sd, SkillCategory sc, int result) {
        if (sc == null)
            sc = sd.categories[0];
        sendSkillDialogue(player, sd.menuCSMapId, sd.menuNamesCSMapId, sc, result);
    }

    private static void sendSkillDialogue(final Player player, int menuCSMapId, int menuNamesCSMapId, SkillCategory sc, int result) {
        sendSkillDialogue(player, menuCSMapId, menuNamesCSMapId, sc, result, true);
    }

    private static void sendSkillDialogue(final Player player, int menuCSMapId, int menuNamesCSMapId, SkillCategory sc, int result, boolean selectDefaultProduct) {
        if (sc == null)
            return;
        if (player.getInventionManager().hasChargePack()) {
            player.getVarBitManager().sendVarBit(30225, 1);
            player.getVarBitManager().sendVarBit(30224, 1);
        }
        player.getPackets().sendGlobalString(2390, "");
        player.getVarBitManager().sendVar(SKILL_VAR, menuCSMapId);
        if (selectDefaultProduct)
            setCategory(player, sc);
        else
            player.getVarBitManager().sendVar(CATEGORY_VAR, sc.getItemsCSMapId());
//        player.getVarBitManager().forcesendVarBit(3033, 1);
        player.getVarBitManager().sendVar(7881, menuNamesCSMapId);
//        System.out.println(sd.menuNamesCSMapId);
        player.getInterfaceManager().setInterface(true, 1370, 0, 1371);
        player.getInterfaceManager().sendInterface(1370);
        player.getPackets().sendIComponentSettings(1371, 22, 0, 500, 2);
        player.getPackets().sendIComponentSettings(1371, 20, 0, 1000, 2);
        player.getPackets().sendIComponentSettings(1371, 19, 0, 7, 2);
        if (result != -1)
            setProduct(player, result);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                 player.getPackets().sendExecuteScript(3689, 1370, InterfaceManager.getComponentUId(1477, MainInterfaceComponents.CENTRAL_INTERFACE.getComponentId()), 40);
                player.getVarBitManager().sendVar(SKILL_VAR, -1);
                player.getVarBitManager().sendVar(CATEGORY_VAR, -1);
                player.getVarBitManager().sendVar(PRODUCT_VAR, -1);
                player.getVarBitManager().sendVar(CURRENT_QUANTITY_VAR, 0);
            }
        });

    }

    private static SkillCategory getCategoryWithMaterial(SkillDialogue sd, int materialId) {
        for (SkillCategory sc : sd.categories) {
            for (int itemId : sc.getItems()) {
                Item[] actualRequiredItems = RS3SkillsDialogue.getActualRequiredItems(itemId);
                ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
                for (Item item : actualRequiredItems) {
                    if (item != null && item.getId() == materialId)
                        return sc;
                }
                if (defs.getCSOpcode(2655) == materialId)
                    return sc;
            }
        }
        return null;
    }

    public static void setCategoryByIndex(Player player, int index) {
        int skill = player.getVarBitManager().getValue(SKILL_VAR);
        if (skill == 0)
            return;
        SkillDialogue sd = getSkillDialogue(skill);
        if (sd == null || index >= sd.categories.length)
            return;
        setCategory(player, sd.categories[index]);
    }

    private static void setCategory(Player player, SkillCategory sc) {
        player.getVarBitManager().sendVar(CATEGORY_VAR, sc.getItemsCSMapId());
        int bestProduct = -1;
        l: for (int i = sc.getItems().length - 1; i >= 0; i--) { // best product
            // for lvl
            // with
            // material
            ItemDefinitions defs = ItemDefinitions.getItemDefinitions(sc.getItems()[i]);
            int skillId = RS3ClientScriptMap.getMap(681).getIntValue(defs.getCSOpcode(2640));
            int level = defs.getCSOpcode(2645);
            int multiplierQuantity = defs.getCSOpcode(2653);
            if (skillId != -1 && (player.getSkills().getLevel(skillId) >= level)) {
                for (int i2 = 0; i2 < 3; i2++) {
                    int material = defs.getCSOpcode(2655 + i2);
                    int amount = defs.getCSOpcode(2665 + i2);
                    if (material != 0 && !(player.getInventory().containsItem(material, amount * (multiplierQuantity == 0 ? 1 : multiplierQuantity))))
                        continue l;
                }
                bestProduct = i;
                break;
            }
        }
        if (bestProduct == -1) { // best product for lvl if no mat
            bestProduct = 0;
            for (int i = sc.getItems().length - 1; i >= 0; i--) {
                ItemDefinitions defs = ItemDefinitions.getItemDefinitions(sc.getItems()[i]);
                int skillId = RS3ClientScriptMap.getMap(681).getIntValue(defs.getCSOpcode(2640));
                int level = defs.getCSOpcode(2645);
                if (skillId == -1 || player.getSkills().getLevel(skillId) >= level) {
                    bestProduct = i;
                    break;
                }
            }
        }
        setProduct(player, sc.getItems()[bestProduct]);
    }

    public static void setProductByIndex(Player player, int index) {
        int skill = player.getVarBitManager().getValue(SKILL_VAR);
        if (skill == 0)
            return;
        SkillCategory sc = getSkillDialogue(skill).getCategory(player.getVarBitManager().getValue(CATEGORY_VAR));
        if (sc == null || index >= sc.getItems().length)
            return;
        setProduct(player, sc.getItems()[index]);
    }

    public static void setProduct(Player player, int result) {
        player.getVarBitManager().sendVar(PRODUCT_VAR, result);
        setMaxQuantity(player, result);
    }

    public static void setProduct(Player player, int result, int maxQuantity) {
        player.getVarBitManager().sendVar(PRODUCT_VAR, result);
        setMaxQuantity(player, result, maxQuantity);
    }

    public static void forceSetProduct(Player player, int result, int maxQuantity) {
        forceSendDialogueVar(player, PRODUCT_VAR, result);
        forceSendDialogueVar(player, MAX_QUANTITY_VAR, maxQuantity);
        forceSetCurrentQuantity(player, maxQuantity);
    }

    private static void forceSetCurrentQuantity(Player player, int quantity) {
        int max = player.getVarBitManager().getValue(MAX_QUANTITY_VAR);
        if (quantity > max)
            quantity = max;
        else if (quantity < 1)
            quantity = 1;
        forceSendDialogueVar(player, CURRENT_QUANTITY_VAR, quantity);
    }

    private static void forceSendDialogueVar(Player player, int varId, int value) {
        player.getVarBitManager().setVar(varId, value);
        player.getPackets().sendConfig(varId, value);
    }

    public static void setMaxQuantity(Player player, int result) {
        setMaxQuantity(player, result, -1);
    }

    public static void setMaxQuantity(Player player, int result, int maxQuantity) {
        if (maxQuantity != -1) {
            player.getVarBitManager().sendVar(MAX_QUANTITY_VAR, maxQuantity);
            setCurrentQuantity(player, maxQuantity);
            return;
        }
        if (maxQuantity == -1 && result != 36375) {
            maxQuantity = result == 36389 && player.getInventionManager().hasChargePack() ? 0 : result == 36389 ? 1 : RS3SkillsDialogue.script_6504(player, result);
            player.getVarBitManager().sendVar(MAX_QUANTITY_VAR, maxQuantity);
        } else {
            if (maxQuantity == -1)
                maxQuantity = player.getVarBitManager().getValue(MAX_QUANTITY_VAR);
            else
                player.getVarBitManager().sendVar(MAX_QUANTITY_VAR, maxQuantity);
        }
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(result);
        int skillId = ClientScriptMap.getMap(681).getIntValue(defs.getCSOpcode(2640));
        if (skillId != Skills.INVENTION) {
            maxQuantity = RS3SkillsDialogue.getMaxCreateAmount(player, result);
            player.getVarBitManager().sendVar(MAX_QUANTITY_VAR, maxQuantity);
        }
        setCurrentQuantity(player, maxQuantity);
    }

    public static void setCurrentQuantity(Player player, boolean increase) {
        setCurrentQuantity(player, player.getVarBitManager().getValue(CURRENT_QUANTITY_VAR) + (increase ? 1 : -1));
    }

    public static void setCurrentQuantity(Player player, int quantity) {
        setCurrentQuantity(player, quantity, false);
    }

    public static void setCurrentQuantity(Player player, int quantity, boolean sendInterface) {
        if (sendInterface) {
            SkillDialogueResult result = new SkillDialogueResult(player.getVarBitManager().getValue(PRODUCT_VAR), player.getVarBitManager().getValue(CURRENT_QUANTITY_VAR));
            RS3SkillsDialogue.setProduct(player, result.getProduce());
            setMaxQuantity(player, result.getProduce());
        }

        int max = player.getVarBitManager().getValue(MAX_QUANTITY_VAR);
        if (quantity > max)
            quantity = max;
        else if (quantity < 1)
            quantity = 1;
        player.getVarBitManager().sendVar(CURRENT_QUANTITY_VAR, quantity);
    }

    public static class SkillDialogueResult {

        private final int productId;
        private final int quantity;

        private SkillDialogueResult(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public int getProduce() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static SkillDialogueResult getResult(Player player, boolean closeInterfaces) {
        SkillDialogueResult result = new SkillDialogueResult(player.getVarBitManager().getValue(PRODUCT_VAR), player.getVarBitManager().getValue(CURRENT_QUANTITY_VAR));
        if (closeInterfaces)
            player.getInterfaceManager().closeScreenInterface();
        return result;
    }

    public static int CONTINUE_OPTION = 26, CHOOSE_AMOUNT_OPTION = -1;

    public static final int REQUIRED_ITEMS_STRUCT_OP_START = 2675, STRUCT_ITEM_IDS_START = 2655, STRUCT_ITEM_AMOUNTS_START = 2665, STRUCT_ITEM_IN_BANK_BOX_INDEX = 5456;

    public static Item[][] getRequiredItemsForItem(int itemId) {
        List<Item[]> requiredItems = new ArrayList<Item[]>();
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        for (int opcode = REQUIRED_ITEMS_STRUCT_OP_START; opcode <= REQUIRED_ITEMS_STRUCT_OP_START + 9; opcode++) {
            if (defs.getCSOpcode(opcode) == 0)
                continue;
            RS3GeneralRequirementMap map = RS3GeneralRequirementMap.getMap(defs.getCSOpcode(opcode));
            if (map == null || map.getValues() == null)
                continue;
            List<Item> items = new ArrayList<Item>();
            for (int i = 0; i < 10; i++) {
                int checkItemId = map.getIntValue((long) STRUCT_ITEM_IDS_START + i);
                int checkAmount = map.getIntValue((long) STRUCT_ITEM_AMOUNTS_START + i);
                if (checkItemId == 0 || checkAmount == 0)
                    continue;
                items.add(new Item(checkItemId, checkAmount));

            }
            if (!items.isEmpty())
                requiredItems.add(items.toArray(new Item[items.size()]));
        }
        for (int i = 0; i < 10; i++) {
            int checkItemId = defs.getCSOpcode(STRUCT_ITEM_IDS_START + i);
            int checkAmount = defs.getCSOpcode(STRUCT_ITEM_AMOUNTS_START + i);
            if (checkItemId == 0 || checkAmount == 0)
                continue;
            requiredItems.add(new Item[] { new Item(checkItemId, checkAmount) });
        }
        return requiredItems.toArray(new Item[requiredItems.size()][]);
    }

    public static Item[] getActualRequiredItems(int itemId) {
        return getActualRequiredItems(itemId, null);
    }

    public static Item[] getActualRequiredItems(int itemId, Item[][] requiredItems) {
        if (requiredItems == null)
            requiredItems = getRequiredItemsForItem(itemId);
        Item[] actualRequiredItems = new Item[requiredItems.length];
        for (int i = 0; i < requiredItems.length; i++) {
            for (int j = 0; j < requiredItems[i].length; j++) {
                Item item = requiredItems[i][j];
                if (item == null)
                    continue;
                actualRequiredItems[i] = requiredItems[i][j];
                break;
            }
        }
        return actualRequiredItems;
    }

    public static boolean hasRequirement(Player player, int productId) {
        return hasRequirement(player, productId, false);
    }
    //script_7106 for requirement type
    public static boolean hasRequirement(Player player, int productId, boolean sendMessage) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(productId);
        int requirementIdStart = defs.getCSOpcode(2640);
        int requirementAmount = defs.getCSOpcode(2645);
        boolean unknownboolean = defs.getCSOpcode(317) == 1 || defs.getCSOpcode(7801) > 0;
        int count = 1;
        List<String> messages = new ArrayList<String>();
        while (requirementIdStart > 0) {
            String msg = getRequirementMessageFor(player, productId, requirementIdStart, requirementAmount, unknownboolean, defs.getCSOpcode(3649), count);
            if (msg != null) {
                messages.add(msg);
            }
            if (count == 5)
                break;
            if (count >= 1) {
                requirementIdStart = defs.getCSOpcode(2641 + (count - 1));
                requirementAmount = defs.getCSOpcode(2646 + (count - 1));
            }
            count++;
        }
        if (!messages.isEmpty() && sendMessage) {
            if (messages.size() == 1)
                player.getPackets().sendGameMessage(messages.get(0));
            else {
                player.getPackets().sendGameMessage("You need the following:");
                for (String msg : messages)
                    player.getPackets().sendGameMessage(msg);
            }
        }
        return messages.isEmpty();
    }

    private static String getRequirementMessageFor(Player player, int productId, int requirementIdStart, int requirementAmount, boolean unknownboolean, int arg3, int arg4) {
        if (requirementIdStart > 0 && requirementIdStart < 61) {// skills loop
            int skillId = ClientScriptMap.getMap(681).getIntValue(requirementIdStart);
            if ((arg3 == 1 && player.getSkills().getLevel(skillId) < requirementAmount) || (arg3 == 0 && player.getSkills().getLevelForXp(skillId) < requirementAmount))
                return "You need at least level " + requirementAmount + " " + Skills.SKILL_NAME[skillId] + " to create " + ItemDefinitions.getItemDefinitions(productId).getName() + ".";
            return null;
        }
        if (requirementIdStart == 63) {
            int index = InventionDefinitions.getBluePrintIndex(requirementAmount);
            int dataId = ClientScriptMap.getMap(10743).getIntValue(index);
            if (!player.getInventionManager().hasDiscoveredBluePrint(index))
                return "You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can create " + ItemDefinitions.getItemDefinitions(productId).getName() + ".";
        }
        return null;
    }

    public static boolean hasRequiredTool(Player player, int productId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(productId);
        int requirementIdStart = defs.getCSOpcode(2650);
        int count = 1;
        while (requirementIdStart > 0) {
            if (!hasTool(player, requirementIdStart))
                return false;
            if (count == 3)
                break;
            if (count >= 1)
                requirementIdStart = defs.getCSOpcode(2651 + (count - 1));
            count++;
        }
        return true;
    }

    private static boolean hasTool(Player player, int requirementIdStart) {
        if (player.getEquipment().getWeaponId() != -1)
            switch (requirementIdStart) {
            case 32633:
            case 32634:
                if (ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getCSOpcode(4663) == 1)
                    return true;
                break;
            case 32635:
            case 32636:
                if (ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getCSOpcode(4662) == 1) {
                    return true;
                }
                break;
            case 32637:
            case 32638:
                if (ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getCSOpcode(4660) == 1) {
                    return true;
                }
                break;
            case 32640:
            case 32641:
                if (ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getCSOpcode(4659) == 1) {
                    return true;
                }
                break;
            case 32642:
            case 32643:
                if (ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getCSOpcode(4657) == 1) {
                    return true;
                }
                break;
            case 35719:
            case 35720:
                if (ItemDefinitions.getItemDefinitions(player.getEquipment().getWeaponId()).getCSOpcode(5322) == 1) {
                    return true;
                }
                break;
            }
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(requirementIdStart);
        if (defs.isWearItem() && player.getEquipment().getItem(defs.getEquipSlot()) != null && player.getEquipment().getItem(defs.getEquipSlot()).getId() == requirementIdStart)
            return true;
        return player.getInventory().containsItem(requirementIdStart, 1);
    }

    public static int script_6504(Player player, int item0) {
        int int1;
        int item2;
        int item3;
        int int4;
        int attrmap5;
        boolean boolean6;
        int item7;
        int int8;
        int int9;
        int int10;
        int int11;
        int int12;
        int int13;
        int item14;
        int int15;
        boolean boolean16;
        int int17;
        if (item0 < 0) {
            return 0;
        }
        if (!hasRequiredTool(player, item0)) {
            return 0;
        }
        int1 = 0;
        ItemDefinitions defs0 = ItemDefinitions.getItemDefinitions(item0);
        item2 = defs0.getCSOpcode(2655);
        item3 = defs0.getCSOpcode(2665);
        int4 = defs0.getCSOpcode(5456);
        attrmap5 = defs0.getCSOpcode(2675);
        boolean6 = defs0.getCSOpcode(2686) == 1;
        item7 = defs0.itemCategory == 3027 ? 3 : defs0.itemCategory == 62 ? 10 : defs0.getCSOpcode(2995);
        if (item7 == 0) {
            item7 = 60;
        }
        int8 = 1;
        int9 = 0;
        int10 = 0;
        int11 = defs0.getCSOpcode(2653) != 0 ? defs0.getCSOpcode(2653) : 1;
        int12 = 1;
        int13 = 0;
        item14 = 0;
        while (int8 <= 10) {
            int[] data = script_6500(item0, int8);
            item2 = data[0];
            int4 = data[1];
            item3 = data[2];
            attrmap5 = data[3];
            boolean6 = data[4] == 1;
            if (attrmap5 != 0) {
                int[] data2 = script_6503(player, attrmap5, int11, item7, int1);
                int10 = data2[0];
                int9 = data2[1];
                item7 = Math.min(item7, int10);
                if (int9 == 0) {
                    int12 = 0;
                    int13 = int13 - 1;
                }
            } else if (item2 >= 0 && item3 > 0) {
                item14 = script_6494(player, item0, item3, item2, int4, int1);
                if (!ItemDefinitions.getItemDefinitions(item2).isStackable()) {
                    int12 = 0;
                    int13 = int13 - 1;
                }
                if (item14 >= 0) {
                    if (!boolean6) {
                        item7 = Math.min(item7, (item14 + (item3 * int11 - 1)) / (item3 * int11));
                    } else {
                        item7 = Math.min(item7, item14 / item3);
                    }
                }
            } else {
                int8 = 11;
            }
            if (item7 == 0) {
                return 0;
            }
            int8 = int8 + 1;
        }
        int15 = defs0.getCSOpcode(7764);
        boolean16 = defs0.isStackable();
        if (int15 == -1 && RS3ClientScriptMap.getMap(15093).getValues().containsValue(item0)) {
            if (player.getEquipment().getGlovesId() == 776) {
                int15 = 0;
            } else {
                int15 = 1;
            }
        }
        if (int15 <= 0) {
            return item7;
        }
        int17 = int15 + int13;
        if (int17 <= 0) {
            if (int15 <= 0) {
                return item7;
            }
            return Math.min(28, item7);
        }
        if (int17 > 0) {
            if (boolean16) {
                if (player.getInventory().getFreeSlots() >= int15) {
                    return item7;
                }
                return 0;
            }
            if (int12 == 1) {
                return Math.min(item7, player.getInventory().getFreeSlots());
            }
            return Math.min(item7, ((28 / int17)));
        }
        return 0;
    }

    public static int[] script_6500(int item0, int arg1) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item0);
        return arg1 == 0 ? new int[5] : new int[] { defs.getCSOpcode(2655 + (arg1 - 1)), defs.getCSOpcode(5456 + (arg1 - 1)), defs.getCSOpcode(2665 + (arg1 - 1)), defs.getCSOpcode(2675 + (arg1 - 1)), defs.getCSOpcode(2686 + (arg1 - 1)) };
    }

    public static int script_6501(int attrmap0) {
        int int1;
        int1 = 1;
        if (getrs3AttributeMapValue(attrmap0, 2686) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2665);
        }
        if (getrs3AttributeMapValue(attrmap0, 2687) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2666);
        }
        if (getrs3AttributeMapValue(attrmap0, 2688) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2667);
        }
        if (getrs3AttributeMapValue(attrmap0, 2689) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2668);
        }
        if (getrs3AttributeMapValue(attrmap0, 2690) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2669);
        }
        if (getrs3AttributeMapValue(attrmap0, 2691) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2670);
        }
        if (getrs3AttributeMapValue(attrmap0, 2692) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2671);
        }
        if (getrs3AttributeMapValue(attrmap0, 2693) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2672);
        }
        if (getrs3AttributeMapValue(attrmap0, 2694) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2673);
        }
        if (getrs3AttributeMapValue(attrmap0, 2695) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 2674);
        }
        if (getrs3AttributeMapValue(attrmap0, 5476) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 5471);
        }
        if (getrs3AttributeMapValue(attrmap0, 5477) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 5472);
        }
        if (getrs3AttributeMapValue(attrmap0, 5478) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 5473);
        }
        if (getrs3AttributeMapValue(attrmap0, 5479) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 5474);
        }
        if (getrs3AttributeMapValue(attrmap0, 5480) == 0) {
            int1 = int1 * getrs3AttributeMapValue(attrmap0, 5475);
        }
        return int1;
    }

    private static int getrs3AttributeMapValue(int attrmap0, int i) {
        return RS3GeneralRequirementMap.getMap(attrmap0).getIntValue(i);
    }

    public static int script_6494(Player player, int item0, int item1, int item2, int arg3, int arg4) {
        int int6;
        int item7;
        int6 = 0;
        item7 = 0;
        switch (item2) {
        case 17792:
        case 17793:
        case 554:
        case 555:
        case 556:
        case 557:
        case 558:
        case 559:
        case 560:
        case 561:
        case 562:
        case 563:
        case 564:
        case 565:
        case 566:
        case 9075:
        case 17780:
        case 17781:
        case 17782:
        case 17783:
        case 17784:
        case 17785:
        case 17786:
        case 17787:
        case 17788:
        case 17789:
        case 17790:
        case 17791:
            item7 = script_6488(player, item2, ItemDefinitions.getItemDefinitions(item0).getCSOpcode(7173));
            break;
        case 5331:
        case 5333:
        case 5334:
        case 5335:
        case 5336:
        case 5337:
        case 5338:
        case 5339:
        case 5340:
            if (hasRequiredTool(player, 18682)) {
                item7 = 0;
            } else {
                item7 = ((8 * getItemAmtInContainer(player, 93, 5340) + 7 * getItemAmtInContainer(player, 93, 5339) + 6 * getItemAmtInContainer(player, 93, 5338) + 5 * getItemAmtInContainer(player, 93, 5337) + 4 * getItemAmtInContainer(player, 93, 5336) + 3 * getItemAmtInContainer(player, 93, 5335) + 2 * getItemAmtInContainer(player, 93, 5334) + getItemAmtInContainer(player, 93, 5333)));
            }
            break;
        case 3430:
        case 3432:
        case 3434:
        case 3436:
            item7 = ((getItemAmtInContainer(player, 93, 3436) + getItemAmtInContainer(player, 93, 3434) * 2 + getItemAmtInContainer(player, 93, 3432) * 3 + getItemAmtInContainer(player, 93, 3430) * 4));
            break;
        case 995:
            if (2147483647 - getItemAmtInContainer(player, 623, 995) - getItemAmtInContainer(player, 93, 995) < 0) {
                item7 = 2147483647;
            } else {
                item7 = Math.min(2147483647, getItemAmtInContainer(player, 623, 995) + getItemAmtInContainer(player, 93, 995));
            }
            break;
        case 6797:
            item7 = ((getItemAmtInContainer(player, 93, 5339) + getItemAmtInContainer(player, 93, 5338) + getItemAmtInContainer(player, 93, 5337) + getItemAmtInContainer(player, 93, 5336) + getItemAmtInContainer(player, 93, 5335) + getItemAmtInContainer(player, 93, 5334) + getItemAmtInContainer(player, 93, 5333) + getItemAmtInContainer(player, 93, 5331)));
            break;
        case 1825:
        case 1827:
        case 1829:
        case 1831:
            item7 = ((getItemAmtInContainer(player, 93, 1831) + getItemAmtInContainer(player, 93, 1829) + getItemAmtInContainer(player, 93, 1827) + getItemAmtInContainer(player, 93, 1825)));
            break;
        case 2169:
            if (item0 != 0 && getItemAmtInContainer(player, 93, 2169) > 0) {
                item7 = 0;
            } else if (arg4 == 1) {
                item7 = ((getItemAmtInContainer(player, 93, item2) + getItemAmtInContainer(player, 530, item2)));
            } else {
                item7 = getItemAmtInContainer(player, 93, item2);
            }
            break;
        case 36365:
            item7 = player.getInventionManager().getMaterials()[InventionDefinitions.getMaterialIndex(arg3)];
            break;
        case 37411:
            item7 = player.getDungeoneeringTokens();
            break;
        case 960:
        case 8778:
        case 8780:
        case 8782:
            item7 = ((getItemAmtInContainer(player, 93, item2) + getItemAmtInContainer(player, 93, 30037)));
            if (arg4 == 1) {
                item7 = item7 + (getItemAmtInContainer(player, 530, item2) + getItemAmtInContainer(player, 530, 30037));
            }
            break;
        default:
            if (ItemDefinitions.getItemDefinitions(item2).getCSOpcode(5774) == 1) {
                if (ItemDefinitions.getItemDefinitions(item2).getCSOpcode(5772) == 1) {
                    item7 = 0;
                } else {
                    item7 = player.getInventory().containsItem(item2, 1) ? 1 : 0;
                }
            } else if (arg4 == 1) {
                item7 = ((getItemAmtInContainer(player, 93, item2) + getItemAmtInContainer(player, 530, item2)));
            } else {
                item7 = getItemAmtInContainer(player, 93, item2);
            }
            break;
        }
        if (item0 != 0 && item7 != 0) {
            int6 = 0;// script_6493(item0, item2, item1);
        }
        return item7 + int6;
    }

    public static int script_6488(Player player, int item0, int arg1) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item0);
        if (defs.itemCategory != 149) {
            return 0;
        }
        switch (item0) {
        case 17780:
            for (Item item : player.getEquipment().getItems().getItems()) {
                if (item != null && item.getDefinitions().getCSOpcode(972) > 0)
                    return -1;
            }
            return getItemAmtInContainer(player, 93, 17780) + getItemAmtInContainer(player, 93, 16091);
        case 17781:
            for (Item item : player.getEquipment().getItems().getItems()) {
                if (item != null && item.getDefinitions().getCSOpcode(973) > 0)
                    return -1;
            }
            return getItemAmtInContainer(player, 93, 17781) + getItemAmtInContainer(player, 93, 16092);
        case 17783:
            for (Item item : player.getEquipment().getItems().getItems()) {
                if (item != null && item.getDefinitions().getCSOpcode(975) > 0)
                    return -1;
            }
            return getItemAmtInContainer(player, 93, 17783) + getItemAmtInContainer(player, 93, 16094);
        case 17782:
            for (Item item : player.getEquipment().getItems().getItems()) {
                if (item != null && item.getDefinitions().getCSOpcode(974) > 0)
                    return -1;
            }
            return getItemAmtInContainer(player, 93, 17782) + getItemAmtInContainer(player, 93, 16093);
        case 17784:
            return getItemAmtInContainer(player, 93, 17784) + getItemAmtInContainer(player, 93, 16095);
        case 17788:
            return getItemAmtInContainer(player, 93, 17788) + getItemAmtInContainer(player, 93, 16099);
        case 17785:
            return getItemAmtInContainer(player, 93, 17785) + getItemAmtInContainer(player, 93, 16096);
        case 17786:
            return getItemAmtInContainer(player, 93, 17786) + getItemAmtInContainer(player, 93, 16097);
        case 17787:
            return getItemAmtInContainer(player, 93, 17787) + getItemAmtInContainer(player, 93, 16098);
        case 17793:
            return getItemAmtInContainer(player, 93, 17793) + getItemAmtInContainer(player, 93, 16104);
        case 17789:
            return getItemAmtInContainer(player, 93, 17789) + getItemAmtInContainer(player, 93, 16100);
        case 17791:
            return getItemAmtInContainer(player, 93, 17791) + getItemAmtInContainer(player, 93, 16102);
        case 17790:
            return getItemAmtInContainer(player, 93, 17790) + getItemAmtInContainer(player, 93, 16101);
        case 17792:
            return getItemAmtInContainer(player, 93, 17792) + getItemAmtInContainer(player, 93, 16103);
        }
        return getItemAmtInContainer(player, 93, item0);
    }

    private static int getItemAmtInContainer(Player player, int key, int itemId) {
        return key == 93 ? player.getInventory().getAmountOf(itemId) : key == 94 ? player.getEquipment().getAmountOf(itemId) : 0;
    }

    public static int[] script_6503(Player player, int attrmap0, int arg1, int arg2, int arg3) {
        int item4;
        int int5;
        int int6;
        boolean boolean7;
        int item8;
        int item9;
        int int10;
        int int11;
        int int12;
        int int13;
        int int14;
        if (attrmap0 == 0) {
            return new int[2];
        }
        item4 = getrs3AttributeMapValue(attrmap0, 2655);
        int5 = getrs3AttributeMapValue(attrmap0, 2665);
        int6 = getrs3AttributeMapValue(attrmap0, 5456);
        boolean7 = getrs3AttributeMapValue(attrmap0, 2686) == 1;
        item8 = 0;
        item9 = 0;
        int10 = 1;
        int11 = script_6501(attrmap0);
        int12 = 0;
        int13 = 0;
        int14 = 0;
        while (item4 >= 0) {
            if (int5 != 0) {
                item9 = script_6494(player, -1, -1, item4, int6, arg3);
                if (ItemDefinitions.getItemDefinitions(item4).isStackable() && item9 != 0) {
                    int14 = 1;
                }
                if (item9 < 0) {
                    item8 = 2147483647;
                    int10 = 2147483647;
                } else if (!boolean7) {
                    item8 = item8 + item9 / (int5 * arg1);
                    int12 = int12 + item9 % (int5 * arg1) / int5;
                    int13 = int13 + item9 % (int5 * arg1) % int5 * int11 / int5;
                } else {
                    item8 = script_6502(item8, item9 / int5);
                }
            }
            int10 = int10 + 1;
            switch (int10) {
            case 2:
                item4 = getrs3AttributeMapValue(attrmap0, 2656);
                int5 = getrs3AttributeMapValue(attrmap0, 2666);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2687) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5457);
                break;
            case 3:
                item4 = getrs3AttributeMapValue(attrmap0, 2657);
                int5 = getrs3AttributeMapValue(attrmap0, 2667);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2688) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5458);
                break;
            case 4:
                item4 = getrs3AttributeMapValue(attrmap0, 2658);
                int5 = getrs3AttributeMapValue(attrmap0, 2668);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2689) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5459);
                break;
            case 5:
                item4 = getrs3AttributeMapValue(attrmap0, 2659);
                int5 = getrs3AttributeMapValue(attrmap0, 2669);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2690) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5460);
                break;
            case 6:
                item4 = getrs3AttributeMapValue(attrmap0, 2660);
                int5 = getrs3AttributeMapValue(attrmap0, 2670);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2691) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5461);
                break;
            case 7:
                item4 = getrs3AttributeMapValue(attrmap0, 2661);
                int5 = getrs3AttributeMapValue(attrmap0, 2671);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2692) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5462);
                break;
            case 8:
                item4 = getrs3AttributeMapValue(attrmap0, 2662);
                int5 = getrs3AttributeMapValue(attrmap0, 2672);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2693) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5463);
                break;
            case 9:
                item4 = getrs3AttributeMapValue(attrmap0, 2663);
                int5 = getrs3AttributeMapValue(attrmap0, 2673);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2694) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5464);
                break;
            case 10:
                item4 = getrs3AttributeMapValue(attrmap0, 2664);
                int5 = getrs3AttributeMapValue(attrmap0, 2674);
                boolean7 = getrs3AttributeMapValue(attrmap0, 2695) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5465);
                break;
            case 11:
                item4 = getrs3AttributeMapValue(attrmap0, 5451);
                int5 = getrs3AttributeMapValue(attrmap0, 5471);
                boolean7 = getrs3AttributeMapValue(attrmap0, 5476) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5466);
                break;
            case 12:
                item4 = getrs3AttributeMapValue(attrmap0, 5452);
                int5 = getrs3AttributeMapValue(attrmap0, 5472);
                boolean7 = getrs3AttributeMapValue(attrmap0, 5477) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5467);
                break;
            case 13:
                item4 = getrs3AttributeMapValue(attrmap0, 5453);
                int5 = getrs3AttributeMapValue(attrmap0, 5473);
                boolean7 = getrs3AttributeMapValue(attrmap0, 5478) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5468);
                break;
            case 14:
                item4 = getrs3AttributeMapValue(attrmap0, 5454);
                int5 = getrs3AttributeMapValue(attrmap0, 5474);
                boolean7 = getrs3AttributeMapValue(attrmap0, 5479) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5469);
                break;
            case 15:
                item4 = getrs3AttributeMapValue(attrmap0, 5455);
                int5 = getrs3AttributeMapValue(attrmap0, 5475);
                boolean7 = getrs3AttributeMapValue(attrmap0, 5480) == 1;
                int6 = getrs3AttributeMapValue(attrmap0, 5470);
                break;
            default:
                item4 = -1;
                break;
            }
        }
        if (int12 + int13 > 0) {
            int12 = int12 + int13 / int11;
            item8 = item8 + int12 / arg1;
            if (int13 / int11 % arg1 + int12 % arg1 > 0) {
                item8 = item8 + 1;
            }
        }
//        if (VARP[8621] > 0) {
//            item8 = (Item)min(item8, VARP[8621]);
//        }
        return new int[] { item8, int14 };
    }

    public static int script_6502(int arg0, int arg1) {
        if (arg1 > 0) {
            if (arg0 <= (2147483647 - arg1)) {
                arg0 = arg0 + arg1;
            } else {
                arg0 = 2147483647;
            }
        } else if (arg1 < 0) {
            if (arg0 >= (-2147483648 - arg1)) {
                arg0 = arg0 + arg1;
            } else {
                arg0 = -2147483648;
            }
        }
        return arg0;
    }
    
    public static final int SKILLS_START_OPCODE = 2696, XPS_START_OPCODE = 2697, REUIRED_TYPE_START_OPCODE = 2640, REUIRED_AMOUT_START_OPCODE = 2645, ITEMS_START_OPCODE = 2655, AMOUNTS_START_OPCODE = 2665, MATERIALS_START_OPCODE = 5456, REPLACEABLE_ITEMS_MAP_OPCODE = 2675, REPLACEABLE_ITEMS_START_OPCODE = 5451, REPLACEABLE_AMOUNTS_START_OPCODE = 5471, BLUE_PRINT_OPCODE = 2646, CREATION_QUANTITY = 2653;

    
    public static int getMaxCreateAmount(Player player, int productId) {
        Item[][] requiredItems = getRequiredItemsForItem(productId);
        if (requiredItems == null)
            return -1;
        int[] amountOfItems = new int[requiredItems.length];
        for (int i = 0; i < requiredItems.length; i++) {
            for (int j = 0; j < requiredItems[i].length; j++) {
                Item item = requiredItems[i][j];
                amountOfItems[i] += player.getInventory().getAmountOf(item.getId());
            }
        }
        int maxAmountToCreate = Integer.MAX_VALUE;
        for (int i = 0; i < requiredItems.length; i++) {
            Item checkItem = requiredItems[i][0];
            int availableAmount = amountOfItems[i];
            int createAmount = availableAmount / checkItem.getAmount();
            if (createAmount < maxAmountToCreate)
                maxAmountToCreate = createAmount;
            if (createAmount == 0)
                break;
        }
        Item[] actualRequiredItems = getActualRequiredItems(productId, requiredItems);
        int extraSpace = 0;
        for (Item item : actualRequiredItems) {
            if (item == null)
                continue;
            extraSpace += !item.getDefinitions().isStackable() ? item.getAmount() * maxAmountToCreate : (player.getInventory().getAmountOf(item.getId()) == (player.getInventory().getFreeSlots() + item.getAmount()) ? 1 : 0);
        }
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(productId);
        if (!defs.isStackable() && maxAmountToCreate > player.getInventory().getFreeSlots() + extraSpace)
            maxAmountToCreate = player.getInventory().getFreeSlots() + extraSpace;
        else if (defs.isStackable() && !player.getInventory().containsItem(productId, 1) && (player.getInventory().getFreeSlots() + extraSpace) == 0)
            maxAmountToCreate = 0;
        if (maxAmountToCreate > 60)
            maxAmountToCreate = 60;
        return maxAmountToCreate;
    }
}
