package com.rs.game.player.actions.woodcutting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.FarmingManager.FarmingSpot;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.Native950Woodcutting;
import com.rs.game.player.client.Native950Skilling;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.player.actions.firemaking.defs.Log;
import com.rs.game.player.actions.hunter.TrapAction;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.HatchetDefinitions;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.TreeDefinitions;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.items.BirdNests;
import com.rs.game.player.content.items.EliteOutfits;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.skillingcontracts.impl.WoodcuttingContractList;
import com.rs.game.player.controllers.Wilderness;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

public final class Woodcutting extends Action {

    private static final long ELDER_TREE_ACTIVE_TIME = 5 * 60 * 1000L;
    private static final long ELDER_TREE_REPLENISH_TIME = 10 * 60 * 1000L;
    private static final String ELDER_TREE_ATTRIBUTE_PREFIX = "woodcuttingElderTree:";

    private final WorldObject tree;
    private final TreeDefinitions definitions;
    private final FarmingSpot spot;

    private int emoteId;
    private final boolean usingBeaver = false;
    private int hatchetPower;

    private boolean usedDeplateAurora;
    private int nativeAnimationDelay;

    public Woodcutting(WorldObject tree, TreeDefinitions definitions, FarmingSpot spot) {
        this.tree = tree;
        this.definitions = definitions;
        this.spot = spot;
    }

    private void addLogsChopped(Player player, int amount) {
        for (int i = 0; i < amount; i++)
            player.addLogsChopped();
    }

    private void addLogsBurned(Player player, int amount) {
        for (int i = 0; i < amount; i++)
            player.addLogsBurned();
    }

    private boolean shouldInfernoAdzeBurn(Player player) {
        return player.getEquipment().getWeaponId() == 13661 && ThreadLocalRandom.current().nextInt(3) == 0;
    }

    private boolean requiresFreeInventorySlot(Player player) {
        if (definitions == TreeDefinitions.DONOR_TREE)
            return true;
        return definitions.getLogsId() != -1 && !player.woodSpirit;
    }

