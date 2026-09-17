package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class VoragoSignpostD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("Adventurers beware! Only those who have no fear of death should continue.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			stage = (byte) (player.isCanStartHardModeVorago(false) ? 1 : 0);
			sendDialogue("Hardmode unlock state: "
					+ (player.isCanStartHardModeVorago(false) ? "<col=00FF00>Unlocked" : "<col=FF0000>locked") + ".");
			break;
		case 0:
			player.isCanStartHardModeVorago(true);
			end();
			break;
		case 1:
			end();
			break;
		}
	}

	@Override
	public void finish() {

	}

}
