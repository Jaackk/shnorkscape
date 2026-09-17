package com.rs.game.activities.seasonalevents;

import com.rs.cores.CoresManager;
import com.rs.game.tasks.WorldTask;
import lombok.val;

import java.time.LocalDate;

/**
 * The task that will check periodically to start and end events.
 *
 * @author lare96
 */
public final class SeasonalEventTask extends WorldTask {

    @Override
    public void run() {
        val now = LocalDate.now();
        for (SeasonalEvent event : SeasonalEventManager.getAll()) {
            boolean withinEventPeriod = event.getPeriod().isWithin(now);
            if (!event.isActive() && withinEventPeriod) {
                event.setActive(true);
                event.doStart();
                CoresManager.getServiceProvider().executeNow(() -> {
                    try {
                        event.asyncStart();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else if (event.isActive() && !withinEventPeriod) {
                event.setActive(false);
                event.doEnd();
                CoresManager.getServiceProvider().executeNow(() -> {
                    try {
                        event.asyncEnd();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
