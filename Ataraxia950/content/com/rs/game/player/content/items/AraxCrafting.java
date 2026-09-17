package com.rs.game.player.content.items;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/**
 * Handles Noxious weapon assembly.
 *
 * @author Noel
 */
public class AraxCrafting {

	/**
	 * Handles creating the Spider leg.
	 *
	 * @param player
	 */
	public static void handleSpiderLeg(Player player) {
		if (!player.getInventory().containsItem(31718, 1) || !player.getInventory().containsItem(31719, 1)
				|| !player.getInventory().containsItem(31720, 1)) {
			player.sendMessage("You'll need all 3 Spider leg pieces in order to do this.");
			return;
		}
		if (player.getSkills().getLevelForXp(Skills.CRAFTING) < 30) {
			player.sendMessage("You'll need a Crafting level of at least 30 in order to do this.");
			return;
		}
		player.getInventory().deleteItem(31718, 1);
		player.getInventory().deleteItem(31719, 1);
		player.getInventory().deleteItem(31720, 1);
		player.getInventory().addItem(31721, 1);
		player.sendMessage("You've successfully made a full, gross.. spider leg!");
	}

	/**
	 * Checks if we can craft a Noxious weapon.
	 *
	 * @param player
	 * @return true if yes
	 */
	public static boolean canCraftWeapon(Player player, int bodyID) {
		if (!player.getInventory().containsOneItem(31721)) {
			player.sendMessage("You do not have a Spider leg to work with.");
			return false;
		}
		if (!player.getInventory().containsOneItem(bodyID)) {
			player.sendMessage("You do not have an Araxxi's body piece to work with.");
			return false;
		}
		if (player.getSkills().getLevel(Skills.CRAFTING) < 90) {
			player.sendMessage("You'll need a Crafting level of at least 90 in order to do this.");
			return false;
		}
		return true;
	}
}
