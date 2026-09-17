package com.rs.cores;

import com.rs.utils.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * A subtyped {@link ScheduledThreadPoolExecutor} with error logging
 * cabailities.
 *
 * @author David O'Neill
 */
final class SlowThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    /**
     * Construct a {@link SlowThreadPoolExecutor} object backed
     * by a {@link SlowThreadFactory}.
     *
     * @param corePoolSize the number of threads to hold in the pool
     * @param threadFactory the {@code ThreadFactory}
     */
    SlowThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        Logger.getGlobal().info("SlowThreadPoolExecutor open. Fixed thread pool size: " + corePoolSize);
    }

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
            Logger.getGlobal().error("SlowThreadPoolExecutor caught an exception.", t);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        Logger.getGlobal().info("SlowThreadPoolExecutor closing. No longer queueing tasks.");
    }
}
