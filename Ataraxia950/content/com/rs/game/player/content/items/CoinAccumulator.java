package com.rs.game.player.content.items;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;

/**
 * Handles the Coin Accumulator item.
 *
 * @author Noel
 */
public class CoinAccumulator {

	/**
	 * Handles the item.
	 *
	 * @param player
	 *            The player.
	 * @param dropId
	 *            The coin ID.
	 * @return true if coin drop.
	 */
	public static boolean handleCoinAccumulator(Player player, NPC npc, int quantity) {
		if (player.getPerkManager().hasPerkActive(DonationPerk.TREASURE_GOBLIN)) {
			dropCoins(player, npc, (int) (quantity * 1.25), true);
			return true;
		}
		return false;
	}

	/**
	 * Adds a coin drop.
	 *
	 * @param player
	 *            The player.
	 * @param npc
	 *            The NPC.
	 * @param quantity
	 *            Coin quantity.
	 * @param inventory
	 *            if add to Inventory.
	 */
	private static void dropCoins(Player player, NPC npc, int quantity, boolean inventory) {
		Item item = new Item(995, quantity);
		if (player.getMoneyPouchValue() + quantity < 0 || player.getMoneyPouchValue() + quantity == Integer.MAX_VALUE)
			return;
		if (!inventory) {
			World.updateGroundItem(item, new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), player, 60, 0, false);
		} else
			player.addMoney(quantity);
	}
}