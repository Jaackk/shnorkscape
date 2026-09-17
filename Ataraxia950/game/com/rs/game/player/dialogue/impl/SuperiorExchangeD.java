package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.content.ports.SuperiorExchange;
import com.rs.game.player.dialogue.Dialogue;

public class SuperiorExchangeD extends Dialogue {

	private int npcId;
	private Item item;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		item = (Item) parameters[1];
		sendNPCDialogue(npcId, NORMAL,
				"Would you like me to exchange your " + item.getName() + " " + "for a superior version?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue(
					"Upgrade <col=ff0000>" + item.getName() + "</col> for <col=ff0000>"
							+ SuperiorExchange.getPrice(item.getId()) + "</col> Chime?",
					"Yes, please.", "No, thank you.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Yes, please, upgrade it for me!");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "No, thank you.");
				stage = 99;
				break;
			}
			break;
		case 1:
			stage = 99;
			if (SuperiorExchange.exchange(player, item.getId()))
				sendNPCDialogue(npcId, NORMAL, "There you go, sir.");
			else
				sendNPCDialogue(npcId, SAD, "It seems that you don't have enough Chime. Come back later when you do.");
			break;
		case 99:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}