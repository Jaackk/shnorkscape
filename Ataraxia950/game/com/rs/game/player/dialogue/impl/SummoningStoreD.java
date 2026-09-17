package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class SummoningStoreD extends Dialogue {

	/**
	 * Handles Pikkustix's dialogue.
	 *
	 * @author Noel
	 */
	@Override
	public void start() {
		sendOptionsDialogue("Which Shop to Open", "Basic Ingredients shop", "Starter Ingredients shop",
				"Intermediate Ingredients shop", "Novice Ingredients shop");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				ShopsDataParser.openShop(player, 9);
				finish();
				break;
			case OPTION_2:
				ShopsDataParser.openShop(player, 10);
				finish();
				break;
			case OPTION_3:
				ShopsDataParser.openShop(player, 11);
				finish();
				break;
			case OPTION_4:
				ShopsDataParser.openShop(player, 12);
				finish();
				break;
			default:
				finish();
				break;
			}
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}