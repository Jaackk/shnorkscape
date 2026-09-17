package com.rs.game.player.content.items;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/**
 * Handles everything related to bug lanterns.
 *
 * @author Noel
 */
public class BugLantern {

	/**
	 * Lights the bug lantern.
	 *
	 * @param player
	 *            The player lighting.
	 * @item the bug Lantern item.
	 */
	public static boolean handleLatern(Player player, Item item, int slotId) {
		if (item.getDefinitions().containsOption("Light")) {
			if (item.getId() != 7051)
				return false;
			if (player.getSkills().getLevel(Skills.FIREMAKING) < 33) {
				player.sendMessage("You need a Firemaking level of 33 to lit the " + item.getName() + ".");
				return false;
			}
			if (!player.getInventory().containsOneItem(590)) {
				player.sendMessage("You will need a Tinderbox in order to lit the " + item.getName() + ".");
				return false;
			}
			player.getInventory().replaceItem(7053, 1, slotId);
			player.sendMessage("You light the Unlit bug lantern.", true);
			return true;
		}
		if (item.getDefinitions().containsOption("Extinguish")) {
			player.getInventory().replaceItem(7051, 1, slotId);
			player.sendMessage("You extinguish the fire in the Lit bug lantern.", true);
			return true;
		}
		return false;
	}
}
