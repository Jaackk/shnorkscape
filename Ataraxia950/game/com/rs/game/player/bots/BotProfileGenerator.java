package com.rs.game.player.bots;

import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates and applies "looks-like-a-real-account" profiles to bots:
 * realistic stat distribution + level-gated mix-and-match gear + a generated
 * display name.
 *
 * Inspired by 2009scape's AIPBuilder + AIPlayer.setLevels, but simplified and
 * targeted at this server's RS3-era item IDs and Skills enum.
 *
 * Usage:
 *     BotProfile profile = BotProfileGenerator.random();
 *     profile.apply(botPlayer);
 *
 * Or pick a tier yourself:
 *     BotProfile profile = BotProfileGenerator.forTier(Tier.HIGH);
 *
 * Equipment is set directly via Equipment.set(slot, item), but the generator
 * still filters every wearable pool against the bot's rolled levels first.
 */
public final class BotProfileGenerator {

    private BotProfileGenerator() {
    }

    public enum Tier {
        NEWBIE,    // combat 3-15  | mostly low skills, no real gear
        LOW,       // combat 15-40 | bronze/iron set, partial skills
        MID,       // combat 40-70 | steel/mithril set
        HIGH,      // combat 70-99 | adamant/rune mix, several 70+ skills
        MAXED      // combat 99    | rune/dragon, mostly 90+ skills
    }

    public enum OutfitPreference {
        DEFAULT,
        BANKSTANDER,
        TRADER,
        FASHIONSCAPE,
        MAXED_SHOWOFF,
        NEWBIE
    }

    /** Random tier with weighting biased toward mid-tier to feel realistic. */
    public static BotProfile random() {
        return forTier(rollTier());
    }

    public static BotProfile random(OutfitPreference preference) {
        return forTier(rollTier(preference), preference);
    }

    public static BotProfile forTier(Tier tier) {
        return forTier(tier, OutfitPreference.DEFAULT);
    }

