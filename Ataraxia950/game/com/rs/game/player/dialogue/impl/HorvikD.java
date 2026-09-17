package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles Horvik's dialogue.
 *
 * @author Noel
 */
public class HorvikD extends Dialogue {

	/**
	 * The NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Hello, do you need any help?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "No, thanks. I'm just looking around.", "Do you want to trade?");
			stage = 0;
			break;
		case 0:
			if (componentId == OPTION_1) {
				sendPlayerDialogue(NORMAL, "No, thanks. I'm just looking around");
				stage = 1;
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(NORMAL, "Do you want to trade?");
				stage = 3;
			}
			break;
		case 1:
			sendPlayerDialogue(NORMAL, "No, thanks. I'm just looking around.");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, NORMAL, "Well, come and see me if you're ever in need of equipment!");
			stage = 4;
			break;
		case 3:
			ShopsDataParser.openShop(player, 3);// TODO shop ID
			finish();
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
