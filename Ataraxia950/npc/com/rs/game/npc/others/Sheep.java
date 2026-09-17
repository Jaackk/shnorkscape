package com.rs.game.npc.others;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

@SuppressWarnings("serial")
public class Sheep extends NPC {

	int ticks, originalId;

	public Sheep(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		originalId = id;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (getId() != originalId) {
			if (ticks++ == 60) {
				setNextNPCTransformation(originalId);
				ticks = 0;
			}
		}
	}
}
