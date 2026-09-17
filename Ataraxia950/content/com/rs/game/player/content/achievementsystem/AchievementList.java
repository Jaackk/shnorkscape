package com.rs.game.player.content.achievementsystem;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 * Created on Oct 25, 2018.
 */
public enum AchievementList {

    /**
     * Easy
     */
    COMPLETE_10_CLUESCROLLS("Complete 10 Clue Scrolls.", "Investigator I", Rewards.EASY_CLUE_SCROLL, AchievementDifficulty.EASY, 10),
    CRAFT_70_MILESTONE_CAPE("Craft the tier 70 Milestone Cape.",  "Getting There", Rewards.COINS_1M, AchievementDifficulty.EASY, 1),
    SMITH_RUNE_PLATEBODY("Smith a Rune Platebody.", "Blacksmith", Rewards.COINS_1M, AchievementDifficulty.EASY, 1),
    COOK_10_SUMMER_PIES("Cook 10 Summer Pies.", "Baked Goods", Rewards.COINS_1M, AchievementDifficulty.EASY, 10),
    CRAFT_DRAGONSTONE_AMULET("Craft a Dragonstone Amulet.", "Dragon Jewel", Rewards.COINS_1M, AchievementDifficulty.EASY, 1),
    RUNECRAFT_500_BLOOD_RUNES("Runecraft 500 Blood Runes.", "Blood Sacrifice", Rewards.BLOOD_RUNES, AchievementDifficulty.EASY, 500),
    COMPLETE_25_SLAYER_TASKS("Complete 25 Slayer tasks.", "On Task I", Rewards.ABYSSAL_WHIP, AchievementDifficulty.EASY, 25),
    UNLOCK_BETA_SHIP("Unlock the Beta Ship at Player-owned port.", "The Skipper", Rewards.COINS_1M, AchievementDifficulty.EASY, 1),
    REACH_1000_LOYALTY("Reach 1000 Loyalty points.", "Dedication", Rewards.COINS_1M, AchievementDifficulty.EASY, 1000),
    REACH_20_DOMINION_TOWER_KC("Reach a boss killcount of 20 at Dominion Tower.", "Kill Confirmed", Rewards.COINS_1M, AchievementDifficulty.EASY, 20),
    WIN_10_PEST_CONTROL_GAMES("Win 10 Pest Control games.", "A-Void Conflict", Rewards.VOID_GLOVES, AchievementDifficulty.EASY, 10),
    REACH_20_TRIVIA_POINTS("Reach 20 Trivia points.", "Star Student", Rewards.COINS_1M, AchievementDifficulty.EASY, 20),
    REACH_20_TOTAL_VOTES("Reach 20 total game vote points.", "Citizenship I", Rewards.MYSTERY_BOX, AchievementDifficulty.EASY, 20),
    RUN_100_TOTAL_AGILITY_LAPS("Run 100 Agility course laps.", "Track Star I", Rewards.SILVERHAWK_FEATHERS, AchievementDifficulty.EASY, 100),
    REACH_10_HRS_PLAYTIME("Reach 10 hours of in-game playtime.", "The New Guy", Rewards.COINS_1M, AchievementDifficulty.EASY, 10),
    COMPLETE_5_DAILY_TASKS("Complete 5 skilling contracts.", "Skilling Addict I", Rewards.MYSTERY_BOX, AchievementDifficulty.EASY, 5),
    OBTAIN_DRAGON_DEFENDER("Obtain the Dragon Defender.", "I'm A Warrior", Rewards.CORRUPTION_SIGIL, AchievementDifficulty.EASY, 1),
    
