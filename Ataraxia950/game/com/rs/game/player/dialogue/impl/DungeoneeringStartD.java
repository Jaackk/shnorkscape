package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class DungeoneeringStartD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Would you like to start the dungeon?", "Yes.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) {
			if (player.getDungeoneeringManager().canEnter()) {
				player.getDungeoneeringManager().enterDungeon(false, true, true);
			}
		}
		end();
	}

	@Override
	public void finish() {
		
	}

}
