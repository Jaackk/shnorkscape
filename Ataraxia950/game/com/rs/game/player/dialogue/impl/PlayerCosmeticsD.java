package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.player.dialogue.Dialogue;

public final class PlayerCosmeticsD extends Dialogue {

	@Override
	public void start() {
	sendOptionsDialogue("Select an option", "Open cosmetics interface", "Talk to solomon", "Open cosmetics store");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			if(componentId == OPTION_1){
			    end();
			    player.getInterfaceManager().openMenu(1, 2);
				return;
			} else if(componentId == OPTION_2){
				player.getDialogueManager().startDialogue("SolomonD",18808);
			} else if(componentId == OPTION_3)
				player.getPackets().sendOpenURL(Settings.DONATE + "/?page=1&category=4");
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}