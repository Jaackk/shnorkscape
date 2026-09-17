package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

public class HomeTeleport extends Dialogue {

	@Override
	public void finish() {
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(5414, 2339, 0));
				end();
				return;
			}
			if (componentId == OPTION_2) {
				player.getInterfaceManager().sendInterface(1092);
				end();
				return;
			}
		}
	}

	@Override
	public void start() {
		sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Home Teleport", "Lodestone network");
	}
}