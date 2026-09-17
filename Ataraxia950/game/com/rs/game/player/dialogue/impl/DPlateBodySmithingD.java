package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;

public class DPlateBodySmithingD extends Dialogue {

	@Override
	public void start() {
		if (!player.getInventory().containsItem(14472, 1) || !player.getInventory().containsItem(14474, 1)
				|| !player.getInventory().containsItem(14476, 1)) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You will need all three ruined dragon armour pieces in order to do this.");
			return;
		}
		if (player.getSkills().getLevel(Skills.SMITHING) < 92) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You will need a Smithing level of 92 in order to do this.");
			return;
		}
		if (!player.getInventory().containsOneItem(2347)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You will need a hammer to work on the anvil.");
			return;
		}
		sendItemDialogue(14479, 1,
				"You set to work, trying to attach all three platebody pieces "
						+ "together. It's not an easy to work with these damaged ancient artifacts and it takes "
						+ "all of your skill as a master smith..");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			player.setNextAnimation(new Animation(898));
			player.getInventory().deleteItem(14472, 1);
			player.getInventory().deleteItem(14474, 1);
			player.getInventory().deleteItem(14476, 1);
			player.getInventory().addItem(14479, 1);
			player.getSkills().addXp(Skills.SMITHING, 2000);
			sendItemDialogue(14479, 1,
					"Even for an expert armourer it is not an easy task, but eventually it is ready. "
							+ "You have crafted all three ruined dragon armour pieces into a dragon platebody.");
			stage = 0;
			break;
		case 0:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}