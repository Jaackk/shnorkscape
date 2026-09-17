package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class FightKilnDialogue extends Dialogue {

	@Override
	public void finish() {
		player.getControlerManager().startControler("FightKilnControler", 0);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void start() {
		player.lock();
		sendDialogue("You journey directly to the Kiln.");
	}
}
