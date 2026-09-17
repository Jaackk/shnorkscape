package com.rs.game.activites.soulwars;

import com.rs.cores.FixedLengthRunnable;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.soulwars.SoulWarsManager.PlayerType;
import com.rs.game.activites.soulwars.SoulWarsManager.Teams;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Savions Sw
 */
public class GameTask extends FixedLengthRunnable {

	public static final WorldTile[][] AREAS = { { new WorldTile(1837, 3213, 0), new WorldTile(1847, 3223, 0) },
			{ new WorldTile(1872, 3221, 0), new WorldTile(1901, 3242, 0) },
			{ new WorldTile(1928, 3240, 0), new WorldTile(1938, 3250, 0) }, };

	private static final WorldTile[] ANTI_CARRICADES = { new WorldTile(1959, 3239, 0), new WorldTile(1933, 3243, 0),
			new WorldTile(1842, 3220, 0), new WorldTile(1815, 3225, 0) };

	private static final String[] OBJECT_NAMES = { "western graveyard", "soul obelisk", "eastern graveyard" };

	private Teams[] areas = new Teams[3];

	private int callOutGhosts = -1;

	private int[] areaValues = new int[3];

	private final ArrayList<Player> players = new ArrayList<Player>(500);

	private int[] kills = new int[2];

	private int[] levels = { 100, 100 };

	private final ArrayList<Barricade> redBarricades = new ArrayList<Barricade>(5);

	private final ArrayList<Barricade> blueBarricades = new ArrayList<Barricade>(5);

	private final Avatar[] avatars = new Avatar[2];

	private int ticks = 1;

