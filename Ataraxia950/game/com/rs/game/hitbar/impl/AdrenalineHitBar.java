package com.rs.game.hitbar.impl;

import com.rs.game.hitbar.HitBar;
import com.rs.game.player.Player;

public class AdrenalineHitBar extends HitBar {

	public AdrenalineHitBar(final int amount) {
		this.amount = amount;
	}

	private final int amount;

	@Override
	public int getPercentage() {
		return amount * 255 / 100;
	}

	@Override
	public int getType() {
		return 7;
	}

	@Override
	public boolean display(Player player) {
		return true;
	}
}
