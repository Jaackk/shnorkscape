package com.rs.game.player.dialogue.impl;

import com.rs.game.activities.instances.Instance;
import com.rs.game.player.dialogue.Dialogue;

public class InstanceJoiningD extends Dialogue {

	private Instance normal, hard;
	
	@Override
	public void start() {
		normal = (Instance) parameters[0];
		hard = (Instance) parameters[1];
		sendOptionsDialogue("This user currently has two active instances to this boss.<br>Select which you'd like to join.",
				"Normal mode.",
				"Hard mode.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_1) 
			normal.enterInstance(player);
		else
			hard.enterInstance(player);
	}

	@Override
	public void finish() {

	}

}
