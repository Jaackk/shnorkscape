package com.rs.game.activites.gim.season;

import com.google.common.collect.ImmutableList;
import com.rs.cores.CoresManager;
import com.rs.game.activites.gim.GIM;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Retrieves the seasonal data from the "season" table.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMSeasonDataSql extends SQLRunnable {


    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement pst = c.prepareStatement("SELECT * FROM gim_season;");
             ResultSet results = pst.executeQuery()) {
            if (!results.next()) {
                startSeason();
                return;
            }
            Timestamp seasonStart = results.getTimestamp("season_start");
            Timestamp seasonEnd = results.getTimestamp("season_end");
            List<Boolean> stats = new ArrayList<>();
            for (int week = 1; week <= 11; week++) {
                stats.add(results.getBoolean("week" + week));
            }
            GIM.setSeasonData(new GIMSeasonData(seasonStart, seasonEnd, stats));
        } catch (final Exception ex) {
            Logger.getGlobal().catching(ex);
        }
    }

    /**
     * Starts a season for the first time.
     */
    private void startSeason() {
        GIMRenewSeasonSql renewSeasonSql;
        GIMSeasonData seasonData;
        List<Boolean> defaultStats = IntStream.range(0, 11).mapToObj(value -> false).collect(Collectors.toList());
        if (GIM.BETA_MODE) {
            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime = startTime.plusHours(2);
            renewSeasonSql = new GIMRenewSeasonSql(startTime, endTime, null);
            seasonData = new GIMSeasonData(startTime, endTime, defaultStats);
        } else {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusMonths(3);
            LocalTime nowTime = LocalTime.now();
            LocalDateTime startDateTime = LocalDateTime.of(startDate, nowTime);
            LocalDateTime endDateTime = LocalDateTime.of(endDate, nowTime);
            renewSeasonSql = new GIMRenewSeasonSql(startDateTime, endDateTime, null);
            seasonData = new GIMSeasonData(startDateTime, endDateTime, defaultStats);
        }
        renewSeasonSql.setClearData(false);
        renewSeasonSql.prepare();
        GIM.setSeasonData(seasonData);
    }
}
