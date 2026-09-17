package com.rs.game.player.content.fistofguthix;

import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.FOGController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.Collections;
import java.util.LinkedList;

/**
 * The fist of guthix minigame class.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 *         Created in Apr 30, 2017 at 8:19:22 PM.
 */
public class FistOfGuthix {

	/**
	 * The coordinates {@link WorldTile} to the center of the fist of guthix
	 * minigame.
	 */
	public static final WorldTile FOG_CENTER = new WorldTile(1664, 5695, 0);

	/**
	 * The locations to which the prey or hunted player is to be teleported to.
	 */
	static final WorldTile[] PREY_LOCS = { new WorldTile(1658, 5665, 0), new WorldTile(1630, 5678, 0), new WorldTile(1626, 5710, 0), new WorldTile(1647, 5729, 0), };

	/**
	 * The locations to which the hunter "character" is teleported to.
	 */
	static final WorldTile[] HUNTER_LOCS = { new WorldTile(1674, 5729, 0), new WorldTile(1690, 5714, 0), new WorldTile(1705, 5692, 0), new WorldTile(1687, 5665, 0), };

	/**
	 * A {@link LinkedList} of the members in the fist of guthix cave.
	 */
	private final LinkedList<Player> caveMembers = new LinkedList<Player>();

	/**
	 * Gets the list of members in the {@link FistOfGuthix} cave.
	 * 
	 * @return the caveMembers
	 */
	private LinkedList<Player> caveMembers() {
		return caveMembers;
	}

	/**
	 * A {@link LinkedList} of the members in the fist of guthix lobby.
	 */
	private final LinkedList<Player> lobbyUsers = new LinkedList<Player>();

	/**
	 * Gets the list of members in the {@link FistOfGuthix} lobby.
	 * 
	 * @return the lobbyUsers
	 */
	private LinkedList<Player> lobbyUsers() {
		return lobbyUsers;
	}

	/**
	 * A {@link LinkedList} of the members in the fist of guthix game.
	 */
	private final LinkedList<Participant> gameParticipants = new LinkedList<Participant>();

	/**
	 * Gets the list of members in the {@link FistOfGuthix} game.
	 * 
	 * @return the gameParticipants
	 */
	LinkedList<Participant> gameMembers() {
		return gameParticipants;
	}

	/**
	 * The number of lobby ticks.
	 */
	private long lobbyTicks;

	/**
	 * Gets the number of lobby ticks.
	 * 
	 * @return the lobbyTicks
	 */
	private long getLobbyTicks() {
		return lobbyTicks;
	}

	/**
	 * Gets the amt of lobby ticks in the {@link FistOfGuthix} lobby.
	 * 
	 * @param ticks
	 */
	FistOfGuthix(long ticks) {
		this.lobbyTicks = ticks;
	}

	/**
	 * Gets the team members.
	 * 
	 * @param player
	 *            The player participant.
	 * @return the participant
	 */
	public Participant getTeam(Player player) {
		for (final Participant participant : gameMembers()) {
			if (participant.chased() == player || participant.hunter() == player)
				return participant;
		}
		return null;
	}

	/**
	 * If the player is at the fist of guthix cave.
	 * 
	 * @param player
	 *            The player's location.
	 * @return the caveLoc
	 */
	public boolean isAtCave(Player player) {
		return (player.getX() >= 1672 && player.getX() <= 1720 && player.getY() >= 5592 && player.getY() <= 5608);
	}

	/**
	 * If the player is in the fist of guthix lobby.
	 * 
	 * @param location
	 *            The location of the player.
	 * @return the lobbyLoc
	 */
	public boolean isInLobby(Player location) {
		return (location.getX() >= 1604 && location.getX() <= 1653 && location.getY() >= 5579 && location.getY() <= 5627);
	}

