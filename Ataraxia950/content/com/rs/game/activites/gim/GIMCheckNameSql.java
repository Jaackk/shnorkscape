package com.rs.game.activites.gim;

import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;

/**
 * An SQL query that determines if a group name is already taken.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMCheckNameSql extends SQLRunnable implements Callable<Boolean> {

    /**
     * The group key.
     */
    private final String key;

    /**
     * The result of the query, if a group with the same key was found.
     */
    private boolean foundGroup;

    /**
     * Creates a new {@link GIMCheckNameSql}.
     */
    public GIMCheckNameSql(String key) {
        this.key = key;
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement pst = c.prepareStatement("SELECT 1 FROM gim_group_data WHERE group_key = ?;")) {
            pst.setString(1, key);
            try (ResultSet results = pst.executeQuery()) {
                if (results.next()) {
                    foundGroup = true;
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    @Override
    public Boolean call() throws Exception {
        prepare();
        return foundGroup;
    }
}
