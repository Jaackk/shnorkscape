package com.rs.game.activites.gim.highscores;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.rs.cores.CoresManager;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Pair;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.val;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An SQL task that refreshes the leaderboards. Assigns a total score and rank to every group.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMHighscoresRefreshSql extends SQLRunnable {

    /**
     * A representation of data about a group read from an SQL table.
     */
    private static final class SqlGIMGroup {

        /**
         * The group id.
         */
        private final int id;

        /**
         * The group name.
         */
        private final String name;

        /**
         * The group key.
         */
        private final String key;

        /**
         * The group leader.
         */
        private final String leader;

        /**
         * The group creation date.
         */
        private final Date creationDate;

        /**
         * Creates a new {@link SqlGIMGroup}.
         */
        private SqlGIMGroup(int id, String name, String key, String leader, Date creationDate) {
            this.id = id;
            this.name = name;
            this.key = key;
            this.leader = leader;
            this.creationDate = creationDate;
        }
    }

    /**
     * Will compare group scores.
     */
    private static final class ScoreComparator implements Comparator<GIMScore> {

        @Override
        public int compare(GIMScore o1, GIMScore o2) {
            return Long.compare(o1.getScore(), o2.getScore());
        }
    }

    /**
     * Will run the highscores refresh every 5 mins.
     */
    static final class GIMHighscoresRefreshTask extends WorldTask implements Runnable {

        /**
         * The delay.
         */
        private final int delay;

        /**
         * The counter to meet the delay.
         */
        private int counter;

        /**
         * If an update should be forced.
         */
        private final AtomicBoolean forceUpdate = new AtomicBoolean();

        /**
         * If this refresh task is paused.
         */
        private volatile boolean paused;

        /**
         * Creates a new {@link GIMHighscoresRefreshTask}.
         */
        GIMHighscoresRefreshTask() {
            delay = GIM.BETA_MODE ? 100 : GIMHighscores.REFRESH_MINUTES * 50;
        }

        @Override
        public void run() {
            if (paused) {
                return;
            }
            if (forceUpdate.getAndSet(false)) {
                Logger.getGlobal().info("Executing forced GIM highscores refresh...");
                CoresManager.getServiceProvider().executeNow(new GIMHighscoresRefreshSql());
                counter = 0;
                return;
            }
            if (++counter >= delay) {
                Logger.getGlobal().info("Executing timed GIM highscores refresh...");
                CoresManager.getServiceProvider().executeNow(new GIMHighscoresRefreshSql());
                counter = 0;
            }
        }

        /**
         * Starts this refresh task instance.
         */
        void start() {
            // Initial refresh.
            GIMHighscoresRefreshSql refreshSql = new GIMHighscoresRefreshSql();
            refreshSql.prepare();

            // Start task.
            WorldTasksManager.schedule(this, 2, 2);
        }

        /**
         * Forces the server to update the highscores on the next game tick. Use {@link GIMHighscores#refreshNow()} if you need an immediate refresh.
         */
        public void forceUpdate() {
            forceUpdate.set(true);
        }

        /**
         * Pauses the task. No updates or counter increments will occur.
         */
        public void pause() {
            paused = true;
        }

        /**
         * Unpauses the task. Updates and counter increments will resume.
         */
        public void unpause() {
            paused = false;
        }
    }

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection conn = Pool.getConnection(auth, "ataraxia")) {
            conn.setAutoCommit(false);
            try {
                // Get relevant data from databases.
                Map<Integer, SqlGIMGroup> groupsData = getGroupData(conn);
                ImmutableSetMultimap<Integer, String> membersData = getGroupMembers(conn);
                Map<Integer, GIMScore> scoresData = getGroupScore(conn, membersData);
                ImmutableList<GIMGroup> rankings = getRankings(groupsData, membersData, scoresData);

                // Update all group scores.
                if (!rankings.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE gim_group_data SET group_score = ?, group_rank = ? WHERE group_id = ?;")) {
                        for (GIMGroup group : rankings) {
                            ps.setLong(1, group.getScore());
                            ps.setInt(2, group.getRank());
                            ps.setInt(3, group.getGroupId());
                            ps.addBatch();
                        }

                        if (ps.executeBatch().length < 1) {
                            Logger.getGlobal().warn("No rankings to update? D:");
                        }
                    }

                    // Build group data.
                    ImmutableMap.Builder<String, GIMGroup> groupsBuilder = ImmutableMap.builder();
                    for (GIMGroup group : rankings) {
                        groupsBuilder.put(group.getGroupKey(), group);
                    }
                    ImmutableMap<String, GIMGroup> groupData = groupsBuilder.build();

                    // Build group member data.
                    ImmutableMap.Builder<String, GIMGroup> groupsNameBuilder = ImmutableMap.builder();
                    for (GIMGroup group : groupData.values()) {
                        for (String member : group.getMembers()) {
                            groupsNameBuilder.put(member, group);
                        }
                    }
                    ImmutableMap<String, GIMGroup> groupMemberData = groupsNameBuilder.build();

                    // Cache relevant info.
                    GIM.setGroupData(groupData);
                    GIM.setGroupMemberData(groupMemberData);
                    GIM.setRankings(rankings);
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }

    }

    /**
     * Retrieves mappings of the group id to the group data.
     */
    private Map<Integer, SqlGIMGroup> getGroupData(Connection c) throws SQLException {
        Map<Integer, SqlGIMGroup> groups = new HashMap<>();
        try (PreparedStatement pst = c.prepareStatement("SELECT * FROM gim_group_data;");
             ResultSet results = pst.executeQuery()) {
            while (results.next()) {
                int groupId = results.getInt("group_id");
                String groupName = results.getString("group_name");
                String groupKey = results.getString("group_key");
                String groupLeader = results.getString("group_leader");
                Date creationDate = results.getDate("creation_date");
                groups.put(groupId, new SqlGIMGroup(groupId, groupName, groupKey, groupLeader, creationDate));
            }
        }
        return groups;
    }

    /**
     * Retrieves mappings of the group id to the list of group members.
     */
    private ImmutableSetMultimap<Integer, String> getGroupMembers(Connection c) throws SQLException {
        List<Pair<Integer, String>> members = new ArrayList<>(250);
        try (PreparedStatement pst = c.prepareStatement("SELECT group_id, member_name FROM gim_group_score;");
             ResultSet results = pst.executeQuery()) {
            while (results.next()) {
                int groupId = results.getInt("group_id");
                String memberName = results.getString("member_name");
                members.add(new Pair<>(groupId, memberName));
            }
        }

        ImmutableSetMultimap.Builder<Integer, String> membersBuilder = ImmutableSetMultimap.builder();
        for (val nextMember : members) {
            membersBuilder.put(nextMember.left, nextMember.right);
        }
        return membersBuilder.build();
    }

    /**
     * Retrieves mappings of the group id to the group score.
     */
    private Map<Integer, GIMScore> getGroupScore(Connection c, ImmutableSetMultimap<Integer, String> membersData) throws SQLException {
        Map<Integer, GIMScore> groupScores = new HashMap<>(100);
        try (PreparedStatement pst = c.prepareStatement("SELECT group_id, SUM(total_xp), SUM(total_deaths), SUM(total_boss_points), SUM(total_levels), SUM(prestiges) " +
                "FROM gim_group_score " +
                "GROUP BY group_id;");
             ResultSet results = pst.executeQuery()) {
            while (results.next()) {
                int groupId = results.getInt("group_id");
                long totalXp = results.getLong("SUM(total_xp)");
                int totalDeaths = results.getInt("SUM(total_deaths)");
                int totalBp = results.getInt("SUM(total_boss_points)");
                int totalLevels = results.getInt("SUM(total_levels)");
                int prestiges = results.getInt("SUM(prestiges)");
                groupScores.put(groupId, new GIMScore(groupId, totalXp, totalDeaths, totalBp, totalLevels, prestiges, membersData.keys().count(groupId)));
            }
        }
        return groupScores;
    }

    /**
     * Computes the rank of every group using the queried data.
     */
    private ImmutableList<GIMGroup> getRankings(Map<Integer, SqlGIMGroup> groups, ImmutableSetMultimap<Integer, String> members, Map<Integer, GIMScore> scores) {
        Comparator<GIMScore> scoreComparator = Collections.reverseOrder(new ScoreComparator());
        List<GIMScore> groupScores = new ArrayList<>(scores.values());
        groupScores.sort(scoreComparator);

        ImmutableList.Builder<GIMGroup> groupList = ImmutableList.builder();
        for (int index = 0; index < groupScores.size(); index++) {
            GIMScore score = groupScores.get(index);
            int groupId = score.getGroupId();
            SqlGIMGroup groupData = groups.get(groupId);
            groupList.add(new GIMGroup(groupId, groupData.name, groupData.key, groupData.leader,
                    members.get(groupId), scores.get(groupId), index + 1));
        }
        return groupList.build();
    }

    protected GIMHighscoresRefreshSql() {
    }
}
