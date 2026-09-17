package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.external.api.json.JsonParser;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.utils.DisplayNames;
import com.rs.utils.Utils;
import lombok.AllArgsConstructor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class StaffActivityMonitor implements Runnable {

    @AllArgsConstructor
    private static final class Data {
        private final LocalDateTime lastDate;
        private final Map<String, Duration> activity;
    }

    private static final int DAYS_TO_MONITOR = 1; // Monitors activity for <x> days before reporting.
    private static final boolean ACTIVE = !Settings.TEST_SERVER_MODE;
    private static final Path DATA_PATH = Paths.get("data", "activity_monitor.json");
    private static StaffActivityMonitor instance;
    private volatile LocalDateTime lastDate = LocalDateTime.now();
    private final Map<String, Duration> activity = new ConcurrentHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    public void load() {
        if (!ACTIVE)
            return;
        if (Files.exists(DATA_PATH)) {
            Data data = new JsonParser(DATA_PATH, Data.class).getFileLoaded();
            if (data == null)
                throw new IllegalStateException("No data in activity monitor file!");
            lastDate = data.lastDate;
            activity.putAll(data.activity);
        }
        CoresManager.getServiceProvider().scheduleRepeatingTask(this, 5, 15, TimeUnit.MINUTES);
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_PATH.toFile()))) {
            Utils.GSON.toJson(new Data(lastDate, activity), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTracker(Player player) {
        if (!player.onlineTracker.isRunning()) {
            player.onlineTracker.reset().start();
            throw new IllegalStateException("Online tracker is not running?");
        }

        if (player.isStaff() && player.onlineTracker.isRunning()) {
            Duration onlineDuration = player.onlineTracker.elapsed();
            Duration currentDuration = activity.getOrDefault(player.getUsername(), Duration.ZERO);
            activity.put(player.getUsername(), currentDuration.plus(onlineDuration));
            player.onlineTracker.reset().start();
        }
    }

    private void sendTrackedStats() {
        StringBuilder onlineData = new StringBuilder("Daily staff monitoring  ~ **" + formatter.format(lastDate) + "**\n\n");
        if (activity.isEmpty()) {
            onlineData.append("No staff have been online today! <:reeee:352490805535047683>");
        } else {
            Duration totalDuration = Duration.ZERO;
            for (Map.Entry<String, Duration> tracked : activity.entrySet()) {
                String username = tracked.getKey();
                Duration onlineDuration = tracked.getValue();
                String displayName = DisplayNames.getDisplayName(username);
                totalDuration = totalDuration.plus(onlineDuration);
                onlineData.append("**")
                        .append(displayName).
                        append('(').
                        append(username).
                        append(")** online for **").
                        append(Utils.prettyDuration(onlineDuration)).
                        append("**\n\n");
            }
            onlineData.append("Total staff time online ~ **").
                    append(Utils.prettyDuration(totalDuration)).
                    append("** <:smug:323836814379188225> \n");
        }

        //World.getDiscordBot().buildMessage(DiscordUtils.STAFF_ACTIVITY_CHANNEL, onlineData.toString()).complete();
    }

    @Override
    public void run() {
        if (lastDate == null) { // Shouldn't happen but just in case.
            lastDate = LocalDateTime.now();
            activity.clear();
            throw new IllegalStateException("Activity monitor isn't tracking a date!");
        }
        LocalDateTime finished = lastDate.plusDays(DAYS_TO_MONITOR);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(finished)) {
           sendTrackedStats();
            lastDate = now;
            activity.clear();
        } else {
            CoresManager.getServiceProvider().addGameTask(() -> {
                for (Player player : World.getPlayers()) {
                    if (player == null) {
                        continue;
                    }
                    saveTracker(player);
                }
            });
        }
        save();
    }

    public static StaffActivityMonitor getInstance() {
        if (instance == null) {
            instance = new StaffActivityMonitor();
        }
        return instance;
    }
}
