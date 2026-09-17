package com.rs.game.npc.slayer;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public class Killerwatt extends NPC {

	private static final long serialVersionUID = -6896156436854654332L;

	public Killerwatt(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (this.getId() == 3202)
			transformIntoNPC(3201);
		super.handleIngoingHit(hit);
	}

}