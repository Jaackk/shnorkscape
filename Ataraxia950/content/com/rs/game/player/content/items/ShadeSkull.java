package com.rs.game.player.content.items;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/**
 * Handles the Shade Skull item.
 *
 * @author Noel
 */
public class ShadeSkull {

	/**
	 * Attaches the skull to the staff.
	 *
	 * @param player
	 *            The player attaching.
	 * @param item1
	 *            Item used.
	 * @param item2
	 *            Item used with.
	 * @return if can attach.
	 */
	public static boolean attachSkull(Player player, Item item1, Item item2) {
		if (item1.getId() == 21488 || item2.getId() == 21488) {
			if (item1.getId() == 1381
					|| item2.getId() == 1381) { /** Staff of Air **/
				handleAttaching(player, item1, item2, new Item(21490));
				return true;
			}
			if (item1.getId() == 1383
					|| item2.getId() == 1383) { /** Staff of Water **/
				handleAttaching(player, item1, item2, new Item(21491));
				return true;
			}
			if (item1.getId() == 1385
					|| item2.getId() == 1385) { /** Staff of Earth **/
				handleAttaching(player, item1, item2, new Item(21492));
				return true;
			}
			if (item1.getId() == 1387
					|| item2.getId() == 1387) { /** Staff of Fire **/
				handleAttaching(player, item1, item2, new Item(21493));
				return true;
			}
			if (item1.getId() == 1397
					|| item2.getId() == 1397) { /** Air Battlestaff **/
				handleAttaching(player, item1, item2, new Item(21496));
				return true;
			}
			if (item1.getId() == 1395
					|| item2.getId() == 1395) { /** Water Battlestaff **/
				handleAttaching(player, item1, item2, new Item(21495));
				return true;
			}
			if (item1.getId() == 1399
					|| item2.getId() == 1399) { /** Earth Battlestaff **/
				handleAttaching(player, item1, item2, new Item(21497));
				return true;
			}
			if (item1.getId() == 1393
					|| item2.getId() == 1393) { /** Fire Battlestaff **/
				handleAttaching(player, item1, item2, new Item(21494));
				return true;
			}
			if (item1.getId() == 1405
					|| item2.getId() == 1405) { /** Air mystic staff **/
				handleAttaching(player, item1, item2, new Item(21500));
				return true;
			}
			if (item1.getId() == 1403
					|| item2.getId() == 1403) { /** Water mystic staff **/
				handleAttaching(player, item1, item2, new Item(21499));
				return true;
			}
			if (item1.getId() == 1407
					|| item2.getId() == 1407) { /** Earth mystic staff **/
				handleAttaching(player, item1, item2, new Item(21501));
				return true;
			}
			if (item1.getId() == 1401
					|| item2.getId() == 1401) { /** Fire mystic staff **/
				handleAttaching(player, item1, item2, new Item(21498));
				return true;
			}
			if (item1.getId() == 3053
					|| item2.getId() == 3053) { /** Lava Battlestaff **/
				handleAttaching(player, item1, item2, new Item(21502));
				return true;
			}
			if (item1.getId() == 3054
					|| item2.getId() == 3054) { /** Mystic lava staff **/
				handleAttaching(player, item1, item2, new Item(21503));
				return true;
			}
			if (item1.getId() == 6562
					|| item2.getId() == 6562) { /** Mud battlestaff **/
				handleAttaching(player, item1, item2, new Item(21504));
				return true;
			}
			if (item1.getId() == 6563
					|| item2.getId() == 6563) { /** Mystic mud staff **/
				handleAttaching(player, item1, item2, new Item(21505));
				return true;
			}
			if (item1.getId() == 11738
					|| item2.getId() == 11738) { /** Mystic steam staff **/
				handleAttaching(player, item1, item2, new Item(21507));
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * Handles the actual attaching insides.
	 *
	 * @param player
	 *            The player.
	 * @param item1
	 *            The item used.
	 * @param item2
	 *            The item used with.
	 * @param product
	 *            The product Item.
	 */
	private static void handleAttaching(Player player, Item item1, Item item2, Item product) {
		if (player.getSkills().getLevel(Skills.CRAFTING) < 85) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"Attaching the Shade Skull requires a level of 85 Crafting.");
			return;
		}
		player.getInventory().deleteItem(item1);
		player.getInventory().deleteItem(item2);
		player.getInventory().addItem(product);
	}

	/**
	 * Handles the detaching of shade skulls.
	 *
	 * @param player
	 *            The player.
	 * @param item
	 *            The item to detach the skull from.
	 * @return if can detach.
	 */
	public static boolean detachSkull(Player player, Item item) {
		if (!player.getInventory().hasFreeSlots()) {
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return false;
		}
		if (item.getId() == 21498) { /** Necromancer's fire staff **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1401, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21499) { /** Necromancer's water staff **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1403, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21500) { /** Necromancer's air staff **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1405, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21501) { /** Necromancer's earth staff **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1407, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21503) { /** Necromancer's lava staff **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(3054, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21505) { /** Necromancer's mud staff **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(6563, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21507) { /** Necromancer's steam staff **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(11738, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21494) { /** Skeletal battlestaff of fire **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1393, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21495) { /** Skeletal battlestaff of water **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1395, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21496) { /** Skeletal battlestaff of air **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1397, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21497) { /** Skeletal battlestaff of earth **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1399, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21490) { /** Skeletal staff of air **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1381, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21491) { /** Skeletal staff of water **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1383, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21492) { /** Skeletal staff of earth **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1385, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		if (item.getId() == 21493) { /** Skeletal staff of fire **/
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(1387, 1);
			player.getInventory().addItem(21488, 1);
			return true;
		}
		return false;
	}
}