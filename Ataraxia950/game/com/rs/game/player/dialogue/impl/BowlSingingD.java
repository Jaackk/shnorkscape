package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.smithing.BowlSinging;
import com.rs.game.player.actions.smithing.BowlSinging.CrystalCreation;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author Tom
 * @date April 18, 2017
 */

public class BowlSingingD extends Dialogue {

	@SuppressWarnings("unused")
	private CrystalCreation crystal;

	@Override
	public void start() {
		sendOptionsDialogue("What sort of crystal item would you like to make?", "Regular Crystal Items.",
				"Attuned Crystal Items.");
		stage = 1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		this.crystal = (CrystalCreation) parameters[0];
		switch (stage) {
		case 1:
			switch (componentId) {
			case OPTION_1:
				SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE,
						"Choose how many you wish to make,<br>then click on the item to begin.",
						player.getInventory().getItems().getNumberOf(32622),
						new int[] { 32219, 32222, 32228, 32231, 32210, 32213, 32240, 32243, 32237 }, null);
				stage = 2;
				break;
			case OPTION_2:
				SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE,
						"Choose how many you wish to make,<br>then click on the item to begin.",
						player.getInventory().getItems().getNumberOf(32622),
						new int[] { 32647, 32649, 32651, 32653, 32655, 32657, 32659, 32627, 32629, 32631 }, null);
				stage = 3;
				break;
			}
			break;
		case 2:
			player.getActionManager().setAction(
					new BowlSinging(CrystalCreation.VALUES.get(componentId - 14), SkillsDialogue.getMaxQuantity(player)));
			end();
			break;
		case 3:
			player.getActionManager().setAction(
					new BowlSinging(CrystalCreation.VALUES.get(componentId - 5), SkillsDialogue.getMaxQuantity(player)));
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

}
