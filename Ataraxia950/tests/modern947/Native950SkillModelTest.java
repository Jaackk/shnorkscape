package modern947;

import com.rs.game.player.Skills;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * The skill model against the 947 cache, hermetically: no cache read, no Player, no
 * channel. Every expected number below is TRANSCRIBED from the evidence pass
 * (OpenNXT/data/prot/947/generated/native947-3/verified/ui/STAT_DEFINITIONS.md, which
 * decodes flat cache index 28 group 9 - the STAT defaults group - plus enums 680, 716,
 * 10699, 10865, 10866 and cache scripts 1432 and 4708). None of it is read back out of
 * the code under test, so this file fails if {@code Skills} drifts from the cache.
 *
 * <p>Sources, per assertion group:
 * <ul>
 * <li>names and order - enum 680 (17 group 2 file 168), size 29, index 27 Archaeology
 *     and 28 Necromancy;</li>
 * <li>caps - the unsigned short in each stat definition (28/9 opcode 1), which agrees
 *     with enum 10865 on all 29 ids;</li>
 * <li>curves - only stat 26 sets {@code flags & 0x4} (cache table 0 == enum 10699);
 *     the other 28 carry no table id and use the default series, enum 716;</li>
 * <li>maximum experience - the flag-zero clamp 0x0BEBC200 in setter 0x140369C10 and
 *     the literal 200000000 in cache script 8489 instruction 630;</li>
 * <li>combat level - cache script 1432, both branches.</li>
 * </ul>
 */
public final class Native950SkillModelTest {

    /** Enum 680, in order. Index IS the stat id. */
    private static final String[] ENUM_680 = {
            "Attack", "Defence", "Strength", "Constitution", "Ranged", "Prayer", "Magic",
            "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting",
            "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming",
            "Runecrafting", "Hunter", "Construction", "Summoning", "Dungeoneering",
            "Divination", "Invention", "Archaeology", "Necromancy" };

    /** The cap from each stat definition (28/9 opcode 1), cross-checked against enum 10865. */
    private static final int[] CACHE_CAP = {
            120, 99, 120, 99, 120, 99, 120, 99, 110, 110,
             99, 110, 110, 110, 110, 120, 99, 120, 120, 120,
            110, 110, 99, 99, 120, 99, 120, 120, 120 };

    /** True where the stat definition sets {@code flags & 0x4}, i.e. uses the elite table. */
    private static final boolean[] USES_ELITE_CURVE = new boolean[29];
    static { USES_ELITE_CURVE[26] = true; }

    /** Enum 716 (the default series) at the levels this test pins. */
    private static final int DEFAULT_L2 = 83, DEFAULT_L10 = 1154, DEFAULT_L50 = 101333,
            DEFAULT_L98 = 11805606, DEFAULT_L99 = 13034431, DEFAULT_L100 = 14391160,
            DEFAULT_L110 = 38737661, DEFAULT_L120 = 104273167;

    /** Enum 10699 / cache table 0 (the elite series) at the levels this test pins. */
    private static final int ELITE_L2 = 830, ELITE_L98 = 34580790, ELITE_L99 = 36073511,
            ELITE_L100 = 37608773, ELITE_L110 = 56412678, ELITE_L120 = 80618654,
            ELITE_L150 = 194927409;

    /** Cache script 8489 instruction 630 / setter clamp 0x0BEBC200. */
    private static final int CACHE_MAX_EXPERIENCE = 200000000;

    // ------------------------------------------------------------------ shape and names

    @Test public void theModelCarriesTheTwentyNineStatsTheCacheDefines() {
        assertEquals("stat definition count byte (28/9 opcode 1)", 29, Skills.SKILL_COUNT);
        assertEquals(Skills.SKILL_COUNT, Skills.SKILL_NAME.length);
        assertEquals(Skills.SKILL_COUNT, new Skills().getLevelsCopy().length);
        assertEquals(Skills.SKILL_COUNT, new Skills().getXpCopy().length);
    }

    @Test public void theSkillNamesAndTheirOrderAreEnum680() {
        assertEquals(ENUM_680.length, Skills.SKILL_NAME.length);
        for (int skill = 0; skill < ENUM_680.length; skill++)
            assertEquals("enum 680 index " + skill, ENUM_680[skill], Skills.SKILL_NAME[skill]);
        assertEquals(27, Skills.ARCHAEOLOGY);
        assertEquals(28, Skills.NECROMANCY);
        assertEquals("Archaeology", Skills.SKILL_NAME[Skills.ARCHAEOLOGY]);
        assertEquals("Necromancy", Skills.SKILL_NAME[Skills.NECROMANCY]);
        assertEquals(Integer.valueOf(27), Skills.NAME_TO_ID.get("archaeology"));
        assertEquals(Integer.valueOf(28), Skills.NAME_TO_ID.get("necromancy"));
        assertEquals(29, Skills.SKILL_IDS.size());
    }

