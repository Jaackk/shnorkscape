package com.rs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.DailyManager;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.utils.Colors;
import com.rs.utils.WorldInformation;

/**
 * A class used to store all server-wide/in-game configurations.
 *
 * @author Noel
 */
public final class Settings {

    /**
     * Server data configuration
     **/
    public static final String SERVER_NAME = "RuneScape";
    /* Server management */
    public static final int MINIMUM_RAM_ALLOCATED = 2000000000; // 1000mb
    /**
     * Player configurations
     **/
    public static final int START_PLAYER_HITPOINTS = 100;
    public static final WorldTile START_PLAYER_LOCATION = new WorldTile(3222, 3218, 0);
    public static final WorldTile RESPAWN_PLAYER_LOCATION = new WorldTile(3222, 3218, 0);
    public static final int MAX_STARTER_COUNT = 1;

    public static final float PERFECT_RING_DROP_DUPLICATION_RATE = 0.04F;// 4%

    public static final String[] ELEVATED_ACCESS_ACCOUNTS = {};
    public static final boolean NO_AUTOMATIC_OWNER = true;
    public static final List<String> UNRESTRICTED_COMMAND_ACCOUNTS = new ArrayList<String>();

    public static final String[] FAKE_DEV_ACCOUNTS = {};

    public static final String[] AUTOMATICALLY_FLAGGED_WORDS = {"nigger"};

    /**
     * Game configuration
     **/
    public static final int AIR_GUITAR_MUSICS_COUNT = 250;
    public static final int QUESTS = 183;
    public static final long MAX_PACKETS_DECODER_PING_DELAY = 30000;
    public static final int WORLD_CYCLE_TIME = 600; // recycle 600 milliseconds
    /**
     * Well of Goodwill At the request of Jaedmo, this is set statically to 100mil.
     **/
    public static final int WELL_MAX_AMOUNT = 100000000;

    public static final int[] NON_WALKING_NPCS = {6893, 340, 756, 522, 557, 44, 554, 7914, 279, 594, 961, 669, 538, 537, 549, 546, 550, 2676, 548, 598, 531, 659, 2824, 2234, 5913, 4247, 6539, 9400, 19519, 9085, 278, 22153, 14381, 5776, 2059};

    public static final int[] FORCE_WALKING_NPCS = {15309, 18150, 18151, 18153, 18155, 18157, 18159, 18161, 18163, 18165, 18167, 18169, 18171, 18204, 21630, 21631, 21632};


    public static final List<String> MASTER_IPS = new ArrayList<String>();
    public static final int MASTER_PIN = Integer.getInteger("ataraxia.legacy.masterPin", -1);
    public static final boolean PRINTS = true;
    public static boolean DOUBLE_DROPS = false;
    public static boolean DXP = false;
    public static boolean TRIPLE_EXP_ENABLED = false;

    public static boolean atHomeArea(Player p) {
    	return p.getX() >= 4075 && p.getX() <= 4176 && p.getY() >= 5816 && p.getY() <= 5895;
    }

	/**
	 * Squeal of Fortune code moved into the proper place; com -> rs -> game -> player -> SquealOfFortune.java
	 */
	public static int SERVER_PORT = 43595;
	public static int WORLD_ID = 1;
	/**
	 * SQL IP connection
	 **/
	public static String WEBHOST_IP = "127.0.0.1";
	public static boolean SQL_ENABLED = false;
	public static boolean SQL_RETRY = false;

    /**
     * DEV MODE FOR ALL
     */
    public static boolean TEST_SERVER_MODE = false;

    public static boolean BOT_ENABLED = false;
    /**
     * Home region
     **/
    public static int MARKET_REGION_ID = 11324;
    public static int HOME_REGION_ID = 9265;

    /**
     * Update log stuffs
     */
    public static String LATEST_UPDATE = Colors.LPURPLE + "Welcome to RuneScape";


    public static String CRASH = "";
    public static String DONATE = "";
    public static String VOTE = "";
    public static String YOUTUBE = "";
    public static String RS3WIKI = "";
    public static String ITEMDB = "";
    public static String THREAD = "";

    public static boolean ARAXXOR_DEBUG = true;
    public static boolean DEBUG = false, GUI_MODE = false, SUPERLOG, WORLDPVP;
    public static int VET_XP = 1, INTERM_XP = 1, INTERMEDIATE_XP = 1, IRONMAN_XP = 1, HCIRONMAN_XP = 1, EXPERT_XP = 1, KING_OF_THE_SKILL_XP = 1;

    public static double VET_DROP = 1, INTERM_DROP = 1, INTERMEDIATE_DROP = 1, IRONMAN_DROP = 1, HCIRONMAN_DROP = 1, EXPERT_DROP = 1, KING_OF_THE_SKILL_DROP = 1;


    // Disable or Enable Yell.
    public static boolean serverYell = true;
    public static String yellChangedBy;
    // MISC
    public static boolean LENDING_DISABLED = true;
    public static int ZEAL_MODIFIER = World.isWeekend() ? 3 : 3;
    public static int PC_MODIFIER = World.isWeekend() ? 2 : 2;
    public static int DUNG_MODIFIER = World.isWeekend() ? 2 : 2;


	public static boolean WORLD_DC_DEBUG = true;
	public static boolean AUTO_KICK_SOULLESS = true;

	public static boolean DUAL_COMBAT = true;
	public static double static_damage_buff = 0.47;

	/**
	 * Routes accuracy and damage through the pre-EOC RS2 combat formula in
	 * com.rs.game.player.combat.rs2 instead of the existing EOC-style math.
	 * Off by default; flip on for test worlds while the integration is staged
	 * in. Mutable so it can be toggled at runtime by an admin command.
	 */
	public static boolean RS2_COMBAT = true;

