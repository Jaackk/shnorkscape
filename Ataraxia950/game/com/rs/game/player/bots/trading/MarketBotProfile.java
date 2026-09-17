package com.rs.game.player.bots.trading;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.bots.IdleCrowdBotScript;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class MarketBotProfile {

    public enum Archetype {
        SUPPLY_TRADER,
        RESOURCE_BUYER,
        SKILLER,
        PKER,
        BOSSER,
        MERCHER,
        COLLECTOR,
        NEWCOMER
    }

    public enum PersonalityStyle {
        BALANCED,
        HARD_NEGOTIATOR,
        IMPATIENT_SELLER,
        DESPERATE_BUYER,
        BULK_BUYER,
        COLLECTOR,
        SCAM_SENSITIVE
    }

    public enum TraderSpecialty {
        POTION_FLIPPER,
        HERB_RUNNER,
        FISHMONGER,
        RUNE_MERCHANT,
        SMITHING_SUPPLIER,
        FLETCHING_SUPPLIER,
        CRAFTING_JEWELER,
        FARMING_SUPPLIER,
        SUMMONING_SUPPLIER,
        PVP_RESTOCKER,
        PVM_SUPPLY_RUNNER,
        CLUE_RARE_COLLECTOR,
        HIGH_TIER_GEAR_TRADER,
        SKILLING_BULK_BUYER,
        BOSS_LOOT_DUMPER,
        BROKE_NEWCOMER,
        IMPATIENT_BANK_CLEARER
    }

    public enum InventoryTheme {
        POTIONS_AND_HERBS(
                new MarketItemCategory[] {MarketItemCategory.HERBS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.POTIONS, MarketItemCategory.SUPPLIES},
                new MarketItemCategory[] {MarketItemCategory.HERBS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.POTIONS, MarketItemCategory.SUPPLIES}),
        PVP_RESTOCK(
                new MarketItemCategory[] {MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.POTIONS, MarketItemCategory.SUPPLIES,
                        MarketItemCategory.PVP_GEAR, MarketItemCategory.RANGED_AMMO},
                new MarketItemCategory[] {MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.POTIONS, MarketItemCategory.SUPPLIES,
                        MarketItemCategory.PVP_GEAR, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.MELEE_GEAR, MarketItemCategory.RANGED_GEAR,
                        MarketItemCategory.MAGE_GEAR}),
        SKILLING_MATERIALS(
                new MarketItemCategory[] {MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.BARS, MarketItemCategory.SKILLING_INPUTS,
                        MarketItemCategory.DIVINATION_ENERGY},
                new MarketItemCategory[] {MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.BARS, MarketItemCategory.SKILLING_INPUTS,
                        MarketItemCategory.DIVINATION_ENERGY}),
        FISH_AND_FOOD(
                new MarketItemCategory[] {MarketItemCategory.RAW_FISH, MarketItemCategory.FOOD},
                new MarketItemCategory[] {MarketItemCategory.RAW_FISH, MarketItemCategory.FOOD}),
        RUNES_AND_MAGIC(
                new MarketItemCategory[] {MarketItemCategory.RUNES, MarketItemCategory.TALISMANS,
                        MarketItemCategory.MAGE_GEAR},
                new MarketItemCategory[] {MarketItemCategory.RUNES, MarketItemCategory.TALISMANS,
                        MarketItemCategory.MAGE_GEAR}),
        CRAFTING_AND_JEWELRY(
                new MarketItemCategory[] {MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.GEMS,
                        MarketItemCategory.JEWELRY, MarketItemCategory.SHORTBOWS},
                new MarketItemCategory[] {MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.GEMS,
                        MarketItemCategory.JEWELRY, MarketItemCategory.SHORTBOWS}),
        FARMING(
                new MarketItemCategory[] {MarketItemCategory.FARMING_SEEDS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.HERBS},
                new MarketItemCategory[] {MarketItemCategory.FARMING_SEEDS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.HERBS}),
        SUMMONING(
                new MarketItemCategory[] {MarketItemCategory.SUMMONING_REAGENTS, MarketItemCategory.BONES},
                new MarketItemCategory[] {MarketItemCategory.SUMMONING_REAGENTS, MarketItemCategory.BONES}),
        BOSS_LOOT(
                new MarketItemCategory[] {MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                        MarketItemCategory.POTIONS, MarketItemCategory.RUNES, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.SUMMONING_REAGENTS},
                new MarketItemCategory[] {MarketItemCategory.BOSS_LOOT, MarketItemCategory.BONES,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY}),
        RARES_AND_CLUES(
                new MarketItemCategory[] {MarketItemCategory.RARES, MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY},
                new MarketItemCategory[] {MarketItemCategory.RARES, MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY}),
        NEWCOMER_CLEAROUT(
                new MarketItemCategory[] {MarketItemCategory.FOOD, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.RUNES, MarketItemCategory.CRAFTING_INPUTS},
                new MarketItemCategory[] {MarketItemCategory.FOOD, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.RUNES, MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.JEWELRY}),
        BANK_SALE(
                new MarketItemCategory[] {MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.POTIONS, MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.SUMMONING_REAGENTS},
                new MarketItemCategory[] {MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                        MarketItemCategory.POTIONS, MarketItemCategory.RUNES, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.BARS, MarketItemCategory.SKILLING_INPUTS,
                        MarketItemCategory.GEMS, MarketItemCategory.HERBS, MarketItemCategory.RAW_FISH,
                        MarketItemCategory.DIVINATION_ENERGY, MarketItemCategory.FARMING_SEEDS,
                        MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.TALISMANS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS, MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.GENERAL_MARKET});

        private final MarketItemCategory[] buyCategories;
        private final MarketItemCategory[] sellCategories;

        InventoryTheme(MarketItemCategory[] buyCategories, MarketItemCategory[] sellCategories) {
            this.buyCategories = buyCategories;
            this.sellCategories = sellCategories;
        }

        private MarketItemCategory[] buyCategories() {
            return buyCategories;
        }

        private MarketItemCategory[] sellCategories() {
            return sellCategories;
        }
    }

    private static final int COINS = 995;
    private static final int BPS = 10000;
    private static final int CHAT_BUY_TOLERANCE_BPS = 250;
    private static final int CHAT_COUNTER_WINDOW_MS = 30000;
    private static final int NEWCOMER_TRADE_LIMIT = 300_000;
    private static final int MIN_BUY_INTERESTS = 14;
    private static final int MAX_BUY_INTERESTS = 44;
    private static final int MIN_SELL_BUNDLE_ITEMS = 3;
    private static final int MAX_SELL_BUNDLE_ITEMS = 12;
    private static final int MIN_PRICE_MULTIPLIER_BPS = 8800;
    private static final int MAX_PRICE_MULTIPLIER_BPS = 11200;
    private static final int NO_NOTE_ID = -1;
    private static final Map<Integer, String[]> ITEM_ALIASES = createItemAliases();
    private static final List<Integer> MARKET_ITEM_IDS = MarketItemCategory.allItemIds();
    private static final CategoryCue[] CATEGORY_CUES = createCategoryCues();
    private static final Map<Integer, Integer> NOTE_ID_CACHE = new ConcurrentHashMap<>();

    private final Archetype archetype;
    private final PersonalityStyle style;
    private final TraderSpecialty specialty;
    private final InventoryTheme theme;
    private final Map<Integer, Integer> buyLimits = new HashMap<>();
    private final Map<Integer, Integer> initialBuyLimits = new HashMap<>();
    private final Map<Integer, Integer> sellStock = new HashMap<>();
    private final Map<Integer, Integer> initialSellStock = new HashMap<>();
    private final Map<Integer, Integer> buyItemVarianceBps = new HashMap<>();
    private final Map<Integer, Integer> sellItemVarianceBps = new HashMap<>();
    private final int initialGpBudget;
    private final int buyMultiplierBps;
    private final int sellMultiplierBps;
    private final int concessionBps;
    private final int hagglePatienceBps;

    private int gpBudget;

    private MarketBotProfile(Archetype archetype, PersonalityStyle style, int gpBudget,
            int buyMultiplierBps, int sellMultiplierBps, int concessionBps, int hagglePatienceBps) {
        this(archetype, style, rollSpecialty(archetype), gpBudget, buyMultiplierBps,
                sellMultiplierBps, concessionBps, hagglePatienceBps);
    }

    private MarketBotProfile(Archetype archetype, PersonalityStyle style, TraderSpecialty specialty, int gpBudget,
            int buyMultiplierBps, int sellMultiplierBps, int concessionBps, int hagglePatienceBps) {
        this.archetype = archetype;
        this.style = style == null ? PersonalityStyle.BALANCED : style;
        this.specialty = specialty == null ? rollSpecialty(archetype) : specialty;
        this.theme = rollTheme(archetype, this.specialty);
        this.gpBudget = Math.max(0, gpBudget);
        this.initialGpBudget = this.gpBudget;
        this.buyMultiplierBps = clampNormalBps(buyMultiplierBps);
        this.sellMultiplierBps = clampNormalBps(sellMultiplierBps);
        this.concessionBps = Math.max(0, Math.min(600, concessionBps));
        this.hagglePatienceBps = Math.max(0, Math.min(BPS, hagglePatienceBps));
    }

    public static boolean isTradingRole(IdleCrowdBotScript.Role role) {
        return role == IdleCrowdBotScript.Role.BUYER
                || role == IdleCrowdBotScript.Role.SELLER
                || role == IdleCrowdBotScript.Role.MERCHER
                || role == IdleCrowdBotScript.Role.NEWCOMER;
    }

    public static MarketBotProfile forRole(IdleCrowdBotScript.Role role) {
        return random(pickArchetype(role));
    }

    public static MarketBotProfile random() {
        return random(randomArchetype());
    }

    private static MarketBotProfile random(Archetype archetype) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        PersonalityStyle style = rollStyle(archetype);
        MarketBotProfile profile;
        switch (archetype) {
            case RESOURCE_BUYER:
                profile = new MarketBotProfile(archetype, style, rand(2_000_000, 30_000_000),
                        rollPriceMultiplierBps(), rollPriceMultiplierBps(), rand(60, 200), rand(2500, 5500));
                profile.addBuyCategories(MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.BARS, MarketItemCategory.HERBS,
                        MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.DIVINATION_ENERGY, MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.SUMMONING_REAGENTS);
                profile.addSellCategories(MarketItemCategory.FOOD, MarketItemCategory.RAW_FISH,
                        MarketItemCategory.RUNES, MarketItemCategory.TALISMANS,
                        MarketItemCategory.RANGED_AMMO, MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.SUMMONING_REAGENTS);
                break;
            case SKILLER:
                profile = new MarketBotProfile(archetype, style, rand(750_000, 14_000_000),
                        rollPriceMultiplierBps(), rollPriceMultiplierBps(), rand(80, 260), rand(3000, 6500));
                profile.addBuyCategories(MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.BARS, MarketItemCategory.HERBS,
                        MarketItemCategory.RAW_FISH, MarketItemCategory.GEMS,
                        MarketItemCategory.FARMING_SEEDS, MarketItemCategory.DIVINATION_ENERGY,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.SUMMONING_REAGENTS);
                profile.addSellCategories(MarketItemCategory.RUNES, MarketItemCategory.TALISMANS,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.SHORTBOWS,
                        MarketItemCategory.GEMS, MarketItemCategory.BARS,
                        MarketItemCategory.DIVINATION_ENERGY, MarketItemCategory.FARMING_SEEDS,
                        MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.JEWELRY, MarketItemCategory.SUMMONING_REAGENTS);
                break;
            case PKER:
                profile = new MarketBotProfile(archetype, style, rand(1_500_000, 35_000_000),
                        rollPriceMultiplierBps(), rollPriceMultiplierBps(), rand(40, 170), rand(2000, 5000));
                profile.addBuyCategories(MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.POTIONS,
                        MarketItemCategory.PVP_GEAR, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.JEWELRY, MarketItemCategory.SUMMONING_REAGENTS);
                profile.addSellCategories(MarketItemCategory.PVP_GEAR, MarketItemCategory.MELEE_GEAR,
                        MarketItemCategory.RANGED_GEAR, MarketItemCategory.MAGE_GEAR,
                        MarketItemCategory.RANGED_AMMO, MarketItemCategory.JEWELRY);
                break;
            case BOSSER:
                profile = new MarketBotProfile(archetype, style, rand(5_000_000, 90_000_000),
                        rollPriceMultiplierBps(), rollPriceMultiplierBps(), rand(50, 190), rand(2500, 6000));
                profile.addBuyCategories(MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                        MarketItemCategory.POTIONS, MarketItemCategory.RUNES,
                        MarketItemCategory.RANGED_AMMO, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS);
                profile.addSellCategories(MarketItemCategory.BOSS_LOOT, MarketItemCategory.BONES,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY);
                break;
            case MERCHER:
                profile = new MarketBotProfile(archetype, style, rand(10_000_000, 140_000_000),
                        rollPriceMultiplierBps(), rollPriceMultiplierBps(), rand(20, 110), rand(1000, 3500));
                profile.addBuyCategories(MarketItemCategory.SUPPLIES, MarketItemCategory.PVP_GEAR,
                        MarketItemCategory.FOOD, MarketItemCategory.RUNES, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.BARS, MarketItemCategory.GEMS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.BOSS_LOOT, MarketItemCategory.GENERAL_MARKET);
                profile.addSellCategories(MarketItemCategory.SUPPLIES, MarketItemCategory.POTIONS,
                        MarketItemCategory.PVP_GEAR,
                        MarketItemCategory.FOOD, MarketItemCategory.RUNES, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.BARS, MarketItemCategory.GEMS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY,
                        MarketItemCategory.HERBS, MarketItemCategory.RAW_FISH,
                        MarketItemCategory.DIVINATION_ENERGY, MarketItemCategory.FARMING_SEEDS,
                        MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.TALISMANS,
                        MarketItemCategory.SUMMONING_REAGENTS, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.BOSS_LOOT, MarketItemCategory.GENERAL_MARKET);
                break;
            case COLLECTOR:
                profile = new MarketBotProfile(archetype, PersonalityStyle.COLLECTOR,
                        rand(15_000_000, 160_000_000), rollPriceMultiplierBps(), rollPriceMultiplierBps(),
                        rand(30, 120), rand(1500, 4000));
                profile.addBuyCategories(MarketItemCategory.RARES, MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY);
                profile.addSellCategories(MarketItemCategory.PVP_GEAR, MarketItemCategory.ADVANCED_GEAR,
                        MarketItemCategory.JEWELRY);
                break;
            case NEWCOMER:
                profile = new MarketBotProfile(archetype, style, rand(25_000, 1_250_000),
                        rollPriceMultiplierBps(), rollPriceMultiplierBps(), rand(150, 500), rand(5000, 8500));
                profile.addBuyCategories(MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.CRAFTING_INPUTS);
                profile.addSellCategories(MarketItemCategory.RUNES, MarketItemCategory.PVP_GEAR,
                        MarketItemCategory.FOOD, MarketItemCategory.RAW_FISH,
                        MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY);
                break;
            case SUPPLY_TRADER:
            default:
                profile = new MarketBotProfile(archetype, style, rand(1_000_000, 24_000_000),
                        rollPriceMultiplierBps(), rollPriceMultiplierBps(), rand(80, 240), rand(3000, 6500));
                profile.addBuyCategories(MarketItemCategory.HERBS, MarketItemCategory.FOOD,
                        MarketItemCategory.RAW_FISH, MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.BARS, MarketItemCategory.FARMING_SEEDS,
                        MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.SUMMONING_REAGENTS);
                profile.addSellCategories(MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                        MarketItemCategory.RAW_FISH, MarketItemCategory.POTIONS,
                        MarketItemCategory.RUNES, MarketItemCategory.HERBS,
                        MarketItemCategory.FARMING_SEEDS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS);
                break;
        }
        profile.applySpecialty();
        profile.applyTheme();
        profile.limitBuyInterests();
        if (random.nextInt(100) < 20 && profile.style != PersonalityStyle.DESPERATE_BUYER) {
            profile.gpBudget = Math.max(1, profile.gpBudget / rand(2, 5));
        }
        profile.removeOverlappingSellItems();
        profile.limitSellStock();
        profile.registerGuardrails();
        return profile;
    }

    private static Archetype pickArchetype(IdleCrowdBotScript.Role role) {
        if (role == null) {
            return randomArchetype();
        }
        switch (role) {
            case BUYER:
                return weightedRandom(Archetype.RESOURCE_BUYER, Archetype.SKILLER,
                        Archetype.PKER, Archetype.BOSSER, Archetype.COLLECTOR);
            case SELLER:
                return weightedRandom(Archetype.SUPPLY_TRADER, Archetype.BOSSER,
                        Archetype.PKER, Archetype.SKILLER);
            case MERCHER:
                return Archetype.MERCHER;
            case NEWCOMER:
                return Archetype.NEWCOMER;
            default:
                return randomArchetype();
        }
    }

    private static Archetype randomArchetype() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 22) return Archetype.SUPPLY_TRADER;
        if (roll < 38) return Archetype.RESOURCE_BUYER;
        if (roll < 52) return Archetype.SKILLER;
        if (roll < 66) return Archetype.PKER;
        if (roll < 80) return Archetype.BOSSER;
        if (roll < 92) return Archetype.MERCHER;
        if (roll < 97) return Archetype.COLLECTOR;
        return Archetype.NEWCOMER;
    }

    private static PersonalityStyle rollStyle(Archetype archetype) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int roll = random.nextInt(100);
        switch (archetype) {
            case MERCHER:
                if (roll < 55) return PersonalityStyle.HARD_NEGOTIATOR;
                if (roll < 75) return PersonalityStyle.SCAM_SENSITIVE;
                return PersonalityStyle.BULK_BUYER;
            case COLLECTOR:
                return PersonalityStyle.COLLECTOR;
            case RESOURCE_BUYER:
            case BOSSER:
                if (roll < 35) return PersonalityStyle.DESPERATE_BUYER;
                if (roll < 65) return PersonalityStyle.BULK_BUYER;
                break;
            case SUPPLY_TRADER:
            case SKILLER:
                if (roll < 35) return PersonalityStyle.IMPATIENT_SELLER;
                if (roll < 55) return PersonalityStyle.BULK_BUYER;
                break;
            case PKER:
                if (roll < 35) return PersonalityStyle.DESPERATE_BUYER;
                if (roll < 55) return PersonalityStyle.HARD_NEGOTIATOR;
                break;
            default:
                break;
        }
        if (roll < 20) return PersonalityStyle.HARD_NEGOTIATOR;
        if (roll < 35) return PersonalityStyle.SCAM_SENSITIVE;
        return PersonalityStyle.BALANCED;
    }

    private static TraderSpecialty rollSpecialty(Archetype archetype) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        switch (archetype) {
            case RESOURCE_BUYER:
            case SKILLER:
                return weightedRandom(TraderSpecialty.SKILLING_BULK_BUYER,
                        TraderSpecialty.HERB_RUNNER, TraderSpecialty.FISHMONGER,
                        TraderSpecialty.RUNE_MERCHANT, TraderSpecialty.SMITHING_SUPPLIER,
                        TraderSpecialty.FLETCHING_SUPPLIER, TraderSpecialty.CRAFTING_JEWELER,
                        TraderSpecialty.FARMING_SUPPLIER, TraderSpecialty.SUMMONING_SUPPLIER,
                        TraderSpecialty.POTION_FLIPPER);
            case PKER:
                return weightedRandom(TraderSpecialty.PVP_RESTOCKER,
                        TraderSpecialty.RUNE_MERCHANT, TraderSpecialty.FISHMONGER,
                        TraderSpecialty.PVM_SUPPLY_RUNNER);
            case BOSSER:
                return weightedRandom(TraderSpecialty.BOSS_LOOT_DUMPER,
                        TraderSpecialty.PVM_SUPPLY_RUNNER, TraderSpecialty.HIGH_TIER_GEAR_TRADER,
                        TraderSpecialty.RUNE_MERCHANT, TraderSpecialty.SUMMONING_SUPPLIER);
            case MERCHER:
                return weightedRandom(TraderSpecialty.POTION_FLIPPER, TraderSpecialty.PVP_RESTOCKER,
                        TraderSpecialty.SKILLING_BULK_BUYER, TraderSpecialty.RUNE_MERCHANT,
                        TraderSpecialty.HIGH_TIER_GEAR_TRADER, TraderSpecialty.CRAFTING_JEWELER,
                        TraderSpecialty.SMITHING_SUPPLIER, TraderSpecialty.IMPATIENT_BANK_CLEARER);
            case COLLECTOR:
                return weightedRandom(TraderSpecialty.CLUE_RARE_COLLECTOR,
                        TraderSpecialty.HIGH_TIER_GEAR_TRADER);
            case NEWCOMER:
                return TraderSpecialty.BROKE_NEWCOMER;
            case SUPPLY_TRADER:
            default:
                return weightedRandom(TraderSpecialty.POTION_FLIPPER, TraderSpecialty.PVP_RESTOCKER,
                        TraderSpecialty.FISHMONGER, TraderSpecialty.RUNE_MERCHANT,
                        TraderSpecialty.PVM_SUPPLY_RUNNER, TraderSpecialty.HERB_RUNNER,
                        TraderSpecialty.FARMING_SUPPLIER, TraderSpecialty.SUMMONING_SUPPLIER,
                        TraderSpecialty.IMPATIENT_BANK_CLEARER);
        }
    }

    private static TraderSpecialty weightedRandom(TraderSpecialty... specialties) {
        return specialties[ThreadLocalRandom.current().nextInt(specialties.length)];
    }

    private static InventoryTheme weightedRandom(InventoryTheme... themes) {
        return themes[ThreadLocalRandom.current().nextInt(themes.length)];
    }

    private static InventoryTheme rollTheme(Archetype archetype, TraderSpecialty specialty) {
        if (specialty == TraderSpecialty.IMPATIENT_BANK_CLEARER) {
            return InventoryTheme.BANK_SALE;
        }
        switch (specialty) {
            case POTION_FLIPPER:
            case HERB_RUNNER:
                return InventoryTheme.POTIONS_AND_HERBS;
            case FISHMONGER:
                return InventoryTheme.FISH_AND_FOOD;
            case PVP_RESTOCKER:
                return InventoryTheme.PVP_RESTOCK;
            case RUNE_MERCHANT:
                return InventoryTheme.RUNES_AND_MAGIC;
            case SMITHING_SUPPLIER:
                return InventoryTheme.SKILLING_MATERIALS;
            case FLETCHING_SUPPLIER:
                return weightedRandom(InventoryTheme.SKILLING_MATERIALS, InventoryTheme.PVP_RESTOCK);
            case CRAFTING_JEWELER:
                return InventoryTheme.CRAFTING_AND_JEWELRY;
            case FARMING_SUPPLIER:
                return InventoryTheme.FARMING;
            case SUMMONING_SUPPLIER:
                return InventoryTheme.SUMMONING;
            case PVM_SUPPLY_RUNNER:
                return InventoryTheme.BOSS_LOOT;
            case CLUE_RARE_COLLECTOR:
            case HIGH_TIER_GEAR_TRADER:
                return InventoryTheme.RARES_AND_CLUES;
            case BOSS_LOOT_DUMPER:
                return InventoryTheme.BOSS_LOOT;
            case BROKE_NEWCOMER:
                return InventoryTheme.NEWCOMER_CLEAROUT;
            case SKILLING_BULK_BUYER:
                return weightedRandom(InventoryTheme.SKILLING_MATERIALS, InventoryTheme.FISH_AND_FOOD,
                        InventoryTheme.CRAFTING_AND_JEWELRY, InventoryTheme.FARMING,
                        InventoryTheme.SUMMONING, InventoryTheme.RUNES_AND_MAGIC);
            default:
                break;
        }
        switch (archetype) {
            case RESOURCE_BUYER:
            case SKILLER:
                return weightedRandom(InventoryTheme.SKILLING_MATERIALS, InventoryTheme.FISH_AND_FOOD,
                        InventoryTheme.CRAFTING_AND_JEWELRY, InventoryTheme.FARMING,
                        InventoryTheme.SUMMONING);
            case PKER:
                return InventoryTheme.PVP_RESTOCK;
            case BOSSER:
                return InventoryTheme.BOSS_LOOT;
            case COLLECTOR:
                return InventoryTheme.RARES_AND_CLUES;
            case NEWCOMER:
                return InventoryTheme.NEWCOMER_CLEAROUT;
            case MERCHER:
                return weightedRandom(InventoryTheme.POTIONS_AND_HERBS, InventoryTheme.PVP_RESTOCK,
                        InventoryTheme.RUNES_AND_MAGIC, InventoryTheme.CRAFTING_AND_JEWELRY,
                        InventoryTheme.BANK_SALE);
            case SUPPLY_TRADER:
            default:
                return weightedRandom(InventoryTheme.POTIONS_AND_HERBS, InventoryTheme.PVP_RESTOCK,
                        InventoryTheme.RUNES_AND_MAGIC, InventoryTheme.FISH_AND_FOOD,
                        InventoryTheme.BANK_SALE);
        }
    }

    private void focusSpecialty(MarketItemCategory[] buyCategories, MarketItemCategory[] sellCategories) {
        if (buyCategories != null && buyCategories.length > 0) {
            retainBuyCategories(buyCategories);
            addBuyCategories(buyCategories);
        }
        if (sellCategories != null && sellCategories.length > 0) {
            retainSellCategories(sellCategories);
            addSellCategories(sellCategories);
        }
    }

    private void applySpecialty() {
        switch (specialty) {
            case POTION_FLIPPER:
                retainBuyCategories(MarketItemCategory.HERBS, MarketItemCategory.SUPPLIES,
                        MarketItemCategory.POTIONS, MarketItemCategory.FARMING_PRODUCTS);
                retainSellCategories(MarketItemCategory.HERBS, MarketItemCategory.SUPPLIES,
                        MarketItemCategory.POTIONS, MarketItemCategory.FARMING_PRODUCTS);
                addBuyCategories(MarketItemCategory.HERBS, MarketItemCategory.SUPPLIES,
                        MarketItemCategory.POTIONS, MarketItemCategory.FARMING_PRODUCTS);
                addSellCategories(MarketItemCategory.HERBS, MarketItemCategory.SUPPLIES,
                        MarketItemCategory.POTIONS, MarketItemCategory.FARMING_PRODUCTS);
                break;
            case HERB_RUNNER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.HERBS, MarketItemCategory.FARMING_PRODUCTS,
                                MarketItemCategory.FARMING_SEEDS, MarketItemCategory.POTIONS,
                                MarketItemCategory.SUPPLIES},
                        new MarketItemCategory[] {MarketItemCategory.HERBS, MarketItemCategory.FARMING_PRODUCTS,
                                MarketItemCategory.POTIONS, MarketItemCategory.SUPPLIES});
                break;
            case FISHMONGER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.RAW_FISH, MarketItemCategory.FOOD,
                                MarketItemCategory.SUPPLIES},
                        new MarketItemCategory[] {MarketItemCategory.RAW_FISH, MarketItemCategory.FOOD,
                                MarketItemCategory.SUPPLIES});
                break;
            case RUNE_MERCHANT:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.RUNES, MarketItemCategory.TALISMANS,
                                MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.MAGE_GEAR},
                        new MarketItemCategory[] {MarketItemCategory.RUNES, MarketItemCategory.TALISMANS,
                                MarketItemCategory.MAGE_GEAR, MarketItemCategory.SKILLING_INPUTS});
                break;
            case SMITHING_SUPPLIER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.ORES, MarketItemCategory.BARS,
                                MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.MELEE_GEAR},
                        new MarketItemCategory[] {MarketItemCategory.ORES, MarketItemCategory.BARS,
                                MarketItemCategory.MELEE_GEAR});
                break;
            case FLETCHING_SUPPLIER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.LOGS, MarketItemCategory.SHORTBOWS,
                                MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.RANGED_AMMO},
                        new MarketItemCategory[] {MarketItemCategory.LOGS, MarketItemCategory.SHORTBOWS,
                                MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.RANGED_AMMO,
                                MarketItemCategory.RANGED_GEAR});
                break;
            case CRAFTING_JEWELER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.CRAFTING_INPUTS,
                                MarketItemCategory.GEMS, MarketItemCategory.JEWELRY},
                        new MarketItemCategory[] {MarketItemCategory.CRAFTING_INPUTS,
                                MarketItemCategory.GEMS, MarketItemCategory.JEWELRY});
                break;
            case FARMING_SUPPLIER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.FARMING_SEEDS,
                                MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.HERBS},
                        new MarketItemCategory[] {MarketItemCategory.FARMING_SEEDS,
                                MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.HERBS,
                                MarketItemCategory.POTIONS});
                break;
            case SUMMONING_SUPPLIER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.SUMMONING_REAGENTS,
                                MarketItemCategory.BONES, MarketItemCategory.TALISMANS},
                        new MarketItemCategory[] {MarketItemCategory.SUMMONING_REAGENTS,
                                MarketItemCategory.BONES, MarketItemCategory.TALISMANS,
                                MarketItemCategory.SUPPLIES});
                break;
            case PVP_RESTOCKER:
                retainBuyCategories(MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.POTIONS,
                        MarketItemCategory.PVP_GEAR, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.MELEE_GEAR, MarketItemCategory.RANGED_GEAR,
                        MarketItemCategory.MAGE_GEAR, MarketItemCategory.JEWELRY);
                retainSellCategories(MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.POTIONS,
                        MarketItemCategory.PVP_GEAR, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.MELEE_GEAR, MarketItemCategory.RANGED_GEAR,
                        MarketItemCategory.MAGE_GEAR, MarketItemCategory.JEWELRY);
                addBuyCategories(MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.POTIONS,
                        MarketItemCategory.PVP_GEAR, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.JEWELRY);
                addSellCategories(MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.POTIONS,
                        MarketItemCategory.PVP_GEAR, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.JEWELRY);
                break;
            case PVM_SUPPLY_RUNNER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                                MarketItemCategory.POTIONS, MarketItemCategory.RUNES,
                                MarketItemCategory.RANGED_AMMO, MarketItemCategory.JEWELRY,
                                MarketItemCategory.SUMMONING_REAGENTS},
                        new MarketItemCategory[] {MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                                MarketItemCategory.POTIONS, MarketItemCategory.RUNES,
                                MarketItemCategory.RANGED_AMMO, MarketItemCategory.JEWELRY,
                                MarketItemCategory.SUMMONING_REAGENTS});
                break;
            case CLUE_RARE_COLLECTOR:
                retainBuyCategories(MarketItemCategory.RARES, MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY);
                retainSellCategories(MarketItemCategory.RARES, MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.PVP_GEAR, MarketItemCategory.ADVANCED_GEAR,
                        MarketItemCategory.JEWELRY);
                addBuyCategories(MarketItemCategory.RARES, MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY);
                addSellCategories(MarketItemCategory.RARES, MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY);
                break;
            case HIGH_TIER_GEAR_TRADER:
                focusSpecialty(
                        new MarketItemCategory[] {MarketItemCategory.ADVANCED_GEAR,
                                MarketItemCategory.BOSS_LOOT, MarketItemCategory.RARES,
                                MarketItemCategory.JEWELRY, MarketItemCategory.PVP_GEAR},
                        new MarketItemCategory[] {MarketItemCategory.ADVANCED_GEAR,
                                MarketItemCategory.BOSS_LOOT, MarketItemCategory.RARES,
                                MarketItemCategory.JEWELRY, MarketItemCategory.PVP_GEAR});
                break;
            case SKILLING_BULK_BUYER:
                retainBuyCategories(MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.BARS, MarketItemCategory.HERBS,
                        MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.RAW_FISH,
                        MarketItemCategory.GEMS, MarketItemCategory.DIVINATION_ENERGY,
                        MarketItemCategory.FARMING_SEEDS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS);
                retainSellCategories(MarketItemCategory.RUNES, MarketItemCategory.SUPPLIES,
                        MarketItemCategory.POTIONS, MarketItemCategory.FOOD,
                        MarketItemCategory.SHORTBOWS, MarketItemCategory.GEMS,
                        MarketItemCategory.BARS, MarketItemCategory.DIVINATION_ENERGY,
                        MarketItemCategory.FARMING_SEEDS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.TALISMANS, MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.JEWELRY, MarketItemCategory.SUMMONING_REAGENTS);
                addBuyCategories(MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.BARS, MarketItemCategory.HERBS,
                        MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.RAW_FISH,
                        MarketItemCategory.GEMS, MarketItemCategory.DIVINATION_ENERGY,
                        MarketItemCategory.FARMING_SEEDS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS);
                addSellCategories(MarketItemCategory.RUNES, MarketItemCategory.TALISMANS,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.SHORTBOWS,
                        MarketItemCategory.GEMS, MarketItemCategory.BARS,
                        MarketItemCategory.DIVINATION_ENERGY, MarketItemCategory.FARMING_SEEDS,
                        MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.JEWELRY, MarketItemCategory.SUMMONING_REAGENTS);
                break;
            case BOSS_LOOT_DUMPER:
                retainBuyCategories(MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                        MarketItemCategory.POTIONS, MarketItemCategory.RUNES,
                        MarketItemCategory.RANGED_AMMO, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS);
                retainSellCategories(MarketItemCategory.BOSS_LOOT, MarketItemCategory.BONES,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY);
                addBuyCategories(MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                        MarketItemCategory.POTIONS, MarketItemCategory.RUNES,
                        MarketItemCategory.RANGED_AMMO, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS);
                addSellCategories(MarketItemCategory.BOSS_LOOT, MarketItemCategory.BONES,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY);
                break;
            case BROKE_NEWCOMER:
                retainBuyCategories(MarketItemCategory.FOOD, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.RUNES,
                        MarketItemCategory.CRAFTING_INPUTS);
                retainSellCategories(MarketItemCategory.FOOD, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.RUNES,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY);
                addBuyCategories(MarketItemCategory.FOOD, MarketItemCategory.LOGS,
                        MarketItemCategory.ORES, MarketItemCategory.CRAFTING_INPUTS);
                addSellCategories(MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY);
                gpBudget = Math.min(gpBudget, rand(80_000, 550_000));
                break;
            case IMPATIENT_BANK_CLEARER:
            default:
                addSellCategories(MarketItemCategory.SUPPLIES, MarketItemCategory.FOOD,
                        MarketItemCategory.POTIONS, MarketItemCategory.RUNES,
                        MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.GEMS,
                        MarketItemCategory.HERBS, MarketItemCategory.RAW_FISH,
                        MarketItemCategory.DIVINATION_ENERGY, MarketItemCategory.FARMING_SEEDS,
                        MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.TALISMANS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS, MarketItemCategory.BOSS_LOOT);
                retainBuyCategories(MarketItemCategory.FOOD, MarketItemCategory.RUNES,
                        MarketItemCategory.SUPPLIES, MarketItemCategory.POTIONS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.SUMMONING_REAGENTS);
                break;
        }
    }

    private void applyTheme() {
        if (theme == InventoryTheme.BANK_SALE) {
            addSellCategories(theme.sellCategories());
            addBuyCategories(theme.buyCategories());
            return;
        }
        if (theme.buyCategories().length > 0) {
            retainBuyCategories(theme.buyCategories());
            addBuyCategories(theme.buyCategories());
        }
        if (theme.sellCategories().length > 0) {
            retainSellCategories(theme.sellCategories());
            addSellCategories(theme.sellCategories());
        }
    }

    public synchronized List<Item> chooseSellBundle() {
        if (sellStock.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ids = themedSellOrder();
        List<Item> bundle = new ArrayList<>();
        int targetEntries = rand(Math.min(MIN_SELL_BUNDLE_ITEMS, ids.size()),
                Math.min(MAX_SELL_BUNDLE_ITEMS, ids.size()));
        long targetValue = rand(45_000, archetype == Archetype.NEWCOMER ? 180_000
                : style == PersonalityStyle.BULK_BUYER ? 1_250_000 : 800_000);
        if (ThreadLocalRandom.current().nextInt(100) < 28) {
            targetValue = Math.max(5_000L, targetValue / rand(2, 5));
        } else if (ThreadLocalRandom.current().nextInt(100) < 18) {
            targetValue = Math.min(3_000_000L, targetValue * rand(2, 4));
        }
        for (int id : ids) {
            Integer stock = sellStock.get(id);
            if (stock == null || stock <= 0) {
                continue;
            }
            int amount = chooseBundleAmount(id, stock, targetValue);
            if (amount <= 0) {
                continue;
            }
            bundle.add(new Item(id, amount));
            if (bundle.size() >= targetEntries) {
                break;
            }
        }
        return bundle;
    }

    private List<Integer> themedSellOrder() {
        List<Integer> ids = new ArrayList<>(sellStock.keySet());
        if (ids.size() < 2 || theme == InventoryTheme.BANK_SALE
                || ThreadLocalRandom.current().nextInt(100) < 14) {
            Collections.shuffle(ids);
            return ids;
        }
        List<Integer> themed = new ArrayList<>();
        List<Integer> other = new ArrayList<>();
        for (Integer id : ids) {
            if (id != null && isThemeSellItem(id)) {
                themed.add(id);
            } else {
                other.add(id);
            }
        }
        if (themed.isEmpty()) {
            Collections.shuffle(ids);
            return ids;
        }
        Collections.shuffle(themed);
        Collections.shuffle(other);
        themed.addAll(other);
        return themed;
    }

    private boolean isThemeSellItem(int itemId) {
        return MarketItemCategory.containsItem(itemId, theme.sellCategories());
    }

    private boolean isThemeBuyItem(int itemId) {
        return MarketItemCategory.containsItem(itemId, theme.buyCategories());
    }

    public synchronized RequestedBundle chooseRequestedSellBundle(String request) {
        if (sellStock.isEmpty()) {
            return RequestedBundle.fail("I'm not selling anything right now.");
        }
        int itemId = findMatchingItemId(sellStock, request);
        if (itemId < 0) {
            return RequestedBundle.fail("I don't have that with me. I've got " + describeSellInterests(5) + ".");
        }
        int stock = Math.max(1, sellStock.get(itemId));
        int requestedAmount = parseFirstAmount(request, -1);
        int amount = requestedAmount > 0 ? Math.min(stock, requestedAmount)
                : Math.min(stock, Math.max(1, chooseBundleAmount(itemId, stock, 1_000_000)));
        List<Item> bundle = Collections.singletonList(new Item(itemId, amount));
        return RequestedBundle.ok(bundle, "Alright, " + formatAmount(amount) + " " + itemName(itemId) + ".");
    }

    public synchronized int quoteBuyPrice(int itemId, int amount) {
        if (itemId == COINS || amount <= 0 || !buyLimits.containsKey(itemId)) {
            return -1;
        }
        Integer limit = buyLimits.get(itemId);
        if (limit == null || amount > limit) {
            return -1;
        }
        long total = getBuyTotal(itemId, amount);
        if (total <= 0 || total >= Integer.MAX_VALUE || total > gpBudget) {
            return -1;
        }
        return (int) total;
    }

    public synchronized int quoteBuyTotal(Map<Integer, Integer> items) {
        long total = 0;
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int quote = quoteBuyPrice(entry.getKey(), entry.getValue());
            if (quote < 0) {
                return -1;
            }
            total += quote;
            if (total >= Integer.MAX_VALUE || total > gpBudget) {
                return -1;
            }
        }
        return (int) total;
    }

    /**
     * Lets the trade flow distinguish "I can't afford that much" from
     * "I don't buy that item." When this returns true every quoteBuyPrice
     * call below the cheapest item would also fail with -1, so without
     * this distinction the player gets a misleading "I'm not buying X
     * right now" message.
     */
    public synchronized boolean isOutOfCash() {
        return gpBudget < 1000;
    }

    public synchronized boolean hasStock(List<Item> items) {
        for (Item item : items) {
            if (item == null) {
                continue;
            }
            Integer stock = sellStock.get(item.getId());
            if (stock == null || stock < item.getAmount()) {
                return false;
            }
        }
        return true;
    }

    public synchronized int getSellBundlePrice(List<Item> items) {
        return getSellBundlePrice(items, 0);
    }

    public synchronized int getSellBundlePrice(List<Item> items, int timePressureDiscountBps) {
        long total = 0;
        for (Item item : items) {
            if (item == null) {
                continue;
            }
            total += getSellTotal(item.getId(), item.getAmount(), timePressureDiscountBps);
            if (total > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) Math.max(1, total);
    }

    public synchronized int getCounterOfferPrice(List<Item> items, int offeredCoins,
            int timePressureDiscountBps) {
        int ask = getSellBundlePrice(items, timePressureDiscountBps);
        if (offeredCoins >= ask) {
            return ask;
        }
        long concession = ((long) ask * Math.max(25, concessionBps)) / BPS;
        int counter = (int) Math.max(offeredCoins + 1L, ask - concession);
        return Math.max(1, Math.min(ask, counter));
    }

    public synchronized int getBuyPrice(int itemId) {
        return getBuyPrice(itemId, 1);
    }

    public synchronized int getBuyPrice(int itemId, int amount) {
        return scalePrice(itemId, adjustedBuyBps(itemId, amount));
    }

    public synchronized int getSellPrice(int itemId) {
        return getSellPrice(itemId, 1, 0);
    }

    public synchronized int getSellPrice(int itemId, int amount, int timePressureDiscountBps) {
        return scalePrice(itemId, adjustedSellBps(itemId, amount, timePressureDiscountBps));
    }

    public synchronized int getHaggleChanceBps(int offeredCoins, int askingPrice) {
        if (offeredCoins >= askingPrice) {
            return BPS;
        }
        if (askingPrice <= 0 || concessionBps <= 0 || offeredCoins <= 0) {
            return 0;
        }
        long maxGap = Math.max(1L, ((long) askingPrice * concessionBps) / BPS);
        long gap = askingPrice - offeredCoins;
        if (gap > maxGap) {
            return 0;
        }
        long closeness = maxGap - gap;
        int patience = Math.max(1000, hagglePatienceBps);
        int chance = (int) Math.max(250, Math.min(BPS,
                1000L + ((closeness * (patience - 1000L)) / maxGap)));
        if (style == PersonalityStyle.HARD_NEGOTIATOR || style == PersonalityStyle.SCAM_SENSITIVE) {
            chance /= 2;
        } else if (style == PersonalityStyle.IMPATIENT_SELLER) {
            chance = Math.min(BPS, chance + 1200);
        }
        return chance;
    }

    public synchronized int getTimePressureDiscountBps(long openedAt, long expiresAt) {
        long now = Utils.currentTimeMillis();
        long duration = Math.max(1L, expiresAt - openedAt);
        long elapsed = Math.max(0L, now - openedAt);
        int elapsedBps = (int) Math.min(BPS, (elapsed * BPS) / duration);
        int maxDiscount;
        switch (style) {
            case IMPATIENT_SELLER:
                maxDiscount = 280;
                break;
            case DESPERATE_BUYER:
            case BULK_BUYER:
                maxDiscount = 130;
                break;
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
                maxDiscount = 40;
                break;
            default:
                maxDiscount = 90;
        }
        return (elapsedBps * maxDiscount) / BPS;
    }

    public synchronized void recordSale(int coinsReceived, List<Item> soldItems) {
        for (Item item : soldItems) {
            if (item == null) {
                continue;
            }
            // sellStock is keyed by un-noted ("stock") IDs. The sellBundle is
            // built from sellStock.keySet() so its IDs are already un-noted in
            // the normal path, but normalize defensively in case a future
            // change ever lets a noted ID slip in — otherwise decrement would
            // silently no-op against the wrong key.
            decrement(sellStock, toStockItemId(item.getId()), item.getAmount());
        }
        if (coinsReceived <= 0) {
            return;
        }
        // Match the cap used by replenishFromBank so a long-lived bot can't
        // accumulate toward Integer.MAX_VALUE through repeated sales and make
        // initialGpBudget meaningless. Excess coins are dropped (stashed
        // off-screen).
        long budgetCap = Math.max(1L, (long) initialGpBudget * 2L);
        if (gpBudget >= budgetCap) {
            return;
        }
        gpBudget = (int) Math.min(budgetCap, (long) gpBudget + coinsReceived);
    }

    public synchronized void recordPurchase(int coinsPaid, Map<Integer, Integer> purchasedItems) {
        gpBudget = Math.max(0, gpBudget - Math.max(0, coinsPaid));
        for (Map.Entry<Integer, Integer> entry : purchasedItems.entrySet()) {
            decrement(buyLimits, entry.getKey(), entry.getValue());
            if (archetype == Archetype.MERCHER) {
                // Add to current stock so the merchant can flip the item, but
                // leave initialSellStock alone — that map is the replenishment
                // baseline. Bumping it on every purchase would let merchants
                // permanently inflate their stock ceiling for any item they
                // happen to flip.
                increment(sellStock, entry.getKey(), entry.getValue());
                MarketPriceGuard.registerSellIntent(entry.getKey(), baseSellIntentBps(entry.getKey()));
            }
        }
    }

    public synchronized void replenishFromBank() {
        int buyRestockBps = rand(1800, 4200);
        for (Map.Entry<Integer, Integer> entry : initialBuyLimits.entrySet()) {
            int itemId = entry.getKey();
            int initial = Math.max(1, entry.getValue());
            int current = buyLimits.containsKey(itemId) ? buyLimits.get(itemId) : 0;
            if (current >= initial) {
                continue;
            }
            int amount = Math.max(1, (int) (((long) initial * buyRestockBps) / BPS));
            increment(buyLimits, itemId, Math.min(amount, initial - current));
        }
        int sellRestockBps = rand(1500, 3500);
        for (Map.Entry<Integer, Integer> entry : initialSellStock.entrySet()) {
            int itemId = entry.getKey();
            int initial = Math.max(1, entry.getValue());
            int current = sellStock.containsKey(itemId) ? sellStock.get(itemId) : 0;
            if (current >= initial) {
                continue;
            }
            int amount = Math.max(1, (int) (((long) initial * sellRestockBps) / BPS));
            increment(sellStock, itemId, Math.min(amount, initial - current));
            MarketPriceGuard.registerSellIntent(itemId, baseSellIntentBps(itemId));
        }
        if (gpBudget < initialGpBudget) {
            int cashRestock = rand(Math.max(1, initialGpBudget / 6), Math.max(1, initialGpBudget / 2));
            gpBudget = (int) Math.min((long) initialGpBudget * 2L, (long) gpBudget + cashRestock);
        }
    }

    public synchronized int getOfferDurationTicks() {
        switch (style) {
            case IMPATIENT_SELLER:
                return rand(120, 220);
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
                return rand(240, 420);
            default:
                return rand(170, 320);
        }
    }

    public int getAdvertResponseDelayTicks() {
        int delay;
        switch (style) {
            case IMPATIENT_SELLER:
            case DESPERATE_BUYER:
                delay = rand(3, 7);
                break;
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
                delay = rand(7, 14);
                break;
            case COLLECTOR:
                delay = rand(8, 16);
                break;
            case BULK_BUYER:
                delay = rand(5, 11);
                break;
            default:
                delay = rand(5, 10);
                break;
        }
        if (ThreadLocalRandom.current().nextInt(100) < 18) {
            delay += rand(3, 9);
        }
        return delay;
    }

    public int getAdvertTradeDelayTicks() {
        int extra;
        switch (style) {
            case IMPATIENT_SELLER:
            case DESPERATE_BUYER:
                extra = rand(2, 5);
                break;
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
                extra = rand(5, 11);
                break;
            default:
                extra = rand(3, 8);
                break;
        }
        if (ThreadLocalRandom.current().nextInt(100) < 12) {
            extra += rand(3, 8);
        }
        return extra;
    }

    public int getBotAcceptDelayTicks(boolean firstStage) {
        int delay;
        switch (style) {
            case IMPATIENT_SELLER:
            case DESPERATE_BUYER:
                delay = rand(1, 3);
                break;
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
                delay = rand(3, 8);
                break;
            case COLLECTOR:
                delay = rand(4, 9);
                break;
            case BULK_BUYER:
                delay = rand(2, 5);
                break;
            default:
                delay = rand(2, 5);
                break;
        }
        if (!firstStage) {
            delay += rand(1, 2);
        }
        if (ThreadLocalRandom.current().nextInt(100) < 16) {
            delay += rand(2, 6);
        }
        return delay;
    }

    public boolean shouldSendAcceptPauseLine(boolean firstStage) {
        int chance = firstStage ? 16 : 10;
        switch (style) {
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
            case COLLECTOR:
                chance += 8;
                break;
            case IMPATIENT_SELLER:
            case DESPERATE_BUYER:
                chance -= 7;
                break;
            default:
                break;
        }
        return ThreadLocalRandom.current().nextInt(100) < Math.max(3, chance);
    }

    public String getAcceptPauseLine(boolean firstStage) {
        if (!firstStage) {
            return pickLine("yep", "ok", "looks right", "done");
        }
        switch (style) {
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
                return pickLine("checking", "one sec", "counting", "let me see");
            case IMPATIENT_SELLER:
            case DESPERATE_BUYER:
                return pickLine("sure", "ok", "fine", "deal");
            case BULK_BUYER:
                return pickLine("checking amount", "sec", "looks fine", "counting");
            default:
                return pickLine("sec", "checking", "looks fine", "ok");
        }
    }

    public synchronized int getBigTradeThreshold() {
        switch (archetype) {
            case NEWCOMER:
                return 250_000;
            case COLLECTOR:
            case MERCHER:
                return 5_000_000;
            default:
                return 1_500_000;
        }
    }

    public synchronized int getGpBudget() {
        return gpBudget;
    }

    public synchronized boolean exceedsTradeLimit(int value) {
        return archetype == Archetype.NEWCOMER && value > NEWCOMER_TRADE_LIMIT;
    }

    public String getTradeLimitLine() {
        return "can't trade that much yet, sorry.";
    }

    public int getCounterOfferHoldMillis() {
        return CHAT_COUNTER_WINDOW_MS;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public PersonalityStyle getStyle() {
        return style;
    }

    public TraderSpecialty getSpecialty() {
        return specialty;
    }

    public InventoryTheme getTheme() {
        return theme;
    }

    public synchronized String describeBuyInterests(int limit) {
        return describeItems(buyLimits, limit);
    }

    public synchronized String describeSellInterests(int limit) {
        return describeItems(sellStock, limit);
    }

    public synchronized String describeStock(List<Item> stock) {
        if (stock == null || stock.isEmpty()) {
            return "nothing right now";
        }
        List<String> parts = new ArrayList<>();
        List<Item> sorted = new ArrayList<>();
        for (Item item : stock) {
            if (item == null) {
                continue;
            }
            sorted.add(item);
        }
        Collections.sort(sorted, new Comparator<Item>() {
            @Override
            public int compare(Item first, Item second) {
                long secondValue = (long) GrandExchange.getPrice(second.getId()) * Math.max(1, second.getAmount());
                long firstValue = (long) GrandExchange.getPrice(first.getId()) * Math.max(1, first.getAmount());
                return secondValue > firstValue ? 1 : secondValue < firstValue ? -1 : 0;
            }
        });
        for (Item item : sorted) {
            parts.add(formatAmount(item.getAmount()) + " " + itemName(item.getId()));
        }
        return join(parts);
    }

    public synchronized String getAskPriceLine() {
        if (sellStock.isEmpty()) {
            return "I'm not selling anything right now. I'm buying " + describeBuyInterests(4) + ".";
        }
        int itemId = randomKey(sellStock);
        int stock = Math.max(1, sellStock.get(itemId));
        int amount = Math.min(stock, Math.max(1, chooseBundleAmount(itemId, stock, 800_000)));
        int price = getSellPrice(itemId, amount, 0);
        return "Selling " + formatAmount(amount) + " " + itemName(itemId) + " around "
                + formatAmount(price) + " each.";
    }

    public synchronized String getBuyingLine() {
        if (buyLimits.isEmpty()) {
            return "I'm not buying anything else right now.";
        }
        int itemId = randomKey(buyLimits);
        int limit = Math.max(1, buyLimits.get(itemId));
        int sampleAmount = Math.min(limit, likelyBulkAmount(itemId, limit));
        int price = getBuyPrice(itemId, sampleAmount);
        return "Buying " + itemName(itemId) + " up to " + formatAmount(limit)
                + ". I'd pay about " + formatAmount(price) + " gp each for "
                + formatAmount(sampleAmount) + ".";
    }

    public synchronized String getTradeChatter(boolean preferBuying) {
        if (preferBuying && !buyLimits.isEmpty()) {
            int itemId = randomKey(buyLimits);
            int limit = Math.max(1, buyLimits.get(itemId));
            int amount = Math.min(limit, likelyBulkAmount(itemId, limit));
            return formatSpecialtyChatter(true, itemName(itemId).toLowerCase(),
                    formatAmount(getBuyPrice(itemId, amount)));
        }
        if (!sellStock.isEmpty()) {
            int itemId = randomKey(sellStock);
            return formatSpecialtyChatter(false, itemName(itemId).toLowerCase(),
                    formatAmount(getSellPrice(itemId, 1, 0)));
        }
        if (!buyLimits.isEmpty()) {
            int itemId = randomKey(buyLimits);
            int limit = Math.max(1, buyLimits.get(itemId));
            int amount = Math.min(limit, likelyBulkAmount(itemId, limit));
            return formatSpecialtyChatter(true, itemName(itemId).toLowerCase(),
                    formatAmount(getBuyPrice(itemId, amount)));
        }
        return null;
    }

    private String formatSpecialtyChatter(boolean buying, String item, String priceEach) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextInt(100) >= 68) {
            return IdleCrowdBotScript.marketOfferLine(buying, item, random.nextBoolean() ? priceEach : null);
        }
        String price = priceEach == null || priceEach.isEmpty() || random.nextInt(100) < 35
                ? "" : " " + priceEach + " ea";
        String quick = random.nextBoolean() ? " " : " quick ";
        switch (specialty) {
            case POTION_FLIPPER:
                return buying ? pickLine("buying herbs " + item + price, "wtb pots " + item + price,
                        "need herb stuff " + item) : pickLine("selling pots " + item + price,
                        "wts potion restock " + item + price, "pots for sale " + item);
            case HERB_RUNNER:
                return buying ? pickLine("buying clean herbs " + item + price, "need seconds " + item,
                        "wtb herb run " + item + price, "restocking herblore " + item)
                        : pickLine("selling herb supplies " + item + price, "herb tab " + item,
                        "wts clean herbs " + item + price, "potion bits " + item);
            case FISHMONGER:
                return buying ? pickLine("buying fish " + item + price, "wtb raw or cooked " + item,
                        "need food stacks " + item + price, "fish buyer " + item)
                        : pickLine("selling food " + item + price, "wts fish " + item + price,
                        "cooked and raw " + item, "food stall " + item);
            case RUNE_MERCHANT:
                return buying ? pickLine("buying runes " + item + price, "wtb essence or runes " + item,
                        "need mage stock " + item + price, "rune buyer " + item)
                        : pickLine("selling runes " + item + price, "wts mage stock " + item,
                        "runes for sale " + item + price, "rc tab " + item);
            case SMITHING_SUPPLIER:
                return buying ? pickLine("buying ores bars " + item + price, "wtb smithing mats " + item,
                        "need metal stock " + item + price, "ore buyer " + item)
                        : pickLine("selling smithing mats " + item + price, "wts ores bars " + item,
                        "metal tab " + item, "smithing stock " + item + price);
            case FLETCHING_SUPPLIER:
                return buying ? pickLine("buying logs bows " + item + price, "wtb fletching mats " + item,
                        "need shafts strings " + item, "buying ammo bits " + item + price)
                        : pickLine("selling fletching stuff " + item + price, "wts bows ammo " + item,
                        "logs and strings " + item, "fletch stock " + item + price);
            case CRAFTING_JEWELER:
                return buying ? pickLine("buying gems jewelry " + item + price, "wtb crafting mats " + item,
                        "need hides gems " + item, "jeweler buying " + item + price)
                        : pickLine("selling crafting stuff " + item + price, "wts gems jewelry " + item,
                        "rings ammies hides " + item, "crafting tab " + item + price);
            case FARMING_SUPPLIER:
                return buying ? pickLine("buying seeds produce " + item + price, "wtb farm stock " + item,
                        "need farming bits " + item, "buying herbs seeds " + item + price)
                        : pickLine("selling farm stock " + item + price, "wts seeds produce " + item,
                        "farming tab " + item, "herbs and seeds " + item + price);
            case SUMMONING_SUPPLIER:
                return buying ? pickLine("buying charms pouches " + item + price, "wtb summoning bits " + item,
                        "need pouch mats " + item, "buying shards charms " + item + price)
                        : pickLine("selling summoning mats " + item + price, "wts pouches charms " + item,
                        "summoning tab " + item, "pouch supplies " + item + price);
            case PVP_RESTOCKER:
                return buying ? pickLine("buying pvp supplies " + item + price, "wtb pk gear " + item,
                        "need " + item + price) : pickLine("selling pk stuff " + item + price,
                        "wts pvp restock " + item + price, "risk supplies " + item);
            case PVM_SUPPLY_RUNNER:
                return buying ? pickLine("buying pvm supplies " + item + price, "need boss stock " + item,
                        "restocking trips " + item + price, "wtb raid supplies " + item)
                        : pickLine("selling pvm supplies " + item + price, "boss trip stock " + item,
                        "wts supplies " + item + price, "pvm restock " + item);
            case CLUE_RARE_COLLECTOR:
                return buying ? pickLine("buying rares " + item + price, "collector buying " + item,
                        "paying for " + item + price) : pickLine("selling spare loot " + item + price,
                        "taking offers " + item, "wts " + item + price);
            case HIGH_TIER_GEAR_TRADER:
                return buying ? pickLine("buying high tier " + item + price, "wtb boss gear " + item,
                        "paying for rare gear " + item, "looking at " + item + price)
                        : pickLine("selling high tier " + item + price, "wts boss gear " + item,
                        "taking serious offers " + item, "gear tab " + item + price);
            case SKILLING_BULK_BUYER:
                return buying ? pickLine("bulk buying " + item + price, "wtb skilling mats " + item,
                        "buying " + item + quick + price) : pickLine("selling skiller stuff " + item + price,
                        "wts mats " + item + price, "bank sale " + item);
            case BOSS_LOOT_DUMPER:
                return buying ? pickLine("buying boss supplies " + item + price, "need pvm stuff " + item,
                        "restocking " + item + price) : pickLine("dumping loot " + item + price,
                        "wts boss loot " + item + price, "loot tab selling " + item);
            case BROKE_NEWCOMER:
                return buying ? pickLine("buying cheap " + item, "wtb " + item + price,
                        "need " + item) : pickLine("selling spare " + item + price,
                        "wts " + item + price, "selling my " + item);
            case IMPATIENT_BANK_CLEARER:
                return buying ? pickLine("buying quick " + item + price, "wtb fast " + item,
                        "need " + item + " now") : pickLine("bank clear " + item + price,
                        "selling quick " + item + price, "wts " + item + " fast");
            default:
                return IdleCrowdBotScript.marketOfferLine(buying, item, priceEach);
        }
    }

    public synchronized BuyAdvertMatch matchPlayerSellingAdvert(String message) {
        String normalized = normalize(message);
        if (normalized.isEmpty() || !hasPlayerSellingIntent(normalized)) {
            return null;
        }
        int itemId = findMatchingItemId(buyLimits, normalized);
        if (itemId < 0) {
            return null;
        }
        int unitPrice = parseAdvertUnitPrice(normalized, itemId);
        int amount = parseAdvertAmount(normalized, itemId, unitPrice,
                Math.max(1, buyLimits.containsKey(itemId) ? buyLimits.get(itemId) : 1), false);
        int quote = quoteBuyPrice(itemId, amount);
        if (quote < 0 || exceedsTradeLimit(quote)) {
            return null;
        }
        if (unitPrice > 0) {
            int botUnitPrice = getBuyPrice(itemId, amount);
            long maxReasonable = ((long) botUnitPrice * (BPS + CHAT_BUY_TOLERANCE_BPS)) / BPS;
            if (unitPrice > maxReasonable || exceedsTradeLimit(cappedTotal(unitPrice, amount))) {
                return null;
            }
        }
        return new BuyAdvertMatch(itemId, amount, unitPrice,
                playerSellingResponse(itemId, amount, unitPrice));
    }

    public synchronized BuyAdvertMatch matchPlayerBuyingAdvert(String message) {
        String normalized = normalize(message);
        if (normalized.isEmpty() || !hasPlayerBuyingIntent(normalized)) {
            return null;
        }
        int itemId = findMatchingItemId(sellStock, normalized);
        if (itemId < 0) {
            return null;
        }
        Integer stock = sellStock.get(itemId);
        if (stock == null || stock <= 0) {
            return null;
        }
        int unitPrice = parseAdvertUnitPrice(normalized, itemId);
        int amount = Math.max(1, Math.min(stock, parseAdvertAmount(normalized, itemId, unitPrice,
                stock, true)));
        int botSellPrice = getSellPrice(itemId, amount, 0);
        int totalSellValue = cappedTotal(botSellPrice, amount);
        if (totalSellValue <= 0 || exceedsTradeLimit(totalSellValue)) {
            return null;
        }
        if (unitPrice > 0) {
            long minReasonable = ((long) botSellPrice * (BPS - CHAT_BUY_TOLERANCE_BPS)) / BPS;
            if (unitPrice < minReasonable) {
                return null;
            }
        }
        String request = amount > 1 ? formatAmount(amount) + " " + itemName(itemId) : itemName(itemId);
        return new BuyAdvertMatch(itemId, amount, unitPrice,
                playerBuyingResponse(itemId, amount, unitPrice), request);
    }

    private String playerSellingResponse(int itemId, int amount, int unitPrice) {
        String item = itemName(itemId).toLowerCase();
        String qty = amount > 1 ? formatAmount(amount) + " " : "";
        if (unitPrice > 0) {
            switch (style) {
                case HARD_NEGOTIATOR:
                case SCAM_SENSITIVE:
                    return pickLine("Trade me, I'll check " + item + ".",
                            "I'll look, no swaps.", "Show me the " + item + ".");
                case DESPERATE_BUYER:
                case BULK_BUYER:
                    return pickLine("Yeah, bring the " + item + ".",
                            "I'll buy " + qty + item + ".", "Trade me with the " + item + ".");
                default:
                    return pickLine("Trade me, I'll take a look.",
                            "Show me " + qty + item + ".", "I'll check the price.");
            }
        }
        switch (specialty) {
            case FISHMONGER:
                return pickLine("Got fish? Trade me.", "I'll price the " + item + ".", "Bring the catch here.");
            case RUNE_MERCHANT:
                return pickLine("Runes? Trade me.", "I'll count the " + item + ".", "Show me the rune stack.");
            case SMITHING_SUPPLIER:
                return pickLine("Metal stock? Trade me.", "I'll look at the " + item + ".", "Show me the bars or ores.");
            case CRAFTING_JEWELER:
                return pickLine("Gems or jewelry? Trade me.", "I'll look at the " + item + ".", "Show me the craft stock.");
            case HIGH_TIER_GEAR_TRADER:
            case CLUE_RARE_COLLECTOR:
                return pickLine("Show me, serious offers only.", "Trade me, I'll inspect it.", "I'll look at the " + item + ".");
            default:
                return pickLine("Trade me, I'll make an offer.", "Show me the " + item + ".", "I'll take a look.");
        }
    }

    private String playerBuyingResponse(int itemId, int amount, int unitPrice) {
        String item = itemName(itemId).toLowerCase();
        String qty = amount > 1 ? formatAmount(amount) + " " : "";
        if (unitPrice > 0) {
            return pickLine("I've got " + qty + item + ". Trade me.",
                    "I can sell you " + item + ".", "Trade me for the " + item + ".");
        }
        switch (specialty) {
            case PVM_SUPPLY_RUNNER:
                return pickLine("I've got boss supplies.", "I can sell " + item + ".", "Trade me, I've got stock.");
            case FISHMONGER:
                return pickLine("I've got food.", "I can sell " + item + ".", "Trade me for the fish.");
            case RUNE_MERCHANT:
                return pickLine("I've got runes.", "I can sell " + item + ".", "Trade me for mage stock.");
            case FLETCHING_SUPPLIER:
                return pickLine("I've got fletching stock.", "I can sell " + item + ".", "Trade me for bows or ammo.");
            case HIGH_TIER_GEAR_TRADER:
                return pickLine("I've got gear.", "I can show you " + item + ".", "Trade me, serious offers.");
            default:
                return pickLine("I've got " + item + ". Trade me.", "I can sell you " + item + ".", "Trade me.");
        }
    }

    private long getBuyTotal(int itemId, int amount) {
        return (long) getBuyPrice(itemId, amount) * amount;
    }

    private static int cappedTotal(int unitPrice, int amount) {
        if (unitPrice <= 0 || amount <= 0) {
            return -1;
        }
        long total = (long) unitPrice * amount;
        return total >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
    }

    private long getSellTotal(int itemId, int amount, int timePressureDiscountBps) {
        return (long) getSellPrice(itemId, amount, timePressureDiscountBps) * amount;
    }

    private int adjustedBuyBps(int itemId, int amount) {
        int bps = buyMultiplierBps + getBuyItemVarianceBps(itemId);
        switch (style) {
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
                bps -= 120;
                break;
            case DESPERATE_BUYER:
                bps += 180;
                break;
            case BULK_BUYER:
                bps += bulkPremiumBps(itemId, amount);
                break;
            case COLLECTOR:
                if (MarketItemCategory.isRareItem(itemId)) {
                    bps += 250;
                }
                break;
            default:
                if (amount >= 1000) {
                    bps -= 60;
                }
                break;
        }
        return MarketPriceGuard.guardedBuyBps(itemId, clampNormalBps(bps));
    }

    private int adjustedSellBps(int itemId, int amount, int timePressureDiscountBps) {
        int bps = sellMultiplierBps + getSellItemVarianceBps(itemId);
        switch (style) {
            case HARD_NEGOTIATOR:
            case SCAM_SENSITIVE:
                bps += 120;
                break;
            case IMPATIENT_SELLER:
                bps -= 80;
                break;
            case BULK_BUYER:
                bps -= bulkDiscountBps(itemId, amount);
                break;
            default:
                bps -= amount >= 500 ? 60 : 0;
                break;
        }
        bps += lowStockPremiumBps(itemId);
        bps -= Math.max(0, timePressureDiscountBps);
        return MarketPriceGuard.guardedSellBps(itemId, clampNormalBps(bps));
    }

    private int baseSellIntentBps(int itemId) {
        return clampNormalBps(sellMultiplierBps + getSellItemVarianceBps(itemId));
    }

    private int getBuyItemVarianceBps(int itemId) {
        Integer variance = buyItemVarianceBps.get(itemId);
        if (variance == null) {
            variance = rollItemVarianceBps();
            buyItemVarianceBps.put(itemId, variance);
        }
        return variance;
    }

    private int getSellItemVarianceBps(int itemId) {
        Integer variance = sellItemVarianceBps.get(itemId);
        if (variance == null) {
            variance = rollItemVarianceBps();
            sellItemVarianceBps.put(itemId, variance);
        }
        return variance;
    }

    private int rollItemVarianceBps() {
        int maxSteps = getItemVarianceMaxSteps();
        int roll = ThreadLocalRandom.current().nextInt(getItemVarianceWeight(maxSteps));
        int zeroWeight = maxSteps + 2;
        if (roll < zeroWeight) {
            return 0;
        }
        roll -= zeroWeight;
        for (int step = 1; step <= maxSteps; step++) {
            int weight = maxSteps + 1 - step;
            if (roll < weight) {
                return -step * 50;
            }
            roll -= weight;
            if (roll < weight) {
                return step * 50;
            }
            roll -= weight;
        }
        return 0;
    }

    private int getItemVarianceMaxSteps() {
        if (archetype == Archetype.NEWCOMER) {
            return 8;
        }
        if (archetype == Archetype.MERCHER || style == PersonalityStyle.COLLECTOR
                || style == PersonalityStyle.BULK_BUYER) {
            return 5;
        }
        return 6;
    }

    private static int getItemVarianceWeight(int maxSteps) {
        int total = maxSteps + 2;
        for (int step = 1; step <= maxSteps; step++) {
            total += (maxSteps + 1 - step) * 2;
        }
        return total;
    }

    private int bulkPremiumBps(int itemId, int amount) {
        if (amount >= 1000) return 180;
        if (amount >= 250) return 110;
        if (amount >= 50 && isStackable(itemId)) return 60;
        return 0;
    }

    private int bulkDiscountBps(int itemId, int amount) {
        if (!isStackable(itemId)) {
            return amount >= 3 ? 60 : 0;
        }
        if (amount >= 1000) return 220;
        if (amount >= 250) return 140;
        if (amount >= 50) return 70;
        return 0;
    }

    private int lowStockPremiumBps(int itemId) {
        Integer current = sellStock.get(itemId);
        Integer initial = initialSellStock.get(itemId);
        if (current == null || initial == null || initial <= 0) {
            return 0;
        }
        int ratioBps = (int) (((long) current * BPS) / initial);
        if (ratioBps <= 2000) return 240;
        if (ratioBps <= 5000) return 110;
        return 0;
    }

    private int chooseBundleAmount(int itemId, int stock, long targetValue) {
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(itemId);
        int price = Math.max(1, getSellPrice(itemId));
        if (definitions == null || (!definitions.isStackable() && !definitions.isNoted()
                && !shouldOfferNoted(itemId))) {
            return Math.min(stock, style == PersonalityStyle.BULK_BUYER ? rand(1, Math.min(3, stock)) : 1);
        }
        int amountByValue = (int) Math.max(1, Math.min(Integer.MAX_VALUE, targetValue / price));
        int cap = Math.max(1, Math.min(stock, amountByValue));
        if (style != PersonalityStyle.BULK_BUYER && cap > 100 && ThreadLocalRandom.current().nextInt(100) < 35) {
            cap = rand(8, Math.max(8, Math.min(cap, 120)));
        }
        int amount = rand(1, cap);
        if (style == PersonalityStyle.BULK_BUYER && stock > amount) {
            amount = Math.min(stock, Math.max(amount, amount * 2));
        }
        if (amount > 20 && ThreadLocalRandom.current().nextInt(100) < 55) {
            int chip = ThreadLocalRandom.current().nextInt(0, Math.max(1, amount / 12) + 1);
            amount = Math.max(1, amount - chip);
        }
        return Math.max(1, Math.min(stock, amount));
    }

    private void addBuyCategories(MarketItemCategory... categories) {
        int minEntries = archetype == Archetype.NEWCOMER ? 5 : 6;
        int maxEntries = archetype == Archetype.NEWCOMER ? 7 : style == PersonalityStyle.COLLECTOR ? 8 : 9;
        for (MarketItemCategory category : categories) {
            addBuyTable(category.roll(minEntries, maxEntries));
        }
    }

    private void addSellCategories(MarketItemCategory... categories) {
        int minEntries = archetype == Archetype.NEWCOMER ? 5 : 6;
        int maxEntries = archetype == Archetype.NEWCOMER ? 7 : 9;
        for (MarketItemCategory category : categories) {
            addSellTable(category.roll(minEntries, maxEntries));
        }
    }

    private void limitBuyInterests() {
        int maxEntries = maxBuyInterestEntries();
        if (buyLimits.size() <= maxEntries) {
            return;
        }
        int minEntries = Math.min(minBuyInterestEntries(), maxEntries);
        int targetEntries = rand(minEntries, maxEntries);
        List<Integer> ids = new ArrayList<>(buyLimits.keySet());
        final Map<Integer, Integer> scores = new HashMap<>();
        for (Integer itemId : ids) {
            if (itemId != null) {
                scores.put(itemId, buyInterestRetentionScore(itemId));
            }
        }
        Collections.sort(ids, new Comparator<Integer>() {
            @Override
            public int compare(Integer first, Integer second) {
                int firstScore = scores.containsKey(first) ? scores.get(first) : 0;
                int secondScore = scores.containsKey(second) ? scores.get(second) : 0;
                if (firstScore != secondScore) {
                    return Integer.compare(secondScore, firstScore);
                }
                return first.compareTo(second);
            }
        });
        for (int index = targetEntries; index < ids.size(); index++) {
            Integer itemId = ids.get(index);
            buyLimits.remove(itemId);
            initialBuyLimits.remove(itemId);
            buyItemVarianceBps.remove(itemId);
        }
    }

    private void retainBuyCategories(MarketItemCategory... categories) {
        retainCategories(buyLimits, categories);
        retainCategories(initialBuyLimits, categories);
        retainCategories(buyItemVarianceBps, categories);
    }

    private void retainSellCategories(MarketItemCategory... categories) {
        retainCategories(sellStock, categories);
        retainCategories(initialSellStock, categories);
        retainCategories(sellItemVarianceBps, categories);
    }

    private static void retainCategories(Map<Integer, Integer> items, MarketItemCategory... categories) {
        for (Integer itemId : new ArrayList<>(items.keySet())) {
            if (!MarketItemCategory.containsItem(itemId, categories)) {
                items.remove(itemId);
            }
        }
    }

    private void addBuyTable(Map<Integer, Integer> items) {
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            addMarketItem(buyLimits, entry.getKey(), entry.getValue());
            addMarketItem(initialBuyLimits, entry.getKey(), entry.getValue());
        }
    }

    private void addSellTable(Map<Integer, Integer> items) {
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            addMarketItem(sellStock, entry.getKey(), entry.getValue());
            addMarketItem(initialSellStock, entry.getKey(), entry.getValue());
        }
    }

    private void removeOverlappingSellItems() {
        // Merchers exist precisely to buy and sell the same items. Their
        // buy multiplier (rand 9500-9900) is well below their sell multiplier
        // (rand 10100-10500), so the spread prevents arbitrage on its own.
        // Stripping overlap here would leave most merchers with empty sell stock.
        if (archetype == Archetype.MERCHER || isOverlapAllowedSpecialty()) {
            return;
        }
        for (Integer itemId : new ArrayList<>(buyLimits.keySet())) {
            if (sellStock.remove(itemId) != null) {
                initialSellStock.remove(itemId);
                sellItemVarianceBps.remove(itemId);
            }
        }
    }

    private void limitSellStock() {
        int maxEntries = maxSellStockEntries();
        if (sellStock.size() <= maxEntries) {
            return;
        }
        int minEntries = Math.min(minSellStockEntries(), maxEntries);
        int targetEntries = rand(minEntries, maxEntries);
        List<Integer> ids = new ArrayList<>(sellStock.keySet());
        final Map<Integer, Integer> scores = new HashMap<>();
        for (Integer itemId : ids) {
            if (itemId != null) {
                scores.put(itemId, sellStockRetentionScore(itemId));
            }
        }
        Collections.sort(ids, new Comparator<Integer>() {
            @Override
            public int compare(Integer first, Integer second) {
                int firstScore = scores.containsKey(first) ? scores.get(first) : 0;
                int secondScore = scores.containsKey(second) ? scores.get(second) : 0;
                if (firstScore != secondScore) {
                    return Integer.compare(secondScore, firstScore);
                }
                return first.compareTo(second);
            }
        });
        for (int index = targetEntries; index < ids.size(); index++) {
            Integer itemId = ids.get(index);
            sellStock.remove(itemId);
            initialSellStock.remove(itemId);
            sellItemVarianceBps.remove(itemId);
        }
    }

    private int minSellStockEntries() {
        if (theme == InventoryTheme.BANK_SALE || specialty == TraderSpecialty.IMPATIENT_BANK_CLEARER) {
            return 60;
        }
        switch (archetype) {
            case NEWCOMER:
                return 15;
            case RESOURCE_BUYER:
            case COLLECTOR:
                return 20;
            case PKER:
            case BOSSER:
                return 28;
            case MERCHER:
                return 44;
            case SKILLER:
            case SUPPLY_TRADER:
            default:
                return 34;
        }
    }

    private int maxSellStockEntries() {
        if (theme == InventoryTheme.BANK_SALE || specialty == TraderSpecialty.IMPATIENT_BANK_CLEARER) {
            return 90;
        }
        switch (archetype) {
            case NEWCOMER:
                return 25;
            case RESOURCE_BUYER:
            case COLLECTOR:
                return 34;
            case PKER:
            case BOSSER:
                return 46;
            case MERCHER:
                return 70;
            case SKILLER:
            case SUPPLY_TRADER:
            default:
                return 56;
        }
    }

    private int minBuyInterestEntries() {
        switch (archetype) {
            case NEWCOMER:
                return 14;
            case RESOURCE_BUYER:
                return 30;
            case COLLECTOR:
                return 18;
            case MERCHER:
                return 38;
            case PKER:
            case BOSSER:
            case SKILLER:
            case SUPPLY_TRADER:
            default:
                return 26;
        }
    }

    private int maxBuyInterestEntries() {
        switch (archetype) {
            case NEWCOMER:
                return 24;
            case RESOURCE_BUYER:
                return 50;
            case COLLECTOR:
                return 30;
            case MERCHER:
                return 60;
            case PKER:
            case BOSSER:
            case SKILLER:
            case SUPPLY_TRADER:
            default:
                return 44;
        }
    }

    private int buyInterestRetentionScore(int itemId) {
        int score = ThreadLocalRandom.current().nextInt(1000);
        if (isThemeBuyItem(itemId)) {
            score += 4500;
        }
        Integer limit = buyLimits.get(itemId);
        if (limit != null) {
            score += Math.min(1800, Math.max(0, limit));
        }
        if (MarketItemCategory.isStapleItem(itemId)) {
            score += 1100;
        }
        int price = Math.max(1, GrandExchange.getPrice(itemId));
        if (price <= 25_000) {
            score += 650;
        } else if (price <= 250_000) {
            score += 350;
        } else if (price >= 5_000_000 && archetype != Archetype.COLLECTOR
                && specialty != TraderSpecialty.HIGH_TIER_GEAR_TRADER
                && specialty != TraderSpecialty.CLUE_RARE_COLLECTOR) {
            score -= 900;
        }
        if (MarketItemCategory.isHighTierItem(itemId)
                && archetype != Archetype.COLLECTOR
                && specialty != TraderSpecialty.HIGH_TIER_GEAR_TRADER
                && specialty != TraderSpecialty.CLUE_RARE_COLLECTOR) {
            score -= 650;
        }
        if (MarketItemCategory.isRareItem(itemId)) {
            if (archetype == Archetype.COLLECTOR || specialty == TraderSpecialty.CLUE_RARE_COLLECTOR) {
                score += 2200;
            } else {
                score -= 2200;
            }
        }
        return score;
    }

    private int sellStockRetentionScore(int itemId) {
        int score = ThreadLocalRandom.current().nextInt(1000);
        if (isThemeSellItem(itemId)) {
            score += 5000;
        }
        Integer stock = sellStock.get(itemId);
        if (stock != null) {
            score += Math.min(2000, Math.max(0, stock));
        }
        int price = Math.max(1, GrandExchange.getPrice(itemId));
        if (price <= 10_000) {
            score += 800;
        } else if (price >= 5_000_000 && archetype != Archetype.COLLECTOR
                && specialty != TraderSpecialty.HIGH_TIER_GEAR_TRADER
                && specialty != TraderSpecialty.CLUE_RARE_COLLECTOR) {
            score -= 600;
        }
        if (MarketItemCategory.isStapleItem(itemId)) {
            score += 1000;
        }
        if (MarketItemCategory.isHighTierItem(itemId)
                && archetype != Archetype.COLLECTOR
                && specialty != TraderSpecialty.HIGH_TIER_GEAR_TRADER
                && specialty != TraderSpecialty.CLUE_RARE_COLLECTOR) {
            score -= 650;
        }
        if (MarketItemCategory.isRareItem(itemId)) {
            if (archetype == Archetype.COLLECTOR || specialty == TraderSpecialty.CLUE_RARE_COLLECTOR) {
                score += 3000;
            } else {
                score -= 1200;
            }
        }
        return score;
    }

    private boolean isOverlapAllowedSpecialty() {
        switch (specialty) {
            case POTION_FLIPPER:
            case HERB_RUNNER:
            case FISHMONGER:
            case RUNE_MERCHANT:
            case SMITHING_SUPPLIER:
            case FLETCHING_SUPPLIER:
            case CRAFTING_JEWELER:
            case FARMING_SUPPLIER:
            case SUMMONING_SUPPLIER:
            case PVP_RESTOCKER:
            case PVM_SUPPLY_RUNNER:
            case CLUE_RARE_COLLECTOR:
            case HIGH_TIER_GEAR_TRADER:
            case SKILLING_BULK_BUYER:
                return true;
            default:
                return false;
        }
    }

    private void registerGuardrails() {
        for (Integer itemId : sellStock.keySet()) {
            MarketPriceGuard.registerSellIntent(itemId, baseSellIntentBps(itemId));
        }
    }

    private static void addMarketItem(Map<Integer, Integer> table, int itemId, int amount) {
        if (amount <= 0 || itemId == COINS || !MarketItemCategory.isMarketItem(itemId)) {
            return;
        }
        increment(table, itemId, amount);
    }

    static int toTradeOfferItemId(int itemId, int amount) {
        if (amount <= 1 || !shouldOfferNoted(itemId)) {
            return itemId;
        }
        int noteId = getNoteId(itemId);
        return noteId == NO_NOTE_ID ? itemId : noteId;
    }

    static int toStockItemId(int itemId) {
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(itemId);
        if (definitions != null && definitions.isNoted() && definitions.getCertId() > 0) {
            return definitions.getCertId();
        }
        return itemId;
    }

    private static boolean shouldOfferNoted(int itemId) {
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(itemId);
        return definitions != null
                && !definitions.isStackable()
                && !definitions.isNoted()
                && MarketItemCategory.containsItem(itemId,
                        MarketItemCategory.FOOD, MarketItemCategory.RAW_FISH,
                        MarketItemCategory.LOGS, MarketItemCategory.ORES,
                        MarketItemCategory.BARS, MarketItemCategory.HERBS,
                        MarketItemCategory.POTIONS, MarketItemCategory.SUPPLIES,
                        MarketItemCategory.SKILLING_INPUTS, MarketItemCategory.SHORTBOWS,
                        MarketItemCategory.GEMS, MarketItemCategory.BONES,
                        MarketItemCategory.FARMING_SEEDS, MarketItemCategory.FARMING_PRODUCTS,
                        MarketItemCategory.CRAFTING_INPUTS, MarketItemCategory.JEWELRY,
                        MarketItemCategory.SUMMONING_REAGENTS, MarketItemCategory.TALISMANS)
                && getNoteId(itemId) != NO_NOTE_ID;
    }

    private static int getNoteId(int itemId) {
        Integer cached = NOTE_ID_CACHE.get(itemId);
        if (cached != null) {
            return cached;
        }
        int likelyNoteId = itemId + 1;
        if (isNoteFor(likelyNoteId, itemId)) {
            NOTE_ID_CACHE.put(itemId, likelyNoteId);
            return likelyNoteId;
        }
        for (int noteId = 0; noteId < Utils.getItemDefinitionsSize(); noteId++) {
            if (isNoteFor(noteId, itemId)) {
                NOTE_ID_CACHE.put(itemId, noteId);
                return noteId;
            }
        }
        NOTE_ID_CACHE.put(itemId, NO_NOTE_ID);
        return NO_NOTE_ID;
    }

    private static boolean isNoteFor(int noteId, int itemId) {
        if (noteId <= 0 || noteId == itemId || !Utils.itemExists(noteId)) {
            return false;
        }
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(noteId);
        return definitions != null
                && definitions.isNoted()
                && definitions.getCertId() == itemId
                && ItemConstants.isTradeable(new Item(noteId, 1));
    }

    private static void increment(Map<Integer, Integer> table, int itemId, int amount) {
        Integer current = table.get(itemId);
        long next = (long) (current == null ? 0 : current) + amount;
        table.put(itemId, (int) Math.min(Integer.MAX_VALUE, Math.max(0, next)));
    }

    private static void decrement(Map<Integer, Integer> table, int itemId, int amount) {
        Integer current = table.get(itemId);
        if (current == null) {
            return;
        }
        int next = current - amount;
        if (next <= 0) {
            table.remove(itemId);
        } else {
            table.put(itemId, next);
        }
    }

    private static int scalePrice(int itemId, int multiplierBps) {
        int base = Math.max(1, GrandExchange.getPrice(itemId));
        long price = ((long) base * multiplierBps + (BPS / 2)) / BPS;
        if (price < 1) {
            return 1;
        }
        if (price > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) price;
    }

    private static int likelyBulkAmount(int itemId, int limit) {
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(itemId);
        if (definitions == null || (!definitions.isStackable() && !definitions.isNoted()
                && !shouldOfferNoted(itemId))) {
            return 1;
        }
        return Math.max(1, Math.min(limit, limit >= 1000 ? 1000 : limit >= 250 ? 250 : limit >= 50 ? 50 : limit));
    }

    private static boolean isBulkTradeItem(int itemId) {
        return isStackable(itemId) || shouldOfferNoted(itemId);
    }

    private static boolean isStackable(int itemId) {
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(itemId);
        return definitions != null && (definitions.isStackable() || definitions.isNoted());
    }

    private static int findMatchingItemId(Map<Integer, Integer> table, String text) {
        String normalized = normalize(text);
        ItemMention tableMention = findBestItemMention(table.keySet(), normalized);
        ItemMention marketMention = findBestItemMention(MARKET_ITEM_IDS, normalized);
        if (tableMention != null) {
            if (marketMention != null && marketMention.itemId != tableMention.itemId
                    && marketMention.termLength > tableMention.termLength) {
                return -1;
            }
            return tableMention.itemId;
        }
        if (marketMention != null) {
            return -1;
        }
        return findCategoryMatchingItemId(table, normalized);
    }

    private static ItemMention findBestItemMention(Iterable<Integer> itemIds, String normalized) {
        if (itemIds == null || normalized == null || normalized.isEmpty()) {
            return null;
        }
        ItemMention best = null;
        for (Integer itemId : itemIds) {
            if (itemId == null || itemId <= 0) {
                continue;
            }
            best = findBetterItemMention(best, itemId, itemName(itemId), normalized);
            String[] aliases = ITEM_ALIASES.get(itemId);
            if (aliases == null) {
                continue;
            }
            for (String alias : aliases) {
                best = findBetterItemMention(best, itemId, alias, normalized);
            }
        }
        return best;
    }

    private static ItemMention findBetterItemMention(ItemMention best, int itemId, String term, String normalized) {
        String normalizedTerm = normalize(term).replace(",", " ").replace(".", " ");
        if (normalizedTerm.isEmpty()) {
            return best;
        }
        int index = findTermIndex(normalized, normalizedTerm);
        if (index < 0) {
            return best;
        }
        int termLength = normalizedTerm.replace(" ", "").length();
        if (best == null || termLength > best.termLength
                || termLength == best.termLength && index < best.index) {
            return new ItemMention(itemId, termLength, index);
        }
        return best;
    }

    private static int findCategoryMatchingItemId(Map<Integer, Integer> table, String normalized) {
        if (table == null || table.isEmpty() || normalized == null || normalized.isEmpty()) {
            return -1;
        }
        List<MarketItemCategory> matchedCategories = new ArrayList<>();
        for (CategoryCue cue : CATEGORY_CUES) {
            if (cue.matches(normalized)) {
                Collections.addAll(matchedCategories, cue.categories);
            }
        }
        if (matchedCategories.isEmpty()) {
            return -1;
        }
        List<Integer> candidates = new ArrayList<>();
        MarketItemCategory[] categories = matchedCategories.toArray(new MarketItemCategory[matchedCategories.size()]);
        for (Integer itemId : table.keySet()) {
            if (itemId != null && MarketItemCategory.containsItem(itemId, categories)) {
                candidates.add(itemId);
            }
        }
        if (candidates.isEmpty()) {
            return -1;
        }
        Collections.shuffle(candidates);
        Collections.sort(candidates, new Comparator<Integer>() {
            @Override
            public int compare(Integer first, Integer second) {
                int firstAmount = table.containsKey(first) ? table.get(first) : 0;
                int secondAmount = table.containsKey(second) ? table.get(second) : 0;
                if (firstAmount != secondAmount) {
                    return Integer.compare(secondAmount, firstAmount);
                }
                int firstPrice = GrandExchange.getPrice(first);
                int secondPrice = GrandExchange.getPrice(second);
                return Integer.compare(secondPrice, firstPrice);
            }
        });
        int poolSize = Math.min(candidates.size(), 4);
        return candidates.get(ThreadLocalRandom.current().nextInt(poolSize));
    }

    static int parsePriceFromText(String text) {
        String[] words = normalize(text).split(" ");
        int explicitPrice = -1;
        int suffixedPrice = -1;
        int lastPrice = -1;
        for (int i = 0; i < words.length; i++) {
            int parsed = parseAmountToken(words[i]);
            if (parsed <= 0) {
                continue;
            }
            lastPrice = parsed;
            if (hasPriceSuffix(words[i])) {
                suffixedPrice = parsed;
            }
            if (isPriceToken(words, i) && !isAmountToken(words, i)) {
                explicitPrice = parsed;
            }
        }
        if (suffixedPrice > 0) {
            return suffixedPrice;
        }
        return explicitPrice > 0 ? explicitPrice : lastPrice;
    }

    private static int parseFirstAmount(String text, int fallback) {
        String[] words = normalize(text).split(" ");
        for (int i = 0; i < words.length; i++) {
            int amount = parseAmountToken(words[i]);
            if (amount <= 0) {
                continue;
            }
            // Skip tokens that are clearly prices, not amounts, so a request
            // like "buy sharks 500gp" doesn't get interpreted as "buy 500
            // sharks". The fallback path then lets chooseBundleAmount pick
            // a sensible quantity.
            if (hasPriceSuffix(words[i]) || isPriceToken(words, i)) {
                continue;
            }
            return amount;
        }
        return fallback;
    }

    private int parseAdvertAmount(String normalized, int itemId, int unitPrice,
            int maxAmount, boolean defaultBulkForStackables) {
        int limit = Math.max(1, maxAmount);
        int bulkFallback = Math.min(limit, likelyBulkAmount(itemId, limit));
        if (hasBulkAmountMarker(normalized) || hasVagueAmountMarker(normalized)) {
            return bulkFallback;
        }
        String[] words = normalized.split(" ");
        int itemIndex = findItemMentionIndex(itemId, normalized);
        int charOffset = 0;
        int best = -1;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int parsed = parseAmountToken(word);
            if (parsed > 0) {
                boolean priceToken = isPriceToken(words, i)
                        || (unitPrice > 0 && parsed == unitPrice && isLastNumericToken(words, i)
                        && (itemIndex < 0 || charOffset > itemIndex));
                if (!priceToken) {
                    best = parsed;
                    if (isAmountToken(words, i) || itemIndex < 0 || charOffset < itemIndex) {
                        break;
                    }
                }
            }
            charOffset += word.length() + 1;
        }
        if (best > 0) {
            return Math.max(1, Math.min(limit, best));
        }
        if (defaultBulkForStackables && isBulkTradeItem(itemId)) {
            return bulkFallback;
        }
        return 1;
    }

    private static int parseAdvertUnitPrice(String normalized, int itemId) {
        String[] words = normalized.split(" ");
        int itemIndex = findItemMentionIndex(itemId, normalized);
        int explicitPrice = -1;
        int lastPrice = -1;
        int lastPriceOffset = -1;
        int numericTokens = 0;
        int charOffset = 0;
        for (int i = 0; i < words.length; i++) {
            int parsed = parseAmountToken(words[i]);
            if (parsed > 0) {
                numericTokens++;
                lastPrice = parsed;
                lastPriceOffset = charOffset;
                if (isPriceToken(words, i)) {
                    explicitPrice = parsed;
                }
            }
            charOffset += words[i].length() + 1;
        }
        if (explicitPrice > 0) {
            return explicitPrice;
        }
        if (lastPrice > 0 && (numericTokens > 1 || itemIndex < 0 || lastPriceOffset > itemIndex)) {
            return lastPrice;
        }
        return -1;
    }

    private static boolean hasPlayerSellingIntent(String normalized) {
        if (isPlayerAskingToBuy(normalized) || hasNegatedSellIntent(normalized)) {
            return false;
        }
        // Bare "have"/"having" used to be in this list and caused false
        // positives on questions like "do you have any sharks?" — the player
        // was asking the bot, not advertising. Specific patterns like
        // "i have X" / "got X" still cover real adverts below.
        return containsWord(normalized, "selling")
                || containsWord(normalized, "sell")
                || containsWord(normalized, "sells")
                || containsWord(normalized, "wts")
                || (normalized.startsWith("got ") && !normalized.startsWith("got any "))
                || normalized.startsWith("got some ")
                || normalized.startsWith("got plenty ")
                || normalized.startsWith("i got ")
                || normalized.startsWith("i have ")
                || normalized.contains("for sale")
                || normalized.contains("bank sale")
                || normalized.contains("taking offers")
                || normalized.contains("need cash")
                || normalized.contains("want to sell")
                || normalized.contains("wanna sell")
                || normalized.contains("trying to sell")
                || normalized.contains("looking to sell")
                || normalized.contains("anyone buying")
                || normalized.contains("anyone buy")
                || normalized.contains("any buyers")
                || containsWord(normalized, "unloading")
                || containsWord(normalized, "offloading")
                || containsWord(normalized, "clearing")
                || containsWord(normalized, "dumping");
    }

    private static boolean hasPlayerBuyingIntent(String normalized) {
        if (isPlayerAskingForBuyer(normalized) || isClearSellAdvert(normalized)
                || hasNegatedBuyIntent(normalized)) {
            return false;
        }
        return containsWord(normalized, "buying")
                || containsWord(normalized, "buy")
                || containsWord(normalized, "buys")
                || containsWord(normalized, "wtb")
                || containsWord(normalized, "lf")
                || containsWord(normalized, "lfb")
                || containsWord(normalized, "need")
                || containsWord(normalized, "needing")
                || normalized.contains("looking for")
                || normalized.contains("looking to buy")
                || normalized.contains("want to buy")
                || normalized.contains("wanna buy")
                || normalized.contains("trying to buy")
                || normalized.contains("paying for")
                || normalized.contains("anyone selling")
                || normalized.contains("anyone got")
                || normalized.contains("anyone have")
                || normalized.contains("who has")
                || normalized.contains("who got")
                || normalized.contains("can i buy")
                || hasAskingForStockPattern(normalized);
    }

    private static boolean isPlayerAskingToBuy(String normalized) {
        if (isPlayerAskingForBuyer(normalized)) {
            return false;
        }
        return containsWord(normalized, "buying")
                || containsWord(normalized, "buy")
                || containsWord(normalized, "buys")
                || containsWord(normalized, "wtb")
                || containsWord(normalized, "lf")
                || containsWord(normalized, "lfb")
                || containsWord(normalized, "needing")
                || normalized.contains("looking for")
                || normalized.contains("looking to buy")
                || normalized.contains("want to buy")
                || normalized.contains("wanna buy")
                || normalized.contains("trying to buy")
                || normalized.contains("paying for")
                || normalized.contains("anyone selling")
                || normalized.contains("anyone got")
                || normalized.contains("anyone have")
                || normalized.contains("who has")
                || normalized.contains("who got")
                || normalized.contains("can i buy")
                || hasAskingForStockPattern(normalized);
    }

    /**
     * Question forms where the player is asking whether someone has stock —
     * effectively a buy-side advert. Kept separate from the bare keyword list
     * so it's easy to extend without inflating the regex.
     */
    private static boolean hasAskingForStockPattern(String normalized) {
        return normalized.contains("do you have")
                || normalized.contains("you got")
                || normalized.contains("u got")
                || normalized.contains("ya got")
                || normalized.contains("got any")
                || normalized.contains("you sell")
                || normalized.contains("u sell")
                || normalized.contains("do you sell");
    }

    private static boolean isPlayerAskingForBuyer(String normalized) {
        return normalized.contains("anyone buying")
                || normalized.contains("anyone buy")
                || normalized.contains("any buyers")
                || normalized.contains("who buys")
                || normalized.contains("who is buying")
                || normalized.contains("who's buying");
    }

    private static boolean isClearSellAdvert(String normalized) {
        if (isPlayerAskingToBuy(normalized) || hasNegatedSellIntent(normalized)) {
            return false;
        }
        return containsWord(normalized, "selling")
                || containsWord(normalized, "sell")
                || containsWord(normalized, "sells")
                || containsWord(normalized, "wts")
                || containsWord(normalized, "unloading")
                || containsWord(normalized, "offloading")
                || containsWord(normalized, "clearing")
                || containsWord(normalized, "dumping")
                || normalized.contains("for sale")
                || normalized.contains("bank sale")
                || normalized.contains("taking offers")
                || normalized.contains("need cash")
                || normalized.contains("want to sell")
                || normalized.contains("wanna sell")
                || normalized.contains("trying to sell")
                || normalized.contains("looking to sell");
    }

    /**
     * "not selling", "don t sell", "won t sell" etc. Apostrophes are
     * stripped by {@link #normalize(String)} so contractions show up as
     * two words separated by a space (e.g. "don t").
     */
    private static boolean hasNegatedSellIntent(String normalized) {
        return normalized.contains("not selling")
                || normalized.contains("not sell ")
                || normalized.contains("don t sell")
                || normalized.contains("dont sell")
                || normalized.contains("won t sell")
                || normalized.contains("wont sell")
                || normalized.contains("didn t sell")
                || normalized.contains("didnt sell")
                || normalized.contains("no longer selling")
                || normalized.contains("stopped selling")
                || normalized.contains("not for sale");
    }

    private static boolean hasNegatedBuyIntent(String normalized) {
        return normalized.contains("not buying")
                || normalized.contains("not buy ")
                || normalized.contains("don t buy")
                || normalized.contains("dont buy")
                || normalized.contains("won t buy")
                || normalized.contains("wont buy")
                || normalized.contains("didn t buy")
                || normalized.contains("didnt buy")
                || normalized.contains("no longer buying")
                || normalized.contains("stopped buying")
                || normalized.contains("can t buy")
                || normalized.contains("cant buy");
    }

    private static boolean hasBulkAmountMarker(String normalized) {
        return containsWord(normalized, "all")
                || containsWord(normalized, "bulk")
                || containsWord(normalized, "noted")
                || containsWord(normalized, "stack")
                || containsWord(normalized, "lot")
                || normalized.contains("bank sale");
    }

    private static boolean hasVagueAmountMarker(String normalized) {
        return containsWord(normalized, "some")
                || containsWord(normalized, "few")
                || containsWord(normalized, "several")
                || containsWord(normalized, "bunch")
                || containsWord(normalized, "loads")
                || containsWord(normalized, "many")
                || normalized.contains("a lot")
                || normalized.contains("lots of");
    }

    private static boolean containsWord(String text, String word) {
        return (" " + text + " ").contains(" " + word + " ");
    }

    private static boolean containsPhrase(String text, String phrase) {
        String normalizedPhrase = normalize(phrase);
        return !normalizedPhrase.isEmpty()
                && (" " + text + " ").contains(" " + normalizedPhrase + " ");
    }

    private static boolean isPriceToken(String[] words, int index) {
        String word = words[index];
        if (word.contains("gp") || word.contains("ea")) {
            return true;
        }
        String previous = index > 0 ? words[index - 1] : "";
        String next = index + 1 < words.length ? words[index + 1] : "";
        return isPriceLeadingWord(previous) || isPriceTrailingWord(next);
    }

    private static boolean isPriceLeadingWord(String word) {
        return "for".equals(word) || "at".equals(word) || "price".equals(word)
                || "pc".equals(word) || "ask".equals(word) || "asking".equals(word)
                || "pay".equals(word) || "paying".equals(word) || "offer".equals(word)
                || "offers".equals(word) || "o".equals(word);
    }

    private static boolean isPriceTrailingWord(String word) {
        return "ea".equals(word) || "each".equals(word) || "gp".equals(word)
                || "per".equals(word);
    }

    private static boolean isAmountToken(String[] words, int index) {
        String previous = index > 0 ? words[index - 1] : "";
        String next = index + 1 < words.length ? words[index + 1] : "";
        return "x".equals(previous) || "qty".equals(previous) || "quantity".equals(previous)
                || "amount".equals(previous) || "x".equals(next);
    }

    private static boolean hasPriceSuffix(String word) {
        return word != null && (word.contains("gp") || word.contains("ea")
                || word.endsWith("k") || word.endsWith("m") || word.endsWith("b"));
    }

    private static boolean isLastNumericToken(String[] words, int index) {
        for (int i = index + 1; i < words.length; i++) {
            if (parseAmountToken(words[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    private static int findItemMentionIndex(int itemId, String normalized) {
        int best = findTermIndex(normalized, itemName(itemId));
        String[] aliases = ITEM_ALIASES.get(itemId);
        if (aliases != null) {
            for (String alias : aliases) {
                int index = findTermIndex(normalized, alias);
                if (index >= 0 && (best < 0 || index < best)) {
                    best = index;
                }
            }
        }
        return best;
    }

    private static int findTermIndex(String normalized, String term) {
        String searchable = normalize(normalized).replace(",", " ").replace(".", " ");
        String normalizedTerm = normalize(term).replace(",", " ").replace(".", " ");
        if (normalizedTerm.isEmpty()) {
            return -1;
        }
        String padded = " " + searchable + " ";
        int index = padded.indexOf(" " + normalizedTerm + " ");
        if (index >= 0) {
            return Math.max(0, index);
        }
        if (normalizedTerm.indexOf(' ') < 0) {
            return -1;
        }
        String compactText = searchable.replace(" ", "");
        String compactTerm = normalizedTerm.replace(" ", "");
        return compactTerm.length() < 3 ? -1 : compactText.indexOf(compactTerm);
    }

    private static int parseAmountToken(String word) {
        if (word == null || word.isEmpty()) {
            return -1;
        }
        // Strip suffix markers in order: thousands separators, then trailing
        // unit/price markers, then magnitude. Greedy substring stripping
        // (the previous behaviour) misparsed tokens like "100each" into
        // "100ch" and tokens like "great" lost their letters silently.
        String cleaned = word.replace(",", "");
        if (cleaned.endsWith("each")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        } else if (cleaned.endsWith("ea")) {
            cleaned = cleaned.substring(0, cleaned.length() - 2);
        }
        if (cleaned.endsWith("gp")) {
            cleaned = cleaned.substring(0, cleaned.length() - 2);
        }
        int multiplier = 1;
        if (cleaned.endsWith("k")) {
            multiplier = 1_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        } else if (cleaned.endsWith("m")) {
            multiplier = 1_000_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        } else if (cleaned.endsWith("b")) {
            multiplier = 1_000_000_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.isEmpty()) {
            return -1;
        }
        try {
            double value = Double.parseDouble(cleaned);
            if (value <= 0) {
                return -1;
            }
            double total = value * multiplier;
            return total >= Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, (int) total);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9., ]", " ").replaceAll("\\s+", " ").trim();
        String padded = " " + normalized + " ";
        padded = padded.replace(" sellign ", " selling ");
        padded = padded.replace(" sellin ", " selling ");
        padded = padded.replace(" sellling ", " selling ");
        padded = padded.replace(" salling ", " selling ");
        padded = padded.replace(" buyign ", " buying ");
        padded = padded.replace(" buyingg ", " buying ");
        padded = padded.replace(" addy ", " adamant ");
        padded = padded.replace(" addies ", " adamant ");
        padded = padded.replace(" mith ", " mithril ");
        padded = padded.replace(" miths ", " mithril ");
        padded = padded.replace(" d'hide ", " dragonhide ");
        padded = padded.replace(" dhide ", " dragonhide ");
        padded = padded.replace(" blk ", " black ");
        padded = padded.replace(" hides ", " hide ");
        padded = padded.replace(" leathers ", " leather ");
        padded = padded.replace(" rune bars ", " runite bar ");
        padded = padded.replace(" rune ores ", " runite ore ");
        padded = padded.replace(" nats ", " nature rune ");
        padded = padded.replace(" nat ", " nature rune ");
        padded = padded.replace(" deaths ", " death rune ");
        padded = padded.replace(" death runes ", " death rune ");
        padded = padded.replace(" bloods ", " blood rune ");
        padded = padded.replace(" blood runes ", " blood rune ");
        padded = padded.replace(" laws ", " law rune ");
        padded = padded.replace(" law runes ", " law rune ");
        padded = padded.replace(" cosmics ", " cosmic rune ");
        padded = padded.replace(" cosmic runes ", " cosmic rune ");
        padded = padded.replace(" souls ", " soul rune ");
        padded = padded.replace(" soul runes ", " soul rune ");
        padded = padded.replace(" astrals ", " astral rune ");
        padded = padded.replace(" astral runes ", " astral rune ");
        padded = padded.replace(" airs ", " air rune ");
        padded = padded.replace(" waters ", " water rune ");
        padded = padded.replace(" earths ", " earth rune ");
        padded = padded.replace(" fires ", " fire rune ");
        padded = padded.replace(" minds ", " mind rune ");
        padded = padded.replace(" chaos runes ", " chaos rune ");
        padded = padded.replace(" bowstrings ", " bowstring ");
        padded = padded.replace(" ppots ", " prayer potion ");
        padded = padded.replace(" ppot ", " prayer potion ");
        padded = padded.replace(" prayer pots ", " prayer potion ");
        padded = padded.replace(" brews ", " saradomin brew ");
        padded = padded.replace(" restores ", " super restore ");
        padded = padded.replace(" sharks ", " shark ");
        padded = padded.replace(" rocktails ", " rocktail ");
        padded = padded.replace(" monks ", " monkfish ");
        padded = padded.replace(" lobs ", " lobster ");
        padded = padded.replace(" lobsters ", " lobster ");
        padded = padded.replace(" ranarrs ", " ranarr ");
        padded = padded.replace(" snaps ", " snapdragon ");
        padded = padded.replace(" snap seeds ", " snapdragon seed ");
        padded = padded.replace(" torstols ", " torstol ");
        padded = padded.replace(" phats ", " partyhat ");
        padded = padded.replace(" phat ", " partyhat ");
        padded = padded.replace(" hweens ", " halloween ");
        return padded.replaceAll("\\s+", " ").trim();
    }

    private static CategoryCue[] createCategoryCues() {
        return new CategoryCue[] {
                cue(new MarketItemCategory[] {MarketItemCategory.FOOD, MarketItemCategory.RAW_FISH},
                        "food", "fish", "cooked fish", "raw fish", "sharks", "rocktails", "monks",
                        "lobs", "lobsters", "trout", "salmon"),
                cue(new MarketItemCategory[] {MarketItemCategory.RUNES, MarketItemCategory.TALISMANS},
                        "runes", "rune stack", "mage stock", "magic stock", "essence",
                        "pure essence", "talismans", "rc stuff"),
                cue(new MarketItemCategory[] {MarketItemCategory.LOGS, MarketItemCategory.SHORTBOWS},
                        "logs", "woodcutting", "wc supplies", "fletching", "bows",
                        "bowstrings", "bow string", "flax"),
                cue(new MarketItemCategory[] {MarketItemCategory.ORES, MarketItemCategory.BARS},
                        "ores", "ore", "bars", "bar", "smithing", "metal stock",
                        "mining supplies", "coal"),
                cue(new MarketItemCategory[] {MarketItemCategory.HERBS, MarketItemCategory.POTIONS,
                        MarketItemCategory.FARMING_PRODUCTS},
                        "herbs", "herb", "pots", "potions", "potion supplies",
                        "herblore", "seconds", "secondaries", "prayer pots", "brews", "restores"),
                cue(new MarketItemCategory[] {MarketItemCategory.FARMING_SEEDS,
                        MarketItemCategory.FARMING_PRODUCTS, MarketItemCategory.HERBS},
                        "seeds", "seed", "farming", "farm stock", "produce", "tree seeds"),
                cue(new MarketItemCategory[] {MarketItemCategory.CRAFTING_INPUTS,
                        MarketItemCategory.GEMS, MarketItemCategory.JEWELRY},
                        "crafting", "crafting mats", "hides", "leather", "dragon leather",
                        "gems", "jewelry", "jewellery", "rings", "necklaces", "ammies",
                        "amulets", "bracelets"),
                cue(new MarketItemCategory[] {MarketItemCategory.SUMMONING_REAGENTS,
                        MarketItemCategory.BONES},
                        "summoning", "pouches", "charms", "shards", "pouch mats",
                        "bones", "prayer bones", "ashes"),
                cue(new MarketItemCategory[] {MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.RANGED_GEAR},
                        "ammo", "arrows", "bolts", "darts", "knives", "range gear",
                        "ranged gear", "bows"),
                cue(new MarketItemCategory[] {MarketItemCategory.MELEE_GEAR,
                        MarketItemCategory.RANGED_GEAR, MarketItemCategory.MAGE_GEAR,
                        MarketItemCategory.PVP_GEAR},
                        "pk gear", "pvp gear", "risk gear", "melee gear", "mage gear",
                        "magic gear", "combat gear", "sets", "weapons", "armour", "armor"),
                cue(new MarketItemCategory[] {MarketItemCategory.SUPPLIES,
                        MarketItemCategory.POTIONS, MarketItemCategory.FOOD,
                        MarketItemCategory.RUNES, MarketItemCategory.RANGED_AMMO,
                        MarketItemCategory.SUMMONING_REAGENTS},
                        "supplies", "boss supplies", "pvm supplies", "restock", "trip supplies"),
                cue(new MarketItemCategory[] {MarketItemCategory.BOSS_LOOT,
                        MarketItemCategory.ADVANCED_GEAR, MarketItemCategory.JEWELRY},
                        "boss loot", "loot tab", "pvm loot", "high tier", "boss gear",
                        "nex gear", "gwd gear", "drygores", "nox", "noxious"),
                cue(new MarketItemCategory[] {MarketItemCategory.RARES},
                        "rares", "rare", "partyhats", "partyhat", "phats", "phat",
                        "santa", "hween", "halloween masks", "mask set")
        };
    }

    private static CategoryCue cue(MarketItemCategory[] categories, String... keywords) {
        return new CategoryCue(categories, keywords);
    }

    private static Map<Integer, String[]> createItemAliases() {
        Map<Integer, String[]> aliases = new HashMap<>();
        aliases.put(4151, new String[] {"whip", "abby whip"});
        aliases.put(11732, new String[] {"d boots", "dboots", "dragon boots"});
        aliases.put(4587, new String[] {"d scim", "dscim", "dragon scim"});
        aliases.put(5698, new String[] {"dds", "d dagger", "dragon dagger"});
        aliases.put(1215, new String[] {"dds", "d dagger", "dragon dagger"});
        aliases.put(1305, new String[] {"d long", "dragon long", "dragon longsword"});
        aliases.put(1377, new String[] {"d baxe", "dragon baxe", "dragon battleaxe"});
        aliases.put(1434, new String[] {"d mace", "dragon mace"});
        aliases.put(861, new String[] {"msb", "magic shortbow"});
        aliases.put(9185, new String[] {"rcb", "rune c bow", "rune crossbow"});
        aliases.put(11235, new String[] {"dbow", "dark bow"});
        aliases.put(2503, new String[] {"black dhide", "blk d hide", "black d'hide", "black dragonhide body"});
        aliases.put(2497, new String[] {"black dhide chaps", "black d'hide chaps", "blk chaps"});
        aliases.put(2491, new String[] {"black dhide vambs", "black d'hide vambs", "black vambs"});
        aliases.put(2434, new String[] {"pray pot", "prayer pot", "ppot"});
        aliases.put(3024, new String[] {"restore", "super restore", "restores", "s restore", "super restores"});
        aliases.put(6685, new String[] {"brew", "brews", "sara brew"});
        aliases.put(2440, new String[] {"super attack", "sup attack", "s attack"});
        aliases.put(2436, new String[] {"super strength", "sup strength", "s strength", "str pot"});
        aliases.put(2442, new String[] {"super defence", "super defense", "sup def", "def pot"});
        aliases.put(2444, new String[] {"range pot", "ranging pot", "ranging potion"});
        aliases.put(3040, new String[] {"magic pot", "mage pot", "magic potion"});
        aliases.put(1759, new String[] {"ball wool", "balls of wool", "wool"});
        aliases.put(1704, new String[] {"glory", "amulet glory", "amulet of glory"});
        aliases.put(6585, new String[] {"fury", "amulet fury", "amulet of fury"});
        aliases.put(6737, new String[] {"b ring", "berserker ring", "zerker ring"});
        aliases.put(6733, new String[] {"archer ring", "archers ring"});
        aliases.put(6731, new String[] {"seer ring", "seers ring"});
        aliases.put(6735, new String[] {"warrior ring"});
        aliases.put(15126, new String[] {"range ammy", "ranging ammy", "amulet of ranging"});
        aliases.put(11113, new String[] {"skills neck", "skill necklace", "skills necklace"});
        aliases.put(11126, new String[] {"combat brace", "combat bracelet"});
        aliases.put(12155, new String[] {"pouches", "summoning pouch", "summoning pouches"});
        aliases.put(12183, new String[] {"shards", "spirit shard", "spirit shards"});
        aliases.put(12158, new String[] {"gold charms", "gold charm"});
        aliases.put(12159, new String[] {"green charms", "green charm"});
        aliases.put(12160, new String[] {"crim charms", "crimson charms", "crimson charm"});
        aliases.put(12163, new String[] {"blue charms", "blue charm"});
        aliases.put(15272, new String[] {"rocktail", "rocktails"});
        aliases.put(385, new String[] {"shark", "sharks"});
        aliases.put(383, new String[] {"raw shark", "raw sharks"});
        aliases.put(7946, new String[] {"monkfish", "monks"});
        aliases.put(7944, new String[] {"raw monkfish", "raw monks"});
        aliases.put(379, new String[] {"lobster", "lobsters", "lobs"});
        aliases.put(377, new String[] {"raw lobster", "raw lobsters", "raw lobs"});
        aliases.put(373, new String[] {"swordfish", "swords"});
        aliases.put(371, new String[] {"raw swordfish", "raw swords"});
        aliases.put(333, new String[] {"trout"});
        aliases.put(335, new String[] {"raw trout"});
        aliases.put(329, new String[] {"salmon"});
        aliases.put(331, new String[] {"raw salmon"});
        aliases.put(361, new String[] {"tuna"});
        aliases.put(359, new String[] {"raw tuna"});
        aliases.put(1511, new String[] {"logs", "normal logs"});
        aliases.put(1521, new String[] {"oaks", "oak logs"});
        aliases.put(1519, new String[] {"willows", "willow logs"});
        aliases.put(1517, new String[] {"maples", "maple logs"});
        aliases.put(1515, new String[] {"yews", "yew logs"});
        aliases.put(1513, new String[] {"magics", "magic logs"});
        aliases.put(6333, new String[] {"teaks", "teak logs"});
        aliases.put(6332, new String[] {"mahogs", "mahogany logs"});
        aliases.put(1777, new String[] {"bowstring", "bowstrings", "bs"});
        aliases.put(1779, new String[] {"flax"});
        aliases.put(7936, new String[] {"p ess", "pure ess", "pure essence"});
        aliases.put(1436, new String[] {"rune ess", "ess", "rune essence"});
        aliases.put(554, new String[] {"fires", "fire rune"});
        aliases.put(555, new String[] {"waters", "water rune"});
        aliases.put(556, new String[] {"airs", "air rune"});
        aliases.put(557, new String[] {"earths", "earth rune"});
        aliases.put(558, new String[] {"minds", "mind rune"});
        aliases.put(559, new String[] {"bodies", "body rune"});
        aliases.put(562, new String[] {"chaos", "chaos rune"});
        aliases.put(563, new String[] {"laws", "law rune"});
        aliases.put(564, new String[] {"cosmics", "cosmic rune"});
        aliases.put(561, new String[] {"nats", "nat rune", "nature", "nature rune"});
        aliases.put(560, new String[] {"deaths", "death rune"});
        aliases.put(565, new String[] {"bloods", "blood rune"});
        aliases.put(566, new String[] {"souls", "soul rune"});
        aliases.put(9075, new String[] {"astrals", "astral rune"});
        aliases.put(892, new String[] {"rune arrows", "rune arrow"});
        aliases.put(11212, new String[] {"dragon arrows", "dragon arrow", "d arrows", "d arrow"});
        aliases.put(9244, new String[] {"dragon bolts e", "dragon bolts", "d bolts", "dbolts"});
        aliases.put(9243, new String[] {"diamond bolts e", "diamond bolts"});
        aliases.put(9144, new String[] {"rune bolts", "runite bolts"});
        aliases.put(868, new String[] {"rune knives", "rune knife"});
        aliases.put(811, new String[] {"rune darts", "rune dart"});
        aliases.put(447, new String[] {"mith ore", "mithril ore"});
        aliases.put(2359, new String[] {"mith bar", "mithril bar"});
        aliases.put(449, new String[] {"addy ore", "adamant ore", "adamantite ore"});
        aliases.put(2361, new String[] {"addy bar", "adamant bar"});
        aliases.put(451, new String[] {"rune ore", "runite ore"});
        aliases.put(2363, new String[] {"rune bar", "runite bar"});
        aliases.put(5300, new String[] {"snap seed", "snapdragon seed"});
        aliases.put(5304, new String[] {"torstol seed", "torstol seeds"});
        aliases.put(4716, new String[] {"dharok helm", "dharoks helm", "dh helm"});
        aliases.put(4718, new String[] {"dharok axe", "dharoks axe", "dh axe", "dharoks greataxe"});
        aliases.put(4720, new String[] {"dharok body", "dharoks body", "dh body"});
        aliases.put(4722, new String[] {"dharok legs", "dharoks legs", "dh legs"});
        aliases.put(4708, new String[] {"ahrim hood", "ahrims hood"});
        aliases.put(4712, new String[] {"ahrim top", "ahrims top", "ahrim robetop"});
        aliases.put(4714, new String[] {"ahrim skirt", "ahrims skirt", "ahrim bottom"});
        aliases.put(4732, new String[] {"karil coif", "karils coif"});
        aliases.put(4734, new String[] {"karil bow", "karils bow", "karil crossbow"});
        aliases.put(4736, new String[] {"karil top", "karils top", "karil leathertop"});
        aliases.put(4738, new String[] {"karil skirt", "karils skirt", "karil leatherskirt"});
        aliases.put(20135, new String[] {"torva helm", "torva full helm"});
        aliases.put(20139, new String[] {"torva body", "torva platebody"});
        aliases.put(20143, new String[] {"torva legs", "torva platelegs"});
        aliases.put(20147, new String[] {"pernix cowl"});
        aliases.put(20151, new String[] {"pernix body"});
        aliases.put(20155, new String[] {"pernix chaps"});
        aliases.put(20159, new String[] {"virtus mask"});
        aliases.put(20163, new String[] {"virtus top", "virtus robe top"});
        aliases.put(20167, new String[] {"virtus legs", "virtus robe legs"});
        aliases.put(11694, new String[] {"ags", "armadyl godsword"});
        aliases.put(11696, new String[] {"bgs", "bandos godsword"});
        aliases.put(11698, new String[] {"sgs", "saradomin godsword"});
        aliases.put(11700, new String[] {"zgs", "zamorak godsword"});
        aliases.put(11724, new String[] {"bcp", "bandos chest"});
        aliases.put(11726, new String[] {"tassets", "bandos tassets"});
        aliases.put(11283, new String[] {"dfs", "dragonfire shield"});
        aliases.put(11286, new String[] {"visage", "draconic visage"});
        aliases.put(14484, new String[] {"d claws", "dclaws", "dragon claws"});
        aliases.put(20171, new String[] {"z bow", "zaryte bow"});
        aliases.put(24338, new String[] {"royal cbow", "royal crossbow"});
        aliases.put(26579, new String[] {"drygore long", "drygore longsword", "drygores"});
        aliases.put(26583, new String[] {"drygore rapier", "drygores"});
        aliases.put(26587, new String[] {"drygore mace", "drygores"});
        aliases.put(31725, new String[] {"nox scythe", "noxious scythe"});
        aliases.put(31729, new String[] {"nox bow", "noxious bow", "nox longbow", "noxious longbow"});
        aliases.put(31733, new String[] {"nox staff", "noxious staff"});
        aliases.put(1050, new String[] {"santa", "santa hat"});
        aliases.put(29571, new String[] {"black santa", "black santa hat"});
        aliases.put(1038, new String[] {"red phat", "red partyhat"});
        aliases.put(1040, new String[] {"yellow phat", "yellow partyhat"});
        aliases.put(1042, new String[] {"blue phat", "blue partyhat"});
        aliases.put(1044, new String[] {"green phat", "green partyhat"});
        aliases.put(1046, new String[] {"purple phat", "purple partyhat"});
        aliases.put(1048, new String[] {"white phat", "white partyhat"});
        aliases.put(1053, new String[] {"green mask", "green hween", "green halloween"});
        aliases.put(1055, new String[] {"blue mask", "blue hween", "blue halloween"});
        aliases.put(1057, new String[] {"red mask", "red hween", "red halloween"});
        return aliases;
    }

    private static Archetype weightedRandom(Archetype... archetypes) {
        return archetypes[ThreadLocalRandom.current().nextInt(archetypes.length)];
    }

    private static int randomKey(Map<Integer, Integer> map) {
        int index = ThreadLocalRandom.current().nextInt(map.size());
        int count = 0;
        for (Integer key : map.keySet()) {
            if (count++ == index) {
                return key;
            }
        }
        return map.keySet().iterator().next();
    }

    private static int rand(int minInclusive, int maxInclusive) {
        if (maxInclusive <= minInclusive) {
            return minInclusive;
        }
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static int rollPriceMultiplierBps() {
        int roll = ThreadLocalRandom.current().nextInt(200);
        if (roll < 20) {
            return BPS;
        }
        roll -= 20;
        for (int percent = 1; percent <= 12; percent++) {
            int weight = 14 - percent; // 6.5% at 1%, stepping down to 1% at 12%.
            if (roll < weight) {
                return BPS - percent * 100;
            }
            roll -= weight;
            if (roll < weight) {
                return BPS + percent * 100;
            }
            roll -= weight;
        }
        return BPS;
    }

    private static String pickLine(String... lines) {
        if (lines == null || lines.length == 0) {
            return "";
        }
        return lines[ThreadLocalRandom.current().nextInt(lines.length)];
    }

    private static int clampNormalBps(int bps) {
        return Math.max(MIN_PRICE_MULTIPLIER_BPS, Math.min(MAX_PRICE_MULTIPLIER_BPS, bps));
    }

    private static String describeItems(Map<Integer, Integer> source, int limit) {
        if (source.isEmpty()) {
            return "nothing right now";
        }
        List<String> parts = new ArrayList<>();
        List<Integer> ids = new ArrayList<>(source.keySet());
        Collections.sort(ids, new Comparator<Integer>() {
            @Override
            public int compare(Integer first, Integer second) {
                int result = itemName(first).compareToIgnoreCase(itemName(second));
                return result != 0 ? result : first.compareTo(second);
            }
        });
        for (Integer itemId : ids) {
            parts.add(itemName(itemId));
            if (parts.size() >= limit) {
                break;
            }
        }
        return join(parts);
    }

    private static String percentVsGuide(int bps) {
        int diff = bps - BPS;
        if (Math.abs(diff) < 50) {
            return "at guide";
        }
        int percent = (Math.abs(diff) + 50) / 100;
        return percent + "% " + (diff > 0 ? "over guide" : "under guide");
    }

    private static String join(List<String> parts) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < parts.size(); index++) {
            if (index > 0) {
                builder.append(index == parts.size() - 1 ? " and " : ", ");
            }
            builder.append(parts.get(index));
        }
        return builder.toString();
    }

    private static String itemName(int itemId) {
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(itemId);
        return definitions == null ? "item " + itemId : definitions.getName();
    }

    private static String formatAmount(int amount) {
        return Utils.getFormattedNumber(amount);
    }

    public static final class RequestedBundle {
        private final List<Item> items;
        private final String message;

        private RequestedBundle(List<Item> items, String message) {
            this.items = items;
            this.message = message;
        }

        private static RequestedBundle ok(List<Item> items, String message) {
            return new RequestedBundle(items, message);
        }

        private static RequestedBundle fail(String message) {
            return new RequestedBundle(Collections.<Item>emptyList(), message);
        }

        public List<Item> getItems() {
            return items;
        }

        public String getMessage() {
            return message;
        }
    }

    private static final class CategoryCue {
        private final MarketItemCategory[] categories;
        private final String[] keywords;

        private CategoryCue(MarketItemCategory[] categories, String[] keywords) {
            this.categories = categories == null ? new MarketItemCategory[0] : categories;
            this.keywords = keywords == null ? new String[0] : keywords;
        }

        private boolean matches(String normalized) {
            for (String keyword : keywords) {
                if (containsPhrase(normalized, keyword)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class ItemMention {
        private final int itemId;
        private final int termLength;
        private final int index;

        private ItemMention(int itemId, int termLength, int index) {
            this.itemId = itemId;
            this.termLength = termLength;
            this.index = index;
        }
    }

    public static final class BuyAdvertMatch {
        private final int itemId;
        private final int amount;
        private final int unitPrice;
        private final String response;
        private final String requestText;

        private BuyAdvertMatch(int itemId, int amount, int unitPrice, String response) {
            this(itemId, amount, unitPrice, response, null);
        }

        private BuyAdvertMatch(int itemId, int amount, int unitPrice, String response, String requestText) {
            this.itemId = itemId;
            this.amount = amount;
            this.unitPrice = unitPrice;
            this.response = response;
            this.requestText = requestText;
        }

        public int getItemId() {
            return itemId;
        }

        public int getAmount() {
            return amount;
        }

        public int getUnitPrice() {
            return unitPrice;
        }

        public String getResponse() {
            return response;
        }

        public String getRequestText() {
            return requestText;
        }
    }
}
