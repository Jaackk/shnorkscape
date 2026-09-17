package com.rs.utils;

import com.google.common.base.Stopwatch;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lare96
 */
public abstract class HighscoresBuilder<T extends Comparable<T>> {

    private final List<T> entries = new ArrayList<>();
    private final String title;
    private final int refreshMins;
    private final Stopwatch lastUpdated = Stopwatch.createUnstarted();

    public HighscoresBuilder(String title, int refreshMins) {
        this.title = title;
        this.refreshMins = refreshMins;
    }

    public abstract String buildEntry(int rank, T entry);

    public abstract void cache(List<T> entries);

    protected void build(Player player, DataInterface inter, boolean empty) {
        buildHighscores(player, inter);
    }

    protected final void buildHighscores(Player player, DataInterface inter) {
        int rank = 1;
        inter.blankLine();
        inter.add("~ Leaderboard ~");
        for (val next : entries) {
            inter.add(buildEntry(rank, next));
            rank++;
        }
    }

    public final void open(Player player) {
        if (!lastUpdated.isRunning() || lastUpdated.elapsed().toMinutes() > refreshMins) {
            // Lazily rebuild highscores.
            entries.clear();
            cache(entries);
            Collections.sort(entries);
            lastUpdated.reset().start();
        }
        DataInterface inter = new DataInterface(title);
        inter.add(getRefreshTime(lastUpdated));
        inter.blankLine();
        build(player, inter, entries.isEmpty());
        inter.show(player);
    }


    private String getRefreshTime(Stopwatch sw) {
        long millis = sw.elapsed().toMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        return "Last refresh | " + String.format(minutes == 0 ? "Just now!" : "%d min ago", minutes);
    }
}
