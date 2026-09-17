package com.rs.game.player.dialogue.impl;

import com.rs.game.npc.NPC;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Dec 19, 2018.
 */
public class Dicer extends Dialogue {

	@Override
	public void start() {
		sendNPCDialogue(340, GOOFY_LAUGH, "Ggghbbbbhggrgrrrrrrrr!!!!!!!!!!!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		stage = 0;
		if (stage == 0) {
			sendOptionsDialogue("What would you like to do?", "Dice", "Check Active Dices", "Clear Dices");
			stage = 1;
		}

		if (stage == 1) {
			if (componentId == OPTION_1) {
				end();
				player.dicingManager.addDicing((NPC) parameters[0]);
			} else if (componentId == OPTION_2) {
				end();
				player.dicingManager.openCurrentActiveDicings();
			} else if (componentId == OPTION_3) {
				end();
				player.dicingManager.clearActiveDicings();
			}
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
