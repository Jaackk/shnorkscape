package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

public class ChaoticRemnantUsageD extends Dialogue {

	private int itemId;
	private Item product;
	
	@Override
	public void start() {
		this.itemId = (int) parameters[0];
		this.product = itemId == 25028 ? new Item(31448) : itemId == 25031 ? new Item(18335) : new Item(31445);
		sendOptionsDialogue("Select an Option", "Yes, create a " + product.getName() + ".", "No, keep the remnant.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) {
				if (player.getInventory().containsItem(new Item(31449, 1))) {
					player.getInventory().deleteItem(new Item(31449, 1));
					player.getInventory().deleteItem(itemId, 1);
					player.getInventory().addItem(product);
					sendItemDialogue(product.getId(), 1, "You attach the chaotic remnant to the " + ItemDefinitions.getItemDefinitions(itemId).getName() + ".");
					stage = 1;
				} else
					sendDialogue("You need a chaotic remnant to do this.");
			} else
				end();
		} else
			end();
	}

	@Override
	public void finish() {
		
	}

}
