package com.rs.game.activites.creations;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.bots.BotManager;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;

public class StealingCreationGame extends Controller {

	private static final int DEFAULT_KILN_CATEGORY = 0;
	private static final int[][] KILN_CATEGORY_PRODUCTS = {
			{ 15, 16, 17, 18, 19, 23, 20, 21, 22, 24 },
			{ 6, 7, 8, 9, 11, 10, 12, 13, 14 },
			{ 0, 1, 2, 3, 4, 5 },
			{ 27, 28, 29, 30, 31, 32, 33, 34 }
	};

	private int index = 0;
	private int kilnCategory = DEFAULT_KILN_CATEGORY;
	private int selectedKilnProduct = -1;
	private boolean inRedTeam;
	private boolean loggingOut;
	private boolean interfacesReady;
	private boolean hiddenByFog;

	// X : 1926 Y : 5716

	private void calculateKilnIndex() {
		for (int index = 4; index >= 0; index--) {
			int item = StealingCreation.SACRED_CLAY[index];
			if (player.getInventory().containsOneItem(item)) {
				this.index = index;
				return;
			}
		}
		this.index = 0;
	}

	@Override
	public boolean login() {
		StealingCreation.removeFromTeams(player);
		return true;// removes script
	}

	@Override
	public boolean logout() {
		loggingOut = true;
		clearFogVisibility();
		player.setCanPvp(false);
		StealingCreation.resetGamePlayerOptions(player);
		StealingCreation.removeFromTeams(player);
		return true;// removes script
	}

	@Override
	public void forceClose() {
		super.forceClose();
		clearFogVisibility();
		interfacesReady = false;
		boolean forceJoiningLobby = Boolean.TRUE.equals(player.getTemporaryAttributtes()
				.get(StealingCreation.FORCE_JOINING_ATTR));
		player.setCanPvp(false);
		player.setForceMultiArea(false);
		StealingCreation.removeStealingCreationItems(player);
		StealingCreation.resetPlayerInterface(player);
		StealingCreation.resetGamePlayerOptions(player);
		StealingCreation.removeFromTeams(player);
		if (!loggingOut && !player.hasFinished() && !forceJoiningLobby) {
			player.setNextWorldTile(StealingCreation.LOBBY_WORLDTILE);
		}
	}

