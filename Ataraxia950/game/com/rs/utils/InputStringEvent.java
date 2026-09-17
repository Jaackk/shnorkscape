package com.rs.utils;

import com.rs.game.player.Player;

public abstract class InputStringEvent {

    private String string;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public abstract void run(Player player);
}
