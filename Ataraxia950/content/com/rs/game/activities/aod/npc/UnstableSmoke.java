package com.rs.game.activities.aod.npc;

import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.npc.NPC;

/**
 * A class to handle the unstable smoke NPC spawned by a specific ability. NPC does not move or get
 * processed by any means.
 * @author Kris | 30. sept 2017 : 17:22.25
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class UnstableSmoke extends NPC {

	private static final long serialVersionUID = 5310491023721921325L;

	public UnstableSmoke(final WorldTile tile, final AngelOfDeath instance) {
		super(24015, tile, -1, false, true);
	}
	
	@Override
	public void processNPC() { }

	@Override
	public boolean isIntelligentRouteFinder() {
		return true;
	}
}
