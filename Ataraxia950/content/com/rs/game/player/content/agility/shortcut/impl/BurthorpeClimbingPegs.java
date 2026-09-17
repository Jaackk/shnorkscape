package com.rs.game.player.content.agility.shortcut.impl;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.agility.shortcut.Shortcut;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris {@link https://www.rune-server.ee/members/kris/}
 * Date: 23rd August 2017.
 */
public final class BurthorpeClimbingPegs implements Shortcut {
	
	private static final WorldTile START = new WorldTile(2934, 3559, 0);
	private static final WorldTile END = new WorldTile(2939, 3559, 0);
	private static final Animation DOWN = new Animation(3463),
			UP = new Animation(3464);
	
	@Override
	public int getLevel() {
		return 1;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 34904 };
	}

	@Override
	public WorldTile getStart() {
		return START;
	}

	@Override
	public WorldTile getEnd() {
		return END;
	}

	@Override
	public void succeed(Player player, boolean forward) {
		player.lock();
		final boolean run = player.getRun();
		player.setRunHidden(false);
		player.setNextFaceWorldTile(forward ? getEnd() : getStart());
		player.setNextAnimation(forward ? DOWN : UP);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getSkills().addXp(Skills.AGILITY, 2);
				player.setNextWorldTile(forward ? END : START);
				player.setRunHidden(run);
				player.unlock();
			}
		}, 5);
	}

	
}
