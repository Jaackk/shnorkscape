package com.rs.game.player.actions.hunter;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public final class JadinkoCatch implements Serializable {
    private static final long serialVersionUID = 4252804348113467140L;
    private final TrapAction.HunterNPC hunterNPC;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public boolean expired() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(timestamp.plusHours(4));
    }
}
