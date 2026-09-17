package com.rs.game.player.content.polls.archive;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ClearArchiveSql extends SQLRunnable {

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement deleteInfo = c.prepareStatement("DELETE FROM poll_history;");
             PreparedStatement deleteQuestions = c.prepareStatement("DELETE FROM poll_history_questions;");
             PreparedStatement deleteAnswers = c.prepareStatement("DELETE FROM poll_history_answers;")) {
            c.setAutoCommit(false);
            try {
                deleteInfo.executeUpdate();
                deleteQuestions.executeUpdate();
                deleteAnswers.executeUpdate();
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw new RuntimeException(e);
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}