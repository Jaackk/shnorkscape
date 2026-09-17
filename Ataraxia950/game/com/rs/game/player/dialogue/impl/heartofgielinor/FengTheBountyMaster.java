package com.rs.game.player.dialogue.impl.heartofgielinor;

import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.dialogue.Dialogue;

public class FengTheBountyMaster extends Dialogue {

	private static final int NPC = 22437;
	private int option;
	private boolean bounty;
	
	@Override
	public void start() {
		option = (int) parameters[0];
		bounty = player.getHeart().getBountyFaction() != -1;
		if (option == 2) {
			sendPlayerDialogue(NORMAL, "Can you assign me a bounty?");
			stage = 1;
		} else if (option == 3) {
			if (!bounty) {
				sendNPCDialogue(NPC, NORMAL, "You do not have a bounty right now.");
				stage = 40;
			} else {
				if (player.getHeart().getCurrentBounty() == 0) {
					player.getHeart().handBountyIn();
					sendNPCDialogue(NPC, NORMAL, "Thank you for your service.");
					stage = 40;
				} else {
					sendNPCDialogue(NPC, NORMAL, "You haven't finished your bounty yet.");
					stage = 40;
				}
			}
		} else
			sendNPCDialogue(NPC, NORMAL, "Welcome, fellow mercenary, welcome.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			if (bounty)
				sendOptionsDialogue("Select an Option", 
						"Can you assign me a bounty?", 
						"How many bounties do I have available?", 
						"I'd like to abandon my current bounty.", 
						"Can you tell me more about your bounties?", 
						"Goodbye.");
			else
				sendOptionsDialogue("Select an Option", 
						"Can you assign me a bounty?", 
						"How many bounties do I have available?", 
						"Can you tell me more about your bounties?", 
						"Goodbye.");
			break;
		case 0:
			switch(componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Can you assign me a bounty?");
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "How many bounties do I have available?");
				stage = 10;
				return;
			case OPTION_3:
				if (bounty)
					sendPlayerDialogue(NORMAL, "I'd like to abandon my current bounty.");
				else
					sendPlayerDialogue(NORMAL, "Can you tell me more about your bounties?");
				stage = (byte) (bounty ? 20 : 30);
				return;
			case OPTION_4:
				if (bounty)
					sendPlayerDialogue(NORMAL, "Can you tell me more about your bounties?");
				else
					sendPlayerDialogue(NORMAL, "Goodbye.");
				stage = (byte) (bounty ? 30 : 40);
				return;
			case OPTION_5:
				sendPlayerDialogue(NORMAL, "Goodbye.");
				stage = 40;
				return;
			}
			break;
		case 1:
			if (!bounty)
				sendOptionsDialogue("Which faction do you fight for today, my friend?",
						"Sliske.",
						"Zamorak.",
						"Seren.",
						"Zaros");
			else {
				final int targetGod = player.getHeart().getBountyTargetFaction();
				final int god = player.getHeart().getBountyFaction();
				final String targetFaction = targetGod == HeartOfGielinor.SEREN ? "Seren" : targetGod == HeartOfGielinor.SLISKE ? "Sliske" : targetGod == HeartOfGielinor.ZAMORAK ? "Zamorak" : "Zaros";
				final String faction = god == HeartOfGielinor.SEREN ? "Seren" : god == HeartOfGielinor.SLISKE ? "Sliske" : god == HeartOfGielinor.ZAMORAK ? "Zamorak" : "Zaros";
				sendNPCDialogue(NPC, NORMAL, "You already have a bounty to slay those loyal to " + targetFaction + " in the name of " + faction + ".");
				stage = (byte) (option == 2 ? 40 : -1);
				return;
			}
			break;
		case 2:
			final int selectedGod = componentId == OPTION_1 ? HeartOfGielinor.SLISKE : componentId == OPTION_2 ? HeartOfGielinor.ZAMORAK : componentId == OPTION_3 ? HeartOfGielinor.SEREN : HeartOfGielinor.ZAROS;
			player.getHeart().setRandomBounty(selectedGod);
			final int targetGod = player.getHeart().getBountyTargetFaction();
			final int god = player.getHeart().getBountyFaction();
			final String targetFaction = targetGod == HeartOfGielinor.SEREN ? "Seren" : targetGod == HeartOfGielinor.SLISKE ? "Sliske" : targetGod == HeartOfGielinor.ZAMORAK ? "Zamorak" : "Zaros";
			final String faction = god == HeartOfGielinor.SEREN ? "Seren" : god == HeartOfGielinor.SLISKE ? "Sliske" : god == HeartOfGielinor.ZAMORAK ? "Zamorak" : "Zaros";
			bounty = player.getHeart().getBountyFaction() != -1;
			sendNPCDialogue(NPC, NORMAL, "Very well. Your bounty is to slay those loyal to " + targetFaction + " in the name of " + faction + ".");
			stage = (byte) (option == 2 ? 40 : -1);
			return;
		case 10:
			if (player.getHeart().getQuota() == 0) {
				sendNPCDialogue(NPC, NORMAL, "You have reached your quota. Check in tomorrow, I may have something in store for you then.");
				stage = -1;
				return;
			}
			if (!bounty)
				sendNPCDialogue(NPC, NORMAL, "I can assign you " + player.getHeart().getQuota() + " " + (player.getHeart().getQuota() == 1 ? "bounty" : "bounties") + " before you reach your quota. "
						+ "You can have one now, if you like?");
			else {
				sendNPCDialogue(NPC, NORMAL, "I can assign you " + player.getHeart().getQuota() + " " + (player.getHeart().getQuota() == 1 ? "bounty" : "bounties") + " before you reach your quota.");
				stage = -1;
				return;
			}
			break;
		case 11:
			sendOptionsDialogue("Select an Option", 
					"Yes, I'd like a bounty.", 
					"No, not right now.");
			break;
		case 12:
			switch(componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Yes, I'd like a bounty.");
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "No, not right now.");
				stage = -1;
				return;
			}
			break;
		case 13:
			sendOptionsDialogue("Which faction do you fight for today, my friend?",
					"Sliske.",
					"Zamorak.",
					"Seren.",
					"Zaros");
			stage = 2;
			return;
		case 20:
			sendNPCDialogue(NPC, NORMAL, "Hmm, abandon your bounty? I must warn you, "
					+ "it will still count towards your quota. Are you sure?");
			break;
		case 21:
			sendOptionsDialogue("Abandon your current bounty?",
					"Yes, abandon my bounty.",
					"No, keep my bounty.");
			break;
		case 22:
			switch(componentId) {
			case OPTION_1:
				player.getHeart().abandonBounty();
				sendDialogue("You abandon your bounty..");
				bounty = player.getHeart().getBountyFaction() != -1;
				stage = -1;
				return;
			case OPTION_2:
				if (bounty)
					sendOptionsDialogue("Select an Option", 
							"Can you assign me a bounty?", 
							"How many bounties do I have available?", 
							"I'd like to abandon my current bounty.", 
							"Can you tell me more about your bounties?", 
							"Goodbye.");
				else
					sendOptionsDialogue("Select an Option", 
							"Can you assign me a bounty?", 
							"How many bounties do I have available?", 
							"Can you tell me more about your bounties?", 
							"Goodbye.");
				stage = -1;
				return;
			}
			break;
		case 30:
			sendNPCDialogue(NPC, NORMAL, "I assign bounties to fellow mercenaries like yourself. "
					+ "This is a war, you see, and there's killin' to be done. The deals "
					+ "I cut with the faction representatives here mean you can pick one "
					+ "to do your bounties for, earning reputation with them in return.");
			break;
		case 31:
			sendPlayerDialogue(NORMAL, "What does a bounty entail?");
			break;
		case 32:
			sendNPCDialogue(NPC, NORMAL, "Well, killin' of course. One hundred kills of "
					+ "the faction I assign you, to be precise, but if you kill their "
					+ "general it counts as twenty-five! You pick the faction you fight "
					+ "for, I pick the faction you fight against, then away you go. Happy hunting.");
			break;
		case 33:
			sendPlayerDialogue(NORMAL, "So why are you even here in the first place?");
			break;
		case 34:
			sendNPCDialogue(NPC, NORMAL, "I'm a mercenary, much like anyone else in this war-torn place. 'Course I have my own nefarious agenda, but that's for me to know.");
			stage = -1;
			return;
		case 40:
			end();
			break;
		}
		stage++;
	}

	@Override
	public void finish() {}

}
