package com.rs.game.activites.gim.season;

import com.rs.game.activites.gim.GIMGroupMember;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;

/**
 * Fetches a group member from the database.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMFetchMember extends SQLRunnable implements Callable<GIMGroupMember> {

    /**
     * The member's username.
     */
    private final String member;

    /**
     * The fetched group member instance.
     */
    private GIMGroupMember loadedMember;

    /**
     * Creates a new {@link GIMFetchMember}.
     */
    public GIMFetchMember(String member) {
        this.member = member;
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia")) {
            try (PreparedStatement selectMemberData = c.prepareStatement("SELECT * FROM gim_group_score WHERE member_name = ?;")) {
                selectMemberData.setString(1, member);
                try(ResultSet results = selectMemberData.executeQuery()) {
                    if(results.next()) {
                        int groupId = results.getInt("group_id");
                        String memberName = results.getString("member_name");
                        long totalXp = results.getLong("total_xp");
                        int totalDeaths = results.getInt("total_deaths");
                        int totalBp = results.getInt("total_boss_points");
                        int totalLevels = results.getInt("total_levels");
                        int prestiges = results.getInt("prestiges");
                        loadedMember = new GIMGroupMember(groupId, memberName, totalXp, totalDeaths, totalBp, totalLevels, prestiges);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    @Override
    public GIMGroupMember call() throws Exception {
        prepare();
        return loadedMember;
    }
}