	/**
	 * If the player is inside a barrier within the game.
	 * 
	 * @param location
	 *            The player location.
	 * @return the barrierLoc
	 */
	boolean isInsideBarrier(Player location) {
		return ((location.getX() >= 1650 && location.getX() <= 1652 && location.getY() >= 5702 && location.getY() <= 5704)
				|| (location.getX() >= 1655 && location.getX() <= 1657 && location.getY() >= 5682 && location.getY() <= 5684)
				|| (location.getX() >= 1666 && location.getX() <= 1668 && location.getY() >= 5703 && location.getY() <= 5705)
				|| (location.getX() >= 1675 && location.getX() <= 1677 && location.getY() >= 5687 && location.getY() <= 5689));
	}

	/**
	 * Whether the player is permitted to have those items or not.
	 * 
	 * @param item
	 *            The item.
	 * @return the permittedItem
	 */
	private boolean isPermitted(Item item) {
		return item.getDefinitions().containsOption("Wear") || item.getDefinitions().containsOption("Wield") || item.getDefinitions().getName().contains(" rune")
				|| item.getDefinitions().getName().contains(" guthix token");
	}

	/**
	 * Handles the entering of the passageway to the {@link FistOfGuthix} lobby.
	 * 
	 * @param participant
	 *            The player that enters the passageway.
	 */
	public void enterPassageway(Player participant) {
		if (caveMembers().contains(participant)) {
			caveMembers().remove(participant);
			final LinkedList<Item> forbiddenItems = new LinkedList<Item>();
			for (int index = 0; index < 28; index++) {
				if (participant.getInventory().getItems().get(index) != null) {
					if (!isPermitted(participant.getInventory().getItems().get(index)))
						forbiddenItems.add(participant.getInventory().getItems().get(index));
				}
			}
			boolean permitEntrance = forbiddenItems.size() <= 0;
			if (!permitEntrance) {
				participant.getPackets().sendGameMessage("An item you are carrying is not allowed into the arena. Please bank it.");
				return;
			}
			if (participant.hasFamiliar()) {
				participant.getPackets().sendGameMessage("Familiars are not allowed in the arena. Please dismiss your familiar.");
				return;
			}
			participant.getControlerManager().startControler("FOGController");
			participant.setNextWorldTile(new WorldTile(1653, 5601, 0));
		} else if (lobbyUsers().contains(participant)) {
			lobbyUsers().remove(participant);
			participant.setNextWorldTile(new WorldTile(1718, 5599, 0));
			if (participant.getControlerManager().getControler() != null)
				((FOGController) participant.getControlerManager().getControler()).exit();
			participant.getControlerManager().removeControlerWithoutCheck();
		}
	}

	/**
	 * The countdown until the game can begin.
	 * 
	 * @param player
	 *            The player.
	 */
	private void countDown(final Player player) {
		player.stopAll();
		player.lock();
		player.reset(false);
		player.getTemporaryAttributtes().put("canFight", false);
		WorldTasksManager.schedule(new WorldTask() {
			int count = 3;

			@Override
			public void run() {
				if (count > 0)
					player.setNextForceTalk(new ForceTalk("" + count));
				if (count == 0) {
					player.getTemporaryAttributtes().put("canFight", true);
					player.unlock();
					player.setNextForceTalk(new ForceTalk("Go!"));
					this.stop();
					return;
				}
				count--;
			}
		}, 0, 1);
	}
	
	/**
	 * If the participant is permitted to grab a stone or not.
	 * 
	 * @param participant
	 *            The participant.
	 */
	public void grabStone(Player participant) {
		boolean canGrab = false;
		for (final Participant player : gameMembers()) {
			if (player.chased() == participant)
				canGrab = true;
		}
		if (participant.getInventory().containsItem(12845, 1))
			canGrab = false;
		if (participant.getEquipment().getWeaponId() == 12845)
			canGrab = false;
		if (canGrab)
			participant.getInventory().addItem(12845, 1);
		else
			participant.getPackets().sendGameMessage("Hunted participants may have one stone only, and hunter participants are not allowed to have any.");
	}


