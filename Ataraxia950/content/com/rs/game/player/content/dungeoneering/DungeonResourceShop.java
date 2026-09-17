package com.rs.game.player.content.dungeoneering;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.content.ItemConstants;

public class DungeonResourceShop {

	public static final int RESOURCE_SHOP = 956, RESOURCE_SHOP_INV = 957;
	private static final int[] CS2MAPS = { 2989, 2991, 2993, 2987 };

	public static void openResourceShop(final Player player, int complexity) {
		if (complexity <= 1) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", DungeonConstants.SMUGGLER,
					"Sorry, but I don't have anything to sell.");
			return;
		}

		player.getPackets().sendGlobalConfig(1320, complexity);
		player.getTemporaryAttributtes().put(Key.DUNG_COMPLEXITY, complexity);

		player.getInterfaceManager().sendInterface(RESOURCE_SHOP);
		player.getInterfaceManager().sendInventoryInterface(RESOURCE_SHOP_INV);

		player.getPackets().sendUnlockIComponentOptionSlots(RESOURCE_SHOP, 24, 0, 429, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendUnlockIComponentOptionSlots(RESOURCE_SHOP, 23, 0, 429, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendUnlockIComponentOptionSlots(RESOURCE_SHOP, 25, 0, 429, 0, 1, 2, 3, 4, 5);

		player.getPackets().sendUnlockIComponentOptionSlots(RESOURCE_SHOP_INV, 0, 0, 27, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendInterSetItemsOptionsScript(
				RESOURCE_SHOP_INV, 0, 93, 4, 7,
				"Value", "Sell 1", "Sell 5", "Sell 10", "Sell 50", "Examine");

		player.setCloseInterfacesEvent(() -> player.getTemporaryAttributtes().remove(Key.DUNG_COMPLEXITY));
	}


	public static void handlePurchaseOptions(Player player, int slotId, int quantity) {
		Integer complexity = (Integer) player.getTemporaryAttributtes().get(Key.DUNG_COMPLEXITY);
		if (complexity == null || complexity <= 1 || quantity == 0 || quantity < -1 || slotId < 2)
			return;
		int baseMap = CS2MAPS[complexity >= 5 ? 3 : complexity - 2];
		int slot = (slotId - 2) / 5;
		ClientScriptMap map = ClientScriptMap.getMap(baseMap);
		if (slot < 0 || map == null)
			return;
		if (slot >= map.getSize()) {
			slot -= map.getSize();
			map = ClientScriptMap.getMap(baseMap + 1);
			if (map == null || slot >= map.getSize())
				return;
		}
		int item = map.getIntValue(slot);
		if (item <= 0)
			return;
		ItemDefinitions def = ItemDefinitions.getItemDefinitions(item);
		int value = getBuyValue(def);
		if (quantity == -1) {
			player.getPackets().sendGameMessage(def.getName() + ": currently costs " + value + " rusty coins.");
			return;
		}
		int coinsCount = player.getInventory().getNumberOf(DungeonConstants.RUSTY_COINS);
		long price = (long) value * quantity;
		if (price > coinsCount) {
			quantity = coinsCount / value;
			price = (long) quantity * value;
			player.getPackets().sendGameMessage("You don't have enough rusty coins to buy that!");
		}
		int openSlots = player.getInventory().getFreeSlots();
		if (!def.isStackable())
			quantity = quantity > openSlots ? openSlots : quantity;
		price = (long) quantity * value;
		if (quantity == 0)
			return;
		if (player.getInventory().addItem(item, quantity))
			player.getInventory().deleteItem(new Item(DungeonConstants.RUSTY_COINS, (int) price));
	}

	public static void handleSellOptions(Player player, int slotId, int itemId, int quantity) {
		Item item = player.getInventory().getItem(slotId);
		if (item == null || itemId != item.getId() || quantity == 0 || quantity < -1)
			return;
		if (!ItemConstants.isTradeable(item) || item.getId() == DungeonConstants.RUSTY_COINS || item.getDefinitions().isRingOfKinship()) {
			player.getPackets().sendGameMessage("You can't sell this item.");
			return;
		}
		ItemDefinitions def = item.getDefinitions();

		int value = getSellValue(def);
		if (quantity == -1) {
			player.getPackets().sendGameMessage(def.getName() + ": shop will buy for " + value + " rusty coins. Right-click the item to sell.");
			return;
		}
		int itemCount = player.getInventory().getNumberOf(item.getId());
		if (quantity > itemCount)
			quantity = itemCount;
		long price = (long) value * quantity;
		if (price > Integer.MAX_VALUE) {
			quantity = Integer.MAX_VALUE / value;
			price = (long) value * quantity;
		}
		if (quantity == 0)
			return;
		player.getInventory().deleteItem(new Item(item.getId(), quantity));
		player.getInventory().addItem(DungeonConstants.RUSTY_COINS, (int) price);
	}

	private static int getBuyValue(ItemDefinitions def) {
		return Math.max(1, (int) (def.getValue() * def.getDungShopValueMultiplier()));
	}

	private static int getSellValue(ItemDefinitions def) {
		return Math.max(1, (int) ((def.getValue() * def.getDungShopValueMultiplier()) * 0.3D));
	}
}
