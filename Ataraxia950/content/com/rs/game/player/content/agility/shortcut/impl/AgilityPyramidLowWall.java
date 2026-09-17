package com.rs.game.player.content.agility.shortcut.impl;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.agility.shortcut.Shortcut;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class AgilityPyramidLowWall implements Shortcut {

	private static final WorldTile START = new WorldTile(3392, 2818, 1);
	private static final WorldTile END = new WorldTile(3390, 2818, 1);
	
	private static final Animation CLIMB = new Animation(1252);
	
	@Override
	public void succeed(Player player, boolean start) {
		player.lock();
		final boolean run = player.getRun();
		player.setRunHidden(false);
		player.setNextFaceWorldTile(start ? END : START);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 0) {
					player.setNextAnimation(CLIMB);
				} else if (ticks == 1) {
					player.setNextForceMovement(new ForceMovement(start ? END : START, 1, start ? ForceMovement.WEST : ForceMovement.EAST));
				} else if (ticks == 2) {
					player.setNextWorldTile(start ? END : START);
					player.setRunHidden(run);
				} else if (ticks == 3) {
					player.unlock();
					stop();
				}
				ticks++;
			}
		}, 0, 0);
	}

	@Override
	public int getLevel() {
		return 1;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 28512 };
	}

	@Override
	public WorldTile getStart() {
		return START;
	}

	@Override
	public WorldTile getEnd() {
		return END;
	}

}
