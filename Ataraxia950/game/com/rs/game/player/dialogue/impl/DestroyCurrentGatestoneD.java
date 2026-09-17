package com.rs.game.player.dialogue.impl;

import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.dialogue.Dialogue;

public class DestroyCurrentGatestoneD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Are you sure you'd like to destroy your current gatestone?", "Yes, I'm sure.", "No, keep it.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) {
			if (player.getControlerManager().getControler() instanceof DungeonController)
				((DungeonController) player.getControlerManager().getControler()).createGatestone(true);
		}
		end();
	}

	@Override
	public void finish() {
		
	}

}
