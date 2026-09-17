package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

public class ToolLeprechaunTeleD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Choose a Destination", "North of Catherby", "South of Falador", "North of Ardougne",
				"West of Port Phasmatys");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) // north of catherby
				Magic.compCapeTeleport(player, 2807, 3463, 0);
			if (componentId == OPTION_2) // south of falador
				Magic.compCapeTeleport(player, 3052, 3303, 0);
			if (componentId == OPTION_3) // north of ardougne
				Magic.compCapeTeleport(player, 2664, 3374, 0);
			if (componentId == OPTION_4) // west of port phasmatys
				Magic.compCapeTeleport(player, 3599, 3523, 0);
			end();
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}