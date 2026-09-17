package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class AngofD extends Dialogue {


	@Override
	public void start() {
			sendNPCDialogue(21633, NORMAL, "Hello, " + player.getDisplayName()
					+ ", are you interested in buying crystal armour?");
			stage = 1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage){
		case 1:
			sendOptionsDialogue("Select an option", "Yes.", "No.");
			stage = 2;
			break;
		case 2:
			switch (componentId){
			case OPTION_1:
				ShopsDataParser.openShop(player, 65);
				end();
				break;
			case OPTION_2:
				end();
				break;
			}
			break;
		}

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
