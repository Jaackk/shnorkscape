package com.rs.utils.mysql.impl;

import com.rs.cores.CoresManager;
import com.rs.game.player.Player;
import com.rs.game.player.content.VoteManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckVote extends SQLRunnable {

    private final Player player;

    private final static String CHECK_QUERY = "SELECT * FROM votes WHERE username=? AND claimed=0 and voted_on != -1";
    private final static String UPDATE_QUERY = "UPDATE votes SET claimed = 1 WHERE username = ? and claimed = 0 and voted_on != -1";

    public CheckVote(final Player player) {
        this.player = player;
    }

    @Override
    public void execute(final DatabaseCredential auth) {
        final String name = player.getUsername();
        int votes = getVotesCount(Utils.formatPlayerNameForDisplay(name), auth) + getVotesCount(Utils.formatPlayerNameForProtocol(name), auth);
        if (votes == 0)
            CoresManager.getServiceProvider().addGameTask(() -> VoteManager.handleEmptyReward(player));
        else {
            CoresManager.getServiceProvider().addGameTask(() -> VoteManager.startRewardProcess(player, votes));
        }
    }

    private int getVotesCount(String name, DatabaseCredential auth) {
        int votes = 0;
        try (final Connection con = Pool.getConnection(auth, "ataraxia")) {
            con.setAutoCommit(false);
            try (final PreparedStatement checkVote = con.prepareStatement(CHECK_QUERY); final PreparedStatement updateVote = con.prepareStatement(UPDATE_QUERY)) {
                checkVote.setString(1, name);
                try (final ResultSet results = checkVote.executeQuery()) {
                    if (!results.next()) {
                        con.rollback();
                        return votes;
                    }
                    results.beforeFirst();
                    while (results.next()) {
                        final String ipAddress = results.getString("ip_address");
                        final int siteId = results.getInt("site_id");
                        Logger.getGlobal().info("Vote by {}. (sid: {}, ip: {})", name, siteId, ipAddress);
                        votes++;
                    }
                    updateVote.setString(1, name);
                    updateVote.execute();
                    con.commit();
                    return votes;
                } catch (Exception e) {
                    con.rollback();
                    Logger.getGlobal().error(new ParameterizedMessage("Checking votes failed; player: {}", name), e);
                    return votes;
                } finally {
                    con.setAutoCommit(true);
                }
            } catch (Exception e) {
                Logger.getGlobal().error(new ParameterizedMessage("Checking votes failed; player: {}", name), e);
                return votes;
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            return votes;
        }
    }
}
