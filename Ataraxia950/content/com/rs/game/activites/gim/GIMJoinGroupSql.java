package com.rs.game.activites.gim;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * An SQL statement that adds a player to an existing group.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMJoinGroupSql extends SQLRunnable {

    /**
     * The group ID to add the new player to.
     */
    private final int groupId;

    /**
     * The new player to add.
     */
    private final String memberName;

    /**
     * Creates a new {@link GIMJoinGroupSql}.
     */
    public GIMJoinGroupSql(int groupId, String memberName) {
        this.groupId = groupId;
        this.memberName = memberName;
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement insertMember = c.prepareStatement("INSERT INTO gim_group_score(group_id,member_name,total_xp,total_deaths,total_boss_points,total_levels,prestiges) VALUES (?,?,?,?,?,?,?)")) {
            insertMember.setInt(1, groupId);
            insertMember.setString(2, memberName);
            insertMember.setLong(3, 0);
            insertMember.setInt(4, 0);
            insertMember.setInt(5, 0);
            insertMember.setInt(6, 0);
            insertMember.setInt(7, 0);
            if(insertMember.executeUpdate() < 1) {
                throw new IllegalStateException("Insert for new group member "+memberName+" failed!");
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
        GIM.getHighscores().refreshNow();
    }
}
