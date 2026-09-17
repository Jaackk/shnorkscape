package com.rs.game.activites.gim.highscores;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Will update the score database with a player's progress.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMUpdateScoreSql extends SQLRunnable {

    /**
     * The SQL statement that updates the score.
     */
    static String getUpdateStatement() {
        return "UPDATE gim_group_score " +
                "SET total_xp = total_xp + ?, total_deaths = total_deaths + ?, total_boss_points = total_boss_points + ?, total_levels = total_levels + ?, prestiges = prestiges + ? " +
                "WHERE member_name = ?;";
    }

    /**
     * The player's username.
     */
    private final String username;

    /**
     * The levels gained.
     */
    private final int levelsGained;

    /**
     * The XP gained.
     */
    private final long xpGained;

    /**
     * The deaths gained.
     */
    private final int deaths;

    /**
     * The boss points gained.
     */
    private final int bpGained;

    /**
     * The prestiges gained.
     */
    private final int prestigesGained;

    /**
     * Creates a new {@link GIMUpdateScoreSql}.
     */
    public GIMUpdateScoreSql(String username, long xpGained, int deaths, int bpGained, int levelsGained, int prestigesGained) {
        this.username = username;
        this.xpGained = xpGained;
        this.deaths = deaths;
        this.bpGained = bpGained;
        this.levelsGained = levelsGained;
        this.prestigesGained = prestigesGained;
    }

    @Override
    public void execute(DatabaseCredential auth) {
        if (xpGained == 0 && deaths == 0 && bpGained == 0 && levelsGained == 0 && prestigesGained == 0) {
            return;
        }
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement pst = c.prepareStatement(getUpdateStatement())) {
            pst.setLong(1, xpGained);
            pst.setInt(2, deaths);
            pst.setInt(3, bpGained);
            pst.setInt(4, levelsGained);
            pst.setInt(5, prestigesGained);
            pst.setString(6, username);
            pst.executeUpdate();
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
