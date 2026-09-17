package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class KillmeWarning extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Are you sure you wish to suicide?<br>You can hide this warning by using ;;togglekillme command.", "Yes, I know the consequences.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) {
			player.killme();
		}
		end();
	}

	@Override
	public void finish() {
		
	}

}
