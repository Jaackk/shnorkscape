package com.rs.utils.mysql.impl;

import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class ResetUserHiscores extends SQLRunnable {

    private final String username;
    private final String table;

    public ResetUserHiscores(final String username, final String table) {
        this.username = username;
        this.table = table;
    }

    @Override
    public void execute(final DatabaseCredential auth) {
        final String name = Utils.formatPlayerNameForDisplay(username);

        try(final Connection con = Pool.getConnection(auth, "ataraxia");
            final PreparedStatement pst = con.prepareStatement("DELETE FROM "+table+" WHERE username = ?")) {

            pst.setString(1, name);
            pst.execute();

        } catch(final Exception ex) {
            Logger.getGlobal().catching(ex);
        }
    }

}
