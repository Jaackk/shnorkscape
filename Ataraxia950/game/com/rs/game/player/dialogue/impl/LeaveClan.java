package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.dialogue.Dialogue;

public class LeaveClan extends Dialogue {

	@Override
	public void start() {
		sendDialogue("If you leave the current clan, you will not be able to join back, "
				+ "are you sure you wan't to leave the clan you're currently in?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			stage = 0;
			sendOptionsDialogue("Select an Option", "Yes, leave my current clan.", "No, stay in my current clan.");
			break;
		case 0:
			if (componentId == OPTION_1) {
				ClansManager.leaveClanCompletly(player);
				end();
			} else if (componentId == OPTION_2) {
				player.getPackets().sendGameMessage("You decide to stay in the clan you're currently in.");
				end();
			}
			break;
		default:
			end();
			break;
		}

	}

	@Override
	public void finish() {
	}
}