package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.lottery.LotteryManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

public class GamblerD extends Dialogue {

	private static final int NPC = 2998;
	private static final int COST = 50_000_000;
	
	@Override
	public void start() {
		sendNPCDialogue(NPC, NORMAL, "Hello there, " + player.getDisplayName() + ".<br>What can I do for you on this fine day?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			sendOptionsDialogue("Select an Option",
					"Who are you?",
					"I'd like to purchase a lottery ticket.",
					"How can I obtain tickets for the lottery?",
					"How many tickets may I purchase?",
					"What is the current pot at?");
			break;
		case 0:
			switch(componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Who are you?");
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "I'd like to purchase a lottery ticket.");
				stage = 10;
				return;
			case OPTION_3:
				sendPlayerDialogue(NORMAL, "How can I obtain tickets for the lottery?");
				stage = 20;
				return;
			case OPTION_4:
				sendPlayerDialogue(NORMAL, "How many tickets may I purchase?");
				stage = 30;
				return;
			case OPTION_5:
				sendPlayerDialogue(NORMAL, "What is the current pot at?");
				stage = 40;
				return;
			}
			break;
		case 1:
			sendNPCDialogue(NPC, NORMAL, "I am the official lottery host of Ataraxia.");
			break;
		case 2:
			sendNPCDialogue(NPC, NORMAL, "I host the weekly lottery, which is held on Fridays.");
			break;
		case 3:
			sendNPCDialogue(NPC, NORMAL, "At the end of the week, three winners of the lottery will be picked. The winners will receive 50%, 25% & 10% of the pot respectively based on their place on the lottery.");
			stage = -1;
			return;
		case 10:
			if (LotteryManager.canPurchaseTicket(player))
				sendNPCDialogue(NPC, NORMAL, "Sure thing. What currency would you like to purchase the ticket for?");
			else {
				sendNPCDialogue(NPC, NORMAL, "I'm sorry but it appears you've reached your quota for the lottery.");
				stage = -1;
				return;
			}
			break;
		case 11:
			sendOptionsDialogue("What currency would you like to use?",
					"Purchase for gold pieces.",
					"Purchase for vote points.");
			break;
		case 12:
			switch(componentId) {
			case OPTION_1:
				if (player.hasMoney(COST)) {
					player.takeMoney(COST);
					sendItemDialogue(995, COST, "You hand " + Utils.formatNumber(COST) + " gold pieces to the gambler.");
				} else {
					sendNPCDialogue(NPC, NORMAL, "It appears you haven't got enough money to cover the ticket! You need at least " + Utils.formatNumber(COST) + " gold pieces to purchase it.");
					stage = 100;
					return;
				}
				break;
			case OPTION_2:
				if (player.getVotePoints() >= 50) {
					player.setVotePoints(player.getVotePoints() - 50);
					sendItemDialogue(31187, 1, "You hand 50 vote points to the gambler.");
				} else {
					sendNPCDialogue(NPC, NORMAL, "It appears you haven't got enough vote points to cover the ticket! You need at least 50 vote points to purchase it.");
					stage = 100;
					return;
				}
				break;
			}
			break;
		case 13:
			sendNPCDialogue(NPC, NORMAL, "Pleasure doing business with you!");
			stage = 100;
			return;
		case 20:
			sendNPCDialogue(NPC, NORMAL, "There are two ways to purchase a ticket. You can either pay " + Utils.formatNumber(COST) + " gold pieces or 50 vote points for one ticket.");
			stage = -1;
			return;
		case 30:
			sendNPCDialogue(NPC, NORMAL, "You can purchase a total of " + LotteryManager.getMaximumTicketsAmount(player) + " lottery tickets. You have so far bought " + LotteryManager.getTickets(player) + " lottery tickets.");
			stage = -1;
			return;
		case 40:
			sendNPCDialogue(NPC, NORMAL, "The current pot is at " + Utils.formatNumber(LotteryManager.getPot()) + " gold pieces.");
			stage = -1;
			return;
		case 100:
			end();
			return;
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
