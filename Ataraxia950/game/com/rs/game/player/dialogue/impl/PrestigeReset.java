package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class PrestigeReset extends Dialogue {

	@Override
	public void start() {
		sendDialogue("Are you sure you want to reset your dungeon progress? Your previous progress will be set to the number of floors you have completed and all floors will be marked as incomplete. This cannot be undone.");
	}

	@Override
	public void run(int interfaceId, int componentId) {

		if (stage == -1) {
			sendOptionsDialogue("Select an Option", "Yes, reset my progress.", "No, don't reset my progress.");
			stage = 0;
		} else if (stage == 0) {
			if (componentId == OPTION_1)
				player.getDungeoneeringManager().resetProgress();
			end();
		}
	}

	@Override
	public void finish() {

	}

}
