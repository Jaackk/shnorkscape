package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.protean.ProteanSmithing;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

public class ProteanSmithingD extends Dialogue {

	private int amount;
	private boolean portable;

	@Override
	public void finish() {
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot >= 6 || slot < 0)
			return;
		player.getActionManager().setAction(new ProteanSmithing(31351 + slot, amount, portable));
		end();
	}

	@Override
	public void start() {
		this.amount = (int) parameters[0];
		this.portable = (boolean) parameters[1];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to smith,<br>then click on the item to begin.", player.getInventory().getItems().getNumberOf(31350), new int[] { 31351, 31352, 31353, 31354, 31355, 31356 }, null);

	}

}
