package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class LoweD extends Dialogue {

	/**
	 * The NPC ID.
	 */
	private int npcId;

	/**
	 * Handles Lowe's dialogue.
	 *
	 * @author Noel
	 */
	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Welcome to Lowe's Archery Emporium. Do you want to see my wares?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Yes, please.", "No, I prefer to bash things close up.");
			stage = 0;
			break;
		case 0:
			if (componentId == OPTION_1) {
				ShopsDataParser.openShop(player, 8);
				end();
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(NORMAL, "No, I prefer to bash things close up.");
				stage = 1;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, GLANCE_DOWN, "Humph, philistine.");
			stage = 2;
			break;
		case 2:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}