package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Guard's dialogue.
 *
 * @author Noel
 */
public class GuardD extends Dialogue {

	/**
	 * The NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, ANGRY, "You better not be stealing anything from here.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(SCARED, "No, Sir.. Mr.. Guard.. Sir!");
			stage = 0;
			break;
		case 0:
			sendNPCDialogue(npcId, GLANCE_DOWN, "I'm keeping an eye out for you.");
			stage = 1;
			break;
		case 1:
			sendPlayerDialogue(GLANCE_DOWN, "Why...");
			stage = 0;
			break;
		case 2:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}
