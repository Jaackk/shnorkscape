package com.rs.game.player.content.agility.shortcut.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.agility.Failable;
import com.rs.game.player.content.agility.shortcut.Shortcut;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris {@link https://www.rune-server.ee/members/kris/}
 * Date: 23rd August 2017.
 */
public final class ArdougneLogBalance implements Shortcut, Failable {

	private static final WorldTile ARDOUGNE_LOG_BALANCE_START = new WorldTile(2602, 3336, 0);
	private static final WorldTile ARDOUGNE_LOG_BALANCE_END = new WorldTile(2598, 3336, 0);
	
	private static final Animation BALANCING = new Animation(763), 
			FALLING_LEFT = new Animation(2581), 
			FALLING_RIGHT = new Animation(2582), 
			WALKING = new Animation(9908);
	private static final WorldTile FAIL_DESTINATION_A = new WorldTile(2603, 3330, 0), 
			FAIL_DESTINATION_B = new WorldTile(2598, 3331, 0), 
			FAIL_MIDDLE = new WorldTile(2600, 3331, 0), 
			SPLASH_TILE = new WorldTile(2600, 3334, 0), 
			MIDDLE = new WorldTile(2600, 3336, 0);
	private static final Graphics SPLASH = new Graphics(68);

	
	@Override
	public void succeed(Player player, boolean forward) {
		player.lock();
		final boolean run = player.getRun();
		player.setRunHidden(false);
		player.setNextAnimation(WALKING);
		player.getAppearence().setRenderEmote(155);
		player.sendMessage("You attempt to walk across the slippery log.");
		if (forward)
			player.addWalkSteps(getEnd().getX(), getEnd().getY(), -1, false);
		else
			player.addWalkSteps(getStart().getX(), getStart().getY(), -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.sendMessage("You make it across the log without any problems.");
				player.getAppearence().setRenderEmote(-1);
				player.setNextAnimation(RESET);
				player.getSkills().addXp(Skills.AGILITY, 4);
				player.setRunHidden(run);
				player.unlock();
			}
		}, 3);
	}

	@Override
	public int getLevel() {
		return 33;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 35999, 35997 };
	}

	@Override
	public WorldTile getStart() {
		return ARDOUGNE_LOG_BALANCE_START;
	}

	@Override
	public WorldTile getEnd() {
		return ARDOUGNE_LOG_BALANCE_END;
	}

	@Override
	public void fail(Player player, boolean forward) {
		player.lock();
		final boolean run = player.getRun();
		player.setRunHidden(false);
		player.setNextAnimation(WALKING);
		player.getAppearence().setRenderEmote(155);
		player.sendMessage("You attempt to walk across the slippery log.");
		player.addWalkSteps(MIDDLE.getX(), MIDDLE.getY(), -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			private final boolean bool = Utils.randomBool();
			@Override
			public void run() {
				if (ticks == 0) {
					player.setNextAnimation(RESET);
				} else if (ticks == 1) {
					player.stopAll();
					player.getAppearence().setRenderEmote(-1);
					player.setNextAnimation(BALANCING);
				} else if (ticks == 2) {
					player.sendMessage("You lose your footing and fall into the river.");
					player.setNextAnimation(forward ? FALLING_LEFT : FALLING_RIGHT);
				} else if (ticks == 3) {
					player.getAppearence().setRenderEmote(188);
					World.sendGraphics(player, SPLASH, SPLASH_TILE);
					player.setNextWorldTile(SPLASH_TILE);
					player.getPackets().sendSound(6382, 0, 2);
				} else if (ticks == 4) {
					player.sendMessage("You feel like you're drowning.");
					player.addWalkSteps(FAIL_MIDDLE.getX(), FAIL_MIDDLE.getY(), -1, false);
				} else if (ticks == 7) {
					final WorldTile tile = bool ? FAIL_DESTINATION_A : FAIL_DESTINATION_B;
					player.addWalkSteps(tile.getX(), tile.getY(), -1, false);
				} else if (ticks == (bool ? 10 : 9)) {
					player.sendMessage("You finally come to the shore.");
					player.getAppearence().setRenderEmote(-1);
					player.applyHit(new Hit(Utils.random(40), HitLook.REGULAR_DAMAGE));
					player.getSkills().addXp(Skills.AGILITY, 2);
					player.setRunHidden(run);
					player.unlock();
					stop();
				}
				ticks++;
			}
		}, 0, 0);
	}

}
