package com.rs.game.player.dialogue.impl.npccontact;

import com.rs.game.player.dialogue.Dialogue;

public class OneiromancerD extends Dialogue {

	private static final int NPC = 4511;
	
	@Override
	public void start() {
		sendNPCDialogue(NPC, NORMAL, "Hello.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			sendPlayerDialogue(NORMAL, "Hello. How's life back at moon clan?");
			break;
		case 0:
			sendNPCDialogue(NPC, NORMAL, "It's going good. Practising and learning new spells every day.");
			break;
		case 1:
			sendPlayerDialogue(NORMAL, "That's great to hear.");
			break;
		case 2:
			end();
			break;
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