	/**
	 * Processes the {@link FistOfGuthix} minigame, it's components, and
	 * aspects.
	 * 
	 * @return the process complete
	 */
	public FistOfGuthix process() {
		for (final Player player : World.getPlayers()) {
			if (isAtCave(player) && !caveMembers().contains(player) && player.isActive())
				caveMembers().add(player);
			if (isAtCave(player) || isInLobby(player))
				player.setCanPvp(false);
		}
		for (final Player p : caveMembers()) {
			final LinkedList<Item> unusableItems = new LinkedList<Item>();
			if (p.isActive() && isAtCave(p)) {
				if (!p.getInterfaceManager().containsInterface(731))
				    p.getInterfaceManager().sendMinigameHudInterface(731);
				for (int i = 0; i < 28; i++) {
					if (p.getInventory().getItems().get(i) != null) {
						if (!isPermitted(p.getInventory().getItems().get(i)))
							unusableItems.add(p.getInventory().getItems().get(i));
					}
				}
				p.getPackets().sendIComponentText(731, 2, "Rating: " + p.fogRating());
				if (unusableItems.size() > 0)
					p.getPackets().sendIComponentText(731, 0, "The following item is not allowed into the arena:<br> " + unusableItems.getFirst().getDefinitions().getName());
				p.getPackets().sendHideIComponent(731, 5, (!(unusableItems.size() > 0)));
				p.getPackets().sendHideIComponent(731, 4, true);
			}
		}
		for (final Player user : caveMembers()) {
			if (!isAtCave(user)) {
				caveMembers().remove(user);
				if (user.getInterfaceManager().containsInterface(731))
				    user.getInterfaceManager().removeMinigameHudInterface();
			}
		}
		for (final Player p : World.getPlayers()) {
			if (isInLobby(p) && !lobbyUsers().contains(p) && (getTeam(p) == null || !gameParticipants.contains(getTeam(p))))
				lobbyUsers().add(p);
		}
		for (final Player user : lobbyUsers()) {
			final long secs = (lobbyTicks - (((int) getLobbyTicks() / 60) * 60));
			if (user.isActive() && isInLobby(user)) {
				if (!user.getInterfaceManager().containsInterface(731))
				    user.getInterfaceManager().sendMinigameHudInterface(731);
				user.getPackets().sendHideIComponent(731, 5, true);
				user.getPackets().sendHideIComponent(731, 4, false);
				user.getPackets().sendIComponentText(731, 2, "Rating: " + user.fogRating());
				user.getPackets().sendIComponentText(731, 1,
						(lobbyUsers().size() >= 2 ? ("00:0" + ((int) getLobbyTicks() / 60) + ":" + (secs <= 9 ? "0" : "") + secs) : "Not enough players"));
			}
		}
		if (lobbyUsers().size() >= 2 || gameParticipants.size() > 1)
			lobbyTicks--;
		if (lobbyTicks == 0) {
			if (lobbyUsers().size() >= 2 || lobbyUsers.size() > 1) {
				Collections.shuffle(lobbyUsers());
				for (final Player player : lobbyUsers()) {
					for (final Player p2 : lobbyUsers()) {
						if (p2 != player && gameParticipants.size() <= 125) {
							final Participant p = new Participant(player, p2);
							gameMembers().add(p);
							lobbyUsers().remove(player);
							lobbyUsers().remove(p2);
						}
						for (final Participant participant : gameMembers()) {
							final int location = Utils.random(HUNTER_LOCS.length);
							participant.hunter().setNextWorldTile(HUNTER_LOCS[location]);
							participant.chased().setNextWorldTile(PREY_LOCS[location]);
							participant.hunter().setCanPvp(true);
							participant.chased().setCanPvp(true);
							countDown(participant.chased());
							countDown(participant.hunter());
						}
					}
				}
				for (final Player player : lobbyUsers()) {
					player.getPackets().sendGameMessage(gameParticipants.size() == 125 ? "You have been left behind because the game is full."
							: "You have been left behind because the server was unable to pair you with a partner.");
				}
				lobbyTicks = 60;
			} else {
				for (final Player player : lobbyUsers()) {
					player.getPackets().sendGameMessage("Not enough players to start a game, you need " + 2 + " - 250 players to run a game.");
				}
			}
		}
		for (final Participant participant : gameMembers()) {
			if (participant.chased().getInterfaceManager().containsInterface(731))
			    participant.chased().getInterfaceManager().removeMinigameHudInterface();
			if (participant.hunter().getInterfaceManager().containsInterface(731))
			    participant.hunter().getInterfaceManager().removeMinigameHudInterface();
			if (!participant.hunter().getInterfaceManager().containsInterface(730))
			    participant.hunter().getInterfaceManager().sendMinigameHudInterface(730);
			if (!participant.chased().getInterfaceManager().containsInterface(730))
				participant.chased().getInterfaceManager().sendMinigameHudInterface(730);
			participant.process();
		}
		return this;
	}

