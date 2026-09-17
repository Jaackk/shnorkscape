package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author _jordan
 */
public class PetPerksD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("You must complete this tutorial in order to use this item on your pet.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			sendOptionsDialogue("What would you like to do?", "Start Tutorial", "Skip Tutorial");
			stage = 0;
		} else if (stage == 0) {
			if (componentId == OPTION_1) {//conitnue dialogue
				sendDialogue("Pet perks allow you to give more purpose to your companions while adventuring in Ataraxia.");
				stage = 1;
			} else if (componentId == OPTION_2) {//end dialogue
				player.getPetPerkManager().setDidTutorial(true);
				end();
			}
		} else if(stage == 1) {
			sendDialogue("There are many perks you can equip to each of your pets, ", "which each provide different buffs in exchange for gameplay debuffs.");
			stage = 2;
		} else if (stage == 2) {
			sendDialogue("Pet perks allow up to 5 slots of perks you can equip to each pet and each perk can be upgraded in up to 3 tiers.");
			stage = 3;
		} else if (stage == 3) {
			sendDialogue("According to your player information, you are allowed up to " + player.getPetPerkManager().getNumberOfSlotsCanUse() + " slot perks equipped to any one pet.");
			stage = 4;
		} else if (stage == 4) {
			sendDialogue("The upgrading system is very simple. If you use the same perk on a pet more than once, the current tier of that perk will upgrade to the next one.", "For example...");
			stage = 5;
		} else if (stage == 5) {
			sendDialogue("Nice but Dim", "", "Tier 1: Increases your drop rate 5% and decreases your experience gain 10%.");
			stage = 6;
		} else if (stage == 6) {
			sendDialogue("Nice but Dim", "", "Tier 2: Increases your drop rate 10% and decreases your experience gain 5%.");
			stage = 7;
		} else if (stage == 7) {
			sendDialogue("Nice but Dim", "", "Tier 3: Increases your drop rate 15% and decreases your experience gain 3%.");
			stage = 8;
		} else if (stage == 8) {
			sendDialogue("You can view your pet perk information by examining your pet companinion while it is following you.");
			stage = 9;
		} else if (stage == 9) {
			sendOptionsDialogue("Repeat tutorial?", "Yes.", "No.");
			stage = 10;
		} else if (stage == 10) {
			if (componentId == OPTION_1) {//restart dialogue to beginning
				sendDialogue("Pet perks allow you to give more purpose to your companions while adventuring in Ataraxia.");
				stage = 1;
			} else if (componentId == OPTION_2) {//end dialogue
				player.getPetPerkManager().setDidTutorial(true);
				end();
			}
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
