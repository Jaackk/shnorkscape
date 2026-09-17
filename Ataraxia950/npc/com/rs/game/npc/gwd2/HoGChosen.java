package com.rs.game.npc.gwd2;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public class HoGChosen extends NPC {

	private static final long serialVersionUID = 3766640511800636584L;

	public HoGChosen(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}
	
	@Override
	public int getRespawnDirection() {
		switch(id) {
		case 22433:
			return 4096;
		case 22434:
			return 12288;
		default:
			return 8192;
		}
	}

}
