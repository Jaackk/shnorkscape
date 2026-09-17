package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

import java.util.ArrayList;
import java.util.List;

public class KelharD extends Dialogue {

	private static final int NPC = 24169;
	private List<Integer> gems;
	private List<String> names;
	private int option;
	
	@Override
	public void start() {
		option = (int) parameters[0];
		if (option == 1) {
		sendOptionsDialogue("Select an Option",
				"What is in that cave?",
				"Can I enter the cave?",
				"I'd like to make a payment to enter the cave.");
		} else if (option == 2)
			checkGems();
		else if (option == 3) {
			if (player.getGemstoneKC() == 0)
				sendNPCDialogue(NPC, NORMAL, "I will not allow you to kill any dragons until further payments.");
			else
				sendNPCDialogue(NPC, NORMAL, "I will allow you to kill another " + player.getGemstoneKC() + " gemstone dragon" + (player.getGemstoneKC() == 1 ? "" : "s") +" before I will request further gem payments.");
			stage = 8;
		} else if (option == 4) {
			sendNPCDialogue(NPC, NORMAL, "I shall only let you through if a Slayer master has instructed you to do so... or if you are willing to make a payment");
			stage = 8;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			switch(componentId) {
			case OPTION_1:
				sendNPCDialogue(NPC, NORMAL, "Within that cave there are dragons unlike any you may have encountered before.");
				break;
			case OPTION_2:
				sendNPCDialogue(NPC, NORMAL, "I shall only let you through if a Slayer master has instructed you to do so... or if you are willing to make a payment");
				stage = 4;
				return;
			case OPTION_3:
				checkGems();
				return;
			}
			break;
		case 0:
			sendNPCDialogue(NPC, NORMAL, "Tipped with gems, they are stronger than the regular dragons you may have faced before.");
			break;
		case 1:
			sendNPCDialogue(NPC, NORMAL, "I guard this cave to protect those who may otherwise enter unaware.");
			break;
		case 2:
			sendOptionsDialogue("Select an Option", 
					"Let me face them.",
					"Thanks for letting me know.");
			break;
		case 3:
			switch(componentId) {
			case OPTION_1:
				sendNPCDialogue(NPC, NORMAL, "I shall only let you through if a Slayer master has instructed you to do so... or if you are willing to make a payment");
				break;
			case OPTION_2:
				end();
				return;
			}
			break;
		case 4:
			sendPlayerDialogue(NORMAL, "How much do you want?");
			break;
		case 5:
			sendNPCDialogue(NPC, NORMAL, "Not in your coins. In gems.");
			break;
		case 6:
			sendPlayerDialogue(NORMAL, "There are gems all around us. I'll mine some and be right back.");
			break;
		case 7:
			sendNPCDialogue(NPC, NORMAL, "No. I require some a bit more precious. Bring me uncut dragonstone, onyx or an incomplete hydrix and I shall allow you in. With each gem I will allow you to kill a certain amount before you must leave.");
			break;
		case 8:
			end();
			return;
		case 10:
			switch(componentId) {
			case OPTION_1:
				pay(gems, 0);
				return;
			case OPTION_2:
				pay(gems, 1);
				return;
			case OPTION_3:
				pay(gems, 2);
				return;
			}
			break;
		}
		stage++;
	}
	
	private final void checkGems() {
		gems = getDifferentGemsAmount();
		if (gems.size() == 0) {
			sendItemDialogue(1631, 1, "Kelhar will only accept uncut dragonstone, onyx and incomplete hydrix gems as payment to enter the cave.");
			stage = 8;
		} else if (gems.size() == 1)
			pay(gems, 0);
		else {
			names = new ArrayList<String>();
			if (gems.contains(1631))
				names.add("Dragonstones.");
			if (gems.contains(6571))
				names.add("Onyxes.");
			if (gems.contains(31851))
				names.add("Hydrixes.");
			sendOptionsDialogue("Which gems would you like to pay in?", 
					names.toArray(new String[names.size()]));
			stage = 10;
			return;
		}
	}
	
	private final void pay(List<Integer> gems, int option) {
		final String name = gems.contains(1631) && option == 0 ? "Dragonstones" : (gems.contains(6571) && gems.contains(1631) && option == 1 || gems.contains(6571) && !gems.contains(1631) && option == 0) ? "Onyxes" : "Hydrixes";
		player.sendInputInteger("How many " + name + " would you like to pay? ", new InputIntegerEvent() {
			@Override
			public void run(Player player) {
				final int total = getInteger();
				if (total <= 0) {
					end();
					return;
				}
				int amount = total;
				final int modifier = name.contains("Dragon") ? 2 : name.contains("Onyx") ? 400 : 4000;
				if (player.getGemstoneKC() + (amount * modifier) > 60000)
					amount = (60000 - player.getGemstoneKC()) / modifier;
				if (amount == 0) {
					end();
					player.sendMessage("You cannot store more than 60000 gemstone kills at once!");
					return;
				}
				final int unnoted = player.getInventory().getAmountOf(gems.get(option));
				player.getInventory().deleteItem(gems.get(option), amount);
				amount -= unnoted > amount ? amount : unnoted;
				final int noted = player.getInventory().getAmountOf(gems.get(option) + 1);
				player.getInventory().deleteItem(gems.get(option) + 1, amount);
				amount -= noted > amount ? amount : noted;
				final int paidAmount = total - amount;
				player.addGemstoneKC(paidAmount * modifier);
				sendItemDialogue(gems.get(option), 1, "You pay " + paidAmount + " " + (paidAmount > 1 ? name : name.substring(0, name.length() - 2)) + " to Kehlar to kill another " + (paidAmount * modifier) + " dragons.");
				stage = 8;
			}
		});
	}
	
	private final List<Integer> getDifferentGemsAmount() {
		List<Integer> gems = new ArrayList<Integer>();
		if (player.getInventory().containsOneItem(1631, 1632))
			gems.add(1631);
		if (player.getInventory().containsOneItem(6571, 6572))
			gems.add(6571);
		if (player.getInventory().containsOneItem(31851, 31852))
			gems.add(31851);
		return gems;
	}

	@Override
	public void finish() {
		
	}

}
