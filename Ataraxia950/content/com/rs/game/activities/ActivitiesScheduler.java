package com.rs.game.activities;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.external.api.json.JsonParser;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.TimeUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.val;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Log
public class ActivitiesScheduler {
    private static final String EVENT_CHANNEL_ID = "488718915522789403";
    private static final int HOURS_BEFORE_FOR_VISIBILITY = 24;
    @Getter(lazy = true)
    private static final ActivitiesScheduler instance = new ActivitiesScheduler();

    private Object2ObjectLinkedOpenHashMap<Activity, FixedLengthRunnable> taskForActivities;

    private Activity[] loadFile() {
        JsonParser parser = new JsonParser("data/activities.json", Activity[].class);
        return parser.getFileLoaded();
    }

    public void sendScheduleInterface(Player player) {
        val interfaceId = 227;
        val titleComponentId = 2;
        val threadComponentId = 3;
        val startingListComponentId = 7;
        val endingListComponentId = 53;

        player.interfaceManager.sendInterface(interfaceId);
        player.getPackets().sendIComponentText(interfaceId, titleComponentId, "Upcoming " + Settings.SERVER_NAME + " Events");
        player.getPackets().sendIComponentText(interfaceId, threadComponentId, "Do ;;calendar to see a calendar view with");
        player.getPackets().sendIComponentText(interfaceId, threadComponentId + 2, "timezones adjusted to yours!");
        player.getPackets().sendIComponentText(interfaceId, threadComponentId + 3, "");

        int componentId = startingListComponentId;
        for (Activity activity : taskForActivities.keySet()) {
            player.getPackets().sendIComponentText(interfaceId, componentId++, activity.toString());
        }
        for (int i = componentId; i <= endingListComponentId; i++) {
            player.getPackets().sendIComponentText(interfaceId, componentId++, "");
        }
    }
    
    public void load() {
        if (taskForActivities != null && !taskForActivities.isEmpty()) {
            cancelTasks();
        }
        taskForActivities = new Object2ObjectLinkedOpenHashMap<>();
        scheduleTasks(loadFile());
    }

    private void cancelTasks() {
        for (FixedLengthRunnable task : taskForActivities.values()) {
            task.stopNow(true);
        }
    }

    private void scheduleTasks(Activity[] activities) {
        for (Activity activity : activities) {
            val dateTime = activity.getDate().asLocalDateTime();
            val timeDifference = TimeUtils.getTimeDifferenceMillis(LocalDateTime.now(), dateTime);
            if (timeDifference < 0) {
                continue;
            }
            val runnable = new FixedLengthRunnable() {
                @Override
                public boolean repeat() {
                    announceActivity(activity);
                    taskForActivities.remove(activity);
                    return false;
                }
            };
            taskForActivities.put(activity, runnable);
            CoresManager.getServiceProvider().executeWithDelay(runnable, timeDifference, TimeUnit.MILLISECONDS);
        }
    }

    private void announceActivity(Activity activity) {
        World.sendWorldMessage("<img=7><col=E0246F>" + Settings.SERVER_NAME + " Events: " + activity.getAnnouncement(), false);
        log.info("Sent an activity announcement! Event: " + activity.getEventName());
    }

    public String createTaskTabString() {
        StringBuilder taskTabStringBuilder = new StringBuilder();
        taskForActivities.forEach((activity, task) -> {
            val timeNow = LocalDateTime.now();
            val timeTill = activity.getDate().asLocalDateTime();
            val hoursTimeDifference = TimeUtils.getTimeDifference(ChronoUnit.HOURS, timeNow, timeTill);
            if (hoursTimeDifference <= HOURS_BEFORE_FOR_VISIBILITY) {
                taskTabStringBuilder.append("- ").append(Colors.WHITE).append(activity.getEventName()).append(": ")
                        .append(Colors.GREEN).append(TimeUtils.getHHMMSSTime(timeNow, timeTill)).append("<br>");
            }
        });
        return taskTabStringBuilder.toString();
    }

    public void openCalendarLink(Player player) {
        val url = "https://forum.ataraxia-ps.com/calendar/";
        player.getPackets().sendOpenURL(url);
    }
}