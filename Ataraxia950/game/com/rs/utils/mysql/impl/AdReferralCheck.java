package com.rs.utils.mysql.impl;

import com.rs.cores.CoresManager;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.var;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class AdReferralCheck extends SQLRunnable {

    private final static String REFERRAL_QUERY = "SELECT referral FROM ad_referrals WHERE ipAddress = ? AND logged = 0";
    private final static String SITE_QUERY = "UPDATE ad_sites SET logged = logged + 1 WHERE name = ?";
    private final static String UPDATE_MAIN = "UPDATE ad_referrals SET logged = 1 WHERE ipAddress = ?";

    private final Player player;

    public AdReferralCheck(final Player player) {
        this.player = player;
    }

    @Override
    public void execute(final DatabaseCredential auth) {
        var ipAddress = player.getIP();

        try (final Connection con = Pool.getConnection(auth, "ataraxia")) {
            con.setAutoCommit(false);
            try (final PreparedStatement referralCheck = con.prepareStatement(REFERRAL_QUERY);
                 final PreparedStatement siteCheck = con.prepareStatement(SITE_QUERY);
                 final PreparedStatement updateMain = con.prepareStatement(UPDATE_MAIN)) {

                referralCheck.setString(1, ipAddress);
                try (final ResultSet set = referralCheck.executeQuery()) {

                    if (!set.next()) {
                       con.rollback();
                       return;
                    }

                    var siteName = set.getString("referral");
                    siteCheck.setString(1, siteName);
                    siteCheck.execute();

                    updateMain.setString(1, ipAddress);
                    updateMain.execute();
                }
                con.commit();
                CoresManager.getServiceProvider().addGameTask(() -> player.setReferralChecked(true));
            } catch (Exception e) {
                con.rollback();
                Logger.getGlobal().error(new ParameterizedMessage("Ad referral check failed for: {}", player.getDisplayName()), e);
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.getGlobal().error(new ParameterizedMessage("Ad referral check failed for: {}", player.getDisplayName()), e);
        }
    }

}