    /**
     * Medium
     */
    COMPLETE_25_CLUE_SCROLLS("Complete 25 Clue Scrolls.", "Investigator II", Rewards.MEDIUM_CLUE_SCROLL, AchievementDifficulty.MEDIUM, 25),
    COMPLETE_50_SLAYER_TASKS("Complete 50 Slayer tasks.", "On Task II", Rewards.COINS_5M, AchievementDifficulty.MEDIUM, 50),
    COMPLETE_10_REAPER_TASKS("Complete 10 Reaper tasks.", "Reapers Sins I", Rewards.COINS_5M, AchievementDifficulty.MEDIUM, 10),
    LOOT_100_BARROWS_CHESTS("Loot 100 Barrows chests.", "Sibling Love I", Rewards.COINS_5M, AchievementDifficulty.MEDIUM, 100),
    CRAFT_90_MILESTONE_CAPE("Craft the tier 90 Milestone Cape.", "Roarin 90s", Rewards.HUGE_EXP_LAMP, AchievementDifficulty.MEDIUM, 1),
    HARVEST_MAGIC_TREE("Harvest a Magic Tree.", "Fall Harvest", Rewards.LARGE_FARMING_LAMP, AchievementDifficulty.MEDIUM, 1),
    RUN_250_AGILITY_LAPS("Run 250 Agility course laps.", "Track Star II", Rewards.SILVERHAWK_FEATHERS_100, AchievementDifficulty.MEDIUM, 250),
    REACH_100_HRS_OF_PLAYTIME("Reach 100 hours of in-game playtime.", "Fitting In", Rewards.VETERAN_CAPE_5, AchievementDifficulty.MEDIUM, 100),
    OBTAIN_CORRUPTED_DEFENDER("Obtain a Corrupted Defender.", "Corrupt Guard", Rewards.DHAROK_SET, AchievementDifficulty.MEDIUM, 1),
    OBTAIN_TAINTED_REPRISER("Obtain a Tainted Repriser.", "Tainted Archer", Rewards.KARIL_SET, AchievementDifficulty.MEDIUM, 1),
    REACH_50_TOTAL_VOTES("Reach 50 total game vote points.", "Citizenship II", Rewards.RARE_ITEM_TOKENS_2, AchievementDifficulty.MEDIUM, 50),
    OBTAIN_BLIGHTED_REBOUNDER("Obtain a Blighted Rebounder.", "Blighted Rally", Rewards.AHRIM_SET, AchievementDifficulty.MEDIUM, 1),
    COMPLETE_20_DAILY_TASKS("Complete 25 skilling contracts.", "Skilling Addict II", Rewards.MYSTERY_BOX_2, AchievementDifficulty.MEDIUM, 25),
    OBTAIN_BARROW_DEFENDERS("Obtain all three Barrows Defenders.", "Barrows Brother", Rewards.ANCIENT_EMBLEM, AchievementDifficulty.MEDIUM, 3),
   
    /**
     * Hard
     */
    COMPLETE_100_CLUE_SCROLLS("Complete 100 Clue Scrolls.", "Investigator III", Rewards.HARD_CLUE_SCROLL, AchievementDifficulty.HARD, 100),
    COMPLETE_100_SLAYER_TASKS("Complete 100 Slayer tasks.", "On Task III", Rewards.COINS_10M, AchievementDifficulty.HARD, 100),
    COMPLETE_50_REAPER_TASKS("Complete 50 Reaper tasks.", "Reapers Sins II", Rewards.COINS_10M, AchievementDifficulty.HARD, 50),
    LOOT_250_BARROWS_CHESTS("Loot 250 Barrows chests.", "Sibling Love II", Rewards.COINS_10M, AchievementDifficulty.HARD, 250),
    OBTAIN_SKILLING_OUTFITS("Obtain skilling outfit pieces.", "Full Wardrobe", Rewards.HUGE_EXP_LAMP, AchievementDifficulty.HARD, 97),
    RUN_1000_AGIL_LAPS("Run 1,000 Agility course laps.", "Track Star III", Rewards.SILVERHAWK_FEATHERS_150, AchievementDifficulty.HARD, 1000),
    REACH_250_HRS_OF_PLAYTIME("Reach 250 hours of in-game playtime.", "The Veteran", Rewards.VETERAN_CAPE_10, AchievementDifficulty.HARD, 250),
    PURCHASE_FULL_SLAYER_HELMET("Purchase a full Slayer Helm.", "Slayer Safety", Rewards.COINS_10M, AchievementDifficulty.HARD, 1),
    OBTAIN_ANCIENT_DEFENDER("Obtain an Ancient Defender.", "Ancient Guard", Rewards.COINS_10M, AchievementDifficulty.HARD, 1),
    OBTAIN_ANCIENT_REPRISER("Obtain an Ancient Repriser.", "Ancient Archer", Rewards.COINS_10M, AchievementDifficulty.HARD, 1),
    OBTAIN_ANCIENT_LANTERN("Obtain an Ancient Lantern.", "Ancient Light", Rewards.COINS_10M, AchievementDifficulty.HARD, 1),
    REACH_100_TOTAL_VOTES("Reach 100 total game vote points.", "Citizenship III", Rewards.RARE_ITEM_TOKENS_3, AchievementDifficulty.HARD, 100),
    CRAFT_ASCENSION_CBOW("Craft an Ascension Crossbow.", "From Up Above", Rewards.ASCENDRI_BOLTS_500, AchievementDifficulty.HARD, 1),
//    CREATE_ALL_SUPERIOR_PORTS_ARMOR("Craft all superior Player-owned port armours", "The Deckhand", Rewards.ROCKTAIL_SOUP_200, AchievementDifficulty.HARD, 20),
    COMPLETE_50_DAILY_TASKS("Complete 50 skilling contracts.", "Skilling Addict III", Rewards.MYSTERY_BOX_3, AchievementDifficulty.HARD, 50),
    KILL_250_GWD1_BOSSES("Defeat 250 bosses inside the God Wars Dungeon.", "God Slayer I", Rewards.COINS_10M, AchievementDifficulty.HARD, 250),
    KILL_5000_MONSTERS("Kill a total of 5,000 npc's.", "Killer I", Rewards.MYSTERY_BOX, AchievementDifficulty.HARD, 5000),
    OBTAIN_NEX_DEFENDERS("Obtain all three Nex Defenders.", "Of Zaros", Rewards.PERFECT_CHITIN, AchievementDifficulty.HARD, 3),
   
