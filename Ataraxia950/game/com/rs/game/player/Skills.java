package com.rs.game.player;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rs.Settings;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.event.GIMEventType;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.Revenant;
import com.rs.game.npc.others.randomevent.impl.CraftingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.FletchingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.SummoningRandomEvent;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.actions.summoning.Summoning;
import com.rs.game.player.actions.thieving.Thieving;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.RuneCrafting;
import com.rs.game.player.content.SkillingPets;
import com.rs.game.player.content.XPLamps;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.dialogue.impl.LevelUp;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

import lombok.Getter;
import lombok.Setter;

/**
 * A class used to handle all players Skills & Experience.
 *
 * @author Noel
 */
public final class Skills implements Serializable {

    /**
     * Maximum experience per skill, as the 947 client enforces it.
     *
     * <p>CACHE FIDELITY: the live stat table the UPDATE_STAT sink writes
     * ({@code [[owner+0x198E0]+0x7618]}, built at {@code 0x1400CDF20}) carries entry
     * flag 0, and the experience setter {@code 0x140369C10} clamps a flag-zero entry
     * at {@code 0x0BEBC200} = 200,000,000. Cache script 8489 instruction 630 compares
     * the stored experience against the literal 200000000 to light the "maxed"
     * graphic, and neither cache curve can express more (the elite table tops out at
     * 194,927,409 and the default table crosses 200,000,000 between level 126 and
     * 127). See {@code verified/ui/STAT_DEFINITIONS.md} section 3b.
     *
     * <p>This replaces the 910 value of 2,000,000,000. Consequences are handled
     * explicitly in {@link #addXp}, {@link #silentAddXp} and
     * {@code notes/SKILLS-27-28.md}: stored experience above the new ceiling is
     * clamped down the first time the skill is touched, and the 250m/500m/1000m/
     * 1500m/2000m milestones are unreachable and were replaced by a single
     * maximum-experience message.
     */
    public static final double MAXIMUM_EXP = 200000000;
    /**
     * Integers representing Skill ID's.
     */
    public static final int ATTACK = 0, DEFENCE = 1, STRENGTH = 2, HITPOINTS = 3, RANGE = 4, PRAYER = 5, MAGIC = 6, COOKING = 7, WOODCUTTING = 8, FLETCHING = 9, FISHING = 10, FIREMAKING = 11, CRAFTING = 12, SMITHING = 13, MINING = 14, HERBLORE = 15, AGILITY = 16, THIEVING = 17, SLAYER = 18, FARMING = 19, RUNECRAFTING = 20, CONSTRUCTION = 22, HUNTER = 21, SUMMONING = 23, DUNGEONEERING = 24, DIVINATION = 25, INVENTION = 26, ARCHAEOLOGY = 27, NECROMANCY = 28;

    /**
     * The number of stats the 947 cache defines: the count byte of opcode 1 in the
     * STAT defaults group (flat cache index 28, group 9), cross-checked against
     * enum 680 (29 names) and enum 10865 (size 29). Every skill-shaped array in this
     * class is this long; nothing may hardcode 27 or 29 anywhere else.
     */
    public static final int SKILL_COUNT = 29;

    /**
     * Per-skill level cap, decoded from the unsigned short in each stat definition
     * (28/9 opcode 1) and agreeing with enum 10865 on all 29 ids.
     * See {@code verified/ui/STAT_DEFINITIONS.md} section 1.
     */
    private static final int[] LEVEL_CAP = {
            120,  99, 120,  99, 120,  99, 120,  99, 110, 110,   // 0 Attack .. 9 Fletching
             99, 110, 110, 110, 110, 120,  99, 120, 120, 120,   // 10 Fishing .. 19 Farming
            110, 110,  99,  99, 120,  99, 120, 120, 120 };      // 20 Runecrafting .. 28 Necromancy

    public static final int[] SHARDS = {
            // these are all added by skill id, so SHARDS[skill] returns a
            // skills shard
            32069, 32071, 32070, 32074, 32075, 32073, 32076, 32077, 32064, 32080, 32063, 32079, 32082, 32084, 32065, 32081, 32087, 32086, 32088, 32067, 32078, 32068, 32083, 32072, 32085, 32066};

    public static final int[] SKELETON_OUTFIT_PIECES = new int[]{9921, 9922, 9923, 9924, 9925};

    public static final int[] LUNARFURY_OUTFIT_PIECES = new int[]{39870, 39872, 39874, 39876, 39878};

    public int getHighestSkillLevel() {
        int maxLevel = 1;
        for (int skill = 0; skill < level.length; skill++) {
            int level = getLevelForXp(skill);
            if (level > maxLevel)
                maxLevel = level;
        }
        return maxLevel;
    }

