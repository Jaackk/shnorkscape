package com.rs.utils.mysql;

import com.rs.Settings;
import com.rs.utils.Logger;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class QueryExecutor {

    private static final Queue<SQLRunnable> QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * Queries dropped because {@link Settings#SQL_ENABLED} is false. Only
     * {@code SQLThread} drains the queue and it is only started when SQL is
     * enabled, so with SQL disabled every submit used to accumulate forever
     * (hiscores on logout, News on drops, trade logs, ...). The 947 JVM runs
     * without a database, so those submits are counted and dropped instead.
     */
    private static final AtomicLong DROPPED = new AtomicLong();

    static void process() {
        if (QUEUE.isEmpty())
            return;
        Iterator<SQLRunnable> i = QUEUE.iterator();
        while (i.hasNext()) {
            SQLRunnable entry = i.next();
            try {
                entry.run();
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
            i.remove();
        }
    }

    public static void submit(SQLRunnable query) {
        if (query == null)
            return;
        if (!Settings.SQL_ENABLED) {
            long dropped = DROPPED.incrementAndGet();
            if (dropped == 1 || dropped % 1000 == 0)
                Logger.getGlobal().info("SQL disabled: dropped " + dropped + " queued queries (last "
                        + query.getClass().getSimpleName() + ")");
            return;
        }
        QUEUE.add(query);
    }

    /** Number of queries dropped because SQL is disabled. */
    public static long getDroppedCount() {
        return DROPPED.get();
    }

    /** Number of queries waiting for the SQL thread. */
    public static int getQueuedCount() {
        return QUEUE.size();
    }

}
