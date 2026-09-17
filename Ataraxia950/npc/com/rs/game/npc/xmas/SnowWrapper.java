package com.rs.game.npc.xmas;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public class SnowWrapper extends NPC {

	/**
	 * generated id for serializing data
	 */
	private static final long serialVersionUID = 5830495535241240802L;

	public SnowWrapper(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
	}

}
