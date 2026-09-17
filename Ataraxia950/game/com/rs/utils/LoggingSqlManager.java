package com.rs.utils;

import com.rs.ServerLauncher;
import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.item.Item;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The logging SQL manager. Uses its own dedicated thread as to not flood {@link CoresManager}.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class LoggingSqlManager extends Thread {

    @AllArgsConstructor
    public static final class GrandExchangeLogSql extends SQLRunnable {

        public final String username;
        public final String displayName;
        public final String ip;
        public final String itemName;
        public final int itemAmount;
        public final LocalDateTime timestamp = LocalDateTime.now();

        @Override
        public void execute(DatabaseCredential auth) {
            try (Connection c = Pool.getConnection(auth, "logs");
                 PreparedStatement statement = c.prepareStatement("INSERT INTO grand_exchange(username,display_name,ip,item_name,item_amount,timestamp) VALUES(?,?,?,?,?,?);")) {
                statement.setString(1, username);
                statement.setString(2, displayName);
                statement.setString(3, ip);
                statement.setString(4, itemName);
                statement.setInt(5, itemAmount);
                statement.setTimestamp(6, Timestamp.valueOf(timestamp));
                if (statement.executeUpdate() < 1) {
                    throw new IllegalStateException("Did not complete successfully!");
                }
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    @AllArgsConstructor
    public static final class DropPickupLogSql extends SQLRunnable {

        public final String username;
        public final String displayName;
        public final String ip;
        public final String itemName;
        public final int itemAmount;
        public final int x;
        public final int y;
        public final int z;
        public final boolean drop;
        public final LocalDateTime timestamp = LocalDateTime.now();

        @Override
        public void execute(DatabaseCredential auth) {
            try (Connection c = Pool.getConnection(auth, "logs");
                 PreparedStatement statement = c.prepareStatement(getStatement())) {
                statement.setString(1, username);
                statement.setString(2, displayName);
                statement.setString(3, ip);
                statement.setString(4, itemName);
                statement.setInt(5, itemAmount);
                statement.setInt(6, x);
                statement.setInt(7, y);
                statement.setInt(8, z);
                statement.setTimestamp(9, Timestamp.valueOf(timestamp));
                if (statement.executeUpdate() < 1) {
                    throw new IllegalStateException("Did not complete successfully!");
                }
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }

        private String getStatement() {
            return drop ? "INSERT INTO `drop`(username,display_name,ip,item_name,item_amount,x,y,z,timestamp) VALUES(?,?,?,?,?,?,?,?,?);" :
                    "INSERT INTO pickup(username,display_name,ip,item_name,item_amount,x,y,z,timestamp) VALUES(?,?,?,?,?,?,?,?,?);";
        }
    }

    @AllArgsConstructor
    public static final class TradeLogSql extends SQLRunnable {
        public final String username;
        public final String displayName;
        public final String ip;
        public final String targetUsername;
        public final String targetDisplayName;
        public final String targetIP;
        public final List<Item> gaveItems;
        public final LocalDateTime timestamp = LocalDateTime.now();

        @Override
        public void execute(DatabaseCredential auth) {
            try (Connection c = Pool.getConnection(auth, "logs");
                 PreparedStatement statement = c.prepareStatement(
                         "INSERT INTO trade(username,display_name,ip,target_username,target_display_name,target_ip,gave_items,timestamp) " +
                                 "VALUES (?,?,?,?,?,?,?,?);")) {
                statement.setString(1, username);
                statement.setString(2, displayName);
                statement.setString(3, ip);
                statement.setString(4, targetUsername);
                statement.setString(5, targetDisplayName);
                statement.setString(6, targetIP);
                statement.setString(7, itemsToText(gaveItems));
                statement.setTimestamp(8, Timestamp.valueOf(timestamp));
                if (statement.executeUpdate() < 1) {
                    throw new IllegalStateException("Did not complete successfully!");
                }
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }

        private String itemsToText(List<Item> items) {
            StringBuilder sb = new StringBuilder();
            for (Item next : items) {
                if (next == null) {
                    continue;
                }
                sb.append(next.getName()).append(" (x").append(Utils.formatPrice(next.getAmount())).append(")\n");
            }
            return sb.toString();
        }
    }


    private volatile boolean running = true;
    private static final LoggingSqlManager instance = new LoggingSqlManager();

    public static LoggingSqlManager getInstance() {
        return instance;
    }

    private final BlockingQueue<SQLRunnable> loggingTasks = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        while (running) {
            try {
                loggingTasks.take().prepare();
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    public void queueTask(SQLRunnable task) {
        loggingTasks.add(task);
    }

    public void terminate() {
        running = false;
    }
}
