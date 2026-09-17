package com.rs.utils.mysql.impl;

import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.DatabaseUtil;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import com.rs.utils.mysql.struct.PrivateMessageLog;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class SubmitPrivateLogs extends SQLRunnable {

    private final static String INSERT = "INSERT INTO logs_private_chat ( user, user_ip, friend, message, time_added ) VALUES ( ? ,? ,?, ?, ? )";

    public SubmitPrivateLogs() {}

    @Override
    public void execute(final DatabaseCredential auth) {
        try(final Connection con = Pool.getConnection(auth, "ataraxia");
            final PreparedStatement pst = con.prepareStatement(DatabaseUtil.buildBatch(INSERT, PrivateMessageLog.list.size(), 5))) {

            int index = 0;
            for(final PrivateMessageLog log : PrivateMessageLog.list) {
                final Player player = log.getPlayer();
                pst.setString(++index, player.getUsername());
                pst.setString(++index, player.getIP());
                pst.setString(++index, log.getFriend());
                pst.setString(++index, log.getMessage());
                pst.setTimestamp(++index, log.getDate());
            }

            pst.execute();
            PrivateMessageLog.list.clear();

        } catch(final Exception ex) {
            Logger.getGlobal().error("Failed to insert public chat log bundle", ex);
        }
    }
}
