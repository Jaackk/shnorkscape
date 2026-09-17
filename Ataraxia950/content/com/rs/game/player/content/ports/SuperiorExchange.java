package com.rs.game.player.content.ports;

import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to handle exchanging regular port armors for superior ones.
 *
 * @author Noel
 */
public class SuperiorExchange {

	/**
	 * Used to handle exchanging items.
	 *
	 * @param player
	 *            The player exchanging.
	 * @param itemId
	 *            The itemId to exchange.
	 * @return if exchanged.
	 */
	public static boolean exchange(Player player, int itemId) {
		final PortArmor portItems = PortArmor.forId(itemId);
		int price = portItems.getPrice();
		if (player.getPorts().chime >= price) {
			player.getInventory().deleteItem(itemId, 1);
			player.getInventory().addItem(portItems.getId2(), 1);
			player.getPorts().chime -= price;
			return true;
		} else {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You dont have enough Chime to repair this; you need " + getPrice(itemId) + ".");
			return false;
		}
	}

	/**
	 * Checks superior item price to be exchange.
	 *
	 * @param player
	 *            The player checking.
	 * @param itemId
	 *            The itemId to check.
	 * @return The price as an int.
	 */
	public static int checkPrice(Player player, int itemId) {
		final PortArmor portItems = PortArmor.forId(itemId);
		int price = portItems.getPrice();
		return price;
	}

	/**
	 * Gets the price and formats it.
	 *
	 * @param itemId
	 *            The itemId to get the price.
	 * @return the formatted String.
	 */
	public static String getPrice(int itemId) {
		final PortArmor portItems = PortArmor.forId(itemId);
		int price = portItems.getPrice();
		return Utils.getFormattedNumber(price);
	}

	/**
	 * An enum containing all superior port item data.
	 *
	 * @author Noel
	 */
	public enum PortArmor {

		// 200 point increase on helm
		SUPERIOR_TETSU_HELM(26325, 26322, 1000), SUPERIOR_WORN_TETSU_HELM(26328, 26322, 1200),

		// 400 point increase on the body
		SUPERIOR_TETSU_BODY(26326, 26323, 2100), SUPERIOR_WORN_TETSU_BODY(26329, 26323, 2500),

		// 300 point increase on the legs
		SUPERIOR_TETSU_LEGS(26327, 26324, 1600), SUPERIOR_WORN_TETSU_LEGS(26330, 26324, 1900),

		SUPERIOR_SEASINGER_HOOD(26337, 26334, 1000), SUPERIOR_WORN_SEASINGER_HOOD(26340, 26334, 1200),

		SUPERIOR_SEASINGER_ROBE_TOP(26338, 26335, 2100), SUPERIOR_WORN_SEASINGER_ROBE_TOP(26341, 26335, 2500),

		SUPERIOR_SEASINGER_ROBE_BOTTOM(26339, 26336, 1600), SUPERIOR_WORN_SEASINGER_ROBE_BOTTOM(26342, 26336, 1900),

		SUPERIOR_DEATH_LOTUS_HOOD(26346, 26352, 1000), SUPERIOR_WORN_DEATH_LOTUS_HOOD(26349, 26352, 1200),

		SUPERIOR_DEATH_LOTUS_CHESTPLATE(26347, 26353, 2100), SUPERIOR_WORN_DEATH_LOTUS_CHESTPLATE(26350, 26353, 2500),

		SUPERIOR_DEATH_LOTUS_CHAPS(26348, 26354, 1600), SUPERIOR_WORN_DEATH_LOTUS_CHAPS(26351, 26354, 1900)

		/** Regular - Superior - Cost **/
		;

		private static final Map<Integer, PortArmor> PORTITEMS = new HashMap<Integer, PortArmor>();

		static {
			for (PortArmor brokenitems : PortArmor.values())
				PORTITEMS.put(brokenitems.getId(), brokenitems);
		}

		private final int id;
		private final int id2;
		private final int Price;

		PortArmor(int id, int id2, int Price) {
			this.id = id;
			this.id2 = id2;
			this.Price = Price;
		}

		public static PortArmor forId(int id) {
			return PORTITEMS.get(id);
		}

		public int getId() {
			return id;
		}

		public int getId2() {
			return id2;
		}

		public int getPrice() {
			return Price;
		}

	}
}