package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class ClanMotto extends Dialogue {

	@Override
	public void start() {
		player.getInterfaceManager().sendChatBoxInterface(1103);
		player.getTemporaryAttributtes().put("SetClanMotto", Boolean.TRUE);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();

	}

	@Override
	public void finish() {

	}

}
