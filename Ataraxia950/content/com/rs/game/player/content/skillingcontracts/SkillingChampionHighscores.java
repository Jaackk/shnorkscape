package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillingChampionHighscores {

    private static final class SkillingChampionEntry implements Comparable<SkillingChampionEntry> {
        final String name;
        final int score;

        private SkillingChampionEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public int compareTo(@NotNull SkillingChampionHighscores.SkillingChampionEntry o) {
            return Integer.compare(o.score, score);
        }
    }

    private final List<SkillingChampionEntry> entries = new ArrayList<>();
    private final List<SkillingChampionEntry> immutableEntries = Collections.unmodifiableList(entries);
    boolean rebuildList = true;
    private LocalDateTime lastUpdated;


    public void showInterface(Player player) {
        getImmutableEntries(); // Ensure entries are cached before displaying leaderboard.
        val data = new DataInterface("Skilling Champion ~ Daily Leaderboard");
        int place = 1;
        data.blankLine();
        LocalDateTime now = LocalDateTime.now();
        Long lastUpdatedHours = lastUpdated != null ? lastUpdated.until(now, ChronoUnit.HOURS) : null;
        String lastUpdatedStr;
        if(lastUpdatedHours == null) {
            lastUpdatedStr = "Never";
        } else if(lastUpdatedHours == 0) {
            lastUpdatedStr = "Just now";
        } else if(lastUpdatedHours == 1) {
            lastUpdatedStr = "1 hour ago";
        } else {
            lastUpdatedStr = lastUpdatedHours + " hours ago";
        }

        data.add(Colors.DARK_RED + "Last updated ~ " + lastUpdatedStr);
        data.blankLine();
        for (SkillingChampionEntry entry : getImmutableEntries()) {
            // Only show first 25.
            if (place > 25) {
                break;
            }
            data.add(place++ + ". " + entry.name + " ~ Score: " + entry.score);
        }
        if (getImmutableEntries().size() == 0) {
            data.add("No one has completed skilling contracts today.");
        }
        data.show(player);
    }

    private void buildEntries() {
        entries.clear();
        for (val entry : SkillingContractTracker.getSingleton().completedTasks.entrySet()) {
            val newEntry = new SkillingChampionEntry(entry.getElement().displayName, entry.getCount());
            entries.add(newEntry);
        }
        Collections.sort(entries);
        lastUpdated = LocalDateTime.now();
    }

    public List<SkillingChampionEntry> getImmutableEntries() {
        if (rebuildList) {
            buildEntries();
            rebuildList = false;
        }
        return immutableEntries;
    }

    private static final SkillingChampionHighscores instance = new SkillingChampionHighscores();

    public static SkillingChampionHighscores getInstance() {
        return instance;
    }
}
