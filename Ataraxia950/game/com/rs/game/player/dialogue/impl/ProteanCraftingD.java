package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.protean.ProteanCrafting;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

public class ProteanCraftingD extends Dialogue {

	private int itemId, amount;
	private boolean portable;

	@Override
	public void finish() {
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getActionManager().setAction(new ProteanCrafting(amount, portable));
		end();
	}

	@Override
	public void start() {
		this.itemId = (int) parameters[0];
		this.amount = (int) parameters[1];
		this.portable = (boolean) parameters[2];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", player.getInventory().getItems().getNumberOf(itemId), new int[] { itemId }, null);

	}

}
