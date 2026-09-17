package com.rs.game.activities.instances;

import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.Muspahs;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris 
 * {@link https://www.rune-server.ee/members/kris/ } 
 */
public class MuspahInstance extends Instance {
	
	private NPC[] muspahs;
	
	public MuspahInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
		super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
		chunksToBind = new int[] { 528, 776 };
		sizes = new int[] { 8, 8 };
		boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
	}
	
	@Override
	public void addPlayer(Player player) {
		players.add(player);
		player.setForceMultiArea(true);
		if (this instanceof Instance)
			player.setCurrentInstance(this);
		player.sendMessage("You activate the memoriam and wind up in a cradle with Muspahs.");
	}

	@Override
	public WorldTile getWaitingRoomCoords() {
		return getWorldTile(35, 34);
	}
	
	public WorldTile getRandomTile() {
		return new WorldTile(getWorldTile(30, 25), 3);
	}

	@Override
	public void initiateSpawningSequence() {
		MuspahInstance instance = this;
		muspahs = new NPC[3];
		muspahs[0] = new Muspahs(19150, getRandomTile(), -1, true, this);
		muspahs[1] = new Muspahs(19151, getRandomTile(), -1, true, this);
		muspahs[2] = new Muspahs(19152, getRandomTile(), -1, true, this);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (!instance.getOwner().withinDistance(getWorldTile(39, 25), 50)) {
					instance.destroyInstance();
					for (int i = 0; i < muspahs.length; i++) {
						if (muspahs[i] != null)
							muspahs[i].finish();
					}
					stop();
				}
			}
		}, 5, 5);
	}

	@Override
	public WorldTile getOutsideCoordinates() {
		return null;
	}

	@Override
	public NPC getBossNPC() {
		return null;
	}
	
	@Override
	public void performOnSpawn() {}

}
