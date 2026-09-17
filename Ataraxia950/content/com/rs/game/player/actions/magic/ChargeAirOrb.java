package com.rs.game.player.actions.magic;

import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.Magic;

/**
 * Handles Charging air orbs.
 *
 * @author Noel
 */
public class ChargeAirOrb extends Action {

	/**
	 * Checks if the player has all the requirements needed.
	 *
	 * @param player
	 *            The player.
	 * @return if meets requirements.
	 */
	public boolean checkAll(Player player) {
		if (!player.getInventory().containsItem(567, 1)) {
			player.sendMessage("You've ran out of unpowered orbs.", true);
			return false;
		}
        return Magic.checkSpellRequirements(player, 66, false, 556, 30, 564, 3);
    }

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		if (checkAll(player)) {
			setActionDelay(player, 2);
			player.getInventory().deleteItem(567, 1);
			Magic.checkSpellRequirements(player, 66, true, 556, 30, 564, 3);
			player.getSkills().addXp(Skills.MAGIC, 76);
			player.setNextAnimationNoPriority(new Animation(726));
			player.sendMessage("You fill the orb with the power of Air.", true);
			player.getInventory().addItem(573, 1);
		}
		return 0;
	}

	@Override
	public boolean start(Player player) {
        return checkAll(player);
    }

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 1);
	}
}