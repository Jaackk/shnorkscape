package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Utils;

	/**
	 * Used to handle playing Harmonious harps at Prifddinas for Crafting EXP.
	 *
	 * @author Jaedmo
	 */
	public class DiamondCrafter extends Action {

		/**
		 * Declaring the Harp object (Only for facing measures).
		 */
		private final WorldObject crafter;
		/**
		 * The initial ticks as Integer.
		 */
		private int ticks = 100;

		/**
		 * Initing the actual Action action.
		 *
		 * @param object
		 *            The crafter object we're initing.
		 */
		public DiamondCrafter(WorldObject object) {
			this.crafter = object;
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
			if (crafter.getX() == 3553 && crafter.getY() == 6038) {
				player.faceObject(crafter);
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
			player.faceObject(crafter);
			World.sendObjectAnimation(crafter, new Animation(25034));
			return true;
		}

		@Override
		public void stop(Player player) {
			player.setNextAnimation(new Animation(25023));
			World.sendObjectAnimation(crafter, new Animation(-1));
			this.setActionDelay(player, 5);
		}

		@Override
		public int processWithDelay(Player player) {
			ticks--;
			if (Utils.random(100) >= 10)
				player.getSkills().addXp(Skills.CRAFTING, 140);
			else
				player.getSkills().addXp(Skills.CONSTRUCTION, 150);
			player.getInventory().addItem(new Item(32622, 3));
			if (ticks > 0)
				return 15;
			return -1;
		}
	}
