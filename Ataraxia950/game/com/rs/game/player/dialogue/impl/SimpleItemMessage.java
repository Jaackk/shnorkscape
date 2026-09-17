package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class SimpleItemMessage extends Dialogue {

	private int itemId, amount;
	private String message;

	@Override
	public void start() {
		itemId = (Integer) parameters[0];
		amount = (Integer) parameters[1];
		message = (String) parameters[2];
		sendItemDialogue(itemId, amount, message);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {
	}

}