package com.rs.game.player.dialogue.impl.npccontact;

import com.rs.game.player.dialogue.Dialogue;

public class MurphyD extends Dialogue {

	private static final int NPC = 466;
	
	@Override
	public void start() {
		sendNPCDialogue(NPC, NORMAL, "Hello there.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			sendPlayerDialogue(NORMAL, "Hello, Murphy.");
			break;
		case 0:
			sendNPCDialogue(NPC, NORMAL, "How are you today?");
			break;
		case 1:
			sendPlayerDialogue(NORMAL, "I'm doing fine. How's fishing trawler going?");
			break;
		case 2:
			sendNPCDialogue(NPC, NORMAL, "It's going well.");
			break;
		case 3:
			end();
			break;
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
