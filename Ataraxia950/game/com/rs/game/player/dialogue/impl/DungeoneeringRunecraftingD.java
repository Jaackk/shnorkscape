package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeoneeringSkillsDialogue;
import com.rs.game.player.content.dungeoneering.skills.runecrafting.DungeoneeringRunecrafting;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class DungeoneeringRunecraftingD extends Dialogue {

	private int[] items;

	@Override
	public void start() {
		items = (int[]) parameters[0];
		DungeoneeringSkillsDialogue.sendSkillsDialogue(player, items[0] == 27707 ? "Select the weapon you wish to imbue." : "Select the runes you wish to runecraft.", items);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int amount = DungeoneeringSkillsDialogue.getAmount(componentId);
		int option = DungeoneeringSkillsDialogue.getItemSlot(amount == 1 ? componentId : amount == 5 ? componentId + 1 : amount == 10 ? componentId + 2 : componentId + 3);
		if (option > items.length) {
			end();
			return;
		}
		if (amount == -1) {
			player.sendInputInteger("How many would you like to Runecraft?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					player.getActionManager().setAction(new DungeoneeringRunecrafting(DungeoneeringRunecrafting.getData(items[option]), getInteger()));
				}
			});
			end();
			return;
		}
		player.getActionManager().setAction(new DungeoneeringRunecrafting(DungeoneeringRunecrafting.getData(items[option]), amount));
		end();
	}

	@Override
	public void finish() {}

}