package com.rs.game.activities.instances;

import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.Nihils;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris 
 * {@link https://www.rune-server.ee/members/kris/ } 
 */
public class NihilInstance extends Instance {
	
	public static final int[][] NIHIL_PATHS = new int[][] {
		{ 28, 37, 28, 38, 28, 36 }, { 29, 38, 30, 38, 27, 38 }, { 29, 28, 30, 28, 28, 28 }, { 29, 20, 30, 20, 28, 20 },
		{ 28, 19, 28, 18, 28, 20 }, { 20, 19, 20, 18, 20, 20 }, { 12, 19, 12, 18, 12, 20 }, { 11, 20, 10, 20, 12, 20 },
		{ 11, 28, 10, 28, 12, 28 }, { 11, 36, 10, 36, 12, 36 }, { 12, 37, 12, 38, 12, 36 }, { 20, 37, 20, 38, 20, 36 }
	};
	
	public static final int[] getRandomLocation(int npcId) {
		int[][] paths;
		int random = Utils.random(3);
		switch(npcId) {
		case 19148:
			paths = new int[][] { NIHIL_PATHS[0], NIHIL_PATHS[11], NIHIL_PATHS[10] };
			return paths[random];
		case 19147:
			paths = new int[][] { NIHIL_PATHS[4], NIHIL_PATHS[5], NIHIL_PATHS[6] };
			return paths[random];
		case 19149:
			paths = new int[][] { NIHIL_PATHS[6], NIHIL_PATHS[7], NIHIL_PATHS[8] };
			return paths[random];
		default:
			paths = new int[][] { NIHIL_PATHS[1], NIHIL_PATHS[2], NIHIL_PATHS[3] };
			return paths[random];
		}
	}
	
	private NPC[] nihils;
	
	public NihilInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
		super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
		chunksToBind = new int[] { 505, 785 };
		sizes = new int[] { 6, 7 };
		boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
	}
	
	@Override
	public void addPlayer(Player player) {
		players.add(player);
		player.setForceMultiArea(true);
		if (this instanceof Instance)
			player.setCurrentInstance(this);
		player.sendMessage("You activate the memoriam and wind up in a pit with Nihils.");
	}

	@Override
	public WorldTile getWaitingRoomCoords() {
		return getWorldTile(20, 28);
	}

	@Override
	public void initiateSpawningSequence() {
		NihilInstance instance = this;
		nihils = new NPC[8];
		nihils[0] = new Nihils(19146, getWorldTile(27, 36), -1, true, this);
		nihils[1] = new Nihils(19146, getWorldTile(20, 20), -1, true, this);
		nihils[2] = new Nihils(19147, getWorldTile(13, 35), -1, true, this);
		nihils[3] = new Nihils(19147, getWorldTile(13, 28), -1, true, this);
		nihils[4] = new Nihils(19148, getWorldTile(20, 35), -1, true, this);
		nihils[5] = new Nihils(19148, getWorldTile(13, 20), -1, true, this);
		nihils[6] = new Nihils(19149, getWorldTile(27, 21), -1, true, this);
		nihils[7] = new Nihils(19149, getWorldTile(27, 28), -1, true, this);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (!instance.getOwner().withinDistance(getWorldTile(20, 20), 100)) {
					instance.destroyInstance();
					for (int i = 0; i < nihils.length; i++)
						nihils[i].finish();
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
