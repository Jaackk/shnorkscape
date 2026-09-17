package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;

public class DFSSmithingD extends Dialogue {

	@Override
	public void start() {
		if (!player.getInventory().containsItem(11286, 1)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You will need a Draconic visage to do this!");
			return;
		}
		if (!player.getInventory().containsItem(1540, 1)) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You need an Anti-dragon shield to create a dragonfire shield!");
			return;
		}
		if (player.getSkills().getLevel(Skills.SMITHING) < 90) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You need a Smithing level of 90 to create this.");
			return;
		}
		if (!player.getInventory().containsOneItem(2347)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a hammer to work on the anvil!");
			return;
		}
		sendItemDialogue(11286, 1,
				"You set to work, trying to attach the ancient draconic visage to your "
						+ "anti-dragonbreath shield. It's not easy to work with the ancient artifact and it takes "
						+ "all of your skill as a master smith.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("How would you like to forge the shield?", "Forge towards Melee", "Forge towards Magic",
					"Forge towards Ranged");
			stage = 0;
			break;
		case 0:
			player.setNextAnimation(new Animation(898));
			player.getInventory().deleteItem(11286, 1);
			player.getInventory().deleteItem(1540, 1);
			player.getSkills().addXp(Skills.SMITHING, 2000);
			switch (componentId) {
			case OPTION_1:
				player.getInventory().addItem(11283, 1);
				// player.getCharges().addCharges(11283, 100,
				// Equipment.SLOT_SHIELD);
				sendItemDialogue(11283, 1,
						"Even for an expert armourer it is not an easy task, but eventually it is ready. "
								+ "You have crafted the draconic visage and anti-dragonbreath shield into a dragonfire shield.");
				break;
			case OPTION_2:
				player.getInventory().addItem(25558, 1);
				// player.getCharges().addCharges(25558, 100,
				// Equipment.SLOT_SHIELD);
				sendItemDialogue(25558, 1,
						"Even for an expert armourer it is not an easy task, but eventually it is ready. "
								+ "You have crafted the draconic visage and anti-dragonbreath shield into a dragonfire shield.");
				break;
			case OPTION_3:
				player.getInventory().addItem(25561, 1);
				// player.getCharges().addCharges(25561, 100,
				// Equipment.SLOT_SHIELD);
				sendItemDialogue(25561, 1,
						"Even for an expert armourer it is not an easy task, but eventually it is ready. "
								+ "You have crafted the draconic visage and anti-dragonbreath shield into a dragonfire shield.");
				break;
			}
			stage = 1;
			break;
		case 1:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}