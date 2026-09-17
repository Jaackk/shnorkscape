package com.rs.game.player.dialogue.impl;

import com.rs.game.player.controllers.RunespanController;
import com.rs.game.player.dialogue.Dialogue;

public class RuneSpanLeaving extends Dialogue {

	@Override
	public void finish() {

	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1) {
			sendOptionsDialogue("Teleport to the Wizards' Tower?", "Yes", "No");
			stage = 2;
		} else if (stage == 2) {
			if (componentId == OPTION_1) {
				if (player.getControlerManager().getControler() instanceof RunespanController)
					((RunespanController) player.getControlerManager().getControler()).leaveRunespan();
			}
			end();
		}
	}

	@Override
	public void start() {
		sendDialogue("All your runes will be converted into points when you leave.");
		stage = 1;
	}
}
