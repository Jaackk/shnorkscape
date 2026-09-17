package com.rs.game.player.actions.invention;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;

public class Manufacture extends Action {

    public static final int SKILLS_START_OPCODE = 2696, XPS_START_OPCODE = 2697, REUIRED_TYPE_START_OPCODE = 2640, REUIRED_AMOUT_START_OPCODE = 2645, ITEMS_START_OPCODE = 2655, AMOUNTS_START_OPCODE = 2665, MATERIALS_START_OPCODE = 5456, REPLACEABLE_ITEMS_MAP_OPCODE = 2675, REPLACEABLE_ITEMS_START_OPCODE = 5451, REPLACEABLE_AMOUNTS_START_OPCODE = 5471, BLUE_PRINT_OPCODE = 2646, CREATION_QUANTITY = 2653;
    private static final ConcurrentHashMap<Integer, ManufactureData> manufactureData = new ConcurrentHashMap<Integer, ManufactureData>();

    private int quantity;
    private final ManufactureData data;

    public Manufacture(ManufactureData data, int quantity) {
        this.quantity = quantity;
        this.data = data;
    }

    public Manufacture(int productId, int quantity) {
        this.quantity = quantity;
        data = getManufactureData(productId);
    }

    @Override
    public boolean start(Player player) {
        if (data == null)
            return false;
        if (!checkAll(player))
            return false;
        if (data.productId == 41083)
            quantity = 1;
        return true;
    }

    private boolean checkAll(Player player) {
        return checkAll(player, data, true);
    }

    @Override
    public boolean process(Player player) {
        return checkAll(player) && quantity > 0;
    }

    @Override
    public int processWithDelay(Player player) {
        String name = ItemDefinitions.getItemDefinitions(data.productId).getName();
        boolean augment = name.toLowerCase().contains("augmented");
        quantity--;
        for (int i = 0; i < data.requiredItems.length; i++) {
            int itemId = data.getRequiredItems()[i][0];
            int amount = data.getRequiredItems()[i][1];
            int materialIndex = data.getRequiredItems()[i][2];
            if (materialIndex == -1)
                player.getInventory().removeItemMoneyPouch(new Item(itemId, amount));
            else
                player.getInventionManager().getMaterials()[materialIndex] -= amount;
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
                player.getSkills().addXp(skillId, xp);
        }
        Item item = new Item(data.productId, data.getQuantity());
        if (augment) {
            int orignalId = -1;
            for (int i = 0; i < data.requiredItems.length; i++) {
                int itemId = data.getRequiredItems()[i][0];
                if(ItemDefinitions.getItemDefinitions(itemId).isWearItem()) {
                    orignalId = itemId;
                    break;
                }
            }
            item.setInventionData(new InventionData(0));
            item.getInventionData().setOriginalItemId(orignalId);
            if (item.getChargesData() != null)
                item.setChargesData(null);
        }
        if (item.getId() == 36389) {
            player.getInventionManager().setHasChargePack(true);
            player.getPackets().sendGameMessage("Charge pack has been added to your toolbelt!");
        } else
            player.getInventory().addItem(item);
        player.setNextAnimation(new Animation(27997));
        player.getInventionManager().refreshMaterials();
        return 3;
    }

    @Override
    public void stop(Player player) {
        setActionDelay(player, 3);
    }

    public static boolean checkAll(Player player, ManufactureData data, boolean sendMessage) {
        String name = ItemDefinitions.getItemDefinitions(data.productId).getName();
        boolean augment = name.toLowerCase().contains("augmented");
        for (int i = 0; i < data.getRequirements().length; i++) {
            int type = data.getRequirements()[i][0];
            int amount = data.getRequirements()[i][1];
            if (type < 61) {
                int skillId = ClientScriptMap.getMap(681).getIntValue(type);
                int level = amount;
                if (player.getSkills().getLevelForXp(skillId) < level) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + level + " to " + (augment ? "augment" : "manufacture") + " " + name + ".");
                    return false;
                }
            } else {
                if (type == 63) {
                    int index = InventionDefinitions.getBluePrintIndex(amount);
                    if (index == -1) {
                        if (sendMessage)
                            player.getPackets().sendGameMessage("You don't know how to " + (augment ? "augment this item." : "manufacture " + name + "."));
                        return false;
                    }
                    int dataId = ClientScriptMap.getMap(10743).getIntValue(index);
                    if (!player.getInventionManager().hasDiscoveredBluePrint(index)) {
                        if (augment) {
                            if (sendMessage)
                                player.getPackets().sendGameMessage("You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment this item.");
                        } else {
                            if (sendMessage)
                                player.getPackets().sendGameMessage("You don't know how to manufacture " + name + ".");
                        }
                        return false;
                    }
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
                        player.getPackets().sendGameMessage("You don't have enough " + ItemDefinitions.getItemDefinitions(itemId).getName() + " to " + (augment ? "augment" : "manufacture") + " " + name + ".");
                    return false;
                }
            } else {
                if (player.getInventionManager().getMaterials()[materialIndex] < amount) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage("You don't have enough " + InventionDefinitions.getMaterialName(materialIndex) + " to " + (augment ? "augment" : "manufacture") + " " + name + ".");
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
                    player.getPackets().sendGameMessage("You need to have any of the following items to " + (augment ? "augment" : "manufacture") + " " + name + ":", true);
                int count = 1;
                for (Item item : replaceableItems) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage(count + ") " + item.getAmount() + " x " + item.getName() + ".", true);
                    count++;
                }
                return false;
            }
        }
        return true;
    }

    public static void openManufactureDialogue(Player player) {
        player.getDialogueManager().startDialogue("ManufactureD");
    }

    public static ManufactureData getManufactureData(int productId) {
        if (manufactureData.containsKey(productId))
            return manufactureData.get(productId);
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(productId);
        if (defs == null || defs.getName().equals("null"))
            return null;
        List<int[]> requirements = new ArrayList<int[]>();
        for (int i = 0; i < 5; i++) {
            if (defs.getCSOpcode(REUIRED_TYPE_START_OPCODE + i) == 0)
                break;
            int type = defs.getCSOpcode(REUIRED_TYPE_START_OPCODE + i);
            int amount = defs.getCSOpcode(REUIRED_AMOUT_START_OPCODE + i);
            requirements.add(new int[] { type, amount });
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
        for(int i=0;i<requiredItems.size();i++) {
            int[] item = requiredItems.get(i);
            Integer[] repairData = ItemDefinitions.getItemDefinitions(item[0]).getRepairData();
            if (repairData != null && repairData.length > 0) {
                item[0] = repairData[0];

            }
        }
        ManufactureData data = new ManufactureData(productId, quantity, requirements.toArray(new int[requirements.size()][2]), productXp.toArray(new int[productXp.size()][2]), requiredItems.toArray(new int[requiredItems.size()][3]), replaceableItems == null ? null : replaceableItems.toArray(new Item[replaceableItems.size()]));
        manufactureData.put(productId, data);
        return data;
    }

    public static class ManufactureData {
        private final int[][] requirements;
        private final int[][] productXp;
        private final int[][] requiredItems;
        private final Item[] replaceableItems;
        private final int productId;
        private final int quantity;

        public ManufactureData(int productId, int quantity, int[][] requirements, int[][] productXp, int[][] requiredItems, Item[] replaceableItems) {
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