    public static double woodcuttingSet(Player player) {
        double xpBoost = 1.0;
        boolean wearingFullLumberjack = player.getEquipment().getChestId() == 10939
                && player.getEquipment().getLegsId() == 10940
                && player.getEquipment().getHatId() == 10941
                && player.getEquipment().getBootsId() == 10933;
        if (player.getEquipment().getChestId() == 10939)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 10940)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 10941)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 10933)
            xpBoost *= 1.01;
        if (wearingFullLumberjack)
            xpBoost *= 1.01;
        if (!wearingFullLumberjack && EliteOutfits.wearingSentinel(player) && player.hasItems(10939, 10940, 10941, 10933))
            xpBoost *= 1.05;
        if (Wilderness.isAtWild(player) && player.getEquipment().getGlovesId() == 13850)
            xpBoost *= 1.04;
        return xpBoost;
    }

    private void addLog(Player player) {
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("dragon hatchet") || weapon.getName().toLowerCase().contains("crystal hatchet"));
        if (!hasAugmentedTool)
            weapon = null;
        int amount = 1;
        if (player.getPerkManager().hasPerkActive(DonationPerk.LUMBER_LEGEND) && Utils.random(100) <= 33)
            amount++;
        if (definitions == TreeDefinitions.DONOR_TREE) {
            int log = Utils.random(9);
            double xp = 25;
            Item item = null;
            switch (log) {
                case 0: /** Normal **/
                    item = new Item(1511, amount);
                    break;
                case 1: /** Oak **/
                    item = new Item(1521, amount);
                    xp = 37.5;
                    break;
                case 2: /** Willow **/
                    item = new Item(1519, amount);
                    xp = 67.5;
                    break;
                case 3: /** Maple **/
                    item = new Item(1517, amount);
                    xp = 100;
                    break;
                case 4: /** Mahogany **/
                    item = new Item(6332, amount);
                    xp = 125;
                    break;
                case 5: /** Yew **/
                    item = new Item(1515, amount);
                    xp = 175;
                    break;
                case 6: /** Magic **/
                    item = new Item(1513, amount);
                    xp = 250;
                    break;
                case 7: /** Elder **/
                    item = new Item(29556, amount);
                    xp = 325;
                    break;
                case 8: /** Teak **/
                case 9:
                    item = new Item(6333, amount);
                    xp = 85;
                    break;
            }
            if (player.getSkillingTask() == 8 && item != null && player.getDailyManager().getTask() != null) {
                if (item.getId() == player.getTaskItemId()) {
                    player.getDailyManager().processTask();
                }
            }
            BirdNests.dropNest(player);
            double xpToGive = xp * woodcuttingSet(player) * amount;
            int skillChompa = TrapAction.getSkillChompa(player);
            if (skillChompa != -1) {
                player.getEquipment().removeAmmo(skillChompa, -1);
                xpToGive *= 1.0;
            }
            Log log2 = Log.forId(item.getId());
            if (log2 != null) {
                WoodcuttingContractList.listen(player, log2, amount);
            }
            player.getInventionManager().processSkillXp(Skills.WOODCUTTING, xpToGive, weapon);
            player.getSkills().addXp(Skills.WOODCUTTING, xpToGive);
            addLogsChopped(player, amount);
            player.getPackets().sendGameMessage("You get some " + item.getName().toLowerCase() + "; total chopped: " + Colors.RED + Utils.getFormattedNumber(player.getLogsChopped()) + "</col>.", true);
            PetPerkHandler.handleExtraArms(player, item);
            if (shouldInfernoAdzeBurn(player)) {
                addLogsBurned(player, amount);
                player.getSkills().addXp(Skills.FIREMAKING, Firemaking.increasedExperience(player, (xp + Utils.random(2, 5)) * amount));
                player.getPackets().sendGameMessage("The adze's heat instantly incinerates the " + item.getName().toLowerCase() + "; " + "logs burned: " + Colors.RED + Utils.getFormattedNumber(player.getLogsBurned()) + "</col>.", true);
                World.sendProjectile(new NewProjectile(player, new WorldTile(player.getX(), player.getY() - 3, 0), 1776, 30, 0, 15, 0));
            } else {
                if (player.getInventionManager().procGathering(weapon, item, Skills.WOODCUTTING, xpToGive))
                    player.getInventory().addItem(item);
            }
            return;
        }
        boolean jujuActive = player.jujuPotions.isActive(Pots.Effects.PERFECT_WOODCUTTING_JUJU) && ThreadLocalRandom.current().nextInt(20) == 0;
        if (jujuActive) {
            amount++;
            player.sendMessage("Through the power of juju, you cut an additional log!");
        }

        double totalXp = definitions.getXp() * woodcuttingSet(player) * amount;
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            player.getEquipment().removeAmmo(skillChompa, -1);
        }
        Log log = Log.forId(definitions.getLogsId());
        if (log != null) {
            WoodcuttingContractList.listen(player, log, amount);
        }

        if (ThreadLocalRandom.current().nextInt(10) == 0 &&
                player.jujuPotions.isActive(Pots.Effects.WOODCUTTING_JUJU)) {
            player.jujuPotions.sendWoodSpirit();
        }

        player.getInventionManager().processSkillXp(Skills.WOODCUTTING, totalXp, weapon);
        player.getSkills().addXp(Skills.WOODCUTTING, totalXp);
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
        if (!(definitions == TreeDefinitions.JADE_ROOT_HEALTHY || definitions == TreeDefinitions.JADE_ROOT_MUTATED || definitions == TreeDefinitions.CRYSTAL_TREE_SHARD))
            BirdNests.dropNest(player);
        if (definitions == TreeDefinitions.IVY) {
            player.getPackets().sendGameMessage("You successfully cut an ivy vine.", true);
        } else if (definitions == TreeDefinitions.CRYSTAL_TREE_SHARD) {
            player.getPackets().sendGameMessage("You successfully chip away at the crystal tree.", true);
        } else {
            addLogsChopped(player, amount);
            String logName = ItemDefinitions.getItemDefinitions(definitions.getLogsId()).getName().toLowerCase();
            player.getPackets().sendGameMessage("You get some " + logName + "; total chopped: " + Colors.RED + Utils.getFormattedNumber(player.getLogsChopped()) + "</col>.", true);
            if (shouldInfernoAdzeBurn(player)) {
                addLogsBurned(player, amount);
                player.getSkills().addXp(Skills.FIREMAKING, Firemaking.increasedExperience(player, definitions.getXp() * amount));
                player.getPackets().sendGameMessage("The adze's heat instantly incinerates the " + logName + "; " + "logs burned: " + Utils.getFormattedNumber(player.getLogsBurned()) + ".", true);
                World.sendProjectile(new NewProjectile(player, new WorldTile(player.getX(), player.getY() - 3, 0), 1776, 30, 0, 15, 0));
            } else {

                ItemDefinitions def = ItemDefinitions.getItemDefinitions(definitions.getLogsId());
                int newAmount = amount;

                if (player.getPerkManager().hasPerkActive(DonationPerk.GREEN_THUMB) && spot != null && !def.isStackable()) {
                    boolean otherJujuActive = player.jujuPotions.isActive(Pots.Effects.PERFECT_FARMING_JUJU) &&
                            ThreadLocalRandom.current().nextInt(40) == 3;
                    if (otherJujuActive) {
                        player.getInventory().addItemDrop(32947, 1);
                        player.sendMessage("Through the power of juju, you receive harmony moss!");
                    }
                    if(player.woodSpirit) {
                        player.getBank().addItem(new Item(definitions.getLogsId(), newAmount), false);
                    } else {
                        player.getInventory().addItem(def.certId, newAmount);
                    }
                } else {
                    if (player.getInventionManager().procGathering(weapon, definitions.getLogsId(), Skills.WOODCUTTING, totalXp)) {
                        if(player.woodSpirit) {
                            player.getBank().addItem(new Item(definitions.getLogsId(), newAmount), false);
                        } else {
                            player.getInventory().addItem(definitions.getLogsId(), newAmount);
                        }
                    }
                }

                PetPerkHandler.handleExtraArms(player, new Item(definitions.getLogsId(), 1));
            }
            return;
        }
        player.addLogsChopped();
    }

    private boolean checkAll(Player player) {
        if (player.isNative950()) return checkNative(player);
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.sendMessage("Please finish what you're doing before doing this action.");
            return false;
        }
        if (!hasAxe(player)) {
            player.getPackets().sendObjectMessage(0, 15263739, tree, "You need a hatchet to chop down the " + (tree.getId() >= 87508 && tree.getId() <= 87530 ? "elder tree." : spot != null ? "tree." : tree.getDefinitions().name + "."));
            return false;
        }
        if (!setAxe(player)) {
            int skillChompa = TrapAction.getSkillChompa(player);
            if (skillChompa == -1)
                player.getPackets().sendObjectMessage(0, 15263739, tree, "You don't have the required level to use your " + new Item(player.getEquipment().getWeaponId()).getName() + ".");
            return false;
        }
        if (!hasWoodcuttingLevel(player))
            return false;
        if (isElderTreeReplenishing(player))
            return false;
        if (requiresFreeInventorySlot(player) && !player.getInventory().hasFreeSlots()) {
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return false;
        }
        return true;
    }

    private boolean checkTree(Player player) {
        if (player.isNative950()) return Native950Woodcutting.current(tree);
        return spot != null ? !spot.isEmpty() : World.containsObjectWithId(tree, tree.getId());
    }

    private int getWoodcuttingDelay(Player player) {
        return 4;
    }

    private boolean rollWoodcuttingSuccess(Player player) {
        if (definitions == TreeDefinitions.DONOR_TREE)
            return true;
        int roll = getWoodcuttingRoll(player);
        return ThreadLocalRandom.current().nextInt(255) < roll;
    }

    private int getWoodcuttingRoll(Player player) {
        double lowChance = lowChopChance(definitions, getHatchetPower());
        double highChance = Math.floor(lowChance * getTreeRatio(definitions));
        if (player.isNative950())
            return interpolateChopChance(lowChance, highChance, player.getSkills().getLevel(Skills.WOODCUTTING));
        highChance = Math.floor(highChance * player.getAuraManager().getWoodcuttingAccurayMultiplier());
        if (player.getPerkManager().hasPerkActive(DonationPerk.LUMBER_LEGEND))
            highChance = Math.floor(highChance * 1.05);
        if (EliteOutfits.wearingSentinel(player))
            highChance = Math.floor(highChance * getSentinelSuccessMultiplier(player));
        int summoningBonus = player.getFamiliar() != null && (player.getFamiliar().getId() == 6808 || player.getFamiliar().getId() == 6807) ? 2 : 0;
        int level = Math.max(1, player.getSkills().getLevel(Skills.WOODCUTTING) + summoningBonus);
        return interpolateChopChance(lowChance, highChance, level);
    }

    private double getSentinelSuccessMultiplier(Player player) {
        if (EliteOutfits.Sentinel.isWearing("nature's", player))
            return 1.07;
        return 1.05;
    }

    /** Original low/high chance curve, shared by native play and the legacy bonuses path. */
    public static int ordinaryChopChance(TreeDefinitions type, int level, int power) {
        double low=lowChopChance(type,power);
        return interpolateChopChance(low,Math.floor(low*getTreeRatio(type)),level);
    }
    private static double lowChopChance(TreeDefinitions type, int power) {
        double base=getBaseChopChance(type);
        return base+(base/2.0)*((Math.max(100,power)-100)/100.0);
    }
    private static int interpolateChopChance(double low, double high, int level) {
        double chance=low+((Math.min(99,Math.max(1,level))-1)*(high-low)/98.0);
        return Math.max(1,Math.min(254,(int)Math.floor(chance)));
    }
    private static int getBaseChopChance(TreeDefinitions definitions) {
        switch (definitions) {
            case NORMAL:
            case EVERGREEN:
            case DEAD:
            case FRUIT_TREES:
                return 80;
            case OAK:
                return 45;
            case WILLOW:
            case TEAK:
                return 30;
            case MAPLE:
            case MAHOGANY:
                return 16;
            case IVY:
                return 14;
            case YEW:
                return 6;
            case MAGIC:
            case CURSED_MAGIC:
            case ELDER:
            case DREAM_TREE:
                return 3;
            case CRYSTAL_TREE_SHARD:
                return 2;
            case BAMBOO:
                return 18;
            default:
                return 10;
        }
    }

    private static double getTreeRatio(TreeDefinitions definitions) {
        switch (definitions) {
            case NORMAL:
            case EVERGREEN:
            case DEAD:
            case FRUIT_TREES:
                return 2.0;
            case MAGIC:
            case CURSED_MAGIC:
            case ELDER:
            case CRYSTAL_TREE_SHARD:
                return 3.5;
            default:
                return 3.125;
        }
    }

    private int getHatchetPower() {
        return Math.max(100, hatchetPower);
    }

    private boolean rollTreeFell() {
        int chance = getTreeFellChance();
        return chance > 0 && ThreadLocalRandom.current().nextInt(256) < chance;
    }

    private int getTreeFellChance() {
        switch (definitions) {
            case NORMAL:
            case EVERGREEN:
            case DEAD:
            case FRUIT_TREES:
                return 96;
            case BLOOD:
                return 64;
            case ELDER:
            case DONOR_TREE:
            case DREAM_TREE:
                return 0;
            case BAMBOO:
                return 8;
            default:
                return definitions.getRandomLifeProbability() <= 0 ? 0 : 32;
        }
    }

    private String getElderTreeAttributeKey(String suffix) {
        return ELDER_TREE_ATTRIBUTE_PREFIX + tree.getPlane() + ':' + tree.getX() + ':' + tree.getY() + ':' + suffix;
    }

    private long getLongAttribute(Player player, String key) {
        Object value = player.getTemporaryAttributtes().get(key);
        return value instanceof Long ? (Long) value : 0L;
    }

    private boolean isElderTreeReplenishing(Player player) {
        if (definitions != TreeDefinitions.ELDER)
            return false;
        String replenishKey = getElderTreeAttributeKey("replenishUntil");
        long replenishUntil = getLongAttribute(player, replenishKey);
        if (replenishUntil > Utils.currentTimeMillis()) {
            player.sendMessage("This elder tree is regrowing and cannot be chopped yet.");
            return true;
        }
        if (replenishUntil != 0L)
            player.getTemporaryAttributtes().remove(replenishKey);
        return false;
    }

    private void startElderTreeCycle(Player player) {
        if (definitions != TreeDefinitions.ELDER)
            return;
        long now = Utils.currentTimeMillis();
        String activeKey = getElderTreeAttributeKey("activeUntil");
        String replenishKey = getElderTreeAttributeKey("replenishUntil");
        long replenishUntil = getLongAttribute(player, replenishKey);
        if (replenishUntil > now)
            return;
        if (replenishUntil != 0L)
            player.getTemporaryAttributtes().remove(replenishKey);
        if (getLongAttribute(player, activeKey) <= now)
            player.getTemporaryAttributtes().put(activeKey, now + ELDER_TREE_ACTIVE_TIME);
    }

    private boolean depleteElderTreeIfNeeded(Player player) {
        if (definitions != TreeDefinitions.ELDER)
            return false;
        String activeKey = getElderTreeAttributeKey("activeUntil");
        long activeUntil = getLongAttribute(player, activeKey);
        if (activeUntil == 0L) {
            startElderTreeCycle(player);
            return false;
        }
        if (activeUntil > Utils.currentTimeMillis())
            return false;
        player.getTemporaryAttributtes().remove(activeKey);
        player.getTemporaryAttributtes().put(getElderTreeAttributeKey("replenishUntil"), Utils.currentTimeMillis() + ELDER_TREE_REPLENISH_TIME);
        player.setNextAnimation(new Animation(-1));
        player.sendMessage("This elder tree has been depleted and needs time to regrow.");
        return true;
    }

    private boolean hasAxe(Player player) {
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1)
            return true;
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        if (weapon != null && weapon.getInventionData() != null) {
            String name = weapon.getName().toLowerCase();
            if (name.contains("dragon hatchet") || name.contains("crystal hatchet"))
                return true;
        }
        if (player.getInventory().containsOneItem(1351, 1349, 1353, 1355, 1357, 1361, 1359, 6739, 13661, 32645))
            return true;
        if (player.getToolBelt().contains(1351) || player.getToolBelt().contains(1349)
                || player.getToolBelt().contains(1353) || player.getToolBelt().contains(1361)
                || player.getToolBelt().contains(1355) || player.getToolBelt().contains(1357)
                || player.getToolBelt().contains(1359) || player.getToolBelt().contains(6739)
                || player.getToolBelt().contains(13661) || player.getToolBelt().contains(32645))
            return true;
        int weaponId = player.getEquipment().getWeaponId();
        if (weaponId == -1)
            return false;
        switch (weaponId) {
            case 1351:// Bronze Axe
            case 1349:// Iron Axe
            case 1353:// Steel Axe
            case 1361:// Black Axe
            case 1355:// Mithril Axe
            case 1357:// Adamant Axe
            case 1359:// Rune Axe
            case 6739:// Dragon Axe
            case 13661: // Inferno adze
            case 32645: // Crystal hatchet
                return true;
            default:
                return false;
        }
    }

    private boolean hasWoodcuttingLevel(Player player) {
        if (definitions.getLevel() > player.getSkills().getLevel(8)) {
            player.sendMessage("You need a woodcutting level of " + definitions.getLevel() + " to chop down this tree.");
            return false;
        }
        return true;
    }

    @Override
    public boolean process(Player player) {
        if (player.isNative950()) {
            if (depleteElderTreeIfNeeded(player) || !checkAll(player)) return false;
            // The verified Lumberjack sequence lasts185 client cycles. Let it finish before renewal.
            if (--nativeAnimationDelay <= 0) { player.setNextAnimation(new Animation(emoteId)); nativeAnimationDelay=7; }
            return true;
        }
        if (player.getAnimations().hasRoundHouseWc && player.getAnimations().roundHouseWc) {
            player.setNextAnimation(new Animation(17304));
            player.setNextGraphics(new Graphics(3301));
        } else if (player.getAnimations().hasStrongWc && player.getAnimations().strongWc) {
            player.setNextAnimation(new Animation(20302));
            player.setNextGraphics(new Graphics(4006));
        } else if (player.getAnimations().hasExplosiveWc && player.getAnimations().explosiveWc) {
            player.setNextAnimation(new Animation(17948));
            player.setNextGraphics(new Graphics(3457));
        } else if (player.getAnimations().hasSingerWc && player.getAnimations().singerWc) {
            player.setNextAnimation(new Animation(24621));
            player.setNextGraphics(new Graphics(5157));
        } else
            player.setNextAnimation(new Animation(usingBeaver ? 1 : emoteId));
        if (depleteElderTreeIfNeeded(player))
            return false;
        return checkTree(player) && checkAll(player);
    }

    @Override
    public int processWithDelay(Player player) {
        if (player.isNative950()) return processNativeWithDelay(player);
        player.setNextAnimation(new Animation(emoteId));
        int skillChompa = TrapAction.getSkillChompa(player);
        if (depleteElderTreeIfNeeded(player))
            return -1;
        if (!rollWoodcuttingSuccess(player))
            return getWoodcuttingDelay(player);
        addLog(player);
        if (player.getSkillingTask() == 8) {
            if (player.getDailyManager().getTask() != null) {
                if (definitions.getLogsId() == player.getTaskItemId()) {
                    player.getDailyManager().processTask();
                }
            }
        }
        if (skillChompa != -1)
            World.sendGraphics(null, new Graphics(3037), new WorldTile(tree));
        if (spot != null) {
            player.addProduceGathered();
            if (!usedDeplateAurora && (1 + Math.random()) < player.getAuraManager().getChanceNotDepleteMN_WC())
                usedDeplateAurora = true;
            else if (rollTreeFell()) {
                int time = definitions.getRespawnDelay();
                spot.setEmpty(true);
                spot.refresh();
                spot.setCycleTime(true, time * 1000); // time in seconds
                player.setNextAnimation(new Animation(-1));
                player.sendMessage("You chop down the tree in the patch; produce harvested: " + Colors.RED + Utils.getFormattedNumber(player.produceGathered) + "</col>.", true);
                if (definitions == TreeDefinitions.MAGIC)
                    player.getAchievements().updateProgress(1, AchievementList.HARVEST_MAGIC_TREE);
                return -1;
            }
        } else {
            if (!usedDeplateAurora && (1 + Math.random()) < player.getAuraManager().getChanceNotDepleteMN_WC())
                usedDeplateAurora = true;
            else if (rollTreeFell() && definitions != TreeDefinitions.DONOR_TREE && definitions != TreeDefinitions.CRYSTAL_TREE_SHARD && definitions != TreeDefinitions.DREAM_TREE) {
                long time = definitions.getRespawnDelay() * 600;
                World.spawnObjectTemporary(new WorldObject(definitions.getStumpId(), tree.getType(), tree.getRotation(), tree.getX(), tree.getY(), tree.getPlane()), time);
                if (tree.getPlane() < 3 && definitions != TreeDefinitions.IVY) {
                    WorldObject object = World.getStandartObject(new WorldTile(tree.getX() - 1, tree.getY() - 1, tree.getPlane() + 1));

                    if (object == null) {
                        object = World.getStandartObject(new WorldTile(tree.getX(), tree.getY() - 1, tree.getPlane() + 1));
                        if (object == null) {
                            object = World.getStandartObject(new WorldTile(tree.getX() - 1, tree.getY(), tree.getPlane() + 1));
                            if (object == null)
                                object = World.getStandartObject(new WorldTile(tree.getX(), tree.getY(), tree.getPlane() + 1));
                        }
                    }

                    if (object != null)
                        World.removeObjectTemporary(object, time, true);
                }
                player.setNextAnimation(new Animation(-1));
                return -1;
            }
        }
        if (requiresFreeInventorySlot(player) && !player.getInventory().hasFreeSlots()) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return -1;
        }
        return getWoodcuttingDelay(player);
    }

    private boolean setAxe(Player player) {
        int level = player.getSkills().getLevel(8);
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            int requiredLevel = skillChompa == 40995 ? 71 : 31 + (10 * (skillChompa - 31595));
            if (level >= requiredLevel) {
                emoteId = 23793;
                hatchetPower = getSkillchompaPower(skillChompa);
                return true;
            }
            player.getPackets().sendObjectMessage(0, 15263739, tree, "You need a Woodcutting level of " + requiredLevel + " to use this skillchompa.");
            return false;
        }
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("dragon hatchet") || weapon.getName().toLowerCase().contains("crystal hatchet"));
        if (!hasAugmentedTool)
            weapon = null;
        if (weapon != null) {
            if (weapon.getName().toLowerCase().contains("dragon hatchet")) {
                emoteId = AxeDef.DRAGON.emote;
                if (player.getAnimations().hasLumberjackWc && player.getAnimations().lumberjackWc)
                    emoteId = AxeDef.DRAGON.specialEmote;
                hatchetPower = AxeDef.DRAGON.power;
            } else {
                emoteId = AxeDef.CRYSTAL.emote;
                if (player.getAnimations().hasLumberjackWc && player.getAnimations().lumberjackWc)
                    emoteId = AxeDef.CRYSTAL.specialEmote;
                hatchetPower = AxeDef.CRYSTAL.power;
            }
            return true;
        }
        AxeDef axe = AxeDef.get(player);
        if (axe != null) {
            emoteId = axe.emote;
            if (player.getAnimations().hasLumberjackWc && player.getAnimations().lumberjackWc)
                emoteId = axe.specialEmote;
            hatchetPower = axe.power;
            return true;
        }
        return false;
    }

    private int getSkillchompaPower(int skillChompa) {
        switch (skillChompa) {
            case 31595:
                return AxeDef.MITHRIL.power;
            case 31596:
                return AxeDef.ADAMANT.power;
            case 31597:
                return AxeDef.RUNE.power;
            case 31598:
                return AxeDef.DRAGON.power;
            case 40995:
                return AxeDef.CRYSTAL.power;
            default:
                return AxeDef.BRONZE.power;
        }
    }

    @Override
    public boolean start(Player player) {
        if (!checkAll(player))
            return false;
        startElderTreeCycle(player);
        if (player.isNative950()) {
            player.setNextFaceWorldTile(tree);
            player.setNextAnimation(new Animation(emoteId));
            nativeAnimationDelay=7;
        }
        player.sendMessage(usingBeaver ? "Your beaver uses its strong teeth to chop down the tree..." : "You swing your hatchet at the " + (TreeDefinitions.IVY == definitions ? "ivy" : "tree") + "...", true);
        setActionDelay(player, getWoodcuttingDelay(player));
        return true;
    }

    @Override
    public void stop(Player player) {
        if (player.isNative950()) player.setNextAnimation(new Animation(-1));
        setActionDelay(player, 3);
    }

    /** Same action lifecycle, ordinary yield and original rates; optional legacy content stays with its own ports. */
    private boolean checkNative(Player player) {
        if (spot!=null || Native950Woodcutting.definition(tree)!=definitions || !checkTree(player)
                || player.isDead() || player.hasFinished() || player.isLocked()
                || !Native950Woodcutting.inReach(player,tree)) return false;
        if (!hasWoodcuttingLevel(player) || isElderTreeReplenishing(player)) return false;
        AxeDef axe=Native950Woodcutting.bestAxe(player);
        if (axe==null) { player.sendMessage("You need a hatchet you have the Woodcutting level to use.");return false; }
        if (!Native950Woodcutting.validStump(definitions) || Native950Woodcutting.itemEntry(definitions.getLogsId())==null) {
            player.sendMessage("That tree is not available for woodcutting yet.");return false;
        }
        if (!Native950Skilling.hasSpace(player,definitions.getLogsId(),1)) {
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");return false;
        }
        emoteId=Native950Woodcutting.animation(axe);hatchetPower=axe.power;
        return emoteId>=0;
    }
    private int processNativeWithDelay(Player player) {
        // Recheck the world slot and inventory immediately before committing a reward.
        if (!checkNative(player) || depleteElderTreeIfNeeded(player)) return -1;
        if (!rollWoodcuttingSuccess(player)) return getWoodcuttingDelay(player);
        if (!Native950Skilling.giveItem(player,definitions.getLogsId(),1)) return -1;
        player.addLogsChopped();
        player.getSkills().addXp(Skills.WOODCUTTING,definitions.getXp());
        player.sendMessage("You get some "+Native950Woodcutting.itemEntry(definitions.getLogsId()).name.toLowerCase(java.util.Locale.ROOT)+".",true);
        if (rollTreeFell()) {
            long time=definitions.getRespawnDelay()*600L;
            if (definitions.getStumpId()<0) World.removeObjectTemporary(tree,time,true);
            else World.spawnObjectTemporary(new WorldObject(definitions.getStumpId(),tree.getType(),tree.getRotation(),tree),time);
            return -1;
        }
        return Native950Skilling.hasSpace(player,definitions.getLogsId(),1)?getWoodcuttingDelay(player):-1;
    }

    public static final HatchetDefinitions getHatchetDefinitions(Player player, boolean dungeoneering) {
        HatchetDefinitions defs = null;
        if (dungeoneering) {
            for (int i = 20; i >= 10; i--) {
                HatchetDefinitions definitions = HatchetDefinitions.ALL.get(i);
                if (player.getInventory().containsItem(definitions.getItemId(), 1) || player.getDungeoneeringToolbelt().containsTool(definitions.getItemId())) {
                    if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) >= definitions.getLevelRequired())
                        defs = definitions;
                }
            }
        } else {
            for (int i = 9; i >= 0; i--) {
                HatchetDefinitions d = HatchetDefinitions.ALL.get(i);
                if (player.getInventory().containsItem(d.getItemId(), 1)) {
                    if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) >= d.getLevelRequired())
                        defs = d;
                }
            }
        }
        return defs;
    }

}
