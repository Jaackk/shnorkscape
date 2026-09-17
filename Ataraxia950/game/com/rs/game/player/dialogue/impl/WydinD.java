package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles Wydin's dialogue.
 *
 * @author Noel
 */
public class WydinD extends Dialogue {

	/**
	 * The NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Welcome to my consumable store! Would you like to buy anything?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Yes please.", "No, thank you.", "What can you recommend?",
					"Can I get a job?");
			stage = 0;
			break;
		case 0:
			if (componentId == OPTION_1) {
				sendPlayerDialogue(NORMAL, "Yes, please, show me your goods.");
				stage = 10;
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(GLANCE_DOWN, "No, thank you.");
				stage = 20;
			}
			if (componentId == OPTION_3) {
				sendPlayerDialogue(NORMAL, "What can you recommend?");
				stage = 30;
			}
			if (componentId == OPTION_4) {
				sendPlayerDialogue(GLANCE_DOWN, "Can I get a job?");
				stage = 40;
			}
			break;
		case 10:
			ShopsDataParser.openShop(player, 6);
			finish();
			break;
		case 20:
			finish();
			break;
		case 30:
			sendNPCDialogue(npcId, NORMAL,
					"We have this really exotic fish all the way from the Baltic Seas - it's called a 'karambwan'.");
			stage = 31;
			break;
		case 31:
			sendOptionsDialogue("Select an Option", "Hmm, I think I'll try one.", "I don't like the sound of that.");
			stage = 32;
			break;
		case 32:
			if (componentId == OPTION_1) {
				sendPlayerDialogue(NORMAL, "Hmm, I think I'll try one.");
				stage = 10;
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(GLANCE_DOWN, "I don't like the sound of that.");
				stage = 99;
			}
			break;
		case 40:
			sendNPCDialogue(npcId, SAD,
					"Well, you're keen, I'll give you that. But unfortunately I don't have any jobs for you.");
			stage = 99;
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
