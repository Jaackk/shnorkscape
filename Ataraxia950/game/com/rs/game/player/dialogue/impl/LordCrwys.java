package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Prifddinas Cities Lord Crwys's dialogue.
 *
 * @author Noel
 */
public class LordCrwys extends Dialogue {

	/**
	 * Integer representing Lord Crwy's NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, CALM, "Ah, hello again, young Player. How can I help you?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Ask about Lord Crwys...", "Ask about Clan Crwys...",
					"Ask about Crwys skills...", "Nothing, thanks.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "Can you tell me about yourself?");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "Can you tell me about Clan Crwys?");
				stage = 8;
				break;
			case OPTION_3:
				sendPlayerDialogue(CROOKED_HEAD, "Can you tell me about Clan Crwys's skills?");
				stage = 12;
				break;
			case OPTION_4:
				sendPlayerDialogue(CALM, "Nothing, thanks.");
				stage = 99;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, CALM,
					"I am the lord of Clan Crwys. That, I suppose, is where I "
							+ "should begin, for it is my most important function. Beyond that "
							+ "I am an elf, or I was, or I am?");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, CALM, "It's hard to remember, my limbs have been bark for so long "
					+ "that I cannot recall whether my veins should run with blood or sap.");
			stage = 3;
			break;
		case 3:
			sendPlayerDialogue(CALM, "You're an elf. You only turned yourself into a tree as a disguise.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, NORMAL, "Ha! Yes, I suppose you're right. Or perhaps I am a tree that "
					+ "turned itself into an elf as a disguise to free the wrath of my arboreal brethren.");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, CALM, "Tree-kind can be surprisingly brutal to one another. "
					+ "The battle for resources is never-ending, and we do not lift a branch when " + "another falls.");
			stage = 6;
			break;
		case 6:
			sendPlayerDialogue(CROOKED_HEAD, "You mean you can't lift a branch...right?");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(npcId, NORMAL, "Ha ha ha. Yes, yes, that is what we'd have you believe.");
			stage = -1;
			break;
		case 8:
			sendNPCDialogue(npcId, CALM,
					"Where other clans create or destroy, clan Crwys maintains. "
							+ "We create and destroy plant life in equal quantities, maintaining a status "
							+ "quo in which we can flourish.");
			stage = 9;
			break;
		case 9:
			sendNPCDialogue(npcId, CALM,
					"We are not a clan that revels in luxury and pomp. "
							+ "We are the clan with soil beneath our fingernails and splinters in our skin. "
							+ "Some would argue ours is a simple life, but they have never spent the seasons "
							+ "watching over a farming patch.");
			stage = 10;
			break;
		case 10:
			sendNPCDialogue(npcId, CALM,
					"Learning to spot the signs of disease or infestation. "
							+ "Nor have they had to worry about where a tree will fall and make sure that it "
							+ "falls on nothing of import.");
			stage = 11;
			break;
		case 11:
			sendNPCDialogue(npcId, NORMAL, "Hah! Simple, give them a season with us and we'll see if "
					+ "they still think we live a simple life.");
			stage = -1;
			break;
		case 12:
			sendNPCDialogue(npcId, CALM,
					"We focus on the art of woodcutting and the craft of farming. "
							+ "Both serve their role in maintaining the elven culture and civilisation. "
							+ "Through our farms we provide food and vital herbs needed by the other clans.");
			stage = 13;
			break;
		case 13:
			sendNPCDialogue(npcId, CALM,
					"Without us the city would starve and sickness would run "
							+ "rampant. Through woodcutting we hold the trees back from rampant overgrowth, "
							+ "which could have untold damage on our fair city.");
			stage = 14;
			break;
		case 14:
			sendNPCDialogue(npcId, CALM,
					"Through their wood we provide fuel for fires and materials "
							+ "for our artisans. Both these skills are vital. I would encourage you to build "
							+ "on your own skills.");
			stage = -1;
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