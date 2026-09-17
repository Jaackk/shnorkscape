package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * Handles the Banker dialogue.
 *
 * @author Matrix, updated by Noel.
 */
public class Banker extends Dialogue {

	int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, 9827, "Good day, How may I help you?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (player.isNative950()) {
			// Reuse the banker's dialogue stages and Bank implementation, exposing
			// only services whose native presentation and handlers are available.
			if (stage == -1) {
				stage = 0;
				sendOptionsDialogue("What would you like to say?",
						"I'd like to access my bank account, please.", "Never mind.");
			} else if (stage == 0 && (componentId == OPTION_1 || componentId == OPTION_2)) {
				end();
				if (componentId == OPTION_1) player.getBank().openBank();
			}
			return;
		}
		switch(stage) {
		case -1:
			stage = 0;
			sendOptionsDialogue("What would you like to say?", "I'd like to access my bank account, please.",
					"I'd like to see my collection box.", Colors.GREEN+"Bank settings",
					"Nevermind");
			break;
		case 0:
			finish();
			if(componentId == OPTION_4)
				break;
			switch(componentId) {
			case OPTION_1:
				if (!player.promptList())
					player.getBank().openBank();
				else
					player.getDialogueManager().startDialogue("BankList", false);
				break;
			case OPTION_2:
				player.getGEManager().openCollectionBox();
				break;
			case OPTION_3:
				player.getDialogueManager().startDialogue("BankList", true);
				break;
			}
			break;
		}	
	}

	@Override
	public void finish() { player.getInterfaceManager().closeChatBoxInterface(); }

}