    public static final int[] INVENTION_XP_FOR_LEVEL = {0, 0, 830, 1861, 2902, 3980, 5126, 6380, 7787, 9400, 11275, 13605, 16372, 19656, 23546, 28134, 33520, 39809, 47109, 55535, 65209, 77190, 90811, 106221, 123573, 143025, 164742, 188893, 215651, 245196, 277713, 316311, 358547, 404634, 454796, 509259, 568254, 632019, 700797, 774834, 854383, 946227, 1044569, 1149696, 1261903, 1381488, 1508756, 1644015, 1787581, 1939773, 2100917, 2283490, 2476369, 2679917, 2894505, 3120508, 3358307, 3608290, 3870846, 4146374, 4435275, 4758122, 5096111, 5449685, 5819299, 6205407, 6608473, 7028964, 7467354, 7924122, 8399751, 8925664, 9472665, 10041285, 10632061, 11245538, 11882262, 12542789, 13227679, 13937496, 14672812, 15478994, 16313404, 17176661, 18069395, 18992239, 19945833, 20930821, 21947856, 22997593, 24080695, 25259906, 26475754, 27728955, 29020233, 30350318, 31719944, 33129852, 34580790, 36073511, 37608773, 39270442, 40978509, 42733789, 44537107, 46389292, 48291180, 50243611, 52247435, 54303504, 56412678, 58575824, 60793812, 63067521, 65397835, 67785643, 70231841, 72737330, 75303019, 77929820, 80618654, 83370445, 86186124, 89066630, 92012904, 95025896, 98106559, 101255855, 104474750, 107764216, 111125230, 114558777, 118065845, 121647430, 125304532, 129038159, 132849323, 136739041, 140708338, 144758242, 148889790, 153104021, 157401983, 161784728, 166253312, 170808801, 175452262, 180184770, 185007406, 189921255, 194927409};
    public static final int[] FIXED_SLOTS = { ATTACK, HITPOINTS, MINING, STRENGTH, AGILITY, SMITHING, DEFENCE, HERBLORE,
            FISHING, RANGE, THIEVING, COOKING, PRAYER, CRAFTING, FIREMAKING, MAGIC, FLETCHING, WOODCUTTING,
            RUNECRAFTING, SLAYER, FARMING, CONSTRUCTION, HUNTER, SUMMONING, DUNGEONEERING, DIVINATION, INVENTION };
    /**
     * Strings representing Skill names.
     */
    public static final String[] SKILL_NAME = {"Attack", "Defence", "Strength", "Constitution", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Hunter", "Construction", "Summoning", "Dungeoneering", "Divination", "Invention", "Archaeology", "Necromancy"};

    /**
     * The generated serial UID.
     */
    private static final long serialVersionUID = -7086829989489745985L;

    public static final ImmutableMap<String, Integer> NAME_TO_ID;
    public static final ImmutableList<Integer> SKILL_IDS;

    static {
        if (SKILL_NAME.length != SKILL_COUNT || LEVEL_CAP.length != SKILL_COUNT)
            throw new IllegalStateException("The skill model must carry exactly " + SKILL_COUNT + " stats");
        Map<String, Integer> nameToId = new HashMap<>(SKILL_NAME.length);
        List<Integer> skills = new ArrayList<>();
        for (int index = 0; index < SKILL_NAME.length; index++) {
            nameToId.put(SKILL_NAME[index].toLowerCase(), index);
            skills.add(index);
        }
        SKILL_IDS = ImmutableList.copyOf(skills);
        NAME_TO_ID = ImmutableMap.copyOf(nameToId);
    }

    public short[] level;
    private double[] xp;
    private double[] xpTracks;
    private boolean[] trackSkills;
    private byte[] trackSkillsIds;
    private boolean xpDisplay, xpPopup;
    private transient int currentCounter;
    private boolean[] enabledSkillsTargets;
    private boolean[] skillsTargetsUsingLevelMode;
    private int[] skillsTargetsValues;
    private int[] skillsTargetsInitialValues;
    @Setter
    private double bonusPrismaticXp;
    private boolean virtualLevels;
    private double[] xpBonuses;
    private boolean[] xpAnnouncements;

    /**
     * Calls the Player file.
     */
    private transient Player player;

    /**
     * Initializes the Skills Serializable.
     */
    public Skills() {
        level = new short[SKILL_COUNT];
        xp = new double[SKILL_COUNT];
        for (int i = 0; i < level.length; i++) {
            level[i] = 1;
            xp[i] = 0;
        }
        level[3] = 10;
        xp[3] = 1155; // the cache curve puts level 10 at 1154; 1155 still resolves to 10, so this is left alone
        //level[HERBLORE] = 3;
        //xp[HERBLORE] = 175;
        xpPopup = true;
        xpTracks = new double[3];
        trackSkills = new boolean[3];
        trackSkillsIds = new byte[3];
        trackSkills[0] = true;
        for (int i = 0; i < trackSkillsIds.length; i++)
            trackSkillsIds[i] = 30;
        xpBonuses = new double[SKILL_COUNT];
        xpAnnouncements = new boolean[11];
    }

    public void addBonusPrismaticXp(double xp) {
        bonusPrismaticXp += xp;
    }

    public double getBonusPrismaticXp() {
        return bonusPrismaticXp;
    }

    public double getRawPrismaticLampXp(int level) {
        switch (level) {
            case 0:
                return 10_322;
            case 1:
                return 20_644;
            case 2:
                return 41_290;
            case 3:
                return 82_578;
            default:
                throw new IllegalStateException("Invalid prismatic level " + level);
        }
    }

    public void switchVirtualLevels() {
        virtualLevels = !virtualLevels;
        player.sendMessage("You're now playing " + (virtualLevels ? "with" : "without") + " virtual leveling.");
        init();
    }

    public boolean virtualLeveling() {
        return virtualLevels;
    }

    public double getPrismaticLampXp(int level) {
        return getRawPrismaticLampXp(level) * getXPRates();
    }

    private static Map<String, Integer> names;





    public static Integer getSkill(String name) {
        if (names == null) {
            names = new HashMap<>(SKILL_NAME.length);
            for (int index = 0; index < SKILL_NAME.length; index++) {
                names.put(SKILL_NAME[index].toLowerCase(), index);
            }
        }
        return names.get(name);
    }

    /**
     * The cache-derived level cap for a stat: the unsigned short at offset +1 of its
     * stat definition (28/9 opcode 1), which is also what the client's own base-level
     * recomputation clamps with ({@code min(entriesBelowXp + def+0x20, def+4)}).
     *
     * <p>This is the ONLY cap oracle in the codebase. {@link #getXPForLevel} is not
     * one: it happily answers for a level above the cap, because requirement checks
     * legitimately ask "what is level 92 worth" for a skill capped at 99.
     *
     * @param skill a stat id in {@code 0..SKILL_COUNT-1}.
     * @return the cap, or 99 (the cache's own {@code defaultInt} for enum 10865) for
     *         an id outside the model, so a stray caller degrades instead of throwing.
     */
    public static int getLevelCap(int skill) {
        return skill < 0 || skill >= SKILL_COUNT ? 99 : LEVEL_CAP[skill];
    }

    /**
     * The cumulative experience curve a stat uses, 0-indexed by {@code level - 1}.
     *
     * <p>CACHE FIDELITY: exactly one of the 29 stat definitions sets {@code flags & 0x4}
     * (id 26 Invention, table id 0); every other stat, 27 Archaeology and 28 Necromancy
     * included, carries no table id and therefore uses the client's built-in default
     * series, which the cache states as enum 716. Enum 10866 (skill -> xp-table enum,
     * {@code defaultInt = 716}, single override {@code 26 -> 10699}) is the same split.
     * {@link #expArray} equals enum 716 for levels 1..120 and
     * {@link #INVENTION_XP_FOR_LEVEL} equals enum 10699 / cache table 0, both asserted
     * by the evidence decoder. See {@code verified/ui/STAT_DEFINITIONS.md} section 2.
     */
    private static int[] getXpTable(int skill) {
        return skill == INVENTION ? ELITE_XP_TABLE : expArray;
    }

    /**
     * Cache table 0 / enum 10699, re-based to the same 0-indexed-by-{@code level - 1}
     * shape as {@link #expArray}. {@link #INVENTION_XP_FOR_LEVEL} keeps its historical
     * 1-indexed layout (leading padding zero) because callers outside this class index
     * it by level directly.
     */
    private static final int[] ELITE_XP_TABLE = buildEliteXpTable();

    private static int[] buildEliteXpTable() {
        int[] table = new int[INVENTION_XP_FOR_LEVEL.length - 1];
        for (int level = 1; level < INVENTION_XP_FOR_LEVEL.length; level++)
            table[level - 1] = INVENTION_XP_FOR_LEVEL[level];
        return table;
    }

    /**
     * Cumulative experience required for {@code level} in {@code skill}, on that
     * skill's own cache curve. Levels below 1 read as 1 and levels past the end of the
     * curve read as its last entry, which is what the 910 code did with its
     * {@code level > 119 ? 119} clamp - but per curve, so Invention now answers up to
     * level 150 instead of silently reading the default table.
     */
    public static int getXPForLevel(int skill, int level) {
        int[] table = getXpTable(skill);
        if (level < 1)
            level = 1;
        if (level > table.length)
            level = table.length;
        return table[level - 1];
    }

    public void addSkillXpRefresh(int skill, double xp) {
        this.xp[skill] += xp;
        if (this.xp[skill] > MAXIMUM_EXP)
            this.xp[skill] = MAXIMUM_EXP;
        level[skill] = (short) getLevelForXp(skill);
    }

    private double getXPRates() {
        if (player.isExpert())
            return Settings.VET_XP;
        if (player.isNovice())
            return Settings.INTERM_XP;
        if (player.isLegendary())
            return Settings.EXPERT_XP;
        if (player.isNoviceIronMan())
            return Settings.INTERM_XP;
        if (player.isExpertIronMan())
            return Settings.VET_XP;
        if (player.isIntermediate())
            return Settings.INTERMEDIATE_XP;
        if (player.isIntermediateIronMan())
            return Settings.INTERMEDIATE_XP;
        if (player.isKingOfTheSkillGameMode()) {
            return Settings.KING_OF_THE_SKILL_XP;
        }
        return Settings.IRONMAN_XP;
    }

    public double xpNoBonus(int skill, double exp) {
        return exp / (getXPRates() * ((World.isWellActive() || World.isWeekend()) ? 2 : 1));
    }

    public static boolean hasCamouflageOutfit(Player player, boolean master) {
        if (!master) {
            if (player.getEquipment().getHatId() == 37343 && player.getEquipment().getChestId() == 37344 && player.getEquipment().getLegsId() == 37345 && player.getEquipment().getGlovesId() == 37346 && player.getEquipment().getBootsId() == 37347)
                return true;
            if (player.getEquipment().getHatId() == 37348 && player.getEquipment().getChestId() == 37349 && player.getEquipment().getLegsId() == 37350 && player.getEquipment().getGlovesId() == 37351 && player.getEquipment().getBootsId() == 37352)
                return true;
            return player.getEquipment().getHatId() == 37353 && player.getEquipment().getChestId() == 37354 && player.getEquipment().getLegsId() == 37355 && player.getEquipment().getGlovesId() == 37356 && player.getEquipment().getBootsId() == 37357;
        } else {
            return player.getEquipment().getHatId() == 37358 && player.getEquipment().getChestId() == 37359 && player.getEquipment().getLegsId() == 37360 && player.getEquipment().getGlovesId() == 37361 && player.getEquipment().getBootsId() == 37362;
        }
    }

    public boolean hasHunterOutfit(boolean master) {
        if (!master) {
            if (player.getEquipment().getHatId() == 41008 && player.getEquipment().getChestId() == 41009 && player.getEquipment().getLegsId() == 41010 && player.getEquipment().getGlovesId() == 41011 && player.getEquipment().getBootsId() == 41012)
                return true;
            if (player.getEquipment().getHatId() == 41013 && player.getEquipment().getChestId() == 41014 && player.getEquipment().getLegsId() == 41015 && player.getEquipment().getGlovesId() == 41016 && player.getEquipment().getBootsId() == 41017)
                return true;
            return player.getEquipment().getHatId() == 41018 && player.getEquipment().getChestId() == 41019 && player.getEquipment().getLegsId() == 41020 && player.getEquipment().getGlovesId() == 41021 && player.getEquipment().getBootsId() == 41022;
        } else {
            return player.getEquipment().getHatId() == 41023 && player.getEquipment().getChestId() == 41024 && player.getEquipment().getLegsId() == 41025 && player.getEquipment().getGlovesId() == 41026 && player.getEquipment().getBootsId() == 41027;
        }
    }

    public double hunterBonus() {
        double xpBoost = 1.0;
        if (hasHunterOutfit(false))
            xpBoost *= 1.07;
        if (hasHunterOutfit(true))
            xpBoost *= 1.1;
        return xpBoost;
    }

    public double artisansBonus() {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 25185)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 32281)
            xpBoost *= 1.03;
        if (player.getEquipment().getChestId() == 25186)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 25187)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 25188)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 25189)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 25185 && player.getEquipment().getChestId() == 25186 && player.getEquipment().getLegsId() == 25187 && player.getEquipment().getBootsId() == 25188 && player.getEquipment().getGlovesId() == 25189)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 32281 && player.getEquipment().getChestId() == 25186 && player.getEquipment().getLegsId() == 25187 && player.getEquipment().getBootsId() == 25188 && player.getEquipment().getGlovesId() == 25189)
            xpBoost *= 1.03;
        return xpBoost;

    }

    public double nimbleBonus() {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 36894)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 36893)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 36892)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 36891)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 36890)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 36894 && player.getEquipment().getChestId() == 36893 && player.getEquipment().getLegsId() == 36892 && player.getEquipment().getBootsId() == 36891 && player.getEquipment().getGlovesId() == 36890)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 36894 && player.getEquipment().getChestId() == 36893 && player.getEquipment().getLegsId() == 36892 && player.getEquipment().getBootsId() == 36891 && player.getEquipment().getGlovesId() == 36890)
            xpBoost *= 1.03;
        return xpBoost;
    }

    public double fletchersBonus() {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 36899)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 36898)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 36897)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 36895)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 36896)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 36899 && player.getEquipment().getChestId() == 36898 && player.getEquipment().getLegsId() == 36897 && player.getEquipment().getBootsId() == 36895 && player.getEquipment().getGlovesId() == 36896)
            xpBoost *= 1.01;
        return xpBoost;
    }

    public double farmersBonus() {
        double xpBoost = 1.0;
        boolean hasFarmerHat = player.getEquipment().getHatId() == 31347 || player.getEquipment().getHatId() == 34926;
        if (hasFarmerHat)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 31346)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 31345)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 31343)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 31344)
            xpBoost *= 1.01;
        if (hasFarmerHat && player.getEquipment().getChestId() == 31346 && player.getEquipment().getLegsId() == 31345 && player.getEquipment().getBootsId() == 31343 && player.getEquipment().getGlovesId() == 31344)
            xpBoost *= 1.01;
        return xpBoost;
    }

    public double getExperienceMultiplier(int skill) {
        return 0;
    }

    private final double getBrawlersBoost(int skill) {
        if (player.getEquipment().getGlovesId() == -1)
            return 1;
        final int glovesId = Revenant.getGloves(skill);
        if (glovesId == -1)
            return 1;
        if (player.getEquipment().getGlovesId() != glovesId)
            return 1;
        if (Wilderness.isAtWild(player) && Wilderness.getWildLevel(player) > 46)
            return 4;
        return 1.5;
    }

    public double addXp(int skill, double xp) {
        return addXp(skill, xp, true, true);
    }



    // 1) At class scope (outside any method): custom xp curve
    private static double levelXpMultiplier(int level, boolean combat) {
        return combat ? 7.0 : 3.5;
    }


    public double addXp(int skill, double exp, boolean activateShb, boolean usePrismatic) {
        //if (!player.getAccountPin().hasEnteredPin()) {
        //  return 0;
        //}
        if (player.isXpLocked() && skill != 18)
            return 0;
        if (skill < 0 || skill >= SKILL_COUNT || !Double.isFinite(exp) || exp <= 0)
            return 0;
        if (player.isNative950()) {
            // Keep the 910 Skills engine as the single XP owner. Native admission does
            // not create the seasonal/pet/invention/social managers used below.
            // Its supported awards share the account rate, combat curve, level/cap,
            // trackers and refresh path, without entering those unported callbacks.
            return commitExperience(skill, applyExperienceRate(skill, exp), true);
        }
        // M3: the juju pillar manager is a legacy-login field. Native 947 admission
        // builds it in Native950Interactions.ensureLegacyFields, but a Player that
        // never opened a session (tests, tools) has none and addXp must not NPE.
        if (player.harmonyPillars != null)
            player.harmonyPillars.addXp(skill, exp);
        player.getControlerManager().trackXP(skill, (int) exp);
        if (getBrawlersBoost(skill) > 1) {
            final Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
            final double experience = exp;
            if (gloves != null) {
                exp *= getBrawlersBoost(skill);
                if (skill > 4 && skill != 6)
                    player.getEquipment().getItem(Equipment.SLOT_HANDS).setCharges(gloves.getCharges() - 1);
                else
                    player.getEquipment().getItem(Equipment.SLOT_HANDS).setCharges(gloves.getCharges() - (int) (exp - experience));
                if (player.getEquipment().getItem(Equipment.SLOT_HANDS).getCharges() <= 0) {
                    player.sendMessage("Your " + gloves.getName() + " have degraded to dust.");
                    player.getEquipment().set(Equipment.SLOT_HANDS, null);
                    player.getEquipment().refresh(Equipment.SLOT_HANDS);
                    player.getAppearence().generateAppearenceData();
                }
            }
        }








        exp = applyExperienceRate(skill, exp);

        boolean notAffectedByDXP = player.isIronMan() || player.isHCIronMan() || player.isKingOfTheSkillGameMode() || player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan();
        exp *= (Settings.DXP ? (notAffectedByDXP ? 1 : 1) : 1);





        /** Equipment experience increases **/
        if (player.getEquipment().getCapeId() == 37694 && skill != Skills.ATTACK && skill != Skills.DEFENCE && skill != Skills.STRENGTH && skill != Skills.MAGIC && skill != Skills.RANGE) {
            exp *= 1.05;
        }
        if (player.getEquipment().getCapeId() == 25450 && skill != Skills.ATTACK && skill != Skills.DEFENCE && skill != Skills.STRENGTH && skill != Skills.MAGIC && skill != Skills.RANGE) {
            exp *= 1.10;
        }
        if (ChristmasSeasonalEvent.useCmasAmulet(player)) {
            exp *= 1.25;
        }
        if (skill == Skills.DIVINATION)
            exp *= DivineObject.divinationSuit(player);
        if (skill == Skills.THIEVING)
            exp *= Thieving.outfitBoost(player);
        if (skill == Skills.RUNECRAFTING)
            exp *= RuneCrafting.runecrafterSuit(player);
        if (skill == Skills.COOKING)
            exp *= Cooking.chefsSuit(player);
        if (skill == Skills.FLETCHING) {
            exp *= fletchersBonus();
            if (Utils.random(200) == 0 && player.hasRandomEvent()) {
                if (!player.followedByRandomEventNPC()) {
                    NPC npc = new FletchingRandomEvent(player, player);
                    if (npc.withinDistance(player, 14))
                        player.setCurrentRandomEventNPC(npc);
                }
            }
        }
        if (skill == FARMING && player.jujuPotions.isActive(Pots.Effects.SARADOMINS_BLESSING_JUJU)) {
            exp *= 1.10;
        }
        if (skill == HERBLORE && player.jujuPotions.isActive(Pots.Effects.PERFECT_HERBLORE_JUJU)) {
            exp *= 1.15;
        }
        if (skill == HUNTER && player.jujuPotions.isActive(Pots.Effects.ZAMORAKS_FAVOUR_JUJU)) {
            exp *= 1.10;
        }
        if (skill == MINING && player.jujuPotions.isActive(Pots.Effects.PERFECT_MINING_JUJU)) {
            exp *= 1.15;
        }
        if (skill == SMITHING && player.jujuPotions.isActive(Pots.Effects.PERFECT_SMITHING_JUJU)) {
            exp *= 1.15;
        }
        if (skill == PRAYER && player.jujuPotions.isActive(Pots.Effects.PERFECT_PRAYER_JUJU)) {
            exp *= 1.15;
        }
        if (skill == DUNGEONEERING && player.jujuPotions.isActive(Pots.Effects.PERFECT_DUNGEONEERING_JUJU)) {
            exp *= 1.15;
        }
        if (ChristmasSeasonalEvent.skillGivesPresent(skill)) {
            int chance = 1;
            if (skill == THIEVING) {
                chance = 15;
            }
            if (chance == 1 || ThreadLocalRandom.current().nextInt(chance) == 0) {
                ChristmasSeasonalEvent.awardSmallPresent(player);
            }
        }
        if (player.getControlerManager().getControler() instanceof DungeonController)
            if (skill != DUNGEONEERING)
                exp *= player.getGorajanTrailblazer().getExperienceBoost();

        if (!Skills.isCombatSkill(skill) && activateShb) {
            if (player.getEquipment().getBootsId() >= 30920 && player.getEquipment().getBootsId() <= 30924) {
                Item boots = player.getEquipment().getItem(Equipment.SLOT_FEET);
                if (boots.getCharges() > 0) {
                    if (!player.shSkillingCooldown.isRunning()) {
                        player.shSkillingCooldown.start();
                    }
                    if (player.shSkillingCooldown.elapsed(TimeUnit.SECONDS) >= 30) {
                        boots.setCharges(boots.getCharges() - 1);
                        player.sendMessage("<col=b3b3b3>Your Silverhawk boots have granted you some experience.", true);
                        player.setNextGraphics(new Graphics(4605));
                        addXp(Skills.AGILITY, XPLamps.getExp(getLevelForXp(Skills.AGILITY), 0) * 0.10, false, true);
                        player.shSkillingCooldown.reset().start();
                    }
                }
            }
        }
        if (skill == Skills.CRAFTING) {
            exp *= artisansBonus();
            if (Utils.random(250) == 0 && player.hasRandomEvent()) {
                if (!player.followedByRandomEventNPC()) {
                    NPC npc = new CraftingRandomEvent(player, player);
                    if (npc.withinDistance(player, 14))
                        player.setCurrentRandomEventNPC(npc);
                }
            }
        }

        if (skill == Skills.AGILITY)
            exp *= nimbleBonus();

        if (skill == Skills.SUMMONING) {
            exp *= Summoning.shamanSuit(player);
            if (Utils.random(200) == 0 && player.hasRandomEvent()) {
                if (!player.followedByRandomEventNPC()) {
                    NPC npc = new SummoningRandomEvent(player, player);
                    if (npc.withinDistance(player, 14))
                        player.setCurrentRandomEventNPC(npc);
                }
            }
        }

        if (skill == Skills.FARMING) {
            exp *= farmersBonus();
        }
        if (player.getAuraManager().usingWisdom())
            exp *= 1.25;
        if (player.getAuraManager().usingGWisdom())
            exp *= 1.35;
        if (player.getAuraManager().usingMWisdom())
            exp *= 1.50;
        if (player.getAuraManager().usingSWisdom())
            exp *= 1.60;
        if (player.getAuraManager().usingLWisdom())
            exp *= 1.75;

        /** Try/Catch just for your sanity, Noel. */
        if (player.hasBonusEXP()) {
            try {
                exp *= (Double.parseDouble(getPercentage()) / 100) + 1;
            } catch (Exception e) {
                Logger.getGlobal().error("Wrongful var parsed as byte.", e);
            }
        }
//        /**
//         * Skilling pets
//         */
//        for (SkillingPets pet : SkillingPets.values()) {
//            if (player.getTemporaryAttributtes().remove("dungxpbuy") != null)
//                break;
//            if (skill == pet.getSkillId()) {
//                final int chance = pet.getChance() / (player.getPerkManager().petChanter ? 2 : 1);
//                if (Utils.random(chance) == 0) {
//                    if (!player.hasItem(new Item(pet.getItemId(), 1))) {
//                        if (player.getInventory().hasFreeSlots() && !(player.getControlerManager().getControler() instanceof DungeonController)) {
//                            player.getInventory().addItem(new Item(pet.getItemId(), 1));
//                            player.sendMessage(Colors.BLUE + "Congratulations! You've unlocked " + ItemDefinitions.getItemDefinitions(pet.getItemId()).getName() + ", the " + Utils.formatPlayerNameForDisplay(pet.toString()) + " pet!");
//                            World.sendWorldMessage("<col=4286f4><img=6>News: Congratulations! " + player.getDisplayName() + " has unlocked " + ItemDefinitions.getItemDefinitions(pet.getItemId()).getName() + ", the " + Utils.formatPlayerNameForDisplay(pet.toString()) + " pet!", false);
//                        } else {
//                            player.getBank().addItem(new Item(pet.getItemId(), 1), true);
//                            player.sendMessage(Colors.BLUE + "Congratulations! You've unlocked " + ItemDefinitions.getItemDefinitions(pet.getItemId()).getName() + ", the " + Utils.formatPlayerNameForDisplay(pet.toString()) + " pet!");
//                            player.sendMessage("It has ran to your bank.");
//                            World.sendWorldMessage("<col=4286f4><img=6>News: Congratulations! " + player.getDisplayName() + " has unlocked " + ItemDefinitions.getItemDefinitions(pet.getItemId()).getName() + ", the " + Utils.formatPlayerNameForDisplay(pet.toString()) + " pet!", false);
//                        }
//                        player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_PET);
//                    } else {
//                        addXp(skill, 50000);
//                        player.sendMessage("You have a strange feeling you would've been followed. You feel a surge of experience flow through you instead.");
//                    }
//                }
//                break;
//            }
//        }

        if (skill == Skills.HERBLORE) {
            if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                exp *= 1.25;
        }
        if (skill == Skills.PRAYER) {
            if (player.getPerkManager().hasPerkActive(DonationPerk.PRAYER_BETRAYER))
                exp *= 1.25;
        }
        if (skill == Skills.DIVINATION) {
            if (player.getPerkManager().hasPerkActive(DonationPerk.DEDICATED_DIVINATION))
                exp *= 1.25;
        }
        if (skill == Skills.HUNTER) {
            if (player.getPerkManager().hasPerkActive(DonationPerk.HUNTSMAN))
                exp *= 1.25;
            exp *= hunterBonus();
        }

        final boolean isIronman = player.isIronMan() || player.isHCIronMan() || player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan();
        final boolean isKingOfTheSkill = player.isKingOfTheSkillGameMode();

        if (Settings.TRIPLE_EXP_ENABLED && !isKingOfTheSkill) {
            exp *= (isIronman ? 1.0 : 1.0);
        } else if (player.getVoteDoubleXpTimeRemaining() > System.currentTimeMillis()) {
            exp *= 1.0;
        } else if (World.isWeekend() && (!isIronman && !isKingOfTheSkill)) {
            exp *= 1.0;
        } else if (World.isWellActive() && (!isIronman && !isKingOfTheSkill)) {
            exp *= 1.0;
        }

        /**
         * 1.1% extra exp if the member is in the members zone & 1.2x for Plat
         */
//        if (player.isPlatinumDonor())
//            exp *= 1.1;
//        if (player.isDiamondDonor())
//            exp *= 1.2;
//        if (player.isMasterDonator())
//            exp *= 1.3;

        exp *= getSkeletonSuitBonus();

        exp *= getLunarfurySuitBonus();

        if (player.getCurrentPet() != null) {
            if (player.getCurrentPet().getPerks().contains(PetPerk.NICE_BUT_DIM)) {
                int tier = PetPerkUtils.getPerkTier(PetPerk.NICE_BUT_DIM, player.getCurrentPet());
                double multiplier = tier == 1 ? 0.1 : tier == 2 ? 0.05 : tier == 3 ? 0.03 : 0;
                exp -= (exp * multiplier);
            }
        }

//        if (HomeAreaHandler.playerIsAtHome(player)) {
//            exp *= HomeAreaHandler.getExperienceModifierForPlayer(player);
//        }

        exp *= player.experienceModifier();

        SkillingPets.rollForPetDrop(player, skill, exp);

        if (bonusPrismaticXp > 1 && !Skills.isCombatSkill(skill) && usePrismatic) {
            double previousBonusXp = bonusPrismaticXp;
            if (bonusPrismaticXp > exp) {
                bonusPrismaticXp -= exp;
                exp *= 2;
            } else {
                exp += bonusPrismaticXp;
                bonusPrismaticXp = 0;
            }

            if (bonusPrismaticXp > 0) {
                if (bonusPrismaticXp < 50_000 && previousBonusXp >= 50_000) {
                    player.sendMessage(Colors.RED + "Your prismatic bonus XP is getting low!");
                } else if (bonusPrismaticXp < 10_000 && previousBonusXp >= 10_000) {
                    player.sendMessage(Colors.RED + "Your prismatic bonus XP is going to run out soon!");
                } else if (bonusPrismaticXp < 2500 && previousBonusXp >= 2500) {
                    player.sendMessage(Colors.RED + "Your prismatic bonus XP is about to run out!");
                }
            } else if (bonusPrismaticXp == 0) {
                player.sendMessage(Colors.RED + "Your prismatic bonus XP has ran out!");
            }
        }
        double xpBonus = xpBonuses[skill] - exp >= 0 ? exp : xpBonuses[skill];
        exp += xpBonus;
        setXpBonuses(skill, xpBonuses[skill] - xpBonus);
        player.getVarBitManager().sendVar(95, (int) xpBonus * 10);
        if (player.isGroupIronman() && GIM.getEventManager().isRunning(GIMEventType.XP)) {
            exp *= 2;
        }
        double bonusXpWise = 0;
        Perk wise = player.getInventionManager().hasPerk(Perks.WISE);
        if (wise != null) {
            bonusXpWise = exp * 0.01;
            if (dailyWiseXP + bonusXpWise >= 500000) {
                bonusXpWise = 500000 - dailyWiseXP;
                if (dailyWiseXP != 500000)
                    player.getPackets().sendGameMessage("<col=ff0000>You have used all the bonus xp from wise perk effect for today.");
            }
            dailyWiseXP += bonusXpWise;
        }
        exp += bonusXpWise;
        return commitExperience(skill, exp, false);
    }

    /** Account rates and the original custom combat/skilling curve, shared by both clients. */
    private double applyExperienceRate(int skill, double exp) {
        exp *= getXPRates() / (skill == INVENTION ? 5.0 : 1.0);
        int skillLevel = getLevelForXp(skill);
        if (skillLevel > 0) exp *= levelXpMultiplier(skillLevel, isCombatSkill(skill));
        return exp;
    }

    /** The original XP/level commit; native callers omit only unported content callbacks. */
    private double commitExperience(int skill, double exp, boolean nativeClient) {
        int oldLevel = getLevelForXp(skill);
        int oldXP = (int) xp[skill];
        if (nativeClient) {
            exp = Math.min(exp, MAXIMUM_EXP - xp[skill]);
            if (exp <= 0) return 0;
            ensureXpTrackerInitialised();
        }
        xp[skill] += exp;

        if (!nativeClient && player.isGroupIronman()) {
            player.gimTracker.incrementXpGained((long) exp);
        }
        for (int i = 0; i < trackSkills.length; i++) {
            if (trackSkills[i]) {
                if (trackSkillsIds[i] == 30 || (trackSkillsIds[i] == 29 && (skill == Skills.ATTACK || skill == Skills.DEFENCE || skill == Skills.STRENGTH || skill == Skills.MAGIC || skill == Skills.RANGE || skill == Skills.HITPOINTS)) || trackSkillsIds[i] == getCounterSkill(skill)) {
                    xpTracks[i] += exp;
                    refreshCounterXp(i);
                }
            }
        }

        if (xp[skill] >= MAXIMUM_EXP)
            xp[skill] = MAXIMUM_EXP;

        /*
         * if ((player.isIronMan() || player.isLegendary() || player.isHCIronMan()) &&
         * xp[skill] >= 100_000_000 && oldXP < 100_000_000) LevelUp.send100m(player,
         * skill);
         */
        if (!nativeClient) {
            announceExperienceMilestones(skill, oldXP);
            sendTotalXpAnnouncements();
        }
        int newLevel = getLevelForXp(skill);
        int levelDiff = newLevel - oldLevel;
        if (newLevel > oldLevel) {
            level[skill] += levelDiff;
            if (!nativeClient && player.isGroupIronman())
                player.gimTracker.incrementLevelsGained(levelDiff);
            announceLevelUp(skill);
            if (skill == SUMMONING || (skill >= ATTACK && skill <= MAGIC)) {
                player.getAppearence().generateAppearenceData();
                if (skill == HITPOINTS)
                    player.heal(levelDiff * 10);
                else if (skill == PRAYER)
                    player.getPrayer().restorePrayer(levelDiff * 10);
                // player.getPrayer().restorePrayer(levelDiff * 10);
            }
            if (!nativeClient) player.getQuestManager().checkCompleted();
        }
        refresh(skill);
        if (!nativeClient) handleSkillShards(skill);
        if (skill != HITPOINTS) {
//            if (player.hasBonusEXP() || player.getAuraManager().usingWisdom())
//                player.getPackets().sendConfig(2044, (int) (exp * 10) / 4);
//            else if (World.isWeekend() || World.isWellActive())
//                player.getPackets().sendConfig(2044, (int) (exp * 10) / 2);
        }
        return exp;
    }

    /**
     * Like {@code addXp} but doesn't apply any multipliers (except for XP rate) or
     * notify any listeners.
     */
    public void silentAddXp(int skill, double exp) {
        exp *= getXPRates();

        int oldLevel = getLevelForXp(skill);
        int oldXP = (int) xp[skill];
        xp[skill] += exp;
        for (int i = 0; i < trackSkills.length; i++) {
            if (trackSkills[i]) {
                if (trackSkillsIds[i] == 30 || (trackSkillsIds[i] == 29 && (skill == Skills.ATTACK || skill == Skills.DEFENCE || skill == Skills.STRENGTH || skill == Skills.MAGIC || skill == Skills.RANGE || skill == Skills.HITPOINTS)) || trackSkillsIds[i] == getCounterSkill(skill)) {
                    xpTracks[i] += exp;
                    refreshCounterXp(i);
                }
            }
        }

        if (xp[skill] >= MAXIMUM_EXP)
            xp[skill] = MAXIMUM_EXP;

        /*
         * if ((player.isIronMan() || player.isLegendary() || player.isHCIronMan()) &&
         * xp[skill] >= 100_000_000 && oldXP < 100_000_000) LevelUp.send100m(player,
         * skill);
         */
        announceExperienceMilestones(skill, oldXP);

        int newLevel = getLevelForXp(skill);
        int levelDiff = newLevel - oldLevel;
        if (newLevel > oldLevel) {
            level[skill] += levelDiff;
            announceLevelUp(skill);
            if (skill == SUMMONING || (skill >= ATTACK && skill <= MAGIC)) {
                player.getAppearence().generateAppearenceData();
                if (skill == HITPOINTS)
                    player.heal(levelDiff * 10);
                else if (skill == PRAYER)
                    player.getPrayer().restorePrayer(levelDiff * 10);
                // player.getPrayer().restorePrayer(levelDiff * 10);
            }
            player.getQuestManager().checkCompleted();
        }
        refresh(skill);
        handleSkillShards(skill);
    }

    public static boolean isCombatSkill(int skill) {
        switch (skill) {
            case HITPOINTS:
            case ATTACK:
            case STRENGTH:
            case DEFENCE:
            case RANGE:
            case SUMMONING:
            case MAGIC:
                case SLAYER:
            case PRAYER:
                return true;
            default:
                return false;
        }
    }

    public int drainLevel(int skill, int drain) {
        int drainLeft = drain - level[skill];
        if (drainLeft < 0)
            drainLeft = 0;
        level[skill] -= drain;
        if (level[skill] < 0)
            level[skill] = 0;
        refresh(skill);
        return drainLeft;
    }

    public void drainSummoning(int amt) {
        int level = getLevel(Skills.SUMMONING);
        if (level == 0)
            return;
        set(Skills.SUMMONING, amt > level ? 0 : level - amt);
    }

    private void refreshSummoningPoints() {
        player.getPackets().sendConfigByFile(41524, getLevel(Skills.SUMMONING) * 100);
    }

    /**
     * The client's own combat level, cache script 1432, on the NON-MEMBERS branch.
     *
     * <p>The 947 client computes and displays this itself, so a server formula that
     * disagrees is a visible bug. Script 1432 is straight-line and has two branches
     * selected by {@code op_034F} (is-members-world); this method is the
     * {@code op_034F() != 1} one, which substitutes a literal {@code 1} for
     * {@code floor(Summoning/2)} and does not read stat 23 at all.
     * {@link #getCombatLevelWithSummoning()} is the members branch.
     *
     * @see #clientCombatLevel(boolean)
     */
    public int getCombatLevel() {
        return clientCombatLevel(false);
    }

    /**
     * The client's own combat level, cache script 1432, on the MEMBERS branch (the one
     * that reads Summoning). This is the value the appearance block and varp 1000 carry.
     *
     * @see #clientCombatLevel(boolean)
     */
    public int getCombatLevelWithSummoning() {
        return clientCombatLevel(true);
    }

    /**
     * Cache script 1432, instruction for instruction
     * ({@code verified/ui/STAT_DEFINITIONS.md} section 5.1):
     *
     * <pre>
     * m = max(base(Attack) + base(Strength), 2*base(Ranged), 2*base(Magic), 2*base(Necromancy))
     * a = (m * 13) / 10
     * combat = (a + base(Defence) + base(Constitution) + base(Prayer)/2
     *             + (members ? base(Summoning)/2 : 1)) / 4
     * </pre>
     *
     * Every division is CS2 integer division and truncates where it appears; all the
     * operands are non-negative, so Java's {@code /} is the same operation. There is no
     * final clamp in the script - the 910 code's {@code min(126)} / {@code min(138)} do
     * not exist client side, and the cache caps allow a maximum of 152.
     *
     * <p>Four things changed from the 910 formula, all because the client says so:
     * Ranged and Magic weigh x2 rather than x1.5; Necromancy joins the same {@code max}
     * as a fourth candidate; the rounding happens at {@code /10}, at each {@code /2}
     * and at {@code /4} instead of once at the end in floating point; and the clamps are
     * gone.
     */
    private int clientCombatLevel(boolean members) {
        int melee = getLevelForXp(ATTACK) + getLevelForXp(STRENGTH);
        int ranged = getLevelForXp(RANGE) * 2;
        int magic = getLevelForXp(MAGIC) * 2;
        int necromancy = getLevelForXp(NECROMANCY) * 2;
        int highest = melee;
        if (ranged > highest)
            highest = ranged;
        if (magic > highest)
            highest = magic;
        if (necromancy > highest)
            highest = necromancy;
        int weighted = (highest * 13) / 10;
        int tail = members ? getLevelForXp(SUMMONING) / 2 : 1;
        return (weighted + getLevelForXp(DEFENCE) + getLevelForXp(HITPOINTS)
                + getLevelForXp(PRAYER) / 2 + tail) / 4;
    }

    /**
     * The per-skill experience milestones, rebuilt around the 947 ceiling.
     *
     * <p>MAXIMUM_EXP is now 200,000,000 (cache, see the constant), so the old 250m,
     * 500m, 1000m, 1500m and 2000m announcements are unreachable by construction and
     * are gone; {@code LevelUp.send250m}..{@code send2000m} are left in place untouched
     * for any other caller. Two milestones remain:
     *
     * <ul>
     * <li>104,273,167 - the default curve's level 120, so it is only a milestone for a
     *     skill the cache caps at 120 on the default curve. It used to fire for every
     *     skill except Invention, which meant a skill capped at 99 or 110 got a
     *     "you have achieved level 120" world announcement at a level it can never
     *     reach.</li>
     * <li>MAXIMUM_EXP itself - a plain message, because the only existing world
     *     announcement for a ceiling is {@code send2000m}, whose text says
     *     "2,000,000,000" and would now be a lie.</li>
     * </ul>
     */
    private void announceExperienceMilestones(int skill, double oldXP) {
        if (skill != INVENTION && getLevelCap(skill) == 120
                && oldXP < 104273167 && xp[skill] >= 104273167)
            LevelUp.send104m(player, skill);
        if (oldXP < MAXIMUM_EXP && xp[skill] >= MAXIMUM_EXP)
            player.sendMessage(Colors.RED + "You have reached the maximum experience of "
                    + Utils.formatNumber((long) MAXIMUM_EXP) + " in " + SKILL_NAME[skill] + ".");
    }

    public int getCounterSkill(int skill) {
        switch (skill) {
            case ATTACK:
                return 0;
            case STRENGTH:
                return 1;
            case DEFENCE:
                return 4;
            case RANGE:
                return 2;
            case HITPOINTS:
                return 5;
            case PRAYER:
                return 6;
            case AGILITY:
                return 7;
            case HERBLORE:
                return 8;
            case THIEVING:
                return 9;
            case CRAFTING:
                return 10;
            case MINING:
                return 12;
            case SMITHING:
                return 13;
            case FISHING:
                return 14;
            case COOKING:
                return 15;
            case FIREMAKING:
                return 16;
            case WOODCUTTING:
                return 17;
            case SLAYER:
                return 19;
            case FARMING:
                return 20;
            case CONSTRUCTION:
                return 21;
            case HUNTER:
                return 22;
            case SUMMONING:
                return 23;
            case DUNGEONEERING:
                return 24;
            case DIVINATION:
                return 25;
            case INVENTION:
                return 26;
            case MAGIC:
                return 3;
            case FLETCHING:
                return 18;
            case RUNECRAFTING:
                return 11;
            default:
                return -1;
        }
    }

    public int getLevel(int skill) {
        return (skill < 0 || skill >= SKILL_COUNT) ? 1 : level[skill];
    }

    private static final int[] expArray = {0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833, 16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431, 14391160, 15889109, 17542976, 19368992, 21385073, 23611006, 26068632, 28782069, 31777943, 35085654, 38737661, 42769801, 47221641, 52136869, 57563718, 63555443, 70170840, 77474828, 85539082, 94442737, 104273167};

    /**
     * The base level for the stored experience, on this skill's cache curve and
     * clamped by its cache cap.
     *
     * <p>This is the server's copy of what the 947 client does for itself on every
     * UPDATE_STAT ({@code base = min(entriesBelowXp + def+0x20, def+4)}, setter
     * {@code 0x140369C10}), so the two agree by construction. It replaces the 910 rule
     * of "120 for Dungeoneering, Slayer and Invention, 99 for everything else": 15
     * further skills are capped above 99 by the cache. See
     * {@code notes/SKILLS-27-28.md} for the full before/after table.
     */
    public int getLevelForXp(int skill) {
        return getLevelForXp(skill, skill < 0 || skill >= xp.length ? 0 : xp[skill]);
    }

    /** {@link #getLevelForXp(int)} against an arbitrary experience value; hermetic, no player state. */
    public static int getLevelForXp(int skill, double experience) {
        int[] table = getXpTable(skill);
        int highest = getLevelCap(skill);
        if (highest > table.length)
            highest = table.length;
        for (int level = highest; level > 1; level--) {
            if (table[level - 1] <= experience)
                return level;
        }
        return 1;
    }

    /**
     * Historically "the level for xp, but always on the 120 scale", used by the Slayer
     * requirement check in PlayerCombat. Slayer's cache cap IS 120, so this is now just
     * {@link #getLevelForXp(int)} and is kept only so that call site keeps compiling.
     */
    public int getSlayerLevelForXp(int skill) {
        return getLevelForXp(skill);
    }

    public short[] getLevels() {
        return level;
    }

    /**
     * The stat's display name, straight out of {@link #SKILL_NAME}, i.e. out of
     * enum 680. This used to be a second hand-written switch that could drift from
     * the array - and did: it called stat 3 "Hitpoints" where the cache (and the
     * array) call it "Constitution".
     */
    public String getSkillName(int skill) {
        return skill < 0 || skill >= SKILL_COUNT ? "Null" : SKILL_NAME[skill];
    }

    public int getTotalLevel(Player player) {
        int totallevel = 0;
        for (int i = 0; i < level.length; i++)
            totallevel += player.getSkills().getLevelForXp(i);
        return totallevel;
    }

    public double[] getXp() {
        return xp;
    }

    public double getXp(int skill) {
        return (skill < 0 || skill >= SKILL_COUNT) ? 0 : xp[skill];
    }

    public void handleSetupXPCounter(int componentId) {
        if (componentId >= 22 && componentId <= 24)
            setCurrentCounter(componentId - 22);
        else if (componentId == 27)
            switchTrackCounter();
        else if (componentId == 61)
            resetCounterXP();
        else if (componentId >= 31 && componentId <= 57)
            if (componentId == 33)
                setCounterSkill(4);
            else if (componentId == 34)
                setCounterSkill(2);
            else if (componentId == 35)
                setCounterSkill(3);
            else if (componentId == 42)
                setCounterSkill(18);
            else if (componentId == 49)
                setCounterSkill(11);
            else
                setCounterSkill(componentId >= 56 ? componentId - 27 : componentId - 31);
    }

    public boolean hasRequiriments(int... skills) {
        for (int i = 0; i < skills.length; i += 2) {
            int skillId = skills[i];
            int skillLevel = skills[i + 1];
            if (getLevelForXp(skillId) < skillLevel)
                return false;
        }
        return true;
    }

    /**
     * Defensive recovery for the XP counter state. Old saves can deserialise
     * with these fields null (added after the save was created) or with all
     * slots toggled off (user clicked them all off and never re-enabled one),
     * which leaves the counter HUD stuck on the "Start skilling to populate
     * the XP counter..." placeholder regardless of how much XP is actually
     * gained. This guarantees slot 0 is enabled and tracking Total XP, which
     * is the constructor default for new accounts.
     */
    public void ensureXpTrackerInitialised() {
        boolean changed = false;
        if (xpTracks == null) {
            xpTracks = new double[3];
            changed = true;
        }
        if (trackSkills == null) {
            trackSkills = new boolean[3];
            changed = true;
        }
        if (trackSkillsIds == null) {
            trackSkillsIds = new byte[3];
            for (int i = 0; i < trackSkillsIds.length; i++) {
                trackSkillsIds[i] = 30; // 30 = "Total XP" pseudo-skill
            }
            changed = true;
        }
        boolean anyEnabled = false;
        for (boolean s : trackSkills) {
            if (s) { anyEnabled = true; break; }
        }
        if (!anyEnabled) {
            trackSkills[0] = true;
            changed = true;
        }
        if (changed && player != null) {
            // Log to console so it's easy to confirm the recovery fired.
            System.out.println("[XPCounter] recovered tracker state for "
                    + player.getUsername()
                    + " (slot0=" + trackSkills[0]
                    + " skill0=" + trackSkillsIds[0] + ")");
        }
    }

    /** Raw access for diagnostic commands; not for general use. */
    public boolean[] getTrackSkillsRaw() {
        return trackSkills;
    }

    /** Raw access for diagnostic commands; not for general use. */
    public byte[] getTrackSkillsIdsRaw() {
        return trackSkillsIds;
    }

    /** Raw access for diagnostic commands; not for general use. */
    public double[] getXpTracksRaw() {
        return xpTracks;
    }

    /**
     * Hard reset of the XP counter back to defaults (slot 0 enabled, all
     * three slots set to Total XP, all accumulated XP zeroed) and push the
     * fresh state to the client. Use when the on-screen counter is stuck.
     */
    public void resetXpCounter() {
        xpTracks = new double[3];
        trackSkills = new boolean[3];
        trackSkillsIds = new byte[3];
        trackSkills[0] = true;
        for (int i = 0; i < trackSkillsIds.length; i++) {
            trackSkillsIds[i] = 30;
        }
        currentCounter = 0;
        sendXPDisplay();
        refreshCurrentCounter();
        if (player != null) {
            player.sendMessage("XP counter reset. Slot 1 is now tracking Total XP.");
        }
    }

    public void init() {
        if (player.isNative950()) {
            initNative950();
            return;
        }
        for (int skill = 0; skill < level.length; skill++)
            refresh(skill);
        ensureXpTrackerInitialised();
        sendXPDisplay();
        refreshEnabledSkillsTargets();
        refreshUsingLevelTargets();
        refreshSkillsTargetsValues();
        refreshVirtualLeveling();
        refreshXpBonuses();
        refreshXPPopUp();
        refreshXPDisplay();
    }

    /**
     * M3 login burst for a native 947 player: exactly one UPDATE_STAT per modelled
     * skill plus the two CONFIRMED companion vars, and nothing else.
     *
     * <p>The 947 skills tab (interface 1466) has no onLoad hook: per
     * {@code verified/ui/SKILLS_TAB.md} section 2 its builder fires on any stat
     * transmit (the onStatTransmit hook carries no stat filter), so this burst is
     * what draws the panel. The client recomputes the base level from the
     * experience and computes the total and combat levels itself, so the server
     * sends only experience and the CURRENT (possibly boosted or drained) level -
     * which is exactly what {@code sendSkillLevel} encodes.
     *
     * <p>Deliberately NOT run here, unlike the legacy path:
     * <ul>
     * <li>{@link #sendXPDisplay()} / {@link #refreshCounterXp(int)} /
     *     {@link #refreshXPDisplay()} - the XP-counter varps (91..93) and varbits
     *     (225+, 229+, 19964) have no 947 reader; the XP tracker slot (enum 7716
     *     key 1015) is still CANDIDATE in the UI evidence.</li>
     * <li>{@link #refreshXPPopUp()} - it opens interface 1213 in slot 1026, also
     *     CANDIDATE, and would push an unverified interface through the facade.</li>
     * <li>the skill-target refreshes - they resolve ids through
     *     {@code RS3ClientScriptMap.getMap(1482)}, a 910 cache map, and write
     *     varps 1115/1117/1118+ that the 947 tooltip script 547 does not use in
     *     that layout.</li>
     * <li>{@link #refreshXpBonuses()} - varps 3304.. carry XP TARGETS in the 947
     *     scripts, not bonus xp (SKILLS_TAB.md 9.6), so writing bonus values there
     *     would be a wrong binding rather than a missing one.</li>
     * </ul>
     * Skills 27 (Archaeology) and 28 (Necromancy) are part of the model now, so the
     * burst is {@code SKILL_COUNT} frames and the tab no longer renders those two from
     * the client's stat-table initialiser defaults. This is the gap
     * {@code MIGRATION-BACKLOG.md} section 10 recorded.
     */
    private void initNative950() {
        ensureXpTrackerInitialised(); // packet-free; addXp dereferences the tracker arrays
        for (int skill = 0; skill < level.length; skill++)
            player.getPackets().sendSkillLevel(skill);
        refreshVirtualLeveling();
        refreshSummoningPoints();
    }

    private void refreshVirtualLeveling() {
        if (player.isNative950()) {
            // SKILLS_TAB.md 9.6: nothing in cache index 12 writes varbit 19007, so the
            // server must, and VARS.md puts it in the varbit family (the facade
            // sends every varbit on the confirmed VARBIT_LARGE 71). The legacy route
            // (VarBitManager -> VarsManager.updateVarBit) instead recomputes the WHOLE
            // of varp 458 - which also carries 19009 (show-max icon) and bit 30/31 state
            // this server does not model - and needs a cache lookup to find the bit range.
            player.getPackets().sendConfigByFile(19007, virtualLevels ? 1 : 0);
            return;
        }
        player.getVarBitManager().sendVarBit(19007, virtualLevels ? 1 : 0);
    }

    public void passLevels(Player p) {
        this.level = p.getSkills().level;
        this.xp = p.getSkills().xp;
    }

    /**
     * Refreshes the skill by sending a packet to the client.
     *
     * @param skill the Skill ID to refresh.
     */
    public void refresh(int skill) {
        player.getPackets().sendSkillLevel(skill);
        if (player.isNative950()) {
            // The 947 appearance body carries the combat level (and nothing else
            // skill-derived), so only a combat stat can change it. Regenerating it
            // for all SKILL_COUNT stats would rebuild and MD5 the body once per stat per login
            // for no visible difference.
            if (affectsNative950Appearance(skill))
                player.getAppearence().generateAppearenceData();
            // refreshXpBonuses is deliberately skipped: varps 3304.. are the 947
            // XP-TARGET vars (SKILLS_TAB.md 9.6), not bonus xp. See initNative950.
            if (skill == Skills.SUMMONING)
                refreshSummoningPoints();
            return;
        }
        player.getAppearence().generateAppearenceData();
        refreshXpBonuses(skill);
        if (skill == Skills.SUMMONING)
            refreshSummoningPoints();
    }

    /**
     * True for the stats the native 947 appearance body actually encodes, i.e. the
     * ones {@code GlobalPlayerUpdater.buildNative950AppearanceData} reads through
     * {@code getCombatLevel} / {@code getCombatLevelWithSummoning}.
     */
    private static boolean affectsNative950Appearance(int skill) {
        switch (skill) {
        case ATTACK:
        case DEFENCE:
        case STRENGTH:
        case HITPOINTS:
        case RANGE:
        case PRAYER:
        case MAGIC:
        case SUMMONING:
            return true;
        default:
            return false;
        }
    }

    /**
     * Level-up feedback. Legacy players get the unchanged {@code LevelUp} dialogue;
     * a native 947 player gets a verified MESSAGE_GAME line instead, because that
     * dialogue drives the 910 chatbox interfaces (1186/1191 component numbers that
     * the 947 cache refutes, see {@code verified/ui/CHATBOX_DIALOGUE.md}) and the
     * dialogue family is M7 work.
     */
    private void announceLevelUp(int skill) {
        if (player.isNative950()) {
            String name = skill >= 0 && skill < SKILL_NAME.length ? SKILL_NAME[skill] : ("skill " + skill);
            player.getPackets().sendGameMessage("Congratulations, you have just advanced a " + name
                    + " level. You have reached level " + getLevelForXp(skill) + ".");
            return;
        }
        player.getDialogueManager().startDialogue("LevelUp", skill);
    }

    public void refreshCounterXp(int counter) {
        // M3: the 947 XP counter has no verified binding (the XP tracker slot,
        // enum 7716 key 1015, is CANDIDATE and varps 91..93 have no 947 reader).
        // The value is still accumulated in xpTracks; only the
        // unverified varp write is suppressed, so the native drop inventory in
        // notes/P5-router.md does not grow with ids nothing can read.
        if (player != null && player.isNative950())
            return;
        // The on-screen counter HUD is unsupported here (see switchXPDisplay
        // for the reasoning). The legacy varp 91+slot write is preserved
        // because the value is still recorded server-side and may be read
        // by other overlays, but the modern 1801/2475/2476 writes that were
        // tried experimentally are removed - interfaces 1214/1215 have zero
        // components in this cache so those writes went nowhere.
        player.getPackets().sendConfig(91 + counter, (int) (xpTracks[counter] * 10));
    }

    public void refreshCurrentCounter() {
        player.getPackets().sendConfig(96, currentCounter + 1);
    }

    public void resetCounterXP() {
        xpTracks[currentCounter] = 0;
        refreshCounterXp(currentCounter);
    }

    public void resetSkillNoRefresh(int skill) {
        xp[skill] = 0;
        level[skill] = 1;
    }

    public void resetAllSkills() {
        for (int skill = 0; skill < xp.length; skill++) {
            resetSkillNoRefresh(skill);
            refresh(skill);
        }
        player.getInventionManager().reset();
        level[3] = 10;
        xp[3] = 1155; // the cache curve puts level 10 at 1154; 1155 still resolves to 10, so this is left alone
        refresh(3);
    }

    public void restoreSkills() {
        for (int skill = 0; skill < level.length; skill++) {
            level[skill] = (short) getLevelForXp(skill);
            refresh(skill);
        }
    }

    public void restoreSummoning() {
        level[23] = (short) getLevelForXp(23);
        refresh(23);
    }

    public void sendXPDisplay() {
        // Only the legacy varbits (225+, 229+) are written. The 10440-10798
        // varbits target interface 1215 which has zero components in this
        // cache - experimental writes there did nothing visible.
        for (int i = 0; i < trackSkills.length; i++) {
            player.getPackets().sendConfigByFile(229 + i, trackSkills[i] ? 1 : 0);
            player.getPackets().sendConfigByFile(225 + i, trackSkillsIds[i] + 1);
            refreshCounterXp(i);
        }
    }

    public void set(int skill, int newLevel) {
        level[skill] = (short) newLevel;
        refresh(skill);
    }

    /**
     * P4 persistence setter: writes the current level without {@link #refresh},
     * i.e. no UPDATE_STAT, no appearance regeneration. Used by the native 947
     * profile binder while restoring a schema-3 SKILLS section; a legacy caller
     * that wants the client updated must use {@link #set(int, int)}.
     */
    public void setLevelWithoutRefresh(int skill, int newLevel) {
        if (skill < 0 || skill >= level.length)
            throw new IllegalArgumentException("Unknown skill " + skill);
        level[skill] = (short) newLevel;
    }

    /** P4 persistence setter: writes the experience without {@link #refresh}; see {@link #setLevelWithoutRefresh}. */
    public void setXpWithoutRefresh(int skill, double exp) {
        if (skill < 0 || skill >= xp.length)
            throw new IllegalArgumentException("Unknown skill " + skill);
        xp[skill] = exp;
    }

    /** Copy of all SKILL_COUNT current levels exactly as stored (short, may be boosted or drained). */
    public short[] getLevelsCopy() {
        return level.clone();
    }

    /** Copy of all SKILL_COUNT experience values exactly as stored (double). */
    public double[] getXpCopy() {
        return xp.clone();
    }

    /**
     * Increases the level.
     *
     * @param skill The skill to increase.
     * @param levels The amount of levels to increase.
     */
    public void updateLevel(int skill, int levels) {
        level[skill] += (short) levels;
        refresh(skill);
    }

    public void setCounterSkill(int skill) {
        xpTracks[currentCounter] = 0;
        trackSkillsIds[currentCounter] = (byte) skill;
        player.getPackets().sendConfigByFile(225 + currentCounter, trackSkillsIds[currentCounter] + 1);
        refreshCounterXp(currentCounter);
    }

    public void setCurrentCounter(int counter) {
        if (counter != currentCounter) {
            currentCounter = counter;
            refreshCurrentCounter();
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
        // temporary
        if (xpTracks == null) {
            xpPopup = true;
            xpTracks = new double[3];
            trackSkills = new boolean[3];
            trackSkillsIds = new byte[3];
            trackSkills[0] = true;
            for (int i = 0; i < trackSkillsIds.length; i++)
                trackSkillsIds[i] = 30;
        }
        // Every skill-shaped array is normalised to SKILL_COUNT here, which is where
        // the pre-947 code already did it - this is not new legacy-save migration, and
        // there is deliberately no readObject hook: a 910 .p save is out of scope
        // (notes/SKILLS-27-28.md). What this DOES guarantee is that a field added after
        // an existing profile was written comes back usable instead of null.
        enabledSkillsTargets = grow(enabledSkillsTargets, false);
        skillsTargetsUsingLevelMode = grow(skillsTargetsUsingLevelMode, false);
        skillsTargetsValues = grow(skillsTargetsValues, 0);
        skillsTargetsInitialValues = grow(skillsTargetsInitialValues, 0);
        // EXPERIENCE is the durable truth, exactly as the client treats it: the level is
        // recomputed from it below rather than trusted, so a cap or curve change lands
        // on load instead of leaving a stale stored level behind.
        xp = grow(xp, 0D);
        level = grow(level, (short) 1);
        for (int skill = 0; skill < level.length; skill++) {
            if (xp[skill] > MAXIMUM_EXP)
                xp[skill] = MAXIMUM_EXP;
            int base = getLevelForXp(skill);
            // A stored level BELOW the recomputed base is either a drain (self-heals in
            // a minute) or a level written under the old 99 cap for a skill the cache
            // caps at 110/120 (permanent, and would render as a drained red level for
            // ever). Raising it costs a drain and fixes the stale cap, so raise it.
            // A level ABOVE the base is left alone: that is a live potion boost, which
            // legitimately exceeds even the cap.
            if (level[skill] < base)
                level[skill] = (short) base;
        }
        xpBonuses = grow(xpBonuses, 0D);
        if (xpAnnouncements == null) {
            xpAnnouncements = new boolean[11];
            for (int i = 0; i < xpAnnouncements.length; i++) {
                if (getTotalXp() >= (i == xpAnnouncements.length - 1 ? ((long) MAXIMUM_EXP * (long) xp.length) : (5000000000L * (long) (i + 1)))) {
                    xpAnnouncements[i] = true;
                }
            }
        }
    }

    /**
     * Returns a {@code SKILL_COUNT}-long copy of {@code values}, padding with
     * {@code fill}. A null or already-correct array is handled too, so callers do not
     * need their own guard. One of these per array type because Java 8 has no way to
     * write it once for primitives.
     */
    private static boolean[] grow(boolean[] values, boolean fill) {
        if (values != null && values.length == SKILL_COUNT)
            return values;
        boolean[] grown = new boolean[SKILL_COUNT];
        for (int i = 0; i < grown.length; i++)
            grown[i] = values != null && i < values.length ? values[i] : fill;
        return grown;
    }

    private static int[] grow(int[] values, int fill) {
        if (values != null && values.length == SKILL_COUNT)
            return values;
        int[] grown = new int[SKILL_COUNT];
        for (int i = 0; i < grown.length; i++)
            grown[i] = values != null && i < values.length ? values[i] : fill;
        return grown;
    }

    private static short[] grow(short[] values, short fill) {
        if (values != null && values.length == SKILL_COUNT)
            return values;
        short[] grown = new short[SKILL_COUNT];
        for (int i = 0; i < grown.length; i++)
            grown[i] = values != null && i < values.length ? values[i] : fill;
        return grown;
    }

    private static double[] grow(double[] values, double fill) {
        if (values != null && values.length == SKILL_COUNT)
            return values;
        double[] grown = new double[SKILL_COUNT];
        for (int i = 0; i < grown.length; i++)
            grown[i] = values != null && i < values.length ? values[i] : fill;
        return grown;
    }

    public void sendTotalXpAnnouncements() {
        for (int i = 0; i < xpAnnouncements.length; i++) {
            long currentXp = (long) (i == xpAnnouncements.length - 1 ? (MAXIMUM_EXP * xp.length) : (5000000000L * (long) (i + 1)));
            if (!xpAnnouncements[i] && getTotalXp() >= currentXp) {
                xpAnnouncements[i] = true;
                World.sendWorldMessage("<img=6>" + Colors.RED + "<shad=000000>News: " + player.getDisplayName() + " has achieved " + Utils.formatNumber(currentXp) + " total XP on " + player.getXPMode() + " mode!", false);
                HcimNewsManager.getInstance().addNews(player,"<#player> achieved " + Utils.formatNumber(currentXp) + " Total XP!", 2);
                QueryExecutor.submit(new News(player, "<b> " + player.getDisplayName() + " has achieved " + Utils.formatNumber(currentXp) + " Total XP on " + player.getXPMode() + " mode."));
            }
        }
    }

    public void resetLevel(int skill) {
        level[skill] = (short) (skill == Skills.HITPOINTS ? 10 : 1);
        xp[skill] = skill == Skills.HITPOINTS ? Skills.getXPForLevel(Skills.HITPOINTS, 10) : 0;
        refresh(skill);
    }

    public void setXp(int skill, double exp) {
        xp[skill] = exp;
        refresh(skill);
    }

    public void switchTrackCounter() {
        trackSkills[currentCounter] = !trackSkills[currentCounter];
        player.getPackets().sendConfigByFile(229 + currentCounter, trackSkills[currentCounter] ? 1 : 0);
    }

    public void switchXPDisplay() {
        // The persistent XP counter HUD is unsupported in this codebase /
        // cache combination: the codebase targets interfaces 1214/1215 which
        // have zero components in the 910 cache, and the real widget
        // (interface 1213) requires a CS2 setup flow (menu == 8 in
        // InterfaceManager.openMenu) that was never implemented. Every
        // varp/varbit we tried left the widget on its "Start skilling to
        // populate the XP counter..." placeholder. Rather than leak a
        // half-working button to players, hard-pin the widget hidden and
        // tell the user.
        xpDisplay = false;
        refreshXPDisplay();
        if (player != null) {
            player.sendMessage("The persistent XP counter is not available in this build. Floating XP drops still work.");
        }
    }

    private void refreshXPDisplay() {
        // 0 = show, 1 = hide. Force hide regardless of stored state.
        player.getVarBitManager().sendVarBit(19964, 1);
    }

    public void switchXPPopup() {
        xpPopup = !xpPopup;
        player.sendMessage("XP pop-ups are now " + (xpPopup ? "en" : "dis") + "abled.");
        refreshXPPopUp();
    }

    public void refreshXPPopUp() {
        // XP popup overlay (interface 1213) is bound to window slot keyed by
        // XP_POPUPS (1026) in sendNISScreenInterfaces.
        //
        // Three things have to be true for floating XP drops to render:
        //   (a) interface 1213 is bound to slot 1026 on parent 1477
        //   (b) that slot's component is not hidden
        //   (c) varbit 228 says popups are enabled (0 = show, 1 = hide)
        //
        // Original code only tried (b) and used the wrong component key
        // (1025 + gmapKey 3503), which resolves to a neighbouring overlay -
        // not the XP popup. We now address all three explicitly:
        //   - Re-send the slot binding so (a) is true regardless of mode
        //     swaps that might have cleared it.
        //   - Un-hide both fixed (3503) and resizable (3505) variants of
        //     the slot, because only one is active per screen mode.
        //   - Send varbit 228 with the user's toggle state.
        final int xpKey = 1026; // XP_POPUPS
        player.getInterfaceManager().setWindowInterfaceByKey(xpKey, 1213);
        player.getPackets().sendHideIComponent(1477,
                InterfaceManager.getComponentIdByKey(7716, xpKey, 3503), false);
        player.getPackets().sendHideIComponent(1477,
                InterfaceManager.getComponentIdByKey(7716, xpKey, 3505), false);
        player.getVarBitManager().sendVarBit(228, xpPopup ? 0 : 1);
    }

    public long getTotalXp() {
        long totalxp = 0;
        for (double xp : getXp()) {
            totalxp += xp;
        }
        return totalxp;
    }

    public String getTotalXp(Player player) {
        double totalxp = 0;
        for (double xp : player.getSkills().getXp())
            totalxp += xp;
        return new DecimalFormat("#,###,##0").format(totalxp);
    }

    public int getTargetIdByComponentId(int componentId) {
        switch (componentId) {
            case 150: // Attack
                return 0;
            case 9: // Strength
                return 1;
            case 40: // Range
                return 2;
            case 71: // Magic
                return 3;
            case 22: // Defence
                return 4;
            case 145: // Constitution
                return 5;
            case 58: // Prayer
                return 6;
            case 15: // Agility
                return 7;
            case 28: // Herblore
                return 8;
            case 46: // Theiving
                return 9;
            case 64: // Crafting
                return 10;
            case 84: // Runecrafting
                return 11;
            case 140: // Mining
                return 12;
            case 135: // Smithing
                return 13;
            case 34: // Fishing
                return 14;
            case 52: // Cooking
                return 15;
            case 130: // Firemaking
                return 16;
            case 125: // Woodcutting
                return 17;
            case 77: // Fletching
                return 18;
            case 90: // Slayer
                return 19;
            case 96: // Farming
                return 20;
            case 102: // Construction
                return 21;
            case 108: // Hunter
                return 22;
            case 114: // Summoning
                return 23;
            case 120: // Dungeoneering
                return 24;
            case 158:
                return 25;
            case 164:
                return 26;
            default:
                return -1;
        }
    }

    public int getSkillIdByTargetId(int targetId) {
        switch (targetId) {
            case 0: // Attack
                return ATTACK;
            case 1: // Strength
                return STRENGTH;
            case 2: // Range
                return RANGE;
            case 3: // Magic
                return MAGIC;
            case 4: // Defence
                return DEFENCE;
            case 5: // Constitution
                return HITPOINTS;
            case 6: // Prayer
                return PRAYER;
            case 7: // Agility
                return AGILITY;
            case 8: // Herblore
                return HERBLORE;
            case 9: // Thieving
                return THIEVING;
            case 10: // Crafting
                return CRAFTING;
            case 11: // Runecrafting
                return RUNECRAFTING;
            case 12: // Mining
                return MINING;
            case 13: // Smithing
                return SMITHING;
            case 14: // Fishing
                return FISHING;
            case 15: // Cooking
                return COOKING;
            case 16: // Firemaking
                return FIREMAKING;
            case 17: // Woodcutting
                return WOODCUTTING;
            case 18: // Fletching
                return FLETCHING;
            case 19: // Slayer
                return SLAYER;
            case 20: // Farming
                return FARMING;
            case 21: // Construction
                return CONSTRUCTION;
            case 22: // Hunter
                return HUNTER;
            case 23: // Summoning
                return SUMMONING;
            case 24: // Dungeoneering
                return DUNGEONEERING;
            case 25: // Divination
                return DIVINATION;
            case 26: // Invention
                return INVENTION;
            default:
                return -1;
        }
    }

    public void refreshEnabledSkillsTargets() {
        int varValue = 0;
        for (int skillId = 0; skillId < enabledSkillsTargets.length; skillId++)
            varValue |= ((enabledSkillsTargets[skillId] ? 1 : 0) << getUseableSkillId(skillId));
        player.getPackets().sendConfig(1115, varValue);
    }

    public void refreshUsingLevelTargets() {
        int varValue = 0;
        for (int skillId = 0; skillId < skillsTargetsUsingLevelMode.length; skillId++)
            varValue |= ((skillsTargetsUsingLevelMode[skillId] ? 1 : 0) << getUseableSkillId(skillId));
        player.getPackets().sendConfig(1117, varValue);
    }

    public static int getUseableSkillId(int skillId) {
        return RS3ClientScriptMap.getMap(1482).getIntValue(skillId);
    }

    public void refreshSkillsTargetsValues() {
        for (int skillId = 0; skillId < skillsTargetsValues.length; skillId++) {
            int varId = skillId == Skills.DIVINATION ? 3839 : skillId == Skills.INVENTION ? 6095 : (1117 + getUseableSkillId(skillId));
            player.getPackets().sendConfig(varId, skillsTargetsValues[skillId]);
        }
    }

    public void refreshSkillsTargetsInitialValues() {
        for (int skillId = 0; skillId < skillsTargetsInitialValues.length; skillId++) {
            int varId = skillId == Skills.DIVINATION ? 3840 : skillId == Skills.INVENTION ? 6096 : (1142 + getUseableSkillId(skillId));
            player.getPackets().sendConfig(varId, skillsTargetsInitialValues[skillId]);
        }
    }

    public void setSkillTargetEnabled(int id, boolean enabled) {
        enabledSkillsTargets[id] = enabled;
        refreshEnabledSkillsTargets();
    }

    public void setSkillTargetUsingLevelMode(int id, boolean using) {
        skillsTargetsUsingLevelMode[id] = using;
        refreshUsingLevelTargets();
    }

    public void setSkillTargetValue(int skillId, int value) {
        skillsTargetsValues[skillId] = value;
        refreshSkillsTargetsValues();
    }

    public void setSkillTargetInitialValue(int skillId, int value) {
        skillsTargetsInitialValues[skillId] = value;
        refreshSkillsTargetsInitialValues();
    }

    public void setSkillTarget(boolean usingLevel, int skillId, int target) {
        setSkillTargetEnabled(skillId, true);
        setSkillTargetUsingLevelMode(skillId, usingLevel);
        setSkillTargetValue(skillId, target);
        setSkillTargetInitialValue(skillId, usingLevel ? player.getSkills().getLevelForXp(skillId) : (int) xp[skillId]);
    }

    /**
     * Gets the players total level.
     *
     * @return Total Level as Integer.
     */
    public int getTotalLevel() {
        int totallevel = 0;
        for (int i = 0; i < level.length; i++)
            totallevel += getLevelForXp(i);
        return totallevel;
    }

    /**
     * Handles adding Skill Shards for Expert Skillcapes.
     *
     * <p>The threshold was a hardcoded 99 per skill; it is now
     * {@link #getLevelCap(int)}, so a shard is earned at the level the 947 cache calls
     * mastery for that skill - 110 for Woodcutting through Hunter, 120 for the twelve
     * skills the cache caps at 120. Archaeology and Necromancy have no shard item id in
     * {@link #SHARDS} and are deliberately not listed here; see notes/SKILLS-27-28.md.
     *
     * @param skill The Skill to add the shard to.
     */
    private void handleSkillShards(int skill) {
        if (Utils.random(100) <= 50) // Doesn't immediately give the shard,
            // same, as RS
            return;

        /**
         * Combatant's cape
         */
        if (!player.hasItem(new Item(32053))) {
            if (getLevelForXp(ATTACK) >= getLevelCap(ATTACK) && skill == ATTACK)
                addShard(new Item(32069));
            if (getLevelForXp(STRENGTH) >= getLevelCap(STRENGTH) && skill == STRENGTH)
                addShard(new Item(32070));
            if (getLevelForXp(DEFENCE) >= getLevelCap(DEFENCE) && skill == DEFENCE)
                addShard(new Item(32071));
            if (getLevelForXp(HITPOINTS) >= getLevelCap(HITPOINTS) && skill == HITPOINTS)
                addShard(new Item(32074));
            if (getLevelForXp(RANGE) >= getLevelCap(RANGE) && skill == RANGE)
                addShard(new Item(32075));
            if (getLevelForXp(PRAYER) >= getLevelCap(PRAYER) && skill == PRAYER)
                addShard(new Item(32073));
            if (getLevelForXp(MAGIC) >= getLevelCap(MAGIC) && skill == MAGIC)
                addShard(new Item(32076));
            if (getLevelForXp(SUMMONING) >= getLevelCap(SUMMONING) && skill == SUMMONING)
                addShard(new Item(32072));
        }
        /**
         * Artisan's cape
         */
        if (!player.hasItem(new Item(32054))) {
            if (getLevelForXp(CRAFTING) >= getLevelCap(CRAFTING) && skill == CRAFTING)
                addShard(new Item(32082));
            if (getLevelForXp(CONSTRUCTION) >= getLevelCap(CONSTRUCTION) && skill == CONSTRUCTION)
                addShard(new Item(32083));
            if (getLevelForXp(FIREMAKING) >= getLevelCap(FIREMAKING) && skill == FIREMAKING)
                addShard(new Item(32079));
            if (getLevelForXp(FLETCHING) >= getLevelCap(FLETCHING) && skill == FLETCHING)
                addShard(new Item(32080));
            if (getLevelForXp(HERBLORE) >= getLevelCap(HERBLORE) && skill == HERBLORE)
                addShard(new Item(32081));
            if (getLevelForXp(SMITHING) >= getLevelCap(SMITHING) && skill == SMITHING)
                addShard(new Item(32084));
            if (getLevelForXp(COOKING) >= getLevelCap(COOKING) && skill == COOKING)
                addShard(new Item(32077));
            if (getLevelForXp(RUNECRAFTING) >= getLevelCap(RUNECRAFTING) && skill == RUNECRAFTING)
                addShard(new Item(32078));
        }
        /**
         * Gatherer's cape
         */
        if (!player.hasItem(new Item(32052))) {
            if (getLevelForXp(DIVINATION) >= getLevelCap(DIVINATION) && skill == DIVINATION)
                addShard(new Item(32066));
            if (getLevelForXp(FARMING) >= getLevelCap(FARMING) && skill == FARMING)
                addShard(new Item(32067));
            if (getLevelForXp(FISHING) >= getLevelCap(FISHING) && skill == FISHING)
                addShard(new Item(32063));
            if (getLevelForXp(MINING) >= getLevelCap(MINING) && skill == MINING)
                addShard(new Item(32065));
            if (getLevelForXp(HUNTER) >= getLevelCap(HUNTER) && skill == HUNTER)
                addShard(new Item(32068));
            if (getLevelForXp(WOODCUTTING) >= getLevelCap(WOODCUTTING) && skill == WOODCUTTING)
                addShard(new Item(32064));
        }
        /**
         * Support cape
         */
        if (!player.hasItem(new Item(32055))) {
            if (getLevelForXp(AGILITY) >= getLevelCap(AGILITY) && skill == AGILITY)
                addShard(new Item(32087));
            if (getLevelForXp(DUNGEONEERING) >= getLevelCap(DUNGEONEERING) && skill == DUNGEONEERING)
                addShard(new Item(32085));
            if (getLevelForXp(SLAYER) >= getLevelCap(SLAYER) && skill == SLAYER)
                addShard(new Item(32088));
            if (getLevelForXp(THIEVING) >= getLevelCap(THIEVING) && skill == THIEVING)
                addShard(new Item(32086));
        }
    }

    /**
     * Handles checking and adding of Skill Shards.
     *
     * @param item The Shard to check & add.
     */
    private void addShard(Item item) {
        if (!player.hasItem(item) && !player.shardStored(item.getId())) {
            player.setShard(item.getId(), true);
            player.sendMessage(Colors.RED + "You've found " + Utils.getAorAn(item.getName()) + "" + item.getName() + "! It has been added to your shard pouch.");
        }
    }

    private double getSkeletonSuitBonus() {
        double boost = 1.0;

        for (int item : SKELETON_OUTFIT_PIECES) {
            if (player.getEquipment().containsOneItem(item))
                boost *= 1.02;
        }

        return boost;
    }

    private double getLunarfurySuitBonus() {
        double boost = 1.0;

        for (int item : LUNARFURY_OUTFIT_PIECES) {
            if (player.getEquipment().containsOneItem(item))
                boost *= 1.03;
        }

        return boost;
    }

    public String getPercentage() {
        if (player.isMasterDonator())
            return Settings.expBoosts[6][1];
        if (player.isUltimateDonator())
            return Settings.expBoosts[5][1];
        else if (player.isSupremeDonator())
            return Settings.expBoosts[4][1];
        else if (player.isLegendaryDonator())
            return Settings.expBoosts[3][1];
        else if (player.isExtremeDonator())
            return Settings.expBoosts[2][1];
        else if (player.isDonator())
            return Settings.expBoosts[1][1];
        else
            return "0";
    }

    public int getVirtualLevel(int skill) {
        double experience = xp[skill];
        int total = 0;
        int output_experience = 0;
        for (int level = 1; level <= 120; level++) {
            total += Math.floor(level + 300.0 * Math.pow(2.0, level / 7.0));
            output_experience = (int) Math.floor(total / 4);
            if (output_experience - 1 >= experience) {
                return level;
            }
        }
        int actual_level = getLevel(skill);
        return actual_level > 120 ? actual_level : (actual_level < 120 && actual_level > 99 ? actual_level : 120);
    }

    public int getSkillId(int slotId) {
        switch (slotId) {
            case 0:
                return Skills.ATTACK;
            case 1:
                return Skills.HITPOINTS;
            case 2:
                return Skills.MINING;
            case 3:
                return Skills.STRENGTH;
            case 4:
                return Skills.AGILITY;
            case 5:
                return Skills.SMITHING;
            case 6:
                return Skills.DEFENCE;
            case 7:
                return Skills.HERBLORE;
            case 8:
                return Skills.FISHING;
            case 9:
                return Skills.RANGE;
            case 10:
                return Skills.THIEVING;
            case 11:
                return Skills.COOKING;
            case 12:
                return Skills.PRAYER;
            case 13:
                return Skills.CRAFTING;
            case 14:
                return Skills.FIREMAKING;
            case 15:
                return Skills.MAGIC;
            case 16:
                return Skills.FLETCHING;
            case 17:
                return Skills.WOODCUTTING;
            case 18:
                return Skills.RUNECRAFTING;
            case 19:
                return Skills.SLAYER;
            case 20:
                return Skills.FARMING;
            case 21:
                return Skills.CONSTRUCTION;
            case 22:
                return Skills.HUNTER;
            case 23:
                return Skills.SUMMONING;
            case 24:
                return Skills.DUNGEONEERING;
            case 25:
                return Skills.DIVINATION;
            case 26:
                return Skills.INVENTION;
            default:
                return -1;
        }
    }

    public double addBonusXpStar(int skill, double exp) {
        exp *= getStarXPRates();
        addXpBonuses(skill, exp);
        return exp;
    }

    public void addXpBonuses(int skillId, double amount) {
        this.xpBonuses[skillId] += amount;
        if (xp[skillId] + xpBonuses[skillId] > Skills.MAXIMUM_EXP)
            xpBonuses[skillId] = Skills.MAXIMUM_EXP - xp[skillId];
        refresh(skillId);
    }

    public void setXpBonuses(int skillId, double bonuesXp) {
        xpBonuses[skillId] = bonuesXp;
        if (xp[skillId] + xpBonuses[skillId] > Skills.MAXIMUM_EXP)
            xpBonuses[skillId] = Skills.MAXIMUM_EXP - xp[skillId];
        refresh(skillId);
    }

    private double getStarXPRates() {
        return 1;
    }

    public void refreshXpBonuses() {
        for (int skillId = 0; skillId < xpBonuses.length; skillId++)
            refreshXpBonuses(skillId);
    }

    public void refreshXpBonuses(int skillId) {
        int varId = getSkillBonusXpVarId(skillId);
        // 27 Archaeology and 28 Necromancy have no bonus-xp var in the 910 table, and
        // the 947 evidence does not name one either (varps 3304.. are the XP TARGET vars
        // in the 947 scripts, SKILLS_TAB.md 9.6). Writing -1 would be a wrong id on the
        // wire rather than a missing one, so skip.
        if (varId < 0)
            return;
        player.getVarBitManager().sendVar(varId, (int) (xpBonuses[skillId] * 10));
    }

    public static int getSkillBonusXpVarId(int skillId) {
        switch (skillId) {
            case 0:
                return 3304;
            case 2:
                return 3305;
            case 1:
                return 3306;
            case 6:
                return 3307;
            case 4:
                return 3308;
            case 5:
                return 2850;
            case 23:
                return 3309;
            case 14:
                return 3310;
            case 13:
                return 3311;
            case 12:
                return 3312;
            case 8:
                return 3313;
            case 19:
                return 3314;
            case 11:
                return 3315;
            case 10:
                return 3316;
            case 7:
                return 3317;
            case 24:
                return 3318;
            case 18:
                return 3319;
            case 20:
                return 3320;
            case 21:
                return 3321;
            case 17:
                return 3322;
            case 22:
                return 3323;
            case 3:
                return 3324;
            case 16:
                return 3325;
            case 9:
                return 3326;
            case 15:
                return 3327;
            case 25:
                return 3836;
            case 26:
                return 6092;
        }
        return -1;
    }

    public void unlockSkills(boolean menu) {
        player.getPackets().sendIComponentSettings(menu ? 320 : 1466, menu ? 9 : 7, 0, Skills.SKILL_NAME.length - 1, 30);
    }

    private int selectedSkillId = -1;

    public static final int[] SKILL_MENU_COMPONENTS = { 23, 63, 200, 31, 168, 160, 144, 47, 7, 111, 103, 95, 55, 192,
            152, 119, 15, 216, 184, 87, 176, 127, 39, 208, 79, 71, 135 };

    public void setSelectedSkillId(int skillId) {
        selectedSkillId = skillId;
        setRefreshSkillMenu();
    }

    public void sendSkillMenu(int componentId) {
        int selectedSkillId = -1;
        if (componentId != -1) {
            for (int i = 0; i < SKILL_MENU_COMPONENTS.length; i++)
                if (componentId == SKILL_MENU_COMPONENTS[i]) {
                    selectedSkillId = i;
                    break;
                }
        }
        if (selectedSkillId == -1 && componentId != -1)
            return;
        if (componentId == -1)
            selectedSkillId = this.selectedSkillId;
        if (selectedSkillId == -1)
            selectedSkillId = Skills.AGILITY;
        this.selectedSkillId = selectedSkillId;
        setRefreshSkillMenu();
    }

    private void setRefreshSkillMenu() {
        player.getPackets().sendExecuteScript(5682, getUseableSkillId(selectedSkillId));
        player.getInterfaceManager().setInterface(false, 1218, 0, 1217);
        player.getPackets().sendIComponentSettings(1218, 231, 0, 0, 2);
        player.getPackets().sendIComponentSettings(1218, 246, 0, 0, 2);
    }

    public void sendCombatLevel() {
        player.getPackets().sendConfigByFile(1000, getCombatLevelWithSummoning());
    }

    @Getter
    @Setter
    private double dailyWiseXP;
}
