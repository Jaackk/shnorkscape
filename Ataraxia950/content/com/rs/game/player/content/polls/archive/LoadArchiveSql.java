package com.rs.game.player.content.polls.archive;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.Data;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class LoadArchiveSql extends SQLRunnable implements Callable<List<ArchivedPoll>> {

    @Data
    private static final class LoadedArchivedPoll {
        private final int pollId;
        private final LocalDate endDate;
        private final String title;
        private final String description;
        private final int totalVoters;
    }

    @Data
    private static final class LoadedArchivedAnswer implements Comparable<LoadedArchivedAnswer> {
        private final int answerId;
        private final int questionId;
        private final int order;
        private final String title;
        private final int votes;
        private final boolean highestVoted;

        @Override
        public int compareTo(@NotNull LoadArchiveSql.LoadedArchivedAnswer o) {
            return Integer.compare(order, o.order);
        }
    }

    @Data
    private static final class LoadedArchivedQuestion implements Comparable<LoadedArchivedQuestion> {
        private final int questionId;
        private final int pollId;
        private final int order;
        private final String title;

        @Override
        public int compareTo(@NotNull LoadArchiveSql.LoadedArchivedQuestion o) {
            return Integer.compare(order, o.order);
        }
    }


    private final List<ArchivedPoll> archive = new ArrayList<>();

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement selectInfo = c.prepareStatement("SELECT * FROM poll_history;");
             PreparedStatement selectQuestions = c.prepareStatement("SELECT * FROM poll_history_questions;");
             PreparedStatement selectAnswers = c.prepareStatement("SELECT * FROM poll_history_answers;")) {
            c.setAutoCommit(false);

            try {

                // Retrieve list of polls.
                List<LoadedArchivedPoll> pollInfo = new ArrayList<>();
                try (ResultSet results = selectInfo.executeQuery()) {
                    while (results.next()) {
                        int pollId = results.getInt(1);
                        LocalDate endDate = results.getDate(2).toLocalDate();
                        String title = results.getString(3);
                        String description = results.getString(4);
                        int totalVoters = results.getInt(5);
                        pollInfo.add(new LoadedArchivedPoll(pollId, endDate, title, description, totalVoters));
                    }
                }

                // Retrieve all questions for polls.
                ListMultimap<Integer, LoadedArchivedQuestion> pollQuestions = ArrayListMultimap.create();
                try (ResultSet results = selectQuestions.executeQuery()) {
                    while (results.next()) {
                        int questionId = results.getInt(1);
                        int pollId = results.getInt(2);
                        int order = results.getInt(3);
                        String title = results.getString(4);
                        pollQuestions.put(pollId, new LoadedArchivedQuestion(questionId, pollId, order, title));
                    }
                }

                // Retrieve all answers for questions.
                ListMultimap<Integer, LoadedArchivedAnswer> pollAnswers = ArrayListMultimap.create();
                try (ResultSet results = selectAnswers.executeQuery()) {
                    while (results.next()) {
                        int answerId = results.getInt(1);
                        int questionId = results.getInt(2);
                        int order = results.getInt(3);
                        String title = results.getString(4);
                        int votes = results.getInt(5);
                        boolean highestVoted = results.getBoolean(6);
                        pollAnswers.put(questionId, new LoadedArchivedAnswer(answerId, questionId, order, title, votes, highestVoted));
                    }
                }

                // Load all poll info.
                for (LoadedArchivedPoll info : pollInfo) {

                    // Load all poll questions.
                    List<ArchivedQuestion> questionList = new ArrayList<>();
                    List<LoadedArchivedQuestion> loadedQuestionList = new ArrayList<>(pollQuestions.get(info.pollId));
                    Collections.sort(loadedQuestionList);
                    for (LoadedArchivedQuestion question : loadedQuestionList) {

                        // Load all poll answers.
                        List<ArchivedAnswer> answerList = new ArrayList<>();
                        List<LoadedArchivedAnswer> loadedAnswerList = new ArrayList<>(pollAnswers.get(question.questionId));
                        Collections.sort(loadedAnswerList);
                        for (LoadedArchivedAnswer answer : loadedAnswerList) {
                            answerList.add(new ArchivedAnswer(answer.title, answer.votes, answer.highestVoted));
                        }
                        val answerArray = Iterables.toArray(answerList, ArchivedAnswer.class);
                        questionList.add(new ArchivedQuestion(question.title, answerArray));
                    }
                    val questionArray = Iterables.toArray(questionList, ArchivedQuestion.class);
                    val descriptionArray = Iterables.toArray(Utils.listOfLines(info.description), String.class);
                    archive.add(new ArchivedPoll(info.endDate, info.title, descriptionArray, info.totalVoters, questionArray));
                }
                c.commit();
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    @Override
    public List<ArchivedPoll> call() throws Exception {
        prepare();
        return archive;
    }
}