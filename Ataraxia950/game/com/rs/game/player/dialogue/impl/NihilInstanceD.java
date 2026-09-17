package com.rs.game.player.dialogue.impl;

import com.rs.game.activities.instances.Instance;
import com.rs.game.activities.instances.NihilInstance;
import com.rs.game.player.dialogue.Dialogue;

public class NihilInstanceD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Select an Option", "Fight the Nihils", "Nothing");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) {
			Instance instance = new NihilInstance(player, 600, 5, -1, -1, 19146, false);
			instance.constructInstance();
		}
		end();
	}

	@Override
	public void finish() {
		
	}

}
