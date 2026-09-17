package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Utils;

/**
 * Used to handle playing Harmonious harps at Prifddinas for Crafting EXP.
 *
 * @author Noel
 */
public class PrifddinasHarp extends Action {

	/**
	 * Declaring the Harp object (Only for facing measures).
	 */
	private final WorldObject harp;
	/**
	 * The initial ticks as Integer.
	 */
	private int ticks = 124;

	/**
	 * Initing the actual Action action.
	 *
	 * @param object
	 *            The Harp object we're initing.
	 */
	public PrifddinasHarp(WorldObject object) {
		this.harp = object;
	}

	/**
	 * Checks if we can continue interacting.
	 *
	 * @param player
	 *            The player interacting.
	 * @return if we can continue.
	 */
	private boolean checkAll(Player player) {
		if (player.getSkills().getLevel(Skills.CRAFTING) < 75) {
			player.sendMessage("You need a Crafting level of at least 75 to do this activity.");
			return false;
		}
		return true;
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		if (!player.getInventory().addItem(new Item(32622, 1))) {
			player.sendMessage("Not enough inventory space for Harmonic dust.", true);
			return false;
		}
		if (harp.getX() == 2129 && harp.getY() == 3335) {
			if (player.getX() != 2128 && player.getY() != 3335)
				player.setNextWorldTile(new WorldTile(2128, 3335, 1));
			player.faceObject(harp);
		} else if (harp.getX() == 2132 && harp.getY() == 3338) {
			if (player.getX() != 2132 && player.getY() != 3339)
				player.setNextWorldTile(new WorldTile(2132, 3339, 1));
			player.faceObject(harp);
		} else if (harp.getX() == 2135 && harp.getY() == 3335) {
			if (player.getX() != 2136 && player.getY() != 3335)
				player.setNextWorldTile(new WorldTile(2136, 3335, 1));
			player.faceObject(harp);
		} else if (harp.getX() == 2132 && harp.getY() == 3332) {
			if (player.getX() != 2132 && player.getY() != 3331)
				player.setNextWorldTile(new WorldTile(2132, 3331, 1));
			player.faceObject(harp);
		}
		player.setNextAnimation(new Animation(25022));
		this.setActionDelay(player, 3);
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (!checkAll(player))
			return false;
		player.setNextAnimation(new Animation(25021));
		player.faceObject(harp);
		World.sendObjectAnimation(harp, new Animation(25034));
		return true;
	}

	@Override
	public void stop(Player player) {
		player.setNextAnimation(new Animation(25023));
		World.sendObjectAnimation(harp, new Animation(-1));
		this.setActionDelay(player, 5);
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		if (Utils.random(100) >= 10)
			player.getSkills().addXp(Skills.CRAFTING, 80);
		else
			player.getSkills().addXp(Skills.CONSTRUCTION, 5);
		player.getInventory().addItem(new Item(32622, 3));
		if (ticks > 0)
			return 15;
		return -1;
	}
}