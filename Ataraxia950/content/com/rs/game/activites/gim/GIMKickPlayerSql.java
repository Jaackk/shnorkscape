package com.rs.game.activites.gim;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

/**
 * An SQL task that removes a player from the GIM database.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMKickPlayerSql extends SQLRunnable {

    /**
     * The member's name.
     */
    private final String memberName;

    /**
     * Creates a new {@link GIMKickPlayerSql}.
     */
    public GIMKickPlayerSql(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement deleteMember = c.prepareStatement("DELETE FROM gim_group_score WHERE member_name = ?;")) {
            deleteMember.setString(1, memberName);
            if (deleteMember.executeUpdate() < 1) {
                throw new IllegalStateException("No player was deleted!");
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
