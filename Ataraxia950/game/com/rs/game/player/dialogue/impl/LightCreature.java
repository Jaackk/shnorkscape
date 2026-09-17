package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Light Creature's dialogue.
 */
public class LightCreature extends Dialogue {

	/**
	 * @param player
	 *            The player to reward.
	 * @return the Reward item ID.
	 */
	private static Item getReward(Player player) {
		Item[] rewards = { new Item(9475), new Item(7479), new Item(7218) };
		for (Item item : rewards)
			return item;
		return null;
	}

	@Override
	public void start() {
		if (player.hefinLapReward) {
			sendItemDialogue(getReward(player).getId(), getReward(player).getAmount(),
					"The light creature gives you a " + getReward(player).getName().toLowerCase() + ".");
			player.getInventory().addItemDrop(getReward(player).getId(), getReward(player).getAmount());
			player.hefinLapReward = false;
		} else
			player.sendMessage("You have no reward to claim.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}