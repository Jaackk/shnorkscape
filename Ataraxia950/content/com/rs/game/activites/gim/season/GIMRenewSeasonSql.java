package com.rs.game.activites.gim.season;

import com.google.common.collect.ImmutableList;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Renews or starts a new season.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMRenewSeasonSql extends SQLRunnable {

    /**
     * The new season start date.
     */
    private final Timestamp startTimestamp;

    /**
     * The new season end date.
     */
    private final Timestamp endTimestamp;

    /**
     * The winning group. Will be {@code null} if {@link #clearData} is {@code false}.
     */
    private final GIMGroup winner;

    /**
     * If score data should be cleared. This needs to be {@code true} if the season is being renewed.
     */
    private boolean clearData = true;

    /**
     * Creates a new {@link GIMRenewSeasonSql}.
     */
    public GIMRenewSeasonSql(LocalDateTime startDateTime, LocalDateTime endDateTime, GIMGroup winner) {
        this.winner = winner;
        startTimestamp = Timestamp.valueOf(startDateTime);
        endTimestamp = Timestamp.valueOf(endDateTime);
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia")) {
            if (clearData) {
                c.setAutoCommit(false);
                try (PreparedStatement updateSeason = c.prepareStatement("UPDATE gim_season " + // Update season start and end.
                        "SET season_start = ?, season_end = ?, week1 = 0, week2 = 0, week3 = 0, week4 = 0, week5 = 0, " +
                        "week6 = 0, week7 = 0, week8 = 0, week9 = 0, week10 = 0, week11 = 0;");
                     PreparedStatement resetGroupScore = c.prepareStatement("UPDATE gim_group_data " + // Reset group score.
                             "SET group_score = 0;");
                     PreparedStatement resetGroupTracking = c.prepareStatement("UPDATE gim_group_score " + // Reset group tracking.
                             "SET total_xp = 0,total_deaths = 0,total_boss_points = 0,total_levels = 0,prestiges = 0;");
                     PreparedStatement recordWinners = c.prepareStatement("INSERT INTO gim_previous_winners (group_id,win_start,win_end,group_name,group_score) " + // Record winners.
                             "VALUES (?,?,?,?,?);");
                     PreparedStatement selectWinners = c.prepareStatement("SELECT * FROM gim_previous_winners;")) {
                    updateSeason.setTimestamp(1, startTimestamp);
                    updateSeason.setTimestamp(2, endTimestamp);
                    if (updateSeason.executeUpdate() < 1) {
                        rollback(c);
                    }
                    if (resetGroupScore.executeUpdate() < 1) {
                        rollback(c);
                    }
                    if (resetGroupTracking.executeUpdate() < 1) {
                        rollback(c);
                    }
                    recordWinners.setInt(1, winner.getGroupId());
                    recordWinners.setTimestamp(2, startTimestamp);
                    recordWinners.setTimestamp(3, endTimestamp);
                    recordWinners.setString(4, winner.getGroupName());
                    recordWinners.setLong(5, winner.getScore());
                    if (recordWinners.executeUpdate() < 1) {
                        rollback(c);
                    }
                    if (!selectWinners.execute()) {
                        rollback(c);
                    }

                    ImmutableList<GIMSeasonWinner> previousWinners = GIMSeasonWinner.buildSeasonWinners(selectWinners);
                    GIM.setWinnerData(previousWinners);
                    c.commit();
                } finally {
                    c.setAutoCommit(true);
                }
            } else {
                try (PreparedStatement pst = c.prepareStatement("INSERT INTO gim_season (season_start, season_end) VALUES (?,?);")) { // Insert season start and end.
                    pst.setTimestamp(1, startTimestamp);
                    pst.setTimestamp(2, endTimestamp);
                    pst.executeUpdate();
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Stop the SQL transaction and throw an exception.
     */
    private void rollback(Connection c) throws SQLException, IllegalStateException {
        c.rollback();
        throw new IllegalStateException("Could not complete GIM season renewal. Needs to be looked at immediately!");
    }

    public void setClearData(boolean clearData) {
        this.clearData = clearData;
    }
}
