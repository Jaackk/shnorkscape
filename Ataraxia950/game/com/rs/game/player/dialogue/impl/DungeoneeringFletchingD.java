package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeoneeringSkillsDialogue;
import com.rs.game.player.content.dungeoneering.skills.fletching.DungeoneeringFletching;
import com.rs.game.player.content.dungeoneering.skills.fletching.DungeoneeringFletchingData;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class DungeoneeringFletchingD extends Dialogue {

	private DungeoneeringFletchingData items;

	@Override
	public void start() {
		items = (DungeoneeringFletchingData) parameters[0];
		DungeoneeringSkillsDialogue.sendSkillsDialogue(player, "Select the item you wish to fletch.", items.getProducts());
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int amount = DungeoneeringSkillsDialogue.getAmount(componentId);
		int option = DungeoneeringSkillsDialogue.getItemSlot(amount == 1 ? componentId : amount == 5 ? componentId + 1 : amount == 10 ? componentId + 2 : componentId + 3);
		if (option > items.getProducts().length) {
			end();
			return;
		}
		int invQuantity = player.getInventory().getItems().getNumberOf(items.getLogsId());
		if (amount > invQuantity)
			amount = invQuantity;
		if (amount > 25)
			amount = 25;
		if (amount == -1) {
			player.sendInputInteger("How many would you like to fletch?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					player.getActionManager().setAction(new DungeoneeringFletching(items, option, getInteger()));
				}
			});
			end();
			return;
		}
		player.getActionManager().setAction(new DungeoneeringFletching(items, option, amount));
		end();
	}

	@Override
	public void finish() {}

}