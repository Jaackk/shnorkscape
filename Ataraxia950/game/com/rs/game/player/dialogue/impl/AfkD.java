package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AfkD extends Dialogue {

	@Override
	public void start() {
		String timeLeft = (String) parameters[0];
		sendDialogue(Colors.RED + "You will be sent to evil bob in " + timeLeft + " for inactivity.");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}