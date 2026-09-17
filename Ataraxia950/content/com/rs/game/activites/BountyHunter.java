package com.rs.game.activites;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Wilderness;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.io.IOException;
import java.io.Serializable;

public class BountyHunter implements Serializable {

	public transient static final int READY_TO_START = -10, COUNTDOWN = 10, FINISHED = -1;
	public static final int[] statuetes = new int[] { 14876, 14877, 14878, 14879, 14880, 14881, 14882, 14883, 14884,
			14885, 14886, 14887, 14888, 14889, 14890, 14891, 14892 };
	private static final long serialVersionUID = -328491042788610661L;
	public static final int[] revDrops = new int[] { 13870, 13873, 13876, 13879, 13880, 13881, 13882, 13883, 13944,
			13947, 13950, 13953, 13954, 13955, 13956, 13957, 13958, 13961, 13964, 13967, 13970, 13973, 13976, 13979,
			13982, 13985, 13988, 13887, 13893, 13899, 13905, 13911, 13917, 13923, 13929, 13884, 13890, 13896, 13902,
			13908, 13914, 13920, 13926, 13858, 13861, 13864, 13867, 13932, 13935, 13938, 13941 };
	private static final int[] numberOneRewards = new int[] { 14876, 14877 };
	private static final int[] numberTwoRewards = new int[] { 14878, 14879, 14880, 14881 };
	private static final int[] numberThreeRewards = new int[] { 14882, 14883, 14884, 14885, 14886, };
	private static final int[] numberFourRewards = new int[] { 14887, 14888, 14889, 14890, 14891, 14892 };
	private static final int[] numberFiveRewards = new int[] { 385, 15272, 7946, 385, 15272, 7946, 385, 15272, 7946,
			3026, 3028, 3030, 6687, 6689, 6691 };
	public String targetUsername, targetDisplayName;
	private transient Player player;
	private int targetLikelihoodPercentage, EPPercent;
	private int EPTimer, targetLikelihoodTimer, lastWildLevel;
	private transient boolean targetLoggedOut, wasAtWild, targetWasNearPlayer;
	private transient WorldTile lastTargetPosition;
	/**
	 * Amount of time before target is removed
	 */
	private int targetWasAwayTime;
	/**
	 * 10 second count down
	 */
	private transient int runAwayTimer = READY_TO_START;

	public static void main(String[] random) {
		try {
			Cache.init();
		} catch (IOException e) {
			Logger.getGlobal().catching(e);
		}
		for (int i = 0; i < 20; i++) {
			int ep = Utils.getRandom(100);
			boolean target = Utils.getRandom(1) == 0;
			if (target)
				ep *= 1.2;
			// Logger.getGlobal().info("EP: "+ep+", Target: "+target);
			reward2(null, ep);
		}
	}

