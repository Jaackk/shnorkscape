package com.rs.game.activites.gim.season;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.rs.cores.CoresManager;

import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.DisplayNames;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The task that runs the GIM season. Handles very important tasks consisting of
 * <ul>
 *   <li>Sending weekly updates to the #gim-news channel</li>
 *   <li>Determines when the season is done and announces winners</li>
 *   <li>Renews the season once a winner is picked</li>
 *   <li>Starts a new season if no previous season took place</li>
 * </ul>
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMSeasonTask implements Runnable {

    /**
     * GIM tips sent when this task runs an iteration.
     */
    private static final ImmutableList<String> TIPS = ImmutableList.of(
            "Use the Discord command ;;registergim to stay updated on all the latest GIM news!",
            "Examine a Group Ironman player to see what group they're in.",
            "The #1 team at the end of the season receives a custom reward that other players can't get!",
            "Don't die! You might get made fun of on #gim-news...",
            "Be sure to use ;;registergim on the #bots Discord channel for the Group Ironman role!",
            "Did you know? You can search group and player stats by right-clicking the GIM Leaderboard.",
            "Did you know? You can view recent changes to your shared bank by right clicking the GIM bank chest."
    );

    /**
     * GIM messages sent when a team is leading.
     */
    private static final ImmutableList<String> LEADING = ImmutableList.of(
            " has taken the lead!",
            " are in 1st place!",
            " are now #1 on the leaderboards!",
            " are dominating!",
            " are the #1 group!",
            " are making an awesome comeback!"
    );

    /**
     * The amount of highlights to display.
     */
    private static final int HIGHLIGHTS_COUNT = 12;

    /**
     * The last winning group.
     */
    private GIMGroup lastGroup;

    /**
     * Starts the task that runs the GIM season.
     */
    public void start() {
        if (GIM.BETA_MODE) {
            CoresManager.getServiceProvider().scheduleRepeatingTask(this, 5, 5, TimeUnit.MINUTES);
        } else {
            CoresManager.getServiceProvider().scheduleRepeatingTask(this, 1, 1, TimeUnit.HOURS);
        }
    }

    /**
     * The date time formatter.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    @Override
    public void run() {
        try {
            Stopwatch timer = Stopwatch.createStarted();
            sendLeaderboardUpdate();

            GIMSeasonDataSql seasonDataSql = new GIMSeasonDataSql();
            seasonDataSql.prepare();

            GIMSeasonData seasonData = GIM.getSeasonData();
            LocalDateTime seasonStart = seasonData.getSeasonStart();
            LocalDateTime seasonEnd = seasonData.getSeasonEnd();
            if (GIM.BETA_MODE) {
                checkBetaSeason(seasonStart, seasonEnd, seasonData);
            } else {
                checkLiveSeason(seasonStart.toLocalDate(), seasonEnd.toLocalDate(), seasonData);
            }
            long elapsedMs = timer.elapsed(TimeUnit.MILLISECONDS);
            Logger.getGlobal().warn("GIM season task took {}ms.", elapsedMs);
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Send any changes to the #1 group.
     */
    private void sendLeaderboardUpdate() {
        boolean sendTips = true;
        if (lastGroup == null) {
            lastGroup = GIM.getGroupForRank(1);
        } else {
            GIMGroup currentGroup = GIM.getGroupForRank(1);
            if (currentGroup != null && currentGroup.getGroupId() != lastGroup.getGroupId()) {
                GIM.sendWorldMsg("Group " + Colors.RED + currentGroup.getGroupName() + "</col> " + Colors.DEF_SEARCH_CYAN + Utils.randomFrom(LEADING));
                lastGroup = currentGroup;
                sendTips = false;
            }
        }
        if (sendTips) {
            CoresManager.getServiceProvider().addGameTask(() -> GIM.sendWorldMsg(Utils.randomFrom(TIPS)));
        }
    }

    /**
     * Checks the beta server season.
     */
    private void checkBetaSeason(LocalDateTime startTime, LocalDateTime endTime, GIMSeasonData seasonData) {
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isEqual(endTime) || currentTime.isAfter(endTime)) {
            GIM.getHighscores().refreshNow();

            GIMGroup winner = GIM.getGroupForRank(1);
            if (winner == null) { // Try again later.
                return;
            }
            LocalDateTime newEndDate = currentTime.plusHours(2);
            endSeason(startTime, currentTime, newEndDate, winner);

            LocalDateTime currentDate = LocalDateTime.now();
            GIMRenewSeasonSql renewSeasonSql = new GIMRenewSeasonSql(currentDate, newEndDate, winner);
            renewSeasonSql.prepare();
        } else {
            ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();
            int halfHour = Math.toIntExact((currentTime.toEpochSecond(zoneOffset) - startTime.toEpochSecond(zoneOffset)) / 1800);
            updateForInterval(halfHour, seasonData);
        }
    }

    /**
     * Checks the live server season.
     */
    private void checkLiveSeason(LocalDate startDate, LocalDate endDate, GIMSeasonData seasonData) {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isEqual(endDate) || currentDate.isAfter(endDate)) {
            GIM.getHighscores().refreshNow();

            GIMGroup winner = GIM.getGroupForRank(1);
            if (winner == null) { // Try again later.
                return;
            }
            LocalDate newEndDate = currentDate.plusMonths(3);
            endSeason(startDate, currentDate, newEndDate, winner);

            LocalTime currentTime = LocalTime.now();
            LocalDateTime startDateTime = LocalDateTime.of(currentDate, currentTime);
            LocalDateTime endDateTime = LocalDateTime.of(endDate, currentTime);
            GIMRenewSeasonSql renewSeasonSql = new GIMRenewSeasonSql(startDateTime, endDateTime, winner);
            renewSeasonSql.prepare();
        } else {
            int week = (int) ((currentDate.toEpochDay() - startDate.toEpochDay()) / 7);
            updateForInterval(week, seasonData);
        }
    }

    /**
     * Determines whether or not a news update should be sent.
     */
    private void updateForInterval(int interval, GIMSeasonData seasonData) {
        if (interval > 0 && interval < 12) {
            int intervalIndex = interval - 1;
            if (!seasonData.isStatsSent(intervalIndex)) {
                GIMFlagWeekSql flagWeekSql = new GIMFlagWeekSql(interval);
                flagWeekSql.prepare();
                sendNews(interval, GIM.getHighlights().take(HIGHLIGHTS_COUNT));
            }
        }
    }

    /**
     * Sends a news update to the #gim-news channel.
     */
    private void sendNews(int week, List<String> highlights) {
        GIMGroup firstPlace = GIM.getGroupForRank(1);
        List<String> messages = new ArrayList<>();
        if (firstPlace != null) {
            messages.add("");
        }
        if (highlights.size() > 0) {
            messages.add("");
            messages.add("**Highlights**");
            StringBuilder sb = new StringBuilder();
            for (String msg : highlights) {
                sb.append("- ").append(msg);
                if (msg.contains("** died")) {

                }
                messages.add(sb.toString());
                sb.setLength(0);
            }
        }
        if (messages.isEmpty()) {
            messages.add("");
            messages.add("Erm, that's surprising... Looks like there's no stats for this week.");
        }

    }

    /**
     * Ends the season and hands out awards.
     */
    private void endSeason(TemporalAccessor start, TemporalAccessor end, TemporalAccessor newEnd, GIMGroup winner) {

        StringBuilder membersBuilder = new StringBuilder();
        for (String member : winner.getMembers()) {
            membersBuilder.append(DisplayNames.getDisplayName(member)).append(", ");
        }
        membersBuilder.setLength(membersBuilder.length() - 2);


        GIM.getEventManager().stop();
        GIM.getRewards().register(winner);
        CoresManager.getServiceProvider().addGameTask(() -> {
            for (Player player : World.getPlayers()) {
                if (winner.getMembers().contains(player.getUsername())) {
                    GIM.sendPlayerMsg(player, "Congratulations, your group has won the current season! Claim your reward(s) at the GIM guide.");
                }
            }
            GIM.sendWorldMsg("The winner for the current season is " + winner.getGroupName() + ", congratulations! The new season starts right away.");
        });
    }

    /**
     * Deletes every message in the #gim-news channel.
     */

}
