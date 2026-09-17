package com.rs.game.npc.others;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

@SuppressWarnings("serial")
public class TestNPC extends NPC {

	public TestNPC(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		this.setCantFollowUnderCombat(true);
	}

	@Override
	public void processNPC() {

	}

}
