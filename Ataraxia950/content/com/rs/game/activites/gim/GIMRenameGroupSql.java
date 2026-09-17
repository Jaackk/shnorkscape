package com.rs.game.activites.gim;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * An SQL task that will rename a group.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMRenameGroupSql extends SQLRunnable {

    /**
     * The new group name.
     */
    private final String newGroupName;

    /**
     * The new group key.
     */
    private final String newGroupKey;

    /**
     * The group ID.
     */
    private final int groupId;

    /**
     * Creates a new {@link GIMRenameGroupSql}.
     */
    public GIMRenameGroupSql(String newGroupName, String newGroupKey, int groupId) {
        this.newGroupName = newGroupName;
        this.newGroupKey = newGroupKey;
        this.groupId = groupId;
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement updateGroupData = c.prepareStatement("UPDATE gim_group_data SET group_name = ?, group_key = ? WHERE group_id = ?;");
             PreparedStatement updatePreviousWinners = c.prepareStatement("UPDATE gim_previous_winners SET group_name = ? WHERE group_id = ?;")) {
            c.setAutoCommit(false);
            try {
                updateGroupData.setString(1, newGroupName);
                updateGroupData.setString(2, newGroupKey);
                updateGroupData.setInt(3, groupId);
                if (updateGroupData.executeUpdate() < 1) {
                    c.rollback();
                    throw new IllegalStateException("Rename query incomplete! (failed @ gim_group_data)");
                }
                updatePreviousWinners.setString(1, newGroupName);
                updatePreviousWinners.setInt(2, groupId);
                updatePreviousWinners.executeUpdate();
                c.commit();
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}