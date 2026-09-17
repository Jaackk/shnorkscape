package com.rs.utils.mysql.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import com.rs.utils.mysql.struct.NamedItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class SubmitTradeLog extends SQLRunnable {

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String INSERT = "INSERT INTO logs_trades ( user, user_ip, given, partner, partner_ip, received ) VALUES ( ?, ?, ?, ?, ?, ? )";

    private transient final Player player;
    private transient final Player friend;
    private final List<NamedItem> given;
    private final List<NamedItem> received;

    public SubmitTradeLog(final Player player, final Player friend, final List<NamedItem> given, final List<NamedItem> received) {
        this.player = player;
        this.friend = friend;
        this.given = given;
        this.received = received;
    }

    @Override
    public void execute(final DatabaseCredential auth) {
        try(final Connection con = Pool.getConnection(auth, "ataraxia");
            final PreparedStatement pst = con.prepareStatement(INSERT)) {

            pst.setString(1, player.getUsername());
            pst.setString(2, player.getIP());
            pst.setString(3, gson.toJson(given));

            pst.setString(4, friend.getUsername());
            pst.setString(5, friend.getIP());
            pst.setString(6, gson.toJson(received));
            pst.execute();

        } catch(final Exception ex) {
            Logger.getGlobal().error("Failed to submit trade log for users:"+player.getUsername()+" / "+friend.getUsername(), ex);
        }
    }
}
