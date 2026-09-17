package com.rs.game.activites.gim.highscores;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.rs.cores.CoresManager;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activites.gim.GIMGroupMember;
import com.rs.game.activites.gim.highscores.GIMHighscoresRefreshSql.GIMHighscoresRefreshTask;
import com.rs.game.activites.gim.season.GIMFetchMember;
import com.rs.game.activites.gim.season.GIMSeasonData;
import com.rs.game.activites.gim.season.GIMSeasonWinner;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.DisplayNames;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Handles leaderboards. Searching, viewing, displaying, etc.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMHighscores {

    /**
     * Will refresh the highscore rankings every {@code n} minutes.
     */
    static final int REFRESH_MINUTES = 5;

    /**
     * Only {@code n} ranks will be displayed on the regular highscores.
     */
    private static final int DISPLAY_LIMIT = 25;

    /**
     * The date time formatter.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, uuuu");

    /**
     * The season date time formatter.
     */
    private static final DateTimeFormatter SEASON_FORMATTER = DateTimeFormatter.ofPattern("MMM d, uuuu");

    /**
     * The last refresh.
     */
    private final Stopwatch lastRefresh = Stopwatch.createUnstarted();

    /**
     * The highscores refresh task.
     */
    private final GIMHighscoresRefreshTask refreshTask = new GIMHighscoresRefreshTask();

    /**
     * Forces the server to update the highscores on the next game tick. Use {@link #refreshNow()} if you need an
     * immediate synchronous refresh.
     */
    public void requestRefresh() {
       refreshTask.forceUpdate();
    }

    /**
     * Performs an immediate synchronous refresh. Pauses the refresh task.
     */
    public void refreshNow() {
        refreshTask.pause();
        GIMHighscoresRefreshSql highscoresRefreshSql = new GIMHighscoresRefreshSql();
        highscoresRefreshSql.prepare();
        refreshTask.unpause();
    }

    /**
     * Starts the automatic highscores refreshing.
     */
    public void start() {
        refreshTask.start();
        WorldTasksManager.schedule(new GIMUpdateScoreTask(), 500, 500);
    }

    /**
     * Displays the results of a group search.
     */
    public void displayGroup(Player player, String groupKey) {
        GIMGroup group = GIM.getGroupData().get(groupKey);
        if (group == null) {
            Dialogue.sendSingleDialogue(player, "No group named '" + groupKey + "' was found!");
            return;
        }

        String groupName = group.getGroupName();
        String leaderName = group.getLeaderName();
        int rank = group.getRank();

        DataInterface inter = new DataInterface(groupName);
        inter.add(Colors.DEF_SEARCH_CYAN + "~ <img=33> Group Information <img=33> ~");
        inter.blankLine();
        inter.add("Date created: " + group.getCreationDate().format(FORMATTER));
        inter.add("Name: " + groupName);
        inter.add("Leader: " + DisplayNames.getDisplayName(leaderName));
        inter.blankLine();
        inter.blankLine();
        inter.add(Colors.DEF_SEARCH_CYAN + "~ <img=33> Group Score <img=33> ~");
        inter.blankLine();
        inter.add(Colors.DARK_RED + "Rank # " + (rank == -1 ? "Unranked" : rank));
        inter.add("Score: " + Utils.formatNumber(group.getScore()));
        inter.add("Total XP: " + Utils.formatNumber(group.getTotalXp()));
        inter.add("Total boss points: " + Utils.formatNumber(group.getTotalBp()));
        inter.add("Total levels gained: " + Utils.formatNumber(group.getTotalLevels()));
        inter.add("Total deaths: " + group.getTotalDeaths());
        inter.add("Total prestiges: "+ group.getTotalPrestiges());
        inter.blankLine();
        inter.blankLine();
        inter.add(Colors.DEF_SEARCH_CYAN + "~ <img=33> Members <img=33> ~");
        inter.blankLine();
        for (String member : group.getMembers()) {
            String prefix = "";
            if (leaderName.equals(member)) {
                prefix = Colors.DARK_RED;
            }
            inter.add(prefix + DisplayNames.getDisplayName(member));
        }
        inter.show(player);
    }

    /**
     * Displays the top-25 leaderboard.
     */
    public void displayLeaderboard(Player player) {
        GIMSeasonData seasonData = GIM.getSeasonData();
        LocalDateTime seasonStart = seasonData.getSeasonStart();
        LocalDateTime seasonEnd = seasonData.getSeasonEnd();
        ImmutableList<GIMGroup> rankings = GIM.getRankings();
        DataInterface inter = new DataInterface("Top " + DISPLAY_LIMIT + " Teams");
        long millis = lastRefresh.elapsed().toMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        inter.add(Colors.DEF_SEARCH_CYAN + "Last refresh | " + String.format(minutes == 0 ? "Just now!" : "%d min ago", minutes));
        inter.blankLine();
        inter.blankLine();
        inter.add(Colors.DEF_SEARCH_CYAN + "~  <img=33> Season  " + SEASON_FORMATTER.format(seasonStart) + " " + Colors.SHAD + "to</shad> " + Colors.DEF_SEARCH_CYAN + SEASON_FORMATTER.format(seasonEnd) + " <img=33> ~");
        if (rankings != null && rankings.size() > 0) {
            String color = Colors.DARK_RED;
            int loops = 0;
            for (GIMGroup group : rankings) {
                if(loops >= DISPLAY_LIMIT) {
                    break;
                }
                inter.add(color + group.getRank() + ". " + group.getGroupName() + " ~ Score: " + Utils.formatNumber(group.getScore()));
                color = "";
                loops++;
            }
        } else {
            inter.add("There are currently no teams to display.");
        }
        inter.show(player);
    }

    /**
     * Displays the previous winners.
     */
    public void displayPreviousWinners(Player player) {
        DataInterface inter = new DataInterface("Season Winners");
        ImmutableList<GIMSeasonWinner> winners = GIM.getWinnerData();
        if (winners.size() > 0) {
            for (GIMSeasonWinner sw : winners) {
                inter.add(Colors.DEF_SEARCH_CYAN + "~  <img=33> Season  " + SEASON_FORMATTER.format(sw.getWinStartDate()) + " " + Colors.SHAD + "to</shad> " + Colors.DEF_SEARCH_CYAN + SEASON_FORMATTER.format(sw.getWinEndDate()) + " <img=33> ~");
                inter.add(sw.getGroupName() + " ~ Score: " + sw.getGroupScore());
                inter.blankLine();
            }
        } else {
            inter.add(Colors.DEF_SEARCH_CYAN + "There are no previous winners found.");
        }
        inter.show(player);
    }

    /**
     * Displays a player.
     */
    public void displayPlayer(Player player, String query, GIMGroupMember groupMember) {
        DataInterface inter = new DataInterface("Player stats ~ " + query);
        if (groupMember != null) {
            GIMGroup group = GIM.getGroupForMember(groupMember.getUsername());
            if (group != null) {
                inter.add("Group name: " + group.getGroupName());
                inter.add("XP Gained: " + Utils.formatNumber(groupMember.getTotalXp()));
                inter.add("Deaths: " + groupMember.getTotalDeaths());
                inter.add("Boss Points Gained: " + Utils.formatNumber(groupMember.getTotalBp()));
                inter.add("Levels Gained: " + Utils.formatNumber(groupMember.getTotalLevels()));
                inter.add("Prestiges Gained: " + Utils.formatNumber(groupMember.getTotalPrestiges()));
            } else {
                throw new IllegalStateException("Group member data not yet built!");
            }
        } else {
            inter.add(Colors.DEF_SEARCH_CYAN + "No GIM player with display name '" + query + "' was found.");
        }
        inter.show(player);
    }

    /**
     * Opens an interface that allows for searching for a group.
     */
    public void searchGroup(Player player) {
        if (player.groupSearchThrottle.isRunning() && player.groupSearchThrottle.elapsed(TimeUnit.MILLISECONDS) < 1000) {
            Dialogue.sendSingleDialogue(player, "Please wait a second before searching again.");
            return;
        }
        player.sendInputString("Enter the group's name", new InputStringEvent() {
            @Override
            public void run(Player player) {
                displayGroup(player, getString().toLowerCase().trim());
                player.groupSearchThrottle.reset().start();
            }
        });
    }

    /**
     * Opens an interface that allows for searching for a player.
     */
    public void searchPlayer(Player player) {
        if (player.groupSearchThrottle.isRunning() && player.groupSearchThrottle.elapsed(TimeUnit.MILLISECONDS) < 1000) {
            Dialogue.sendSingleDialogue(player, "Please wait a second before searching again.");
            return;
        }
        player.sendInputString("Enter the player's name", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String query = getString().toLowerCase().trim();
                player.lock();
                Dialogue.sendNPCDialogueNoContinue(player, 12320, Dialogue.NORMAL, "Looking up stats for player '" + query + "' ...");
                String username = DisplayNames.getUsername(query);
                Future<GIMGroupMember> memberResult = CoresManager.getServiceProvider().submitNow(new GIMFetchMember(username));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if (memberResult.isDone()) {
                            Dialogue.closeNoContinueDialogue(player);
                            player.unlock();
                            stop();
                            GIMGroupMember member;
                            try {
                                member = memberResult.get();
                            } catch (Exception e) {
                                Dialogue.sendNPCDialogue(player, 12320, Dialogue.NORMAL, "An error occurred while fetching stats for '" + query + "', please report to an admin.");
                                Logger.getGlobal().catching(e);
                                return;
                            }
                            displayPlayer(player, query, member);
                            player.groupSearchThrottle.reset().start();
                        }
                    }
                }, 2, 1);
            }
        });
    }

    /**
     * Resets the refresh timer.
     */
    public void resetLastRefresh() {
        lastRefresh.reset().start();
    }
}