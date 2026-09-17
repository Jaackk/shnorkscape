package com.rs.game.player.content.achievementsystem;

import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.SkillingPets;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.items.Defenders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Oct 25, 2018.
 */
public class Achievements implements Serializable {

    private static final long serialVersionUID = 216783436830108527L;

    private Map<AchievementList, Integer> achievements;
    private List<AchievementList> completedAchivements;
    private boolean updatedAchievements;
    private boolean awarded;
    private boolean completionist;
    private boolean completionistTrimmed;
    private transient Player player;

    public Achievements() {
        this.achievements = new HashMap<AchievementList, Integer>();
        this.completedAchivements = new ArrayList<AchievementList>();
    }

    public void init() {
        // gonna keep this as a just in-case measure.
        if (achievements == null)
            achievements = new HashMap<AchievementList, Integer>();
        if (completedAchivements == null)
            completedAchivements = new ArrayList<AchievementList>();

        if (!updatedAchievements) {
            updateProgress(player.getCompletedClues(), AchievementList.COMPLETE_10_CLUESCROLLS, AchievementList.COMPLETE_25_CLUE_SCROLLS, AchievementList.COMPLETE_100_CLUE_SCROLLS, AchievementList.COMPLETE_250_CLUESCROLLS);
            updateProgress(player.getSlayerTasks(), AchievementList.COMPLETE_25_SLAYER_TASKS, AchievementList.COMPLETE_50_SLAYER_TASKS, AchievementList.COMPLETE_100_SLAYER_TASKS, AchievementList.COMPLETE_500_SLAYER_TASKS);
            if (!completedAchivements.contains(AchievementList.UNLOCK_BETA_SHIP) && player.getPorts().hasSecondShip) {
                updateProgress(1, AchievementList.UNLOCK_BETA_SHIP);
            }
            updateProgress(player.getLoyaltyPoints(), AchievementList.REACH_1000_LOYALTY);
            updateProgress(player.getDominionTower().getKilledBossesCount(), AchievementList.REACH_20_DOMINION_TOWER_KC);
            updateProgress(player.getTotalTrivia(), AchievementList.REACH_20_TRIVIA_POINTS);
            updateProgress(player.getTotalVotes(), AchievementList.REACH_20_TOTAL_VOTES, AchievementList.REACH_50_TOTAL_VOTES, AchievementList.REACH_100_TOTAL_VOTES);
            updateProgress(player.getDailyTasksCompleted(), AchievementList.COMPLETE_5_DAILY_TASKS, AchievementList.COMPLETE_20_DAILY_TASKS, AchievementList.COMPLETE_50_DAILY_TASKS, AchievementList.COMPLETE_100_DAILY_TASKS);
            updateProgress(player.getLapsRan(), AchievementList.RUN_100_TOTAL_AGILITY_LAPS, AchievementList.RUN_250_AGILITY_LAPS, AchievementList.RUN_1000_AGIL_LAPS, AchievementList.RUN_2500_AGIL_LAPS);
            updateProgress(player.getBarrowsRunsDone(), AchievementList.LOOT_100_BARROWS_CHESTS, AchievementList.LOOT_250_BARROWS_CHESTS, AchievementList.LOOT_500_BARROWS_CHESTS);
            updateProgress(player.getTotalContract(), AchievementList.COMPLETE_10_REAPER_TASKS, AchievementList.COMPLETE_50_REAPER_TASKS);
            updateProgress(player.getPestControlGames(), AchievementList.WIN_10_PEST_CONTROL_GAMES);

            if (player.hasItem(new Item(36890))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36891))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36892))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36893))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36894))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(775))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25184))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25183))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25182))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25181))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25180)) || player.hasItem(new Item(34924))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(34920)) || player.hasItem(new Item(34924))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25185))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25186))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25187))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25188))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25189))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(32777)) || player.hasItem(new Item(32281))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(29869))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(29868))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(29865)) || player.hasItem(new Item(32279))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(29867))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(29866))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(32275)) || player.hasItem(new Item(32279))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(7409, 1))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(31347)) || player.hasItem(new Item(34926))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(31346))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(31345))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(31344))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(31343))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(34926)) || player.hasItem(new Item(34922))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13659))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13660))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13661))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(24427))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(24428))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(24429))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(24430))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36899))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36898))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36897))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36896))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(36895))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25194))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25193))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25190)) || player.hasItem(new Item(34923))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25192))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25191))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(34923)) || player.hasItem(new Item(34919))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(20789))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(20791))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(20790))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(20788))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(20787))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(27591))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(27590))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(27589))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(27588))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(27587))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(34921)) || player.hasItem(new Item(34925))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(21487))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(21485))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13631))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13632))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13633))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13630))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13634))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13635))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13636))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13637))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13638))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13639))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13640))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13641))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(13642))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(21486))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(21484))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25195)) || player.hasItem(new Item(32280))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25196))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25197))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25198))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(25199))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(32276)) || player.hasItem(new Item(32280))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(28998))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(28999))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(28995)) || player.hasItem(new Item(32278))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(28997))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(28996))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(32278)) || player.hasItem(new Item(32274))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(21483))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(21482))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(21481))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(21480))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(10933))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(10939))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(10940))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            if (player.hasItem(new Item(10941))) {
                updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
            }

            updateProgress(player.isCompletedFightKiln() ? 1 : 0, AchievementList.COMPLETE_25_FIGHT_KILNS);

            if (player.hasItem(new Item(36153))) {
                updateProgress(1, AchievementList.OBTAIN_CORRUPTED_DEFENDER, AchievementList.OBTAIN_BARROW_DEFENDERS);
            }

            if (player.hasItem(new Item(36176))) {
                updateProgress(1, AchievementList.OBTAIN_TAINTED_REPRISER, AchievementList.OBTAIN_BARROW_DEFENDERS);
            }

            if (player.hasItem(new Item(36168))) {
                updateProgress(1, AchievementList.OBTAIN_BLIGHTED_REBOUNDER, AchievementList.OBTAIN_BARROW_DEFENDERS);
            }

            if (player.hasItem(new Item(36157))) {
                updateProgress(1, AchievementList.OBTAIN_ANCIENT_DEFENDER, AchievementList.OBTAIN_NEX_DEFENDERS);
            }

            if (player.hasItem(new Item(36179))) {
                updateProgress(1, AchievementList.OBTAIN_ANCIENT_REPRISER, AchievementList.OBTAIN_NEX_DEFENDERS);
            }

            if (player.hasItem(new Item(36171))) {
                updateProgress(1, AchievementList.OBTAIN_ANCIENT_LANTERN, AchievementList.OBTAIN_NEX_DEFENDERS);
            }

            if (player.hasItem(new Item(36160))) {
                updateProgress(1, AchievementList.OBTAIN_KALPHITE_DEFENDER);
            }

            if (player.hasItem(new Item(36181))) {
                updateProgress(1, AchievementList.OBTAIN_KALPHITE_REPRISER);
            }

            if (player.hasItem(new Item(36173))) {
                updateProgress(1, AchievementList.OBTAIN_KALPHITE_REBOUNDER);
            }

            if (player.isMax()) {
                updateProgress(1, AchievementList.OBTAIN_MAX_CAPE);
            }

            if (player.getPorts().hasFirstShip) {
                updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
            }

            if (player.getPorts().hasSecondShip) {
                updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
            }

            if (player.getPorts().hasThirdShip) {
                updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
            }

            if (player.getPorts().hasFourthShip) {
                updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
            }

            if (player.getPorts().hasFifthShip) {
                updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
            }

            for (SkillingPets.PetData petData : SkillingPets.PetData.values()) {
                if (player.hasItem(new Item(petData.getPet().getId()))) {
                    updateProgress(1, AchievementList.OBTAIN_SKILLING_PET);
                }
            }

            final int[] bosses = new int[]{33806, 33805, 33807, 33804, 33826, 33828, 33827, 33811, 33816, 33817, 31459, 33748, 33749, 33750, 33751, 33752, 33753, 33812, 33818, 33808, 33825, 33815, 28630, 33717, 33819, 33820, 33821, 33822, 33823, 33824, 27490};
            for (int boss : bosses) {
                if (player.hasItem(new Item(boss))) {
                    updateProgress(1, AchievementList.OBTAIN_BOSS_PET);
                }
            }

            if (player.hasItem(new Item(13263))) {
                updateProgress(1, AchievementList.PURCHASE_FULL_SLAYER_HELMET);
            }

            int gwd1 = player.getKillStatistics(1) + player.getKillStatistics(2) + player.getKillStatistics(3) + player.getKillStatistics(4) + player.getKillStatistics(5);
            updateProgress(gwd1, AchievementList.KILL_250_GWD1_BOSSES);

            int gwd2 = player.getKillStatistics(113) + player.getKillStatistics(114) + player.getKillStatistics(115) + player.getKillStatistics(120) + player.getKillStatistics(121) + player.getKillStatistics(122) + player.getKillStatistics(123) + player.getKillStatistics(124);
            updateProgress(gwd2, AchievementList.KILL_500_GWD2_BOSSES);

            updateProgress(player.getBossKillcount(), AchievementList.KILL_5000_MONSTERS, AchievementList.KILL_10000_MONSTERS);

            updatedAchievements = true;
        }
        if (player.isOwner()) {
            updatedAchievements = false;
        }

        //------------------------------UPDATE ACHIEVEMENTS HERE REALTIME------------------------------
        achievements.put(AchievementList.COMPLETE_25_SLAYER_TASKS, 0);
        achievements.put(AchievementList.COMPLETE_50_SLAYER_TASKS, 0);
        achievements.put(AchievementList.COMPLETE_100_SLAYER_TASKS, 0);
        achievements.put(AchievementList.COMPLETE_500_SLAYER_TASKS, 0);
        updateProgress(player.getSlayerTasks(), AchievementList.COMPLETE_25_SLAYER_TASKS, AchievementList.COMPLETE_50_SLAYER_TASKS, AchievementList.COMPLETE_100_SLAYER_TASKS, AchievementList.COMPLETE_500_SLAYER_TASKS);

        achievements.put(AchievementList.KILL_500_GWD2_BOSSES, 0);
        int gwd2 = player.getKillStatistics(113) + player.getKillStatistics(114) + player.getKillStatistics(115) + player.getKillStatistics(120) + player.getKillStatistics(121) + player.getKillStatistics(122) + player.getKillStatistics(123) + player.getKillStatistics(124);
        updateProgress(gwd2, AchievementList.KILL_500_GWD2_BOSSES);

        if (!completedAchivements.contains(AchievementList.OBTAIN_DRAGON_DEFENDER) && player.hasItem(20072)) {
            updateProgress(1, AchievementList.OBTAIN_DRAGON_DEFENDER);
        }
        if (!achievements.containsKey(AchievementList.OBTAIN_BARROW_DEFENDERS)) {
            Defenders.countBarrowsDefendersForAchievement(player, completedAchivements);
        }
        if (!achievements.containsKey(AchievementList.OBTAIN_NEX_DEFENDERS)) {
            Defenders.countNexDefendersForAchievement(player, completedAchivements);
        }
    }

    public void updateProgress(int amount, AchievementList... lists) {
        for (AchievementList list : lists) {
            achievements.put(list, achievements.getOrDefault(list, 0) + amount);
            checkCompletion(list);
        }
    }

    private void checkCompletion(AchievementList list) {
        if (isComplete(list) && !completedAchivements.contains(list)) {
            completedAchivements.add(list);
            if (ClueScrollDistributor.isClueScroll(list.getReward().getItemId()) && player.getTreasureTrails().hasClueScrollItem()) {
                player.sendMessage("Congratulations! You have successfully completed " + list.getMiniName() + "!");
                player.sendMessage("Your reward is unable to be given because you already have a clue scroll item.");
            } else {
                if (ClueScrollDistributor.isClueScroll(list.getReward().getItemId())) {
                    ClueScrollDistributor.givePlayerClueScroll(player, ClueScrollDistributor.getLevelForId(list.getReward().getItemId()));
                } else {
                    player.addItem(new Item(list.getReward().getItemId(), list.getReward().getAmount()));
                }
                player.getTreasureHunter().setEarnedKeys(player.getTreasureHunter().getEarnedKeys() + 1);
                if (list == AchievementList.REACH_1000_LOYALTY)
                    player.setLoyaltyPoints(player.getLoyaltyPoints() + 500);
                player.sendMessage("Congratulations! You have successfully completed " + list.getMiniName() + " and you have received your reward.");
            }
        }

        if (!completionist) {
            if (hasCompletionistTasksDone()) {
                World.sendWorldMessage("<col=4286f4><img=6>News: " + player.getDisplayName() + " has completed all of the easy and medium achievement tasks!", false);
                HcimNewsManager.getInstance().addNews(player, "<#player> completed all of the easy and medium achievement tasks!");
                completionist = true;
            }
        }

        if (!completionistTrimmed) {
            if (hasCompletionistTrimmedTasksDone()) {
                World.sendWorldMessage("<col=4286f4><img=6>News: " + player.getDisplayName() + " has completed all of the hard and elite achievement tasks!", false);
                HcimNewsManager.getInstance().addNews(player, "<#player> completed all of the hard and elite achievement tasks!");
                completionistTrimmed = true;
            }
        }

        if (hasCompletionistTasksDone() && hasCompletionistTrimmedTasksDone()) {
            if (!awarded) {
                if (!player.hasItem(new Item(36166))) {
                    player.addItem(new Item(36166));
                    player.sendMessage("Congratulations! You have completed all the achievement tasks and have been awarded the Master quest cape!");
                    HcimNewsManager.getInstance().addNews(player, "<#player> completed all the achievement tasks and was awarded the Master quest cape!");
                    World.sendWorldMessage("<col=4286f4><img=6>News: Congratulations! " + player.getDisplayName() + " has completed all of the achievement tasks and has been awarded the Master quest cape!", false);
                    awarded = true;
                }
            }
        }
    }

    public boolean hasCompletionistTasksDone() {
        for (AchievementList list : AchievementList.values()) {
            if (list == null)
                continue;

            if (list.getDifficulty() == AchievementDifficulty.EASY || list.getDifficulty() == AchievementDifficulty.MEDIUM) {
                if (!isComplete(list))
                    return false;
            }
        }
        return true;
    }

    public boolean hasCompletionistTrimmedTasksDone() {
        for (AchievementList list : AchievementList.values()) {
            if (list == null)
                continue;

            if (list.getDifficulty() == AchievementDifficulty.HARD || list.getDifficulty() == AchievementDifficulty.ELITE) {
                if (!isComplete(list))
                    return false;
            }
        }
        return true;
    }

    public int getCurrentProgressAmount(AchievementList list) {
        return (achievements.get(list) == null ? 0 : achievements.get(list));
    }

    public boolean isComplete(AchievementList list) {
        return (achievements.get(list) != null && getCurrentProgressAmount(list) >= list.getAmountToComplete());
    }

    public void quickFinish() {
        for (AchievementList list : AchievementList.values()) {
            if (list == null)
                continue;

            updateProgress(list.getAmountToComplete(), list);
        }
    }

    public AchievementState getCurrentState(AchievementList list) {
        int current = getCurrentProgressAmount(list);
        if (current <= 0)
            return AchievementState.NOT_STARTED;
        else if (current < list.getAmountToComplete())
            return AchievementState.STARTED;
        else if (current >= list.getAmountToComplete() || isComplete(list))
            return AchievementState.COMPLETED;
        return AchievementState.NOT_STARTED;
    }

    public Map<AchievementList, Integer> getAchievements() {
        return achievements;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isUpdatedAchievements() {
        return updatedAchievements;
    }

    public void setUpdatedAchievements(boolean updatedAchievements) {
        this.updatedAchievements = updatedAchievements;
    }

}
