package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * A class used to handle most Polypore dungeon content.
 *
 * @author Noel
 */
public class PolyporeDungeon {

	/**
	 * Represents the needle item.
	 */
	private static final Item NEEDLE = new Item(1733);

	/**
	 * Represents the thread item.
	 */
	private static final Item THREAD = new Item(1734);

	/**
	 * Used to handle item making.
	 *
	 * @param player
	 *            The player.
	 * @param item1
	 *            The item used.
	 * @param item2
	 *            The item used with.
	 * @return true if successfull.
	 */
	public static boolean handleItemOnItem(Player player, Item item1, Item item2) {
		if (item2.getName().toLowerCase().contains("flake")) {
			Item newItem1 = item1; // placeholder
			item1 = item2;
			item2 = newItem1;
		}
		int id1 = item1.getId(); // flakes
		int id2 = item2.getId(); // web armour
		// fungal visor
		if (id1 == 22452 && id2 == 22449 || id1 == 22449 && id2 == 22452) {
			if (player.getInventory().containsItem(22449, 300)) {
				handleCraft(player, new Item(item1.getId(), 300), item2, new Item(22458), 3, 15);
			} else
				player.sendMessage("You'll need at least 300 fungal flakes to do this.");
			return true;
		} // fungal poncho
		if (id1 == 22456 && id2 == 22449 || id1 == 22449 && id2 == 22456) {
			if (player.getInventory().containsItem(22449, 2500)) {
				handleCraft(player, new Item(item1.getId(), 2500), item2, new Item(22466), 21, 125);
			} else
				player.sendMessage("You'll need at least 2,500 fungal flakes to do this.");
			return true;
		} // fungal leggings
		if (id1 == 22454 && id2 == 22449 || id1 == 22449 && id2 == 22454) {
			if (player.getInventory().containsItem(22449, 1000)) {
				handleCraft(player, new Item(item1.getId(), 1000), item2, new Item(22462), 12, 50);
			} else
				player.sendMessage("You'll need at least 1,000 fungal flakes to do this.");
			return true;
		} // grifolic visor
		if (id1 == 22452 && id2 == 22450 || id1 == 22450 && id2 == 22452) {
			if (player.getInventory().containsItem(22450, 400)) {
				handleCraft(player, new Item(item1.getId(), 400), item2, new Item(22470), 65, 40);
			} else
				player.sendMessage("You'll need at least 400 grifolic flakes to do this!");
			return true;
		} // grifolic poncho
		if (id1 == 22456 && id2 == 22450 || id1 == 22450 && id2 == 22456) {
			if (player.getInventory().containsItem(22450, 3500)) {
				handleCraft(player, new Item(item1.getId(), 3500), item2, new Item(22478), 78, 350);
			} else
				player.sendMessage("You'll need at least 3,500 grifolic flakes to do this!");
			return true;
		} // grifolic leggings
		if (id1 == 22454 && id2 == 22450 || id1 == 22450 && id2 == 22454) {
			if (player.getInventory().containsItem(22450, 1200)) {
				handleCraft(player, new Item(item1.getId(), 1200), item2, new Item(22474), 72, 120);
			} else
				player.sendMessage("You'll need at least 1,200 grifolic flakes to do this!");
			return true;
		} // ganodermic visor
		if (id1 == 22452 && id2 == 22451 || id1 == 22451 && id2 == 22452) {
			if (player.getInventory().containsItem(22451, 500))
				handleCraft(player, new Item(item1.getId(), 500), item2, new Item(22482), 86, 100);
			else
				player.sendMessage("You'll need at least 500 ganodermic flakes to do this.");
			return true;
		} // ganodermic poncho
		if (id1 == 22456 && id2 == 22451 || id1 == 22451 && id2 == 22456) {
			if (player.getInventory().containsItem(22451, 5000)) {
				handleCraft(player, new Item(item1.getId(), 5000), item2, new Item(22490), 98, 1000);
			} else
				player.sendMessage("You'll need at least 5,000 ganodermic flakes to do this.");
			return true;
		} // ganodermic leggings
		if (id1 == 22454 && id2 == 22451 || id1 == 22451 && id2 == 22454) {
			if (player.getInventory().containsItem(22451, 1500)) {
				handleCraft(player, new Item(item1.getId(), 1500), item2, new Item(22486), 92, 300);
			} else
				player.sendMessage("You'll need at least 1,500 ganodermic flakes to do this.");
			return true;
		} // polypore staff
		if (id1 == 22448 && id2 == 22498 || id1 == 22498 && id2 == 22448) {
			if (player.getSkills().getLevel(Skills.FARMING) < 80) {
				player.sendMessage("You need a farming level of 80 to make the Polypore staff.");
				return false;
			}
			if (!player.getInventory().containsItem(22448, 3000)) {
				player.sendMessage("You'll need at least 3,000 polypore spores to make the Polypore staff.");
				return false;
			}
			if (!player.getInventory().containsItem(554, 15000)) {
				player.sendMessage("You'll need at least 15,000 fire runes to make the Polypore staff.");
				return false;
			}
			player.setNextAnimation(new Animation(15434));
			player.setNextGraphics(new Graphics(2032));
			player.getInventory().deleteItem(554, 15000);
			player.getInventory().deleteItem(22448, 3000);
			player.getInventory().deleteItem(22498, 1);
			player.getInventory().addItem(22494, 1);
			player.getSkills().addXp(Skills.FARMING, 300);
			player.sendMessage("You've successfully made a Polypore staff.");
			return true;
		}
		return false;
	}

	/**
	 * Crafts the item.
	 *
	 * @param player
	 *            The player.
	 * @param item
	 *            The produce.
	 * @param lvl
	 *            The level needed.
	 * @return true if crafted.
	 */
	private static boolean handleCraft(Player player, Item item1, Item item2, Item product, int lvl, int xp) {
		if (player.getSkills().getLevel(Skills.CRAFTING) < lvl) {
			player.sendMessage("You need a crafting level of " + lvl + " to make a " + product.getName() + ".");
			return false;
		}
		if (!player.getInventory().containsItem(NEEDLE) && !player.getToolBelt().contains(NEEDLE.getId())) {
			player.sendMessage("You'll need a needle to make a " + product.getName() + ".");
			return false;
		}
		if (!player.getInventory().containsItem(THREAD)) {
			player.sendMessage("You'll need at least 1 reel of thread to make a " + product.getName() + ".");
			return false;
		}
		player.addItemsMade();
		player.getInventory().deleteItem(item1.getId(), item1.getAmount());
		player.getInventory().deleteItem(item2);
		player.getInventory().deleteItem(THREAD);
		player.getInventory().addItem(product);
		player.setNextAnimation(new Animation(1249));
		player.getSkills().addXp(Skills.CRAFTING, xp);
		player.sendMessage("You've successfully made a " + product.getName() + "; " + "items crafted: " + Colors.RED
				+ Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
		return true;
	}
}