package com.rs.game.activites.gim;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * An SQL task that will transfer leadership to another member.
 *
 * @author lare96 <http://github.com/lare96>
 */
@AllArgsConstructor
public final class GIMTransferLeadershipSql extends SQLRunnable {

    /**
     * The group ID.
     */
    private final int groupId;

    /**
     * The new leader.
     */
    private final String newLeader;

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement checkGroupMember = c.prepareStatement("SELECT 1 FROM gim_group_score WHERE member_name = ?;");
             PreparedStatement updateGroupLeader = c.prepareStatement("UPDATE gim_group_data SET group_leader = ? WHERE group_id = ?;")) {
            c.setAutoCommit(false);
            try {
                // Ensure the new leader is a part of the group.
                checkGroupMember.setString(1, newLeader);
                try (ResultSet resultSet = checkGroupMember.executeQuery()) {
                    if (!resultSet.next()) {
                        c.rollback();
                        throw new IllegalStateException("Group with ID=" + groupId + " does not contain member=" + newLeader + ".");
                    }
                }

                // Update the leader.
                updateGroupLeader.setString(1, newLeader);
                updateGroupLeader.setInt(2, groupId);
                if (updateGroupLeader.executeUpdate() < 1) {
                    c.rollback();
                    throw new IllegalStateException("Group with ID=" + groupId + " was not updated!");
                }

                c.commit();
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
        GIM.getHighscores().requestRefresh();
    }
}
