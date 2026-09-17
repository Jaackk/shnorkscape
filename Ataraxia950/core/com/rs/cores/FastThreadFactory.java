package com.rs.cores;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread factory for spawning threads for the
 * fast executor service.
 * @author David O'Neill
 */
final class FastThreadFactory implements ThreadFactory {

    private final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final Thread.UncaughtExceptionHandler handler;

    FastThreadFactory(Thread.UncaughtExceptionHandler handler) {
        this.handler = handler;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "fastExecutor Thread Pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (thread.isDaemon())
            thread.setDaemon(false);
        if (thread.getPriority() != Thread.MIN_PRIORITY)
            thread.setPriority(Thread.MIN_PRIORITY);
        thread.setUncaughtExceptionHandler(handler);
        return thread;
    }
}