	@Override
	public boolean canHit(Entity entity) {
		if (isFogCombatBlocked(entity)) {
			return false;
		}
		if (entity instanceof Player && StealingCreation.isSameTeam(player, (Player) entity)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean processPlayerOption1(Entity target) {
		if (isFogCombatBlocked(target)) {
			player.getPackets().sendGameMessage("The fog hides that player from combat.");
			return false;
		}
		if (target instanceof Player && StealingCreation.isSameTeam(player, (Player) target)) {
			player.getPackets().sendGameMessage("You cannot attack players on your own team.");
			return false;
		}
		return true;
	}

	@Override
	public boolean canPlayerOption1(Player target) {
		if (isFogCombatBlocked(target)) {
			player.getPackets().sendGameMessage("The fog hides that player from combat.");
			return false;
		}
		if (StealingCreation.isSameTeam(player, target)) {
			player.getPackets().sendGameMessage("You cannot attack players on your own team.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processPlayerOption4(Player target) {
		StealingCreation.startPickpocket(player, target);
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage",
				"A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage",
				"A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public boolean keepCombating(boolean mainHand, Entity target) {
		return !isFogCombatBlocked(target);
	}

	private boolean isFogCombatBlocked(Entity target) {
		return target instanceof Player
				&& (StealingCreation.isInsideFog(player) || StealingCreation.isInsideFog((Player) target));
	}

	@Override
	public boolean sendDeath() {
		player.lock(5);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;

			@Override
			public void run() {
				if (player == null || player.hasFinished()) {
					stop();
					return;
				}
				if (loop == 0) {
					Player killer = player.getMostDamageReceivedSourcePlayer();
					StealingCreation.handleDefeat(player);
					BotManager.notifyDeath(player, killer);
					player.setNextAnimation(new Animation(836));
					player.getPackets().sendGameMessage("You have been defeated and return to your base.");
				} else if (loop == 2) {
					player.reset();
					player.resetReceivedDamage();
					player.setNextWorldTile(StealingCreation.getGameTile(inRedTeam));
					player.setNextAnimation(new Animation(-1));
					player.setCanPvp(true);
					StealingCreation.sendGamePlayerOptions(player);
					interfacesReady = true;
					sendInterfaces();
					player.unlock();
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void trackXP(int skillId, int addedXp) {
		if (skillId == Skills.HITPOINTS) {
			StealingCreation.recordCombatDamage(player, addedXp);
		}
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (interfaceId == 813) {
			if (componentId >= 1 && componentId <= 5) {
				index = componentId - 1;
				player.getPackets().sendGameMessage("Selected class " + (index + 1) + " clay.");
				return true;
			}
			if (componentId >= 8 && componentId <= 11) {
				kilnCategory = componentId - 8;
				selectedKilnProduct = -1;
				return true;
			}
			if (componentId == 18) {
				selectKilnProduct(slotId, packetId);
				return true;
			}
			if (componentId >= 12 && componentId <= 26) {
				selectKilnProduct(componentId - 12, packetId);
				return true;
			}
			if (componentId >= 37 && componentId <= 71) {
				selectedKilnProduct = componentId - 37;
				processKilnExchange(componentId, packetId);
			}
			return true;
		}
		return true;
	}

	private void selectKilnProduct(int categorySlot, int packetId) {
		int componentIndex = getCategoryProductIndex(categorySlot);
		if (componentIndex < 0) {
			player.getPackets().sendGameMessage("There is no Stealing Creation item in that slot.");
			return;
		}
		selectedKilnProduct = componentIndex;
		processSelectedKilnProduct(packetId);
	}

	private void processSelectedKilnProduct(int packetId) {
		if (selectedKilnProduct < 0) {
			player.getPackets().sendGameMessage("Select an item to make first.");
			return;
		}
		processKilnExchange(selectedKilnProduct + 37, packetId);
	}

	private int getCategoryProductIndex(int categorySlot) {
		if (kilnCategory < 0 || kilnCategory >= KILN_CATEGORY_PRODUCTS.length) {
			return -1;
		}
		int[] products = KILN_CATEGORY_PRODUCTS[kilnCategory];
		if (categorySlot < 0 || categorySlot >= products.length) {
			return -1;
		}
		return products[categorySlot];
	}

	private void processKilnExchange(int componentId, int packetId) {
		int itemId = StealingCreation.SACRED_CLAY[index];
		int amount = 0;
		if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
			amount = 1;
		else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
			amount = 5;
		else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
			player.getTemporaryAttributtes().put("scIndex", index);
			player.getTemporaryAttributtes().put("scComponentId", componentId);
			player.getTemporaryAttributtes().put("scItemId", itemId);
			player.getTemporaryAttributtes().put("scAmount", amount);
			player.getTemporaryAttributtes().put("kilnX", true);
			player.getPackets().sendInputIntegerScript("Enter Amount:");
		} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
			amount = player.getInventory().getAmountOf(itemId);
		if (amount == 0 && packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
			return;
		}
		if (amount == 0) {
			amount = 1;
		}
		if (amount > 0) {
			amount = Math.min(amount, player.getInventory().getAmountOf(itemId));
		}
		int componentIndex = componentId - 37;
		if (!isProductInCurrentCategory(componentIndex)) {
			player.getPackets().sendGameMessage("Select an item from the active Stealing Creation category.");
			return;
		}
		if (StealingCreation.checkSkillRequriments(player, StealingCreation.getRequestedKilnSkill(componentIndex), index)) {
			if ((amount != 0 && StealingCreation.proccessKilnItems(player, componentId, index, itemId, amount)))
				return;
		}
	}

	private boolean isProductInCurrentCategory(int componentIndex) {
		if (kilnCategory < 0 || kilnCategory >= KILN_CATEGORY_PRODUCTS.length) {
			return componentIndex >= 0 && componentIndex < StealingCreation.CLASS_ITEMS_BASE.length;
		}
		for (int product : KILN_CATEGORY_PRODUCTS[kilnCategory]) {
			if (product == componentIndex) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (StealingCreation.isResourceBarrier(object)) {
			StealingCreation.handleResourceBarrierClick(player, object);
			return false;
		}
		if (StealingCreation.isBaseGateHelper(object)) {
			if (StealingCreation.canPassBaseGateHelper(player, object)) {
				StealingCreation.passBaseGateHelper(player, object);
			} else {
				player.getPackets().sendGameMessage("You cannot pass through the enemy team's barrier.");
			}
			return false;
		}
		if (StealingCreation.isBaseDoor(object)) {
			if (StealingCreation.canPassBaseDoor(player, object)) {
				StealingCreation.passBaseDoor(player, object);
			} else {
				player.getPackets().sendGameMessage("You cannot pass through the enemy team's barrier.");
			}
			return false;
		}
		if (object.getId() == StealingCreation.DEPOSIT_OBJECT) {
			StealingCreation.depositInventory(player);
			return false;
		}
		if (object.getId() == StealingCreation.PROCESSING_KILN) {
			if (!player.getInventory().containsOneItem(StealingCreation.SACRED_CLAY)) {
				player.getPackets().sendGameMessage(
						"You try using the processing point, but quickly realize that you have no sacred clay with you.");
				return false;
			}
			calculateKilnIndex();
			kilnCategory = DEFAULT_KILN_CATEGORY;
			selectedKilnProduct = -1;
			player.getInterfaceManager().sendInterface(StealingCreation.KILN_INTERFACE);
			unlockKilnInterface();
			return false;
		}
		int baseId = StealingCreation.getResourceBaseId(object);
		if (baseId >= 0) {
			startResourceGathering(object, baseId, StealingCreation.getResourceAnimationStyle(object));
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		if (StealingCreation.isResourceBarrierBuildSpot(object)) {
			StealingCreation.buildResourceBarrier(player, object);
			return false;
		}
		return true;
	}

	@Override
	public boolean handleItemOnObject(WorldObject object, Item item) {
		if (StealingCreation.isResourceBarrierBuildSpot(object)) {
			StealingCreation.buildResourceBarrier(player, object, item == null ? -1 : item.getId());
			return false;
		}
		return true;
	}

	private void startResourceGathering(WorldObject object, int baseId, int animationStyle) {
		int resourceIndex = StealingCreation.getResourceIndex(object.getId());
		if (resourceIndex < 0) {
			return;
		}
		int toolIndex = StealingCreation.getBestToolIndexForBase(player, baseId);
		Animation animation = toolIndex < 0 ? null : StealingCreation.getAnimationForBase(toolIndex, animationStyle);
		StealingCreation.startDynamicSkill(player, object, animation, baseId, resourceIndex);
	}

	private void unlockKilnInterface() {
		for (int componentId = 1; componentId <= 5; componentId++) {
			unlockKilnComponent(componentId);
		}
		for (int componentId = 8; componentId <= 11; componentId++) {
			unlockKilnComponent(componentId);
		}
		for (int componentId = 12; componentId <= 26; componentId++) {
			unlockKilnComponent(componentId);
		}
		unlockKilnComponent(18, 0, 14, 6, 0, 1, 2, 3, 4);
	}

	private void unlockKilnComponent(int componentId) {
		unlockKilnComponent(componentId, 0, 0, 2, 0);
	}

	private void unlockKilnComponent(int componentId, int fromSlot, int toSlot, int settings, int... options) {
		player.getPackets().sendIComponentSettings(StealingCreation.KILN_INTERFACE, componentId, fromSlot, toSlot,
				settings);
		player.getPackets().sendUnlockIComponentOptionSlots(StealingCreation.KILN_INTERFACE, componentId, fromSlot,
				toSlot, options);
	}

	@Override
	public void process() {
		updateFogVisibility();
		healInOwnBase();
	}

	private void updateFogVisibility() {
		boolean inFog = StealingCreation.isInsideFog(player);
		if (inFog) {
			if (!player.getAppearence().isHidden()) {
				player.getAppearence().setHidden(true);
				hiddenByFog = true;
			}
			if (player.getActionManager().getAction() instanceof PlayerCombat) {
				player.getActionManager().forceStop();
				player.setNextFaceEntity(null);
			}
		} else {
			clearFogVisibility();
		}
	}

	private void clearFogVisibility() {
		if (hiddenByFog) {
			player.getAppearence().setHidden(false);
			hiddenByFog = false;
		}
	}

	private void healInOwnBase() {
		if (!StealingCreation.isInsideOwnBase(player, inRedTeam)) {
			return;
		}
		int maxHitpoints = player.getMaxHitpoints();
		if (maxHitpoints <= 0 || player.getHitpoints() >= maxHitpoints) {
			return;
		}
		player.setHitpoints(maxHitpoints);
		player.refreshHitPoints();
	}

	@Override
	public void sendInterfaces() {
		if (!interfacesReady) {
			return;
		}
		StealingCreation.refreshGameHud(player);
		player.getInterfaceManager().sendMinigameHudInterface(StealingCreation.GAME_INTERFACE);
		StealingCreation.refreshGameHud(player);
	}

	@Override
	public void start() {
		inRedTeam = getArguments() != null && getArguments().length > 0 && (boolean) getArguments()[0];
		interfacesReady = false;
		StealingCreation.ensureGameTeam(player, inRedTeam);
		StealingCreation.ensureScore(player, inRedTeam);
		StealingCreation.resetPlayerInterface(player);
		player.stopAll(true, false, true);
		player.setCanPvp(true);
		StealingCreation.sendGamePlayerOptions(player);
		player.setForceMultiArea(true);
		player.setNextWorldTile(StealingCreation.getGameTile(inRedTeam));
		player.getPackets().sendGameMessage("The Stealing Creation match has begun. You are on the "
				+ (inRedTeam ? "red" : "blue") + " team.");
		sendInterfacesWhenArenaLoads();
	}

	private void sendInterfacesWhenArenaLoads() {
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;

			@Override
			public void run() {
				if (player == null || player.hasFinished() || player.getControlerManager() == null
						|| player.getControlerManager().getControler() != StealingCreationGame.this) {
					stop();
					return;
				}
				if (!player.clientHasLoadedMapRegion() && ticks++ < 10) {
					return;
				}
				interfacesReady = true;
				sendInterfaces();
				stop();
			}
		}, 1, 1);
	}
}
