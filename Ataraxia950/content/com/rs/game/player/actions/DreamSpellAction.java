package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Player;

public class DreamSpellAction extends Action {

	private boolean doneCycle;

	@Override
	public boolean start(Player player) {
		if (!process(player))
			return false;
		player.setNextAnimation(new Animation(6295, -1, -1, -1, -1, 0));
		setActionDelay(player, 6);
		return true;
	}

	@Override
	public boolean process(Player player) {
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		if (!doneCycle) {
			doneCycle = !doneCycle;
			player.setResting(true);
		}
		player.setNextAnimation(new Animation(6296));
		player.setNextGraphics(new Graphics(277, 0, 0));
		return 3;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
		if (player.hasWalkSteps())
			player.resetWalkSteps();
		player.lock(2);
		player.setNextAnimation(new Animation(6297));
		player.setResting(false);
	}
}
