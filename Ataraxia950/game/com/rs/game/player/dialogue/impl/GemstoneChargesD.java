package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class GemstoneChargesD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("Dragonstone charges: " + player.getGemstoneArmour().getCharges(0) + "<br>"
				+ "Onyx charges: " + player.getGemstoneArmour().getCharges(1) + "<br>"
						+ "Hydrix charges: " + player.getGemstoneArmour().getCharges(2));
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {}

}
