package com.rs.game.player.content.barrows;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.npc.others.BarrowsCreature;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.contracts.ContractHandler.ContractData;
import com.rs.game.player.content.contracts.ReaperPerks;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.items.BarrowsAmulet;
import com.rs.game.player.content.items.Defenders;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;

public final class Barrows extends Controller {

	private static final WorldTile CENTER = new WorldTile(3565, 3289, 0);
	private static final int LEFT_DOOR = 6715, RIGHT_DOOR = 6732;
	
	private static final WorldTile[] DOOR_LOCATIONS = new WorldTile[] { new WorldTile(3541, 9695, 0), new WorldTile(3541, 9694, 0), new WorldTile(3551, 9684, 3), new WorldTile(3552, 9684, 3), new WorldTile(3562, 9694, 2), new WorldTile(3562, 9695, 2), new WorldTile(3552, 9705, 1), new WorldTile(3551, 9705, 1) };
	private static final WorldTile[] INNER_DOOR_LOCATIONS = new WorldTile[] { new WorldTile(3545, 9694, 2), new WorldTile(3545, 9695, 2), new WorldTile(3552, 9688, 1), new WorldTile(3551, 9688, 1), new WorldTile(3558, 9695, 0), new WorldTile(3558, 9694, 0), new WorldTile(3551, 9701, 3), new WorldTile(3552, 9701, 3) };
	private static final int[][] DOOR_OFFSETS = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };
	public static final WorldTile[] ROOM_CENTER_LOCATIONS = new WorldTile[] { new WorldTile(3534, 9677, 0), new WorldTile(3552, 9677, 0), new WorldTile(3569, 9677, 0), new WorldTile(3569, 9694, 0), new WorldTile(3569, 9712, 0), new WorldTile(3552, 9711, 0), new WorldTile(3534, 9712, 0), new WorldTile(3534, 9694, 0) };
	private static final WorldTile[] BETWEEN_DOORS = new WorldTile[] { new WorldTile(3543, 9679, 0), new WorldTile(3560, 9678, 0), new WorldTile(3568, 9686, 0), new WorldTile(3568, 9703, 0), new WorldTile(3560, 9711, 0), new WorldTile(3543, 9711, 0), new WorldTile(3535, 9703, 0), new WorldTile(3535, 9686, 0), new WorldTile(3552, 9686, 0), new WorldTile(3560, 9694, 0), new WorldTile(3551, 9703, 0), new WorldTile(3543, 9694, 0) };
	/**
	 * Item ID, minimum amount, maximum amount, chance %
	 */
	private static final int[][] RING_OF_WEALTH_REWARDS = {
			{ 558, 60, 60, 70 },
			{ 560, 15, 15, 70 },
			{ 385, 4, 4, 40 },
			{ 141, 1, 1, 40 },
			{ 129, 1, 1, 40 },
			{ 165, 1, 1, 40 },
			{ 4740, 35, 280, 70 },
			{ 985, 1, 1, 30 },
			{ 987, 1, 1, 30 }
	};
	
	private static final int[][] STANDARD_REWARDS = { 
			{ 995, 1, 500000, 70 },
			{ 565, 35, 630, 70 },
			{ 562, 115, 1890, 70 },
			{ 560, 70, 1190, 70 },
			{ 558, 250, 4900, 70 },
			{ 4740, 35, 280, 40 },
			{ 30004, 1, 13, 40 },
			{ 1149, 1, 1, 15 },
			{ 28547, 1, 2, 3 },
			{ 28548, 1, 1, 3 },
			{ 28549, 1, 1, 3 }
	};
	
	public static final int[][] BARROWS_REWARDS = {
			{ 4708, 4710, 4712, 4714, 25652, 25672 },
			{ 4716, 4718, 4720, 4722 },
			{ 4724, 4726, 4728, 4730 },
			{ 4732, 4734, 4736, 4738, 25918, 25895 },
			{ 4745, 4747, 4749, 4751 },
			{ 4753, 4755, 4757, 4759 },
			{ 21736, 21744, 21752, 21760 },
			{ 37433, 37437, 37441, 37445, 37449 }
	};
	
	private static final ItemsContainer<Item> getRewards(Player player) {
		List<Item> standardRewardTable = new ArrayList<Item>();
		List<Item> ringOfWealthRewardTable = null;
		int chance = player.getBarrowsKillCount() >= 8 ? 2 : 1;
		ItemsContainer<Item> finalRewards = new ItemsContainer<Item>(15, true);
		for (int[] standard : STANDARD_REWARDS) {
			if (Utils.random(player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && player.getContract() != null && player.getContract().getNpcId() == ContractData.BARROWS.getNpcId() ? 95 : 100) < standard[3])
				standardRewardTable.add(new Item(standard[0], Utils.random(standard[1],  standard[2])));
		}
		if (player.getEquipment().getRingId() == 2572 || player.getEquipment().getRingId() >= 20653 && player.getEquipment().getRingId() <= 20659) {
			ringOfWealthRewardTable = new ArrayList<Item>();
			for (int[] row : RING_OF_WEALTH_REWARDS) {
				if (Utils.random(player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && player.getContract() != null && player.getContract().getNpcId() == ContractData.BARROWS.getNpcId() ? 95 : 100) < row[3])
					standardRewardTable.add(new Item(row[0], Utils.random(row[1], row[2])));
			}
		}
		for (int i = 0; i < 8; i++) {
			if (player.getKilledBarrowBrothers()[i]) {
				chance++;
				for (int x = 0; x < BARROWS_REWARDS[i].length; x++) {
					if (Utils.random(player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && player.getContract() != null && player.getContract().getNpcId() == ContractData.BARROWS.getNpcId() ? 9500 : 10000) < 125) {
						Item barrowsItem = new Item(BARROWS_REWARDS[i][x]);
						player.getDropCollectionHandler().handleBossKills(barrowsItem, DropCollectionConstants.BARROWS_ID);
						finalRewards.add(barrowsItem);
					}
				}
			}
		}
		for (int i = 0; i < 5; i++) {
			if (standardRewardTable.size() != 0) {
				Item item = standardRewardTable.get(Utils.random(standardRewardTable.size()));
				if (!finalRewards.contains(item))
					finalRewards.add(item);
			} 
		}
		/**
		 * Has to be separate to exclude all kinds of interferance with the main rewards, otherwise RoW could potentially lower the total worth of the end drop.
		 */
		for (int i = 0; i < 5; i++) {
			if (ringOfWealthRewardTable != null && ringOfWealthRewardTable.size() != 0) {
				Item item = ringOfWealthRewardTable.get(Utils.random(ringOfWealthRewardTable.size()));
				if (!finalRewards.contains(item))
					finalRewards.add(item);
			}
		}
		if (!player.getTreasureTrails().hasClueScrollItem()) {
			if (Utils.random(player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && player.getContract() != null && player.getContract().getNpcId() == ContractData.BARROWS.getNpcId() ? 230 : 250) <= chance) {
				if (!player.getTreasureTrails().hasClueScrollItem()) {
					ClueScrollDistributor.givePlayerClueScroll(player, 3);
					player.sendMessage(Colors.CYAN + "You find an elite clue scroll!", false);
				}
			}
		}
		if (Defenders.getCurrentTier(player, 0) && Utils.random(74 - (3 * chance)) == 0) {
			player.setHasUpgradedBarrowsDefender();
			finalRewards.add(new Item(36156));
			player.getDropCollectionHandler().handleBossKills(new Item(36156), DropCollectionConstants.BARROWS_ID);
			player.sendMessage(Colors.CYAN + "You have received a corruption sigil!", false);
			World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Corruption Sigil from Barrows!", false);
		}
		if (finalRewards.getSize() == 0)
			finalRewards.add(new Item(995, Utils.random(100, 1000000)));
		return finalRewards;
	}
	
	private void sendRewards() {
		player.getInterfaceManager().sendInterface(1284);
		player.getPackets().sendInterSetItemsOptionsScript(1284, 7, 99, 8, 3, "Take", "Bank", "Discard", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(1284, 7, 0, 10, 0, 1, 2, 3);
		if (rewards != null)
			player.getPackets().sendItems(99, rewards);
	}
	
	private BarrowsCreature target;
	private BarrowsPuzzle puzzle;
	private int headComponentId, timer;
	private boolean isInside, unlocked;
	private ArrayList<WorldObject> objects;
	
	private ItemsContainer<Item> rewards;
	
	@Override
	public void start() {
		if (player.getHiddenBrother() == -1)
			player.setHiddenBrother(Utils.random(6));
		if (player.hasLootedBarrowsChest())
			if (player.withinDistance(CENTER, 17))
				player.setLootedBarrowsChest(false);
		sendInterfaces();
		puzzle = new BarrowsPuzzle(player, this);
		objects = new ArrayList<WorldObject>();
	}
	
	public void switchLocation() {
		isInside = !isInside;
	}
	
	public void switchLock() {
		this.unlocked = !unlocked;
	}
	
	@Override
	public void sendInterfaces() {
		updateInterface(player, false);
		player.getInterfaceManager().sendMinigameHudInterface(24);
	}
	
	public static final boolean dig(final Player player) {
		for (BarrowsConstants constant : BarrowsConstants.values()) {
			if (constant.getOutsideCoordinates() == null)
				continue;
			if (player.withinDistance(constant.getOutsideCoordinates(), 4)) {
				player.sendMessage("You break into the crypt.");
				((Barrows) player.getControlerManager().getControler()).switchLocation();
				player.setNextWorldTile(constant.getInsideCoordinates());
				player.getPackets().sendBlackOut(2);
				return true;
			}
		}
		return false;
	}
	
	public final void shiftDoors() {
		int random = Utils.random(4);
		for (int i = 0; i < DOOR_LOCATIONS.length; i++) {
			player.getPackets().addSpawnedObject(new WorldObject((i & 1) == 0 ? LEFT_DOOR : RIGHT_DOOR, 0, DOOR_LOCATIONS[i].getPlane(), new WorldTile(DOOR_LOCATIONS[i].getX(), DOOR_LOCATIONS[i].getY(), 0)));
			player.getPackets().addSpawnedObject(new WorldObject((i & 1) == 0 ? LEFT_DOOR : RIGHT_DOOR, 0, INNER_DOOR_LOCATIONS[i].getPlane(), new WorldTile(INNER_DOOR_LOCATIONS[i].getX(), INNER_DOOR_LOCATIONS[i].getY(), 0)));
		}
		player.getPackets().addSpawnedObject(new WorldObject(World.getObjectWithType(new WorldTile(DOOR_LOCATIONS[random * 2].getX(), DOOR_LOCATIONS[random * 2].getY(), 0), 0).getId(), 0, DOOR_LOCATIONS[random * 2].getPlane(), new WorldTile(DOOR_LOCATIONS[random * 2].getX(), DOOR_LOCATIONS[random * 2].getY(), 0)));
		player.getPackets().addSpawnedObject(new WorldObject(World.getObjectWithType(new WorldTile(DOOR_LOCATIONS[(random * 2) + 1].getX(), DOOR_LOCATIONS[(random * 2) + 1].getY(), 0), 0).getId(), 0, DOOR_LOCATIONS[(random * 2) + 1].getPlane(), new WorldTile(DOOR_LOCATIONS[(random * 2) + 1].getX(), DOOR_LOCATIONS[(random * 2) + 1].getY(), 0)));
		player.getPackets().addSpawnedObject(new WorldObject(World.getObjectWithType(new WorldTile(INNER_DOOR_LOCATIONS[random * 2].getX(), INNER_DOOR_LOCATIONS[random * 2].getY(), 0), 0).getId(), 0, INNER_DOOR_LOCATIONS[random * 2].getPlane(), new WorldTile(INNER_DOOR_LOCATIONS[random * 2].getX(), INNER_DOOR_LOCATIONS[random * 2].getY(), 0)));
		player.getPackets().addSpawnedObject(new WorldObject(World.getObjectWithType(new WorldTile(INNER_DOOR_LOCATIONS[(random * 2) + 1].getX(), INNER_DOOR_LOCATIONS[(random * 2) + 1].getY(), 0), 0).getId(), 0, INNER_DOOR_LOCATIONS[(random * 2) + 1].getPlane(), new WorldTile(INNER_DOOR_LOCATIONS[(random * 2) + 1].getX(), INNER_DOOR_LOCATIONS[(random * 2) + 1].getY(), 0)));
	}
	
	public final void openDoor(final WorldObject object) {
		if (target != null) {
			if (!target.isDead() && target.isWight()) {
				player.sendMessage("You need to defeat your target before going further.");
				return;
			}
		}
		if (player == null || object == null)
			return;
		if (player.getHash() != object.getHash()) {
			player.addWalkSteps(object.getX(), object.getY(), 1, true);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					enterDoor(object);
				}
			});
		} else 
			enterDoor(object);
	}
	
	private final void enterDoor(final WorldObject object) {
		player.lock(1);
		int rotation = getRotation(object.getRotation(), object.getId() > 6731);
		if (rotation >= DOOR_OFFSETS.length)
			return;
		WorldObject door = new WorldObject(object.getId(), object.getType(), rotation, object.getX() + DOOR_OFFSETS[object.getRotation()][0], object.getY() + DOOR_OFFSETS[object.getRotation()][1], object.getPlane());
		player.getPackets().addSpawnedObject(new WorldObject(49063, 0, 0, object.getX(), object.getY(), object.getPlane()));
		player.getPackets().addSpawnedObject(door);
		if (player.getTileHash() != object.getTileHash())
			player.addWalkSteps(object.getX(), object.getY(), 1, false);
		else
			player.addWalkSteps(object.getX() + DOOR_OFFSETS[object.getRotation()][0], object.getY() + DOOR_OFFSETS[object.getRotation()][1], 1, false);
		WorldObject otherDoor = null;
		boolean addObjects = objects.isEmpty();
		for (WorldObject objects : World.getRegion(player.getRegionId()).getAllObjects()) {
			if (addObjects && objects.getId() == 37736 && (player.getX() > 3573 && objects.getX() > 3573 || player.getY() < 9673 && objects.getY() < 9673 || player.getX() < 3530 && objects.getX() < 3530 || player.getY() > 9716 && objects.getY() > 9716 || objects.getDistance(player) < 4)) {
				player.getPackets().sendDestroyObject(objects);
				this.objects.add(objects);
			}
			if (otherDoor == null && objects.getHash() != object.getHash() && objects.getId() >= 6716 && objects.getId() <= 6749 && objects.withinDistance(object, 1))
				otherDoor = objects;
		}
		if (!addObjects) {
			objects.forEach(o -> player.getPackets().sendSpawnedObject(o));
			objects.clear();
		}
		if (otherDoor != null)
			handleOtherDoor(otherDoor);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().addSpawnedObject(new WorldObject(49063, 0, 0, door.getX(), door.getY(), door.getPlane()));
				player.getPackets().addSpawnedObject(object);
				spawnRandomNPC();
			}
		}, 1);
	}
	
	private final void handleOtherDoor(final WorldObject object) {
		int rotation = getRotation(object.getRotation(), object.getId() > 6731);
		WorldObject door = new WorldObject(object.getId(), object.getType(), rotation, object.getX() + DOOR_OFFSETS[object.getRotation()][0], object.getY() + DOOR_OFFSETS[object.getRotation()][1], object.getPlane());
		player.getPackets().addSpawnedObject(new WorldObject(49063, 0, 0, object.getX(), object.getY(), object.getPlane()));
		player.getPackets().addSpawnedObject(door);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().addSpawnedObject(new WorldObject(49063, 0, 0, door.getX(), door.getY(), door.getPlane()));
				player.getPackets().addSpawnedObject(object);
			}
		}, 1);
	}
	
	private static final int getRotation(int prev, boolean right) {
		if (right) {
			switch(prev) {
			case 0:
				return 3;
			case 1:
				return 0;
			case 2:
				return 1;
			case 3:
				return 2;
			}
		}
		return prev == 3 ? 0 : prev + 1;
	}
	
	private void sendTarget(int id, WorldTile tile, boolean wight) {
		if (target != null) {
			if (target.isWight())
				target.removeNPC();
			else
				target.finish();
		}
		target = new BarrowsCreature(id, tile, this, wight);
		player.setTarget(null);
		player.setAttackedByDelay(0);
		target.setTarget(player);
		if (wight) {
			target.setNextForceTalk(new ForceTalk("You dare disturb my rest" + (id > 20000 ? "?" : "!")));
			player.getHintIconsManager().addHintIcon(target, 1, -1, false);
		}
	}
	
	public static final void updateInterface(Player player, boolean creaturesOnly) {
		if (!creaturesOnly) {
			for (BarrowsConstants c : BarrowsConstants.values()) {
				if (c.ordinal() == BarrowsConstants.AKRISAE.ordinal())
					player.getPackets().sendConfigByFile(11655, player.getKilledBarrowBrothers()[c.ordinal()] ? 1 : 0);
				else if (c.ordinal() >= BarrowsConstants.LINZA.ordinal())
                    player.getPackets().sendConfigByFile(31434, player.getKilledBarrowBrothers()[c.ordinal()] ? 1 : 0);
				else
					player.getPackets().sendConfigByFile(4554 + c.ordinal(), player.getKilledBarrowBrothers()[c.ordinal()] ? 1 : 0);
			}
		}
		player.getPackets().sendConfigByFile(4562, player.getBarrowsKillCount() + getSlainBrothersCount(player));
	}
	
	private static final int getSlainBrothersCount(Player player) {
		int count = 0;
		for (int i = 0; i < 7; i++)
			if (player.getKilledBarrowBrothers()[i])
				count++;
		return count;
	}
	
	public void finishTarget() {
		if (target == null)
			return;
		if (target.isWight()) {
			for (BarrowsConstants c : BarrowsConstants.values())
				if (c.getNpcId() == target.getId())
					player.getKilledBarrowBrothers()[c.ordinal()] = true;
		} else
			player.setBarrowsKillCount(player.getBarrowsKillCount() + 1);
		updateInterface(player, !target.isWight());
		resetTarget();
	}
	
	public void resetTarget() {
		if (target.isWight())
			player.getHintIconsManager().removeUnsavedHintIcon();
		target = null;
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 6775) {
			if (player.getHiddenBrother() != -1 && !player.getKilledBarrowBrothers()[player.getHiddenBrother()] && !BarrowsAmulet.quickUse(player, 6775)) {
				player.sendMessage("You need to defeat your target before opening the chest.");
				return false;
			}
			if (!player.hasLootedBarrowsChest()) {
				rewards = getRewards(player);
				unlocked = false;
				BarrowsAmulet.resetUses(player);
				player.setLootedBarrowsChest(true);
				player.getPackets().sendCameraShake(3, 12, 25, 12, 25);
				player.getInterfaceManager().removeMinigameHudInterface();
				player.resetBarrows();
				player.incrementBarrowsRunsDone();
				player.sendMessage("You open the chest and find some treasure; barrows runs done: " + Colors.RED + Utils.getFormattedNumber(player.getBarrowsRunsDone()) + "</col>.");
				ContractHandler.updateNonNpcContract(player, 100000);
				player.getAchievements().updateProgress(1, AchievementList.LOOT_100_BARROWS_CHESTS, AchievementList.LOOT_250_BARROWS_CHESTS, AchievementList.LOOT_500_BARROWS_CHESTS);
				ChristmasSeasonalEvent.awardSmallPresent(player);
			}
			sendRewards();
			return false;
		}
		for (BarrowsConstants c : BarrowsConstants.values()) {
			if (c.ordinal() == player.getHiddenBrother() && object.getId() == 6708) {
				player.getHintIconsManager().removeUnsavedHintIcon();
				finishTarget();
				player.setNextWorldTile(c.getBySarcophagusCoordinates());
				return false;
			} else if (object.getId() == c.getStaircaseId()) {
				player.setNextWorldTile(c.getOutsideCoordinates());
				player.getPackets().sendMiniMapStatus(0);
				switchLocation();
				if (target != null)
					target.finish();
				return false;
			} else if (object.getId() == c.getSaprcophagusId()) {
				if (c.ordinal() == player.getHiddenBrother())
					player.getDialogueManager().startDialogue("BarrowsD");
				else if (target != null || player.getKilledBarrowBrothers()[c.ordinal()])
					player.sendMessage("You find nothing.");
				else if(!BarrowsAmulet.quickUse(player, object.getId()))
					sendTarget(c.getNpcId(), player, true);
				return false;
			}
		}
		if (object.getId() == 10284) {
			if (player.getHiddenBrother() != -1) {
				if (!player.getKilledBarrowBrothers()[player.getHiddenBrother()])
					sendTarget(BarrowsConstants.values()[player.getHiddenBrother()].getNpcId(), player, true);
			}
			player.getPackets().addSpawnedObject(new WorldObject(6775, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane()));
			if (target != null)
				return false;
			return false;
		} else if (object.getId() >= 6716 && object.getId() <= 6750) {
			for (WorldTile loc : DOOR_LOCATIONS) {
				if (!unlocked && object.getDistance(loc) < 3) {
					if(!player.getPerkManager().hasPerkActive(DonationPerk.THE_SKIPPER)) {
						player.getTemporaryAttributtes().put("barrowsDoor", object);
						puzzle.shufflePuzzle();
						puzzle.sendInterface();
						return false;
					}
				}
			}
			openDoor(object);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean processObjectClick2(WorldObject object) {
		if (object != null &&
				rewards != null &&
				object.getId() == 6775) {
			for (Item r : rewards.getItems()) {
				if (r == null)
					continue;
				player.getInventory().addItemDrop(r.getId(), r.getAmount());
			}
			rewards.clear();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean processObjectClick3(WorldObject object) {
		if (object.getId() == 6775) {
			player.getPackets().addSpawnedObject(new WorldObject(10284, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane()));
			return false;
		}
		return true;
	}

	public void reloadObjects() {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				for (WorldTile loc : Barrows.ROOM_CENTER_LOCATIONS) 
					player.getPackets().addSpawnedObject(new WorldObject(49063, 10, 0, loc));
				if (player.getHiddenBrother() != -1)
					player.getPackets().addSpawnedObject(new WorldObject(6708, 10, 0, Barrows.ROOM_CENTER_LOCATIONS[player.getHiddenBrother()]));
				shiftDoors();
			}
		}, 2);
	}
	
	private void spawnRandomNPC() {
		List<Integer> list = new ArrayList<Integer>();
		List<WorldTile> locations = new ArrayList<WorldTile>();
		for (BarrowsConstants c : BarrowsConstants.values()) {
			if (player.getKilledBarrowBrothers()[c.ordinal()] || c.ordinal() >= BarrowsConstants.AKRISAE.ordinal() || player.hasLootedBarrowsChest() || c.ordinal() == player.getHiddenBrother()) 
				list.add(Utils.random(2031, 2037));
			else
				list.add(c.getNpcId());
		}
		for (int i = 0; i < 7; i++)
			list.add(Utils.random(2031, 2037));
		int t = list.get(Utils.random(list.size()));
		for (byte[] dirs : Utils.DIRS) {
			WorldTile tile = new WorldTile(player.getX() + dirs[0], player.getY() + dirs[1], player.getPlane());
			if (player.clipedProjectile(tile, true))
				locations.add(tile);
		}
		WorldTile spawnLocation = null;
		if (locations.size() > 0)
			spawnLocation = locations.get(Utils.random(locations.size()));
		if (spawnLocation != null)
			sendTarget(t, spawnLocation, t < 2031 || t > 2037);
	}
	
	private int getBrotherHeadIndex() {
		Integer head = (Integer) player.getTemporaryAttributtes().remove("BarrowsHead");
		if (head == null || head == player.getKilledBarrowBrothers().length - 1)
			head = 0;
		player.getTemporaryAttributtes().put("BarrowsHead", head + 1);
		return player.getKilledBarrowBrothers()[head] ? head : -1;
	}
	
	@Override
	public void process() {
		if (!isInside)
			return;
		if (timer > 0) {
			timer--;
			return;
		}
		
		if (headComponentId == 0) {
            if (player.getHiddenBrother() == -1) {
                player.applyHit(new Hit(player, Utils.random(50) + 1, HitLook.REGULAR_DAMAGE));
                timer = 20 + Utils.random(6);
                return;
            }
			int headIndex = getBrotherHeadIndex();
			if (headIndex == -1) {
				timer = 20 + Utils.random(6);
				return;
			}
//			headComponentId = 9 + Utils.random(2);
//			player.getPackets().sendItemOnIComponent(24, headComponentId, 4761 + headIndex, 0);
//			player.getPackets().sendIComponentAnimation(9810, 24, headComponentId);
			int activeLevel = player.getPrayer().getPrayerpoints();
			if (activeLevel > 0) {
				int level = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
				player.getPrayer().drainPrayer(level / 6);
			}
			timer = 3;
		} else {
//			player.getPackets().sendItemOnIComponent(24, headComponentId, -1, 0);
			headComponentId = 0;
			timer = 20 + Utils.random(6);
		}
	}
	
	@Override
	public void moved() {
		if (!isInside && !isAtBarrows(player))
			forceClose();
	}
	
	@Override
	public boolean login() {
		if (player.getHiddenBrother() == -1)
			player.getPackets().sendCameraShake(3, 25, 50, 25, 50);
		if (!isAtBarrows(player)) {
			player.getPackets().sendMiniMapStatus(2);
			isInside = true;
			World.getRegion(new WorldTile(DOOR_LOCATIONS[0].getX(), DOOR_LOCATIONS[0].getY(), 0).getRegionId(), true);
			reloadObjects();
		}
		timer = 20 + Utils.random(6);
		sendInterfaces();
		puzzle = new BarrowsPuzzle(player, this);
		this.objects = new ArrayList<WorldObject>();
		if (player.withinDistance(ROOM_CENTER_LOCATIONS[0], 50)) {
			boolean hideObjects = false;
			for (WorldTile t : BETWEEN_DOORS) {
				if (player.withinDistance(t, 2)) {
					hideObjects = true;
					break;
				}
			}
			if (player.getX() > 3573 || player.getY() < 9673 || player.getX() < 3530 || player.getY() > 9716)
				hideObjects = true;
			if (hideObjects) {
				for (WorldObject objects : World.getRegion(player.getRegionId()).getAllObjects()) {
					if (objects.getId() == 37736 && (player.getX() > 3573 && objects.getX() > 3573 || player.getY() < 9673 && objects.getY() < 9673 || player.getX() < 3530 && objects.getX() < 3530 || player.getY() > 9716 && objects.getY() > 9716 || objects.getDistance(player) < 4)) {
						player.getPackets().sendDestroyObject(objects);
						this.objects.add(objects);
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (puzzle.checkPuzzle(interfaceId, componentId))
			return false;
		else if (interfaceId == 1284) {
			if (rewards == null)
				return false;
			if (componentId == 7) {
				if (rewards.getSize() < slotId + 1)
					return false;
				if (rewards.get(slotId) == null)
					return false;
				if (rewards.get(slotId).getId() != slotId2)
					return false;
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					if (!rewards.get(slotId).getDefinitions().isStackable() && player.getInventory().getFreeSlots() >= rewards.get(slotId).getAmount() || rewards.get(slotId).getDefinitions().isStackable() && player.getInventory().containsItem(rewards.get(slotId)) || rewards.get(slotId).getDefinitions().isStackable() && player.getInventory().hasFreeSlots()) {
						player.getInventory().addItem(rewards.get(slotId));
						rewards.remove(slotId, rewards.get(slotId));
						rewards.shift();
						player.getPackets().sendItems(99, rewards);
					} else 
						player.sendMessage("You don't have enough free space in inventory to hold any more items.");
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					player.getBank().addItem(rewards.get(slotId), true);
					rewards.remove(slotId, rewards.get(slotId));
					rewards.shift();
					player.getPackets().sendItems(99, rewards);
				} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
					rewards.remove(slotId, rewards.get(slotId));
					rewards.shift();
					player.getPackets().sendItems(99, rewards);
				} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
					player.sendMessage(ItemExaminesDataParser.getExamine(rewards.get(slotId)));
					player.sendMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(new Item(slotId2))) + ".");
				}
			} else if (componentId == 8) {
				if (rewards.freeSlots() == rewards.getSize()) {
					player.sendMessage("There's nothing left in the chest.");
					return false;
				}
				if (rewards != null)
					for (Item items : rewards.getItems()) {
						if (items == null)
							continue;
					player.getBank().addItem(items, true);
				}
				rewards.clear();
				player.getPackets().sendItems(99, rewards);
				player.getBank().refreshItems();
			} else if (componentId == 10) {
				if (rewards.freeSlots() == rewards.getSize()) {
					player.sendMessage("There's nothing left in the chest.");
					return false;
				}
				boolean outOfSpace = false;
				for (Item items : rewards.getItems()) {
					if (items == null)
						continue;
					if (!items.getDefinitions().isStackable() && player.getInventory().getFreeSlots() >= items.getAmount() || items.getDefinitions().isStackable() && player.getInventory().containsItem(items) || items.getDefinitions().isStackable() && player.getInventory().hasFreeSlots()) {
						player.getInventory().addItem(items);
						rewards.remove(items);
					} else {
						outOfSpace = true;
						continue;
					}
				}
				if (outOfSpace)
					player.sendMessage("You don't have enough free space in inventory to hold any more items.");
				rewards.shift();
				player.getPackets().sendItems(99, rewards);
			} else if (componentId == 9) {
				if (rewards.freeSlots() == rewards.getSize()) {
					player.sendMessage("There's nothing left in the chest.");
					return false;
				}
				rewards.clear();
				player.getPackets().sendItems(99, rewards);
			}
		}
		return true;
	}

	@Override
	public boolean processPlayerOption1(Entity target) {
		if (target instanceof BarrowsCreature && target != this.target) {
			player.sendMessage("This isn't your target.");
			return false;
		}
		return true;
	}
	
	@Override
	public void magicTeleported(int type) {
		forceClose();
	}
	
	@Override
	public boolean logout() {
		if (target != null)
			target.finish();
		player.getPackets().sendMiniMapStatus(0);
		if (player.getHiddenBrother() == -1)
			player.getPackets().sendStopCameraShake();
		player.getInterfaceManager().removeMinigameHudInterface();
		return false;
	}

	@Override
	public void forceClose() {
		if (target != null)
			target.finish();
		player.getPackets().sendMiniMapStatus(0);
		if (player.getHiddenBrother() == -1)
			player.getPackets().sendStopCameraShake();
		player.getInterfaceManager().removeMinigameHudInterface();
		removeControler();
	}

	public static final boolean isAtBarrows(Player player) {
		return player.withinDistance(CENTER, 17);
	}
	
}