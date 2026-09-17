package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Class used to handle the Xuan's dialogue.
 *
 * @author Noel
 */
public class XuanD extends Dialogue {

	/**
	 * Ints representing the NPC id and the stage of the dialogue.
	 */
	int npcId, stage;
	public static final boolean DISABLE_LOYALTY_SHOP = false;
	
	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		stage = (Integer) parameters[1];
		if(DISABLE_LOYALTY_SHOP) {
		 stage = 50;
		sendNPCDialogue(npcId, NORMAL, "I am no longer in the auras business, I hear you can buy them from the aura management interface in equipment tab.");
		return;
		}
		if (stage == 1) {
			sendNPCDialogue(npcId, NORMAL, "Hello, what can I do for you?");
			stage = 1;
		} else {
			sendOptionsDialogue("Choose an Option", "Combat Aura Shop", "Skilling Aura Shop", "$50+ Aura Shop",
					"$100+ Aura Shop");
			stage = 4;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 1:
			sendPlayerDialogue(NORMAL, "Hello.. what could you do for me..?");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, NORMAL,
					"I offer a wide variety of Auras that you can use to help your " + "game-play throughout "
							+ Settings.SERVER_NAME + ". Donators also have access to " + "higher-tier auras.");
			stage = 3;
			break;
		case 3:
			sendOptionsDialogue("Choose an Option", "Combat Aura Shop", "Skilling Aura Shop", "$50+ Aura shop",
					"$100+ Aura shop");
			stage = 4;
			break;
		case 4:
			switch (componentId) {
			case OPTION_1:
				finish();
				ShopsDataParser.openShop(player, 53);
				break;
			case OPTION_2:
				finish();
				ShopsDataParser.openShop(player, 54);
				break;
			case OPTION_3:
				if (!player.isExtremeDonator()) {
				sendNPCDialogue(npcId, SAD,
						"I'm sorry, but these auras are only available to Silver members and higher..");
				stage = 3;
				return;
			}
				finish();
				ShopsDataParser.openShop(player, 55);
				break;
			case OPTION_4:
				if (!player.isLegendaryDonator()) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but these auras are only available to Gold members and higher..");
					stage = 3;
					return;
				}
				finish();
				ShopsDataParser.openShop(player, 56);
				break;
			}
			break;
		case 50:
		    end();
		    break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}