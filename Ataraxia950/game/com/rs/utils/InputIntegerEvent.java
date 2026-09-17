package com.rs.utils;

import com.rs.game.player.Player;

public abstract class InputIntegerEvent {

    private int integer;

	public int getInteger() {
		return integer;
	}
	
	public void setInteger(int i) {
		this.integer = i;
	}

    public abstract void run(Player player);
}
