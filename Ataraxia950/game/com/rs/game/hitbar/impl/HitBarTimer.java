package com.rs.game.hitbar.impl;

import com.rs.game.hitbar.HitBar;
import com.rs.game.player.Player;

public class HitBarTimer extends HitBar {
	
	private final int remaining;
	private boolean display;

	public HitBarTimer(int remaining) {
		this.remaining = remaining;
	}
	
	public void setDisplay(final boolean val) {
		this.display = val;
	}

	@Override
	public int getPercentage() {
		return remaining * 255 / 100;
	}

	@Override
	public int getType() {
		return 5;
	}
	
	@Override
	public int getDelay() {
		return remaining;
	}

	// 0, 3, 4, 7
	@Override
	public boolean display(Player player) {
		return !display;
	}
}
