package com.rs.game.player.dialogue.impl;

import com.rs.game.activities.dfm.DemonFlashMobs;
import com.rs.game.player.dialogue.Dialogue;

public class DemonFlashScrollD extends Dialogue {

	int titleIndex;
	int itemId;
	@Override
	public void start() {
		titleIndex = (int) parameters[0];
		this.itemId = (int) parameters[1];
		String title = (String) DemonFlashMobs.TITLES[titleIndex][0];
		boolean afterName = (boolean) DemonFlashMobs.TITLES[titleIndex][1];
		sendDialogue("By reading this scroll, you will unlock a new title " + (afterName ? ("'" + player.getDisplayName() + title + "</col>'") : ("'" + title + "</col>" + player.getDisplayName() + "'")) + ". You will however lose the scroll in the process. Are you sure you wish to read it?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			sendOptionsDialogue("Read the scroll?", "Read it.", "Keep it.");
			break;
		case 0:
			if (componentId == OPTION_1) {
				if (player.getInventory().containsItem(itemId, 1)) {
					player.DFMScroll[titleIndex] = true;
					player.sendMessage("You've unlocked a new title! You can select it in the titles manager.");
					player.getInventory().deleteItem(itemId, 1);
				}
			}
			end();
			break;
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
