package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.DungeonRewardShop;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Used to handle the Dungeoneering rewards trader dialogue.
 *
 * @author Noel
 */
public final class RewardsTraderD extends Dialogue {

	private int npcId, option;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		option = (Integer) parameters[1];
		if (option == 3) {
			sendNPCDialogue(npcId, SAD, "Oh, I'm sorry.. You'll have to recharge your items " + "by talking to Bob.");
			stage = 30;
			return;
		}
		sendNPCDialogue(npcId, CALM, "Oh, hello, I didn't see...");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(CALM, "Hey. I was wondering if you could help me?");
			stage = 0;
			break;
		case 0:
			sendNPCDialogue(npcId, CALM, "Help? Uh...I'm not sure that I can...uh...");
			stage = 1;
			break;
		case 1:
			sendOptionsDialogue("What would you like to say?", "Who are you?", "I'm done with you.");
			stage = 2;
			break;
		case 2:
			if (componentId == OPTION_1) {
				sendPlayerDialogue(CALM, "Who are you?");
				stage = 3;
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(ANGRY, "I'm done with you.");
				stage = 51;
			}
			break;
		case 3:
			sendNPCDialogue(npcId, CALM, "I'm..I used to be...");
			stage = 4;
			break;
		case 4:
			sendPlayerDialogue(CALM, "Yes?");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, CALM, "I just handle the...uh..equipment.");
			stage = 6;
			break;
		case 6:
			sendPlayerDialogue(CALM, "I mean, who are you? Like, what is your name?");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(npcId, CALM, "A name? I..uh....");
			stage = 8;
			break;
		case 8:
			sendOptionsDialogue("What would you like to say?", "You are driving me crazy.",
					"You mentioned something about equipment?", "I'm done with you.");
			stage = 9;
			break;
		case 9:
			if (componentId == OPTION_1) {
				sendPlayerDialogue(ANGRY, "You are driving me crazy.");
				stage = 10;
			}
			if (componentId == OPTION_2) {
				sendNPCDialogue(npcId, CALM, "You mentioned something about equipment?");
				stage = 60;
			}
			if (componentId == OPTION_3) {
				sendPlayerDialogue(ANGRY, "I'm done with you.");
				stage = 51;
			}
			break;
		case 10:
			sendNPCDialogue(npcId, SAD, "I'm sorry. I'm...");
			stage = 11;
			break;
		case 11:
			sendPlayerDialogue(ANGRY, "Yes?");
			stage = 12;
			break;
		case 12:
			sendNPCDialogue(SAD, CALM, "They called me Ma...");
			stage = 13;
			break;
		case 13:
			sendPlayerDialogue(ANGRY, "Malcom? Mandrake? Ma..um..Mango?");
			stage = 14;
			break;
		case 14:
			sendNPCDialogue(SAD, CALM, "Uh, no, those aren't quite right.");
			stage = 15;
			break;
		case 15:
			sendOptionsDialogue("What would you like to say?", "You mentioned something about equipment?",
					"I'm done with you.");
			stage = 16;
			break;
		case 16:
			if (componentId == OPTION_1) {
				sendNPCDialogue(npcId, CALM, "You mentioned something about equipment?");
				stage = 60;
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(ANGRY, "I'm done with you.");
				stage = 51;
			}
			break;
		case 30:
			sendPlayerDialogue(CALM, "I see, I see.. alright, thank you, see you later!");
			stage = 51;
			break;
		case 51:
			sendNPCDialogue(npcId, SAD, "Fine, fine, fine. I'll be here.");
			stage = 99;
			break;
		case 60:
			sendNPCDialogue(npcId, SAD,
					"Yes..uh..I did, you're quite right. I sell things, mend things. " + "What interests you?");
			stage = 61;
			break;
		case 61:
			sendOptionsDialogue("What would you like to say?", "Let's see what you sell, then.",
					"What exactly can you mend?", "I'm done with you.");
			stage = 62;
			break;
		case 62:
			if (componentId == OPTION_1) {
				player.getInterfaceManager().closeChatBoxInterface();
				DungeonRewardShop.openRewardShop(player);
			}
			if (componentId == OPTION_2) {
				sendPlayerDialogue(CALM, "What exactly can you mend?");
				stage = 63;
			}
			if (componentId == OPTION_3) {
				sendPlayerDialogue(ANGRY, "I'm done with you.");
				stage = 51;
			}
			break;
		case 63:
			sendNPCDialogue(SAD, CALM,
					"Weapons, I guess. I became good at mending in..there. If you buy something "
							+ "from me, I should be able to repair it if it's..uh...degraded. Shot it to me and I'll see what "
							+ "can be done.");
			stage = 64;
			break;
		case 64:
			sendNPCDialogue(SAD, CALM,
					"I feel..like I can trust yo. Let me tell you something: I have a friend in.."
							+ "there, someone who brings me tools and items from that place. That is how I can offer you services "
							+ "that others...can't. Don't tell anyone. Please.");
			stage = 99;
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