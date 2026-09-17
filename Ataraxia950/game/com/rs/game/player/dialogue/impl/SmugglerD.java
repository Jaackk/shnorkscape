package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.dungeoneering.DungeonConstants;
import com.rs.game.player.content.dungeoneering.DungeonResourceShop;
import com.rs.game.player.dialogue.Dialogue;

public class SmugglerD extends Dialogue {

	@Override
	public void start() {
		sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL, "Hail, " + player.getDisplayName() + ". Need something?");
		stage = -1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			sendOptionsDialogue(
					"Select an Option",
					"What can you tell me about this place?",
					"Who are you?",
					"Do I have any rewards to claim?",
					"I'm here to trade."
			);
			stage = 0;
			return;
		}

		if (stage == 0) {
			if (componentId == OPTION_1) {
				sendPlayerDialogue(NORMAL, "What can you tell me about this place?");
				stage = 1;
			} else if (componentId == OPTION_2) {
				sendPlayerDialogue(NORMAL, "Who are you?");
				stage = 2;
			} else if (componentId == OPTION_3) {
				sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL, "I have no rewards for you at the moment.");
				stage = 100;
			} else if (componentId == OPTION_4) {
				sendPlayerDialogue(NORMAL, "I'm here to trade.");
				stage = 23;
			}
			return;
		}

		if (stage == 1) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL,
					"You know all that I can teach you already, friend, having conquered many floors yourself.");
			stage = 100;
			return;
		}

		if (stage == 2) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL, "A friend.");
			stage = 3;
			return;
		}

		if (stage == 3) {
			sendPlayerDialogue(NORMAL, "Okay, what are you doing here, friend?");
			stage = 4;
			return;
		}

		if (stage == 4) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL, "I'm here to help out.");
			stage = 5;
			return;
		}

		if (stage == 5) {
			sendPlayerDialogue(NORMAL, "With what?");
			stage = 6;
			return;
		}

		if (stage == 6) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL,
					"Well, let's say you find yourself in need of an adventuring kit, and you've a heavy pile of rusty coins weighing you down. I can help you with both those problems. Savvy?");
			stage = 7;
			return;
		}

		if (stage == 7) {
			sendPlayerDialogue(NORMAL, "Ah, so you're a trader?");
			stage = 8;
			return;
		}

		if (stage == 8) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL, "Keep it down, you fool!");
			stage = 9;
			return;
		}

		if (stage == 9) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL, "Yes, I'm a trader. But I'm not supposed to be trading here.");
			stage = 10;
			return;
		}

		if (stage == 10) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL,
					"If you want my goods, you'll learn not to talk about me.");
			stage = 11;
			return;
		}

		if (stage == 11) {
			sendPlayerDialogue(NORMAL, "Right, got you.");
			stage = 12;
			return;
		}

		if (stage == 12) {
			sendPlayerDialogue(NORMAL, "Is there anything else you can do for me?");
			stage = 13;
			return;
		}

		if (stage == 13) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL, "Well, there's the job I'm supposed to be doing down here.");
			stage = 14;
			return;
		}

		if (stage == 14) {
			sendPlayerDialogue(NORMAL, "Which is?");
			stage = 15;
			return;
		}

		if (stage == 15) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL,
					"Say you chance upon an object that you know little about. Show it to me, and I'll tell you what it's used for.");
			stage = 16;
			return;
		}

		if (stage == 16) {
			sendPlayerDialogue(NORMAL, "That's good to know.");
			stage = 17;
			return;
		}

		if (stage == 17) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL,
					"I can also offer you knowledge about the behaviour of powerful opponents you might meet in the area. I've spent a long time down here, observing them.");
			stage = 18;
			return;
		}

		if (stage == 18) {
			sendPlayerDialogue(NORMAL, "I'll be sure to come back if I find a particularly strong opponent, then.");
			stage = 19;
			return;
		}

		if (stage == 19) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL, "You'd be wise to, " + player.getDisplayName() + ".");
			stage = 20;
			return;
		}

		if (stage == 20) {
			sendPlayerDialogue(NORMAL, "How do you know my name?");
			stage = 21;
			return;
		}

		if (stage == 21) {
			sendNPCDialogue(DungeonConstants.SMUGGLER, NORMAL,
					"Nothing gets in or out of Daemonheim without me knowing about it.");
			stage = 22;
			return;
		}

		if (stage == 22) {
			sendPlayerDialogue(NORMAL, "Fair enough.");
			stage = 100;
			return;
		}

		if (stage == 23) {
			DungeonResourceShop.openResourceShop(player, getShopComplexity());
			end();
			return;
		}

		if (stage == 100) {
			end();
		}
	}

	@Override
	public void finish() {
		// no-op
	}

	private int getShopComplexity() {
		if (parameters != null && parameters.length > 0 && parameters[0] instanceof Number) {
			return ((Number) parameters[0]).intValue();
		}
		if (player.getDungeoneeringManager().getParty() != null && player.getDungeoneeringManager().getParty().getComplexity() > 0) {
			return player.getDungeoneeringManager().getParty().getComplexity();
		}
		return 6;
	}
}
