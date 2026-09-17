package com.rs.utils;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;

/**
 * Handles the setting & getting of Item values.
 *
 * @author Noel
 */
public final class EconomyPrices {

	public enum AlchTier {
		TABLE(0.80),
		LOW(0.70),
		HIGH(0.80);

		private final double payout;

		AlchTier(double payout) {
			this.payout = payout;
		}

		public double payout() {
			return payout;
		}
	}


	/**
	 * Gets the item value.
	 *
	 * @param itemId
	 *            The item ID.
	 * @return the Value.
	 */
	public static int getPrice(int itemId) {
		if (itemId == 995)
			return 1;
		ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
		if (defs == null)
			return 1;
		if (defs.isNoted() && defs.getCertId() > 0 && defs.getCertId() != itemId)
			return getPrice(defs.getCertId());
		if (defs.isLended() && defs.getLendId() > 0 && defs.getLendId() != itemId)
			return getPrice(defs.getLendId());
		return Math.max(1, defs.getValue());
	}




	public static int getBaseAlchValue(Item item) {
		return getPrice(item.getId());
	}


	public static int getAlchCoins(Item item, AlchTier tier) {
		int base = getBaseAlchValue(item);
		long coins = Math.round(base * tier.payout());
		return coins > Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, (int) coins);
	}






}
