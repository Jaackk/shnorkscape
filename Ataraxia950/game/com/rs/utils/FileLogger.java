package com.rs.utils;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.World;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An asynchronous file logger.
 *
 * @author lare96
 */
public final class FileLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("LLLL dd, uuuu | hh:mm a");
    private final Path path;

    public FileLogger(String fileName) {
        path = Paths.get("data/playersaves/logs/", fileName);
    }

    public void logMessage(String str) {
        logMessage(str, false);
    }

    public void logMessage(String str, boolean sendToStaff) {
        if (sendToStaff && !Settings.TEST_SERVER_MODE) {
            World.sendWorldMessage(str, true);
        }
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                File writeFile = path.toFile();
                if(!writeFile.exists()) {
                    writeFile.createNewFile();
                }
                String timestamp = FORMATTER.format(LocalDateTime.now());
                try (BufferedWriter fw = new BufferedWriter(new FileWriter(writeFile, true))) {
                    fw.write('[');
                    fw.write(timestamp);
                    fw.write("]: ");
                    fw.write(str);
                    fw.newLine();
                }
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        });
    }
}
