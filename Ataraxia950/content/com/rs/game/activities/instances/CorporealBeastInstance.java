package com.rs.game.activities.instances;

import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.corp.CorporealBeast;
import com.rs.game.player.Player;

/**
 * @author Kris 
 * {@link https://www.rune-server.ee/members/kris/ } 
 */
public class CorporealBeastInstance extends Instance {
	
	public CorporealBeastInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
		super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
		chunksToBind = new int[] { 368, 544 };
		sizes = new int[] { 8, 7 };
		boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
	}

	@Override
	public WorldTile getWaitingRoomCoords() {
		return getWorldTile(26, 32);
	}
	
	@Override
	public WorldTile getOutsideCoordinates() {
		return new WorldTile(2970, 4384, 2);
	}
	
	@Override
	public WorldTile getWorldTile(int x, int y) {
		return new WorldTile((boundChunks[0] * 8) + x, (boundChunks[1] * 8) + y, 2);
	}

	@Override
	public NPC getBossNPC() {
		return new CorporealBeast(8133, getWorldTile(45, 31), -1, true, true);
	}

	@Override
	public void performOnSpawn() {}
}