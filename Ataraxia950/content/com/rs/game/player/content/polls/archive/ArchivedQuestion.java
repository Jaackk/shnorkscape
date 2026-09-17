package com.rs.game.player.content.polls.archive;

import java.util.Optional;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ArchivedQuestion {
    public final String question;
    public final ArchivedAnswer[] answers;

    public ArchivedQuestion(String question, ArchivedAnswer[] answers) {
        this.question = question;
        this.answers = answers;
    }

    public int getTotalVotes() {
        int votes = 0;
        for(ArchivedAnswer answer : answers) {
            votes += answer.votes;
        }
        return votes;
    }

    public Optional<ArchivedAnswer> getHighestVoted() {
        for (ArchivedAnswer answer : answers) {
            if (answer.highestVoted) {
                return Optional.of(answer);
            }
        }
        return Optional.empty();
    }
}