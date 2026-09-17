package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeoneeringSkillsDialogue;
import com.rs.game.player.content.dungeoneering.skills.herblore.DungeoneeringHerblore;
import com.rs.game.player.content.dungeoneering.skills.herblore.DungeoneeringHerbloreIngredients;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class DungeoneeringHerbloreD extends Dialogue {

	private DungeoneeringHerbloreIngredients items;
	
	@Override
	public void start() {
		items = (DungeoneeringHerbloreIngredients) parameters[0];
		DungeoneeringSkillsDialogue.sendSkillsDialogue(player, "Select the amount you wish to create.", new int[] { items.getItems()[2] });
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int amount = DungeoneeringSkillsDialogue.getAmount(componentId);
		int option = DungeoneeringSkillsDialogue.getItemSlot(amount == 1 ? componentId : amount == 5 ? componentId + 1 : amount == 10 ? componentId + 2 : componentId + 3);
		if (option > 1) {
			end();
			return;
		}
		if (amount == -1) {
			player.sendInputInteger("How many would you like to create?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					player.getActionManager().setAction(new DungeoneeringHerblore(items, getInteger()));
				}
			});
			end();
			return;
		}
		player.getActionManager().setAction(new DungeoneeringHerblore(items, amount));
		end();
	}

	@Override
	public void finish() {}

}