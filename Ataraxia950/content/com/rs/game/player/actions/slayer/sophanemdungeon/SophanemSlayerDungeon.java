package com.rs.game.player.actions.slayer.sophanemdungeon;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.controllers.Controller;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

/**
 * Location: 2384, 6793, 3
 */
public final class SophanemSlayerDungeon extends Controller {

	private static final int INTERFACE_ID = 1073;
	private static final int CHEST_OBJECT_ID = 109140;
	private static final int CORRUPTION_COMPONENT_ID = 2;
	private static final int CORRUPTION_AMOUNT_COMPONENT_ID = 3;

	@Override
	public void start() {
		sendInterfaces();
	}

	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public boolean login() {
		sendInterfaces();
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		resetCorruption();
		exitInterface();
		removeControler();
	}

	@Override
	public boolean processMagicTeleport(WorldTile to) {
		resetCorruption();
		exitInterface();
		removeControler();
		return true;
	}

	@Override
	public boolean processItemTeleport(WorldTile to) {
		resetCorruption();
		exitInterface();
		removeControler();
		return true;
	}

	@Override
	public void forceClose() {
		resetCorruption();
		exitInterface();
		removeControler();
	}

	@Override
	public boolean sendDeath() {
		resetCorruption();
		exitInterface();
		removeControler();
		return true;
	}

	@Override
	public void sendInterfaces() {
	    //player.getInterfaceManager().sendMinigameHudInterface(INTERFACE_ID);
		//player.getPackets().sendIComponentText(INTERFACE_ID, CORRUPTION_COMPONENT_ID, "Corruption:");
		//player.getPackets().sendIComponentText(INTERFACE_ID, CORRUPTION_AMOUNT_COMPONENT_ID, player.getSophanemCorruption() + "%");
	}

	void updateSophanemCorruptionComponent() {
		player.getPackets().sendIComponentText(INTERFACE_ID, CORRUPTION_AMOUNT_COMPONENT_ID,
				player.getSophanemCorruption() + "%");
	}

	private void exitInterface() {
	    player.getInterfaceManager().removeMinigameHudInterface();
		player.getPackets().sendIComponentText(INTERFACE_ID, CORRUPTION_COMPONENT_ID, "");
		player.getPackets().sendIComponentText(INTERFACE_ID, CORRUPTION_AMOUNT_COMPONENT_ID, "");
	}

