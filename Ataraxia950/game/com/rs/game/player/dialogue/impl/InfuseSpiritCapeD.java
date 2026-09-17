package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class InfuseSpiritCapeD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("By choosing to infuse the essence of the spirit cape with your completionist's cape, you will lose your spirit cape, but in return, receive the hidden summoning special reduction boost of the cape on all of your completionist's capes.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) 
			sendOptionsDialogue("Infuse the spirit cape into your completionist's cape?", "Infuse it.", "Keep the cape.");
		else {
			if (componentId == OPTION_1 && player.getInventory().containsItem(19893, 1)) {
				player.getInventory().deleteItem(19893, 1);
				player.infusedSpiritCapeEffect = true;
				player.sendMessage("You've infused the effects of the spirit cape in your completionist's cape.");
			}
			end();
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
