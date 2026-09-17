package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles The Trader's dialogue.
 */
public class TheTraderD extends Dialogue {

	// The NPC ID.
	int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		if (player.getPorts().firstTimer) {
			sendDialogue("The Trader has no goods for sale at the moment. Check bank once you have "
					+ "completed the Port tutorial.");
			stage = 99;
		} else
			sendNPCDialogue(npcId, NORMAL, "Hello there!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "What do you do around here?", "I would like to trade.",
					"I must be going.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(GLANCE_DOWN, "What do you do around here?");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "I would like to trade.");
				stage = 2;
				break;
			case OPTION_3:
				sendPlayerDialogue(CALM, "I must be going.");
				stage = 4;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, NORMAL, "I can trade your extra resources for some Chime as well as "
					+ "exchange your resources for various powerful equipment.");
			stage = -1;
			break;
		case 2:
			sendNPCDialogue(npcId, CALM, "Let's see what I have for you.");
			stage = 3;
			break;
		case 3:
			player.getDialogueManager().startDialogue("TheTraderRewardsD", npcId);
			break;
		case 4:
			sendNPCDialogue(npcId, NORMAL, "If you change your mind, come back. This offer won't last forever.");
			stage = 99;
			break;

		case 99:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}
}