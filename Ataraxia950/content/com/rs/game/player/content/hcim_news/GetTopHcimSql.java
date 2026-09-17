package com.rs.game.player.content.hcim_news;

import com.google.common.collect.ImmutableMap;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public final class GetTopHcimSql extends SQLRunnable implements Callable<ImmutableMap<String, TopHcim>> {
    private final Map<String, TopHcim> users = new LinkedHashMap<>();

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement pst = c.prepareStatement("SELECT username, overall_xp FROM hs_users WHERE difficulty = 'hardcore' ORDER BY overall_xp DESC LIMIT 10;")) {
            try (ResultSet results = pst.executeQuery()) {
                int rank = 1;
                while (results.next()) {
                    String username = results.getString("username");
                    long overallXp = results.getLong("overall_xp");
                    users.put(username, new TopHcim(rank++, username, overallXp));
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    @Override
    public ImmutableMap<String, TopHcim> call() throws Exception {
        prepare();
        return ImmutableMap.copyOf(users);
    }
}
