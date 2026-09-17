package com.rs.game.player.actions.mining;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.Native950ItemCatalog;
import com.rs.game.player.client.Native950Mining;
import com.rs.game.player.client.Native950Skilling;
import com.rs.game.player.actions.hunter.TrapAction;
import com.rs.game.player.actions.mining.defs.RockDefinitions;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.items.OreBox;
import com.rs.game.player.content.skillingcontracts.impl.MiningContractList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class Mining extends MiningBase {

    private static final int SWING_DELAY = 4;
    private static final int SEDIMENTARY_GEODE = 44816;
    private static final int IGNEOUS_GEODE = 44817;
    private static final int METAMORPHIC_GEODE = 44818;
    private static final String MINING_PROGRESS_KEY = "mining_rock_progress";

    private static final class MiningProgress {
        private final int objectId;
        private final int objectType;
        private final int objectRotation;
        private final int x;
        private final int y;
        private final int plane;
        private final RockDefinitions definitions;
        private final int oreProgress;
        private final int oresLeft;

        private MiningProgress(WorldObject rock, RockDefinitions definitions, int oreProgress, int oresLeft) {
            this.objectId = rock.getId();
            this.objectType = rock.getType();
            this.objectRotation = rock.getRotation();
            this.x = rock.getX();
            this.y = rock.getY();
            this.plane = rock.getPlane();
            this.definitions = definitions;
            this.oreProgress = oreProgress;
            this.oresLeft = oresLeft;
        }

        private boolean matches(WorldObject rock, RockDefinitions definitions) {
            return objectId == rock.getId()
                    && objectType == rock.getType()
                    && objectRotation == rock.getRotation()
                    && x == rock.getX()
                    && y == rock.getY()
                    && plane == rock.getPlane()
                    && this.definitions == definitions;
        }
    }

    private final WorldObject rock;
    private final RockDefinitions definitions;

    private int stamina = 100;
    private int oreProgress;
    private boolean usedDeplateAurora;
    private int nativeAnimationDelay;
    private int nativeStaminaCapacity;

    public Mining(WorldObject rock, RockDefinitions definitions) {
        this.rock = rock;
        this.definitions = definitions;
    }

    private boolean checkAll(Player player) {
        if (player.isNative950()) return checkNative(player);
        player.closeInterfaces();
        if (!hasUsablePickaxe(player)) {
            player.getPackets().sendObjectMessage(1, 15263739, rock,
                    "You need a pickaxe to mine the " + rock.getDefinitions().name + ".");
            return false;
        }
        if (!setMiningTool(player)) {
            player.getPackets().sendPlayerMessage(1, 15263739, player,
                    "You don't have the required level to use this pickaxe.", true);
            return false;
        }
        if (!hasMiningLevel(player))
            return false;
        if (!hasRoomForMinedResource(player)) {
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            player.getPackets().sendPlayerMessage(1, 15263739, player,
                    "Inventory full. To make more room, sell, drop or bank something.", true);
            return false;
        }
        return true;
    }

    private boolean hasRoomForMinedResource(Player player) {
        if (player.getInventory().hasFreeSlots())
            return true;
        if (definitions == RockDefinitions.GEM_ROCK || definitions == RockDefinitions.Donor_Ore)
            return false;
        int oreId = definitions.getOreId();
        return oreId != -1 && OreBox.canStore(player, oreId, 1);
    }

    private boolean checkRock() {
        return World.containsObjectWithId(rock, rock.getId());
    }

    private boolean restoreOreProgress(Player player) {
        Object saved = player.getTemporaryAttributtes().get(MINING_PROGRESS_KEY);
        if (saved instanceof MiningProgress) {
            MiningProgress progress = (MiningProgress) saved;
            if (progress.matches(rock, definitions)) {
                oreProgress = Math.max(0, Math.min(progress.oreProgress, getRockDurability() - 1));
                player.oresLeft = Math.max(0, progress.oresLeft);
                return true;
            }
        }
        clearOreProgress(player);
        return false;
    }

    private void saveOreProgress(Player player) {
        if (oreProgress <= 0 && (!canDeplete() || player.oresLeft <= 0)) {
            clearOreProgress(player);
            return;
        }
        player.getTemporaryAttributtes().put(MINING_PROGRESS_KEY,
                new MiningProgress(rock, definitions, oreProgress, player.oresLeft));
    }

    private void clearOreProgress(Player player) {
        player.getTemporaryAttributtes().remove(MINING_PROGRESS_KEY);
    }

    private int getMiningDelay(Player player) {
        return TrapAction.getSkillChompa(player) != -1 ? 3 : SWING_DELAY;
    }

    private boolean hasMiningLevel(Player player) {
        if (definitions.getLevel() > player.getSkills().getLevel(Skills.MINING)) {
            player.sendMessage("You need a Mining level of " + definitions.getLevel() + " to mine this rock.");
            return false;
        }
        return true;
    }

    private double masterMinerperk(Player player) {
        return player.getPerkManager().hasPerkActive(DonationPerk.MASTER_MINER) ? 1.25 : 1.0;
    }

    private double miningSuit(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 20789)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 20791)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 20790)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 20788)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 20789 && player.getEquipment().getChestId() == 20791
                && player.getEquipment().getLegsId() == 20790 && player.getEquipment().getBootsId() == 20788)
            xpBoost *= 1.01;
        if (hasGolemOutfit(player))
            xpBoost *= 1.07;
        return xpBoost;
    }

    private boolean hasGolemOutfit(Player player) {
        if (player.getEquipment().getHatId() == 31575 && player.getEquipment().getChestId() == 31576
                && player.getEquipment().getLegsId() == 31577 && player.getEquipment().getGlovesId() == 31578
                && player.getEquipment().getBootsId() == 31579)
            return true;
        if (player.getEquipment().getHatId() == 31580 && player.getEquipment().getChestId() == 31581
                && player.getEquipment().getLegsId() == 31582 && player.getEquipment().getGlovesId() == 31583
                && player.getEquipment().getBootsId() == 31584)
            return true;
        if (player.getEquipment().getHatId() == 31585 && player.getEquipment().getChestId() == 31586
                && player.getEquipment().getLegsId() == 31587 && player.getEquipment().getGlovesId() == 31588
                && player.getEquipment().getBootsId() == 31589)
            return true;
        return player.getEquipment().getHatId() == 31590 && player.getEquipment().getChestId() == 31591
                && player.getEquipment().getLegsId() == 31592 && player.getEquipment().getGlovesId() == 31593
                && player.getEquipment().getBootsId() == 31594;
    }

    private boolean hasUsablePickaxe(Player player) {
        if (TrapAction.getSkillChompa(player) != -1)
            return true;
        if (getAugmentedMiningTool(player) != null)
            return true;
        return getBestPickaxeId(player) != -1;
    }

    private boolean setMiningTool(Player player) {
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            int requiredLevel = skillChompa == 40995 ? 71 : 31 + (10 * (skillChompa - 31595));
            if (player.getSkills().getLevel(Skills.MINING) >= requiredLevel) {
                emoteId = 23793;
                pickaxeTime = requiredLevel;
                return true;
            }
            player.sendMessage("You need a Mining level of " + requiredLevel + " to use this skillchompa.");
            return false;
        }

        Item augmentedTool = getAugmentedMiningTool(player);
        if (augmentedTool != null) {
            String name = augmentedTool.getName().toLowerCase();
            if (name.contains("earth and song")) {
                emoteId = 32618;
                pickaxeTime = 90;
            } else if (name.contains("crystal")) {
                emoteId = 28305;
                pickaxeTime = 70;
            } else {
                emoteId = 28304;
                pickaxeTime = 60;
            }
            return true;
        }

        int pickaxeId = getBestPickaxeId(player);
        if (pickaxeId == -1)
            return false;
        emoteId = getPickaxeAnimation(pickaxeId);
        pickaxeTime = getPickaxeTier(pickaxeId);
        return true;
    }

    private Item getAugmentedMiningTool(Player player) {
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        if (weapon == null || weapon.getInventionData() == null)
            return null;
        String name = weapon.getName().toLowerCase();
        if (name.contains("pickaxe") || name.contains("earth and song"))
            return weapon;
        return null;
    }

    private Item getInventionMiningTool(Player player) {
        Item weapon = getAugmentedMiningTool(player);
        return weapon != null ? weapon : null;
    }

    private int getBestPickaxeId(Player player) {
        int[] ids = {
                44834, 45642, 45154, 29522, 32646, 46372, 20786, 15259, 13661, 29662, 29654, 20785, 1275,
                20783, 1271, 20784, 1273, 20782, 1269, 20781, 1267, 20780, 1265
        };
        int level = player.getSkills().getLevel(Skills.MINING);
        for (int id : ids) {
            if (level < getPickaxeRequiredLevel(id))
                continue;
            if (player.getEquipment().getWeaponId() == id
                    || player.getInventory().containsItem(id, 1)
                    || player.getToolBelt().contains(id))
                return id;
        }
        return -1;
    }

    private int getPickaxeRequiredLevel(int itemId) {
        switch (itemId) {
            case 44834:
            case 45642:
                return 90;
            case 45154:
            case 29522:
                return 80;
            case 32646:
            case 46372:
                return 70;
            case 20786:
            case 15259:
                return 60;
            case 13661:
            case 29662:
            case 29654:
            case 20785:
            case 1275:
                return 50;
            case 20783:
            case 1271:
                return 40;
            case 20784:
            case 1273:
                return 30;
            case 20782:
            case 1269:
                return 20;
            case 20781:
            case 1267:
                return 10;
            default:
                return 1;
        }
    }

    private int getPickaxeTier(int itemId) {
        switch (itemId) {
            case 44834:
            case 45642:
                return 90;
            case 45154:
            case 29522:
                return 80;
            case 32646:
            case 46372:
                return 70;
            case 20786:
            case 15259:
                return 60;
            case 13661:
            case 29662:
            case 29654:
            case 20785:
            case 1275:
                return 50;
            case 20783:
            case 1271:
                return 40;
            case 20784:
            case 1273:
                return 30;
            case 20782:
            case 1269:
                return 20;
            case 20781:
            case 1267:
                return 10;
            default:
                return 1;
        }
    }

    private int getPickaxeAnimation(int itemId) {
        switch (itemId) {
            case 44834:
                return 32618;
            case 45642:
                return 32611;
            case 45154:
                return 32606;
            case 46372:
                return 32603;
            case 29522:
            case 32646:
                return 25062;
            case 20786:
            case 15259:
                return 12190;
            case 13661:
                return 10222;
            case 29662:
            case 29654:
            case 20785:
            case 1275:
                return 32566;
            case 20783:
            case 1271:
                return 32562;
            case 20784:
            case 1273:
                return 32558;
            case 20782:
            case 1269:
                return 32552;
            case 20781:
            case 1267:
                return 32548;
            default:
                return 32540;
        }
    }

    private int rollMiningDamage(Player player) {
        int miningLevel = player.getSkills().getLevel(Skills.MINING);
        int strengthLevel = player.getSkills().getLevel(Skills.STRENGTH);
        int tier = Math.max(1, pickaxeTime);
        int minDamage = Math.max(2, tier / 2);
        int maxDamage = Math.max(minDamage + 1, tier + (tier / 2));
        int damage = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);

        damage += Math.max(0, miningLevel - definitions.getLevel()) / 4;
        damage += Math.max(0, strengthLevel) / 20;

        int hardness = getRockHardness();
        if (hardness > 0 && tier < hardness) {
            damage = Math.max(1, (int) Math.round(damage * Math.max(0.10, tier / (double) hardness)));
        }

        if (!player.isNative950()) {
            if (stamina == 100) damage = (int) Math.round(damage * 1.25);
            else if (stamina > 0) damage = (int) Math.round(damage * 1.125);
        }

        damage = (int) Math.round(damage * player.getAuraManager().getMiningAccurayMultiplier());
        if (player.getPerkManager().hasPerkActive(DonationPerk.MASTER_MINER))
            damage = (int) Math.round(damage * 1.10);
        if (hasGolemOutfit(player))
            damage = (int) Math.round(damage * 1.07);

        if (ThreadLocalRandom.current().nextDouble() < getCriticalChance(player)) {
            damage += getCriticalSwingBonus(miningLevel);
            if (!player.isNative950())
                player.applyHit(new Hit(player, 0, Hit.HitLook.MINING_CRITCAL_SWING));
            player.sendMessage("<col=f2490c>Critical swing!");
        }
        if (player.isNative950())
            damage = (int) Math.floor(damage * Native950Mining.staminaMultiplier(miningLevel, stamina, nativeStaminaCapacity));
        // 2019 RS3 caps a single swing at 100% of the rock's durability; any
        // carried progress comes from the previous partial ore, not one huge hit.
        return Math.max(1, Math.min(damage, getRockDurability()));
    }

    private double getCriticalChance(Player player) {
        if (!isCoreRock())
            return 0.10;
        int level = player.getSkills().getLevel(Skills.MINING);
        if (level >= definitions.getLevel() + 8)
            return 0.20;
        if (level >= definitions.getLevel() + 3)
            return 0.15;
        return 0.10;
    }

    private int getCriticalSwingBonus(int level) {
        if (level >= 105) return 105;
        if (level >= 99) return 100;
        if (level >= 87) return 95;
        if (level >= 84) return 90;
        if (level >= 79) return 85;
        if (level >= 74) return 80;
        if (level >= 65) return 75;
        if (level >= 61) return 70;
        if (level >= 59) return 65;
        if (level >= 52) return 60;
        if (level >= 44) return 55;
        if (level >= 43) return 50;
        if (level >= 34) return 45;
        if (level >= 32) return 40;
        if (level >= 24) return 35;
        if (level >= 22) return 30;
        if (level >= 17) return 25;
        if (level >= 11) return 20;
        if (level >= 9) return 15;
        if (level >= 3) return 10;
        return 5;
    }

    private int getRockDurability() {
        switch (definitions) {
            case Copper_Ore:
            case Tin_Ore:
            case CLAY:
            case SOFT_CLAY:
                return 40;
            case Iron_Ore:
                return 120;
            case Coal_Ore:
                return 140;
            case Mithril_Ore:
                return 240;
            case Adamant_Ore:
            case LUMINITE_ORE:
                return 380;
            case Runite_Ore:
                return 600;
            case ORICHALCITE_ORE:
            case DRAKOLITH_ORE:
                return 1000;
            case NECRITE_ORE:
            case PHASMATITE_ORE:
                return 1300;
            case BANE_ORE:
                return 1700;
            case LIGHT_ORE:
            case DARK_ORE:
                return 2000;
            case LRC_Coal_Ore:
                return 500;
            case LRC_Gold_Ore:
                return 750;
            case GEM_ROCK:
                return 80;
            case SEREN_STONE:
                return 1500;
            default:
                return Math.max(80, definitions.getOreBaseTime() * 8);
        }
    }

    private int getRockHardness() {
        switch (definitions) {
            case Iron_Ore:
                return 5;
            case Coal_Ore:
                return 15;
            case Mithril_Ore:
                return 30;
            case Adamant_Ore:
            case LUMINITE_ORE:
                return 50;
            case Runite_Ore:
                return 75;
            case ORICHALCITE_ORE:
            case DRAKOLITH_ORE:
                return 105;
            case NECRITE_ORE:
            case PHASMATITE_ORE:
                return 140;
            case BANE_ORE:
                return 180;
            case LIGHT_ORE:
            case DARK_ORE:
                return 225;
            default:
                return 0;
        }
    }

    private double getRockXpMultiplier() {
        switch (definitions) {
            case Copper_Ore:
            case Tin_Ore:
                return 0.66;
            case Iron_Ore:
                return 0.68;
            case Coal_Ore:
                return 0.70;
            case Mithril_Ore:
                return 0.72;
            case Adamant_Ore:
            case LUMINITE_ORE:
                return 0.74;
            case Runite_Ore:
                return 0.76;
            case ORICHALCITE_ORE:
            case DRAKOLITH_ORE:
                return 0.78;
            case NECRITE_ORE:
            case PHASMATITE_ORE:
                return 0.80;
            case BANE_ORE:
                return 0.82;
            case LIGHT_ORE:
            case DARK_ORE:
                return 0.84;
            default:
                return Math.max(0.25, definitions.getXp() / Math.max(100.0, getRockDurability()));
        }
    }

    private boolean isCoreRock() {
        switch (definitions) {
            case Copper_Ore:
            case Tin_Ore:
            case Iron_Ore:
            case Coal_Ore:
            case Mithril_Ore:
            case Adamant_Ore:
            case LUMINITE_ORE:
            case Runite_Ore:
            case ORICHALCITE_ORE:
            case DRAKOLITH_ORE:
            case NECRITE_ORE:
            case PHASMATITE_ORE:
            case BANE_ORE:
            case LIGHT_ORE:
            case DARK_ORE:
                return true;
            default:
                return false;
        }
    }

    private boolean canDeplete() {
        return !isCoreRock()
                && definitions != RockDefinitions.Donor_Ore
                && definitions != RockDefinitions.SEREN_STONE
                && definitions.getEmptyId() != -1;
    }

    private int getOreBaseQuantity() {
        if (definitions == RockDefinitions.LRC_Coal_Ore || definitions == RockDefinitions.LRC_Gold_Ore)
            return 5;
        return 1;
    }

    private int getDoubleOreChance(Player player) {
        if (!isCoreRock())
            return 0;
        int overLevel = player.getSkills().getLevel(Skills.MINING) - definitions.getLevel();
        if ((definitions == RockDefinitions.BANE_ORE && overLevel >= 6)
                || ((definitions == RockDefinitions.LIGHT_ORE || definitions == RockDefinitions.DARK_ORE) && overLevel >= 8))
            return 10;
        if (overLevel >= 2)
            return 5;
        return 0;
    }

    private int getStoneSpiritId() {
        switch (definitions) {
            case Copper_Ore:
                return 44799;
            case Tin_Ore:
                return 44800;
            case Iron_Ore:
                return 44801;
            case Coal_Ore:
            case LRC_Coal_Ore:
                return 44804;
            case Mithril_Ore:
                return 44805;
            case LUMINITE_ORE:
                return 44806;
            case Adamant_Ore:
                return 44807;
            case Runite_Ore:
                return 44808;
            case ORICHALCITE_ORE:
                return 44809;
            case DRAKOLITH_ORE:
                return 44810;
            case NECRITE_ORE:
                return 44811;
            case PHASMATITE_ORE:
                return 44812;
            case BANE_ORE:
                return 44813;
            case LIGHT_ORE:
                return 44814;
            case DARK_ORE:
                return 44815;
            case Silver_Ore:
                return 44802;
            case Gold_Ore:
            case LRC_Gold_Ore:
                return 44803;
            default:
                return -1;
        }
    }

    private void rollGeode(Player player) {
        if (!isCoreRock())
            return;
        int chance = 5;
        if (player.getSkills().getLevel(Skills.MINING) >= definitions.getLevel() + 8)
            chance = 15;
        else if (player.getSkills().getLevel(Skills.MINING) >= definitions.getLevel() + 3)
            chance = 10;
        if (ThreadLocalRandom.current().nextInt(100) >= chance)
            return;
        int geode = getGeodeId();
        if (ThreadLocalRandom.current().nextInt(100) == 0)
            geode = METAMORPHIC_GEODE;
        player.getInventory().addItemDrop(geode, 1);
        player.sendMessage("<col=f2490c>You find " + Utils.getAorAn(ItemDefinitions.getItemDefinitions(geode).getName().toLowerCase()) + ".");
    }

    private int getGeodeId() {
        switch (definitions) {
            case ORICHALCITE_ORE:
            case DRAKOLITH_ORE:
            case NECRITE_ORE:
            case PHASMATITE_ORE:
            case BANE_ORE:
            case LIGHT_ORE:
            case DARK_ORE:
                return IGNEOUS_GEODE;
            default:
                return SEDIMENTARY_GEODE;
        }
    }

    private void addOre(Player player, double xpForProc) {
        if (definitions == RockDefinitions.Donor_Ore) {
            addDonorOre(player, xpForProc);
            return;
        }
        if (definitions == RockDefinitions.GEM_ROCK) {
            addGem(player, xpForProc);
            return;
        }
        if (definitions.getOreId() == -1)
            return;

        int oreId = getOreId();
        int amount = getOreBaseQuantity();
        int doubleOreChance = getDoubleOreChance(player);
        if (doubleOreChance > 0 && ThreadLocalRandom.current().nextInt(100) < doubleOreChance)
            amount++;

        int stoneSpirit = getStoneSpiritId();
        if (stoneSpirit != -1 && player.getInventory().containsItem(stoneSpirit, 1)) {
            player.getInventory().deleteItem(stoneSpirit, 1);
            amount += getOreBaseQuantity();
            player.sendMessage("<col=f2490c>Your stone spirit grants you extra ore.");
        }

        Item ore = new Item(oreId, amount);
        Item weapon = getInventionMiningTool(player);
        if (player.getInventionManager().procGathering(weapon, ore, Skills.MINING, xpForProc)) {
            int remaining = OreBox.store(player, oreId, amount);
            if (remaining > 0)
                player.getInventory().addItemDrop(new Item(oreId, remaining));
        }
        for (int i = 0; i < amount; i++)
            player.addOresMined();
        MiningContractList.listen(player, definitions, amount);
        player.sendMessage("You mine " + (amount > 1 ? amount + " " : "some ")
                + ore.getName().toLowerCase() + "; ores mined: " + Colors.PBLUE
                + Utils.getFormattedNumber(player.getOresMined()) + "</col>.", true);
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
        PetPerkHandler.handleExtraArms(player, new Item(oreId, 1));
    }

    private int getOreId() {
        int oreId = definitions.getOreId();
        if (definitions == RockDefinitions.Granite_Ore)
            return oreId + (Utils.getRandom(2) * 2);
        if (definitions == RockDefinitions.Sandstone_Ore)
            return oreId + (Utils.getRandom(3) * 2);
        return oreId;
    }

    private void addDonorOre(Player player, double xpForProc) {
        int[] ores = { 438, 436, 440, 453, 444, 447, 449, 451, 44830 };
        int oreId = ores[ThreadLocalRandom.current().nextInt(ores.length)];
        Item ore = new Item(oreId, 1);
        Item weapon = getInventionMiningTool(player);
        if (player.getInventionManager().procGathering(weapon, ore, Skills.MINING, xpForProc)) {
            int remaining = OreBox.store(player, oreId, 1);
            if (remaining > 0)
                player.getInventory().addItemDrop(ore);
        }
        player.addOresMined();
        RockDefinitions def = RockDefinitions.get(oreId);
        if (def != null)
            MiningContractList.listen(player, def, 1);
        player.sendMessage("You mine some " + ore.getName().toLowerCase() + "; ores mined: "
                + Colors.RED + Utils.getFormattedNumber(player.getOresMined()) + "</col>.", true);
        PetPerkHandler.handleExtraArms(player, ore);
    }

    private void addGem(Player player, double xpForProc) {
        int gemId;
        if ((rock.getId() == 112998 || rock.getId() == 112999) && ThreadLocalRandom.current().nextInt(2500) == 0) {
            gemId = 6571;
        } else {
            int[] gems = { 1625, 1627, 1629, 1623, 1621, 1619, 1617, 1631 };
            gemId = gems[ThreadLocalRandom.current().nextInt(gems.length)];
        }
        Item gem = new Item(gemId, 1);
        Item weapon = getInventionMiningTool(player);
        if (player.getInventionManager().procGathering(weapon, gem, Skills.MINING, xpForProc))
            player.getInventory().addItem(gem);
        player.addOresMined();
        player.sendMessage("You mine " + Utils.getAorAn(gem.getName().toLowerCase()) + "; ores mined: "
                + Colors.RED + Utils.getFormattedNumber(player.getOresMined()) + "</col>.", true);
    }

    private boolean handleDepletion(Player player) {
        if (!canDeplete())
            return false;
        if (!usedDeplateAurora && (1 + Math.random()) < player.getAuraManager().getChanceNotDepleteMN_WC()) {
            usedDeplateAurora = true;
            return false;
        }
        if (--player.oresLeft > 0)
            return false;
        player.oresLeft = 0;
        World.spawnTemporaryObjectWithReplacement(
                new WorldObject(definitions.getEmptyId(), rock.getType(), rock.getRotation(), rock.getX(), rock.getY(), rock.getPlane()),
                rock, definitions.getRespawnDelay());
        player.setNextAnimation(new Animation(-1));
        return true;
    }

    private void processSwing(Player player) {
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1)
            player.getEquipment().removeAmmo(skillChompa, -1);
        int damage = rollMiningDamage(player);
        oreProgress += damage;
        stamina = Math.max(0, stamina - 10);
        World.sendGraphics(null, new Graphics(stamina == 100 ? 7169 : 7168), rock);

        double xp = damage * getRockXpMultiplier() * 0.4 * miningSuit(player) * masterMinerperk(player);
        Item weapon = getInventionMiningTool(player);
        player.getInventionManager().processSkillXp(Skills.MINING, xp, weapon);
        player.getSkills().addXp(Skills.MINING, xp);
        rollGeode(player);

        int percent = Math.min(100, (int) Math.round((oreProgress * 100.0) / getRockDurability()));
        player.getPackets().sendPlayerMessage(1, 15263739,
                "<col=f2490c>Stamina: " + stamina + "%<br><col=0867af>Ore: " + percent + "%", false);
    }

    @Override
    public boolean process(Player player) {
        if (player.isNative950()) {
            if (!checkNative(player)) return false;
            if (--nativeAnimationDelay <= 0) {
                player.setNextAnimation(new Animation(emoteId));
                nativeAnimationDelay = Native950Mining.animationDelay(emoteId);
            }
            return true;
        }
        setAnimationAndGFX(player);
        player.faceObject(rock);
        return checkRock() && checkAll(player);
    }

    @Override
    public int processWithDelay(Player player) {
        if (player.isNative950()) return processNativeWithDelay(player);
        if (!hasRoomForMinedResource(player)) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return -1;
        }

        processSwing(player);
        int durability = getRockDurability();
        while (oreProgress >= durability) {
            oreProgress -= durability;
            addOre(player, Math.max(1.0, durability * getRockXpMultiplier() * 0.4));
            int skillChompa = TrapAction.getSkillChompa(player);
            if (skillChompa != -1)
                World.sendGraphics(null, new Graphics(3037), new WorldTile(rock));
            if (handleDepletion(player))
                return -1;
            if (!hasRoomForMinedResource(player)) {
                player.setNextAnimation(new Animation(-1));
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                saveOreProgress(player);
                return -1;
            }
        }
        saveOreProgress(player);
        return getMiningDelay(player);
    }

    @Override
    public boolean start(Player player) {
        if (!checkAll(player))
            return false;
        stamina = 100;
        boolean restoredProgress = restoreOreProgress(player);
        if (player.isNative950()) {
            nativeStaminaCapacity = Native950Mining.staminaCapacity(player.getSkills().getLevel(Skills.MINING), player.getSkills().getLevel(Skills.AGILITY));
            stamina = nativeStaminaCapacity;
            player.setNextFaceWorldTile(rock);
            player.setNextAnimation(new Animation(emoteId));
            nativeAnimationDelay = Native950Mining.animationDelay(emoteId);
            Native950Mining.progress(player, Native950Mining.staminaPercent(stamina, nativeStaminaCapacity), oreProgress, getRockDurability());
            player.sendMessage("You swing your pickaxe at the rock.", true);
            if (!restoredProgress && canDeplete()) {
                int oreLife = definitions.getOreLife();
                player.oresLeft = oreLife > 0 ? (oreLife == 1 ? 1 : Utils.inclusive(1, oreLife)) : player.getInventory().getFreeSlots();
            }
            setActionDelay(player, getMiningDelay(player));
            return true;
        }
        int skillChompa = TrapAction.getSkillChompa(player);
        player.sendMessage(
                skillChompa != -1 ? "You throw a skillchompa at the rock." : "You swing your pickaxe at the rock.",
                true);
        if (!restoredProgress) {
            int oreLife = definitions.getOreLife();
            player.oresLeft = oreLife > 0 ? (oreLife == 1 ? 1 : Utils.inclusive(1, oreLife)) : player.getInventory().getFreeSlots();
        }
        setActionDelay(player, getMiningDelay(player));
        return true;
    }

    @Override
    public void stop(Player player) {
        if (player.isNative950()) {
            player.setNextAnimation(new Animation(-1));
            Native950Mining.clearProgress(player);
            if (Native950Mining.current(rock))
                saveOreProgress(player);
            else
                clearOreProgress(player);
            setActionDelay(player, 3);
            return;
        }
        if (checkRock())
            saveOreProgress(player);
        else
            clearOreProgress(player);
        super.stop(player);
    }

    /** Same action lifecycle, ordinary yield and RS3 damage formula for native 950 players. */
    private boolean checkNative(Player player) {
        if (Native950Mining.definition(rock) != definitions || !Native950Mining.current(rock)
                || player.isDead() || player.hasFinished() || player.isLocked()
                || !Native950Mining.inReach(player, rock)) return false;
        if (!hasMiningLevel(player)) return false;
        Native950Mining.PickaxeDef pickaxe = Native950Mining.bestPickaxe(player);
        if (pickaxe == null) {
            player.sendMessage("You need a pickaxe you have the Mining level to use.");
            return false;
        }
        if ((canDeplete() && !Native950Mining.validEmptyRock(definitions))
                || (definitions.getOreId() != -1 && Native950Mining.itemEntry(definitions.getOreId()) == null)) {
            player.sendMessage("That rock is not available for mining yet.");
            return false;
        }
        if (definitions.getOreId() != -1 && !Native950Skilling.hasSpace(player, definitions.getOreId(), 1)) {
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return false;
        }
        emoteId = Native950Mining.animation(pickaxe);
        pickaxeTime = pickaxe.tier;
        return emoteId >= 0;
    }

    private int rollGemId() {
        if ((rock.getId() == 112998 || rock.getId() == 112999) && ThreadLocalRandom.current().nextInt(2500) == 0) {
            return 6571;
        }
        int[] gems = { 1625, 1627, 1629, 1623, 1621, 1619, 1617, 1631 };
        return gems[ThreadLocalRandom.current().nextInt(gems.length)];
    }

    private int processNativeWithDelay(Player player) {
        if (!checkNative(player)) return -1;
        int capacity = Native950Mining.staminaCapacity(player.getSkills().getLevel(Skills.MINING), player.getSkills().getLevel(Skills.AGILITY));
        if (capacity != nativeStaminaCapacity) {
            // Keep stamina already spent while admitting a new milestone or a visible skill boost.
            stamina = Math.max(0, Math.min(capacity, stamina + capacity - nativeStaminaCapacity));
            nativeStaminaCapacity = capacity;
        }
        int damage = rollMiningDamage(player);
        oreProgress += damage;
        stamina = Math.max(0, stamina - 10);

        double xp = damage * getRockXpMultiplier() * 0.4 * miningSuit(player) * masterMinerperk(player);
        player.getSkills().addXp(Skills.MINING, xp);

        int percent = Math.min(100, (int) Math.round((oreProgress * 100.0) / getRockDurability()));
        Native950Mining.progress(player, Native950Mining.staminaPercent(stamina, nativeStaminaCapacity), oreProgress, getRockDurability());

        int durability = getRockDurability();
        while (oreProgress >= durability) {
            oreProgress -= durability;
            int oreId = definitions == RockDefinitions.GEM_ROCK ? rollGemId() : getOreId();
            int amount = getOreBaseQuantity();
            int doubleOreChance = getDoubleOreChance(player);
            if (doubleOreChance > 0 && ThreadLocalRandom.current().nextInt(100) < doubleOreChance)
                amount++;

            // A bonus ore must not discard the base yield when only one slot remains.
            if (!Native950Skilling.hasSpace(player, oreId, amount)) amount = 1;
            if (!Native950Skilling.giveItem(player, oreId, amount)) {
                oreProgress += durability;
                saveOreProgress(player);
                return -1;
            }
            player.addOresMined();
            Native950ItemCatalog.Entry entry = Native950Mining.itemEntry(oreId);
            String name = entry != null ? entry.name : "ore";
            player.sendMessage("You mine " + (amount > 1 ? amount + " " : "some ")
                    + name.toLowerCase(Locale.ROOT) + ".", true);

            if (canDeplete()) {
                if (--player.oresLeft <= 0) {
                    player.oresLeft = 0;
                    long time = definitions.getRespawnDelay() * 600L;
                    World.spawnObjectTemporary(new WorldObject(definitions.getEmptyId(), rock.getType(), rock.getRotation(), rock), time);
                    player.setNextAnimation(new Animation(-1));
                    return -1;
                }
            }
            if (!Native950Skilling.hasSpace(player, definitions.getOreId(), 1)) {
                player.setNextAnimation(new Animation(-1));
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                saveOreProgress(player);
                return -1;
            }
        }
        Native950Mining.progress(player, Native950Mining.staminaPercent(stamina, nativeStaminaCapacity), oreProgress, getRockDurability());
        saveOreProgress(player);
        return Native950Skilling.hasSpace(player, definitions.getOreId(), 1) ? getMiningDelay(player) : -1;
    }

    public static final PickaxeDefinitions getPickaxeDefinitions(Player player, boolean dungeoneering) {
        PickaxeDefinitions defs = null;
        if (dungeoneering) {
            for (PickaxeDefinitions definitions : PickaxeDefinitions.values()) {
                if (definitions == PickaxeDefinitions.BRONZE)
                    break;
                if (player.getInventory().containsItem(definitions.getPickAxeId(), 1)
                        || player.getDungeoneeringToolbelt().containsTool(definitions.getPickAxeId())
                        || ItemDefinitions.getItemDefinitions(definitions.getPickAxeId()).isBindItem()) {
                    if (player.getSkills().getLevelForXp(Skills.MINING) >= definitions.getLevelRequried())
                        defs = definitions;
                }
            }
        } else {
            for (int i = 11; i < PickaxeDefinitions.values().length; i++) {
                PickaxeDefinitions d = PickaxeDefinitions.values()[i];
                if (player.getInventory().containsItem(d.getPickAxeId(), 1)
                        || player.getToolBelt().contains(d.getPickAxeId())) {
                    if (player.getSkills().getLevelForXp(Skills.MINING) >= d.getLevelRequried())
                        defs = d;
                }
            }
        }
        return defs;
    }
}
