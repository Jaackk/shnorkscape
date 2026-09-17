package com.rs.game.activites.gim.season;

import com.google.common.collect.ImmutableList;
import com.mysql.cj.protocol.Resultset;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class representing a previous GIM season winner.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMSeasonWinner implements Comparable<GIMSeasonWinner> {

    /**
     * Builds a list of previous winners from an SQL statement.
     */
    public static ImmutableList<GIMSeasonWinner> buildSeasonWinners(PreparedStatement statement) throws SQLException {
        List<GIMSeasonWinner> winnersBuilder = new ArrayList<>();
        try (ResultSet results = statement.getResultSet()) {
            while (results.next()) {
                int groupId = results.getInt("group_id");
                LocalDate winStart = results.getDate("win_start").toLocalDate();
                LocalDate winEnd = results.getDate("win_end").toLocalDate();
                String groupName = results.getString("group_name");
                long groupScore = results.getLong("group_score");
                winnersBuilder.add(new GIMSeasonWinner(groupId, winStart, winEnd, groupName, groupScore));
            }
        }
        Collections.sort(winnersBuilder);
        return ImmutableList.copyOf(winnersBuilder);
    }

    /**
     * The winning group ID.
     */
    private final int groupId;

    /**
     * The season start date.
     */
    private final LocalDate winStartDate;

    /**
     * The season end date.
     */
    private final LocalDate winEndDate;

    /**
     * The winning group name.
     */
    private final String groupName;

    /**
     * The winning group score.
     */
    private final long groupScore;

    /**
     * Creates a new {@link GIMSeasonWinner}.
     */
    public GIMSeasonWinner(int groupId, LocalDate winStartDate, LocalDate winEndDate, String groupName, long groupScore) {
        this.groupId = groupId;
        this.winStartDate = winStartDate;
        this.winEndDate = winEndDate;
        this.groupName = groupName;
        this.groupScore = groupScore;
    }

    @Override
    public int compareTo(@NotNull GIMSeasonWinner o) {
        return o.winStartDate.compareTo(winStartDate);
    }

    public int getGroupId() {
        return groupId;
    }

    public LocalDate getWinStartDate() {
        return winStartDate;
    }

    public LocalDate getWinEndDate() {
        return winEndDate;
    }

    public String getGroupName() {
        return groupName;
    }

    public long getGroupScore() {
        return groupScore;
    }
}
