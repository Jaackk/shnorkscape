package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.VoteHiscores;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Used to handle Vote rewards.
 *
 * @author Noel
 */
public class VoteManager {

    /**
     * Starts a voting party at this amount of votes.
     */
    private static final int START_PARTY = Settings.TEST_SERVER_MODE ? 2 : 100;

    private static final Item COINS = new Item(995, 2000000);
    /**
     * An Integer containing all server votes.
     */
    public static int VOTES;
    /**
     * A Byte representing how much parties have been thrown.
     */
    public static byte PARTIES;

    /**
     * Handles the Vote Reward.
     *
     * @param player The player being rewarded.
     */
    public static void handleQueuedReward(final Player player) {
        VOTES++;
        if (player.getSitesSuccessfullyVotedOn() < 0 || player.getSitesSuccessfullyVotedOn() > 3) {
            player.setSitesSuccessfullyVotedOn(0);
        }
        player.incrementSitesSuccessfullyVotedOn();
        player.setVotes(player.getVotes() + 1);
        player.addItem(COINS);
        // handle all roll / extra rewards
        doBonusRolls(player);

        player.setVotePoints(player.getVotePoints() + 2);
        player.setTotalVotes(player.getTotalVotes() + 1);
        player.lastVote = LocalDateTime.now();
        World.setLastVoter(player.getDisplayName());
        VoteHiscores.checkRank(player);
        player.getAchievements().updateProgress(1, AchievementList.REACH_20_TOTAL_VOTES, AchievementList.REACH_50_TOTAL_VOTES, AchievementList.REACH_100_TOTAL_VOTES);

        if (VOTES % 10 == 0 && VOTES > 0) {
            World.sendWorldMessage("<img=6><col=008FB2>Total of [" + Colors.RED + Utils.getFormattedNumber(VOTES) + "<col=008FB2>] votes have been claimed! " + "Vote now using the ;;vote command!", false);

            /** Custom Vote Party every 100 claimed votes **/
            if (VOTES % START_PARTY == 0) {
                voteParty();
            }
        }
    }

    public static final void startRewardProcess(final Player player, final int amount) {
        for (int index = 0; index < amount; index++) {
            handleQueuedReward(player);
        }
        if (player.getSitesSuccessfullyVotedOn() >= 2) {
            player.setVoteDoubleXpTimeRemaining(System.currentTimeMillis() + (60_000 * 60));
            if (!player.isGroupIronman() && !player.isKingOfTheSkillGameMode()) {
                player.donorObjectBypassTime = System.currentTimeMillis() + (60_000 * 60);
            }
            player.sendMessage(Colors.GREEN + "You've received 1 hour of double xp" + ((!player.isGroupIronman() && !player.isKingOfTheSkillGameMode()) ? " and 1 hour of access to Donator objects at home" : "") + "!");
        }
        player.getDialogueManager().startDialogue("SimpleItemMessage", 20214, 1, "You have received " + Colors.wrap(Colors.DARK_GREEN, "<shad=000000>" + (amount * 2) + "</shad>") + " vote points!");
        player.setSitesSuccessfullyVotedOn(0);
    }

    public static final void handleEmptyReward(final Player player) {
        player.getDialogueManager().startDialogue("SimpleItemMessage", 20243, 1, "You do not have any pending votes waiting for you!");
    }

    public static final void voteParty() {
        PARTIES++;
        World.edelarParty();
        /** Partying for 4 seconds **/

        List<Player> recentVoters = new ArrayList<>(World.getPlayersOnline());
        for (Player player : World.getPlayers()) {
            if (player.hasVotedInLast24H()) {
                recentVoters.add(player);
            } else {
                player.sendMessage("<img=6><col=008FB2>[Vote Party] Unfortunately, you did not receive anything because you haven't voted today.");
            }
        }

        final int players = recentVoters.size();
        recentVoters.forEach(p -> {
            if (p != null && !p.isKingOfTheSkillGameMode()) {
                final Item voteBook = new Item(11640, PARTIES);
                p.addItem(voteBook);
                if (World.isWeekend()) {
                    p.sWeekendBooks(PARTIES);
                }
                p.sendMessage("<img=6><col=008FB2>[Vote Party] " + (players == 1 ? "You" : "You and " + (players - 1) + " others") + " " + "have received " + (PARTIES == 1 ? "an extra vote book" : PARTIES + " extra vote books") + "!", false);
            }
        });
    }

    private static void doBonusRolls(final Player player) {
        if (Utils.random(100) > 60) {
            player.addItem(new Item(6199));
        }
        if (Utils.random(100) > 99) {
            player.addItem(new Item(34027));
            World.sendWorldMessage("<img=6><col=008FB2>" + player.getDisplayName() + " has received a Rare item token from ;;vote!", false);
        }
        if (Utils.random(100) > 95) {
            player.addItem(new Item(24155, 10));
            World.sendWorldMessage("<img=6><col=008FB2>" + player.getDisplayName() + " has received 20 reward keys from ;;vote!", false);
        }
        if (Utils.random(100) > 97) {
            player.addItem(new Item(25202, 1));
            World.sendWorldMessage("<img=6><col=008FB2>" + player.getDisplayName() + " has received a Deathtouched Dart from ;;vote!", false);
        }

        if (!player.wonVoteDonator && player.getVotes() >= 8 && Utils.getHoursPlayed(player.getCreationDate()) < 48) {
            player.wonVoteDonator = true;
            player.setDonatorTimeTill(Utils.currentTimeMillis() + TimeUnit.DAYS.toMillis(7));
            World.sendWorldMessage("<img=6><col=008FB2>" + player.getDisplayName() + " has won temporary donator status for 7 days from ;;vote!", false);
        }
    }

}