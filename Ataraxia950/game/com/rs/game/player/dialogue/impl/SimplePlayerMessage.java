package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class SimplePlayerMessage extends Dialogue {

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void start() {
		String[] messages = new String[parameters.length];
		for (int i = 0; i < messages.length; i++)
			messages[i] = (String) parameters[i];
		sendPlayerDialogue(CALM, messages);
	}

	@Override
	public void finish() {
	}

}