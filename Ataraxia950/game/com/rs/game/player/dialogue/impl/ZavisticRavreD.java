package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class ZavisticRavreD extends Dialogue {

	@Override
	public void start() {
		sendNPCDialogue(2059, Dialogue.CALM_TALKING, "What can I do for you, "+player.getUsername()+"?");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case 0:
			sendOptionsDialogue("Talk about...", "Buy stone of binding", "Nevermind");
			stage = 1;
			break;
		case 1:
			sendPlayerDialogue(Dialogue.CALM_TALKING, "I'd like to buy a stone of binding");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(2059, Dialogue.CALM_TALKING, "Sure thing, they will be 500,000 coins each.");
			stage = 3;			
			break;
		case 3:
			sendOptionsDialogue("How many would you like?", "1 - 500,000 coins", "3 - 1,500,000 coins", "5 - 2,500,000 coins");
			stage = 4;
			break;
		case 4:
			switch(componentId) {
			case OPTION_1:
				getStones(1);
				break;
			case OPTION_2:
				getStones(3);
				break;
			case OPTION_3:
				getStones(5);
				break;
			}
			finish();
			break;
		}
	}

	@Override
	public void finish() { player.getInterfaceManager().closeChatBoxInterface(); }
	
	private void getStones(int amount) {
		boolean paid = false;
		int cost = amount * 500000;
		if(player.getInventory().getFreeSlots() < amount) {
			player.sendMessage(Colors.RED+"You don't have enough inventory space for this!");
			return;
		}

		if(player.getMoneyPouch().getTotal() < (cost) && !player.getInventory().containsCoins(cost)) {
			player.sendMessage(Colors.RED+"You don't have enough coins for this!");
			return;
		}
		
		if(player.getMoneyPouch().getTotal() >= cost) {
			player.getMoneyPouch().removeAmount(cost);
			paid = true;
		}
		
		if(!paid && player.getInventory().containsCoins(cost)) {
			player.getInventory().deleteCoins(cost);
			paid = true;
		}
		
		if(paid) {
			player.getInventory().addItem(28629, amount);
			player.sendMessage(Colors.GREEN+"Zavister takes your coins and gives you the stone"+(amount > 1 ? "s" : "")+"!");
			return;
		}
		
	}

}
