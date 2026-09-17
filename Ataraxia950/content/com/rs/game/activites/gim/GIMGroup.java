package com.rs.game.activites.gim;

import com.google.common.collect.ImmutableSet;
import com.rs.game.activites.gim.highscores.GIMScore;

import java.time.LocalDate;

/**
 * A class representing the core GIM group data.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMGroup {

    /**
     * The SQL {@code AUTO_INCREMENT} id.
     */
    private final int groupId;

    /**
     * The group name.
     */
    private final String groupName;

    /**
     * The group key.
     */
    private final String groupKey;

    /**
     * The group leader's username.
     */
    private final String leaderName;

    /**
     * The group members' usernames.
     */
    private final ImmutableSet<String> members;

    /**
     * The group's score.
     */
    private final GIMScore score;

    /**
     * The group's rank.
     */
    private final int rank;

    /**
     * The date that this group was created on.
     */
    private final LocalDate creationDate;

    /**
     * Creates a new {@link GIMGroup}.
     */
    public GIMGroup(int groupId, String groupName, String groupKey, String leaderName, ImmutableSet<String> members, GIMScore score, int rank) {
        this.groupId = groupId;
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.leaderName = leaderName;
        this.members = members;
        this.score = score;
        this.rank = rank;
        creationDate = LocalDate.now();
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public ImmutableSet<String> getMembers() {
        return members;
    }

    public long getScore() {
        return score.getScore();
    }

    public long getTotalXp() {
        return score.getTotalXp();
    }

    public int getTotalDeaths() {
        return score.getTotalDeaths();
    }

    public int getTotalBp() {
        return score.getTotalBp();
    }

    public int getTotalLevels() {
        return score.getTotalLevels();
    }

    public int getTotalPrestiges() {
        return score.getPrestiges();
    }

    public int getRank() {
        return rank;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }
}