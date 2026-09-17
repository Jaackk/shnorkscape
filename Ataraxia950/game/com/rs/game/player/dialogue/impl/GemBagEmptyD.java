package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class GemBagEmptyD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("Are you sure you wish to empty the contents of the gem bag? This will permanently destroy all held gems.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1)
			sendOptionsDialogue("Empty the gem bag?", "Empty it.", "Keep the gems.");
		else if (stage == 0) {
			if (componentId == OPTION_1)
				player.getGemBag().empty();
			end();
		}
		
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
