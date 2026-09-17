package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.GodWarsBosses;
import com.rs.game.activities.instances.GodwarsInstance;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles everything Godwars dungeon related.
 *
 * @author Noel
 */
public class GodWars extends Controller {

	/**
	 * Warpriest of Zamorak equipment.
	 */
	private static final Item[] warpriestZamorak = { new Item(28773), new Item(28776), new Item(28779), new Item(28782),
			new Item(28785), new Item(28788) };
	/**
	 * Warpriest of Saradomin equipment.
	 */
	private static final Item[] warpriestSaradomin = { new Item(28755), new Item(28758), new Item(28761), new Item(28764),
			new Item(28767), new Item(28770) };
	/**
	 * Warpriest of Armadyl equipment.
	 */
	private static final Item[] warpriestArmadyl = { new Item(30288), new Item(30291), new Item(30294), new Item(30297),
			new Item(30300), new Item(30303) };
	/**
	 * Warpriest of Bandos equipment.
	 */
	private static final Item[] warpriestBandos = { new Item(30306), new Item(30309), new Item(30312), new Item(30315),
			new Item(30318), new Item(30321) };
	/**
	 * The Item array for rewards.
	 */
	private static Item[] reward;
	/**
	 * Ints containing our killcount.
	 */
	private int armadylKC, bandosKC, saraKC, zammyKC, zarosKC;

	@Override
	public void start() {
		sendInterfaces();
	}
	
	@Override
	public boolean processCommand(String s, boolean b, boolean c) {
		if (player.isOwner())
			return true;
		return !s.equals("b") && !s.contains("bank");
	}

	/**
	 * Closing all interfaces upon controller removal.
	 */
	public void closeKCInterface() {
		player.getInterfaceManager().closeOverlay(false);
		removeControler();
		resetKillCount();
	}

	@Override
	public void forceClose() {
		checkInstance();
		closeKCInterface();
	}

	public int getArmadylKC() {
		return armadylKC;
	}

	public void setArmadylKC(int armadylKC1) {
		armadylKC = armadylKC1;
	}

	public int getBandosKC() {
		return bandosKC;
	}

	public void setBandosKC(int bandosKC1) {
		bandosKC = bandosKC1;
	}

	public int getSaraKC() {
		return saraKC;
	}

	public void setSaraKC(int saraKC1) {
		saraKC = saraKC1;
	}

	public int getZammyKC() {
		return zammyKC;
	}

	public void setZammyKC(int zammyKC1) {
		zammyKC = zammyKC1;
	}

	public int getZarosKC() {
		return zarosKC;
	}

	public void setZarosKC(int zarosKC1) {
		zarosKC = zarosKC1;
	}

	@Override
	public boolean login() {
		sendInterfaces();
		return false;
	}

	@Override
	public boolean logout() {
		if (!GodWarsBosses.isAtGodwars(player)) {
			if (player.getCurrentInstance() != null && player.getCurrentInstance() instanceof GodwarsInstance) {
				final GodwarsInstance instance = (GodwarsInstance) player.getCurrentInstance();
				if (instance.getWaitingRoom() != null) {
					player.setLocation(instance.getWaitingRoom());
					player.getCurrentInstance().removePlayer(player);
				}
			}
		}
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		checkInstance();
		closeKCInterface();
	}
	
	private final void checkInstance() {
		if (!GodWarsBosses.isAtGodwars(player))
			if (player.getCurrentInstance() != null)
				player.getCurrentInstance().removePlayer(player);
	}

