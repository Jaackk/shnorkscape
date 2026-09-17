package com.rs.game.player.content.polls;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ClearPollSql extends SQLRunnable {

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement clearPoll = c.prepareStatement("DELETE FROM poll_current;")) {
            clearPoll.executeUpdate();
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
