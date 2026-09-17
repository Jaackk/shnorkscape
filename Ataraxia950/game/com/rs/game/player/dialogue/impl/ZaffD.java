package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles Zaff's dialogue.
 *
 * @author Noel
 */
public class ZaffD extends Dialogue {

	/**
	 * The NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL,
				"Would you like to buy or sell magic equipment or is there something else you need?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Yes, please.", "No, thank you.");
			stage = 0;
			break;
		case 0:
			if (componentId == OPTION_1) {
				sendPlayerDialogue(NORMAL, "Yes, please, what do you have in stock?");
				stage = 1;
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(NORMAL, "No, thank you.");
				stage = 2;
			}
			break;
		case 1:
			ShopsDataParser.openShop(player, 7);
			finish();
			break;
		case 2:
			sendNPCDialogue(npcId, GOOFY_LAUGH, "Well, 'stick' your head in again if you change your mind.");
			stage = 3;
			break;
		case 3:
			sendPlayerDialogue(GOOFY_LAUGH, "Huh, terrible pun. You just can't get the 'staff' these days!");
			stage = 4;
			break;
		case 4:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}
