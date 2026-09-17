package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

public class QbdPortal extends Dialogue {

	@Override
	public void finish() {

	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (componentId) {
		case OPTION_1:
			Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(1195, 6499, 0));
			end();
			break;
		case OPTION_2:
			Magic.sendNormalTeleportSpell(player, 0, 0, player.getHomeTile());
			end();
			break;
		}
	}

	@Override
	public void start() {
		sendOptionsDialogue("Teleport to:", "Underground Portal.", "Surface.");
	}
}