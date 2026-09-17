package com.rs.game.activites.gim.event;

/**
 * The event type.
 */
public enum GIMEventType {
    XP("2x experience"),
    BOSS_POINTS("2x boss points");

    /**
     * The description of the event.
     */
    private final String description;

    /**
     * Creates a new {@link GIMEventType}.
     */
    GIMEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
