package com.rs.game.player.dialogue.impl.npccontact;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class PikkupstixD extends Dialogue {

	private static final int NPC = 6988;
	
	@Override
	public void start() {
		sendNPCDialogue(NPC, NORMAL, "Hello there, " + player.getDisplayName() + ".");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			sendOptionsDialogue("Select an Option",
					"How do I train summoning?",
					"Can I purchase some supplies?",
					"Nothing, bye.");
			break;
		case 0:
			switch(componentId) {
			case OPTION_1:
				sendNPCDialogue(NPC, NORMAL, "To train summoning, you need some charms - these can be obtained from killing various monsters.");
				break;
			case OPTION_2:
				sendNPCDialogue(NPC, NORMAL, "Sure, which shop would you like to see?");
				stage = 10;
				return;
			case OPTION_3:
				end();
				return;
			}
			break;
		case 1:
			sendNPCDialogue(NPC, NORMAL, "After you've obtained some charms, you will need some spirit shards and empty pouches. You can purchase those from me.");
			break;
		case 2:
			sendNPCDialogue(NPC, NORMAL, "And after you have all those items, you will need to infuse the aforementioned items with a tertiary item to create unique pouches.");
			break;
		case 3:
			sendOptionsDialogue("Select an Option",
					"How do I train summoning?",
					"Can I purchase some supplies?",
					"Nothing, bye.");
			stage = 0;
			return;
		case 10:
			sendOptionsDialogue("Select an Option", "Basic Ingredients shop", "Starter Ingredients shop",
					"Intermediate Ingredients shop", "Novice Ingredients shop");
			break;
		case 11:
			switch (componentId) {
			case OPTION_1:
				ShopsDataParser.openShop(player, 9);
				break;
			case OPTION_2:
				ShopsDataParser.openShop(player, 10);
				break;
			case OPTION_3:
				ShopsDataParser.openShop(player, 11);
				break;
			case OPTION_4:
				ShopsDataParser.openShop(player, 12);
				break;
			}
			end();
			break;
		}
		stage++;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
