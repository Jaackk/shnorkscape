package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;

public class ExchangeTrailblazerDialogue extends Dialogue {

	private Item item;
	
	@Override
	public void start() {
		item = (Item) parameters[0];
		sendItemDialogue(item.getId(), 1, "You show the trailblazer outfit piece to the Dungeoneering tutor.");
	}
	
	public static final boolean hasAllGorajanSets(Player player) {
		boolean hasItems = true;
		for (int i = 38521; i < 38541; i++) {
			if (!player.getInventory().containsItem(new Item(i, 1))) {
				hasItems = false;
				break;
			}
		}
		return hasItems;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 100) {
			end();
			return;
		}
		if (stage == -1) {
			if (!player.hasItem(new Item(item.getId(), 2))) {
				sendNPCDialogue(9712, NORMAL, "It appears as if you haven't got a spare piece of this equipment. Unfortunately, I cannot exchange this into other pieces for you.");
				stage = 100;
				return;
			}
			sendNPCDialogue(9712, NORMAL, "I see you'd like to exchange this item for another piece. Well, I'd be more than glad to help you out. Pick the piece you wish to exchange this item for.");
		} else if (stage == 0) 
			sendOptionsDialogue("Select the piece you'd like.", "Head", "Body", "Legs", "Gloves", "Boots");
		else if (stage == 1) {
			if (player.getInventory().containsItem(item)) {
				player.getInventory().deleteItem(item);
				int tier = (item.getId() - 38521) / 5;
				int option = 0;
				switch(componentId) {
				case OPTION_2:
					option = 1;
					break;
				case OPTION_3:
					option = 2;
					break;
				case OPTION_4:
					option = 3;
					break;
				case OPTION_5:
					option = 4;
					break;
				}
				int piece = 38521 + (5 * tier) + option;
				player.getInventory().addItem(piece, 1);
				sendItemDialogue(piece, 1, "Dungeoneering Tutor hands you a " + ItemDefinitions.getItemDefinitions(piece).getName() + " in exchange for the " + item.getName() + ".");
				player.getGorajanTrailblazer().unlockOutfit(piece);
				stage = 100;
				return;
			} else end();
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
