package com.rs.game.activites.gim.season;

import com.google.common.collect.ImmutableList;
import com.rs.game.activites.gim.GIM;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A class representing GIM data in the "season" table.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMSeasonData {

    /**
     * The date time formatter.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, uuuu");

    /**
     * The season start date.
     */
    private final LocalDateTime seasonStart;

    /**
     * The season end date.
     */
    private final LocalDateTime seasonEnd;

    /**
     * If stats were sent for time chunk {@code x}.
     */
    private final ImmutableList<Boolean> stats;

    /**
     * Creates a new {@link GIMSeasonData}.
     */
    public GIMSeasonData(Timestamp seasonStart, Timestamp seasonEnd, List<Boolean> stats) {
        this.seasonStart = seasonStart.toLocalDateTime();
        this.seasonEnd = seasonEnd.toLocalDateTime();
        this.stats = ImmutableList.copyOf(stats);
    }

    /**
     * Creates a new {@link GIMSeasonData}.
     */
    public GIMSeasonData(LocalDateTime seasonStart, LocalDateTime seasonEnd, List<Boolean> stats) {
        this.seasonStart = seasonStart;
        this.seasonEnd = seasonEnd;
        this.stats = ImmutableList.copyOf(stats);
    }

    public String getSeasonStartFormatted() {
        return seasonStart.format(FORMATTER);
    }

    public String getSeasonEndFormatted() {
        return seasonEnd.format(FORMATTER);
    }

    public LocalDateTime getSeasonStart() {
        return seasonStart;
    }

    public LocalDateTime getSeasonEnd() {
        return seasonEnd;
    }

    /**
     * If stats were sent for {@code timeChunk}.
     */
    public boolean isStatsSent(int timeChunkIndex) {
        return stats.get(timeChunkIndex);
    }
}