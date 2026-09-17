package com.rs.game.activities.seasonalevents;

import com.rs.cores.CoresManager;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.val;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches the collection of seasonal events.
 *
 * @author lare96
 */
public final class SeasonalEventManager {

    private static final boolean ACTIVE = true;
    private static final int CYCLE_RATE = 6000;
    private static final Map<Class<? extends SeasonalEvent>, SeasonalEvent> eventMap = new HashMap<>();

    public static void init() {
        if (!ACTIVE) {
            return;
        }
        FastClasspathScanner scanner = new FastClasspathScanner("com.rs.game.activities.seasonalevents");
        scanner.matchSubclassesOf(SeasonalEvent.class, eventClass -> {
            try {
                val eventInstance = eventClass.getDeclaredConstructor().newInstance();
                eventMap.put(eventClass, eventInstance);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                Logger.getGlobal().catching(e);
            }
        });
        scanner.scan();
        WorldTasksManager.schedule(new SeasonalEventTask(), 1, CYCLE_RATE);
    }

    public static boolean isActive(Class<? extends SeasonalEvent> type) {
        val eventInstance = eventMap.get(type);
        if (eventInstance != null) {
            return eventInstance.isActive();
        }
        return false;
    }

    public static SeasonalEvent get(Class<? extends SeasonalEvent> type) {
        return eventMap.get(type);
    }

    static Collection<SeasonalEvent> getAll() {
        return eventMap.values();
    }
}
