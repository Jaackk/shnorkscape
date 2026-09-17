package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles the Hunter Expert's dialogue.
 *
 * @author Noel
 */
public class HunterExpertD extends Dialogue {

	/**
	 * Represents the NPC's ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (int) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Hey, want to buy some hunting supplies?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Open shop", "Walk away slowly..");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				end();
				ShopsDataParser.openShop(player, 31);
				break;
			case OPTION_2:
				sendPlayerDialogue(GLANCE_DOWN, "Ehm, no thank you. Maybe another time..");
				stage = 1;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, NORMAL, "I'll be right here if you need me!");
			stage = 2;
			break;
		case 2:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}
}