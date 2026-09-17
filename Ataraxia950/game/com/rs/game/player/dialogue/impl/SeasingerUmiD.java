package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles The Seasinger Umi's dialogue.
 */
public class SeasingerUmiD extends Dialogue {

	// The NPC ID.
	int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		if (player.getPorts().firstTimer) {
			sendNPCDialogue(npcId, CALM,
					"So you're the new Portmaster? I can tell you're busy, but " + "maybe we should catch up later?");
			stage = 99;
		} else
			sendNPCDialogue(npcId, CALM, "Oh, hello there. Don't mind me, I'm just taking a " + "break from sweeping.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(GLANCE_DOWN, "Who are you?");
			stage = 0;
			break;
		case 0:
			sendNPCDialogue(npcId, CALM, "I'm Umi.");
			stage = 1;
			break;
		case 1:
			sendPlayerDialogue(GLANCE_DOWN, "What are you?");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, ANGRY, "What am I? How rude. I'm a siren, obviously.");
			stage = 3;
			break;
		case 3:
			sendPlayerDialogue(GLANCE_DOWN, "Are you a siren?");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, GOOFY_LAUGH,
					"Very observant! Normally I'm asked whether I'm a "
							+ "mermaid or a fish. I've heard more fish jokes than you could shake a "
							+ "mermaid's tail at. Nice to meet you.");
			stage = 5;
			break;
		case 5:
			sendPlayerDialogue(GLANCE_DOWN, "Seems odd that a siren would work as a sweep. "
					+ "Shouldn't you be luring sailors to their watery graves?");
			stage = 6;
			break;
		case 6:
			sendNPCDialogue(npcId, CALM,
					"If only. No I'm training to become a seasinger. "
							+ "One day I'll be able to control the seas and the creatures that "
							+ "live within it. It just takes such a long time to learn. Until then I've "
							+ "got to earn my keep.");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(npcId, NORMAL, "Some day I'll work for a khan, guide his ships and "
					+ "sleep in his palace. When I think of that, well, the sweeping doesn't seem so bad.");
			stage = 99;
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