    public static BotProfile forTier(Tier tier, OutfitPreference preference) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int[] levels = rollSkillLevels(tier, random);
        applyPreferenceLevels(levels, preference, random);
        int[] equipment = pickEquipmentSet(tier, levels, random, preference);
        String displayName = BotName.generate();
        return new BotProfile(displayName, tier, levels, equipment);
    }

    private static Tier rollTier() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 20) return Tier.NEWBIE;
        if (roll < 50) return Tier.LOW;
        if (roll < 75) return Tier.MID;
        if (roll < 92) return Tier.HIGH;
        return Tier.MAXED;
    }

    private static Tier rollTier(OutfitPreference preference) {
        if (preference == OutfitPreference.NEWBIE) {
            return Tier.NEWBIE;
        }
        if (preference == OutfitPreference.MAXED_SHOWOFF) {
            return Tier.MAXED;
        }
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (preference == OutfitPreference.TRADER) {
            if (roll < 8) return Tier.LOW;
            if (roll < 42) return Tier.MID;
            if (roll < 84) return Tier.HIGH;
            return Tier.MAXED;
        }
        if (preference == OutfitPreference.BANKSTANDER) {
            if (roll < 12) return Tier.LOW;
            if (roll < 55) return Tier.MID;
            if (roll < 88) return Tier.HIGH;
            return Tier.MAXED;
        }
        if (preference == OutfitPreference.FASHIONSCAPE) {
            if (roll < 8) return Tier.LOW;
            if (roll < 35) return Tier.MID;
            if (roll < 78) return Tier.HIGH;
            return Tier.MAXED;
        }
        return rollTier();
    }

    // ---- Skill rolling -----------------------------------------------------

    /**
     * Returns a length-Skills.NUM_SKILLS array of static skill levels. The
     * combat skills are concentrated to look intentional (it's rare for a
     * real account to have 80 attack and 5 strength); non-combat skills are
     * scattered with the linear-decreasing distribution from AIPBuilder.
     */
    private static int[] rollSkillLevels(Tier tier, ThreadLocalRandom random) {
        int[] levels = new int[Skills.SKILL_NAME.length];
        int combatCap = combatCapForTier(tier, random);
        int skillCap = skillCapForTier(tier, random);

        // Combat block: distribute around the cap with some noise.
        levels[Skills.HITPOINTS] = Math.max(10, jitter(combatCap, 5, random));
        levels[Skills.ATTACK] = jitter(combatCap, 8, random);
        levels[Skills.STRENGTH] = jitter(combatCap, 8, random);
        levels[Skills.DEFENCE] = jitter(combatCap, 10, random);
        levels[Skills.RANGE] = jitter(combatCap - random.nextInt(0, 15), 12, random);
        levels[Skills.MAGIC] = jitter(combatCap - random.nextInt(0, 15), 12, random);
        levels[Skills.PRAYER] = jitter(combatCap / 2 + random.nextInt(0, 20), 8, random);

        // Non-combat: each skill has independent fall-off from the cap.
        int[] skillingSlots = {
                Skills.COOKING, Skills.WOODCUTTING, Skills.FLETCHING, Skills.FISHING,
                Skills.FIREMAKING, Skills.CRAFTING, Skills.SMITHING, Skills.MINING,
                Skills.HERBLORE, Skills.AGILITY, Skills.THIEVING, Skills.SLAYER,
                Skills.FARMING, Skills.RUNECRAFTING, Skills.HUNTER, Skills.CONSTRUCTION,
                Skills.SUMMONING, Skills.DUNGEONEERING, Skills.DIVINATION, Skills.INVENTION
        };
        for (int slot : skillingSlots) {
            levels[slot] = linearDecreaseRand(skillCap, random);
        }

        clampAll(levels, 1, 99);
        levels[Skills.HITPOINTS] = Math.max(10, levels[Skills.HITPOINTS]);
        return levels;
    }

    private static int combatCapForTier(Tier tier, ThreadLocalRandom random) {
        switch (tier) {
            case NEWBIE: return random.nextInt(3, 16);
            case LOW:    return random.nextInt(15, 41);
            case MID:    return random.nextInt(40, 71);
            case HIGH:   return random.nextInt(70, 95);
            case MAXED:
            default:     return random.nextInt(90, 100);
        }
    }

    private static int skillCapForTier(Tier tier, ThreadLocalRandom random) {
        switch (tier) {
            case NEWBIE: return random.nextInt(1, 20);
            case LOW:    return random.nextInt(10, 50);
            case MID:    return random.nextInt(40, 80);
            case HIGH:   return random.nextInt(70, 99);
            case MAXED:
            default:     return random.nextInt(90, 100);
        }
    }

    private static int jitter(int value, int spread, ThreadLocalRandom random) {
        return Math.max(1, value + random.nextInt(-spread, spread + 1));
    }

    /**
     * Picks a random number 1..max with linearly decreasing probability -
     * higher values are increasingly rare. Mirrors AIPBuilder.linearDecreaseRand.
     */
    private static int linearDecreaseRand(int max, ThreadLocalRandom random) {
        if (max <= 1) return 1;
        int a = random.nextInt(max);
        int b = random.nextInt(max);
        return Math.min(a, b) + 1;
    }

    private static void clampAll(int[] levels, int min, int max) {
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] < min) levels[i] = min;
            if (levels[i] > max) levels[i] = max;
        }
    }

    private static void applyPreferenceLevels(int[] levels, OutfitPreference preference,
            ThreadLocalRandom random) {
        OutfitPreference safePreference = preference == null ? OutfitPreference.DEFAULT : preference;
        if (safePreference == OutfitPreference.MAXED_SHOWOFF) {
            levels[Skills.HITPOINTS] = 99;
            levels[Skills.DEFENCE] = Math.max(levels[Skills.DEFENCE], 90 + random.nextInt(10));
            levels[Skills.ATTACK] = Math.max(levels[Skills.ATTACK], 90 + random.nextInt(10));
            levels[Skills.STRENGTH] = Math.max(levels[Skills.STRENGTH], 90 + random.nextInt(10));
            int skillCapeSkill = SKILLCAPE_SKILLS[random.nextInt(SKILLCAPE_SKILLS.length)];
            levels[skillCapeSkill] = 99;
            if (random.nextBoolean()) {
                levels[SKILLCAPE_SKILLS[random.nextInt(SKILLCAPE_SKILLS.length)]] = 99;
            }
        } else if (safePreference == OutfitPreference.FASHIONSCAPE && random.nextInt(100) < 18) {
            levels[SKILLCAPE_SKILLS[random.nextInt(SKILLCAPE_SKILLS.length)]] = 99;
        } else if (safePreference == OutfitPreference.TRADER) {
            levels[Skills.MAGIC] = Math.max(levels[Skills.MAGIC], 55);
        } else if (safePreference == OutfitPreference.NEWBIE) {
            levels[Skills.HITPOINTS] = Math.max(10, Math.min(levels[Skills.HITPOINTS], 18));
        }
        clampAll(levels, 1, 99);
        levels[Skills.HITPOINTS] = Math.max(10, levels[Skills.HITPOINTS]);
    }

    // ---- Equipment templates ----------------------------------------------

    /** Slot count used by Equipment.SLOT_* constants. */
    private static final int EQUIPMENT_SLOT_COUNT = 20;

    private static final int[] SKILLCAPE_SKILLS = {
            Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.RANGE,
            Skills.PRAYER, Skills.MAGIC, Skills.RUNECRAFTING, Skills.HITPOINTS,
            Skills.AGILITY, Skills.HERBLORE, Skills.THIEVING, Skills.CRAFTING,
            Skills.FLETCHING, Skills.SLAYER, Skills.CONSTRUCTION, Skills.MINING,
            Skills.SMITHING, Skills.FISHING, Skills.COOKING, Skills.FIREMAKING,
            Skills.WOODCUTTING, Skills.FARMING, Skills.HUNTER, Skills.SUMMONING,
            Skills.DUNGEONEERING, Skills.DIVINATION, Skills.INVENTION
    };

    private static final int[] SKILLCAPE_IDS = {
            9747, 9750, 9753, 9756,
            9759, 9762, 9765, 9768,
            9771, 9774, 9777, 9780,
            9783, 9786, 9789, 9792,
            9795, 9798, 9801, 9804,
            9807, 9810, 9948, 12169,
            18508, 29185, 36351
    };

    private static final GearPiece[] MELEE_HATS = {
            g(1155, 6), g(1153, 6), g(1157, 7, Skills.DEFENCE, 5),
            g(1165, 5, Skills.DEFENCE, 10), g(1159, 7, Skills.DEFENCE, 20),
            g(1161, 7, Skills.DEFENCE, 30), g(1163, 8, Skills.DEFENCE, 40),
            g(1149, 5, Skills.DEFENCE, 60), g(4716, 3, Skills.DEFENCE, 70),
            g(4745, 3, Skills.DEFENCE, 70), g(4753, 3, Skills.DEFENCE, 70),
            g(20135, 2, Skills.DEFENCE, 80)
    };

    private static final GearPiece[] MELEE_CHESTS = {
            g(1117, 6), g(1115, 6), g(1119, 7, Skills.DEFENCE, 5),
            g(1125, 5, Skills.DEFENCE, 10), g(1121, 7, Skills.DEFENCE, 20),
            g(1123, 7, Skills.DEFENCE, 30), g(1127, 8, Skills.DEFENCE, 40),
            g(3140, 5, Skills.DEFENCE, 60), g(4720, 3, Skills.DEFENCE, 70),
            g(4728, 3, Skills.DEFENCE, 70), g(4749, 3, Skills.DEFENCE, 70),
            g(4757, 3, Skills.DEFENCE, 70), g(11724, 3, Skills.DEFENCE, 65),
            g(20139, 2, Skills.DEFENCE, 80)
    };

    private static final GearPiece[] MELEE_LEGS = {
            g(1075, 6), g(1067, 6), g(1069, 7, Skills.DEFENCE, 5),
            g(1077, 5, Skills.DEFENCE, 10), g(1071, 7, Skills.DEFENCE, 20),
            g(1073, 7, Skills.DEFENCE, 30), g(1079, 8, Skills.DEFENCE, 40),
            g(4087, 5, Skills.DEFENCE, 60), g(4585, 5, Skills.DEFENCE, 60),
            g(4722, 3, Skills.DEFENCE, 70), g(4730, 3, Skills.DEFENCE, 70),
            g(4751, 3, Skills.DEFENCE, 70), g(4759, 3, Skills.DEFENCE, 70),
            g(11726, 3, Skills.DEFENCE, 65), g(20143, 2, Skills.DEFENCE, 80)
    };

    private static final GearPiece[] MELEE_WEAPONS = {
            g(1277, 6), g(1279, 6), g(1281, 7, Skills.ATTACK, 5),
            g(1327, 5, Skills.ATTACK, 10), g(1285, 7, Skills.ATTACK, 20),
            g(1289, 7, Skills.ATTACK, 30), g(1291, 8, Skills.ATTACK, 40),
            g(4587, 5, Skills.ATTACK, 60), g(1305, 4, Skills.ATTACK, 60),
            g(1215, 4, Skills.ATTACK, 60), g(4151, 5, Skills.ATTACK, 70),
            g(4718, 2, Skills.ATTACK, 70, Skills.STRENGTH, 70),
            g(4755, 2, Skills.ATTACK, 70), g(11694, 2, Skills.ATTACK, 75),
            g(11696, 2, Skills.ATTACK, 75), g(11698, 2, Skills.ATTACK, 75),
            g(11700, 2, Skills.ATTACK, 75), g(18349, 2, Skills.ATTACK, 80,
                    Skills.DUNGEONEERING, 80)
    };

    private static final GearPiece[] MELEE_SHIELDS = {
            g(1189, 5), g(1191, 5), g(1193, 6, Skills.DEFENCE, 5),
            g(1195, 4, Skills.DEFENCE, 10), g(1197, 6, Skills.DEFENCE, 20),
            g(1199, 6, Skills.DEFENCE, 30), g(1201, 7, Skills.DEFENCE, 40),
            g(1187, 4, Skills.DEFENCE, 60), g(8850, 4, Skills.ATTACK, 40,
                    Skills.DEFENCE, 40), g(11283, 2, Skills.DEFENCE, 75),
            g(13734, 2, Skills.DEFENCE, 45, Skills.PRAYER, 55)
    };

    private static final GearPiece[] RANGE_HATS = {
            g(1167, 6), g(1169, 6), g(6326, 4, Skills.RANGE, 30),
            g(2581, 4, Skills.RANGE, 40),
            g(4732, 3, Skills.DEFENCE, 70, Skills.RANGE, 70),
            g(11718, 3, Skills.DEFENCE, 70, Skills.RANGE, 70),
            g(20147, 2, Skills.DEFENCE, 80, Skills.RANGE, 80)
    };

    private static final GearPiece[] RANGE_CHESTS = {
            g(1129, 6), g(1131, 5, Skills.DEFENCE, 10),
            g(1133, 5, Skills.DEFENCE, 20), g(1135, 7, Skills.DEFENCE, 40,
                    Skills.RANGE, 40), g(2499, 6, Skills.DEFENCE, 40,
                    Skills.RANGE, 50), g(2501, 5, Skills.DEFENCE, 40,
                    Skills.RANGE, 60), g(2503, 5, Skills.DEFENCE, 40,
                    Skills.RANGE, 70), g(4736, 3, Skills.DEFENCE, 70,
                    Skills.RANGE, 70), g(11720, 3, Skills.DEFENCE, 70,
                    Skills.RANGE, 70), g(20151, 2, Skills.DEFENCE, 80,
                    Skills.RANGE, 80)
    };

    private static final GearPiece[] RANGE_LEGS = {
            g(1095, 6), g(1097, 5, Skills.DEFENCE, 10),
            g(1099, 7, Skills.RANGE, 40), g(2493, 6, Skills.RANGE, 50),
            g(2495, 5, Skills.RANGE, 60), g(2497, 5, Skills.RANGE, 70),
            g(4738, 3, Skills.DEFENCE, 70, Skills.RANGE, 70),
            g(11722, 3, Skills.DEFENCE, 70, Skills.RANGE, 70),
            g(20155, 2, Skills.DEFENCE, 80, Skills.RANGE, 80)
    };

    private static final GearPiece[] RANGE_WEAPONS = {
            g(841, 6), g(843, 6, Skills.RANGE, 5),
            g(849, 7, Skills.RANGE, 20), g(853, 7, Skills.RANGE, 30),
            g(855, 7, Skills.RANGE, 40), g(859, 7, Skills.RANGE, 50),
            g(9185, 5, Skills.RANGE, 61), g(4212, 3, Skills.RANGE, 70),
            g(4734, 3, Skills.RANGE, 70), g(18357, 2, Skills.RANGE, 80,
                    Skills.DUNGEONEERING, 80), g(20171, 2, Skills.RANGE, 80)
    };

    private static final GearPiece[] MAGIC_HATS = {
            g(579, 6), g(4089, 6, Skills.MAGIC, 40),
            g(6918, 4, Skills.MAGIC, 50, Skills.DEFENCE, 25),
            g(4708, 3, Skills.MAGIC, 70, Skills.DEFENCE, 70),
            g(20159, 2, Skills.MAGIC, 80, Skills.DEFENCE, 80)
    };

    private static final GearPiece[] MAGIC_CHESTS = {
            g(577, 6), g(640, 5), g(4091, 6, Skills.MAGIC, 40),
            g(6916, 4, Skills.MAGIC, 50, Skills.DEFENCE, 25),
            g(4712, 3, Skills.MAGIC, 70, Skills.DEFENCE, 70),
            g(20163, 2, Skills.MAGIC, 80, Skills.DEFENCE, 80)
    };

    private static final GearPiece[] MAGIC_LEGS = {
            g(1011, 6), g(650, 5), g(4093, 6, Skills.MAGIC, 40),
            g(6924, 4, Skills.MAGIC, 50, Skills.DEFENCE, 25),
            g(4714, 3, Skills.MAGIC, 70, Skills.DEFENCE, 70),
            g(20167, 2, Skills.MAGIC, 80, Skills.DEFENCE, 80)
    };

    private static final GearPiece[] MAGIC_WEAPONS = {
            g(1379, 6), g(1381, 6), g(1383, 6), g(1385, 6),
            g(1387, 6), g(1389, 6), g(4675, 4, Skills.MAGIC, 50,
                    Skills.ATTACK, 50), g(4710, 3, Skills.MAGIC, 70,
                    Skills.ATTACK, 70), g(15486, 2, Skills.MAGIC, 75,
                    Skills.ATTACK, 75), g(18355, 2, Skills.MAGIC, 80,
                    Skills.DUNGEONEERING, 80)
    };

    private static final GearPiece[] MAGIC_SHIELDS = {
            g(6889, 3, Skills.MAGIC, 60), g(18361, 2, Skills.MAGIC, 80,
                    Skills.DUNGEONEERING, 80)
    };

    private static final GearPiece[] CASUAL_HATS = {
            g(2633, 5), g(2635, 5), g(2637, 5),
            g(2639, 5), g(2641, 5), g(2643, 5),
            g(8950, 4), g(13101, 4), g(19747, 3),
            g(2581, 3, Skills.RANGE, 40), g(12204, 3),
            g(7112, 4), g(7124, 4), g(7130, 4),
            g(7136, 4), g(3797, 4), g(6547, 2),
            g(33593, 2), g(14791, 2), g(14749, 2)
    };

    private static final GearPiece[] RARE_HATS = {
            g(1038, 1), g(1040, 1), g(1042, 1), g(1044, 1),
            g(1046, 1), g(1048, 1), g(1050, 2), g(1053, 1),
            g(1055, 1), g(1057, 1), g(30412, 1), g(36080, 1)
    };

    private static final GearPiece[] CASUAL_CHESTS = {
            g(426, 5), g(544, 5), g(1833, 5),
            g(3767, 4), g(3769, 4), g(3771, 4),
            g(3793, 4), g(6341, 4), g(6351, 4),
            g(6361, 4), g(6371, 4),
            g(7110, 4), g(7122, 4), g(8952, 4),
            g(8954, 4), g(8956, 4), g(10420, 3),
            g(10424, 3), g(10428, 3), g(10432, 3),
            g(284, 4), g(430, 4), g(1005, 4),
            g(1757, 4), g(1844, 4), g(5030, 4),
            g(5032, 4), g(5034, 4), g(6384, 4),
            g(6388, 4), g(7592, 3), g(10061, 3)
    };

    private static final GearPiece[] CASUAL_LEGS = {
            g(428, 5), g(542, 5), g(1835, 5),
            g(3795, 4), g(6343, 3), g(6353, 3),
            g(6363, 3), g(6373, 3), g(7116, 4), g(7126, 4),
            g(7132, 4), g(10402, 3), g(10406, 3),
            g(10410, 3), g(10414, 3), g(10394, 4),
            g(285, 4), g(5036, 4), g(5038, 4),
            g(5040, 4), g(5042, 4), g(5044, 4),
            g(5046, 4), g(5048, 4), g(5050, 4),
            g(5052, 4), g(6181, 3), g(6187, 3),
            g(6390, 3), g(7593, 3), g(9642, 3),
            g(10063, 3)
    };

    private static final GearPiece[] FASHION_FEET = {
            g(1061, 5), g(1837, 5), g(3105, 5),
            g(88, 5), g(626, 4), g(628, 4), g(630, 4),
            g(632, 4), g(634, 4), g(2579, 3),
            g(7114, 4), g(9005, 4), g(9006, 4),
            g(10689, 3, Skills.MAGIC, 20), g(2577, 3, Skills.RANGE, 40),
            g(11732, 4, Skills.DEFENCE, 60), g(11728, 3, Skills.DEFENCE, 65),
            g(25010, 2, Skills.DEFENCE, 70, Skills.RANGE, 70),
            g(30920, 3, Skills.AGILITY, 30),
            g(21787, 2, Skills.DEFENCE, 85), g(21790, 2, Skills.RANGE, 85),
            g(21793, 2, Skills.MAGIC, 85)
    };

    private static final GearPiece[] ACCESSORY_HANDS = {
            g(1059, 7), g(1063, 6), g(1065, 6),
            g(2487, 5, Skills.RANGE, 50), g(2489, 5, Skills.RANGE, 60),
            g(2491, 5, Skills.RANGE, 70), g(2902, 4),
            g(2912, 4), g(2922, 4), g(2932, 4),
            g(2942, 4), g(3060, 4),
            g(4095, 4, Skills.MAGIC, 40), g(4105, 3, Skills.MAGIC, 40),
            g(4115, 3, Skills.MAGIC, 40), g(3799, 4), g(4308, 4),
            g(5556, 3), g(6068, 3), g(6110, 3),
            g(7460, 4, Skills.DEFENCE, 40), g(7462, 3, Skills.DEFENCE, 70),
            g(11126, 4), g(11133, 3), g(24977, 2, Skills.DEFENCE, 80)
    };

    private static final GearPiece[] AMULETS = {
            g(1478, 5), g(1725, 6), g(1727, 5), g(1729, 5),
            g(1731, 5), g(1704, 5), g(6585, 4),
            g(15126, 3, Skills.RANGE, 50), g(19335, 2), g(32703, 2),
            g(6857, 3), g(6859, 3), g(6861, 3), g(6863, 3),
            g(9470, 3)
    };

    private static final GearPiece[] RINGS = {
            g(2550, 5), g(2552, 5), g(2572, 5),
            g(6735, 3, Skills.ATTACK, 40), g(6737, 3, Skills.STRENGTH, 40),
            g(6731, 3, Skills.RANGE, 40), g(6733, 3, Skills.MAGIC, 40),
            g(13560, 4), g(19760, 3), g(15220, 2, Skills.STRENGTH, 60)
    };

    private static final GearPiece[] CAPES = {
            g(1019, 6), g(1021, 6), g(1023, 6),
            g(1027, 6), g(4514, 5), g(4516, 5),
            g(6568, 5, Skills.DEFENCE, 40), g(6570, 3, Skills.HITPOINTS, 70),
            g(10499, 4, Skills.RANGE, 50), g(20068, 3, Skills.RANGE, 70),
            g(9813, 2), g(20763, 2), g(23639, 2, Skills.DEFENCE, 80),
            g(3759, 4), g(3761, 4), g(3763, 4), g(3765, 4),
            g(4041, 3), g(4042, 3),
            g(4304, 3), g(6070, 3), g(6111, 3)
    };

    private static final GearPiece[] POCKET_ITEMS = {
            g(26296, 3), g(26300, 3), g(26302, 3),
            g(26304, 3), g(26306, 3), g(26308, 3), g(26310, 3),
            g(3840, 2), g(3842, 2), g(3844, 2)
    };

    private static final GearPiece[] ARROWS = {
            g(882, 6), g(884, 5, Skills.RANGE, 5),
            g(886, 5, Skills.RANGE, 20), g(888, 5, Skills.RANGE, 30),
            g(890, 4, Skills.RANGE, 40), g(892, 4, Skills.RANGE, 50)
    };

    /** Returns slot-to-itemId map (or 0 = empty). Indexed by Equipment.SLOT_*. */
    private static int[] pickEquipmentSet(Tier tier, int[] levels, ThreadLocalRandom random,
            OutfitPreference preference) {
        int[] slots = new int[EQUIPMENT_SLOT_COUNT];
        OutfitStyle style = pickOutfitStyle(tier, levels, random, preference);
        if (style == OutfitStyle.CASUAL) {
            fillCasualOutfit(slots, levels, random);
        } else {
            fillCombatOutfit(slots, style, levels, random);
            addFashionBreaks(slots, tier, levels, random, preference);
        }
        addAccessories(slots, style, tier, levels, random, preference);
        applySkillcapeIfEarned(slots, levels, random);
        return slots;
    }

    private static OutfitStyle pickOutfitStyle(Tier tier, int[] levels, ThreadLocalRandom random,
            OutfitPreference preference) {
        if (roll(random, casualChance(tier, preference))) {
            return OutfitStyle.CASUAL;
        }
        int melee = (level(levels, Skills.ATTACK) + level(levels, Skills.STRENGTH)
                + level(levels, Skills.DEFENCE)) / 3;
        int range = (level(levels, Skills.RANGE) + level(levels, Skills.DEFENCE)) / 2;
        int magic = (level(levels, Skills.MAGIC) + level(levels, Skills.DEFENCE)) / 2;
        int total = Math.max(1, melee) + Math.max(1, range) + Math.max(1, magic);
        int choice = random.nextInt(total);
        if (choice < melee) {
            return OutfitStyle.MELEE;
        }
        if (choice < melee + range) {
            return OutfitStyle.RANGE;
        }
        return OutfitStyle.MAGIC;
    }

    private static int casualChance(Tier tier, OutfitPreference preference) {
        if (preference == OutfitPreference.NEWBIE) {
            return 70;
        }
        if (preference == OutfitPreference.FASHIONSCAPE) {
            return 62;
        }
        if (preference == OutfitPreference.BANKSTANDER) {
            return 42;
        }
        if (preference == OutfitPreference.TRADER) {
            return 22;
        }
        if (preference == OutfitPreference.MAXED_SHOWOFF) {
            return 38;
        }
        switch (tier) {
            case NEWBIE: return 35;
            case LOW: return 25;
            case MID: return 18;
            case HIGH: return 16;
            case MAXED:
            default: return 24;
        }
    }

    private static int fashionChance(Tier tier, OutfitPreference preference) {
        if (preference == OutfitPreference.NEWBIE) {
            return 50;
        }
        if (preference == OutfitPreference.FASHIONSCAPE) {
            return 90;
        }
        if (preference == OutfitPreference.BANKSTANDER) {
            return 72;
        }
        if (preference == OutfitPreference.TRADER) {
            return 55;
        }
        if (preference == OutfitPreference.MAXED_SHOWOFF) {
            return 84;
        }
        switch (tier) {
            case NEWBIE: return 35;
            case LOW: return 42;
            case MID: return 48;
            case HIGH: return 58;
            case MAXED:
            default: return 68;
        }
    }

    private static void fillCombatOutfit(int[] slots, OutfitStyle style, int[] levels,
            ThreadLocalRandom random) {
        switch (style) {
            case RANGE:
                setRolled(slots, Equipment.SLOT_HAT, RANGE_HATS, levels, random, 70);
                setRolled(slots, Equipment.SLOT_CHEST, RANGE_CHESTS, levels, random, 86);
                setRolled(slots, Equipment.SLOT_LEGS, RANGE_LEGS, levels, random, 84);
                setRolled(slots, Equipment.SLOT_WEAPON, RANGE_WEAPONS, levels, random, 92);
                if (roll(random, 65)) {
                    setRolled(slots, Equipment.SLOT_ARROWS, ARROWS, levels, random, 100);
                }
                break;
            case MAGIC:
                setRolled(slots, Equipment.SLOT_HAT, MAGIC_HATS, levels, random, 70);
                setRolled(slots, Equipment.SLOT_CHEST, MAGIC_CHESTS, levels, random, 86);
                setRolled(slots, Equipment.SLOT_LEGS, MAGIC_LEGS, levels, random, 84);
                setRolled(slots, Equipment.SLOT_WEAPON, MAGIC_WEAPONS, levels, random, 92);
                setRolled(slots, Equipment.SLOT_SHIELD, MAGIC_SHIELDS, levels, random, 35);
                break;
            case MELEE:
            default:
                setRolled(slots, Equipment.SLOT_HAT, MELEE_HATS, levels, random, 70);
                setRolled(slots, Equipment.SLOT_CHEST, MELEE_CHESTS, levels, random, 86);
                setRolled(slots, Equipment.SLOT_LEGS, MELEE_LEGS, levels, random, 84);
                setRolled(slots, Equipment.SLOT_WEAPON, MELEE_WEAPONS, levels, random, 92);
                setRolled(slots, Equipment.SLOT_SHIELD, MELEE_SHIELDS, levels, random, 45);
                break;
        }
    }

    private static void fillCasualOutfit(int[] slots, int[] levels, ThreadLocalRandom random) {
        setRolled(slots, Equipment.SLOT_HAT, randomHatPool(random), levels, random, 65);
        setRolled(slots, Equipment.SLOT_CHEST, CASUAL_CHESTS, levels, random, 82);
        setRolled(slots, Equipment.SLOT_LEGS, CASUAL_LEGS, levels, random, 82);
        setRolled(slots, Equipment.SLOT_FEET, FASHION_FEET, levels, random, 75);
        if (roll(random, 45)) {
            setRolled(slots, Equipment.SLOT_WEAPON, MELEE_WEAPONS, levels, random, 100);
        }
    }

    private static void addFashionBreaks(int[] slots, Tier tier, int[] levels,
            ThreadLocalRandom random, OutfitPreference preference) {
        int chance = fashionChance(tier, preference);
        if (roll(random, chance / 2)) {
            setRolled(slots, Equipment.SLOT_HAT, randomHatPool(random), levels, random, 100);
        }
        if (roll(random, chance / 3)) {
            setRolled(slots, Equipment.SLOT_CHEST, CASUAL_CHESTS, levels, random, 100);
        }
        if (roll(random, chance / 3)) {
            setRolled(slots, Equipment.SLOT_LEGS, CASUAL_LEGS, levels, random, 100);
        }
        if (roll(random, chance / 2)) {
            setRolled(slots, Equipment.SLOT_FEET, FASHION_FEET, levels, random, 100);
        }
        if (roll(random, chance / 3)) {
            setRolled(slots, Equipment.SLOT_HANDS, ACCESSORY_HANDS, levels, random, 100);
        }
        if (preference == OutfitPreference.FASHIONSCAPE
                || preference == OutfitPreference.MAXED_SHOWOFF) {
            if (roll(random, 45)) {
                setRolled(slots, Equipment.SLOT_CAPE, CAPES, levels, random, 100);
            }
            if (roll(random, 35)) {
                setRolled(slots, Equipment.SLOT_AMULET, AMULETS, levels, random, 100);
            }
        }
    }

    private static void addAccessories(int[] slots, OutfitStyle style, Tier tier, int[] levels,
            ThreadLocalRandom random, OutfitPreference preference) {
        int accessoryBoost = preference == OutfitPreference.FASHIONSCAPE
                || preference == OutfitPreference.MAXED_SHOWOFF ? 18 : 0;
        setRolled(slots, Equipment.SLOT_AMULET, AMULETS, levels, random, 78 + accessoryBoost);
        setRolled(slots, Equipment.SLOT_RING, RINGS, levels, random, 60 + accessoryBoost);
        if (slots[Equipment.SLOT_HANDS] == 0 || roll(random, 45)) {
            setRolled(slots, Equipment.SLOT_HANDS, ACCESSORY_HANDS, levels, random,
                    72 + accessoryBoost);
        }
        if (slots[Equipment.SLOT_FEET] == 0 || roll(random, 38)) {
            setRolled(slots, Equipment.SLOT_FEET, FASHION_FEET, levels, random,
                    80 + accessoryBoost);
        }
        if (slots[Equipment.SLOT_CAPE] == 0 || roll(random, style == OutfitStyle.CASUAL ? 55 : 28)) {
            setRolled(slots, Equipment.SLOT_CAPE, CAPES, levels, random,
                    70 + accessoryBoost);
        }
        if (tier != Tier.NEWBIE && roll(random, 24 + accessoryBoost)) {
            setRolled(slots, Equipment.SLOT_POCKET, POCKET_ITEMS, levels, random, 100);
        }
    }

    private static GearPiece[] randomHatPool(ThreadLocalRandom random) {
        return roll(random, 10) ? RARE_HATS : CASUAL_HATS;
    }

    private static void applySkillcapeIfEarned(int[] slots, int[] levels, ThreadLocalRandom random) {
        int count = 0;
        for (int skill : SKILLCAPE_SKILLS) {
            if (level(levels, skill) >= 99) {
                count++;
            }
        }
        if (count == 0) {
            return;
        }
        int pick = random.nextInt(count);
        for (int i = 0; i < SKILLCAPE_SKILLS.length; i++) {
            if (level(levels, SKILLCAPE_SKILLS[i]) < 99) {
                continue;
            }
            if (pick-- == 0) {
                slots[Equipment.SLOT_CAPE] = SKILLCAPE_IDS[i];
                return;
            }
        }
    }

    private static void setRolled(int[] slots, int slot, GearPiece[] pieces, int[] levels,
            ThreadLocalRandom random, int percentChance) {
        if (!roll(random, percentChance)) {
            return;
        }
        int itemId = chooseEligible(slot, pieces, levels, random);
        if (itemId > 0) {
            slots[slot] = itemId;
        }
    }

    private static int chooseEligible(int slot, GearPiece[] pieces, int[] levels,
            ThreadLocalRandom random) {
        int totalWeight = 0;
        for (GearPiece piece : pieces) {
            if (canUsePiece(piece, slot, levels)) {
                totalWeight += piece.weight;
            }
        }
        if (totalWeight <= 0) {
            return 0;
        }
        int roll = random.nextInt(totalWeight);
        for (GearPiece piece : pieces) {
            if (!canUsePiece(piece, slot, levels)) {
                continue;
            }
            roll -= piece.weight;
            if (roll < 0) {
                return piece.itemId;
            }
        }
        return 0;
    }

    private static boolean canUsePiece(GearPiece piece, int slot, int[] levels) {
        return piece.canWear(levels) && itemFitsSlot(piece.itemId, slot);
    }

    private static boolean itemFitsSlot(int itemId, int slot) {
        try {
            return Equipment.getItemSlot(itemId) == slot;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean roll(ThreadLocalRandom random, int percentChance) {
        return percentChance > 0 && random.nextInt(100) < percentChance;
    }

    private static int level(int[] levels, int skill) {
        return skill >= 0 && skill < levels.length ? levels[skill] : 1;
    }

    private static GearPiece g(int itemId, int weight, int... requirements) {
        return new GearPiece(itemId, weight, requirements);
    }

    private enum OutfitStyle {
        MELEE,
        RANGE,
        MAGIC,
        CASUAL
    }

    private static final class GearPiece {
        private final int itemId;
        private final int weight;
        private final int[] requirements;

        private GearPiece(int itemId, int weight, int[] requirements) {
            this.itemId = itemId;
            this.weight = Math.max(1, weight);
            this.requirements = requirements == null ? new int[0] : requirements;
        }

        private boolean canWear(int[] levels) {
            for (int i = 0; i + 1 < requirements.length; i += 2) {
                if (level(levels, requirements[i]) < requirements[i + 1]) {
                    return false;
                }
            }
            return true;
        }
    }

    // ---- Profile object ----------------------------------------------------

    public static final class BotProfile {

        private final String displayName;
        private final Tier tier;
        private final int[] skillLevels;
        private final int[] equipmentBySlot;

        BotProfile(String displayName, Tier tier, int[] skillLevels, int[] equipmentBySlot) {
            this.displayName = displayName;
            this.tier = tier;
            this.skillLevels = skillLevels;
            this.equipmentBySlot = equipmentBySlot;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Tier getTier() {
            return tier;
        }

        public int getSkillLevel(int skillId) {
            return skillId >= 0 && skillId < skillLevels.length ? skillLevels[skillId] : 1;
        }

        /** Applies this profile to an existing player. Bot or otherwise. */
        public void apply(Player player) {
            applySkills(player);
            applyEquipment(player);
            player.getAppearence().generateAppearenceData();
        }

        private void applySkills(Player player) {
            Skills skills = player.getSkills();
            for (int skill = 0; skill < skillLevels.length; skill++) {
                int level = skillLevels[skill];
                if (level <= 0) continue;
                double xp = Skills.getXPForLevel(skill, level);
                skills.setXp(skill, xp);
            }
            skills.restoreSkills();
            skills.init();
            player.setHitpoints(player.getMaxHitpoints());
            player.refreshHitPoints();
        }

        private void applyEquipment(Player player) {
            Equipment equipment = player.getEquipment();
            for (int slot = 0; slot < equipmentBySlot.length; slot++) {
                int itemId = equipmentBySlot[slot];
                if (itemId <= 0) continue;
                try {
                    equipment.set(slot, new Item(itemId, 1));
                } catch (Throwable ignored) {
                    // Bad item id - leave the slot empty rather than crashing the spawn.
                }
            }
            equipment.refreshItemContainer();
        }

        @Override
        public String toString() {
            return "BotProfile{name=" + displayName + ",tier=" + tier
                    + ",cb=" + getCombatLevel() + "}";
        }

        public int getCombatLevel() {
            int att = skillLevels[Skills.ATTACK];
            int str = skillLevels[Skills.STRENGTH];
            int def = skillLevels[Skills.DEFENCE];
            int hp  = skillLevels[Skills.HITPOINTS];
            int pra = skillLevels[Skills.PRAYER];
            int rng = skillLevels[Skills.RANGE];
            int mag = skillLevels[Skills.MAGIC];
            // Standard RS combat formula approximation
            double base = 0.25 * (def + hp + Math.floor(pra / 2.0));
            double melee = 0.325 * (att + str);
            double range = 0.325 * Math.floor(rng * 1.5);
            double mage = 0.325 * Math.floor(mag * 1.5);
            return (int) Math.floor(base + Math.max(melee, Math.max(range, mage)));
        }
    }
}
