package com.rs.utils;

import com.rs.game.player.Player;

import lombok.Getter;
import lombok.Setter;

public abstract class DropDownMenuEvent {
    @Getter
    @Setter
    private int slotId;

    public abstract void run(Player player);
}
