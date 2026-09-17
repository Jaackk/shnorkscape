package com.rs.game.player.dialogue.impl.npccontact;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Lanthus for lunar spells NPC contact.
 * 
 * @author Kris
 */
public class LanthusD extends Dialogue {

	private static final int NPC = 1526;

	@Override
	public void start() {
		sendNPCDialogue(NPC, 9827, "Good day, How may I help you?");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			sendOptionsDialogue("What would you like to ask?", "When's the next Caster wars game beginning?",
					"How many players are on either team?", "How are you, sir?", "Nothing, bye.");
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(9827, "When's the next Caster wars game beginning?");
				stage = 9;
				break;
			case OPTION_2:
				sendPlayerDialogue(9827, "How many players are on either team?");
				stage = 19;
				break;
			case OPTION_3:
				sendPlayerDialogue(9827, "How are you, sir?");
				stage = 29;
				break;
			case OPTION_4:
				sendPlayerDialogue(9827, "Nothing, bye.");
				stage = 49;
				break;
			}
			break;
		case 10:
			sendNPCDialogue(NPC, 9827, "Castles have been destructed completely!",
					"It'll take awhile before we get back,", "on our feet, unfortunately..");
			break;
		case 11:
			sendOptionsDialogue("What would you like to ask?", "How many players are on either team?",
					"How are you, sir?", "Nothing, bye.");
			break;
		case 12:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(9827, "How many players are on either team?");
				stage = 19;
				break;
			case OPTION_2:
				sendPlayerDialogue(9827, "How are you, sir?");
				stage = 29;
				break;
			case OPTION_3:
				sendPlayerDialogue(9827, "Nothing, bye.");
				stage = 49;
				break;
			}
			break;
		case 20:
			sendNPCDialogue(NPC, 9827, "Currently none, since castles have been", "destructed completely!");
			break;
		case 21:
			sendNPCDialogue(NPC, 9827, "It will be awhile before anyone can", "get back into the game..");
			break;
		case 22:
			sendOptionsDialogue("What would you like to ask?", "When's the next Caster wars game beginning?",
					"How are you, sir?", "Nothing, bye.");
			break;
		case 23:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(9827, "When's the next Caster wars game beginning?");
				stage = 9;
				break;
			case OPTION_2:
				sendPlayerDialogue(9827, "How are you, sir?");
				stage = 29;
				break;
			case OPTION_3:
				sendPlayerDialogue(9827, "Nothing, bye.");
				stage = 49;
				break;
			}
			break;
		case 30:
			sendNPCDialogue(NPC, 9827, "Not so good..", "I'm not sure whether you've heard or not,",
					"but the castles have been destructed!");
			break;
		case 31:
			sendNPCDialogue(NPC, 9827, "It may take awhile before we resettle in..",
					"In the mean time, however, no one shall enter", "the castles!");
			break;
		case 32:
			sendOptionsDialogue("What would you like to ask?", "When's the next Caster wars game beginning?",
					"How many players are on either team?", "Nothing, bye.");
			break;
		case 33:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(9827, "When's the next Caster wars game beginning?");
				stage = 9;
				break;
			case OPTION_2:
				sendPlayerDialogue(9827, "How many players are on either team?");
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