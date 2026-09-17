package com.rs.game.activites.gim;

/**
 * A class representing a single group member.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMGroupMember {

    /**
     * Their group ID.
     */
    private final int groupId;

    /**
     * Their username.
     */
    private final String username;

    /**
     * Their total XP for this season.
     */
    private final long totalXp;

    /**
     * Their deaths for this season.
     */
    private final int totalDeaths;

    /**
     * Their total boss points for this season.
     */
    private final int totalBp;

    /**
     * Their total levels for this season.
     */
    private final int totalLevels;

    /**
     * Their total prestiges for this season.
     */
    private final int totalPrestiges;

    /**
     * Creates a new {@link GIMGroupMember}.
     */
    public GIMGroupMember(int groupId, String username, long totalXp, int totalDeaths, int totalBp, int totalLevels, int totalPrestiges) {
        this.groupId = groupId;
        this.username = username;
        this.totalXp = totalXp;
        this.totalDeaths = totalDeaths;
        this.totalBp = totalBp;
        this.totalLevels = totalLevels;
        this.totalPrestiges = totalPrestiges;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getUsername() {
        return username;
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

    public int getTotalPrestiges() {
        return totalPrestiges;
    }
}
