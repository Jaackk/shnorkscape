package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Prifddinas Cities Elen Anterth's dialogue.
 *
 * @author Noel
 */
public class ElenAnterth extends Dialogue {

	/**
	 * Integer representing Elen Anterth's ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Oh, hey there!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(NORMAL, "Hey!");
			stage = 0;
			break;
		case 0:
			sendOptionsDialogue("What would you like to say?", "What's your name?", "Do you know someone called Max?");
			stage = 1;
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "What's your name?");
				stage = 2;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "Do you know someone called Max?");
				stage = 8;
				break;
			}
			break;
		case 2:
			sendNPCDialogue(npcId, NORMAL, "I'm Elen! It's great to meet you! Welcome to the Max Guild!");
			stage = 3;
			break;
		case 3:
			sendNPCDialogue(npcId, NORMAL, "Well done on getting in here - it's well deserved! ");
			stage = 4;
			break;
		case 4:
			sendPlayerDialogue(CROOKED_HEAD, "Elen? Shouldn't you be called Maxine or something?");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, NORMAL,
					"Maxine? What a strange name that would be for an elf! "
							+ "Haha, I like it! But no, it's Elen - Elen Anterth. My family falls "
							+ "under the auspices of Clan Cadarn.");
			stage = 6;
			break;
		case 6:
			sendPlayerDialogue(CROOKED_HEAD, "How are you so skilled if the city's only recently been regrown?");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(npcId, NORMAL,
					"Oh, I've been all over! My family travelled east with the "
							+ "Cadarn way back. I was only young, but I saw so much and learned a lot. "
							+ "You'd be surprised what sticks with you.");
			stage = 0;
			break;
		case 8:
			sendNPCDialogue(npcId, CROOKED_HEAD, "You don't mean that bullish human from Varrock, do you?");
			stage = 9;
			break;
		case 9:
			sendPlayerDialogue(CALM, "Bullish? Well, I guess he is always running from place to place.");
			stage = 10;
			break;
		case 10:
			sendNPCDialogue(npcId, ANGRY, "Urgh, I hate that guy.");
			stage = 11;
			break;
		case 11:
			sendPlayerDialogue(CROOKED_HEAD, "When did you meet him?");
			stage = 12;
			break;
		case 12:
			sendNPCDialogue(npcId, CALM,
					"Since the city was regrown, he shows up here sometimes. "
							+ "Never stops long. Just rushes about the place training, doesn't take the "
							+ "time to just relax and enjoy things.");
			stage = 13;
			break;
		case 13:
			sendPlayerDialogue(NORMAL, "That sounds like Max.");
			stage = 14;
			break;
		case 14:
			sendNPCDialogue(npcId, CALM,
					"I tried talking to him the first few times I saw him, "
							+ "but he always just said he was 'doing his dailies' and didn't have time. "
							+ "There's more to our beautiful city than 'dailies'.");
			stage = 15;
			break;
		case 15:
			sendPlayerDialogue(CROOKED_HEAD, "Does he come in here at all?");
			stage = 16;
			break;
		case 16:
			sendNPCDialogue(npcId, CALM, "There's no gain for him here, he says. It'd be a waste, "
					+ "he says. You're not welcome here then, I said.");
			stage = 0;
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