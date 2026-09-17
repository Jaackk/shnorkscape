package com.rs.cores;

import com.rs.utils.Logger;

/**
 * @author David O'Neill
 */
final class FastThreadHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Logger.getGlobal().error("(" + thread.getName() + ", fast pool) - Printing trace", throwable);
    }

}
