package com.rs.game.npc.others.randomevent;

import com.rs.game.ForceTalk;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Sep 16, 2018.
 */
public abstract class RandomEventNPC extends NPC {

	private static final long serialVersionUID = 4026080030246960067L;

	/**
	 * Constructs a new class.
	 * @param id
	 * @param tile
	 */
	public RandomEventNPC(int id, WorldTile tile, Player target) {
		super(id, new WorldTile(tile), -1, true, true);
		setRandomEventTarget(target);
		setCreateTime(Utils.currentTimeMillis());
		setNextForceTalk(new ForceTalk("Hey, " + target.getDisplayName() + ", talk to me."));
		setRun(true);
	}

	/**
	 * Gives a random event reward to the player.
	 * @param player The player receiving the reward.
	 */
	public abstract void giveReward(Player player);

	@Override
	public void processNPC() {
		sendFollow(randomEventTarget);
		if (randomEventTarget.hasFinished() || getCreateTime() + 60000 < Utils.currentTimeMillis()) {
			randomEventTarget.setCurrentRandomEventNPC(null);
			finish();
		}
		if (!isStop()) {
			if (Utils.random(50) <= 2)
				setNextForceTalk(new ForceTalk(randomEventTarget.getDisplayName() + " talk to me!"));
			else if (Utils.random(50) >= 48)
				setNextForceTalk(new ForceTalk("Talk to me, " + randomEventTarget.getDisplayName() + "!"));
		}
	}

	@Override
	public boolean withinDistance(Player tile, int distance) {
		return (tile == randomEventTarget && super.withinDistance(tile, distance));
	}

	private void sendFollow(Player player) {
		if (!withinDistance(player, 2))
			setNextWorldTile(player);
		if (getLastFaceEntity() != player.getClientIndex())
			setNextFaceEntity(player);
		if (isFrozen())
			return;
		int size = getSize();
		int targetSize = player.getSize();
		if (Utils.colides(getX(), getY(), size, player.getX(), player.getY(), targetSize) && !player.hasWalkSteps()) {
			resetWalkSteps();
			if (!addWalkSteps(player.getX() + targetSize, getY())) {
				resetWalkSteps();
				if (!addWalkSteps(player.getX() - size, getY())) {
					resetWalkSteps();
					if (!addWalkSteps(getX(), player.getY() + targetSize)) {
						resetWalkSteps();
						if (!addWalkSteps(getX(), player.getY() - size)) {
							return;
						}
					}
				}
			}
			return;
		}
		resetWalkSteps();
		if (!clipedProjectile(player, true) || !Utils.isOnRange(getX(), getY(), size, player.getX(), player.getY(), targetSize, 0))
			calcFollow(player, 2, true, false);
	}

}
