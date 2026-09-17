package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class AttuneGemstoneArmourD extends Dialogue {

	private int type;
	
	@Override
	public void start() {
		sendOptionsDialogue("Attune armour",
				"Dragon bolts",
				"Onyx bolts",
				"Ascendri bolts.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			switch(componentId) {
			case OPTION_1:
				sendItemDialogue(9244, 100, "Attuning your armour to dragon bolts gives you the effect of occasionally dealing dragonfire damage against your target. Are you sure you wish to attune to dragon bolts?");
				type = 0;
				break;
			case OPTION_2:
				sendItemDialogue(9245, 100, "Attuning your armour to onyx bolts gives you the effect of occasionally healing 25% lifepoints based on the maximum damage of your attack. Are you sure you wish to attune to onyx bolts?");
				type = 1;
				break;
			case OPTION_3:
				sendItemDialogue(31881, 100, "Attuning your armour to ascendri bolts gives you the effect of occasionally restoring one percent of special attack energy per attack for 15 seconds. Are you sure you wish to attune to ascendri bolts?");
				type = 2;
				break;
			}
			break;
		case 0:
			sendOptionsDialogue("Attune to " + (type == 0 ? "dragon bolts" : type == 1 ? "onyx bolts" : "ascendri bolts") + "?",
					"Yes.",
					"No.",
					"Return to previous selection.");
			break;
		case 1:
			switch(componentId) {
			case OPTION_1:
				switch(type) {
				case 0:
					sendItemDialogue(9244, 100, "Your armour is now attuned to the following bolt:<br>Dragon bolts (e)<br>You have " + player.getGemstoneArmour().getCharges(0) + " charges.");
					player.getGemstoneArmour().setAttuned(0);
					player.sendMessage(Colors.YELLOW + "Your armour is now attuned to the following bolt: Dragon bolts (e)");
					break;
				case 1:
					sendItemDialogue(9245, 100, "Your armour is now attuned to the following bolt:<br>Onyx bolts (e)<br>You have " + player.getGemstoneArmour().getCharges(1) + " charges.");
					player.getGemstoneArmour().setAttuned(1);
					player.sendMessage(Colors.YELLOW + "Your armour is now attuned to the following bolt: Onyx bolts (e)");
					break;
				case 2:
					sendItemDialogue(31881, 100, "Your armour is now attuned to the following bolt:<br>Ascendri bolts (e)<br>You have " + player.getGemstoneArmour().getCharges(2) + " charges.");
					player.getGemstoneArmour().setAttuned(2);
					player.sendMessage(Colors.YELLOW + "Your armour is now attuned to the following bolt: Ascendri bolts (e)");
					break;
				}
				break;
			case OPTION_2:
				end();
				return;
			case OPTION_3:
				stage = -1;
				start();
				return;
			}
			break;
		case 2:
			end();
			return;
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
