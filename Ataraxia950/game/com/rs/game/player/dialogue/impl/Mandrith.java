package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class Mandrith extends Dialogue {

	private int npcId;

	@Override
	public void finish() {

	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			stage = 1;
			sendOptionsDialogue("Mandrith", "Open Shop", "Exchange Pk tokens for Pk Points",
					"How many Pk Points do I have?", "Exit");
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
				ShopsDataParser.openShop(player, 34);
				end();
				break;
			case OPTION_2:
				stage = 2;
				sendPlayerDialogue(9827, "Can you exchange my pk tokens for points?");
				break;
			case OPTION_3:
				stage = 5;
				sendPlayerDialogue(9827, "How many Pk Points do I have?");
				break;
			case OPTION_4:
				end();
				break;
			}
			break;
		case 2:
			sendNPCDialogue(npcId, 9827, "Yes of course I can.");
			stage = 3;
			break;
		case 3:
			sendOptionsDialogue("Do you want to exchange your tokens for points?", "Yes", "No");
			stage = 4;
			break;
		case 4:
			switch (componentId) {
			case OPTION_1:
				int givenPoints = 0;
				givenPoints = player.getInventory().getAmountOf(12852);
				player.setPkPoints(player.getPkPoints() + givenPoints);
				player.getInventory().deleteItem(12852, givenPoints);
				if (givenPoints != 0) {
					sendDialogue("You've exchanged " + givenPoints + " pk tokens for " + givenPoints + " points.");
				} else {
					sendDialogue("You don't have any pk tokens to exchange for points.");
				}
				stage = 6;
				break;
			case OPTION_2:
				end();
				break;
			}
			break;
		case 5:
			sendNPCDialogue(npcId, 9827,
					player.getDisplayName() + " you currently have " + player.getPkPoints() + " Pk Points.");
			stage = 6;
			break;
		case 6:
			end();
			break;
		}

	}

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		stage = 0;
		sendNPCDialogue(npcId, 9827, "Greetings " + player.getDisplayName()
				+ ". I'm the master of player killing, You can exchange your Pk tokens for Pk Points or buy items in my Pk shop.");
	}
}