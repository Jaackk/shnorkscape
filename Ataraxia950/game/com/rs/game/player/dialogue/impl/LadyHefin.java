package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Prifddinas Cities Lady Hefin's dialogue.
 *
 * @author Noel
 */
public class LadyHefin extends Dialogue {

	/**
	 * Integer representing Lady Hefin's NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Praise Seren for sending you, human. You are the saviour of our city.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Ask about Lady Hefin...", "Ask about Clan Hefin...",
					"Ask about Hefin skills...", "Nothing, thanks.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "Can you tell me about yourself?");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "Can you tell me about Clan Hefin?");
				stage = 3;
				break;
			case OPTION_3:
				sendPlayerDialogue(CROOKED_HEAD, "Can you tell me about Clan Hefin's skills?");
				stage = 5;
				break;
			case OPTION_4:
				sendPlayerDialogue(CALM, "Nothing, thanks.");
				stage = 99;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, CALM, "I am merely another traveller on the journey to enlightenment. "
					+ "It is a road few dare walk, but the light of Seren shall show us the way.");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, CALM, "Who I am is not important. Nor indeed my origin. To focus on "
					+ "what we were inhibits our potential to become something more.");
			stage = -1;
			break;
		case 3:
			sendNPCDialogue(npcId, CALM,
					"Where other clans build or fight or destroy, clan Hefin looks "
							+ "inward. We contemplate the physical and spiritual world, and meditate upon the "
							+ "wisdom of Seren.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, CALM, "We strengthen our bodies and minds, that we may be enlightened "
					+ "by her grace and the beauty of her spirit.");
			stage = -1;
			break;
		case 5:
			sendNPCDialogue(npcId, CALM,
					"We seek to cleanse our minds in prayer, and hone our bodies "
							+ "through agility exercises. We welcome all who crave enlightenment, "
							+ "and provide for their physical and spiritual needs.");
			stage = 6;
			break;
		case 6:
			sendNPCDialogue(npcId, CALM, "Why not leap atop a serenity post, and meditate in unity upon "
					+ "the wisdom of Seren? I think you will find the experience quite edifying.");
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