	/**
	 * Processes the death process.
	 * 
	 * @param player
	 *            The player to die.
	 */
	public void processDeath(Player player) {
		for (final Participant participant : gameMembers()) {
			if (participant.chased() == player || participant.chased() == player)
				participant.processDeath(player);
		}
	}

	/**
	 * Manages the entering of the portal to a certain world tile.
	 * 
	 * @param player
	 *            The player to enter.
	 */
	public void managePortalEntering(Player player) {
		player.setNextWorldTile(new WorldTile(1677, 5599, 0));
	}

	/**
	 * Manages the exiting of the portal to a certain world-tile.
	 * 
	 * @param player
	 *            The player to exit.
	 */
	public void managePortalExit(Player player) {
		player.setNextWorldTile(new WorldTile(2969, 9672, 0));
	}

	/**
	 * Handles the barriers for the prey to escape via.
	 * 
	 * @param objBarrier
	 *            The object that is the barrier.
	 * @param participant
	 *            The participant to enter the barrier.
	 */
	public void handleBarriers(WorldObject objBarrier, Player participant) {
		if (getTeam(participant) != null && getTeam(participant).hunter() == participant) {
			participant.getPackets().sendGameMessage("The magical barrier prevents you from passing through.");
			return;
		}
		if (getTeam(participant) != null && getTeam(participant).lastEnteredBase() != 0 && !(Utils.currentTimeMillis() - getTeam(participant).lastEnteredBase() >= 4000)
				&& !isInsideBarrier(participant)) {
			participant.getPackets().sendGameMessage("You are unable to enter the hiding base until another 4 seconds.");
			return;
		}
		switch (objBarrier.getRotation()) {
		case 1:
			if (!isInsideBarrier(participant) && participant.getX() == objBarrier.getX() && participant.getY() == objBarrier.getY()) {
				participant.setNextWorldTile(new WorldTile(participant.getX(), participant.getY() + 1, participant.getPlane()));
				getTeam(participant).lastEnteredBase(Utils.currentTimeMillis());
			} else if (participant.getX() == objBarrier.getX()) {
				participant.setNextWorldTile(new WorldTile(participant.getX(), participant.getY() - 1, participant.getPlane()));
				getTeam(participant).lastEnteredBase(Utils.currentTimeMillis());
			}
			break;
		case 2:
			if (!isInsideBarrier(participant) && participant.getX() == objBarrier.getX() && participant.getY() == objBarrier.getY()) {
				participant.setNextWorldTile(new WorldTile(participant.getX() + 1, participant.getY(), participant.getPlane()));
				getTeam(participant).lastEnteredBase(Utils.currentTimeMillis());
			} else if (participant.getY() == objBarrier.getY()) {
				participant.setNextWorldTile(new WorldTile(participant.getX() - 1, participant.getY(), participant.getPlane()));
				getTeam(participant).lastEnteredBase(Utils.currentTimeMillis());
			}
			break;
		case 0:
			if (!isInsideBarrier(participant) && participant.getNextWorldTile().getX() == objBarrier.getX() && participant.getNextWorldTile().getY() == objBarrier.getY()) {
				participant.setNextWorldTile(new WorldTile(participant.getNextWorldTile().getX() - 1, participant.getNextWorldTile().getY(), participant.getPlane()));
				getTeam(participant).lastEnteredBase(Utils.currentTimeMillis());
			} else if (participant.getNextWorldTile().getY() == objBarrier.getY()) {
				participant.setNextWorldTile(new WorldTile(participant.getNextWorldTile().getX() + 1, participant.getNextWorldTile().getY(), participant.getPlane()));
				getTeam(participant).lastEnteredBase(Utils.currentTimeMillis());
			}
			break;
		case 3:
			if (!isInsideBarrier(participant) && participant.getNextWorldTile().getX() == objBarrier.getX() && participant.getNextWorldTile().getY() == objBarrier.getY()) {
				participant.setNextWorldTile(new WorldTile(participant.getNextWorldTile().getX(), participant.getNextWorldTile().getY() - 1, participant.getPlane()));
				getTeam(participant).lastEnteredBase(Utils.currentTimeMillis());
			} else if (participant.getNextWorldTile().getX() == objBarrier.getX()) {
				participant.setNextWorldTile(new WorldTile(participant.getNextWorldTile().getX(), participant.getNextWorldTile().getY() + 1, participant.getPlane()));
				getTeam(participant).lastEnteredBase(Utils.currentTimeMillis());
			}
			break;
		}
	}

