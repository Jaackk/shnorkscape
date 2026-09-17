package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.construction.House;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputNameEvent;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * @author Jamal
 */

public class HousePortal extends Dialogue {

	@Override
	public void start() {
		stage = 1;
		sendOptionsDialogue("What would you like to do?",
				"Enter Your House (" + (player.getHouse().isBuildMode() ? "Build" : "Public") + " mode)",
				"Enter Friend's House", "Construction Shop");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1) {
			if (componentId == OPTION_1) {
				if (player.hasHouse) {
					finish();
					player.getHouse().enterMyHouse();
				} else {
					sendDialogue("You must first purchase a house from the Estate Agent.");
					stage = 99;
				}
			} else if (componentId == OPTION_2) {
				player.sendInputName("Please enter the name of the player:", new InputNameEvent() {
					@Override
					public void run(Player player) {
						House.enterHouse(player, getString());
					}
				});
				end();
			} else if (componentId == OPTION_3) {
				finish();
				ShopsDataParser.openShop(player, 19);
			}
		} else if (stage == 99) {
			player.getInterfaceManager().closeChatBoxInterface();
			end();
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}