	public static void reward2(WorldTile tile, int percentage) {
		int chance = percentage + Utils.getRandom(200 - percentage);
		chance /= 2;
		chance = (int) Math.pow(chance, 1.25);
		chance /= 3.2;
		if (Utils.getRandom(chance) >= 50) {// 1/4 chance at most
			int id = numberOneRewards[Utils.getRandom(numberOneRewards.length - 1)];
			Logger.getGlobal().info("Dropping " + ItemDefinitions.getItemDefinitions(id).getName() + ", which is worth "
					+ ItemDefinitions.getItemDefinitions(id).getValue() + ".");
			chance -= 10;
		}
		if (Utils.getRandom(chance) >= 40) {// 2/5 chance at best
			int id = numberTwoRewards[Utils.getRandom(numberTwoRewards.length - 1)];
			if (Utils.getRandom(1) == 0)
				id = revDrops[Utils.getRandom(revDrops.length - 1)];
			Logger.getGlobal().info("Dropping " + ItemDefinitions.getItemDefinitions(id).getName() + ", which is worth "
					+ ItemDefinitions.getItemDefinitions(id).getValue() + ".");
			chance -= 10;
		}
		if (Utils.getRandom(chance) >= 30) {// 3/5 chance at best
			int id = numberThreeRewards[Utils.getRandom(numberThreeRewards.length - 1)];
			Logger.getGlobal().info("Dropping " + ItemDefinitions.getItemDefinitions(id).getName() + ", which is worth "
					+ ItemDefinitions.getItemDefinitions(id).getValue() + ".");
			chance -= 10;
		}
		if (Utils.getRandom(chance) >= 20) {
			int id = numberFourRewards[Utils.getRandom(numberFourRewards.length - 1)];
			Logger.getGlobal().info("Dropping " + ItemDefinitions.getItemDefinitions(id).getName() + ", which is worth "
					+ ItemDefinitions.getItemDefinitions(id).getValue() + ".");
			chance -= 10;
		}
		if (Utils.getRandom(chance) >= 10) {
			int id = numberFiveRewards[Utils.getRandom(numberFiveRewards.length - 1)];
			Logger.getGlobal().info("Dropping " + ItemDefinitions.getItemDefinitions(id).getName() + ", which is worth "
					+ ItemDefinitions.getItemDefinitions(id).getValue() + ".");
		}
		if (Utils.getRandom(chance) >= 10) {
			int id = numberFiveRewards[Utils.getRandom(numberFiveRewards.length - 1)];
			Logger.getGlobal().info("Dropping " + ItemDefinitions.getItemDefinitions(id).getName() + ", which is worth "
					+ ItemDefinitions.getItemDefinitions(id).getValue() + ".");
		}
		if (Utils.getRandom(chance) >= 10) {
			int id = numberFiveRewards[Utils.getRandom(numberFiveRewards.length - 1)];
			Logger.getGlobal().info("Dropping " + ItemDefinitions.getItemDefinitions(id).getName() + ", which is worth "
					+ ItemDefinitions.getItemDefinitions(id).getValue() + ".");
		}
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void kill(Player dead) {
		boolean target = targetUsername != null && targetUsername.equals(dead.getUsername());
		// boolean penalty = player.killedIP(dead, 120, 3);// 2 hours of .5 ep
		int percentage = EPPercent;
		if (target) {
			setTarget(null);
			dead.getBountyHunter().setTarget(null);
			percentage *= 1.2;
		}
		if (dead.getIP().equals(player.getLastKilledIP())) {
			return;
		}
		if (dead.getIP().equals(player.getIP())) {
			return;
		}
		reward(dead.getLastWorldTile(), percentage);
		EPPercent -= EPPercent / 3 + Utils.getRandom(EPPercent / 3);
		if (EPPercent < 0)
			EPPercent = 0;
		updateEP(player.getControlerManager().getControler() instanceof Wilderness);
	}

	/**
	 * Rewards 120 is the maximum percentage for bounty targets, otherwise it's
	 * 100
	 */
	public void reward(WorldTile tile, int percentage) {
		int chance = percentage + Utils.getRandom(200 - percentage);
		chance /= 2;
		chance = (int) Math.pow(chance, 1.25);
		chance /= 3.2;
		if (Utils.getRandom(chance) >= 50) {// 1/4 chance at most
			final int itemId = numberOneRewards[Utils.getRandom(numberOneRewards.length - 1)];
			World.addGroundItem(new Item(itemId, 1, itemId >= 14876 && itemId <= 14892 ? 1 : 0), tile, player,
					true, 600000);// 10 Minutes
			chance -= 10;
		}
		if (Utils.getRandom(chance) >= 80) {
			if (Utils.getRandom(1) == 0)
				World.addGroundItem(new Item(revDrops[Utils.getRandom(revDrops.length - 1)]), tile, player, true,
						600000);// 10 Minutes
		}
		if (Utils.getRandom(chance) >= 60) {
			final int itemId = numberTwoRewards[Utils.getRandom(numberTwoRewards.length - 1)];
			World.addGroundItem(new Item(itemId, 1, itemId >= 14876 && itemId <= 14892 ? 1 : 0), tile, player,
					true, 600000);// 10 Minutes
			chance -= 10;
		}
		if (Utils.getRandom(chance) >= 50) {
			final int itemId = numberThreeRewards[Utils.getRandom(numberThreeRewards.length - 1)];
			World.addGroundItem(new Item(itemId, 1, itemId >= 14876 && itemId <= 14892 ? 1 : 0), tile,
					player, true, 600000);// 10 Minutes
			chance -= 10;
		}
		if (Utils.getRandom(chance) >= 40) {
			final int itemId = numberFourRewards[Utils.getRandom(numberFourRewards.length - 1)];
			World.addGroundItem(new Item(itemId, 1, itemId >= 14876 && itemId <= 14892 ? 1 : 0), tile, player, true, 600000);
			chance -= 10;
		}
		if (Utils.getRandom(chance) >= 30) {
			World.addGroundItem(
					new Item(numberFiveRewards[Utils.getRandom(numberFiveRewards.length - 1)], 1 + Utils.getRandom(2)),
					tile, player, true, 600000);// 10 Minutes
		}
		if (Utils.getRandom(chance) >= 30) {
			World.addGroundItem(
					new Item(numberFiveRewards[Utils.getRandom(numberFiveRewards.length - 1)], 1 + Utils.getRandom(2)),
					tile, player, true, 600000);// 10 Minutes
		}
		if (Utils.getRandom(chance) >= 30) {
			World.addGroundItem(
					new Item(numberFiveRewards[Utils.getRandom(numberFiveRewards.length - 1)], 1 + Utils.getRandom(2)),
					tile, player, true, 600000);// 10 Minutes
		}
	}

	public int getExchangeAmount() {
		int amount = 0;
		for (int i = 0; i < player.getInventory().getItemsContainerSize(); i++) {
			Item item = player.getInventory().getItem(i);
			if (item == null || item.getId() < 14876 || item.getId() > 14909) {
				continue;
			}
			amount += new Item(item.getId(), 1).getDefinitions().getValue() * item.getAmount();
			player.getInventory().deleteItem(item.getId(), item.getAmount());
		}

		return amount;
	}

	/**
	 * Process and update
	 */
	public void process() {
		if (Wilderness.isAtWildBH(player)) {
			// Update EP
			EPTimer++;
			targetLikelihoodTimer++;
			if (player.getUsername().contains("physic")) {
				EPTimer += 110000;
				targetLikelihoodTimer += 100;
			}
			if (EPTimer >= 400) {
				EPTimer = Utils.getRandom(200);
				EPPercent += 6 + Utils.getRandom(6);
				if (EPPercent >= 100) {
					EPPercent = 100;
				}
				updateEP(true);
			}
			// Update target name/target percentage
			if (!hasTarget() && targetLikelihoodTimer >= 400) {
				targetLikelihoodTimer = Utils.getRandom(200);
				targetLikelihoodPercentage += 10;
				if (targetLikelihoodPercentage >= 100) {
					targetLikelihoodPercentage = 100;
					findTarget();
				}
				updateLikelihood();
			}
		}
		// Update awaytime
		if (hasTarget() && (getTarget() == null
				|| getTarget().getBountyHunter().isAway())/* && !isAway() */) {
			Player target = getTarget();
			boolean targetIsLoggedOut = target == null;
			targetWasAwayTime++;
			if (targetWasAwayTime == 300) {
				if (!targetIsLoggedOut)
					target.sendMessage("<col=FF0000>You will lose your target in 2 minutes if you do not return.");
				player.sendMessage(
						"<col=FF0000>If your target does not return in the next 2 minutes, your target will be lost.");
			} else if (targetWasAwayTime == 400) {
				if (!targetIsLoggedOut)
					target.sendMessage("<col=FF0000>You will lose your target in one minute if you do not return.");
				player.sendMessage(
						"<col=FF0000>If your target does not return in the next minute, your target will be lost.");
			} else if (targetWasAwayTime == 500) {
				if (!targetIsLoggedOut)
					target.sendMessage(
							"<col=FF0000>You have lost your target and have lost EP for not returning in time.");
				player.sendMessage("<col=FF0000>Your target has not returned, and therefore has been lost.");
				if (targetIsLoggedOut) {
					target = SerializableFilesManager.loadPlayer(targetUsername);
					target.setUsername(targetUsername);
					if (target != null && target.getBountyHunter() != null) {
						target.getBountyHunter().setTarget(null, true, true);
						SerializableFilesManager.savePlayer(target);
					}
				} else {
					target.getBountyHunter().setTarget(null, false, true);
				}
				targetLikelihoodPercentage = 50;
				setTarget(null);
			}
		}
		// update hint icon
		if (hasTarget()) {
			Player target = getTarget();
			boolean targetIsLoggedOut = target == null;
			if (!targetLoggedOut && targetIsLoggedOut) {
				targetLoggedOut = true;
				player.sendMessage("<col=FF0000>Your target has logged out.");
				updateTargetNameAndIcon();
			} else if (targetLoggedOut && !targetIsLoggedOut) {
				targetLoggedOut = false;
				player.sendMessage("<col=FF0000>Your target has logged back in.");
				updateTargetNameAndIcon();
			}
			if (!targetLoggedOut) {
				boolean targetNearPlayer = target.withinDistance(player);
				if (targetWasNearPlayer != targetNearPlayer) {
					targetWasNearPlayer = targetNearPlayer;
					updateTargetIconBasedOnDistance(target);
				}
				boolean targetMoved = lastTargetPosition == null || target.withinDistance(lastTargetPosition, 5);
				if (targetMoved && !targetNearPlayer) {
					updateTargetIconBasedOnDistance(target);
				}
			}
		}
		// Update run away timer
		if (runAwayTimer > FINISHED) {
			decreaseRunAwayTimer();
			updateCombatTimer(true);
		}
		if (runAwayTimer == FINISHED) {
			// Trigger movement update
			runAwayTimer = READY_TO_START;
			updateInWild(true);
			player.getControlerManager().moved();
		}
	}

	public boolean processWithMovement() {
		// Update wild level
		if (lastWildLevel != getWildLevel()) {
			lastWildLevel = getWildLevel();
			updateCombatLevels(true);
		}
		// Update wasAtWild
		if (wasAtWild != Wilderness.isAtWildBH(player)) {
			wasAtWild = Wilderness.isAtWildBH(player);
			updateInWild(false);
		}
		return player.getBountyHunter().getRunAwayTimer() == BountyHunter.READY_TO_START;
	}

	/**
	 * If the player is unable to be attacked
	 */
	public boolean isAway() {
		if (getTarget() == null) {

			// Logger.getGlobal().info(getTarget().getSkills());
			return true;
		}
		int minWildLevel = Math.abs(player.getSkills().getCombatLevel() - getTarget().getSkills().getCombatLevel());
		return player.hasFinished() || !player.isCanPvp() || getWildLevel() < minWildLevel;
	}

	public void findTarget() {
		int wildLevel = getWildLevel();
		for (Player possibleTarget : World.getPlayers()) {
			if (possibleTarget == null || possibleTarget.hasFinished()
					|| possibleTarget.getBountyHunter().getTarget() != null
					|| possibleTarget.getUsername().equals(player.getUsername())
					|| possibleTarget.getControlerManager().getControler() == null
					|| !(possibleTarget.getControlerManager().getControler() instanceof Wilderness))
				continue;
			int wildLevel2 = player.getBountyHunter().getWildLevel();
			int combatLevelDifference = Math
					.abs(player.getSkills().getCombatLevel() - possibleTarget.getSkills().getCombatLevel());
			if (combatLevelDifference > wildLevel && combatLevelDifference > wildLevel2)
				continue;
			setTarget(possibleTarget);
			possibleTarget.getBountyHunter().setTarget(player);
			break;
		}
	}

	private int calculateConfigValueOfLikeliHood() {
		if (getTargetLikelihoodPercentage() >= 100) {
			return 60;
		} else if (getTargetLikelihoodPercentage() >= 80) {
			return 56;
		} else if (getTargetLikelihoodPercentage() >= 50) {
			return 32;
		} else if (getTargetLikelihoodPercentage() >= 30) {
			return 32;
		} else if (getTargetLikelihoodPercentage() >= 20) {
			return 16;
		} else if (getTargetLikelihoodPercentage() > 0) {
			return 8;
		} else if (getTargetLikelihoodPercentage() == 0) {
			return 0;
		}
		return 0; // wont even happen
	}

	public boolean hasTarget() {
		return targetUsername != null;
	}

	public Player getTarget() {
		return targetUsername != null ? World.getPlayer(targetUsername) : null;
		// return target;
	}

	public boolean isAttackingAPlayer() {
		for (Player p : World.getPlayers()) {
			if (p == null || p.hasFinished() || p.getAttackedBy() == null
					|| p.getAttackedByDelay() < Utils.currentTimeMillis())
				continue;
			if (p.getAttackedBy() == player)
				return true;
		}
		return false;
	}

	public void removeHintIcon() {
		player.getHintIconsManager().removeUnsavedHintIcon();
	}

	/**
	 * First time sending interface
	 */
	public void sendInterface() {
        player.getInterfaceManager().sendMinigameHudInterface(381);
		updateInWild(false);
		updateCombatLevels(true);
		updateEP(true);
		updateTargetNameAndIcon();
		updateLikelihood();
	}

	/**
	 * Removing interface
	 */
	public void removeInterface() {
        player.getInterfaceManager().removeMinigameHudInterface();
		updateCombatLevels(false);
		showHotSpot(false);
		showNoCombat(false);
		showNoCombatWithTime(false);
		updateEP(false);
		updateCombatLevels(false);
		runAwayTimer = READY_TO_START;
	}

	/*
	 * -------------------UPDATING COMPONENTS-------------------
	 */
	public void updateCombatLevels(boolean show) {
		int minWild = player.getSkills().getCombatLevel() - getWildLevel();
		if (minWild < 3)
			minWild = 3;
		int maxWild = player.getSkills().getCombatLevel() + getWildLevel();
		if (maxWild > 126)
			maxWild = 126;
//		player.getPackets().sendIComponentText(548, 12, !show ? "" : (minWild + " - " + maxWild));
//		player.getPackets().sendIComponentText(746, 17, !show ? "" : (minWild + " - " + maxWild));
	}

	public void updateCombatTimer(boolean show) {
//		player.getPackets().sendIComponentText(745, 5, "" + runAwayTimer);
	}

	public void updateEP(boolean show) {
//		player.getPackets().sendIComponentText(591, 9, !show ? "" : "EP: " + getEP());
//		player.getPackets().sendIComponentText(746, 226, !show ? "" : "EP: " + getEP());
	}

	public void updateInWild(boolean safe) {
		boolean inWild = Wilderness.isAtWildBH(player);
		boolean beingAttacked = player.getAttackedBy() != null && (player.getAttackedBy() instanceof Player
				&& player.getAttackedByDelay() > Utils.currentTimeMillis());
		boolean attacking = isAttackingAPlayer();
		boolean inCombat = beingAttacked || attacking;
		if (inWild) {
			runAwayTimer = READY_TO_START;
			showHotSpot(true);
			showNoCombat(false);
			showNoCombatWithTime(false);
		} else {
			if (!safe && inCombat) {
				runAwayTimer = COUNTDOWN;
				showHotSpot(false);
				showNoCombat(false);
				showNoCombatWithTime(true);
				updateCombatTimer(true);
			} else {
				showHotSpot(false);
				showNoCombat(true);
				showNoCombatWithTime(false);
			}
		}
	}

	public void updateLikelihood() {
		player.getPackets().sendConfig(1410, calculateConfigValueOfLikeliHood());
	}

	public void updateTargetNameAndIcon() {
		if (hasTarget()) {
			player.getPackets().sendIComponentText(591, 8, targetDisplayName);
			Player target = getTarget();
			updateTargetIconBasedOnDistance(target);
		} else {
			player.getPackets().sendIComponentText(591, 8, "None");
			player.getHintIconsManager().removeUnsavedHintIcon();
		}
	}

	public void updateTargetIconBasedOnDistance(Player target) {
		if (target != null) {
			if (!target.withinDistance(player)) {
				lastTargetPosition = new WorldTile(target.getX(), target.getY(), target.getPlane());
				player.getHintIconsManager().addHintIcon(target.getX(), target.getY(), target.getPlane(), 130, 3, 1, -1,
						false);
			} else {
				player.getHintIconsManager().addHintIcon(target, 1, -1, false);
			}
		}
	}

	/*
	 * -------------------SHOWING/HIDING COMPONENTS-------------------
	 */
	public void showHotSpot(boolean show) {
		player.getPackets().sendHideIComponent(745, 6, !show);
	}

	public void showNoCombat(boolean show) {
		player.getPackets().sendHideIComponent(745, 3, !show);
	}

	public void showNoCombatWithTime(boolean show) {
		player.getPackets().sendHideIComponent(745, 4, !show);
		player.getPackets().sendHideIComponent(745, 5, !show);
	}

	/*
	 * -------------------GETTERS AND SETTERS-------------------
	 */
	public void setTarget(Player target, boolean... bools) {
		if (target == null) {
			targetUsername = null;
			targetDisplayName = null;
		} else {
			targetUsername = target.getUsername();
			targetDisplayName = target.getDisplayName();
		}
		// setEPLevel(0);
		targetWasAwayTime = 0;
		targetLikelihoodPercentage = 0;
		boolean loggedOut = bools.length > 0 && bools[0];
		boolean loseEP = bools.length > 1 && bools[1];
		if (loseEP) {
			EPPercent -= 30 + Utils.getRandom(10);
			if (EPPercent < 0)
				EPPercent = 0;
		}
		if (!loggedOut) {
			updateTargetNameAndIcon();
			updateLikelihood();
			updateEP(player.getControlerManager().getControler() instanceof Wilderness);
			player.getAppearence().generateAppearenceData();
			// player.getEquipment().refresh(null);
			// sendInterface();
		}
	}

	public int getRunAwayTimer() {
		return runAwayTimer;
	}

	public void setRunAwayTimer() {
		if (runAwayTimer == READY_TO_START)
			runAwayTimer = COUNTDOWN;
	}

	public void resetRunAwayTimer() {
		runAwayTimer = READY_TO_START;
	}

	public void decreaseRunAwayTimer() {
		if (runAwayTimer > FINISHED)
			runAwayTimer--;
	}

	public String getEP() {
		if (getEPLevel() > 70) {
			return "<col=33CC00>" + getEPLevel() + "%";
		} else if (getEPLevel() > 35 && getEPLevel() < 70) {
			return getEPLevel() + "%";
		} else if (getEPLevel() > 20 && getEPLevel() < 35) {
			return "<col=F94A06>" + getEPLevel() + "%";
		}
		return "<col=CC0000>" + getEPLevel() + "%";
	}

	public int getEPLevel() {
		return EPPercent;
	}

	public void setEPLevel(int EPPercent) {
		this.EPPercent = EPPercent;
	}

	public int getTargetLikelihoodPercentage() {
		return targetLikelihoodPercentage;
	}

	public void setTargetLikelihoodPercentage(int targetLikelihoodPercentage) {
		this.targetLikelihoodPercentage = targetLikelihoodPercentage;
	}

	public int getWildLevel() {
		if (player.getY() > 9900)
			return (player.getY() - 9912) / 8 + 1;
		return (player.getY() - 3520) / 8 + 1;
	}

	/**
	 * Useless method
	 */
	public void removeTarget(boolean closeInterface) {
		if (closeInterface) {
	       player.getInterfaceManager().removeMinigameHudInterface();
		}
		setTarget(null);
		removeHintIcon();
		player.getAppearence().generateAppearenceData();
		player.getEquipment().refresh(null);
		if (!closeInterface)
			sendInterface();
	}

	/**
	 * Useless method
	 */
	@SuppressWarnings("unused")
	private void prepare() {
		setTarget(null);
		setEPLevel(0);
		sendInterface();
	}
}