package com.rs.game.player.content.polls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rs.cores.CoresManager;
import com.rs.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class PollQuestionsDatabase {

    public static final class PollQuestion {
        public final String question;
        public final String[] answers;

        public PollQuestion(String question, String... answers) {
            this.question = question;
            this.answers = answers;
        }
    }

    private final Path questionsFile = Paths.get("data/pollbooth/questions.txt");
    private ImmutableList<PollQuestion> pollQuestions;
    private ImmutableList<String> explanation;
    private String title;
    private LocalDate endDate;

    public void load() {
        if (Files.exists(questionsFile)) {
            try (Scanner scanner = new Scanner(questionsFile)) {
                List<PollQuestion> questions = new ArrayList<>();
                boolean parsed = false;
                while (scanner.hasNextLine()) {
                    String nextLine = scanner.nextLine().trim();
                    if (nextLine.contains("~")) {
                        throw new IllegalStateException("Do not use the ~ character in questions.txt");
                    }
                    if (nextLine.startsWith("title:")) {
                        if (title == null) {
                            title = nextLine.substring(6);
                            parsed = true;
                        } else {
                            throw new IllegalStateException("Title specified twice!");
                        }
                    } else if (nextLine.startsWith("ends:")) {
                        if (endDate == null) {
                            endDate = LocalDate.parse(nextLine.substring(5),
                                    DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));
                            parsed = true;
                        } else {
                            throw new IllegalStateException("End date specified twice!");
                        }
                    } else if (nextLine.equals("explanation:")) {
                        parsed = true;

                        List<String> explanationLines = new ArrayList<>();
                        while (scanner.hasNextLine()) {
                            String nextExplLine = scanner.nextLine().trim();
                            if (nextExplLine.contains("~")) {
                                throw new IllegalStateException("Do not use the ~ character in questions.txt");
                            }
                            if (nextExplLine.equals("end")) {
                                break;
                            }
                            if (nextExplLine.startsWith("->")) {
                                explanationLines.add(nextExplLine.substring(2));
                            }
                        }
                        if (explanationLines.isEmpty()) {
                            throw new IllegalStateException("Explanation must have at least one line!");
                        }
                        explanation = ImmutableList.copyOf(explanationLines);
                    } else if (nextLine.startsWith("question:")) {
                        parsed = true;
                        String question = nextLine.substring(9);
                        List<String> answers = new ArrayList<>();
                        while (scanner.hasNextLine()) {
                            String answerLine = scanner.nextLine().trim();
                            if (answerLine.contains("~")) {
                                throw new IllegalStateException("Do not use the ~ character in questions.txt");
                            }
                            if (answerLine.equals("end")) {
                                break;
                            }
                            if (answerLine.startsWith("answer:")) {
                                answers.add(answerLine.substring(7));
                            }
                        }
                        if (answers.isEmpty()) {
                            throw new IllegalStateException("Poll questions must have at least one answer!");
                        }
                        answers.add("Skip question.");
                        questions.add(new PollQuestion(question, Iterables.toArray(answers, String.class)));
                    }
                }
                if (parsed && questions.isEmpty()) {
                    throw new IllegalStateException("There must be at least one poll question.");
                }
                if (parsed && title == null) {
                    throw new IllegalStateException("There must be a poll title specified.");
                }
                if (parsed && endDate == null) {
                    throw new IllegalStateException("There must be a poll end date specified.");
                }
                pollQuestions = ImmutableList.copyOf(questions);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (pollQuestions == null) {
            pollQuestions = ImmutableList.of();
        }
        if (explanation == null) {
            explanation = ImmutableList.of();
        }
    }

    public void clear(boolean clearFile) {
        pollQuestions = null;
        explanation = null;
        title = null;
        endDate = null;
        if (clearFile)
           CoresManager.getServiceProvider().executeNow(() -> Utils.clearFile(questionsFile));
    }

    public PollQuestion get(int index) {
        return pollQuestions.get(index);
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ImmutableList<String> getExplanation() {
        return explanation;
    }

    public ImmutableList<PollQuestion> getPollQuestions() {
        return pollQuestions;
    }

    public int size() {
        if (pollQuestions == null)
            return 0;
        return pollQuestions.size();
    }
}