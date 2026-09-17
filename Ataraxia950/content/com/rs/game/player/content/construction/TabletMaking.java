package com.rs.game.player.content.construction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.dialogue.Dialogue;

public class TabletMaking {

    public static void openTabInterface(Player player, WorldObject object, int index) {
        player.getVarBitManager().sendVar(549, index + 1);
        player.faceObject(object);
        player.setNextAnimation(new Animation(3652));
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                RS3SkillsDialogue.sendSkillDialogueByProduce(player, getBestAvailableProduct(player));
                player.getPackets().sendGlobalString(2390, "Tablet Making");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                SkillDialogueResult result = RS3SkillsDialogue.getResult(player, componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                if (componentId == RS3SkillsDialogue.CONTINUE_OPTION) {
                    end();
                    int productId = result.getProduce();
                    int quantity = result.getQuantity();
                    TabletMakingData data = getTabletMakingData(productId);
                    if (data == null)
                        return;
                    boolean enchantedAtleastOne = false;
                    double[] totalXps = new double[player.getSkills().getLevels().length];
                    for (int count = 0; count < quantity; count++) {
                        if (!checkAll(player, data, true))
                            break;
                        enchantedAtleastOne = true;
                        for (int i = 0; i < data.requiredItems.length; i++) {
                            int itemId = data.getRequiredItems()[i][0];
                            int amount = data.getRequiredItems()[i][1];
                            player.getInventory().removeItemMoneyPouch(new Item(itemId, amount));
                        }
                        if (data.getReplaceableItems() != null)
                            for (Item item : data.getReplaceableItems())
                                if (player.getInventory().containsItem(item.getId(), item.getAmount())) {
                                    player.getInventory().deleteItem(item.getId(), item.getAmount());
                                    break;
                                }
                        for (int i = 0; i < data.getProductXp().length; i++) {
                            int skillId = data.getProductXp()[i][0];
                            double xp = ((double) data.getProductXp()[i][1]) / 10.0;
                            if (xp > 0)
                                totalXps[skillId] += xp;
                        }
                        Item item = new Item(data.productId, data.getQuantity());
                        player.getInventory().addItem(item);
                    }
                    if (enchantedAtleastOne) {
                        player.faceObject(object);
                        player.setNextAnimation(new Animation(3645));
                        for (int i = 0; i < totalXps.length; i++)
                            if (totalXps[i] > 0)
                                player.getSkills().addXp(i, totalXps[i]);
                    }
                } else 
                    player.setNextAnimation(new Animation(-1));
            }

            @Override
            public void finish() {

            }
        });
    }

    public static int getBestAvailableProduct(Player player) {
        int selectedProductId = -1;
        int highestLevel = -1;
        for (Object o2 : ClientScriptMap.getMap(8083).getValues().values()) {
            int productId = (int) o2;
            TabletMakingData data = getTabletMakingData(productId);
            if (data == null) {
                System.out.println("error getting tablet making data for productId=" + productId);
                continue;
            }
            if (!checkAll(player, data, false))
                continue;
            if (data.magicLevel > highestLevel) {
                selectedProductId = productId;
                highestLevel = data.magicLevel;
                continue;
            }
        }
        if (selectedProductId == -1) {
            return ClientScriptMap.getMap(8083).getIntValue(0);
        }
        return selectedProductId;
    }

    public static boolean checkAll(Player player, TabletMakingData data, boolean sendMessage) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(data.productId);
        String name = defs.getName();
        for (int i = 0; i < data.getRequirements().length; i++) {
            int type = data.getRequirements()[i][0];
            int amount = data.getRequirements()[i][1];
            if (type < 61) {
                int skillId = ClientScriptMap.getMap(681).getIntValue(type);
                int level = amount;
                if (player.getSkills().getLevelForXp(skillId) < level) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + level + " to " + ("Enchant") + " " + name + ".");
                    return false;
                }
            }
        }
        for (int i = 0; i < data.getRequiredItems().length; i++) {
            int itemId = data.getRequiredItems()[i][0];
            int amount = data.getRequiredItems()[i][1];
            int materialIndex = data.getRequiredItems()[i][2];
            if (materialIndex == -1) {
                if (!player.getInventory().containsItem(itemId, amount)) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage("You don't have enough " + ItemDefinitions.getItemDefinitions(itemId).getName() + " to " + ("Enchant") + " " + name + ".");
                    return false;
                }
            } else {
                if (player.getInventionManager().getMaterials()[materialIndex] < amount) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage("You don't have enough " + InventionDefinitions.getMaterialName(materialIndex) + " to " + ("Enchant") + " " + name + ".");
                    return false;
                }
            }
        }
        Item[] replaceableItems = data.getReplaceableItems();
        if (replaceableItems != null) {
            boolean hasOne = false;
            for (Item item : replaceableItems)
                if (player.getInventory().containsItem(item.getId(), item.getAmount()))
                    hasOne = true;
            if (!hasOne) {
                if (sendMessage)
                    player.getPackets().sendGameMessage("You need to have any of the following items to " + ("Enchant") + " " + name + ":", true);
                int count = 1;
                for (Item item : replaceableItems) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage(count + ") " + item.getAmount() + " x " + item.getName() + ".", true);
                    count++;
                }
                return false;
            }
        }
        if (sendMessage) {
            if ((defs.isStackable() && player.getInventory().getAmountOf(data.productId) == 0 && player.getInventory().getFreeSlots() == 0) || (!defs.isStackable() && player.getInventory().getFreeSlots() == 0)) {
                player.getPackets().sendGameMessage("You don't have enough inventory space.", true);
                return false;
            }
        }
        return true;
    }

    private static final ConcurrentHashMap<Integer, TabletMakingData> tabletMakingData = new ConcurrentHashMap<Integer, TabletMakingData>();
    public static final int SKILLS_START_OPCODE = 2696, XPS_START_OPCODE = 2697, REUIRED_TYPE_START_OPCODE = 2640, REUIRED_AMOUT_START_OPCODE = 2645, ITEMS_START_OPCODE = 2655, AMOUNTS_START_OPCODE = 2665, MATERIALS_START_OPCODE = 5456, REPLACEABLE_ITEMS_MAP_OPCODE = 2675, REPLACEABLE_ITEMS_START_OPCODE = 5451, REPLACEABLE_AMOUNTS_START_OPCODE = 5471, BLUE_PRINT_OPCODE = 2646, CREATION_QUANTITY = 2653;

    public static TabletMakingData getTabletMakingData(int productId) {
        if (tabletMakingData.containsKey(productId))
            return tabletMakingData.get(productId);
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(productId);
        if (defs == null || defs.getName().equals("null"))
            return null;
        int magicLevel = 0;
        List<int[]> requirements = new ArrayList<int[]>();
        for (int i = 0; i < 5; i++) {
            if (defs.getCSOpcode(REUIRED_TYPE_START_OPCODE + i) == 0)
                break;
            int type = defs.getCSOpcode(REUIRED_TYPE_START_OPCODE + i);
            int amount = defs.getCSOpcode(REUIRED_AMOUT_START_OPCODE + i);
            requirements.add(new int[] { type, amount });
            if (type < 61) {
                int skillId = ClientScriptMap.getMap(681).getIntValue(type);
                if (skillId == Skills.MAGIC)
                    magicLevel = amount;
            }
        }
        List<int[]> productXp = new ArrayList<int[]>();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (defs.getCSOpcode(SKILLS_START_OPCODE + (i * 2)) == 0)
                break;
            int skillId = defs.getCSOpcode(SKILLS_START_OPCODE + (i * 2));
            skillId = ClientScriptMap.getMap(681).getIntValue(skillId);
            int xp = defs.getCSOpcode(XPS_START_OPCODE + (i * 2));
            productXp.add(new int[] { skillId, xp });
        }
        int replaceableItemsMap = defs.getCSOpcode(REPLACEABLE_ITEMS_MAP_OPCODE);
        List<Item> replaceableItems = null;
        if (replaceableItemsMap != 0) {
            replaceableItems = new ArrayList<Item>();
            GeneralRequirementMap map = GeneralRequirementMap.getMap(replaceableItemsMap);
            if (map != null) {
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    if (map.getIntValue(ITEMS_START_OPCODE + i) == 0 || ITEMS_START_OPCODE + i >= AMOUNTS_START_OPCODE)
                        break;
                    replaceableItems.add(new Item(map.getIntValue(ITEMS_START_OPCODE + i), map.getIntValue(AMOUNTS_START_OPCODE + i)));
                }
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    if (map.getIntValue(REPLACEABLE_ITEMS_START_OPCODE + i) == 0)
                        break;
                    replaceableItems.add(new Item(map.getIntValue(REPLACEABLE_ITEMS_START_OPCODE + i), map.getIntValue(REPLACEABLE_AMOUNTS_START_OPCODE + i)));
                }
            }
        }
        List<int[]> requiredItems = new ArrayList<int[]>();
        for (int i = replaceableItemsMap != 0 ? 1 : 0; i < Integer.MAX_VALUE; i++) {
            if (defs.getCSOpcode(ITEMS_START_OPCODE + i) == 0)
                break;
            int itemId = defs.getCSOpcode(ITEMS_START_OPCODE + i);
            int amount = defs.getCSOpcode(AMOUNTS_START_OPCODE + i);
            int materialId = itemId == 36365 ? defs.getCSOpcode(MATERIALS_START_OPCODE + i) : -1;
            if (materialId != -1)
                materialId = InventionDefinitions.getMaterialIndex(materialId);
            requiredItems.add(new int[] { itemId, amount, materialId });
        }
        int quantity = defs.getCSOpcode(CREATION_QUANTITY) == 0 ? 1 : defs.getCSOpcode(CREATION_QUANTITY);
        if (productId >= 41335 && productId <= 41545) {
            productId = 48269 + (productId - 41335);
        }
        for (int i = 0; i < requiredItems.size(); i++) {
            int[] item = requiredItems.get(i);
            Integer[] repairData = ItemDefinitions.getItemDefinitions(item[0]).getRepairData();
            if (repairData != null && repairData.length > 0) {
                item[0] = repairData[0];

            }
        }
        TabletMakingData data = new TabletMakingData(productId, quantity, requirements.toArray(new int[requirements.size()][2]), productXp.toArray(new int[productXp.size()][2]), requiredItems.toArray(new int[requiredItems.size()][3]), replaceableItems == null ? null : replaceableItems.toArray(new Item[replaceableItems.size()]));
        data.magicLevel = magicLevel;
        tabletMakingData.put(productId, data);
        return data;
    }

    public static class TabletMakingData {
        private final int[][] requirements;
        private final int[][] productXp;
        private final int[][] requiredItems;
        private final Item[] replaceableItems;
        private final int productId;
        private final int quantity;
        private int magicLevel;

        public TabletMakingData(int productId, int quantity, int[][] requirements, int[][] productXp, int[][] requiredItems, Item[] replaceableItems) {
            this.productId = productId;
            this.quantity = quantity;
            this.requirements = requirements;
            this.productXp = productXp;
            this.requiredItems = requiredItems;
            this.replaceableItems = replaceableItems;
        }

        public int getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public int[][] getRequirements() {
            return requirements;
        }

        public int[][] getProductXp() {
            return productXp;
        }

        public int[][] getRequiredItems() {
            return requiredItems;
        }

        public Item[] getReplaceableItems() {
            return replaceableItems;
        }

    }

}