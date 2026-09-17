package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Used to handle Polypore dungeon's Merchant Dialogue.
 *
 * @author Noel
 */
public class MerchantD extends Dialogue {

	int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, CALM, "It's a young gentleman. Perhaps it wants to "
				+ "trade with us. Or perhapts it needs us to look after its spores and flakes.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("What would you like to say?", "I have a question for you.", "I'd like to trade.",
					"I need you to look after something.", "I think I'll leave you alone!");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CALM, "I have a question for you.");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "I'd like to trade.");
				stage = 30;
				break;
			case OPTION_3:
				sendPlayerDialogue(CALM, "I need you to look after something.");
				stage = 31;
				break;
			case OPTION_4:
				sendPlayerDialogue(CALM, "I think I'll leave you alone!");
				stage = 33;
				break;
			}
			break;
		case 1:
			sendOptionsDialogue("What would you like to say?", "Who are you?", "What is this place?",
					"Tell me about the things you sell.", "Can I buy your costume?",
					"Okay, I've finished asking questions.");
			stage = 2;
			break;
		case 2:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(WHY, "Who are you?");
				stage = 3;
				break;
			case OPTION_2:
				sendPlayerDialogue(WHY, "What is this place?");
				stage = 12;
				break;
			case OPTION_3:
				sendPlayerDialogue(WHY, "Tell me about the things you sell.");
				stage = 17;
				break;
			case OPTION_4:
				sendPlayerDialogue(WHY, "Can I buy your costume?");
				stage = 24;
				break;
			case OPTION_5:
				sendPlayerDialogue(CALM, "Okay, I've finished asking questions.");
				stage = 29;
				break;
			}
			break;
		case 3:
			sendNPCDialogue(npcId, CALM, "It wants to know about us, does it? It's a "
					+ "nosey little gentleman, we thinks. But we shall answer it.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, CALM,
					"We used to live in a house, a little house in a " + "warm land, just us and Darling.");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, CALM,
					"Yes, we had Darling then, with her soft hair, " + "silken tresses on the pillow.. so long ago.");
			stage = 6;
			break;
		case 6:
			sendNPCDialogue(npcId, CALM, "We lived in our house, and the sun shone, and every "
					+ "day Darling would sing as she cooked and cleaned.");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(npcId, CALM, "We made pretty things to sell to the gentlefolk, and "
					+ "Darling loved the pretty things, and we loved Darling.");
			stage = 8;
			break;
		case 8:
			sendNPCDialogue(npcId, CALM, "Then there was no more darling - the doctor tried "
					+ "everything - so no more silken tresses, no more singing... just the " + "empty house.");
			stage = 9;
			break;
		case 9:
			sendNPCDialogue(npcId, CALM, "We left; we walked through days and nights and sun "
					+ "and rain. Then we found this place, and we met Ladyship.");
			stage = 10;
			break;
		case 10:
			sendNPCDialogue(npcId, CALM, "Ladyship makes pretty things too. Folks come to see "
					+ "them, and we trade with the folks. We sell them clothes made by " + "Ladyship; she lets us.");
			stage = 1;
			break;
		case 12:
			sendNPCDialogue(npcId, CALM, "It doesn't know where it is, silly little gentleman. " + "We shall tell it.");
			stage = 13;
			break;
		case 13:
			sendNPCDialogue(npcId, CALM, "This is Ladyship's garden. Ladyship came from a long "
					+ "way away. Ladyship is clever; she makes the flowers grow.");
			stage = 14;
			break;
		case 14:
			sendNPCDialogue(npcId, NORMAL, "Once there were ugly beasts here, but Ladyship grew "
					+ "her flowers all over the beasts to make them beautiful. Now they are " + "her pets.");
			stage = 15;
			break;
		case 15:
			sendNPCDialogue(npcId, CALM, "Folks come to see the pretty things Ladyship makes. "
					+ "Ladyship doesn't mind; she likes it when folks play with her pets.");
			stage = 1;
			break;
		case 17:
			sendNPCDialogue(npcId, CALM,
					"It wants to know about Ladyship's clever clothes. " + "We hope it will buy things afterwards.");
			stage = 18;
			break;
		case 18:
			sendNPCDialogue(npcId, CALM, "Ladyship makes clothes out of webs of mycelium, taken "
					+ "from her flowers. Wizards can wear Ladyship's clothes to help their " + "magicks.");
			stage = 19;
			break;
		case 19:
			sendNPCDialogue(npcId, CALM, "If we gather from Ladyship's pets, we can sew them "
					+ "onto Ladyship's clothes to give them more of her power - lots more power.");
			stage = 20;
			break;
		case 20:
			sendNPCDialogue(npcId, CALM, "The best flakes come from ladyship's strongest pets, but "
					+ "they're tricky to fight; very tricky indeed. Not many folks can touch them.");
			stage = 21;
			break;
		case 21:
			sendNPCDialogue(npcId, CALM, "Also, the flakes fall off the clothes after a while, so "
					+ "folks come back to get more flakes from Ladyship's pets.");
			stage = 1;
			break;
		case 24:
			sendNPCDialogue(npcId, NORMAL,
					"The young gentleman wants to buy our clothes. If we " + "sell it our clothes, we'll be naked.");
			stage = 25;
			break;
		case 25:
			sendNPCDialogue(npcId, NORMAL, "Ladyship doesn't like it when we're naked; she said so.");
			stage = 26;
			break;
		case 26:
			sendNPCDialogue(npcId, CALM,
					"We'd better tell the young gentleman it can't have our " + "clothes, not at any price.");
			stage = 27;
			break;
		case 27:
			sendPlayerDialogue(CALM, "Fair enough!");
			stage = 1;
			break;
		case 29:
			sendNPCDialogue(npcId, CALM, "Perhaps the young gentleman would like to trade now. "
					+ "Or we can look after its spores and flakes if it wishes.");
			stage = -1;
			break;
		case 30:
			finish();
			ShopsDataParser.openShop(player, 51);
			break;
		case 31:
			sendNPCDialogue(npcId, SAD, "The gentleman wants to know if we can store flowers "
					+ "for them. We don't store flowers for folks anymore.");
			stage = -1;
			break;
		case 33:
			sendNPCDialogue(npcId, CALM, "It's going to leave us alone.", "But we're not alone, are we?");
			stage = 34;
			break;
		case 34:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}