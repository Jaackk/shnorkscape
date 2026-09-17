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

/**
 * Fetches all previous winners.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMFetchWinnersSql extends SQLRunnable {

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement previousWinners = c.prepareStatement("SELECT * FROM gim_previous_winners;")) {
            if (previousWinners.execute()) {
                ImmutableList<GIMSeasonWinner> winners = GIMSeasonWinner.buildSeasonWinners(previousWinners);
               GIM.setWinnerData(winners);
            }
        } catch (final Exception ex) {
            Logger.getGlobal().catching(ex);
        }
    }
}
