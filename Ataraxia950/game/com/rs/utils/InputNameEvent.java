package com.rs.utils;

import com.rs.game.player.Player;

/**
 * @author Unknown
 * Ported over from old source, David O'Neill
 */
public abstract class InputNameEvent {

    private String string;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public abstract void run(Player player);
}
