package com.rs.utils;

import com.rs.game.tasks.WorldTasksManager;
import org.apache.logging.log4j.LogManager;

public final class Logger {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    public static void logBenchmark(long processTime) {
        logger.info("---DEBUG--- start");
        logger.info("WorldProcessTime: " + processTime);
        logger.info("WorldRunningTasks: " + WorldTasksManager.getTasksCount());
        logger.info("---DEBUG--- end");
    }

    public static org.apache.logging.log4j.Logger getGlobal() {
        return logger;
    }
}