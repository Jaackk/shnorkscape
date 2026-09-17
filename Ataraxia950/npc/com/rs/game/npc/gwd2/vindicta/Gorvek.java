package com.rs.game.npc.gwd2.vindicta;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

/**
 * @author Tom
 * @date April 9, 2017
 */

public class Gorvek extends NPC {

	private static final long serialVersionUID = -4771707128153113580L;

	public Gorvek(final int id, final WorldTile tile, final int mapAreaNameHash,
			final boolean canBeAttackFromOutOfArea, final boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setRun(true);
	}
	
	@Override
	public boolean isIntelligentRouteFinder() {
		return true;
	}
	
	@Override
	public boolean canWalkNPC(final int toX, final int toY) {
		return true;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 1;
	}

	@Override
	public void spawn() {
		super.spawn();
		setNextAnimation(new Animation(28264));
	}

}