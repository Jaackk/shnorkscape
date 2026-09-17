package com.rs.game.player.content.polls;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author lare96 <http://github.com/lare96>
 */
@AllArgsConstructor
public final class SavePollSql extends SQLRunnable {

    private final PollQuestionsDatabase poll;
    private final int voteCount;

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement clearPoll = c.prepareStatement("DELETE FROM poll_current;");
             PreparedStatement verifyEmpty = c.prepareStatement("SELECT COUNT(*) FROM poll_current;");
             PreparedStatement addPoll = c.prepareStatement("INSERT INTO `poll_current`(`id`, `title`, `description`, `end_date`, `vote_count`) VALUES (?, ?, ?, ?, ?);")) {

            StringBuilder description = new StringBuilder();
            for (String str : poll.getExplanation()) {
                description.append(str).append('\n');
            }
            description.setLength(description.length() - 1);

            addPoll.setInt(1, 0);
            addPoll.setString(2, poll.getTitle());
            addPoll.setString(3, description.toString());
            addPoll.setDate(4, Date.valueOf(poll.getEndDate()));
            addPoll.setInt(5, voteCount);

            c.setAutoCommit(false);
            try {
                clearPoll.executeUpdate();
                try (ResultSet resultSet = verifyEmpty.executeQuery()) {
                    if (!resultSet.next() || resultSet.getInt(1) > 0) {
                        c.rollback();
                        throw new IllegalStateException("Old poll was not cleared from SQL table.");
                    }
                }

                if (addPoll.executeUpdate() < 1) {
                    c.rollback();
                    throw new IllegalStateException("New poll was not added to SQL table.");
                }
                c.commit();
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
