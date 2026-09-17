package com.rs.game.player.content;

import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;


public final class SkillsDialogue {

	public static final int MAKE = 0, MAKE_SETS = 1, COOK = 2, ROAST = 3, OFFER = 4, SELL = 5, BAKE = 6, CUT = 7, DEPOSIT = 8, MAKE_NO_ALL_NO_CUSTOM = 9, TELEPORT = 10, SELECT = 11, TAKE = 13;

	public interface ItemNameFilter {

		String rename(String name);
	}

	public static void sendSkillsDialogue(Player player, int option, String explanation, int maxQuantity, int[] items, ItemNameFilter filter) {
		sendSkillsDialogue(player, option, explanation, maxQuantity, items, filter, true);
	}

	public static void sendSkillsDialogue(Player player, int option, String explanation, int maxQuantity, int[] items, ItemNameFilter filter, boolean sendQuantitySelector) {
		setMaxQuantity(player, maxQuantity);
		setQuantity(player, maxQuantity);

		player.getInterfaceManager().sendChatBoxInterface(905);
		player.getInterfaceManager().setInterface(true, 905, 4, 916);
		player.getInterfaceManager().refreshInterface(true);
		if (!sendQuantitySelector) {
			maxQuantity = -1;
			player.getPackets().sendHideIComponent(916, 9, true);
			player.getPackets().sendHideIComponent(916, 14, true);
		} else {
			if (option != MAKE_SETS && option != MAKE_NO_ALL_NO_CUSTOM) {
				player.getPackets().sendIComponentSettings(916, 13, -1, 0, 0x2); // unlocks all
				player.getPackets().sendIComponentSettings(905, 6, -1, -1, 0x1); // unlocks custom
			}
		}
		player.getPackets().sendIComponentText(916, 6, explanation);
		player.getPackets().sendGlobalConfig(7500, option);
		for (int i = 0; i < 14; i++) {
			if (i >= items.length) {
				player.getPackets().sendGlobalConfig(7516 + i, -1);
				player.getPackets().sendGlobalString(7502 + i, "");
				continue;
			}
			player.getPackets().sendGlobalConfig(7516 + i, items[i]);
			String name = ItemDefinitions.getItemDefinitions(items[i]).getName();
			if (filter != null)
				name = filter.rename(name);
			player.getPackets().sendGlobalString(7502 + i, name);
		}
		player.setCloseInterfacesEvent(new Runnable() {

			@Override
			public void run() {
				player.getPackets().sendGlobalConfig(7500, option);
				for (int i = 0; i < 14; i++) {
					player.getPackets().sendGlobalConfig(7516 + i, -1);
					player.getPackets().sendGlobalString(7502 + i, "");
				}
			}

		});
	}

	public static void handleSetQuantityButtons(Player player, int componentId) {
		if (componentId == 10)
			setQuantity(player, 1, false);
		else if (componentId == 11)
			setQuantity(player, 5, false);
		else if (componentId == 12)
			setQuantity(player, 10, false);
		else if (componentId == 13)
			setQuantity(player, getMaxQuantity(player), false);
		else if (componentId == 24)
			setQuantity(player, getQuantity(player) + 1, false);
		else if (componentId == 25)
			setQuantity(player, getQuantity(player) - 1, false);
	}

	public static void setMaxQuantity(Player player, int maxQuantity) {
		player.getTemporaryAttributtes().put("SkillsDialogueMaxQuantity", maxQuantity);
		player.getVarBitManager().forceSendVarBit(50013, maxQuantity);
	}

	public static void setQuantity(Player player, int quantity) {
		setQuantity(player, quantity, true);
	}

	public static void setQuantity(Player player, int quantity, boolean refresh) {
		int maxQuantity = getMaxQuantity(player);
		if (quantity > maxQuantity)
			quantity = maxQuantity;
		else if (quantity < 0)
			quantity = 0;
		player.getTemporaryAttributtes().put("SkillsDialogueQuantity", quantity);
		if (refresh)
			player.getVarBitManager().forceSendVarBit(50012, quantity);
	}

	public static int getMaxQuantity(Player player) {
		Integer maxQuantity = (Integer) player.getTemporaryAttributtes().get("SkillsDialogueMaxQuantity");
		if (maxQuantity == null)
			return 0;
		return maxQuantity;
	}

	public static int getQuantity(Player player) {
		Integer quantity = (Integer) player.getTemporaryAttributtes().get("SkillsDialogueQuantity");
		if (quantity == null)
			return 0;
		return quantity;
	}

	public static int getItemSlot(int componentId) {
		if (componentId < 14)
			return 0;
		return componentId - 14;
	}

	private SkillsDialogue() {

	}

	public static final int STRUCT_ITEM_IDS_START = 2655, STRUCT_ITEM_AMOUNTS_START = 2665;

	public static Item[][] getRequiredItemsForItem(int itemId) {
		List<Item[]> requiredItems = new ArrayList<Item[]>();
		ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
		for (int i = 0; i < 10; i++) {
			int checkItemId = defs.getCSOpcode(STRUCT_ITEM_IDS_START + i);
			int checkAmount = defs.getCSOpcode(STRUCT_ITEM_AMOUNTS_START + i);
			if (checkItemId == 0 || checkAmount == 0)
				continue;
			requiredItems.add(new Item[] { new Item(checkItemId, checkAmount) });
		}
		return requiredItems.toArray(new Item[requiredItems.size()][]);
	}

	public static Item[] getActualRequiredItems(int itemId) {
		return getActualRequiredItems(itemId, null);
	}

	public static Item[] getActualRequiredItems(int itemId, Item[][] requiredItems) {
		if (requiredItems == null)
			requiredItems = getRequiredItemsForItem(itemId);
		Item[] actualRequiredItems = new Item[requiredItems.length];
		for (int i = 0; i < requiredItems.length; i++) {
			for (int j = 0; j < requiredItems[i].length; j++) {
				Item item = requiredItems[i][j];
				if (item == null)
					continue;
				actualRequiredItems[i] = requiredItems[i][j];
				break;
			}
		}
		return actualRequiredItems;
	}
}
