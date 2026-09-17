package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Lord Iorwerth's dialogue.
 *
 * @author Noel
 */
public class LordIorwerth extends Dialogue {

	/**
	 * Represents the NPC's ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendPlayerDialogue(NORMAL, "Good day, Lord Iorwerth.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendNPCDialogue(npcId, CALM,
					"Ahh Player. Good day, sorry I cannot stop to talk. So much to do, " + "so little time.");
			stage = 0;
			break;
		case 0:
			sendPlayerDialogue(CALM, "Very well, good bye.");
			stage = 1;
			break;
		case 1:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}
}