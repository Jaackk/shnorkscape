package com.rs.game.activities.rots;

import com.rs.cores.CoresManager;
import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Class used for generating Rise of the Six maps dynamically
 * 
 * @author Noel
 * Modified by Kris.
 */
public class RiseOfTheSixMap {

	/* Global variables for data access */
	private static final Random r = Utils.RANDOM;
	private static final int[] DIRECTIONS = { 2, 3, 0, 1 };

	private final MapBlocks[] blocks = prepareList();
	private final List<int[]> blacklist = new ArrayList<int[]>();
	private final Map<String, int[]> indexes = new HashMap<String, int[]>();

	private int[] chunks, index;
	private final int size;
	public int[] start;
	private final List<Room> rooms;
	private Room last;
	private int[][] mapIndexes;
	private final Map<String, int[]> bridgeRooms = new HashMap<String, int[]>();

	public RiseOfTheSixMap() {
		size = 24;
		chunks = MapBuilder.findEmptyChunkBound(size, size);
		rooms = new ArrayList<Room>();
		create();
	}

	public int[][] getAllMapRooms() {
		return mapIndexes;
	}
	
	public HashMap<String, int[]> getBridges() {
		return (HashMap<String, int[]>) bridgeRooms;
	}

	public void clean() {
		CoresManager.getServiceProvider().executeWithDelay(() -> {
			MapBuilder.destroyMap(chunks[0], chunks[1], size, size);
			chunks = null;
		}, 6000, TimeUnit.MILLISECONDS);
		rooms.clear();
		blacklist.clear();
	}

	public WorldTile getTile(final String name) {
		final int[] index = indexes.get(name);
		return new WorldTile(index[0], index[1], 0);
	}

	public int[] getIndex(final String name) {
		return indexes.get(name);
	}

