package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

public class HefinMonkD extends Dialogue {
	
	@Override
	public void start() {
		sendOptionsDialogue("What do you want to do?",
				"5x Cleansing Crystal - 1.5M",
				"25x Cleansing Crystal - 7.5M",
				"100x Cleansing Crystal - 30M",
				"Nevermind");
		stage = 0;
	}
	
	@Override
	public void run(int interfaceId, int componentId) {
		int amount = getAmount(componentId);
		switch(stage) {
		case 0:
			switch(componentId) {
			case OPTION_1:
			case OPTION_2:
			case OPTION_3:
				if(player.getInventory().hasFreeSlots()) {
					if(!player.hasItem(new Item(32615, 100))) {
						if(player.getInventory().containsItem(995, amount) || player.getMoneyPouchValue() >= amount) {
							doBuy(amount);
							finish();
							return;
						} else
							sendDialogue("You don't have enough cash for this many crystals!");
					} else
						sendDialogue("You already have enough of these crystals! (Max: 100)");
				} else
					sendDialogue("You need inventory space to do this!");
				stage = 1;	
				break;
			case OPTION_4:
				finish();
				break;
			}
			break;
		case 1:
			finish();
			break;
		}
	}
	
	@Override
	public void finish() { player.getInterfaceManager().closeChatBoxInterface(); }

	public void doBuy(int amount) {
		if(player.getMoneyPouchValue() >= amount)
			player.getMoneyPouch().removeMoneyMisc(amount);
		else
			player.getInventory().deleteItem(995, amount);
		player.getInventory().addItem(32615, (amount/300000));
	}
	
	public int getAmount(int amount) {
		switch(amount){
		case OPTION_1:
			return 1500000;
		case OPTION_2:
			return 7500000;
		case OPTION_3:
			return 30000000;
		default:
			return 300000;
		}
	}
	
}
