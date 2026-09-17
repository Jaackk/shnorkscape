package com.rs.game.player.content.agility.shortcut.impl;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
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
public class FaladorCrumblingWall implements Shortcut {

	private static final WorldTile START = new WorldTile(2934, 3355, 0);
	private static final WorldTile END = new WorldTile(2936, 3355, 0);
	private static final WorldTile MIDDLE = new WorldTile(2935, 3355, 0);
	
	private static final Animation CLIMB_START = new Animation(12915),
			CLIMB_END = new Animation(12916);
	
	@Override
	public void succeed(Player player, boolean start) {
		player.lock();
		final boolean run = player.getRun();
		player.setRunHidden(false);
		player.setNextFaceWorldTile(start ? END : START);
		if (start)
			player.addWalkSteps(MIDDLE.getX(), MIDDLE.getY(), -1, true);
		else
			player.setNextAnimation(CLIMB_END);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 0) {
					if (!start) {
						player.setNextForceMovement(new ForceMovement(start ? END : MIDDLE, 1, start ? ForceMovement.EAST : ForceMovement.WEST));
						ticks++;
					} else {
						player.setNextAnimation(CLIMB_START);
						player.setNextForceMovement(new ForceMovement(start ? END : MIDDLE, 2, start ? ForceMovement.EAST : ForceMovement.WEST));
					}
				} else if (ticks == 2 && !start) {
					player.setNextWorldTile(MIDDLE);
				} else if (ticks == 3) {
					if (!start)
						player.addWalkSteps(START.getX(), START.getY(), -1, false);
					player.getSkills().addXp(Skills.AGILITY, 0.5);
					if (start)
						player.setNextWorldTile(END);
					player.setRunHidden(run);
				} else if (ticks == 4) {
					player.unlock();
					stop();
				}
				ticks++;
			}
		}, 0, 0);
	}

	@Override
	public int getLevel() {
		return 5;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 11844 };
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
