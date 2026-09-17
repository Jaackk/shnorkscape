package com.rs.utils;

import com.rs.game.player.Player;

public abstract class DialogueOptionEvent {

    public static final int OPTION_1 = 8, OPTION_2 = 13, OPTION_3 = 18, OPTION_4 = 23, OPTION_5 = 28;

    private int option;

    public abstract void run(Player player);

    public int getOption() {
        return option;
    }

    public void setOption(int integer) {
        this.option = integer;
    }
}

