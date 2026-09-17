package com.rs.game.player.content.polls.archive;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public final class SaveArchiveSql extends SQLRunnable {

    private final ArchivedPoll poll;

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement insertInfo = c.prepareStatement("INSERT INTO `poll_history`(`end_date`, `title`, `description`, `total_voters`) " +
                     "VALUES(?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertQuestions = c.prepareStatement("INSERT INTO `poll_history_questions`(`poll_id`, `order`, `title`) " +
                     "VALUES(?, ?, ?);");
             PreparedStatement insertAnswers = c.prepareStatement("INSERT INTO `poll_history_answers`(`question_id`, `order`, `title`, `votes`, `is_highest_voted`) " +
                     "VALUES(?, ?, ?, ?, ?);");
             PreparedStatement selectQuestions = c.prepareStatement("SELECT `question_id`, `order` FROM `poll_history_questions` WHERE `poll_id` = ? ORDER BY `order` ASC;")) {
            c.setAutoCommit(false);
            try {

                // INSERT poll info.
                int index = 1;
                insertInfo.setDate(index++, Date.valueOf(poll.ended));
                insertInfo.setString(index++, poll.title);
                insertInfo.setString(index++, poll.getExplanationAsString());
                insertInfo.setInt(index, poll.totalVoters);
                if (insertInfo.executeUpdate() < 1) {
                    c.rollback();
                    throw new IllegalStateException("Poll info could not be added!");
                }
                int pollId = -1;
                try (ResultSet results = insertInfo.getGeneratedKeys()) {
                    if (results.next()) {
                        pollId = results.getInt(1);
                    }
                }
                if (pollId == -1) {
                    c.rollback();
                    throw new IllegalStateException("Poll ID could not be retrieved!");
                }

                // INSERT poll questions.
                int order = 1;
                for (ArchivedQuestion question : poll.questions) {
                    index = 1;
                    insertQuestions.setInt(index++, pollId);
                    insertQuestions.setInt(index++, order++);
                    insertQuestions.setString(index, question.question);
                    insertQuestions.addBatch();
                }
                if (insertQuestions.executeBatch().length < 1) {
                    c.rollback();
                    throw new IllegalStateException("No poll questions were added to batch.");
                }

                // SELECT question IDs (disgusting JDBC limitation).
                List<Integer> questionIdList = new ArrayList<>();
                selectQuestions.setInt(1, pollId);
                try (ResultSet results = selectQuestions.executeQuery()) {
                    while (results.next()) {
                        questionIdList.add(results.getInt(1));
                    }
                }

                // INSERT poll answers.
                int questionIndex = 0;
                for (ArchivedQuestion question : poll.questions) {
                    order = 1;
                    for (ArchivedAnswer answer : question.answers) {
                        index = 1;
                        insertAnswers.setInt(index++, questionIdList.get(questionIndex));
                        insertAnswers.setInt(index++, order++);
                        insertAnswers.setString(index++, answer.answer);
                        insertAnswers.setInt(index++, answer.votes);
                        insertAnswers.setBoolean(index, answer.highestVoted);
                        insertAnswers.addBatch();
                    }
                    questionIndex++;
                }
                if (insertAnswers.executeBatch().length < 1) {
                    c.rollback();
                    throw new IllegalStateException("No poll answers were added to batch.");
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
