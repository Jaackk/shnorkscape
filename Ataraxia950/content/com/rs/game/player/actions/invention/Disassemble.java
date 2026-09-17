package com.rs.game.player.actions.invention;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.ChargesManagerNew;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.invention.InventionData.Gizmo;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.EconomyPrices;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemDisassembleDataParser;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData.Component;

public class Disassemble extends Action {

    private static final ImmutableSet<Integer> DISASSEMBLE_WARNING = ImmutableSet.of(41069, 41007);

    private Item item;
    private ItemDisassembleData data;
    private int ticks;
    private double show_xp;
    boolean canstart;

    public Disassemble(Item item) {
        this.item = item;
        if (item == null)
            return;
    }

    public Disassemble(Item item, boolean canstart) {
        this.item = item;
        if (item == null)
            return;
        this.canstart = canstart;
    }

    public static double getDisassembleXP(int level) {
        if (level >= 10)
            return 540000;
        if (level >= 9)
            return 378000;
        if (level >= 8)
            return 270000;
        if (level >= 7)
            return 198000;
        if (level >= 6)
            return 144000;
        if (level >= 5)
            return 108000;
        if (level >= 4)
            return 54000;
        if (level >= 3)
            return 27000;
        if (level >= 2)
            return 9000;
        return 0;
    }

