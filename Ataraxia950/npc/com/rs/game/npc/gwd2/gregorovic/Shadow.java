package com.rs.game.npc.gwd2.gregorovic;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public class Shadow extends NPC {

	private static final long serialVersionUID = 7269079941433132336L;
	private int phase;

	public Shadow(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setIntelligentRouteFinder(true);
		setForceTargetDistance(30);
		setForceMultiArea(true);
		setNextAnimation(new Animation(28232));
	}
	
	@Override
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}

	public int getPhase() {
		return phase;
	}

	public void nextPhase() {
		phase++;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

}