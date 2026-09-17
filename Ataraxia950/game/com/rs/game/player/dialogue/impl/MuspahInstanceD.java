package com.rs.game.player.dialogue.impl;

import com.rs.game.activities.instances.Instance;
import com.rs.game.activities.instances.MuspahInstance;
import com.rs.game.player.dialogue.Dialogue;

public class MuspahInstanceD extends Dialogue {
	
	@Override
	public void start() {
		
		sendOptionsDialogue("Select an Option", "Fight the Muspahs", "Nothing");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) {
			Instance instance = new MuspahInstance(player, 600, 5, -1, -1, 19150, false);
			instance.constructInstance();
		}
		end();
	}

	@Override
	public void finish() {
		
	}

}
