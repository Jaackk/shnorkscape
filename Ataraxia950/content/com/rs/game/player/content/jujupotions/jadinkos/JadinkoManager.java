package com.rs.game.player.content.jujupotions.jadinkos;

import com.google.common.collect.ImmutableList;
import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.utils.Colors;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class JadinkoManager {


    private static final class JadinkoManagerTask implements Runnable {

        @Getter
        @Setter
        private int hoursRemaining;

        @Getter
        private volatile boolean godJadinkosAvailable;

        @Getter
        @Setter
        private volatile boolean weekendMode;

        @Override
        public void run() {
            DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
            if (currentDay == DayOfWeek.SATURDAY ||
                    currentDay == DayOfWeek.SUNDAY) {
                CoresManager.getServiceProvider().addGameTask(() ->
                        World.sendWorldMessage(Colors.DARK_GREEN + "<img=6>God Jadinkos are always available on weekends! Talk to the Witch Doctor at ;;home to access them.", false));
                godJadinkosAvailable = true;
                weekendMode = true;
                return;
            }
            if (weekendMode) {
                hoursRemaining = ThreadLocalRandom.current().nextInt(HOURS_MIN_DISABLE, HOURS_MAX_DISABLE);
                weekendMode = false;
                godJadinkosAvailable = false;
                return;
            }
            if (!godJadinkosAvailable) {
                onDisabled();
            } else {
                onEnabled();
            }
        }

        private void onEnabled() {
            hoursRemaining--;
            if (hoursRemaining == 1) {
                CoresManager.getServiceProvider().addGameTask(() ->
                        World.sendWorldMessage(Colors.RED + "<img=6>God Jadinkos will go back into hiding in about an hour!", false));
            } else if (hoursRemaining <= 0) {
                hoursRemaining = ThreadLocalRandom.current().nextInt(HOURS_MIN_DISABLE, HOURS_MAX_DISABLE);
                godJadinkosAvailable = false;
                CoresManager.getServiceProvider().addGameTask(() -> {
                    godInstance.kickAll();
                    World.sendWorldMessage(Colors.RED + "<img=6>God Jadinkos have went back into hiding!", false);
                });
            }
        }

        private void onDisabled() {
            if (--hoursRemaining <= 0) {
                hoursRemaining = ThreadLocalRandom.current().nextInt(HOURS_MIN_ENABLE, HOURS_MAX_ENABLE);
                godJadinkosAvailable = true;
                CoresManager.getServiceProvider().addGameTask(() ->
                        World.sendWorldMessage(Colors.DARK_GREEN + "<img=6>God Jadinkos are now available for " + hoursRemaining + " hours! Talk to the Witch Doctor at ;;home to access them.", false));
            } else {
                CoresManager.getServiceProvider().addGameTask(() -> {
                    String hour = hoursRemaining > 1 ? "hours" : "hour";
                    World.sendWorldMessage(Colors.DARK_GREEN + "<img=6>God Jadinkos will be back in " + hoursRemaining + " " + hour + ".", false);
                });
            }
        }
    }

    private static final int HOURS_MIN_ENABLE = 3;
    private static final int HOURS_MAX_ENABLE = 6;
    private static final int HOURS_MIN_DISABLE = 6;
    private static final int HOURS_MAX_DISABLE = 12;
    public static final JadinkoInstance commonInstance = new JadinkoInstance(335, 469, new WorldTile(2719, 3795, 0), ImmutableList.of(
            new JadinkoSpawn(13119, 2731, 3786, 7, 7),
            new JadinkoSpawn(13143, 2730, 3775, 7, 7),
            new JadinkoSpawn(13155, 2714, 3775, 7, 7),
            new JadinkoSpawn(13142, 2720, 3787, 7, 7),
            new JadinkoSpawn(13130, 2702, 3790, 7, 7),
            new JadinkoSpawn(13154, 2730, 3775, 7, 7),
            new JadinkoSpawn(13796, 2714, 3775, 7, 7)
    ));
    public static final JadinkoInstance godInstance = new JadinkoInstance(170, 738, new WorldTile(1376, 5911, 0), ImmutableList.of(
            new JadinkoSpawn(13163, 1375, 5919, 9, 7),
            new JadinkoSpawn(13164, 1375, 5919, 9, 7),
            new JadinkoSpawn(13165, 1375, 5919, 9, 7)
    ));
    private static final JadinkoManagerTask jadinkoTask = new JadinkoManagerTask();

    public static void start() {
        commonInstance.load();
        godInstance.load();
        boolean available = ThreadLocalRandom.current().nextBoolean();
        jadinkoTask.godJadinkosAvailable = available;
        jadinkoTask.hoursRemaining = ThreadLocalRandom.current().nextInt(available ? HOURS_MIN_ENABLE : HOURS_MIN_DISABLE,
                available ? HOURS_MAX_ENABLE : HOURS_MAX_DISABLE);
        CoresManager.getServiceProvider().scheduleRepeatingTask(jadinkoTask,
                Settings.TEST_SERVER_MODE ? 100 : 3600,
                Settings.TEST_SERVER_MODE ? 100 : 3600);
    }

    public static boolean isGodActive() {
        return jadinkoTask.godJadinkosAvailable;
    }

    public static String getGodTimeRemaining(boolean verbose) {
        int hours = jadinkoTask.hoursRemaining;
        if (jadinkoTask.weekendMode) {
            return "Weekend";
        } else if (hours > 1) {
            return hours + (verbose ? " hours" : "h");
        } else {
            return hours + (verbose ? " hour" : "h");
        }
    }

    public static String getStatus() {
        if (jadinkoTask.godJadinkosAvailable) {
            return Colors.GREEN + "Active (" + getGodTimeRemaining(false) + ")";
        } else {
            return Colors.RED + "Inactive (" + getGodTimeRemaining(false) + ")";
        }
    }
}
