package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles Crystal Glassblowing into Crystal flask.
 *
 * @author Noel
 */
public class CrystalGlassBlowing extends Action {

	/**
	 * The glassblowing animation.
	 */
	public static final Animation BLOWING_ANIMATION = new Animation(24890);
	
	private final boolean portable;
	
	/**
	 * Constructs a new class.
	 */
	public CrystalGlassBlowing(boolean portable) {
		this.portable = portable;
	}

	/**
	 * Handles the actual item making.
	 *
	 * @param player The player making the item.
	 */
	private void blow(Player player) {
		player.setNextAnimation(BLOWING_ANIMATION);
		if (player.getInventory().containsItem(new Item(32845, 1))) {
			player.getSkills().addXp(Skills.CRAFTING, 150 * (portable ? 1.1 : 1));
			player.getInventory().deleteItem(new Item(32845));
			player.getInventory().addItem(new Item(32843));
		} else {
			player.getSkills().addXp(Skills.CRAFTING, 100 * (portable ? 1.1 : 1));
			player.getInventory().deleteItem(new Item(23193));
			player.getInventory().addItem(new Item(23191));
		}
		player.addItemsMade();
		player.sendMessage("You form a figure out of the glass; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
	}

	@Override
	public boolean process(Player player) {
		if (!player.getInventory().containsItem(new Item(32845, 1)) && !player.getInventory().containsItem(new Item(23193, 1))) {
			player.sendMessage("You have no more glass to work with.");
			return false;
		}
		if (player.clickedObject != null) {
            return World.containsObjectWithId(player.clickedObject, player.clickedObject.getId());
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		blow(player);
		return 2;
	}

	@Override
	public boolean start(Player player) {
		if (player.getSkills().getLevel(Skills.CRAFTING) < 89) {
			player.sendMessage("You need a Crafting level of 89 to make this.");
			return false;
		}
		if (!player.getInventory().containsItem(new Item(32845, 1)) && !player.getInventory().containsItem(new Item(23193, 1))) {
			player.sendMessage("You have no glass to work with.");
			return false;
		}
		if (!player.getInventory().containsOneItem(1785)) {
			player.sendMessage("You do not have a glassblowing pipe to work the glass with.");
			return false;
		}
		return true;
	}

	@Override
	public void stop(Player player) {
		this.setActionDelay(player, 2);
		player.clickedObject = null;
	}
}