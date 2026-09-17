package com.rs.game.activites.gim.event;

import java.time.LocalDate;

/**
 * A class representing an event for GIM players.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMEvent {

    /**
     * The event type.
     */
    private final GIMEventType type;

    /**
     * The event end date.
     */
    private final LocalDate endDate;

    /**
     * Creates a new {@link GIMEvent}.
     */
    public GIMEvent(GIMEventType type, LocalDate endDate) {
        this.type = type;
        this.endDate = endDate;
    }

    public GIMEventType getType() {
        return type;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
