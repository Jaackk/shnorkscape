package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.CustomFurClothing;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles Fancy-Dress shop owner's dialogue.
 *
 * @author Noel
 */
public class FancyDressShopOwnerD extends Dialogue {

	/**
	 * The NPC ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Can I help you?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Yes, I'd like to see your shop.",
					"Could you exchange these hides for clothing?", "No, thank you.");
			stage = 0;
			break;
		case 0:
			if (componentId == OPTION_1) {
				sendPlayerDialogue(NORMAL, "Yes, I'd like to see your shop.");
				stage = 1;
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(NORMAL, "Could you exchange these hides for clothing?");
				stage = 20;
			}
			if (componentId == OPTION_3) {
				sendPlayerDialogue(NORMAL, "No, I prefer to bash things close up.");
				stage = 30;
			}
			break;
		case 1:
			ShopsDataParser.openShop(player, 5);
			finish();
			break;
		case 2:
			CustomFurClothing.openInterface(player);
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}