package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.invention.Disassemble;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData;
import com.rs.game.player.actions.invention.InventionData.Gizmo;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.actions.invention.Manufacture;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemDisassembleDataParser;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData.Component;
import com.rs.utils.data.parsers.misc.PerkGenerationDataParser;
import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData.PerkData;

import lombok.Getter;

public class InventionManager implements Serializable {
    public static final int[] MATERIALS_VARS = { 5998, 5999, 6000, 6001, 6002, 6003, 6004, 6005, 6006, 6007, 6008, 6009, 6010, 6011, 6012, 6013, 6014, 6015, 6016, 6017, 6018, 6019, 6020, 6021, 6022, 6023, 6025, 6064, 6065, 6026, 6027, 6028, 6029, 6030, 6031, 6032, 6033, 6034, 6035, 6036, 6037, 6038, 6039, 6024, 6040, 6041, 6042, 6043, 6044, 6045, 6046, 6047, 6062, 6048, 6049, 6050, 6051, 6052, 6053, 6054, 6055, 6056, 6057, 6058, 6059, 6060, 6061, 6063, 6066, 6215, 6216, 6217, 6218, 6508, 6509, 5997 };
    //inter 1615 spring cleaner
    private static final long serialVersionUID = 3329963111343241716L;
    private static final int CHARGE_PACK_VAR_MULTIPLIER = 3000;
    private static final int CHARGES_PER_DIVINE_CHARGE = 3000;
    private static final int INTERNAL_CHARGE_PER_DIVINE_CHARGE = CHARGES_PER_DIVINE_CHARGE * CHARGE_PACK_VAR_MULTIPLIER;
    private static final int STARTING_CHARGE_PACK_CHARGES = 100000 * CHARGE_PACK_VAR_MULTIPLIER;
    private transient Player player;
    public boolean[] discoveredBluePrints;
    private transient BluePrint currentBluePrint;
    private int[] materials;
    private boolean hasChargePack;
    @Getter
    private int divineCharges;

    /** Enum ids this manager sizes its arrays from: blueprints and materials. */
    public static final int BLUEPRINTS_ENUM_ID = 10743, MATERIALS_ENUM_ID = 10742;

    /**
     * Size of a cache enum, tolerating a JVM in which index 17 is not loaded.
     * With the enum present this is exactly {@code ClientScriptMap.getMap(id).getSize()},
     * so legacy 910 logins keep their existing array lengths; without a cache
     * {@code getMap} hands back an empty map (or, defensively, null) and the
     * manager is constructed empty instead of throwing. P4 requires the manager
     * to exist for every hydrated native player because
     * {@code Player.processEntity} dereferences it unconditionally.
     */
    private static int enumSize(int enumId) {
        ClientScriptMap map = ClientScriptMap.getMap(enumId);
        return map == null ? 0 : map.getSize();
    }

    /** Seeds a starting material only when the sized array actually has that index. */
    private void seedStartingMaterial(int index, int amount) {
        if (index >= 0 && index < materials.length)
            materials[index] = amount;
    }

