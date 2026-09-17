package com.rs.game.player.content;

import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.DisplayNames;
import com.rs.utils.HighscoresBuilder;
import lombok.Data;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author lare96
 */
public class TriviaBotHighscoresBuilder extends HighscoresBuilder<TriviaBotHighscoresBuilder.TriviaWinner> {

    @Data
    public static final class TriviaWinner implements Comparable<TriviaWinner> {
        private final String username;
        private final int wins;

        @Override
        public int compareTo(@NotNull TriviaBotHighscoresBuilder.TriviaWinner o) {
            return Integer.compare(o.wins, wins);
        }
    }

    public TriviaBotHighscoresBuilder() {
        super("Top 25 Trivia Winners", 5);
    }

    @Override
    public String buildEntry(int rank, TriviaWinner entry) {
        String displayName = DisplayNames.getDisplayName(entry.getUsername());
        return rank + ". " + displayName + " ~ " + Colors.DARK_RED + entry.getWins() + "</col>";
    }

    @Override
    public void build(Player player, DataInterface inter, boolean empty) {
        if (empty) {
            inter.add("No trivia questions have been answered yet.");
        } else {
            buildHighscores(player, inter);
        }
    }

    @Override
    public void cache(List<TriviaWinner> entries) {
        for(val next : TriviaBot.getWinners().entrySet()) {
            entries.add(new TriviaWinner(next.getElement(), next.getCount()));
        }
    }
}
