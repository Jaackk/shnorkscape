package com.rs.game.activites.gim.season;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

/**
 * Sets a flag that signifies that highlights were sent for a specific week.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMFlagWeekSql extends SQLRunnable {

    /**
     * The week to flag.
     */
    private final int week;

    /**
     * Creates a new {@link GIMFlagWeekSql}.
     */
    public GIMFlagWeekSql(int week) {
        this.week = week;
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement pst = c.prepareStatement("UPDATE gim_season " +
                     "SET week" + week + " = 1;")) {
            pst.executeUpdate();
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
