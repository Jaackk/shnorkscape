package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles Prifddinas Cities Ianto's dialogue.
 *
 * @author Noel
 */
public class Ianto extends Dialogue {

	/**
	 * Integer representing Ianto's ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Hi there!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(NORMAL, "Hi, Ianto.");
			stage = 0;
			break;
		case 0:
			if (!player.hasReceivedCracker()) {
				sendNPCDialogue(npcId, NORMAL,
						"The makers of the world have one an award! Have a cracker, with our compliments!");
				stage = 1;
			} else {
				sendOptionsDialogue("Select an Option", "Want to see my spinning plates or kites?",
						"Want to check out my range of party items?");
				stage = 7;
			}
			break;
		case 1:
			sendOptionsDialogue("Would you like a golden cracker?", "Yes.", "No.");
			stage = 2;
			break;
		case 2:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Yes, please!");
				stage = 3;
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "No, thank you.");
				stage = 6;
				break;
			}
			break;
		case 3:
			sendItemDialogue(20083, 1, "Ianto gives you a golden cracker.");
			player.addItem(new Item(20083));
			player.setReceivedCracker();
			stage = 4;
			break;
		case 4:
			sendPlayerDialogue(NORMAL, "Thanks, Ianto!");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, NORMAL,
					"Pull it with another person and you'll get a special surprise! " + "Now, what can I do for you?");
			stage = 0;
			break;
		case 6:
			sendNPCDialogue(npcId, NORMAL, "Feel free to collect it later! Now, what can I do for you?");
			stage = 0;
			break;
		case 7:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "Spinning plates?");
				stage = 8;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "Party items, you say?");
				stage = 14;
				break;
			}
			break;
		case 8:
			sendNPCDialogue(npcId, CALM, "There was a mix-up some years back with a man who lives in Draynor - "
					+ "something about 'dragon plates' being confused for armour.");
			stage = 50;
			break;
		case 14:
			sendNPCDialogue(npcId, NORMAL, "Yes, I have all sorts of stuff - confetti, bubble markers, fireworks, "
					+ "firecrackers and some mugs! Take a look!");
			stage = 50;
			break;
		case 50:
			end();
			ShopsDataParser.openShop(player, 23);// TODO make the shop.
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