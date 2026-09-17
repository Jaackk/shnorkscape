package com.rs.game.player.content.polls.archive;

import lombok.AllArgsConstructor;

/**
 * @author lare96 <http://github.com/lare96>
 */
@AllArgsConstructor
public final class ArchivedAnswer {
    public final String answer;
    public final int votes;
    public final boolean highestVoted;
}