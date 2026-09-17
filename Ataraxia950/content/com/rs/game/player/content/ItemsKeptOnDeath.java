package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.CorpBeastController;
import com.rs.game.player.controllers.CrucibleController;
import com.rs.utils.Utils;

import java.util.concurrent.CopyOnWriteArrayList;

public class ItemsKeptOnDeath {

	private static final ItemsContainer<Item> rewards = new ItemsContainer<>(28, false);

	public static int getPrice(Item item) {
		switch (item.getId()) {
		case 23659:
			item.getDefinitions().setValue(1);
			break;
		case 6570:
			item.getDefinitions().setValue(1);
			break;
		case 4151:
			item.getDefinitions().setValue(10000000);
			break;
		case 11694:
			item.getDefinitions().setValue(1500000000);
			break;
		case 11730:
			item.getDefinitions().setValue(40000000);
			break;
		case 18353:
			item.getDefinitions().setValue(285000000);
			break;
		case 6585:
			item.getDefinitions().setValue(28500000);
			break;
		case 14484:
			item.getDefinitions().setValue(1000000000);
			break;
		case 13740:
			item.getDefinitions().setValue(2000000000);
			break;
		case 13738:
			item.getDefinitions().setValue(1700000000);
			break;
		case 13744:
			item.getDefinitions().setValue(1500000000);
			break;
		case 13742:
			item.getDefinitions().setValue(1900000000);
			break;
		case 22494:
			item.getDefinitions().setValue(35000000);
			break;
		case 21787:
		case 21793:
		case 21790:
			item.getDefinitions().setValue(75000000);
			break;
		case 18357:
		case 18355:
		case 16403:
		case 16425:
		case 16955:
		case 18349:
		case 16423:
		case 18351:
			item.getDefinitions().setValue(1000000000);
			break;
		case 6737:
		case 6731:
		case 6733:
		case 6734:
			item.getDefinitions().setValue(80000000);
			break;
		case 1038:
		case 1040:
		case 1042:
		case 1044:
		case 1046:
		case 1048:
			item.getDefinitions().setValue(2100000000);
			break;
		case 1050:
			item.getDefinitions().setValue(1);
			break;
		case 11724:
			item.getDefinitions().setValue(170000000);
			break;
		case 19784:
			item.getDefinitions().setValue(900000000);
			break;
		case 11726:
			item.getDefinitions().setValue(150000000);
			break;
		case 21371:
			item.getDefinitions().setValue(50000000);
			break;
		case 4722:
		case 4720:
		case 4718:
		case 4716:
			item.getDefinitions().setValue(30000000);
			break;
		case 23675:
		case 23676:
		case 23677:
		case 23678:
		case 28020:
			item.getDefinitions().setValue(3);
			break;
		case 37130:
			item.getDefinitions().setValue(4);
			break;
		case 34007:
		case 34008:
		case 34036:
			item.getDefinitions().setValue(5);
			break;
		case 33733:
		case 33734:
		case 33735:
		case 24474:
		case 20929:
			item.getDefinitions().setValue(7);
			break;
		case 962:
		case 37131:
		case 34006:
			item.getDefinitions().setValue(10);
			break;
		case 34028:
			item.getDefinitions().setValue(15);
			break;
		}
		return item.getDefinitions().getValue();
	}

	public static int getRiskedWealth(ItemsContainer<Item> lostItems) {
		long amount = 0;
		for (Item item : lostItems.toArray()) {
			if (item != null) {
				amount += getPrice(item) * item.getAmount();
			}
		}
		if (amount < 0 || amount > Integer.MAX_VALUE) {
			amount = Integer.MAX_VALUE;
		}
		return (int) amount;
	}

	public static void showInterface(Player player) {
		if (player.getInterfaceManager().containsScreenInter()) {
			player.getPackets().sendGameMessage(
					"Please close the interface you have opened before using Items Kept On Death interface.");
			return;
		}
		if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
			player.getPackets()
					.sendGameMessage("You can't open the interface until 10 seconds after the end of combat.");
			return;
		}
		CopyOnWriteArrayList<Item> containedItems = new CopyOnWriteArrayList<Item>();
		rewards.clear();
		/*
		 * keptItems.clear(); containedItems.clear();
		 */
		int keptAmount = 3;
		if (!(player.getControlerManager().getControler() instanceof CorpBeastController)
				&& !(player.getControlerManager().getControler() instanceof CrucibleController)) {
			keptAmount = player.hasSkull() ? 0 : 3;
		} else {
			keptAmount = 0;
		}
		if (player.getPrayer().isProtectingItem())
			keptAmount++;
		Item lastItem = new Item(1, 1);
		for (int i = 0; i < 14; i++) {
			if (player.getEquipment().getItem(i) != null && player.getEquipment().getItem(i).getId() != -1
					&& player.getEquipment().getItem(i).getAmount() != -1)
				containedItems.add(new Item(player.getEquipment().getItem(i).getId(),
						player.getEquipment().getItem(i).getAmount()));
		}
		for (int i = 0; i < 28; i++) {
			if (player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getId() != -1
					&& player.getInventory().getItem(i).getAmount() != -1)
				containedItems.add(new Item(player.getInventory().getItem(i).getId(),
						player.getInventory().getItem(i).getAmount()));
		}
		if (containedItems.isEmpty()) {
			player.getPackets().sendGameMessage("You're not risking anything.");
			return;
		}
		if (keptAmount == 0) {
			player.getPackets().sendGameMessage("You're not protecting any items, unless you're in a safe zone.");
			return;
		}
		int itemsToProtect = 0;
		for (int i = 0; i < keptAmount; i++) {
			for (Item item : containedItems) {
				int price = getPrice(item);
				if (price >= lastItem.getDefinitions().getValue()) {
					lastItem = item;
				}
			}
			if (itemsToProtect == keptAmount || itemsToProtect > containedItems.size())
				break;
			rewards.add(lastItem);
			containedItems.remove(lastItem);
			itemsToProtect++;
			lastItem = new Item(1, 1);
		}
		player.getInterfaceManager().sendInterface(364);
		player.getPackets().sendInterSetItemsOptionsScript(364, 4, 90, 3, 4, "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(364, 4, 0, 12, 0, 1, 2, 3);
		player.getPackets().sendItems(90, rewards);
		player.getDialogueManager().startDialogue("SimpleNPCMessage", 3709,
				"The items that appears on the interface above will be kept on death, your TOTAL wealth is "
						+ (getRiskedWealth(rewards) == Integer.MAX_VALUE ? "UNKNOWN" : getRiskedWealth(rewards) + ".")
						+ ".");
	}

}