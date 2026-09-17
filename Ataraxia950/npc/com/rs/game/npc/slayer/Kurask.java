package com.rs.game.npc.slayer;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public class Kurask extends NPC {

	private static final long serialVersionUID = -4494741319426883984L;

	public Kurask(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
	}

}