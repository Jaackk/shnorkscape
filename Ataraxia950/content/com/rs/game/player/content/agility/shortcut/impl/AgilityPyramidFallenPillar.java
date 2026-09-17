package com.rs.game.player.content.agility.shortcut.impl;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.agility.shortcut.Shortcut;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris {@link https://www.rune-server.ee/members/kris/}
 * Date: 23rd August 2017.
 */
public final class AgilityPyramidFallenPillar implements Shortcut {
	
	private static final WorldTile START = new WorldTile(3420, 2801, 0);
	private static final WorldTile END = new WorldTile(3420, 2803, 1);
	private static final Animation CLIMB = new Animation(7640);
	
	@Override
	public int getLevel() {
		return 1;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 28515, 28516 };
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
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 0) {
					player.setNextAnimation(CLIMB);
					player.sendMessage("You squeeze through the narrow tunnel.", true);
					player.setNextForceMovement(new ForceMovement(forward ? END : START, 2, forward ? ForceMovement.NORTH : ForceMovement.SOUTH));
				} else if (ticks == 2) {
					player.setNextWorldTile(forward ? END : START);
					player.setRunHidden(run);
				} else if (ticks == 3) {
					player.unlock();
					stop();
				}
				ticks++;
			}
		}, 0, 0);
	}

	
}
