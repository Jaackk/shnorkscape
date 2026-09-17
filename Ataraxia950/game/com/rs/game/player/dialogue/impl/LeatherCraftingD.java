package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.crafting.LeatherCrafting;
import com.rs.game.player.actions.crafting.LeatherCrafting.LeatherData;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

public class LeatherCraftingD extends Dialogue {
	
	private boolean portable;

	@Override
	public void start() {
		this.portable = (boolean) parameters[parameters.length - 1];
		int[] items = new int[parameters.length - 1];
		for (int i = 0; i < items.length; i++)
			items[i] = ((LeatherData) parameters[i]).getFinalProduct();

		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", 28, items, null);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int option = SkillsDialogue.getItemSlot(componentId);
		if (option > parameters.length || option < 0) {
			end();
			return;
		}
		LeatherData data = (LeatherData) parameters[option];
		if (data == null) {
			end();
			return;
		}
		int quantity = SkillsDialogue.getQuantity(player);
		int invQuantity = player.getInventory().getItems().getNumberOf(data.getLeatherId());
		if (quantity > invQuantity)
			quantity = invQuantity;
		player.getActionManager().setAction(new LeatherCrafting(data, quantity, portable));
		end();
	}

	@Override
	public void finish() {
	}
}