package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Prifddinas Cities Mhistyll's dialogue.
 *
 * @author Noel
 */
public class Mhistyll extends Dialogue {

	/**
	 * Integer representing Mhistyll's NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, CALM, "Good day, " + player.getDisplayName() + ". Welcome to my shop. Have you come "
				+ "to help with the crystal urchins?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an option", "Who are you?", "What are the crystal urchins?",
					"What can I do to help?", "Can I see what you have for sale?");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(GLANCE_DOWN, "Who are you?");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(GLANCE_DOWN, "What are the crystal urchins?");
				stage = 3;
				break;
			case OPTION_3:
				sendPlayerDialogue(GLANCE_DOWN, "What can I do to help?");
				stage = 10;
				break;
			case OPTION_4:
				sendPlayerDialogue(GLANCE_DOWN, "Can I see what you have for sale?");
				stage = 18;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, CALM, "I am Mhistyll. I am the keeper of the cave that lies behind me.");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, CALM, "I also reward those who try and deal with the infestation of "
					+ "crystal urchins in the waterfall above.");
			stage = -1;
			break;
		case 3:
			sendNPCDialogue(npcId, CALM,
					"The rock pool at the top of the waterfall used to be filled " + "with regular urchins.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, SAD,
					"But a crystal within the caves below extended into the pool, " + "and the urchins changed.");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, CALM, "Now they are living crystal. They breed rapidly and cannot "
					+ "be killed by their regular predators.");
			stage = 6;
			break;
		case 6:
			sendNPCDialogue(npcId, CROOKED_HEAD,
					"If something is not done then the waterfall will " + "become a cascade of spiny, crystal death!");
			stage = 7;
			break;
		case 7:
			sendPlayerDialogue(TALK_SWING, "That seems a little melodramatic.");
			stage = 8;
			break;
		case 8:
			sendNPCDialogue(npcId, CALM, "Some of them are the size of beach balls. Can you imagine "
					+ "the damage they will cause when they start raining down onto the city?");
			stage = 9;
			break;
		case 9:
			sendPlayerDialogue(CALM, "That's a good point.");
			stage = -1;
			break;
		case 10:
			sendNPCDialogue(npcId, CALM,
					"To help fish out the urchins you will need to climb to " + "the top of the waterfall.");
			stage = 11;
			break;
		case 11:
			sendNPCDialogue(npcId, CALM, "There is a route next to me leading up the side of the "
					+ "waterfall but you will need to bring a crossbow and a grapple.");
			stage = 12;
			break;
		case 12:
			sendItemDialogue(9419, 1, "You need at least 90 Ranged, 90 Agility and 90 Strength "
					+ "to use the grapple location, as well as a crossbow and a grapple.");
			stage = 13;
			break;
		case 13:
			sendNPCDialogue(npcId, CALM, "Once there you can fish small, medium and large urchins, "
					+ "if you are a sufficiently talented fisher.");
			stage = 14;
			break;
		case 14:
			sendNPCDialogue(npcId, CALM,
					"You can then grind them to dust, which can be used to " + "make perfect juju fishing potions.");
			stage = 15;
			break;
		case 15:
			sendNPCDialogue(npcId, NORMAL, "Or if you don't want to do that then you can trade " + "them to me.");
			stage = 16;
			break;
		case 16:
			sendNPCDialogue(npcId, CALM, "I have lamps that can help increase your fishing skill, "
					+ "as well as the song that can let you create a crystal fishing rod.");
			stage = 17;
			break;
		case 17:
			sendNPCDialogue(npcId, NORMAL, "Also, if you find a hermit crab in the pool I have "
					+ "a collection of objects you can use to give them a home.");
			stage = -1;
			break;
		case 18:
			sendNPCDialogue(npcId, SAD, "I do not have anything for sale right now.");
			stage = -1;
			break;
		}
	}

	@Override
	public void finish() {
	}

}