package com.rs.game.activities.seasonalevents;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.npc.NPC;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstraction model for seasonal events. Use this model to automatically run special events that only take place
 * between a specific time period.
 * <p>
 * Subclasses must have a no-args constructor and must be declared in the implementation package in order to be loaded
 * by the {@link SeasonalEventManager}.
 * <p>
 * All data related to these seasonal events should be saved in a database, as events are subject to change at any time.
 * This relates to the aim of reducing clutter and unused fields in the Player class.
 *
 * @author lare96
 */
public abstract class SeasonalEvent {

    protected static final Path SAVE_DIR = Paths.get("data", "seasonal_events");
    protected static Path getPath(String... paths) {
        Path path = SAVE_DIR;
        for(String p : paths) {
            path = path.resolve(p);
        }
        return path;
    }

    /**
     * The object list.
     */
    protected final List<WorldObject> objects = new ArrayList<>();

    /**
     * The NPC list.
     */
    protected final List<NPC> npcs = new ArrayList<>();

    /**
     * If this event is currently active.
     */
    private boolean active;

    /**
     * The event season dates.
     */
    private SeasonalEventPeriod period;

    public SeasonalEvent() {
    }

    /**
     * Start the event: Spawn any NPCs required, schedule tasks, etc.
     */
    protected abstract void start();

    /**
     * Exactly the same as {@link #start()} except its ran in a CoresManager thread.
     */
    public void asyncStart() throws Exception {

    }

    /**
     * Stop the event: Despawn any NPCs, cancel tasks, etc.
     */
    protected abstract void end();

    /**
     * Exactly the same as {@link #end()} except its ran in a CoresManager thread.
     */
    public void asyncEnd() throws Exception{

    }

    /**
     * Computes the event period (at what point in a year the event will take place). Retrieve using {@link #getPeriod()}.
     */
    protected abstract SeasonalEventPeriod computePeriod();

    public void doStart() {
        start();
        objects.forEach(World::spawnObject);
    }

    public void doEnd() {
        end();
        objects.forEach(World::removeObject);
        npcs.forEach(World::removeNPC);
    }

    public final boolean isActive() {
        return active;
    }

    public final void setActive(boolean active) {
        this.active = active;
    }

    public final SeasonalEventPeriod getPeriod() {
        if(period == null) {
            period = computePeriod();
        }
        return period;
    }
}
