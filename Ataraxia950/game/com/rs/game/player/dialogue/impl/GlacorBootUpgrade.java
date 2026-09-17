package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * Handles Glacor boot upgrading.
 *
 * @author Noel
 */
public class GlacorBootUpgrade extends Dialogue {

	/**
	 * Represents the 2 items being used one on each other.
	 */
	Item item, upgrade;

	@Override
	public void start() {
		item = (Item) parameters[0];
		upgrade = (Item) parameters[1];
		if (handleUpgrade(item, upgrade, false))
			sendItemDialogue(item.getId(), item.getAmount(),
					"Upgrading your " + item.getName() + " will " + "make them permanently untradeable.");
		else {
			player.sendMessage("You cannot use the " + upgrade.getName() + " on your " + item.getName() + ".", true);
			end();
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue(Colors.RED + "Do you want to upgrade your " + item.getName() + "?", "Yes.", "No.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				if (handleUpgrade(item, upgrade, true))
					sendDialogue(Colors.RED + "You've successfully upgraded your " + item.getName() + "!");
				else
					sendDialogue(Colors.RED + "You cannot upgrade this item at the moment.");
				stage = 1;
				break;
			default:
				end();
				break;
			}
			break;
		case 1:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

	/**
	 * Handles the actual item Dyeing.
	 *
	 * @param item
	 *            The item to dye.
	 * @param dye
	 *            The dye to use.
	 * @return if Item is Dye'able.
	 */
	private boolean handleUpgrade(Item item, Item upgrade, boolean remove) {
		if (item == null || upgrade == null)
			return false;
		if (!player.getInventory().containsItem(item))
			return false;
		if (!player.getInventory().containsItem(upgrade.getId(), 2)) {
			player.sendMessage("You need 2 of " + upgrade.getName() + "s to upgrade your boots.");
			return false;
		}
		// Flarefrost boots
		if (upgrade.getId() == 34974 && item.getId() == 21790) {
			if (!remove)
				return true;
			player.getInventory().deleteItem(item);
			player.getInventory().deleteItem(upgrade.getId(), 2);
			player.getInventory().addItem(new Item(34981));
			return true;
		}
		// Hailfire boots
		if (upgrade.getId() == 34976 && item.getId() == 21793) {
			if (!remove)
				return true;
			player.getInventory().deleteItem(item);
			player.getInventory().deleteItem(upgrade.getId(), 2);
			player.getInventory().addItem(new Item(34984));
			return true;
		}
		// Emberkeen boots
		if (upgrade.getId() == 34972 && item.getId() == 21787) {
			if (!remove)
				return true;
			player.getInventory().deleteItem(item);
			player.getInventory().deleteItem(upgrade.getId(), 2);
			player.getInventory().addItem(new Item(34978));
			return true;
		}
		return false;
	}
}