package com.rs.game.activites;

import com.rs.cores.CoresManager;
import com.rs.cores.TrackedRunnable;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.others.CastleWarBarricade;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.Foods.Food;
import com.rs.game.player.controllers.castlewars.CastleWarsPlaying;
import com.rs.game.player.dialogue.impl.CastleWarsScoreboard;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class CastleWars {
	public static final int CW_TICKET = 4067;
	public static final int SARADOMIN = 0;
	public static final int ZAMORAK = 1;
	public static final WorldTile LOBBY = new WorldTile(2442, 3090, 0), SARA_WAITING = new WorldTile(2381, 9489, 0),
			ZAMO_WAITING = new WorldTile(2421, 9523, 0), SARA_BASE = new WorldTile(2426, 3076, 1),
			ZAMO_BASE = new WorldTile(2373, 3131, 1);
	private static final int GUTHIX = 2;
	private static final int OLD_SARADOMIN_PORTAL = 4387, OLD_ZAMORAK_PORTAL = 4388, OLD_GUTHIX_PORTAL = 4408;
	private static final int SARADOMIN_PORTAL = 83631, ZAMORAK_PORTAL = 83638, GUTHIX_PORTAL = 83642;
	@SuppressWarnings("unchecked")
	private static final List<Player>[] waiting = new List[2];
	@SuppressWarnings("unchecked")
	private static final List<Player>[] playing = new List[2];
	private static final int[] seasonWins = new int[2];
	private static PlayingGame playingGame;
	private static boolean endingGame;

	static {
		init();
	}

	public static void init() {
		for (int i = 0; i < waiting.length; i++)
			waiting[i] = Collections.synchronizedList(new LinkedList<Player>());
		for (int i = 0; i < playing.length; i++)
			playing[i] = Collections.synchronizedList(new LinkedList<Player>());
	}

	public static void viewScoreBoard(Player player) {
		player.getInterfaceManager().sendChatBoxInterface(55);
		player.getPackets().sendIComponentText(55, 2, "Saradomin: " + seasonWins[SARADOMIN]);
		player.getPackets().sendIComponentText(55, 3, "Zamorak: " + seasonWins[ZAMORAK]);
	}

	public static int getPowerfullestTeam() {
		int zamorak = waiting[ZAMORAK].size() + playing[ZAMORAK].size();
		int saradomin = waiting[SARADOMIN].size() + playing[SARADOMIN].size();
		if (saradomin == zamorak)
			return GUTHIX;
		if (zamorak > saradomin)
			return ZAMORAK;
		return SARADOMIN;
	}

	public static void joinPortal(Player player, int team) {
		if (player.getEquipment().getHatId() != -1 || player.getEquipment().getCapeId() != -1) {
			player.getPackets().sendGameMessage("You cannot wear hats, capes or helms in the arena.");
			return;
		}
		for (Item item : player.getInventory().getItems().getItems()) {
			if (item == null)
				continue;
			if (Food.forId(item.getId()) != null) {
				player.getPackets().sendGameMessage("You cannot bring food into the arena.");
				return;
			}
		}
		int powerfullestTeam = getPowerfullestTeam();
		if (team == GUTHIX) {
			team = powerfullestTeam == ZAMORAK ? SARADOMIN : ZAMORAK;
		} else if (team == powerfullestTeam) {
			if (team == ZAMORAK)
				player.getPackets().sendGameMessage(
						"The Zamorak team is powerful enough already! Guthix demands balance - join the Saradomin team instead!");
			else if (team == SARADOMIN)
				player.getPackets().sendGameMessage(
						"The Saradomin team is powerful enough already! Guthix demands balance - join the Zamorak team instead!");
			return;
		}
		player.lock(2);
		waiting[team].add(player);
		setCape(player, new Item(team == ZAMORAK ? 4042 : 4041));
		setHood(player, new Item(team == ZAMORAK ? 4515 : 4513));
		player.getControlerManager().startControler("CastleWarsWaiting", team);
		player.setNextWorldTile(new WorldTile(team == ZAMORAK ? ZAMO_WAITING : SARA_WAITING, 1));
		player.getMusicsManager().playMusic(318); // temp testing else 5
		if (playingGame == null && waiting[team].size() >= 5) // at least
			// 9players to
			// start
			createPlayingGame();
		else
			refreshTimeLeft(player);
		// You cannot take non-combat items into the arena
	}

	public static void setHood(Player player, Item hood) {
		player.getEquipment().getItems().set(Equipment.SLOT_HAT, hood);
		player.getEquipment().refresh(Equipment.SLOT_HAT);
		player.getAppearence().generateAppearenceData();
	}

	public static void setCape(Player player, Item cape) {
		player.getEquipment().getItems().set(Equipment.SLOT_CAPE, cape);
		player.getEquipment().refresh(Equipment.SLOT_CAPE);
		player.getAppearence().generateAppearenceData();
	}

	public static void setWeapon(Player player, Item weapon) {
		player.getEquipment().getItems().set(Equipment.SLOT_WEAPON, weapon);
		player.getEquipment().refresh(Equipment.SLOT_WEAPON);
		player.getAppearence().generateAppearenceData();
	}

	public static void createPlayingGame() {
		playingGame = new PlayingGame();
		CoresManager.getServiceProvider().scheduleAndTrackRepeatingTask(playingGame, 1, 1, TimeUnit.MINUTES);
		//CoresManager.scheduleRepeatedTask(playingGame, 1, 1, TimeUnit.MINUTES);
		//CoresManager.fastExecutor.scheduleAtFixedRate(playingGame, 60000, 60000);
		refreshAllPlayersTime();
	}

	public static void destroyPlayingGame() {
		if (playingGame == null)
			return;
		playingGame.reset();
		CoresManager.getServiceProvider().cancelTrackedTask(playingGame.getTrackingKey(), true);
		playingGame = null;
		refreshAllPlayersTime();
		leavePlayersSafely();
	}

	public static void leavePlayersSafely() {
		leavePlayersSafely(-1);
	}

	public static void leavePlayersSafely(final int winner) {
		if (winner != -1)
			endingGame = true;
		for (int i = 0; i < playing.length; i++) {
			for (final Player player : playing[i]) {
				player.lock(7);
				player.stopAll();
			}
		}
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				for (int i = 0; i < playing.length; i++)
					for (Player player : playing[i].toArray(new Player[playing[i].size()])) {
						forceRemovePlayingPlayer(player);
						if (winner != -1) {
							if (winner == -2) {
								player.getPackets().sendGameMessage("You draw.");
								player.getInventory().addItem(CW_TICKET, 1);
							} else if (winner == i) {
								player.getPackets().sendGameMessage("You won.");
								player.getInventory().addItem(CW_TICKET, 2);
							} else
								player.getPackets().sendGameMessage("You lost.");
						}
					}
				if (winner != -1)
					endingGame = false;
			}
		}, 6);
	}

	// unused
	public static void forceRemoveWaitingPlayer(Player player) {
		player.getControlerManager().forceStop();
	}

	public static void removeWaitingPlayer(Player player, int team) {
		waiting[team].remove(player);
		setCape(player, null);
		setHood(player, null);
		player.setNextWorldTile(new WorldTile(LOBBY, 2));
		if (playingGame != null && waiting[team].size() == 0 && playing[team].size() == 0)
			destroyPlayingGame(); // cancels if 0 players playing/waiting on any
		// of the teams
	}

	public static void refreshTimeLeft(Player player) {
		player.getPackets().sendConfig(1417,
				playingGame == null ? 0
						: playingGame.minutesLeft
								- (player.getControlerManager().getControler() instanceof CastleWarsPlaying ? 5 : 0));
	}

	public static void startGame() {
		for (int i = 0; i < waiting.length; i++) {
			for (Player player : waiting[i].toArray(new Player[waiting[i].size()])) {
				joinPlayingGame(player, i);
			}
		}
	}

	public static boolean forceStart() {
		if (getWaitingPlayersCount() == 0 || getPlayingPlayersCount() > 0)
			return false;
		if (playingGame == null)
			createPlayingGame();
		playingGame.forceStart();
		return true;
	}

	private static int getWaitingPlayersCount() {
		int count = 0;
		for (List<Player> players : waiting)
			count += players.size();
		return count;
	}

	private static int getPlayingPlayersCount() {
		int count = 0;
		for (List<Player> players : playing)
			count += players.size();
		return count;
	}

	public static void forceRemovePlayingPlayer(Player player) {
		player.getControlerManager().forceStop();
	}

	public static void removePlayingPlayer(Player player, int team) {
		playing[team].remove(player);
		player.reset();
		player.setCanPvp(false);
		// remove the items
		setCape(player, null);
		setHood(player, null);
		int weaponId = player.getEquipment().getWeaponId();
		if (weaponId == 4037 || weaponId == 4039) {
			CastleWars.setWeapon(player, null);
			if (playingGame != null && !endingGame) {
				WorldTile dropTile = player.getLastWorldTile() == null ? new WorldTile(player) : player.getLastWorldTile();
				CastleWars.dropFlag(dropTile, weaponId == 4037 ? CastleWars.SARADOMIN : CastleWars.ZAMORAK);
			}
		}
		player.closeInterfaces();
		player.getInventory().deleteItem(4043, Integer.MAX_VALUE); // rocks
		player.getInventory().deleteItem(4045, Integer.MAX_VALUE); // explosive potions
		player.getInventory().deleteItem(4047, Integer.MAX_VALUE); // climbing ropes
		player.getInventory().deleteItem(4049, Integer.MAX_VALUE); // bandages
		player.getInventory().deleteItem(4050, Integer.MAX_VALUE); // bandages
		player.getInventory().deleteItem(4051, Integer.MAX_VALUE); // toolkits
		player.getInventory().deleteItem(4053, Integer.MAX_VALUE); // barricades
		player.getInventory().deleteItem(18704, Integer.MAX_VALUE); // flares

		player.getHintIconsManager().removeUnsavedHintIcon();
		player.getMusicsManager().reset();
		player.setNextWorldTile(new WorldTile(LOBBY, 2));
		if (playingGame != null && waiting[team].size() == 0 && playing[team].size() == 0)
			destroyPlayingGame(); // cancels if 0 players playing/waiting on any
		// of the tea
	}

	public static void joinPlayingGame(Player player, int team) {
		playingGame.refresh(player);
		waiting[team].remove(player);
		player.getControlerManager().removeControlerWithoutCheck();
		player.getInterfaceManager().removeMinigameHudInterface();
		playing[team].add(player);
		player.setCanPvp(true);
		player.getControlerManager().startControler("CastleWarsPlaying", team);
		player.setNextWorldTile(new WorldTile(team == ZAMORAK ? ZAMO_BASE : SARA_BASE, 1));
	}

	public static void endGame(int winner) {
		if (winner >= 0 && winner < seasonWins.length)
			seasonWins[winner]++;
		leavePlayersSafely(winner);
	}

	public static void refreshAllPlayersTime() {
		for (int i = 0; i < waiting.length; i++)
			for (Player player : waiting[i])
				refreshTimeLeft(player);
		for (int i = 0; i < playing.length; i++)
			for (Player player : playing[i]) {
				player.getMusicsManager().playMusic(i == ZAMORAK ? 845 : 314);
				refreshTimeLeft(player);
			}
	}

	public static void refreshAllPlayersPlaying() {
		for (int i = 0; i < playing.length; i++)
			for (Player player : playing[i])
				playingGame.refresh(player);
	}

    public static void refreshAllPlayersTakenBy(String name) {
        for (int i = 0; i < playing.length; i++)
            for (Player player : playing[i])
                playingGame.refreshTakenBy(player, name);
    }

	public static void addHintIcon(int team, Player target) {
		for (Player player : playing[team]) {
			player.getHintIconsManager().addHintIcon(target, 0, -1, false);
		}
	}

	public static void removeHintIcon(int team) {
		for (Player player : playing[team]) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		}
	}

	public static void addScore(Player player, int team, int flagTeam) {
		if (playingGame == null)
			return;
		playingGame.addScore(player, team, flagTeam);
	}

	public static void takeFlag(Player player, int team, int flagTeam, WorldObject object, boolean droped) {
		if (playingGame == null)
			return;
		playingGame.takeFlag(player, team, flagTeam, object, droped);
	}

	public static void dropFlag(WorldTile tile, int flagTeam) {
		if (playingGame == null)
			return;
		playingGame.dropFlag(tile, flagTeam);
	}

	public static void removeBarricade(int team, CastleWarBarricade npc) {
		if (playingGame == null)
			return;
		playingGame.removeBarricade(team, npc);
	}

	public static void addBarricade(int team, Player player) {
		if (playingGame == null)
			return;
		playingGame.addBarricade(team, player);
	}

	public static boolean isBarricadeAt(WorldTile tile) {
		if (playingGame == null)
			return false;
		return playingGame.isBarricadeAt(tile);
	}

	public static void handleInterfaces(Player player, int interfaceId, int componentId) {
		if (interfaceId == 55) {
			if (componentId == 9)
				player.closeInterfaces();
		}
	}

	public static boolean handleObjects(Player player, int objectId) {
		if (objectId == 4484) { // scoreboard
			player.getDialogueManager().startDialogue(new CastleWarsScoreboard());
			return true;
		}
		if (objectId == OLD_ZAMORAK_PORTAL || objectId == ZAMORAK_PORTAL) {
			joinPortal(player, ZAMORAK);
			return true;
		}
		if (objectId == OLD_GUTHIX_PORTAL || objectId == GUTHIX_PORTAL) {
			joinPortal(player, GUTHIX);
			return true;
		}
		if (objectId == OLD_SARADOMIN_PORTAL || objectId == SARADOMIN_PORTAL) {
			joinPortal(player, SARADOMIN);
			return true;
		}
		return false;
	}

	public static List<Player>[] getPlaying() {
		return playing;
	}

	private static class PlayingGame extends TrackedRunnable {

		private static final int SAFE = 0, TAKEN = 1, DROPPED = 2;
		private static final int SARADOMIN_EMPTY_STAND = 83553, ZAMORAK_EMPTY_STAND = 83554;
		private static final int SARADOMIN_DROPPED_FLAG = 4900, ZAMORAK_DROPPED_FLAG = 4901;
		private static final int SARADOMIN_FLAG_ITEM = 4037, ZAMORAK_FLAG_ITEM = 4039;
		private final LinkedList<WorldObject> spawnedObjects = new LinkedList<WorldObject>();
		private final LinkedList<CastleWarBarricade> barricades = new LinkedList<CastleWarBarricade>();
		private int minutesLeft;
		private int[] score;
		private int[] flagStatus;
		private int[] barricadesCount;

		public PlayingGame() {
			reset();
		}

		public void reset() {
			minutesLeft = 5; // temp testing else 5
			score = new int[2];
			flagStatus = new int[2];
			barricadesCount = new int[2];
			cleanupSpawnedObjects();
			removeHintIcon(SARADOMIN);
			removeHintIcon(ZAMORAK);
			refreshAllPlayersTakenBy(null);
			for (CastleWarBarricade npc : barricades.toArray(new CastleWarBarricade[barricades.size()]))
				npc.finish();
			barricades.clear();
		}

		private void cleanupSpawnedObjects() {
			for (Iterator<WorldObject> it = spawnedObjects.iterator(); it.hasNext();) {
				WorldObject object = it.next();
				it.remove();
				World.removeObject(object, true);
			}
		}

		private boolean sameObject(WorldObject first, WorldObject second) {
			return first.getId() == second.getId() && first.getType() == second.getType()
					&& first.getX() == second.getX() && first.getY() == second.getY()
					&& first.getPlane() == second.getPlane();
		}

		private boolean removeSpawnedObject(WorldObject object) {
			for (Iterator<WorldObject> it = spawnedObjects.iterator(); it.hasNext();) {
				WorldObject spawned = it.next();
				if (sameObject(spawned, object)) {
					it.remove();
					World.removeObject(spawned, true);
					return true;
				}
			}
			return false;
		}

		private void removeSpawnedObjectsById(int objectId) {
			for (Iterator<WorldObject> it = spawnedObjects.iterator(); it.hasNext();) {
				WorldObject object = it.next();
				if (object.getId() == objectId) {
					it.remove();
					World.removeObject(object, true);
				}
			}
		}

		private int getEmptyStandId(int flagTeam) {
			return flagTeam == SARADOMIN ? SARADOMIN_EMPTY_STAND : ZAMORAK_EMPTY_STAND;
		}

		private int getDroppedFlagId(int flagTeam) {
			return flagTeam == SARADOMIN ? SARADOMIN_DROPPED_FLAG : ZAMORAK_DROPPED_FLAG;
		}

		private int getFlagItemId(int flagTeam) {
			return flagTeam == SARADOMIN ? SARADOMIN_FLAG_ITEM : ZAMORAK_FLAG_ITEM;
		}

		public boolean isBarricadeAt(WorldTile tile) {
			for (Iterator<CastleWarBarricade> it = barricades.iterator(); it.hasNext();) {
				CastleWarBarricade npc = it.next();
				if (npc.isDead() || npc.hasFinished()) {
					it.remove();
					continue;
				}
				if (npc.getX() == tile.getX() && npc.getY() == tile.getY() && npc.getPlane() == tile.getPlane())
					return true;
			}
			return false;
		}

		public void addBarricade(int team, Player player) {
			if (isBarricadeAt(player)) {
				player.getPackets().sendGameMessage("You cannot place a barricade here!");
				return;
			}
			if (barricadesCount[team] >= 10) {
				player.getPackets()
						.sendGameMessage("Each team in the activity can have a maximum of 10 barricades set up.");
				return;
			}
			player.getInventory().deleteItem(new Item(4053, 1));
			barricadesCount[team]++;
			barricades.add(new CastleWarBarricade(team, new WorldTile(player)));
		}

		public void removeBarricade(int team, CastleWarBarricade npc) {
			if (team < 0 || team >= barricadesCount.length)
				return;
			if (barricades.remove(npc) && barricadesCount[team] > 0)
				barricadesCount[team]--;
		}

		public void forceStart() {
			minutesLeft = 25;
			startGame();
			refreshAllPlayersTime();
			refreshAllPlayersPlaying();
		}

		public void takeFlag(Player player, int team, int flagTeam, WorldObject object, boolean droped) {
			if (!droped && team == flagTeam)
				return;
			if (droped && flagStatus[flagTeam] != DROPPED)
				return;
			else if (!droped && flagStatus[flagTeam] != SAFE)
				return;

			if (flagTeam != team
					&& (player.getEquipment().getWeaponId() != -1 || player.getEquipment().getShieldId() != -1)) {
				// TODO no space message
				player.getPackets().sendGameMessage("You can't take flag while wearing something in your hands.");
				return;
			}
			if (!droped) {
				WorldObject flagStand = new WorldObject(getEmptyStandId(flagTeam), object.getType(),
						object.getRotation(), object.getX(), object.getY(), object.getPlane());
				spawnedObjects.add(flagStand);
				World.spawnObject(flagStand);
			} else {
				if (!removeSpawnedObject(object))
					return;
				if (flagTeam == team) {
					makeSafe(flagTeam);
					return;
				}
			}
			addHintIcon(flagTeam, player);
			flagStatus[flagTeam] = TAKEN;
			setWeapon(player, new Item(getFlagItemId(flagTeam), 1));
			refreshAllPlayersPlaying();
			refreshAllPlayersTakenBy(player.getDisplayName());
		}

		public void addScore(Player player, int team, int flagTeam) {
			if (flagStatus[team] != SAFE) {
				player.getPackets().sendGameMessage("Your team's flag must be safe before you can score.");
				return;
			}
			if (player.getEquipment().getWeaponId() != getFlagItemId(flagTeam))
				return;
			setWeapon(player, null);
			score[team] += 1;
			makeSafe(flagTeam);
		}

		private void makeSafe(int flagTeam) {
			removeHintIcon(flagTeam);
			removeSpawnedObjectsById(getEmptyStandId(flagTeam));
			removeSpawnedObjectsById(getDroppedFlagId(flagTeam));
			flagStatus[flagTeam] = SAFE;
			refreshAllPlayersPlaying();
	        refreshAllPlayersTakenBy(null);
		}

		public void dropFlag(WorldTile tile, int flagTeam) {
			if (flagStatus[flagTeam] == SAFE) {
				return;
			}
			if (tile == null) {
				makeSafe(flagTeam);
				return;
			}
			removeHintIcon(flagTeam);
			removeSpawnedObjectsById(getDroppedFlagId(flagTeam));
			WorldObject flagDroped = new WorldObject(getDroppedFlagId(flagTeam), 10, 0, tile.getX(),
					tile.getY(), tile.getPlane());
			spawnedObjects.add(flagDroped);
			World.spawnObject(flagDroped);
			flagStatus[flagTeam] = DROPPED;
			refreshAllPlayersPlaying();
	        refreshAllPlayersTakenBy(null);
		}

		public void refresh(Player player) {
			player.getPackets().sendConfigByFile(4184, flagStatus[SARADOMIN]);
			player.getPackets().sendConfigByFile(4186, score[SARADOMIN]);
			player.getPackets().sendConfigByFile(4194, flagStatus[ZAMORAK]);
			player.getPackets().sendConfigByFile(4196, score[ZAMORAK]);
		}

        public void refreshTakenBy(Player player, String name) {
            player.getPackets().sendGlobalString(2731, name == null ? "" : name);
        }

		@Override
		public void run() {
			minutesLeft--;
			if (minutesLeft == 5) {
				endGame(score[SARADOMIN] == score[ZAMORAK] ? -2
						: score[SARADOMIN] > score[ZAMORAK] ? SARADOMIN : ZAMORAK);
				reset();
			} else if (minutesLeft == 0) {
				minutesLeft = 25;
				startGame();
			} else if (minutesLeft > 6) // adds ppl waiting on lobby
				startGame();
			refreshAllPlayersTime();
		}

	}
}