	/**
	 * Killcount required to enter any of the boss rooms.
	 *
	 * @return the KC required.
	 */
	public int kcRequired() {
		if (player.isDeveloper())
			return 0;
		if (player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST))
			return 0;
		return 25;
	}
	
	public static final boolean hasAncientCeremonial(Player player) {
		return player.getEquipment().getHatId() == 20125 &&
				player.getEquipment().getChestId() == 20116 &&
				player.getEquipment().getLegsId() == 20117 &&
				player.getEquipment().getGlovesId() == 20118 &&
				player.getEquipment().getBootsId() == 20119;
	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (player.isLocked())
			return false;
		if (object.getId() == 57258) { // Nex bank room
			player.lock(1);
			if (player.getX() <= 2899) {
				if (getZarosKC() >= kcRequired()) {
					player.setNextWorldTile(new WorldTile(2900, 5203, 0));
					setZarosKC(0);
					sendInterfaces();
				} else {
					if (hasAncientCeremonial(player)) {
						player.setNextWorldTile(new WorldTile(2900, 5203, 0));
						sendInterfaces();
					} else
					player.sendMessage("This door is locked by the power of Zarosian; " + "you'll need <col=ff0000>"
							+ kcRequired() + "</col> KC to enter!");
				}
				return false;
			}
			player.setNextWorldTile(new WorldTile(2899, 5203, 0));
			return false;
		}
		if (object.getId() == 57234) { // Ice path door to Nex's follower room
			player.lock(2);
			final WorldTile tile = new WorldTile(2863 + (player.getX() <= 2860 ? 0 : -3), 5219, 0);
			player.setNextAnimation(new Animation(1133));
			player.setNextForceMovement(
					new ForceMovement(tile, 2, player.getX() <= 2860 ? ForceMovement.EAST : ForceMovement.WEST));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.setNextWorldTile(tile);
					sendInterfaces();
				}
			}, 1);
			return false;
		}
		if (object.getId() == 57256 || object.getId() == 57260) { // Climb
																	// down/up
																	// staircase
																	// Nex
																	// chamber
			if (object.getId() == 57256) {
				if (player.getY() > 5277)
					return false;
			}
			player.lock(3);
			player.getInterfaceManager().sendFadingInterface(120);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.getPackets().sendResetCamera();
					if (object.getId() == 57256)
						player.setNextWorldTile(new WorldTile(2855, 5222, 0));
					else
						player.setNextWorldTile(new WorldTile(2887, 5276, 0));
					player.getInterfaceManager().closeFadingInterface();
					sendInterfaces();
					this.stop();
				}
			}, 2);
			return false;
		}
		if (object.getId() == 75089) { // frozen key door
			player.lock(1);
			if (player.getY() <= 5277) {
				player.setNextWorldTile(new WorldTile(2887, 5279, 0));
				return false;
			}
			if (player.getInventory().containsOneItem(20120)) {
				if (player.getFrozenKeyCharges() > 0) {
					player.setFrozenKeyCharges((byte) (player.getFrozenKeyCharges() - 1));
					if (player.getFrozenKeyCharges() == 0) {
						player.sendMessage("You've used up your last Frozen Key charge; repair it at Bob.");
					} else {
						player.sendMessage("You've used up one Frozen Key charge; " + "charges left: " + Colors.RED
								+ player.getFrozenKeyCharges() + "</col>.");
					}
					player.setNextWorldTile(new WorldTile(2887, 5277, 0));
					return false;
				}
				player.sendMessage("Your Frozen Key has 0 charges left; repair it at Bob.");
				return false;
			}
			player.sendMessage("You will need a Frozen Key to enter this door.");
			return false;
		}
		if (object.getId() == 26286 || object.getId() == 26289 || object.getId() == 26288 || object.getId() == 26287) {
			player.lock(1);
			if (player.getAttackedByDelay() + 15000 > Utils.currentTimeMillis()) {
				player.sendMessage("You can't use this altar until 15 seconds after the end of combat.", true);
				return false;
			}
			final int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
			if (player.getPrayer().getPrayerpoints() < maxPrayer) {
				player.lock(1);
				player.sendMessage("The gods have recharged your prayer points.", true);
				player.setNextAnimation(new Animation(645));
				player.getPrayer().restorePrayer(maxPrayer);
			} else
				player.sendMessage("You already have full prayer points.", true);
			return false;
		}
		if (object.getId() == 26293) { // Rope to outside
			player.lock(1);
			player.useStairs(828, new WorldTile(2916, 3739, 0), 1, 1);
			closeKCInterface();
			return true;
		}
		
		if (object.getId() == 75463) { // Armadyl follower room
			player.lock(1);
			if (player.getY() >= 5280) {
				if (getArmadylKC() >= kcRequired()) {
					if (player.getSkills().getLevelForXp(Skills.RANGE) < 70) {
						player.sendMessage("You need a level of 70 Ranged in order to do this.");
						return false;
					}
					player.useStairs(-1, new WorldTile(2872, 5272, 0), 0, 0);
					return false;
				}
				player.sendMessage("This door is locked by the power of Armadyl; " + "you'll need <col=ff0000>"
						+ kcRequired() + "</col> KC to enter!");
				return false;
			}
			player.useStairs(-1, new WorldTile(2872, 5280, 0), 0, 0);
			return false;
		}
		if (object.getId() == 84024) { // Armadyl boss room
			checkInstance();
			player.lock(3);
			if (player.getX() >= 2831) {
				if (getArmadylKC() >= kcRequired()) {
					player.setNextWorldTile(new WorldTile(2830, 5284, 0));
					setArmadylKC(0);
					sendInterfaces();
				} else
					player.sendMessage("This door is locked by the power of Armadyl; " + "you'll need <col=ff0000>"
							+ kcRequired() + "</col> KC to enter!");
				return false;
			}
			if (GodWarsBosses.isAtGodwars(player))
				player.setNextWorldTile(new WorldTile(2831, 5284, 0));
			else
				player.setNextWorldTile(new WorldTile(2830, 5284, 0));
			return false;
		}
		if (object.getId() == 75462 && object.getX() == 2912) { // Saradomin
																// follower room
			if (player.getSkills().getLevelForXp(Skills.AGILITY) < 70) {
				player.sendMessage("You need a level of 70 Agility in order to climb the rocky wall.");
				return false;
			}
			player.lock(1);
			if (player.getX() <= 2911) {
				if (getSaraKC() >= kcRequired()) {
					player.setNextWorldTile(new WorldTile(2915, 5298, 0));
				} else
					player.sendMessage("This rocky wall is protected by the power of Saradomin; "
							+ "you'll need <col=ff0000>" + kcRequired() + "</col> KC to enter!");
				return false;
			}
			player.setNextWorldTile(new WorldTile(2911, 5298, 0));
			return false;
		}
		if (object.getId() == 75462 && (object.getX() == 2919 || object.getX() == 2920)) { // Saradomin
																							// follower
																							// room
			if (player.getSkills().getLevelForXp(Skills.AGILITY) < 70) {
				player.sendMessage("You need a level of 70 Agility in order to climb the rocky wall.");
				return false;
			}
			player.lock(1);
			if (player.getY() >= 5279) {
				if (getSaraKC() >= kcRequired()) {
					player.setNextWorldTile(new WorldTile(player.getX(), 5275, 0));
				} else
					player.sendMessage("This rocky wall is protected by the power of Saradomin; "
							+ "you'll need <col=ff0000>" + kcRequired() + "</col> KC to enter!");
				return false;
			}
			player.setNextWorldTile(new WorldTile(player.getX(), 5279, 0));
			return false;
		}
		if (object.getId() == 84026) { // Saradomin boss room
			checkInstance();
			player.lock(3);
			if (player.getY() > 5264) {
				if (getSaraKC() >= kcRequired()) {
					player.setNextWorldTile(new WorldTile(2919, 5264, 0));
					setSaraKC(0);
					sendInterfaces();
				} else
					player.sendMessage("This door is locked by the power of Saradomin; " + "you'll need <col=ff0000>"
							+ kcRequired() + "</col> KC to enter!");
				return false;
			}
			player.setNextWorldTile(new WorldTile(2919, 5265, 0));
			return false;
		}
		if (object.getId() == 26384) { // Bandos follower room
			player.lock(1);
			if (player.getX() >= 2851) {
				if (getBandosKC() >= kcRequired()) {
					if (!player.getInventory().containsOneItem(2347)) {
						player.sendMessage("You will need a Hammer in order to bang on the big door.");
						return false;
					}
					if (player.getSkills().getLevelForXp(Skills.STRENGTH) < 70) {
						player.sendMessage("You need a level of 70 Strength in order to bang on the big door.");
						return false;
					}
					player.setNextWorldTile(new WorldTile(2850, 5334, 0));
				} else
					player.sendMessage("This door is locked by the power of Bandos; " + "you'll need <col=ff0000>"
							+ kcRequired() + "</col> KC to enter!");
				return false;
			}
			player.setNextWorldTile(new WorldTile(2851, 5334, 0));
			return false;
		}
		if (object.getId() == 26439) { // Zamorak follower room
			player.lock(1);
			if (player.getY() <= 5338) {
				if (getZammyKC() >= kcRequired()) {
					if (player.getSkills().getLevelForXp(Skills.HITPOINTS) < 70) {
						player.sendMessage("You need a level of 70 Constitution in order to jump over.");
						return false;
					}
					player.setNextWorldTile(new WorldTile(2887, 5344, 0));
				} else
					player.sendMessage("This bridge is protected by the power of Zamorak; " + "you'll need <col=ff0000>"
							+ kcRequired() + "</col> KC to enter!");
				return false;
			}
			player.setNextWorldTile(new WorldTile(2887, 5338, 0));
			return false;
		}
		if (object.getId() == 84028) { // Zamorak boss room
			checkInstance();
			player.lock(3);
			if (player.getY() > 5339) {
				if (getZammyKC() >= kcRequired()) {
					player.setNextWorldTile(new WorldTile(2925, 5339, 0));
					setZammyKC(0);
					sendInterfaces();
				} else
					player.sendMessage("This door is locked by the power of Zamorak; " + "you'll need <col=ff0000>"
							+ kcRequired() + "</col> KC to enter!");
				return false;
			}
			if (GodWarsBosses.isAtGodwars(player))
				player.setNextWorldTile(new WorldTile(2925, 5340, 0));
			else
				player.setNextWorldTile(new WorldTile(2925, 5339, 0));
			return false;
		}
		if (object.getId() >= 26425 && object.getId() <= 26428) {
			if (object.getId() == 26426) {
				if (player.getY() > object.getY()) {
					player.setNextWorldTile(new WorldTile(object.getX(), object.getY(), object.getPlane()));
					return false;
				}
			} else if (object.getId() == 26425) {
				if (player.getX() > object.getX()) {
					player.setNextWorldTile(new WorldTile(object.getX(), object.getY(), object.getPlane()));
					return false;
				}
			} else if (object.getId() == 26428) {
				if (player.getY() < object.getY()) {
					player.setNextWorldTile(new WorldTile(object.getX(), object.getY(), object.getPlane()));
					return false;
				}
			} else if (object.getId() == 26427) {
				if (player.getY() < object.getY()) {
					player.setNextWorldTile(new WorldTile(object.getX(), object.getY(), object.getPlane()));
					return false;
				}
			}
			player.getDialogueManager().startDialogue("GodwarsInstanceD", object.getId() == 26425 ? 6260 : object.getId() == 26428 ? 6203 : object.getId() == 26427 ? 6247 : 6222);
			return false;
		}
		if (object.getId() == 84022) { // Bandos boss room
			checkInstance();
			player.lock(3);
			if (player.getX() <= 2856) {
				if (getBandosKC() >= kcRequired()) {
					player.setNextWorldTile(new WorldTile(2857, 5357, 0));
					setBandosKC(0);
					sendInterfaces();
				} else
					player.sendMessage("This door is locked by the power of Bandos; " + "you'll need <col=ff0000>"
							+ kcRequired() + "</col> KC to enter!");
				return false;
			}
			if (GodWarsBosses.isAtGodwars(player))
				player.setNextWorldTile(new WorldTile(2856, 5357, 0));
			else
				player.setNextWorldTile(new WorldTile(2856, 5356, 0));
			return false;
		}
		if (object.getId() == 57225) { // Nex boss room
			if (player.getCurrentInstance() == null) {
				if (player.getX() < 2900)
					return false;
			/*	if (player.getPet() != null || Pets.hasPet(player)) {
					player.sendMessage("Maybe you should dismiss your pet before passing into the Zaros Ancient Barrier.");
					return false;
				}*/
//				player.getDialogueManager().startDialogue("NexEntrance");
				player.getDialogueManager().startDialogue("GodwarsInstanceD", 13447);
			}/* else {
				if (player.getCurrentInstance().getBoss() != 13447)
					return false;
				if (player.getX() > 77)
					return false;
				player.lock();
				player.sendOptionsDialogue("Are you sure you wish to climb down?", new String[] { "Climb down.", "Stay here." }, new DialogueOptionEvent() {
					
					@Override
					public void run(Player player) {
						int option = getOption();
						if (option == OPTION_1)
							player.setNextWorldTile(player.getCurrentInstance().getWorldTile(15, 19));
					}
				});
				player.unlock();
			}*/
			return false;
		}
		return true;
	}

	/**
	 * Resets all kill count.
	 */
	public void resetKillCount() {
		setZammyKC(0);
		setBandosKC(0);
		setSaraKC(0);
		setArmadylKC(0);
		setZarosKC(0);
	}

	@Override
	public boolean sendDeath() {
		closeKCInterface();
		checkInstance();
		return true;
	}

	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().sendOverlay(601, false);
		sendKCString();
	}

	/**
	 * Fills the interface with our killcount.
	 */
	public void sendKCString() {
		player.getPackets().sendIComponentText(601, 18, getArmadylKC() + "");
		player.getPackets().sendIComponentText(601, 19, getBandosKC() + "");
		player.getPackets().sendIComponentText(601, 20, getSaraKC() + "");
		player.getPackets().sendIComponentText(601, 21, getZammyKC() + "");
	}

	/**
	 * Handles the NPC killcounts / additional drops.
	 *
	 * @param npc
	 *            The NPC.
	 * @return if Godwars NPC.
	 */
	public boolean handleKC(NPC npc) {
		sendKCString();
		if (npc.getId() >= 6210 && npc.getId() <= 6221) {
			setZammyKC(getZammyKC() + 1);
			if (Utils.random((int) (25 - Settings.getDropQuantityRate(player))) == 0) {
				Item item = new Item(20123, 1);
				if (!player.hasItem(item) && !player.hasItem(new Item(20120, 1))) {
					World.addGroundItem(item, new WorldTile(npc), player, false, 60);
					player.sendMessage("The " + npc.getName().toLowerCase() + " has dropped a " + item.getName() + ".");
				}
			}
			/** Warpriest item drops based on Defence level **/
			if (Utils.random((int) (150 - (Settings.getDropQuantityRate(player) * 10))) <= 1) {
				int defenceLvl = player.getSkills().getLevelForXp(Skills.DEFENCE);
				if (defenceLvl >= 25) {
					reward = new Item[] { warpriestZamorak[Utils.random(warpriestZamorak.length)] };
					for (Item item : reward) {
						if (player.hasItem(item))
							continue;
						World.updateGroundItem(item, new WorldTile(npc), player, true);
					}
				}
			}
			return true;
		} else if (npc.getId() >= 6229 && npc.getId() <= 6246) {
			setArmadylKC(getArmadylKC() + 1);
			if (Utils.random((int) (25 - Settings.getDropQuantityRate(player))) == 0) {
				Item item = new Item(20121, 1);
				if (!player.hasItem(item) && !player.hasItem(new Item(20120, 1))) {
					World.addGroundItem(new Item(20121), new WorldTile(npc), player, false, 60);
					player.sendMessage("The " + npc.getName().toLowerCase() + " has dropped a " + item.getName() + ".");
				}
			}
			/** Warpriest item drops based on Defence level **/
			if (Utils.random((int) (250 - (Settings.getDropQuantityRate(player) * 10))) <= 1) {
				int defenceLvl = player.getSkills().getLevelForXp(Skills.DEFENCE);
				if (defenceLvl >= 25) {
					reward = new Item[] { warpriestArmadyl[Utils.random(warpriestArmadyl.length)] };
					for (Item item : reward) {
						if (player.hasItem(item))
							continue;
						World.updateGroundItem(item, new WorldTile(npc), player, true);
					}
				}
			}
			return true;
		} else if (npc.getId() >= 6254 && npc.getId() <= 6259) {
			setSaraKC(getSaraKC() + 1);
			if (Utils.random((int) (25 - Settings.getDropQuantityRate(player))) == 0) {
				Item item = new Item(20124, 1);
				if (!player.hasItem(item) && !player.hasItem(new Item(20120, 1))) {
					World.addGroundItem(item, new WorldTile(npc), player, false, 60);
					player.sendMessage("The " + npc.getName().toLowerCase() + " has dropped a " + item.getName() + ".");
				}
			}
			/** Warpriest item drops based on Defence level **/
			if (Utils.random((int) (250 - (Settings.getDropQuantityRate(player) * 10))) <= 1) {
				int defenceLvl = player.getSkills().getLevelForXp(Skills.DEFENCE);
				if (defenceLvl >= 25) {
					reward = new Item[] { warpriestSaradomin[Utils.random(warpriestSaradomin.length)] };
					for (Item item : reward) {
						if (player.hasItem(item))
							continue;
						World.updateGroundItem(item, new WorldTile(npc), player, true);
					}
				}
			}
			return true;
		} else if (npc.getId() >= 6268 && npc.getId() <= 6283) {
			setBandosKC(getBandosKC() + 1);
			if (Utils.random((int) (25 - Settings.getDropQuantityRate(player))) == 0) {
				Item item = new Item(20122, 1);
				if (!player.hasItem(item) && !player.hasItem(new Item(20120, 1))) {
					World.addGroundItem(item, new WorldTile(npc), player, false, 60);
					player.sendMessage("The " + npc.getName().toLowerCase() + " has dropped a " + item.getName() + ".");
				}
			}
			/** Warpriest item drops based on Defence level **/
			if (Utils.random((int) (250 - (Settings.getDropQuantityRate(player) * 10))) <= 1) {
				int defenceLvl = player.getSkills().getLevelForXp(Skills.DEFENCE);
				if (defenceLvl >= 25) {
					reward = new Item[] { warpriestBandos[Utils.random(warpriestBandos.length)] };
					for (Item item : reward) {
						if (player.hasItem(item))
							continue;
						World.updateGroundItem(item, new WorldTile(npc), player, true);
					}
				}
			}
			return true;
		} else if (npc.getId() >= 13456 && npc.getId() <= 13459) {
			setZarosKC(getZarosKC() + 1);
			player.sendMessage("You've killed a total of " + Colors.RED + Utils.getFormattedNumber(getZarosKC())
					+ "</col> Zaros followers.");
			return true;
		} else
			return false;
	}
}