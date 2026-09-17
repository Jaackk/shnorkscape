package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class DungeonDifficulty extends Dialogue {

	@Override
	public void start() {
		int partySize = (int) parameters[0];
		String[] options = new String[partySize];
		for (int i = 0; i < options.length; i++)
			options[i] = "" + (i + 1);
		options[partySize / 2] += " (recommended)";
		sendOptionsDialogue("What difficulty of dungeon would you like?", options);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1)
			player.getDungeoneeringManager().setDifficulty(1);
		else if (componentId == OPTION_2)
			player.getDungeoneeringManager().setDifficulty(2);
		else if (componentId == OPTION_3)
			player.getDungeoneeringManager().setDifficulty(3);
		else if (componentId == OPTION_4)
			player.getDungeoneeringManager().setDifficulty(4);
		else if (componentId == OPTION_5)
			player.getDungeoneeringManager().setDifficulty(5);
		end();
		player.getDungeoneeringManager().enterDungeon(true, true, false);
		
	}

	@Override
	public void finish() {

	}

}
