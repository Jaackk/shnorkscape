package com.rs.game.player.content.polls.archive;

import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * @author lare96 <http://github.com/lare96>
 */
@AllArgsConstructor
public final class ArchivedPoll {
    public final LocalDate ended;
    public final String title;
    public final String[] explanation;
    public final int totalVoters;
    public final ArchivedQuestion[] questions;

    public String getExplanationAsString() {
        StringBuilder sb = new StringBuilder();
        for (String str : explanation) {
            sb.append(str).append('\n');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}