package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class SimpleMessage extends Dialogue {

	@Override
	public void finish() {

	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void start() {
		String[] messages = new String[parameters.length];
		for (int i = 0; i < messages.length; i++)
			messages[i] = (String) parameters[i];
		sendDialogue(messages);
	}

}
