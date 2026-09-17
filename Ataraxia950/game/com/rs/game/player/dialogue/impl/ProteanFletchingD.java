package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.protean.ProteanFletching;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

public class ProteanFletchingD extends Dialogue {

	private int amount;
	private boolean portable;

	@Override
	public void finish() {
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot >= 2 || slot < 0)
			return;
		player.getActionManager().setAction(new ProteanFletching(amount, slot == 0, portable));
		end();
	}

	@Override
	public void start() {
		this.amount = (int) parameters[0];
		this.portable = (boolean) parameters[1];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the action you wish to perform.", player.getInventory().getItems().getNumberOf(34528), new int[] { 34529, 34531 }, null);

	}

}
