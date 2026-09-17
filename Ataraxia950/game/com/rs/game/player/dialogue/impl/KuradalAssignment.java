package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

//import com.rs.game.player.content.SlayerTask;
//import com.rs.game.player.content.SlayerTask.Master;

/**
 * Used to handle the Assignment button.
 *
 * @author Noel
 */
public class KuradalAssignment extends Dialogue {

	int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		if (player.getTask() == null)
			player.getDialogueManager().startDialogue("Kuradal", npcId);
		else {
			if (!player.hasItem(new Item(4155))) {
				sendNPCDialogue(npcId, 9827, "It seems you don't have a Slayer gem.");
				stage = 80;
			} else
				sendPlayerDialogue(NORMAL, "Could you reset my task for me?");
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 80:
			sendItemDialogue(4155, 1, "Kuradal has given you a Slayer gem.");
			player.addItem(new Item(4155));
			stage = 81;
			break;
		case 81:
			sendPlayerDialogue(NORMAL, "Could you reset my task for me?");
			stage = -1;
			break;
		case -1:
			sendNPCDialogue(npcId, CALM, "Certainly. That'll cost you "
					+ ((player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) || Settings.DEBUG) ? 0 : 10) + " Slayer points.");
			stage = 0;
			break;
		case 0:
			sendOptionsDialogue(Colors.RED + "Reset Slayer task?", "Yes, please.", "No, thank you.");
			stage = 1;
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Yes, please, reset my task.");
				stage = 2;
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "No, thank you, I'll keep my current task.");
				stage = 5;
				break;
			}
			break;
		case 2:
			if (player.getSlayerPoints() < ((player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) || Settings.DEBUG) ? 0 : 10)) {
				sendNPCDialogue(npcId, CALM, "You don't have enough points, come back later when you do.");
				stage = 6;
				return;
			}
			// SlayerTask.random(player, Master.KURADAL, true);
			player.setTask(null);
			player.setSlayerPoints(
					player.getSlayerPoints() - ((player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) || Settings.DEBUG) ? 0 : 10));
			sendNPCDialogue(npcId, NORMAL, "Excellent, you're doing great. Talk to me for a new task.");
			stage = 6;
			break;
		case 5:
			sendNPCDialogue(npcId, CALM, "As you wish.");
			stage = 6;
			break;
		case 6:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}