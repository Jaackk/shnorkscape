package com.rs.utils.mysql;

import com.google.common.base.Stopwatch;
import com.rs.game.activites.gim.highscores.GIMHighscoresRefreshSql;
import com.rs.utils.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class Pool {

    public final static boolean DEBUG = true;
    public final static boolean FAILOVER = false;

    public final static HashMap<DatabaseCredential, HashMap<String, HikariDataSource>> pools = new HashMap<>();
    public final static DatabaseTopology TOPOLOGY = DatabaseTopology.MAIN;

    public final int MINIMUM_DATABASE_CONNECTIONS = 1;
    public final int MAXIMUM_DATABASE_CONNECTIONS = 4;

    public Pool() {
        for (DatabaseCredential auth : TOPOLOGY.getNodes())
            pools.put(auth, new HashMap<>());

        for (Database database : Database.databases.values()) {
            final DatabaseDetails details = database.getDetails();
            if (!details.getAuth().isConfigured()) continue;
            final HikariConfig config = new HikariConfig();
            config.setPoolName("main");
            config.setMinimumIdle(MINIMUM_DATABASE_CONNECTIONS);
            config.setMaximumPoolSize(MAXIMUM_DATABASE_CONNECTIONS);
            config.setJdbcUrl("jdbc:mysql://" + details.getAuth().getHost() + ":3306/" + details.getDatabase()
                    + "?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
            config.setUsername(details.getAuth().getUser());
            config.setPassword(details.getAuth().getPass());
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("useServerPrepStmts", true);
            config.addDataSourceProperty("prepStmtCacheSize", 256);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            pools.get(database.getDetails().getAuth()).put(database.getDetails().getDatabase(),
                    new HikariDataSource(config));
        }
    }

    public static Connection getConnection(final DatabaseCredential auth, final String database) throws SQLException {
        return getPool(auth, database).getConnection();
    }

    public static HikariDataSource getPool(final DatabaseCredential auth, final String name) {
        return pools.get(auth).get(name);
    }

    public static void submit(SQLRunnable query) {
        long start = 0, end = 0;

        if (query == null)
            return;

        Stopwatch benchmark = Stopwatch.createStarted();
        for (DatabaseCredential auth : TOPOLOGY.getNodes()) {
            try {
                query.execute(auth);
                long duration = benchmark.elapsed().toMillis();
                String message = "Query [" + query.getClass().getSimpleName() + "] took approximately "
                        + duration + "ms to execute.";
                if (duration < 2500) {
                    Logger.getGlobal().info(message);
                } else {
                    if (query.getClass() == GIMHighscoresRefreshSql.class) {
                        if (duration < 15_000) {
                            Logger.getGlobal().info(message);
                        } else {
                            Logger.getGlobal().warn(message);
                        }
                    } else {
                        Logger.getGlobal().warn(message);
                    }
                }
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    public static void preload() {
        external:
        for (final DatabaseDetails entry : DatabaseDetails.VALUES) {
            internal:
            for (final DatabaseCredential auth : Pool.TOPOLOGY.getNodes()) {
                if (auth != entry.getAuth())
                    continue external;

                if (auth.isConfigured() && entry.getDatabase() != null) {
                    Database.databases.put(entry.getDatabase(), new Database(entry));
                }
            }
        }
    }
}
