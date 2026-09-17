package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

public class ChaoticItemsCreationD extends Dialogue {

	private int itemId;
	private Item product;
	
	@Override
	public void start() {
		this.itemId = (int) parameters[0];
		this.product = itemId == 11716 ? new Item(31463) : itemId == 14484 ? new Item(27069) : new Item(27071);
		sendOptionsDialogue("Select an Option", "Yes, create a " + product.getName() + ".", "No, keep the spikes.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) {
				if (player.getInventory().containsItem(new Item(27068, product.getId() == 31463 ? 10 : 5))) {
					player.getInventory().deleteItem(new Item(27068, product.getId() == 31463 ? 10 : 5));
					player.getInventory().deleteItem(itemId, 1);
					player.getInventory().addItem(product);
					sendItemDialogue(product.getId(), 1, "You attach the chaotic spikes to the " + ItemDefinitions.getItemDefinitions(itemId).getName() + ".");
					stage = 1;
				} else
					sendDialogue("You need at least " + (product.getId() == 31463 ? 10 : 5) + " Chaotic spikes to create a " + product.getName() + ".");
			} else
				end();
		} else
			end();
	}

	@Override
	public void finish() {
		
	}

}