    public InventionManager() {
        this.discoveredBluePrints = new boolean[enumSize(BLUEPRINTS_ENUM_ID)];
        this.materials = new int[enumSize(MATERIALS_ENUM_ID)];
        seedStartingMaterial(0, 100);// simple parts
        seedStartingMaterial(10, 100);
        divineCharges = 0;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void init() {
        player.getVarBitManager().sendVarBit(30223, 105);// unlocks invention tutorial
        ClientScriptMap map = ClientScriptMap.getMap(10743);
        if (discoveredBluePrints == null)
            discoveredBluePrints = new boolean[map.getSize()];
        if (materials == null)
            materials = new int[ClientScriptMap.getMap(10742).getSize()];
        if (discoveredBluePrints.length != map.getSize()) {// if rs added new blue prints
            boolean[] newBluePrints = new boolean[map.getSize()];
            for (int i = 0; i < discoveredBluePrints.length; i++)
                newBluePrints[i] = discoveredBluePrints[i];
            this.discoveredBluePrints = newBluePrints;
        }
        int[] autoDiscovered = { 81, 80, 82, 30, 79, 21 };
        for (int auto : autoDiscovered)
            discoveredBluePrints[auto] = true;
        for (int i = 0; i < discoveredBluePrints.length; i++) {
            if (discoveredBluePrints[i]) {
                ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
                int dataId = bluePrintsMap.getIntValue(i);
                InventionDefinitions def = InventionDefinitions.getData(dataId);
                player.getPackets().sendExecuteScript(12060, def.getDataInIndex(0));
            }
        }
        player.getVarBitManager().sendVarBit(30225, hasChargePack ? 1 : 0);
        player.getVarBitManager().sendVarBit(30224, 1);
        refreshMaterials();
        refreshEquipedItemsDrainRate();
    }

    private transient int gizmoType;
    private transient int interactionIndex;
    private transient int[] addedMaterials;
    private transient Map<List<Perk>, Double> possiblePerks;

    public void openAddMaterialsInterface(int itemId) {
        if (itemId != 36719 && itemId != 36721 && itemId != 36723) {
            return;
        }
        gizmoType = (itemId - 36719) / 2;
        addedMaterials = new int[5];
        possiblePerks = new LinkedHashMap<List<Perk>, Double>();
        Arrays.fill(addedMaterials, -1);
        interactionIndex = 0;
        refreshMaterials();
        player.getInterfaceManager().sendCentralInterfaceLargeInterface(1712);
        player.getPackets().sendExecuteScript(12171, itemId);
        player.getPackets().sendIComponentSettings(1712, 3, 0, 8, 2621470);
        player.getPackets().sendIComponentSettings(1712, 6, 0, 75, 786462);
        refreshAddMaterialsInterface();
    }

    private void refreshAddMaterialsInterface() {
        refreshAddedMaterials();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                refreshPossiblePerks();
            }
        });
    }

    private void refreshAddedMaterials() {
        ClientScriptMap map = ClientScriptMap.getMap(10742);
        int[] materialsFieldIds = new int[5];
        for (int i = 0; i < materialsFieldIds.length; i++) {
            materialsFieldIds[i] = addedMaterials[i] == -1 ? -1 : map.getIntValue(addedMaterials[i]);
        }
        for (int i = 0; i < 9; i++) {
            player.getVarBitManager().sendVar(6079 + i, i >= materialsFieldIds.length ? -1 : materialsFieldIds[i]);
        }
        player.getPackets().sendExecuteScript(12173, interactionIndex, -1, materialsFieldIds[1], -1, materialsFieldIds[2], materialsFieldIds[0], materialsFieldIds[3], -1, materialsFieldIds[4], -1);
    }

    private void refreshPossiblePerks() {
        possiblePerks = getPossiblePerks();
        int[] values = new int[8];
        Map<Integer, Integer[]> perks = new LinkedHashMap<Integer, Integer[]>();
        for (Entry<List<Perk>, Double> e : possiblePerks.entrySet()) {
            for (Perk perk : e.getKey()) {
                if (!perks.containsKey(perk.getId())) {
                    PerkData data = PerkGenerationDataParser.DATA.getPerks()[perk.getId()];
                    perks.put(perk.getId(), data.getRanks().length == 1 ? null : new Integer[] { perk.getRank() });
                } else {
                    Integer[] ranks = perks.get(perk.getId());
                    if (ranks == null)
                        continue;
                    List<Integer> arr = Lists.newArrayList(ranks);
                    if (arr.contains(perk.getRank()))
                        continue;
                    arr.add(perk.getRank());
                    Collections.sort(arr);
                    perks.put(perk.getId(), arr.toArray(new Integer[arr.size()]));
                }
            }
        }
        for (int i = 0; i < values.length; i++) {
            if (i >= perks.size())
                continue;
            int perkId = perks.keySet().stream().toArray(Integer[]::new)[i];
            Integer[] ranks = perks.get(perkId);
            int firstRank = ranks == null ? 0 : ranks[0];
            int secondRank = ranks == null ? 0 : ranks[ranks.length - 1];
            if (secondRank == firstRank)
                secondRank = 0;
            values[i] = ((firstRank + 256 * secondRank) << 16 | perkId);
        }
        player.getPackets().sendExecuteScript(12188, interactionIndex, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);
    }

    private Map<List<Perk>, Double> getPossiblePerks() {
        return InventionDefinitions.getMaterialsProb(player.getSkills().getLevel(Skills.INVENTION), gizmoType, addedMaterials);
    }

    public int getMaterialCount(int materialId) {
        int count = 0;
        if (addedMaterials == null)
            return 0;
        for (int i = 0; i < addedMaterials.length; i++) {
            if (addedMaterials[i] == materialId)
                count++;
        }
        return count;
    }

    public void addMaterial(int materialId) {
        addMaterial(-1, materialId, false);
    }

    public void addMaterial(int index, int materialId, boolean forceAdd) {
        if (!isValidMaterial(materialId)) {
            refreshAddMaterialsInterface();
            return;
        }
        if (index == -1) {
            for (int i = 0; i < addedMaterials.length; i++) {
                if (addedMaterials[i] == -1) {
                    index = i;
                    break;
                }
            }
        }
        interactionIndex++;
        if (index == -1 || (!forceAdd && addedMaterials[index] != -1)) {
            refreshAddMaterialsInterface();
            return;
        }
        if (index < 0 || index >= addedMaterials.length || !hasMaterialsForSlot(materialId, index)) {
            player.getPackets().sendGameMessage("You don't have enough " + InventionDefinitions.getMaterialName(materialId) + " to add that material.");
            refreshAddMaterialsInterface();
            return;
        }
        addedMaterials[index] = materialId;
        refreshAddMaterialsInterface();
    }

    public void switchMaterials(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= addedMaterials.length || toIndex < 0 || toIndex >= addedMaterials.length) {
            interactionIndex++;
            refreshAddMaterialsInterface();
            return;
        }
        int temp = addedMaterials[fromIndex];
        interactionIndex++;
        addedMaterials[fromIndex] = addedMaterials[toIndex];
        addedMaterials[toIndex] = temp;
        refreshAddMaterialsInterface();
    }

    public void removeMaterial(int index) {
        if (index < 0 || index >= addedMaterials.length) {
            interactionIndex++;
            refreshAddMaterialsInterface();
            return;
        }
        interactionIndex++;
        addedMaterials[index] = -1;
        refreshAddMaterialsInterface();
    }

    public void clearMaterials() {
        interactionIndex++;
        Arrays.fill(addedMaterials, -1);
        refreshAddMaterialsInterface();
    }
    
    private void createGizmo() {
        final int shellId = 36719 + (gizmoType * 2);
        if (!player.getInventory().containsItem(shellId, 1)) {
            player.getPackets().sendGameMessage("You need an empty gizmo shell to create a gizmo.");
            player.getInterfaceManager().removeCentralInterfaceLargeInterface();
            return;
        }
        if (!hasEnoughSelectedMaterials(true))
            return;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (!player.getInventory().containsItem(shellId, 1) || !hasEnoughSelectedMaterials(true)) {
                    refreshMaterials();
                    refreshAddMaterialsInterface();
                    return;
                }
                possiblePerks = getPossiblePerks();
                List<Perk> selectedPerks = InventionDefinitions.selectRandomPerk(possiblePerks);
                if (selectedPerks == null) {
                    player.getPackets().sendExecuteScript(12192, 0, 0, 0, 0, 0, 0, player.getInventory().getAmountOf(36719 + (gizmoType * 2)));
                    refreshMaterials();
                    return;
                }
                Perk[] perks = selectedPerks.toArray(new Perk[selectedPerks.size()]);
                ClientScriptMap map = ClientScriptMap.getMap(10742);
                int[] materialsFieldIds = new int[5];
                for (int i = 0; i < materialsFieldIds.length; i++) {
                    materialsFieldIds[i] = addedMaterials[i] == -1 ? -1 : map.getIntValue(addedMaterials[i]);
                }
                int[][] materialsUsed = new int[addedMaterials.length][2];
                Map<Integer, Integer> totalMaterialsUsed = new HashMap<Integer, Integer>();
                double xp = 0.0;
                if (perks.length > 0) {
                    for (int i = 0; i < addedMaterials.length; i++) {
                        if (addedMaterials[i] == -1) {
                            materialsUsed[i] = new int[] { -1, -1 };
                            continue;
                        }
                        int requiredAmount = getRequiredMaterialAmount(addedMaterials[i]);
                        materials[addedMaterials[i]] -= requiredAmount;
                        xp += PerkGenerationDataParser.DATA.getComps()[addedMaterials[i]].getXp();
                        materialsUsed[i] = new int[] { addedMaterials[i], requiredAmount };
                        if (totalMaterialsUsed.containsKey(addedMaterials[i])) {
                            totalMaterialsUsed.put(addedMaterials[i], totalMaterialsUsed.get(addedMaterials[i]) + requiredAmount);
                        } else
                            totalMaterialsUsed.put(addedMaterials[i], requiredAmount);
                    }
                }
                int amountRepeats = Integer.MAX_VALUE;
                for (int i = 0; i < materials.length; i++) {
                    if (!totalMaterialsUsed.containsKey(i))
                        continue;
                    if ((materials[i] / totalMaterialsUsed.get(i)) < amountRepeats)
                        amountRepeats = materials[i] / totalMaterialsUsed.get(i);
                }
                if (perks.length == 0) {
                    player.getPackets().sendExecuteScript(12192, 0, 0, 0, 0, 0, amountRepeats, player.getInventory().getAmountOf(36719 + (gizmoType * 2)));
                } else {
                    Item gizmo = new Item(36720 + (gizmoType * 2), 1);
                    Gizmo gizmos = new Gizmo(perks, materialsUsed);
                    gizmo.setInventionData(new InventionData(gizmos));
                    player.getInventory().deleteItem(shellId, 1);
                    player.getInventory().addItem(gizmo);
                    double addedXp = player.getSkills().addXp(Skills.INVENTION, xp);
                    player.getPackets().sendExecuteScript(12192, perks[0].getId(), perks[0].getRank(), perks.length == 1 ? 0 : perks[1].getId(), perks.length == 1 ? 0 : perks[1].getRank(), (int) (addedXp * 10), amountRepeats, player.getInventory().getAmountOf(36719 + (gizmoType * 2)));
                }
                refreshMaterials();
            }
        });
    }

    private boolean isValidMaterial(int materialId) {
        return materials != null && materialId >= 0 && materialId < materials.length;
    }

    private int getRequiredMaterialAmount(int materialId) {
        if (!isValidMaterial(materialId))
            return Integer.MAX_VALUE;
        InventionDefinitions defs = InventionDefinitions.getData(ClientScriptMap.getMap(10742).getIntValue(materialId));
        return defs != null && defs.getDataInIndex(7) instanceof Integer && ((int) defs.getDataInIndex(7)) == 2 ? 5 : 1;
    }

    private boolean hasMaterialsForSlot(int materialId, int replacingIndex) {
        int required = 0;
        for (int i = 0; i < addedMaterials.length; i++) {
            int addedMaterial = i == replacingIndex ? materialId : addedMaterials[i];
            if (addedMaterial == materialId)
                required += getRequiredMaterialAmount(materialId);
        }
        return materials[materialId] >= required;
    }

    private boolean hasEnoughSelectedMaterials(boolean sendMessage) {
        Map<Integer, Integer> required = new HashMap<Integer, Integer>();
        for (int materialId : addedMaterials) {
            if (materialId == -1)
                continue;
            if (!isValidMaterial(materialId))
                return false;
            int amount = getRequiredMaterialAmount(materialId);
            required.put(materialId, required.containsKey(materialId) ? required.get(materialId) + amount : amount);
        }
        if (required.isEmpty()) {
            if (sendMessage)
                player.getPackets().sendGameMessage("You need to add at least one material to create a gizmo.");
            return false;
        }
        for (Entry<Integer, Integer> entry : required.entrySet()) {
            if (materials[entry.getKey()] < entry.getValue()) {
                if (sendMessage)
                    player.getPackets().sendGameMessage("You don't have enough " + InventionDefinitions.getMaterialName(entry.getKey()) + " to create that gizmo.");
                return false;
            }
        }
        return true;
    }

    public void openCheckPerksInterface(Item item) {
        int itemId = item.getId();
        if (itemId != 36720 && itemId != 36722 && itemId != 36724) {
            return;
        }
        if (item.getInventionData() == null || item.getInventionData().getGizmos() == null) {
            player.getPackets().sendGameMessage("This item doesn't have perks!");
            return;
        }
        player.getInterfaceManager().sendInterface(1713);
        Perk[] perks = item.getInventionData().getGizmos()[0].getPerks();
        if (perks == null) {
            player.getPackets().sendGameMessage("This item doesn't have perks!");
            return;
        }
        player.getPackets().sendExecuteScript(12170, item.getId(), perks[0].getId(), perks[0].getRank(), perks[1] == null ? 0 : perks[1].getId(), perks[1] == null ? 0 : perks[1].getRank());
    }

    public void openBagOfMaterialsInterface() {
        if (player.getInterfaceManager().containsScreenInter()) {
            player.getPackets().sendGameMessage("Please finish what you're doing before opening the materials bag.");
            return;
        }
        int[] skillIds = { Skills.CRAFTING, Skills.DIVINATION, Skills.SMITHING };
        for (int skillId : skillIds) {
            if (player.getSkills().getLevelForXp(skillId) < 80) {
                player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + 80 + " to do that.");
                return;
            }
        }
        refreshMaterials();
        player.stopAll();
        player.getInterfaceManager().sendInterface(1709);
    }

    public void refreshMaterials() {
        for (int i = 0; i < materials.length && i < MATERIALS_VARS.length; i++) {
            player.getVarBitManager().sendVar(MATERIALS_VARS[i], materials[i]);
        }
    }

    public void processCombatXp(int damage) {
        if (damage <= 0 || !hasChargePack)
            return;
        double totalXP = Math.ceil((double) damage / 2.5);
        if (totalXP == 0)
            return;
        totalXP *= 2;
        for (int slotId = 0; slotId < player.getEquipment().getItems().getItems().length; slotId++) {
            Item item = player.getEquipment().getItems().getItems()[slotId];
            if (item == null || item.getInventionData() == null || !isAugmentedItemPowered(item))
                continue;
            ItemDefinitions defs = item.getDefinitions();
            if (defs == null)
                continue;
            String name = defs.getName().toLowerCase();
            boolean tool = name.contains("augmented dragon hatchet") || name.contains("augmented dragon pickaxe") || name.contains("augmented crystal pickaxe") || name.contains("augmented crystal hatchet") || name.contains("augmented crystal fishing rod") || name.contains("augmented crystal tinderbox") || name.contains("augmented crystal hammer") || name.contains("augmented tavia's fishing rod") || name.contains("fishing rod-o-matic") || name.contains("pyro-matic") || name.contains("hammer-tron");
            if (tool)
                continue;
            boolean twoHanded = Equipment.isTwoHandedWeapon(item);
            double xp = (twoHanded ? 0.06 : defs.getEquipSlot() == Equipment.SLOT_SHIELD ? 0.02 : 0.04) * totalXP;
            Perk enlightened = player.getInventionManager().hasPerk(item, Perks.ENLIGHTENED);
            if (enlightened != null)
                xp *= (1.00 + (0.03 * (double) enlightened.getRank()));
            int levelBefore = InventionData.getItemLevel(player, item);
            item.getInventionData().setXp((item.getInventionData().getXp() + xp));
            int levelafter = InventionData.getItemLevel(player, item);
            if (levelafter > levelBefore) {
                player.getPackets().sendGameMessage("<col=FFFF00>Congratulations! Your " + item.getName() + " has gained a level! It is now level " + levelafter);
                if (levelafter >= 10)
                    player.getChargesManagerNew().useCharge(slotId, item);
            }
        }
        player.getEquipment().refreshItemContainer();
    }

    public void processSkillXp(int skillId, double xp, Item item) {
        if (item == null || item.getInventionData() == null || !isAugmentedItemPowered(item) || player.getEquipment().getItem(Equipment.SLOT_WEAPON) != item)
            return;
        double itemXp = skillId == Skills.FISHING || skillId == Skills.WOODCUTTING || skillId == Skills.MINING ? (xp * 0.118) : skillId == Skills.FIREMAKING ? (xp * 0.065) : skillId == Skills.SMITHING ? (xp * 0.078) : 0;
        if (itemXp <= 0)
            return;
        processSkillChargeDrain(item);
        Perk enlightened = player.getInventionManager().hasPerk(item, Perks.ENLIGHTENED);
        if (enlightened != null)
            itemXp *= (1.00 + (0.03 * (double) enlightened.getRank()));
        int levelBefore = InventionData.getItemLevel(player, item);
        item.getInventionData().setXp((item.getInventionData().getXp() + itemXp));
        int levelafter = InventionData.getItemLevel(player, item);
        if (levelafter > levelBefore) {
            player.getPackets().sendGameMessage("<col=FFFF00>Congratulations! Your " + item.getName() + " has gained a level! It is now level " + levelafter);
            if (levelafter >= 10)
                player.getChargesManagerNew().useCharge(Equipment.SLOT_WEAPON, item);
        }
        player.getEquipment().refreshItemContainer();
    }

    private long skillDrainCooldown;

    public void processSkillChargeDrain(Item item) {
        double drainRate = ItemConstants.getAugmentedItemDrainRate(player, item);
        if ((skillDrainCooldown == 0 || Utils.currentTimeMillis() >= skillDrainCooldown)) {
            skillDrainCooldown = Utils.currentTimeMillis() + 6000;
            double drain = 6.00 * drainRate * CHARGE_PACK_VAR_MULTIPLIER;
            setDivineCharges((int) (divineCharges - drain <= 0 ? 0 : divineCharges - drain));
            refreshDivineCharges();
        }
    }

    public boolean procGathering(Item weapon, int reasourceId, int skillId, double xp) {
        return procGathering(weapon, new Item(reasourceId, 1), skillId, xp);
    }

    public boolean procGathering(Item weapon, Item reasource, int skillId, double xp) {
        if (weapon == null || weapon.getInventionData() == null || !isAugmentedItemPowered(weapon) || player.getEquipment().getItem(Equipment.SLOT_WEAPON) != weapon)
            return true;
        Perk furnace = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.FURNACE) : null;
        if (furnace != null && Math.random() <= (0.05 * (double) furnace.getRank() * (furnace.hasIncreasedChance() ? 1.15 : 1.00))) {
            player.getSkills().addXp(skillId, xp);
            player.getPackets().sendGameMessage("<col=00FF00>Your furnace perk consumes some of what you gather and gives you extra XP.");
            return false;
        }
        Perk butterfingers = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.BUTTERFINGERS) : null;
        if (butterfingers != null && Math.random() <= (0.03 * (double) butterfingers.getRank() * (butterfingers.hasIncreasedChance() ? 1.15 : 1.00))) {
            if (reasource.getDefinitions().isDestroyItem()) {
                player.getPackets().sendGameMessage("<col=00FE00>Your butterfingers perk causes you to throw the items away.");
            } else {
                World.updateGroundItem(new Item(reasource.getId(), reasource.getAmount()).setAttributes(reasource.getAttributes()), new WorldTile(player), player, 60, 0, false);
                player.getPackets().sendGameMessage("<col=00FE00>Your butterfingers perk causes you to drop your items.");
            }
            return false;
        }
        Perk impSouled = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.IMP_SOULED) : null;
        if (impSouled != null && Math.random() <= (0.03 * (double) impSouled.getRank() * (impSouled.hasIncreasedChance() ? 1.15 : 1.00)) && player.getPrayer().getPrayerpoints() >= 30) {
            player.getPackets().sendGameMessage("<col=00ff00>Your imp-souled tool teleported your items to the bank.");
            player.getBank().addItem(reasource, true);
            player.getPrayer().drainPrayer(30);
            return false;
        }
        Perk confused = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.CONFUSED) : null;
        if (confused != null && Math.random() <= (0.01 * (double) confused.getRank())) {
            player.stopAll();
            WorldTile tile = new WorldTile(player);
            WorldTile teleTile = new WorldTile(player);
            while (teleTile.matches(new WorldTile(player))) {
                for (int trycount = 0; trycount < 10; trycount++) {
                    if (tile == null)
                        break;
                    teleTile = new WorldTile(tile, 2);
                    if (tile == null || teleTile == null)
                        break;
                    if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
                        break;
                    teleTile = tile;
                }
            }
            player.setNextWorldTile(teleTile);
            player.getPackets().sendGameMessage("<col=3CB71E>Your confused perk teleported you to a new location.");
            return true;
        }
        Perk cheapskate = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.CHEAPSKATE) : null;
        if (cheapskate != null && Math.random() <= (0.01 * (double) cheapskate.getRank())) {
            Item transmute = getTransmutedItem(reasource, true);
            if (transmute != null) {
                player.getInventory().addItemDrop(transmute);
                player.getPackets().sendGameMessage("<col=00ff00>Your cheapskate perk downgrades the gathered resource.");
                return false;
            }
        }
        Perk polishing = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.POLISHING) : null;
        if (polishing != null && Math.random() <= (0.03 * (double) polishing.getRank() * (polishing.hasIncreasedChance() ? 1.15 : 1.00))) {
            Item transmute = getTransmutedItem(reasource, false);
            if (transmute != null) {
                player.getInventory().addItemDrop(transmute);
                player.getPackets().sendGameMessage("<col=00ff00>Your polishing perk upgrades the gathered resource.");
                return false;
            }
        }
        Perk charitable = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.CHARITABLE) : null;
        boolean charitableActivated = charitable != null && Math.random() <= (0.01 * (double) charitable.getRank() * (charitable.hasIncreasedChance() ? 1.15 : 1.00));
        if (charitableActivated) {
            List<Player> possiblePlayers = new ArrayList<Player>();
            for (int index : World.getRegion(player.getRegionId()).getPlayerIndexes()) {
                Player p = World.getPlayers().get(index);
                if (p == null || p == player || p.hasFinished() || p.isDead() || !p.withinDistance(new WorldTile(player), 14))
                    continue;
                possiblePlayers.add(p);
            }
            if (possiblePlayers.isEmpty())
                return true;
            Player target = possiblePlayers.get(Utils.random(possiblePlayers.size()));
            if (target == null)
                return true;
            player.getPackets().sendGameMessage("<col=00ff00>Your charitable perk caused an item to appear at " + target.getDisplayName() + "'s feet.");
            target.getPackets().sendGameMessage("<col=00ff00>" + player.getDisplayName() + "'s charitable perk caused an item to appear at your feet: " + reasource.getName() + ".");
            World.updateGroundItem(new Item(reasource.getId(), 1), new WorldTile(target), target, 60, 0, true);
        }
        return true;
    }

    private Item getTransmutedItem(Item reasource, boolean downgrade) {
        if (reasource == null)
            return null;
        String name = reasource.getName().toLowerCase();
        int newId = -1;
        if (!downgrade && reasource.getId() == 1511)
            newId = 1521;
        switch (name) {
        case "raw shrimps":
            newId = downgrade ? -1 : 321;
            break;
        case "raw crayfish":
            newId = downgrade ? -1 : 345;
            break;
        case "raw sardine":
            newId = downgrade ? -1 : 345;
            break;
        case "raw herring":
            newId = downgrade ? 327 : 341;
            break;
        case "raw anchovies":
            newId = downgrade ? 317 : 341;
            break;
        case "raw mackerel":
            newId = downgrade ? 327 : 341;
            break;
        case "raw trout":
            newId = downgrade ? 327 : 331;
            break;
        case "raw cod":
            newId = downgrade ? 353 : 359;
            break;
        case "raw pike":
            newId = downgrade ? 335 : 331;
            break;
        case "raw salmon":
        case "raw salamon":
            newId = downgrade ? 335 : 359;
            break;
        case "raw tuna":
            newId = downgrade ? 341 : 363;
            break;
        case "raw lobster":
            newId = downgrade ? 359 : 363;
            break;
        case "raw bass":
            newId = downgrade ? 359 : 371;
            break;
        case "leaping trout":
            newId = downgrade ? 359 : 11330;
            break;
        case "raw swordfish":
            newId = downgrade ? 359 : 7944;
            break;
        case "raw monkfish":
            newId = downgrade ? 363 : 383;
            break;
        case "raw shark":
            newId = downgrade ? 7944 : 15264;
            break;
        case "raw cavefish":
            newId = downgrade ? 383 : 15270;
            break;
        case "raw rocktail":
            newId = downgrade ? 15264 : -1;
            break;
        case "copper ore":
        case "tin ore":
            newId = downgrade ? -1 : 440;
            break;
        case "iron ore":
            newId = downgrade ? (Utils.random(2) == 0 ? 436 : 438) : 453;
            break;
        case "silver ore":
            newId = downgrade ? -1 : 444;
            break;
        case "coal":
            newId = downgrade ? 440 : 447;
            break;
        case "mithril ore":
            newId = downgrade ? 453 : 449;
            break;
        case "adamantite ore":
            newId = downgrade ? 447 : 451;
            break;
        case "gold ore":
            newId = downgrade ? 442 : -1;
            break;
        case "runite ore":
            newId = downgrade ? 449 : -1;
            break;
        case "achey tree logs":
            newId = downgrade ? 1511 : 1521;
            break;
        case "oak logs":
            newId = downgrade ? 1511 : 1519;
            break;
        case "willow logs":
            newId = downgrade ? 1521 : 1517;
            break;
        case "teak logs":
            newId = downgrade ? 1519 : 6332;
            break;
        case "maple logs":
            newId = downgrade ? 1519 : 12581;
            break;
        case "mahogany logs":
            newId = downgrade ? 6333 : 1515;
            break;
        case "arctic pine logs":
            newId = downgrade ?1517 : 1515;
            break;
        case "eucalyptus logs":
            newId = downgrade ?1517: 1515;
            break;
        case "yew logs":
            newId = downgrade ? 12581 : 1513;
            break;
        case "magic logs":
            newId = downgrade ? 1515 : 29556;
            break;
        case "elder logs":
            newId = downgrade ? 1513 : -1;
            break;
        }
        return newId == -1 ? null : new Item(newId, reasource.getAmount());
    }

    private boolean isAugmentedItemPowered(Item item) {
        if (item == null || item.getInventionData() == null)
            return false;
        if (item.getDefinitions().usesChargesInside())
            return item.getId() != item.getDefinitions().getUnchargedItemId();
        return hasChargePack && divineCharges > 0 && item.getId() != item.getDefinitions().getUnchargedItemId();
    }

    public boolean sendWear(int slotId, int itemId, int charges, boolean wear2) {
        Item item = player.getInventory().getItem(slotId);
        if (item.getId() != 37546 && item.getId() != 39648 && item.getId() != 39652)
            return true;
        Item originalItem = item;
        int orignalId = originalItem.getId();
        ItemDefinitions defs = originalItem.getDefinitions();
        int augmentedId = defs.getAugmentedItemId();
        Item toGive = new Item(originalItem.getId());
        toGive.setChargesData(originalItem.getChargesData());
        toGive.setId(augmentedId);
        if ((!hasChargePack || divineCharges <= 0) && !toGive.getDefinitions().usesChargesInside())
            toGive.setId(toGive.getDefinitions().getUnchargedItemId());
        toGive.setInventionData(new InventionData(0));
        toGive.getInventionData().setOriginalItemId(orignalId);
        if (toGive.getChargesData() != null)
            toGive.setChargesData(null);
        player.getInventory().set(slotId, toGive);
        player.getInventory().refresh();
        if (wear2) {
            ButtonHandler.sendWear2(player, slotId, toGive.getId(), charges);
            player.getInventory().refresh();
            player.getEquipment().refresh(toGive.getDefinitions().getEquipSlot());
            player.getAppearence().generateAppearenceData();
        } else
            ButtonHandler.sendWear(player, slotId, toGive.getId());
        return false;
    }

    private void refreshDivineCharges() {
        player.getVarBitManager().sendVar(5984, divineCharges);
        player.getCombatDefinitions().refreshBonuses();
    }

    public void openDiscoveryInterface() {
        if (player.getInterfaceManager().containsScreenInter()) {
            player.getPackets().sendGameMessage("Please finish what you're doing before opening the discovery interface.");
            return;
        }
        int[] skillIds = { Skills.CRAFTING, Skills.DIVINATION, Skills.SMITHING };
        for (int skillId : skillIds) {
            if (player.getSkills().getLevelForXp(skillId) < 80) {
                player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + 80 + " to do that.");
                return;
            }
        }
        player.stopAll();
        player.getInterfaceManager().sendCentralInterfaceLargeInterface(1708);
        player.getPackets().sendUnlockIComponentOptionSlots(1708, 33, 0, ClientScriptMap.getMap(10743).getSize() - 1, 0, 1);
        player.getPackets().sendExecuteScript(12121);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                currentBluePrint = null;
                player.getInterfaceManager().setInterface(false, 1708, 71, 0);
                player.getPackets().sendHideIComponent(1708, 71, true);
                player.getInterfaceManager().removeCentralInterfaceLargeInterface();
            }
        });
    }

    public void openAnalysisInterface(int itemId) {
        if (player.getInterfaceManager().containsScreenInter()) {
            player.getPackets().sendGameMessage("Please finish what you're doing before opening the material analysis interface.");
            return;
        }
        int[] skillIds = { Skills.CRAFTING, Skills.DIVINATION, Skills.SMITHING };
        for (int skillId : skillIds) {
            if (player.getSkills().getLevelForXp(skillId) < 80) {
                player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + 80 + " to do that.");
                return;
            }
        }
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        ItemDisassembleData data = ItemDisassembleDataParser.getItemDisassembleData(player, itemId);
        if (data == null) {
            data = ItemDisassembleDataParser.getItemDisassembleData(player, defs.certId);
            if (data == null || data.getComponents() == null || data.getComponents().length == 0) {
                player.getPackets().sendGameMessage("You can't disassemble this item.");
                return;
            }
        }
        player.stopAll();
        player.getInterfaceManager().sendInterface(1048);
        String materials = "";
        ClientScriptMap map = ClientScriptMap.getMap(10742);
        int count = 0;
        boolean hasSpecial = false;
        for (Component comp : data.getComponents()) {
            if (comp.getChance() == 1) {
                materials += "* <col=ffffff>" + InventionDefinitions.getDataName(map.getIntValue(comp.getId())) + "</col> (Always)<br>";
                hasSpecial = true;
            }
        }
        for (Component comp : data.getComponents()) {
            if (count >= 10)
                continue;
            if (comp.getChance() <= 0.05) {
                materials += "* <col=ffffff>" + InventionDefinitions.getDataName(map.getIntValue(comp.getId())) + "</col> (Rarely)<br>";
                count++;
            }
        }
        for (Component comp : data.getComponents()) {
            if (count >= 10)
                continue;
            if (comp.getChance() > 0.05 && comp.getChance() <= 0.15) {
                materials += "* <col=ffffff>" + InventionDefinitions.getDataName(map.getIntValue(comp.getId())) + "</col> (Sometimes)<br>";
                count++;
            }
        }
        for (Component comp : data.getComponents()) {
            if (count >= 10)
                continue;
            if (comp.getChance() > 0.15 && comp.getChance() < 1) {
                materials += "* <col=ffffff>" + InventionDefinitions.getDataName(map.getIntValue(comp.getId())) + "</col> (Often)<br>";
                count++;
            }
        }
        if (count >= 10) {
            materials += "* ... and other materials.";
        }
        player.getPackets().sendExecuteScript(9727, "Material analysis", "<col=ffff00>" + defs.getName() + "</col><br><br>Junk chance: <col=ffffff>" + (data.getJunkChance() * getJunkChanceReductionMultiplier()) + "%</col><br>Chances for materials: <col=ffffff>" + data.getMaterialCount() + "</col>" + (hasSpecial ? " (excludes special)" : "") + "<br><br>This may disassemble into:<br>" + materials, "Ok", "", "", -1);
    }

    public double getJunkChanceReductionMultiplier() {
        for (int i = 8; i >= 0; i--) {
            if (hasDiscoveredBluePrint(59 + i))
                return 1.00 - (i >= 6 && i <= 8 ? (0.14 + ((i - 6) * 0.03)) : i == 5 ? 0.12 : (0.01 + (i * 0.02)));
        }
        return 1;
    }

    public void handleInterface(int interfaceId, int componentId, int slotId, int slotId2) {
        if (interfaceId == 1708) {
            if (componentId >= 43 && componentId <= 52) {
                if (currentBluePrint == null)
                    return;

                currentBluePrint.addModul(componentId - 42);
            } else if (componentId >= 23 && componentId <= 27) {
                if (currentBluePrint == null)
                    return;
                if (currentBluePrint.getStage() == 2)
                    currentBluePrint.addModul(componentId - 22);
                else
                    currentBluePrint.removeModul(componentId - 23);
            } else if (componentId >= 14 && componentId <= 18) {
                int index = componentId - 14;
                int selectedIndex = currentBluePrint.getSelectedIndex();
                if (selectedIndex != -1) {
                    currentBluePrint.switchModul(index, selectedIndex);
                    currentBluePrint.setSelectedIndex(-1);
                    return;
                }
                currentBluePrint.setSelectedIndex(index);
            } else if (componentId == 33) {
                if (currentBluePrint != null && currentBluePrint.getBluePrintId() == slotId)
                    return;
                currentBluePrint = new BluePrint(slotId).start();
            } else if (componentId == 80) {
                currentBluePrint.createPrototype();
            } else if (componentId == 103) {
                currentBluePrint.invent();
            }
        } else if (interfaceId == 1530) {
            if (currentBluePrint == null)
                return;
            if (currentBluePrint.getStage() == 3) {
                player.getInterfaceManager().removeInterface(1530);
                player.getInterfaceManager().setInterface(false, 1708, 71, 0);
                player.getPackets().sendHideIComponent(1708, 71, true);
                player.getPackets().sendExecuteScript(12130, -1, 0, "", 0, 0);
                player.getPackets().sendExecuteScript(12116);
                currentBluePrint = null;
                return;
            }
            if (componentId == 34 || componentId == 18) {
                player.getInterfaceManager().removeInterface(1530);
                player.getInterfaceManager().setInterface(false, 1708, 71, 0);
                player.getPackets().sendHideIComponent(1708, 71, true);
                currentBluePrint.setStage(2);
            } else if (componentId == 28) {
                currentBluePrint.setStage(3);
                currentBluePrint.invent();
            }
        } else if (interfaceId == 1712) {
            if (componentId == 2) {
                clearMaterials();
            } else if (componentId == 3) {
                int index = slotId == 4 ? 0 : slotId == 1 ? 1 : slotId == 3 ? 2 : slotId == 7 ? 4 : 3;
                removeMaterial(index);
            } else if (componentId == 6) {
                addMaterial(slotId);
            } else if (componentId == 24 || componentId == 41) {
                createGizmo();
            } else if (componentId == 58) {
                openAddMaterialsInterface(36719 + (gizmoType * 2));
            }
        }
    }

    public void handleSwitchComponents(int fromInterfaceId, int fromComponentId, int toInterfaceId, int toComponentId, int fromSlot, int toSlot) {
        if (fromInterfaceId == 1708 && toInterfaceId == 1708) {
            if (currentBluePrint == null)
                return;
            if (fromComponentId >= 43 && fromComponentId <= 52 && toComponentId >= 23 && toComponentId <= 27) {
                currentBluePrint.setModul(toComponentId - 23, fromComponentId - 42);
            } else if (fromComponentId == 29 || fromComponentId >= 116 && fromComponentId <= 122) {
                int fromIndex = fromComponentId == 29 ? 0 : fromComponentId == 116 ? 1 : fromComponentId == 118 ? 2 : fromComponentId == 120 ? 3 : 4;
                if (toComponentId >= 23 && toComponentId <= 27 && currentBluePrint.getStage() == 1) {
                    currentBluePrint.switchModul(fromIndex, toComponentId - 23);
                } else if (toComponentId == 42 && currentBluePrint.getStage() == 1) {
                    currentBluePrint.removeModul(fromIndex);
                } else if (toComponentId >= 14 && toComponentId <= 18) {
                    currentBluePrint.setModul(toComponentId - 14, fromIndex + 1);
                }
            } else if (fromComponentId >= 14 && fromComponentId <= 18 && toComponentId >= 14 && toComponentId <= 18) {
                currentBluePrint.switchModul(toComponentId - 14, fromComponentId - 14);
            }
        } else if (fromInterfaceId == 1712 && toInterfaceId == 1712) {
            if (fromComponentId == 6 && toComponentId == 3) {
                int index = toSlot == 4 ? 0 : toSlot == 1 ? 1 : toSlot == 3 ? 2 : toSlot == 7 ? 4 : toSlot == 5 ? 3 : -1;
                if (index == -1) {
                    interactionIndex++;
                    refreshAddMaterialsInterface();
                    return;
                }
                addMaterial(index, fromSlot, true);
            } else if (fromComponentId == 3 && toComponentId == 3) {
                int fromIndex = fromSlot == 4 ? 0 : fromSlot == 1 ? 1 : fromSlot == 3 ? 2 : fromSlot == 7 ? 4 : 3;
                int toIndex = toSlot == 4 ? 0 : toSlot == 1 ? 1 : toSlot == 3 ? 2 : toSlot == 7 ? 4 : toSlot == 5 ? 3 : -1;
                if (toIndex == -1) {
                    removeMaterial(fromIndex);
                    return;
                }
                switchMaterials(fromIndex, toIndex);
            }
        }
    }

    public boolean hasDiscoveredBluePrint(int index) {
        return discoveredBluePrints != null && index >= 0 && index < discoveredBluePrints.length && discoveredBluePrints[index];
    }

    public int[] getMaterials() {
        return materials;
    }

    public boolean hasChargePack() {
        return hasChargePack;
    }

    public void setHasChargePack(boolean hasChargePack) {
        boolean hadChargePack = this.hasChargePack;
        this.hasChargePack = hasChargePack;
        if (hasChargePack && !hadChargePack && divineCharges <= 0)
            setDivineCharges(STARTING_CHARGE_PACK_CHARGES);
        player.getVarBitManager().sendVarBit(30225, hasChargePack ? 1 : 0);
    }

    public boolean augmentItem(Item used, int fromSlot, Item usedWith, int toSlot) {
        final Item augmentor = used.getId() == 36725 ? used : usedWith.getId() == 36725 ? usedWith : null;
        final Item originalItem = augmentor == null ? null : augmentor == used ? usedWith : used;
        if (augmentor == null || originalItem == null)
            return false;
        ItemDefinitions defs = originalItem.getDefinitions();
        if (defs.isNoted()) {
            player.getPackets().sendGameMessage("You can't augment a noted item.");
            return true;
        }
        int augmentedId = defs.getAugmentedItemId();
        if (augmentedId == 0 || originalItem.getId() == 37546 || originalItem.getId() == 39648 || originalItem.getId() == 39652) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You can't seem to work out how to augment that item.");
            return true;
        }
        if (originalItem.getInventionData() != null) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You cannot augment an already augmented item.");
            return true;
        }
        int category = defs.itemCategory;
        ClientScriptMap map = ClientScriptMap.getMap(10743);
        int dataId = map.getIntValue(8);
        if (!hasDiscoveredBluePrint(8) && (category == 7 || category == 18)) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment " + originalItem.getName() + ".");
            return true;
        }
        dataId = map.getIntValue(9);
        if (!hasDiscoveredBluePrint(9) && (category == 8 || category == 10)) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment " + originalItem.getName() + ".");
            return true;
        }
        dataId = map.getIntValue(10);
        if (!hasDiscoveredBluePrint(10) && (category == 67)) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment " + originalItem.getName() + ".");
            return true;
        }
        dataId = map.getIntValue(12);
        if (!hasDiscoveredBluePrint(12) && (category == 35)) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment " + originalItem.getName() + ".");
            return true;
        }
        Manufacture.ManufactureData manufactureData = defs.getCSOpcode(5551) != 0 ? null : Manufacture.getManufactureData(augmentedId);
        if (defs.getCSOpcode(5551) == 0 && manufactureData == null) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You can't seem to work out how to augment that item.");
            return true;
        }
        int[][] requiredItems = defs.getCSOpcode(5551) != 0 ? new int[][] { { 36725, 1 }, { originalItem.getId(), 1 } } : manufactureData.getRequiredItems();
        for (int i = 0; i < requiredItems.length; i++) {
            int itemId = requiredItems[i][0];
            int amount = requiredItems[i][1];
            if (itemId == originalItem.getId())
                continue;
            if (!player.getInventory().containsItem(itemId, amount)) {
                player.getPackets().sendGameMessage("You need " + amount + " x " + ItemDefinitions.getItemDefinitions(itemId).getName() + " to augment " + originalItem.getName() + ".");
                return false;
            }
        }