    @Test public void everySkillIdResolvesToItsOwnName() {
        Skills skills = new Skills();
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++)
            assertEquals("getSkillName(" + skill + ")", ENUM_680[skill], skills.getSkillName(skill));
    }

    // ------------------------------------------------------------------ caps

    @Test public void everyLevelCapIsTheOneInTheStatDefinition() {
        for (int skill = 0; skill < CACHE_CAP.length; skill++)
            assertEquals("cap of " + ENUM_680[skill], CACHE_CAP[skill], Skills.getLevelCap(skill));
    }

    @Test public void theCapsBreakDownExactlyAsTheEvidenceCountsThem() {
        int at99 = 0, at110 = 0, at120 = 0;
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
            int cap = Skills.getLevelCap(skill);
            if (cap == 99) at99++;
            else if (cap == 110) at110++;
            else if (cap == 120) at120++;
            else fail("unexpected cap " + cap + " for " + ENUM_680[skill]);
        }
        assertEquals("skills capped at 99", 9, at99);
        assertEquals("skills capped at 110", 8, at110);
        assertEquals("skills capped at 120", 12, at120);
    }

    @Test public void anIdOutsideTheModelDegradesToTheEnum10865Default() {
        assertEquals(99, Skills.getLevelCap(-1));
        assertEquals(99, Skills.getLevelCap(Skills.SKILL_COUNT));
    }

    // ------------------------------------------------------------------ curves

    @Test public void theDefaultCurveIsEnum716() {
        // Every stat except 26 rides this one, Archaeology and Necromancy included.
        int skill = Skills.ARCHAEOLOGY;
        assertEquals(0, Skills.getXPForLevel(skill, 1));
        assertEquals(DEFAULT_L2, Skills.getXPForLevel(skill, 2));
        assertEquals(DEFAULT_L10, Skills.getXPForLevel(skill, 10));
        assertEquals(DEFAULT_L50, Skills.getXPForLevel(skill, 50));
        assertEquals(DEFAULT_L98, Skills.getXPForLevel(skill, 98));
        assertEquals(DEFAULT_L99, Skills.getXPForLevel(skill, 99));
        assertEquals(DEFAULT_L100, Skills.getXPForLevel(skill, 100));
        assertEquals(DEFAULT_L110, Skills.getXPForLevel(skill, 110));
        assertEquals(DEFAULT_L120, Skills.getXPForLevel(skill, 120));
    }

    @Test public void theEliteCurveIsEnum10699AndOnlyInventionUsesIt() {
        assertEquals(0, Skills.getXPForLevel(Skills.INVENTION, 1));
        assertEquals(ELITE_L2, Skills.getXPForLevel(Skills.INVENTION, 2));
        assertEquals(ELITE_L98, Skills.getXPForLevel(Skills.INVENTION, 98));
        assertEquals(ELITE_L99, Skills.getXPForLevel(Skills.INVENTION, 99));
        assertEquals(ELITE_L100, Skills.getXPForLevel(Skills.INVENTION, 100));
        assertEquals(ELITE_L110, Skills.getXPForLevel(Skills.INVENTION, 110));
        assertEquals(ELITE_L120, Skills.getXPForLevel(Skills.INVENTION, 120));
        assertEquals(ELITE_L150, Skills.getXPForLevel(Skills.INVENTION, 150));
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
            int expected = USES_ELITE_CURVE[skill] ? ELITE_L99 : DEFAULT_L99;
            assertEquals("curve of " + ENUM_680[skill] + " at level 99",
                    expected, Skills.getXPForLevel(skill, 99));
        }
    }

    @Test public void theExperienceAtEachSkillsCapIsTheCacheValue() {
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
            int cap = CACHE_CAP[skill];
            int expected;
            if (USES_ELITE_CURVE[skill]) expected = ELITE_L120;      // stat 26, cap 120, elite curve
            else if (cap == 99) expected = DEFAULT_L99;
            else if (cap == 110) expected = DEFAULT_L110;
            else expected = DEFAULT_L120;
            assertEquals("xp at the cap of " + ENUM_680[skill], expected, Skills.getXPForLevel(skill, cap));
        }
    }

    // ------------------------------------------------------------------ xp <-> level

    @Test public void everySkillRoundTripsAtLevelOneNinetyEightNinetyNineTheHundredBoundaryAndItsCap() {
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
            int cap = CACHE_CAP[skill];
            assertRoundTrip(skill, 1);
            assertRoundTrip(skill, 98);
            assertRoundTrip(skill, 99);
            // the 99/100 boundary: one point short of 100 is still 99, and 100 is 100.
            assertEquals(ENUM_680[skill] + " one point short of level 100",
                    99, Skills.getLevelForXp(skill, Skills.getXPForLevel(skill, 100) - 1));
            if (cap >= 100)
                assertRoundTrip(skill, 100);
            assertRoundTrip(skill, cap);
        }
    }

    private static void assertRoundTrip(int skill, int level) {
        int experience = Skills.getXPForLevel(skill, level);
        assertEquals(ENUM_680[skill] + " level " + level + " round trip",
                level, Skills.getLevelForXp(skill, experience));
    }

    @Test public void noSkillCanExceedItsOwnCapHoweverMuchExperienceItHolds() {
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
            assertEquals("cap of " + ENUM_680[skill] + " under maximum experience",
                    CACHE_CAP[skill], Skills.getLevelForXp(skill, CACHE_MAX_EXPERIENCE));
            assertEquals("cap of " + ENUM_680[skill] + " under absurd experience",
                    CACHE_CAP[skill], Skills.getLevelForXp(skill, 5000000000D));
        }
    }

    @Test public void theFifteenSkillsTheCacheRaisedAboveNinetyNineActuallyGoThere() {
        // The disagreement table in STAT_DEFINITIONS.md 3.2: these fifteen were capped
        // at 99 by the 910 model and are capped higher by the cache.
        int[] raisedTo120 = { Skills.ATTACK, Skills.STRENGTH, Skills.RANGE, Skills.MAGIC,
                Skills.HERBLORE, Skills.THIEVING, Skills.FARMING };
        int[] raisedTo110 = { Skills.WOODCUTTING, Skills.FLETCHING, Skills.FIREMAKING,
                Skills.CRAFTING, Skills.SMITHING, Skills.MINING, Skills.RUNECRAFTING, Skills.HUNTER };
        for (int skill : raisedTo120) {
            assertEquals(ENUM_680[skill], 120, Skills.getLevelCap(skill));
            assertEquals(ENUM_680[skill], 120, Skills.getLevelForXp(skill, DEFAULT_L120));
        }
        for (int skill : raisedTo110) {
            assertEquals(ENUM_680[skill], 110, Skills.getLevelCap(skill));
            assertEquals(ENUM_680[skill], 110, Skills.getLevelForXp(skill, DEFAULT_L110));
        }
    }

    @Test public void theNineSkillsTheCacheLeavesAtNinetyNineStayThere() {
        int[] unchanged = { Skills.DEFENCE, Skills.HITPOINTS, Skills.PRAYER, Skills.COOKING,
                Skills.FISHING, Skills.AGILITY, Skills.CONSTRUCTION, Skills.SUMMONING,
                Skills.DIVINATION };
        for (int skill : unchanged) {
            assertEquals(ENUM_680[skill], 99, Skills.getLevelCap(skill));
            assertEquals(ENUM_680[skill], 99, Skills.getLevelForXp(skill, DEFAULT_L120));
        }
    }

    @Test public void theTwoNewSkillsAreCapOneTwentyOnTheDefaultCurve() {
        for (int skill : new int[] { Skills.ARCHAEOLOGY, Skills.NECROMANCY }) {
            assertEquals(120, Skills.getLevelCap(skill));
            assertEquals(DEFAULT_L120, Skills.getXPForLevel(skill, 120));
            assertEquals(1, Skills.getLevelForXp(skill, 0));
            assertEquals(120, Skills.getLevelForXp(skill, DEFAULT_L120));
        }
    }

    // ------------------------------------------------------------------ maximum experience

    @Test public void theMaximumExperienceIsTheClientsTwoHundredMillion() {
        assertEquals(CACHE_MAX_EXPERIENCE, (long) Skills.MAXIMUM_EXP);
    }

    @Test public void experienceAboveTheMaximumClampsAndTheLevelFollowsTheCap() {
        Skills skills = zeroed();
        skills.addSkillXpRefresh(Skills.ATTACK, 5000000000D);
        assertEquals(CACHE_MAX_EXPERIENCE, (long) skills.getXp(Skills.ATTACK));
        assertEquals("Attack is capped at 120", 120, skills.getLevel(Skills.ATTACK));

        skills.addSkillXpRefresh(Skills.DEFENCE, 5000000000D);
        assertEquals(CACHE_MAX_EXPERIENCE, (long) skills.getXp(Skills.DEFENCE));
        assertEquals("Defence is still capped at 99", 99, skills.getLevel(Skills.DEFENCE));

        skills.addSkillXpRefresh(Skills.NECROMANCY, 5000000000D);
        assertEquals(CACHE_MAX_EXPERIENCE, (long) skills.getXp(Skills.NECROMANCY));
        assertEquals(120, skills.getLevel(Skills.NECROMANCY));
    }

    @Test public void theTotalLevelSumsAllTwentyNineStatsLikeScript4708() {
        Skills skills = zeroed();
        assertEquals("29 stats at level 1", 29, skills.getTotalLevel());
        skills.addSkillXpRefresh(Skills.ARCHAEOLOGY, DEFAULT_L120);
        skills.addSkillXpRefresh(Skills.NECROMANCY, DEFAULT_L99);
        assertEquals(29 - 2 + 120 + 99, skills.getTotalLevel());
    }

    // ------------------------------------------------------------------ combat level, script 1432

    @Test public void combatLevelIsScript1432OnBothBranches() {
        // max(att+str, 2*rng, 2*mag, 2*nec) -> *13/10 -> + def + con + pray/2
        // + (members ? summ/2 : 1) -> /4, truncating at every step.
        Skills base = zeroed();
        assertEquals("everything at level 1, members", 1, base.getCombatLevelWithSummoning());
        assertEquals("everything at level 1, free", 1, base.getCombatLevel());

        // A Necromancy-only character: 99 Necromancy and nothing else.
        Skills necro = zeroed();
        necro.addSkillXpRefresh(Skills.NECROMANCY, DEFAULT_L99);
        // max = 2*99 = 198; (198*13)/10 = 257; members (257+1+1+0+0)/4 = 64
        assertEquals(64, necro.getCombatLevelWithSummoning());
        // free-to-play substitutes a literal 1 for summoning/2: (257+1+1+0+1)/4 = 65
        assertEquals(65, necro.getCombatLevel());

        // Magic weighs x2, not the 910 model's x1.5.
        Skills mage = zeroed();
        mage.addSkillXpRefresh(Skills.MAGIC, DEFAULT_L99);
        assertEquals(64, mage.getCombatLevelWithSummoning());

        // Ranged likewise.
        Skills ranger = zeroed();
        ranger.addSkillXpRefresh(Skills.RANGE, DEFAULT_L99);
        assertEquals(64, ranger.getCombatLevelWithSummoning());

        // Both halves truncate before they are added: prayer 99 -> 49, summoning 99 -> 49.
        Skills prayer = zeroed();
        prayer.addSkillXpRefresh(Skills.PRAYER, DEFAULT_L99);
        prayer.addSkillXpRefresh(Skills.SUMMONING, DEFAULT_L99);
        // max = 1+1 = 2; (2*13)/10 = 2; (2+1+1+49+49)/4 = 25
        assertEquals(25, prayer.getCombatLevelWithSummoning());
        // free: (2+1+1+49+1)/4 = 13
        assertEquals(13, prayer.getCombatLevel());

        // The highest the cache caps allow, and there is no clamp at 126 or 138.
        Skills maxed = zeroed();
        maxed.addSkillXpRefresh(Skills.ATTACK, DEFAULT_L120);
        maxed.addSkillXpRefresh(Skills.STRENGTH, DEFAULT_L120);
        maxed.addSkillXpRefresh(Skills.DEFENCE, DEFAULT_L99);
        maxed.addSkillXpRefresh(Skills.HITPOINTS, DEFAULT_L99);
        maxed.addSkillXpRefresh(Skills.PRAYER, DEFAULT_L99);
        maxed.addSkillXpRefresh(Skills.SUMMONING, DEFAULT_L99);
        // max = 240; (240*13)/10 = 312; (312+99+99+49+49)/4 = 152
        assertEquals(152, maxed.getCombatLevelWithSummoning());

        // Necromancy joins the same max(), so 120 Necromancy alone matches 120+120 melee.
        Skills necroMax = zeroed();
        necroMax.addSkillXpRefresh(Skills.NECROMANCY, DEFAULT_L120);
        necroMax.addSkillXpRefresh(Skills.DEFENCE, DEFAULT_L99);
        necroMax.addSkillXpRefresh(Skills.HITPOINTS, DEFAULT_L99);
        necroMax.addSkillXpRefresh(Skills.PRAYER, DEFAULT_L99);
        necroMax.addSkillXpRefresh(Skills.SUMMONING, DEFAULT_L99);
        assertEquals(152, necroMax.getCombatLevelWithSummoning());
    }

    /** A Skills with every stat at level 1 and zero experience, matching the client's initialiser. */
    private static Skills zeroed() {
        Skills skills = new Skills();
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
            skills.setXpWithoutRefresh(skill, 0);
            skills.setLevelWithoutRefresh(skill, 1);
        }
        return skills;
    }
}
