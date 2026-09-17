package com.rs.game.player.bots.lumbridge;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.mining.defs.RockDefinitions;
import com.rs.game.player.actions.smithing.defs.ForgingBar;
import com.rs.game.player.actions.smithing.defs.SmeltingBar;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.TreeDefinitions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class LumbridgeIronmanData {

    public static final WorldTile LUMBRIDGE_SPAWN = new WorldTile(3222, 3218, 0);
    public static final WorldTile LUMBRIDGE_COURTYARD = new WorldTile(3222, 3221, 0);
    public static final WorldTile CHICKEN_COOP = new WorldTile(3230, 3298, 0);
    public static final WorldTile COW_FIELD = new WorldTile(3255, 3267, 0);
    public static final WorldTile GOBLIN_FIELD = new WorldTile(3247, 3238, 0);
    public static final WorldTile LUMBRIDGE_SWAMP_MINE = new WorldTile(3229, 3150, 0);
    public static final WorldTile LUMBRIDGE_TREE_PATCH = new WorldTile(3226, 3248, 0);
    public static final WorldTile LUMBRIDGE_WATER = new WorldTile(3239, 3241, 0);
    public static final WorldTile SMITHING_SEARCH_TILE = new WorldTile(3227, 3255, 0);
    public static final WorldTile BANK_SEARCH_TILE = new WorldTile(3222, 3218, 0);

    public static final int BRONZE_PICKAXE = 1265;
    public static final int BRONZE_HATCHET = 1351;
    public static final int HAMMER = 2347;
    public static final int TINDERBOX = 590;
    public static final int SMALL_FISHING_NET = 303;
    public static final int FISHING_ROD = 307;
    public static final int FISHING_BAIT = 313;
    public static final int KNIFE = 946;

    public static final int COPPER_ORE = 436;
    public static final int TIN_ORE = 438;
    public static final int IRON_ORE = 440;
    public static final int COAL = 453;
    public static final int MITHRIL_ORE = 447;
    public static final int ADAMANT_ORE = 449;
    public static final int RUNITE_ORE = 451;

    public static final int BRONZE_BAR = 2349;
    public static final int IRON_BAR = 2351;
    public static final int STEEL_BAR = 2353;
    public static final int MITHRIL_BAR = 2359;
    public static final int ADAMANT_BAR = 2361;
    public static final int RUNE_BAR = 2363;

    public static final int RAW_SHRIMP = 317;
    public static final int SHRIMP = 315;
    public static final int RAW_ANCHOVIES = 321;
    public static final int ANCHOVIES = 319;
    public static final int RAW_TROUT = 335;
    public static final int TROUT = 333;
    public static final int RAW_SALMON = 331;
    public static final int SALMON = 329;
    public static final int RAW_CHICKEN = 2138;
    public static final int COOKED_CHICKEN = 2140;
    public static final int RAW_BEEF = 2132;
    public static final int COOKED_MEAT = 2142;
    public static final int BONES = 526;
    public static final int FEATHERS = 314;
    public static final int COWHIDE = 1739;
    public static final int COINS = 995;

    public static final int[] STARTER_TOOLS = {
            BRONZE_PICKAXE, BRONZE_HATCHET, HAMMER, TINDERBOX, SMALL_FISHING_NET, FISHING_ROD, KNIFE
    };

    public static final int[] RAW_FOODS = {
            RAW_SHRIMP, RAW_ANCHOVIES, RAW_TROUT, RAW_SALMON, RAW_CHICKEN, RAW_BEEF
    };

    public static final int[] COOKED_FOODS = {
            SHRIMP, ANCHOVIES, TROUT, SALMON, COOKED_CHICKEN, COOKED_MEAT
    };

    public static final int[] ALWAYS_LOOT = {
            COINS, BONES, FEATHERS, COWHIDE, RAW_CHICKEN, RAW_BEEF,
            BRONZE_PICKAXE, BRONZE_HATCHET, HAMMER, TINDERBOX, SMALL_FISHING_NET
    };

    private static final Set<Integer> KEEP_ITEMS = new HashSet<Integer>();

    static {
        addAll(KEEP_ITEMS, STARTER_TOOLS);
        addAll(KEEP_ITEMS, COOKED_FOODS);
        KEEP_ITEMS.add(FISHING_ROD);
        KEEP_ITEMS.add(FISHING_BAIT);
    }

    private LumbridgeIronmanData() {
    }

    public static void ensureStarterKit(Player player) {
        for (int itemId : STARTER_TOOLS) {
            if (!hasItemAnywhere(player, itemId)) {
                player.getInventory().addItem(itemId, 1);
            }
        }
        if (!hasItemAnywhere(player, FISHING_BAIT)) {
            player.getInventory().addItem(FISHING_BAIT, 100);
        }
    }

    public static boolean isKeepItem(int itemId) {
        return KEEP_ITEMS.contains(itemId) || isEquippedGear(itemId);
    }

    public static boolean isUsefulLoot(int itemId) {
        for (int useful : ALWAYS_LOOT) {
            if (useful == itemId) {
                return true;
            }
        }
        return isRawFood(itemId) || isCookedFood(itemId) || isOre(itemId) || isBar(itemId) || isEquippedGear(itemId);
    }

    public static boolean isRawFood(int itemId) {
        for (int id : RAW_FOODS) {
            if (id == itemId) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCookedFood(int itemId) {
        for (int id : COOKED_FOODS) {
            if (id == itemId) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOre(int itemId) {
        return itemId == COPPER_ORE || itemId == TIN_ORE || itemId == IRON_ORE || itemId == COAL
                || itemId == MITHRIL_ORE || itemId == ADAMANT_ORE || itemId == RUNITE_ORE;
    }

    public static boolean isBar(int itemId) {
        return itemId == BRONZE_BAR || itemId == IRON_BAR || itemId == STEEL_BAR || itemId == MITHRIL_BAR
                || itemId == ADAMANT_BAR || itemId == RUNE_BAR;
    }

    public static boolean isEquippedGear(int itemId) {
        for (GearTier tier : GearTier.values()) {
            if (tier.contains(itemId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasItemAnywhere(Player player, int itemId) {
        return player.getInventory().containsItem(itemId, 1)
                || player.getEquipment().getWeaponId() == itemId
                || player.getEquipment().getHatId() == itemId
                || player.getEquipment().getChestId() == itemId
                || player.getEquipment().getLegsId() == itemId
                || player.getEquipment().getShieldId() == itemId
                || player.getBank().containsItemCurrentBank(itemId, 1);
    }

    public static int countInventoryAndBank(Player player, int itemId) {
        int count = player.getInventory().getAmountOf(itemId);
        Item banked = player.getBank().getItem(itemId);
        if (banked != null) {
            count += banked.getAmount();
        }
        return count;
    }

    public static int countCookedFood(Player player) {
        int amount = 0;
        for (int itemId : COOKED_FOODS) {
            amount += player.getInventory().getAmountOf(itemId);
        }
        return amount;
    }

    public static int countRawFood(Player player) {
        int amount = 0;
        for (int itemId : RAW_FOODS) {
            amount += player.getInventory().getAmountOf(itemId);
        }
        return amount;
    }

    public static int firstRawFood(Player player) {
        for (int itemId : RAW_FOODS) {
            if (player.getInventory().containsItem(itemId, 1)) {
                return itemId;
            }
        }
        return -1;
    }

    public static GearTier currentMeleeTier(Player player) {
        GearTier best = GearTier.NONE;
        for (GearTier tier : GearTier.values()) {
            if (tier == GearTier.NONE) {
                continue;
            }
            if (tier.hasAnyEquipped(player) && tier.ordinal() > best.ordinal()) {
                best = tier;
            }
        }
        return best;
    }

    public static GearTier bestSmithableTier(Player player) {
        int smithing = player.getSkills().getLevel(Skills.SMITHING);
        GearTier best = GearTier.BRONZE;
        for (GearTier tier : GearTier.values()) {
            if (tier == GearTier.NONE) {
                continue;
            }
            if (tier.ordinal() > GearTier.IRON.ordinal()) {
                continue;
            }
            if (smithing >= tier.smithingLevel && tier.ordinal() > best.ordinal()) {
                best = tier;
            }
        }
        return best;
    }

    public static GearTier nextUsefulGearTier(Player player) {
        GearTier current = currentMeleeTier(player);
        GearTier smithable = bestSmithableTier(player);
        if (smithable.ordinal() <= current.ordinal()) {
            return current;
        }
        return smithable;
    }

    public static MineTarget bestMineTarget(Player player, GearTier desiredTier) {
        int mining = player.getSkills().getLevel(Skills.MINING);
        if (desiredTier == GearTier.BRONZE || mining < 15) {
            int copper = countInventoryAndBank(player, COPPER_ORE);
            int tin = countInventoryAndBank(player, TIN_ORE);
            return copper <= tin ? MineTarget.COPPER : MineTarget.TIN;
        }
        return MineTarget.IRON;
    }

    public static SmeltingBar bestSmeltableBar(Player player, GearTier desiredTier) {
        GearTier target = desiredTier == GearTier.NONE ? GearTier.BRONZE : desiredTier;
        for (GearTier tier = target; tier != GearTier.NONE; tier = tier.previous()) {
            if (tier.canSmelt(player)) {
                return tier.smeltingBar;
            }
        }
        return null;
    }

    public enum ActivityKind {
        BANK,
        COOK,
        FISH,
        MINE,
        SMELT,
        SMITH,
        WOODCUT,
        COMBAT,
        LOOT,
        WANDER
    }

    public enum GearPiece {
        WEAPON(Equipment.SLOT_WEAPON, 12, 1),
        HELM(Equipment.SLOT_HAT, 16, 1),
        BODY(Equipment.SLOT_CHEST, 28, 5),
        LEGS(Equipment.SLOT_LEGS, 27, 3),
        SHIELD(Equipment.SLOT_SHIELD, 23, 2),
        PICKAXE(Equipment.SLOT_WEAPON, 29, 1),
        HATCHET(Equipment.SLOT_WEAPON, 1, 1);

        public final int equipmentSlot;
        public final int smithingIndex;
        public final int bars;

        GearPiece(int equipmentSlot, int smithingIndex, int bars) {
            this.equipmentSlot = equipmentSlot;
            this.smithingIndex = smithingIndex;
            this.bars = bars;
        }
    }

    public enum GearTier {
        NONE(0, null, null, -1, -1, -1, -1, -1, -1, -1),
        BRONZE(1, SmeltingBar.BRONZE, ForgingBar.BRONZE, 1321, 1155, 1117, 1075, 1189, 1265, 1351),
        IRON(10, SmeltingBar.IRON, ForgingBar.IRON, 1323, 1153, 1115, 1067, 1191, 1267, 1349),
        STEEL(20, SmeltingBar.STEEL, ForgingBar.STEEL, 1325, 1157, 1119, 1069, 1193, 1269, 1353),
        MITHRIL(30, SmeltingBar.MITHRIL, ForgingBar.MITHRIL, 1329, 1159, 1121, 1071, 1197, 1273, 1355),
        ADAMANT(40, SmeltingBar.ADAMANT, ForgingBar.ADAMANT, 1331, 1161, 1123, 1073, 1199, 1271, 1357),
        RUNE(50, SmeltingBar.RUNE, ForgingBar.RUNE, 1333, 1163, 1127, 1079, 1201, 1275, 1359);

        public final int smithingLevel;
        public final SmeltingBar smeltingBar;
        public final ForgingBar forgingBar;
        public final int weapon;
        public final int helm;
        public final int body;
        public final int legs;
        public final int shield;
        public final int pickaxe;
        public final int hatchet;

        GearTier(int smithingLevel, SmeltingBar smeltingBar, ForgingBar forgingBar, int weapon, int helm,
                int body, int legs, int shield, int pickaxe, int hatchet) {
            this.smithingLevel = smithingLevel;
            this.smeltingBar = smeltingBar;
            this.forgingBar = forgingBar;
            this.weapon = weapon;
            this.helm = helm;
            this.body = body;
            this.legs = legs;
            this.shield = shield;
            this.pickaxe = pickaxe;
            this.hatchet = hatchet;
        }

        public GearTier previous() {
            int index = ordinal() - 1;
            return index < 0 ? NONE : values()[index];
        }

        public int itemFor(GearPiece piece) {
            switch (piece) {
                case WEAPON:
                    return weapon;
                case HELM:
                    return helm;
                case BODY:
                    return body;
                case LEGS:
                    return legs;
                case SHIELD:
                    return shield;
                case PICKAXE:
                    return pickaxe;
                case HATCHET:
                    return hatchet;
                default:
                    return -1;
            }
        }

        public boolean contains(int itemId) {
            return itemId == weapon || itemId == helm || itemId == body || itemId == legs || itemId == shield
                    || itemId == pickaxe || itemId == hatchet;
        }

        public boolean hasAnyEquipped(Player player) {
            return player.getEquipment().getWeaponId() == weapon
                    || player.getEquipment().getHatId() == helm
                    || player.getEquipment().getChestId() == body
                    || player.getEquipment().getLegsId() == legs
                    || player.getEquipment().getShieldId() == shield;
        }

        public boolean isMissingCoreGear(Player player) {
            return !hasItemAnywhere(player, weapon)
                    || !hasItemAnywhere(player, helm)
                    || !hasItemAnywhere(player, body)
                    || !hasItemAnywhere(player, legs)
                    || !hasItemAnywhere(player, shield);
        }

        public GearPiece firstMissingCorePiece(Player player) {
            GearPiece[] order = {
                    GearPiece.WEAPON, GearPiece.BODY, GearPiece.LEGS, GearPiece.HELM, GearPiece.SHIELD
            };
            for (GearPiece piece : order) {
                if (!hasItemAnywhere(player, itemFor(piece))) {
                    return piece;
                }
            }
            return null;
        }

        public boolean canSmelt(Player player) {
            if (smeltingBar == null) {
                return false;
            }
            if (player.getSkills().getLevel(Skills.SMITHING) < smeltingBar.getLevelRequired()) {
                return false;
            }
            for (Item item : smeltingBar.getItemsRequired()) {
                if (countInventoryAndBank(player, item.getId()) < item.getAmount()) {
                    return false;
                }
            }
            return true;
        }

        public boolean hasBarsFor(Player player, GearPiece piece) {
            return forgingBar != null && countInventoryAndBank(player, forgingBar.getBarId()) >= piece.bars;
        }
    }

    public enum MineTarget {
        COPPER(RockDefinitions.Copper_Ore, COPPER_ORE, LUMBRIDGE_SWAMP_MINE, "copper rock"),
        TIN(RockDefinitions.Tin_Ore, TIN_ORE, LUMBRIDGE_SWAMP_MINE, "tin rock"),
        IRON(RockDefinitions.Iron_Ore, IRON_ORE, LUMBRIDGE_SWAMP_MINE, "iron rock"),
        COAL(RockDefinitions.Coal_Ore, LumbridgeIronmanData.COAL, LUMBRIDGE_SWAMP_MINE, "coal rock"),
        MITHRIL(RockDefinitions.Mithril_Ore, MITHRIL_ORE, LUMBRIDGE_SWAMP_MINE, "mithril rock"),
        ADAMANT(RockDefinitions.Adamant_Ore, ADAMANT_ORE, LUMBRIDGE_SWAMP_MINE, "adamantite rock"),
        RUNE(RockDefinitions.Runite_Ore, RUNITE_ORE, LUMBRIDGE_SWAMP_MINE, "runite rock");

        public final RockDefinitions rock;
        public final int oreId;
        public final WorldTile searchTile;
        public final String objectName;

        MineTarget(RockDefinitions rock, int oreId, WorldTile searchTile, String objectName) {
            this.rock = rock;
            this.oreId = oreId;
            this.searchTile = searchTile;
            this.objectName = objectName;
        }
    }

    public enum TreeTarget {
        NORMAL(TreeDefinitions.NORMAL, LUMBRIDGE_TREE_PATCH, "tree"),
        OAK(TreeDefinitions.OAK, LUMBRIDGE_TREE_PATCH, "oak"),
        WILLOW(TreeDefinitions.WILLOW, LUMBRIDGE_TREE_PATCH, "willow");

        public final TreeDefinitions tree;
        public final WorldTile searchTile;
        public final String objectName;

        TreeTarget(TreeDefinitions tree, WorldTile searchTile, String objectName) {
            this.tree = tree;
            this.searchTile = searchTile;
            this.objectName = objectName;
        }

        public static TreeTarget bestFor(Player player) {
            return NORMAL;
        }
    }

    public enum CombatTarget {
        CHICKEN("chicken", CHICKEN_COOP, 1, 1),
        COW("cow", COW_FIELD, 8, 12),
        GOBLIN("goblin", GOBLIN_FIELD, 12, 20);

        public final String npcName;
        public final WorldTile searchTile;
        public final int minCombat;
        public final int foodWanted;

        CombatTarget(String npcName, WorldTile searchTile, int minCombat, int foodWanted) {
            this.npcName = npcName;
            this.searchTile = searchTile;
            this.minCombat = minCombat;
            this.foodWanted = foodWanted;
        }

        public static CombatTarget bestFor(Player player) {
            int combat = player.getSkills().getCombatLevel();
            int food = countCookedFood(player);
            if (combat >= GOBLIN.minCombat && food >= GOBLIN.foodWanted) {
                return GOBLIN;
            }
            if (combat >= COW.minCombat && food >= COW.foodWanted) {
                return COW;
            }
            return CHICKEN;
        }
    }

    private static void addAll(Set<Integer> target, int[] ids) {
        for (int id : ids) {
            target.add(id);
        }
    }

    public static String idsToString(int[] ids) {
        return Arrays.toString(ids);
    }
}
