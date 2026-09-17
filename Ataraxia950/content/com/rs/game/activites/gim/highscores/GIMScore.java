package com.rs.game.activites.gim.highscores;

/**
 * A class representing the total score of a group.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMScore {

    /**
     * {@code x} * {@code deaths} XP will be removed from the team per death.
     */
    private static final int DEATH_XP_PENALTY = 1_500_000;

    /**
     * {@code x} * {@code deaths} BP will be removed from the team per death.
     */
    private static final int DEATH_BP_PENALTY = 15;

    /**
     * Will take {@code x} XP to gain 1 score.
     */
    private static final int XP_PER_SCORE = 150_000;

    /**
     * Will take {@code x} BP to gain 1 score.
     */
    private static final int BP_PER_SCORE = 2;

    /**
     * Will take {@code x} levels reached to gain 1 score.
     */
    private static final int LEVEL_PER_SCORE = 3;

    /**
     * Will give {@code x} score for each prestige.
     */
    private static final int SCORE_PER_PRESTIGE = 500;

    /**
     * The group SQL ID.
     */
    private final int groupId;

    /**
     * The group's total gained XP.
     */
    private final long totalXp;

    /**
     * The group's total deaths.
     */
    private final int totalDeaths;

    /**
     * The group's total gained boss points.
     */
    private final int totalBp;

    /**
     * The group's total gained levels.
     */
    private final int totalLevels;

    /**
     * The group's total prestiges.
     */
    private final int prestiges;

    /**
     * The group's score.
     */
    private final long score;

    /**
     * Creates a new {@link GIMScore}.
     */
    public GIMScore(int groupId, long totalXp, int totalDeaths, int totalBp, int totalLevels, int prestiges, int memberCount) {
        this.groupId = groupId;
        this.totalXp = totalXp;
        this.totalDeaths = totalDeaths;
        this.totalBp = totalBp;
        this.totalLevels = totalLevels;
        this.prestiges = prestiges;
        score = computeScore(memberCount);
    }

    /**
     * Weighted by {@code deaths -> boss points -> total level -> XP} in order of highest -> lowest.
     */
    private long computeScore(int memberCount) {
        long deathPenalty;
        long xpFactor = 0;
        long bossPointsFactor = 0;
        long totalLevelFactor = 0;
        long prestigeFactor = 0;

        // <x> XP = 1 Score, Lose 1/<x> of XP per death
        if (totalXp > 0) {
            deathPenalty = DEATH_XP_PENALTY * totalDeaths;
            xpFactor = totalXp - deathPenalty;
            if(xpFactor < 0) {
                xpFactor = 0;
            }
            xpFactor /= XP_PER_SCORE;
        }

        // <x> BP = 1 Score, Lose 1/<x> of BP per death
        if (totalBp > 0) {
            deathPenalty = DEATH_BP_PENALTY * totalDeaths;
            bossPointsFactor = totalBp - deathPenalty;
            if(bossPointsFactor < 0) {
                bossPointsFactor = 0;
            }
            bossPointsFactor /= BP_PER_SCORE;
        }

        // <x> Levels = 1 Score
        if (totalLevels > 0) {
            totalLevelFactor = totalLevels / LEVEL_PER_SCORE;
        }

        // 1 Prestige = 750 Score
        if (prestiges > 0) {
           prestigeFactor = prestiges * SCORE_PER_PRESTIGE;
        }

        // Reduce the score for groups with more group members.
        long grossScore = xpFactor + bossPointsFactor + totalLevelFactor + prestigeFactor;
        if (memberCount == 2) {
            return (long) (grossScore * 1.50);
        } else if (memberCount == 3) {
            return (long) (grossScore * 1.25);
        } else if (memberCount == 4) {
            return grossScore;
        } else {
            throw new IllegalStateException("Group with invalid member count!");
        }
    }

    public int getGroupId() {
        return groupId;
    }

    public long getTotalXp() {
        return totalXp;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public int getTotalBp() {
        return totalBp;
    }

    public int getTotalLevels() {
        return totalLevels;
    }

    public int getPrestiges() {
        return prestiges;
    }

    public long getScore() {
        return score;
    }
}