package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class SimpleNPCMessageNoContinue extends Dialogue {

	private int npcId;

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		String[] messages = new String[parameters.length - 1];
		for (int i = 0; i < messages.length; i++)
			messages[i] = (String) parameters[i + 1];
		sendNPCDialogueNoContinue(player, npcId, 9827, messages);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				closeNoContinueDialogue(player);
				end();
			}
		}, 15);
	}

	@Override
	public void finish() {
	}

}