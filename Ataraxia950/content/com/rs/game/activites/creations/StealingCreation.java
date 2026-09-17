package com.rs.game.activites.creations;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Richard (Flamable)
 * @author Khaled
 */
public class StealingCreation {

	public static final int REQUIRED_PLAYERS_PER_TEAM = 5;
	public static final int LOBBY_INTERFACE = 804;
	public static final int GAME_INTERFACE = 809;
	public static final int KILN_INTERFACE = 813;
	public static final int SCORE_INTERFACE = 810;
	private static final int SCORE_AWARDS_REFRESH_SCRIPT = 1948;
	public static final int SCORE_CONFIG = 1333;
	private static final int GAME_HUD_TIMER_VARC = 557;
	private static final int GAME_HUD_SCORE_VARC = 558;
	private static final int GAME_HUD_TIMER_COMPONENT = 3;
	private static final int GAME_HUD_SCORE_COMPONENT = 5;
	private static final int GAME_HUD_ENDING_BACKDROP_COMPONENT = 6;
	private static final int GAME_HUD_ONE_MINUTE_COMPONENT = 7;
	private static final int LOBBY_GAME_START_COMPONENT = 2;
	private static final int LOBBY_WAITING_CONTAINER_COMPONENT = 3;
	private static final int LOBBY_TEAM_SKILL_TOTAL_COMPONENT = 5;
	private static final int LOBBY_TEAM_COMBAT_TOTAL_COMPONENT = 6;
	private static final int LOBBY_ENEMY_COMBAT_TOTAL_COMPONENT = 7;
	private static final int LOBBY_ENEMY_SKILL_TOTAL_COMPONENT = 8;
	private static final int LOBBY_ENEMY_WAITING_COMPONENT = 13;
	private static final int LOBBY_TEAM_WAITING_COMPONENT = 14;
	private static final int LOBBY_COUNTDOWN_VARC = 550;
	private static final int LOBBY_TEAM_WAITING_VARC = 554;
	private static final int LOBBY_ENEMY_WAITING_VARC = 555;
	public static final int SCORE_GATHERING_CONFIG = 1332;
	public static final int SCORE_DEPOSITING_CONFIG = 1333;
	public static final int SCORE_PROCESSING_CONFIG = 1334;
	public static final int SCORE_WITHDRAWING_CONFIG = 1335;
	public static final int SCORE_DAMAGING_CONFIG = 1337;
	public static final int PROCESSING_KILN = 39546;
	public static final int DEPOSIT_OBJECT = 39533;
	public static final int STARTER_RESOURCE_OBJECT = 39548;
	public static final int BLUE_DOOR_1 = 39766;
	public static final int BLUE_DOOR_2 = 39768;
	public static final int RED_DOOR_1 = 39767;
	public static final int RED_DOOR_2 = 39769;
	public static final int BLUE_GATE_HELPER = 39508;
	public static final int RED_GATE_HELPER = 39509;
	public static final int RESOURCE_BARRIER_BUILD_SPOT_START = 39615;
	public static final int RESOURCE_BARRIER_BUILD_SPOT_END = 39617;
	private static final int RESOURCE_BARRIER_WALL_ATTACK_START = 39669;
	private static final int RESOURCE_BARRIER_DOOR_ATTACK_START = 39699;
	private static final int RESOURCE_BARRIER_ITEM_BASE = 14172;
	private static final int RESOURCE_BARRIER_ITEM_COUNT = 4;
	private static final int RESOURCE_BARRIER_SLOT_COUNT = 20;
	public static final int[] FISHING_RESOURCE_OBJECTS = { 39548, 39549, 39562, 39563, 39564, 39565, 39566, 39567,
			39568, 39569, 39570, 39571, 39572, 39573 };
	public static final int[] HUNTER_RESOURCE_OBJECTS = { 39548, 39549, 39574, 39575, 39576, 39577, 39578, 39579,
			39580, 39581, 39582, 39583, 39584, 39585 };
	public static final int[] WOODCUTTING_RESOURCE_OBJECTS = { 39548, 39549, 39586, 39587, 39588, 39589, 39590, 39591,
			39592, 39593, 39594, 39595, 39596, 39597 };
	public static final int[] MINING_RESOURCE_OBJECTS = { 39548, 39549, 39550, 39551, 39552, 39553, 39554, 39555,
			39556, 39557, 39558, 39559, 39560, 39561 };
	public static final int[] RESOURCE_OBJECTS = { 39548, 39549, 39550, 39551, 39552, 39553, 39554, 39555, 39556,
			39557, 39558, 39559, 39560, 39561, 39562, 39563, 39564, 39565, 39566, 39567, 39568, 39569, 39570, 39571,
			39572, 39573, 39574, 39575, 39576, 39577, 39578, 39579, 39580, 39581, 39582, 39583, 39584, 39585, 39586,
			39587, 39588, 39589, 39590, 39591, 39592, 39593, 39594, 39595, 39596, 39597 };
	private static final int LOBBY_WAIT_MINUTES = 2;
	private static final int GAME_DURATION_SECONDS = 20 * 60;
	private static final int DEATH_DROP_LIMIT = 3;
	static final String FORCE_JOINING_ATTR = "sc_force_joining";
	private static final String FORCE_JOIN_SEQUENCE_ATTR = "sc_force_join_sequence";
	private static final String PICKPOCKET_DELAY_ATTR = "sc_pickpocket_delay";
	private static final int PICKPOCKET_COOLDOWN_MILLIS = 2400;
	private static final int PICKPOCKET_MAX_TARGET_LEVEL_ADVANTAGE = 20;
	private static final int PICKPOCKET_MIN_SUCCESS_CHANCE = 20;
	private static final int PICKPOCKET_BASE_SUCCESS_CHANCE = 45;
	private static final int PICKPOCKET_MAX_SUCCESS_CHANCE = 70;
	private static final Animation PICKPOCKET_ANIMATION = new Animation(24887);

	public static final int[] SACRED_CLAY = { 14182, 14184, 14186, 14188, 14190 };
	private static final int MINING_TOOL_BASE = 14122;
	private static final int FISHING_TOOL_BASE = 14142;
	private static final int HUNTER_TOOL_BASE = 14152;
	private static final int WOODCUTTING_TOOL_BASE = 14132;
	public static final int[] CLASS_ITEMS_BASE = { 14132, 14122, 14142, 14152, 14172, 14162, 14367, 14357, 14347, 14411,
			14391, 14401, 14337, 14317, 14327, 14297, 14287, 14307, 14192, 14202, 12850, 12851, 14422, 14377, 14421, -1,
			-1, 14215, 14225, 14235, 14245, 14255, 14265, 14275, 14285 };
	public static final int BOT_PRODUCT_BARRIER = 4;
	public static final int BOT_PRODUCT_FOOD = 5;

	public static final WorldTile LOBBY_WORLDTILE = new WorldTile(2968, 9701, 0);
	private static final WorldTile RED_LOBBY_TILE = new WorldTile(2966, 9701, 0);
	private static final WorldTile BLUE_LOBBY_TILE = new WorldTile(2970, 9701, 0);
	private static final WorldTile RED_GAME_TILE = new WorldTile(1924, 5716, 0);
	private static final WorldTile BLUE_GAME_TILE = new WorldTile(1960, 5706, 0);
	private static final int GAME_SPAWN_RANDOMIZE = 3;
	private static final WorldTile STATIC_KILN_TILE = new WorldTile(1926, 5716, 0);
	private static final int GAME_MAP_CHUNKS = 8;
	private static final int[] RED_GAME_LOCAL_TILE = { 4, 5 };
	private static final int[] BLUE_GAME_LOCAL_TILE = { ((GAME_MAP_CHUNKS - 1) * 8) + 4,
			((GAME_MAP_CHUNKS - 1) * 8) + 2 };
	private static final int[] KILN_OBJECT_LOCAL_TILE = { 6, 4 };
	private static final int[] BLUE_DOOR_P1 = { 3, 7 };
	private static final int[] BLUE_DOOR_P2 = { 4, 7 };
	private static final int[] BLUE_DOOR_P3 = { 7, 4 };
	private static final int[] BLUE_DOOR_P4 = { 7, 3 };
	private static final int[] RED_DOOR_P1 = { 4, 0 };
	private static final int[] RED_DOOR_P2 = { 3, 0 };
	private static final int[] RED_DOOR_P3 = { 0, 3 };
	private static final int[] RED_DOOR_P4 = { 0, 4 };
	private static final int TYPE_RESERVED = 0;
	private static final int TYPE_BASE = 1;
	private static final int TYPE_EMPTY = 2;
	private static final int TYPE_RIFT = 3;
	private static final int TYPE_WALL = 4;
	private static final int TYPE_FOG = 5;
	private static final int TYPE_LARGE_ROCK = 6;
	private static final int TYPE_ALTAR = 7;
	private static final int TYPE_KILN = 8;
	private static final int TYPE_SKILL_ROCK = 9;
	private static final int TYPE_SKILL_TREE = 10;
	private static final int TYPE_SKILL_POOL = 11;
	private static final int TYPE_SKILL_SWARM = 12;
	private static final int[] CHUNK_BASE = { 240, 712 };
	private static final int[] CHUNK_EMPTY = { 241, 715 };
	private static final int[] CHUNK_RESERVED_1 = { 240, 713 };
	private static final int[] CHUNK_RESERVED_2 = { 241, 712 };
	private static final int[] CHUNK_RESERVED_3 = { 241, 713 };
	private static final int[] CHUNK_KILN = { 240, 714 };
	private static final int[] CHUNK_ALTAR = { 241, 714 };
	private static final int[] CHUNK_FOG = { 240, 715 };
	private static final int[] CHUNK_RIFT = { 240, 716 };
	private static final int[] CHUNK_WALL = { 241, 716 };
	private static final int[] CHUNK_LARGE_ROCK = { 242, 716 };
	private static final int[] CHUNK_SKILL_ROCK = { 247, 715 };
	private static final int[] CHUNK_SKILL_POOL = { 247, 714 };
	private static final int[] CHUNK_SKILL_SWARM = { 247, 713 };
	private static final int[] CHUNK_SKILL_TREE = { 247, 712 };

	private static final int[] TOTAL_SKILL_IDS = { Skills.WOODCUTTING, Skills.MINING, Skills.FISHING, Skills.HUNTER,
			Skills.COOKING, Skills.HERBLORE, Skills.CRAFTING, Skills.SMITHING, Skills.FLETCHING, Skills.RUNECRAFTING,
			Skills.CONSTRUCTION };
	private static final int[] TOTAL_COMBAT_IDS = { Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.HITPOINTS,
			Skills.RANGE, Skills.MAGIC, Skills.PRAYER, Skills.SUMMONING };
	private static final int[] BASE_ANIMATIONS = { 10603, 10608, 10618, 10613 };

	private static final List<Player> redTeam = new ArrayList<Player>();
	private static final List<Player> blueTeam = new ArrayList<Player>();

	private static FixedLengthRunnable lobbyTask;
	private static boolean lobbyTaskRunning;
	private static int lobbyCountdownMinutes = LOBBY_WAIT_MINUTES;
	private static FixedLengthRunnable gameTask;
	private static boolean gameTaskRunning;
	private static boolean gameEnding;
	private static int gameSecondsRemaining;
	private static boolean transferringToGame;
	private static int[] gameMapChunks;
	private static int[][] gameMapFlags;
	private static WorldTile primaryKilnTile;
	private static final List<WorldTile> processingKilnTiles = new ArrayList<WorldTile>();
	private static final Map<Player, StealingCreationScore> gameScores = new HashMap<Player, StealingCreationScore>();
	private static final Map<Integer, ResourceBarrier> resourceBarriers = new HashMap<Integer, ResourceBarrier>();

	private static synchronized boolean canEnter(Player player, boolean inRedTeam) {
		pruneTeams();
		int skillTotal = getTotalLevel(TOTAL_SKILL_IDS, inRedTeam, player);
		int combatTotal = getTotalLevel(TOTAL_COMBAT_IDS, inRedTeam, player);
		int otherSkillTotal = getTotalLevel(TOTAL_SKILL_IDS, !inRedTeam, player);
		int otherCombatTotal = getTotalLevel(TOTAL_COMBAT_IDS, !inRedTeam, player);
		boolean inventoryEmpty = player.getInventory().getFreeSlots() == player.getInventory().getItemsContainerSize();
		if ((skillTotal + combatTotal) > (otherSkillTotal + otherCombatTotal)) {
			player.getPackets().sendGameMessage("This team is too strong for you to join at present.");
			return false;
		} else if (player.getEquipment().wearingArmour() || !inventoryEmpty || player.getFamiliar() != null) {
			player.getPackets().sendGameMessage(
					"You may not take any items into Stealing Creation. Use the nearby bank deposit box to empty your inventory and worn items.");
			return false;
		}
		return true;
	}

	public static boolean checkSkillRequriments(Player player, int requestedSkill, int index) {
		int level = getLevelForIndex(index);
		if (player.getSkills().getLevel(requestedSkill) < level) {
			player.getPackets().sendGameMessage("You dont have the requried " + Skills.SKILL_NAME[requestedSkill]
					+ " level for that quality of clay.");
			return false;
		}
		return true;
	}

	public static void enterTeamLobby(Player player, boolean inRedTeam) {
		enterTeamLobby(player, inRedTeam, false);
	}

	public static void forceEnterTeamLobby(Player player, boolean inRedTeam) {
		enterTeamLobby(player, inRedTeam, true);
	}

