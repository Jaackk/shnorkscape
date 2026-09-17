package com.rs.utils.mysql.impl;

import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.DatabaseUtil;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import com.rs.utils.mysql.struct.GenericChatLog;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class SubmitPublicLogs extends SQLRunnable {

    private final static String INSERT = "INSERT INTO logs_public_chat ( user, user_ip, message, time_added ) VALUES ( ? ,? ,?, ? )";

    public SubmitPublicLogs() {}

    @Override
    public void execute(final DatabaseCredential auth) {
        try(final Connection con = Pool.getConnection(auth, "ataraxia");
            final PreparedStatement pst = con.prepareStatement(DatabaseUtil.buildBatch(INSERT, GenericChatLog.list.size(), 4))) {

            int index = 0;
            for(final GenericChatLog log : GenericChatLog.list) {
                final Player player = log.getPlayer();
                pst.setString(++index, player.getUsername());
                pst.setString(++index, player.getIP());
                pst.setString(++index, log.getMessage());
                pst.setTimestamp(++index, log.getDate());
            }

            pst.execute();
            GenericChatLog.list.clear();

        } catch(final Exception ex) {
            Logger.getGlobal().error("Failed to insert public chat log bundle", ex);
        }
    }

}
