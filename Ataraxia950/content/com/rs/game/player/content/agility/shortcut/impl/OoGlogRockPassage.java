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
public final class OoGlogRockPassage implements Shortcut {
	
	private static final WorldTile START = new WorldTile(2596, 2871, 0);
	private static final WorldTile END = new WorldTile(2596, 2869, 0);
	private static final Animation CLIMB_START = new Animation(10578),
			CLIMB_END = new Animation(10579);
	
	@Override
	public int getLevel() {
		return 29;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 29099 };
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
					player.setNextAnimation(forward ? CLIMB_START : CLIMB_END);
					player.sendMessage("You squeeze through the narrow tunnel.", true);
					player.setNextForceMovement(new ForceMovement(forward ? END : START, 2, forward ? ForceMovement.SOUTH : ForceMovement.NORTH));
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