	public static void queueForceEnterTeamLobby(final Player player, final boolean inRedTeam) {
		if (player == null || player.hasFinished()) {
			return;
		}
		final Object sequence = new Object();
		final WorldTile lobbyTile = getLobbyTile(inRedTeam);
		player.getTemporaryAttributtes().put(FORCE_JOINING_ATTR, Boolean.TRUE);
		player.getTemporaryAttributtes().put(FORCE_JOIN_SEQUENCE_ATTR, sequence);
		prepareForcedLobbyTransfer(player, lobbyTile);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;

			@Override
			public void run() {
				if (player.hasFinished() || player.getControlerManager() == null
						|| !isForceJoinSequence(player, sequence)) {
					clearForceJoinSequence(player, sequence);
					stop();
					return;
				}
				if (!player.clientHasLoadedMapRegion() && ticks++ < 10) {
					return;
				}
				resetPlayerInterface(player);
				enterTeamLobby(player, inRedTeam, true);
				clearForceJoinSequence(player, sequence);
				stop();
			}
		}, 1, 1);
	}

	private static void prepareForcedLobbyTransfer(Player player, WorldTile lobbyTile) {
		player.stopAll();
		if (player.getControlerManager() != null) {
			player.getControlerManager().forceStop();
		}
		player.setCanPvp(false);
		resetPlayerInterface(player);
		if (player.getInterfaceManager().containsScreenInter()) {
			player.getInterfaceManager().closeScreenInterface();
		}
		if (player.getInterfaceManager().containsChatBoxInterface()) {
			player.getInterfaceManager().closeChatBoxInterface();
		}
		player.setNextWorldTile(lobbyTile);
	}

	private static boolean isForceJoinSequence(Player player, Object sequence) {
		return player.getTemporaryAttributtes().get(FORCE_JOIN_SEQUENCE_ATTR) == sequence;
	}

	private static void clearForceJoinSequence(Player player, Object sequence) {
		if (player != null && isForceJoinSequence(player, sequence)) {
			player.getTemporaryAttributtes().remove(FORCE_JOIN_SEQUENCE_ATTR);
			player.getTemporaryAttributtes().remove(FORCE_JOINING_ATTR);
		}
	}

	private static void enterTeamLobby(Player player, boolean inRedTeam, boolean force) {
		if (player == null) {
			return;
		}
		if (!force && !canEnter(player, inRedTeam)) {
			return;
		}
		player.getControlerManager().startControler("StealingCreationLobby", inRedTeam);
	}

	static synchronized void addToTeam(Player player, boolean inRedTeam) {
		if (player == null) {
			return;
		}
		redTeam.remove(player);
		blueTeam.remove(player);
		(inRedTeam ? redTeam : blueTeam).add(player);
		ensureLobbyTask();
	}

	static synchronized void ensureGameTeam(Player player, boolean inRedTeam) {
		if (player == null) {
			return;
		}
		List<Player> team = inRedTeam ? redTeam : blueTeam;
		List<Player> otherTeam = inRedTeam ? blueTeam : redTeam;
		otherTeam.remove(player);
		if (!team.contains(player)) {
			team.add(player);
		}
	}

	public static synchronized void removeFromTeams(Player player) {
		if (player == null) {
			return;
		}
		redTeam.remove(player);
		blueTeam.remove(player);
		updateInterfaces();
		if (gameTaskRunning && !gameEnding) {
			pruneTeams();
			if (redTeam.isEmpty() || blueTeam.isEmpty()) {
				finishGame(false);
			}
		}
	}

	private static void ensureLobbyTask() {
		if (lobbyTaskRunning) {
			return;
		}
		lobbyCountdownMinutes = LOBBY_WAIT_MINUTES;
		lobbyTask = new LobbyTimer();
		lobbyTaskRunning = true;
		CoresManager.getServiceProvider().scheduleFixedLengthTask(lobbyTask, 0, 1, TimeUnit.MINUTES);
	}

	private static void stopLobbyTask() {
		if (lobbyTask != null) {
			lobbyTask.stopNow(false);
			lobbyTask = null;
		}
		lobbyTaskRunning = false;
		lobbyCountdownMinutes = LOBBY_WAIT_MINUTES;
	}

	private static void startGameTask(List<Player> redPlayers, List<Player> bluePlayers) {
		stopGameTask();
		gameScores.clear();
		for (Player player : redPlayers) {
			if (player != null && !player.hasFinished()) {
				gameScores.put(player, new StealingCreationScore(player.getDisplayName(), true));
			}
		}
		for (Player player : bluePlayers) {
			if (player != null && !player.hasFinished()) {
				gameScores.put(player, new StealingCreationScore(player.getDisplayName(), false));
			}
		}
		gameSecondsRemaining = GAME_DURATION_SECONDS;
		gameTask = new GameTimer();
		gameTaskRunning = true;
		CoresManager.getServiceProvider().scheduleFixedLengthTask(gameTask, 0, 1, TimeUnit.SECONDS);
	}

	private static void stopGameTask() {
		if (gameTask != null) {
			gameTask.stopNow(false);
			gameTask = null;
		}
		gameTaskRunning = false;
		gameSecondsRemaining = 0;
	}

	public static Animation getAnimationForBase(int baseId, int index) {
		return new Animation(BASE_ANIMATIONS[index] + baseId);
	}

	static Item getBestItem(Player player, int baseId) {
		int index = getBestToolIndexForBase(player, baseId);
		if (index >= 0) {
			return new Item(baseId + (index * 2), 1);
		}
		return new Item(-1, 1);
	}

	public static List<Player> getBlueTeam() {
		return blueTeam;
	}

	public static WorldTile getGameTile(boolean inRedTeam) {
		if (gameMapChunks != null) {
			int[] local = inRedTeam ? RED_GAME_LOCAL_TILE : BLUE_GAME_LOCAL_TILE;
			return new WorldTile(getGameMapTile(local[0], local[1]), GAME_SPAWN_RANDOMIZE);
		}
		return new WorldTile(inRedTeam ? RED_GAME_TILE : BLUE_GAME_TILE, GAME_SPAWN_RANDOMIZE);
	}

	public static WorldTile getProcessingKilnTile() {
		if (gameMapChunks != null && primaryKilnTile != null) {
			return new WorldTile(primaryKilnTile);
		}
		return new WorldTile(STATIC_KILN_TILE);
	}

	public static WorldTile getNearestProcessingKilnTile(WorldTile fromTile) {
		if (gameMapChunks == null || processingKilnTiles.isEmpty()) {
			return getProcessingKilnTile();
		}
		WorldTile nearest = processingKilnTiles.get(0);
		int nearestDistance = Integer.MAX_VALUE;
		for (WorldTile tile : processingKilnTiles) {
			int distance = getDistance(fromTile, tile);
			if (distance < nearestDistance) {
				nearest = tile;
				nearestDistance = distance;
			}
		}
		return new WorldTile(nearest);
	}

	public static List<WorldTile> getProcessingKilnTiles() {
		List<WorldTile> tiles = new ArrayList<WorldTile>();
		if (gameMapChunks == null || processingKilnTiles.isEmpty()) {
			tiles.add(getProcessingKilnTile());
			return tiles;
		}
		for (WorldTile tile : processingKilnTiles) {
			tiles.add(new WorldTile(tile));
		}
		return tiles;
	}

	public static WorldTile getGameMapCenterTile() {
		if (gameMapChunks == null) {
			return new WorldTile(STATIC_KILN_TILE);
		}
		int center = (GAME_MAP_CHUNKS * 8) / 2;
		return getGameMapTile(center, center);
	}

	public static WorldTile getRandomGameMapTileNear(WorldTile fromTile, int radius) {
		if (gameMapChunks == null || fromTile == null) {
			return fromTile == null ? LOBBY_WORLDTILE : new WorldTile(fromTile, radius);
		}
		int size = (GAME_MAP_CHUNKS * 8) - 2;
		int localX = clamp(fromTile.getX() - (gameMapChunks[0] * 8), 1, size);
		int localY = clamp(fromTile.getY() - (gameMapChunks[1] * 8), 1, size);
		localX = clamp(localX + ThreadLocalRandom.current().nextInt(-radius, radius + 1), 1, size);
		localY = clamp(localY + ThreadLocalRandom.current().nextInt(-radius, radius + 1), 1, size);
		return getGameMapTile(localX, localY);
	}

	public static WorldTile getNearestResourceTileForStyle(WorldTile fromTile, int style, int maxResourceIndex) {
		if (gameMapChunks == null || gameMapFlags == null || fromTile == null) {
			return null;
		}
		int resourceType = getResourceMapTypeForStyle(style);
		WorldTile tile = getNearestResourceTile(fromTile, resourceType, maxResourceIndex);
		return tile == null ? getNearestResourceTile(fromTile, -1, maxResourceIndex) : tile;
	}

	private static WorldTile getNearestResourceTile(WorldTile fromTile, int requiredType, int maxResourceIndex) {
		int maxTier = clamp(maxResourceIndex + 1, 1, 5);
		WorldTile nearest = null;
		int nearestDistance = Integer.MAX_VALUE;
		for (int x = 0; x < gameMapFlags.length; x++) {
			for (int y = 0; y < gameMapFlags[x].length; y++) {
				int type = getGameMapType(x, y);
				if (!isResourceMapType(type) || requiredType >= 0 && type != requiredType) {
					continue;
				}
				int tier = getGameMapTier(x, y);
				if (tier <= 0 || tier > maxTier) {
					continue;
				}
				WorldTile tile = getGameMapTile((x * 8) + 4, (y * 8) + 4);
				int distance = getDistance(fromTile, tile);
				if (distance < nearestDistance) {
					nearest = tile;
					nearestDistance = distance;
				}
			}
		}
		return nearest;
	}

	private static boolean isResourceMapType(int type) {
		return type == TYPE_SKILL_ROCK || type == TYPE_SKILL_POOL || type == TYPE_SKILL_TREE
				|| type == TYPE_SKILL_SWARM;
	}

	private static int getResourceMapTypeForStyle(int style) {
		switch (Math.abs(style) % 4) {
			case 0:
				return TYPE_SKILL_ROCK;
			case 1:
				return TYPE_SKILL_POOL;
			case 2:
				return TYPE_SKILL_SWARM;
			default:
				return TYPE_SKILL_TREE;
		}
	}

	public static boolean recoverToGameArena(Player player) {
		if (player == null || gameMapChunks == null || isInsideGameArena(player)) {
			return false;
		}
		player.resetWalkSteps();
		player.setRouteEvent(null);
		player.setNextWorldTile(getNearestGameArenaTile(player));
		return true;
	}

	public static boolean isInsideGameArena(WorldTile tile) {
		if (tile == null || gameMapChunks == null) {
			return false;
		}
		int localX = tile.getX() - (gameMapChunks[0] * 8);
		int localY = tile.getY() - (gameMapChunks[1] * 8);
		return tile.getPlane() == 0 && localX >= 0 && localY >= 0 && localX < GAME_MAP_CHUNKS * 8
				&& localY < GAME_MAP_CHUNKS * 8;
	}

	private static WorldTile getNearestGameArenaTile(WorldTile tile) {
		int size = (GAME_MAP_CHUNKS * 8) - 2;
		int localX = clamp(tile.getX() - (gameMapChunks[0] * 8), 1, size);
		int localY = clamp(tile.getY() - (gameMapChunks[1] * 8), 1, size);
		return getGameMapTile(localX, localY);
	}

	public static int getBestToolIndexForStyle(Player player, int style) {
		if (player == null) {
			return -1;
		}
		int baseId = getToolBaseIdForStyle(style);
		if (baseId < 0) {
			return -1;
		}
		return getBestToolIndexForBase(player, baseId);
	}

	public static int getBestToolIndexForBase(Player player, int baseId) {
		if (player == null || baseId < 0) {
			return -1;
		}
		for (int index = 4; index >= 0; index--) {
			if (hasTool(player, baseId + (index * 2))) {
				return index;
			}
		}
		return -1;
	}

	private static boolean hasTool(Player player, int itemId) {
		if (player == null || itemId < 0) {
			return false;
		}
		if (player.getInventory().containsItem(itemId, 1)) {
			return true;
		}
		if (player.getEquipment() == null) {
			return false;
		}
		return player.getEquipment().getWeaponId() == itemId
				|| player.getEquipment().getShieldId() == itemId
				|| player.getEquipment().getAmountOf(itemId) > 0;
	}

	private static int getLevelForIndex(int index) {
		if (index <= 0) {
			return 1;
		}
		return Math.min(80, index * 20);
	}

	public static WorldTile getLobbyTile(boolean inRedTeam) {
		return new WorldTile(inRedTeam ? RED_LOBBY_TILE : BLUE_LOBBY_TILE);
	}

	public static List<Player> getRedTeam() {
		return redTeam;
	}

	public static int[] getResourceObjectsForStyle(int style) {
		switch (Math.abs(style) % 4) {
			case 0:
				return MINING_RESOURCE_OBJECTS;
			case 1:
				return FISHING_RESOURCE_OBJECTS;
			case 2:
				return HUNTER_RESOURCE_OBJECTS;
			default:
				return WOODCUTTING_RESOURCE_OBJECTS;
		}
	}

	public static int getResourceIndex(int objectId) {
		switch (objectId) {
			case 39548:
			case 39549:
				return 0;
			case 39553:
			case 39557:
			case 39561:
			case 39562:
			case 39566:
			case 39570:
			case 39574:
			case 39578:
			case 39582:
			case 39586:
			case 39590:
			case 39594:
				return 4;
			case 39552:
			case 39556:
			case 39560:
			case 39563:
			case 39567:
			case 39571:
			case 39575:
			case 39579:
			case 39583:
			case 39587:
			case 39591:
			case 39595:
				return 3;
			case 39551:
			case 39555:
			case 39559:
			case 39564:
			case 39568:
			case 39572:
			case 39576:
			case 39580:
			case 39584:
			case 39588:
			case 39592:
			case 39596:
				return 2;
			case 39550:
			case 39554:
			case 39558:
			case 39565:
			case 39569:
			case 39573:
			case 39577:
			case 39581:
			case 39585:
			case 39589:
			case 39593:
			case 39597:
				return 1;
			default:
				return -1;
		}
	}

	public static int getResourceAnimationStyle(WorldObject object) {
		if (object == null || getResourceIndex(object.getId()) < 0) {
			return -1;
		}
		int generatedType = getGeneratedChunkType(object);
		if (generatedType >= 0) {
			switch (generatedType) {
				case TYPE_SKILL_ROCK:
					return 0;
				case TYPE_SKILL_POOL:
					return 1;
				case TYPE_SKILL_SWARM:
					return 2;
				case TYPE_SKILL_TREE:
					return 3;
			}
		}
		if (containsResourceObject(MINING_RESOURCE_OBJECTS, object.getId())) {
			return 0;
		}
		if (containsResourceObject(FISHING_RESOURCE_OBJECTS, object.getId())) {
			return 1;
		}
		if (containsResourceObject(HUNTER_RESOURCE_OBJECTS, object.getId())) {
			return 2;
		}
		if (containsResourceObject(WOODCUTTING_RESOURCE_OBJECTS, object.getId())) {
			return 3;
		}
		return 0;
	}

	public static int getResourceBaseId(WorldObject object) {
		switch (getResourceAnimationStyle(object)) {
			case 0:
				return MINING_TOOL_BASE;
			case 1:
				return FISHING_TOOL_BASE;
			case 2:
				return HUNTER_TOOL_BASE;
			case 3:
				return WOODCUTTING_TOOL_BASE;
			default:
				return -1;
		}
	}

	private static boolean containsResourceObject(int[] objects, int objectId) {
		for (int id : objects) {
			if (id == objectId) {
				return true;
			}
		}
		return false;
	}

	private static int getGeneratedChunkType(WorldTile tile) {
		if (gameMapChunks == null || gameMapFlags == null || tile == null) {
			return -1;
		}
		int localChunkX = tile.getChunkX() - gameMapChunks[0];
		int localChunkY = tile.getChunkY() - gameMapChunks[1];
		if (localChunkX < 0 || localChunkY < 0 || localChunkX >= gameMapFlags.length
				|| localChunkY >= gameMapFlags.length) {
			return -1;
		}
		return getGameMapType(localChunkX, localChunkY);
	}

	public static int getRequestedKilnSkill(int indexedId) {
		if (indexedId >= 0 && indexedId <= 1 || indexedId >= 6 && indexedId <= 8 || indexedId >= 15 && indexedId <= 17)
			return Skills.SMITHING;
		else if (indexedId >= 2 && indexedId <= 3 || indexedId >= 9 && indexedId <= 14
				|| indexedId >= 18 && indexedId <= 19 || indexedId == 23)
			return Skills.CRAFTING;
		else if (indexedId == 4)
			return Skills.CONSTRUCTION;
		else if (indexedId == 5)
			return Skills.COOKING;
		else if (indexedId >= 20 && indexedId <= 21)
			return Skills.RUNECRAFTING;
		else if (indexedId >= 22 && indexedId <= 24)
			return Skills.SUMMONING;
		else if (indexedId >= 25 && indexedId <= 32)
			return Skills.HERBLORE;
		return Skills.SMITHING;
	}

	public static int getRequestedObjectSkill() {
		return Skills.HUNTER;
	}

	public static int getRequestedObjectSkill(int baseId) {
		if (baseId == MINING_TOOL_BASE) {
			return Skills.MINING;
		}
		if (baseId == FISHING_TOOL_BASE) {
			return Skills.FISHING;
		}
		if (baseId == HUNTER_TOOL_BASE) {
			return Skills.HUNTER;
		}
		if (baseId == WOODCUTTING_TOOL_BASE) {
			return Skills.WOODCUTTING;
		}
		return Skills.HUNTER;
	}

	public static int getToolBaseIdForStyle(int style) {
		switch (Math.abs(style) % 4) {
			case 0:
				return MINING_TOOL_BASE;
			case 1:
				return FISHING_TOOL_BASE;
			case 2:
				return HUNTER_TOOL_BASE;
			default:
				return WOODCUTTING_TOOL_BASE;
		}
	}

	public static int getTargetResourceIndexForStyle(Player player, int style) {
		int toolIndex = getBestToolIndexForStyle(player, style);
		if (toolIndex < 0) {
			return 0;
		}
		return Math.min(4, Math.max(1, toolIndex + 1));
	}

	public static boolean hasToolUpgradeClayForBot(Player player, int style) {
		if (player == null) {
			return false;
		}
		int toolIndex = getBestToolIndexForStyle(player, style);
		for (int index = SACRED_CLAY.length - 1; index >= 0; index--) {
			if (index > toolIndex && player.getInventory().containsItem(SACRED_CLAY[index], 1)) {
				return true;
			}
		}
		return false;
	}

	public static synchronized int getTeamSize(boolean inRedTeam) {
		pruneTeams();
		return inRedTeam ? redTeam.size() : blueTeam.size();
	}

	private static int getTotalLevel(int[] ids, boolean inRedTeam) {
		return getTotalLevel(ids, inRedTeam, null);
	}

	private static int getTotalLevel(int[] ids, boolean inRedTeam, Player ignoredPlayer) {
		int skillTotal = 0;
		for (Player player : inRedTeam ? redTeam : blueTeam) {
			if (player == null || player == ignoredPlayer)
				continue;
			for (int skillRequested : ids) {
				skillTotal += player.getSkills().getLevel(skillRequested);
			}
		}
		return skillTotal;
	}

	public static synchronized boolean hasRequiredPlayers() {
		pruneTeams();
		return redTeam.size() >= REQUIRED_PLAYERS_PER_TEAM && blueTeam.size() >= REQUIRED_PLAYERS_PER_TEAM;
	}

	private static boolean hasRequiredPlayersUnsafe() {
		return redTeam.size() >= REQUIRED_PLAYERS_PER_TEAM && blueTeam.size() >= REQUIRED_PLAYERS_PER_TEAM;
	}

	public static synchronized boolean isSameTeam(Player player, Player target) {
		return player != null && target != null
				&& (redTeam.contains(player) && redTeam.contains(target)
						|| blueTeam.contains(player) && blueTeam.contains(target));
	}

	public static synchronized String getRightClickDisplayName(Player viewer, Player target) {
		if (viewer == null || target == null || viewer == target || !isInGame(viewer) || !isInGame(target)
				|| isInsideFog(target)) {
			return null;
		}
		String colour = isSameTeam(viewer, target) ? "ffffff" : "ff0000";
		return "<col=" + colour + ">" + target.getDisplayName() + "</col>";
	}

	public static void sendGamePlayerOptions(Player player) {
		if (player == null || player.hasFinished()) {
			return;
		}
		player.getPackets().sendPlayerOption("Pickpocket", 4, false);
	}

	public static void resetGamePlayerOptions(Player player) {
		if (player == null || player.hasFinished()) {
			return;
		}
		player.getTemporaryAttributtes().remove(PICKPOCKET_DELAY_ATTR);
		player.sendDefaultPlayersOptions();
	}

	public static boolean startPickpocket(Player thief, Player target) {
		return startPickpocket(thief, target, true);
	}

	public static boolean startPickpocket(final Player thief, final Player target, final boolean message) {
		if (!canPickpocket(thief, target, message)) {
			return false;
		}
		thief.stopAll(false);
		thief.setRouteEvent(new RouteEvent(target, () -> attemptPickpocket(thief, target, message)));
		return true;
	}

	public static boolean canPickpocket(Player thief, Player target, boolean message) {
		if (thief == null || target == null || thief == target || thief.hasFinished() || target.hasFinished()
				|| thief.isDead() || target.isDead()) {
			return false;
		}
		if (!isGameRunning() || !isInGame(thief) || !isInGame(target)) {
			sendPickpocketMessage(thief, message, "You can only pickpocket players during a Stealing Creation match.");
			return false;
		}
		if (isSameTeam(thief, target)) {
			sendPickpocketMessage(thief, message, "You cannot pickpocket players on your own team.");
			return false;
		}
		if (isInsideFog(thief)) {
			sendPickpocketMessage(thief, message, "You cannot pickpocket while hidden in the fog.");
			return false;
		}
		if (isInsideFog(target)) {
			sendPickpocketMessage(thief, message, "The fog hides that player from pickpocketing.");
			return false;
		}
		if (isInsideAnyBase(thief) || isInsideAnyBase(target)) {
			sendPickpocketMessage(thief, message, "You cannot pickpocket players inside a team base.");
			return false;
		}
		if (thief.isUnderCombat()) {
			sendPickpocketMessage(thief, message, "You cannot pickpocket while you are in combat.");
			return false;
		}
		if (isPickpocketOnCooldown(thief)) {
			return false;
		}
		if (getPickpocketChance(thief, target) <= 0) {
			sendPickpocketMessage(thief, message,
					"Your Thieving level must be within 20 levels of that player's Thieving level.");
			return false;
		}
		if (!hasStealableInventoryItem(target)) {
			sendPickpocketMessage(thief, message, target.getDisplayName() + " has no items you can steal.");
			return false;
		}
		if (getPickpocketCandidates(thief, target).isEmpty()) {
			sendPickpocketMessage(thief, message, "You do not have enough inventory space to steal from that player.");
			return false;
		}
		return true;
	}

	public static int getPickpocketChance(Player thief, Player target) {
		if (thief == null || target == null) {
			return 0;
		}
		int thiefLevel = thief.getSkills().getLevel(Skills.THIEVING);
		int targetLevel = target.getSkills().getLevel(Skills.THIEVING);
		if (targetLevel > thiefLevel + PICKPOCKET_MAX_TARGET_LEVEL_ADVANTAGE) {
			return 0;
		}
		int difference = thiefLevel - targetLevel;
		int chance = PICKPOCKET_BASE_SUCCESS_CHANCE + (difference * 5 / 4);
		return Math.max(PICKPOCKET_MIN_SUCCESS_CHANCE, Math.min(PICKPOCKET_MAX_SUCCESS_CHANCE, chance));
	}

	private static boolean attemptPickpocket(Player thief, Player target, boolean message) {
		if (!canPickpocket(thief, target, message)) {
			return false;
		}
		setPickpocketCooldown(thief);
		thief.faceEntity(target);
		thief.setNextAnimation(PICKPOCKET_ANIMATION);
		sendPickpocketMessage(thief, message, "You attempt to pickpocket " + target.getDisplayName() + ".");
		if (ThreadLocalRandom.current().nextInt(100) >= getPickpocketChance(thief, target)) {
			sendPickpocketMessage(thief, message, "You fail to pickpocket " + target.getDisplayName() + ".");
			target.getPackets().sendGameMessage(thief.getDisplayName() + " fails to pickpocket you.");
			return false;
		}
		PickpocketItem selection = selectPickpocketItem(thief, target);
		if (selection == null) {
			sendPickpocketMessage(thief, message, "You do not manage to steal anything.");
			return false;
		}
		Item current = target.getInventory().getItem(selection.slot);
		if (current == null || current.getId() != selection.item.getId()
				|| current.getAmount() < selection.item.getAmount()) {
			sendPickpocketMessage(thief, message, "You do not manage to steal anything.");
			return false;
		}
		Item stolen = new Item(current);
		stolen.setAmount(selection.item.getAmount());
		if (!canReceivePickpocketItem(thief, stolen)) {
			sendPickpocketMessage(thief, message, "You do not have enough inventory space to steal from that player.");
			return false;
		}
		int targetAmountBefore = target.getInventory().getAmountOf(stolen.getId());
		target.getInventory().deleteItem(selection.slot, stolen);
		if (target.getInventory().getAmountOf(stolen.getId()) > targetAmountBefore - stolen.getAmount()) {
			sendPickpocketMessage(thief, message, "You do not manage to steal anything.");
			return false;
		}
		if (!thief.getInventory().addItem(new Item(stolen))) {
			target.getInventory().addItem(new Item(stolen));
			sendPickpocketMessage(thief, message, "You do not have enough inventory space to steal from that player.");
			return false;
		}
		String itemName = formatPickpocketItem(stolen);
		sendPickpocketMessage(thief, message,
				"You pickpocket " + target.getDisplayName() + " and steal " + itemName + ".");
		target.getPackets().sendGameMessage(thief.getDisplayName() + " pickpockets you and steals " + itemName + ".");
		return true;
	}

	private static PickpocketItem selectPickpocketItem(Player thief, Player target) {
		List<PickpocketItem> candidates = getPickpocketCandidates(thief, target);
		if (candidates.isEmpty()) {
			return null;
		}
		return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
	}

	private static List<PickpocketItem> getPickpocketCandidates(Player thief, Player target) {
		List<PickpocketItem> candidates = new ArrayList<PickpocketItem>();
		if (thief == null || target == null) {
			return candidates;
		}
		Item[] items = target.getInventory().getItems().getItemsCopy();
		for (int slot = 0; slot < items.length; slot++) {
			Item item = items[slot];
			if (item == null || item.getAmount() <= 0 || !isStealingCreationItem(item.getId())) {
				continue;
			}
			Item stolen = new Item(item);
			stolen.setAmount(item.getDefinitions().isStackable() ? item.getAmount() : 1);
			if (canReceivePickpocketItem(thief, stolen)) {
				candidates.add(new PickpocketItem(slot, stolen));
			}
		}
		return candidates;
	}

	private static boolean hasStealableInventoryItem(Player target) {
		if (target == null) {
			return false;
		}
		for (Item item : target.getInventory().getItems().getItemsCopy()) {
			if (item != null && item.getAmount() > 0 && isStealingCreationItem(item.getId())) {
				return true;
			}
		}
		return false;
	}

	private static boolean canReceivePickpocketItem(Player thief, Item item) {
		return thief != null && item != null && item.getAmount() > 0
				&& thief.getInventory().hasSpaceFor(item.getId(), item.getAmount());
	}

	private static boolean isPickpocketOnCooldown(Player thief) {
		Object delay = thief.getTemporaryAttributtes().get(PICKPOCKET_DELAY_ATTR);
		return delay instanceof Long && (Long) delay > System.currentTimeMillis();
	}

	private static void setPickpocketCooldown(Player thief) {
		thief.getTemporaryAttributtes().put(PICKPOCKET_DELAY_ATTR,
				System.currentTimeMillis() + PICKPOCKET_COOLDOWN_MILLIS);
	}

	private static void sendPickpocketMessage(Player player, boolean message, String text) {
		if (message && player != null && !player.hasFinished()) {
			player.getPackets().sendGameMessage(text);
		}
	}

	private static String formatPickpocketItem(Item item) {
		String name = item.getDefinitions().getName();
		return item.getAmount() == 1 ? name : item.getAmount() + " x " + name;
	}

	public static synchronized boolean isRedTeam(Player player) {
		return player != null && redTeam.contains(player);
	}

	public static synchronized boolean isBlueTeam(Player player) {
		return player != null && blueTeam.contains(player);
	}

	public static boolean isInsideOwnBase(Player player, boolean inRedTeam) {
		if (player == null || gameMapChunks == null) {
			return false;
		}
		return isInsideTeamBase(player, inRedTeam, 0);
	}

	public static boolean isInsideAnyBase(WorldTile tile) {
		return isInsideTeamBase(tile, true, 0) || isInsideTeamBase(tile, false, 0);
	}

	public static boolean isInsideFog(Player player) {
		return player != null && isInsideFog((WorldTile) player);
	}

	public static boolean isInsideFog(WorldTile tile) {
		if (tile == null || gameMapChunks == null || gameMapFlags == null || tile.getPlane() != 0) {
			return false;
		}
		int localX = tile.getX() - (gameMapChunks[0] * 8);
		int localY = tile.getY() - (gameMapChunks[1] * 8);
		if (localX < 0 || localY < 0 || localX >= GAME_MAP_CHUNKS * 8 || localY >= GAME_MAP_CHUNKS * 8) {
			return false;
		}
		return getGameMapType(localX / 8, localY / 8) == TYPE_FOG;
	}

	public static boolean isNearAnyBase(WorldTile tile, int padding) {
		int safePadding = Math.max(0, padding);
		return isInsideTeamBase(tile, true, safePadding) || isInsideTeamBase(tile, false, safePadding);
	}

	public static synchronized boolean isInGame(Player player) {
		if (player == null || player.hasFinished() || player.getControlerManager() == null) {
			return false;
		}
		return (redTeam.contains(player) || blueTeam.contains(player))
				&& player.getControlerManager().getControler() instanceof StealingCreationGame;
	}

	public static synchronized boolean isTransferringToGame() {
		return transferringToGame;
	}

	public static synchronized boolean passToGame() {
		pruneTeams();
		if (redTeam.isEmpty() || blueTeam.isEmpty()) {
			return false;
		}
		List<Player> redPlayers = new ArrayList<Player>(redTeam);
		List<Player> bluePlayers = new ArrayList<Player>(blueTeam);
		stopLobbyTask();
		createGameMap();
		startGameTask(redPlayers, bluePlayers);
		transferringToGame = true;
		try {
			for (Player player : redPlayers) {
				if (player != null && !player.hasFinished() && redTeam.contains(player)) {
					player.getControlerManager().startControler("StealingCreationGame", true);
				}
			}
			for (Player player : bluePlayers) {
				if (player != null && !player.hasFinished() && blueTeam.contains(player)) {
					player.getControlerManager().startControler("StealingCreationGame", false);
				}
			}
		} finally {
			transferringToGame = false;
		}
		return true;
	}

	public static boolean forceStart() {
		return passToGame();
	}

	public static int getBestClayIndex(Player player) {
		if (player == null) {
			return -1;
		}
		for (int index = SACRED_CLAY.length - 1; index >= 0; index--) {
			if (player.getInventory().containsItem(SACRED_CLAY[index], 1)) {
				return index;
			}
		}
		return -1;
	}

	public static int getSacredClayAmount(Player player) {
		if (player == null) {
			return 0;
		}
		int amount = 0;
		for (int clayId : SACRED_CLAY) {
			amount += player.getInventory().getAmountOf(clayId);
		}
		return amount;
	}

	public static boolean hasSacredClay(Player player) {
		return getSacredClayAmount(player) > 0;
	}

	public static synchronized void ensureScore(Player player, boolean inRedTeam) {
		if (player != null && !gameScores.containsKey(player)) {
			gameScores.put(player, new StealingCreationScore(player.getDisplayName(), inRedTeam));
		}
	}

	public static synchronized StealingCreationScore getScore(Player player) {
		return player == null ? null : gameScores.get(player);
	}

	public static synchronized void updateScore(Player player, int gathering, int processing, int depositing,
			int withdrawing, int damaging, int kills, int deaths) {
		StealingCreationScore score = gameScores.get(player);
		if (score == null || player == null || player.hasFinished()) {
			return;
		}
		score.updateGathering(gathering);
		score.updateProcessing(processing);
		score.updateDepositing(depositing);
		score.updateWithdrawing(withdrawing);
		score.updateDamaging(damaging);
		score.updateKills(kills);
		score.updateDeaths(deaths);
		refreshScore(player);
	}

	public static void recordGathering(Player player, int resourceIndex) {
		updateScore(player, 25 * (Math.max(0, resourceIndex) + 1), 0, 0, 0, 0, 0, 0);
	}

	public static void recordProcessing(Player player, int clayIndex, int amount) {
		updateScore(player, 0, 15 * (Math.max(0, clayIndex) + 1) * Math.max(1, amount), 0, 0, 0, 0, 0);
	}

	public static void recordCombatDamage(Player player, int damage) {
		if (damage > 0) {
			updateScore(player, 0, 0, 0, 0, damage, 0, 0);
		}
	}

	public static int handleDefeat(Player victim) {
		if (victim == null) {
			return 0;
		}
		Player killer = victim.getMostDamageReceivedSourcePlayer();
		if (killer != null && (killer == victim || !isInGame(killer) || isSameTeam(victim, killer))) {
			killer = null;
		}
		updateScore(victim, 0, 0, 0, 0, 0, 0, 1);
		if (killer != null) {
			updateScore(killer, 0, 0, 0, 0, 0, 1, 0);
		}
		return transferDeathItems(victim, killer);
	}

	public static void refreshScore(Player player) {
		StealingCreationScore score = getScore(player);
		if (player == null || player.hasFinished() || score == null) {
			return;
		}
		int total = score.total(false);
		player.getPackets().sendConfig(SCORE_CONFIG, total);
		player.getPackets().sendCSVarInteger(GAME_HUD_SCORE_VARC, total);
		player.getPackets().sendCSVarString(GAME_HUD_SCORE_VARC, String.valueOf(total));
		player.getPackets().sendIComponentText(GAME_INTERFACE, GAME_HUD_SCORE_COMPONENT, "Score: " + total);
	}

	public static void refreshGameHud(Player player) {
		refreshScore(player);
		refreshGameTimer(player);
	}

	private static void refreshGameTimer(Player player) {
		if (player == null || player.hasFinished()) {
			return;
		}
		int secondsRemaining = gameTaskRunning && !gameEnding ? gameSecondsRemaining : 0;
		int safeSeconds = Math.max(0, secondsRemaining);
		player.getPackets().sendCSVarInteger(GAME_HUD_TIMER_VARC, safeSeconds);
		player.getPackets().sendIComponentText(GAME_INTERFACE, GAME_HUD_TIMER_COMPONENT, formatTime(safeSeconds));
		player.getPackets().sendHideIComponent(GAME_INTERFACE, GAME_HUD_ENDING_BACKDROP_COMPONENT, true);
		player.getPackets().sendHideIComponent(GAME_INTERFACE, GAME_HUD_ONE_MINUTE_COMPONENT, safeSeconds > 60);
	}

	public static int depositInventory(Player player) {
		if (player == null || !isInGame(player)) {
			return 0;
		}
		boolean red = isRedTeam(player);
		if (gameMapChunks != null && !isInsideOwnBase(player, red)) {
			player.getPackets().sendGameMessage("You need to return to your team's base to deposit your creations.");
			return 0;
		}
		int depositedScore = 0;
		int depositedItems = 0;
		boolean[] protectedTools = new boolean[4];
		Item[] items = player.getInventory().getItems().getItemsCopy();
		for (Item item : items) {
			if (item == null || !isStealingCreationItem(item.getId())) {
				continue;
			}
			if (shouldKeepToolForDeposit(player, item.getId(), protectedTools)) {
				continue;
			}
			Item deposit = new Item(item.getId(), item.getAmount());
			int value = getItemScoreValue(deposit);
			if (value <= 0) {
				continue;
			}
			player.getInventory().deleteItem(deposit);
			depositedScore += value;
			depositedItems += deposit.getAmount();
		}
		if (depositedScore > 0) {
			updateScore(player, 0, 0, depositedScore, 0, 0, 0, 0);
			player.getPackets().sendGameMessage("You deposit " + depositedItems + " Stealing Creation item"
					+ (depositedItems == 1 ? "" : "s") + " for your team.");
		} else {
			player.getPackets().sendGameMessage("You have no Stealing Creation items to deposit.");
		}
		return depositedItems;
	}

	private static boolean shouldKeepToolForDeposit(Player player, int itemId, boolean[] protectedTools) {
		for (int style = 0; style < protectedTools.length; style++) {
			if (protectedTools[style]) {
				continue;
			}
			int baseId = getToolBaseIdForStyle(style);
			int bestIndex = getBestToolIndexForBase(player, baseId);
			if (bestIndex >= 0 && itemId == baseId + (bestIndex * 2)) {
				protectedTools[style] = true;
				return true;
			}
		}
		return false;
	}

	public static int getInventoryScoreValue(Player player) {
		if (player == null) {
			return 0;
		}
		int value = 0;
		for (Item item : player.getInventory().getItems().getItemsCopy()) {
			value += getItemScoreValue(item);
		}
		return value;
	}

	public static boolean isStealingCreationItem(int itemId) {
		return getStealingCreationItemTier(itemId) >= 0;
	}

	public static int getStealingCreationItemTier(int itemId) {
		for (int index = 0; index < SACRED_CLAY.length; index++) {
			if (SACRED_CLAY[index] == itemId) {
				return index;
			}
		}
		for (int componentIndex = 0; componentIndex < CLASS_ITEMS_BASE.length; componentIndex++) {
			if (CLASS_ITEMS_BASE[componentIndex] < 0) {
				continue;
			}
			for (int index = 0; index < SACRED_CLAY.length; index++) {
				if (getProductId(componentIndex, index) == itemId) {
					return index;
				}
			}
		}
		return -1;
	}

	public static synchronized boolean isGameRunning() {
		return gameTaskRunning && !gameEnding;
	}

	public static synchronized boolean forceEnd() {
		return finishGame(true);
	}

	private static int transferDeathItems(Player victim, Player killer) {
		int moved = 0;
		for (Item item : victim.getInventory().getItems().getItemsCopy()) {
			if (item == null || !isStealingCreationItem(item.getId())) {
				continue;
			}
			int amount = item.getDefinitions().isStackable() ? Math.min(item.getAmount(), DEATH_DROP_LIMIT - moved) : 1;
			if (amount <= 0) {
				continue;
			}
			Item removed = new Item(item.getId(), amount);
			victim.getInventory().deleteItem(removed);
			if (killer != null && !killer.hasFinished()) {
				killer.getInventory().addItemDrop(new Item(removed), new WorldTile(victim));
			} else {
				World.addGroundItem(new Item(removed), new WorldTile(victim));
			}
			moved += amount;
			if (moved >= DEATH_DROP_LIMIT) {
				break;
			}
		}
		if (moved > 0) {
			victim.getPackets().sendGameMessage("Some of your Stealing Creation items spill onto the ground.");
			if (killer != null && !killer.hasFinished()) {
				killer.getPackets().sendGameMessage("You take some of your opponent's Stealing Creation items.");
			}
		}
		return moved;
	}

	private static int getItemScoreValue(Item item) {
		if (item == null || item.getId() <= 0 || item.getAmount() <= 0) {
			return 0;
		}
		int tier = getStealingCreationItemTier(item.getId());
		if (tier < 0) {
			return 0;
		}
		for (int clayId : SACRED_CLAY) {
			if (clayId == item.getId()) {
				return 10 * (tier + 1) * item.getAmount();
			}
		}
		int baseValue = item.getDefinitions().isStackable() ? 1 : 30;
		return baseValue * (tier + 1) * item.getAmount();
	}

	private static int getProductId(int componentIndex, int index) {
		if (componentIndex < 0 || componentIndex >= CLASS_ITEMS_BASE.length || CLASS_ITEMS_BASE[componentIndex] < 0) {
			return -1;
		}
		int componentId = componentIndex + 37;
		if (componentId == 57 || componentId == 58 || componentId == 61) {
			return CLASS_ITEMS_BASE[componentIndex];
		}
		if (componentId == 56) {
			return CLASS_ITEMS_BASE[componentIndex] + index;
		}
		if (componentId >= 64) {
			return CLASS_ITEMS_BASE[componentIndex] - (index * 2);
		}
		return CLASS_ITEMS_BASE[componentIndex] + (index * 2);
	}

	public static int processBestClayForBot(Player player) {
		return processBestClayForBot(player, 0);
	}

	public static int processBestClayForBot(Player player, int style) {
		int baseId = getToolBaseIdForStyle(style);
		if (baseId < 0) {
			return 0;
		}
		int toolIndex = getBestToolIndexForBase(player, baseId);
		for (int index = SACRED_CLAY.length - 1; index >= 0; index--) {
			int clayId = SACRED_CLAY[index];
			if (index <= toolIndex || !player.getInventory().containsItem(clayId, 1)) {
				continue;
			}
			int itemId = baseId + (index * 2);
			player.getInventory().deleteItem(clayId, 1);
			if (player.getInventory().addItem(new Item(itemId, 1))) {
				recordProcessing(player, index, 1);
				return 1;
			}
			player.getInventory().addItem(new Item(clayId, 1));
			return 0;
		}
		return 0;
	}

	public static int processBestClayProductForBot(Player player, int componentIndex, int maxAmount) {
		if (player == null || componentIndex < 0 || componentIndex >= CLASS_ITEMS_BASE.length || maxAmount <= 0
				|| CLASS_ITEMS_BASE[componentIndex] < 0) {
			return 0;
		}
		int componentId = componentIndex + 37;
		int requestedSkill = getRequestedKilnSkill(componentIndex);
		for (int index = SACRED_CLAY.length - 1; index >= 0; index--) {
			int clayId = SACRED_CLAY[index];
			int amount = Math.min(maxAmount, player.getInventory().getAmountOf(clayId));
			if (amount <= 0 || !checkSkillRequriments(player, requestedSkill, index)) {
				continue;
			}
			if (proccessKilnItems(player, componentId, index, clayId, amount)) {
				return amount;
			}
		}
		return 0;
	}

	public static int getBestProductIdForBot(Player player, int componentIndex) {
		if (player == null || componentIndex < 0 || componentIndex >= CLASS_ITEMS_BASE.length) {
			return -1;
		}
		for (int index = SACRED_CLAY.length - 1; index >= 0; index--) {
			int productId = getProductId(componentIndex, index);
			if (productId > 0 && player.getInventory().containsItem(productId, 1)) {
				return productId;
			}
		}
		return -1;
	}

	public static int getBestBarrierItemIdForBot(Player player) {
		return getBestBarrierItemId(player);
	}

	public static int getBestBarrierItemCountForBot(Player player) {
		if (player == null) {
			return 0;
		}
		int best = 0;
		for (int tier = SACRED_CLAY.length - 1; tier >= 0; tier--) {
			int itemId = RESOURCE_BARRIER_ITEM_BASE + (tier * 2);
			best = Math.max(best, player.getInventory().getAmountOf(itemId));
		}
		return best;
	}

	public static synchronized boolean proccessKilnItems(Player player, int componentId, int index, int itemId,
			int amount) {
		if (player == null || index < 0 || index >= SACRED_CLAY.length || amount <= 0) {
			return false;
		}
		int componentIndex = componentId - 37;
		if (componentIndex < 0 || componentIndex >= CLASS_ITEMS_BASE.length || CLASS_ITEMS_BASE[componentIndex] < 0) {
			player.getPackets().sendGameMessage("That item cannot be made from sacred clay.");
			return false;
		}
		int clayId = SACRED_CLAY[index];
		amount = Math.min(amount, player.getInventory().getAmountOf(clayId));
		if (amount <= 0) {
			player.getPackets().sendGameMessage("You have no clay to process.");
			return false;
		}
		int productId = getProductId(componentIndex, index);
		int productAmount = (componentId >= 56 && componentId <= 58 ? 15 * (index + 1)
				: componentId == 61 ? index + 1 : 1) * amount;
		if (productId <= 0 || productAmount <= 0) {
			player.getPackets().sendGameMessage("That item cannot be made from sacred clay.");
			return false;
		}
		player.getInventory().deleteItem(clayId, amount);
		if (player.getInventory().addItem(new Item(productId, productAmount))) {
			recordProcessing(player, index, amount);
			return true;
		}
		player.getInventory().addItem(new Item(clayId, amount));
		player.getPackets().sendGameMessage("You do not have enough inventory space to process that clay.");
		return false;
	}

	private static void pruneTeam(List<Player> team) {
		for (Iterator<Player> iterator = team.iterator(); iterator.hasNext();) {
			Player player = iterator.next();
			if (player == null || player.hasFinished() || player.getControlerManager() == null) {
				iterator.remove();
				continue;
			}
			Object controller = player.getControlerManager().getControler();
			if (!(controller instanceof StealingCreationLobby) && !(controller instanceof StealingCreationGame)) {
				iterator.remove();
			}
		}
	}

	private static void pruneTeams() {
		pruneTeam(redTeam);
		pruneTeam(blueTeam);
	}

	public static synchronized int reset() {
		List<Player> players = new ArrayList<Player>();
		players.addAll(redTeam);
		players.addAll(blueTeam);
		int count = players.size();
		stopLobbyTask();
		stopGameTask();
		gameEnding = false;
		transferringToGame = false;
		for (Player player : players) {
			if (player != null && !player.hasFinished() && player.getControlerManager() != null) {
				player.getControlerManager().forceStop();
				resetPlayerInterface(player);
				player.setNextWorldTile(LOBBY_WORLDTILE);
			}
		}
		redTeam.clear();
		blueTeam.clear();
		gameScores.clear();
		destroyGameMap();
		return count;
	}

	public static void startDynamicSkill(Player player, WorldObject object, Animation animation, int baseId,
			int objectIndex) {
		int requestedSkill = getRequestedObjectSkill(baseId);
		if (!checkSkillRequriments(player, requestedSkill, objectIndex))
			return;
		Item item = getBestItem(player, baseId);
		if (item.getId() == -1)
			animation = new Animation(10602);
		else if (hasTool(player, item.getId()))
			player.setNextAnimation(animation);
		player.getActionManager().setAction(new CreationSkillsAction(object, animation, item, baseId, objectIndex,
				requestedSkill));
	}

	public static boolean isResourceBarrierBuildSpot(WorldObject object) {
		return object != null && object.getId() >= RESOURCE_BARRIER_BUILD_SPOT_START
				&& object.getId() <= RESOURCE_BARRIER_BUILD_SPOT_END
				&& isResourceMapType(getGeneratedChunkType(object));
	}

	public static synchronized boolean isResourceBarrier(WorldObject object) {
		return getResourceBarrier(object) != null;
	}

	public static synchronized boolean isTeamResourceBarrier(Player player, WorldObject object) {
		ResourceBarrier barrier = getResourceBarrier(object);
		return player != null && barrier != null && isOnBarrierTeam(player, barrier.redTeam);
	}

	public static synchronized boolean isEnemyResourceBarrier(Player player, WorldObject object) {
		ResourceBarrier barrier = getResourceBarrier(object);
		return player != null && barrier != null && !isOnBarrierTeam(player, barrier.redTeam);
	}

	public static synchronized int getResourceBarrierTier(WorldObject object) {
		ResourceBarrier barrier = getResourceBarrier(object);
		return barrier == null ? -1 : barrier.tier;
	}

	public static synchronized boolean buildResourceBarrier(Player player, WorldObject object) {
		return buildResourceBarrier(player, object, getBestBarrierItemId(player));
	}

	public static synchronized boolean buildResourceBarrier(Player player, WorldObject object, int itemId) {
		if (player == null || object == null || !isResourceBarrierBuildSpot(object)) {
			return false;
		}
		if (!isInGame(player)) {
			return true;
		}
		int barrierTier = getBarrierItemTier(itemId);
		if (barrierTier < 0) {
			player.getPackets().sendGameMessage("You need four Stealing Creation barriers to build this.");
			return true;
		}
		if (!checkSkillRequriments(player, Skills.CONSTRUCTION, barrierTier)) {
			return true;
		}
		if (player.getInventory().getAmountOf(itemId) < RESOURCE_BARRIER_ITEM_COUNT) {
			player.getPackets().sendGameMessage("You need four barriers of the same class to build this.");
			return true;
		}
		int chunkKey = getChunkKey(object);
		if (hasEnemyResourceBarrier(player, chunkKey)) {
			player.getPackets().sendGameMessage("You must destroy the enemy barrier before building here.");
			return true;
		}
		List<WorldObject> spots = getResourceBarrierBuildSpots(object);
		List<WorldObject> missingSpots = new ArrayList<WorldObject>();
		for (WorldObject spot : spots) {
			if (!resourceBarriers.containsKey(getTileKey(spot))) {
				missingSpots.add(spot);
			}
		}
		if (missingSpots.isEmpty()) {
			player.getPackets().sendGameMessage("This resource is already protected by your team's barrier.");
			repairTeamResourceBarriers(player, chunkKey);
			return true;
		}
		player.getInventory().deleteItem(itemId, RESOURCE_BARRIER_ITEM_COUNT);
		boolean redTeamOwner = isRedTeam(player);
		for (WorldObject spot : missingSpots) {
			spawnResourceBarrier(spot, redTeamOwner, barrierTier, chunkKey);
		}
		repairTeamResourceBarriers(player, chunkKey);
		player.setNextAnimation(new Animation(898));
		player.getPackets().sendGameMessage("You build a barrier around the sacred clay source.");
		return true;
	}

	public static synchronized boolean handleResourceBarrierClick(Player player, WorldObject object) {
		ResourceBarrier barrier = getResourceBarrier(object);
		if (player == null || barrier == null) {
			return false;
		}
		if (isOnBarrierTeam(player, barrier.redTeam)) {
			if (barrier.door) {
				passResourceBarrierDoor(player, barrier.object);
			} else {
				player.getPackets().sendGameMessage("This barrier belongs to your team.");
			}
			return true;
		}
		damageResourceBarrier(player, barrier);
		return true;
	}

	private static ResourceBarrier getResourceBarrier(WorldObject object) {
		return object == null ? null : resourceBarriers.get(getTileKey(object));
	}

	private static int getBarrierItemTier(int itemId) {
		for (int tier = 0; tier < SACRED_CLAY.length; tier++) {
			if (RESOURCE_BARRIER_ITEM_BASE + (tier * 2) == itemId) {
				return tier;
			}
		}
		return -1;
	}

	private static int getBestBarrierItemId(Player player) {
		if (player == null) {
			return -1;
		}
		for (int tier = SACRED_CLAY.length - 1; tier >= 0; tier--) {
			int itemId = RESOURCE_BARRIER_ITEM_BASE + (tier * 2);
			if (player.getInventory().getAmountOf(itemId) >= RESOURCE_BARRIER_ITEM_COUNT) {
				return itemId;
			}
		}
		return -1;
	}

	private static boolean hasEnemyResourceBarrier(Player player, int chunkKey) {
		for (ResourceBarrier barrier : resourceBarriers.values()) {
			if (barrier.chunkKey == chunkKey && !isOnBarrierTeam(player, barrier.redTeam)) {
				return true;
			}
		}
		return false;
	}

	private static void repairTeamResourceBarriers(Player player, int chunkKey) {
		for (ResourceBarrier barrier : resourceBarriers.values()) {
			if (barrier.chunkKey == chunkKey && isOnBarrierTeam(player, barrier.redTeam)) {
				barrier.life = barrier.maxLife;
			}
		}
	}

	private static List<WorldObject> getResourceBarrierBuildSpots(WorldObject object) {
		List<WorldObject> spots = new ArrayList<WorldObject>();
		List<WorldObject> objects = World.getRegion(object.getRegionId()).getAllObjects();
		if (objects == null) {
			return spots;
		}
		for (WorldObject candidate : objects) {
			if (candidate.getPlane() == object.getPlane() && candidate.getChunkX() == object.getChunkX()
					&& candidate.getChunkY() == object.getChunkY() && isResourceBarrierBuildSpot(candidate)) {
				spots.add(candidate);
			}
		}
		return spots;
	}

	private static void spawnResourceBarrier(WorldObject spot, boolean redTeamOwner, int barrierTier, int chunkKey) {
		int slotIndex = getResourceBarrierSlotIndex(spot);
		boolean door = isResourceBarrierDoorSpot(spot);
		int objectId = (door ? RESOURCE_BARRIER_DOOR_ATTACK_START : RESOURCE_BARRIER_WALL_ATTACK_START) + slotIndex;
		WorldObject barrierObject = new WorldObject(objectId, spot.getType(), spot.getRotation(), spot);
		World.spawnObject(barrierObject);
		ResourceBarrier barrier = new ResourceBarrier(barrierObject, redTeamOwner, door, barrierTier, chunkKey);
		resourceBarriers.put(getTileKey(barrierObject), barrier);
	}

	private static boolean isResourceBarrierDoorSpot(WorldObject spot) {
		return spot != null && (spot.getId() == 39616 || spot.getId() == 39617);
	}

	private static int getResourceBarrierSlotIndex(WorldObject object) {
		int localX = object.getXInChunk();
		int localY = object.getYInChunk();
		if (localY == 1 && localX >= 1 && localX <= 6) {
			return localX - 1;
		}
		if (localX == 6 && localY >= 2 && localY <= 6) {
			return 6 + (localY - 2);
		}
		if (localY == 6 && localX >= 1 && localX <= 5) {
			return 11 + (5 - localX);
		}
		if (localX == 1 && localY >= 2 && localY <= 5) {
			return 16 + (5 - localY);
		}
		return Math.abs(object.getX() + object.getY() + object.getRotation()) % RESOURCE_BARRIER_SLOT_COUNT;
	}

	private static int getTileKey(WorldTile tile) {
		return tile == null ? 0 : tile.getTileHash();
	}

	private static int getChunkKey(WorldTile tile) {
		if (tile == null) {
			return 0;
		}
		return WorldTile.getHash(tile.getChunkX(), tile.getChunkY(), tile.getPlane());
	}

	private static boolean isOnBarrierTeam(Player player, boolean redTeamOwner) {
		return redTeamOwner ? isRedTeam(player) : isBlueTeam(player);
	}

	private static void passResourceBarrierDoor(Player player, WorldObject object) {
		if (player.isFrozen()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from moving.");
			return;
		}
		WorldTile target = getResourceBarrierPassTarget(player, object);
		if (target == null) {
			player.getPackets().sendGameMessage("You cannot pass through this barrier from here.");
			return;
		}
		forcePassBaseDoor(player, object, target);
	}

	private static WorldTile getResourceBarrierPassTarget(Player player, WorldObject object) {
		int distanceX = player.getX() - object.getX();
		int distanceY = player.getY() - object.getY();
		if (Math.abs(distanceX) + Math.abs(distanceY) == 1) {
			return new WorldTile(object.getX() - distanceX, object.getY() - distanceY, object.getPlane());
		}
		if (distanceX == 0 && distanceY == 0) {
			return getFaceTile(object, player);
		}
		return null;
	}

	private static void damageResourceBarrier(Player player, ResourceBarrier barrier) {
		player.setNextAnimation(new Animation(422));
		barrier.life--;
		if (barrier.life > 0) {
			player.getPackets().sendGameMessage("You damage the barrier. It has " + barrier.life + " hit"
					+ (barrier.life == 1 ? "" : "s") + " left.");
			return;
		}
		resourceBarriers.remove(getTileKey(barrier.object));
		World.removeObject(barrier.object, true);
		recordCombatDamage(player, 25 * (barrier.tier + 1));
		player.getPackets().sendGameMessage("You destroy the barrier.");
	}

	public static void resetPlayerInterface(Player player) {
		if (player == null || player.hasFinished()) {
			return;
		}
		boolean hadGameInterface = player.getInterfaceManager().containsInterface(GAME_INTERFACE);
		player.getInterfaceManager().removeMinigameHudInterface();
		if (player.getInterfaceManager().containsInterface(LOBBY_INTERFACE)
				|| player.getInterfaceManager().containsInterface(GAME_INTERFACE)) {
			player.getInterfaceManager().closeOverlay(false);
		}
		if (player.getInterfaceManager().containsInterface(KILN_INTERFACE)) {
			player.getInterfaceManager().removeInterface(KILN_INTERFACE);
		}
		if (player.getInterfaceManager().containsInterface(SCORE_INTERFACE)) {
			player.getInterfaceManager().removeInterface(SCORE_INTERFACE);
		}
		player.getPackets().sendConfig(SCORE_CONFIG, 0);
		player.getPackets().sendCSVarInteger(GAME_HUD_SCORE_VARC, 0);
		player.getPackets().sendCSVarString(GAME_HUD_SCORE_VARC, "");
		player.getPackets().sendCSVarInteger(GAME_HUD_TIMER_VARC, 0);
		if (hadGameInterface) {
			player.getPackets().sendIComponentText(GAME_INTERFACE, GAME_HUD_SCORE_COMPONENT, "Score: 0");
			player.getPackets().sendIComponentText(GAME_INTERFACE, GAME_HUD_TIMER_COMPONENT, "");
			player.getPackets().sendHideIComponent(GAME_INTERFACE, GAME_HUD_ENDING_BACKDROP_COMPONENT, true);
			player.getPackets().sendHideIComponent(GAME_INTERFACE, GAME_HUD_ONE_MINUTE_COMPONENT, true);
		}
	}

	public static boolean isBaseDoor(WorldObject object) {
		if (object == null) {
			return false;
		}
		return object.getId() == BLUE_DOOR_1 || object.getId() == BLUE_DOOR_2 || object.getId() == RED_DOOR_1
				|| object.getId() == RED_DOOR_2;
	}

	public static boolean canPassBaseDoor(Player player, WorldObject object) {
		if (!isBaseDoor(object)) {
			return false;
		}
		if (object.getId() == RED_DOOR_1 || object.getId() == RED_DOOR_2) {
			return isRedTeam(player);
		}
		return isBlueTeam(player);
	}

	public static boolean isBaseGateHelper(WorldObject object) {
		if (object == null) {
			return false;
		}
		return object.getId() == BLUE_GATE_HELPER || object.getId() == RED_GATE_HELPER;
	}

	public static boolean canPassBaseGateHelper(Player player, WorldObject object) {
		if (!isBaseGateHelper(object)) {
			return false;
		}
		if (object.getId() == RED_GATE_HELPER) {
			return isRedTeam(player);
		}
		return isBlueTeam(player);
	}

	public static boolean isOwnBaseGateHelperOnPlayerSide(Player player, WorldObject object) {
		if (player == null || object == null || !canPassBaseGateHelper(player, object) || gameMapChunks == null) {
			return false;
		}
		boolean red = isRedTeam(player);
		boolean playerInsideBase = isInsideOwnBase(player, red);
		boolean helperInsideBase = isInsideTeamBase(object, red);
		return playerInsideBase == helperInsideBase;
	}

	public static void passBaseGateHelper(Player player, WorldObject object) {
		if (player == null || object == null || !isBaseGateHelper(object)) {
			return;
		}
		if (player.isLocked()) {
			return;
		}
		if (player.isFrozen()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from moving.");
			return;
		}
		WorldTile target = getBaseGateHelperTargetTile(player, object);
		if (target == null) {
			player.getPackets().sendGameMessage("You cannot pass through this barrier from here.");
			return;
		}
		forcePassBaseDoor(player, object, target);
	}

	public static boolean passOwnBaseBarrier(Player player) {
		if (player == null || player.isLocked() || player.isFrozen()) {
			return false;
		}
		boolean red = isRedTeam(player);
		boolean blue = isBlueTeam(player);
		if (!red && !blue || !isInsideOwnBase(player, red)) {
			return false;
		}
		WorldTile target = getNearestOwnBaseExitTile(player, red);
		if (target == null) {
			return false;
		}
		forcePassBaseDoor(player, null, target);
		return true;
	}

	public static void passBaseDoor(final Player player, final WorldObject object) {
		if (player == null || object == null || !isBaseDoor(object)) {
			return;
		}
		if (player.isLocked()) {
			return;
		}
		if (player.isFrozen()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from moving.");
			return;
		}
		WorldTile directExit = getOwnBaseDoorExitTile(player, object);
		if (directExit != null) {
			forcePassBaseDoor(player, object, directExit);
			return;
		}
		player.lock(3);
		if (!setWalkToGate(object, player)) {
			player.unlock();
			return;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int step;

			@Override
			public void run() {
				if (step == 0 && !isAtGate(object, player)) {
					if (!player.hasWalkSteps() && player.getNextWalkDirection() == -1) {
						stop();
						player.unlock();
					}
					return;
				}
				if (step == 0) {
					WorldTile fromTile = new WorldTile(player.getX(), player.getY(), player.getPlane());
					WorldTile faceTile = getFaceTile(object, player);
					if (faceTile == null) {
						stop();
						player.unlock();
						return;
					}
					player.getPackets().sendGameMessage("You pass through the barrier.");
					player.setNextWorldTile(faceTile);
					player.setNextForceMovement(new ForceMovement(fromTile, 0, faceTile, 1,
							getFaceDirection(faceTile, player)));
					player.setNextAnimation(new Animation(10584));
					player.setNextGraphics(new Graphics(isRedTeam(player) ? 1871 : 1870));
					step++;
				} else if (step == 1) {
					stop();
					player.unlock();
				}
			}
		}, 0, 0);
	}

	private static WorldTile getOwnBaseDoorExitTile(Player player, WorldObject object) {
		boolean red = isRedTeam(player);
		boolean blue = isBlueTeam(player);
		if (!red && !blue || !isInsideOwnBase(player, red)) {
			return null;
		}
		switch (object.getRotation()) {
			case 0:
				return new WorldTile(object.getX() - 1, object.getY(), object.getPlane());
			case 1:
				return new WorldTile(object.getX(), object.getY() + 1, object.getPlane());
			case 2:
				return new WorldTile(object.getX() + 1, object.getY(), object.getPlane());
			case 3:
				return new WorldTile(object.getX(), object.getY() - 1, object.getPlane());
			default:
				return null;
		}
	}

	private static WorldTile getBaseGateHelperTargetTile(Player player, WorldObject object) {
		boolean red = isRedTeam(player);
		boolean blue = isBlueTeam(player);
		if (!red && !blue || gameMapChunks == null) {
			return null;
		}
		boolean insideBase = isInsideTeamBase(object, red);
		int distance = insideBase ? 2 : -2;
		switch (object.getRotation()) {
			case 0:
				return new WorldTile(object.getX() - distance, object.getY(), object.getPlane());
			case 1:
				return new WorldTile(object.getX(), object.getY() + distance, object.getPlane());
			case 2:
				return new WorldTile(object.getX() + distance, object.getY(), object.getPlane());
			case 3:
				return new WorldTile(object.getX(), object.getY() - distance, object.getPlane());
			default:
				return null;
		}
	}

	private static boolean isInsideTeamBase(WorldObject object, boolean red) {
		return isInsideTeamBase((WorldTile) object, red, 0);
	}

	private static boolean isInsideTeamBase(WorldTile tile, boolean red, int padding) {
		if (tile == null || gameMapChunks == null) {
			return false;
		}
		int localX = tile.getX() - (gameMapChunks[0] * 8);
		int localY = tile.getY() - (gameMapChunks[1] * 8);
		int baseOffset = red ? 0 : (GAME_MAP_CHUNKS - 1) * 8;
		return localX >= baseOffset - padding && localY >= baseOffset - padding
				&& localX < baseOffset + 8 + padding && localY < baseOffset + 8 + padding;
	}

	private static WorldTile getNearestOwnBaseExitTile(Player player, boolean red) {
		int blueBaseOffset = (GAME_MAP_CHUNKS - 1) * 8;
		int[][] exits = red ? new int[][] {
				{ 4, 8 },
				{ 8, 4 }
		} : new int[][] {
				{ blueBaseOffset + 4, blueBaseOffset - 1 },
				{ blueBaseOffset - 1, blueBaseOffset + 4 }
		};
		WorldTile nearest = null;
		int nearestDistance = Integer.MAX_VALUE;
		for (int[] exit : exits) {
			WorldTile tile = getGameMapTile(exit[0], exit[1]);
			int distance = Math.abs(player.getX() - tile.getX()) + Math.abs(player.getY() - tile.getY());
			if (distance < nearestDistance) {
				nearest = tile;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	private static void forcePassBaseDoor(final Player player, WorldObject object, final WorldTile toTile) {
		player.stopAll(true, false, true);
		WorldTile fromTile = new WorldTile(player.getX(), player.getY(), player.getPlane());
		player.lock(3);
		player.getPackets().sendGameMessage("You pass through the barrier.");
		player.setNextWorldTile(toTile);
		player.setNextForceMovement(new ForceMovement(fromTile, 0, toTile, 1, getFaceDirection(toTile, player)));
		player.setNextAnimation(new Animation(10584));
		player.setNextGraphics(new Graphics(isRedTeam(player) ? 1871 : 1870));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.unlock();
			}
		}, 1);
	}

	private static synchronized boolean finishGame(boolean forced) {
		if (!gameTaskRunning || gameEnding) {
			return false;
		}
		gameEnding = true;
		List<Player> players = new ArrayList<Player>();
		players.addAll(redTeam);
		players.addAll(blueTeam);
		List<StealingCreationScore> scores = getScoreSnapshotUnsafe();
		if (scores.isEmpty()) {
			for (Player player : players) {
				if (player != null && !player.hasFinished()) {
					scores.add(new StealingCreationScore(player.getDisplayName(), redTeam.contains(player)));
				}
			}
		}
		int redBaseTotal = StealingCreationScore.total(scores, true, false);
		int blueBaseTotal = StealingCreationScore.total(scores, false, false);
		int winner = redBaseTotal > blueBaseTotal ? 2 : blueBaseTotal > redBaseTotal ? 1 : 0;
		int redTotal = StealingCreationScore.total(scores, true, winner == 2);
		int blueTotal = StealingCreationScore.total(scores, false, winner == 1);
		stopGameTask();
		transferringToGame = false;
		for (Player player : players) {
			if (player == null || player.hasFinished()) {
				continue;
			}
			boolean red = redTeam.contains(player);
			StealingCreationScore personal = getScore(player);
			if (personal == null) {
				personal = new StealingCreationScore(player.getDisplayName(), red);
			} else {
				personal = personal.copy();
			}
			player.stopAll();
			player.setCanPvp(false);
			removeStealingCreationItems(player);
			resetPlayerInterface(player);
			if (player.getControlerManager() != null) {
				player.getControlerManager().forceStop();
			}
			player.setNextWorldTile(LOBBY_WORLDTILE);
			displayScoreScreen(player, personal, scores, winner, redTotal, blueTotal);
			player.getPackets().sendGameMessage("The Stealing Creation match has ended"
					+ (forced ? " by an administrator." : "."));
		}
		redTeam.clear();
		blueTeam.clear();
		gameScores.clear();
		destroyGameMap();
		gameEnding = false;
		return true;
	}

	private static void displayScoreScreen(Player player, StealingCreationScore personal,
			List<StealingCreationScore> scores, int winner, int redTotal, int blueTotal) {
		player.getInterfaceManager().sendInterface(SCORE_INTERFACE);
		sendScoreScreenVars(player, personal, scores, winner, redTotal, blueTotal);
		scheduleScoreScreenRefresh(player, personal, scores, winner, redTotal, blueTotal, 1);
		scheduleScoreScreenRefresh(player, personal, scores, winner, redTotal, blueTotal, 3);
		scheduleScoreScreenRefresh(player, personal, scores, winner, redTotal, blueTotal, 6);
		boolean red = personal.isRedTeam();
		player.getPackets().sendGameMessage("Blue team: " + blueTotal + " points. Red team: " + redTotal + " points.");
		if (winner == 0) {
			player.getPackets().sendGameMessage("The match ends in a draw.");
		} else if (winner == (red ? 2 : 1)) {
			player.getPackets().sendGameMessage("Your team is victorious. Your score receives a 10% victory bonus.");
		} else {
			player.getPackets().sendGameMessage("Your team is defeated.");
		}
		player.getPackets().sendGameMessage("Your total score: " + personal.total((red ? 2 : 1) == winner)
				+ " points.");
	}

	private static void scheduleScoreScreenRefresh(final Player player, final StealingCreationScore personal,
			final List<StealingCreationScore> scores, final int winner, final int redTotal, final int blueTotal,
			int delay) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (player != null && !player.hasFinished()
						&& player.getInterfaceManager().containsInterface(SCORE_INTERFACE)) {
					sendScoreScreenVars(player, personal, scores, winner, redTotal, blueTotal);
				}
			}
		}, delay);
	}

	private static void sendScoreScreenVars(Player player, StealingCreationScore personal,
			List<StealingCreationScore> scores, int winner, int redTotal, int blueTotal) {
		boolean red = personal.isRedTeam();
		player.getPackets().sendCSVarInteger(588, red ? 2 : 1);
		player.getPackets().sendCSVarInteger(589, winner);
		player.getPackets().sendCSVarInteger(598, red ? redTotal : blueTotal);
		player.getPackets().sendCSVarInteger(597, red ? blueTotal : redTotal);
		player.getPackets().sendCSVarInteger(590, personal.getGathering());
		player.getPackets().sendCSVarInteger(591, personal.getProcessing());
		player.getPackets().sendCSVarInteger(592, personal.getNetDepositScore());
		player.getPackets().sendCSVarInteger(593, personal.getDamaging());
		player.getPackets().sendCSVarInteger(594, personal.getKills());
		player.getPackets().sendCSVarInteger(595, personal.getDeaths());
		player.getPackets().sendCSVarInteger(596, personal.total((red ? 2 : 1) == winner));
		sendAward(player, 45, 608, 600, StealingCreationScore.mostGathered(scores), ScoreKind.GATHERING, winner);
		sendAward(player, 44, 607, 599, StealingCreationScore.mostProcessed(scores), ScoreKind.PROCESSING, winner);
		sendAward(player, 49, 612, 604, StealingCreationScore.mostDeposited(scores), ScoreKind.DEPOSITING, winner);
		sendAward(player, 48, 611, 603, StealingCreationScore.mostDamaged(scores), ScoreKind.DAMAGING, winner);
		sendAward(player, 46, 609, 601, StealingCreationScore.mostKills(scores), ScoreKind.KILLS, winner);
		sendAward(player, 47, 610, 602, StealingCreationScore.mostDeaths(scores), ScoreKind.DEATHS, winner);
		sendAward(player, 50, 613, 605, StealingCreationScore.highestTotal(scores, winner), ScoreKind.TOTAL, winner);
		sendAward(player, 51, 614, 606, StealingCreationScore.lowestTotal(scores, winner), ScoreKind.TOTAL, winner);
		player.getPackets().sendExecuteScript(SCORE_AWARDS_REFRESH_SCRIPT);
	}

	private static void sendAward(Player player, int stringConfig, int teamConfig, int valueConfig,
			StealingCreationScore score, ScoreKind kind, int winner) {
		if (score == null) {
			player.getPackets().sendCSVarString(stringConfig, "");
			player.getPackets().sendCSVarInteger(teamConfig, 0);
			player.getPackets().sendCSVarInteger(valueConfig, 0);
			return;
		}
		player.getPackets().sendCSVarString(stringConfig, score.getName());
		player.getPackets().sendCSVarInteger(teamConfig, score.isRedTeam() ? 2 : 1);
		player.getPackets().sendCSVarInteger(valueConfig, getScoreValue(score, kind, winner));
	}

	private static int getScoreValue(StealingCreationScore score, ScoreKind kind, int winner) {
		switch (kind) {
			case GATHERING:
				return score.getGathering();
			case PROCESSING:
				return score.getProcessing();
			case DEPOSITING:
				return score.getNetDepositScore();
			case DAMAGING:
				return score.getDamaging();
			case KILLS:
				return score.getKills();
			case DEATHS:
				return score.getDeaths();
			default:
				return score.total((score.isRedTeam() ? 2 : 1) == winner);
		}
	}

	public static void removeStealingCreationItems(Player player) {
		for (Item item : player.getInventory().getItems().getItemsCopy()) {
			if (item != null && isStealingCreationItem(item.getId())) {
				player.getInventory().deleteItem(new Item(item.getId(), item.getAmount()));
			}
		}
		for (Item item : player.getEquipment().getItems().getItemsCopy()) {
			if (item != null && isStealingCreationItem(item.getId())) {
				player.getEquipment().deleteItem(item.getId(), item.getAmount());
			}
		}
		player.getAppearence().generateAppearenceData();
	}

	private static List<StealingCreationScore> getScoreSnapshotUnsafe() {
		List<StealingCreationScore> scores = new ArrayList<StealingCreationScore>();
		for (StealingCreationScore score : gameScores.values()) {
			if (score != null) {
				scores.add(score.copy());
			}
		}
		return scores;
	}

	private static String formatTime(int seconds) {
		seconds = Math.max(0, seconds);
		int minutes = seconds / 60;
		int remainingSeconds = seconds % 60;
		return minutes + ":" + (remainingSeconds < 10 ? "0" : "") + remainingSeconds;
	}

	private static String getScoreName(StealingCreationScore score) {
		return score == null ? "none" : score.getName();
	}

	private enum ScoreKind {
		GATHERING,
		PROCESSING,
		DEPOSITING,
		DAMAGING,
		KILLS,
		DEATHS,
		TOTAL
	}

	private static class ResourceBarrier {
		private final WorldObject object;
		private final boolean redTeam;
		private final boolean door;
		private final int tier;
		private final int chunkKey;
		private final int maxLife;
		private int life;

		private ResourceBarrier(WorldObject object, boolean redTeam, boolean door, int tier, int chunkKey) {
			this.object = object;
			this.redTeam = redTeam;
			this.door = door;
			this.tier = tier;
			this.chunkKey = chunkKey;
			this.maxLife = 3 + tier;
			this.life = maxLife;
		}
	}

	public static synchronized String getStatus() {
		pruneTeams();
		List<StealingCreationScore> scores = getScoreSnapshotUnsafe();
		int redScore = StealingCreationScore.total(scores, true, false);
		int blueScore = StealingCreationScore.total(scores, false, false);
		return "Red: " + redTeam.size() + ", blue: " + blueTeam.size() + ", required per team: "
				+ REQUIRED_PLAYERS_PER_TEAM + ", ready: " + hasRequiredPlayersUnsafe() + ", lobby timer: "
				+ (lobbyTaskRunning ? lobbyCountdownMinutes + " minute(s)" : "stopped") + ", game timer: "
				+ (gameTaskRunning ? formatTime(gameSecondsRemaining) : "stopped") + ", score red=" + redScore
				+ " blue=" + blueScore + ".";
	}

	public static synchronized String getScoreDebug() {
		List<StealingCreationScore> scores = getScoreSnapshotUnsafe();
		int redScore = StealingCreationScore.total(scores, true, false);
		int blueScore = StealingCreationScore.total(scores, false, false);
		StealingCreationScore gathered = StealingCreationScore.mostGathered(scores);
		StealingCreationScore processed = StealingCreationScore.mostProcessed(scores);
		StealingCreationScore deposited = StealingCreationScore.mostDeposited(scores);
		StealingCreationScore damaged = StealingCreationScore.mostDamaged(scores);
		return "SC score red=" + redScore + ", blue=" + blueScore + ", remaining="
				+ (gameTaskRunning ? formatTime(gameSecondsRemaining) : "stopped") + ", top gatherer="
				+ getScoreName(gathered) + ", top processor=" + getScoreName(processed) + ", top depositor="
				+ getScoreName(deposited) + ", top damage=" + getScoreName(damaged) + ".";
	}

	private static void createGameMap() {
		destroyGameMap();
		gameMapChunks = MapBuilder.findEmptyChunkBound(GAME_MAP_CHUNKS, GAME_MAP_CHUNKS);
		gameMapFlags = calculateGameMapFlags(GAME_MAP_CHUNKS);
		primaryKilnTile = null;
		processingKilnTiles.clear();
		copyGeneratedGameMap();
		loadGameMapRegions();
	}

	private static void clearResourceBarriers() {
		if (resourceBarriers.isEmpty()) {
			return;
		}
		for (ResourceBarrier barrier : new ArrayList<ResourceBarrier>(resourceBarriers.values())) {
			World.removeObject(barrier.object, true);
		}
		resourceBarriers.clear();
	}

	private static void destroyGameMap() {
		clearResourceBarriers();
		if (gameMapChunks == null) {
			return;
		}
		MapBuilder.destroyMap(gameMapChunks[0], gameMapChunks[1], GAME_MAP_CHUNKS, GAME_MAP_CHUNKS);
		gameMapChunks = null;
		gameMapFlags = null;
		primaryKilnTile = null;
		processingKilnTiles.clear();
	}

	private static WorldTile getGameMapTile(int localX, int localY) {
		return new WorldTile((gameMapChunks[0] * 8) + localX, (gameMapChunks[1] * 8) + localY, 0);
	}

	private static int[][] calculateGameMapFlags(int size) {
		int[][] flags = new int[size][size];
		for (int x = 0; x < size; x++) {
			Arrays.fill(flags[x], TYPE_EMPTY);
		}
		setGameMapFlag(flags, 0, 0, TYPE_BASE, 0, 0);
		setGameMapFlag(flags, 0, 1, TYPE_RESERVED, 0, 0);
		setGameMapFlag(flags, 1, 0, TYPE_RESERVED, 1, 0);
		setGameMapFlag(flags, 1, 1, TYPE_RESERVED, 2, 0);
		setGameMapFlag(flags, size - 1, size - 1, TYPE_BASE, 0, 2);
		setGameMapFlag(flags, size - 1, size - 2, TYPE_RESERVED, 0, 2);
		setGameMapFlag(flags, size - 2, size - 1, TYPE_RESERVED, 1, 2);
		setGameMapFlag(flags, size - 2, size - 2, TYPE_RESERVED, 2, 2);
		int total = size * size;
		int skillPlots = (int) (total * 0.30F);
		int obstacles = (int) (total * 0.20F);
		while (skillPlots-- > 0) {
			int tier = skillPlots == 0 ? 5 : randomInclusive(1, 5);
			setRandomGameMapFlag(flags, 100, 0, 0, size, size, TYPE_SKILL_ROCK + random(4), tier, random(4));
		}
		while (obstacles-- > 0) {
			int type = TYPE_RIFT + random(5);
			int rotation = type == TYPE_FOG ? 3 : random(4);
			setRandomGameMapFlag(flags, 100, 0, 0, size, size, type, 0, rotation);
		}
		int kilnsBlue = randomInclusive(1, 2);
		int kilnsRed = randomInclusive(1, 2);
		while (kilnsBlue-- > 0) {
			setRandomGameMapFlag(flags, 100, 0, 0, size / 2, size / 2, TYPE_KILN, 0, random(4));
		}
		while (kilnsRed-- > 0) {
			setRandomGameMapFlag(flags, 100, size / 2, size / 2, size, size, TYPE_KILN, 0, random(4));
		}
		return flags;
	}

	private static void setGameMapFlag(int[][] flags, int x, int y, int type, int tier, int rotation) {
		flags[x][y] = type | (tier << 4) | (rotation << 8);
	}

	private static boolean setRandomGameMapFlag(int[][] flags, int attempts, int minX, int minY, int maxX, int maxY,
			int type, int tier, int rotation) {
		while (attempts-- > 0) {
			int x = minX + random(maxX - minX);
			int y = minY + random(maxY - minY);
			if (getGameMapType(flags, x, y) == TYPE_EMPTY) {
				setGameMapFlag(flags, x, y, type, tier, rotation);
				return true;
			}
		}
		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				if (getGameMapType(flags, x, y) == TYPE_EMPTY) {
					setGameMapFlag(flags, x, y, type, tier, rotation);
					return true;
				}
			}
		}
		return false;
	}

	private static void copyGeneratedGameMap() {
		for (int x = 0; x < gameMapFlags.length; x++) {
			for (int y = 0; y < gameMapFlags.length; y++) {
				int type = getGameMapType(x, y);
				int tier = getGameMapTier(x, y);
				int rotation = getGameMapRotation(x, y);
				int[] source = getSourceChunkForGameMapTile(type, tier);
				MapBuilder.copyChunk(source[0], source[1], 0, gameMapChunks[0] + x, gameMapChunks[1] + y, 0, rotation);
				if (type == TYPE_KILN) {
					WorldTile kilnTile = getGameMapTile((x * 8) + KILN_OBJECT_LOCAL_TILE[0],
							(y * 8) + KILN_OBJECT_LOCAL_TILE[1]);
					processingKilnTiles.add(kilnTile);
					if (primaryKilnTile == null) {
						primaryKilnTile = kilnTile;
					}
				}
			}
		}
		spawnBaseDoors();
		spawnBaseGateHelpers();
	}

	private static void spawnBaseDoors() {
		int blueBaseOffset = (GAME_MAP_CHUNKS - 1) * 8;
		spawnBaseDoor(RED_DOOR_1, 0, 1, BLUE_DOOR_P1[0], BLUE_DOOR_P1[1]);
		spawnBaseDoor(RED_DOOR_2, 0, 1, BLUE_DOOR_P2[0], BLUE_DOOR_P2[1]);
		spawnBaseDoor(RED_DOOR_1, 0, 2, BLUE_DOOR_P3[0], BLUE_DOOR_P3[1]);
		spawnBaseDoor(RED_DOOR_2, 0, 2, BLUE_DOOR_P4[0], BLUE_DOOR_P4[1]);
		spawnBaseDoor(BLUE_DOOR_1, 0, 3, blueBaseOffset + RED_DOOR_P1[0], blueBaseOffset + RED_DOOR_P1[1]);
		spawnBaseDoor(BLUE_DOOR_2, 0, 3, blueBaseOffset + RED_DOOR_P2[0], blueBaseOffset + RED_DOOR_P2[1]);
		spawnBaseDoor(BLUE_DOOR_1, 0, 0, blueBaseOffset + RED_DOOR_P3[0], blueBaseOffset + RED_DOOR_P3[1]);
		spawnBaseDoor(BLUE_DOOR_2, 0, 0, blueBaseOffset + RED_DOOR_P4[0], blueBaseOffset + RED_DOOR_P4[1]);
	}

	private static void spawnBaseDoor(int id, int type, int rotation, int localX, int localY) {
		WorldObject door = new WorldObject(id, type, rotation, getGameMapTile(localX, localY));
		World.spawnObject(door);
		World.getRegion(door.getRegionId()).unclip(door, door.getXInRegion(), door.getYInRegion());
	}

	private static void spawnBaseGateHelpers() {
		int blueBaseOffset = (GAME_MAP_CHUNKS - 1) * 8;
		spawnBaseGateHelper(RED_GATE_HELPER, 1, 4, 6);
		spawnBaseGateHelper(RED_GATE_HELPER, 1, 4, 8);
		spawnBaseGateHelper(RED_GATE_HELPER, 2, 6, 4);
		spawnBaseGateHelper(RED_GATE_HELPER, 2, 8, 4);
		spawnBaseGateHelper(BLUE_GATE_HELPER, 3, blueBaseOffset + 4, blueBaseOffset + 1);
		spawnBaseGateHelper(BLUE_GATE_HELPER, 3, blueBaseOffset + 4, blueBaseOffset - 1);
		spawnBaseGateHelper(BLUE_GATE_HELPER, 0, blueBaseOffset + 1, blueBaseOffset + 4);
		spawnBaseGateHelper(BLUE_GATE_HELPER, 0, blueBaseOffset - 1, blueBaseOffset + 4);
	}

	private static void spawnBaseGateHelper(int id, int rotation, int localX, int localY) {
		WorldObject helper = new WorldObject(id, 10, rotation, getGameMapTile(localX, localY));
		World.spawnObject(helper);
		World.getRegion(helper.getRegionId()).unclip(helper, helper.getXInRegion(), helper.getYInRegion());
	}

	private static int[] getSourceChunkForGameMapTile(int type, int tier) {
		int[] source;
		switch (type) {
			case TYPE_RESERVED:
				source = tier == 0 ? CHUNK_RESERVED_1 : tier == 1 ? CHUNK_RESERVED_2 : CHUNK_RESERVED_3;
				break;
			case TYPE_BASE:
				source = CHUNK_BASE;
				break;
			case TYPE_RIFT:
				source = CHUNK_RIFT;
				break;
			case TYPE_WALL:
				source = CHUNK_WALL;
				break;
			case TYPE_FOG:
				source = CHUNK_FOG;
				break;
			case TYPE_LARGE_ROCK:
				source = CHUNK_LARGE_ROCK;
				break;
			case TYPE_ALTAR:
				source = CHUNK_ALTAR;
				break;
			case TYPE_KILN:
				source = CHUNK_KILN;
				break;
			case TYPE_SKILL_ROCK:
				return getTieredResourceChunk(CHUNK_SKILL_ROCK, tier);
			case TYPE_SKILL_TREE:
				return getTieredResourceChunk(CHUNK_SKILL_TREE, tier);
			case TYPE_SKILL_POOL:
				return getTieredResourceChunk(CHUNK_SKILL_POOL, tier);
			case TYPE_SKILL_SWARM:
				return getTieredResourceChunk(CHUNK_SKILL_SWARM, tier);
			default:
				source = CHUNK_EMPTY;
				break;
		}
		return source;
	}

	private static int[] getTieredResourceChunk(int[] source, int tier) {
		if (tier <= 0) {
			return CHUNK_EMPTY;
		}
		return new int[] { source[0] - Math.min(5, tier), source[1] };
	}

	private static int getGameMapType(int x, int y) {
		return getGameMapType(gameMapFlags, x, y);
	}

	private static int getGameMapType(int[][] flags, int x, int y) {
		return flags[x][y] & 0xF;
	}

	private static int getGameMapTier(int x, int y) {
		return (gameMapFlags[x][y] >> 4) & 0xF;
	}

	private static int getGameMapRotation(int x, int y) {
		return (gameMapFlags[x][y] >> 8) & 0x3;
	}

	private static int random(int bound) {
		return ThreadLocalRandom.current().nextInt(bound);
	}

	private static int randomInclusive(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static int getDistance(WorldTile fromTile, WorldTile toTile) {
		if (fromTile == null || toTile == null) {
			return Integer.MAX_VALUE;
		}
		return Math.abs(fromTile.getX() - toTile.getX()) + Math.abs(fromTile.getY() - toTile.getY());
	}

	private static boolean setWalkToGate(WorldObject gate, Player player) {
		if (player.getX() == gate.getX() && player.getY() == gate.getY()) {
			return true;
		}
		if (gate.getRotation() == 0) {
			if (player.getX() == gate.getX() - 1 && player.getY() == gate.getY()) {
				return true;
			}
			return player.addWalkSteps(gate.getX(), gate.getY()) || player.addWalkSteps(gate.getX() - 1, gate.getY());
		} else if (gate.getRotation() == 1) {
			if (player.getX() == gate.getX() && player.getY() == gate.getY() + 1) {
				return true;
			}
			return player.addWalkSteps(gate.getX(), gate.getY()) || player.addWalkSteps(gate.getX(), gate.getY() + 1);
		} else if (gate.getRotation() == 2) {
			if (player.getX() == gate.getX() + 1 && player.getY() == gate.getY()) {
				return true;
			}
			return player.addWalkSteps(gate.getX(), gate.getY()) || player.addWalkSteps(gate.getX() + 1, gate.getY());
		} else if (gate.getRotation() == 3) {
			if (player.getX() == gate.getX() && player.getY() == gate.getY() - 1) {
				return true;
			}
			return player.addWalkSteps(gate.getX(), gate.getY()) || player.addWalkSteps(gate.getX(), gate.getY() - 1);
		}
		return false;
	}

	private static boolean isAtGate(WorldObject gate, Player player) {
		if (player.getX() == gate.getX() && player.getY() == gate.getY()) {
			return true;
		}
		if (gate.getRotation() == 0) {
			return player.getX() == gate.getX() - 1 && player.getY() == gate.getY();
		} else if (gate.getRotation() == 1) {
			return player.getX() == gate.getX() && player.getY() == gate.getY() + 1;
		} else if (gate.getRotation() == 2) {
			return player.getX() == gate.getX() + 1 && player.getY() == gate.getY();
		} else if (gate.getRotation() == 3) {
			return player.getX() == gate.getX() && player.getY() == gate.getY() - 1;
		}
		return false;
	}

	private static WorldTile getFaceTile(WorldObject gate, Player player) {
		if (player.getX() != gate.getX() || player.getY() != gate.getY()) {
			return new WorldTile(gate.getX(), gate.getY(), gate.getPlane());
		}
		if (gate.getRotation() == 0) {
			return new WorldTile(gate.getX() - 1, gate.getY(), gate.getPlane());
		} else if (gate.getRotation() == 1) {
			return new WorldTile(gate.getX(), gate.getY() + 1, gate.getPlane());
		} else if (gate.getRotation() == 2) {
			return new WorldTile(gate.getX() + 1, gate.getY(), gate.getPlane());
		} else if (gate.getRotation() == 3) {
			return new WorldTile(gate.getX(), gate.getY() - 1, gate.getPlane());
		}
		return null;
	}

	private static int getFaceDirection(WorldTile faceTile, Player player) {
		if (player.getX() < faceTile.getX()) {
			return ForceMovement.EAST;
		} else if (player.getX() > faceTile.getX()) {
			return ForceMovement.WEST;
		} else if (player.getY() < faceTile.getY()) {
			return ForceMovement.NORTH;
		} else if (player.getY() > faceTile.getY()) {
			return ForceMovement.SOUTH;
		}
		return 0;
	}

	private static void loadGameMapRegions() {
		int startRegionX = gameMapChunks[0] >> 3;
		int startRegionY = gameMapChunks[1] >> 3;
		for (int regionX = startRegionX; regionX <= ((gameMapChunks[0] + GAME_MAP_CHUNKS - 1) >> 3); regionX++) {
			for (int regionY = startRegionY; regionY <= ((gameMapChunks[1] + GAME_MAP_CHUNKS - 1) >> 3); regionY++) {
				World.getRegion((regionX << 8) | regionY).quickLoad();
			}
		}
	}

	public static synchronized void updateInterfaces() {
		boolean ready = hasRequiredPlayersUnsafe();
		for (Player player : new ArrayList<Player>(redTeam)) {
			if (!isLobbyInterfaceReady(player)) {
				continue;
			}
			updateTeamInterface(player, true, ready);
		}
		for (Player player : new ArrayList<Player>(blueTeam)) {
			if (!isLobbyInterfaceReady(player)) {
				continue;
			}
			updateTeamInterface(player, false, ready);
		}
	}

	private static boolean isLobbyInterfaceReady(Player player) {
		return player != null && !player.hasFinished() && player.getControlerManager() != null
				&& player.getControlerManager().getControler() instanceof StealingCreationLobby
				&& player.getInterfaceManager().containsInterface(LOBBY_INTERFACE);
	}

	public static void updateTeamInterface(Player player, boolean inRedTeam, boolean ready) {
		int skillTotal = getTotalLevel(TOTAL_SKILL_IDS, inRedTeam);
		int combatTotal = getTotalLevel(TOTAL_COMBAT_IDS, inRedTeam);
		int otherSkillTotal = getTotalLevel(TOTAL_SKILL_IDS, !inRedTeam);
		int otherCombatTotal = getTotalLevel(TOTAL_COMBAT_IDS, !inRedTeam);
		int teamSize = inRedTeam ? redTeam.size() : blueTeam.size();
		int otherTeamSize = inRedTeam ? blueTeam.size() : redTeam.size();
		int teamWaiting = ready ? teamSize : Math.max(0, REQUIRED_PLAYERS_PER_TEAM - teamSize);
		int enemyWaiting = ready ? otherTeamSize : Math.max(0, REQUIRED_PLAYERS_PER_TEAM - otherTeamSize);
		player.getPackets().sendHideIComponent(LOBBY_INTERFACE, LOBBY_GAME_START_COMPONENT, !ready);
		player.getPackets().sendHideIComponent(LOBBY_INTERFACE, LOBBY_WAITING_CONTAINER_COMPONENT, ready);
		player.getPackets().sendCSVarInteger(LOBBY_COUNTDOWN_VARC, ready ? Math.max(0, lobbyCountdownMinutes) : 0);
		player.getPackets().sendCSVarInteger(LOBBY_TEAM_WAITING_VARC, teamWaiting);
		player.getPackets().sendCSVarInteger(LOBBY_ENEMY_WAITING_VARC, enemyWaiting);
		player.getPackets().sendIComponentText(LOBBY_INTERFACE, LOBBY_TEAM_SKILL_TOTAL_COMPONENT, "" + skillTotal);
		player.getPackets().sendIComponentText(LOBBY_INTERFACE, LOBBY_TEAM_COMBAT_TOTAL_COMPONENT, "" + combatTotal);
		player.getPackets().sendIComponentText(LOBBY_INTERFACE, LOBBY_ENEMY_SKILL_TOTAL_COMPONENT, "" + otherSkillTotal);
		player.getPackets().sendIComponentText(LOBBY_INTERFACE, LOBBY_ENEMY_COMBAT_TOTAL_COMPONENT, "" + otherCombatTotal);
		player.getPackets().sendIComponentText(LOBBY_INTERFACE, LOBBY_TEAM_WAITING_COMPONENT, "" + teamWaiting);
		player.getPackets().sendIComponentText(LOBBY_INTERFACE, LOBBY_ENEMY_WAITING_COMPONENT, "" + enemyWaiting);
	}

	private static class GameTimer extends FixedLengthRunnable {

		@Override
		public boolean repeat() {
			synchronized (StealingCreation.class) {
				if (!gameTaskRunning || gameEnding) {
					return false;
				}
				pruneTeams();
				if (redTeam.isEmpty() || blueTeam.isEmpty() || gameSecondsRemaining-- <= 0) {
					finishGame(false);
					return false;
				}
				if (gameSecondsRemaining == 600 || gameSecondsRemaining == 300 || gameSecondsRemaining == 60
						|| gameSecondsRemaining == 30 || gameSecondsRemaining == 10) {
					for (Player player : redTeam) {
						if (player != null && !player.hasFinished()) {
							player.getPackets().sendGameMessage(
									"The Stealing Creation match ends in " + formatTime(gameSecondsRemaining) + ".");
						}
					}
					for (Player player : blueTeam) {
						if (player != null && !player.hasFinished()) {
							player.getPackets().sendGameMessage(
									"The Stealing Creation match ends in " + formatTime(gameSecondsRemaining) + ".");
						}
					}
				}
				for (Player player : redTeam) {
					refreshGameHud(player);
				}
				for (Player player : blueTeam) {
					refreshGameHud(player);
				}
				return true;
			}
		}
	}

	private static class PickpocketItem {
		private final int slot;
		private final Item item;

		private PickpocketItem(int slot, Item item) {
			this.slot = slot;
			this.item = item;
		}
	}

	private static class LobbyTimer extends FixedLengthRunnable {

		@Override
		public boolean repeat() {
			synchronized (StealingCreation.class) {
				pruneTeams();
				if (redTeam.isEmpty() && blueTeam.isEmpty()) {
					lobbyTaskRunning = false;
					lobbyTask = null;
					lobbyCountdownMinutes = LOBBY_WAIT_MINUTES;
					return false;
				}
				updateInterfaces();
				if (!hasRequiredPlayersUnsafe()) {
					lobbyCountdownMinutes = LOBBY_WAIT_MINUTES;
					return true;
				}
				if (lobbyCountdownMinutes-- <= 0) {
					return !passToGame();
				}
				return true;
			}
		}
	}
}
