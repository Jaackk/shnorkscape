package com.rs.game.player.content.achievements.ctx;

import lombok.Getter;

/**
 * @author David (Chryonic)
 */
public enum AchievementList {
    /**
     * Easy
     */
    COMPLETE_10_CLUESCROLLS("Complete 10 Clue Scrolls.", Rewards.EASY_CLUE_SCROLL, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 10),
    CRAFT_70_MILESTONE_CAPE("Craft the Level 70 Milestone Cape.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 1),
    SMITH_RUNE_PLATEBODY("Smith a Rune Platebody.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 1),
    COOK_10_SUMMER_PIES("Cook 10 Summer Pies.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 10),
    CRAFT_DRAGONSTONE_AMULET("Craft a Dragonstone Amulet.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 1),
    RUNECRAFT_500_BLOOD_RUNES("Runecraft 500 Blood Runes.", Rewards.BLOOD_RUNES, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 500),
    COMPLETE_25_SLAYER_TASKS("Complete 25 Slayer Tasks.", Rewards.ABYSSAL_WHIP, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 25),
    UNLOCK_BETA_SHIP("Unlock the Beta Ship at PoP.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 1),
    REACH_1000_LOYALTY("Reach 1000 Loyalty Points.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 1000),
    REACH_1000_DOMINION_TOWER_KC("Reach a kc of 1000 at DT.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 1000),
    WIN_10_PEST_CONTROL_GAMES("Win 10 Pest Control games.", Rewards.VOID_GLOVES, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 10),
    REACH_100_TRIVIA_POINTS("Reach 100 Trivia Points.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 100),
    REACH_50_TOTAL_VOTES("Reach 50 total Votes.", Rewards.MYSTERY_BOX, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 50),
    RUN_100_TOTAL_AGILITY_LAPS("Run 100 Agility course laps.", Rewards.SILVERHAWK_FEATHERS, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 100),
    REACH_10_HRS_PLAYTIME("Reach 10 hrs of playtime.", Rewards.COINS_1M, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 10),
    COMPLETE_5_DAILY_TASKS("Complete 5 Daily Tasks.", Rewards.MYSTERY_BOX, AchievementState.NOT_STARTED, AchievementDifficulty.EASY, 0, 5),
    /**
     * Medium
     */
    COMPLETE_25_CLUE_SCROLLS("Complete 25 Clue Scrolls.", Rewards.MEDIUM_CLUE_SCROLL, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 25),
    COMPLETE_50_SLAYER_TASKS("Complete 50 Slayer Tasks.", Rewards.COINS_5M, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 50),
    COMPLETE_10_REAPER_TASKS("Complete 10 Reaper Tasks.", Rewards.COINS_5M, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 10),
    LOOT_100_BARROWS_CHESTS("Loot 100 Barrows Chests.", Rewards.COINS_5M, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 100),
    CRAFT_90_MILESTONE_CAPE("Craft the 90 Milestone Cape.", Rewards.HUGE_EXP_LAMP, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 1),
    HARVEST_MAGIC_TREE("Harvest a Magic Tree.", Rewards.LARGE_FARMING_LAMP, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 1),
    RUN_250_AGILITY_LAPS("Run 250 Agility Laps.", Rewards.SILVERHAWK_FEATHERS_100, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 250),
    REACH_100_HRS_OF_PLAYTIME("Reach 100 hrs of playtime.", Rewards.VETERAN_CAPE_5, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 100),
    OBTAIN_CORRUPTED_DEFENDER("Obtain Corrupted Defender.", Rewards.DHAROK_SET, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 1),
    OBTAIN_TAINTED_REPRISER("Obtain Tainted Repriser.", Rewards.KARIL_SET, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 1),
    OBTAIN_BLIGHTED_REBOUNDER("Obtain Blighted Rebounder.", Rewards.AHRIM_SET, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 1),
    COMPLETE_20_DAILY_TASKS("Complete 20 Daily Tasks.", Rewards.MYSTERY_BOX_2, AchievementState.NOT_STARTED, AchievementDifficulty.MEDIUM, 0, 20),
    /**
     * Hard
     */
    COMPLETE_100_CLUE_SCROLLS("Complete 100 Clue Scrolls.", Rewards.HARD_CLUE_SCROLL, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    COMPLETE_100_SLAYER_TASKS("Complete 100 Slayer Tasks.", Rewards.COINS_10M, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    COMPLETE_50_REAPER_TASKS("Complete 50 Reaper Tasks.", Rewards.COINS_10M, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    LOOT_250_BARROWS_CHESTS("Loot 250 Barrows Chests.", Rewards.COINS_10M, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    OBTAIN_SKILLING_OUTFITS("Obtain all Skilling outfits.", Rewards.HUGE_EXP_LAMP, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    RUN_1000_AGIL_LAPS("Run 1,000 Agility Laps.", Rewards.SILVERHAWK_FEATHERS_150, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    REACH_250_HRS_OF_PLAYTIME("Reach 250 hrs of playtime.", Rewards.VETERAN_CAPE_10, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    CRAFT_FULL_SLAYER_HELMET("Craft a Full Slayer Helm.", Rewards.COINS_10M, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    OBTAIN_ANCIENT_DEFENDER("Obtain an Ancient Defender.", Rewards.COINS_10M, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    OBTAIN_ANCIENT_REPRISER("Obtain an Ancient Repriser.", Rewards.COINS_10M, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    OBTAIN_ANCIENT_LANTERN("Obtain an Ancient Lantern.", Rewards.COINS_10M, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    CRAFT_ASCENSION_CBOW("Craft an Ascension CBow.", Rewards.RUBY_BOLTS_2000, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    CREATE_ALL_SUPERIOR_PORTS_ARMOR("Craft all Superior PoP Armor.", Rewards.ROCKTAIL_SOUP_200, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    OBTAIN_MAX_CAPE("Obtain your max cape.", Rewards.COINS_20M, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    COMPLETE_50_DAILY_TASKS("complete 50 Daily Tasks.", Rewards.MYSTERY_BOX_3, AchievementState.NOT_STARTED, AchievementDifficulty.HARD, 0, 20),
    /**
     * Elite
     */

    COMPLETE_250_CLUESCROLLS("Complete 250 Clue Scrolls.", Rewards.ELITE_CLUE_SCROLL, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    CREATE_100_SUPREME_OVERLOADS("Create 100 Supreme Overloads.", Rewards.HUGE_FARMING_LAMP, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    LOOT_1000_BARROWS_CHESTS("Loot 1,000 Barrows Chests", Rewards.CORRUPTION_SIGIL, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    UNLOCK_ALL_PORT_BOATS("Unlock all PoP boats.", Rewards.COINS_15M, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    COMPLETE_500_SLAYER_TASKS("Complete 500 Slayer Tasks.", Rewards.COINS_15M, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    COMPLETE_25_FIGHT_KILNS("Complete 25 Fight Kilns.", Rewards.COINS_15M, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    WIN_VERY_RARE_FROM_SOF("Win a Very Rare from the SoF.", Rewards.MIMIC_PLUSH, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    ACHIEVE_FINAL_BOSS_TITLE("Achieve the Final Boss Title.", Rewards.HYDRIX, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    OBTAIN_BOSS_PET("Obtain a boss pet.", Rewards.BABY_TROLL, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    OBTAIN_SKILLING_PET("Obtain a skilling pet.", Rewards.ROWENA, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    RUN_2500_AGIL_LAPS("Run 2,500 Agility Laps.", Rewards.SILVERHAWK_BOOTS, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    REACH_1000_HRS_PLAYTIME("Reach 1,000 hrs of playtime.", Rewards.VETERAN_CAPE_15, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    CRAFT_COLORED_SLAYER_HELMS("Craft colored Slayer Helms.", Rewards.COINS_15M, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    OBTAIN_KALPHITE_DEFENDER("Obtain a Kalphite Defender.", Rewards.COINS_15M, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    OBTAIN_KALPHITE_REPRISER("Obtain a Kalphite Repriser.", Rewards.COINS_15M, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    OBTAIN_KALPHITE_REBOUNDER("Obtain a Kalphite Rebounder.", Rewards.COINS_15M, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    OBTAIN_COMP_CAPE_T("Obtain your Comp Cape (t).", Rewards.COINS_30M, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20),
    COMPLETE_100_DAILY_TASKS("Complete 100 Daily Tasks.", Rewards.MYSTERY_BOX_5, AchievementState.NOT_STARTED, AchievementDifficulty.ELITE, 0, 20);


    @Getter
    private final String name;
    @Getter
    private final Rewards rewards;
    private AchievementState state;
    private final AchievementDifficulty difficulty;
    private final int amountDone;
    @Getter
    private final int amountToComplete;

    AchievementList(String name, Rewards rewards, AchievementState state, AchievementDifficulty difficulty, int amountDone, int amountToComplete) {
        this.name = name;
        this.rewards = rewards;
        this.state = state;
        this.difficulty = difficulty;
        this.amountDone = amountDone;
        this.amountToComplete = amountToComplete;
    }


    public AchievementState setState(AchievementState state) {
        return this.state = state;
    }

    public AchievementState getState() {
        return state;
    }

    public AchievementDifficulty getDifficulty() {
        return difficulty;
    }

}

