package com.rs.game.player.content.dungeoneering;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.others.NDungeonBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.MapAreas;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Dungeoneering {

	
	public static final ArrayList<String> generateLines(final String text, final int width) {
		final ArrayList<String> generatedLines = new ArrayList<String>();
		final String[] lines = text.split("\\s+");
		StringBuilder tempLine = new StringBuilder();
		int count = 0;
		for (final String l : lines) {
			if (tempLine.length() < width) {
				if (l.startsWith("<br>")) {
					generatedLines.add(tempLine.toString());
					tempLine = new StringBuilder();
					count++;
					continue;
				}
				tempLine.append(l + " ");
			if (count == lines.length - 1) {
				generatedLines.add(tempLine.toString());
			}
			} else {
				generatedLines.add(tempLine.toString());
				tempLine = new StringBuilder();
				if (l.startsWith("<br>")) {
					generatedLines.add(tempLine.toString());
					tempLine = new StringBuilder();
					count++;
					continue;
				}
				tempLine.append(l + " ");
				if (count == lines.length - 1) {
					generatedLines.add(tempLine.toString());
				}
			}
			count++;
		}
		return generatedLines;
	}
	
	/*
	 * objects handling
	 */
	public static final int[] DUNGEON_DOORS = new int[] { 50342 },
			DUNGEON_EXITS = new int[] { 51156 };

	/*
	 * floor types
	 */
	public static final int FROZEN_FLOORS = 0, ABANDONED_FLOORS = 1,
			FURNISHED_FLOORS = 2, ABANDONED2_FLOORS = 3, OCCULT_FLOORS = 4,
			WARPED_FLOORS = 5;
	/*
	 * door directions
	 */
	public static final int EAST_DOOR = 0, WEST_DOOR = 1, NORTH_DOOR = 2,
			SOUTH_DOOR = 3;

	public static final StartRoom[] START_ROOMS = new StartRoom[] {
	// FROZEN_FLOOR
	new StartRoom(822, 14, 624, SOUTH_DOOR) };

	public static Object getRandomValue(final Object[] objectArray) {
		return objectArray[Utils.getRandom(objectArray.length - 1)];
	}

	public static final BossRoom[][] BOSS_ROOMS = new BossRoom[][] {
	// FROZEN_FLOORS
	new BossRoom[] {
			
			
			// Gluttonous behemoth
			new BossRoom((dungeon, pos) -> {
				final WorldTile base = dungeon.getBaseCoords(pos);
				World.spawnObject(new WorldObject(49283, 10, 3,
						base.getX() - 7, base.getY() + 4, 0), true);
				if (dungeon.getPartySize() >= 2) {
					World.spawnObject(
							new WorldObject(49283, 10, 2, base.getX() + 3,
									base.getY() + 4, 0), true);
				}
				final int npcId = getBossId(1,
						dungeon.getStartPartyTotalCombatLevel(),
						"Gluttonous behemoth");
				final NDungeonBoss boss = new NDungeonBoss(npcId, new WorldTile(
						base.getX() - 3, base.getY() + 2, 0), dungeon);
				boss.setCantFollowUnderCombat(true);
			}, 817, 1, 28, 624, SOUTH_DOOR),
			
			
			// Icy Bones
			new BossRoom((dungeon, pos) -> {
				final WorldTile base = dungeon.getBaseCoords(pos);
				final int npcId = getBossId(1,dungeon.getStartPartyTotalCombatLevel(),"Icy Bones");
				final NDungeonBoss boss = new NDungeonBoss(npcId, new WorldTile(
						base.getX() - 3, base.getY() + 2, 0), dungeon);
				boss.setCantFollowUnderCombat(false);
			}, 817, 1, 28, 624, SOUTH_DOOR),
			
			
			// Astea Frostweb
			new BossRoom((dungeon, pos) -> {
				final int npcId = getBossId(1,
						dungeon.getStartPartyTotalCombatLevel(),
						"Astea Frostweb");
				final WorldTile base = dungeon.getBaseCoords(pos);
				new com.rs.game.npc.others.FrostWeb(npcId, new WorldTile(base.getX() - 1,
						base.getY() - 4, 0), dungeon);
			}, 816, 1, 30, 624, SOUTH_DOOR) } };

	public static int getBossId(final int numberNPCSonRoom,
			final int startPartyTotalCombatLevel, final String npcName) {
		final int recommendedLevel = startPartyTotalCombatLevel / numberNPCSonRoom;
		int lastCombatLevelDifference = -1;
		int npcId = -1;
		for (int id = 0; id < Utils.getNPCDefinitionsSize(); id++) {
			final NPCDefinitions npcDef = NPCDefinitions.getNPCDefinitions(id);
			if (npcDef.name != null && npcDef.name.equals(npcName)) {
				int difference = npcDef.combatLevel - recommendedLevel;
				if (difference < 0) {
					difference = -difference;
				}
				if (lastCombatLevelDifference == -1
						|| difference < lastCombatLevelDifference
						|| (difference == lastCombatLevelDifference && NPCDefinitions
								.getNPCDefinitions(npcId).combatLevel < npcDef.combatLevel)) {
					lastCombatLevelDifference = difference;
					npcId = id;
				}
			}
		}
		return npcId;
	}

	/*
	 * TODO dungLevel for boss
	 */
	public static Room getRandomBossRoom(final int floorType, final int dungLevel) {
		return (Room) getRandomValue(BOSS_ROOMS[floorType]);
	}

	public static void main(final String[] args) { // Test
		final long start = Utils.currentTimeMillis();
		final DungeonStructure smallDungeon = generateDungeonStructure(FROZEN_FLOORS,
				1, 120);
		Logger.getGlobal().info("Took: " + (Utils.currentTimeMillis() - start)
				+ " ms.");
		Logger.getGlobal().info("Small dungeon has: " + smallDungeon.getRoomsCount()
				+ " rooms.");
	}

	/*
	 * makes up to 10 trys to generate random dungeon structures, if all them
	 * too small returns biggest one
	 */
	public static DungeonStructure generateDungeonStructure(final int floorType,
			final int complexity, final int lowestDungLevel) {
		final int[] ratio = getRatio(0);
		final DungeonStructure structure = new DungeonStructure(floorType, ratio[0],
				ratio[1]);
		final Room startRoom = START_ROOMS[floorType];
		final Room bossRoom = getRandomBossRoom(floorType, lowestDungLevel);
		if (bossRoom == null) {
			return null;
		}
		structure.addRoom(0, 0, startRoom, 2);
		structure.addRoom(0, 1, bossRoom, 0);
		return structure;
	}

	public static final class DungeonStructure {

		private final Room[][] rooms;
		private final int[][] rotations;
		private int roomsCount;
		private final int floorType;

		public DungeonStructure(final int floorType, final int ratioX, final int ratioY) {
			rooms = new Room[ratioX][ratioY];
			rotations = new int[ratioX][ratioY];
			this.floorType = floorType;
		}

		public Room[][] getRooms() {
			return rooms;
		}

		public int[][] getRotations() {
			return rotations;
		}

		public void addRoom(final int x, final int y, final Room room, final int rotation) {
			rooms[x][y] = room;
			rotations[x][y] = rotation;
			roomsCount++;
		}

		public int[] getBossRoomPos() {
			for (int x = 0; x < rooms.length; x++) {
				for (int y = 0; y < rooms[x].length; y++) {
					if (rooms[x][y] != null && rooms[x][y] instanceof BossRoom) {
						return new int[] { x, y };
					}
				}
			}
			return new int[] { 0, 0 };
		}

		public int[] getStartRoomPos() {
			for (int x = 0; x < rooms.length; x++) {
				for (int y = 0; y < rooms[x].length; y++) {
					if (rooms[x][y] != null && rooms[x][y] instanceof StartRoom) {
						return new int[] { x, y };
					}
				}
			}
			return new int[] { 0, 0 };
		}

		public int getRoomsCount() {
			return roomsCount;
		}

		public int getFloorType() {
			return floorType;
		}

	}

	public static boolean checkPlaceRoomBounds(final int x, final int y, final Room room,
			final int ratioX, final int ratioY, final int rotation) {
		if (x == 0 && room.hasWestDoor(rotation)) {
			return false;
		}
		if (y == 0 && room.hasSouthDoor(rotation)) {
			return false;
		}
		if (x == ratioX - 1 && room.hasEastDoor(rotation)) {
			return false;
		}
		return y != ratioY - 1 || !room.hasNorthDoor(rotation);
	}

	private interface RoomEvent {

		void openRoom(Dungeon dungeon, int[] pos);
	}

	private static final class BossRoom extends Room {

		private final int requiredLevel;

		private BossRoom(final RoomEvent event, final int musicId, final int requiredLevel,
				final int regionX, final int regionY, final int... doorsDirections) {
			super(event, musicId, regionX, regionY, doorsDirections);
			this.requiredLevel = requiredLevel;
		}

		@SuppressWarnings("unused")
		public int getRequiredLevel() {
			return requiredLevel;
		}

	}

	private static class StartRoom extends Room {

		private StartRoom(final int musicId, final int regionX, final int regionY,
				final int... doorsDirections) {
			super(musicId, regionX, regionY, doorsDirections);
		}
	}

	private static class Room {

		private final int regionX;
		private final int regionY;
		private final int[] doorsDirections;
		private final int musicId;
		private final RoomEvent event;

		private Room(final int musicId, final int regionX, final int regionY,
				final int... doorsDirections) {
			this(null, musicId, regionX, regionY, doorsDirections);
		}

		private Room(final RoomEvent event, final int musicId, final int regionX, final int regionY,
				final int... doorsDirections) {
			this.event = event;
			this.regionX = regionX;
			this.regionY = regionY;
			this.doorsDirections = doorsDirections;
			this.musicId = musicId;
		}

		public int getRegionX() {
			return regionX;
		}

		public int getRegionY() {
			return regionY;
		}

		public void openRoom(final Dungeon dungeon, final int[] pos) {
			if (event == null) {
				return;
			}
			event.openRoom(dungeon, pos);
		}

		public boolean hasSouthDoor(final int rotation) {
			return hasDoor(rotation == 0 ? SOUTH_DOOR
					: rotation == 1 ? WEST_DOOR : rotation == 2 ? NORTH_DOOR
							: EAST_DOOR);
		}

		public boolean hasNorthDoor(final int rotation) {
			return hasDoor(rotation == 0 ? NORTH_DOOR
					: rotation == 1 ? EAST_DOOR : rotation == 2 ? SOUTH_DOOR
							: WEST_DOOR);
		}

		public boolean hasWestDoor(final int rotation) {
			return hasDoor(rotation == 0 ? WEST_DOOR
					: rotation == 1 ? NORTH_DOOR : rotation == 2 ? EAST_DOOR
							: SOUTH_DOOR);
		}

		public boolean hasEastDoor(final int rotation) {
			return hasDoor(rotation == 0 ? EAST_DOOR
					: rotation == 1 ? SOUTH_DOOR : rotation == 2 ? WEST_DOOR
							: NORTH_DOOR);
		}

		public boolean hasDoor(final int direction) {
			for (final int dir : doorsDirections) {
				if (dir == direction) {
					return true;
				}
			}
			return false;
		}

		public int getMusicId() {
			return musicId;
		}

	}

	public static void startDungeon(final int floor, final int complexity,
			final int size, final Player... teamArray) {
		new Dungeon(floor, complexity, size, teamArray);
	}

	public static int[] getRatio(final int size) {
		final int ratioX = 1, ratioY = 2;
		return new int[] { ratioX, ratioY };
	}

	public static class Dungeon {

		private final int floor;
		private final CopyOnWriteArrayList<Player> team;
		private final boolean[][] openedRooms;
		private int openedRoomsCount;
		private DungeonStructure structure;
		private int[] mapBaseCoords;
		private boolean destroyed;
		private boolean started;
		private int startPartyTotalCombatLevel;
		private int dungeonBossRoomHash;

		private Dungeon(final int floor, final int complexity, final int size,
				final Player[] teamArray) {
			this.floor = floor;
			dungeonBossRoomHash = -1;
			final int[] ratio = getRatio(0);
			openedRooms = new boolean[ratio[0]][ratio[1]];
			team = new CopyOnWriteArrayList<Player>();
			for (final Player player : teamArray) {
				team.add(player);
				player.getControlerManager().startControler("DungeonController",
						this);
			}
			setPartyTotalCombatLevel();
			CoresManager.getServiceProvider().executeNow(() -> {
				try {
					structure = generateDungeonStructure(
							getFloorType(floor), complexity, size);
					mapBaseCoords = MapBuilder.findEmptyChunkBound(
							ratio[0] * 2, ratio[1] * 2);
					MapBuilder.cutMap(mapBaseCoords[0], mapBaseCoords[1], ratio[0] * 2, ratio[1] * 2, 0);
					dungeonBossRoomHash = MapAreas.getRandomAreaHash();
					final WorldTile base = getBaseCoords(structure.getBossRoomPos());
					MapAreas.addArea(dungeonBossRoomHash,new int[] { base.getPlane(), base.getX() - 8,base.getX() + 8, base.getY() - 8, base.getY() + 8 });
					if (!checkRoom(structure.getStartRoomPos())) {
						destroyDungeon();
						return;
					}
					startDungeon();
				} catch (final Throwable e) {
					Logger.getGlobal().catching(e);
				}
			});
		}

		public void remove(final Player player) {
			team.remove(player);
			if (started) {
				if (team.isEmpty()) {
					destroyDungeon();
				}
			}
		}

		public void exitCave(final Player player, final boolean logout) {
			team.remove(player);
			player.stopAll();
			if (logout) {
				player.setLocation(new WorldTile(DungeonConstants.OUTSIDE, 2));
			} else {
				player.useStairs(-1, new WorldTile(DungeonConstants.OUTSIDE, 2),
						0, 1);
				player.setForceMultiArea(false);
				player.getControlerManager().removeControlerWithoutCheck();
				player.getInterfaceManager().closeOverlay(false);
				
			}
		}
		
		public void startDungeon() {
			if (team.isEmpty()) {
				destroyDungeon();
				return;
			}
			final WorldTile homeTile = getHomeTeleTile();
			final int[] homeRoom = structure.getStartRoomPos();
			World.spawnNPC(11226, new WorldTile(homeTile, 2), -1, true);
			for (final Player player : team) {
				int lastBonfire = player.getLastBonfire();
				player.stopAll();
				player.resetForDungeoneering();
				player.setMapSize(0); // biggest map size so less reloading when
										// walking from a part of dungeon to
										// another
				player.setForceMultiArea(true);
				player.setNextWorldTile(homeTile);
				playMusic(player, homeRoom);
																// spellbook
				player.getPackets().sendGameMessage("");
				player.getPackets().sendGameMessage("-Welcome to Daemonheim-");
				player.getPackets().sendGameMessage(
						"Floor " + floor + " Complexity " + 6 + " (Full)");
				player.getPackets().sendGameMessage("Dungeon Size: " + "Small");
				player.getPackets().sendGameMessage(
						"Party Size:Dificulty " + team.size() + ":"
								+ team.size());
				player.getPackets().sendGameMessage("");
			}
			started = true;
		}

		public WorldTile getHomeTeleTile() {
			return getBaseCoords(structure.getStartRoomPos());
		}

		public WorldTile getBaseCoords(final int[] pos) {
			return new WorldTile(((mapBaseCoords[0] << 3) + pos[0] * 16) + 8,
					((mapBaseCoords[1] << 3) + pos[1] * 16) + 8, 0);

		}

		public void playMusic(final Player player, final int... pos) {
			final Room room = structure.getRooms()[pos[0]][pos[1]];
			player.getMusicsManager().playMusic(room.getMusicId());
		}

		/*
		 * true doesnt move, false moves
		 */
		public boolean checkRoom(final int... pos) {
			if (pos.length != 2) {
				return true;
			}
			if (openedRooms[pos[0]][pos[1]]) {
				return false;
			}
			if (openedRoomsCount >= structure.getRoomsCount()) {
				return true;
			}
			final Room room = structure.getRooms()[pos[0]][pos[1]];
			if (room == null) {
				return true;
			}
			openedRooms[pos[0]][pos[1]] = true;
			openedRoomsCount++;
			final int roomRegionX = mapBaseCoords[0] + (pos[0] * 2);
			final int roomRegionY = mapBaseCoords[1] + (pos[1] * 2);
			MapBuilder.copy2RatioSquare(room.getRegionX(),
					room.getRegionY(), roomRegionX, roomRegionY,
					structure.getRotations()[pos[0]][pos[1]]);
			final int regionId = (((roomRegionX / 8) << 8) + (roomRegionY / 8));
			for (final Player player : team) {
				if (!player.getMapRegionsIds().contains(regionId)) {
					// is to
																	// far... no
																	// need to
																	// reload,
																	// he will
																	// reload
																	// when walk
																	// to that
																	// room
					continue;
				}
				player.setForceNextMapLoadRefresh(true);
				player.loadMapRegions();
			}
			room.openRoom(this, pos);
			return true;
		}

		public int[] getCurrentRoomCoords(final WorldTile tile) {
			return new int[] { tile.getChunkX() << 3, tile.getChunkY() << 3 };
		}

		public int[] getCurrentRoomPos(final WorldTile tile) {
			return new int[] { (tile.getChunkX() - mapBaseCoords[0]) / 2,
					(tile.getChunkY() - mapBaseCoords[1]) / 2 };
		}

		public int getPartySize() {
			return team.size();
		}

		public int getDungeonBossRoomHash() {
			return dungeonBossRoomHash;
		}

		public void destroyDungeon() {
			if (destroyed) {
				return;
			}
			destroyed = true;
			MapAreas.removeArea(dungeonBossRoomHash);
			// gives 1 game ticket so people can leave
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					try {
						final int[] ratio = getRatio(0);
						MapBuilder.destroyMap(mapBaseCoords[0], mapBaseCoords[1], ratio[0] * 2, ratio[1] * 2);
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
			}, 1);
		}

		public int getLowestPartDungeonneringLevel() {
			int lowestDungeonneringLevel = 120;
			for (final Player player : team) {
				final int dungLevel = player.getSkills().getLevelForXp(
						Skills.DUNGEONEERING);
				if (dungLevel < lowestDungeonneringLevel) {
					lowestDungeonneringLevel = dungLevel;
				}
			}
			return lowestDungeonneringLevel;
		}

		public int getStartPartyTotalCombatLevel() {
			return startPartyTotalCombatLevel;
		}

		public void setPartyTotalCombatLevel() {
			int combatLevel = 0;
			for (final Player player : team) {
				combatLevel += player.getSkills().getCombatLevelWithSummoning();
			}
			startPartyTotalCombatLevel = combatLevel;
		}

		public boolean hasStarted() {
			return started;
		}

		public boolean isDestroyed() {
			return destroyed;
		}

		public void openStairs() {
			WorldObject object;
			final int[] pos = structure.getBossRoomPos();
			switch (structure.getFloorType()) {
			case FROZEN_FLOORS:
			default:
				object = new WorldObject(3784, 10, 3,
						((mapBaseCoords[0] << 3) + pos[0] * 16) + 7,
						((mapBaseCoords[1] << 3) + pos[1] * 16) + 15, 0);
				break;
			}
			World.spawnObject(object, false);
			for (final Player player : team) {
				player.getPackets().sendMusicEffect(415);
			}
		}

	}

	public static int getFloorType(final int floor) {
		return FROZEN_FLOORS; // TODO
	}

	private Dungeoneering() {

	}
}
