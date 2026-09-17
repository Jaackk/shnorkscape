package com.rs.game.player.dialogue.impl.npccontact;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Honest Jimmy for lunar spells NPC contact.
 * 
 * @author Kris
 */
public class HonestJimmyD extends Dialogue {

	int npcId = 4362;

	@Override
	public void start() {
		sendNPCDialogue(npcId, 9827, "Good day, How may I help you?");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			sendOptionsDialogue("What would you like to ask?", "When's the next Trouble Brewing game beginning?",
					"How are you?", "How can you speak to me?", "Nothing, bye.");
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(9827, "When's the next Trouble Brewing game beginning?");
				stage = 9;
				break;
			case OPTION_2:
				sendPlayerDialogue(9827, "How are you?");
				stage = 19;
				break;
			case OPTION_3:
				sendPlayerDialogue(9827, "How can you speak to me?");
				stage = 29;
				break;
			case OPTION_4:
				sendPlayerDialogue(9827, "Nothing, bye.");
				stage = 49;
				break;
			}
			break;
		case 10:
			sendNPCDialogue(npcId, 9827, "Our machines are down,", "therefore the time is currently unknown,",
					"unfortunately.");
			break;
		case 11:
			sendOptionsDialogue("What would you like to ask?", "How are you?", "How can you speak to me?",
					"Nothing, bye.");
			break;
		case 12:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(9827, "How are you?");
				stage = 19;
				break;
			case OPTION_2:
				sendPlayerDialogue(9827, "How can you speak to me?");
				stage = 29;
				break;
			case OPTION_3:
				sendPlayerDialogue(9827, "Nothing, bye.");
				stage = 49;
				break;
			}
			break;
		case 20:
			sendNPCDialogue(npcId, 9827, "I'm not so good, sadly..");
			break;
		case 21:
			sendNPCDialogue(npcId, 9827, "Our machines here have broken down", "if I don't manage to get this resolved",
					"any time soon, my business may go down!");
			break;
		case 22:
			sendPlayerDialogue(9827, "That's unfortunate to hear.", "Well, I hope things get better soon!");
			break;
		case 23:
			sendOptionsDialogue("What would you like to ask?", "When's the next Trouble Brewing game beginning?",
					"How can you speak to me?", "Nothing, bye.");
			break;
		case 24:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(9827, "When's the next Trouble Brewing game beginning?");
				stage = 9;
				break;
			case OPTION_2:
				sendPlayerDialogue(9827, "How can you speak to me?");
				stage = 29;
				break;
			case OPTION_3:
				sendPlayerDialogue(9827, "Nothing, bye.");
				stage = 49;
				break;
			}
			break;
		case 30:
			sendNPCDialogue(npcId, 9827, "Silly you. You're the one casting spells here,",
					"yet you still feel the urge to ask me?");
			break;
		case 31:
			sendOptionsDialogue("What would you like to ask?", "When's the next Trouble Brewing game beginning?",
					"How are you?", "Nothing, bye.");
			break;
		case 32:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(9827, "When's the next Trouble Brewing game beginning?");
				stage = 9;
				break;
			case OPTION_2:
				sendPlayerDialogue(9827, "How are you?");
				stage = 19;
				break;
			case OPTION_3:
				sendPlayerDialogue(9827, "Nothing, bye.");
				stage = 49;
				break;
			}
			break;
		case 50:
			end();
			break;
		default:
			break;
		}
		stage++;
	}

	@Override
	public void finish() {
	}

}