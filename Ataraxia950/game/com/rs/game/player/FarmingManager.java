package com.rs.game.player;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.TreeDefinitions;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.skillingcontracts.impl.FarmingContractList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class FarmingManager implements Serializable {

    public static final int[] COMPOST_ORGANIC = {6055, 1942, 1957, 1965, 5986, 5504, 5982, 249, 251, 253, 255, 257, 2998, 259, 261, 263, 3000, 265, 2481, 267, 269, 1951, 753, 2126, 247, 239, 6018};
    public static final int[] SUPER_COMPOST_ORGANIC = {2114, 5978, 5980, 5982, 6004, 247, 6469};
    private static final long serialVersionUID = -6487741852718632170L;
    private static final int ALLOTMENT = 0, TREES = 1, HOPS = 2, FLOWERS = 3, FRUIT_TREES = 4, BUSHES = 5, HERBS = 6, COMPOST = 7, MUSHROOMS = 8, BELLADONNA = 9;
    private static final int EMPTY_BUCKET = 1925, ULTRACOMPOST = 43966, MagicSecs = 7409;
    private static final String[] PATCH_NAMES = {"allotment", "tree", "hops", "flower", "fruit tree", "bush", "herb", "compost", "mushroom", "belladonna"};
    private static final int[][] HARVEST_AMOUNTS = {{7, 18}, {1, 1}, {3, 7}, {1, 3}, {3, 5}, {3, 5}, {5, 12}, {0, 0}, {9, 9}};
    public static final Animation RAKING_ANIMATION = new Animation(2273);
    private static final Animation WATERING_ANIMATION = new Animation(2293);
    public static final Animation SEED_DIPPING_ANIMATION = new Animation(2291);
    public static final Animation SPADE_ANIMATION = new Animation(830);
    public static final Animation HERB_PICKING_ANIMATION = new Animation(2282);
    private static final Animation MAGIC_PICKING_ANIMATION = new Animation(2286);
    private static final Animation CURE_PLANT_ANIMATION = new Animation(2288);
    private static final Animation CHECK_TREE_ANIMATION = new Animation(832);
    private static final Animation PRUNING_ANIMATION = new Animation(2275);
    private static final Animation FLOWER_PICKING_ANIMATION = new Animation(2292);
    private static final Animation FRUIT_PICKING_ANIMATION = new Animation(2280);
    private static final Animation COMPOST_ANIMATION = new Animation(2283);
    public static final Animation BUSH_PICKING_ANIMATION = new Animation(2281);
    private static final Animation FILL_COMPOST_ANIMATION = new Animation(832);


    // ---- Farming tuning knobs ----

    // One farming growth minute. Product cycle times are expressed in these minutes.
    private static final long BASE_TICK_MS = 60_000L;

    // Crop speed: multiply product cycle time by this scale.
// < 1.0 = faster crops; > 1.0 = slower crops.
    private static final double CROP_TICK_SCALE = 1.0;

    // Weed speed: used when no crop is planted.
// > 1.0 = slower weeds; < 1.0 = faster weeds.
    private static final double WEED_TICK_SCALE = 1.0;

    // Disease: odds are 1 / (N + 1) per growth tick (higher N = safer).
    private static final int DISEASE_ODDS_BASE            = 7;  // about 1/8 per growth stage
    private static final int DISEASE_ODDS_WATERED_BONUS   = 7;
    private static final int DISEASE_ODDS_COMPOST_BONUS   = 7;
    private static final int DISEASE_ODDS_SUPER_BONUS     = 33;
    private static final int DISEASE_ODDS_ULTRA_BONUS     = 73;
    private static final int DISEASE_ODDS_PERK_BONUS      = 25; // GREEN_THUMB perk benefit






    public static int[] produces = {199, 201, 203, 205, 207, 3049, 209, 211, 213, 3051, 215, 2485, 217, 219, 21626, // herbs
            1942, 1957, 1965, 1982, 5986, 5504, 5982, // allotments
            6010, 6014, 6012, 1793, 255, 6055, 21622, 6016, // flowers
            6006, 5994, 5996, 5931, 5998, 6000, 6002, 1955, 1963, 2108, 5970, 2114, 5972, 5974}; // fruit
    // and
    public static int[] cleanproduce = {249, 251, 253, 255, 257, 2998, 259, 261, 263, 3000, 265, 2481, 265, 267, 269, 21624

    }; // hops

    private final List<FarmingSpot> spots;
    private transient Player player;

    public List<FarmingSpot> getSpots() {
        return spots;
    }

    public FarmingManager() {
        spots = new CopyOnWriteArrayList<>();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void init() {
        for (SpotInfo info : SpotInfo.getInfo())
            player.getPackets().sendConfigByFile(info.configFileId, 0, true);
        for (FarmingSpot spot : spots)
            spot.refresh();
    }

    public void process() {
        for (FarmingSpot spot : spots)
            spot.process();
    }

    public FarmingSpot getSpot(SpotInfo info) {
        for (FarmingSpot spot : spots)
            if (spot.spotInfo.equals(info))
                return spot;
        return null;
    }

    public boolean isFarming(WorldObject object, Item item, int optionId) {
        player.faceObject(object);
        int objectId = object.getId();
        SpotInfo info = SpotInfo.getInfo(objectId);
        if (info != null) {
            handleFarming(object, info, item, optionId);
            return true;
        }
        return false;
    }

    public void handleFarming(WorldObject object, SpotInfo info, Item item, int optionId) {
        FarmingSpot spot = getSpot(info);
        if (spot == null)
            spot = new FarmingSpot(info);
        if (!spot.isCleared()) {
            if (item != null) {
                if (info.type == COMPOST)
                    fillCompostBin(spot, item);
            } else {
                switch (optionId) {
                    case 1: // rake
                        if (info.type == COMPOST) {
                            if (spot.getHarvestAmount() == 15) {
                                spot.setCleared(true);
                                spot.setActive(ProductInfo.Compost_Bin);
                                spot.setHarvestAmount(15);
                                spot.refresh();
                                player.sendMessage("You close the compost bin.");
                                player.sendMessage("The vegetation begins to decompose.");
                            }
                        } else
                            startRakeAction(spot); // creates usable spot
                        break;
                    case 2: // inspect
                        sendNeedsWeeding(spot.isCleared());
                        break;
                    case 4: // guide
                        openGuide();
                        break;
                }
            }
        } else {
            if (item != null) {
                String itemName = item.getName().toLowerCase();
                if (itemName.startsWith("watering can ("))
                    startWateringAction(spot, item);
                else if (itemName.contains("compost"))
                    startCompostAction(spot, item, itemName.equals("supercompost"), item.getId() == ULTRACOMPOST || itemName.equals("ultracompost"));
                else if (item.getId() == 6036)
                    startCureAction(spot, item);
                startFarmingCycle(spot, item);
            } else if (spot.productInfo != null) {
                switch (optionId) {
                    case 1:
                        if (info.type == TREES) {
                            if (spot.reachedMaxStage() && !spot.hasChecked())
                                checkHealth(spot);
                            else if (spot.reachedMaxStage() && !spot.isEmpty())
                                collectTreeProducts(object, spot, TreeDefinitions.valueOf(spot.productInfo.name().toUpperCase()));
                            else if (spot.reachedMaxStage() && spot.isEmpty())
                                startHarvestingAction(spot);
                            else if (spot.isDead())
                                clearFarmingPatch(spot);
                            else if (spot.isDiseased())
                                startCureAction(spot, null);
                        } else if (info.type == FRUIT_TREES) {
                            if (spot.reachedMaxStage() && !spot.hasChecked())
                                checkHealth(spot);
                            else if (spot.reachedMaxStage() && !spot.hasEmptyHarvestAmount())
                                startPickingAction(spot);
                            else if (spot.reachedMaxStage() && !spot.isEmpty())
                                collectTreeProducts(object, spot, TreeDefinitions.FRUIT_TREES);
                            else if (spot.isDead() || spot.hasEmptyHarvestAmount())
                                clearFarmingPatch(spot);
                            else if (spot.isDiseased())
                                startCureAction(spot, null);
                        } else if (info.type == BUSHES) {
                            if (spot.reachedMaxStage() && !spot.hasChecked())
                                checkHealth(spot);
                            else if (spot.reachedMaxStage() && !spot.hasEmptyHarvestAmount())
                                startPickingAction(spot);
                            else if (spot.isDead())
                                clearFarmingPatch(spot);
                            else if (spot.isDiseased())
                                startCureAction(spot, null);
                        } else if (info.type == COMPOST) {
                            if (spot.reachedMaxStage() && !spot.hasChecked()) {
                                spot.setChecked(true);
                                spot.refresh();
                                player.sendMessage("You open the compost bin.");
                            } else if (!spot.reachedMaxStage())
                                player.sendMessage("The weeds haven't finished decomposing yet.");
                            else
                                clearCompostAction(spot);
                        } else {
                            if (spot.reachedMaxStage() && !spot.isDead())
                                startHarvestingAction(spot);
                            else if (spot.isDead())
                                clearFarmingPatch(spot);
                            if (spot != null && !spot.reachedMaxStage() && !spot.isDead() && getWaterCan(player) != -1 && !spot.isWatered())
                                startWateringAction(spot, new Item(getWaterCan(player), 1));
                        }
                        break;
                    case 2:// inspect... usless tbh
                        if (spot.isDead() || spot.isEmpty()) {
                            player.sendMessage("This farming patch spot needs clearing.");
                        } else if (spot.stage >= 0 && spot.productInfo != null) {
                            int stagesLeft = Math.max(0, spot.productInfo.maxStage - spot.stage - 1);
                            double cropScale = spot.productInfo.type == HERBS ? 2.0 : 1.0;
                            long stageMs = Math.round(BASE_TICK_MS * CROP_TICK_SCALE * cropScale * spot.productInfo.cycleTime);
                            long msLeft = spot.getCycleTime() + (stagesLeft * stageMs);
                            long minsLeft = TimeUnit.MILLISECONDS.toMinutes(msLeft);
                            if (minsLeft <= 60) {
                                if (minsLeft <= 1) {
                                    player.sendMessage("There's something growing in the patch. Check again in 1 minute.");
                                } else {
                                    player.sendMessage("There's something growing in the patch. Check again in  " + minsLeft + " minutes.");
                                }
                            } else {
                                long hours = TimeUnit.MILLISECONDS.toHours(msLeft);
                                long minutes = TimeUnit.MILLISECONDS.toMinutes(msLeft) - TimeUnit.HOURS.toMinutes(hours);
                                player.sendMessage(String.format("There's something growing in the patch. Check again in %d hour(s) and %d minute(s).", hours, minutes));
                            }
                        }
                        break;
                    case 3:// clear & guide
                        if (spot.isDiseased() || spot.isDead() || spot.isEmpty() || (spot.reachedMaxStage() && spot.hasEmptyHarvestAmount()))
                            clearFarmingPatch(spot);
                        break;
                    case 4:
                        openGuide();
                        break;
                }
            }
        }
    }

    private void startRakeAction(final FarmingSpot spot) {
        player.getActionManager().setAction(new Action() {

            @Override
            public boolean start(Player player) {
                if (!player.getInventory().containsOneItem(5341)) {
                    player.sendMessage("You need a rake to do this.");
                    return false;
                }
                return true;
            }

            @Override
            public boolean process(Player player) {
                return spot.stage != 3;
            }

            @Override
            public int processWithDelay(Player player) {
                player.setNextAnimation(RAKING_ANIMATION);
                if (Utils.random(1) == 0) {
                    spot.increaseStage();
                    if (spot.stage == 3)
                        spot.setCleared(true);
                    player.getInventory().addItem(6055, 1);
                    player.getSkills().addXp(Skills.FARMING, 1);
                }
                return 1;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 1);
            }
        });
    }

    public void startHarvestingAction(final FarmingSpot spot) {
        final String patchName = getPatchName(spot.productInfo.type);
        player.getActionManager().setAction(new Action() {

            @Override
            public boolean start(Player player) {
                if (!player.getInventory().hasFreeSlots()) {
                    player.sendMessage("You do not have enough inventory space to do that.");
                    return false;
                }
                if (spot.hasEmptyHarvestAmount() && !spot.hasGivenAmount()) {
                    spot.setHarvestAmount(getRandomHarvestAmount(spot));
                    spot.setHasGivenAmount(true);
                } else if (spot.harvestAmount <= 0) {
                    player.sendMessage("You have successfully harvested this patch for new crops.");
                    player.setNextAnimation(new Animation(-1));
                    spot.setIdle();
                    return false;
                }
                player.sendMessage("You begin to harvest the " + patchName + " patch.");
                setActionDelay(player, 1);
                return true;
            }

            @Override
            public boolean process(Player player) {
                if (!player.getInventory().hasFreeSlots()) {
                    player.sendMessage("You do not have enough inventory space to do that.");
                    return false;
                }
                if (spot.harvestAmount > 0)
                    return true;
                else {
                    player.setNextAnimation(new Animation(-1));
                    spot.setIdle();
                    return false;
                }
            }

            @Override
            public int processWithDelay(Player player) {
                spot.harvestAmount--;
                player.setNextAnimation(spot.productInfo == ProductInfo.FlyTrap || spot.productInfo == ProductInfo.Sunchoke ? FLOWER_PICKING_ANIMATION : getHarvestAnimation(spot.productInfo.type));
                FarmingContractList.listen(player, spot.productInfo);
                player.getSkills().addXp(Skills.FARMING, spot.productInfo.experience);
                player.addProduceGathered();
                if (spot.productInfo == ProductInfo.FlyTrap) {
                    player.getSkills().addXp(Skills.THIEVING, getFlyTrapThievingXp());
                } else if (spot.productInfo == ProductInfo.Sunchoke) {
                    player.getSkills().addXp(Skills.STRENGTH, getSunchokeStrengthXp());
                } else {
                    ItemDefinitions def = ItemDefinitions.getItemDefinitions(spot.productInfo.productId);
                    int amount = 1;
                    if (spot.productInfo.type == HERBS && player.jujuPotions.isActive(Pots.Effects.FARMING_JUJU) &&
                            ThreadLocalRandom.current().nextInt(3) == 0) {
                        amount++;
                        player.sendMessage("Through the power of juju, you receive an extra herb!");
                    }
                    if (player.getPerkManager().hasPerkActive(DonationPerk.GREEN_THUMB) && !def.isStackable()) {
                        player.getInventory().addItem(def.certId, amount);
                    } else {
                        player.getInventory().addItemDrop(spot.productInfo.productId, amount);
                    }
                }
                if (player.hasCleansingActivated() && spot.harvestAmount == 0 && Utils.random(10) == 0) {
                    player.getInventory().addItem(spot.productInfo.seedId, 1);
                    player.sendMessage(Colors.GREEN + "<shad=000000>Your Scroll of Life saves you a seed!", true);
                }
                if (spot.harvestAmount == 0)
                    tryAwardHarmonyMoss();
                String name = spot.productInfo.productId == -1 ? spot.productInfo.name().toLowerCase() : ItemDefinitions.getItemDefinitions(spot.productInfo.productId).name;
                player.sendMessage("You harvest the " + name + "; produce harvested: " + Colors.RED + Utils.getFormattedNumber(player.produceGathered) + "</col>.", true);
                return 1;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 2);
            }
        });
    }

    private void startPickingAction(final FarmingSpot spot) {
        player.getActionManager().setAction(new Action() {

            @Override
            public boolean start(Player player) {
                return true;
            }

            @Override
            public boolean process(Player player) {
                if (spot.harvestAmount > 0)
                    return true;
                else {
                    player.sendMessage("You pick all of the " + (spot.productInfo.type == FRUIT_TREES ? "fruits" : "berries") + " from the " + getPatchName(spot.productInfo.type) + " patch; produce harvested: " + Colors.RED + Utils.getFormattedNumber(player.produceGathered) + "</col>.", true);
                    player.setNextAnimation(new Animation(-1));
                    return false;
                }
            }

            @Override
            public int processWithDelay(Player player) {
                player.sendMessage("You pick a " + ItemDefinitions.getItemDefinitions(spot.productInfo.productId).getName().toLowerCase() + ".");
                player.setNextAnimation(getHarvestAnimation(spot.productInfo.type));
                player.addProduceGathered();
                player.getSkills().addXp(Skills.FARMING, spot.productInfo.experience);
                ItemDefinitions def = ItemDefinitions.getItemDefinitions(spot.productInfo.productId);
                int amount = 1;
                if (player.getPerkManager().hasPerkActive(DonationPerk.GREEN_THUMB) && !def.isStackable()) {
                    player.getInventory().addItem(def.certId, amount);
                } else {
                    player.getInventory().addItem(spot.productInfo.productId, amount);
                }

                spot.harvestAmount--;
                if (spot.harvestAmount == 0)
                    tryAwardHarmonyMoss();
                spot.refresh();
                return 1;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 1);
            }
        });
    }

    public void clearCompostAction(final FarmingSpot spot) {
        player.getActionManager().setAction(new Action() {

            @Override
            public boolean start(Player player) {
                if (spot == null)
                    return false;
                else if (!player.getInventory().containsItem(EMPTY_BUCKET, 1)) {
                    player.sendMessage("You need an empty bucket to clear the compost.");
                    return false;
                }
                return true;
            }

            @Override
            public boolean process(Player player) {
                if (!player.getInventory().containsItem(EMPTY_BUCKET, 1)) {
                    player.sendMessage("You need an empty bucket to clear the compost.");
                    return false;
                } else if (spot.harvestAmount > 0)
                    return true;
                else {
                    spot.setCleared(false);
                    spot.refresh();
                    spot.setProductInfo(null);
                    spot.remove();
                    player.setNextAnimation(new Animation(-1));
                    return false;
                }
            }

            @Override
            public int processWithDelay(Player player) {
                player.setNextAnimation(FILL_COMPOST_ANIMATION);
                player.getSkills().addXp(Skills.FARMING, 5);
                player.getInventory().deleteItem(EMPTY_BUCKET, 1);
                player.getInventory().addItem(spot.getCompost() ? 6032 : 6034, 1);
                spot.harvestAmount--;
                spot.refresh();
                return 1;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 1);
            }
        });
    }

    public void clearFarmingPatch(final FarmingSpot spot) {
        final String patchName = getPatchName(spot.productInfo.type);
        player.getActionManager().setAction(new Action() {

            private int stage;

            @Override
            public boolean start(Player player) {
                if (!player.getInventory().containsOneItem(952) && !player.getToolBelt().contains(952)) {
                    player.sendMessage("You need a spade to do this.");
                    return false;
                }
                player.sendMessage("You start digging up the produce in the " + patchName + " patch.");
                return true;
            }

            @Override
            public boolean process(Player player) {
                if (stage != 2)
                    return true;
                else {
                    player.sendMessage("You clear the " + patchName + " and get it ready for new crops.");
                    player.setNextAnimation(new Animation(-1));
                    spot.setIdle();
                    return false;
                }
            }

            @Override
            public int processWithDelay(Player player) {
                player.setNextAnimation(SPADE_ANIMATION);
                if (Utils.random(3) == 0)
                    stage++;
                return 1;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 2);
            }
        });
    }

    public boolean startFarmingCycle(FarmingSpot spot, Item item) {
        ProductInfo productInfo = ProductInfo.getProduct(item.getId());
        if (spot == null || productInfo == null || spot.spotInfo.type != productInfo.type || !spot.isCleared() || spot.productInfo != null || spot.spotInfo.type == COMPOST)
            return false;
        String patchName = getPatchName(productInfo.type);
        String itemName = item.getDefinitions().getName().toLowerCase();
        int requiredAmount = (productInfo.type == ALLOTMENT || productInfo.type == HOPS) ? 3 : 1;
        boolean isTree = productInfo.type == TREES || productInfo.type == FRUIT_TREES;
        int level = productInfo.level;
        if (!player.getInventory().containsItem(item.getId(), requiredAmount)) {
            player.sendMessage("You don't have enough " + item.getDefinitions().getName().toLowerCase() + " to plant " + (patchName.startsWith("(?i)[^aeiou]") ? "an" : "a") + " " + patchName + " patch.");
            return true;
        } else if (player.getSkills().getLevel(Skills.FARMING) < level) {
            player.sendMessage("You need a farming level of " + level + " to plant this " + (isTree ? "sapling" : "seed") + ".");
            return true;
        }
        if (isTree) {
            if (!player.getInventory().containsOneItem(952) && !player.getToolBelt().contains(952)) {
                player.sendMessage("You need a spade to do this.");
                return false;
            }
        } else if (!player.getInventory().containsOneItem(5343)) {
            player.sendMessage("You need a seed dibber to do this.");
            return false;
        }
        player.sendMessage("You plant the " + itemName + " in the " + patchName + " patch.");
        player.setNextAnimation(isTree ? SPADE_ANIMATION : SEED_DIPPING_ANIMATION);
        player.getSkills().addXp(Skills.FARMING, isTree ? productInfo.experience : productInfo.plantingExperience);
        player.getInventory().deleteItem(new Item(item.getId(), requiredAmount));
        if (isTree) {
            if (player.getInventory().hasFreeSlots())
                player.getInventory().addItem(new Item(5350));
            else
                World.updateGroundItem(new Item(5350), player, player, 60, 0, false);
        }
        spot.setActive(productInfo);
        return true;
    }

    public boolean startWateringAction(final FarmingSpot spot, Item item) {
        if (spot == null || spot.productInfo == null)
            return false;
        if (item.getName().toLowerCase().startsWith("watering can(")) {
            player.sendMessage("You can't water your plants with an empty watering can!");
            return true;
        } else if (spot.isWatered()) {
            player.sendMessage("This patch has already been watered.");
            return true;
        } else if (spot.reachedMaxStage() || spot.productInfo.type == HERBS || spot.productInfo.type == COMPOST || spot.productInfo.type == TREES || spot.productInfo.type == FRUIT_TREES || spot.productInfo == ProductInfo.White_lily || spot.productInfo.type == BUSHES) {
            player.sendMessage("This patch doesn't need watering.");
            return true;
        } else if (spot.isDiseased()) {
            player.sendMessage("This crop is diseased and needs to be cured.");
            return true;
        } else if (spot.isDead()) {
            player.sendMessage("You can't water dead produce!");
            return true;
        }
        player.getInventory().deleteItem(item.getId(), 1);
        if (item.getId() == 5333)
            player.getInventory().addItem(5331, 1);
        else
            player.getInventory().addItem(item.getId() - 1, 1);
        player.sendMessage("You water the " + spot.productInfo + ".");
        player.setNextAnimation(WATERING_ANIMATION);
        spot.setWatered(true);

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                spot.refresh();
            }
        }, 2);
        return true;
    }

    public boolean startCureAction(final FarmingSpot spot, final Item item) {
        if (spot == null || spot.productInfo == null || spot.productInfo.type == COMPOST)
            return false;
        final boolean isTree = spot.productInfo.type == TREES || spot.productInfo.type == FRUIT_TREES;
        final boolean isBush = spot.productInfo.type == BUSHES;
        if (!spot.isDiseased()) {
            player.sendMessage("The produce in this patch isn't diseased and it doesn't need to be cured.");
            return true;
        }
        player.sendMessage(isTree ? "You prune the " + spot.productInfo.name().toLowerCase() + " tree's diseased branches." : isBush ? "You prune the " + spot.productInfo.name().toLowerCase() + " bush's diseased leaves." : "You treat the " + getPatchName(spot.spotInfo.type) + " patch with the plant cure.");
        player.setNextAnimation((isTree || isBush) ? PRUNING_ANIMATION : CURE_PLANT_ANIMATION);
        spot.setDiseased(false);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (!isTree && !isBush) {
                    player.getInventory().deleteItem(item);
                    player.getInventory().addItem(new Item(229, 1));
                } else
                    player.setNextAnimation(new Animation(-1));
                player.sendMessage("The produce in this patch has been restored to its natural health.");
                spot.refresh();
            }
        }, 2);
        return true;
    }

    public boolean startCompostAction(final FarmingSpot spot, final Item item, boolean superCompost, boolean ultraCompost) {
        if (spot == null || spot.spotInfo.type == COMPOST)
            return false;
        final String compostName = ultraCompost ? "ultracompost" : superCompost ? "supercompost" : "compost";
        if (spot.hasCompost()) {
            player.sendMessage("This patch is saturated by " + spot.getCompostName() + ".");
            return true;
        } else if (!spot.isCleared()) {
            player.sendMessage("The patch needs to be cleared in order to saturate it with compost.");
            return true;
        }
        player.sendMessage("You saturate the patch with " + compostName + ".");
        player.setNextAnimation(COMPOST_ANIMATION);
        if (ultraCompost)
            spot.setUltraCompost(true);
        else if (superCompost)
            spot.setSuperCompost(true);
        else
            spot.setCompost(true);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.getInventory().deleteItem(item);
                player.getInventory().addItem(EMPTY_BUCKET, 1);
                player.getSkills().addXp(Skills.FARMING, ultraCompost ? 36 : superCompost ? 26 : 18);
                spot.refresh();
            }
        }, 1);
        return true;
    }

    private void collectTreeProducts(final WorldObject tree, final FarmingSpot spot, final TreeDefinitions definitions) {
        player.getActionManager().setAction(new Woodcutting(tree, definitions, spot));
    }

    private void fillCompostBin(final FarmingSpot spot, final Item item) {
        final boolean[] attributes = isOrganicItem(item.getId());
        player.getActionManager().setAction(new Action() {

            @Override
            public boolean start(Player player) {
                if (item == null || !player.getInventory().containsItem(item.getId(), 1) || spot.isCleared())
                    return false;
                else if (!attributes[0]) {
                    player.sendMessage("You cannot add that to the compost bin.");
                    return false;
                }
                return true;
            }

            @Override
            public boolean process(Player player) {
                return spot.harvestAmount != 15 && player.getInventory().containsItem(item.getId(), 1);
            }

            @Override
            public int processWithDelay(Player player) {
                player.setNextAnimation(FILL_COMPOST_ANIMATION);
                player.getInventory().deleteItem(item.getId(), 1);
                if (attributes[1])
                    spot.setSuperCompost(true);
                else if (!spot.getSuperCompost())
                    spot.setCompost(true);
                spot.harvestAmount++;
                spot.refresh();
                return 1;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 1);
            }
        });
    }

    private void checkHealth(final FarmingSpot spot) {
        player.sendMessage("You examine the " + ((spot.productInfo.type == TREES || spot.productInfo.type == FRUIT_TREES) ? "tree" : "bush") + " for signs of disease, and quickly realise that it is at full health.");
        player.getSkills().addXp(Skills.FARMING, spot.productInfo.plantingExperience);
        player.setNextAnimation(CHECK_TREE_ANIMATION);
        spot.setChecked(true);
        tryAwardHarmonyMoss();
        spot.refresh();
    }

    private void tryAwardHarmonyMoss() {
        if (player.jujuPotions.isActive(Pots.Effects.PERFECT_FARMING_JUJU) &&
                ThreadLocalRandom.current().nextInt(20) == 0) {
            player.getInventory().addItemDrop(32947, 1);
            player.sendMessage("Through the power of juju, you receive harmony moss!");
        }
    }

    private double getFlyTrapThievingXp() {
        int farmingLevel = Math.min(99, Math.max(1, player.getSkills().getLevelForXp(Skills.FARMING)));
        int thievingLevel = Math.min(99, Math.max(1, player.getSkills().getLevelForXp(Skills.THIEVING)));
        double levelFactor = (farmingLevel + thievingLevel) / 198.0D;
        return Math.round(1170.0D * levelFactor * 10.0D) / 10.0D;
    }

    private double getSunchokeStrengthXp() {
        final double[] xp = {867, 906, 945, 966, 984, 1002, 1023, 1041, 1062, 1086,
                1107, 1128, 1149, 1170, 1200, 1212, 1236, 1257, 1278, 1302,
                1323, 1347, 1375, 1403, 1431, 1459, 1485, 1512, 1539, 1566};
        int level = Math.min(99, Math.max(70, player.getSkills().getLevelForXp(Skills.STRENGTH)));
        return xp[level - 70];
    }

    private String getPatchName(int type) {
        return PATCH_NAMES[type];
    }

    private int getRandomHarvestAmount(FarmingSpot spot) {
        ProductInfo info = spot.productInfo;
        int type = info.type;
        if (info == ProductInfo.FlyTrap || info == ProductInfo.Sunchoke)
            return 1;
        if (info == ProductInfo.Limpwurt)
            return Utils.random(3, 6);
        if (usesHarvestLives(type)) {
            int lives = getBaseHarvestLives(spot);
            int harvests = 0;
            while (lives > 0 && harvests < 60) {
                harvests++;
                if (!savesHarvestLife(info))
                    lives--;
            }
            return harvests;
        }
        return Utils.random(HARVEST_AMOUNTS[type][0], HARVEST_AMOUNTS[type][1] + 1);
    }

    private boolean usesHarvestLives(int type) {
        return type == ALLOTMENT || type == HOPS || type == HERBS || type == BELLADONNA;
    }

    private int getBaseHarvestLives(FarmingSpot spot) {
        int lives = 3;
        if (spot.getUltraCompost())
            lives += 3;
        else if (spot.getSuperCompost())
            lives += 2;
        else if (spot.getCompost())
            lives += 1;
        if (player.getPerkManager().hasPerkActive(DonationPerk.GREEN_THUMB))
            lives++;
        return lives;
    }

    private boolean savesHarvestLife(ProductInfo info) {
        int chance = 10 + player.getSkills().getLevel(Skills.FARMING) / 3;
        if (player.getEquipment().getWeaponId() == MagicSecs)
            chance += 10;
        if (player.getPerkManager().hasPerkActive(DonationPerk.GREEN_THUMB))
            chance += 10;
        if (info.type == HERBS)
            chance += 5;
        return Utils.random(100) < Math.min(75, chance);
    }

    private Animation getHarvestAnimation(int type) {
        if (type == ALLOTMENT || type == HOPS || type == TREES || type == MUSHROOMS || type == BELLADONNA)
            return SPADE_ANIMATION;
        else if (type == HERBS || type == FLOWERS) {
            if (player.getEquipment().getWeaponId() == 7409)
                return MAGIC_PICKING_ANIMATION;
            return type == HERBS ? HERB_PICKING_ANIMATION : FLOWER_PICKING_ANIMATION;
        } else if (type == FRUIT_TREES) {
            return FRUIT_PICKING_ANIMATION;
        } else if (type == BUSHES) {
            return BUSH_PICKING_ANIMATION;
        }
        return SPADE_ANIMATION;
    }

    private void sendNeedsWeeding(boolean cleared) {
        player.sendMessage(cleared ? "The patch is ready for planting." : "The patch needs weeding.");
    }

    private void openGuide() {
        player.sendMessage("Google 'RS Wiki Farming' if you need help with training Farming.");
    }

    private boolean[] isOrganicItem(int itemId) {
        boolean[] bools = new boolean[2];
        for (int organicId : COMPOST_ORGANIC) {
            if (itemId == organicId) {
                bools[0] = true;
                bools[1] = false;
            }
        }
        for (int organicId : SUPER_COMPOST_ORGANIC) {
            if (itemId == organicId) {
                bools[0] = true;
                bools[1] = true;
            }
        }
        return bools;
    }

    public void resetSpots() {
        spots.clear();
    }

    public void resetTreeTrunks() {
        for (FarmingSpot spot : spots) {
            if (spot.spotInfo.type == TREES || spot.spotInfo.type == FRUIT_TREES) {
                spots.remove(spot);
            }
        }
    }

    public int getWaterCan(Player player) {
        int[] waters = {5333, 5334, 5335, 5336, 5337, 5338, 5339, 5340};
        for (int i = 0; i < waters.length; i++) {
            if (player.getInventory().containsItem(waters[i], 1))
                return waters[i];
        }
        return -1;
    }

    public boolean checkWaterCan(Player player) {
        if (!player.getInventory().containsOneItem(5333, 5334, 5335, 5336, 5337, 5338, 5339, 5340)) {
            player.sendMessage("You will need a watering can with water in it to do this!");
            return true;
        }
        if (player.getInventory().containsItem(5340, 1)) {
            player.getInventory().deleteItem(5340, 1);
            player.getInventory().addItem(5339, 1);
        } else if (player.getInventory().containsItem(5339, 1)) {
            player.getInventory().deleteItem(5339, 1);
            player.getInventory().addItem(5338, 1);
        } else if (player.getInventory().containsItem(5338, 1)) {
            player.getInventory().deleteItem(5338, 1);
            player.getInventory().addItem(5337, 1);
        } else if (player.getInventory().containsItem(5337, 1)) {
            player.getInventory().deleteItem(5337, 1);
            player.getInventory().addItem(5336, 1);
        } else if (player.getInventory().containsItem(5336, 1)) {
            player.getInventory().deleteItem(5336, 1);
            player.getInventory().addItem(5335, 1);
        } else if (player.getInventory().containsItem(5335, 1)) {
            player.getInventory().deleteItem(5335, 1);
            player.getInventory().addItem(5334, 1);
        } else if (player.getInventory().containsItem(5334, 1)) {
            player.getInventory().deleteItem(5334, 1);
            player.getInventory().addItem(5333, 1);
        } else if (player.getInventory().containsItem(5333, 1)) {
            player.getInventory().deleteItem(5333, 1);
            player.getInventory().addItem(5331, 1);
        }
        return false;
    }

    public enum ProductInfo {

        /**
         * Allotments
         */
        Potato(5318, 1, 1942, 0, 8, 9, 10, ALLOTMENT), Onion(5319, 5, 1957, 1, 9.5, 10.5, 10, ALLOTMENT), Cabbage(5324, 7, 1965, 2, 10, 11.5, 10, ALLOTMENT), Tomato(5322, 12, 1982, 3, 12.5, 14, 10, ALLOTMENT), Sweetcorn(5320, 20, 5986, 4, 17, 19, 10, 6, ALLOTMENT), Strawberry(5323, 31, 5504, 5, 26, 29, 10, 6, 2, ALLOTMENT), Watermelon(5321, 47, 5982, 6, 48.5, 54.5, 10, 8, 4, ALLOTMENT), FlyTrap(28260, 93, -1, 7, 225, 50, 40, 5, 9, ALLOTMENT, true), SnapeGrass(28262, 80, 231, 8, 175, 80, 40, 5, 3, ALLOTMENT, true), Sunchoke(28261, 87, -1, 9, 208, 95, 40, 5, -3, ALLOTMENT, true),

        /**
         * Herbs
         */
        Guam(5291, 9, 199, 0, 11, 12.5, 10, HERBS), Marrentill(5292, 14, 201, 1, 13.5, 15, 10, HERBS), Tarromin(5293, 19, 203, 2, 16, 18, 10, HERBS), Harralander(5294, 26, 205, 3, 21.5, 24, 10, HERBS), Rannar(5295, 32, 207, 4, 27, 30.5, 10, HERBS), Toadflax(5296, 38, 3049, 5, 34, 38.5, 10, HERBS), Irit(5297, 44, 209, 6, 43, 48.5, 10, HERBS), Avantoe(5298, 50, 211, 7, 54.4, 61.5, 10, HERBS), Kwuarm(5299, 56, 213, 6, 69, 78, 10, HERBS), Snapdragon(5300, 62, 3051, 6, 87.5, 98.5, 10, HERBS), Cadantine(5301, 67, 215, 6, 106.5, 120, 10, HERBS), Lantadyme(5302, 73, 2485, 6, 134.5, 151.5, 10, HERBS), Dwarf(5303, 79, 217, 6, 170.5, 192, 10, HERBS), Torstol(5304, 85, 219, 6, 199.5, 224.5, 10, HERBS), Fellstalk(21621, 91, 21626, 6, 225, 315.6, 10, HERBS), Wergali(14870, 46, 14836, 8, 52.8, 52.8, 10, HERBS), Gout(6311, 65, 3261, 27, 105, 45, 10, HERBS),

        /**
         * Flowers
         */
        Marigold(5096, 2, 6010, 0, 8.5, 47, 5, FLOWERS), Rosemary(5097, 11, 6014, 1, 12, 66.5, 5, FLOWERS), Nasturtium(5098, 24, 6012, 2, 19.5, 111, 5, FLOWERS), Woad(5099, 25, 1793, 3, 20.5, 115.5, 5, FLOWERS), Limpwurt(5100, 26, 225, 4, 21.5, 120, 5, FLOWERS), White_lily(14589, 52, 14583, 6, 70, 250, 20, 4, -1, FLOWERS), Butterfly_Flower(28265, 88, 10020, 7, 210, 280, 5, 4, -1, FLOWERS),

        /**
         * Hops
         */
        Barley(5305, 3, 6006, 9, 8.5, 9.5, 10, 4, 1, HOPS), Hammerstone(5307, 4, 5994, 0, 9, 10, 10, 4, 1, HOPS), Asgarnian(5308, 8, 5996, 1, 10.9, 12, 10, 5, 3, HOPS), Jute(5306, 13, 5931, 10, 13, 14.5, 10, 5, 3, HOPS), Yanillian(5309, 16, 5998, 3, 14.5, 16, 10, 6, 1, HOPS), Krandorian(5310, 21, 6000, 5, 17.5, 19.5, 10, 7, HOPS), Wildbood(5311, 28, 6002, 7, 23, 26, 10, 7, 1, HOPS),

        /**
         * Trees
         */
        Oak(5370, 15, 6043, 1, 300, 14, 40, TREES), Willow(5371, 30, 6045, 6, 1000, 25, 40, 6, TREES), Maple(5372, 45, 6047, 17, 2500, 45, 40, 8, TREES), Yew(5373, 60, 6049, 26, 5000, 81, 40, 10, TREES), Magic(5374, 75, 6051, 41, 7500, 145.5, 40, 12, TREES),

        /**
         * Trees of the fruits :)
         */
        Apple(5496, 27, 1955, 1, 600, 22, 160, 6, FRUIT_TREES), Banana(5497, 33, 1963, 26, 800, 28, 160, 6, FRUIT_TREES), Orange(5498, 39, 2108, 65, 2000, 35.5, 160, 6, FRUIT_TREES), Curry(5499, 42, 5970, 90, 3000, 40, 160, 6, FRUIT_TREES), Pineapple(5500, 51, 2114, 4000, 1600, 57, 160, 6, FRUIT_TREES), Papaya(5501, 57, 5972, 154, 5000, 72, 160, 6, FRUIT_TREES), Palm(5502, 68, 5974, 193, 7000, 110.5, 160, 6, FRUIT_TREES),

        /**
         * Bushes of the bush
         */
        Redberry(5101, 10, 1951, -4, 64, 11.5, 20, 5, BUSHES), Cadavaberry(5102, 22, 753, 6, 102.5, 18, 20, 6, BUSHES), Dwellberry(5103, 36, 2126, 19, 177.5, 31.5, 20, 7, BUSHES), Jangerberry(5104, 48, 247, 31, 284.5, 50.5, 20, 8, BUSHES), Whiteberry(5105, 59, 239, 42, 437.5, 78, 20, 8, BUSHES), Poison_ivy(5106, 70, 6018, 188, 675, 120, 20, 8, BUSHES),

        MORCHELLA_MUSHROOM(21620, 74, 21622, 0, 160, 77.7, 80, 4, 3, MUSHROOMS),

        Compost_Bin(7836, 1, -1, 0, 8, 14, 2, 15, COMPOST);

        private static final Map<Integer, ProductInfo> products = new HashMap<Integer, ProductInfo>();

        static {
            for (ProductInfo product : ProductInfo.values())
                products.put(product.seedId, product);
        }

        private final int seedId;
        private final int level;
        public int productId;
        private final int configIndex;
        private final int type;
        private final int maxStage;
        private final int stageSkip;
        private final double experience;
        private final double plantingExperience;
        private final int cycleTime;
        private final boolean offsetStages;

        ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int maxStage, int stageSkip, int type) {
            this(seedId, level, productId, configIndex, plantingExperience, experience, cycleTime, maxStage, stageSkip, type, false);
        }

        ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int maxStage, int stageSkip, int type, boolean offsetStages) {
            this.seedId = seedId;
            this.level = level;
            this.productId = productId;
            this.configIndex = configIndex;
            this.plantingExperience = plantingExperience;
            this.experience = experience;
            this.cycleTime = cycleTime;
            this.maxStage = maxStage;
            this.stageSkip = stageSkip;
            this.type = type;
            this.offsetStages = offsetStages;
        }

        ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int type) {
            this(seedId, level, productId, configIndex, plantingExperience, experience, cycleTime, 4, 0, type);
        }

        ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int maxStage, int type) {
            this(seedId, level, productId, configIndex, plantingExperience, experience, cycleTime, maxStage, 0, type);
        }

        public static ProductInfo getProduct(int itemId) {
            return products.get(itemId);
        }
    }

    public enum SpotInfo {

        Prif_Tree(93288, TREES), Talvery_Tree(8388, TREES), Falador_Garden_Tree(8389, TREES), Varrock_Tree(8390, TREES), Lumbridge_Tree(8391, TREES), Gnome_Tree(19147, TREES), Gnome_Strong_Fruit_Tree(7962, FRUIT_TREES), Prif_Fruit_Tree(94323, FRUIT_TREES), Gnome_Fruit_Tree(7963, FRUIT_TREES), Brimhaven_Fruit_Tree(7964, FRUIT_TREES), Catherby_Fruit_Tree(7965, FRUIT_TREES), Lletya_Fruit_Tree(28919, FRUIT_TREES), Falador_Allotment_North(8550, ALLOTMENT), Falador_Allotment_South(8551, ALLOTMENT), Catherby_Allotment_North(8552, ALLOTMENT), Catherby_Allotment_South(8553, ALLOTMENT), Ardougne_Allotment_North(8554, ALLOTMENT), Ardougne_Allotment_South(8555, ALLOTMENT), Canfis_Allotment_North(8556, ALLOTMENT), Canfis_Allotment_South(8557, ALLOTMENT), Yannile_Hops_Patch(8173, HOPS), Talvery_Hops_Patch(8174, HOPS), Lumbridge_Hops_Patch(8175, HOPS), McGrubor_Hops_Patch(8176, HOPS), Falador_Flower(7847, FLOWERS), Catherby_Flower(7848, FLOWERS), Ardougne_Flower(7849, FLOWERS), Canfis_Flower(7850, FLOWERS), Champions_Bush(7577, BUSHES), Rimmington_Bush(7578, BUSHES), Etceteria_Bush(7579, BUSHES), South_Arddougne_Bush(7580, BUSHES), Prif_Bush(93285, BUSHES), Falador_Herb_Patch(8150, HERBS), Catherby_Herb_Patch(8151, HERBS), Ardougne_Herb_Patch(8152, HERBS), Canfis_Herb_Patch(8153, HERBS), Prif_Herb_Patch(93287, HERBS), Diamond_Herb_Patch(18816, HERBS), Wildy_Herb(104327, HERBS), Falador_Compost_Bin(7836, COMPOST), Catherby_Bin(7837, COMPOST), Port_Phasymatis_Bin(7838, COMPOST), Ardougn_Bin(7839, COMPOST), Taverly_Bin(66577, COMPOST), Mushroom_Special(8337, MUSHROOMS), Belladonna(7572, BELLADONNA);

        private static final Map<Short, SpotInfo> informations = new HashMap<Short, SpotInfo>();
        private static final List<SpotInfo> INFO = new ArrayList<SpotInfo>();

        static {
            for (SpotInfo information : SpotInfo.values()) {
                informations.put((short) information.objectId, information);
                INFO.add(information);
            }
        }

        private final int objectId;
        private final int configFileId;
        private final int type;

        SpotInfo(int objectId, int type) {
            this.objectId = objectId;
            this.configFileId = ObjectDefinitions.getObjectDefinitions(objectId).configFileId;
            this.type = type;
        }

        public static SpotInfo getInfo(int objectId) {
            return informations.get((short) objectId);
        }

        public static List<SpotInfo> getInfo() {
            return INFO;
        }
    }

    public class FarmingSpot implements Serializable {

        private static final long serialVersionUID = -732322970478931771L;

        private final SpotInfo spotInfo;
        public ProductInfo productInfo;
        private int stage;
        private long cycleTime;
        private int harvestAmount;
        private final boolean[] attributes;

        public FarmingSpot(SpotInfo spotInfo) {
            this.spotInfo = spotInfo;
            cycleTime = Utils.currentTimeMillis();
            stage = 0; // stage 0 is default null
            harvestAmount = 0;
            attributes = new boolean[10]; // diseased, watered, dead,
            // firstCycle, usingCompost,
            // usingSuperCompost;
            renewCycle();
            spots.add(this);
        }

        public void setActive(ProductInfo productInfo) {
            setProductInfo(productInfo);
            stage = 0;
            resetCycle(true);
            refresh();
            renewCycle();
        }

        private void resetCycle() {
            resetCycle(false);
        }

        private void resetCycle(boolean keepCompost) {
            boolean compost = keepCompost && attributes[7];
            boolean superCompost = keepCompost && attributes[8];
            cycleTime = Utils.currentTimeMillis();
            harvestAmount = 0;
            for (int index = 0; index < attributes.length; index++) {
                if (index == 4)
                    continue;
                attributes[index] = false;
            }
            attributes[7] = compost;
            attributes[8] = superCompost;
        }

        public void setCycleTime(long cycleTime) {
            setCycleTime(false, cycleTime);
        }

        public void setCycleTime(boolean reset, long cycleTime) {
            if (reset)
                this.cycleTime = 0;
            if (this.cycleTime == 0)
                this.cycleTime = Utils.currentTimeMillis();
            this.cycleTime += cycleTime;
        }

        public void setIdle() {
            stage = 3;
            setProductInfo(null);
            refresh();
            resetCycle();
        }

        public int getStage() {
            return stage;
        }

        public void process() {
            if (cycleTime == 0)
                return;
//here
            while (cycleTime < Utils.currentTimeMillis()) {
                if (productInfo != null) {
                    if (hasChecked() && (isEmpty() || !hasMaximumRegeneration())) {
                        if (isEmpty()) {
                            cycleTime = 0;
                        } else if (!hasMaximumRegeneration()) {
                            if (harvestAmount == 5)
                                cycleTime = 0;
                            harvestAmount++;
                        } else
                            cycleTime = 0;
                        refresh();
                        return;
                    } else {
                        increaseStage();
                        if (reachedMaxStage() || isDead() || isEmpty()) {
                            cycleTime = 0;
                            break;
                        }
                    }
                } else {
                    if (spotInfo.type != COMPOST) {
                        desecreaseStage();
                        if (stage <= 0) {
                            remove();
                            break;
                        }
                    }
                }
                renewCycle();
            }
        }

        public int getConfigBaseValue() {
            if (productInfo != null) {
                if (productInfo.type == ALLOTMENT)
                    return 6 + (productInfo.configIndex * 7);
                else if (productInfo.type == HERBS)
                    return 4 + (productInfo.configIndex * 7);
                else if (productInfo.type == FLOWERS)
                    return 8 + (productInfo.configIndex * 5);
                else if (productInfo.type == HOPS)
                    return 3 + (productInfo.configIndex * 5);
                else if (productInfo.type == TREES || productInfo.type == FRUIT_TREES || productInfo.type == BUSHES)
                    return 8 + (productInfo.configIndex ^ 2 - 1);
            }
            return stage;
        }

        private int getConfigValue(int type) {
            if (type == HERBS)
                return isDead() ? stage + 169 : getConfigBaseValue() + ((isDiseased() && stage != 0) ? stage + 127 : stage);
            else if (type == TREES) {
                int baseValue = getConfigBaseValue() + (isDead() ? stage + 128 : (isDiseased() && stage != 0) ? stage + 64 : stage);
                if (hasChecked()) {
                    baseValue += 2;
                    if (!isEmpty())
                        baseValue--;
                }
                return baseValue;
            } else if (type == FRUIT_TREES) {
                int baseValue = stage + getConfigBaseValue();
                if (hasChecked())
                    baseValue += getHarvestAmount();
                else if (isDead())
                    baseValue += 20;
                else if (isDiseased())
                    baseValue += 12;
                else if (!hasChecked() && reachedMaxStage())
                    baseValue += 20;
                if (isEmpty())
                    baseValue += 19;
                return baseValue;
            } else if (type == BUSHES) {
                int baseValue = stage + getConfigBaseValue();
                if (hasChecked())
                    baseValue += getHarvestAmount();
                else if (isDead())
                    baseValue += 128;
                else if (isDiseased())
                    baseValue += 65;
                else if (!hasChecked() && reachedMaxStage()) // 250
                    baseValue += 240;
                return baseValue;
            } else if (type == COMPOST) {
                return isCleared() ? harvestAmount + 16 + (hasChecked() ? -1 : 0) : productInfo != null && reachedMaxStage() ? 0 : harvestAmount - stage;
            } else if (type == FLOWERS || type == HOPS || type == ALLOTMENT)
                return getConfigBaseValue() + (isDead() ? stage + (productInfo.offsetStages ? (productInfo == ProductInfo.Sunchoke ? 89 : productInfo == ProductInfo.FlyTrap ? 85 : 87) : 192) : (isDiseased() && stage != 0) ? stage + (productInfo.offsetStages ? (productInfo == ProductInfo.Sunchoke ? 25 : productInfo == ProductInfo.FlyTrap ? 21 : 23) : 128) : isWatered() ? 64 + stage : stage);
            return stage + getConfigBaseValue();
        }

        private void checkFactors() {
            if (isDiseased()) {
                if (reachedMaxStage()) {
                    setDead(false);
                    setDiseased(false);
                } else {
                    if (isFirstCycle())
                        setFirstCycle(false);
                    else
                        setDead(true);
                }
            }
            if (productInfo.type == FRUIT_TREES || productInfo.type == BUSHES) {
                if (reachedMaxStage())
                    setHarvestAmount(productInfo.type == BUSHES || productInfo == ProductInfo.Palm ? 4 : 6);
            }
            setWatered(false);

        }

        public boolean reachedMaxStage() {
            if (productInfo == null)
                return false;
            return stage == productInfo.maxStage;
        }

        private boolean hasMaximumRegeneration() {
            if (spotInfo.type != FRUIT_TREES && spotInfo.type != BUSHES)
                return true;
            else return getHarvestAmount() == HARVEST_AMOUNTS[productInfo.type][1];
        }

        public void renewCycle() {
            if (productInfo != null) {
                long ms = Math.round(BASE_TICK_MS * CROP_TICK_SCALE * getCropCycleScale() * productInfo.cycleTime);
                cycleTime += ms;
            } else {
                // Weeds regress from stage 3 ? 0 in three steps. Slow that down independently.
                cycleTime += Math.round(BASE_TICK_MS * WEED_TICK_SCALE * 3);
            }
        }

        private double getCropCycleScale() {
            return productInfo.type == HERBS ? 2.0 : 1.0;
        }


        public long getCycleTime() {
            long diff = cycleTime - Utils.currentTimeMillis();
            if (diff < 0) {
                diff = 0;
            }
            return diff;
        }

        public boolean canBeDiseased() {
            return (stage != 0 || productInfo.type == BUSHES) && !reachedMaxStage() && !isDiseased() && productInfo != ProductInfo.White_lily && productInfo != ProductInfo.Poison_ivy && productInfo.type != COMPOST;
        }

        private boolean checkDisease() {
            if (canBeDiseased()) {
                int odds = DISEASE_ODDS_BASE;
                if (isWatered()) odds += DISEASE_ODDS_WATERED_BONUS;
                if (getUltraCompost()) odds += DISEASE_ODDS_ULTRA_BONUS;
                else if (getSuperCompost()) odds += DISEASE_ODDS_SUPER_BONUS;
                else if (getCompost()) odds += DISEASE_ODDS_COMPOST_BONUS;
                if (player.getPerkManager().hasPerkActive(DonationPerk.GREEN_THUMB))
                    odds += DISEASE_ODDS_PERK_BONUS;

                if (Utils.getRandom(odds) == 0) {
                    setDiseased(true);
                    setFirstCycle(true); // one growth stage grace to cure
                    refresh();
                    return true;
                }
            }
            return false;
        }


        public void increaseStage() {
            if (productInfo != null && !isDead() && !isEmpty()) {
                if (isDiseased()) {
                    checkFactors();
                    refresh();
                    return;
                }
                if (checkDisease()) {
                    setWatered(false);
                    refresh();
                    return;
                }
            }
            stage++;
            if (productInfo != null && !isDead() && !isEmpty())
                checkFactors();
            refresh();
        }

        public void desecreaseStage() {
            setCleared(false);
            stage--;
            refresh();
        }

        private void remove() {
            spots.remove(this);
        }

        public void refresh() {
            int value = spotInfo.type == COMPOST ? getConfigValue(spotInfo.type) : productInfo != null ? getConfigValue(spotInfo.type) + productInfo.stageSkip + (productInfo.offsetStages && stage > 0 ? (stage == productInfo.maxStage ? (productInfo == ProductInfo.Sunchoke ? 17 : productInfo == ProductInfo.FlyTrap ? 5 : 11) : (productInfo == ProductInfo.Sunchoke ? 85 : productInfo == ProductInfo.FlyTrap ? 73 : 79)) : 0) : stage;
            player.getPackets().sendConfigByFile(spotInfo.configFileId, value, true);
        }

        public void setProductInfo(ProductInfo productInfo) {
            this.productInfo = productInfo;
        }

        public boolean isDiseased() {
            return !player.getPerkManager().hasPerkActive(DonationPerk.GREEN_THUMB) && attributes[0];
        }

        public void setDiseased(boolean diseased) {
            this.attributes[0] = diseased;
        }

        public boolean isWatered() {
            return attributes[1];
        }

        public void setWatered(boolean watered) {
            this.attributes[1] = watered;
        }

        public boolean isDead() {
            return attributes[2];
        }

        public void setDead(boolean dead) {
            this.attributes[2] = dead;
            if (dead)
                setDiseased(false);
        }

        public boolean isFirstCycle() {
            return attributes[3];
        }

        public void setFirstCycle(boolean firstCycle) {
            this.attributes[3] = firstCycle;
        }

        public boolean isCleared() {
            return attributes[4];
        }

        public void setCleared(boolean cleared) {
            this.attributes[4] = cleared;
        }

        public boolean hasChecked() {
            return attributes[5];
        }

        public void setChecked(boolean checked) {
            this.attributes[5] = checked;
        }

        public boolean isEmpty() {
            return attributes[6];
        }

        public void setEmpty(boolean empty) {
            this.attributes[6] = empty;
        }

        public boolean hasCompost() {
            return attributes[7] || attributes[8];
        }

        public String getCompostName() {
            if (getUltraCompost())
                return "ultracompost";
            if (getSuperCompost())
                return "supercompost";
            return "compost";
        }

        public boolean getUltraCompost() {
            return attributes[7] && attributes[8];
        }

        public void setUltraCompost(boolean ultraCompost) {
            this.attributes[7] = ultraCompost;
            this.attributes[8] = ultraCompost;
        }

        public boolean getSuperCompost() {
            return attributes[8] && !getUltraCompost();
        }

        public void setSuperCompost(boolean superCompost) {
            this.attributes[8] = superCompost;
            if (superCompost)
                this.attributes[7] = false;
        }

        public boolean hasGivenAmount() {
            return attributes[9];
        }

        public void setHasGivenAmount(boolean amount) {
            this.attributes[9] = amount;
        }

        public boolean getCompost() {
            return attributes[7];
        }

        public void setCompost(boolean compost) {
            this.attributes[7] = compost;
            if (compost)
                this.attributes[8] = false;
        }

        public boolean hasEmptyHarvestAmount() {
            return harvestAmount == 0;
        }

        public int getHarvestAmount() {
            return harvestAmount;
        }

        public void setHarvestAmount(int harvestAmount) {
            this.harvestAmount = harvestAmount;
        }
    }
}
