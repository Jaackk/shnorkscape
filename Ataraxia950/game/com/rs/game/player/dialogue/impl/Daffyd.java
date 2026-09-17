package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Prifddinas Cities Daffyd's dialogue.
 *
 * @author Noel
 */
public class Daffyd extends Dialogue {

	/**
	 * Integer representing Daffyd's NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "How goes the impling catching? Good? Good! I can't wait. Yay, implings!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(CALM, "Hi, Daffyd. I'm making progress.");
			stage = 0;
			break;
		case 0:
			sendNPCDialogue(npcId, CROOKED_HEAD, "Any questions for Daffyd?");
			stage = 1;
			break;
		case 1:
			sendOptionsDialogue("Select an Option", "What do you do here?", "Why do you want implings?",
					"None right now, thanks.");
			stage = 2;
			break;
		case 2:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "What do you do here?");
				stage = 3;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "Why do you want implings?");
				stage = 5;
				break;
			case OPTION_3:
				sendPlayerDialogue(CALM, "None right now, thanks.");
				stage = 99;
				break;
			}
			break;
		case 3:
			sendNPCDialogue(npcId, NORMAL,
					"Yeah, so - I'm a student of Lord Amlodd's. He's teaching "
							+ "me all about divination and summoning. I've got huge interest in familiars "
							+ "and pets and magical stuff, so Amlodd is the place for me, yeah!");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, NORMAL, "My mum thought it would be good for me to get a hobby - so I did!");
			stage = -1;
			break;
		case 5:
			sendNPCDialogue(npcId, NORMAL,
					"I don't know, they're just really cool. Flying around all day, "
							+ "not a care in the world. And I like how they steal stuff that people want, "
							+ "so people want to catch them.");
			stage = 6;
			break;
		case 6:
			sendNPCDialogue(npcId, NORMAL,
					"I want to catch one of each! Not for anything sinister - "
							+ "they'll like it here. I want to look after them. They'll make great pets! "
							+ "Oh! And to study!");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(npcId, NORMAL, "Lord Amlodd wants me to study them, especially the new "
					+ "crystal type that's only been showing up since Prifddinas regrew!");
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