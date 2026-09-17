package com.rs.game.player.content.bank_highscores;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

public class GetRichestBanksSql extends SQLRunnable {

    public static final boolean ACTIVE = !Settings.TEST_SERVER_MODE;
    private static volatile RichestBanks richestBanks = new RichestBanks(null, -1, null, -1, null, -1, null, -1);

    public static RichestBanks getRichestBanks() {
        return richestBanks;
    }

    public static void load() {
        if (!ACTIVE)
            return;
        CoresManager.getServiceProvider().scheduleRepeatingTask(new GetRichestBanksSql(), 1, 15, TimeUnit.MINUTES);
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement regularPst = c.prepareStatement("SELECT username, total_value " +
                     "FROM hs_banks " +
                     "ORDER BY total_value DESC " +
                     "LIMIT 1");
             PreparedStatement gimPst = c.prepareStatement("SELECT username, total_value " +
                     "FROM hs_banks_gim " +
                     "ORDER BY total_value DESC " +
                     "LIMIT 1");
             PreparedStatement hcimPst = c.prepareStatement("SELECT username, total_value " +
                     "FROM hs_banks_hciron " +
                     "ORDER BY total_value DESC " +
                     "LIMIT 1");
             PreparedStatement imPst = c.prepareStatement("SELECT username, total_value " +
                     "FROM hs_banks_iron " +
                     "ORDER BY total_value DESC " +
                     "LIMIT 1")) {
            String regularUsername = null;
            long regularValue = -1;
            try (ResultSet resultSet = regularPst.executeQuery()) {
                if (resultSet.next()) {
                    regularUsername = resultSet.getString("username");
                    regularValue = resultSet.getLong("total_value");
                }
            }
            String gimUsername = null;
            long gimValue = -1;
            try (ResultSet resultSet = gimPst.executeQuery()) {
                if (resultSet.next()) {
                    gimUsername = resultSet.getString("username");
                    gimValue = resultSet.getLong("total_value");
                }
            }
            String hcimUsername = null;
            long hcimValue = -1;
            try (ResultSet resultSet = hcimPst.executeQuery()) {
                if (resultSet.next()) {
                    hcimUsername = resultSet.getString("username");
                    hcimValue = resultSet.getLong("total_value");
                }
            }
            String imUsername = null;
            long imValue = -1;
            try (ResultSet resultSet = imPst.executeQuery()) {
                if (resultSet.next()) {
                    imUsername = resultSet.getString("username");
                    imValue = resultSet.getLong("total_value");
                }
            }
            richestBanks = new RichestBanks(regularUsername, regularValue, gimUsername, gimValue, hcimUsername, hcimValue, imUsername, imValue);
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
