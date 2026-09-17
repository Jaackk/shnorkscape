package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * Handles Raw meat cooking into Sinew.
 *
 * @author Noel
 */
public class SinewCooking extends Action {

	public boolean checkAll(Player player) {
        return player.getInventory().containsOneItem(2132) || player.getInventory().containsOneItem(2134)
                || player.getInventory().containsOneItem(2136);
    }

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		if (checkAll(player)) {
			setActionDelay(player, 3);
			if (player.getInventory().containsOneItem(2132))
				player.getInventory().deleteItem(2132, 1);
			else if (player.getInventory().containsOneItem(2134))
				player.getInventory().deleteItem(2134, 1);
			else if (player.getInventory().containsOneItem(2136))
				player.getInventory().deleteItem(2136, 1);
			player.setNextAnimation(new Animation(897));
			if (Utils.random(5) != 0) {
				player.getInventory().addItem(9436, 1);
				player.getSkills().addXp(Skills.COOKING, 30);
				player.sendMessage("You successfully dry the meat into sinew.", true);
			} else {
				player.getInventory().addItem(2146, 1);
				player.getSkills().addXp(Skills.COOKING, 1);
				player.sendMessage("You failed drying the meat into sinew.", true);
			}
		}
		return 0;
	}

	@Override
	public boolean start(Player player) {
        return checkAll(player);
    }

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}
}