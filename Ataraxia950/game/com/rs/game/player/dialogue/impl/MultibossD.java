package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.dialogue.Dialogue;

public class MultibossD extends Dialogue {

	int dialogueId;

	@Override
	public void start() {
		dialogueId = (Integer) parameters[0];
		if (dialogueId == 0) { // enter the area
			sendOptionsDialogue("Would you like to enter the instance?", "Yes", "No");
			stage = 0;
		} else if (dialogueId == 1) { // leave the area
			sendNPCDialogue(2641, CALM, "Do you really want to exit the area? This will put you back in the lobby!");
			stage = 1;
		}

	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (dialogueId <= 1) {
			switch (stage) {
			case 0: // enter the area
				if (componentId == OPTION_1)
					player.setNextWorldTile(new WorldTile(player.getX() - 2, player.getY(), player.getPlane()));
				finish();
				break;
			case 1: // leave the area
				sendOptionsDialogue("Do you want to leave?", "Yes", "No");
				stage = 2;
				break;
			case 2:
				if (componentId == OPTION_1) {
					// leave
					break;
				}
				finish();
				break;
			}
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}
