package com.rs.game.player.content.items;

import com.rs.game.player.Equipment;
import com.rs.game.player.Player;

/**
 * Handles checking for Ring of Wealth.
 *
 * @author Noel
 */
public class RingOfWealth {

	private static final int[] RING_OF_WEALTH = { 2572, 20653, 20655, 20657, 20659, 773, 41069, 48483 };

	public static boolean checkRow(Player player) {
		if (player.getEquipment().getItem(Equipment.SLOT_RING) != null) {
			for (int ring : RING_OF_WEALTH) {
				if (player.getEquipment().getRingId() == ring)
					return true;
			}
		}
		return false;
	}

}