//        if (originalItem.getChargesData() != null) {
//            int maxCharges = originalItem.getDefinitions().getRepairData() != null ? ItemDefinitions.getItemDefinitions(originalItem.getDefinitions().getRepairData()[0]).getMaxCharges() : originalItem.getDefinitions().getMaxCharges();
//            int chargesLeft = player.getChargesManagerNew().getChargesLeft(originalItem);
//            if (chargesLeft < maxCharges) {
//                player.getPackets().sendGameMessage("You can't augment non fully charged items.");
//                return false;
//            }
//        }
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                ItemDefinitions defs = originalItem.getDefinitions();
                player.getInterfaceManager().sendInterface(1048);
                player.getPackets().sendExecuteScript(9727, "Augment", "This will convert " + defs.getName() + " into an augmented version which requires divine charges to use.<br><br>This process cannot be undone without using an augmentation dissolver.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == 19) {
                    if (!player.getInventory().containsItem(originalItem)) {
                        end();
                        return;
                    }
                    int slotId = player.getInventory().getItemSlot(originalItem);
                    if (slotId == -1) {
                        end();
                        return;
                    }
                    int orignalId = originalItem.getId();
                    for (int i = 0; i < requiredItems.length; i++) {
                        int itemId = requiredItems[i][0];
                        if (itemId == orignalId)
                            continue;
                        int amount = requiredItems[i][1];
                        player.getInventory().removeItemMoneyPouch(new Item(itemId, amount));
                    }
                    Item toGive = new Item(originalItem.getId());
                    toGive.setChargesData(originalItem.getChargesData());
                    toGive.setId(augmentedId);
                    if ((!hasChargePack || divineCharges <= 0) && !toGive.getDefinitions().usesChargesInside())
                        toGive.setId(toGive.getDefinitions().getUnchargedItemId());
                    toGive.setInventionData(new InventionData(0));
                    toGive.getInventionData().setOriginalItemId(orignalId);
                    if (toGive.getChargesData() != null)
                        toGive.setChargesData(null);
                    player.getInventory().set(slotId, toGive);
                    player.getInventory().refresh();
                    player.getPackets().sendPlayerMessageBox("You attach the augmentor, upgrading the item to an augmented version: " + originalItem.getName());
                }
                end();
            }

            @Override
            public void finish() {

            }

        });
        return true;
    }

    public boolean disolveItem(Item used, Item usedWith) {
        final Item disolver = used.getId() == 36961 ? used : usedWith.getId() == 36961 ? usedWith : null;
        final Item augmentedItem = disolver == null ? null : disolver == used ? usedWith : used;
        if (disolver == null || augmentedItem == null)
            return false;
        if (augmentedItem.getInventionData() == null)
            return false;
        if (augmentedItem.getUnAugmentedItemId() == 0)
            return false;
        ItemDefinitions originalDefinitions = ItemDefinitions.getItemDefinitions(augmentedItem.getUnAugmentedItemId());
        Integer[] repairData = ItemDefinitions.getItemDefinitions(augmentedItem.getUnAugmentedItemId()).getRepairData();
        if (repairData != null && repairData.length == 1)
            originalDefinitions  = ItemDefinitions.getItemDefinitions(repairData[0]);
        Integer[] degradeData = originalDefinitions.getItemDegradeData();
        if ((degradeData != null && degradeData.length == 2 && degradeData[1] == -1) || ChargesManagerNew.getUnDyedVersion(augmentedItem.getDefinitions().getChargedItemId()) != -1) {
            player.getPackets().sendGameMessage("You can't remove augmentation from this item.");
            return false;
        }
        String name = augmentedItem.getName().toLowerCase();
        if (name.contains("hammer-tron") || name.contains("pyro-matic") || name.contains("fishing rod-o-matic")) {
            player.getPackets().sendGameMessage("You can't remove augmentation from this item.");
            return false;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                ItemDefinitions defs = augmentedItem.getDefinitions();
                int originalId = degradeData != null ? degradeData[1] : augmentedItem.getUnAugmentedItemId();
                player.getInterfaceManager().sendInterface(1048);
                player.getPackets().sendExecuteScript(9727, "Remove Augmentation", "You are about to remove an augmentation from: " + defs.getName() + "<br><br>This will change into: " + ItemDefinitions.getItemDefinitions(originalId).getName() + "<br><br>This cannot be undone.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == 19) {
                    if (!player.getInventory().containsItem(augmentedItem) || !player.getInventory().containsItem(disolver)){
                        end();
                        return;
                    }
                    ItemDefinitions defs = augmentedItem.getDefinitions();
                    int originalId = degradeData != null ? degradeData[1] : augmentedItem.getUnAugmentedItemId();
                    augmentedItem.setId(originalId);
                    augmentedItem.setInventionData(null);
                    player.getInventory().deleteItem(36961, 1);
                    player.getInventory().refresh();
                    player.getPackets().sendPlayerMessageBox("You remove the augmentation from: " + defs.getName() + ", reverting the item back to: " + ItemDefinitions.getItemDefinitions(originalId).getName());
                }
                end();
            }

            @Override
            public void finish() {

            }

        });
        return true;
    }

    public boolean installGizmo(Item used, Item usedWith) {
        final Item gizmo = (used.getId() == 36720 || used.getId() == 36722 || used.getId() == 36724) ? used : (usedWith.getId() == 36720 || usedWith.getId() == 36722 || usedWith.getId() == 36724) ? usedWith : null;
        final Item augmentedItem = gizmo == null ? null : gizmo == used ? usedWith : used;
        if (gizmo == null || augmentedItem == null)
            return false;
        if (gizmo.getInventionData() == null || augmentedItem.getInventionData() == null)
            return false;
        if (augmentedItem.getUnAugmentedItemId() == 0 && augmentedItem.getDefinitions().getAugmentedItemId() == 0)
            return false;
        ItemDefinitions defs = augmentedItem.getDefinitions();
        int itemCategory = defs.itemCategory;
        int gizmoType = (gizmo.getId() - 36720) / 2;
        int augmentedItemType = (itemCategory == 35 || itemCategory == 67) ? 2 : (itemCategory == 7 || itemCategory == 8 || itemCategory == 18 || itemCategory == 10) ? 1 : 0;
        if (defs.getEquipSlot() == Equipment.SLOT_CHEST || defs.getEquipSlot() == Equipment.SLOT_LEGS)
            augmentedItemType = 1;
        String name = defs.getName().toLowerCase();
        if (name.contains("fishing rod-o-matic") || name.contains("pyro-matic") || name.contains("hammer-tron") || name.contains(" hatchet") || name.contains("pickaxe") || name.contains(" hammer") || name.contains("tinderbox") || name.contains("fishing rod"))
            augmentedItemType = 2;
        if (gizmoType != augmentedItemType) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You can only use that gizmo on augmented " + (gizmoType == 2 ? "tools" : gizmoType == 1 ? "armour" : "weapons") + ".");
            return true;
        }
        int gizmosCount = augmentedItem.getInventionData().getGizmosCount();
        boolean twoHanded = Equipment.isTwoHandedWeapon(augmentedItem) || name.contains("fishing rod-o-matic");
        int equipSlot = defs.getEquipSlot();
        int maxGizmos = ((!twoHanded && (equipSlot == Equipment.SLOT_WEAPON || equipSlot == Equipment.SLOT_SHIELD))) ? 1 : 2;
        Gizmo[] gizmos = augmentedItem.getInventionData().getGizmos();
        if (gizmosCount >= maxGizmos) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>You can't install more than " + (maxGizmos == 2 ? "two gizmos" : "one gizmo") + " to this item.");
            return true;
        }
        int gizmoSlot = player.getInventory().getItemSlot(gizmo);
        if (gizmoSlot == -1)
            return false;
        player.getInventory().getItems().set(gizmoSlot, null);
        for (int i = 0; i < gizmos.length; i++) {
            if (gizmos[i] == null) {
                gizmos[i] = gizmo.getInventionData().getGizmos()[0];
                break;
            }
        }
        player.getInventory().refresh();
        player.getPackets().sendPlayerMessageBox("You successfully install the gizmo");
        return true;
    }

    public boolean disolveGizmo(Item used, Item usedWith) {
        final Item disolver = used.getId() == 36726 ? used : usedWith.getId() == 36726 ? usedWith : null;
        final Item augmentedItem = disolver == null ? null : disolver == used ? usedWith : used;
        if (disolver == null || augmentedItem == null)
            return false;
        if (augmentedItem.getInventionData() == null)
            return false;
        if (augmentedItem.getUnAugmentedItemId() == 0 && augmentedItem.getDefinitions().getAugmentedItemId() == 0)
            return false;
        int gizmoCount = augmentedItem.getInventionData().getGizmosCount();
        if (gizmoCount == 0) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>This item does not have any gizmos installed.");
            return true;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            private int selectedIndex;

            @Override
            public void start() {
                Gizmo[] gizmos = augmentedItem.getInventionData().getGizmos();
                if (gizmoCount == 1) {
                    sendConfirmGizmo(gizmos[0] == null ? 1 : 0);
                    return;
                }
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Dissolve Gizmo #1", "Dissolve Gizmo #2", "Nevermind.");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                case -1:
                    switch (componentId) {
                    case OPTION_1:
                    case OPTION_2:
                        sendConfirmGizmo(componentId == OPTION_2 ? 1 : 0);
                        break;
                    case OPTION_3:
                        end();
                        break;
                    }
                    break;
                case 0:
                    if (componentId == 19) {
                        if (!player.getInventory().containsItem(augmentedItem) || !player.getInventory().containsItem(disolver)) {
                            end();
                            return;
                        }
                        augmentedItem.getInventionData().getGizmos()[selectedIndex] = null;
                        player.getInventory().deleteItem(36726, 1);
                        player.getInventory().refresh();
                        player.getPackets().sendPlayerMessageBox("You successfully remove the gizmo");
                    }
                    end();
                    break;
                }
            }

            public void sendConfirmGizmo(int index) {
                stage = 0;
                ItemDefinitions defs = augmentedItem.getDefinitions();
                selectedIndex = index;
                player.getInterfaceManager().closeChatBoxInterface();
                player.getInterfaceManager().sendInterface(1048);
                player.getPackets().sendExecuteScript(9727, "Remove Gizmo #" + (index + 1), "You are about to remove Gizmo #" + (index + 1) + " from: " + defs.getName() + "<br><br>This cannot be undone.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
            }

            @Override
            public void finish() {

            }
        });
        return true;
    }

    public boolean dissolveEquipment(Item used, Item usedWith) {
        final Item disolver = used.getId() == 36728 ? used : usedWith.getId() == 36728 ? usedWith : null;
        final Item augmentedItem = disolver == null ? null : disolver == used ? usedWith : used;
        if (disolver == null || augmentedItem == null)
            return false;
        if (augmentedItem.getInventionData() == null)
            return false;
        if (augmentedItem.getUnAugmentedItemId() == 0 && augmentedItem.getDefinitions().getAugmentedItemId() == 0)
            return false;
        int gizmosCount = augmentedItem.getInventionData().getGizmosCount();
        if (gizmosCount == 0) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>This item does not have any gizmos installed.");
            return true;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                ItemDefinitions defs = augmentedItem.getDefinitions();
                player.getInterfaceManager().sendInterface(1048);
                player.getPackets().sendExecuteScript(9727, "Dissolve Equipment", "You are about to destroy: " + defs.getName() + "<br><br>This will Destroy the item and return all installed gizmos. <br><br>This cannot be undone.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == 19) {
                    if (!player.getInventory().containsItem(augmentedItem) || !player.getInventory().containsItem(disolver)) {
                        end();
                        return;
                    }
                    ItemDefinitions defs = augmentedItem.getDefinitions();
                    int augmentedItemSlot = player.getInventory().getItemSlot(augmentedItem);
                    if (augmentedItemSlot == -1) {
                        end();
                        return;
                    }
                    int itemCategory = defs.itemCategory;
                    int augmentedItemType = (itemCategory == 35 || itemCategory == 67) ? 2 : (itemCategory == 7 || itemCategory == 8 || itemCategory == 18 || itemCategory == 10) ? 1 : 0;
                    if (defs.getEquipSlot() == Equipment.SLOT_CHEST || defs.getEquipSlot() == Equipment.SLOT_LEGS)
                        augmentedItemType = 1;
                    int gizmoId = 36720 + 2 * augmentedItemType;
                    player.getInventory().getItems().set(augmentedItemSlot, null);
                    player.getInventory().deleteItem(36728, 1);
                    for (int i = 0; i < augmentedItem.getInventionData().getGizmos().length; i++) {
                        if (augmentedItem.getInventionData().getGizmos()[i] != null) {
                            Item gizmo = new Item(gizmoId, 1);
                            gizmo.setInventionData(new InventionData(augmentedItem.getInventionData().getGizmos()[i]));
                            player.getInventory().addItem(gizmo);
                        }
                    }
                    player.getInventory().refresh();
                    player.getPackets().sendPlayerMessageBox("You successfully extract the gizmos.");
                }
                end();
            }

            @Override
            public void finish() {

            }

        });
        return true;
    }

    public boolean siphonEquipment(Item used, Item usedWith) {
        final Item siphon = used.getId() == 36730 || used.getId() == 38872 ? used : usedWith.getId() == 36730 || usedWith.getId() == 38872 ? usedWith : null;
        final Item augmentedItem = siphon == null ? null : siphon == used ? usedWith : used;
        if (siphon == null || augmentedItem == null)
            return false;
        if (augmentedItem.getInventionData() == null)
            return false;
        if (augmentedItem.hasGizmo())
            return false;
        boolean crystalSiphon = siphon.getId() == 38872;
        if (crystalSiphon && !augmentedItem.getDefinitions().getName().toLowerCase().contains("crystal")) {
            player.getPackets().sendGameMessage("You can only use this to siphon crystal equipment.");
            return true;
        }
        int itemLevel = InventionData.getItemLevel(player, augmentedItem);
        if (itemLevel <= 3) {
            player.getPackets().sendGameMessage("Items below level 4 cannot be siphoned.");
            return true;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                player.getInterfaceManager().sendInterface(1048);
                int baseXp = (int) Disassemble.getDisassembleXP(itemLevel - 2);
                int tier = crystalSiphon ? 90 : augmentedItem.getDefinitions().getCSOpcode(750);
                if (tier < 60)
                    tier = 70;
                baseXp *= (1.00 + (1.50 * ((double) (tier - 80) / 100.00)));
                player.getPackets().sendExecuteScript(9727, "SIPHON EQUIPMENT", "Are you sure you want to siphon your item?<br><br>It is currently level <col=ffffff>" + itemLevel + "</col>.<br><br><img=6>You will gain <col=ffffff>" + Utils.getFormattedNumber(baseXp) + "</col> Invention XP.<br><br><col=ff0000>if you siphon this, it's xp will be completly reset.", "SIPHON", "Cancel", "", -1, 48);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == 19) {
                    if (!player.getInventory().containsItem(augmentedItem) || !player.getInventory().containsItem(siphon)) {
                        end();
                        return;
                    }
                    int baseXp = (int) Disassemble.getDisassembleXP(itemLevel - 2);
                    int tier = crystalSiphon ? 90 : augmentedItem.getDefinitions().getCSOpcode(750);
                    if (tier < 60)
                        tier = 70;
                    baseXp *= (1.00 + (1.50 * ((double) (tier - 80) / 100.00)));
                    player.getSkills().addXp(Skills.INVENTION, baseXp);
                    boolean consumeSiphon = itemLevel < 16 && (itemLevel < 13 || (Math.random() <= 0.5));
                    if (consumeSiphon)
                        player.getInventory().deleteItem(siphon.getId(), 1);
                    augmentedItem.getInventionData().setXp(0);
                    player.getInventory().refresh();
                    player.getPackets().sendPlayerMessageBox("You successfully siphon the equipment.");
                    if (!consumeSiphon)
                        player.getPackets().sendGameMessage("Your siphon wasn't consumed in the process.", true);
                }
                end();
            }

            @Override
            public void finish() {

            }
        });
        return true;
    }

    public boolean seperateEquipment(Item used, Item usedWith) {
        final Item seperator = used.getId() == 41079 ? used : usedWith.getId() == 41079 ? usedWith : null;
        final Item augmentedItem = seperator == null ? null : seperator == used ? usedWith : used;
        if (seperator == null || augmentedItem == null)
            return false;
        if (augmentedItem.getInventionData() == null)
            return false;
        if (augmentedItem.hasGizmo())
            return false;
        int gizmosCount = augmentedItem.getInventionData().getGizmosCount();
        if (gizmosCount == 0) {
            player.getPackets().sendPlayerMessageBox("<col=ff0000>This item does not have any gizmos installed.");
            return true;
        }
        int itemLevel = InventionData.getItemLevel(player, augmentedItem);
        if (itemLevel < 15) {
            player.getPackets().sendGameMessage("Items below level 15 cannot be seperated.");
            return true;
        }
        ItemDefinitions defs = augmentedItem.getDefinitions();
        int itemCategory = defs.itemCategory;
        int augmentedItemType = (itemCategory == 35 || itemCategory == 67) ? 2 : (itemCategory == 7 || itemCategory == 8 || itemCategory == 18 || itemCategory == 10) ? 1 : 0;
        if (defs.getEquipSlot() == Equipment.SLOT_CHEST || defs.getEquipSlot() == Equipment.SLOT_LEGS)
            augmentedItemType = 1;
        int gizmoId = 36720 + 2 * augmentedItemType;
        String name = defs.getName().toLowerCase();
        if (name.contains("fishing rod-o-matic") || name.contains("pyro-matic") || name.contains("hammer-tron") || name.contains(" hatchet") || name.contains("pickaxe") || name.contains(" hammer") || name.contains("tinderbox") || name.contains("fishing rod"))
            augmentedItemType = 2;
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                ItemDefinitions defs = augmentedItem.getDefinitions();
                player.getInterfaceManager().sendInterface(1048);
                player.getPackets().sendExecuteScript(9727, "Seperate Equipment", "You are about to seperate the gizmos from: " + defs.getName() + "<br><br>This will return all installed gizmos. <br><br>This cannot be undone.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == 19) {
                    if (!player.getInventory().containsItem(augmentedItem) || !player.getInventory().containsItem(seperator)) {
                        end();
                        return;
                    }
                    boolean consumeSeperator = itemLevel >= 19 ? (Math.random() <= 0.50) : itemLevel < 17 || (Math.random() <= 0.75);
                    if (consumeSeperator)
                        player.getInventory().deleteItem(41079, 1);
                    for (int i = 0; i < augmentedItem.getInventionData().getGizmos().length; i++) {
                        if (augmentedItem.getInventionData().getGizmos()[i] != null) {
                            Item gizmo = new Item(gizmoId, 1);
                            gizmo.setInventionData(new InventionData(augmentedItem.getInventionData().getGizmos()[i]));
                            player.getInventory().addItemDrop(gizmo);
                            augmentedItem.getInventionData().getGizmos()[i] = null;
                        }
                    }
                    player.getInventory().refresh();
                    player.getPackets().sendPlayerMessageBox("You successfully seperate the gizmos from your item.");
                    player.getPackets().sendGameMessage("Your gizmos have been added to your inventory.");
                    if (!consumeSeperator)
                        player.getPackets().sendGameMessage("Your seperator wasn't consumed in the process.", true);
                }
                end();
            }

            @Override
            public void finish() {

            }

        });
        return true;
    }

    public double getDrainReductionModifier() {
        if (hasDiscoveredBluePrint(53))
            return 0.80;
        if (hasDiscoveredBluePrint(52))
            return 0.83;
        if (hasDiscoveredBluePrint(51))
            return 0.86;
        if (hasDiscoveredBluePrint(50))
            return 0.88;
        if (hasDiscoveredBluePrint(49))
            return 0.91;
        if (hasDiscoveredBluePrint(48))
            return 0.93;
        if (hasDiscoveredBluePrint(47))
            return 0.95;
        if (hasDiscoveredBluePrint(46))
            return 0.97;
        if (hasDiscoveredBluePrint(45))
            return 0.99;
        return 1;
    }

    public boolean checkAugmentedItem(int slot, Item item, boolean equipment) {
        if (item.getInventionData() == null)
            return false;
        player.getInterfaceManager().sendInterface(1711);
        player.getPackets().sendExecuteScript(12199, equipment ? 94 : 93, slot, (int) (ItemConstants.getAugmentedItemDrainRate(player, item) * 1800), 1);
        return true;
    }

    public double getTotalEquipedItemsDrainRate() {
        if (!hasChargePack || divineCharges <= 0)
            return 0.00;
        double drainRate = 0.00;
        for (int i = 0; i < player.getEquipment().getItems().getItems().length; i++) {
            Item item = player.getEquipment().getItems().get(i);
            if (item != null && item.getInventionData() != null && !item.hasGizmo()) {
                String name = item.getDefinitions().getName().toLowerCase();
                boolean tool = name.contains("augmented dragon hatchet") || name.contains("augmented dragon pickaxe") || name.contains("augmented crystal pickaxe") || name.contains("augmented crystal hatchet") || name.contains("augmented crystal fishing rod") || name.contains("augmented crystal tinderbox") || name.contains("augmented crystal hammer") || name.contains("augmented tavia's fishing rod") || name.contains("fishing rod-o-matic") || name.contains("pyro-matic") || name.contains("hammer-tron");
                if (tool)
                    continue;
                drainRate += ItemConstants.getAugmentedItemDrainRate(player, item);
            }
        }
        return drainRate;
    }

    public void refreshEquipedItemsDrainRate() {
        double drainRate = getTotalEquipedItemsDrainRate();
        player.getVarBitManager().sendVar(5991, (int) (drainRate * 1800));
        onEquipmentChange();
    }

    public void setDivineCharges(int divineCharges) {
        int chargesBefore = this.divineCharges;
        this.divineCharges = divineCharges;
        refreshDivineCharges();
        refreshAugmentedItems(chargesBefore);
    }

    private long drainCoolDown;

    public void process() {
        double drainRate = getTotalEquipedItemsDrainRate();
        if (drainRate > 0) {
            if ((drainCoolDown == 0 || Utils.currentTimeMillis() >= drainCoolDown) && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
                drainCoolDown = Utils.currentTimeMillis() + 6000;
                double drain = 6.00 * drainRate * CHARGE_PACK_VAR_MULTIPLIER;
                setDivineCharges((int) (divineCharges - drain <= 0 ? 0 : divineCharges - drain));
                refreshDivineCharges();
            }
        }
        processCracklingPerk();
    }

    public void resetCombatDrain() {
        drainCoolDown = 0;
    }

    public void refreshAugmentedItems(int chargesBefore) {
        int currentDivineCharges = this.divineCharges;
        if ((chargesBefore <= 0 && currentDivineCharges <= 0) || (chargesBefore > 0 && currentDivineCharges > 0))
            return;
        boolean uncharged = currentDivineCharges <= 0;
        for (Item item : player.getInventory().getItems().getItems()) {
            if (item == null || item.getInventionData() == null || item.getDefinitions().usesChargesInside())
                continue;
            if (item.getDefinitions().getUnchargedItemId() == -1 || item.getDefinitions().getChargedItemId() == -1)
                continue;
            int newId = uncharged ? item.getDefinitions().getUnchargedItemId() : item.getDefinitions().getChargedItemId();
            item.setId(newId);
        }
        player.getInventory().refresh();
        for (Item item : player.getEquipment().getItems().getItems()) {
            if (item == null || item.getInventionData() == null || item.getDefinitions().usesChargesInside())
                continue;
            if (item.getDefinitions().getUnchargedItemId() == -1 || item.getDefinitions().getChargedItemId() == -1)
                continue;
            int newId = uncharged ? item.getDefinitions().getUnchargedItemId() : item.getDefinitions().getChargedItemId();
            item.setId(newId);
        }
        player.getEquipment().refreshItemContainer();
        player.getCombatDefinitions().refreshBonuses();
        player.getAppearence().generateAppearenceData();
        for (Bank bank : player.getBanks()) {
            if (bank == null)
                continue;
            for (int i = 0; i < bank.bankTabs.length; i++) {
                for (Item item : bank.bankTabs[i]) {
                    if (item == null || item.getInventionData() == null || item.getDefinitions().usesChargesInside())
                        continue;
                    if (item.getDefinitions().getUnchargedItemId() == -1 || item.getDefinitions().getChargedItemId() == -1)
                        continue;
                    int newId = uncharged ? item.getDefinitions().getUnchargedItemId() : item.getDefinitions().getChargedItemId();
                    item.setId(newId);
                }
            }
        }
        player.getBank().refreshItems();
        if (uncharged)
            player.getPackets().sendGameMessage("<col=ff0000>You have run out of divine charges!");
    }

    public Perk hasPerk(Perks perks) {
        return hasPerk(null, perks);
    }

    public Perk hasPerk(Item checkItem, Perks perks) {
        if (perks == null)
            return null;
        int id = perks.getId();
        int rank = -1;
        boolean hasIncreasedChance = false;
        for (int i = 0; i < player.getEquipment().getItems().getItems().length; i++) {
            Item item = player.getEquipment().getItems().get(i);
            if (item == null || item.getInventionData() == null || (checkItem != null && item != checkItem))
                continue;
            if ((item.getDefinitions().usesChargesInside() && item.getId() == item.getDefinitions().getUnchargedItemId())
                    || (!item.getDefinitions().usesChargesInside() && (!hasChargePack || this.divineCharges <= 0)))
                continue;
            for (Gizmo gizmo : item.getInventionData().getGizmos()) {
                if (gizmo == null)
                    continue;
                for (Perk perk : gizmo.getPerks()) {
                    if (perk == null || perk.getId() != id)
                        continue;
                    if (perk.getRank() > rank) {
                        rank = perk.getRank();
                        hasIncreasedChance = perks.hasIncreasedChance() && InventionData.getItemLevel(player, item) == 20;
                    }
                }
            }
        }
        Perk perk = rank == -1 ? null : new Perk(id, rank);
        if (perk != null)
            perk.setIncreasedChance(hasIncreasedChance);
        return perk;
    }

    public void setCracklingTarget(Entity target) {
        Perk crackling = hasPerk(Perks.CRACKLING);
        if (crackling == null)
            target = null;
        if (cracklingTarget == null)
            setCracklingCooldown(Utils.currentTimeMillis() + 60000);
        cracklingTarget = target;
        if (target == null)
            setCracklingCooldown(0);
    }

    public void processCracklingPerk() {
        Long cooldown = (Long) player.getTemporaryAttributtes().get(Key.REMOVE_CRACKLING_TARGET_TIMER);
        if (cooldown != null && cooldown > 0 && Utils.currentTimeMillis() >= cooldown)
            cracklingTarget = null;
        Perk crackling = hasPerk(Perks.CRACKLING);
        if (crackling == null && cracklingTarget != null) {
            cracklingTarget = null;
            setCracklingCooldown(0);
            return;
        }
        if (crackling != null && canUseCrackling()) {
            int maxHit = player.getCombatDefinitions().getHandDamage(false);
            int attackStyle = player.getCombatDefinitions().getStyle(false);
            int attackType = Combat.getStyleType(attackStyle);
            int damage = (int) ((double) maxHit * ((cracklingTarget instanceof NPC ? 0.5 : 0.1) * (double) crackling.getRank()));
            cracklingTarget.applyHit(new Hit(player, damage, attackType == Combat.MELEE_TYPE || attackType == Combat.ALL_TYPE ? HitLook.MELEE_DAMAGE : attackType == Combat.RANGE_TYPE ? HitLook.RANGE_DAMAGE : attackType == Combat.MAGIC_TYPE ? HitLook.MAGIC_DAMAGE : HitLook.REGULAR_DAMAGE));
            player.setNextGraphics(new Graphics(6022));
            setCracklingCooldown(Utils.currentTimeMillis() + 60000);
        }
    }

    private transient Entity cracklingTarget;

    private transient long cracklingCooldown;

    public long getCracklingCooldown() {
        return cracklingCooldown;
    }

    public void setCracklingCooldown(long cracklingCooldown) {
        this.cracklingCooldown = cracklingCooldown;
    }

    public boolean canUseCrackling() {
        return cracklingTarget != null && !cracklingTarget.isDead() && !cracklingTarget.hasFinished() && cracklingCooldown != 0 && Utils.currentTimeMillis() >= cracklingCooldown;
    }

    private int afterShockDamage;

    public int getAfterShockDamage() {
        return afterShockDamage;
    }

    public void setAfterShockDamage(int afterShockDamage) {
        this.afterShockDamage = afterShockDamage;
    }

    public void onEquipmentChange() {
        if (hasPerk(Perks.AFTERSHOCK) == null)
            afterShockDamage = 0;
        if (hasPerk(Perks.IMPATIENT) == null)
            impatientDamage = 0;
        if (player.getPrayer().isUsingProtectionPrayer() && hasPerk(Perks.ANTITHEISM) != null) {
            player.getPrayer().closeDamageProtectionPrayers();
            player.getPackets().sendGameMessage("The Antitheism perk closes your protection prayers.");
        }
    }

    public void processScavengingPerk() {
        Perk scavenging = hasPerk(Perks.SCAVENGING);
        if (scavenging == null)
            return;
        if (Math.random() <= ((0.01 * (double) scavenging.getRank()) * (scavenging.hasIncreasedChance() ? 1.1 : 1))) {
            int randomCommonMaterial = InventionDefinitions.getRandomMaterial(Math.random() <= 0.01 ? 3 : 2);
            InventionDefinitions def = InventionDefinitions.getData(ClientScriptMap.getMap(10742).getIntValue(randomCommonMaterial));
            String materialName = (String) def.getDataInIndex(1);
            player.getPackets().sendGameMessage("Your Scavenging perk adds: " + scavenging.getRank() + " x " + materialName + ".", true);
            materials[randomCommonMaterial] += scavenging.getRank();
            refreshMaterials();
        }
    }

    public void addDivineCharges(int quantity) {
        if (!hasChargePack) {
            player.getPackets().sendGameMessage("You need to create a charge pack before you can add divine charges.");
            return;
        }
        int amount = player.getInventory().getAmountOf(36390);
        if (quantity > amount)
            quantity = amount;
        if (quantity == 0)
            return;
        long increase = (long) quantity * INTERNAL_CHARGE_PER_DIVINE_CHARGE;
        double totalAmount = quantity + (double) divineCharges / INTERNAL_CHARGE_PER_DIVINE_CHARGE;
        int maxDivineCharges = getMaxDivineCharges() / INTERNAL_CHARGE_PER_DIVINE_CHARGE;
        if (totalAmount > maxDivineCharges) {
            quantity = (getMaxDivineCharges() - divineCharges) / INTERNAL_CHARGE_PER_DIVINE_CHARGE;
            increase = (long) quantity * INTERNAL_CHARGE_PER_DIVINE_CHARGE;
            player.getPackets().sendGameMessage("You can't store more than " + Utils.getFormattedNumber((getMaxDivineCharges() / CHARGE_PACK_VAR_MULTIPLIER)) + " charges in your pack.");
            if (quantity == 0 || increase == 0)
                return;
        }
        player.getInventory().deleteItem(36390, quantity);
        setDivineCharges((int) Math.min(Integer.MAX_VALUE, divineCharges + increase));
        player.getPackets().sendGameMessage("You add " + quantity + " divine charges containing " + Utils.getFormattedNumber((quantity * CHARGES_PER_DIVINE_CHARGE)) + " charge to your pack. You now have " + Utils.getFormattedNumber((divineCharges / CHARGE_PACK_VAR_MULTIPLIER)) + " charge stored.");
    }

    public int getMaxDivineCharges() {
        if (!hasDiscoveredBluePrint(54))
            return 600000000;
        if (!hasDiscoveredBluePrint(55))
            return 750000000;
        if (!hasDiscoveredBluePrint(56))
            return 900000000;
        if (!hasDiscoveredBluePrint(57))
            return 1050000000;
        if (!hasDiscoveredBluePrint(58))
            return 1200000000;
        return 1500000000;
    }

    private int emptyDivineChargesStored;
    private int divineChargesStored;
    private int containerChargesFilled;
    private boolean convertMemories;

    public int getEmptyDivineChargesStored() {
        return emptyDivineChargesStored;
    }

    public void setEmptyDivineChargesStored(int emptyDivineChargesStored) {
        this.emptyDivineChargesStored = emptyDivineChargesStored;
    }

    public int getDivineChargesStored() {
        return divineChargesStored;
    }

    public void setDivineChargesStored(int divineChargesStored) {
        this.divineChargesStored = divineChargesStored;
    }

    public int getContainerChargesFilled() {
        return containerChargesFilled;
    }

    public void setContainerChargesFilled(int containerChargesFilled) {
        this.containerChargesFilled = containerChargesFilled;
    }

    public boolean isConvertMemories() {
        return convertMemories;
    }

    public void setConvertMemories(boolean convertMemories) {
        this.convertMemories = convertMemories;
    }

    public void checkVaccumCharges() {
        player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " " + (divineChargesStored == 0 ? "is empty" : "has " + divineChargesStored + " divine charges stored") + ".");
    }

    public void configureVaccum() {
        player.stopAll();
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendDialogue("Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " is currently set to siphon " + (convertMemories ? "all energy and memories" : "all energy") + ".");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                case -1:
                    stage = 0;
                    sendOptionsDialogue("WHAT WOULD YOU LIKE TO DO?", "Siphon energy (default)", "Siphon energy and memories");
                    break;
                case 0:
                    convertMemories = componentId == OPTION_2;
                    player.getPackets().sendGameMessage("<col=00ff00>Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " will now siphon " + (convertMemories ? "all energy and memories" : "all energy") + ".");
                    player.getPackets().sendExecuteScript(1211, "<col=00ff00>Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " will now siphon " + (convertMemories ? "all energy and memories" : "all energy") + ".", 0, -120, 0);
                    end();
                    break;
                }
            }

            @Override
            public void finish() {
            }
        });
    }

    public void addEmptyDivineCharges(int amount) {
        if (amount == 0)
            return;
        if (amount > player.getInventory().getAmountOf(41073))
            amount = player.getInventory().getAmountOf(41073);
        if (amount + (emptyDivineChargesStored + divineChargesStored) > 100) {
            amount = 100 - emptyDivineChargesStored - divineChargesStored;
            player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " can't hold more than 100 empty divine charge containers.");
            if (amount == 0)
                return;
        }
        player.getInventory().deleteItem(41073, amount);
        emptyDivineChargesStored += amount;
        player.getPackets().sendGameMessage("You store " + amount + " empty divine charge containers in the " + ItemDefinitions.getItemDefinitions(41083).getName() + ".");
        player.getInventory().refresh();
        player.getEquipment().refresh();
    }

    public void withdrawDivineCharges() {
        if (divineChargesStored == 0) {
            player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " " + (divineChargesStored == 0 ? "is empty" : "has " + divineChargesStored + " divine charges stored") + ".");
            return;
        }
        if (player.getInventory().getFreeSlots() == 0 && !player.getInventory().containsItem(36390, 1)) {
            player.getPackets().sendGameMessage("You don't have enough space in your inventory.");
            return;
        }
        player.getInventory().addItem(36390, divineChargesStored);
        divineChargesStored = 0;
        player.getPackets().sendGameMessage("Your divine charges are placed in your inventory.");
        player.getInventory().refresh();
        player.getEquipment().refresh();
    }

    public void increaseChargesFilled(int ordinal, int amount) {
        if (emptyDivineChargesStored == 0)
            return;
        int increase = (10 + (ordinal * 27)) * amount;
        int before = containerChargesFilled / 300;
        this.containerChargesFilled = (containerChargesFilled + increase) / 300 >= 100 ? 30000 : (containerChargesFilled + increase);
        int after = containerChargesFilled / 300;
        player.getEquipment().refreshItemContainer();
        if (after == 100) {
            containerChargesFilled = 0;
            emptyDivineChargesStored--;
            divineChargesStored++;
            player.getPackets().sendGameMessage("<col=00ff00>Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " has successfully collected enough divine charge to fill a container.");
        } else {
            if (before < 25 && after >= 25) {
                player.getPackets().sendGameMessage("<col=FF8C00>An empty divine charge container is 25% full.");
            }
            if (before < 50 && after >= 50) {
                player.getPackets().sendGameMessage("<col=FF8C00>An empty divine charge container is 50% full.");
            }
            if (before < 75 && after >= 75) {
                player.getPackets().sendGameMessage("<col=FF8C00>An empty divine charge container is 75% full.");
            }
        }
    }

    public class BluePrint {

        private final int bluePrintId;
        private int stage;
        private int[] correctModules;
        private final int[][] selectedModules;
        private boolean[] blockedModules;
        private int selectedIndex;

        public BluePrint(int bluePrintId) {
            this.bluePrintId = bluePrintId;
            this.selectedModules = new int[2][5];
            this.blockedModules = new boolean[10];
            this.selectedIndex = -1;
        }

        public BluePrint start() {
            ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
            int dataId = bluePrintsMap.getIntValue(bluePrintId);
            InventionDefinitions def = InventionDefinitions.getData(dataId);
            player.getPackets().sendExecuteScript(12130, dataId, 0, def.getDataInIndex(3), 0, 0);
            generateRandomModules();
            generateRandomCorrectModules();
            stage = 1;
            player.getPackets().sendExecuteScript(12131);
            player.getVarBitManager().forceSendVarBit(30250, stage);
            player.getVarBitManager().forceSendVarBit(30242, 0);
            return this;
        }

        public void createPrototype() {
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    boolean failed = hasIncorrectModuls();
                    if (!failed) {
                        blockedModules = new boolean[10];
                        stage = 2;
                        player.getVarBitManager().forceSendVarBit(30242, 0);
                        player.getPackets().sendExecuteScript(12146, 1, 1);
                        player.getPackets().sendHideIComponent(1708, 57, false);
                        player.getPackets().sendHideIComponent(1708, 58, true);
                        for (int i = 0; i < 10; i++) {
                            player.getPackets().sendHideIComponent(1708, 43 + i, true);
                            player.getPackets().sendHideIComponent(1708, (i == 0 ? 53 : 83 + i), true);
                        }
                        player.getPackets().sendIComponentText(1708, 4, "Place all the modules on the track.");
                    } else
                        player.getPackets().sendExecuteScript(12142, 0);
                    player.getPackets().sendHideIComponent(1708, 98, false);
                    refresh();
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    }, 1);
                    stop();
                }

            }, 1);
        }

        public void invent() {
            if (discoveredBluePrints[bluePrintId])
                return;
            ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
            int dataId = bluePrintsMap.getIntValue(bluePrintId);
            InventionDefinitions def = InventionDefinitions.getData(dataId);
            String name = (String) def.getDataInIndex(1);
            int level = (int) def.getDataInIndex(8);
            int spirit = (int) def.getDataInIndex(5);
            double xp = getBluePrintXpForLevel(level);
            int n = getIncorrectIndexCount();
            double poorXP = xp * 0.2;
            double extraXp = (n == 5 ? 0 : n == 4 ? (xp * 0.4) : n == 3 ? (xp * 0.6) : n == 2 ? (xp * 0.8) : xp) - (n != 5 ? poorXP : 0);
            xp = poorXP + extraXp;
            discoveredBluePrints[bluePrintId] = true;
            double xpAdded = player.getSkills().addXp(26, xp);
            player.getInterfaceManager().setInterface(false, 1708, 71, 1530);
            String message = "You earned " + Utils.getFormattedNumber((int) xpAdded) + " Invention XP, and discovered a new invention: <col=ffffff>" + name + "</col>.";
            player.getPackets().sendExecuteScript(2734, "Success!", message, "Ok", "", "", spirit);
            player.getPackets().sendGameMessage(message);
            player.getPackets().sendHideIComponent(1708, 71, false);
            player.getPackets().sendExecuteScript(12060, def.getDataInIndex(0));
            stage = 3;
        }

        private void generateRandomModules() {
            ClientScriptMap modulesMap = ClientScriptMap.getMap(10744);
            int[] modules = new int[10];
            int randomModule = modulesMap.getIntValue(Utils.random(modulesMap.getSize()));
            int index = 0;
            while (index < modules.length) {
                boolean contains = false;
                for (int i = 0; i < modules.length; i++) {
                    if (modules[i] == randomModule) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    modules[index] = randomModule;
                    index++;
                }
                randomModule = modulesMap.getIntValue(Utils.random(modulesMap.getSize()));
            }
            for (int i = 0; i < modules.length; i++)
                player.getPackets().sendIComponentSprite(1708, 43 + i, modules[i]);
        }

        private void generateRandomCorrectModules() {
            int[] modules = new int[5];
            int randomModule = Utils.random(1, 11);
            int index = 0;
            while (index < modules.length) {
                boolean contains = false;
                for (int i = 0; i < modules.length; i++) {
                    if (modules[i] == randomModule) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    modules[index] = randomModule;
                    index++;
                }
                randomModule = Utils.random(1, 11);
            }
            correctModules = modules;
        }

        public void addModul(int modulNumber) {
            if (blockedModules[modulNumber - 1])
                return;
            for (int i = 0; i < selectedModules[stage - 1].length; i++) {
                if (selectedModules[stage - 1][i] == 0) {
                    selectedModules[stage - 1][i] = modulNumber;
                    break;
                }
            }
            refresh();
        }

        public void removeModul(int index) {
            if (index >= selectedModules[0].length)
                return;
            selectedModules[0][index] = 0;
            refresh();
        }

        public void setModul(int index, int modulNumber) {
            if (index >= selectedModules[stage - 1].length)
                return;
            if (modulNumber != 0 && blockedModules[modulNumber - 1])
                return;
            selectedModules[stage - 1][index] = modulNumber;
            if (stage == 1)
                player.getPackets().sendExecuteScript(12138, index, modulNumber);
            refresh();
        }

        public void switchModul(int fromIndex, int toIndex) {
            if (fromIndex >= selectedModules[stage - 1].length || toIndex >= selectedModules[stage - 1].length)
                return;
            int temp = selectedModules[stage - 1][fromIndex];
            selectedModules[stage - 1][fromIndex] = selectedModules[stage - 1][toIndex];
            selectedModules[stage - 1][toIndex] = temp;
            refresh();
        }

        private boolean hasIncorrectModuls() {
            boolean hasIncorrectModuls = false;
            for (int i = 0; i < selectedModules[0].length; i++) {
                if (!containsModul(selectedModules[0][i])) {
                    blockIncorrectModul(selectedModules[0][i] - 1);
                    setModul(i, 0);
                    hasIncorrectModuls = true;
                }
            }
            return hasIncorrectModuls;
        }

        private void blockIncorrectModul(int modulIndex) {
            blockedModules[modulIndex] = true;
            refresh();
        }

        private boolean containsModul(int modulNumber) {
            for (int i = 0; i < correctModules.length; i++)
                if (correctModules[i] == modulNumber)
                    return true;
            return false;
        }

        private boolean isSelected(int modulNumber) {
            for (int i = 0; i < selectedModules[1].length; i++)
                if (selectedModules[1][i] == modulNumber)
                    return true;
            return false;
        }

        private int getIncorrectIndexCount() {
            int count = 0;
            for (int i = 0; i < correctModules.length; i++) {
                if (selectedModules[1][i] == 0)
                    return -1;
                int index = selectedModules[1][i] - 1;
                if (correctModules[i] != selectedModules[0][index])
                    count++;
            }
            return count;
        }

        private void refresh() {
            for (int i = 0; i < 5; i++) {
                boolean b = isSelected(i + 1);
                player.getVarBitManager().forceSendVarBit(30237 + i, b ? 0 : selectedModules[0][i]);
                player.getVarBitManager().forceSendVarBit(30259 + i, b ? 0 : selectedModules[0][i]);
                player.getVarBitManager().forceSendVarBit(30244 + i, selectedModules[1][i]);
                player.getVarBitManager().forceSendVarBit(30264 + i, selectedModules[1][i]);
            }
            int value = 0;
            for (int i = 0; i < blockedModules.length; i++)
                if (blockedModules[i])
                    value |= 1 << i;
            player.getVarBitManager().forceSendVarBit(30242, value);
            player.getVarBitManager().forceSendVarBit(30250, stage);
            player.getPackets().sendExecuteScript(stage == 1 ? 12131 : 12148);
            player.getPackets().sendExecuteScript(12128);
            if (stage == 2) {
                int n = getIncorrectIndexCount();
                ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
                int dataId = bluePrintsMap.getIntValue(bluePrintId);
                InventionDefinitions def = InventionDefinitions.getData(dataId);
                int level = (int) def.getDataInIndex(8);
                double xp = getBluePrintXpForLevel(level);
                double poorXP = xp * 0.2;
                double extraXp = (n == 5 ? 0 : n == 4 ? (xp * 0.4) : n == 3 ? (xp * 0.6) : n == 2 ? (xp * 0.8) : xp) - (n != 5 ? poorXP : 0);
                String col = "<col=" + (n == 0 ? "008000" : n == 2 ? "ffffff" : n == 3 ? "ffff00" : n == 4 ? "ffa500" : "ff0000") + ">";
                String text = col + "Optimisation: " + (n == 0 ? "Perfect" : n == 2 ? "Excellent" : n == 3 ? "Good" : n == 4 ? "Satisfactory" : "Poor") + "</col><br>" + "You will gain : <col=ffffff>" + Utils.formatNumber((int) poorXP) + " </col>+ " + col + Utils.formatNumber((int) extraXp) + "</col> extra XP.";
                player.getPackets().sendIComponentText(1708, 4, n == -1 ? "Place all the modules on the track." : text);
                player.getPackets().sendHideIComponent(1708, 104, /* inspiration >= inspirationRequired && */ getIncorrectIndexCount() != -1);
            }
        }

        public int getBluePrintId() {
            return bluePrintId;
        }

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        public void setSelectedIndex(int selectedIndex) {
            this.selectedIndex = selectedIndex;
        }

        public int[] getCorrectModules() {
            return correctModules;
        }

    }

    public static double getBluePrintXpForLevel(int level) {
        if (level == 1)
            return 200.00;
        if (level == 2)
            return 250.00;
        if (level == 3)
            return 400.00;
        if (level == 4)
            return 107.00;
        if (level == 8)
            return 161.00;
        if (level == 16)
            return 628.00;
        if (level == 20)
            return 1198.10;
        if (level == 22)
            return 1541.00;
        if (level == 24)
            return 1541.00;
        if (level == 27)
            return 1541.00;
        if (level == 34)
            return 5446.00;
        if (level == 40)
            return 9184.00;
        if (level == 43)
            return 11221.00;
        if (level == 45)
            return 12726.00;
        if (level == 49)
            return 16114.00;
        if (level == 50)
            return 18257.00;
        if (level == 54)
            return 22600.00;
        if (level == 55)
            return 23779.00;
        if (level == 60)
            return 32284.50;
        if (level == 64)
            return 38610.00;
        if (level == 69)
            return 47562.00;
        if (level == 70)
            return 52591.00;
        if (level == 72)
            return 56862.00;
        if (level == 74)
            return 61347.00;
        if (level == 75)
            return 63672.00;
        if (level == 77)
            return 68489.00;
        if (level == 78)
            return 70981.00;
        if (level == 80)
            return 80618.00;
        if (level == 81)
            return 83441.00;
        if (level == 83)
            return 89273.00;
        if (level == 87)
            return 101703.00;
        if (level == 89)
            return 108310.00;
        if (level == 90)
            return 117921.00;
        if (level == 91)
            return 121584.00;
        if (level == 92)
            return 125320.00;
        if (level == 93)
            return 129127.50;
        if (level == 94)
            return 133008.50;
        if (level == 95)
            return 136962.60;
        if (level == 96)
            return 140990.50;
        if (level == 97)
            return 145093.00;
        if (level == 98)
            return 149272.00;
        if (level == 99)
            return 153526.00;
        if (level == 100)
            return 166167.00;
        if (level == 101)
            return 170806.70;
        if (level == 102)
            return 175528.00;
        if (level == 104)
            return 185218.50;
        if (level == 105)
            return 190188.80;
        if (level == 107)
            return 200382.30;
        if (level == 108)
            return 205606.90;
        if (level == 109)
            return 210917.40;
        if (level == 110)
            return 216314.60;
        if (level == 111)
            return 221798.80;
        if (level == 112)
            return 227370.00;
        if (level == 113)
            return 233031.00;
        if (level == 114)
            return 238780.00;
        if (level == 115)
            return 244619.00;
        if (level == 117)
            return 256568.00;
        if (level == 118)
            return 262680.00;
        return 0;
    }

    public void reset() {
        this.discoveredBluePrints = new boolean[ClientScriptMap.getMap(10743).getSize()];
        init();
    }
    
    
    private int impatientDamage;

    public int getImpatientDamage() {
        return impatientDamage;
    }

    public void setImpatientDamage(int impatientDamage) {
        this.impatientDamage = impatientDamage;
    }

}
