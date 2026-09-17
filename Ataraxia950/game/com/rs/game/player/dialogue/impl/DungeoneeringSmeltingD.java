package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeoneeringSkillsDialogue;
import com.rs.game.player.content.dungeoneering.skills.smithing.DungeoneeringSmelting;
import com.rs.game.player.content.dungeoneering.skills.smithing.DungeoneeringSmeltingData;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class DungeoneeringSmeltingD extends Dialogue {

	@Override
	public void start() {
		int[] items = new int[10];
		int x = 0;
		for (DungeoneeringSmeltingData d : DungeoneeringSmeltingData.values())
			items[x++] = d.getBarId();
		DungeoneeringSkillsDialogue.sendSkillsDialogue(player, "Select the bar you wish to smelt.", items);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int amount = DungeoneeringSkillsDialogue.getAmount(componentId);
		int option = DungeoneeringSkillsDialogue.getItemSlot(amount == 1 ? componentId : amount == 5 ? componentId + 1 : amount == 10 ? componentId + 2 : componentId + 3);
		if (option > 10) {
			end();
			return;
		}
		if (amount == -1) {
			player.sendInputInteger("How many would you like to smelt?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					player.getActionManager().setAction(new DungeoneeringSmelting(DungeoneeringSmeltingData.values()[option], getInteger()));
				}
			});
			end();
			return;
		}
		player.getActionManager().setAction(new DungeoneeringSmelting(DungeoneeringSmeltingData.values()[option], amount));
		end();
	}

	@Override
	public void finish() {}

}