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
public final class LumbridgeStile implements Shortcut {
	
	private static final WorldTile LUMBRIDGE_STILE_START = new WorldTile(3198, 3288, 0);
	private static final WorldTile LUMBRIDGE_STILE_END = new WorldTile(3198, 3285, 0);
	
	private static final Animation CLIMB = new Animation(1560);
	private static final WorldTile END_CLOSEUP = new WorldTile(3198, 3286, 0);
	private static final WorldTile START_CLOSEUP = new WorldTile(3198, 3287, 0);
	
	@Override
	public int getLevel() {
		return 1;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 45205 };
	}

	@Override
	public WorldTile getStart() {
		return LUMBRIDGE_STILE_START;
	}

	@Override
	public WorldTile getEnd() {
		return LUMBRIDGE_STILE_END;
	}

	@Override
	public void succeed(Player player, boolean forward) {
		player.lock();
		final boolean run = player.getRun();
		player.setRunHidden(false);
		player.addWalkSteps(forward ? START_CLOSEUP.getX() : END_CLOSEUP.getX(), forward ? START_CLOSEUP.getY() : END_CLOSEUP.getY(), -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 0) {
					player.setNextAnimation(CLIMB);
					player.setNextFaceWorldTile(forward ? getEnd() : getStart());
				} else if (ticks == 1) {
					player.setNextForceMovement(new ForceMovement(forward ? END_CLOSEUP : START_CLOSEUP, 1, forward ? ForceMovement.SOUTH : ForceMovement.NORTH));
				} else if (ticks == 2) {
					player.getSkills().addXp(Skills.AGILITY, 3);
					player.setNextWorldTile(forward ? END_CLOSEUP : START_CLOSEUP);
					player.setRunHidden(run);
					player.unlock();
				}
				ticks++;
			}
		}, 1, 0);
	}

	
}
