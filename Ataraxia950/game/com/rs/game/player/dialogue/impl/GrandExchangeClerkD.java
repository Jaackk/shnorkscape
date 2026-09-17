package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Grand Exchange Clerk Dialogue.
 *
 * @author Noel
 */
public class GrandExchangeClerkD extends Dialogue {

	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendPlayerDialogue(NORMAL, "Hi there.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendNPCDialogue(npcId, NORMAL, "Good day to you, " + (player.getAppearence().isMale() ? "sir" : "madam")
					+ ". " + "How can I help you?");
			stage = 0;
			break;
		case 0:
			sendOptionsDialogue("Select an Option", "I want to access the Grand Exchange, please.",
					"I want to collect my items.", "Can I see a history of my offers?",
					"Can you help me with item sets?", "I'm fine, actually.");
			stage = 1;
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "I want to access the Grand Exchange, please.");
				stage = 2;
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "I want to collect my items.");
				stage = 10;
				break;
			case OPTION_3:
				sendPlayerDialogue(NORMAL, "Can I see a history of my offers?");
				stage = 20;
				break;
			case OPTION_4:
				sendPlayerDialogue(NORMAL, "Can you help me with item sets?");
				stage = 30;
				break;
			case OPTION_5:
				sendPlayerDialogue(NORMAL, "I'm fine, actually.");
				stage = 40;
				break;
			}
			break;
		case 2:
			sendNPCDialogue(npcId, NORMAL, "Only too happy to help you, sir.");
			stage = 3;
			break;
		case 3:
			finish();
			player.getGEManager().openGrandExchange();
			break;
		case 10:
			sendNPCDialogue(npcId, NORMAL, "As you wish, " + (player.getAppearence().isMale() ? "sir" : "madam") + ".");
			stage = 11;
			break;
		case 11:
			finish();
			player.getGEManager().openCollectionBox();
			break;
		case 20:
			sendNPCDialogue(npcId, NORMAL,
					"If that is your wish, " + (player.getAppearence().isMale() ? "sir" : "madam") + ".");
			stage = 21;
			break;
		case 21:
			finish();
			player.getGEManager().openHistory();
			break;
		case 30:
			sendNPCDialogue(npcId, NORMAL,
					"It would be my pleasure, " + (player.getAppearence().isMale() ? "sir" : "madam") + ".");
			stage = 31;
			break;
		case 31:
			finish();
			player.getGEManager().openItemSets();
			break;
		case 40:
			sendNPCDialogue(npcId, NORMAL,
					"If you say so, " + (player.getAppearence().isMale() ? "sir" : "madam") + ".");
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