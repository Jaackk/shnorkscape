package com.rs.game.npc.gwd2.helwyr;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public class CywirAlpha extends NPC {

	private static final long serialVersionUID = -4310609027271204386L;

	public CywirAlpha(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setRun(true);
		setIntelligentRouteFinder(true);
		setForceAgressive(true);
		setForceMultiArea(true);
		setForceTargetDistance(30);
		setNextAnimation(new Animation(23582));
	}
	
	@Override
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 1;
	}
}
