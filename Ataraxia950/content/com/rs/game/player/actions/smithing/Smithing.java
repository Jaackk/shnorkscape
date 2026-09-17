package com.rs.game.player.actions.smithing;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.hitbar.impl.SmithingHitBar;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.SmithingRandomEvent;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.invention.Disassemble;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.actions.smithing.defs.ForgingBar;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.skillingcontracts.impl.SmithingContractList;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author Cjay0091
 */
public class Smithing extends Action {

    public static int HAMMER = 2347;
    public static final int UNFINISHED_SMITHING_ITEM = 47068;
    private static final int SMITHING_INTERFACE = 300;
    private static final int MAX_HEAT = 1000;
    private static final int HIGH_HEAT_THRESHOLD = 667;
    private static final int MEDIUM_HEAT_THRESHOLD = 333;
    private static final int WORK_DELAY = 2;

    private final boolean portable;
    private final boolean startAtForge;
    private final boolean resumeProject;
    private final int selectedProductId;
    private int index;

    private ForgingBar bar;
    private Item unfinishedItem;
    private int quantity;
    private int currentProductId;
    private int currentOutAmount;
    private Item[] currentRequirements;
    private int progress;
    private int progressRequired;
    private int heat;
    private int maxHeat;
    private int progressMessageStep;

    public Smithing(int ticks, int index, boolean portable) {
        this(ticks, index, portable, false);
    }

    public Smithing(int ticks, int index, boolean portable, boolean startAtForge) {
        this(ticks, index, portable, startAtForge, -1);
    }

    public Smithing(int ticks, int index, boolean portable, boolean startAtForge, int selectedProductId) {
        this.index = index;
        this.quantity = Math.max(1, ticks);
        this.portable = portable;
        this.startAtForge = startAtForge;
        this.selectedProductId = selectedProductId;
        this.resumeProject = false;
    }