    @Override
    public boolean start(Player player) {
        if (item != null) {
            data = ItemDisassembleDataParser.getItemDisassembleData(player, item.getId());
            if (data == null)
                data = ItemDisassembleDataParser.getItemDisassembleData(player, ItemDefinitions.getItemDefinitions(item.getId()).certId);
        }
        if (data == null || item == null) {
            player.getPackets().sendGameMessage("You can't disassemble this item.");
            return false;
        }
        if (!canstart) {
            if ((!player.hasDisableDisassembleHighValueWarning() && EconomyPrices.getPrice(item.getId()) >= 100000) || (item.getInventionData() != null && !item.hasGizmo()) || DISASSEMBLE_WARNING.contains(item.getId())) {
                player.getDialogueManager().startDialogue(new Dialogue() {
                    @Override
                    public void start() {
                        boolean augmentedItem = (item.getInventionData() != null && !item.hasGizmo());
                        player.getInterfaceManager().sendInterface(1048);
                        int itemLevel = InventionData.getItemLevel(player, item);
                        int baseXp = (int) getDisassembleXP(itemLevel);
                        int tier = item.getDefinitions().getCSOpcode(750);
                        if (tier < 60)
                            tier = 70;
                        baseXp *= (1.00 + (1.50 * ((double) (tier - 80) / 100.00)));
                        String message = augmentedItem ? ("<br><br>It is currently level <col=ffffff>" + itemLevel + "</col>.<br><br><img=6>You will gain an extra <col=ffffff>" + Utils.getFormattedNumber(baseXp) + "</col> Invention XP.") : "";
                        if (augmentedItem) {
                            ItemDefinitions originalDefinitions = ItemDefinitions.getItemDefinitions(item.getUnAugmentedItemId());
                            Integer[] degradeData = originalDefinitions.getItemDegradeData();
                            if (degradeData != null && ChargesManagerNew.getUnDyedVersion(item.getDefinitions().getChargedItemId()) != -1) {
                                int originalId = degradeData[1];
                                message += "<br><br><img=6>This will return " + ItemDefinitions.getItemDefinitions(originalId).getName() + ".";
                            }
                        }
                        player.getPackets().sendExecuteScript(9727, "DISASSEMBLE", "Are you sure you want to disassemble your " + (augmentedItem ? "levellable" : "High value") + " item?" + message + "<br><br><col=ff0000>if you disassemble this, it will be completly destroyed.", "DISASSEMBLE", "Cancel", "", -1, 48);
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        if (componentId == 19)
                            player.getActionManager().setAction(new Disassemble(item, true));
                        end();
                    }

                    @Override
                    public void finish() {
                    }
                });
                return false;
            }
        }
        int[] skillIds = { Skills.CRAFTING, Skills.DIVINATION, Skills.SMITHING };
        for (int skillId : skillIds) {
            if (player.getSkills().getLevelForXp(skillId) < 80) {
                player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + 80 + " to do that.");
                return false;
            }
        }
        String name = ItemDefinitions.getItemDefinitions(item.getId()).getName();
        ticks = canstart ? 1 : player.getInventory().getAmountOf(item.getId()) / data.getRequiredQuantity();
        if (ticks == 0) {
            player.getPackets().sendGameMessage("You don't have enough of " + name + ". You need " + data.getRequiredQuantity() + " of " + name + " for each action.");
            return false;
        }
        if (ticks > 60)
            ticks = 60;
        player.getPackets().sendGameMessage("Disassembling: " + name);
        show_xp = 0;
        return true;
    }

    @Override
    public boolean process(Player player) {
        return ticks > 0;
    }

    @Override
    public int processWithDelay(Player player) {
        ticks--;
        if (item == null)
            return -1;
        double junkChance = data.getJunkChance() * player.getInventionManager().getJunkChanceReductionMultiplier();
        double baseXp = data.getXp();
        int requiredQuantity = data.getRequiredQuantity();
        int materialsCount = data.getMaterialCount();
        int[] materials = new int[player.getInventionManager().getMaterials().length];
        String name = ItemDefinitions.getItemDefinitions(item.getId()).getName();
        if (!player.getInventory().containsItem(item, requiredQuantity)) {
            player.getPackets().sendGameMessage("You don't have enough of " + name + ".");
            return -1;
        }
        Component[] components = data.getComponents();
        boolean augmentedItem = (item.getInventionData() != null && !item.hasGizmo());
        int itemLevel = InventionData.getItemLevel(player, item);
        int extraXP = 0;
        int tier = item.getDefinitions().getCSOpcode(750);
        if (tier < 60)
            tier = 70;
        int extraMaterial = -1;
        boolean returnGizmos = false;
        int materialsMultiplier = itemLevel >= 9 ? 4 : itemLevel >= 6 ? 3 : itemLevel >= 3 ? 2 : 1;
        if (augmentedItem) {
            materialsCount *= materialsMultiplier;
            extraXP = (int) getDisassembleXP(itemLevel);
            extraXP *= (1.00 + (1.50 * ((double) (tier - 80) / 100.00)));
            if (itemLevel >= 7) {
                extraMaterial = InventionDefinitions.getRandomMaterial(3);
            }
            if (itemLevel > 1) {
                returnGizmos = itemLevel >= 8 || Math.random() <= 0.5;
            }
        }
        if (returnGizmos && item.getInventionData().getGizmosCount() > 0 && !player.getInventory().hasFreeSlots()) {
            player.getPackets().sendGameMessage("You don't have enough space in your inventory to disassemble that.");
            return -1;
        }
        if (item.getDefinitions().getName().equalsIgnoreCase("Relic of aminishi (rare)")) {
            materials[components[Utils.random(components.length)].getId()] += 1;
        } else {
            List<Component> withoutSpecial = new ArrayList<Component>();
            for (Component comp : components) {
                if (comp.getChance() >= 1)
                    materials[comp.getId()] += comp.getAmount() * materialsMultiplier;
                else {
                    withoutSpecial.add(comp);
                }
            }
            double[] bins = InventionDefinitions.generateCompBins(withoutSpecial);
            for (int i = 0; i < materialsCount; i++) {
                if (Math.random() * 100 <= junkChance && itemLevel < 4) {
                    materials[75] += 1;
                } else if (item.getInventionData() != null && item.hasGizmo()) {
                    Map<Integer, Integer> materialsUsed = new HashMap<Integer, Integer>();
                    for (Gizmo gizmo : item.getInventionData().getGizmos()) {
                        if (gizmo == null)
                            continue;
                        for (int j = 0; j < gizmo.getMaterialsUsed().length; j++) {
                            if (gizmo.getMaterialsUsed()[j] == null || gizmo.getMaterialsUsed()[j][0] == -1)
                                continue;
                            if (materialsUsed.containsKey(gizmo.getMaterialsUsed()[j][0])) {
                                materialsUsed.put(gizmo.getMaterialsUsed()[j][0], materialsUsed.get(gizmo.getMaterialsUsed()[j][0]) + gizmo.getMaterialsUsed()[j][1]);
                            } else
                                materialsUsed.put(gizmo.getMaterialsUsed()[j][0], gizmo.getMaterialsUsed()[j][1]);
                        }
                    }
                    Integer[] materialIds = materialsUsed.keySet().toArray(new Integer[materialsUsed.keySet().size()]);
                    Integer[] materialAmounts = materialsUsed.values().toArray(new Integer[materialsUsed.values().size()]);
                    int random = Utils.random(materialIds.length);
                    int amount = materialAmounts[random] <= 1 ? 1 : Utils.random(1, materialAmounts[random]);
                    if (materials[materialIds[random]] == 0)
                        materials[materialIds[random]] += amount;
                } else {
                    int materialId = withoutSpecial.size() == 1 ? withoutSpecial.get(0).getId() : InventionDefinitions.selectRandomComponent(bins, withoutSpecial).getId();
                    materials[materialId] += 1;
                }
            }
            if (extraMaterial != -1)
                materials[extraMaterial]++;
        }
        int totalAmount = 0;
        for (int materialCount : materials)
            totalAmount += materialCount;
        String materialsGained = "Materials gained: ";
        int rareCount = 0;
        for (int i = 0; i < materials.length; i++) {
            if (materials[i] == 0)
                continue;
            InventionDefinitions def = InventionDefinitions.getData(ClientScriptMap.getMap(10742).getIntValue(i));
            int gained = materials[i];
            int rarity = (int) def.getDataInIndex(7);
            totalAmount -= gained;
            String materialName = (String) def.getDataInIndex(1);
            String color = rarity == 4 ? "<col=ff0000>" : rarity == 3 ? "<col=ff8000>" : "";
            materialsGained += color + gained + " X " + materialName + "</col>" + ((totalAmount == 0) ? "." : ", ");
            player.getInventionManager().getMaterials()[i] += gained;
            if (rarity == 4 || rarity == 3) {
                player.getPackets().sendExecuteScript(12084, gained + " x " + materialName, rarity == 3 ? 16101953 : 15865109, 5 + (rareCount * 10));
                rareCount++;
            }
        }
        player.setNextGraphics(new Graphics(6003));
        player.setNextAnimation(new Animation(27997));
        if (item.getInventionData() != null) {
            if (returnGizmos && item.getInventionData().getGizmosCount() > 0) {
                int itemCategory = item.getDefinitions().itemCategory;
                int augmentedItemType = (itemCategory == 35 || itemCategory == 67) ? 2 : (itemCategory == 7 || itemCategory == 8 || itemCategory == 18 || itemCategory == 10) ? 1 : 0;
                String lowerName = item.getDefinitions().getName().toLowerCase();
                if (lowerName.contains("fishing rod-o-matic") || lowerName.contains("pyro-matic") || lowerName.contains("hammer-tron") || lowerName.contains(" hatchet") || lowerName.contains("pickaxe") || lowerName.contains(" hammer") || lowerName.contains("tinderbox") || lowerName.contains("fishing rod"))
                    augmentedItemType = 2;
                int gizmoId = 36720 + 2 * augmentedItemType;
                for (int i = 0; i < item.getInventionData().getGizmos().length; i++) {
                    if (item.getInventionData().getGizmos()[i] != null) {
                        Item gizmo = new Item(gizmoId, 1);
                        gizmo.setInventionData(new InventionData(item.getInventionData().getGizmos()[i]));
                        player.getInventory().addItem(gizmo);
                    }
                }
                player.getPackets().sendGameMessage("Your extracted gizmos have been added to your inventory.");
            }
            int itemSlot = player.getInventory().getItemSlot(item);
            if (itemSlot != -1) {
                player.getInventory().getItems().set(itemSlot, null);
                player.getInventory().refresh();
            }
        } else {
            Item t = new Item(item.getId(), requiredQuantity);
            t.setAttributes(item.getAttributes());
            player.getInventory().deleteItem(t);
        }
        if (augmentedItem) {
            ItemDefinitions originalDefinitions = ItemDefinitions.getItemDefinitions(item.getUnAugmentedItemId());
            Integer[] degradeData = originalDefinitions.getItemDegradeData();
            if (degradeData != null && ChargesManagerNew.getUnDyedVersion(item.getDefinitions().getChargedItemId()) != -1) {
                int originalId = degradeData[1];
                player.getInventory().addItemDrop(new Item(originalId));
            }
        }
        show_xp = show_xp + player.getSkills().addXp(Skills.INVENTION, baseXp + (extraXP));
        player.getPackets().sendGameMessage(materialsGained, true);
        player.getInventionManager().refreshMaterials();
        item = player.getInventory().getItemById(item.getId());
        return 2;
    }

    public static void forceDisassembleItem(Player player, Item item) {
        if (item == null)
            return;
        ItemDisassembleData data = ItemDisassembleDataParser.getItemDisassembleData(player, item.getId());
        if (data == null)
            data = ItemDisassembleDataParser.getItemDisassembleData(player, ItemDefinitions.getItemDefinitions(item.getId()).certId);
        if (data == null)
            return;
        double junkChance = data.getJunkChance() * player.getInventionManager().getJunkChanceReductionMultiplier();
        int materialsCount = data.getMaterialCount();
        int[] materials = new int[player.getInventionManager().getMaterials().length];
        Component[] components = data.getComponents();
        boolean augmentedItem = (item.getInventionData() != null && !item.hasGizmo());
        if (augmentedItem)
            return;
        int itemLevel = InventionData.getItemLevel(player, item);
        int tier = item.getDefinitions().getCSOpcode(750);
        if (tier < 60)
            tier = 70;
        int extraMaterial = -1;
        int materialsMultiplier = itemLevel >= 9 ? 4 : itemLevel >= 6 ? 3 : itemLevel >= 3 ? 2 : 1;
        if (item.getDefinitions().getName().equalsIgnoreCase("Relic of aminishi (rare)")) {
            materials[components[Utils.random(components.length)].getId()] += 1;
        } else {
            List<Component> withoutSpecial = new ArrayList<Component>();
            for (Component comp : components) {
                if (comp.getChance() >= 1)
                    materials[comp.getId()] += comp.getAmount() * materialsMultiplier;
                else {
                    withoutSpecial.add(comp);
                }
            }
            double[] bins = InventionDefinitions.generateCompBins(withoutSpecial);
            for (int i = 0; i < materialsCount; i++) {
                if (Math.random() * 100 <= junkChance && itemLevel < 4) {
                    materials[75] += 1;
                } else if (item.getInventionData() != null && item.hasGizmo()) {
                    Map<Integer, Integer> materialsUsed = new HashMap<Integer, Integer>();
                    for (Gizmo gizmo : item.getInventionData().getGizmos()) {
                        if (gizmo == null)
                            continue;
                        for (int j = 0; j < gizmo.getMaterialsUsed().length; j++) {
                            if (gizmo.getMaterialsUsed()[j] == null || gizmo.getMaterialsUsed()[j][0] == -1)
                                continue;
                            if (materialsUsed.containsKey(gizmo.getMaterialsUsed()[j][0])) {
                                materialsUsed.put(gizmo.getMaterialsUsed()[j][0], materialsUsed.get(gizmo.getMaterialsUsed()[j][0]) + gizmo.getMaterialsUsed()[j][1]);
                            } else
                                materialsUsed.put(gizmo.getMaterialsUsed()[j][0], gizmo.getMaterialsUsed()[j][1]);
                        }
                    }
                    Integer[] materialIds = materialsUsed.keySet().toArray(new Integer[materialsUsed.keySet().size()]);
                    Integer[] materialAmounts = materialsUsed.values().toArray(new Integer[materialsUsed.values().size()]);
                    int random = Utils.random(materialIds.length);
                    int amount = materialAmounts[random] <= 1 ? 1 : Utils.random(1, materialAmounts[random]);
                    if (materials[materialIds[random]] == 0)
                        materials[materialIds[random]] += amount;
                } else {
                    int materialId = withoutSpecial.size() == 1 ? withoutSpecial.get(0).getId() : InventionDefinitions.selectRandomComponent(bins, withoutSpecial).getId();
                    materials[materialId] += 1;
                }
            }
            if (extraMaterial != -1)
                materials[extraMaterial]++;
        }
        for (int i = 0; i < materials.length; i++) {
            if (materials[i] == 0)
                continue;
            int gained = materials[i];
            player.getInventionManager().getMaterials()[i] += gained;
        }
        player.getInventionManager().refreshMaterials();
    }

    @Override
    public void stop(Player player) {
        setActionDelay(player, 3);
    }

}
