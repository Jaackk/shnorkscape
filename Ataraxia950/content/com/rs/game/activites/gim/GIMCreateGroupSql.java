package com.rs.game.activites.gim;

import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.concurrent.Callable;

/**
 * An SQL statement that adds a group to the database.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMCreateGroupSql extends SQLRunnable implements Callable<Integer> {

    /**
     * The pending group being saved.
     */
    private final GIMPendingGroup group;

    /**
     * The group's SQL identifier.
     */
    private int groupId;

    /**
     * Creates a new {@link GIMCreateGroupSql}.
     */
    public GIMCreateGroupSql(GIMPendingGroup group) {
        this.group = group;
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia")) {
            c.setAutoCommit(false);
            try (PreparedStatement insertGroupData = c.prepareStatement(
                    "INSERT INTO gim_group_data(group_name,group_key,group_leader,group_score,group_rank,creation_date) " +
                            "VALUES (?,?,?,0,-1,?);", Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertGroupScore = c.prepareStatement(insertGroupScoreStatement())) {
                int index = 1;
                insertGroupData.setString(index++, group.getGroupName());
                insertGroupData.setString(index++, group.getGroupKey());
                insertGroupData.setString(index++, group.getLeader().getUsername());
                insertGroupData.setDate(index, Date.valueOf(LocalDate.now()));
                if (insertGroupData.executeUpdate() > 0) { // Try to insert group data into the table.

                    try (ResultSet results = insertGroupData.getGeneratedKeys()) {
                        if (results.next()) {
                            groupId = results.getInt(1); // Retrieve group ID.
                        }
                    }

                    index = 1;
                    for (Player player : group.getMembers()) {
                        insertGroupScore.setInt(index++, groupId);
                        insertGroupScore.setString(index++, player.getUsername());
                    }
                    if (insertGroupScore.executeUpdate() > 0) { // Try to insert group score into the table.
                        Logger.getGlobal().info("New GIM group '" + group.getGroupName() + "' successfully created.");
                    } else {
                        // Group score not inserted, undo commit.
                        groupId = -1;
                        c.rollback();
                        return;
                    }
                } else {
                    // Group data not inserted, undo commit.
                    groupId = -1;
                    c.rollback();
                    return;
                }
                c.commit();
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
        GIM.getPendingGroups().remove(group.getGroupKey());
    }

    @Override
    public Integer call() throws Exception {
        prepare();
        return groupId;
    }

    /**
     * Dynamically builds an SQL statement that will add group scores to the database.
     */
    private String insertGroupScoreStatement() {
        StringBuilder statementBuilder = new StringBuilder("INSERT INTO gim_group_score(group_id,member_name,total_xp,total_deaths,total_boss_points,total_levels,prestiges) VALUES ");
        for (int loop = 0; loop < group.getMembers().size(); loop++) {
            statementBuilder.append("(?,?,0,0,0,0,0),");
        }
        statementBuilder.setLength(statementBuilder.length() - 1);
        statementBuilder.append(";  ");
        return statementBuilder.toString();
    }
}