	private void resetCorruption() {
		player.setSophanemCorruption(0);
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int itemId, int packetId) {
	    val lootingInterface = 1284;

		if (interfaceId == lootingInterface) {
			if (player.getSophanemChestLoot().isEmpty()) {
				return false;
			}
			if (componentId == 7) {
				if (player.getSophanemChestLoot().size() < slotId + 1) {
					return false;
				}
				if (player.getSophanemChestLoot().get(slotId) == null) {
					return false;
				}
				if (player.getSophanemChestLoot().get(slotId).getId() != itemId) {
					return false;
				}
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					if (!player.getSophanemChestLoot().get(slotId).getDefinitions().isStackable() && player.getInventory().getFreeSlots() >= player.getSophanemChestLoot().get(slotId).getAmount() || player.getSophanemChestLoot().get(slotId).getDefinitions().isStackable() && player.getInventory().containsItem(player.getSophanemChestLoot().get(slotId)) || player.getSophanemChestLoot().get(slotId).getDefinitions().isStackable() && player.getInventory().hasFreeSlots()) {
						player.getInventory().addItem(player.getSophanemChestLoot().get(slotId));
						final Item item = player.getSophanemChestLoot().get(slotId);
						player.removeSophanemChestLoot(item.getId(), item.getAmount());
						player.getPackets().sendItems(99, player.getSophanemChestLoot().toArray(new Item[player.getSophanemChestLoot().size()]));
					} else {
						player.sendMessage("You don't have enough free space in inventory to hold any more items.");
					}
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					player.getBank().addItem(player.getSophanemChestLoot().get(slotId), true);
					final Item item = player.getSophanemChestLoot().get(slotId);
					player.removeSophanemChestLoot(item.getId(), item.getAmount());
					player.getPackets().sendItems(99, player.getSophanemChestLoot().toArray(new Item[player.getSophanemChestLoot().size()]));
				} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
					final Item item = player.getSophanemChestLoot().get(slotId);
					player.removeSophanemChestLoot(item.getId(), item.getAmount());
					player.getPackets().sendItems(99, player.getSophanemChestLoot().toArray(new Item[player.getSophanemChestLoot().size()]));
				} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
					player.sendMessage(ItemExaminesDataParser.getExamine(player.getSophanemChestLoot().get(slotId)));
					player.sendMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(new Item(itemId))) + ".");
				}
			} else if (componentId == 8) {
				if (player.getSophanemChestLoot().isEmpty()) {
					player.sendMessage("There's nothing left in the chest.");
					return false;
				}
				for (Item items : player.getSophanemChestLoot()) {
					if (items == null) {
						continue;
					}
					player.getBank().addItem(items, true);
				}
				player.getSophanemChestLoot().clear();
				player.getPackets().sendItems(99, player.getSophanemChestLoot().toArray(new Item[player.getSophanemChestLoot().size()]));
				player.getBank().refreshItems();
			} else if (componentId == 10) {
				if (player.getSophanemChestLoot().isEmpty()) {
					player.sendMessage("There's nothing left in the chest.");
					return false;
				}
				boolean outOfSpace = false;
				final List<Item> toRemove = new ArrayList<Item>();
				for (Item items : player.getSophanemChestLoot()) {
					if (items == null) {
						continue;
					}
					if (!items.getDefinitions().isStackable() && player.getInventory().getFreeSlots() >= items.getAmount() || items.getDefinitions().isStackable() && player.getInventory().containsItem(items) || items.getDefinitions().isStackable() && player.getInventory().hasFreeSlots()) {
						player.getInventory().addItem(items);
						toRemove.add(items);
					} else {
						outOfSpace = true;
					}
				}
				player.getSophanemChestLoot().removeAll(toRemove);
				if (outOfSpace) {
					player.sendMessage("You don't have enough free space in inventory to hold any more items.");
				}
				player.getPackets().sendItems(99, player.getSophanemChestLoot().toArray(new Item[player.getSophanemChestLoot().size()]));
				return false;
			}
		}
		return true;
	}

	private void openChest() {
		val chestInterfaceId = 1284;

		player.getInterfaceManager().sendInterface(chestInterfaceId);
		player.getPackets().sendInterSetItemsOptionsScript(chestInterfaceId, 7, 99, 8, 4, "Take", "Bank", "Discard", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(chestInterfaceId, 7, 0, 32, 0, 1, 2, 3);
		player.getPackets().sendItems(99, player.getSophanemChestLoot().toArray(new Item[0]));
	}

	private void transport(WorldTile tile) {
		val transportTime = 2;

		player.lock();
		FadingScreen.fade(player, transportTime, () -> {
			player.setNextWorldTile(tile);
			player.unlock();
		});
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		val ropeDown = 109155;
		val ropeUp = 109156;
		val exit = 109139;

		val northEastRopeDownTile = new WorldTile(2405, 6857, 3);
		val northWestRopeDownTile = new WorldTile(2375, 6840, 3);
		val southEastRopeDownTile = new WorldTile(2421, 6789, 3);

		val ropeUpTile = new WorldTile(2405, 6862, 1);
		val gorillaAkhRopeUpTile = new WorldTile(2436, 6829, 1);
		val felineAkhRopeUpTile = new WorldTile(2412, 6868, 1);

		val objectId = object.getId();
		val objectLocation = new WorldTile(object);
		if (objectId == CHEST_OBJECT_ID) {
			openChest();
			return false;
		} else if (objectId == ropeDown) {
			if (objectLocation.matches(northEastRopeDownTile)) {
				val downDungeon = new WorldTile(2405, 6862, 1);
				transport(downDungeon);
				return false;
			} else if (objectLocation.matches(northWestRopeDownTile)) {
				val downDungeon = new WorldTile(2412, 6868, 1);
				transport(downDungeon);
				return false;
			} else if (objectLocation.matches(southEastRopeDownTile)) {
				val downDungeon = new WorldTile(2437, 6829, 1);
				transport(downDungeon);
				return false;
			}
		} else if (objectId == ropeUp) {
			if (objectLocation.matches(ropeUpTile)) {
				val upDungeon = new WorldTile(2404, 6856, 3);
				transport(upDungeon);
				return false;
			} else if (objectLocation.matches(gorillaAkhRopeUpTile)) {
				val upDungeon = new WorldTile(2419, 6789, 3);
				transport(upDungeon);
				return false;
			} else if (objectLocation.matches(felineAkhRopeUpTile)) {
				val upDungeon = new WorldTile(2375, 6838, 3);
				transport(upDungeon);
				return false;
			}
		} else if (objectId == exit) {
			val outside = new WorldTile(3285, 2743, 0);
			transport(outside);
			forceClose();
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		if (object.getId() == CHEST_OBJECT_ID) {
			player.setAutomaticLootCollection(!player.isAutomaticSophanemLootCollection());
			player.sendMessage((player.isAutomaticSophanemLootCollection() ? Colors.GREEN : Colors.RED) + "Loot auto-collection toggled: " + (player.isAutomaticSophanemLootCollection() ? "on." : "off."));
			return false;
		}
		return true;
	}

}
