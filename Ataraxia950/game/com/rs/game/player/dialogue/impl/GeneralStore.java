package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class GeneralStore extends Dialogue {

	private int npcId;
	private int shopId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		shopId = (Integer) parameters[1];
		sendNPCDialogue(npcId, NORMAL, "Can I help you at all?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "I'd like to see your goods.",
					"How should I use your shop?", "No, you can't.");
			stage = 0;
			break;
		case 0:
			if (componentId == OPTION_1) {
				// sendPlayerDialogue(NORMAL, "Yes, I'd like to see your
				// goods.");
				ShopsDataParser.openShop(player, shopId);
				finish();
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(NORMAL, "How should I use your shop?");
				stage = 1;
			}
			if (componentId == OPTION_3) {
				sendPlayerDialogue(GLANCE_DOWN, "No, you can't..");
				stage = 5;
			}
			break;
		case 2:
			sendNPCDialogue(npcId, GOOFY_LAUGH, "You can also sell most items to the shop and the price",
					"given will be based on the amount in stock.");
			stage = 5;
			break;
		case 1:
			sendNPCDialogue(npcId, NORMAL, "I'm glad you asked! You can buy as many of the items",
					"stocked as you wish. The price of these items changes", "based on the amount in stock.");
			stage = 2;
			break;
		case 5:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}