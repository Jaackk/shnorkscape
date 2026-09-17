package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeoneeringSkillsDialogue;
import com.rs.game.player.content.dungeoneering.skills.crafting.DungeoneeringCrafting;
import com.rs.game.player.content.dungeoneering.skills.crafting.DungeoneeringCraftingData;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class DungeoneeringCraftingD extends Dialogue {

	private DungeoneeringCraftingData items;

	@Override
	public void start() {
		items = (DungeoneeringCraftingData) parameters[0];
		DungeoneeringSkillsDialogue.sendSkillsDialogue(player, "Select the item you wish to craft.", items.getProducts());
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int amount = DungeoneeringSkillsDialogue.getAmount(componentId);
		int option = DungeoneeringSkillsDialogue.getItemSlot(amount == 1 ? componentId : amount == 5 ? componentId + 1 : amount == 10 ? componentId + 2 : componentId + 3);
		if (option > items.getProducts().length) {
			end();
			return;
		}
		int invQuantity = player.getInventory().getItems().getNumberOf(items.getCloth());
		if (amount > invQuantity)
			amount = invQuantity;
		if (amount > 25)
			amount = 25;
		if (amount == -1) {
			player.sendInputInteger("How many would you like to craft?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					player.getActionManager().setAction(new DungeoneeringCrafting(items, getInteger(), option));
				}
			});
			end();
			return;
		}
		player.getActionManager().setAction(new DungeoneeringCrafting(items, amount, option));
		end();
	}

	@Override
	public void finish() {}

}