package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.CustomFurClothing;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles the Party Pete dialogue.
 *
 * @author Noel
 */
public final class FancyDressShopOwner extends Dialogue {

	int npcId;
	/**
	 * The required items to make the Eagle costume.
	 */
	private final int[] reqItems = { 10167, 1939, 1765 };
	/**
	 * The required item amounts to make the Eagle costume.
	 */
	private final int[] reqAmount = { 10, 1, 1 };
	/**
	 * Has the player given the NPC the items required. In case the Dialogue
	 * ends before items are given.
	 */
	private boolean itemsGiven;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		if (itemsGiven) {
			player.getInventory().addItem(10171, 1);
			player.getInventory().addItem(10172, 1);
			sendItemDialogue(10171, 1, "Fancy-Dress Shop Owner hands you the Eagle costume.");
			itemsGiven = false;
			stage = 99;
			return;
		}
		sendNPCDialogue(npcId, NORMAL, "Now you look like someone who goes to a lot of " + "fancy dress parties.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(CALM, "Err...what are you saying exactly?");
			stage = 0;
			break;
		case 0:
			sendNPCDialogue(npcId, NORMAL,
					"I'm just saying that perhaps you would like to " + "peruse my selection of garments.");
			stage = 1;
			break;
		case 1:
			sendNPCDialogue(npcId, NORMAL,
					"Or, if that doesn't interest you, then maybe you "
							+ "have something else to offer? I'm always on the lookout for interesting "
							+ "or unusual new materials.");
			stage = 2;
			break;
		case 2:
			sendOptionsDialogue("Select an Option", "Okay, lets see what you've got then.",
					"Could you make another bird costume for me?",
					"I think I might just leave the perusing for now thanks.",
					"Can you make clothing suitable for hunting?",
					"What sort of unusual materials did you have in mind?");
			stage = 3;
			break;
		case 3:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Okay, lets see what you've got then.");
				stage = 4;
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "Could you make another eagle costume for me?");
				stage = 10;
				break;
			case OPTION_3:
				sendPlayerDialogue(CALM, "I think I might just leave the persuing for now, thanks.");
				stage = 99;
				break;
			case OPTION_4:
				sendPlayerDialogue(CALM, "Can you make clothing suitable for hunting?");
				stage = 20;
				break;
			case OPTION_5:
				sendPlayerDialogue(CALM, "What sort of unusual materials did you have in mind?");
				stage = 30;
				break;
			}
			break;
		case 4:
			finish();
			ShopsDataParser.openShop(player, 5);
			break;
		case 10:
			sendNPCDialogue(npcId, NORMAL,
					"Certainly, if you can supply the materials. I'll "
							+ "need appropriate feathers, swamp tar and some yellow dye. It'll also cost "
							+ "you 25 gold pieces.");
			stage = 11;
			break;
		case 11:
			if (player.getInventory().containsItems(reqItems, reqAmount) && player.hasMoney(25)) {
				player.getInventory().deleteItem(reqItems[0], reqAmount[0]);
				player.getInventory().deleteItem(reqItems[1], reqAmount[1]);
				player.getInventory().deleteItem(reqItems[2], reqAmount[2]);
				player.takeMoney(25);
				sendPlayerDialogue(NORMAL, "There you go!");
				itemsGiven = true;
				stage = 12;
				return;
			}
			sendPlayerDialogue(CALM, "I'll see what I can do.");
			stage = 99;
			break;
		case 12:
			player.getInventory().addItem(10171, 1);
			player.getInventory().addItem(10172, 1);
			sendItemDialogue(10171, 1, "Fancy-Dress Shop Owner hands you the Eagle costume.");
			itemsGiven = false;
			stage = 99;
			break;
		case 20:
			sendNPCDialogue(npcId, NORMAL, "Certainly. Take a look at my range of made-to-order items. "
					+ "If you can supply the furs, I'll gladly make any of these for you.");
			stage = 21;
			break;
		case 21:
			finish();
			CustomFurClothing.openInterface(player);
			break;
		case 30:
			sendNPCDialogue(npcId, NORMAL,
					"Well, some more colourful feathers might be useful. For some "
							+ "surreal reason, all I normally seem to get offered are large quantities of rather "
							+ "beaten-up looking chicken feathers.");
			stage = 31;
			break;
		case 31:
			sendNPCDialogue(npcId, WHY,
					"People must have some very strange pastimes around these parts, " + "that's all I can say.");
			stage = 32;
			break;
		case 32:
			sendPlayerDialogue(NORMAL, "Okay, lets see what you've got then.");
			stage = 4;
			break;
		case 99:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}