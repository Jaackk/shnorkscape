package com.rs.game.activites.soulwars;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.summoning.Summoning.Pouches;
import com.rs.game.player.content.Foods.Food;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Savions Sw, the legendary.
 */
public final class SoulWarsManager {

	public final static int BANDAGE_ID = 14648, BARRICADE_ID = 14649, EXPLOSIVE_POTION_ID = 14650, TEAM_CAPE_INDEX = 14641, SOUL_FRAGMENT = 14646, BONES = 3187, REQUIRED_TEAM_MEMBERS = 1, JELLY = 8599, PYREFRIEND = 8598, GHOST = 8623, AVATAR_INDEX = 8596;
	public static AtomicInteger MINUTES_BEFORE_NEXT_GAME = new AtomicInteger(3);
	private final HashMap<PlayerType, FixedLengthRunnable> tasks = new HashMap<PlayerType, FixedLengthRunnable>(PlayerType.values().length);

	public void start() {
		startTask(PlayerType.OUTSIDE_LOBBY, new AreaTask());
		startTask(PlayerType.INSIDE_LOBBY, new LobbyTask());
		startTask(PlayerType.IN_GAME, new GameTask());
	}

	public void passBarrier(final PlayerType currentType, final Player player, final WorldObject object) {
		if (currentType.equals(PlayerType.OUTSIDE_LOBBY) && !canEnterLobby(player)) {
			return;
		}
		switch (object.getId()) {
		case 42015:
		case 42018:
			int id = player.getEquipment().getCapeId();
			id -= TEAM_CAPE_INDEX;
			if (id < 0 || id > 1) {
				return;
			}
			final Teams team = Teams.values()[id];
			if ((team.equals(Teams.RED) && object.getId() == 42015) || (team.equals(Teams.BLUE) && object.getId() == 42018) || player.getAppearence().getTransformedNpcId() != -1) {
				player.sendMessage("You must wait 15 seconds before entering the battleground again.", true);
				return;
			}
			int x = player.getX() + object.getX() - player.getX();
			if (object.getId() == 42015 ? (object.getX() >= player.getX()) : (object.getX() <= player.getX())) {
				x = object.getX() + (object.getId() == 42015 ? 1 : -1);
			}
			player.addWalkSteps(x, object.getY(), -1, false);
			player.lock(1);
			break;
		case 42019:
		case 42020:
			if (player.getAppearence().getTransformedNpcId() != -1 || (object.getId() == 42020 && object.getY() <= player.getY()) || (object.getId() == 42019 && object.getY() >= player.getY())) {
				player.sendMessage("You must wait 15 seconds before entering the battleground again.", true);
				return;
			}
			player.addWalkSteps(object.getX(), object.getY(), -1, false);
			player.lock(1);
			break;
		case 42029:
		case 42030:
			x = player.getX() + object.getX() - player.getX();
			if (object.getId() == 42029 ? (object.getX() >= player.getX()) : (object.getX() <= player.getX())) {
				x = object.getX() + (object.getId() == 42029 ? 1 : -1);
			}
			player.addWalkSteps(x, object.getY(), -1, false);
			player.lock(1);
			break;
		case 42031:
			final Teams nextTeam = nextJoiningTeam();
			player.setNextWorldTile(calculateRandomLocation(nextTeam, PlayerType.INSIDE_LOBBY));
			enterLobby(player, nextTeam);
			return;
		}
		if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof GameController) {
			final Boolean bool = (Boolean) player.getTemporaryAttributtes().get("sw_safe_zone");
			if (bool != null) {
				player.getTemporaryAttributtes().put("sw_safe_zone", !bool);
			}
		}
		switch (currentType) {
		case OUTSIDE_LOBBY:
			enterLobby(player, object.getId() == 42029 ? Teams.BLUE : Teams.RED);
			break;
		case INSIDE_LOBBY:
			((LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY)).getPlayers().remove(player);
			resetPlayer(player, PlayerType.OUTSIDE_LOBBY, false);
			break;
		default:
			break;
		}
	}

	private boolean canEnterLobby(final Player player) {
		if (player.getEquipment().getCapeId() != -1) {
			player.getPackets().sendGameMessage("You cannot join the waiting lobby, remove your cape from your equipment.");
			return false;
		}
		if (player.getFamiliar() != null) {
			player.sendMessage("You're not allowed to enter with that familiar.");
			return false;
		}
		if (player.getSkills().getTotalLevel(player) < 250) {
			player.sendMessage("You need a total level of 250 to play Soul Wars.");
			return false;
		}
		for (final Pouches pouch : Pouches.values()) {
			if (pouch == null) {
				continue;
			}
			if (player.getInventory().containsItem(pouch.getPouchId(), 1)) {
				player.sendMessage("You cannot enter with having pouches in your inventory!");
				return false;
			}
		}
		for (final Item item : player.getInventory().getItems().getItems()) {
			if (item == null) {
				continue;
			}
			if (Food.forId(item.getId()) != null) {
				player.sendMessage("You cannot bring food into Soul Wars.");
				return false;
			}
			/*if (Pots.getPot(item.getId()) != null) {
				player.sendMessage("You cannot bring potions into Soul Wars.");
				return false;
			}*/
			final ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item.getId());
			if (defs != null) {
				final String name = defs.getName();
				if (name != null) {
				}
			}
		}
		for (final Item item : player.getEquipment().getItems().getItems()) {
			if (item != null) {
				final ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item.getId());
				if (defs != null) {
					final String name = ItemDefinitions.getItemDefinitions(item.getId()).getName();
					if (name != null) {
					}
				}
			}
		}
		return true;
	}

	private void enterLobby(final Player player, final Teams team) {
		player.getControlerManager().startControler("LobbyController");
		player.getEquipment().getItems().set(Equipment.SLOT_CAPE, new Item(TEAM_CAPE_INDEX + team.ordinal()));
		player.getEquipment().refresh(Equipment.SLOT_CAPE);
		player.getAppearence().generateAppearenceData();
		player.sendMessage("You've joined the " + team.toString().toLowerCase() + " team.");
	}

	public boolean enterTeamLobby(final Player player, final Teams team) {
		if (player == null || player.hasFinished() || team == null || !canEnterLobby(player)) {
			return false;
		}
		forceEnterTeamLobby(player, team);
		return true;
	}

	public void forceEnterTeamLobby(final Player player, final Teams team) {
		if (player == null || player.hasFinished() || team == null) {
			return;
		}
		removeFromTasks(player);
		player.stopAll();
		player.unlock();
		player.setCanPvp(false);
		player.getTemporaryAttributtes().remove("sw_safe_zone");
		player.getTemporaryAttributtes().remove("soulwars_kicked");
		player.getTemporaryAttributtes().remove("soul_wars_last_respawn_index");
		player.getTemporaryAttributtes().remove("soul_wars_last_respawn_loc");
		player.getEquipment().deleteItem(TEAM_CAPE_INDEX, 1);
		player.getEquipment().deleteItem(TEAM_CAPE_INDEX + 1, 1);
		player.getInventory().deleteItem(SOUL_FRAGMENT, Integer.MAX_VALUE);
		player.getInventory().deleteItem(BANDAGE_ID, 28);
		player.getInventory().deleteItem(BONES, 28);
		player.getInventory().deleteItem(EXPLOSIVE_POTION_ID, 28);
		player.getInventory().deleteItem(4053, 28);
		player.getInventory().deleteItem(14644, 28);
		player.getInventory().deleteItem(BARRICADE_ID, 28);
		player.getAppearence().transformIntoNPC(-1);
		player.getPrayer().reset();
		player.setHitpoints(player.getMaxHitpoints());
		player.setRunEnergy(100);
		player.setNextWorldTile(calculateRandomLocation(team, PlayerType.INSIDE_LOBBY));
		enterLobby(player, team);
	}

	private Teams nextJoiningTeam() {
		final int minutes = MINUTES_BEFORE_NEXT_GAME.get();
		final GameTask game = (GameTask) tasks.get(PlayerType.IN_GAME);
		final LobbyTask lobby = (LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY);
		if (minutes < 4) {
			if (lobby.getPlayers(Teams.RED).size() > lobby.getPlayers(Teams.BLUE).size()) {
				return Teams.BLUE;
			} else if (lobby.getPlayers(Teams.RED).size() < lobby.getPlayers(Teams.BLUE).size()) {
				return Teams.RED;
			} else {
				return Teams.values()[Utils.getRandom(1)];
			}
		} else {
			final int[] totalSizes = new int[2];
			for (int index = 0; index < 2; index++) {
				totalSizes[index] = game.getPlayers(Teams.values()[index]).size() + lobby.getPlayers(Teams.values()[index]).size();
			}
			if (totalSizes[Teams.RED.ordinal()] > totalSizes[Teams.BLUE.ordinal()]) {
				return Teams.BLUE;
			} else if (totalSizes[Teams.RED.ordinal()] < totalSizes[Teams.BLUE.ordinal()]) {
				return Teams.RED;
			} else {
				return Teams.values()[Utils.getRandom(1)];
			}
		}
	}

	public void resetPlayer(final Player player, final PlayerType type, final boolean logout) {
		int id = player.getEquipment().getCapeId();
		id -= TEAM_CAPE_INDEX;
		if (id < 0 || id > 1) {
			return;
		}
		final Teams team = Teams.values()[id];
		if (!logout) {
			player.getControlerManager().startControler("AreaController");
		}
		player.getEquipment().deleteItem(TEAM_CAPE_INDEX + id, 1);
		player.getEquipment().refresh(Equipment.SLOT_CAPE);
		player.getAppearence().generateAppearenceData();
		player.getInventory().deleteItem(SOUL_FRAGMENT, Integer.MAX_VALUE);
		player.getInventory().deleteItem(BANDAGE_ID, 28);
		player.getInventory().deleteItem(BONES, 28);
		player.getInventory().deleteItem(EXPLOSIVE_POTION_ID, 28);
		player.getInventory().deleteItem(4053, 28);
		player.getInventory().deleteItem(14644, 28);
		player.getInventory().deleteItem(BARRICADE_ID, 28);
		player.getAppearence().transformIntoNPC(-1);
		//player.sendMessage("If you found any bugs please post them on forums in 'bug' section!.");
		if (type.equals(PlayerType.IN_GAME)) {
			player.setCanPvp(false);
			player.getPrayer().reset();
			player.getPoison().reset();
			player.resetReceivedDamage();
			player.setHitpoints(player.getMaxHitpoints());
			player.setRunEnergy(100);
			player.unlock(); // safety reasons
		}
		if (!type.equals(PlayerType.OUTSIDE_LOBBY)) {
			final WorldTile random = calculateRandomLocation(team, PlayerType.OUTSIDE_LOBBY);
			if (!logout) {
				player.setNextWorldTile(random);
			} else {
				player.setLocation(random);
			}
		}
	}

	public GameTask getGameTask() {
		return (GameTask) tasks.get(PlayerType.IN_GAME);
	}

	public LobbyTask getLobbyTask() {
		return (LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY);
	}

	public Teams getTeam(final Player player) {
		if (player == null) {
			return null;
		}
		int id = player.getEquipment().getCapeId() - TEAM_CAPE_INDEX;
		return id >= 0 && id < Teams.values().length ? Teams.values()[id] : null;
	}

	public boolean isSameTeam(final Player first, final Player second) {
		final Teams firstTeam = getTeam(first);
		return firstTeam != null && firstTeam.equals(getTeam(second));
	}

	public boolean isSafeZone(final Player player) {
		return player != null && Boolean.TRUE.equals(player.getTemporaryAttributtes().get("sw_safe_zone"));
	}

	public boolean isInLobby(final Player player) {
		return player != null && getLobbyTask().getPlayers().contains(player);
	}

	public boolean isInGame(final Player player) {
		return player != null && getGameTask().getPlayers().contains(player);
	}

	public int getLobbySize(final Teams team) {
		return team == null ? 0 : getLobbyTask().getPlayers(team).size();
	}

	public int getGameSize(final Teams team) {
		return team == null ? 0 : getGameTask().getPlayers(team).size();
	}

	public int getTeamSize(final Teams team) {
		return getLobbySize(team) + getGameSize(team);
	}

	public WorldTile getLobbyTile(final Teams team) {
		return getAreaMidpoint(team, PlayerType.INSIDE_LOBBY);
	}

	public WorldTile getGameTile(final Teams team) {
		return getAreaMidpoint(team, PlayerType.IN_GAME);
	}

	public WorldTile getAreaCenter(final int index) {
		if (index < 0 || index >= GameTask.AREAS.length) {
			return null;
		}
		final WorldTile start = GameTask.AREAS[index][0];
		final WorldTile end = GameTask.AREAS[index][1];
		return new WorldTile(start.getX() + ((end.getX() - start.getX()) / 2),
				start.getY() + ((end.getY() - start.getY()) / 2), start.getPlane());
	}

	public Avatar getAvatar(final Teams team) {
		if (team == null) {
			return null;
		}
		return getGameTask().getAvatars()[team.ordinal()];
	}

	public Avatar getEnemyAvatar(final Teams team) {
		return team == null ? null : getAvatar(team.equals(Teams.RED) ? Teams.BLUE : Teams.RED);
	}

	public boolean canDepositFragments(final Player player) {
		final Teams team = getTeam(player);
		return team != null && player.getInventory().getAmountOf(SOUL_FRAGMENT) > 0
				&& team.equals(getGameTask().getTeamAreas()[1]);
	}

	public boolean depositSoulFragments(final Player player) {
		final Teams team = getTeam(player);
		if (team == null || player.getInventory().getAmountOf(SOUL_FRAGMENT) <= 0) {
			return false;
		}
		final GameTask task = getGameTask();
		if (!team.equals(task.getTeamAreas()[1])) {
			player.getPackets().sendGameMessage("Your team needs to control the soul obelisk first.");
			return false;
		}
		final int amount = player.getInventory().getAmountOf(SOUL_FRAGMENT);
		player.getInventory().deleteItem(SOUL_FRAGMENT, amount);
		task.increaseAvatarLevel(team.equals(Teams.BLUE) ? Teams.RED : Teams.BLUE, -amount);
		player.getPackets().sendGameMessage("You weaken the enemy avatar with " + amount + " soul fragments.", true);
		return true;
	}

	public boolean useBandage(final Player player) {
		if (player == null || player.hasFinished() || player.getInventory().getAmountOf(BANDAGE_ID) <= 0) {
			return false;
		}
		int gloves = player.getEquipment().getGlovesId();
		player.heal((int) (player.getMaxHitpoints() * (gloves >= 11079 && gloves <= 11084 ? 0.15 : 0.10)));
		int restoredEnergy = (int) (player.getRunEnergy() * 1.3);
		player.setRunEnergy(restoredEnergy > 100 ? 100 : restoredEnergy);
		if (player.getPoison().isPoisoned()) {
			player.getPoison().reset();
		}
		player.getPrayer().restorePrayer((int) ((int) (Math.floor(player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5) + 70) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
		player.getInventory().deleteItem(BANDAGE_ID, 1);
		player.getPackets().sendGameMessage("You use a bandage and feel restored...", true);
		return true;
	}

	public boolean buryBones(final Player player) {
		final Teams team = getTeam(player);
		if (team == null || player.getInventory().getAmountOf(BONES) <= 0) {
			return false;
		}
		player.stopAll(true);
		player.lock(2);
		player.getPackets().sendSound(2738, 0, 1);
		player.setNextAnimation(new Animation(827));
		player.getInventory().deleteItem(BONES, 1);
		if (Utils.getRandom(4) != 0) {
			getGameTask().increaseAvatarLevel(team, Utils.getRandom(4));
		}
		if (Utils.getRandom(1) == 0) {
			player.getPrayer().restorePrayer(10 + Utils.getRandom(100));
		}
		return true;
	}

	public boolean placeHeldBarricade(final Player player) {
		final Teams team = getTeam(player);
		if (team == null || player.getInventory().getAmountOf(4053) <= 0) {
			return false;
		}
		final GameTask task = getGameTask();
		if (!task.containsBarricade(player)) {
			player.getPackets().sendGameMessage("You can't place a barricade here!");
			return false;
		}
		return task.placeBarricade(player, team.ordinal());
	}

	public boolean forceStart() {
		if (MINUTES_BEFORE_NEXT_GAME.get() > 3 || getLobbySize(Teams.BLUE) < 1 || getLobbySize(Teams.RED) < 1) {
			return false;
		}
		sendPlayers(true);
		if (getGameSize(Teams.BLUE) < 1 || getGameSize(Teams.RED) < 1) {
			return false;
		}
		MINUTES_BEFORE_NEXT_GAME.set(23);
		return true;
	}

	public boolean forceEnd() {
		if (MINUTES_BEFORE_NEXT_GAME.get() <= 3 || getGameTask().getPlayers().isEmpty()) {
			return false;
		}
		endGame(false);
		MINUTES_BEFORE_NEXT_GAME.set(3);
		return true;
	}

	public int reset() {
		int removed = 0;
		final ArrayList<Player> players = new ArrayList<Player>();
		players.addAll(getGameTask().getPlayers());
		players.addAll(getLobbyTask().getPlayers());
		for (final Player player : players) {
			if (player == null || player.hasFinished()) {
				continue;
			}
			final boolean inGame = getGameTask().getPlayers().contains(player);
			removeFromTasks(player);
			resetPlayer(player, inGame ? PlayerType.IN_GAME : PlayerType.INSIDE_LOBBY, false);
			removed++;
		}
		getLobbyTask().getPlayers().clear();
		getGameTask().reset();
		MINUTES_BEFORE_NEXT_GAME.set(3);
		return removed;
	}

	public String getStatus() {
		return "Soul Wars: minutes=" + MINUTES_BEFORE_NEXT_GAME.get()
				+ ", lobby red=" + getLobbySize(Teams.RED)
				+ ", lobby blue=" + getLobbySize(Teams.BLUE)
				+ ", game red=" + getGameSize(Teams.RED)
				+ ", game blue=" + getGameSize(Teams.BLUE) + ".";
	}

	public WorldTile calculateRandomLocation(final Teams team, final PlayerType type) {
		final WorldTile A = team.equals(Teams.BLUE) ? type.getLocationA() : type.getLocationC();
		final WorldTile B = team.equals(Teams.BLUE) ? type.getLocationB() : type.getLocationD();
		final ArrayList<WorldTile> possibleLocations = new ArrayList<WorldTile>();
		for (int x = A.getX(); x <= B.getX(); x++) {
			for (int y = A.getY(); y <= B.getY(); y++) {
				if (World.canMoveNPC(0, x, y, 1)) {
					possibleLocations.add(new WorldTile(x, y, 0));
				}
			}
		}
		return possibleLocations.get(Utils.random(possibleLocations.size()));
	}

	public boolean decrementMinute() {

		if (tasks.size() < 3 || (MINUTES_BEFORE_NEXT_GAME.get() <= 3 && (((LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY)).getPlayers(Teams.BLUE).size() < REQUIRED_TEAM_MEMBERS || ((LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY)).getPlayers(Teams.RED).size() < REQUIRED_TEAM_MEMBERS))) {
			return false;
		}
		if (MINUTES_BEFORE_NEXT_GAME.get() > 3 && (((GameTask) tasks.get(PlayerType.IN_GAME)).getPlayers(Teams.BLUE).size() < 1 || ((GameTask) tasks.get(PlayerType.IN_GAME)).getPlayers(Teams.RED).size() < 1)) {
			endGame(false);
			MINUTES_BEFORE_NEXT_GAME.set(3);
			return false;
		}
		final int decrement = MINUTES_BEFORE_NEXT_GAME.decrementAndGet();
		if (decrement >= 3 + 10) {
			sendPlayers(false);
		}
		if (decrement == 3) {
			endGame(true);
		}
		if (decrement == 0) {
			sendPlayers(true);
			MINUTES_BEFORE_NEXT_GAME.set(23);
		}
		return true;
	}

	private void endGame(final boolean award) {
		final GameTask task = getGameTask();
		if (task.getAvatars()[0] != null) {
			task.getAvatars()[0].resetReceivedDamage();
		}
		if (task.getAvatars()[1] != null) {
			task.getAvatars()[1].resetReceivedDamage();
		}
		final int blue = task.getAvatarDies(Teams.BLUE), red = task.getAvatarDies(Teams.RED);
		final Teams winningTeam = red > blue ? Teams.BLUE : blue > red ? Teams.RED : null;
		final String name = winningTeam == null ? "neither" : winningTeam.equals(Teams.BLUE) ? "<col=337FB5>blue</col>" : "<col=F00004>red</col>";
		for (final Iterator<Player> it = task.getPlayers().iterator(); it.hasNext();) {
			final Player player = it.next();
			it.remove();
			if (player == null || player.hasFinished()) {
				continue;
			}
			int id = player.getEquipment().getCapeId();
			id -= TEAM_CAPE_INDEX;
			if (id < 0 || id > 1) {
				continue;
			}
			final Teams team = Teams.values()[id];
			resetPlayer(player, PlayerType.IN_GAME, false);
			String message = "You received nothing; not enough players left in game!";

			if (award) {
				if (winningTeam != null && winningTeam.equals(team)) {
					player.addSWWin();
				}
				final int zeals = (winningTeam == null ? 3 : winningTeam.equals(team) ? 4 : 2) * Settings.ZEAL_MODIFIER * (player.getPerkManager().hasPerkActive(DonationPerk.THE_MINI___GAMER) ? 2 : 1);
				message = winningTeam == null ? "The game was a draw, you received " + zeals + " zeals for participating." : "The " + name + " team was victorious! You received " + (winningTeam.equals(team) ? zeals + " zeals for winning!" : zeals + " zeals for losing.");
				player.setZeals(player.getZeals() + zeals);
			}
			player.sendMessage(message);
			player.getDialogueManager().startDialogue("SimpleNPCMessage", team.equals(Teams.RED) ? 8528 : 8526, message);
		}
		task.reset();
	}

	private void sendPlayers(final boolean create) {
		if (create) {
			((GameTask) tasks.get(PlayerType.IN_GAME)).start();
		}
		final ArrayList<Player> blue = ((LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY)).getPlayers(Teams.BLUE), red = ((LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY)).getPlayers(Teams.RED);
		if (Settings.DEBUG) {
			for (final Player player : blue) {
				addPlayerToGame(player, Teams.BLUE);
			}
			blue.clear();
			for (final Player player : red) {
				addPlayerToGame(player, Teams.RED);
			}
			red.clear();
		}
		int size = 0;
		if (create) {
			size = blue.size() > red.size() ? red.size() : blue.size();
			if (size < 1) {
				return;
			}
			for (int i = 0; i < 2; i++) {
				int index = 0;
				for (final Iterator<Player> it = (i == 0 ? red.iterator() : blue.iterator()); it.hasNext();) {
					if (index++ >= size) {
						break;
					}
					final Player player = it.next();
					if (player != null && !player.hasFinished() && !player.isLocked()) {
						addPlayerToGame(player, Teams.values()[i]);
					}
					it.remove();
				}
			}
		} else {
			final ArrayList<Player> gameBlue = ((GameTask) tasks.get(PlayerType.IN_GAME)).getPlayers(Teams.BLUE), gameRed = ((GameTask) tasks.get(PlayerType.IN_GAME)).getPlayers(Teams.RED), lobbyBlue = ((LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY)).getPlayers(Teams.BLUE), lobbyRed = ((LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY)).getPlayers(Teams.RED);
			size = gameBlue.size() > gameRed.size() ? gameBlue.size() - gameRed.size() : gameRed.size() - gameBlue.size();
			final int lobbySize = lobbyBlue.size() > lobbyRed.size() ? lobbyRed.size() : lobbyBlue.size();
			final int[] takeOutEachTeam = new int[2];
			takeOutEachTeam[0] = lobbySize + (gameBlue.size() > gameRed.size() ? size + lobbySize > lobbyRed.size() ? lobbyRed.size() : size + lobbySize : 0);
			takeOutEachTeam[1] = lobbySize + (gameRed.size() > gameBlue.size() ? size + lobbySize > lobbyBlue.size() ? lobbyBlue.size() : size + lobbySize : 0);
			if (takeOutEachTeam[0] == 0 && takeOutEachTeam[1] == 0 && gameRed.size() > 0) {
				takeOutEachTeam[0] = gameRed.size();
			}
			if (takeOutEachTeam[1] == 0 && takeOutEachTeam[0] == 0 && gameBlue.size() > 0) {
				takeOutEachTeam[1] = gameBlue.size();
			}
			for (int index = 0; index < 2; index++) {
				final ArrayList<Player> players = index == 0 ? lobbyRed : lobbyBlue;
				int playerIndex = 0;
				for (final Iterator<Player> it = players.iterator(); it.hasNext();) {
					if (playerIndex++ == takeOutEachTeam[index]) {
						break;
					}
					final Player player = it.next();
					if (player != null) {
						addPlayerToGame(player, Teams.values()[index]);
					}
					it.remove();
				}
			}
		}
		for (final Iterator<Player> it = ((LobbyTask) tasks.get(PlayerType.INSIDE_LOBBY)).getPlayers().iterator(); it.hasNext();) {
			final Player player = it.next();
			if (player != null) {
				player.sendMessage("You now have a higher priority to enter a game of Soul Wars.");
			} else {
				it.remove();
			}
		}
	}

	private void addPlayerToGame(final Player player, final Teams team) {
		final GameTask task = (GameTask) tasks.get(PlayerType.IN_GAME);
		if (task.getPlayers().contains(player) || player.getControlerManager().getControler() instanceof GameController) {
			return;
		}
		player.getControlerManager().startControler("GameController", team.ordinal());
		task.getPlayers().add(player);
	}

	private void startTask(final PlayerType type, final FixedLengthRunnable task) {
		tasks.put(type, task);
		CoresManager.getServiceProvider().scheduleFixedLengthTask(task, 0, (type.equals(PlayerType.OUTSIDE_LOBBY) ? 60 : type.equals(PlayerType.IN_GAME) ? 2 : 5), TimeUnit.SECONDS);
	}

	private void removeFromTasks(final Player player) {
		if (player == null) {
			return;
		}
		final FixedLengthRunnable areaTask = tasks.get(PlayerType.OUTSIDE_LOBBY);
		if (areaTask instanceof AreaTask) {
			((AreaTask) areaTask).getPlayers().remove(player);
		}
		final FixedLengthRunnable lobbyTask = tasks.get(PlayerType.INSIDE_LOBBY);
		if (lobbyTask instanceof LobbyTask) {
			((LobbyTask) lobbyTask).getPlayers().remove(player);
		}
		final FixedLengthRunnable gameTask = tasks.get(PlayerType.IN_GAME);
		if (gameTask instanceof GameTask) {
			((GameTask) gameTask).getPlayers().remove(player);
		}
	}

	private WorldTile getAreaMidpoint(final Teams team, final PlayerType type) {
		final WorldTile start = team.equals(Teams.BLUE) ? type.getLocationA() : type.getLocationC();
		final WorldTile end = team.equals(Teams.BLUE) ? type.getLocationB() : type.getLocationD();
		return new WorldTile(start.getX() + ((end.getX() - start.getX()) / 2),
				start.getY() + ((end.getY() - start.getY()) / 2), start.getPlane());
	}

	public HashMap<PlayerType, FixedLengthRunnable> getTasks() {
		return tasks;
	}

	public enum Teams {
		RED,
		BLUE
	}

	public enum PlayerType {

		OUTSIDE_LOBBY(new WorldTile(1884, 3166, 0), new WorldTile(1888, 3174, 0), new WorldTile(1892, 3166, 0), new WorldTile(1896, 3174, 0)),

		INSIDE_LOBBY(new WorldTile(1870, 3158, 0), new WorldTile(1879, 3166, 0), new WorldTile(1900, 3157, 0), new WorldTile(1909, 3166, 0)),

		IN_GAME(new WorldTile(1816, 3220, 0), new WorldTile(1823, 3230, 0), new WorldTile(1951, 3234, 0), new WorldTile(1958, 3244, 0));

		private final WorldTile LOCATION_A;

		private final WorldTile LOCATION_B;

		private final WorldTile LOCATION_C;

		private final WorldTile LOCATION_D;

		PlayerType(final WorldTile a, final WorldTile b, final WorldTile c, final WorldTile d) {
			LOCATION_A = a;
			LOCATION_B = b;
			LOCATION_C = c;
			LOCATION_D = d;
		}

		public final WorldTile getLocationA() {
			return LOCATION_A;
		}

		public final WorldTile getLocationB() {
			return LOCATION_B;
		}

		public final WorldTile getLocationC() {
			return LOCATION_C;
		}

		public final WorldTile getLocationD() {
			return LOCATION_D;
		}
	}
}
