package com.rs.game.player.dialogue.impl.heartofgielinor;

import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.dialogue.Dialogue;

public class VindictasChosen extends Dialogue {

	private static final int NPC = 22436;
	private int option;
	
	@Override
	public void start() {
		option = (int) parameters[0];
		if (option == 2) {
			sendPlayerDialogue(NORMAL, "What is your opinion of me?");
			stage = 10;
			return;
		} else if (option == 3) {
			sendPlayerDialogue(NORMAL, "I'd like to reclaim my rewards.");
			stage = 40;
			return;
		} else
			sendNPCDialogue(NPC, NORMAL, "Greetings.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			if (player.getInventory().containsItem(36918, 1)) {
				sendNPCDialogue(NPC, NORMAL,  "That's an interesting Anima crystal you have there. "
						+ "Would you be willing to donate it?");
				stage = 50;
				return;
			} else
				sendOptionsDialogue("Select an Option",
						"What's going on here?",
						"What is your opinion of me?",
						"How can I improve my reputation?",
						"Where can I get armour like that?",
						"I'd like to reclaim my rewards.");
			stage++;
			break;
		case 0:
			sendOptionsDialogue("Select an Option",
					"What's going on here?",
					"What is your opinion of me?",
					"How can I improve my reputation?",
					"Where can I get armour like that?",
					"I'd like to reclaim my rewards.");
			break;
		case 1:
			switch(componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "What's going on here?");
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "What is your opinion of me?");
				stage = 10;
				return;
			case OPTION_3:
				sendPlayerDialogue(NORMAL, "How can I improve my reputation?");
				stage = 20;
				return;
			case OPTION_4:
				sendPlayerDialogue(NORMAL, "Where can I get armour like that?");
				stage = 30;
				return;
			case OPTION_5:
				sendPlayerDialogue(NORMAL, "I'd like to reclaim my rewards.");
				stage = 40;
				return;
			}
			break;
		case 2:
			sendNPCDialogue(NPC, NORMAL, "You find yourself in the Heart of Gielinor, "
					+ "adventurer. The Empty Lord watches over this vast cavern. "
					+ "Under his gaze we will lay waste to the minions of Seren, "
					+ "Zamorak and Sliske that oppose us.");
			break;
		case 3:
			sendNPCDialogue(NPC, NORMAL, "Commanding our forces from above is the mighty "
					+ "ilujanka Vindicta, riding atop her dragon Gorvek and "
					+ "instilling fear in our enemies.");
			break;
		case 4:
			sendPlayerDialogue(UNSURE, "Why are you all just stood here? Shouldn't you be fighting?");
			break;
		case 5:
			sendNPCDialogue(NPC, NORMAL, "We are not all savages. Upon this platform a "
					+ "truce has been established, allowing us - the envoys of each faction"
					+ " - to use words rather than violence to sway fresh recruits. "
					+ "For below the battle rages on,");
			break;
		case 6:
			sendNPCDialogue(NPC, NORMAL, "so we must do what we can to persuade "
					+ "the mercenaries that join the fray to fight for our cause. "
					+ "Take a look at what I have to offer. I assure you, I'll make it "
					+ "worth your while to fight for Zaros.");
			stage = 0;
			return;
		case 10:
			HeartOfGielinor.sendReputationInterface(player, HeartOfGielinor.ZAROS);
			end();
			break;
		case 20:
			sendNPCDialogue(NPC, NORMAL, "There are a few ways you can improve your standing. "
					+ "The easiest thing you can do is defeat our enemies, and use "
					+ "Ingression fragments to call in reinforcements for us. The bounty master "
					+ "over there, Feng, can also help you improve your standing by offering "
					+ "you bounties.");
			break;
		case 21:
			sendNPCDialogue(NPC, NORMAL, "Speak to him if you'd like to know more. Oh, and I'm sure "
					+ "if you could somehow find a way to defeat the other factions' generals that "
					+ "would certainly earn you a lot of favour.");
			stage = 0;
			return;
		case 30:
			sendNPCDialogue(NPC, NORMAL, "You can collect armour shards and crests around "
					+ "the Heart which can then be combined to make armour similar to my own. "
					+ "If you prove your worth to me, I can show you how to improve it "
					+ "further and unlock its true potential.");
			stage = 0;
			return;
		case 40:
			boolean rewardsToGive = false;
			if (!rewardsToGive) {//TODO If has no rewards to claim..
				sendNPCDialogue(NPC, NORMAL, "There's nothing I can give you.");
				if (option == 3)
					end();
				else
					stage = 0;
				return;
			} else {
				//TODO Give rewards.
				end();
			}
			break;
		case 50:
			sendOptionsDialogue("Select an Option", 
					"Yes. (Gain 500 reputation with Zaros)", "No.");
			break;
		case 51:
			if (componentId == OPTION_1)
				sendPlayerDialogue(NORMAL, "Yes.");
			else {
				sendPlayerDialogue(NORMAL, "No.");
				stage = 0;
				return;
			}
			break;
		case 52:
			sendNPCDialogue(NPC, NORMAL, "Thank you for your help.");
			//TODO Add reputation for Zaros.
			stage = 0;
			return;
			default:
				end();
				break;
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}
}