    public Smithing(Item unfinishedItem) {
        this.index = -1;
        this.quantity = 1;
        this.portable = false;
        this.startAtForge = false;
        this.selectedProductId = -1;
        this.resumeProject = true;
        this.unfinishedItem = unfinishedItem;
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    private double blacksmithSuit(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 25195)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 25196)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 25197)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 25198)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 25199)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 32280)
            xpBoost *= 1.03;
        if (player.getEquipment().getHatId() == 32280 && player.getEquipment().getChestId() == 25196 && player.getEquipment().getLegsId() == 25197 && player.getEquipment().getBootsId() == 25198 && player.getEquipment().getGlovesId() == 25199)
            xpBoost *= 1.03;
        if (player.getEquipment().getHatId() == 25195 && player.getEquipment().getChestId() == 25196 && player.getEquipment().getLegsId() == 25197 && player.getEquipment().getBootsId() == 25198 && player.getEquipment().getGlovesId() == 25199)
            xpBoost *= 1.01;
        return xpBoost;
    }

    private double getExperience(Player player) {
        int productId = currentProductId > 0 ? currentProductId : bar.getItems()[index].getId();
        int barAmount = ForgingInterface.getBarRequirementAmount(bar, productId);
        int xpIndex = barAmount == 5 ? 3 : barAmount - 1;
        if (xpIndex < 0)
            xpIndex = 0;
        if (xpIndex >= bar.getExperience().length)
            xpIndex = bar.getExperience().length - 1;
        double xp = bar.getExperience()[xpIndex];
        if (ForgingInterface.isBurialProduct(productId))
            xp *= 1.25;
        if (portable)
            xp *= 1.1;
        return xp * blacksmithSuit(player);
    }

    private static int getFreedInventorySlots(Player player, Item[] requirements) {
        int slots = 0;
        for (Item requirement : requirements) {
            if (requirement == null)
                continue;
            int inventoryAmount = player.getInventory().getAmountOf(requirement.getId());
            if (inventoryAmount <= 0)
                continue;
            if (ItemDefinitions.getItemDefinitions(requirement.getId()).isStackable()) {
                if (inventoryAmount <= requirement.getAmount())
                    slots++;
            } else {
                slots += Math.min(inventoryAmount, requirement.getAmount());
            }
        }
        return slots;
    }

    private static int getOutputSlotsRequired(Player player, int productId, int outAmount) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(productId);
        if (defs.isStackable())
            return player.getInventory().containsItem(productId, 1) ? 0 : 1;
        return outAmount;
    }

    private static int slotsNeeded(Player player, int productId, int outAmount, Item[] requirements) {
        int usableFreeSlots = player.getInventory().getFreeSlots() + getFreedInventorySlots(player, requirements);
        return Math.max(0, getOutputSlotsRequired(player, productId, outAmount) - usableFreeSlots);
    }

    private static boolean hasSpaceFor(Player player, int productId, int outAmount, Item[] requirements) {
        return slotsNeeded(player, productId, outAmount, requirements) == 0;
    }

    private static int getProjectInt(Item item, Key key, int fallback) {
        if (item == null || item.getAttributes() == null)
            return fallback;
        Object value = item.getAttributes().get(key);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private static void setProjectInt(Item item, Key key, int value) {
        ConcurrentHashMap<Key, Object> attributes = item.getAttributes();
        if (attributes == null)
            attributes = new ConcurrentHashMap<Key, Object>();
        attributes.put(key, value);
        item.setAttributes(attributes);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private static int percentOf(int value, int max) {
        if (max <= 0)
            return 0;
        return Math.min(100, Math.max(0, (value * 100) / max));
    }

    private static void sendProjectHitBars(Player player, int progress, int progressRequired, int heat, int maxHeat) {
        player.getNextHitBars().add(SmithingHitBar.progress(percentOf(progress, progressRequired)));
        player.getNextHitBars().add(SmithingHitBar.heat(percentOf(heat, maxHeat)));
    }

    private static int getReheatTicks(Player player, Item item) {
        ForgingBar projectBar = getProjectBar(item);
        if (projectBar == null)
            return 6;
        int smithingLevel = player.getSkills().getLevel(Skills.SMITHING);
        if (smithingLevel >= projectBar.getLevel() + 15)
            return 2;
        if (smithingLevel >= projectBar.getLevel() + 5)
            return 4;
        return 6;
    }

    public static boolean isUnfinishedSmithingItem(Item item) {
        return item != null && item.getId() == UNFINISHED_SMITHING_ITEM
                && getProjectInt(item, Key.SMITHING_PROJECT_PRODUCT_ID, -1) > 0;
    }

    private static int getProjectProductId(Item item) {
        return getProjectInt(item, Key.SMITHING_PROJECT_PRODUCT_ID, -1);
    }

    private static ForgingBar getProjectBar(Item item) {
        int ordinal = getProjectInt(item, Key.SMITHING_PROJECT_BAR_ORDINAL, -1);
        ForgingBar[] bars = ForgingBar.values();
        if (ordinal >= 0 && ordinal < bars.length)
            return bars[ordinal];
        return ForgingInterface.getBarForProduct(getProjectProductId(item));
    }

    private static int getProjectSlot(Item item, ForgingBar bar, int productId) {
        int slot = getProjectInt(item, Key.SMITHING_PROJECT_SLOT, -1);
        if (bar != null && slot >= 0 && slot < bar.getItems().length && bar.getItems()[slot].getId() == productId)
            return slot;
        return bar == null ? -1 : ForgingInterface.getSlotForProduct(bar, productId);
    }

    private static Item getFirstUnfinishedProject(Player player) {
        for (Item item : player.getInventory().getItemArray())
            if (isUnfinishedSmithingItem(item))
                return item;
        return null;
    }

    private static int getInventorySlot(Player player, Item item) {
        int slot = player.getInventory().getItemSlot(item);
        if (slot != -1)
            return slot;
        Item[] items = player.getInventory().getItemArray();
        for (int i = 0; i < items.length; i++)
            if (items[i] == item)
                return i;
        return -1;
    }

    private static boolean isForgeObject(WorldObject object) {
        if (object == null)
            return false;
        String name = object.getDefinitions().getName().toLowerCase();
        if (name.contains("forge"))
            return true;
        switch (object.getId()) {
            case 113259:
            case 113267:
            case 113270:
            case 113272:
                return true;
            default:
                return PortableStation.isPortableObject(object);
        }
    }

    private static boolean isAnvilObject(WorldObject object) {
        if (object == null)
            return false;
        String name = object.getDefinitions().getName().toLowerCase();
        if (name.contains("anvil"))
            return true;
        switch (object.getId()) {
            case 113258:
            case 113268:
            case 113269:
            case 113271:
                return true;
            default:
                return false;
        }
    }

    public static boolean handleSmithingStationClick(Player player, WorldObject object) {
        Item project = getFirstUnfinishedProject(player);
        if (project == null)
            return false;
        return handleUnfinishedItemOnSmithingStation(player, project, object);
    }

    public static boolean handleUnfinishedItemOnSmithingStation(Player player, Item item, WorldObject object) {
        if (!isUnfinishedSmithingItem(item))
            return false;
        if (isForgeObject(object)) {
            reheatUnfinishedItem(player, item, object);
            return true;
        }
        if (isAnvilObject(object)) {
            player.getActionManager().setAction(new Smithing(item));
            return true;
        }
        return false;
    }

    public static boolean sendUnfinishedProjectDetails(Player player, Item item) {
        if (!isUnfinishedSmithingItem(item))
            return false;
        int productId = getProjectProductId(item);
        int progress = getProjectInt(item, Key.SMITHING_PROJECT_PROGRESS, 0);
        int required = Math.max(1, getProjectInt(item, Key.SMITHING_PROJECT_PROGRESS_REQUIRED, 1));
        int heat = getProjectInt(item, Key.SMITHING_PROJECT_HEAT, 0);
        int maxHeat = Math.max(1, getProjectInt(item, Key.SMITHING_PROJECT_MAX_HEAT, MAX_HEAT));
        String name = ItemDefinitions.getItemDefinitions(productId).getName().toLowerCase();
        player.sendMessage("Unfinished " + name + " - progress: " + Math.min(100, progress * 100 / required)
                + "%; heat: " + Math.min(100, heat * 100 / maxHeat) + "%.", true);
        sendProjectHitBars(player, progress, required, heat, maxHeat);
        return true;
    }

    private static void reheatUnfinishedItem(Player player, Item item, WorldObject object) {
        int maxHeat = getProjectInt(item, Key.SMITHING_PROJECT_MAX_HEAT, MAX_HEAT);
        int currentHeat = clamp(getProjectInt(item, Key.SMITHING_PROJECT_HEAT, 0), 0, maxHeat);
        int productId = getProjectProductId(item);
        final int progress = getProjectInt(item, Key.SMITHING_PROJECT_PROGRESS, 0);
        final int required = Math.max(1, getProjectInt(item, Key.SMITHING_PROJECT_PROGRESS_REQUIRED, 1));
        if (currentHeat >= maxHeat) {
            player.sendMessage("Your unfinished " + ItemDefinitions.getItemDefinitions(productId).getName().toLowerCase() + " is already at full heat.", true);
            sendProjectHitBars(player, progress, required, currentHeat, maxHeat);
            return;
        }
        player.getActionManager().forceStop();
        player.faceObject(object);
        final int startHeat = currentHeat;
        final int heatNeeded = maxHeat - startHeat;
        final int reheatTicks = getReheatTicks(player, item);
        final int finalMaxHeat = maxHeat;
        final String productName = ItemDefinitions.getItemDefinitions(productId).getName().toLowerCase();
        player.lock(reheatTicks + 1);
        player.sendMessage("You reheat the unfinished " + productName + " at the forge.", true);
        WorldTasksManager.schedule(new WorldTask() {
            private int tick;

            @Override
            public void run() {
                int slot = getInventorySlot(player, item);
                if (player.hasFinished() || slot == -1) {
                    player.unlock();
                    stop();
                    return;
                }
                tick++;
                player.faceObject(object);
                player.setNextAnimation(new Animation(24976));
                int newHeat = startHeat + (heatNeeded * tick / reheatTicks);
                if (newHeat > finalMaxHeat)
                    newHeat = finalMaxHeat;
                setProjectInt(item, Key.SMITHING_PROJECT_HEAT, newHeat);
                player.getInventory().refresh(slot);
                sendProjectHitBars(player, progress, required, newHeat, finalMaxHeat);
                if (tick >= reheatTicks) {
                    player.unlock();
                    player.sendMessage("The unfinished " + productName + " is back at full heat.", true);
                    stop();
                }
            }
        }, 0, 0);
    }

    private Item getSmithingTool(Player player) {
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        if (weapon == null || weapon.getInventionData() == null)
            return null;
        String name = weapon.getName().toLowerCase();
        return name.contains("hammer-tron") || name.contains("crystal hammer") ? weapon : null;
    }

    private boolean hasHammer(Player player) {
        return player.getInventory().containsOneItem(HAMMER) || getSmithingTool(player) != null;
    }

    private static final int[] HAMMER_ON_ANVIL_SOUND_IDS = {3190, 3188, 3189};
    private static final int FIRST_HAMMER_SOUND_PACKET_DELAY = 16;
    private static final int SECOND_HAMMER_SOUND_PACKET_DELAY = 56;

    private static void sendSingleHammerOnAnvilSound(Player player, int delay) {
        player.getPackets().sendSound(HAMMER_ON_ANVIL_SOUND_IDS[Utils.random(HAMMER_ON_ANVIL_SOUND_IDS.length)], delay, 1);
    }

    private static void sendHammerOnAnvilSound(Player player) {
        sendSingleHammerOnAnvilSound(player, FIRST_HAMMER_SOUND_PACKET_DELAY);
        sendSingleHammerOnAnvilSound(player, SECOND_HAMMER_SOUND_PACKET_DELAY);
    }

    private void setSmithingAnimation(Player player, Item tool) {
        if (player.getAnimations().hasIronSmith && player.getAnimations().ironSmith) {
            player.setNextAnimation(new Animation(17309));
            player.setNextGraphics(new Graphics(3305));
            return;
        }
        if (tool == null) {
            player.setNextAnimation(new Animation(22143));
            sendHammerOnAnvilSound(player);
            return;
        }
        String toolName = tool.getName().toLowerCase();
        player.setNextAnimation(new Animation(toolName.contains("hammer-tron") ? 30204 : 30203));
        sendHammerOnAnvilSound(player);
    }

    private int getMaximumHeat(Player player, int productId) {
        int requiredLevel = ForgingInterface.getLevels(bar, index, player);
        int smithingLevel = player.getSkills().getLevel(Skills.SMITHING);
        int overLevel = Math.max(0, smithingLevel - requiredLevel);
        int heatBonus = Math.min(200, overLevel * 8);
        int upgradeHeat = ForgingInterface.getUpgradeLevel(productId) * 20;
        int heat = MAX_HEAT + heatBonus - upgradeHeat;
        if (ForgingInterface.isBurialProduct(productId))
            heat -= 50;
        if (heat < 600)
            heat = 600;
        if (heat > 1200)
            heat = 1200;
        return heat;
    }

    private int getProgressRequired(Player player, int productId, Item[] requirements) {
        int bars = ForgingInterface.getBarRequirementAmount(requirements, bar);
        int upgrade = ForgingInterface.getUpgradeLevel(productId);
        int required = 160 + (bars * 180) + ((bar.getLevel() / 10) * 45) + (upgrade * 120);
        String name = ItemDefinitions.getItemDefinitions(productId).getName().toLowerCase();
        if (name.contains("masterwork"))
            required += 300;
        if (name.contains("burial"))
            required += 200;
        if (name.contains("ore box"))
            required += 80;
        return Math.max(160, required);
    }

    private int getBaseProgress(Player player, Item tool) {
        int requiredLevel = ForgingInterface.getLevels(bar, index, player);
        int smithingLevel = player.getSkills().getLevel(Skills.SMITHING);
        int baseProgress = 28 + Math.max(0, smithingLevel - requiredLevel) / 2 + Math.max(0, bar.getLevel() / 20);
        if (tool != null && tool.getName().toLowerCase().contains("crystal hammer"))
            baseProgress += 4;
        return Math.max(15, baseProgress);
    }

    private int getHeatLoss() {
        int bars = ForgingInterface.getBarRequirementAmount(currentRequirements, bar);
        int loss = 70 + (bars * 4) + Math.max(0, bar.getLevel() - 50) / 3;
        loss += ForgingInterface.getUpgradeLevel(currentProductId) * 10;
        if (ForgingInterface.isBurialProduct(currentProductId))
            loss += 15;
        return Math.max(55, loss);
    }

    private double getHeatProgressMultiplier() {
        int heatPercent = getHeatPercent();
        if (heatPercent >= 67)
            return 2.0;
        if (heatPercent >= 34)
            return 1.0;
        return heatPercent == 0 ? 0.5 : 0.66;
    }

    private int getProgressPercent() {
        return percentOf(progress, progressRequired);
    }

    private int getHeatPercent() {
        return percentOf(heat, maxHeat);
    }

    private void sendProjectHitBars(Player player) {
        sendProjectHitBars(player, progress, progressRequired, heat, maxHeat);
    }

    private void sendProgressMessage(Player player) {
        int step = getProgressPercent() / 25;
        if (step <= progressMessageStep || step >= 4)
            return;
        progressMessageStep = step;
        player.sendMessage("Progress: " + (step * 25) + "%; heat: " + getHeatPercent() + "%.", true);
    }

    private void prepareCurrentItem(Player player) {
        currentProductId = selectedProductId > 0 ? selectedProductId : bar.getItems()[index].getId();
        currentOutAmount = ForgingInterface.getForgedAmount(currentProductId);
        currentRequirements = ForgingInterface.getSmithingRequirements(bar, currentProductId);
        progress = 0;
        progressRequired = getProgressRequired(player, currentProductId, currentRequirements);
        maxHeat = getMaximumHeat(player, currentProductId);
        heat = maxHeat;
        progressMessageStep = 0;
    }

    private boolean loadCurrentItem(Player player, Item item) {
        if (!isUnfinishedSmithingItem(item))
            return false;
        int productId = getProjectProductId(item);
        ForgingBar projectBar = getProjectBar(item);
        int projectSlot = getProjectSlot(item, projectBar, productId);
        if (projectBar == null || projectSlot == -1)
            return false;
        bar = projectBar;
        index = projectSlot;
        unfinishedItem = item;
        currentProductId = productId;
        currentOutAmount = getProjectInt(item, Key.SMITHING_PROJECT_OUT_AMOUNT, ForgingInterface.getForgedAmount(productId));
        currentRequirements = ForgingInterface.getSmithingRequirements(bar, currentProductId);
        progressRequired = Math.max(1, getProjectInt(item, Key.SMITHING_PROJECT_PROGRESS_REQUIRED,
                getProgressRequired(player, currentProductId, currentRequirements)));
        maxHeat = Math.max(1, getProjectInt(item, Key.SMITHING_PROJECT_MAX_HEAT, getMaximumHeat(player, currentProductId)));
        progress = clamp(getProjectInt(item, Key.SMITHING_PROJECT_PROGRESS, 0), 0, progressRequired);
        heat = clamp(getProjectInt(item, Key.SMITHING_PROJECT_HEAT, 0), 0, maxHeat);
        progressMessageStep = getProgressPercent() / 25;
        return true;
    }

    private void saveCurrentItem(Player player) {
        if (!isUnfinishedSmithingItem(unfinishedItem))
            return;
        setProjectInt(unfinishedItem, Key.SMITHING_PROJECT_PROGRESS, progress);
        setProjectInt(unfinishedItem, Key.SMITHING_PROJECT_HEAT, heat);
        int slot = getInventorySlot(player, unfinishedItem);
        if (slot != -1)
            player.getInventory().refresh(slot);
    }

    private Item createUnfinishedItem() {
        Item item = new Item(UNFINISHED_SMITHING_ITEM, 1);
        ConcurrentHashMap<Key, Object> attributes = new ConcurrentHashMap<Key, Object>();
        attributes.put(Key.SMITHING_PROJECT_BAR_ORDINAL, bar.ordinal());
        attributes.put(Key.SMITHING_PROJECT_SLOT, index);
        attributes.put(Key.SMITHING_PROJECT_PRODUCT_ID, currentProductId);
        attributes.put(Key.SMITHING_PROJECT_PROGRESS, progress);
        attributes.put(Key.SMITHING_PROJECT_PROGRESS_REQUIRED, progressRequired);
        attributes.put(Key.SMITHING_PROJECT_HEAT, heat);
        attributes.put(Key.SMITHING_PROJECT_MAX_HEAT, maxHeat);
        attributes.put(Key.SMITHING_PROJECT_OUT_AMOUNT, currentOutAmount);
        item.setAttributes(attributes);
        return item;
    }

    private boolean createCurrentProject(Player player, boolean fromForge) {
        prepareCurrentItem(player);
        if (!Utils.itemExists(UNFINISHED_SMITHING_ITEM)) {
            player.sendMessage("The unfinished smithing item is missing from this cache.");
            return false;
        }
        if (!hasSpaceFor(player, UNFINISHED_SMITHING_ITEM, 1, currentRequirements)) {
            int missing = Math.max(1, slotsNeeded(player, UNFINISHED_SMITHING_ITEM, 1, currentRequirements));
            player.sendMessage("You need " + missing + " more free inventory slot" + (missing == 1 ? "" : "s") + " to begin this project.");
            return false;
        }
        if (!ForgingInterface.hasSmithingRequirements(player, bar, currentProductId, true))
            return false;
        ForgingInterface.deleteSmithingRequirements(player, bar, currentRequirements, false);
        unfinishedItem = createUnfinishedItem();
        if (!player.getInventory().addItem(unfinishedItem)) {
            for (Item requirement : currentRequirements)
                if (requirement != null)
                    player.addItem(new Item(requirement.getId(), requirement.getAmount()), false);
            player.sendMessage("You could not start that smithing project.");
            return false;
        }
        String name = ItemDefinitions.getItemDefinitions(currentProductId).getName().toLowerCase();
        if (fromForge)
            player.sendMessage("Your unfinished item is at full heat: " + name + ". Use the anvil.", true);
        else
            player.sendMessage("You heat the unfinished " + name + " and begin smithing.", true);
        sendProjectHitBars(player);
        return true;
    }

    private void refundSavedBars(Player player, int amountBars) {
        if (amountBars <= 0)
            return;
        player.addItem(new Item(bar.getBarId(), amountBars), false);
    }

    private boolean replaceUnfinishedWithResult(Player player, boolean breakdownActive, boolean burialProduct) {
        int slot = getInventorySlot(player, unfinishedItem);
        if (slot == -1) {
            player.sendMessage("You need the unfinished smithing item in your inventory to finish it.");
            return false;
        }
        if (breakdownActive || burialProduct) {
            player.getInventory().replaceItem(-1, 0, slot);
        } else {
            player.getInventory().replaceItem(currentProductId, currentOutAmount, slot);
        }
        unfinishedItem = null;
        return true;
    }

    private boolean finishCurrentItem(Player player, Item tool) {
        int amountBars = ForgingInterface.getBarRequirementAmount(currentRequirements, bar);
        boolean burialProduct = ForgingInterface.isBurialProduct(currentProductId);

        Perk breakdown = tool != null ? player.getInventionManager().hasPerk(tool, Perks.BREAKDOWN) : null;
        boolean breakdownActive = breakdown != null &&
                Math.random() <= (0.008 * amountBars * (double) breakdown.getRank() * (breakdown.hasIncreasedChance() ? 1.15 : 1.00));

        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(bar.getBarId());
        boolean wastelessSave = amountBars > 0 && Utils.random(100) <= 1 && player.hasEfficiencyActivated();
        boolean portableSave = amountBars > 0 && portable && Utils.random(9) == 4;
        boolean effPerkProc = amountBars > 0 && PetPerkHandler.handleEfficiencyExpert(player, new Item(bar.getBarId(), amountBars));
        if (wastelessSave)
            player.sendMessage(Colors.ORANGE + "<shad=000000>Wasteless smithing: " + defs.getName().toLowerCase() + " saved!");
        if (portableSave)
            player.sendMessage(Colors.GOLD + "<shad=000000>The portable forge saves you some resources.", true);
        if (wastelessSave || portableSave || effPerkProc)
            refundSavedBars(player, amountBars);

        String productName = ItemDefinitions.getItemDefinitions(currentProductId).getName().toLowerCase();
        if (!replaceUnfinishedWithResult(player, breakdownActive, burialProduct))
            return false;
        if (breakdownActive) {
            Disassemble.forceDisassembleItem(player, new Item(currentProductId, 1));
            player.getPackets().sendGameMessage("Your breakdown perk has activated and given you the components you would've gotten had you disassembled your: " + productName);
        } else if (burialProduct) {
            player.sendMessage("You finish the " + productName + " and destroy it for additional experience.", true);
        }

        player.addSmithingActions();
        String action = ForgingInterface.getUpgradeLevel(currentProductId) > 0 ? "upgrade" : "smith";
        player.sendMessage("You " + action + " a " + productName + "; smithing actions: " +
                Colors.RED + Utils.getFormattedNumber(player.getSmithingActions()) + "</col>.", true);

        if (currentProductId == 1127)
            player.getAchievements().updateProgress(ForgingInterface.getForgedAmount(currentProductId), AchievementList.SMITH_RUNE_PLATEBODY);

        SmithingContractList.listenSmith(player, currentProductId);
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);

        double xp = getExperience(player);
        Perk tinker = tool != null ? player.getInventionManager().hasPerk(tool, Perks.TINKER) : null;
        boolean tinkerActive = tinker != null && Math.random() <= (0.05 * (double) tinker.getRank() * (tinker.hasIncreasedChance() ? 1.15 : 1.00));
        if (tinkerActive) {
            xp *= 1.25;
            player.getPackets().sendGameMessage("<col=00ff00>Your tinker perk allows you to carry out higher quality work, awarding extra XP.");
        }
        player.getInventionManager().processSkillXp(Skills.SMITHING, xp, tool);
        player.getSkills().addXp(Skills.SMITHING, xp);
        return true;
    }

    @Override
    public boolean process(Player player) {
        if (currentProductId <= 0 && resumeProject && !loadCurrentItem(player, unfinishedItem))
            return false;

        if (bar == null || index < 0 || index >= bar.getItems().length)
            return false;

        if (!hasHammer(player)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a hammer in order to work with a bar of " + new Item(bar.getBarId(), 1).getDefinitions().getName().toLowerCase().replace(" bar", "") + ".");
            return false;
        }

        if (currentProductId > 0 && (!isUnfinishedSmithingItem(unfinishedItem) || getInventorySlot(player, unfinishedItem) == -1)) {
            player.sendMessage("You need the unfinished smithing item in your inventory to continue.");
            return false;
        }

        if (player.getSkills().getLevel(Skills.SMITHING) < ForgingInterface.getLevels(bar, index, player)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a Smithing level of " + ForgingInterface.getLevels(bar, index, player) + " to create this.");
            return false;
        }

        if (player.clickedObject != null) {
            if (!World.containsObjectWithId(new WorldTile(player.clickedObject), player.clickedObject.getId())) {
                return false;
            }
            if (World.getObjectWithId(player.clickedObject, player.clickedObject.getId()) == null) {
                return false;
            }
        }

        if (player.getInterfaceManager().containsScreenInter()) {
            player.getInterfaceManager().closeScreenInterface();
            return true;
        }

        // The random event block has been removed from here.

        return quantity > 0;
    }

    @Override
    public int processWithDelay(Player player) {
        if (currentProductId <= 0 && !loadCurrentItem(player, unfinishedItem))
            return -1;

        Item tool = getSmithingTool(player);
        setSmithingAnimation(player, tool);
        int progressGain = (int) Math.round(getBaseProgress(player, tool) * getHeatProgressMultiplier());
        if (progressGain < 1)
            progressGain = 1;
        progress += progressGain;
        heat = Math.max(0, heat - getHeatLoss());
        sendProjectHitBars(player);
        sendProgressMessage(player);

        Perk rapid = tool != null ? player.getInventionManager().hasPerk(tool, Perks.RAPID) : null;
        boolean rapidActive = rapid != null && Math.random() <= (0.05 * (double) rapid.getRank() * (rapid.hasIncreasedChance() ? 1.15 : 1.00));
        if (rapidActive)
            player.getPackets().sendGameMessage("<col=00FF00>Your rapid perk speeds up the action process.");

        if (progress < progressRequired) {
            if (heat == 0)
                player.sendMessage("The item is cold. Reheat it at a forge to smith it faster.", true);
            saveCurrentItem(player);
            return rapidActive ? 1 : WORK_DELAY;
        }

        if (!finishCurrentItem(player, tool))
            return -1;
        quantity--;
        if (quantity <= 0)
            return -1;
        if (!ForgingInterface.hasSmithingRequirements(player, bar, bar.getItems()[index].getId(), false)) {
            player.sendMessage("You have run out of materials for that smithing project.", true);
            return -1;
        }
        if (!createCurrentProject(player, false))
            return -1;
        return rapidActive ? 1 : WORK_DELAY;
    }



    @Override
    public boolean start(Player player) {
        if (resumeProject) {
            if (!loadCurrentItem(player, unfinishedItem))
                return false;
            if (!hasHammer(player)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a hammer in order to work this unfinished item.");
                return false;
            }
            if (player.getSkills().getLevel(Skills.SMITHING) < ForgingInterface.getLevels(bar, index, player)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Smithing level of " + ForgingInterface.getLevels(bar, index, player) + " to create this.");
                return false;
            }
            player.sendMessage("You continue working on the unfinished " + ItemDefinitions.getItemDefinitions(currentProductId).getName().toLowerCase() + ".", true);
            sendProjectHitBars(player);
            return true;
        }
        if ((bar = (ForgingBar) player.getTemporaryAttributtes().get("SmithingBar")) == null) {
            return false;
        }
        if (index < 0 || index >= bar.getItems().length)
            return false;
        if (!hasHammer(player)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a hammer in order to work with a bar of " + new Item(bar.getBarId(), 1).getDefinitions().getName().replace("Bar ", "") + ".");
            return false;
        }
        if (player.getSkills().getLevel(Skills.SMITHING) < ForgingInterface.getLevels(bar, index, player)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a Smithing level of " + ForgingInterface.getLevels(bar, index, player) + " to create this.");
            return false;
        }
        if (!ForgingInterface.hasSmithingRequirements(player, bar, bar.getItems()[index].getId(), true))
            return false;
        int maxQuantity = ForgingInterface.getMaxQuantity(player, bar, index);
        if (maxQuantity <= 0)
            return false;
        if (quantity > maxQuantity)
            quantity = maxQuantity;
        if (!createCurrentProject(player, startAtForge))
            return false;
        return !startAtForge;
    }

    @Override
    public void stop(Player player) {
        this.setActionDelay(player, 1);
        player.clickedObject = null;
    }

    public static class ForgingInterface {

        private static final String RS3_SMITHING_PRODUCTS_KEY = "SmithingRs3Products";
        private static final String RS3_SMITHING_PRODUCT_MAP_KEY = "SmithingRs3ProductMap";
        private static final String RS3_SMITHING_CATEGORY_MAPS_KEY = "SmithingRs3CategoryMaps";
        private static final String RS3_SMITHING_CATEGORY_INDEX_KEY = "SmithingRs3CategoryIndex";
        private static final int RS3_SMITHING_MENU_MAP = 15093;
        private static final int RS3_SMITHING_MENU_NAMES_MAP = 15092;
        private static final int RS3_SMITHING_STATION_TYPE_VARBIT = 37409;
        private static final int RS3_NORMAL_SMITHING_STATION_VARBIT = 41089;
        private static final int RS3_BARBARIAN_SMITHING_STATION_VARBIT = 41090;
        private static final int RS3_FREMENNIK_SMITHING_STATION_VARBIT = 41091;
        private static final int RS3_SMITHING_STATION_SUPPORT_VARBIT = 41092;
        private static final int METAL_BANK_OBJECT_ID = 47066;

        public static final int[] componentChilds = new int[30];
        public static final int[] CLICKED_CHILDS = { 28, -1, 5, 1 };

        private static void addRequirement(Map<Integer, Integer> requirements, int itemId, int amount) {
            if (itemId <= 0 || amount <= 0)
                return;
            Integer existing = requirements.get(itemId);
            requirements.put(itemId, existing == null ? amount : existing + amount);
        }

        private static Item resolveRequirementItem(ForgingBar bar, Item[] options) {
            if (options == null)
                return null;
            Item metalBankMaterial = null;
            Item firstRealItem = null;
            for (Item option : options) {
                if (option == null || option.getId() <= 0 || option.getAmount() <= 0)
                    continue;
                if (option.getId() == bar.getBarId())
                    return new Item(option.getId(), option.getAmount());
                if (option.getId() == METAL_BANK_OBJECT_ID) {
                    metalBankMaterial = option;
                    continue;
                }
                if (firstRealItem == null)
                    firstRealItem = option;
            }
            if (metalBankMaterial != null)
                return new Item(bar.getBarId(), metalBankMaterial.getAmount());
            return firstRealItem == null ? null : new Item(firstRealItem.getId(), firstRealItem.getAmount());
        }

        private static int getCacheBarRequirementAmount(ForgingBar bar, int productId) {
            int amount = 0;
            Item[][] rawRequirements = RS3SkillsDialogue.getRequiredItemsForItem(productId);
            for (Item[] options : rawRequirements) {
                Item requirement = resolveRequirementItem(bar, options);
                if (requirement != null && requirement.getId() == bar.getBarId())
                    amount += requirement.getAmount();
            }
            return amount;
        }

        private static Item[] getSmithingRequirements(ForgingBar bar, int productId) {
            Map<Integer, Integer> requirements = new LinkedHashMap<Integer, Integer>();
            Item[][] rawRequirements = RS3SkillsDialogue.getRequiredItemsForItem(productId);
            for (Item[] options : rawRequirements) {
                Item requirement = resolveRequirementItem(bar, options);
                if (requirement != null)
                    addRequirement(requirements, requirement.getId(), requirement.getAmount());
            }
            if (requirements.isEmpty()) {
                int levelRequired = bar.getLevel() + getFixedAmount(bar, new Item(productId));
                addRequirement(requirements, bar.getBarId(), getBarsRequired(levelRequired, bar, productId));
            }
            Item[] items = new Item[requirements.size()];
            int index = 0;
            for (Map.Entry<Integer, Integer> entry : requirements.entrySet())
                items[index++] = new Item(entry.getKey(), entry.getValue());
            return items;
        }

        private static int getBarRequirementAmount(ForgingBar bar, int productId) {
            return getBarRequirementAmount(getSmithingRequirements(bar, productId), bar);
        }

        private static int getBarRequirementAmount(Item[] requirements, ForgingBar bar) {
            int amount = 0;
            for (Item requirement : requirements)
                if (requirement != null && requirement.getId() == bar.getBarId())
                    amount += requirement.getAmount();
            return amount;
        }

        private static int getAvailableRequirementAmount(Player player, ForgingBar bar, int itemId) {
            int amount = player.getInventory().getAmountOf(itemId);
            if (itemId == bar.getBarId())
                amount += player.getMetalBankAmount(itemId);
            return amount;
        }

        private static boolean hasSmithingRequirements(Player player, ForgingBar bar, int productId, boolean sendMessage) {
            for (Item requirement : getSmithingRequirements(bar, productId)) {
                if (getAvailableRequirementAmount(player, bar, requirement.getId()) >= requirement.getAmount())
                    continue;
                if (sendMessage) {
                    String name = ItemDefinitions.getItemDefinitions(requirement.getId()).getName().toLowerCase();
                    if (requirement.getId() == bar.getBarId())
                        player.getDialogueManager().startDialogue("SimpleMessage", "You do not have sufficient bars in your inventory or metal bank.");
                    else
                        player.getDialogueManager().startDialogue("SimpleMessage", "You need " + requirement.getAmount() + " x " + name + " to make this.");
                }
                return false;
            }
            return true;
        }

        private static void deleteRequirement(Player player, ForgingBar bar, Item requirement) {
            int amount = requirement.getAmount();
            int inventoryAmount = Math.min(amount, player.getInventory().getAmountOf(requirement.getId()));
            if (inventoryAmount > 0)
                player.getInventory().deleteItem(requirement.getId(), inventoryAmount);
            int remaining = amount - inventoryAmount;
            if (remaining > 0 && requirement.getId() == bar.getBarId())
                player.removeMetalBankItem(requirement.getId(), remaining);
        }

        private static void deleteSmithingRequirements(Player player, ForgingBar bar, Item[] requirements, boolean saveBars) {
            for (Item requirement : requirements) {
                if (requirement == null)
                    continue;
                if (saveBars && requirement.getId() == bar.getBarId())
                    continue;
                deleteRequirement(player, bar, requirement);
            }
        }

        private static void calculateComponentConfigurations() {
            int base = 18;
            for (int i = 0; i < componentChilds.length; i++) {
                if (base == 250)
                    base = 267;
                componentChilds[i] = (base);
                base += 8;
            }
        }

        public static int getBarsRequired(int levelRequired, ForgingBar bar, int id) { // returns the amount of bars needed to smith an item
            int cacheBarAmount = getCacheBarRequirementAmount(bar, id);
            if (cacheBarAmount > 0)
                return cacheBarAmount;
            if (levelRequired >= 99)
                levelRequired = 99;
            int level = levelRequired - bar.getLevel();
            String name = ItemDefinitions.getItemDefinitions(id).getName().toLowerCase();

            if (bar == ForgingBar.DRAGONBANE) {
                return 1;
            }
            if (name.contains("ore box")) {
                return 2;
            }
            if (name.contains("2h sword")) {
                return 3;
            }
            if (name.contains("pickaxe")) {
                return 2;
            }
            if (name.contains("longsword")) {
                return 2;
            }
            if (name.contains("scimitar")) {
                return 2;
            }
            if (name.contains("platebody"))
                return 5;
            if (name.contains("arrowheads")) {
                return 1;
            } else if (level >= 0 && level < 5) {
                return 1;
            } else if (level >= 5 && level <= 8) {
                if (name.contains("knife") || name.contains("limb") || name.contains("studs"))
                    return 1;
                return 2;
            } else if (level >= 9 && level <= 16) {
                if (name.contains("grapple"))
                    return 1;
                else if (name.contains("claws"))
                    return 2;
                return 3;
            } else if (level >= 17) {
                if (name.contains("bullseye"))
                    return 1;
                return 5;
            }
            return 1;
        }

        private static int getBasedAmount(Item item) {
            String def = item.getDefinitions().getName().toLowerCase();

            if (def.contains("2h sword"))
                return 8;
            else if (def.contains("ore box"))
                return 0;
            else if (def.contains("longsword"))
                return 5;



            if (def.contains("dagger"))
                return 1;
            else if (def.contains("hatchet") || def.contains("mace") || def.contains("iron spit"))
                return 2;
            else if (def.contains("bolts") || def.contains("med helm"))
                return 3;
            else if (def.contains("sword") || def.contains("dart tip") || def.contains("nails") || def.contains("wire"))
                return 4;
            else if (def.contains("arrow") || def.contains("pickaxe") || def.contains("scimitar"))
                return 4;
            else if (def.contains("limbs"))
                return 5;
            else if (def.contains("knife") || def.contains("full helm") || def.contains("studs"))
                return 5;
            else if (def.contains("sq shield") || def.contains("square shield") || def.contains("warhammer") || def.contains("grapple tip"))
                return 6;
            else if (def.contains("battleaxe"))
                return 6;
            else if (def.contains("chainbody") || def.contains("oil lantern"))
                return 7;
            else if (def.contains("kiteshield"))
                return 7;
            else if (def.contains("claws"))
                return 8;
            else if (def.contains("plateskirt") || def.contains("platelegs"))
                return 9;
            else if (def.contains("platebody"))
                return 9;
            else if (def.contains("bullseye lantern"))
                return 10;
            return 1;
        }

        private static int getFixedAmount(ForgingBar bar, Item item) {
            String name = item.getDefinitions().getName();
            if (bar == ForgingBar.DRAGONBANE) {
                if (name.contains("bolt")) {
                    return 2;
                } else {
                    return 0;
                }
            }
            int increment = getBasedAmount(item);
            if (name.contains("dagger") && bar != ForgingBar.BRONZE)
                increment--;
            else if (name.contains("hatchet") && bar == ForgingBar.BRONZE)
                increment--;
            return increment;
        }

        private static String getItemName(int id) {
            return ItemDefinitions.getItemDefinitions(id).getName().toLowerCase();
        }

        private static boolean isBurialProduct(int id) {
            return getItemName(id).contains("burial");
        }

        private static int getUpgradeLevel(int id) {
            String name = getItemName(id);
            for (int level = 5; level >= 1; level--) {
                if (name.contains("+" + level) || name.contains("+ " + level))
                    return level;
            }
            return 0;
        }

        public static int getForgedAmount(int id) {
            if (id == 21843 || id == 21823) { // dragonbane bolts or dragonbane arrowheads
                return 25;
            }
            String name = ItemDefinitions.getItemDefinitions(id).getName();
            if (name.contains("knife"))
                return 5;
            else if (name.contains("bolts") || name.contains("dart tip"))
                return 10;
            else if (name.contains("arrowheads") || name.contains("nails"))
                return 15;
            return 1;
        }

        public static int getLevels(ForgingBar bar, int slot, Player player) {
            int base = bar.getLevel();
            int barAmount = getFixedAmount(bar, bar.getItems()[slot]);
            int level = base + barAmount;
            if (level > 99)
                level = 99;
            return level;
        }

        public static String[] getStrings(Player player, ForgingBar bar, int index, int itemId) {
            if (itemId == -1 || index < 0 || index >= bar.getItems().length)
                return null;
            StringBuilder barName = new StringBuilder();
            StringBuilder levelString = new StringBuilder();
            String name = ItemDefinitions.getItemDefinitions(itemId).getName().toLowerCase();
            String barVariableName = bar.toString().toLowerCase();
            int levelRequired = bar.getLevel() + getFixedAmount(bar, bar.getItems()[index]);
            int barAmount = getBarsRequired(levelRequired, bar, itemId);
            if (getAvailableRequirementAmount(player, bar, bar.getBarId()) >= barAmount)
                barName.append("<col=00FF00>");
            barName.append(barAmount).append(" ").append(barAmount > 1 ? "bars" : "bar");
            if (levelRequired >= 99)
                levelRequired = 99;
            if (player.getSkills().getLevel(Skills.SMITHING) >= levelRequired)
                levelString.append("<col=FFFFFF>");
            levelString.append(Utils.formatPlayerNameForDisplay(name.replace(barVariableName + " ", "")));
            return new String[] { levelString.toString(), barName.toString() };
        }

        public static void handleIComponents(Player player, int componentId) {
            int slot = -1;
            int ticks = -1;

            // Loop through valid component buttons
            for (int i = 3; i <= 6; i++) {
                for (int index = 0; index < componentChilds.length; index++) {
                    if (componentChilds[index] + i == componentId) {
                        slot = index;
                        ticks = CLICKED_CHILDS[i - 3];
                        break;
                    }
                }
            }

            // Special case for "Make X" or other known components
            if (componentId == 14) {
                ticks = 28;
            }

            // If no valid slot was found, it's likely the X/close button or something else
            if (slot == -1 || ticks == -1) {
                player.getInterfaceManager().closeScreenInterface();
                return;
            }

            // Debug log
            if (Settings.DEBUG)
                Logger.getGlobal().info("New Smithing action: ticks: " + ticks + "; slot: " + slot + ".");

            // Start the smithing action
            player.getActionManager().setAction(
                    new Smithing(ticks, slot, (boolean) player.getTemporaryAttributtes().get("SmithingObject"),
                            Boolean.TRUE.equals(player.getTemporaryAttributtes().get("SmithingStartAtForge")))
            );
        }


        private static void sendComponentConfigs(Player player, ForgingBar bar) {
            for (int i : bar.getComponentChilds())
                player.getPackets().sendHideIComponent(SMITHING_INTERFACE, i - 1, false);
            if (bar == ForgingBar.DRAGONBANE) {
                for (int i = 19; i < 43; i++) {
                    player.getPackets().sendHideIComponent(SMITHING_INTERFACE, i, true);
                }
                for (int i = 59; i < 99; i++) {
                    player.getPackets().sendHideIComponent(SMITHING_INTERFACE, i, true);
                }
                for (int i = 114; i < 267; i++) {
                    player.getPackets().sendHideIComponent(SMITHING_INTERFACE, i, true);
                }
            }
        }

        private static int getBestAvailableProduct(Player player, ForgingBar bar) {
            int selectedProductId = -1;
            int highestLevel = -1;
            for (int slot = 0; slot < bar.getItems().length; slot++) {
                int productId = bar.getItems()[slot].getId();
                if (productId <= 0)
                    continue;
                int level = getLevels(bar, slot, player);
                if (player.getSkills().getLevel(Skills.SMITHING) < level)
                    continue;
                if (!hasSmithingRequirements(player, bar, productId, false))
                    continue;
                if (level >= highestLevel) {
                    selectedProductId = productId;
                    highestLevel = level;
                }
            }
            if (selectedProductId != -1)
                return selectedProductId;
            for (Item item : bar.getItems()) {
                int productId = item.getId();
                if (productId > 0)
                    return productId;
            }
            return -1;
        }

        private static ForgingBar getBarForProduct(int productId) {
            for (ForgingBar bar : ForgingBar.values()) {
                if (getSlotForProduct(bar, productId) != -1)
                    return bar;
            }
            return null;
        }

        private static int getSlotForProduct(ForgingBar bar, int productId) {
            int exactSlot = getExactSlotForProduct(bar, productId);
            if (exactSlot != -1)
                return exactSlot;
            if (productId <= 0)
                return -1;
            String productName = normalizeProductName(productId);
            for (int slot = 0; slot < bar.getItems().length; slot++) {
                int barProductId = bar.getItems()[slot].getId();
                if (barProductId > 0 && productName.equals(normalizeProductName(barProductId)))
                    return slot;
            }
            return -1;
        }

        private static int getExactSlotForProduct(ForgingBar bar, int productId) {
            for (int slot = 0; slot < bar.getItems().length; slot++) {
                if (bar.getItems()[slot].getId() == productId)
                    return slot;
            }
            return -1;
        }

        private static int getMaxQuantity(Player player, ForgingBar bar, int slot) {
            int productId = bar.getItems()[slot].getId();
            Item[] requirements = getSmithingRequirements(bar, productId);
            int maxQuantity = Integer.MAX_VALUE;
            for (Item requirement : requirements) {
                if (requirement == null || requirement.getAmount() <= 0)
                    continue;
                int availableAmount = getAvailableRequirementAmount(player, bar, requirement.getId());
                maxQuantity = Math.min(maxQuantity, availableAmount / requirement.getAmount());
            }
            if (maxQuantity == Integer.MAX_VALUE)
                maxQuantity = 1;
            if (maxQuantity > 60)
                maxQuantity = 60;
            return Math.max(1, maxQuantity);
        }

        private static boolean isSelectableSmithingBar(ForgingBar bar) {
            String name = ItemDefinitions.getItemDefinitions(bar.getBarId()).getName().toLowerCase();
            return name.endsWith(" bar");
        }

        private static ForgingBar getBestInventoryBar(Player player) {
            int smithLevel = player.getSkills().getLevel(Skills.SMITHING);
            ForgingBar bestBar = null;
            for (ForgingBar bar : ForgingBar.values()) {
                if (smithLevel < bar.getLevel() || !isSelectableSmithingBar(bar)
                        || !player.getInventory().containsItem(bar.getBarId(), 1))
                    continue;
                if (bestBar == null || bar.getLevel() > bestBar.getLevel())
                    bestBar = bar;
            }
            return bestBar;
        }

        private static ForgingBar getBestMetalBankBar(Player player) {
            int smithLevel = player.getSkills().getLevel(Skills.SMITHING);
            ForgingBar bestBar = null;
            for (ForgingBar bar : ForgingBar.values()) {
                if (smithLevel < bar.getLevel() || !isSelectableSmithingBar(bar)
                        || player.getMetalBankAmount(bar.getBarId()) <= 0)
                    continue;
                if (bestBar == null || bar.getLevel() > bestBar.getLevel())
                    bestBar = bar;
            }
            return bestBar;
        }

        private static boolean hasInventoryUpgradeMaterial(Player player, ForgingBar bar, int productId) {
            for (Item requirement : getSmithingRequirements(bar, productId)) {
                if (requirement == null || requirement.getId() <= 0 || requirement.getId() == bar.getBarId())
                    continue;
                if (player.getInventory().containsItem(requirement.getId(), 1))
                    return true;
            }
            return false;
        }

        private static ForgingBar getBestInventoryUpgradeBar(Player player) {
            int smithLevel = player.getSkills().getLevel(Skills.SMITHING);
            ForgingBar bestBar = null;
            int bestProductLevel = -1;
            for (ForgingBar bar : ForgingBar.values()) {
                if (smithLevel < bar.getLevel() || !isSelectableSmithingBar(bar))
                    continue;
                for (int slot = 0; slot < bar.getItems().length; slot++) {
                    int productId = bar.getItems()[slot].getId();
                    if (productId <= 0)
                        continue;
                    int level = getLevels(bar, slot, player);
                    if (smithLevel < level || !hasInventoryUpgradeMaterial(player, bar, productId)
                            || !hasSmithingRequirements(player, bar, productId, false))
                        continue;
                    if (bestBar == null || bar.getLevel() > bestBar.getLevel()
                            || (bar.getLevel() == bestBar.getLevel() && level > bestProductLevel)) {
                        bestBar = bar;
                        bestProductLevel = level;
                    }
                }
            }
            return bestBar;
        }

        private static ForgingBar getPreferredAnvilBar(Player player) {
            ForgingBar bar = getBestInventoryBar(player);
            if (bar != null)
                return bar;
            bar = getBestInventoryUpgradeBar(player);
            if (bar != null)
                return bar;
            return getBestMetalBankBar(player);
        }

        private static int getOreBoxBaseRequirement(int productId) {
            switch (productId) {
                case 44791:
                    return 44789;
                case 44793:
                    return 44791;
                case 44795:
                    return 44793;
                case 44797:
                    return 44795;
                default:
                    return -1;
            }
        }

        private static int[] getRs3CategoryMaps(ForgingBar bar) {
            switch (bar) {
                case BRONZE:
                    return new int[] { 7085 };
                case IRON:
                    return new int[] { 7086 };
                case STEEL:
                    return new int[] { 7087 };
                case MITHRIL:
                    return new int[] { 7088 };
                case ADAMANT:
                    return new int[] { 7089 };
                case RUNE:
                    return new int[] { 7090 };
                case ORIKALKUM:
                    return new int[] { 15052, 15053, 15055, 15054 };
                case NECRONIUM:
                    return new int[] { 15056, 15057, 15059, 15058 };
                case BANE:
                    return new int[] { 15060, 15061, 15062, 15063 };
                case ELDER:
                    return new int[] { 15065, 15066, 15068, 15067 };
                case MASTERWORK:
                    return new int[] { 15071 };
                case DRAGONBANE:
                    return new int[] { 7093 };
                default:
                    return null;
            }
        }

        private static int[] getProductsFromCategory(int categoryMapId) {
            RS3ClientScriptMap map = RS3ClientScriptMap.getMap(categoryMapId);
            int[] products = new int[map.getSize()];
            for (int i = 0; i < products.length; i++)
                products[i] = map.getIntValue(i);
            return products;
        }

        private static void sendRs3StationRequirementState(Player player, boolean active) {
            player.getVarBitManager().forceSendVarBit(RS3_SMITHING_STATION_TYPE_VARBIT, active ? 93 : 0);
            player.getVarBitManager().forceSendVarBit(RS3_NORMAL_SMITHING_STATION_VARBIT, active ? 1 : 0);
            player.getVarBitManager().forceSendVarBit(RS3_BARBARIAN_SMITHING_STATION_VARBIT, active ? 1 : 0);
            player.getVarBitManager().forceSendVarBit(RS3_FREMENNIK_SMITHING_STATION_VARBIT, active ? 1 : 0);
            player.getVarBitManager().forceSendVarBit(RS3_SMITHING_STATION_SUPPORT_VARBIT, active ? 1 : 0);
        }

        private static String normalizeProductName(int productId) {
            String name = ItemDefinitions.getItemDefinitions(productId).getName().toLowerCase();
            return name.replace("off hand ", "").replace("off-hand ", "").replace("off hand", "").replace("off-hand", "")
                    .replace("med helm", "helm").replace("square shield", "sq shield").replace("round shield", "kiteshield")
                    .replace("gloves", "gauntlets").replace("arrowtips", "arrowheads").replace("arrow tips", "arrowheads")
                    .replace("dart tip (unf)", "dart tip").replace(" claws", " claw").replace("(unf)", "")
                    .replace("  ", " ").trim();
        }

        private static boolean isOffHandProduct(int productId) {
            String name = ItemDefinitions.getItemDefinitions(productId).getName().toLowerCase();
            return name.startsWith("off-hand ") || name.startsWith("off hand ");
        }

        private static int getActionProductForDisplay(ForgingBar bar, int displayProduct) {
            int actionProduct = getActionProductForDisplayInBar(bar, displayProduct);
            if (actionProduct != -1)
                return actionProduct;
            for (ForgingBar otherBar : ForgingBar.values()) {
                if (otherBar == bar)
                    continue;
                actionProduct = getActionProductForDisplayInBar(otherBar, displayProduct);
                if (actionProduct != -1)
                    return actionProduct;
            }
            return -1;
        }

        private static int getActionProductForDisplayInBar(ForgingBar bar, int displayProduct) {
            if (getExactSlotForProduct(bar, displayProduct) != -1)
                return displayProduct;
            String displayName = normalizeProductName(displayProduct);
            if (displayName == null)
                return -1;
            for (Item item : bar.getItems()) {
                int actionProduct = item.getId();
                if (actionProduct <= 0)
                    continue;
                String actionName = normalizeProductName(actionProduct);
                if (displayName.equals(actionName))
                    return isOffHandProduct(displayProduct) ? displayProduct : actionProduct;
            }
            return -1;
        }

        private static int getDisplayProductForAction(Map<Integer, Integer> actionProducts, int actionProduct) {
            for (Map.Entry<Integer, Integer> entry : actionProducts.entrySet())
                if (entry.getValue() == actionProduct)
                    return entry.getKey();
            return actionProduct;
        }

        private static int getActionProduct(Player player, int displayProduct) {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> actionProducts = (Map<Integer, Integer>) player.getTemporaryAttributtes().get(RS3_SMITHING_PRODUCT_MAP_KEY);
            if (actionProducts == null)
                return displayProduct;
            Integer actionProduct = actionProducts.get(displayProduct);
            return actionProduct == null ? -1 : actionProduct;
        }

        public static boolean hasRs3SmithingProducts(Player player) {
            return player.getTemporaryAttributtes().get(RS3_SMITHING_PRODUCTS_KEY) != null;
        }

        public static boolean handleRs3ProductSelection(Player player, int slotId) {
            int[] products = (int[]) player.getTemporaryAttributtes().get(RS3_SMITHING_PRODUCTS_KEY);
            if (products == null)
                return false;
            int index = (slotId - 1) / 4;
            if (index < 0 || index >= products.length)
                index = slotId;
            if (index < 0 || index >= products.length)
                return false;
            if (!setRs3Product(player, products[index]))
                player.getPackets().sendGameMessage("That item is not wired into smithing yet.");
            return true;
        }

        private static ForgingBar getBarForDropdownSlot(int slotId) {
            switch (slotId) {
                case 17:
                case 18:
                    return ForgingBar.BRONZE;
                case 19:
                    return ForgingBar.IRON;
                case 20:
                    return ForgingBar.STEEL;
                case 21:
                case 22:
                    return ForgingBar.MITHRIL;
                case 23:
                    return ForgingBar.ADAMANT;
                case 24:
                case 25:
                    return ForgingBar.RUNE;
                case 26:
                    return ForgingBar.ORIKALKUM;
                case 27:
                    return ForgingBar.NECRONIUM;
                case 28:
                    return ForgingBar.BANE;
                case 29:
                    return ForgingBar.ELDER;
                case 32:
                case 33:
                    return ForgingBar.MASTERWORK;
                default:
                    return null;
            }
        }

        public static boolean handleRs3CategorySelection(Player player, int slotId) {
            ForgingBar currentBar = (ForgingBar) player.getTemporaryAttributtes().get("SmithingBar");
            if (currentBar == null)
                return false;

            ForgingBar selectedBar = getBarForDropdownSlot(slotId);
            if (selectedBar != null) {
                if (selectedBar == currentBar)
                    return true;
                int displayProduct = prepareRs3ProductList(player, selectedBar, getBestAvailableProduct(player, selectedBar));
                if (displayProduct == -1)
                    return false;
                player.getTemporaryAttributtes().put("SmithingBar", selectedBar);
                int[] categoryMaps = (int[]) player.getTemporaryAttributtes().get(RS3_SMITHING_CATEGORY_MAPS_KEY);
                Integer categoryIndex = (Integer) player.getTemporaryAttributtes().get(RS3_SMITHING_CATEGORY_INDEX_KEY);
                player.getVarBitManager().sendVar(RS3SkillsDialogue.CATEGORY_VAR,
                        categoryMaps[categoryIndex == null ? 0 : categoryIndex]);
                setRs3Product(player, displayProduct);
                player.getPackets().sendGlobalString(2390, "Smithing");
                return true;
            }

            int[] categoryMaps = (int[]) player.getTemporaryAttributtes().get(RS3_SMITHING_CATEGORY_MAPS_KEY);
            ForgingBar bar = currentBar;
            if (categoryMaps == null || categoryMaps.length <= 1)
                return false;
            Integer currentIndex = (Integer) player.getTemporaryAttributtes().get(RS3_SMITHING_CATEGORY_INDEX_KEY);
            int nextIndex = currentIndex == null ? 0 : (currentIndex + 1) % categoryMaps.length;
            int displayProduct = prepareRs3Category(player, bar, -1, nextIndex);
            if (displayProduct == -1)
                return false;
            player.getVarBitManager().sendVar(RS3SkillsDialogue.CATEGORY_VAR, categoryMaps[nextIndex]);
            setRs3Product(player, displayProduct);
            player.getPackets().sendGlobalString(2390, "Smithing");
            return true;
        }

        private static boolean setRs3Product(Player player, int displayProduct) {
            int actionProduct = getActionProduct(player, displayProduct);
            ForgingBar selectedBar = actionProduct == -1 ? null : getBarForProduct(actionProduct);
            int slot = selectedBar == null ? -1 : getSlotForProduct(selectedBar, actionProduct);
            if (slot == -1)
                return false;
            player.getTemporaryAttributtes().put("SmithingBar", selectedBar);
            sendRs3StationRequirementState(player, true);
            RS3SkillsDialogue.forceSetProduct(player, displayProduct, getMaxQuantity(player, selectedBar, slot));
            refreshRs3Product(player, displayProduct, selectedBar, slot);
            return true;
        }

        private static void refreshRs3Product(final Player player, final int displayProduct, final ForgingBar selectedBar, final int slot) {
            WorldTasksManager.schedule(new WorldTask() {
                private int refreshes;

                @Override
                public void run() {
                    if (!hasRs3SmithingProducts(player)
                            || player.getVarBitManager().getValue(RS3SkillsDialogue.PRODUCT_VAR) != displayProduct) {
                        stop();
                        return;
                    }
                    sendRs3StationRequirementState(player, true);
                    RS3SkillsDialogue.forceSetProduct(player, displayProduct, getMaxQuantity(player, selectedBar, slot));
                    if (++refreshes >= 2)
                        stop();
                }
            }, 1, 1);
        }

        private static int prepareRs3Category(Player player, ForgingBar bar, int actionProduct, int categoryIndex) {
            int[] categoryMaps = (int[]) player.getTemporaryAttributtes().get(RS3_SMITHING_CATEGORY_MAPS_KEY);
            if (categoryMaps == null || categoryIndex < 0 || categoryIndex >= categoryMaps.length)
                return -1;
            int[] products = getProductsFromCategory(categoryMaps[categoryIndex]);
            Map<Integer, Integer> actionProducts = new HashMap<Integer, Integer>();
            for (int product : products) {
                int mappedProduct = getActionProductForDisplay(bar, product);
                if (mappedProduct != -1)
                    actionProducts.put(product, mappedProduct);
            }
            if (actionProducts.isEmpty())
                return -1;
            player.getTemporaryAttributtes().put(RS3_SMITHING_PRODUCTS_KEY, products);
            player.getTemporaryAttributtes().put(RS3_SMITHING_PRODUCT_MAP_KEY, actionProducts);
            player.getTemporaryAttributtes().put(RS3_SMITHING_CATEGORY_INDEX_KEY, categoryIndex);
            if (actionProduct == -1) {
                for (int product : products)
                    if (actionProducts.containsKey(product))
                        return product;
                return -1;
            }
            int displayProduct = getDisplayProductForAction(actionProducts, actionProduct);
            return actionProducts.containsKey(displayProduct) ? displayProduct : -1;
        }

        private static int prepareRs3ProductList(Player player, ForgingBar bar, int actionProduct) {
            int[] categoryMaps = getRs3CategoryMaps(bar);
            if (categoryMaps == null)
                return -1;
            player.getTemporaryAttributtes().put(RS3_SMITHING_CATEGORY_MAPS_KEY, categoryMaps);
            for (int categoryIndex = 0; categoryIndex < categoryMaps.length; categoryIndex++) {
                int displayProduct = prepareRs3Category(player, bar, actionProduct, categoryIndex);
                if (displayProduct != -1)
                    return displayProduct;
            }
            if (actionProduct != -1) {
                for (int categoryIndex = 0; categoryIndex < categoryMaps.length; categoryIndex++) {
                    int displayProduct = prepareRs3Category(player, bar, -1, categoryIndex);
                    if (displayProduct != -1)
                        return displayProduct;
                }
            }
            return -1;
        }

        private static boolean sendRs3SmithingInterface(Player player, ForgingBar bar, WorldObject object) {
            final int initialProduct = getBestAvailableProduct(player, bar);
            if (initialProduct == -1 || getRs3CategoryMaps(bar) == null)
                return false;
            final boolean portable = PortableStation.isPortableObject(object);
            final boolean startAtForge = isForgeObject(object);
            final ForgingBar initialBar = bar;
            player.getTemporaryAttributtes().put("SmithingBar", bar);
            player.getTemporaryAttributtes().put("SmithingObject", portable);
            player.getTemporaryAttributtes().put("SmithingStartAtForge", startAtForge);
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    int displayProduct = prepareRs3ProductList(player, initialBar, initialProduct);
                    if (displayProduct == -1)
                        return;
                    int[] categoryMaps = (int[]) player.getTemporaryAttributtes().get(RS3_SMITHING_CATEGORY_MAPS_KEY);
                    Integer categoryIndex = (Integer) player.getTemporaryAttributtes().get(RS3_SMITHING_CATEGORY_INDEX_KEY);
                    sendRs3StationRequirementState(player, true);
                    RS3SkillsDialogue.sendCustomSkillDialogueWithoutProduct(player, RS3_SMITHING_MENU_MAP, RS3_SMITHING_MENU_NAMES_MAP,
                            categoryMaps[categoryIndex == null ? 0 : categoryIndex]);
                    setRs3Product(player, displayProduct);
                    player.getPackets().sendGlobalString(2390, "Smithing");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    SkillDialogueResult result = RS3SkillsDialogue.getResult(player, componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                    if (componentId != RS3SkillsDialogue.CONTINUE_OPTION)
                        return;
                    int productId = getActionProduct(player, result.getProduce());
                    end();
                    ForgingBar selectedBar = getBarForProduct(productId);
                    int slot = selectedBar == null ? -1 : getSlotForProduct(selectedBar, productId);
                    if (slot == -1) {
                        player.getPackets().sendGameMessage("That item is not wired into smithing yet.");
                        return;
                    }
                    player.getTemporaryAttributtes().put("SmithingBar", selectedBar);
                    player.getTemporaryAttributtes().put("SmithingObject", portable);
                    player.getActionManager().setAction(new Smithing(result.getQuantity(), slot, portable, startAtForge, productId));
                }

                @Override
                public void finish() {
                    player.getTemporaryAttributtes().remove(RS3_SMITHING_PRODUCTS_KEY);
                    player.getTemporaryAttributtes().remove(RS3_SMITHING_PRODUCT_MAP_KEY);
                    player.getTemporaryAttributtes().remove(RS3_SMITHING_CATEGORY_MAPS_KEY);
                    player.getTemporaryAttributtes().remove(RS3_SMITHING_CATEGORY_INDEX_KEY);
                    player.getTemporaryAttributtes().remove("SmithingStartAtForge");
                    sendRs3StationRequirementState(player, false);
                }
            });
            return true;
        }

        public static void sendSmithingInterface(Player player, ForgingBar bar, WorldObject object) {
            if (sendRs3SmithingInterface(player, bar, object))
                return;
            sendLegacySmithingInterface(player, bar, object);
        }

        public static void sendSmithingBarSelection(Player player, WorldObject object) {
            ForgingBar bar = getPreferredAnvilBar(player);
            if (bar == null) {
                if (player.getInventory().containsItem(ForgingBar.DRACONIC_VISAGE.getBarId(), 1)) {
                    player.getDialogueManager().startDialogue("DFSSmithingD");
                    return;
                }
                player.sendMessage("You have no bars for your smithing level.");
                return;
            }
            sendSmithingInterface(player, bar, object);
        }

        private static void sendLegacySmithingInterface(Player player, ForgingBar bar, WorldObject object) {
            calculateComponentConfigurations();
            player.getTemporaryAttributtes().put("SmithingBar", bar);
            sendComponentConfigs(player, bar);
            for (int i = 0; i < bar.getItems().length; i++) {
                player.getPackets().sendItemOnIComponent(SMITHING_INTERFACE, componentChilds[i], bar.getItems()[i].getId(), 1);
                String[] name = getStrings(player, bar, i, bar.getItems()[i].getId());
                if (name != null) {
                    player.getPackets().sendIComponentText(300, componentChilds[i] + 1, name[0]);
                    player.getPackets().sendIComponentText(300, componentChilds[i] + 2, name[1]);
                }
            }
            player.getPackets().sendIComponentText(300, 14, Utils.formatPlayerNameForDisplay(bar.toString().toLowerCase()) + "");
            player.getInterfaceManager().sendInterface(SMITHING_INTERFACE);
            player.getTemporaryAttributtes().put("SmithingObject", PortableStation.isPortableObject(object));
            player.getTemporaryAttributtes().put("SmithingStartAtForge", isForgeObject(object));
        }
    }
}