    /**
     * Elite
     */
    COMPLETE_250_CLUESCROLLS("Complete 250 Clue Scrolls.", "Investigator IV", Rewards.ELITE_CLUE_SCROLL, AchievementDifficulty.ELITE, 250),
    CREATE_100_SUPREME_OVERLOADS("Create 100 Supreme Overload potions.", "Overloaded", Rewards.HUGE_EXP_LAMP, AchievementDifficulty.ELITE, 100),
    LOOT_500_BARROWS_CHESTS("Loot 500 Barrows chests", "Sibling Love III", Rewards.CORRUPTION_SIGIL, AchievementDifficulty.ELITE, 500),
    UNLOCK_ALL_PORT_BOATS("Unlock all Player-owned port boats.", "The Captain", Rewards.COINS_15M, AchievementDifficulty.ELITE, 5),
    COMPLETE_500_SLAYER_TASKS("Complete 500 Slayer tasks.", "On Task IV", Rewards.COINS_15M, AchievementDifficulty.ELITE, 500),
    COMPLETE_25_FIGHT_KILNS("Complete 25 Fight Kiln challenges.", "Kilns Journey", Rewards.COINS_15M, AchievementDifficulty.ELITE, 25),
//    WIN_VERY_RARE_FROM_SOF("Win a Very Rare reward from the SoF.", "RNGesus", Rewards.MIMIC_PLUSH, AchievementDifficulty.ELITE, 20),
    ACHIEVE_FINAL_BOSS_TITLE("Achieve the Final Boss title.", "Final Chapter", Rewards.HYDRIX, AchievementDifficulty.ELITE, 1),
    OBTAIN_BOSS_PET("Obtain a boss pet.", "Beast Handler", Rewards.BABY_TROLL, AchievementDifficulty.ELITE, 1),
    OBTAIN_SKILLING_PET("Obtain a skilling pet.", "Zoo Keeper", Rewards.ROWENA, AchievementDifficulty.ELITE, 1),
    RUN_2500_AGIL_LAPS("Run 2,500 Agility course laps.", "Track Star IV", Rewards.SILVERHAWK_BOOTS, AchievementDifficulty.ELITE, 2500),
    REACH_1000_HRS_PLAYTIME("Reach 1,000 hours of in-game playtime.", "The No-Lifer", Rewards.VETERAN_CAPE_15, AchievementDifficulty.ELITE, 1000),
//    CRAFT_COLORED_SLAYER_HELMS("Craft a colored Slayer Helm.", "Art of Slayer", Rewards.COINS_15M, AchievementDifficulty.ELITE, 20),
    OBTAIN_KALPHITE_DEFENDER("Obtain a Kalphite Defender.", "KK's Defense", Rewards.COINS_15M, AchievementDifficulty.ELITE, 1),
    OBTAIN_KALPHITE_REPRISER("Obtain a Kalphite Repriser.", "KK's Archer", Rewards.COINS_15M, AchievementDifficulty.ELITE, 1),
    OBTAIN_KALPHITE_REBOUNDER("Obtain a Kalphite Rebounder.", "KK's Reflector", Rewards.COINS_15M, AchievementDifficulty.ELITE, 1),
    OBTAIN_MAX_CAPE("Obtain the Max Cape.", "Mad Max", Rewards.COINS_20M, AchievementDifficulty.ELITE, 1),
    COMPLETE_100_DAILY_TASKS("Complete 100 skilling contracts.", "Skilling Addict IV", Rewards.MYSTERY_BOX_5, AchievementDifficulty.ELITE, 100),
    KILL_500_GWD2_BOSSES("Defeat 500 bosses inside the God Wars Dungeon 2.", "God Slayer II", Rewards.GWD_SPECIALIST, AchievementDifficulty.ELITE, 500),
    KILL_10000_MONSTERS("Kill a total of 10,000 npc's.", "Killer II", Rewards.MYSTERY_BOX_2, AchievementDifficulty.ELITE, 10000);
	
	private final String name;
	private final String miniName;
	private final Rewards reward;
	private final AchievementDifficulty difficulty;
	private final int amountToComplete;

	AchievementList(String name, String miniName, Rewards reward, AchievementDifficulty difficulty, int amountToComplete) {
		this.name = name;
		this.miniName = miniName;
		this.reward = reward;
		this.difficulty = difficulty;
		this.amountToComplete = amountToComplete;
	}

	public String getName() {
		return name;
	}

	public String getMiniName() {
		return miniName;
	}

	public Rewards getReward() {
		return reward;
	}

	public AchievementDifficulty getDifficulty() {
		return difficulty;
	}

	public int getAmountToComplete() {
		return amountToComplete;
	}

}
