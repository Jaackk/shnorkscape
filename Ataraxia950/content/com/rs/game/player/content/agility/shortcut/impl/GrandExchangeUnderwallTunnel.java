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
public class GrandExchangeUnderwallTunnel implements Shortcut {

	private static final WorldTile GRAND_EXCHANGE_UNDERWALL_START = new WorldTile(3138, 3516, 0);
	private static final WorldTile GRAND_EXCHANGE_UNDERWALL_END = new WorldTile(3144, 3514, 0);
	
	private static final Animation DOWN = new Animation(2589),
			STILL = new Animation(2590),
			UP = new Animation(2591);
	private static final WorldTile MIDDLE = new WorldTile(3141, 3515, 0),
			TUNNEL_START = new WorldTile(3143, 3514, 0),
			TUNNEL_END = new WorldTile(3139, 3516, 0);
	
	
	@Override
	public void succeed(Player player, boolean start) {
		player.lock();
		final boolean run = player.getRun();
		player.setRunHidden(false);
		player.setNextAnimation(DOWN);
		player.setNextForceMovement(new ForceMovement(start ? TUNNEL_END : TUNNEL_START, 2, start ? ForceMovement.EAST : ForceMovement.WEST));
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 0) {
					player.setNextAnimation(STILL);
					player.setNextForceMovement(new ForceMovement(MIDDLE, 2, 0));
				} else if (ticks == 2) {
					player.setNextWorldTile(MIDDLE);
				} else if (ticks == 4) {
					player.setNextForceMovement(new ForceMovement(start ? TUNNEL_START : TUNNEL_END, 2, start ? ForceMovement.EAST : ForceMovement.WEST));
				} else if (ticks == 6) {
					player.setNextAnimation(UP);
					player.setNextForceMovement(new ForceMovement(start ? getEnd() : getStart(), 1, start ? ForceMovement.EAST : ForceMovement.WEST));
				} else if (ticks == 7) {
					player.setNextWorldTile(start ? getEnd() : getStart());
					player.setRunHidden(run);
					player.unlock();
				}
				ticks++;
			}
		}, 1, 0);
	}

	@Override
	public int getLevel() {
		return 21;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 9311, 9312 };
	}

	@Override
	public WorldTile getStart() {
		return GRAND_EXCHANGE_UNDERWALL_START;
	}

	@Override
	public WorldTile getEnd() {
		return GRAND_EXCHANGE_UNDERWALL_END;
	}

}
