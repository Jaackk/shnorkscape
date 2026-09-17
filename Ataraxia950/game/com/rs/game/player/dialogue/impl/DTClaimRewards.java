package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class DTClaimRewards extends Dialogue {

	@Override
	public void start() {
		sendDialogue("You have a Dominion Factor of " + player.getDominionTower().getDominionFactor() + ".");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendDialogue(Colors.RED + "WARNING</col>: If you claim your rewards your progress will be reset.");
		} else if (stage == 0) {
			end();
			player.getDominionTower().openRewards();
		}
	}

	@Override
	public void finish() {
	}
}