	private void create() {
		// return the next chunk index[] for our next rooms
		index = bossEntry();

		// make some caves boys
		for (int i = 0; i < Utils.random(1, 4); i++) {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					makeRoom();
				}
			}, 1);
		}

		// final call to make rope room
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				ropeEntry();
				int i = 0;
				mapIndexes = new int[indexes.size()][2];
				for (final Map.Entry<String, int[]> entry : indexes.entrySet()) {
					if (entry.getKey().contains("bridge") || entry.getKey().contains("hall2") || entry.getKey().contains("hall3")) {
						bridgeRooms.put(entry.getKey(), new int[] { entry.getValue()[0], entry.getValue()[1], entry.getValue()[2] });
					}
					mapIndexes[i] = new int[] { entry.getValue()[0], entry.getValue()[1] };
					i++;
				}
			}
		}, 5);
	}

	private void makeRoom() {
		final MapBlocks block = randomBlock();
		int face = block.exits.get(0);
		final int exit = last.exits.get(0);
		face = offset(new int[] { exit, block.isExit(DIRECTIONS[exit]) ? DIRECTIONS[exit] : face });

		for (final int exits : block.exits) {
			if (last.block.name.contains("portal")) {
				continue;
			}
			int rotation = offset(new int[] { exit, block.isExit(DIRECTIONS[exit]) ? DIRECTIONS[exit] : face });
			rotation = exits + rotation >= 4 ? (exits + rotation) - 4 : exits + rotation;
			final int[] shadow = offIndex(rotation);
			for (final int[] pair : blacklist) {
				if (pair[0] == shadow[0] && pair[1] == shadow[1] && pair[0] != last.data[0] && pair[1] != last.data[1]) {
					face = offset(new int[] { exit, block.isExit(DIRECTIONS[exit]) ? DIRECTIONS[exit] : block.exits.get(1) });
				}
			}
		}

		last = new Room(block.getName(), new int[] { index[0], index[1], face });
		if (last.exits.contains(DIRECTIONS[exit])) {
			last.exits.remove((Integer) DIRECTIONS[exit]);
		}
		rooms.add(last);
		blacklist.add(index);
		indexes.put((block.getName().contains("bridge") ? block.getName() : (block.getName().equalsIgnoreCase("hall2") || block.getName().equalsIgnoreCase("hall3")) ? block.getName() :"room") + (rooms.size() - 2), new int[] { index[0], index[1], face});

		for (final int cexit : last.exits) {
			index = offIndex(cexit);
		}

		/*
		 * Check for future collisions, remove and re-roll (in the future,
		 * should replace same index with a corner block)
		 */
		for (final int[] pair : blacklist) {
			if (pair[0] == index[0] && pair[1] == index[1]) {
				rooms.remove(last);
				index = blacklist.get(blacklist.size() - 1);
				blacklist.remove(index);
			}
		}
	}

	private int[] bossEntry() {
		final String entry = (Utils.random(100) > 50) ? "gportal" : "yportal";
		final boolean portal = entry.equals("gportal");

		// Spawn the boss room at the top chunk index, add it to our list, as
		// first entry.
		int[] chunk = { chunks[0] + (size - 8), chunks[1] + 12 };
		int offset;
		final Room boss = (new Room("boss", chunk));
		rooms.add(boss);
		blacklist.add(chunk);

		// center line of the arena
		indexes.put("center", new int[] { (chunk[0] * 8) + 18, (chunk[1] * 8) + 20 });
		indexes.put("splatform", new int[] { (chunk[0] * 8) + 17, (chunk[1] * 8) + 11 });
		indexes.put("nplatform", new int[] { (chunk[0] * 8) + 17, (chunk[1] * 8) + 29 });

		// Pick one of two entry chunks, and connect it to the boss room.
		chunk = new int[] { chunks[0] + (size - 10), chunks[1] + 12 };
		offset = MapBlocks.getBlock(entry).getBossDoor();
		final Room portals = new Room(entry, new int[] { chunk[0], chunk[1], offset });

		indexes.put("nplate", new int[] { (chunks[0] * 8) + 12, (chunks[1] * 8) + 11 });
		indexes.put("splate", new int[] { (chunks[0] * 8) + 12, (chunks[1] * 8) + 5 });
		indexes.put("entrance", new int[] { (chunk[0] * 8), (chunk[1] * 8) });

		rooms.add(portals);
		blacklist.add(chunk);
		last = portals;

		return new int[] { chunk[0] - (portal ? 0 : 2), chunk[1] + (portal ? 2 : 0)};
	}

	/*
	 * Special builder for the graveyard, because its weird? adds blocks indexes
	 * to the blacklist for safety..
	 */
	@Deprecated
	@SuppressWarnings("unused")
	private void graveyard(final int[] data) {
		CoresManager.getServiceProvider().executeNow(() -> {
			for (int i = 0; i < 3; i++) {
				MapBuilder.copyChunk(296 + (i == 2 ? 4 : i), 756, 1, data[0] + i, data[1], 0, 0);
				blacklist.add(new int[] { data[0] + i, data[1] });

				MapBuilder.copyChunk(296 + (i == 2 ? 4 : i), 758, 1, data[0] + i, data[1] + 1, 0, 0);
				blacklist.add(new int[] { data[0] + i, data[1] + 1 });

				MapBuilder.copyChunk(296 + (i == 2 ? 4 : i), 759, 1, data[0] + i, data[1] + 2, 0, 0);
				blacklist.add(new int[] { data[0] + i, data[1] + 2 });
			}
		});
	}

	private void ropeEntry() {
		Room rope;
		final MapBlocks block = MapBlocks.getBlock("rope");
		int face = block.exits.get(Utils.random(block.exits.size()));
		final int exit = last.exits.get(0);
		face = offset(new int[] { exit, block.isExit(DIRECTIONS[exit]) ? DIRECTIONS[exit] : face });
		rope = new Room("rope", new int[] { index[0], index[1], face });
		rooms.add(rope);
		indexes.put("rope", new int[] { (index[0] * 8) + 7, (index[1] * 8) + 8 });
	}

	private int offset(final int[] data) {
		final int opposite = DIRECTIONS[data[0]] - data[1];
		return opposite < 0 ? 4 + opposite : opposite;
	}

	private int[] offIndex(final int exit) {
		return new int[] { DIRECTIONS[exit] == 1 ? index[0] - 2 : DIRECTIONS[exit] == 3 ? index[0] + 2 : index[0], DIRECTIONS[exit] == 0 ? index[1] - 2 : DIRECTIONS[exit] == 2 ? index[1] + 2 : index[1] };
	}

	private MapBlocks randomBlock() {
		MapBlocks block = null;
		do {
			block = blocks[r.nextInt(blocks.length)];
		} while (block == null);
		return block;
	}

	@Deprecated
	@SuppressWarnings("unused")
	private MapBlocks randomCorner() {
		MapBlocks block = null;
		do {
			block = blocks[r.nextInt(blocks.length)];
		} while (block.getName().contains("hall"));
		return block;
	}

	private MapBlocks[] prepareList() {
		final MapBlocks[] master = MapBlocks.values();
		final List<MapBlocks> blocklist = new ArrayList<MapBlocks>(Arrays.asList(master));
		final String[] special = { "rope", "boss", "shadow", "gportal", "yportal" };

		for (final String block : special) {
			blocklist.remove(MapBlocks.getBlock(block));
		}
		return blocklist.toArray(master);
	}

	/* directions = 0 - north, 1 - east, 2 - south, 3 - west */
	private enum MapBlocks {

		PORTAL1("gportal", new int[] { 304, 756, 2, 2 }, new Integer[] { 2 }),

		PORTAL2("yportal", new int[] { 304, 758, 2, 1 }, new Integer[] { 2 }),

		BOSS("boss", new int[] { 289, 736, 8, 3 }, new Integer[] { 2, 3 }),

		SHADOW("shadow", new int[] { 288, 752, 8, 3 }, new Integer[] { 2, 3 }),

		CAVE1("rope", new int[] { 304, 752, 2 }, new Integer[] { 2 }),

		// ROCKWALL("wall1", new int[] { 310, 752, 1 }, new Integer[] {}),

		// CAVE3("ycave", new int[] { 310, 754, 2 }, new Integer[] { 0, 2, 3 }),

		CAVE2("archrock", new int[] { 306, 754, 2 }, new Integer[] { 2, 3 }),

		BRIDGE1("waterbridge", new int[] { 306, 756, 2 }, new Integer[] { 2, 3 }),

		BRIDGE2("emptybridge", new int[] { 306, 758, 2 }, new Integer[] { 2, 3 }),

		HALL1("hall1", new int[] { 308, 754, 2 }, new Integer[] { 0, 2 }),

		HALL2("hall2", new int[] { 308, 756, 2 }, new Integer[] { 0, 2 }),

		HALL3("hall3", new int[] { 308, 758, 2 }, new Integer[] { 0, 2 });

		private static final Map<String, MapBlocks> blocks = new HashMap<String, MapBlocks>();

		static {
			for (final MapBlocks block : MapBlocks.values()) {
				blocks.put(block.getName(), block);
			}
		}

		private final String name;
		private final int[] config;
		private final List<Integer> exits;

		MapBlocks(final String name, final int[] config, final Integer[] exits) {
			this.name = name;
			this.config = config;
			this.exits = new ArrayList<Integer>(Arrays.asList(exits));
		}

		public static MapBlocks getBlock(final String name) {
			return blocks.get(name);
		}

		public String getName() {
			return name;
		}

		public List<Integer> getExits() {
			return exits;
		}

		public int getX() {
			return config[0];
		}

		public int getY() {
			return config[1];
		}

		public int getChunks() {
			return config[2];
		}

		public boolean isExit(final int direction) {
			return exits.contains(direction);
		}

		public int getBossDoor() {
			return (config.length > 3) ? config[3] : -1;
		}
	}

	/**
	 * Room wrapper for each copied chunk
	 * 
	 * @author Noel (took this idea from my text-based python game)
	 */
	public class Room {

		private final MapBlocks block;
		private final int[] data;
		private int rotation;
		private final List<Integer> exits = new ArrayList<Integer>();

		public Room(final String name, final int[] data) {
			block = MapBlocks.getBlock(name);
			this.data = data;
			if (data.length == 3) {
				for (final int exit : block.getExits()) {
					rotation = data[2];
					exits.add((exit + rotation) >= 4 ? (exit + rotation) - 4 : exit + rotation);
				}
			}
			create();
		}

		public void create() {
			/* Wrap it in a execution manager for stability and safety */
			// Special catch for spawning the boss room
			if (block.getName().equals("boss")) {
				MapBuilder.copyAllPlanesMap(block.getX(), block.getY(), data[0], data[1], block.getChunks());
			} else {
				// Copies the block into the SW corner with rotation-padding
				MapBuilder.copy2RatioSquare(block.getX(), block.getY(), data[0], data[1], data[2]);
			}
		}
	}
}
