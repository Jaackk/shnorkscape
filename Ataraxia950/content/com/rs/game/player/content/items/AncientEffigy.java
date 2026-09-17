package com.rs.game.player.content.items;

import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Inventory;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;

/**
 * Handles ancient effigies non-dialogue related stuff.
 *
 * @author Raghav/Own4g3 <Raghav_ftw@hotmail.com>
 */
public class AncientEffigy {

	/**
	 * Ancient effigies' item ids.
	 */
	public static final int STARVED_ANCIENT_EFFIGY = 18778, NOURISHED_ANCIENT_EFFIGY = 18779,
			SATED_ANCIENT_EFFIGY = 18780, GORGED_ANCIENT_EFFIGY = 18781, DRAGONKIN_LAMP = 18782;
	public static final int[] effigies = { STARVED_ANCIENT_EFFIGY, NOURISHED_ANCIENT_EFFIGY, SATED_ANCIENT_EFFIGY,
			GORGED_ANCIENT_EFFIGY, DRAGONKIN_LAMP };
	/**
	 * First skill to be nourished.
	 */
	public static int[] SKILL_1 = { Skills.AGILITY, Skills.CONSTRUCTION, Skills.COOKING, Skills.FISHING,
			Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.SUMMONING };
	/**
	 * Second skill to be nourished.
	 */
	public static int[] SKILL_2 = { Skills.CRAFTING, Skills.THIEVING, Skills.FIREMAKING, Skills.FARMING,
			Skills.WOODCUTTING, Skills.HUNTER, Skills.SMITHING, Skills.RUNECRAFTING };

	/**
	 * Getting the required level for each effigy.
	 *
	 * @param id
	 *            The effigy's item id.
	 * @return Required level.
	 */
	public static int getRequiredLevel(int id) {
		switch (id) {
		case STARVED_ANCIENT_EFFIGY:
			return 91;
		case NOURISHED_ANCIENT_EFFIGY:
			return 93;
		case SATED_ANCIENT_EFFIGY:
			return 95;
		case GORGED_ANCIENT_EFFIGY:
			return 97;
		}
		return -1;
	}

	/**
	 * Getting the message.
	 *
	 * @param skill
	 *            The skill
	 * @return message
	 */
	public static String getMessage(int skill) {
		switch (skill) {
		case Skills.AGILITY:
			return "deftness and precision";
		case Skills.CONSTRUCTION:
			return "buildings and security";
		case Skills.COOKING:
			return "fire and preparation";
		case Skills.FISHING:
			return "life and cultivation";
		case Skills.FLETCHING:
			return "lumber and woodworking";
		case Skills.HERBLORE:
			return "flora and fuana";
		case Skills.MINING:
			return "metalwork and minerals";
		case Skills.SUMMONING:
			return "binding essence and spirits";
		}
		return null;
	}

	/**
	 * Getting the experience amount.
	 *
	 * @param itemId
	 *            The effigy's item id.
	 * @return The amount of experience.
	 */
	public static int getExp(int itemId) {
		switch (itemId) {
		case STARVED_ANCIENT_EFFIGY:
			return 1000;
		case NOURISHED_ANCIENT_EFFIGY:
			return 1400;
		case SATED_ANCIENT_EFFIGY:
			return 1650;
		case GORGED_ANCIENT_EFFIGY:
			return 2000;
		}
		return -1;
	}

	/**
	 * Investigation of an effigy.
	 *
	 * @param player
	 *            The player who is doing investigation.
	 * @param id
	 *            The effigy item id.
	 */
	public static void effigyInvestigation(Player player, int id) {
		Inventory inv = player.getInventory();
		inv.deleteItem(id, 1);
		if (id == STARVED_ANCIENT_EFFIGY)
			inv.addItem(NOURISHED_ANCIENT_EFFIGY, 1);
		else if (id == NOURISHED_ANCIENT_EFFIGY)
			inv.addItem(SATED_ANCIENT_EFFIGY, 1);
		else if (id == SATED_ANCIENT_EFFIGY)
			inv.addItem(GORGED_ANCIENT_EFFIGY, 1);
		else if (id == GORGED_ANCIENT_EFFIGY)
			inv.addItem(DRAGONKIN_LAMP, 1);
	}

	public static boolean canSack(Player player) {
		int total = getEffigies(player);
        return total == 25;
    }

	public static int getEffigies(Player player) {
		int total = 0;
		for (int i = 0; i < effigies.length; i++) {
			if (player.getInventory().containsItem(new Item(effigies[i])))
				total += player.getInventory().getAmountOf(effigies[i]);
		}
		return total;
	}

	/*
	 * Handles the trading of effigies for a sack
	 */
	public static void effigySack(Player player) {
		if (canSack(player)) {
			for (int i = 0; i < effigies.length; i++)
				player.getInventory().deleteItem(effigies[i], player.getInventory().getAmountOf(effigies[i]));
			player.getInventory().addItem(new Item(33518));
			World.sendWorldMessage("<img=6>" + Colors.CYAN + "<shad=000000>News: " + player.getDisplayName()
					+ " has received a sack of effigies!", false);
		} else {
			String word = (getEffigies(player) > 25) ? "have too many effigies, you only need 25!"
					: "need at least 25 effigies!";
			player.sendMessage(Colors.RED + "You " + word);
		}
	}
}
