package com.rs.game.activites.gim.highscores;

import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Will send an asynchronous query to update the personal score of all GIM every 5 minutes.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMUpdateScoreTask extends WorldTask {

    /**
     * Updates a queue of scores in one query.
     */
    private static final class UpdateScoreBatchSql extends SQLRunnable {

        /**
         * The player's username.
         */
        private final Queue<BatchRequest> requests;

        /**
         * Creates a new {@link UpdateScoreBatchSql}.
         */
        public UpdateScoreBatchSql(Queue<BatchRequest> requests) {
            this.requests = requests;
        }

        @Override
        public void execute(DatabaseCredential auth) {
            try (Connection c = Pool.getConnection(auth, "ataraxia");
                 PreparedStatement updateScore = c.prepareStatement(GIMUpdateScoreSql.getUpdateStatement())) {
                int requestsCount = 0;
                for (; ; ) {
                    BatchRequest next = requests.poll();
                    if (next == null) {
                        break;
                    }
                    updateScore.setLong(1, next.getXpGained());
                    updateScore.setInt(2, next.getDeaths());
                    updateScore.setInt(3, next.getBpGained());
                    updateScore.setInt(4, next.getLevelsGained());
                    updateScore.setInt(5, next.getPrestigesGained());
                    updateScore.setString(6, next.getUsername());
                    updateScore.addBatch();
                    requestsCount++;
                }
                Logger.getGlobal().info("Starting score batch update for {} players.", requestsCount);
                updateScore.executeBatch();
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    /**
     * A class representing the data that will be updated.
     */
    @Data
    private static final class BatchRequest {
        private final String username;
        private final long xpGained;
        private final int deaths;
        private final int bpGained;
        private final int levelsGained;
        private final int prestigesGained;
    }

    /**
     * The queue of pending batch requests.
     */
    private final Queue<BatchRequest> requests = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
        for (Player next : World.getPlayers()) {
            if (next != null && next.isGroupIronman()) {
                GIMScoreTracker tracker = next.gimTracker;
                if (!tracker.canUpdate()) {
                    continue;
                }
                requests.add(new BatchRequest(next.getUsername(), tracker.getXpGained(), tracker.getDeaths(), tracker.getBpGained(), tracker.getLevelsGained(), tracker.getPrestiges()));
                tracker.reset();
            }
        }
        CoresManager.getServiceProvider().executeNow(new UpdateScoreBatchSql(requests));
    }
}
