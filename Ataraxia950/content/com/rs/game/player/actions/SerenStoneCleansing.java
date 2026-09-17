package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/**
 * Used to handle Corrupted Seren stone cleansing Prayer action.
 *
 * @author Noel
 */
public class SerenStoneCleansing extends Action {

	/**
	 * Declaring the Seren stone object (Only for facing measures).
	 */
	private final WorldObject serenStone;
	/**
	 * The initial ticks as Integer.
	 */
	private int ticks = 18;

	/**
	 * Initing the actual Action action.
	 *
	 * @param object
	 *            The Seren stone object we're initing.
	 */
	public SerenStoneCleansing(WorldObject object) {
		this.serenStone = object;
	}

	/**
	 * Checks if we can continue interacting.
	 *
	 * @param player
	 *            The player interacting.
	 * @return if we can continue.
	 */
	public static boolean checkAll(Player player) {
		if (player.getSkills().getLevel(Skills.PRAYER) < 70) {
			player.sendMessage("You need a Prayer level of at least 70 to do this activity.");
			return false;
		}
		if (!player.getInventory().containsItem(new Item(32615, 1))) {
			player.getDialogueManager().startDialogue("SimpleItemMessage", 32615, 1,
					"You will need to purchase a cleansing crystal from one of the monks in the "
							+ "Hefin Cathedral to cleanse the corruption from this Seren stone.");
			return false;
		}
		return true;
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		player.setNextAnimation(new Animation(24556));
		player.getSkills().addXp(Skills.PRAYER, 525);
		this.setActionDelay(player, 3);
		player.faceObject(serenStone);
		return true;
	}

	@Override
	public boolean process(Player player) {
        return checkAll(player);
    }

	@Override
	public void stop(Player player) {
		player.setNextAnimation(new Animation(24558));
		player.getInventory().deleteItem(new Item(32615, 1));
		player.getSkills().addXp(Skills.PRAYER, 618.625);
		this.setActionDelay(player, 3);
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		if (!player.getInventory().containsItem(new Item(32615, 1))) {
			player.sendMessage("You have no more cleansing crystals to work with.");
			this.stop(player);
		}
		player.getSkills().addXp(Skills.PRAYER, 68.75);
		if (ticks > 0)
			return 9;
		return -1;
	}
}