	/**
	 * When true, RS2 combat sources its attack/strength/defence bonuses from
	 * ClassicItemBonusResolver (per-item classic schema with tier-derived
	 * fallback) instead of the legacy /10 cache divisor in
	 * Rs2AtaraxiaCacheBonuses. Independent from RS2_COMBAT so gear and math
	 * can be A/B tested separately. Defaults off; rebalances every existing
	 * build silently when flipped, so stage to test worlds first.
	 */
	public static boolean RS2_ITEM_BONUSES = false;

	
	public static double dual_combat_dmg_modifier = 0.15;
	public static double twohand_combat_dmg_modifier = 0.15;
	public static double pvp_combat_dmg_modifier = -0.50;
	
	public static int dual_combat_speed_modifier = 0;
	public static int twohand_combat_speed_modifier = 0;


	public static int getExperienceMultiplier(final Player player) {
		if (player.isIronMan()) {
			return IRONMAN_XP;
		}
		if (player.isHCIronMan()) {
			return IRONMAN_XP;
		}
		if (player.isNoviceIronMan()) {
			return INTERM_XP;
		}
		if (player.isExpertIronMan()) {
			return VET_XP;
		}
		if (player.isExpert()) {
			return VET_XP;
		}
		if (player.isIntermediate()) {
			return INTERMEDIATE_XP;
		}
		if (player.isIntermediateIronMan()) {
			return INTERMEDIATE_XP;
		}
		if (player.isNovice()) {
			return INTERM_XP;
		}
		if (player.isLegendary()) {
			return EXPERT_XP;
		}
		if (player.isKingOfTheSkillGameMode()) {
			return KING_OF_THE_SKILL_XP;
		}
		return 1;
	}

    /**
     * Instancing the system Calendar
     **/
    public static final Calendar cal = Calendar.getInstance();

    /**
     * Special weekend boosts
     **/
    public static boolean SLAYER_WEEKEND = cal.get(Calendar.MONTH) == 11 && (cal.get(Calendar.DATE) >= 25 && cal.get(Calendar.DATE) <= 27);

    public static boolean yellEnabled() {
        return serverYell;
    }

    /**
     * Checks for drop rates.
     *
     * @param player The player.
     * @return The rate.
     */
    public static double getDropQuantityRate(final Player player) {
        double rate = getDropRate(player);
        if (player.getCurrentPet() != null)
            if (player.getCurrentPet().getPerks().contains(PetPerk.NICE_BUT_DIM))
                rate += PetPerkUtils.getProModifierForPerk(player, PetPerk.NICE_BUT_DIM);
        return rate;
    }

    private static double getDropRate(final Player player) {
        if (player.isIronMan()) {
            return IRONMAN_DROP;
        }
        if (player.isHCIronMan()) {
            return HCIRONMAN_DROP;
        }
        if (player.isNoviceIronMan()) {
            return INTERM_DROP;
        }
        if (player.isExpertIronMan()) {
            return VET_DROP;
        }
        if (player.isExpert()) {
            return VET_DROP;
        }
        if (player.isIntermediate()) {
            return INTERMEDIATE_DROP;
        }
        if (player.isIntermediateIronMan()) {
            return INTERMEDIATE_DROP;
        }
        if (player.isNovice()) {
            return INTERM_DROP;
        }
        if (player.isLegendary()) {
            return EXPERT_DROP;
        }
        if (player.isKingOfTheSkillGameMode()) {
            return KING_OF_THE_SKILL_DROP;
        }
        return 1;
    }

    /* Data location file */
    public static String CACHE_PATH = "data/cache/";
    public static final String LOGS_PATH = "data/playersaves/logs/";
    public static String[][] expBoosts = {{"Regular", "25"}, {"Bronze", "25"}, {"Silver", "27.5"}, {"Gold", "30"}, {"Platinum", "32.5"}, {"Diamond", "35"}, {"Master", "40"}};
public static boolean SQL_LOGGING = false;
    public static boolean AOD_ENABLED = true;
    public static final boolean USE_DAMAGE_CAP = false;


    public static final int SV_PLAYERS_LIMIT = 2048;
    public static final int SV_LOCAL_PLAYERS_LIMIT = 2048;
    public static final int SV_NPCS_LIMIT = Short.MAX_VALUE;
    public static final int SV_LOCAL_NPCS_LIMIT = 250;

    /* Map configuration size */
    public static final int[] MAP_SIZES = { 104, 120, 136, 168, 72, 256 };
    public static final int DEFAULT_MAP_SIZE = 5;

    public static DailyManager.DailyTasks[] BLACKLISTED_DAILY_TASKS = {DailyManager.DailyTasks.FISH_SHRIMP};

    public static String[] VORAGO_ROTATION_NAMES = {"Ceiling Collapse", "Scopulus", "Vitalis", "Green Bomb", "Team Split", "The End"};
    public static int VORAGO_ROTATION = 0;
    public static boolean VORAGO_JUMP_DISABLED = true;
    public static int DAYS_TO_CHANGE_ROTATION = 3;
    public static String VORAGO_RELEASE_DATE = "2019-04-20";

    public static String[] SPIDER_BOSS_ROTATION_NAMES = {"Minions/Acid", "Acid/Darkness", "Minions/Darkness"};
    public static int SPIDER_BOSS_ROTATION = 0;
    public static int SPIDER_BOSS_DAYS_TO_CHANGE_ROTATION = 2;
    public static String SPIDER_BOSS_RELEASE_DATE = "2019-04-20";
    
    public static WorldInformation[] WORLDS_INFORMATION;
    
}
