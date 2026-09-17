package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class TelescopeD extends Dialogue {

	@Override
	public void start() {
		player.getInterfaceManager().sendInterface(782);
		String[] messages = new String[parameters.length];
		for (int i = 0; i < messages.length; i++)
			messages[i] = (String) parameters[i];
		sendDialogue(messages);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.closeInterfaces();
		end();
	}

	@Override
	public void finish() {
	}

}