	/**
	 * Handles the entering process to the portal.
	 * 
	 * @param portalObj
	 *            The portal to enter.
	 * @param participant
	 *            The participant that enters the portal.
	 */
	public void handlePortalEntering(WorldObject portalObj, final Player participant) {
		if (!isInsideBarrier(participant))
			return;
		if (getTeam(participant) != null && getTeam(participant).hunter() == participant)
			return;
		final LinkedList<WorldTile> portalTiles = new LinkedList<WorldTile>();
		portalTiles.add(new WorldTile(1651, 5703, 0));
		portalTiles.add(new WorldTile(1656, 5683, 0));
		portalTiles.add(new WorldTile(1667, 5704, 0));
		portalTiles.add(new WorldTile(1676, 5688, 0));
		Collections.shuffle(portalTiles);
		for (final WorldTile tile : portalTiles) {
			if (portalObj.getX() != tile.getX() && portalObj.getY() != tile.getY()) {
				participant.addWalkSteps(portalObj.getX(), portalObj.getY(), participant.getPlane(), false);
				participant.getPackets().sendGameMessage("You step into the portal...");
				participant.lock();
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						participant.setNextWorldTile(tile);
						participant.setNextGraphics(new Graphics(2000));
						participant.unlock();
						stop();
					}
				}, 1);
				break;
			}
		}
	}

	/**
	 * Determines the points at certain distances.
	 * 
	 * @param player
	 *            The player.
	 * @return the determinedPoints
	 */
	int determinePoints(Player player) {
		if (FOGUtil.computeDistance(player) >= 0 && FOGUtil.computeDistance(player) <= 5)
			return 27;
		if (FOGUtil.computeDistance(player) >= 6 && FOGUtil.computeDistance(player) <= 10)
			return 23;
		if (FOGUtil.computeDistance(player) >= 11 && FOGUtil.computeDistance(player) <= 15)
			return 19;
		if (FOGUtil.computeDistance(player) >= 16 && FOGUtil.computeDistance(player) <= 20)
			return 14;
		if (FOGUtil.computeDistance(player) >= 21 && FOGUtil.computeDistance(player) <= 25)
			return 10;
		if (FOGUtil.computeDistance(player) >= 26 && FOGUtil.computeDistance(player) <= 30)
			return 6;
		if (FOGUtil.computeDistance(player) >= 31 && FOGUtil.computeDistance(player) <= 50)
			return 3;
		return 0;
	}
}
