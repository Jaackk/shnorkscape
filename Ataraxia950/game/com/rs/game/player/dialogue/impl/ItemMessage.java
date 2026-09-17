package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class ItemMessage extends Dialogue {

	@Override
	public void start() {
	    end();
	    player.getDialogueManager().startDialogue("SimpleItemMessage", parameters[1], 1 , parameters[0]);

	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {
	}
}