	@Override
	public boolean repeat() {
		try {
			if (SoulWarsManager.MINUTES_BEFORE_NEXT_GAME.get() < 4)
				return true;
			handleAreas();
			boolean showInterfaces = ++ticks % 3 == 0;
			if (showInterfaces)
				ticks = 1;
			boolean ghostTick = callOutGhosts != -1;
			for (Iterator<Player> it = players.iterator(); it.hasNext();) {
				Player player = it.next();
				if (player != null && player.getTemporaryAttributtes().remove("soulwars_kicked") != null)
					it.remove();
				else if (player != null && player.getControlerManager().getControler() instanceof GameController) {
					player.getControlerManager().sendInterfaces();
					if (showInterfaces)
						((GameController) player.getControlerManager().getControler()).decreaseActivity();
					if (ghostTick) {
						final Integer respawnIndex = (Integer) player.getTemporaryAttributtes()
								.remove("soul_wars_last_respawn_index");
						final WorldTile respawnTile = (WorldTile) player.getTemporaryAttributtes()
								.remove("soul_wars_last_respawn_loc");
						if (respawnIndex != null && respawnIndex == callOutGhosts && respawnTile != null
								&& respawnIndex != 1 && player.getAppearence().getTransformedNpcId() != -1) {
							final WorldTile base = new WorldTile(
									AREAS[respawnIndex][0].getX()
											+ ((AREAS[respawnIndex][1].getX() - AREAS[respawnIndex][0].getX()) / 2),
									AREAS[respawnIndex][0].getY()
											+ ((AREAS[respawnIndex][1].getY() - AREAS[respawnIndex][0].getY()) / 2),
									0);
							player.setNextWorldTile(base.transform(0, respawnIndex == 0 ? 2 : -2, 0));
							player.getAppearence().transformIntoNPC(-1);
							player.getPackets().sendGameMessage(
									"The graveyard has became neutral again and will no longer give you immunity.");
						}
					}
				}
			}
			callOutGhosts = -1;
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
		return true;
	}

	private void handleAreas() {
		int[][] playerAmounts = new int[3][2];
		for (Player player : players) {
			for (int index = 0; index < playerAmounts.length; index++) {
				if (player != null && player.getX() >= AREAS[index][0].getX() && player.getY() >= AREAS[index][0].getY()
						&& player.getX() <= AREAS[index][1].getX() && player.getY() <= AREAS[index][1].getY()
						&& player.getAppearence().getTransformedNpcId() == -1) {
					final Teams team = Teams.values()[player.getEquipment().getCapeId()
							- SoulWarsManager.TEAM_CAPE_INDEX];
					playerAmounts[index][team.ordinal()]++;
					if (player.getControlerManager().getControler() instanceof GameController)
						((GameController) player.getControlerManager().getControler()).setAreaIndex(index);
					break;
				} else if (player != null)
					if (player.getControlerManager().getControler() instanceof GameController)
						((GameController) player.getControlerManager().getControler()).setAreaIndex(-1);
			}
		}
		for (int index = 0; index < playerAmounts.length; index++) {
			final Teams generalTeam = playerAmounts[index][0] > playerAmounts[index][1] ? Teams.RED
					: playerAmounts[index][0] < playerAmounts[index][1] ? Teams.BLUE : null;
			if (generalTeam != null) {
				if (areas[index] != null && areas[index].equals(generalTeam)) {
					if (areaValues[index] < 10) {
						areaValues[index]++;
						if (areaValues[index] == 10)
							sendGlobalMessage(index, true);
					}
				} else if (areas[index] != null
						&& areas[index].equals(generalTeam.equals(Teams.RED) ? Teams.BLUE : Teams.RED)) {
					if (areaValues[index] == 10)
						sendGlobalMessage(index, false);
					if (--areaValues[index] <= 0)
						areas[index] = null;
				} else if (areaValues[index] < 10) {
					areas[index] = generalTeam;
					areaValues[index]++;
				}
			} else if ((playerAmounts[index][0] > 0 && playerAmounts[index][1] > 0)
					|| (areaValues[index] < 10) && areas[index] != null) {
				if (--areaValues[index] <= 0)
					areas[index] = null;
			}
		}
	}

	private void sendGlobalMessage(int index, boolean taken) {
		if ((index == 0 || index == 2) && !taken)
			callOutGhosts = index;
		for (Iterator<Player> it = players.iterator(); it.hasNext();) {
			Player player = it.next();
			if (player != null)
				player.getPackets().sendGameMessage("The "
						+ (areas[index].equals(Teams.BLUE) ? "<col=337FB5>blue</col>" : "<col=F00004>red</col>")
						+ " team has " + (taken ? "taken" : "lost") + " control of the " + OBJECT_NAMES[index] + ".");
			else
				it.remove();
		}
	}

	public void start() {
		reset();
		for (int i = 0; i < 2; i++)
			(avatars[i] = new Avatar(SoulWarsManager.AVATAR_INDEX + i,
					i == 0 ? new WorldTile(1968, 3251, 0) : new WorldTile(1807, 3210, 0), -1, true)).setRandomWalk(1);
	}

	public void reset() {
		for (int i = 0; i < 2; i++)
			if (avatars[i] != null)
				avatars[i].finish();
		ticks = 1;
		areas = new Teams[3];
		areaValues = new int[3];
		kills = new int[2];
		levels = new int[] { 100, 100 };
		for (Barricade barricade : redBarricades) {
			if (barricade != null)
				barricade.finish();
		}
		redBarricades.clear();
		for (Barricade barricade : blueBarricades) {
			if (barricade != null)
				barricade.finish();
		}
		blueBarricades.clear();
		players.clear();
		callOutGhosts = -1;
	}

	public ArrayList<Player> getPlayers(Teams team) {
		ArrayList<Player> members = new ArrayList<Player>(players.size());
		for (Player player : players) {
			if (player != null && player.getEquipment().getCapeId() != -1
					&& Teams.values()[player.getEquipment().getCapeId() - SoulWarsManager.TEAM_CAPE_INDEX].equals(team))
				members.add(player);
		}
		return members;
	}

	public boolean containsBarricade(WorldTile tile) {
		for (Barricade barricade : redBarricades)
			if (barricade != null && barricade.matches(tile))
				return false;
		for (Barricade barricade : blueBarricades)
			if (barricade != null && barricade.matches(tile))
				return false;
		return true;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public int getAvatarDies(Teams team) {
		return kills[team.ordinal()];
	}

	public void increaseAvatarDies(Teams team) {
		kills[team.ordinal()]++;
	}

	public int getAvatarSlayerLevel(Teams team) {
		return levels[team.ordinal()];
	}

	public void increaseAvatarLevel(Teams team, int amount) {
		levels[team.ordinal()] = levels[team.ordinal()] + amount > 100 ? 100
				: levels[team.ordinal()] + amount < 0 ? 0 : levels[team.ordinal()] + amount;
	}

	public WorldTile getRespawnLocation(Player player, int team) {
		WorldTile respawnLocation = World.soulWars.calculateRandomLocation(Teams.values()[team], PlayerType.IN_GAME);
		int respawnIndex = -1;
		for (int index = 0; index < 3; index++) {
			if (index == 1 || areas[index] == null || !areas[index].equals(Teams.values()[team])
					|| areaValues[index] < 10)
				continue;
			final WorldTile base = new WorldTile(
					AREAS[index][0].getX() + ((AREAS[index][1].getX() - AREAS[index][0].getX()) / 2),
					AREAS[index][0].getY() + ((AREAS[index][1].getY() - AREAS[index][0].getY()) / 2), 0);
			if (Utils.getDistance(player, respawnLocation) > Utils.getDistance(player, base)) {
				respawnLocation = new WorldTile(base, 1);
				respawnIndex = index;
			}
		}
		player.getTemporaryAttributtes().put("soul_wars_last_respawn_index", respawnIndex);
		player.getTemporaryAttributtes().put("soul_wars_last_respawn_loc", respawnLocation);
		return respawnLocation;
	}

	public Teams[] getTeamAreas() {
		return areas;
	}

	public int[] getTeamAreaValues() {
		return areaValues;
	}

	public void increaseKill(int id) {
		kills[id - SoulWarsManager.AVATAR_INDEX]++;
		levels[id - SoulWarsManager.AVATAR_INDEX] = 100;
	}

	public Avatar[] getAvatars() {
		return avatars;
	}

	public void removeBarricade(Barricade barricade, int team) {
		if (team == 0)
			redBarricades.remove(barricade);
		else
			blueBarricades.remove(barricade);
	}

	public boolean placeBarricade(Player player, int team) {
		if (team == 0 ? redBarricades.size() >= 10 : blueBarricades.size() >= 10) {
			player.getPackets().sendGameMessage("Your team has placed enough barricades already.");
			return false;
		}
		for (final WorldTile blockedTile : ANTI_CARRICADES) {
			if (player.matches(blockedTile)) {
				player.getPackets().sendGameMessage("You cannot place a barricade here.");
				return false;
			}
		}
		player.getInventory().deleteItem(4053, 1);
		if (team == 0)
			redBarricades.add(new Barricade(player, team));
		else
			blueBarricades.add(new Barricade(player, team));
		return true;
	}
}
