package com.rs.game.activities.seasonalevents.christmas;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Multiset;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.DisplayNames;
import lombok.Data;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lare96
 */
public final class PresentsHighscoresBuilder {

    private static final int REFRESH_MINS = 10;

    @Data
    private static final class PresentEntry implements Comparable<PresentEntry> {
        private final String username;
        private final int amount;

        @Override
        public int compareTo(@NotNull PresentEntry o) {
            return Integer.compare(o.amount, amount);
        }
    }

    private static final List<PresentEntry> nicePresentsList = new ArrayList<>();
    private static int totalNiceCollected;
    private static final Stopwatch niceLastUpdated = Stopwatch.createUnstarted();
    private static final List<PresentEntry> naughtyPresentsList = new ArrayList<>();
    private static int totalNaughtyCollected;
    private static final Stopwatch naughtyLastUpdated = Stopwatch.createUnstarted();

    public static void showNice(Player player) {
        val returnedPresents = ChristmasSeasonalEvent.returnedPresents;
        int totalNice = buildList(niceLastUpdated, nicePresentsList, returnedPresents);
        if (totalNice != -1) {
            totalNiceCollected = totalNice;
        }
        DataInterface inter = new DataInterface("Santa's nice list");
        if (!nicePresentsList.isEmpty()) {
            int presentsAmount = returnedPresents.count(player.getUsername());
            inter.add(getRefreshTime(naughtyLastUpdated));
            inter.blankLine();
            inter.add(Colors.DEF_SEARCH_CYAN + "Presents YOU returned: " + Colors.DARK_RED + presentsAmount + "</col>");
            inter.add(Colors.DEF_SEARCH_CYAN + "Presents EVERYONE returned: " + Colors.DARK_RED + totalNiceCollected + "</col>");
            displayList(inter, nicePresentsList);
        } else {
            inter.blankLine();
            inter.add(Colors.DEF_SEARCH_CYAN + "No presents have been returned yet.");
        }
        inter.show(player);
    }


    public static void showNaughty(Player player) {
        val stolenPresents = ChristmasSeasonalEvent.stolenPresents;
        int totalNaughty = buildList(naughtyLastUpdated, naughtyPresentsList, stolenPresents);
        if (totalNaughty != -1) {
            totalNaughtyCollected = totalNaughty;
        }
        DataInterface inter = new DataInterface("Santa's naughty list");
        if (!naughtyPresentsList.isEmpty()) {
            int presentsAmount = stolenPresents.count(player.getUsername());
            inter.add(getRefreshTime(naughtyLastUpdated));
            inter.blankLine();
            inter.add(Colors.DEF_SEARCH_CYAN + "Presents YOU stole: " + Colors.DARK_RED + presentsAmount + "</col>");
            inter.add(Colors.DEF_SEARCH_CYAN + "Presents EVERYONE stole: " + Colors.DARK_RED + totalNaughtyCollected + "</col>");
            displayList(inter, naughtyPresentsList);
        } else {
            inter.blankLine();
            inter.add(Colors.DEF_SEARCH_CYAN + "No presents have been stolen yet.");
        }
        inter.show(player);
    }

    private static int buildList(Stopwatch sw, List<PresentEntry> list, Multiset<String> multiset) {
        if (!sw.isRunning() || sw.elapsed().toMinutes() > REFRESH_MINS) {
            // Lazily rebuild highscores every 10 mins or so.
            list.clear();
            int total = 0;
            for (val next : multiset.entrySet()) {
                list.add(new PresentEntry(next.getElement(), next.getCount()));
                total += next.getCount();
            }
            Collections.sort(list);
            sw.reset().start();
            return total;
        }
        return -1;
    }

    private static void displayList(DataInterface inter, List<PresentEntry> presentList) {
        int rank = 1;
        inter.blankLine();
        inter.add("~ Leaderboard ~");
        for (val next : presentList) {
            String displayName = DisplayNames.getDisplayName(next.getUsername());
            inter.add(rank + ". " + displayName + " ~ " + next.getAmount());
            rank++;
        }
    }

    private static String getRefreshTime(Stopwatch sw) {
        long millis = sw.elapsed().toMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        return "Last refresh | " + String.format(minutes == 0 ? "Just now!" : "%d min ago", minutes);
    }
}