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
public final class VarrockFence implements Shortcut {
	
	private static final WorldTile START = new WorldTile(3230, 3331, 0);
	private static final WorldTile END = new WorldTile(3230, 3330, 0);
	private static final Animation CLIMB = new Animation(23895);
	
	@Override
	public int getLevel() {
		return 13;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 9300 };
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
		player.setNextAnimation(CLIMB);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getSkills().addXp(Skills.AGILITY, 1.5);
				player.setNextAnimation(RESET);
				player.setNextWorldTile(forward ? END : START);
				player.setRunHidden(run);
				player.unlock();
			}
		}, 1);
	}

	
}
